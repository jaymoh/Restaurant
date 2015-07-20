package com.jaymoh.restaurant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
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

public class ReadMessageOwner extends ListActivity {

	private SimpleCursorAdapter adapter;
	public static final String KEY_MEAL_NAME="meal_name";
	public static final String KEY_UNAME="customer";
	public static final String KEY_MESSAGE="message";
	public static final String ROW_ID="_id";
	private ListView MessageListView;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_message_owner);
		MessageListView=getListView();
		registerForContextMenu(MessageListView);
		
		//maps the returned results to text views
		String[] from=new String[] {ROW_ID, KEY_UNAME, KEY_MEAL_NAME,KEY_MESSAGE};
		int[] to=new int[]{R.id.req_Id, R.id.req_username, R.id.req_mealname, R.id.req_message};
		adapter=
				new SimpleCursorAdapter(ReadMessageOwner.this, 
						R.layout.read_message_owner_single, null, from, to);
		setListAdapter(adapter);
		
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		new GetRequestTask().execute();
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
	
	//inner class to load data from the database
	private class GetRequestTask extends AsyncTask<Object, Object, Cursor>
	{
		DatabaseHelper db=new DatabaseHelper(ReadMessageOwner.this);

		@Override
		protected Cursor doInBackground(Object... params) 
		{
			return db.getAllMessages();
		}
		
		@Override
		protected void onPostExecute(Cursor result)
		{
			adapter.changeCursor(result);
			db.close();
		}
	}
	//setting oncontext menus
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.read_mes_owner_menu, menu);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		switch(item.getItemId())
		{
		case R.id.replyReq:
			String id=((TextView)info.targetView.findViewById(R.id.req_Id)).getText().toString();
			//do something>.convert id to long
			Long idss=Long.parseLong(id);
			Intent i=new Intent(ReadMessageOwner.this, ReplyNotification.class);
			i.putExtra("ids", idss);
			startActivity(i);
			return true;
		case R.id.deleteReq:
			String id2=((TextView)info.targetView.findViewById(R.id.req_Id)).getText().toString();
			//do something here>> convert id to Long
			Long ids=Long.parseLong(id2);
			deleteRecord(ids);
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	//delete request
	public void deleteRecord(final long rowId)
	{
		AlertDialog.Builder builder=
				new AlertDialog.Builder(ReadMessageOwner.this);
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final DatabaseHelper db=new DatabaseHelper(ReadMessageOwner.this);
				
				AsyncTask<Long, Object, Object>deleteTask=
						new AsyncTask<Long, Object, Object>()
						{
					@Override
					protected Object doInBackground(Long... params)
					{
						db.deleteMessage(params[0]);
						return null;
					}
					@Override
					protected void onPostExecute(Object result)
					{
						new GetRequestTask().execute();
					}
						};
						deleteTask.execute(new Long[] {rowId});
						Toast.makeText(ReadMessageOwner.this, "Deleted...", Toast.LENGTH_LONG).show();
				
			}
		});
		builder.setNegativeButton("Nope", null);
		builder.show();
		
	}
}
