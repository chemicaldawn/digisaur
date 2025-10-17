package info.dawns.scheduling;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import info.dawns.Constants;
import info.dawns.initialization.Google;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ScheduleManager {

    public static Logger scheduleManagerLogger = LoggerFactory.getLogger(ScheduleManager.class);

    private static NetHttpTransport netHttpTransport;
    private static Credential googleCredentials;
    private static Sheets sheets;

    private static Map<Long, String> nameMap;
    private static Map<Long, Integer> rowMap;

    private static Map<Long, Schedule> scheduleByUser;
    private static Set<Shift> bonusShifts;
    private static Map<Long, Set<Shift>> pickupShifts;
    private static Set<Shift> questShifts;

    public static void initialize(NetHttpTransport netHttpTransport, Credential googleCredentials) {
        scheduleManagerLogger.info("Connecting to the Google Sheets API");

        sheets = new Sheets.Builder(netHttpTransport, Google.JSON_FACTORY, googleCredentials)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();
    }

    public static void updateCaches() {
        scheduleManagerLogger.info("Updating caches");

        buildNameMap();
        buildScheduleMap();
    }

    private static void buildNameMap() {
        nameMap = new HashMap<>();
        rowMap = new HashMap<>();
        List<List<String>> data = getRange("Workshift Tracker!A3:B36");

        int offset = 0;
        for (List<String> row : data) {
            long discordId = Long.valueOf(row.get(1));

            nameMap.put(discordId, row.get(0).split(" ")[1]);
            rowMap.put(discordId, 2 + offset);
            offset += 1;
        }
    }

    private static void buildScheduleMap() {
        List<List<String>> dictionaryData = getRange("Workshift Dictionary!A2:D42");

        scheduleByUser = new HashMap<>();
        bonusShifts= new HashSet<>();
        pickupShifts = new TreeMap<>();
        questShifts = new HashSet<>();

        for (List<String> row : dictionaryData) {
            ShiftType newShiftType = new ShiftType(row.get(0), Double.valueOf(row.get(1)), "", Emoji.fromUnicode(row.get(2)), String.valueOf(row.get(3)).toLowerCase());
            ShiftType.shiftTypeRegistry.add(newShiftType);

            if (newShiftType.type.equals("bonus")) {
                bonusShifts.add(new Shift(newShiftType, Day.ANY_DAY));
            }
        }

        List<List<String>> assignmentsData = getRange("Workshift Assignments!A3:J36");

        for (List<String> row : assignmentsData) {
            Schedule newSchedule = new Schedule();

            for (Day day : Day.values()) {
                int col = day.ordinal() + 2;
                newSchedule.addShiftsFromSheet(day, row.get(col));
            }

            scheduleByUser.put(Long.valueOf(row.get(1)), newSchedule);
        }
    }

    public static String getName(long id) {
        return nameMap.get(id);
    }

    public static List<List<String>> getRange(String range) {
        try {
            List<List<Object>> response = sheets.spreadsheets()
                .values()
                .get(Constants.WORKSHIFT_TRACKER_ID, range)
                .execute()
                .getValues();
            return (List<List<String>>)(List<?>) response;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Schedule getScheduleFor(long id) {
        return scheduleByUser.get(id);
    }

    public static Set<Shift> getPickupShiftsFor(long id) { return pickupShifts.getOrDefault(id, new HashSet<>()); }

    public static Set<Shift> getBonusShifts() { return bonusShifts; }

    public static Set<Shift> getQuestShifts() { return questShifts; }

    public static void addPickupShiftFor(long id, Shift s) {
        if (pickupShifts.get(id) == null) {
            pickupShifts.put(id, new HashSet<>());
        }

        pickupShifts.get(id).add(s);
    }

    public static void addQuestShift(Shift s) {
        questShifts.add(s);
    }

    public static void completePickupShiftFor(long id, Shift s) {
        Set<Shift> pickupShiftTypeSet = pickupShifts.getOrDefault(id, new HashSet<>());
        pickupShiftTypeSet.remove(s);
    }

    public static void completeQuestShift(Shift s) {
        questShifts.remove(s);
    }

    public static void verify(long id, Shift shift) throws IOException {

        int row = rowMap.get(id);

        DateTime now = DateTime.now();
        DateTime start = new DateTime(2025, 8, 25, 0, 1);
        Duration diff = new Duration(start, now);

        int weekOn = (int) diff.getStandardDays() / 7;
        int col = 3 + weekOn * 3;

        String range = "'Workshift Tracker'!" + "R" + String.valueOf(row + 1) + "C" + String.valueOf(col + 1);

        ValueRange r = sheets.spreadsheets()
                .values()
                .get(Constants.WORKSHIFT_TRACKER_ID, range)
                .execute();

        Spreadsheet s =  sheets.spreadsheets()
                .get(Constants.WORKSHIFT_TRACKER_ID)
                .setRanges(List.of(range))
                .setFields("sheets/data/rowData/values/note")
                .execute();

        double newHours = 0;

        if (r.getValues() != null) {
            newHours = Double.valueOf((String) r.getValues().get(0).get(0)) + shift.getHours();
        } else {
            newHours = shift.getHours();
        }

        String marker = "[" + String.valueOf(now.getMonthOfYear()) + "/" + String.valueOf(now.getDayOfMonth()) + " " + String.valueOf(now.getHourOfDay()) + ":" + String.valueOf(now.getMinuteOfHour()) + "] " + shift.getName();
        String note = "";

        if (s.getSheets().get(0).getData().get(0).getRowData() != null) {
            note = s.getSheets().get(0).getData().get(0).getRowData().get(0).getValues().get(0).getNote() + "\n";
        }

        note += marker;

        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setUpdateCells(
            new UpdateCellsRequest()
                .setRange(new GridRange()
                    .setSheetId(0)
                    .setStartRowIndex(row)
                    .setEndRowIndex(row + 1)
                    .setStartColumnIndex(col)
                    .setEndColumnIndex(col + 1)
                ).setRows(
                    List.of(
                        new RowData()
                            .setValues(
                                List.of(
                                    new CellData().set("userEnteredValue", new ExtendedValue()
                                            .set("numberValue", newHours))
                                    .set("userEnteredFormat", new CellFormat()
                                            .set("horizontalAlignment", "CENTER")
                                            .set("verticalAlignment","MIDDLE"))
                                    .set("note",note)
                                )
                            )
                    )
                ).setFields("*")
        ));

        sheets.spreadsheets().batchUpdate(
                Constants.WORKSHIFT_TRACKER_ID,
                new BatchUpdateSpreadsheetRequest().setRequests(requests)
        ).execute();
    }
}
