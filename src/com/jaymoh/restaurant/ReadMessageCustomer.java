package com.jaymoh.restaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ReadMessageCustomer extends NavDrawerBase{
	
	private SimpleCursorAdapter adapter;
	public static final String KEY_MEAL_NAME="meal_name";
	public static final String KEY_MESSAGE="message";
	public static final String KEY_HOTEL="hotel_id";
	public static final String ROW_ID="_id";
	private ListView MessageListView;
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_message_customer);
		super.onCreateDrawer();
		MessageListView=getListView();
		registerForContextMenu(MessageListView);
		
		//map to user interface
		String[] from=new String[]{ROW_ID,KEY_HOTEL, KEY_MEAL_NAME, KEY_MESSAGE};
		int[] to=new int[]{R.id.rowID, R.id.hotelnameReply, R.id.mealnameReply, R.id.confirmationReply};
		adapter=
				new SimpleCursorAdapter(ReadMessageCustomer.this, R.layout.read_message_customer_single, null, from, to);
		setListAdapter(adapter);
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		new GetNotificationTask().execute();
	}
	@Override
	protected void onStop()
	{
	Cursor cursor=adapter.getCursor();
	if(cursor!=null)
		cursor.deactivate();
	adapter.changeCursor(null);
	super.onStop();
	}
	private class GetNotificationTask extends AsyncTask<Object, Object, Cursor>
	{
		DatabaseHelper db=new DatabaseHelper(ReadMessageCustomer.this);
		@Override
		protected Cursor doInBackground(Object... params)
		{
			return db.getAllResponses();
		}
		
		@Override
		protected void onPostExecute(Cursor result)
		{
			adapter.changeCursor(result);
			db.close();
		}
	}
	//set up context menu
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.read_mes_customer_menu, menu);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		switch(item.getItemId())
		{
		case R.id.deleteNot:
			String id2=((TextView)info.targetView.findViewById(R.id.rowID)).getText().toString();
			Long ids=Long.parseLong(id2);
			deleteRecord(ids);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	//delete notification
		public void deleteRecord(final long rowId)
		{
			AlertDialog.Builder builder=
					new AlertDialog.Builder(ReadMessageCustomer.this);
			builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final DatabaseHelper db=new DatabaseHelper(ReadMessageCustomer.this);
					
					AsyncTask<Long, Object, Object>deleteTask=
							new AsyncTask<Long, Object, Object>()
							{
						@Override
						protected Object doInBackground(Long... params)
						{
							db.deleteMessageCustomer(params[0]);
							return null;
						}
						@Override
						protected void onPostExecute(Object result)
						{
							new GetNotificationTask().execute();
						}
							};
							deleteTask.execute(new Long[] {rowId});
							Toast.makeText(ReadMessageCustomer.this, "Deleted...", Toast.LENGTH_LONG).show();
					
				}
			});
			builder.setNegativeButton("Nope", null);
			builder.show();
			
		}
}
