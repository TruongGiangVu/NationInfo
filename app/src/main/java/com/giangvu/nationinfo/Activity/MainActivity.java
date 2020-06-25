package com.giangvu.nationinfo.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;

import com.giangvu.nationinfo.Models.National;
import com.giangvu.nationinfo.R;
import com.giangvu.nationinfo.Services.JsonTasks;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private Spinner areas,countries;
    private TextView tvCountryName,tvPopulations,tvAcreage;
    private ImageView imgFlag;
    private CardView cardViewReload;
    private DecimalFormat formatter_int = new DecimalFormat("#,###,###");
    private NumberFormat formatter_double = new DecimalFormat("#,###,###0.00");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        internet();
        initView();
        initEvent();
    }
    private void prepareData(){
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "Loading data",
                "Loading. Please wait...", true);
        dialog.setCancelable(false);
        dialog.show();
        new JsonTasks().jsonReader("http://api.geonames.org/countryInfoJSON?formatted=true&lang=it&username=hieukut456&style=full",
                new JsonTasks.JsonTaskFullListener(){
                    @Override
                    public void onLoadSuccess(final ArrayList<National> nationsArrayList){
                        cardViewReload.setVisibility(View.GONE);
                        dialog.cancel();
                        final ArrayList<String> areaList =  new ArrayList<String>();
                        areaList.add("All");
                        for(int i=0;i<nationsArrayList.size();i++){
                            if(!areaList.contains(nationsArrayList.get(i).getContinentName())){
                                areaList.add(nationsArrayList.get(i).getContinentName());
                            }


                        }
                        ArrayAdapter<String> areaOfCountry = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,areaList);
                        areas.setAdapter(areaOfCountry);

                        areas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                final ArrayList<String> countryList = new ArrayList<String>();
                                if(position == 0){
                                  for(int i=0;i<nationsArrayList.size();i++){
                                      countryList.add(nationsArrayList.get(i).getCountryName());
                                  }

//                                Toast.makeText(MainActivity.this, ">" + areaList.get(position), Toast.LENGTH_LONG).show();
                                }
                                else{
                                    countryList.clear();
                                    for(int i=0;i<nationsArrayList.size();i++){
                                        if(nationsArrayList.get(i).getContinentName().equals(areaList.get(position))){
                                            countryList.add(nationsArrayList.get(i).getCountryName());
                                        }
                                    }
                                }
                                ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,countryList);
                                countries.setAdapter(countriesAdapter);
                                countries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        National nation = new National();
                                        for(int i=0;i<nationsArrayList.size();i++) {
                                            if(nationsArrayList.get(i).getCountryName().equals(countryList.get(position))){
                                                nation = nationsArrayList.get(i);
                                            }
                                        }
                                        Glide.with(MainActivity.this).load("https://img.geonames.org/flags/x/" +
                                                nation.getCountryCode().toLowerCase() + ".gif")

                                                .into(imgFlag);

                                        tvCountryName.setText(nation.getCountryName());
                                        tvPopulations.setText(formatter_int.format(Integer.parseInt(nation.getPopulation())));
                                        tvAcreage.setText(formatter_double.format(Double.parseDouble(nation.getAreaInSqkm()))+" Km\u00B2");
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                    }

                    @Override
                    public void onLoadFail(String error) {
                        dialog.cancel();
                        internet();
                    }
                });
    }
    private void initEvent() {
        cardViewReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internet();
            }
        });
    }
    private void initView() {
        areas = (Spinner)findViewById(R.id.spinner);
        countries = (Spinner)findViewById(R.id.spinner_country);
        cardViewReload = findViewById(R.id.cv_reload);
        tvAcreage = (TextView)findViewById(R.id.acreage);
        tvPopulations = (TextView)findViewById(R.id.populations);
        tvCountryName = (TextView)findViewById(R.id.CountryName);
        imgFlag = (ImageView)findViewById(R.id.imgFlag);
    }
    private void internet() {
        if (isOnline()) {
            prepareData();
        } else {
            android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(this);
            builder1.setTitle("Info");
            builder1.setMessage("Internet not available, Cross check your internet connectivity and try again.");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            cardViewReload.setVisibility(View.VISIBLE);
                        }
                    });
            android.app.AlertDialog alert11 = builder1.create();
            alert11.setCancelable(false);
            alert11.show();
        }
    }
    private boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            Toast.makeText(this, "No Internet connection!", Toast.LENGTH_LONG).show();
            return false;
        } else {
            Toast.makeText(this, "Internet connection!", Toast.LENGTH_LONG).show();
            return true;
        }
    }
}
