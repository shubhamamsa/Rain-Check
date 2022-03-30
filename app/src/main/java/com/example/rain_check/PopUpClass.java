package com.example.rain_check;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

public class PopUpClass extends Activity {
    //PopupWindow display method

    private IconPack iconPack = new IconPack();

    public void showPopupWindow(final View view, JSONObject data) {


        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_layout, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });

        // Dim the background
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.5f;
        wm.updateViewLayout(container, p);

        try {
            setWeatherData(data, popupView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setWeatherData(JSONObject data, View popupView) throws JSONException {

        String degree = "°";

        TextView popupWeatherDate = popupView.findViewById(R.id.popup_weather_date);
        TextView popupTemperature = popupView.findViewById(R.id.popup_temperature);
        TextView popupWeatherType = popupView.findViewById(R.id.popup_weather_type);
        TextView popupCurrentTemperature = popupView.findViewById(R.id.popup_current_temperature);
        TextView popupRealFeel = popupView.findViewById(R.id.popup_real_feel);
        TextView popupWind = popupView.findViewById(R.id.popup_wind);

        ImageView weatherIcon = popupView.findViewById(R.id.popup_weather_icon);

        String temp = String.valueOf((int) Math.round(data.getDouble("TemperatureMax")));
        String real_feel = String.valueOf((int) Math.round(data.getDouble("RealFeelMax")));
        String weatherType = data.getString("WeatherType");
        String wind_speed = String.valueOf((int) Math.round(data.getDouble("WindSpeed")));
        String wind_dir = data.getString("WindDirection");

        Long epochDate = data.getLong("Date");
        LocalDateTime stdDate = LocalDateTime.ofEpochSecond(epochDate, 5000, ZoneOffset.UTC);
        ZonedDateTime stdDateIST = stdDate.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        String dayOfMonth = String.valueOf(stdDateIST.getDayOfMonth());
        String month = String.valueOf(stdDateIST.getMonth());
        month = month.toLowerCase(Locale.ROOT);
        month = month.substring(0, 1).toUpperCase(Locale.ROOT) + month.substring(1, 3);
        String dateAndMonth = dayOfMonth + " " + month;

        temp = temp + degree;
        real_feel = real_feel + degree;
        wind_speed = wind_speed + " km/h";

        String wind = wind_dir + " " + wind_speed;

        popupWeatherDate.setText(dateAndMonth);
        popupTemperature.setText(temp);
        popupWeatherType.setText(weatherType);
        popupCurrentTemperature.setText(temp);
        popupRealFeel.setText(real_feel);
        popupWind.setText(wind);
        weatherIcon.setImageResource(iconPack.IconID(weatherType));

        Long epochSunRise = data.getLong("SunRise");
        Long epochSunSet = data.getLong("SunSet");
        Long epochMoonRise = data.getLong("MoonRise");
        Long epochMoonSet = data.getLong("MoonSet");

        LocalDateTime sunRise = LocalDateTime.ofEpochSecond(epochSunRise, 5000, ZoneOffset.UTC);
        ZonedDateTime sunRiseIST = sunRise.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        LocalDateTime sunSet = LocalDateTime.ofEpochSecond(epochSunSet, 5000, ZoneOffset.UTC);
        ZonedDateTime sunSetIST = sunSet.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        LocalDateTime moonRise = LocalDateTime.ofEpochSecond(epochMoonRise, 5000, ZoneOffset.UTC);
        ZonedDateTime moonRiseIST = moonRise.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        LocalDateTime moonSet = LocalDateTime.ofEpochSecond(epochMoonSet, 5000, ZoneOffset.UTC);
        ZonedDateTime moonSetIST = moonSet.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        TextView sunR = popupView.findViewById(R.id.popup_sun_rise);
        TextView sunS = popupView.findViewById(R.id.popup_sun_set);
        TextView moonR = popupView.findViewById(R.id.popup_moon_rise);
        TextView moonS = popupView.findViewById(R.id.popup_moon_set);
        TextView sunD = popupView.findViewById(R.id.popup_sun_duration);
        TextView moonD = popupView.findViewById(R.id.popup_moon_duration);

        String sunRiseHour = appendZero(String.valueOf(sunRiseIST.getHour()));
        String sunRiseMin = appendZero(String.valueOf(sunRiseIST.getMinute()));
        String sunSetHour = appendZero(String.valueOf(sunSetIST.getHour()));
        String sunSetMin = appendZero(String.valueOf(sunSetIST.getMinute()));
        String moonRiseHour = appendZero(String.valueOf(moonRiseIST.getHour()));
        String moonRiseMin = appendZero(String.valueOf(moonRiseIST.getMinute()));
        String moonSetHour = appendZero(String.valueOf(moonSetIST.getHour()));
        String moonSetMin = appendZero(String.valueOf(moonSetIST.getMinute()));
        String sunDurationHour = appendZero(String.valueOf((epochSunSet - epochSunRise) / 3600));
        String sunDurationMin = appendZero(String.valueOf(((epochSunSet - epochSunRise) / 60) % 60));
        String moonDurationHour = appendZero(String.valueOf((epochMoonSet - epochMoonRise) / 3600));
        String moonDurationMin = appendZero(String.valueOf(((epochMoonSet - epochMoonRise) / 60) % 60));

        sunR.setText(sunRiseHour + ":" + sunRiseMin);
        sunS.setText(sunSetHour + ":" + sunSetMin);
        moonR.setText(moonRiseHour + ":" + moonRiseMin);
        moonS.setText(moonSetHour + ":" + moonSetMin);

        if (sunDurationHour == "01" && sunDurationMin == "01")
            sunD.setText(sunDurationHour + " hr\n" + sunDurationMin + " min");
        else if (sunDurationHour == "01" && sunDurationMin != "01")
            sunD.setText(sunDurationHour + " hr\n" + sunDurationMin + " mins");
        else if (sunDurationHour != "01" && sunDurationMin == "01")
            sunD.setText(sunDurationHour + " hrs\n" + sunDurationMin + " min");
        else
            sunD.setText(sunDurationHour + " hrs\n" + sunDurationMin + " mins");

        if (moonDurationHour == "01" && moonDurationMin == "01")
            moonD.setText(moonDurationHour + " hr\n" + moonDurationMin + " min");
        else if (moonDurationHour == "01" && moonDurationMin != "01")
            moonD.setText(moonDurationHour + " hr\n" + moonDurationMin + " mins");
        else if (moonDurationHour != "01" && moonDurationMin == "01")
            moonD.setText(moonDurationHour + " hrs\n" + moonDurationMin + " min");
        else
            moonD.setText(moonDurationHour + " hrs\n" + moonDurationMin + " mins");

    }

    private String appendZero(String time)  {
        if(time.length() == 1)
            time = "0"+time;
        return time;
    }
}
