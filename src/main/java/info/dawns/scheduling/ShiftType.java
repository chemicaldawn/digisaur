package info.dawns.scheduling;

import info.dawns.utils.Registry;
import info.dawns.utils.RegistryItem;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Locale;
import java.util.Map;

public class ShiftType implements RegistryItem {

    public static Registry<ShiftType> shiftTypeRegistry = new Registry<>();

    private String name;
    private String id;

    public double defaultHoursAward;
    public Map<Long, Double> hoursAdjustments;

    public String description;
    private Emoji emoji;
    public String type;

    public ShiftType(String name, double defaultHoursAward, String description, Emoji emoji, String type) {
        this.name = this.name;
        this.id = this.name.toLowerCase(Locale.ROOT).replaceAll("\\s+\\+?\\s*","-");

        this.defaultHoursAward = defaultHoursAward;
        this.description = description;
        this.emoji = emoji;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public double getDefaultHoursAward;

    public static void clearCache() {
        shiftTypeRegistry = new Registry<>();
    }

    public static ShiftType fromName(String name) {
        return shiftTypeRegistry.get(name);
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ShiftType otherShiftType) {
            return this.name.equals(otherShiftType.name);
        }

        return false;
    }

    @Override
    public String getKey() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
