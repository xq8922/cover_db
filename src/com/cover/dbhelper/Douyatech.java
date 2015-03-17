package com.cover.dbhelper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Douyatech {

	private static final String TAG = "Devicedb";
	private DouYaSqliteHelper helper;

	public Douyatech(Context context) {
		helper = new DouYaSqliteHelper(context);
	}

	/**
	 * 判断是否已经存在于数据库中，即是否已经添加过
	 * 
	 * @param name
	 * @return
	 */
	public boolean exist(String name) {
		// SQLiteDatabase db = helper.getReadableDatabase();
		// Cursor cursor = db.query("deviceinfo", null, "devicename=?",
		// new String[] { name }, null, null, null);
		// return cursor.moveToNext();
		return false;
	}

	/**
	 * 插入
	 * 
	 * @param tableName
	 * @param nameID
	 */
	public void add(String tableName, String nameID) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("name_id", nameID);
		db.insert(tableName, null, contentValues);
	}

	/**
	 * 查询所有已经绑定的设备
	 * 
	 * @return List<DeviceInfo>
	 */
	public boolean isExist(String tableName, String nameID) {
		List<String> infos = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + tableName
				+ " where name_id=?", new String[] { nameID });
		// Cursor cursor=db.query(tableName, null, null, null, null, null,
		// null);
		return cursor.moveToNext();
	}

	public void delete(String tableName, String nameID) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + tableName + " where name_id=?",
				new Object[] { nameID });
		// db.delete(tableName, "name_id=?", new String[] { nameID });
	}

	public void deleteAll(String tableName) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + tableName);
	}

}
