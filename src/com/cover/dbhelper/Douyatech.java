package com.cover.dbhelper;

import java.util.ArrayList;
import java.util.List;

import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;

import android.R.integer;
import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Douyatech {

	private static final String TAG = "Devicedb";
	private DouYaSqliteHelper helper;

	public Douyatech(Context context) {
		if (helper == null)
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
	 * add to table
	 */
	public void addEntity(String entityId, Status status, String tag,
			double lonti, double lati) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("entity_id", entityId);
		int tmp = 0;
		switch (status) {
		case NORMAL:
			tmp = 0;
			break;
		case REPAIR:
			tmp = 1;
			break;
		case EXCEPTION_1:
			tmp = 2;
			break;
		case EXCEPTION_2:
			tmp = 3;
			break;
		case EXCEPTION_3:
			tmp = 4;
			break;
		case SETTING_FINISH:
			tmp = 5;
			break;
		case SETTING_PARAM:
			tmp = 6;
			break;
		}
		contentValues.put("status", tmp);
		contentValues.put("old_status", tmp);
		contentValues.put("tag", tag);
		contentValues.put("lonti", lonti);
		contentValues.put("lati", lati);
		db.insert("entity", null, contentValues);
	}

	/**
	 * last_refresh datetime,activity varchar(20) refresh_time
	 */
	public void add_lastRefresh(String activity) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("activity", activity);
		db.insert("refresh_time", null, contentValues);
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

	public boolean isExistInEntity(String tag, String entityId) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from entity where tag=? and entity_id=?",
				new String[] { tag, entityId });
		return cursor.moveToNext();
	}

	/**
	 * update table entity_id varchar(10),status integer,tag
	 * varchar(10),lonti(double),lati(double),old_status integer
	 * 
	 * @param tableName
	 * @param tag
	 * @param entityId
	 * @param newColumn
	 * @param typeColumn
	 */
	public void updateColumn(String tableName, String tag, String entityId,
			double lonti, double lati, int status, boolean updateStatus) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		if (updateStatus == true) {
			contentValues.put("old_status", status);
		}
		contentValues.put("entity_id", entityId);
		contentValues.put("lonti", lonti);
		contentValues.put("lati", lati);
		contentValues.put("status", status);
		db.update(tableName, contentValues, "tag=?,entity_id=?", new String[] {
				tag, entityId });
	}

	public void updateLonLa(String tableName, String tag, String entityId,
			double lonti, double lati) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("lonti", lonti);
		contentValues.put("lati", lati);
		db.update(tableName, contentValues, "tag=?,entity_id=?", new String[] {
				tag, entityId });
	}

	public int getStatus(String tag, String entityId) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from entity where tag=? and entity_id=?",
				new String[] { tag, entityId });
		return cursor.getInt(cursor.getColumnIndex("old_status"));
	}

	public List<Entity> getEntityList() {
		List<Entity> entityList = new ArrayList<Entity>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query(true, "entity", new String[] { "tag",
				"entity_id", "status", "lonti", "lati", "old_status" },
				new String[] { "1" }, null, null, null, null);
		while(cursor.moveToNext()){
			Entity entity = new Entity();
			
		}
		return entityList;
	}

	/**
	 * 
	 * @param tag
	 * @param entityId
	 */
	public void updateStatusExceptTime(String tag, String entityId) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		int status = getStatus(tag, entityId);
		contentValues.put("status", status);
		db.update("entity", contentValues, "tag=?,entity_id=?", new String[] {
				tag, entityId });
	}

	/**
	 * isExist in last_refresh
	 * 
	 */
	public boolean isExistInRefresh() {
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from refresh_time", null);
		return cursor.moveToNext();
	}

	public String getLastRefresh() {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from refresh_time", null);
		return cursor.getString(1);
	}

	/**
	 * delete
	 * 
	 * @param tableName
	 * @param nameID
	 */
	public void delete(String tableName, String nameID) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + tableName + " where name_id=?",
				new Object[] { nameID });
		// db.delete(tableName, "name_id=?", new String[] { nameID });
	}

	/**
	 * 
	 * @param tableName
	 */
	public void deleteAll(String tableName) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + tableName);
	}

}
