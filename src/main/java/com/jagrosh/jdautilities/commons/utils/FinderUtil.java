/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 * Minimal entity finder utilities used by JMusicBot.
 *
 * <p>This is a trimmed-down subset of the original JDA-Utilities FinderUtil, updated for JDA 5+.
 */
public final class FinderUtil
{
    private static final Pattern DISCORD_ID = Pattern.compile("^\\d{17,20}$");
    private static final Pattern MENTION_ROLE = Pattern.compile("^<@&(\\d{17,20})>$");
    private static final Pattern MENTION_CHANNEL = Pattern.compile("^<#(\\d{17,20})>$");
    private static final Pattern MENTION_USER = Pattern.compile("^<@!?(\\d{17,20})>$");

    private FinderUtil() {}

    public static List<Role> findRoles(String query, Guild guild)
    {
        if(query == null || query.isBlank() || guild == null)
            return Collections.emptyList();

        Matcher mention = MENTION_ROLE.matcher(query.trim());
        if(mention.matches())
        {
            Role role = guild.getRoleById(mention.group(1));
            return role == null ? Collections.emptyList() : Collections.singletonList(role);
        }

        if(DISCORD_ID.matcher(query.trim()).matches())
        {
            Role role = guild.getRoleById(query.trim());
            if(role != null)
                return Collections.singletonList(role);
        }

        return matchByName(query, guild.getRoles(), Role::getName);
    }

    public static List<TextChannel> findTextChannels(String query, Guild guild)
    {
        if(query == null || query.isBlank() || guild == null)
            return Collections.emptyList();

        Matcher mention = MENTION_CHANNEL.matcher(query.trim());
        if(mention.matches())
        {
            TextChannel channel = guild.getTextChannelById(mention.group(1));
            return channel == null ? Collections.emptyList() : Collections.singletonList(channel);
        }

        if(DISCORD_ID.matcher(query.trim()).matches())
        {
            TextChannel channel = guild.getTextChannelById(query.trim());
            if(channel != null)
                return Collections.singletonList(channel);
        }

        return matchByName(query, guild.getTextChannels(), TextChannel::getName);
    }

    public static List<VoiceChannel> findVoiceChannels(String query, Guild guild)
    {
        if(query == null || query.isBlank() || guild == null)
            return Collections.emptyList();

        if(DISCORD_ID.matcher(query.trim()).matches())
        {
            VoiceChannel channel = guild.getVoiceChannelById(query.trim());
            if(channel != null)
                return Collections.singletonList(channel);
        }

        return matchByName(query, guild.getVoiceChannels(), VoiceChannel::getName);
    }

    public static List<Member> findMembers(String query, Guild guild)
    {
        if(query == null || query.isBlank() || guild == null)
            return Collections.emptyList();

        String q = query.trim();

        Matcher mention = MENTION_USER.matcher(q);
        if(mention.matches())
        {
            Member member = guild.getMemberById(mention.group(1));
            return member == null ? Collections.emptyList() : Collections.singletonList(member);
        }

        if(DISCORD_ID.matcher(q).matches())
        {
            Member member = guild.getMemberById(q);
            if(member != null)
                return Collections.singletonList(member);
        }

        // If the user provided `name#1234`, attempt exact match on full tag first.
        if(q.contains("#"))
        {
            String tagLower = q.toLowerCase(Locale.ROOT);
            List<Member> exact = new ArrayList<>();
            for(Member m : guild.getMembers())
            {
                String discrim = m.getUser().getDiscriminator();
                if(discrim != null && !discrim.equals("0000"))
                {
                    String tag = (m.getUser().getName() + "#" + discrim).toLowerCase(Locale.ROOT);
                    if(tag.equals(tagLower))
                        exact.add(m);
                }
            }
            if(!exact.isEmpty())
                return Collections.unmodifiableList(exact);
        }

        // Otherwise match by effective name or username
        String lower = q.toLowerCase(Locale.ROOT);
        List<Member> exact = new ArrayList<>();
        List<Member> contains = new ArrayList<>();
        for(Member m : guild.getMembers())
        {
            String effective = m.getEffectiveName().toLowerCase(Locale.ROOT);
            String username = m.getUser().getName().toLowerCase(Locale.ROOT);
            if(effective.equals(lower) || username.equals(lower))
                exact.add(m);
            else if(effective.contains(lower) || username.contains(lower))
                contains.add(m);
        }
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        return Collections.unmodifiableList(contains);
    }

    private static <T> List<T> matchByName(String query, List<T> items, java.util.function.Function<T, String> nameFn)
    {
        String q = query.trim();
        String lower = q.toLowerCase(Locale.ROOT);
        List<T> exact = new ArrayList<>();
        List<T> wrongCase = new ArrayList<>();
        List<T> startsWith = new ArrayList<>();
        List<T> contains = new ArrayList<>();

        for(T item : items)
        {
            String name = nameFn.apply(item);
            if(name.equals(q))
                exact.add(item);
            else if(name.equalsIgnoreCase(q) && exact.isEmpty())
                wrongCase.add(item);
            else if(name.toLowerCase(Locale.ROOT).startsWith(lower) && wrongCase.isEmpty())
                startsWith.add(item);
            else if(name.toLowerCase(Locale.ROOT).contains(lower) && startsWith.isEmpty())
                contains.add(item);
        }

        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongCase.isEmpty())
            return Collections.unmodifiableList(wrongCase);
        if(!startsWith.isEmpty())
            return Collections.unmodifiableList(startsWith);
        return Collections.unmodifiableList(contains);
    }
}

