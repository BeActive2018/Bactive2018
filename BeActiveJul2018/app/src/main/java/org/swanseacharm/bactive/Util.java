package org.swanseacharm.bactive;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Util {
    private String tag = "Bactive Util";
    private String fileName = "stepHistory.stp";

    public SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public String getfile(Context context)
    {
        String ret = "";
        try{
            InputStream inputStream = context.openFileInput(fileName);

            if(inputStream!=null)
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String recieveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((recieveString = bufferedReader.readLine())!=null)
                {
                    stringBuilder.append(recieveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch(FileNotFoundException e){
            Log.e(tag, e.toString());
        }
        catch (IOException e){
            Log.e(tag, e.toString());
        }

        return ret;
    }

    public String getfile(Context context,String fName)
    {
        String ret = "";
        try{
            InputStream inputStream = context.openFileInput(fName);

            if(inputStream!=null)
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String recieveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((recieveString = bufferedReader.readLine())!=null)
                {
                    stringBuilder.append(recieveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch(FileNotFoundException e){
            Log.e(tag, e.toString());
        }
        catch (IOException e){
            Log.e(tag, e.toString());
        }

        return ret;
    }

    public void saveDataToFile(Context context,String fName,String data,boolean append)
    {
        try{
            OutputStreamWriter outputStreamWriter;
            if(append){
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fName,Context.MODE_APPEND));
            }
            else
            {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fName,Context.MODE_PRIVATE));
            }
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.i(tag,"Data saved to "+fName);

        }
        catch (IOException e){
            Log.e("EXCEPTION", e.toString());
        }
    }

}


