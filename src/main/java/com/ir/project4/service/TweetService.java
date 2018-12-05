package com.ir.project4.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ir.project4.dto.Hashtag;
import com.ir.project4.entity.Tweet;

@Service
public class TweetService{
	
	@Value("${solr.end_point}")
	private String solrEndPoint;
	
	@Autowired
	private HashtagService hashTagService;
		
	private static HashMap<String, String> codeVsLanguage = new HashMap<String, String>();
	
	static{
		codeVsLanguage.put("english","en");
		codeVsLanguage.put("hindi","hi");
		codeVsLanguage.put("thai","th");
		codeVsLanguage.put("french","fr");
		codeVsLanguage.put("spanish","es");
	}
	
	
	public HashMap<String, List> filterTweets(JsonNode filter, String queryString){
		
		SolrClient solrClient = new HttpSolrClient.Builder(solrEndPoint).build();	
		SolrQuery query = new SolrQuery();
		HashMap<String, List> filterResults = new HashMap<String, List>();
		String tweetQuery = null;
		String hashtagQuery = null;
		if(filter!=null){
			String filterQuery = getFilterString(filter);
			hashtagQuery = filterQuery;
			tweetQuery = filterQuery;
			if(filterQuery.contains("sentiment")){
				tweetQuery = filterQuery+" AND tweet_emoticons:*";
			}
		}
		
		if(queryString == null){
			query.setQuery("*:*");
		}
		else{
			query.setQuery(queryString);
		}
		query.setRows(200);
		query.setSort("tweet_lang", ORDER.desc);
		query.setFilterQueries(tweetQuery);
		try{
			QueryResponse response = solrClient.query(query);
			SolrDocumentList documentsList = response.getResults();
			List<Tweet> tweetList = getTweetsListFromDocuments(documentsList);
			query.setRows(60000);
			query.setFilterQueries(hashtagQuery);
			response = solrClient.query(query);
			documentsList = response.getResults();
			List<Hashtag> hashtagList =  hashTagService.getTrendingHashtags(documentsList);
			filterResults.put("tweets",tweetList);
			filterResults.put("hashtags", hashtagList);
			
		}
		catch(Exception ioe){
			System.out.println("Exception occured while filtering tweets");
			ioe.printStackTrace();
		}
		return filterResults;
	}
	
	public HashMap<String, List> searchTweets(String queryString, JsonNode filter){
		
		SolrClient solrClient = new HttpSolrClient.Builder(solrEndPoint).build();	
		SolrQuery query = new SolrQuery();
		if(queryString == null){
			query.setQuery("*:*");
		}
		else{
			query.setQuery(queryString);
		}
		query.setRows(200);
		if(filter!=null){
			String filterQuery = getFilterString(filter);
			query.setFilterQueries(filterQuery);
		}
		HashMap<String, List> searchResults = new HashMap<String, List>();
		try{
			//Add logic for relevancy
			QueryResponse response = solrClient.query(query);
			SolrDocumentList documentsList = response.getResults();
			List<Tweet> tweetList = getTweetsListFromDocuments(documentsList);
			query.setRows(60000);
			response = solrClient.query(query);
			documentsList = response.getResults();
			List<Hashtag> hashtagList=  hashTagService.getTrendingHashtags(documentsList);
			searchResults.put("tweets",tweetList);
			searchResults.put("hashtags", hashtagList);
		}
		catch(Exception ioe){
			System.out.println("Exception occured while searching for tweets");
			ioe.printStackTrace();
		}
		return searchResults;	
	}
	
	
	
	public List<Tweet> getRelevantTweetsByHashtags(String hashtag, JsonNode filter){
		
		SolrClient solrClient = new HttpSolrClient.Builder(solrEndPoint).build();	
		hashtag = hashtag.replace("#", "");
		SolrQuery query = new SolrQuery();
		query.setQuery(hashtag);
		query.setRows(200);
		if(filter!=null){
			String filterQuery = getFilterString(filter);
			query.setFilterQueries(filterQuery);
		}
		//Set query boosting, more weight for hashtag
//		Add logic for relevancy
		List<Tweet> tweetList = null;
		try{
			QueryResponse response = solrClient.query(query);
			SolrDocumentList documentsList = response.getResults();
			tweetList = getTweetsListFromDocuments(documentsList);
		}
		catch(Exception ioe){
			System.out.println("Exception occured");
			ioe.printStackTrace();
		}
		return tweetList;	
	}
	
	
	private List<Tweet> getTweetsListFromDocuments(SolrDocumentList documentsList){
		
		List<Tweet> tweetList = new ArrayList<Tweet>();
		for (SolrDocument document : documentsList){
			
			Tweet tweet = new Tweet();
			String tweetText = document.get("tweet_text").toString();
			tweetText = tweetText.replace("[", "");
			tweetText = tweetText.replace("]", "");
			String tweetDate = document.get("tweet_date").toString();
			String sentiment = document.get("sentiment").toString();
			tweet.setTweetText(tweetText);
			tweet.setDate(tweetDate);
		    tweet.setSentiment(sentiment);
			
			tweetList.add(tweet);
		}
		return tweetList;
	}
	
	private String getOrFilter(String filterName, JsonNode filterValues){
		
		StringBuilder criteriaBuilder = new StringBuilder();
		int i = 1;
		for (JsonNode filterValue : filterValues) {
			String filterValueStr = filterValue.asText();
			if(!"sentiment".equals(filterName)){
				filterValueStr = filterValueStr.toLowerCase();
			}
			if(filterName.contains("lang")){
				filterValueStr = codeVsLanguage.get(filterValueStr);
			}
			else if(filterName.contains("city") && "New York".equals(filterValueStr)){
				filterValueStr = "nyc";
			}
			else if(filterName.contains("topic") && "Infrastructure".equals(filterValueStr)){
				filterValueStr = "infra";
			}
			else if(filterName.contains("topic") && "Social Unrest".equals(filterValueStr)){
				filterValueStr = "social unrest";
			}
			
			
			criteriaBuilder.append(filterName+":"+filterValueStr);
			
			if ((i < filterValues.size())) {
				criteriaBuilder.append(" OR ");
			}
			i++;
		}
		return criteriaBuilder.toString();
	}
	
	public String getFilterString(JsonNode filters){
		
		StringBuilder criteriaBuilder = new StringBuilder();
		int i=1;
		for (JsonNode filter : filters){
			
			String filterName = filter.get("name").asText();
			JsonNode filterValue = filter.get("value");
			String filterString = getOrFilter(filterName, filterValue);
			criteriaBuilder.append(filterString);
			if((i < filters.size())){
				criteriaBuilder.append(" AND ");	
			}
			i++;
		}
		return criteriaBuilder.toString();
	}
	
}