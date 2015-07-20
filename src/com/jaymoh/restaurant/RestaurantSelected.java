package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class RestaurantSelected extends ListActivity {
	TextView MealId;
	TextView MealName;
	TextView MealPrice;
	
	String gcm_uname;
	String gcm_pword;
	
	String id;
	String meal_id;
	int success;
	String customerChoice;
	
	//url location
	private static final String MENU_URL= "http://hackinrom.co.nf/android/hotel_meal_list.php";
	static final String SEND_NOTIFICATION_URL="http://hackinrom.co.nf/gcm/send_notification.php";
	JSONParser jParser=new JSONParser();
	
	ProgressDialog pDialog=null;
	ListAdapter adapter;
	//json tags
	private static final String TAG_SUCCESS="success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_MEALS = "meals";
	private static final String TAG_MNAME="mname";
	private static final String TAG_MPRICE="mprice";
	private static final String TAG_ID="id";
	
	//json array for the returned menu list
	private JSONArray mealDetails;
	
	//array list to manage the returned results
	private ArrayList<HashMap<String, String>>mealDetailList;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.restaurant_selected);
	
	getActionBar().setDisplayHomeAsUpEnabled(true);
	
	ListView lv=getListView();
	registerForContextMenu(lv);
	
	Bundle extras=getIntent().getExtras();
	id=extras.getString("hotel_id");
	
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		//perform network check
		new LoadMenu().execute();
	}
	
	//update menu data
	public void updateMenuData()
	{
		mealDetailList=new ArrayList<HashMap<String, String>>();
		
		//create the parameters
		try
		{
			
			List<NameValuePair>params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("hotel_id", id));
			
			
			JSONObject json=jParser.makeHttpRequest(MENU_URL, "POST", params);
			
			Log.d("request!", "starting");
			
			mealDetails=json.getJSONArray(TAG_MEALS);
			success=json.getInt(TAG_SUCCESS);
			//loop through the returned data
			for(int i=0;i<mealDetails.length(); i++)
			{
				JSONObject c=mealDetails.getJSONObject(i);
				
				String 	MEALID=c.getString(TAG_ID);
				String MEALN=c.getString(TAG_MNAME)+" :";
				String MEALP=c.getString(TAG_MPRICE)+" ksh";
				
				HashMap<String, String>map=new HashMap<String, String>();
				map.put(TAG_ID, MEALID);
				map.put(TAG_MNAME, MEALN);
				map.put(TAG_MPRICE, MEALP);
				
				//add hashmap to the arraylist
				mealDetailList.add(map);
			}
			
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
//update the user interface
	public void updateMenuList()
	{
		adapter=new SimpleAdapter(this, mealDetailList,
				R.layout.restaurant_selected_single,
				new String[] {TAG_ID, TAG_MNAME, TAG_MPRICE},
				new int[]{R.id.meal_id, R.id.meal_name, R.id.meal_price});
		setListAdapter(adapter);
	}
	
	//inner class to load the menu from the server somewhere
	public class LoadMenu extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(RestaurantSelected.this);
			pDialog.setMessage("Loading menu...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		@Override
		protected Boolean doInBackground(Void... arg0)
		{
			updateMenuData();
			return null;
		}
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			if(success==1)
			updateMenuList();
			else
				Toast.makeText(getApplicationContext(), "No menu is available from that restaurant...", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu,
			View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.res_selected, menu);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		
		switch(item.getItemId())
		{
		case R.id.sit_in:
			HashMap<String, Object>obj=(HashMap<String, Object>)
			adapter.getItem(info.position);
			meal_id=(String)obj.get(TAG_ID);
			customerChoice="11";
			takeOptions();
			return true;
			
		case R.id.take_away:
			HashMap<String, Object>obj2=(HashMap<String, Object>)
			adapter.getItem(info.position);
			meal_id=(String)obj2.get(TAG_ID);
			customerChoice="00";
			takeOptions();
			return true;
			
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	public void takeOptions()
	{
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(RestaurantSelected.this);
		gcm_uname=prefs.getString("gcm_uname", "");
		gcm_pword=prefs.getString("gcm_pword", "");
		
		if(gcm_uname.equalsIgnoreCase("")|| gcm_pword.equalsIgnoreCase(""))
		{
			Toast.makeText(RestaurantSelected.this, "You have to register first", Toast.LENGTH_LONG).show();
			Intent ii=new Intent(RestaurantSelected.this, RegisterGcm.class);
			startActivity(ii);
		}
		else
		{
			new SendNotification().execute();
		}
	}
	
	//inner class to send notification
	public class SendNotification extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(RestaurantSelected.this);
			pDialog.setMessage("Sending request notification...");
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
				params.add(new BasicNameValuePair("meal_id", meal_id));
				params.add(new BasicNameValuePair("hotel_id", id));
				params.add(new BasicNameValuePair("options", customerChoice));
				params.add(new BasicNameValuePair("user_id", gcm_uname));
				params.add(new BasicNameValuePair("pword", gcm_pword));
				
				Log.d("request!", "starting");
				JSONObject json=jParser.makeHttpRequest(SEND_NOTIFICATION_URL, "POST", params);
				
				Log.d("Sending notification", json.toString());
				int suc=json.getInt(TAG_SUCCESS);
				if(suc==1)
				{
					Log.d("Notification send...", json.toString());
					
					return json.getString(TAG_MESSAGE);
				}
				else
				{
					Log.d("Failure sending notification", json.getString(TAG_MESSAGE));
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
		}
		
		
	}
	/*
	//check list item clicks
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		TextView mealIDTxt=(TextView)v.findViewById(R.id.meal_id);
		
		//dialog for making an order
		String idd=mealIDTxt.getText().toString();
		Toast.makeText(RestaurantSelected.this, idd, Toast.LENGTH_SHORT).show();
		
	}
	*/
}
