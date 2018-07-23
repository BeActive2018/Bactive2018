package org.swanseacharm.bactive;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Util {


    public SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

}
