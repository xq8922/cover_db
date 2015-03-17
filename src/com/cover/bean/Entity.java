package com.cover.bean;

import java.io.Serializable;

/**
 * @TAG identify Message is Cover or Level
 * @author W
 * 
 */
public class Entity implements Serializable {

	private static final long serialVersionUID = -6348868576723913291L;
	short id = 0;
	Status status = null;
	String tag = null;
	double longtitude = 0;
	double latitude = 0;

	public Entity(short id,Status status, String tag,
			double latitude, double longtitude) {
		super();
		this.id = id;
		this.status = status;
		this.tag = tag;
		this.longtitude = longtitude;
		this.latitude = latitude;
	}

	public short getId() {
		return id;
	}

	public Entity() {
		super();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getTag() {
		return tag;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setId(short id) {
		this.id = id;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * 标志各种状态
	 * 
	 * @EXCEPTION_1 报警状态
	 * @EXCEPTION_2 欠压状态
	 * @EXCEPTION_3 报警欠压
	 * @SETTING_FINISH 撤防中状态
	 * @SETTING_PARAM 参数设置中状态
	 */
	public enum Status {
		NORMAL, REPAIR, EXCEPTION_1, EXCEPTION_2, EXCEPTION_3, SETTING_FINISH, SETTING_PARAM
	}

}
