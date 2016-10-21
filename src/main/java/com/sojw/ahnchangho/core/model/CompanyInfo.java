package com.sojw.ahnchangho.core.model;

public class CompanyInfo {
	private String name;
	private String stockCode;

	public CompanyInfo(String name, String stockCode) {
		super();
		this.name = name;
		this.stockCode = stockCode;
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
}