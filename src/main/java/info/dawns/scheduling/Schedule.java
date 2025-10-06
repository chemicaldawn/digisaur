package info.dawns.scheduling;

import java.util.*;

public class Schedule {

    private Map<Day, List<ShiftType>> internalSchedule;

    public Schedule() {
        this.internalSchedule = new TreeMap<>();
        for (Day day : Day.values()) {
            internalSchedule.put(day, new ArrayList<>());
        }
    }

    protected void addShifts(Day day, String shifts) {
        for (String s : shifts.split(", ")) {
            if (!s.equals("None")) {
                internalSchedule.get(day).add(ShiftType.fromName(s));
            }
        }
    }

    public List<ShiftType> getShiftsFor(Day day) {
        return internalSchedule.get(day);
    }

    public Set<ShiftType> getAllShifts() {
        Set<ShiftType> agg = new HashSet<>();

        for (List<ShiftType> l : internalSchedule.values()) {
            agg.addAll(l);
        }

        return agg;
    }

    public String toString() {
        return internalSchedule.toString();
    }
}
