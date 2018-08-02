package org.swanseacharm.bactive.ui;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateAsXValue extends DefaultLabelFormatter {

    /**
     * the date format that will convert
     * the unix timestamp to string
     */
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM", Locale.ENGLISH);

    /**
     * calendar to avoid creating new date objects
     */
    private final Calendar mCalendar = Calendar.getInstance();;

    /**
     * formats the x-values as date string.
     *
     * @param value raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return value converted to string
     */
    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            mCalendar.setTimeInMillis((long) value);
            return mDateFormat.format(mCalendar.getTimeInMillis());
        } else {
            value = value/1000;
            return (int)value+"K";
        }
    }
}
