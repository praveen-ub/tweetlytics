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
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private static HashMap<String, String> cityVsCountries = new HashMap<String, String>();
	
	@Autowired
	private TweetService tweetService;
	
	
	static{
		
		fieldVsChartType.put("tweet_lang", "bar");
		fieldVsChartType.put("city", "geo");
		fieldVsChartType.put("topic", "pie");
		
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
		
		cityVsCountries.put("delhi", "Delhi, India");
		cityVsCountries.put("paris", "Paris, France");
		cityVsCountries.put("mexico city", "Mexico City, Mexico");
		cityVsCountries.put("bangkok", "Bangkok, Thailand");
		cityVsCountries.put("nyc", "United States");
		
	}
	
	private String capitalise(String strToCapitalise){
		
		String[] tokens = strToCapitalise.split(" ");
		StringBuilder strBuilder = new StringBuilder();
		boolean isMultiWord = tokens.length > 1;
		for (int i=0;i<tokens.length;i++){
			String token = tokens[i];
			String capitalisedToken = token.substring(0, 1).toUpperCase() + token.substring(1);
			strBuilder.append(capitalisedToken);
			if(isMultiWord){
				strBuilder.append(" ");
			}
		}
		String captilasiedString = strBuilder.toString();
		return captilasiedString.trim();
	}
	
	public List<Chart> getCharts (String query, JsonNode filters){
		
		SolrClient solrClient = new HttpSolrClient.Builder(solrEndPoint).build();	
		SolrQuery solrQuery = new SolrQuery();
		
		List<Chart> charts = new ArrayList<Chart>();
		
		if (query == null){
			query = "*:*";
		}
		
		if(filters!=null){
			String filterQuery = tweetService.getFilterString(filters);
			solrQuery.setFilterQueries(filterQuery);
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
				List data = new ArrayList();
				Chart chart = new Chart();
				String fieldName = field.getName();
				boolean isLangField = false;
				boolean isCityField = false;
				if("tweet_lang".equalsIgnoreCase(fieldName)){
					isLangField = true;
					
				}
				if("city".equalsIgnoreCase(fieldName)){
					isCityField = true;
				}
				String chartTitle = "By "+fieldNameVsDescription.get(fieldName);
				for (Count stat: field.getValues()){
					String x = stat.getName();
					if(isLangField){
						x= languageVsCode.get(x);
					}
					if(isCityField){
						x = cityVsCountries.get(x);
					}
					x = capitalise(x);
					List rowData = new ArrayList();
					rowData.add(x);
					rowData.add(stat.getCount());
					data.add(rowData);
				}
				chart.setTitle(chartTitle);
				chart.setChartType(fieldVsChartType.get(fieldName));
				chart.setDimension(fieldName);
				chart.setData(data);
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