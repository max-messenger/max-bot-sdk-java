package ru.max.bot.chat;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import ru.max.bot.commands.CommandHandler;
import ru.max.bot.commands.CommandLine;
import ru.max.bot.commands.CommandLineParser;
import ru.max.botapi.model.Message;

public class ChatBot {
    private final Map<String, CommandHandler> commandHandlers;
    private final Consumer<Message> defaultHandler;
    private final CommandHandler unknownCommandHandler;

    public ChatBot(Map<String, CommandHandler> commandHandlers, Consumer<Message> defaultHandler,
                   CommandHandler unknownCommandHandler) {
        this.commandHandlers = Objects.requireNonNull(commandHandlers, "commandHandlers");
        this.defaultHandler = Objects.requireNonNull(defaultHandler, "defaultHandler");
        this.unknownCommandHandler = Objects.requireNonNull(unknownCommandHandler, "unknownCommandHandler");
    }

    public void replyOn(Message message) {
        String text = message.getBody().getText();
        boolean hasText = text != null && !text.trim().isEmpty();
        if (!hasText) {
            defaultHandler.accept(message);
            return;
        }

        CommandLine commandLine = CommandLineParser.tryParse(text);
        if (commandLine == null) {
            defaultHandler.accept(message);
            return;
        }

        String commandKey = commandLine.getKey();
        CommandHandler handler = commandHandlers.get(commandKey);
        if (handler == null) {
            unknownCommandHandler.execute(message, commandLine);
            return;
        }

        handler.execute(message, commandLine);
    }
}
