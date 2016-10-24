package com.sojw.ahnchangho.core.model;

public class CompanyInfo {
	private String name;
	private String stockCode;
	private String dividenRecord;

	public CompanyInfo(String name, String stockCode, String dividenRecord) {
		this.name = name;
		this.stockCode = stockCode;
		this.dividenRecord = dividenRecord;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getDividenRecord() {
		return dividenRecord;
	}

	public void setDividenRecord(String dividenRecord) {
		this.dividenRecord = dividenRecord;
	}
}