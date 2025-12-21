/*
 * Copyright 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.jagrosh.jmusicbot.utils;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import java.util.Locale;

public final class LoadErrorUtil
{
    private LoadErrorUtil() {}

    public static boolean isAgeRestricted(FriendlyException ex)
    {
        if(ex == null)
            return false;
        if(messageLooksAgeRestricted(ex.getMessage()))
            return true;
        Throwable cause = ex.getCause();
        while(cause != null)
        {
            if(messageLooksAgeRestricted(cause.getMessage()))
                return true;
            cause = cause.getCause();
        }
        return false;
    }

    private static boolean messageLooksAgeRestricted(String message)
    {
        if(message == null || message.isEmpty())
            return false;
        String text = message.toLowerCase(Locale.ROOT);
        return text.contains("age restricted")
                || text.contains("age-restricted")
                || text.contains("confirm your age")
                || text.contains("sign in to confirm your age")
                || text.contains("inappropriate for some users")
                || text.contains("you must be 18");
    }
}
