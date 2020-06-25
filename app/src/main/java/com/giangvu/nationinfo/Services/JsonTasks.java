package com.giangvu.nationinfo.Services;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;


import com.giangvu.nationinfo.Models.National;

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

import java.util.ArrayList;
public class JsonTasks {
    private JsonTask jsonTask;

    public void jsonReader(String url, JsonTaskFullListener jsonTaskFullListener) {
        jsonTask = new JsonTask(url, jsonTaskFullListener);
        jsonTask.execute();
    }
    public interface JsonTaskFullListener{
        void onLoadSuccess(ArrayList<National> nationArrayList);
        void onLoadFail(String error);
    }
    public class JsonTask extends AsyncTask<String, String, String> {

        private String urlLink;
        private ArrayList<National> nationArrayList;
        private JsonTaskFullListener jsonTaskFullListener;
        public JsonTask(String urlLink, JsonTaskFullListener jsonTaskFullListener){
            this.urlLink=urlLink;
            this.jsonTaskFullListener = jsonTaskFullListener;
        }

        @Override
        protected String doInBackground(String... params) {
            if (TextUtils.isEmpty(urlLink)) {

                return "";
            }

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(urlLink);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");

                }
                nationArrayList = jsonToObj(buffer.toString());
                return "";
            } catch (MalformedURLException e) {
                e.printStackTrace();

                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("")) {
                jsonTaskFullListener.onLoadSuccess(nationArrayList);
            } else {
                jsonTaskFullListener.onLoadFail(result);
            }
        }
    }
    private ArrayList<National> jsonToObj(String json){

        ArrayList<National> nationArrayList = new ArrayList<>();
        String countryName = "";
        String continentName ="";
        String population = "";
        String areaInSqKm = "";
        String countryCode = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArraylist = jsonObject.getJSONArray("geonames");
            for (int i=0; i<jsonArraylist.length(); i++){
                JSONObject jsonObjectCountry = jsonArraylist.getJSONObject(i);
                continentName = jsonObjectCountry.getString("continentName");
                countryName = jsonObjectCountry.getString("countryName");
                population = jsonObjectCountry.getString("population");
                areaInSqKm = jsonObjectCountry.getString("areaInSqKm");
                countryCode = jsonObjectCountry.getString("countryCode");

                nationArrayList.add(new National(population,continentName,countryCode,countryName,areaInSqKm));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nationArrayList;
    }
}
