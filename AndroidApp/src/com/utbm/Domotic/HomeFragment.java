package com.utbm.Domotic;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.utbm.pandaboarddomotic.R;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";

    public TextView tvTempCurrent;
    public TextView tvTempMin;
    public TextView tvTempMax;
    public TextView tvTempAverage;
    
    public TextView tvPressureCurent;
    public TextView tvPressureMin;
    public TextView tvPressureMax;
    public TextView tvPressureAverage;
    
    public Button bUpdate;
    
    private XYPlot Temperatureplot;

    private HomeData data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	data = new HomeData();

        View view = inflater.inflate(R.layout.activity_main, container, false);

        tvTempCurrent = (TextView) view.findViewById(R.id.temperature_current);
        tvTempMin = (TextView) view.findViewById(R.id.temperature_min);
        tvTempMax = (TextView) view.findViewById(R.id.temperature_max);
        tvTempAverage = (TextView) view.findViewById(R.id.temperature_average);
        
        tvPressureCurent = (TextView) view.findViewById(R.id.pressure_current);
        tvPressureMin = (TextView) view.findViewById(R.id.pressure_min);
        tvPressureMax = (TextView) view.findViewById(R.id.pressure_max);
        tvPressureAverage = (TextView) view.findViewById(R.id.pressure_average);

        Temperatureplot = (XYPlot)view.findViewById(R.id.XYPlot);
        
        bUpdate = (Button) view.findViewById(R.id.button_update);
        bUpdate.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if(isOnline()) {
                    new HttpRequest(v.getContext()).execute();
                }
            }
        });
        new HttpRequest(view.getContext()).execute();
        return view;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String getServerURI() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String hostname = sharedPrefs.getString("preference_server_hostname", "NULL");
        String port  = sharedPrefs.getString("preference_server_port", "NULL");
        return "http://" + hostname + ":" + port;
    }

    private class HttpRequest extends AsyncTask<String, Void, String>{

        private String responseString = null;
        private Context mContext;

        public HttpRequest (Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
        	
        	Log.i(TAG, "Starting HTTP Request");
        	
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                // execute an HTTP request to get last build number
                response = httpclient.execute(new HttpGet(getServerURI()));
                StatusLine statusLine = response.getStatusLine();

                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    // request successful, then extract response
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
            
            Log.i(TAG,"Request finished - result received - null?"+(responseString==null));
            return responseString;
        }

        protected void onPostExecute(String result){

        	if(result!=null){
        		
        		Log.i(TAG,"Parsing JSON result...");
        		try {
					JSONObject json = new JSONObject(result);
					
					data.settMin(json.getDouble("tMin"));
					data.settMax(json.getDouble("tMax"));
					data.settAverage(json.getDouble("tAverage"));
					
					data.setpMin(json.getDouble("pMin"));
					data.setpMax(json.getDouble("pMax"));
					data.setpAverage(json.getDouble("pAverage"));
					
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					Date date = df.parse(json.getString("DateTMin"));
					Log.i(TAG," date "+date);
					
					data.setDatePMax(df.parse(json.getString("DatePMax")));
					data.setDatePMin(df.parse(json.getString("DatePMin")));
					
					data.setDateTMax(df.parse(json.getString("DateTMax")));
					data.setDateTMin(df.parse(json.getString("DateTMin")));
					
					JSONArray lastMeasures = json.getJSONArray("lastMeasures");
					ArrayList<SensorData> measurementList = new ArrayList<SensorData>();
				    for (int i=0;i< lastMeasures.length(); i++) {
				        JSONObject obj = lastMeasures.getJSONObject(i);
				    	SensorData sensor = new SensorData(
				    			df.parse(obj.getString("Date")),
				    			obj.getDouble("Pressure"),
				    			obj.getDouble("Altitude"),
				    			obj.getDouble("Clock_temperature"),
				    			obj.getDouble("Temperature")
				    			);
				        measurementList.add(sensor);
				        
				        if(i==(lastMeasures.length()-1)){
				        	Log.i(TAG," current values setted");
				        	data.settCurrent(obj.getDouble("Temperature"));
				        	data.setpCurrent(obj.getDouble("Pressure"));
				        }
				    }
				    
				    data.setsensorData(measurementList);
				    
					Log.i(TAG," new data received : current temperature="+data.gettCurrent());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
        		
        		Log.i(TAG,"Setting all UI elements");
        		
        		tvTempCurrent.setText(tvTempCurrent.getText()+String.format(" %.2f°",data.gettCurrent()));
        		tvTempAverage.setText(tvTempAverage.getText()+String.format(" %.2f°", data.gettAverage()));
        		tvTempMin.setText(tvTempMin.getText()+String.format(" %.1f°", data.gettMin()));
        		tvTempMax.setText(tvTempMax.getText()+String.format(" %.1f°", data.gettMax()));
        		
        		tvPressureCurent.setText(tvPressureCurent.getText()+String.format(" %.2fhPa", data.getpCurrent()));
        		tvPressureAverage.setText(tvPressureAverage.getText()+String.format(" %.2fhPa", data.getpAverage()));
        		tvPressureMin.setText(tvPressureMin.getText()+String.format(" %.2fhPa", data.getpMin()));
        		tvPressureMax.setText(tvPressureMax.getText()+String.format(" %.2fhPa", data.getpMax()));
        		
                // Turn the above arrays into XYSeries':
                XYSeries series1 = new SimpleXYSeries(
                		data.getArrayTemperature(),          // SimpleXYSeries takes a List so turn our array into a List
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                        "tempData");                             // Set the display title of the series (not shown)

                 // Create a formatter to use for drawing a series using LineAndPointRenderer
                // and configure it from xml:
                LineAndPointFormatter series1Format = new LineAndPointFormatter();
                series1Format.configure(getActivity(),
                        R.xml.line_point_formatter_with_plf1);

                // add a new series' to the xyplot:
                Temperatureplot.addSeries(series1, series1Format);
        	}
        }
    }
    
    public class SensorData {
    	
    	private Date date;
    	private double pressure;
    	private double altitude;
    	private double clockTemperature;
    	private double temperature;

    	public SensorData(Date date, double pressure, double altitude,
				double clockTemperature, double temperature) {
			super();
			this.date = date;
			this.pressure = pressure;
			this.altitude = altitude;
			this.clockTemperature = clockTemperature;
			this.temperature = temperature;
		}
    	
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public double getPressure() {
			return pressure;
		}
		public void setPressure(double pressure) {
			this.pressure = pressure;
		}
		public double getAltitude() {
			return altitude;
		}
		public void setAltitude(double altitude) {
			this.altitude = altitude;
		}
		public double getClockTemperature() {
			return clockTemperature;
		}
		public void setClockTemperature(double clockTemperature) {
			this.clockTemperature = clockTemperature;
		}
		public double getTemperature() {
			return temperature;
		}
		public void setTemperature(double temperature) {
			this.temperature = temperature;
		}
    	
    	
    }
    
    public class HomeData {
    	
    	private double tCurrent;
    	private double tMin;
    	private double tMax;
    	private double tAverage;
    	
    	private Date dateTMax;
    	private Date dateTMin;
    	
    	private Date datePMax;
    	private Date datePMin;
    	
    	private double pCurrent;
    	private double pMin;
    	private double pMax;
    	private double pAverage;

    	private ArrayList<SensorData> sensorData;
    	
    	public HomeData(){
    		
    	}

		public double gettCurrent() {
			return tCurrent;
		}

		public void settCurrent(double tCurrent) {
			this.tCurrent = tCurrent;
		}

		public double gettMin() {
			return tMin;
		}

		public void settMin(double d) {
			this.tMin = d;
		}

		public double gettMax() {
			return tMax;
		}

		public void settMax(double tMax) {
			this.tMax = tMax;
		}

		public double gettAverage() {
			return tAverage;
		}

		public void settAverage(double tAverage) {
			this.tAverage = tAverage;
		}

		public Date getDateTMax() {
			return dateTMax;
		}

		public void setDateTMax(Date dateTMax) {
			this.dateTMax = dateTMax;
		}

		public Date getDateTMin() {
			return dateTMin;
		}

		public void setDateTMin(Date dateTMin) {
			this.dateTMin = dateTMin;
		}

		public Date getDatePMax() {
			return datePMax;
		}

		public void setDatePMax(Date datePMax) {
			this.datePMax = datePMax;
		}

		public Date getDatePMin() {
			return datePMin;
		}

		public void setDatePMin(Date datePMin) {
			this.datePMin = datePMin;
		}

		public double getpCurrent() {
			return pCurrent;
		}

		public void setpCurrent(double pCurrent) {
			this.pCurrent = pCurrent;
		}

		public double getpMin() {
			return pMin;
		}

		public void setpMin(double pMin) {
			this.pMin = pMin;
		}

		public double getpMax() {
			return pMax;
		}

		public void setpMax(double pMax) {
			this.pMax = pMax;
		}

		public double getpAverage() {
			return pAverage;
		}

		public void setpAverage(double pAverage) {
			this.pAverage = pAverage;
		}
		
		public void setsensorData(ArrayList<SensorData> data) {
			this.sensorData = data;
		}
		
		public ArrayList<SensorData> getsensorData(){
			return sensorData;
		}
		
		public ArrayList<Double> getArrayTemperature(){
			ArrayList<Double> list = new ArrayList<Double>();
			for(SensorData item : sensorData){
				list.add(item.getTemperature());
			}
			return list;	
		}
		
		public ArrayList<Double> getArrayPressure(){
			ArrayList<Double> list = new ArrayList<Double>();
			for(SensorData item : sensorData){
				list.add(item.getPressure());
			}
			return list;	
		}
		
		public ArrayList<Date> getArrayDate(){
			ArrayList<Date> list = new ArrayList<Date>();
			for(SensorData item : sensorData){
				list.add(item.getDate());
			}
			return list;	
		}
    	
    }
}
