package ru.max.bot.longpolling;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import ru.max.bot.MaxBotOptions;

public class LongPollingBotOptions extends MaxBotOptions {
    public static final LongPollingBotOptions DEFAULT = new LongPollingBotOptions(30, null, null, true);

    private final int requestTimeout;
    private final Integer limit;
    private final boolean shouldRemoveWebhook;

    public LongPollingBotOptions(int requestTimeout, @Nullable Integer limit, @Nullable Set<String> updateTypes,
                                 boolean shouldRemoveWebhook) {
        super(updateTypes);
        this.requestTimeout = requestTimeout;
        this.limit = limit;
        this.shouldRemoveWebhook = shouldRemoveWebhook;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public boolean shouldRemoveWebhook() {
        return shouldRemoveWebhook;
    }
}
