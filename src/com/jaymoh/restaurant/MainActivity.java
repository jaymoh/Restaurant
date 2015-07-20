package com.jaymoh.restaurant;


import java.util.ArrayList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {
	//193.168.42.76
    public DrawerLayout drawerLayout;
    public ListView drawerList;
    // nav drawer title
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle drawerToggle;
    private ArrayList<NavDrawerItem> navDrawerItems;
    
 // used to store app title
    private CharSequence mTitle;
    String USERNAME;
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    
    private NavDrawerListAdapter adapter;
	
	protected void onCreateDrawer()
	{
		
		drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
		 mTitle = mDrawerTitle = getTitle();
		 
		 /*....... get screen width and height..... */
		 DisplayMetrics displaymetrics= new DisplayMetrics();
		 getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		 int height=displaymetrics.heightPixels;
		 int width=displaymetrics.widthPixels;
		 
		 int newWidth=width/2;
		 
		 /*....... get screen width and height..... */
		 
		 // load slide menu items
	        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
	        
	     // nav drawer icons from resources
	        navMenuIcons = getResources()
	                .obtainTypedArray(R.array.nav_drawer_icons);
	        
	      //items to be displayed as items
	        drawerToggle=new ActionBarDrawerToggle((Activity)this, drawerLayout, R.drawable.nav_toggle, 0, 0)
			{
				public void onDrawerClosed(View view)
				{
					getActionBar().setTitle(R.string.app_name);
				}
				public void onDrawerOpened(View drawerView)
				{
					getActionBar().setTitle(R.string.app_name);
				}
			};
			
			drawerLayout.setDrawerListener(drawerToggle);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
			
			drawerList = (ListView) findViewById(R.id.list_slidermenu);
			
			 navDrawerItems = new ArrayList<NavDrawerItem>();
			 
	        // adding nav drawer items to array
	        // Home
	        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
	        // Find People
	        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
	        // Photos
	        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
	        // Communities, Will add a counter here
	        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
	        // Pages
	        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
	        
	        // Recycle the typed array
	        navMenuIcons.recycle();
	        
	     // setting the nav drawer list adapter
	        adapter = new NavDrawerListAdapter(getApplicationContext(),
	                navDrawerItems);
	        
	        //drawerList.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, newWidth));
	        drawerList.setAdapter(adapter);
	
	
	drawerList.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			// start activities
			gotoNavDrawerItem(pos);
			
		}
	});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (drawerToggle.onOptionsItemSelected(item)) {
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    drawerToggle.syncState();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    drawerToggle.onConfigurationChanged(newConfig);
	}
	public void gotoNavDrawerItem(int pos)
	{
		Intent intent;
		switch(pos)
		{
		case 0:
			intent=new Intent(this, Restaurant.class);
			startActivity(intent);
			finish();
			break;
		case 1:
			//query from shared preferences
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        			USERNAME = prefs.getString("uname", "");
        			if(USERNAME.equalsIgnoreCase(""))
        			{
						intent=new Intent(this, OwnerLogin.class);
						startActivity(intent);
        			}
        			else
        			{
        				intent=new Intent(this, MealSetter.class);	
        				startActivity(intent);
        			}
			
			break;
		case 2:
			intent=new Intent(this, ReadMessageCustomer.class);
			startActivity(intent);
			finish();
			break;
		case 3:
			intent=new Intent(this, RestaurantReviews.class);
			startActivity(intent);
			finish();
			break;
		case 4:
			dialogInflater();
			break;
		default:
			break;
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
			
			public void dialogInflater()
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				LayoutInflater inflater=LayoutInflater.from(MainActivity.this);
				View layout=inflater.inflate(R.layout.about, null);
				builder.setTitle("About");
				builder.setView(layout);
				builder.setNegativeButton("Okay", null);
				builder.show();
				
			}
			
}