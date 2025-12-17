/*
 * Copyright 2019 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Michaili K.
 */
public class ForceRemoveCmd extends DJCommand
{
    public ForceRemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "forceremove";
        this.help = "removes all entries by a user from the queue";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        if (event.getArgs().isEmpty())
        {
            event.replyError("You need to mention a user!");
            return;
        }

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty())
        {
            event.replyError("There is nothing in the queue!");
            return;
        }


        User target;
        List<Member> found = FinderUtil.findMembers(event.getArgs(), event.getGuild());

        if(found.isEmpty())
        {
            event.replyError("Unable to find the user!");
            return;
        }
        else if(found.size()>1)
        {
            int max = Math.min(4, found.size());
            StringBuilder sb = new StringBuilder();
            sb.append(event.getClient().getWarning()).append(" Found multiple users:\n");
            for(int i=0; i<max; i++)
            {
                Member member = found.get(i);
                sb.append("`").append(i+1).append("` **")
                        .append(FormatUtil.filter(member.getUser().getName()))
                        .append("**#").append(member.getUser().getDiscriminator())
                        .append("\n");
            }
            sb.append("\nType a number `1-").append(max).append("` to select, or `cancel`.");

            event.reply(sb.toString());

            bot.getWaiter().waitForEvent(
                    MessageReceivedEvent.class,
                    e -> !e.getAuthor().isBot()
                            && e.getAuthor().equals(event.getAuthor())
                            && e.getChannel().equals(event.getChannel()),
                    e -> {
                        String content = e.getMessage().getContentRaw().trim();
                        if(content.equalsIgnoreCase("cancel"))
                            return;
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
                        removeAllEntries(found.get(selection - 1).getUser(), event);
                    },
                    1, TimeUnit.MINUTES,
                    () -> {}
            );
            return;
        }
        else
        {
            target = found.get(0).getUser();
        }

        removeAllEntries(target, event);

    }

    private void removeAllEntries(User target, CommandEvent event)
    {
        int count = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue().removeAll(target.getIdLong());
        if (count == 0)
        {
            event.replyWarning("**"+target.getName()+"** doesn't have any songs in the queue!");
        }
        else
        {
            event.replySuccess("Successfully removed `"+count+"` entries from "+FormatUtil.formatUsername(target)+".");
        }
    }
}
