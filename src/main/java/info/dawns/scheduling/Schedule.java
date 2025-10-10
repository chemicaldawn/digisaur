package info.dawns.scheduling;

import java.util.*;

public class Schedule extends HashMap<Day, HashSet<Shift>> {

    public Schedule() {
    }

    protected void putShift(ShiftType type, Day day) {
        if (!this.containsKey(day)) {
            this.put(day, new HashSet<>());
        }

        this.get(day).add(new Shift(type, day));
    }

    protected void addShiftsFromSheet(Day day, String shifts) {
        for (String s : shifts.split(", ")) {
            if (!s.equals("None")) {
                this.putShift(ShiftType.fromName(s), day);
            }
        }
    }

    public Set<Shift> getShiftsFor(Day day) {
        return this.getOrDefault(day, new HashSet<>());
    }

    public Set<Shift> getAllShifts() {
        Set<Shift> agg = new HashSet<>();

        for (Set<Shift> l : this.values()) {
            agg.addAll(l);
        }

        return agg;
    }
}
