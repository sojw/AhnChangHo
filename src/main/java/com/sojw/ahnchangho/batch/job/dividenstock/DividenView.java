package com.sojw.ahnchangho.batch.job.dividenstock;

/**
 * The Class DividenView.
 */
public class DividenView {
	private String stockCode;
	private String stockName;
	private String rate;
	private String nowVal;
	private Double dividen;
	private String divedenRecord;
	private Integer improveFinanceGrade;
	private Integer improveFinancePoint;

	public Integer getImproveFinancePoint() {
		return improveFinancePoint;
	}

	public void setImproveFinancePoint(Integer improveFinancePoint) {
		this.improveFinancePoint = improveFinancePoint;
	}

	public Integer getImproveFinanceGrade() {
		return improveFinanceGrade;
	}

	public void setImproveFinanceGrade(Integer improveFinanceGrade) {
		this.improveFinanceGrade = improveFinanceGrade;
	}

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getStockName() {
		return stockName;
	}
	
	public String getStockNameHtml() {
		if(improveFinanceGrade < 3) {
			return "<font color='red'>" + stockName + "</font>";	
		}
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getNowVal() {
		return nowVal;
	}

	public void setNowVal(String nowVal) {
		this.nowVal = nowVal;
	}

	public Double getDividen() {
		return dividen;
	}

	public void setDividen(Double dividen) {
		this.dividen = dividen;
	}

	public String getDivedenRecord() {
		return divedenRecord;
	}

	public void setDivedenRecord(String divedenRecord) {
		this.divedenRecord = divedenRecord;
	}
}