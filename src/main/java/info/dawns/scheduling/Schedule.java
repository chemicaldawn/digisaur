package info.dawns.scheduling;

import java.util.*;

public class Schedule {

    public enum Day {
        ANY_DAY,
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }

    private Map<Day, List<Shift>> internalSchedule;

    public Schedule() {
        this.internalSchedule = new TreeMap<>();
        for (Day day : Day.values()) {
            internalSchedule.put(day, new ArrayList<>());
        }
    }

    protected void addShifts(Day day, String shifts) {
        for (String s : shifts.split(", ")) {
            if (!s.equals("None")) {
                internalSchedule.get(day).add(Shift.fromName(s));
            }
        }
    }

    public List<Shift> getShiftsFor(Day day) {
        return internalSchedule.get(day);
    }

    public Set<Shift> getAllShifts() {
        Set<Shift> agg = new HashSet<>();

        for (List<Shift> l : internalSchedule.values()) {
            agg.addAll(l);
        }

        return agg;
    }

    public String toString() {
        return internalSchedule.toString();
    }
}
