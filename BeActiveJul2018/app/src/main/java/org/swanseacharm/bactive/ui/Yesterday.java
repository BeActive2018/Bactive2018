package org.swanseacharm.bactive.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.Util;
import org.swanseacharm.bactive.databinding.ActivityYesterdayBinding;
import org.swanseacharm.bactive.services.SaveDataService;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Yesterday extends AppCompatActivity {

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


    public void Yesterday()
    {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(tag,"onCreate called");
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_yesterday);
        yesterdaySteps = getYesterdaySteps();

        home = (Button)findViewById(R.id.button6);
        home.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });

        history = (Button)findViewById(R.id.button7);
        history.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(Yesterday.this,org.swanseacharm.bactive.ui.History.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });
        //Utility class and SharedPreferences class for application wide data management
        Util util = new Util();
        SharedPreferences prefs = this.getSharedPreferences(
                "org.swanseacharm.bactive", Context.MODE_PRIVATE);

        //get application wide data
        mStepsPerCal = prefs.getInt("STEPS_PER_CAL",20);
        mMetersPerStep = util.getDouble(prefs,"METERS_PER_STEP",0.701);
    }

    public void onStart()
    {
        super.onStart();
        DecimalFormat df = new DecimalFormat("#.##");

        binding.stepsTakenToday.setText(String.valueOf(yesterdaySteps));
        binding.textView9.setText(String.valueOf(yesterdaySteps/mStepsPerCal));
        binding.textView10.setText(String.valueOf(Double.valueOf(df.format((yesterdaySteps*mMetersPerStep)/1000))));
    }

    private String getFileFull(Context context)
    {
        Log.i(tag, "getting file");
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
        Log.v(tag,ret+" End of file");
        return ret;
    }

    private String getYesterdayString()
    {
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
        Log.d(tag,formatterDate.format(yesterday()));
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
        String fileStr = getFileFull(this.getApplication());
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.addAll(Arrays.asList(fileStr.split(seperator)));
        String regex = ".*"+getYesterdayString()+"";
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(regex);

        for(String str:stringArrayList)
        {
            matcher = pattern.matcher(str);
            Log.d(tag,"the outer loop"+str+" "+getYesterdayString());
            if(matcher.matches())
            {
                Log.d(tag,"the inner loop"+getYesterdayString());
                stringArrayList = new ArrayList<>(Arrays.asList(str.split(deliminator)));
                return Integer.valueOf(stringArrayList.get(0));
            }
        }
        return 0;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        overridePendingTransition(0,0);
    }
}
