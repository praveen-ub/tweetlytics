package com.ir.project4.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.ir.project4.dto.Hashtag;
import com.ir.project4.entity.Tweet;

@Service
public class TweetService{
	
	@Value("${solr.end_point}")
	private String solrEndPoint;
	
	@Value("${google.translate.endpoint}")
	private String translateEndPoint;
	
	@Value("${google.translate.key}")
	private String translateApiKey;
	
	@Autowired
	private HashtagService hashTagService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	
		
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
		if(filter.size() > 0){
			String filterQuery = getFilterString(filter);
			hashtagQuery = filterQuery;
			tweetQuery = filterQuery;
			if(filterQuery.contains("sentiment")){
				tweetQuery = filterQuery+" AND tweet_emoticons:*";
			}
		}
		else{
			tweetQuery = "tweet_lang:en";
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
		String filterQuery = null;
		if(filter.size() > 0){
			filterQuery = getFilterString(filter);
		}
		else{
			filterQuery = "tweet_lang:en";
		}
		query.setFilterQueries(filterQuery);
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
			String language = document.get("tweet_lang").toString();
			tweet.setTweetText(tweetText);
			tweet.setDate(tweetDate);
		    tweet.setSentiment(sentiment);
		    tweet.setTweetLang(language);
			tweetList.add(tweet);
		}
		return tweetList;
	}
	
	private String getOrFilter(String filterName, JsonNode filterValues){
		
		StringBuilder criteriaBuilder = new StringBuilder();
		criteriaBuilder.append("(");
		int i = 1;
		for (JsonNode filterValue : filterValues) {
			String filterValueStr = filterValue.asText();
			if(!"sentiment".equals(filterName)){
				filterValueStr = filterValueStr.toLowerCase();
			}
			if(filterName.contains("lang")){
				filterValueStr = codeVsLanguage.get(filterValueStr);
			}
			else if(filterName.contains("city") && "New York".equalsIgnoreCase(filterValueStr)){
				filterValueStr = "nyc";
			}
			else if(filterName.contains("topic") && "Infrastructure".equalsIgnoreCase(filterValueStr)){
				filterValueStr = "infra";
			}
			else if(filterName.contains("topic") && "Social Unrest".equalsIgnoreCase(filterValueStr)){
				filterValueStr = "social unrest";
			}
			if(filterValueStr.contains(" ")){
				filterValueStr = "\""+filterValueStr+"\"";
			}
			
			
			criteriaBuilder.append(filterName+":"+filterValueStr);
			
			if ((i < filterValues.size())) {
				criteriaBuilder.append(" OR ");
			}
			i++;
		}
		criteriaBuilder.append(")");
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
	
	public String getTranslatedText(String textToTranslate, String language){
		
		System.out.println("Language is::"+language);
		String translatedQuery = null;
		try{
		   
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);

		    Map<String, String> uriParams= new HashMap<String, String>();
		    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(translateEndPoint)
		            // Add query parameter
//		            .queryParam("q", "सच में . भारत की महान आध्यात्मिक संस्कृति के वाहक ये गाली गलौज करने वाले मोदी और मोदी भक्त ही है")
		    		.queryParam("q", textToTranslate)
		            .queryParam("key", translateApiKey)
		            .queryParam("target", "en")
		            .queryParam("source", language)
		            .queryParam("format", "text");

		    System.out.println(builder.buildAndExpand(uriParams).toUri());
		   
		    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		    ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri() , HttpMethod.POST,
		            requestEntity, JsonNode.class);
		    String translatedText = responseEntity.getBody().get("data").get("translations").get(0).get("translatedText").asText();
		    
		    System.out.println("Response entity is::"+responseEntity);
		    return translatedText;
		}
		catch(Exception e){
			System.out.println("Exception occured while translating query");
			e.printStackTrace();
		}
		return translatedQuery;
	}
	
}