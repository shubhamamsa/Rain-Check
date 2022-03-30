package com.example.rain_check;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.*;

public class SelectCity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.rain_check.DATA";
    private String lat, lon, name;

    final private String accuweather_api_key = "8kQdPPmGyBEyksP57PE0Y3NmUbIBQOpc";
    final private String data_api_key = "nZhvEJPC6yO5a8ZJfqY7rlOupHAOgaqFnfOfO2G3ppIEzjnbeXWmdJjtmGGOs1T0";
    final private String data_app_id = "data-pjzvj";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().setStatusBarColor(Color.parseColor("#111230"));
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(Color.parseColor("#111230"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SelectCity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        else {
            checkGPS();
            getCurrentLocation();
        }

        Button btnLocation;
        Button btnSearch;
        Button adminPage;
        EditText cityName;
        btnLocation = findViewById(R.id.btnGetLocation);
        btnSearch = findViewById(R.id.btnSearch);
        adminPage = findViewById(R.id.admin_page);
        cityName = findViewById(R.id.cityName);

        btnLocation.setOnClickListener(v -> {

                if (lat != null) {
                    try {
                        retrieveData(lat, lon, null);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error fetching result", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    checkGPS();
                    getCurrentLocation();
                    Toast.makeText(this, "Location not found. Try again", Toast.LENGTH_SHORT).show();
                }
        });

        btnSearch.setOnClickListener(v -> {
            String city = cityName.getText().toString();
            if(!city.equals("") && city != null) {
                try {
                    fetchResult(city);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
//                Toast.makeText(SelectCity.this,, Toast.LENGTH_SHORT).show();

        });

        adminPage.setOnClickListener(v -> {
            PopupAdminLogin adminLogin = new PopupAdminLogin();
            adminLogin.showPopupWindow(v);
        });
    }

    private void startLoading() {
        LinearLayout form = findViewById(R.id.form_place);
        LinearLayout loading_bar = findViewById(R.id.loading_bar);
        Button admin = findViewById(R.id.admin_page);
        form.setVisibility(View.GONE);
        admin.setVisibility(View.GONE);
        loading_bar.setVisibility(View.VISIBLE);
    }

    private void endLoading()   {
        LinearLayout form = findViewById(R.id.form_place);
        LinearLayout loading_bar = findViewById(R.id.loading_bar);
        Button admin = findViewById(R.id.admin_page);
        loading_bar.setVisibility(View.GONE);
        form.setVisibility(View.VISIBLE);
        admin.setVisibility(View.VISIBLE);
    }


    private void checkGPS() {
        LocationRequest locationRequest;
        locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        LocationSettingsRequest.Builder builderGPS = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);
        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builderGPS.build());

        locationSettingsResponseTask.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
            } catch (ApiException e) {
                if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(SelectCity.this, 101);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                } else if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(SelectCity.this, "Settings unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SelectCity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(60000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationCallback mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            lat = String.valueOf(location.getLatitude());
                            lon = String.valueOf(location.getLongitude());
                        }
                    }
                }
            };
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void retrieveData(String lat, String lon, String name) throws JSONException {

        startLoading();

        JSONObject data = new JSONObject();
        data.put("Latitude", lat);
        data.put("Longitude", lon);
        if(name != null)
            data.put("City", name);

        Intent weatherDisp = new Intent(this, WeatherDescription.class);

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
                                                            weatherDisp.putExtra(EXTRA_MESSAGE, data.toString());
                                                            startActivity(weatherDisp);

                                                            endLoading();

                                                        } catch (JSONException e) {
                                                            Toast.makeText(SelectCity.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                                                            endLoading();
                                                        }
                                                    }, error -> {
                                                        Toast.makeText(SelectCity.this, "No internet or server error", Toast.LENGTH_SHORT).show();
                                                        endLoading();
                                                    });
                                            Volley.newRequestQueue(getApplicationContext()).add(weatherData);

                                        } catch (JSONException e) {
                                            Toast.makeText(SelectCity.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                                            endLoading();
                                        }
                                    }
                                }, error -> {
                                    Toast.makeText(SelectCity.this, "No internet or server error", Toast.LENGTH_SHORT).show();
                                    endLoading();
                                });
                        Volley.newRequestQueue(getApplicationContext()).add(weatherData);

                    } catch (JSONException e) {
                        Toast.makeText(SelectCity.this, "Error fetching result", Toast.LENGTH_SHORT).show();
                        endLoading();
                    }
                }, error -> {
                    Toast.makeText(SelectCity.this, "No internet or server error", Toast.LENGTH_SHORT).show();
                    endLoading();
                });
        Volley.newRequestQueue(getApplicationContext()).add(locationKey);
    }

    private void fetchResult(String cityName)   throws Exception {
        startLoading();
        cityName = cityName.toLowerCase(Locale.ROOT);
        Intent weatherDisp = new Intent(this, WeatherDescription.class);
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n    \"collection\":\"weather_data\",\n    \"database\":\"rain_meter_data\",\n    \"dataSource\":\"RainCheckCluster\",\n    \"filter\": {\"City\":\""+cityName+"\"}\n\n}");
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://data.mongodb-api.com/app/"+data_app_id+"/endpoint/data/beta/action/findOne")
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
                        Toast.makeText(SelectCity.this, "Failed", Toast.LENGTH_SHORT).show();
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
                            try {
                                JSONObject data = new JSONObject(response.body().string());
                                String finalData = data.getString("document");

                                if(finalData.equals("null"))
                                    Toast.makeText(SelectCity.this, "No city found in database", Toast.LENGTH_SHORT).show();
                                else {
                                    weatherDisp.putExtra(EXTRA_MESSAGE, finalData);
                                    startActivity(weatherDisp);
                                }

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                            endLoading();
                        }
                    });
                }
                else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SelectCity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                            Log.i("Message", response.toString());
                            endLoading();
                        }
                    });
                }
            }
        });

    }

}