package com.example.rain_check;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;

public class WeatherDescription extends AppCompatActivity {

    final private String degree = "°";
    private final IconPack iconPack = new IconPack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_description);
        Intent intent = getIntent();
        String weather = intent.getStringExtra(SelectCity.EXTRA_MESSAGE);
        JSONObject weather_data;

        try {
            weather_data = new JSONObject(weather);
            setBackground(weather_data);
            updateWeather(weather_data);
            updateThreeDaysDetails(weather_data);
            todayDetail(weather_data);
            currentCondition(weather_data);
            next5DayForecast(weather_data);

            JSONArray forecastData = weather_data.getJSONArray("Forecast");
            View.OnLongClickListener longClickListener = v -> {
                try {
                        PopUpClass popUpClass = new PopUpClass();
                        int index = (int)v.getId()-1;
                        popUpClass.showPopupWindow(v, forecastData.getJSONObject(index));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            };

            for(int i=0;i<weather_data.getJSONArray("Forecast").length();i++) {
                RelativeLayout layout = findViewById(i+1);
                layout.setOnLongClickListener(longClickListener);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setBackground(JSONObject weather_data) throws JSONException {

        ScrollView mainView = findViewById(R.id.main_view);

        Long sunRise = weather_data.getJSONArray("Forecast").getJSONObject(0).getLong("SunRise");
        Long sunSet = weather_data.getJSONArray("Forecast").getJSONObject(0).getLong("SunSet");

        Long currentTimestamp = (Instant.now().toEpochMilli())/1000;
        if(currentTimestamp >= sunRise && currentTimestamp <= sunSet) {
            mainView.setBackgroundResource(R.drawable.day_bg_4);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getWindow().setStatusBarColor(Color.parseColor("#299bc0"));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(Color.parseColor("#299bc0"));
        }
        else {
            mainView.setBackgroundResource(R.drawable.night_bg_5);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getWindow().setStatusBarColor(Color.parseColor("#101b2f"));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(Color.parseColor("#101b2f"));
        }
    }

    private void updateWeather(JSONObject weather_data) throws JSONException {
        TextView location = findViewById(R.id.location);
        TextView temperature = findViewById(R.id.temperature);
        ImageView weather_icon = findViewById(R.id.weather_icon);
        TextView weatherType = findViewById(R.id.weather_type);

        String city = weather_data.getString("City");
        String firstLetStr = city.substring(0, 1);
        String remLetStr = city.substring(1);
        firstLetStr = firstLetStr.toUpperCase();
        remLetStr = remLetStr.toLowerCase();
        city = firstLetStr + remLetStr;
        String temp = String.valueOf((int)Math.round(weather_data.getDouble("CurrentTemperature")));
        String wType = weather_data.getString("WeatherType");

        location.setText(city);
        temperature.setText(temp+degree);
        weatherType.setText(wType);
        weather_icon.setImageResource(iconPack.IconID(wType));
    }

    private void updateThreeDaysDetails(JSONObject weather_data) throws JSONException {
        TextView[] temperature = new TextView[3];
        TextView[] realFeel = new TextView[3];
        TextView[] dayOfWeek = new TextView[2];
        ImageView[] weatherIcon = new ImageView[3];

        weatherIcon[0] = findViewById(R.id.today_icon);
        weatherIcon[1] = findViewById(R.id.tom_icon);
        weatherIcon[2] = findViewById(R.id.dtom_icon);

        temperature[0] = findViewById(R.id.today_temp);
        temperature[1] = findViewById(R.id.tom_temp);
        temperature[2] = findViewById(R.id.dtom_temp);

        realFeel[0] = findViewById(R.id.today_real_feel);
        realFeel[1] = findViewById(R.id.tom_real_feel);
        realFeel[2] = findViewById(R.id.dtom_real_feel);

        dayOfWeek[0] = findViewById(R.id.tom_sch);
        dayOfWeek[1] = findViewById(R.id.dtom_sch);

        JSONArray overview = weather_data.getJSONArray("Forecast");

        for(int i=0;i<3;i++)    {
           String temp = String.valueOf((int)Math.round(overview.getJSONObject(i).getDouble("TemperatureMax")));
           String rFeel = String.valueOf((int)Math.round(overview.getJSONObject(i).getDouble("RealFeelMax")));
           Long epochDate = overview.getJSONObject(i).getLong("Date");
           LocalDateTime date = LocalDateTime.ofEpochSecond(epochDate, 5000, ZoneOffset.UTC);
           ZonedDateTime dateIST = date.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
           String day = dateIST.getDayOfWeek().toString().substring(0, 3).toUpperCase(Locale.ROOT);
           if(i > 0)
                dayOfWeek[i-1].setText(day);
           temperature[i].setText(temp+degree);
           realFeel[i].setText("Real Feel: "+rFeel+degree);
           weatherIcon[i].setImageResource(iconPack.IconID(overview.getJSONObject(i).getString("WeatherType")));
        }
    }

    private void todayDetail(JSONObject weather_data) throws JSONException {
        Long epochSunRise = weather_data.getJSONArray("Forecast").getJSONObject(0).getLong("SunRise");
        Long epochSunSet = weather_data.getJSONArray("Forecast").getJSONObject(0).getLong("SunSet");
        Long epochMoonRise = weather_data.getJSONArray("Forecast").getJSONObject(0).getLong("MoonRise");
        Long epochMoonSet = weather_data.getJSONArray("Forecast").getJSONObject(0).getLong("MoonSet");

        LocalDateTime sunRise = LocalDateTime.ofEpochSecond(epochSunRise, 5000, ZoneOffset.UTC);
        ZonedDateTime sunRiseIST = sunRise.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        LocalDateTime sunSet = LocalDateTime.ofEpochSecond(epochSunSet, 5000, ZoneOffset.UTC);
        ZonedDateTime sunSetIST = sunSet.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        LocalDateTime moonRise = LocalDateTime.ofEpochSecond(epochMoonRise, 5000, ZoneOffset.UTC);
        ZonedDateTime moonRiseIST = moonRise.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        LocalDateTime moonSet = LocalDateTime.ofEpochSecond(epochMoonSet, 5000, ZoneOffset.UTC);
        ZonedDateTime moonSetIST = moonSet.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        TextView sunR = findViewById(R.id.sun_rise);
        TextView sunS = findViewById(R.id.sun_set);
        TextView moonR = findViewById(R.id.moon_rise);
        TextView moonS = findViewById(R.id.moon_set);
        TextView sunD = findViewById(R.id.sun_duration);
        TextView moonD = findViewById(R.id.moon_duration);

        String sunRiseHour = appendZero(String.valueOf(sunRiseIST.getHour()));
        String sunRiseMin = appendZero(String.valueOf(sunRiseIST.getMinute()));
        String sunSetHour = appendZero(String.valueOf(sunSetIST.getHour()));
        String sunSetMin = appendZero(String.valueOf(sunSetIST.getMinute()));
        String moonRiseHour = appendZero(String.valueOf(moonRiseIST.getHour()));
        String moonRiseMin = appendZero(String.valueOf(moonRiseIST.getMinute()));
        String moonSetHour = appendZero(String.valueOf(moonSetIST.getHour()));
        String moonSetMin = appendZero(String.valueOf(moonSetIST.getMinute()));
        String sunDurationHour = appendZero(String.valueOf((epochSunSet-epochSunRise)/3600));
        String sunDurationMin = appendZero(String.valueOf(((epochSunSet-epochSunRise)/60)%60));
        String moonDurationHour = appendZero(String.valueOf((epochMoonSet-epochMoonRise)/3600));
        String moonDurationMin = appendZero(String.valueOf(((epochMoonSet-epochMoonRise)/60)%60));

        sunR.setText(sunRiseHour+":"+sunRiseMin);
        sunS.setText(sunSetHour+":"+sunSetMin);
        moonR.setText(moonRiseHour+":"+moonRiseMin);
        moonS.setText(moonSetHour+":"+moonSetMin);

        if(sunDurationHour == "01" && sunDurationMin == "01")
            sunD.setText(sunDurationHour + " hr\n" + sunDurationMin + " min");
        else if(sunDurationHour == "01" && sunDurationMin != "01")
            sunD.setText(sunDurationHour + " hr\n" + sunDurationMin + " mins");
        else if(sunDurationHour != "01" && sunDurationMin == "01")
            sunD.setText(sunDurationHour + " hrs\n" + sunDurationMin + " min");
        else
            sunD.setText(sunDurationHour + " hrs\n" + sunDurationMin + " mins");

        if(moonDurationHour == "01" && moonDurationMin == "01")
            moonD.setText(moonDurationHour + " hr\n" + moonDurationMin + " min");
        else if(moonDurationHour == "01" && moonDurationMin != "01")
            moonD.setText(moonDurationHour + " hr\n" + moonDurationMin + " mins");
        else if(moonDurationHour != "01" && moonDurationMin == "01")
            moonD.setText(moonDurationHour + " hrs\n" + moonDurationMin + " min");
        else
            moonD.setText(moonDurationHour + " hrs\n" + moonDurationMin + " mins");
    }
    private String appendZero(String time)  {
        if(time.length() == 1)
            time = "0"+time;
        return time;
    }

    private void currentCondition(JSONObject weather_data) throws JSONException {
        String current_temp = String.valueOf((int)Math.round(weather_data.getDouble("CurrentTemperature")));
        String current_real_feel = String.valueOf((int)Math.round(weather_data.getDouble("RealFeel")));
        String current_wind_speed = String.valueOf((int)Math.round(weather_data.getDouble("WindSpeed")));
        String current_wind_dir = weather_data.getString("WindDirection");
        String current_humidity = String.valueOf((int)Math.round(weather_data.getDouble("Humidity")));

        current_temp = current_temp+degree;
        current_real_feel = current_real_feel+degree;
        current_wind_speed = current_wind_speed+" km/h";
        current_humidity = current_humidity+"%";

        String current_wind = current_wind_dir+" "+current_wind_speed;

        TextView c_temp = findViewById(R.id.current_temperature);
        TextView c_real_feel = findViewById(R.id.current_real_feel);
        TextView c_wind = findViewById(R.id.current_wind);
        TextView c_humidity = findViewById(R.id.current_humidity);

        c_temp.setText(current_temp);
        c_real_feel.setText(current_real_feel);
        c_wind.setText(current_wind);
        c_humidity.setText(current_humidity);
    }

    private void next5DayForecast(JSONObject weather_data) throws JSONException {

        JSONArray overview = weather_data.getJSONArray("Forecast");

        LinearLayout parentLayout = findViewById(R.id.upcomingDayForecast);

        for(int i=0;i<overview.length();i++)    {

            RelativeLayout layout = new RelativeLayout(this);
            LinearLayout.LayoutParams parentLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(parentLayoutParam);
            layout.setId(i+1);
            parentLayoutParam.topMargin = 50;

            View lineBreaker = new View(this);
            RelativeLayout.LayoutParams lineBreakerParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lineBreaker.setLayoutParams(lineBreakerParam);
            lineBreakerParam.height = 3;
            lineBreakerParam.topMargin = 20;
            lineBreaker.setBackgroundColor(Color.parseColor("#dddddd"));

            int dateId = 10 * (i + 1);
            int tempMinId = 10 * (i + 1) + 1;
            int tempMaxId = 10 * (i + 1) + 2;
            int gradId = 10 * (i + 1) + 3;

            TextView date = new TextView(this);
            TextView temperatureMax = new TextView(this);
            TextView temperatureMin = new TextView(this);
            View grad = new View(this);
            ImageView weatherIcon = new ImageView(this);

            Typeface font = Typeface.createFromAsset(getAssets(),
                    "fonts/gudea.ttf");

            date.setId(dateId);
            temperatureMin.setId(tempMinId);
            temperatureMax.setId(tempMaxId);
            grad.setId(gradId);

            date.setTextColor(Color.parseColor("#e6e6e6"));
            temperatureMax.setTextColor(Color.parseColor("#e6e6e6"));
            temperatureMin.setTextColor(Color.parseColor("#e6e6e6"));

            date.setTypeface(font);
            temperatureMax.setTypeface(font);
            temperatureMin.setTypeface(font);

            grad.setBackgroundResource(R.drawable.temperature_gradient);
            weatherIcon.setImageResource(iconPack.IconID(overview.getJSONObject(i).getString("WeatherType")));
            weatherIcon.setColorFilter(Color.parseColor("#e6e6e6"));

            RelativeLayout.LayoutParams tempMinParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            tempMinParam.addRule(RelativeLayout.LEFT_OF, gradId);

            RelativeLayout.LayoutParams gradParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            gradParam.addRule(RelativeLayout.LEFT_OF, tempMaxId);
            gradParam.height = 10;
            gradParam.width = 250;
            gradParam.topMargin = 30;

            RelativeLayout.LayoutParams tempMaxParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            tempMaxParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            RelativeLayout.LayoutParams weatherIconParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            weatherIconParam.leftMargin = 225;
            weatherIconParam.height = 70;
            weatherIconParam.width = 70;

            weatherIcon.setLayoutParams(weatherIconParam);
            temperatureMin.setLayoutParams(tempMinParam);
            grad.setLayoutParams(gradParam);
            temperatureMax.setLayoutParams(tempMaxParam);

            String tempMin = String.valueOf((int)Math.round(overview.getJSONObject(i).getDouble("TemperatureMin")));
            String tempMax = String.valueOf((int)Math.round(overview.getJSONObject(i).getDouble("TemperatureMax")));
            long epochDate = overview.getJSONObject(i).getLong("Date");
            LocalDateTime stdDate = LocalDateTime.ofEpochSecond(epochDate, 5000, ZoneOffset.UTC);
            ZonedDateTime stdDateIST = stdDate.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
            String dayOfMonth = String.valueOf(stdDateIST.getDayOfMonth());
            if(dayOfMonth.length() == 1)
                dayOfMonth = '0'+dayOfMonth;
            String month = String.valueOf(stdDateIST.getMonth().getValue());
            if(month.length() == 1)
                month = '0'+month;
            String dateAndMonth = dayOfMonth+"/"+month;
            if(i == 0)
                date.setText("TODAY");
            else
                date.setText(dateAndMonth);

            date.setTextSize(16);
            temperatureMax.setTextSize(18);
            temperatureMin.setTextSize(18);

            temperatureMax.setPadding(20, 0, 0, 0);
            temperatureMin.setPadding(0, 0, 20, 0);

            temperatureMin.setText(tempMin+degree);
            temperatureMax.setText(tempMax+degree);

            layout.addView(date);
            layout.addView(weatherIcon);
            layout.addView(temperatureMin);
            layout.addView(grad);
            layout.addView(temperatureMax);
            parentLayout.addView(layout);
            if(i != overview.length()-1)
                parentLayout.addView(lineBreaker);
        }
    }

}