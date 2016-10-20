package com.sojw.ahnchangho.core.model;

import com.sojw.ahnchangho.core.type.BaseObject;

public class DividenStock extends BaseObject {
	private static final long serialVersionUID = -9182723106850976800L;
	private String name;
	private Double dividen;

	public DividenStock(String name, Double dividen) {
		super();
		this.name = name;
		this.dividen = dividen;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getDividen() {
		return dividen;
	}

	public void setDividen(Double dividen) {
		this.dividen = dividen;
	}
}