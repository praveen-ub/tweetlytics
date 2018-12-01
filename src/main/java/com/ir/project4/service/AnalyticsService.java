package com.ir.project4.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ir.project4.entity.Chart;

@Service
public class AnalyticsService{
	
	@Value("${solr.end_point}")
	private String solrEndPoint;
	
	private static HashMap<String, String> fieldVsChartType = new HashMap<String, String>();
	
	private static HashMap<String, String> fieldNameVsDescription = new HashMap<String, String>();
	
	private static HashMap<String, String> languageVsCode = new HashMap<String, String>();
	
	private static HashMap<String, String> codeVsLanguage = new HashMap<String, String>();
	
	static{
		
		fieldVsChartType.put("tweet_lang", "bar");
		fieldVsChartType.put("city", "bar");
		fieldVsChartType.put("topic", "donut");
		
		fieldNameVsDescription.put("tweet_lang", "Language");
		fieldNameVsDescription.put("city", "City");
		fieldNameVsDescription.put("topic", "Topic");
		
		languageVsCode.put("en", "English");
		languageVsCode.put("fr", "French");
		languageVsCode.put("hi", "Hindi");
		languageVsCode.put("th", "Thai");
		languageVsCode.put("es", "Spanish");
		
		codeVsLanguage.put("english","en");
		codeVsLanguage.put("hindi","hi");
		codeVsLanguage.put("thai","th");
		codeVsLanguage.put("french","fr");
		codeVsLanguage.put("spanish","es");
		
	}
	
//	private String capitalise(String strToCapitalise){
//		
//		String[] tokens = strToCapitalise.split(" ");
//		StringBuilder strBuilder = new StringBuilder();
//		for (String token: tokens){
//			String capitalisedToken = 
//			
//		}
//	}
	
	public List<Chart> getCharts (String query, JsonNode filters){
		
		SolrClient solrClient = new HttpSolrClient.Builder(solrEndPoint).build();	
		SolrQuery solrQuery = new SolrQuery();
		
		StringBuilder criteriaBuilder = new StringBuilder();
		int i = 1;
		List<Chart> charts = new ArrayList<Chart>();
		
		if (query == null){
			query = "*:*";
		}
		
		if(filters!=null){
			for (JsonNode filter : filters){
				
				String filterName = filter.get("name").asText();
				String filterValue = filter.get("value").asText().toLowerCase();
				if(filterName.contains("lang")){
					filterValue = codeVsLanguage.get(filterValue);
				}
				criteriaBuilder.append(filterName+":"+filterValue);
				if((i < filters.size())){
					criteriaBuilder.append(" AND ");	
				}
				i++;
			}
			String criteria = criteriaBuilder.toString();
			solrQuery.setFilterQueries(criteria);
		}
		solrQuery.setQuery(query);
		solrQuery.setFacet(true);
		solrQuery.addFacetField("tweet_lang");
		solrQuery.addFacetField("city");
		solrQuery.addFacetField("topic");
		try{
			QueryResponse response = solrClient.query(solrQuery);
			List<FacetField> facetFields = response.getFacetFields();
			for (FacetField field: facetFields){
				Chart chart = new Chart();
				String fieldName = field.getName();
				String chartTitle = "By "+fieldNameVsDescription.get(fieldName);
				List<String> xData = new ArrayList<String>();
				List<Long> yData = new ArrayList<Long>();
				for (Count stat: field.getValues()){
					String x = stat.getName();
					if(fieldName == "tweet_lang"){
						x= languageVsCode.get(x);
					}
//					x = capitalise(x);
					xData.add(x);
					yData.add(stat.getCount());
				}
				chart.setTitle(chartTitle);
				chart.setChartType(fieldVsChartType.get(fieldName));
				chart.setxData(xData);
				chart.setyData(yData);
				charts.add(chart);
			}
		}
		catch(Exception ioe){
			System.out.println("Exception occured");
			ioe.printStackTrace();
		}
		return charts;
	}
}