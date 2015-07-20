package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MealSetter extends MainActivity{
	
	JSONParser jParser=new JSONParser();
	
	String USERNAME;
	int success;
	int hotelSet;
	//JSON element ids from response of the php script
	private static final String TAG_SUCCESS="success";
	private static final String TAG_MESSAGE="message";	
	private static final String TAG_HNAME="hname";
	private static final String TAG_HID="id";
	private static final String TAG_HOTELS="hotels";
	
	private static final String RESTAURANT_URL=
			"http://hackinrom.co.nf/android/load_hotel_name.php";
	private static final String MEAL_SETTER_URL=
			"http://hackinrom.co.nf/android/meal_setter.php";
	
	private ProgressDialog pDialog;
	private JSONArray resNames=null;
	List<String> namesList;
	
	EditText MealName;
	EditText MealPrice;
	Spinner hotelName;
	Button submitBtn;
	TextView errorMessage;
	
	String Mname;
	String Mprice;
	String Mhotel=null;
	String hotelID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meal_setter);
		
		super.onCreateDrawer();
		MealName=(EditText)findViewById(R.id.dish);
		MealPrice=(EditText)findViewById(R.id.price);
		hotelName=(Spinner)findViewById(R.id.spinner);
		submitBtn=(Button)findViewById(R.id.btnSubmit);
		errorMessage=(TextView)findViewById(R.id.noHotelId);
		
		submitBtn.setEnabled(false);
		errorMessage.setClickable(false);
		errorMessage.setVisibility(View.INVISIBLE);
		hotelName.setVisibility(View.INVISIBLE);
		
		//query from shared preferences
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    			USERNAME = prefs.getString("uname", "");
		
		//set text view onclick listener
		errorMessage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i=new Intent(MealSetter.this, RestaurantName.class);
				i.putExtra("uname", USERNAME);
				//finish();
				startActivity(i);
			}
		});
		
		//set button on click listener
		submitBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Mname=MealName.getText().toString();
				Mprice=MealPrice.getText().toString();
				Mhotel=hotelName.getSelectedItem().toString();
				if(Mname.isEmpty())
				{
					Toast.makeText(MealSetter.this, "Enter dish name", 
							Toast.LENGTH_SHORT).show();
				}
				else if(Mprice.isEmpty())
				{
					Toast.makeText(MealSetter.this, "Enter dish price", 
							Toast.LENGTH_SHORT).show();
				}
				else if(Mhotel.isEmpty())
				{
					Toast.makeText(MealSetter.this, "Select restaurant name", 
							Toast.LENGTH_SHORT).show();
				}
				else
				{
					//inner class to add the details
					new SetMeals().execute();
				}
				
			}
		});
		
		/*load hotel names and set spinner visible or else error message
		 * do the stuff in a back ground thread
		 * perform network check always
		 */
		networkCheck();
		//new LoadResNames().execute();
	}
	//update spinner data
	public void updateSpinnerData()
	{	
		
		namesList=new ArrayList<String>();
		
		try
		{
			List<NameValuePair>params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uname", USERNAME));
			
			JSONObject json=jParser.makeHttpRequest(RESTAURANT_URL, "POST", params);
			
			success=json.getInt(TAG_SUCCESS);
			hotelSet=json.getInt(TAG_SUCCESS);
			
			resNames=json.getJSONArray(TAG_HOTELS);
			
			//loop through all hotel names for that user
			for(int i=0; i<resNames.length(); i++)
			{
				JSONObject c=resNames.getJSONObject(i);
				String hname=c.getString(TAG_HNAME);
				hotelID=c.getString(TAG_HID);
				namesList.add(hname);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	//set Spinner Visibility else set error text view visible
	public void SetSpinnerVisible()
	{
		if(success==1)
		{
			ArrayAdapter<String>adapter=new ArrayAdapter<String>(this, 
					android.R.layout.simple_spinner_item, namesList);
			hotelName.setAdapter(adapter);
			hotelName.setVisibility(View.VISIBLE);
			submitBtn.setEnabled(true);
			SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(MealSetter.this);
			Editor edit=sp.edit();
			String hotel=sp.getString("gcm_hotelid", "");
			if(hotel.equalsIgnoreCase(""))
			{
			edit.putString("gcm_hotelid", hotelID);
			edit.apply();
			}
		}
		else
		{
			errorMessage.setVisibility(View.VISIBLE);
			errorMessage.setClickable(true);
		}
	}
	
	//inner class to load res names in a background thread
	public class LoadResNames extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(MealSetter.this);
			pDialog.setMessage("Checking your restaurants...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
			
		}
		@Override
		protected Boolean doInBackground(Void... arg0)
		{
			updateSpinnerData();
			return null;
		}
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			SetSpinnerVisible();
		}
	}
	
	//inner class to set meal details
	public class SetMeals extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(MealSetter.this);
			pDialog.setMessage("Setting meal details...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... arg0)
		{
			try
			{
				List<NameValuePair>params=new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("uname", USERNAME));
				params.add(new BasicNameValuePair("mname", Mname));
				params.add(new BasicNameValuePair("mprice", Mprice));
				params.add(new BasicNameValuePair("hname", Mhotel));
				
				Log.d("request!", "starting");
				JSONObject json=jParser.makeHttpRequest(MEAL_SETTER_URL, "POST", params);
				
				Log.d("Update Attempt", json.toString());
				success=json.getInt(TAG_SUCCESS);
				if(success==1)
				{
					Log.d("Details added successfully!", json.toString());
					
					return json.getString(TAG_MESSAGE);
				}
				else
				{
					Log.d("Failure adding details", json.getString(TAG_MESSAGE));
					return json.getString(TAG_MESSAGE);
				}
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			clearFields();
		}
	}
	
	public void clearFields()
	{
		MealName.setText("");
		MealPrice.setText("");
	}
	
	//to perform network checks
	public void networkCheck()
	{
		if(!niggahHasActiveConnection())
		{
			new AlertDialog.Builder(MealSetter.this).setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Closing the App")
			.setMessage("No Internet connection, check your settings")
			.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MealSetter.this.startActivity(new Intent
							(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
					
				}
			})
			.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					
				}
			})
			.show();
		}
		else
		{
				new LoadResNames().execute();

		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.meal_set, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.add_res:
			Intent i=new Intent(MealSetter.this, RestaurantName.class);
			i.putExtra("uname", USERNAME);
			//confirm if the guy already added a restaurant
			if(hotelSet==1)
			{
				Toast.makeText(MealSetter.this, "No need... You already added a restaurant", Toast.LENGTH_LONG).show();
			}
			else
			startActivity(i);
			return true;
			
		case R.id.edit_menu:
			Intent ed=new Intent(MealSetter.this, EditMenuList.class);
			if(success==1)
			{
			Mhotel=hotelName.getSelectedItem().toString();
			ed.putExtra("uname", USERNAME);
			ed.putExtra("hname", Mhotel);
			startActivity(ed);
			}
			else
			{
				Toast.makeText(MealSetter.this, "You need to have added a restaurant", Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.reg_gcm:
			Intent reg=new Intent(MealSetter.this, RegisterGcmHotel.class);
			SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(MealSetter.this);
			String registered=sp.getString("hotel_registered", "");
			if(registered.equalsIgnoreCase(""))
			{
				startActivity(reg);
			}
			else
				Toast.makeText(MealSetter.this, "No need, you already registered", Toast.LENGTH_LONG).show();
			return true;
		case R.id.read_messages:
			Intent readIntent=new Intent(MealSetter.this, ReadMessageOwner.class);
			startActivity(readIntent);
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
