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
package com.jagrosh.jdautilities.commons.waiter;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.utils.Checks;

/**
 * Minimal JDA 5+ EventWaiter used by JMusicBot menus.
 *
 * <p>This is a trimmed-down subset of the original JDA-Utilities EventWaiter,
 * updated for JDA 5+. It supports waiting for a single event type with a predicate,
 * an action, and an optional timeout.
 */
public class EventWaiter implements EventListener
{
    private final ScheduledExecutorService threadpool;
    private final boolean shutdownAutomatically;
    private final Set<WaitingEvent<?>> waiting = ConcurrentHashMap.newKeySet();

    public EventWaiter()
    {
        this(Executors.newSingleThreadScheduledExecutor(), true);
    }

    public EventWaiter(ScheduledExecutorService threadpool, boolean shutdownAutomatically)
    {
        this.threadpool = Objects.requireNonNull(threadpool, "threadpool");
        this.shutdownAutomatically = shutdownAutomatically;
    }

    @Override
    public void onEvent(GenericEvent event)
    {
        if(shutdownAutomatically && event instanceof ShutdownEvent)
        {
            shutdown();
            return;
        }

        Iterator<WaitingEvent<?>> it = waiting.iterator();
        while(it.hasNext())
        {
            WaitingEvent<?> w = it.next();
            if(w.tryAccept(event))
            {
                it.remove();
                w.cancelTimeout();
            }
        }
    }

    public void shutdown()
    {
        threadpool.shutdownNow();
        waiting.clear();
    }

    public <T extends GenericEvent> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action)
    {
        waitForEvent(classType, condition, action, -1, null, null);
    }

    public <T extends GenericEvent> void waitForEvent(
            Class<T> classType,
            Predicate<T> condition,
            Consumer<T> action,
            long timeout,
            TimeUnit unit,
            Runnable timeoutAction
    )
    {
        Checks.check(classType != null, "classType");
        Checks.check(condition != null, "condition");
        Checks.check(action != null, "action");

        WaitingEvent<T> waitingEvent = new WaitingEvent<>(classType, condition, action);
        if(timeout > 0 && unit != null)
        {
            ScheduledFuture<?> future = threadpool.schedule(() -> {
                waiting.remove(waitingEvent);
                if(timeoutAction != null)
                    timeoutAction.run();
            }, timeout, unit);
            waitingEvent.setTimeoutFuture(future);
        }
        waiting.add(waitingEvent);
    }

    private static final class WaitingEvent<T extends GenericEvent>
    {
        private final Class<T> type;
        private final Predicate<T> condition;
        private final Consumer<T> action;
        private volatile ScheduledFuture<?> timeoutFuture;

        private WaitingEvent(Class<T> type, Predicate<T> condition, Consumer<T> action)
        {
            this.type = type;
            this.condition = condition;
            this.action = action;
        }

        private void setTimeoutFuture(ScheduledFuture<?> future)
        {
            this.timeoutFuture = future;
        }

        private void cancelTimeout()
        {
            ScheduledFuture<?> f = timeoutFuture;
            if(f != null)
                f.cancel(false);
        }

        private boolean tryAccept(GenericEvent event)
        {
            if(!type.isInstance(event))
                return false;
            T casted = type.cast(event);
            if(!condition.test(casted))
                return false;
            action.accept(casted);
            return true;
        }
    }
}

