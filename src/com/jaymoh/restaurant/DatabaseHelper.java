package com.jaymoh.restaurant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	private static final int DATABASE_VERSION=1;
	private static final String DATABASE_NAME="restaurant";
	private static final String OWNER_TABLE_NAME="owner_table";
	private static final String CUSTOMER_TABLE_NAME="customer_table";
	
	private static final String ID="_id";
	private static final String MEAL_NAME="meal_name";
	private static final String CUSTOMER="customer";
	private static final String PASSWORD="password";
	private static final String MESSAGE="message";
	
	private static final String HOTEL_ID="hotel_id";
	
	
	public DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXITS "+OWNER_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXITS "+CUSTOMER_TABLE_NAME);
		 
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
	db.execSQL("CREATE TABLE "+OWNER_TABLE_NAME+"("
			+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
			+MEAL_NAME+" STRING, "
			+CUSTOMER+" STRING, " 
			+PASSWORD+"STRING, "
			+MESSAGE+" STRING)");	
	
	db.execSQL("CREATE TABLE "+CUSTOMER_TABLE_NAME +"("
			+ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
			+MEAL_NAME+" STRING, " 
			+HOTEL_ID+" STRING, "
			+MESSAGE+" STRING )");
	}
	
	public void insertMessage(String mealname, String customer, String password, String message)
	{
		SQLiteDatabase db=getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv.put(MEAL_NAME,mealname);
		cv.put(CUSTOMER, customer);
		cv.put(PASSWORD, password);
		cv.put(MESSAGE, message);
		db.insert(OWNER_TABLE_NAME, null, cv);
		db.close();
	}
	
	public void insertResponse(String mealname, String hotelName, String message)
	{
		SQLiteDatabase db=getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv.put(MEAL_NAME, mealname);
		cv.put(HOTEL_ID, hotelName);
		cv.put(MESSAGE, message);
		db.insert(CUSTOMER_TABLE_NAME, null, cv);
		db.close();
	}
	
	//get single record
	public Cursor getSingleRecord(long id)
	{
		SQLiteDatabase db=this.getReadableDatabase();
		return db.query(OWNER_TABLE_NAME, null, ID+" = "+id, null, null, null, null);
	}
	
	//retrieve messages
	public Cursor getAllMessages()
	{
		SQLiteDatabase db=this.getReadableDatabase();
		return db.query( OWNER_TABLE_NAME, new String[]{ID, MEAL_NAME, CUSTOMER, MESSAGE}, null, null, null, null, ID+" DESC", null);
	}
	public Cursor getAllResponses()
	{
		SQLiteDatabase db=this.getReadableDatabase();
		return db.query(CUSTOMER_TABLE_NAME, new String[]{ID,MEAL_NAME, HOTEL_ID, MESSAGE}, null, null, null, null, ID+" DESC", "10");
	}
	
	//delete single message
	public void deleteMessage(Long params)
	{
		SQLiteDatabase db=this.getWritableDatabase();
		db.delete(OWNER_TABLE_NAME, ID +" = " + params, null);
		db.close();
	}
	public void deleteMessageCustomer(Long params)
	{
		SQLiteDatabase db=this.getWritableDatabase();
		db.delete(CUSTOMER_TABLE_NAME, ID +" = " + params, null);
		db.close();
	}
	
	//open connection
	public void open() throws SQLException
	{
		SQLiteDatabase db=this.getWritableDatabase();
	}
	
	//select password
	public String getPass(String params)
	{
		SQLiteDatabase db=this.getWritableDatabase();
		String sql="SELECT password FROM "+OWNER_TABLE_NAME+" WHERE customer =?";
		Cursor c=db.rawQuery(sql, new String[]{params});
		String pass=c.getString(0);
		return pass;
	}
}
