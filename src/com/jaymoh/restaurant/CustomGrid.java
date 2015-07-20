package com.jaymoh.restaurant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomGrid extends BaseAdapter {
	
	private Context mContext;
	private final String[] restaurants;
	private final int[] ImageId;
	private final String[] restaurant_ids;
	
	public CustomGrid(Context c, String[] restaurants, int[] ImageId, String[] restaurant_ids)
	{
		mContext=c;
		this.ImageId=ImageId;
		this.restaurants=restaurants;
		this.restaurant_ids=restaurant_ids;
	}

	@Override
	public int getCount() {
		return restaurants.length;
	}

	@Override
	public Object getItem(int position) {
		
		return null;
	}

	@Override
	public long getItemId(int position) {
		
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View grid;
		LayoutInflater inflator=(LayoutInflater)mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(convertView==null)
		{
			grid=new View(mContext);
			grid=inflator.inflate(R.layout.grid_single, null);
			ImageView imageView=(ImageView)grid.findViewById(R.id.grid_imag);
			TextView textView=(TextView)grid.findViewById(R.id.grid_text);
			TextView textView2=(TextView)grid.findViewById(R.id.grid_id);		
			
			imageView.setImageResource(ImageId[position]);
			textView.setText(restaurants[position]);		
			textView2.setText(restaurant_ids[position]);
		}
		else
		{
			grid=(View)convertView;
		}
		return grid;
	}

}
