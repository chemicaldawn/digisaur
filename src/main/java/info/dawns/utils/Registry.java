package info.dawns.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Registry<T extends RegistryItem> extends HashMap<String, T> {

    public void add(T item) {
        this.put(item.getKey(), item);
    }

    public T get(String key) {
        return super.getOrDefault(key, null);
    }
}
