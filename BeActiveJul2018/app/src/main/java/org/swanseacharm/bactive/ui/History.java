package org.swanseacharm.bactive.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.Util;
import org.swanseacharm.bactive.databinding.ActivityHistoryBinding;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;

import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class History extends AppCompatActivity {

    private String tag = "History";
    private String fileName = "stepHistory.stp";
    private String deliminator = ",";
    private String seperator = "/n";

    private Calendar firstDayOfWeek;
    private Calendar thisWeekStart;
    private Stack<ArrayList<Integer>> weekStack = new Stack<>();

    private Button home;
    private Button yesterday;
    private ImageButton lastWeekButton;
    private ImageButton nextWeekButton;

    ActivityHistoryBinding binding;

    private GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_history);
        setStartOfThisWeek();
        graph = findViewById(R.id.graph);
        updateGraph(false);


        home = (Button)findViewById(R.id.button6);
        home.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });

        yesterday = (Button)findViewById(R.id.button5);
        yesterday.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(History.this,org.swanseacharm.bactive.ui.Yesterday.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });
        nextWeekButton = (ImageButton)findViewById(R.id.imageButton2);
        nextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextWeek();
                updateGraph(true);
            }
        }
    );

        lastWeekButton = (ImageButton)findViewById(R.id.imageButton);
        lastWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastWeek();
                updateGraph(false);
            }
        });


    }

    @Override
    public void onPause()
    {
        super.onPause();
        overridePendingTransition(0,0);
    }

    private void updateGraph(boolean nextWeek)
    {
        int maxStep = 0;
        DataPoint[] data=null;
        //set dates to use
        Date d1 = firstDayOfWeek.getTime();
        firstDayOfWeek.add(Calendar.DATE,+1);
        Date d2 = firstDayOfWeek.getTime();
        firstDayOfWeek.add(Calendar.DATE,+1);
        Date d3 = firstDayOfWeek.getTime();
        firstDayOfWeek.add(Calendar.DATE,+1);
        Date d4 = firstDayOfWeek.getTime();
        firstDayOfWeek.add(Calendar.DATE,+1);
        Date d5 = firstDayOfWeek.getTime();
        firstDayOfWeek.add(Calendar.DATE,+1);
        Date d6 = firstDayOfWeek.getTime();
        firstDayOfWeek.add(Calendar.DATE,+1);
        Date d7 = firstDayOfWeek.getTime();
        firstDayOfWeek.add(Calendar.DATE,-6);//setting firstDayOfWeek back to the start of the week

        //if next week selected (forward in time) and stack has data then get weeks data from the stack
        if(nextWeek && !weekStack.empty())
        {
            weekStack.pop();
            ArrayList<Integer> weekSteps = weekStack.pop();
            data = new DataPoint[] {
                    new DataPoint(d1, weekSteps.get(0)),
                    new DataPoint(d2, weekSteps.get(1)),
                    new DataPoint(d3, weekSteps.get(2)),
                    new DataPoint(d4, weekSteps.get(3)),
                    new DataPoint(d5, weekSteps.get(4)),
                    new DataPoint(d6,weekSteps.get(5)),
                    new DataPoint(d7,weekSteps.get(6))
            };
            weekStack.push(weekSteps);
        }
        //otherwise retrieve data from disk
        else
        {
            ArrayList<Integer> weekSteps = getWeekSteps();
            data = new DataPoint[] {
                    new DataPoint(d1, weekSteps.get(0)),
                    new DataPoint(d2, weekSteps.get(1)),
                    new DataPoint(d3, weekSteps.get(2)),
                    new DataPoint(d4, weekSteps.get(3)),
                    new DataPoint(d5, weekSteps.get(4)),
                    new DataPoint(d6,weekSteps.get(5)),
                    new DataPoint(d7,weekSteps.get(6))
            };
            weekStack.push(weekSteps);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);

        graph.removeAllSeries();

        graph.addSeries(series);
        graph.getGridLabelRenderer().resetStyles();

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getGridLabelRenderer().setHorizontalLabelsAngle(135);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXValue());
        graph.getGridLabelRenderer().setNumHorizontalLabels(7);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d7.getTime());
        if(maxStep<20000) {
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(20000);
        }
        else if (maxStep<32000){
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(32000);
        }
        else if (maxStep<40000){
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(40000);
        }
        else if (maxStep<64000){
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(64000);
        }

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);
    }

    private String getDateString(Date calender)//converts a single Date object into a string
    {
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
        return formatterDate.format(calender);
    }

    private void nextWeek()
    {

        Log.d(tag,"nextWeek() called");
        firstDayOfWeek.add(firstDayOfWeek.DATE,+7);
        if(firstDayOfWeek.after(thisWeekStart))
        {
            Log.d(tag,"week too far ahead");
            firstDayOfWeek.add(firstDayOfWeek.DATE,-7);
            binding.imageButton2.setVisibility(View.GONE);
        }
        //Log.i(tag,"firstDayOfWeek milli="+getDateString(firstDayOfWeek.getTime()));
        //Log.i(tag,"thisWeekStart milli="+getDateString(thisWeekStart.getTime()));
        if(getDateString(firstDayOfWeek.getTime()).equals(getDateString(thisWeekStart.getTime())))
        {
            Log.i(tag,"This week selected");
            binding.imageButton2.setVisibility(View.GONE);
        }
        else
        {
            Log.i(tag,"Not this week selected");
            binding.imageButton2.setVisibility(View.VISIBLE);
        }
    }
    private void lastWeek()
    {
        Log.i(tag,"lastWeek() called");
        binding.imageButton2.setVisibility(View.VISIBLE);
        Log.i(tag,"old time="+getDateString(firstDayOfWeek.getTime()));
        firstDayOfWeek.add(Calendar.DATE,-7);
        Log.i(tag,"new time="+getDateString(firstDayOfWeek.getTime()));

    }
    private void setStartOfThisWeek()//sets firstDayOfWeek to this week
    {
        Log.i(tag,"setStartOfThisWeek() called");
        firstDayOfWeek = Calendar.getInstance(TimeZone.getTimeZone("GB"), Locale.UK);
        firstDayOfWeek.setTimeInMillis(System.currentTimeMillis());
        firstDayOfWeek.setFirstDayOfWeek(Calendar.MONDAY);
        firstDayOfWeek.set(Calendar.DAY_OF_WEEK,firstDayOfWeek.getActualMaximum(Calendar.DAY_OF_WEEK)+2);

        thisWeekStart = Calendar.getInstance(TimeZone.getTimeZone("GB"), Locale.UK);
        thisWeekStart.setTimeInMillis(System.currentTimeMillis());
        thisWeekStart.setFirstDayOfWeek(Calendar.MONDAY);
        thisWeekStart.set(Calendar.DAY_OF_WEEK,thisWeekStart.getActualMaximum(Calendar.DAY_OF_WEEK)+2);
        binding.imageButton2.setVisibility(View.GONE);
        Log.d(tag,""+firstDayOfWeek.getTime()+" "+thisWeekStart.getTime());
    }

    private ArrayList<Date> getWeekPeriod(Calendar startOfPeriodCalender) //calculates every date for the week given a
                                                                        // calender object starting on the first day of the week.
    {
        ArrayList<Date> dates = new ArrayList<>();
        dates.add(startOfPeriodCalender.getTime());
        startOfPeriodCalender.add(Calendar.DATE,+1);
        dates.add(startOfPeriodCalender.getTime());
        startOfPeriodCalender.add(Calendar.DATE,+1);
        dates.add(startOfPeriodCalender.getTime());
        startOfPeriodCalender.add(Calendar.DATE,+1);
        dates.add(startOfPeriodCalender.getTime());
        startOfPeriodCalender.add(Calendar.DATE,+1);
        dates.add(startOfPeriodCalender.getTime());
        startOfPeriodCalender.add(Calendar.DATE,+1);
        dates.add(startOfPeriodCalender.getTime());
        startOfPeriodCalender.add(Calendar.DATE,+1);
        dates.add(startOfPeriodCalender.getTime());
        startOfPeriodCalender.add(Calendar.DATE,-6);

        return dates;
    }

    private ArrayList<String> getFullWeekDates()//converts GetWeekPeriod's array to array of strings for parsing
    {
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<Date> weekDates = getWeekPeriod(firstDayOfWeek);
        dates.add(getDateString(weekDates.get(0)));
        dates.add(getDateString(weekDates.get(1)));
        dates.add(getDateString(weekDates.get(2)));
        dates.add(getDateString(weekDates.get(3)));
        dates.add(getDateString(weekDates.get(4)));
        dates.add(getDateString(weekDates.get(5)));
        dates.add(getDateString(weekDates.get(6)));

        return dates;
    }

    private ArrayList<Integer> getWeekSteps()
    {
        String fileStr = getFileFull(getBaseContext());//gets file holding dates and steps
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.addAll(Arrays.asList(fileStr.split(seperator)));
        ArrayList<Integer> week = new ArrayList<>();
        for(int i=0;i<=6;i++)
        {
            week.add(0);
        }
        ArrayList<String> weekDays = getFullWeekDates();

        Log.d(tag,weekDays.get(0)+weekDays.get(6)+"");
        String regex1 = ".*"+weekDays.get(0)+"";
        String regex2 = ".*"+weekDays.get(1)+"";
        String regex3 = ".*"+weekDays.get(2)+"";
        String regex4 = ".*"+weekDays.get(3)+"";
        String regex5 = ".*"+weekDays.get(4)+"";
        String regex6 = ".*"+weekDays.get(5)+"";
        String regex7 = ".*"+weekDays.get(6)+"";
        Pattern pattern;
        Matcher matcher;

        for(String str:stringArrayList)
        {
            //Log.d(tag,"checking matches");
            if(isAllNull(weekDays))
            {
                break;
            }
            pattern = Pattern.compile(regex1);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(0,Integer.valueOf(holder.get(0)));
                weekDays.set(0,null);
            }
            pattern = Pattern.compile(regex2);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(1,Integer.valueOf(holder.get(0)));
                weekDays.set(1,null);
            }
            pattern = Pattern.compile(regex3);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                //Log.d(tag,"Weds MATCH");
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(2,Integer.valueOf(holder.get(0)));
                weekDays.set(2,null);
            }
            pattern = Pattern.compile(regex4);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                //Log.d(tag,"Thu MATCH");
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(3,Integer.valueOf(holder.get(0)));
                weekDays.set(3,null);
            }
            pattern = Pattern.compile(regex5);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                //Log.d(tag,"Fri MATCH");
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(4,Integer.valueOf(holder.get(0)));
                weekDays.set(4,null);
            }
            pattern = Pattern.compile(regex6);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(5,Integer.valueOf(holder.get(0)));
                weekDays.set(5,null);
            }
            pattern = Pattern.compile(regex7);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(6,Integer.valueOf(holder.get(0)));
                weekDays.set(6,null);
            }
        }

        return week;
    }

    public boolean isAllNull(Iterable<?> array)
    {
        for (Object element:array)
            if(element!=null) return false;
        return true;
    }

    private String getFileFull(Context context)
    {
        Util util = new Util();
        return util.getfile(context);
    }
}
