package com.ir.project4.entity;

import java.util.List;

public class Chart{
	
	private String title;
	
	private List<String> xData;
	
	private List<Long> yData;
	
	private String chartType;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getxData() {
		return xData;
	}

	public void setxData(List<String> xData) {
		this.xData = xData;
	}

	public List<Long> getyData() {
		return yData;
	}

	public void setyData(List<Long> yData) {
		this.yData = yData;
	}
	
	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	
	
}