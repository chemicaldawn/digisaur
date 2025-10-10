package info.dawns.scheduling;

public class Shift {

    private ShiftType type;
    private Day day;

    private double hoursAward;

    public Shift(ShiftType type, Day day) {
        this.type = type;
        this.day = day;

        this.hoursAward = type.defaultHoursAward;
    }

    public static Shift fromId(String id) {
        String[] halves = id.split(":");
        return new Shift(ShiftType.fromId(halves[0]), Day.values()[Integer.parseInt(halves[1])]);
    }

    public static Shift fromNameAndId(String name, Day day) {
        return new Shift(ShiftType.fromName(name), day);
    }

    public Day getDay() {
        return this.day;
    }

    public String getName() {
        String prefix = "";

        if (this.day != Day.ANY_DAY) {
            prefix += this.day.toString() + " ";
        }

        return prefix + this.type.getName();
    }

    public String getId() {
        return this.type.getId() + ":" + String.valueOf(this.day.ordinal());
    }

    public double getHours() {
        return this.hoursAward;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Shift otherShift) {
            return this.type.equals(otherShift.type) && this.day == otherShift.day;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.type.hashCode() + this.day.ordinal();
    }
}
