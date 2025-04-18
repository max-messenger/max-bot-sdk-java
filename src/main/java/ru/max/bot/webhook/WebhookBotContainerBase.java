package ru.max.bot.webhook;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.max.bot.MaxBot;
import ru.max.bot.exceptions.BotNotFoundException;
import ru.max.bot.exceptions.MaxBotException;
import ru.max.bot.exceptions.WebhookException;
import ru.max.botapi.client.MaxSerializer;
import ru.max.botapi.exceptions.SerializationException;
import ru.max.botapi.model.Update;

/**
 * Base implementation of {@link WebhookBotContainer} that registers bots in map, parses incoming requests as
 * {@link Update} and delegates handling to {@link WebhookBot}.
 */
public abstract class WebhookBotContainerBase implements WebhookBotContainer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Map<String, WebhookBot> bots = new ConcurrentHashMap<>();
    private final Map<MaxBot, String> reverseLookup = new ConcurrentHashMap<>();

    @Override
    public void register(WebhookBot bot) {
        String path = getPath(bot);
        if (bots.putIfAbsent(path, bot) != null) {
            throw new IllegalStateException("Bot " + bot + " is already registered");
        }

        reverseLookup.put(bot, path);
    }

    @Override
    public void unregister(WebhookBot bot) {
        String currentPath = reverseLookup.get(bot);
        if (currentPath == null) {
            throw new IllegalStateException("Bot " + bot + " is not registered");
        }

        reverseLookup.remove(bot, currentPath);
        bots.remove(currentPath, bot);
    }

    @Override
    public Iterable<WebhookBot> getBots() {
        return bots.values();
    }

    @Override
    public void start() throws MaxBotException {
        for (WebhookBot bot : bots.values()) {
            try {
                boolean isStarted = bot.start(this);
                if (!isStarted) {
                    continue;
                }

                LOG.info("Bot {} started", bot);
            } catch (Exception e) {
                LOG.error("Failed to start bot {}", bot, e);
            }
        }
    }

    @Override
    public void stop() {
        for (WebhookBot bot : bots.values()) {
            try {
                boolean isStopped = bot.stop(this);
                if (!isStopped) {
                    continue;
                }

                LOG.info("Bot {} stopped", bot);
            } catch (Exception e) {
                LOG.error("Failed to stop bot {}", bot, e);
            }
        }
    }

    @Override
    public String handleRequest(String path, String method, InputStream body) throws WebhookException {
        if (!method.equals("POST")) {
            return "OK";
        }

        WebhookBot bot = bots.get(path);
        if (bot == null) {
            throw new BotNotFoundException("No bot registered by path: " + path);
        }

        MaxSerializer serializer = bot.getClient().getSerializer();
        Update update;
        try {
            update = serializer.deserialize(body, Update.class);
        } catch (SerializationException e) {
            throw WebhookException.internalServerError("Failed to parse update", e);
        }

        Object response = bot.onUpdate(update);

        try {
            return serializer.serializeToString(response);
        } catch (SerializationException e) {
            throw WebhookException.internalServerError("Failed to serialize response: " + response, e);
        }
    }

    /**
     * @return full bot HTTP path inside container
     */
    protected String getPath(WebhookBot bot) {
        return "/" + bot.getKey();
    }
}
