package com.ir.project4.entity;

import java.util.List;

public class Chart{
	
	private String title;
	
	private List data;
	
	public List getData() {
		return data;
	}

	public void setData(List data) {
		this.data = data;
	}

	private String chartType;
	
	private String dimension;

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	
	
}