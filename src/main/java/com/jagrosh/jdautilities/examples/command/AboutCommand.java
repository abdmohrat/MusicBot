/*
 * Copyright 2016 John Grosh (jagrosh).
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
package com.jagrosh.jdautilities.examples.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.util.StringJoiner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;

/**
 * Lightweight about command used by JMusicBot.
 *
 * <p>This intentionally avoids the old JDA-Utilities documentation/annotation modules.
 */
public class AboutCommand extends Command
{
    private boolean isAuthor = true;
    private String replacementIcon = "+";
    private final Color color;
    private final String description;
    private final Permission[] recommendedPerms;
    private final String[] features;

    public AboutCommand(Color color, String description, String[] features, Permission... recommendedPerms)
    {
        this.color = color;
        this.description = description;
        this.features = features == null ? new String[0] : features;
        this.recommendedPerms = recommendedPerms == null ? new Permission[0] : recommendedPerms;

        this.name = "about";
        this.help = "shows info about the bot";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    public void setIsAuthor(boolean value)
    {
        this.isAuthor = value;
    }

    public void setReplacementCharacter(String value)
    {
        if(value != null && !value.isBlank())
            this.replacementIcon = value;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : color);
        eb.setAuthor("About " + event.getSelfUser().getName(), null, event.getSelfUser().getEffectiveAvatarUrl());

        String owner = event.getClient().getOwnerId();
        User ownerUser = event.getJDA().getUserById(owner);
        String ownerDisplay = ownerUser == null ? "<@" + owner + ">" : ownerUser.getName();

        StringJoiner featureLines = new StringJoiner("\n");
        for(String f : features)
            featureLines.add((event.getClient().getSuccess().startsWith("<") ? replacementIcon : event.getClient().getSuccess()) + " " + f);

        String prefix = event.getClient().getTextualPrefix();
        String helpWord = event.getClient().getHelpWord();

        String desc = "Hello! I am **" + event.getSelfUser().getName() + "**, " + description
                + "\nI " + (isAuthor ? "was written in Java" : "am owned") + " by **" + ownerDisplay + "**."
                + "\nRunning [JDA](" + JDAInfo.GITHUB + ") **" + JDAInfo.VERSION + "**."
                + "\nType `" + prefix + helpWord + "` to see commands."
                + (features.length > 0 ? "\n\nSome features:\n```" + "\n" + featureLines + "\n```" : "");

        eb.setDescription(desc);
        event.reply(eb.build());
    }
}

