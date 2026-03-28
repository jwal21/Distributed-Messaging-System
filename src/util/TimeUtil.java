package util;
//import LocalDateTime to get the current date and time.
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//TimeUtil provides helper methods for generating timestamps.
public class TimeUtil {

	//time formatter used across the system.
	//example output: 11:33:14
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    //returns the current time as a formatted string
    public static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}