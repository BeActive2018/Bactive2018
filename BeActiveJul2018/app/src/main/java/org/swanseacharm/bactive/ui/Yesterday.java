package org.swanseacharm.bactive.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.databinding.ActivityYesterdayBinding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class Yesterday extends AppCompatActivity {

    public void Yesterday()
    {

    }
    //configuration variables
    private String tag = "Yesterday_Activity";
    private String fileName = "stepHistory.stp";
    private String deliminator = ",";
    private String seperator = "/n";
    private int mStepsPerCal = 20;
    private double mMetersPerStep = 0.701;
    //data binding variable and buttons
    ActivityYesterdayBinding binding;
    private Button home;
    private Button history;
    //steps taken yesterday (determined in onCreate()
    private int yesterdaySteps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_yesterday);
        yesterdaySteps = getYesterdaySteps();

        home = (Button)findViewById(R.id.button6);
        home.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //TODO navigate back to main
            }
        });

        history = (Button)findViewById(R.id.button7);
        history.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //TODO navigate back to history
            }
        });
    }

    public void onStart()
    {
        super.onStart();
        DecimalFormat df = new DecimalFormat("#,##");

        binding.stepsTakenToday.setText(String.valueOf(yesterdaySteps));
        binding.textView9.setText(String.valueOf(yesterdaySteps/mStepsPerCal));
        binding.textView10.setText(String.valueOf(Double.valueOf(df.format((yesterdaySteps*mMetersPerStep)/1000))));
    }

    private String getFileFull(Context context)
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

    private String getYesterdayString()
    {
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
        return formatterDate.format(yesterday());

    }
    private Date yesterday()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);
        return calendar.getTime();
    }
    private int getYesterdaySteps()
    {
        String fileStr = getFileFull(getBaseContext());
        ArrayList<String> stringArrayList = (ArrayList<String>) Arrays.asList(fileStr.split(seperator));
        for(String str:stringArrayList)
        {
            if(str.matches(""+getYesterdayString()+"$"))
            {
                stringArrayList = (ArrayList<String>)Arrays.asList(str.split(deliminator));
                return Integer.valueOf(stringArrayList.get(0));
            }
        }
        return 0;
    }
}
