package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditMenuItem extends MainActivity{

	String USERNAME;
	String HOTELNAME;
	String MEAL_ID;
	String MNAME;
	String MPRICE;
	int success;
	
	EditText meal_name;
	EditText meal_price;
	Button editBtn;
	
	private ProgressDialog pDialog;
	JSONParser jsonParser=new JSONParser();
	private JSONArray mealDetails;
	
	String mprice;
	String mname;
	
	private static final String TAG_SUCCESS="success";
	private static final String TAG_MESSAGE="message";
	private static final String TAG_MNAME="mname";
	private static final String TAG_MPRICE="price";
	private static final String TAG_MEAL="meal";
	
	private static final String EDIT_MENU_OPTIONS=
			"http://hackinrom.co.nf/android/meal_edit_options.php";
	private static final String UPDATE_MEAL_ID=
			"http://hackinrom.co.nf/android/update_meal_details.php";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_menu_item);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		super.onCreateDrawer();
		Bundle extras=getIntent().getExtras();
		USERNAME=extras.getString("uname");
		HOTELNAME=extras.getString("hname");
		MEAL_ID=extras.getString("meal_id");
		
		meal_name=(EditText)findViewById(R.id.update_dish);
		meal_price=(EditText)findViewById(R.id.update_price);
		editBtn=(Button)findViewById(R.id.btnUpdate);
		editBtn.setEnabled(false);
		editBtn.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				MNAME=meal_name.getText().toString();
				MPRICE=meal_price.getText().toString();
				if(MNAME.isEmpty())
				{
					Toast.makeText(EditMenuItem.this, "Fill meal name", Toast.LENGTH_SHORT).show();
				}
				else if(MPRICE.isEmpty())
				{
					Toast.makeText(EditMenuItem.this, "Fill meal price", Toast.LENGTH_SHORT).show();
				}
				else
				{
					//network check
					networkCheck();
					//new UpdateMeals().execute();
				}
				
			}
		});
		
		//update meals from an inner class
		new UpdateFields().execute();
		
	}
	
	
	public void updateFieldData()
	{
		try
		{
			List<NameValuePair>params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("meal_id", MEAL_ID));
			params.add(new BasicNameValuePair("options", "11"));
			JSONObject json=jsonParser.makeHttpRequest(EDIT_MENU_OPTIONS, "POST", params);
			Log.d("request!", "starting");
			mealDetails=json.getJSONArray(TAG_MEAL);
			success=json.getInt(TAG_SUCCESS);
			
			JSONObject c=mealDetails.getJSONObject(0);
			mname=c.getString(TAG_MNAME);
			mprice=c.getString(TAG_MPRICE);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		
	}
	
	//display field data and set update button enabled
	public void setFieldData()
	{
		meal_name.setText(mname);
		meal_price.setText(mprice);
		editBtn.setEnabled(true);
	}
	public class UpdateFields extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(EditMenuItem.this);
			pDialog.setMessage("Loading fields...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		@Override
		protected Boolean doInBackground(Void... args)
		{
			updateFieldData();
			return null;
		}
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			if(success==1)
				setFieldData();
			else
			{
				Toast.makeText(EditMenuItem.this, "No data to update", Toast.LENGTH_LONG).show();
				
			}
		}
	}
	public class UpdateMeals extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog.setMessage("Updating...");
			pDialog.setCancelable(true);
			pDialog.setIndeterminate(false);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... args)
		{
			try
			{
			List<NameValuePair>params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("meal_id", MEAL_ID));
			params.add(new BasicNameValuePair("mname",MNAME ));
			params.add(new BasicNameValuePair("mprice", MPRICE));
			
			Log.d("request!", "starting");
			JSONObject json=jsonParser.makeHttpRequest(UPDATE_MEAL_ID, "POST", params);
			Log.d("Updating", json.toString());
			success=json.getInt(TAG_SUCCESS);
			if(success==1)
			{
				Log.d("Update successful", json.toString());
				return json.getString(TAG_MESSAGE);
			}
			Log.d("Failure updating", json.getString(TAG_MESSAGE));
			return json.getString(TAG_MESSAGE);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			if(success==1)
				Toast.makeText(EditMenuItem.this, "Update Successful", Toast.LENGTH_SHORT).show();
			
			Intent intent=new Intent(EditMenuItem.this, EditMenuList.class);
			intent.putExtra("uname", USERNAME);
			intent.putExtra("hname", HOTELNAME);
			finish();
			startActivity(intent);
		}
	}
	//to perform network checks
			public void networkCheck()
			{
				if(!niggahHasActiveConnection())
				{
					new AlertDialog.Builder(getApplicationContext()).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Closing the App")
					.setMessage("No Internet connection, check your settings")
					.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EditMenuItem.this.startActivity(new Intent
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
						new UpdateFields().execute();

				}
			}
}
