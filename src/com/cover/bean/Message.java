package com.cover.bean;

public class Message {
	public final byte header1 = (byte) 0xFA;
	public final byte header2 = (byte) 0xF5;
	public byte[] length = new byte[2];
	public byte function;
	public byte[] data;
	public byte[] check = new byte[2];

	public int getLength() {
		return 7 + (data == null ? 0 : data.length);
	}
}
