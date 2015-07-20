package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class RestaurantReviews extends NavDrawerBase {

	public static String REVIEW_URL=
			"http://hackinrom.co.nf/android/restaurant_review.php";	
	JSONParser jParser=new JSONParser();
	
	ProgressDialog pDialog=null;
	ListAdapter adapter;
	
	int success;
	private static final String TAG_SUCCESS="success";
	private static final String TAG_MESSAGE="message";
	private static final String TAG_REVIEW="review";
	private static final String TAG_UNAME="uname";
	private static final String TAG_RESNAME="resname";
	private static final String TAG_REVIEWS="reviews";
	
	private JSONArray reviewDetails;
	private ArrayList<HashMap<String, String>>reviewsList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restaurant_reviews);
		
		super.onCreateDrawer();
		
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		//perform network check
		networkCheck();
		//new LoadReviews().execute();
	}
	
	public void updateReviewData()
	{
		reviewsList=new ArrayList<HashMap<String,String>>();
		
		//
		JSONObject json=jParser.getJSONFromUrl(REVIEW_URL);
		try
		{
			success=json.getInt(TAG_SUCCESS);
			reviewDetails=json.getJSONArray(TAG_REVIEWS);
			
			for(int i=0; i<reviewDetails.length(); i++)
			{
				JSONObject c=reviewDetails.getJSONObject(i);
				
				String RESNAME=c.getString(TAG_RESNAME);
				String UNAME=c.getString(TAG_UNAME);
				String REVIEW=c.getString(TAG_REVIEW);
				
				//create hashmap
				HashMap<String, String>map=new HashMap<String, String>();
				map.put(TAG_RESNAME, RESNAME);
				map.put(TAG_UNAME, UNAME);
				map.put(TAG_REVIEW, REVIEW);
				
				reviewsList.add(map);
			}
			
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	//update list
	private void updateList()
	{
		if(success==1)
		{
			adapter=new SimpleAdapter(this, reviewsList,
					R.layout.restaurant_reviews_single, new String[] {TAG_RESNAME, TAG_REVIEW, TAG_UNAME}, 
					new int[]{R.id.review_resname, R.id.review_text, R.id.review_uname});
			setListAdapter(adapter);
		}
		else
		{
			Toast.makeText(RestaurantReviews.this, "No restaurant has been reviewed yet, be the first!", Toast.LENGTH_LONG).show();
		}
	}
	//inner class to load the reviews
	public class LoadReviews extends AsyncTask<Void, Void,Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(RestaurantReviews.this);
			pDialog.setMessage("Loading reviews...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... arg)
		{
			updateReviewData();
			return null;
		}
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			updateList();
		}
	}
	
	//
	//to perform network checks
			public void networkCheck()
			{
				if(!niggahHasActiveConnection())
				{
					new AlertDialog.Builder(RestaurantReviews.this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Closing the App")
					.setMessage("No Internet connection, check your settings")
					.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							RestaurantReviews.this.startActivity(new Intent
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
					new LoadReviews().execute();
				}
			}
			//create menu
			@Override
			public boolean onCreateOptionsMenu(Menu menu)
			{
				MenuInflater inflater=getMenuInflater();
				inflater.inflate(R.menu.review_menu, menu);
				return true;
			}
			@Override
			public boolean onOptionsItemSelected(MenuItem item)
			{
				switch(item.getItemId())
				{
				case R.id.add_review:
					SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(RestaurantReviews.this);
					String gcm_uname=sp.getString("gcm_uname", "");
					if(gcm_uname.equalsIgnoreCase(""))
					{
						Toast.makeText(RestaurantReviews.this, "You have to register first", Toast.LENGTH_LONG).show();
						Intent ii=new Intent(RestaurantReviews.this, RegisterGcm.class);
						startActivity(ii);
					}
					else
					{
						Intent ii=new Intent(RestaurantReviews.this, AddReview.class);
						startActivity(ii);
					}
					return true;
				default:
					return super.onOptionsItemSelected(item);
				}
			}
}
