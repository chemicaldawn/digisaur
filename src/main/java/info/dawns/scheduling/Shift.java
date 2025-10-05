package info.dawns.scheduling;

import info.dawns.utils.Registry;
import info.dawns.utils.RegistryItem;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Map;

public class Shift implements RegistryItem {

    public static Registry<Shift> shiftRegistry = new Registry<>();

    public String internalName;

    public double defaultHoursAwarded;
    public Map<Long, Double> hoursAdjustments;

    public String description;
    public Emoji emoji;
    public String type;

    public Shift(String internalName, double defaultHoursAwarded, String description, Emoji emoji, String type) {
        this.internalName = internalName;
        this.defaultHoursAwarded = defaultHoursAwarded;
        this.description = description;
        this.emoji = emoji;
        this.type = type;
    }

    public static void clearCache() {
        shiftRegistry = new Registry<>();
    }

    public static Shift fromName(String name) {
        return shiftRegistry.get(name);
    }

    public String toString() {
        return internalName;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Shift otherShift) {
            return this.internalName.equals(otherShift.internalName);
        }

        return false;
    }

    @Override
    public String getKey() {
        return this.internalName;
    }

    @Override
    public int hashCode() {
        return this.internalName.hashCode();
    }
}
