package info.dawns.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.sheets.v4.Sheets;
import info.dawns.Constants;
import info.dawns.initialization.Google;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarManager {

    private static Logger scheduleManagerLogger = LoggerFactory.getLogger(CalendarManager.class);

    private static Calendar calendar;

    public static void initialize(NetHttpTransport netHttpTransport, Credential googleCredentials) {
        scheduleManagerLogger.info("Connecting to the Google Calendar API");

        calendar = new Calendar.Builder(netHttpTransport, Google.JSON_FACTORY, googleCredentials)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();
    }


}
