package info.dawns.utils;

import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

public class UserMap<T> extends HashMap<Long, T> {

    public T put(User key, T value) {
        return super.put(key.getIdLong(), value);
    }

    public T get(User userKey) {
        return super.get(userKey.getIdLong());
    }

    public T getOrDefault(User userKey, T defaultValue) {
        return super.getOrDefault(userKey, defaultValue);
    }
}
