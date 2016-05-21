package org.pillowsky.niceair;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RotationRing rotationRing;
    private TextView sensorValue;
    private TextView sensorText;
    private Button startDate;
    private Button startTime;
    private Button endDate;
    private Button endTime;
    private Spinner intervalSpinner;
    private LineChart historyChart;

    private final Handler handler = new Handler();
    private final String apiKey = "3a4acf999665687b17a2cfc6762f7251";
    private final String deviceId = "347956";
    private final String sensorId = "388985";
    private URL latestUrl;
    private String lastTimeStamp;
    private GregorianCalendar startCalender;
    private GregorianCalendar endCalender;
    private int intervalValue = 1;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private LineData historyChartData;
    private LineDataSet historyChartDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        rotationRing = (RotationRing) findViewById(R.id.rotation_ring);
        sensorValue = (TextView) findViewById(R.id.sensor_value);
        sensorText = (TextView) findViewById(R.id.sensor_text);
        startDate = (Button) findViewById(R.id.start_date);
        startTime = (Button) findViewById(R.id.start_time);
        endDate = (Button) findViewById(R.id.end_date);
        endTime = (Button) findViewById(R.id.end_time);
        intervalSpinner = (Spinner) findViewById(R.id.interval_spinner);
        historyChart = (LineChart) findViewById(R.id.history_chart);

        try {
            latestUrl = new URL("http://api.yeelink.net/v1.0/device/" + deviceId + "/sensor/" + sensorId + "/datapoints");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLatestData();
            }
        });

        refreshLatestData();

        startCalender = new GregorianCalendar(new SimpleTimeZone(8 * 60 * 60 * 1000, "Asia/Shanghai"));
        startCalender.add(Calendar.DATE, -1);
        endCalender = new GregorianCalendar(new SimpleTimeZone(8 * 60 * 60 * 1000, "Asia/Shanghai"));

        startDate.setText(dateFormat.format(startCalender.getTime()));
        startTime.setText(timeFormat.format(startCalender.getTime()));
        endDate.setText(dateFormat.format(endCalender.getTime()));
        endTime.setText(timeFormat.format(endCalender.getTime()));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.interval_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(adapter);
        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                intervalValue = Integer.parseInt((String)parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        historyChartDataSet = new LineDataSet(null, "PM2.5 Value");
        historyChartDataSet.setDrawCubic(true);

        historyChartData = new LineData();
        historyChartData.addDataSet(historyChartDataSet);
        historyChart.setTouchEnabled(true);
        historyChart.setData(historyChartData);
        historyChart.notifyDataSetChanged();
        historyChart.invalidate();

        refreshHistoryData();
    }

    protected void onStartDateClick(View view) {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                startCalender.set(year, monthOfYear, dayOfMonth);
                startDate.setText(dateFormat.format(startCalender.getTime()));
            }
        }, startCalender.get(Calendar.YEAR), startCalender.get(Calendar.MONTH), startCalender.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    protected void onStartTimeClick(View view) {
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                startCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startCalender.set(Calendar.MINUTE, minute);
                startTime.setText(timeFormat.format(startCalender.getTime()));
            }
        }, startCalender.get(Calendar.HOUR_OF_DAY), startCalender.get(Calendar.MINUTE), true);
        dialog.show();
    }

    protected void onEndDateClick(View view) {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                endCalender.set(year, monthOfYear, dayOfMonth);
                endDate.setText(dateFormat.format(endCalender.getTime()));
            }
        }, endCalender.get(Calendar.YEAR), endCalender.get(Calendar.MONTH), endCalender.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    protected void onEndTimeClick(View view) {
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endCalender.set(Calendar.MINUTE, minute);
                endTime.setText(timeFormat.format(endCalender.getTime()));
            }
        }, endCalender.get(Calendar.HOUR_OF_DAY), endCalender.get(Calendar.MINUTE), true);
        dialog.show();
    }

    protected void onHistoryRefreshClick(View view) {
        historyChartDataSet.clear();
        historyChartData = new LineData();
        historyChartData.addDataSet(historyChartDataSet);
        historyChart.setData(historyChartData);
        refreshHistoryData();
    }

    protected void refreshLatestData() {
        rotationRing.startRotation();
        Toast.makeText(getApplicationContext(), R.string.latest_data_updating, Toast.LENGTH_SHORT).show();
        requestLatestData();
    }

    protected void requestLatestData() {
        AsyncTask<Void, Void, JSONObject> task = new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void[] params) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) latestUrl.openConnection();
                    connection.addRequestProperty("U-ApiKey", apiKey);
                    connection.connect();

                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    connection.disconnect();
                    return new JSONObject(body.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                swipeRefreshLayout.setRefreshing(false);

                if (result != null) {
                    try {
                        String timestamp = result.getString("timestamp");
                        if (timestamp.equals(lastTimeStamp)) {
                            rotationRing.endRotation();
                            Toast.makeText(getApplicationContext(), R.string.latest_data_no_update, Toast.LENGTH_LONG).show();
                        } else {
                            lastTimeStamp = timestamp;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    requestLatestData();
                                }
                            }, 10000);
                        }

                        double value = result.getDouble("value");
                        sensorValue.setText(String.valueOf(value));

                        if (value < 35) {
                            rotationRing.setRingColor(Color.GREEN);
                            sensorText.setText(R.string.sensor_text_excellent);
                        } else if (value < 75) {
                            rotationRing.setRingColor(Color.CYAN);
                            sensorText.setText(R.string.sensor_text_good);
                        } else if (value < 115) {
                            rotationRing.setRingColor(Color.YELLOW);
                            sensorText.setText(R.string.sensor_text_light_pollution);
                        } else if (value < 150) {
                            rotationRing.setRingColor(Color.MAGENTA);
                            sensorText.setText(R.string.sensor_text_medium_pollution);
                        } else if (value < 250) {
                            rotationRing.setRingColor(Color.RED);
                            sensorText.setText(R.string.sensor_text_severe_pollution);
                        } else {
                            rotationRing.setRingColor(Color.BLACK);
                            sensorText.setText(R.string.sensor_text_critical_pollution);
                        }

                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                rotationRing.endRotation();
                Toast.makeText(getApplicationContext(), R.string.latest_data_network_error, Toast.LENGTH_LONG).show();
            }
        };
        task.execute();
    }

    protected void refreshHistoryData() {
        Toast.makeText(getApplicationContext(), R.string.history_updating, Toast.LENGTH_SHORT).show();
        requestHistoryData(datetimeFormat.format(startCalender.getTime()), datetimeFormat.format(endCalender.getTime()), intervalValue, 1);
    }

    protected void requestHistoryData(final String start, final String end, final int interval, final int page) {
        AsyncTask<Void, Void, JSONArray> task = new AsyncTask<Void, Void, JSONArray>() {
            @Override
            protected JSONArray doInBackground(Void[] params) {
                try {
                    URL historyURL = new URL(String.format(Locale.getDefault(), "http://api.yeelink.net/v1.0/device/%s/sensor/%s.json?start=%s&end=%s&interval=%d&page=%d", deviceId, sensorId, start, end, interval, page));
                    HttpURLConnection connection = (HttpURLConnection) historyURL.openConnection();
                    connection.addRequestProperty("U-ApiKey", apiKey);
                    connection.connect();

                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    connection.disconnect();
                    return new JSONArray(body.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONArray result) {
                if (result != null) {
                    int length = result.length();
                    if (length == 200) requestHistoryData(start, end, interval, page + 1);

                    int count = historyChartDataSet.getEntryCount();
                    for (int i = 0; i < length; i++) {
                        try {
                            JSONObject node = result.getJSONObject(i);
                            historyChartData.addXValue(node.getString("timestamp"));
                            historyChartData.addEntry(new Entry((float)node.getDouble("value"), count + i), 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    historyChart.notifyDataSetChanged();
                    historyChart.invalidate();
                }
            }
        };
        task.execute();
    }
}
