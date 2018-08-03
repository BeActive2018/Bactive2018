package org.swanseacharm.bactive.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.Util;
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Yesterday extends AppCompatActivity {

    //configuration variables
    private String tag = "Yesterday_Activity";
    private String deliminator = ",";//separator for date and respective steps
    private String seperator = "/n";//separator for date step pairs

    private int mStepsPerCal = 20; //how many steps need to be taken to burn 1 calorie
    private double mMetersPerStep = 0.701;//distance travelled in 1 step (meters)
    //data binding variable and buttons
    ActivityYesterdayBinding binding;
    private Button home;
    private Button history;
    //steps taken yesterday (determined in onCreate()
    private int yesterdaySteps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag,"onCreate called");
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_yesterday);
        yesterdaySteps = getYesterdaySteps();//get steps for yesterday

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

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.bactivelogo);
    }

    public void onStart()
    {
        super.onStart();
        DecimalFormat df = new DecimalFormat("#.##");//format for distance travelled

        //set UI elements to correct values
        binding.stepsTakenToday.setText(String.valueOf(yesterdaySteps));
        binding.textView9.setText(String.valueOf(yesterdaySteps/mStepsPerCal));
        binding.textView10.setText(String.valueOf(Double.valueOf(df.format((yesterdaySteps*mMetersPerStep)/1000))));

        //start green man animation
        ImageView greenManAnimation = findViewById(R.id.imageView8);
        AnimationDrawable animation1 = (AnimationDrawable) greenManAnimation.getDrawable();
        animation1.start();
        //start group men animation
        ImageView groupAnimation = findViewById(R.id.imageView7);
        AnimationDrawable animation2 = (AnimationDrawable) groupAnimation.getDrawable();
        animation2.start();
        //start top background animation
        ImageView background = findViewById(R.id.imageView);
        AnimationDrawable animation3 = (AnimationDrawable) background.getDrawable();
        animation3.start();
        //start bottom background animation
        ImageView background2 = findViewById(R.id.imageView2);
        AnimationDrawable animation4 = (AnimationDrawable) background2.getDrawable();
        animation4.start();
        //update position of the green man
        updateGreenManPosition();
    }

    private void updateGreenManPosition()//changes green characters position depending on performance compared to group average
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.imageView8).getLayoutParams();
        //TODO remove and replace with values from database
        int temporyDevInt =10000;
        if(yesterdaySteps<=temporyDevInt*0.05)
        {
            params.horizontalBias = 0.80f;
            binding.textView2.setText(R.string.bad);
        }
        else if(yesterdaySteps<=temporyDevInt*0.25)
        {
            params.horizontalBias = 0.6f;
            binding.textView2.setText(R.string.ok);
        }
        else if(yesterdaySteps<=temporyDevInt)
        {
            params.horizontalBias = 0.5f;
            binding.textView2.setText(R.string.good);
        }
        else if(yesterdaySteps<=temporyDevInt*1.25)
        {
            params.horizontalBias = 0.4f;
            binding.textView2.setText(R.string.great);
        }
        else
        {
            params.horizontalBias = 0.2f;
            binding.textView2.setText(R.string.amazing);
        }
        findViewById(R.id.imageView8).setLayoutParams(params);



    }

    private String getFileFull(Context context)
    {
        Util util = new Util();
        return util.getfile(context);
    }

    private String getYesterdayString()//converts a date from yesterday() into correct string date format
    {
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        Log.d(tag,formatterDate.format(yesterday()));
        return formatterDate.format(yesterday());

    }
    private Date yesterday()//returns yesterdays date as Date object
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);
        return calendar.getTime();
    }
    private int getYesterdaySteps()//gets steps taken yesterday
    {
        String fileStr = getFileFull(this.getApplication());//get history file
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.addAll(Arrays.asList(fileStr.split(seperator)));//split file for parsing
        String regex = ".*"+getYesterdayString()+"";//setup regex string
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(regex);

        for(String str:stringArrayList)//for every string in stringArrayList
        {
            matcher = pattern.matcher(str);
            Log.d(tag,"the outer loop"+str+" "+getYesterdayString());
            //if str has yesterdays date
            if(matcher.matches())
            {
                Log.d(tag,"the inner loop"+getYesterdayString());
                stringArrayList = new ArrayList<>(Arrays.asList(str.split(deliminator)));//split date and steps
                return Integer.valueOf(stringArrayList.get(0));//return steps as integer
            }
        }
        return 0;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        overridePendingTransition(0,0);//remove animation
    }
    //TODO get database information and put it into group average
}
