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
}
