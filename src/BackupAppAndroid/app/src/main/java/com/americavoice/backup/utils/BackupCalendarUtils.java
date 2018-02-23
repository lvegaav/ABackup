package com.americavoice.backup.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by lvega on 2/16/2018.
 */

public class BackupCalendarUtils {

    public static Calendar[] getSelectableDaysArray(List<String> selectableDays) throws ParseException {
        Calendar[] days = null;
        if (selectableDays != null) {
            if (!selectableDays.isEmpty()) {
                days = new Calendar[selectableDays.size()];
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                for (int i = 0; i < selectableDays.size(); i++) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(formatter.parse(selectableDays.get(i).substring(0, 10)));
                    days[i] = calendar;
                }

            } else {
                Calendar day = Calendar.getInstance();
                day.add(Calendar.DAY_OF_MONTH, 0);
                days = new Calendar[1];
                days[0] = day;
            }
        }

        return days;
    }
}
