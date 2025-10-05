package info.dawns.scheduling;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import info.dawns.Constants;
import info.dawns.authorization.Google;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class ScheduleManager {

    private NetHttpTransport netHttpTransport;
    private Credential googleCredentials;
    private Sheets sheets;

    private Map<Long, String> nameMap;
    private Map<Long, Integer> rowMap;

    private Map<Long, Schedule> scheduleByUser;
    private Set<Shift> bonusShifts;
    private Map<Long, Set<Shift>> pickupShifts;
    private Set<Shift> questShifts;

    public ScheduleManager() {
        try {
            this.netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.googleCredentials = Google.getCredentials(this.netHttpTransport);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.sheets = new Sheets.Builder(this.netHttpTransport, Google.JSON_FACTORY, this.googleCredentials)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();

        updateCaches();
    }

    public void updateCaches() {
        buildNameMap();
        buildScheduleMap();
    }

    private void buildNameMap() {
        this.nameMap = new HashMap<>();
        this.rowMap = new HashMap<>();
        List<List<String>> data = this.getRange("Workshift Tracker!A3:B36");

        int offset = 0;
        for (List<String> row : data) {
            long discordId = Long.valueOf(row.get(1));
            this.nameMap.put(discordId, row.get(0));
            this.rowMap.put(discordId, 2 + offset);
            offset += 1;
        }
    }

    private void buildScheduleMap() {
        List<List<String>> dictionaryData = this.getRange("Workshift Dictionary!A2:D43");

        scheduleByUser = new HashMap<>();
        bonusShifts = new HashSet<>();
        pickupShifts = new TreeMap<>();
        questShifts = new HashSet<>();

        for (List<String> row : dictionaryData) {
            Shift newShift = new Shift(row.get(0), Double.valueOf(row.get(1)), "", Emoji.fromUnicode(row.get(2)), String.valueOf(row.get(3)));
            Shift.shiftRegistry.add(newShift);

            if (newShift.type.equals("bonus")) {
                bonusShifts.add(newShift);
            }
        }

        List<List<String>> assignmentsData = this.getRange("Workshift Assignments!A3:J36");

        for (List<String> row : assignmentsData) {
            Schedule newSchedule = new Schedule();

            for (Schedule.Day day : Schedule.Day.values()) {
                int col = day.ordinal() + 2;
                newSchedule.addShifts(day, row.get(col));
            }

            this.scheduleByUser.put(Long.valueOf(row.get(1)), newSchedule);
        }
    }

    public List<List<String>> getRange(String range) {
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

    public Schedule getSchedule(long id) {
        return this.scheduleByUser.get(id);
    }

    public Set<Shift> getPickupShiftsFor(long id) { return this.pickupShifts.getOrDefault(id, new HashSet<>()); }

    public Set<Shift> getBonusShifts() { return this.bonusShifts; }

    public Set<Shift> getQuestShifts() { return this.questShifts; }

    public void addPickupShiftFor(long id, Shift s) {
        if (this.pickupShifts.get(id) == null) {
            this.pickupShifts.put(id, new HashSet<>());
        }

        this.pickupShifts.get(id).add(s);
    }

    public void addQuestShift(Shift s) {
        this.questShifts.add(s);
    }

    public void completePickupShiftFor(long id, Shift s) {
        Set<Shift> pickupShifts = this.pickupShifts.getOrDefault(id, new HashSet<>());
        pickupShifts.remove(s);
    }

    public void completeQuestShift(Shift s) {
        questShifts.remove(s);
    }

    public void verify(long id, Shift shift) throws IOException {

        int row = this.rowMap.get(id);

        DateTime now = DateTime.now();
        DateTime start = new DateTime(2025, 8, 25, 0, 1);
        Duration diff = new Duration(start, now);

        int weekOn = (int) diff.getStandardDays() / 7;
        int col = 3 + weekOn * 3;

        String range = "'Workshift Tracker'!" + "R" + String.valueOf(row + 1) + "C" + String.valueOf(col + 1);

        ValueRange r = this.sheets.spreadsheets()
                .values()
                .get(Constants.WORKSHIFT_TRACKER_ID, range)
                .execute();

        Spreadsheet s =  this.sheets.spreadsheets()
                .get(Constants.WORKSHIFT_TRACKER_ID)
                .setRanges(List.of(range))
                .setFields("sheets/data/rowData/values/note")
                .execute();

        double newHours = 0;

        if (r.getValues() != null) {
            newHours = Double.valueOf((String) r.getValues().get(0).get(0)) + shift.defaultHoursAwarded;
        } else {
            newHours = shift.defaultHoursAwarded;
        }

        String marker = "[" + String.valueOf(now.getMonthOfYear()) + "/" + String.valueOf(now.getDayOfMonth()) + " " + String.valueOf(now.getHourOfDay()) + ":" + String.valueOf(now.getMinuteOfHour()) + "] " + shift.internalName;
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

        this.sheets.spreadsheets().batchUpdate(
                Constants.WORKSHIFT_TRACKER_ID,
                new BatchUpdateSpreadsheetRequest().setRequests(requests)
        ).execute();
    }
}
