package info.dawns.scheduling;

import java.util.*;

public class Schedule {

    private Map<Day, Set<ShiftType>> internalSchedule;

    public Schedule() {
        this.internalSchedule = new TreeMap<>();
        for (Day day : Day.values()) {
            internalSchedule.put(day, new HashSet<>());
        }
    }

    protected void addShifts(Day day, String shifts) {
        for (String s : shifts.split(", ")) {
            if (!s.equals("None")) {
                internalSchedule.get(day).add(ShiftType.fromName(s));
            }
        }
    }

    public Set<ShiftType> getShiftsFor(Day day) {
        return internalSchedule.get(day);
    }

    public Set<ShiftType> getAllShifts() {
        Set<ShiftType> agg = new HashSet<>();

        for (Set<ShiftType> l : internalSchedule.values()) {
            agg.addAll(l);
        }

        return agg;
    }

    public String toString() {
        return internalSchedule.toString();
    }
}
