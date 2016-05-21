package org.pillowsky.niceair;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RotationRing rotationRing;
    private TextView sensorValue;
    private TextView sensorText;

    private final Handler handler = new Handler();
    private final String apikey = "3a4acf999665687b17a2cfc6762f7251";
    private final String deviceId = "347956";
    private final String sensorId = "388985";
    private URL latestUrl;
    private String lastTimeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        rotationRing = (RotationRing) findViewById(R.id.rotation_ring);
        sensorValue = (TextView) findViewById(R.id.sensor_value);
        sensorText = (TextView) findViewById(R.id.sensor_text);

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
    }

    protected void refreshLatestData() {
        rotationRing.startRotation();
        Toast.makeText(getApplicationContext(), R.string.sensor_data_updating, Toast.LENGTH_SHORT).show();
        requestLatestData();
    }

    protected void requestLatestData() {
        final AsyncTask<Void, Void, JSONObject> task = new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void[] params) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) latestUrl.openConnection();
                    connection.addRequestProperty("U-ApiKey", apikey);
                    connection.connect();

                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    return new JSONObject(body.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    try {
                        String timestamp = result.getString("timestamp");
                        if (timestamp.equals(lastTimeStamp)) {
                            rotationRing.cancelRotation();
                            Toast.makeText(getApplicationContext(), R.string.sensor_data_no_update, Toast.LENGTH_LONG).show();
                        } else {
                            lastTimeStamp = timestamp;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    requestLatestData();
                                }
                            }, 10000);
                        }

                        swipeRefreshLayout.setRefreshing(false);
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
                            sensorText.setTextSize(R.string.sensor_text_severe_pollution);
                        } else {
                            rotationRing.setRingColor(Color.BLACK);
                            sensorText.setText(R.string.sensor_text_critical_pollution);
                        }

                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                rotationRing.cancelRotation();
                Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            }
        };
        task.execute();
    }
}
