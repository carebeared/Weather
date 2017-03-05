package com.a5corp.weather.fragment;


import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.a5corp.weather.R;
import com.a5corp.weather.internet.CheckConnection;
import com.a5corp.weather.model.WeatherForecast;
import com.a5corp.weather.preferences.Preferences;
import com.a5corp.weather.utilities.Constants;
import com.a5corp.weather.utilities.CustomFormatter;
import com.a5corp.weather.utilities.XFormatter;
import com.a5corp.weather.utilities.YFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.a5corp.weather.utilities.Constants.DAYS_PARAM;
import static com.a5corp.weather.utilities.Constants.FORMAT_PARAM;
import static com.a5corp.weather.utilities.Constants.FORMAT_VALUE;
import static com.a5corp.weather.utilities.Constants.UNITS_PARAM;
import static com.a5corp.weather.utilities.Constants.UNITS_VALUE;

/**
 * A simple {@link Fragment} subclass.
 */
public class GraphsFragment extends Fragment {

    View rootView;
    LineChart chart;
    public List<WeatherForecast> mForecastList;
    CheckConnection mCheckConnection;
    String[] dateArray;
    CustomFormatter customFormatter;
    YFormatter yFormatter;
    Handler handler;
    Preferences preferences;


    public GraphsFragment() {
        // Required empty public constructor
        preferences = new Preferences(getContext());
    }

    private void chartEntry() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_graphs, container, false);
        chart = (LineChart) rootView.findViewById(R.id.chart);
        customFormatter = new CustomFormatter();
        setTemperatureView();
        yFormatter = new YFormatter();
        return rootView;
    }

    private void setTemperatureView() {
        Description d = new Description();
        d.setText("Temperature, " + getString(R.string.c));
        chart.setDescription(d);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setMaxHighlightDistance(300);
        chart.setPinchZoom(true);
        chart.getLegend().setEnabled(false);

        formatDate();
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new XFormatter(dateArray));

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLines(true);
        yAxis.enableGridDashedLine(5f , 10f , 0f);
        yAxis.setGridColor(Color.parseColor("#333333"));
        yAxis.setXOffset(15);
        yAxis.setValueFormatter(yFormatter);

        chart.getAxisRight().setEnabled(false);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < mForecastList.size(); ++i) {
            float temperatureDay = mForecastList.get(i).getTemperatureDay();
            entries.add(new Entry(i , temperatureDay));
        }

        LineDataSet set;
        if (chart.getData() != null) {
            chart.getData().removeDataSet(chart.getData().getDataSetByIndex(
                    chart.getData().getDataSetCount() - 1
            ));
        }
        set = new LineDataSet(entries , "Day");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setDrawValues(false);
        set.setValueTextSize(12f);
        set.setColor(Color.parseColor("#E84E40"));
        set.setHighlightEnabled(false);
        set.setValueFormatter(customFormatter);

        LineData data = new LineData(set);
        chart.setData(data);
        chart.invalidate();
    }

    private void formatDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEE", Locale.getDefault());
        if (mForecastList != null) {
            int mSize = mForecastList.size();
            dateArray = new String[mSize];

            for (int i = 0; i < mSize; i++) {
                Date date = new Date(mForecastList.get(i).getDateTime() * 1000);
                String day = format.format(date);
                dateArray[i] = day;
            }
        }
    }

    private void getWeather() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = Uri.parse(Constants.WEATHER_DAILY)
                                    .buildUpon()
                                    .appendQueryParameter(Constants.QUERY_PARAM , preferences.getCity())
                                    .appendQueryParameter(Constants.FORMAT_PARAM , FORMAT_VALUE)
                                    .appendQueryParameter(UNITS_PARAM , UNITS_VALUE)
                                    .appendQueryParameter(DAYS_PARAM , Integer.toString(10))
                                    .build()
                                    .toString();
                            URL Url = new URL(url);
                            HttpURLConnection connection = (HttpURLConnection) Url.openConnection();
                            connection.addRequestProperty("x-api-key" , getString(R.string.open_weather_maps_app_id));

                        }
                        catch(Exception ex) {

                        }
                    }
                }
        ).start();
    }
}