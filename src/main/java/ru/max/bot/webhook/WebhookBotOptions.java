package ru.max.bot.webhook;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import ru.max.bot.MaxBotOptions;

public class WebhookBotOptions extends MaxBotOptions {
    public static final WebhookBotOptions DEFAULT = new WebhookBotOptions(null);

    private boolean shouldRemoveOldSubscriptions;
    private boolean shouldRemoveSubscriptionOnStop;

    public WebhookBotOptions(@Nullable Set<String> updateTypes) {
        super(updateTypes);
    }

    public boolean shouldRemoveOldSubscriptions() {
        return shouldRemoveOldSubscriptions;
    }

    public void setShouldRemoveOldSubscriptions(boolean shouldRemoveOldSubscriptions) {
        this.shouldRemoveOldSubscriptions = shouldRemoveOldSubscriptions;
    }

    public boolean shouldRemoveSubscriptionOnStop() {
        return shouldRemoveSubscriptionOnStop;
    }

    public void setShouldRemoveSubscriptionOnStop(boolean shouldRemoveSubscriptionOnStop) {
        this.shouldRemoveSubscriptionOnStop = shouldRemoveSubscriptionOnStop;
    }
}
