package info.dawns.scheduling;

public enum Day {
    ANY_DAY,
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;

    @Override
    public String toString() {
        switch (this) {
            case ANY_DAY -> {
                return "Any Day";
            }
            case SUNDAY -> {
                return "Sunday";
            }
            case MONDAY -> {
                return "Monday";
            }
            case TUESDAY -> {
                return "Tuesday";
            }
            case WEDNESDAY -> {
                return "Wednesday";
            }
            case THURSDAY -> {
                return "Thursday";
            }
            case FRIDAY -> {
                return "Friday";
            }
            case SATURDAY -> {
                return "Saturday";
            }
        }

        return "";
    }
}