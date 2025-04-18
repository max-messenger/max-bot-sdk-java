package ru.max.bot;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import ru.max.botapi.model.BotAddedToChatUpdate;
import ru.max.botapi.model.Update;
import ru.max.botapi.model.User;

public class Randoms {
    private static final AtomicLong NEXT_ID = new AtomicLong();

    public static String text() {
        return UUID.randomUUID().toString();
    }

    public static Update randomUpdate() {
        User user = randomUser();
        return new BotAddedToChatUpdate(nextId(), user, ThreadLocalRandom.current().nextBoolean(), nextId());
    }

    public static User randomUser() {
        long userId = nextId();
        String username = "username" + userId;
        return new User(userId, "Name", Long.toString(userId), username, ThreadLocalRandom.current().nextBoolean(),
                ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE));
    }

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static <T> T random(List<T> updates) {
        return updates.get(randomInt(updates.size()));
    }

    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    private static long nextId() {
        return NEXT_ID.incrementAndGet();
    }

}
