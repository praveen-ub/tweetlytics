package com.ir.project4.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.ir.project4.dto.AppResponse;
import com.ir.project4.entity.Chart;
import com.ir.project4.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController{
	
	@Autowired
	private AnalyticsService analyticsService;
	
	@RequestMapping(value="/charts",method=RequestMethod.POST)
	public AppResponse getCharts(@RequestBody JsonNode request){
		
		String query = null;
		if(request.get("query")!=null){
			query = request.get("query").asText();
		}
		JsonNode filters = request.get("filter");
		List<Chart> charts = analyticsService.getCharts(query, filters);
		return new AppResponse(200, "Success",charts);
	} 
}