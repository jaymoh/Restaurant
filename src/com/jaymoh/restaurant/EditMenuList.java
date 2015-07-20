package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class EditMenuList extends ListActivity {
	
	TextView MealId;
	TextView MealName;
	TextView MealPrice;
	int id;
	int success;
	String USERNAME;
	String HOTELNAME;
	String meal_id;
	ListAdapter adapter;
	
	//url location
	private static final String EDIT_MENU_LIST_URL= 
			"http://hackinrom.co.nf/android/edit_meal_list.php";
	private static final String EDIT_MENU_OPTIONS=
			"http://hackinrom.co.nf/android/meal_edit_options.php";
	
	ProgressDialog pDialog=null;
	
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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_menu_list);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		ListView lv=getListView();
		registerForContextMenu(lv);
		
		Bundle extras=getIntent().getExtras();
		USERNAME=extras.getString("uname");
		HOTELNAME=extras.getString("hname");
		
		//perform netcheck and call inner class
		networkCheck();
		//new LoadMenu().execute();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		//should check for internet connectivity here
		//networkCheck();
		//new LoadMenu().execute();
	}
	
	public void updateMenuData()
	{
		mealDetailList=new ArrayList<HashMap<String,String>>();
		
		try
		{
			List<NameValuePair>params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uname", USERNAME));
			params.add(new BasicNameValuePair("hname", HOTELNAME));
			
			JSONParser jParser=new JSONParser();
			JSONObject json=jParser.makeHttpRequest(EDIT_MENU_LIST_URL, "POST", params);
			
			Log.d("request!", "starting");
			mealDetails=json.getJSONArray(TAG_MEALS);
			success=json.getInt(TAG_SUCCESS);
			//loop through the list
			for(int i=0; i<mealDetails.length(); i++)
			{
				JSONObject c=mealDetails.getJSONObject(i);
				
				String MEALID=c.getString(TAG_ID);
				String MEALN=c.getString(TAG_MNAME);
				String MEALP=c.getString(TAG_MPRICE);
				
				HashMap<String, String>map=new HashMap<String, String>();
				map.put(TAG_ID, MEALID);
				map.put(TAG_MNAME, MEALN);
				map.put(TAG_MPRICE, MEALP);
				
				mealDetailList.add(map);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	//update user interface 
	public void updateMenuList()
	{
		adapter=new SimpleAdapter(this, mealDetailList,
				R.layout.edit_menu_list_single,
				new String[] {TAG_ID, TAG_MNAME, TAG_MPRICE},
				new int[] {R.id.m_id, R.id.m_name, R.id.m_price});
		
		setListAdapter(adapter);
	}
	
	//inner class to perform server side querying in a background thread
	public class LoadMenu extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(EditMenuList.this);
			pDialog.setMessage("Loading menu list...");
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
			
			//display the list to the client if it was returned
			if(success==1)
			updateMenuList();
			else
				Toast.makeText(getApplicationContext(), "No list available, please add something", Toast.LENGTH_LONG).show();
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
						EditMenuList.this.startActivity(new Intent
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
					new LoadMenu().execute();

			}
		}
		
		//Internet connectivity status check 
		public boolean niggahHasActiveConnection()
		{
			ConnectivityManager conn=(ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo=conn.getActiveNetworkInfo();
			if(netInfo==null||!netInfo.isConnected()||!netInfo.isAvailable())
			{
				return false;
			}
			return true;
		}
		@Override
		public void onCreateContextMenu(ContextMenu menu,
				View v, ContextMenuInfo menuInfo)
		{
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater=getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);
			
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item)
		{
			AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
			
			switch(item.getItemId())
			{
			case R.id.edit:
				//pass the meal id to edit class
				HashMap<String,Object> obj=(HashMap<String, Object>)adapter.getItem(info.position);
				meal_id=(String)obj.get(TAG_ID);
				Intent i=new Intent(EditMenuList.this, EditMenuItem.class);
				i.putExtra("meal_id", meal_id);
				i.putExtra("uname", USERNAME);
				i.putExtra("hname", HOTELNAME);
				finish();
				startActivity(i);
				return true;
			case R.id.delete:
				//pass id to the inner delete item Asynctask
				HashMap<String,Object> obj2=(HashMap<String, Object>)adapter.getItem(info.position);
				meal_id=(String)obj2.get(TAG_ID);
				AreYouSure();
				return true;
				default:
					return super.onContextItemSelected(item);
			}
		}
		//inner delete action class
		public class DeleteAction extends AsyncTask<String, String, String>
		{
			boolean failure=false;
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				pDialog=new ProgressDialog(EditMenuList.this);
				pDialog.setMessage("Deleting...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}
			@Override
			protected String doInBackground(String... args)
			{
				int suc;
				JSONParser jParser=new JSONParser();
				try
				{
					List<NameValuePair>params=new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("meal_id", meal_id));
					params.add(new BasicNameValuePair("options", "00"));
					Log.d("Deletion", "Starting");
					JSONObject json=jParser.makeHttpRequest(EDIT_MENU_OPTIONS, "POST", params);
					Log.d("Deletion attempt", json.toString());
					
					suc=json.getInt(TAG_SUCCESS);
					if(suc==1)
					{
						Log.d("Deletion successful!", json.toString());
						Intent i=new Intent(EditMenuList.this,EditMenuList.class );
						i.putExtra("uname", USERNAME);
						i.putExtra("hname", HOTELNAME);
						startActivity(i);
						return json.getString(TAG_MESSAGE);
					}
					else
					{
						Log.d("Deletion unsuccessful!", json.getString(TAG_MESSAGE));
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
				if(result!=null)
				{
					Toast.makeText(EditMenuList.this, result, Toast.LENGTH_LONG).show();
				}
			}
		}
		public void AreYouSure()
		{
			AlertDialog.Builder builder=
					new AlertDialog.Builder(EditMenuList.this);
			builder.setTitle("Are you sure?");
			builder.setMessage("This is will delete the item completely");
			//okay button
			builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					//networkCheck();
					new DeleteAction().execute();
				}
			});
			builder.setNegativeButton("Cancel", null);
			builder.show();

		}
		
}
 