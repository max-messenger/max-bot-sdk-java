package ru.max.bot.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import ru.max.bot.commands.Command;
import ru.max.bot.commands.CommandHandler;
import ru.max.botapi.model.Message;

public class ChatBotBuilder {
    private static final Consumer<Message> DO_NOTHING = message -> {
    };

    private final Map<String, CommandHandler> handlers = new ConcurrentHashMap<>();
    private Consumer<Message> defaultHandler;
    private CommandHandler unknownCommandHandler;

    public ChatBotBuilder on(String command, CommandHandler handler) {
        handlers.put(command, handler);
        return this;
    }

    public <T extends CommandHandler & Command> ChatBotBuilder add(T handler) {
        handlers.put(handler.getKey(), handler);
        return this;
    }

    public ChatBotBuilder byDefault(Consumer<Message> defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public ChatBotBuilder onUnknownCommand(CommandHandler unknownCommandHandler) {
        this.unknownCommandHandler = unknownCommandHandler;
        return this;
    }

    public ChatBot build() {
        return new ChatBot(handlers,
                defaultHandler == null ? DO_NOTHING : defaultHandler,
                unknownCommandHandler == null ? CommandHandler.NOOP : unknownCommandHandler);
    }
}
