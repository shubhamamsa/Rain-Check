package com.example.rain_check;

import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Locale;

public class IconPack {
    private HashMap<String, Integer> icons = new HashMap<>();
    IconPack()  {
        icons.put("sunny", R.drawable.ic_sun);
        icons.put("mostly sunny", R.drawable.ic_sun);
        icons.put("partly sunny", R.drawable.ic_partly_cloudy);
        icons.put("intermittent clouds", R.drawable.ic_cloud);
        icons.put("hazy sunshine", R.drawable.ic_sun);
        icons.put("mostly cloudy", R.drawable.ic_cloud);
        icons.put("cloudy", R.drawable.ic_cloud);
        icons.put("dreary (overcast)", R.drawable.ic_cloud);
        icons.put("fog", R.drawable.ic_cloud);
        icons.put("showers", R.drawable.ic_rain);
        icons.put("mostly cloudy w/ showers", R.drawable.ic_rain);
        icons.put("partly sunny w/ showers", R.drawable.ic_rain);
        icons.put("t-storms", R.drawable.ic_t_storm);
        icons.put("mostly cloudy w/ t-storms", R.drawable.ic_t_storm);
        icons.put("partly sunny w/ t-storms", R.drawable.ic_t_storm);
        icons.put("rain", R.drawable.ic_rain);
        icons.put("flurries", R.drawable.ic_snow);
        icons.put("mostly cloudy w/ flurries", R.drawable.ic_snow);
        icons.put("partly sunny w/ flurries", R.drawable.ic_snow);
        icons.put("snow", R.drawable.ic_snow);
        icons.put("mostly cloudy w/ snow", R.drawable.ic_snow);
        icons.put("ice", R.drawable.ic_ice);
        icons.put("sleet", R.drawable.ic_ice);
        icons.put("freezing rain", R.drawable.ic_snow_rain);
        icons.put("rain and snow", R.drawable.ic_snow_rain);
        icons.put("hot", R.drawable.ic_sun);
        icons.put("cold", R.drawable.ic_ice);
        icons.put("windy", R.drawable.ic_wind);
        icons.put("clear", R.drawable.ic_moon);
        icons.put("mostly clear", R.drawable.ic_moon);
        icons.put("partly cloudy", R.drawable.ic_partly_cloudy);
        icons.put("hazy moonlight", R.drawable.ic_cloudy_moon);
        icons.put("partly cloudy w/ showers", R.drawable.ic_cloudy_moon);
        icons.put("partly cloudy w/ t-storms", R.drawable.ic_cloudy_moon);
    }

    public Integer IconID(String weatherType)    {
        if(icons.containsKey(weatherType.toLowerCase(Locale.ROOT)))
            return icons.get(weatherType.toLowerCase(Locale.ROOT));
        return R.drawable.ic_sun;
    }
}
