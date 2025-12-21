/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.LoadErrorUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SearchCmd extends MusicCommand 
{
    protected String searchPrefix = "ytsearch:";
    private final String searchingEmoji;
    
    public SearchCmd(Bot bot)
    {
        super(bot);
        this.searchingEmoji = bot.getConfig().getSearching();
        this.name = "search";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.arguments = "<query>";
        this.help = "searches Youtube for a provided query";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }
    @Override
    public void doCommand(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please include a query.");
            return;
        }
        event.reply(searchingEmoji+" Searching... `["+event.getArgs()+"]`", 
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), searchPrefix + event.getArgs(), new ResultHandler(m,event)));
    }
    
    private class ResultHandler implements AudioLoadResultHandler 
    {
        private final Message m;
        private final CommandEvent event;
        
        private ResultHandler(Message m, CommandEvent event)
        {
            this.m = m;
            this.event = event;
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
        {
            if(bot.getConfig().isTooLong(track))
            {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                        + TimeUtil.formatTime(track.getDuration())+"` > `"+bot.getConfig().getMaxTime()+"`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event)))+1;
            m.editMessage(FormatUtil.filter(event.getClient().getSuccess()+" Added **"+track.getInfo().title
                    +"** (`"+ TimeUtil.formatTime(track.getDuration())+"`) "+(pos==0 ? "to begin playing"
                        : " to the queue at position "+pos))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            int max = Math.min(4, playlist.getTracks().size());
            if(max == 0)
            {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" No results found for `"+event.getArgs()+"`.")).queue();
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(FormatUtil.filter(event.getClient().getSuccess()+" Search results for `"+event.getArgs()+"`:")).append("\n");
            for(int i=0; i<max; i++)
            {
                AudioTrack track = playlist.getTracks().get(i);
                sb.append("`").append(i+1).append("` ")
                        .append("`[").append(TimeUtil.formatTime(track.getDuration())).append("]` ")
                        .append("**").append(FormatUtil.filter(track.getInfo().title)).append("**\n");
            }
            sb.append("\nType a number `1-").append(max).append("` to select, or `cancel`.");

            m.editMessage(sb.toString()).queue();

            bot.getWaiter().waitForEvent(
                    MessageReceivedEvent.class,
                    e -> !e.getAuthor().isBot()
                            && e.getAuthor().equals(event.getAuthor())
                            && e.getChannel().equals(event.getChannel()),
                    e -> {
                        String content = e.getMessage().getContentRaw().trim();
                        if(content.equalsIgnoreCase("cancel"))
                        {
                            m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" Search cancelled.")).queue();
                            return;
                        }
                        int selection;
                        try
                        {
                            selection = Integer.parseInt(content);
                        }
                        catch(NumberFormatException ex)
                        {
                            return;
                        }
                        if(selection < 1 || selection > max)
                            return;

                        AudioTrack track = playlist.getTracks().get(selection - 1);
                        if(bot.getConfig().isTooLong(track))
                        {
                            m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                                    + TimeUtil.formatTime(track.getDuration())+"` > `"+bot.getConfig().getMaxTime()+"`")).queue();
                            return;
                        }
                        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
                        int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event)))+1;
                        m.editMessage(FormatUtil.filter(event.getClient().getSuccess()+" Added **" + track.getInfo().title
                                + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) " + (pos==0 ? "to begin playing"
                                : " to the queue at position "+pos))).queue();
                    },
                    1, TimeUnit.MINUTES,
                    () -> m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" Search timed out.")).queue()
            );
        }

        @Override
        public void noMatches() 
        {
            m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" No results found for `"+event.getArgs()+"`.")).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) 
        {
            if(LoadErrorUtil.isAgeRestricted(throwable) && !bot.getConfig().useYoutubeOauth2())
            {
                m.editMessage(FormatUtil.filter(event.getClient().getError()
                        +" This video is age-restricted. Run `"+event.getClient().getPrefix()
                        +"auth` (owner only) to link YouTube and retry.")).queue();
                return;
            }
            if(throwable.severity==Severity.COMMON)
                m.editMessage(event.getClient().getError()+" Error loading: "+throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError()+" Error loading track.").queue();
        }
    }
}
