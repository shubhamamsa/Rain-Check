package com.example.rain_check;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class AdminPage extends AppCompatActivity {

    final private String mapbox_api_key = "";
    final private String accuweather_api_key = "";
    final private String data_api_key = "";
    final private String data_app_id = "";
    String lat = null, lon = null, name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        AutoCompleteTextView cityName = findViewById(R.id.admin_city_name);
        Button btnUpdateCity = findViewById(R.id.admin_update_city);
        cityName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                lat = null;
                lon = null;
                name = null;
                if(cityName.isPerformingCompletion() == false) {
                    if (s.length() > 2) {

                        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + s + ".json?access_token=" + mapbox_api_key;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                response -> {
                                    try {
                                        JSONObject reader = new JSONObject(response);
                                        JSONArray allCities = reader.getJSONArray("features");
                                        ArrayList<String> dispCities = new ArrayList<>();
                                        for (int i = 0; i < allCities.length(); i++)
                                            dispCities.add(allCities.getJSONObject(i).getString("place_name"));

                                        if (dispCities.size() == 0)
                                            dispCities.add("No place found");

                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AdminPage.this, android.R.layout.simple_list_item_1, dispCities);
                                        cityName.setAdapter(adapter);
                                        cityName.showDropDown();

                                        cityName.setOnItemClickListener((adapterView, view, i, l) -> {
                                            String value = (String) adapterView.getItemAtPosition(i);
//                                            cityName.setText("");
                                            try {
                                                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                            } catch (Exception e) {
                                                // TODO: handle exception
                                            }
                                            if (value != "No place found") {
                                                try {
                                                    lat = allCities.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getString(1);
                                                    lon = allCities.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getString(0);
                                                    name = allCities.getJSONObject(i).getString("text").toLowerCase(Locale.ROOT);
                                                } catch (JSONException e) {
                                                    Toast.makeText(AdminPage.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else    {
                                                lat = null;
                                                lon = null;
                                                name = null;
                                            }
                                        });

                                    } catch (JSONException e) {
                                        Toast.makeText(AdminPage.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                                    }

                                }, error -> Toast.makeText(AdminPage.this, "No internet or server error", Toast.LENGTH_SHORT).show());
                        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
                    } else {
                        cityName.dismissDropDown();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnUpdateCity.setOnClickListener(v -> {
            try {
                if(lat != null) {
                    retrieveData(lat, lon, name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }

    private void startLoading() {
        LinearLayout form = findViewById(R.id.admin_form_place);
        LinearLayout loading_bar = findViewById(R.id.admin_loading_bar);
        form.setVisibility(View.GONE);
        loading_bar.setVisibility(View.VISIBLE);
    }

    private void endLoading()   {
        LinearLayout form = findViewById(R.id.admin_form_place);
        LinearLayout loading_bar = findViewById(R.id.admin_loading_bar);
        loading_bar.setVisibility(View.GONE);
        form.setVisibility(View.VISIBLE);
    }

    private void retrieveData(String lat, String lon, String name) throws JSONException {

        startLoading();

        JSONObject data = new JSONObject();
        data.put("Latitude", lat);
        data.put("Longitude", lon);
        if(name != null)
            data.put("City", name);

        String urlLocation = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey="+accuweather_api_key+"&q="+lat+"%2C"+lon;
        JsonObjectRequest locationKey = new JsonObjectRequest(
                Request.Method.GET, urlLocation, null,
                response -> {
                    try {
                        String key = response.getString("Key");
                        data.put("Key", key);
                        if(name == null)
                            data.put("City", response.getString("EnglishName"));

                        String urlWeather = "http://dataservice.accuweather.com/currentconditions/v1/"+key+"?apikey="+accuweather_api_key+"&details=true";
                        JsonArrayRequest weatherData = new JsonArrayRequest(
                                Request.Method.GET, urlWeather, null,
                                new Response.Listener<JSONArray>() {

                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try {
                                            JSONObject resp = response.getJSONObject(0);
                                            data.put("CurrentTemperature", resp.getJSONObject("Temperature").getJSONObject("Metric").getString("Value"));
                                            data.put("RealFeel", resp.getJSONObject("RealFeelTemperature").getJSONObject("Metric").getString("Value"));
                                            data.put("WeatherType", resp.getString("WeatherText"));
                                            data.put("Humidity", resp.getString("RelativeHumidity"));
                                            data.put("WindSpeed", resp.getJSONObject("Wind").getJSONObject("Speed").getJSONObject("Metric").getString("Value"));
                                            data.put("WindDirection", resp.getJSONObject("Wind").getJSONObject("Direction").getString("English"));

                                            String urlForecast = "http://dataservice.accuweather.com//forecasts/v1/daily/5day/"+key+"?apikey="+accuweather_api_key+"&details=true&metric=true";
                                            JsonObjectRequest weatherData = new JsonObjectRequest(
                                                    Request.Method.GET, urlForecast, null,
                                                    response1 -> {
                                                        try {
                                                            JSONArray resp1 = response1.getJSONArray("DailyForecasts");
                                                            JSONArray filteredData = new JSONArray();
                                                            for(int i = 0; i< resp1.length(); i++)    {
                                                                JSONObject singleElement = new JSONObject();
                                                                singleElement.put("Date", resp1.getJSONObject(i).getString("EpochDate"));
                                                                singleElement.put("SunRise", resp1.getJSONObject(i).getJSONObject("Sun").getString("EpochRise"));
                                                                singleElement.put("SunSet", resp1.getJSONObject(i).getJSONObject("Sun").getString("EpochSet"));
                                                                singleElement.put("MoonRise", resp1.getJSONObject(i).getJSONObject("Moon").getString("EpochRise"));
                                                                singleElement.put("MoonSet", resp1.getJSONObject(i).getJSONObject("Moon").getString("EpochSet"));
                                                                singleElement.put("TemperatureMin", resp1.getJSONObject(i).getJSONObject("Temperature").getJSONObject("Minimum").getString("Value"));
                                                                singleElement.put("TemperatureMax", resp1.getJSONObject(i).getJSONObject("Temperature").getJSONObject("Maximum").getString("Value"));
                                                                singleElement.put("RealFeelMin", resp1.getJSONObject(i).getJSONObject("RealFeelTemperature").getJSONObject("Minimum").getString("Value"));
                                                                singleElement.put("RealFeelMax", resp1.getJSONObject(i).getJSONObject("RealFeelTemperature").getJSONObject("Maximum").getString("Value"));
                                                                singleElement.put("WeatherType", resp1.getJSONObject(i).getJSONObject("Day").getString("IconPhrase"));
                                                                singleElement.put("WindSpeed", resp1.getJSONObject(i).getJSONObject("Day").getJSONObject("Wind").getJSONObject("Speed").getString("Value"));
                                                                singleElement.put("WindDirection", resp1.getJSONObject(i).getJSONObject("Day").getJSONObject("Wind").getJSONObject("Direction").getString("English"));
                                                                filteredData.put(singleElement);
                                                            }
                                                            data.put("Forecast", filteredData);
                                                            insertData(data);

                                                        } catch (JSONException e) {
                                                            Toast.makeText(AdminPage.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                                                            endLoading();
                                                        }
                                                    }, error -> {
                                                Toast.makeText(AdminPage.this, "No internet or server error", Toast.LENGTH_SHORT).show();
                                                endLoading();
                                            });
                                            Volley.newRequestQueue(getApplicationContext()).add(weatherData);

                                        } catch (JSONException e) {
                                            Toast.makeText(AdminPage.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                                            endLoading();
                                        }
                                    }
                                }, error -> {
                            Toast.makeText(AdminPage.this, "No internet or server error", Toast.LENGTH_SHORT).show();
                            endLoading();
                        });
                        Volley.newRequestQueue(getApplicationContext()).add(weatherData);

                    } catch (JSONException e) {
                        Toast.makeText(AdminPage.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                        endLoading();
                    }
                }, error -> {
            Toast.makeText(AdminPage.this, "No internet or server error", Toast.LENGTH_SHORT).show();
            endLoading();
        });
        Volley.newRequestQueue(getApplicationContext()).add(locationKey);
    }

    private void insertData(JSONObject data)    {

        String formattedData = data.toString();
        Log.i("message", formattedData);
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n    \"collection\":\"weather_data\",\n    \"database\":\"rain_meter_data\",\n    \"dataSource\":\"Cluster0\",\n    \"document\": "+formattedData+"\n\n}");
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://data.mongodb-api.com/app/"+data_app_id+"/endpoint/data/beta/action/insertOne")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Access-Control-Request-Headers", "*")
                .addHeader("api-key", data_api_key)
                .build();
        Handler mHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminPage.this, "Failed to insert data", Toast.LENGTH_SHORT).show();
                        endLoading();
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if(response.isSuccessful()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            endLoading();
                            Toast.makeText(AdminPage.this, "Successfully inserted data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            endLoading();
                            Toast.makeText(AdminPage.this, "Failed to insert data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}