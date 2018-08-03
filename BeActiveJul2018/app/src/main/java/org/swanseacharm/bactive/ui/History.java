package org.swanseacharm.bactive.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
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
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class History extends AppCompatActivity {

    private String tag = "History";
    private String deliminator = ",";//separates date and respective steps
    private String seperator = "/n";//separates date and step pairs

    private Calendar firstDayOfWeek;//current week User is looking at
    private Calendar thisWeekStart;//start date of this week (for checking)
    private Stack<ArrayList<Integer>> weekStack = new Stack<>();//stack to hold week data to the future of the users current view. speeds up user experience

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
        setStartOfThisWeek();//initialise week to this week
        graph = findViewById(R.id.graph);
        updateGraph(false);//initialise graph


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

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.bactivelogo);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        overridePendingTransition(0,0);//make transition instant
    }

    private void updateGraph(boolean nextWeek)
    {
        int maxStep = 0;
        DataPoint[] data;
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
            maxStep = Collections.max(weekSteps);//set maxStep to largest step count

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
            maxStep = Collections.max(weekSteps);//set maxStep to largest step count
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);

        graph.removeAllSeries();//clear graph

        graph.addSeries(series);//add current weeks step data to graph

        //style the graph

        graph.getGridLabelRenderer().resetStyles();

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getGridLabelRenderer().setHorizontalLabelsAngle(135);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXValue());
        graph.getGridLabelRenderer().setNumHorizontalLabels(7);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);

        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);

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
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd",Locale.ENGLISH);//date format for data on disk
        return formatterDate.format(calender);
    }

    private void nextWeek()//set calender to 1 week in the future if not already on current week
    {

        Log.d(tag,"nextWeek() called");
        firstDayOfWeek.add(Calendar.DATE,+7);
        if(firstDayOfWeek.after(thisWeekStart))
        {
            Log.d(tag,"week too far ahead");
            firstDayOfWeek.add(Calendar.DATE,-7);
            binding.imageButton2.setVisibility(View.GONE);//get rid of forward button
        }
        //Log.i(tag,"firstDayOfWeek milli="+getDateString(firstDayOfWeek.getTime()));
        //Log.i(tag,"thisWeekStart milli="+getDateString(thisWeekStart.getTime()));
        if(getDateString(firstDayOfWeek.getTime()).equals(getDateString(thisWeekStart.getTime())))
        {
            Log.d(tag,"This week selected");
            binding.imageButton2.setVisibility(View.GONE);
        }
        else
        {
            Log.d(tag,"Not this week selected");
            binding.imageButton2.setVisibility(View.VISIBLE);//set forward button to visible
        }
    }
    private void lastWeek()//set calender to 1 week in to the past of current week
    {
        Log.d(tag,"lastWeek() called");
        binding.imageButton2.setVisibility(View.VISIBLE);//make forward button visible
        Log.v(tag,"old time="+getDateString(firstDayOfWeek.getTime()));
        firstDayOfWeek.add(Calendar.DATE,-7);
        Log.v(tag,"new time="+getDateString(firstDayOfWeek.getTime()));
    }
    private void setStartOfThisWeek()//sets firstDayOfWeek to this week and initialises calenders
    {
        Log.d(tag,"setStartOfThisWeek() called");
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

    private ArrayList<Integer> getWeekSteps()//returns amount of steps from selected week in an ordered array
    {
        String fileStr = getFileFull(getBaseContext());//gets file holding dates and steps
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.addAll(Arrays.asList(fileStr.split(seperator)));//split file data up for parsing
        //initialise week data for return
        ArrayList<Integer> week = new ArrayList<>();
        for(int i=0;i<=6;i++)
        {
            week.add(0);
        }

        ArrayList<String> weekDays = getFullWeekDates();//get dates to check against

        Log.d(tag,weekDays.get(0)+weekDays.get(6)+"");
        //setup regex strings for checking
        String regex1 = ".*"+weekDays.get(0)+"";
        String regex2 = ".*"+weekDays.get(1)+"";
        String regex3 = ".*"+weekDays.get(2)+"";
        String regex4 = ".*"+weekDays.get(3)+"";
        String regex5 = ".*"+weekDays.get(4)+"";
        String regex6 = ".*"+weekDays.get(5)+"";
        String regex7 = ".*"+weekDays.get(6)+"";
        Pattern pattern;
        Matcher matcher;

        for(String str:stringArrayList)//for every string in the file separated by a /n
        {
            //if all dates are matched then stop searching for data
            if(isAllNull(weekDays))
            {
                break;
            }
            pattern = Pattern.compile(regex1);
            matcher = pattern.matcher(str);
            //check if string has this date for all dates
            if(matcher.matches())
            {
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));//split up the string if it's matched
                week.set(0,Integer.valueOf(holder.get(0)));//put the steps for that day into week
                weekDays.set(0,null);//make this days weekDays position in array null to not match again and not that is is done
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

                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(2,Integer.valueOf(holder.get(0)));
                weekDays.set(2,null);
            }
            pattern = Pattern.compile(regex4);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
                ArrayList<String> holder = new ArrayList<>();
                holder.addAll(Arrays.asList(str.split(deliminator)));
                week.set(3,Integer.valueOf(holder.get(0)));
                weekDays.set(3,null);
            }
            pattern = Pattern.compile(regex5);
            matcher = pattern.matcher(str);
            if(matcher.matches())
            {
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

        return week;//return the ordered weeks steps
    }

    public boolean isAllNull(Iterable<?> array)//checks if all items of an array are null
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
    //TODO get database information and graph group average and to 25%
}
