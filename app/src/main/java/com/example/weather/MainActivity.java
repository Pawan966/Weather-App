package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private ImageView bgImg,Search,icon;
    private TextView CityNameTV,temp,conditionTV;
    private RecyclerView timeWeather;
    private TextInputEditText EnCity;
    private ArrayList<WeatherModal> weatherModalArrayList;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        homeRL=findViewById(R.id.home);
        loadingPB=findViewById(R.id.loading);
        bgImg=findViewById(R.id.bgImg);
        Search=findViewById(R.id.Search);
        icon=findViewById(R.id.icon);
        CityNameTV=findViewById(R.id.CityName);
        temp=findViewById(R.id.temp);
        conditionTV=findViewById(R.id.condition);
        timeWeather=findViewById(R.id.timeWeather);
        EnCity=findViewById(R.id.EnCity);
        weatherModalArrayList=new ArrayList<>();
        weatherAdapter=new WeatherAdapter(this,weatherModalArrayList);
        timeWeather.setAdapter(weatherAdapter);

        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLatitude(),location.getLongitude());
        getWeatherInfo(cityName);

        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=EnCity.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
                }else{
                    CityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.d("Request","Permitted");
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please Provide Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double latitude, double longitude){
       String cityName="City Not Found";
        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());

        try{
            List<Address> addresses=gcd.getFromLocation(latitude,longitude,10);

            for(Address adr:addresses){
                if(adr!=null){
                    String city=adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName=city;
                    }else{
                        Toast.makeText(this, "City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url="https://api.weatherapi.com/v1/forecast.json?key=8d89218685984380a3583720211008&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        CityNameTV.setText(cityName);
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherModalArrayList.clear();
                try {
                    String temperature=response.getJSONObject("current").getString("temp_c");
                    temp.setText(temperature+"°C");

                    int isDay=response.getJSONObject("current").getInt("is_day");

                    String condition=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionIcon)).into(icon);
                    conditionTV.setText(condition);

                    if(isDay==1){
                        Picasso.get().load("https://wallpapercave.com/wp/wp3057846.jpg").into(bgImg);
                    }else{
                        Picasso.get().load("https://i.pinimg.com/originals/6c/04/c6/6c04c6b013470efcb9474cf8e8f0456e.jpg").into(bgImg);
                    }

                    JSONObject forecastObj=response.getJSONObject("forecast");
                    JSONObject forcastO=forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forcastO.getJSONArray("hour");

                    for(int i=0;i<hourArray.length();i++)
                    {
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temper=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind_kph");
                        weatherModalArrayList.add(new WeatherModal(time,temper,img,wind));
                    }

                    weatherAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Tag",cityName);
                Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}