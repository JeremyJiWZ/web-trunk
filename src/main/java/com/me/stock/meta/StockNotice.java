package com.me.stock.meta;

public class StockNotice {
	private long id;
	private String code;
	private String stockName;
	private byte isRead;
	private float b1v;
	private int b1n;
	private float s1v;
	private int s1n;
	private long time;
	private byte peType;
	
	public byte getPeType() {
		return peType;
	}
	public void setPeType(byte peType) {
		this.peType = peType;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public byte getIsRead() {
		return isRead;
	}
	public void setIsRead(byte isRead) {
		this.isRead = isRead;
	}
	public float getB1v() {
		return b1v;
	}
	public void setB1v(float b1v) {
		this.b1v = b1v;
	}
	public int getB1n() {
		return b1n;
	}
	public void setB1n(int b1n) {
		this.b1n = b1n;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public float getS1v() {
		return s1v;
	}
	public void setS1v(float s1v) {
		this.s1v = s1v;
	}
	public int getS1n() {
		return s1n;
	}
	public void setS1n(int s1n) {
		this.s1n = s1n;
	}
	
}
