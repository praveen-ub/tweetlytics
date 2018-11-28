package com.ir.project4.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ir.project4.dto.Hashtag;

@Service
public class HashtagService{
	
	@Value("${solr.end_point}")
	private String solrEndPoint;
	
	/*
	 * from the response, parse each document and collect hashtag if present
	 * add hashtags to a map of <String, Integer>, String being the hashtag 
	 * and Integer being the counter
	 *  sort map by descending values of counter
	 *  return top K
	 */
	public List<Hashtag> getTrendingHashtags(String city, String topic){
	
		SolrClient solr = new HttpSolrClient.Builder(solrEndPoint).build();	
		List<Hashtag> trendingHashTagsList = new ArrayList<Hashtag>();		
		int K = 3;
		try{
			
			SolrQuery query = new SolrQuery();
			query.setQuery("BiggBoss");
			//query.setCity(<city>);
			//query.setTopic(<topic>);
			QueryResponse response = solr.query(query);
			SolrDocumentList documentsList = response.getResults();
			HashMap<String, Integer> trendingHashTags = new HashMap<String, Integer>();
			for (SolrDocument document : documentsList){
				
					Object hashtagObject = document.get("hashtags");
					if(hashtagObject!=null){
						String hashtagContent = hashtagObject.toString();
						hashtagContent = hashtagContent.replace("[", "");
						hashtagContent = hashtagContent.replace("]", "");
						String[] hashtagArray = hashtagContent.split(" ");
						for (String hashtag: hashtagArray){
							hashtag = "#"+hashtag;
							if(trendingHashTags.containsKey(hashtag)){
								int count = trendingHashTags.get(hashtag);
								trendingHashTags.put(hashtag, count+1);
							}
							else{
								trendingHashTags.put(hashtag, 1);
							}
						}
					}
			}
			List<Map.Entry<String, Integer>> sortedHashtagEntries = getSortedHashtagsEntries(trendingHashTags.entrySet());
			sortedHashtagEntries = sortedHashtagEntries.subList(0, K); //topK
			for (Map.Entry<String, Integer> entry : sortedHashtagEntries) {
				
				Hashtag tag = new Hashtag();
				tag.setName(entry.getKey());
				tag.setCount(entry.getValue());
				trendingHashTagsList.add(tag);
			}
		
		}
		catch(Exception ioe){
			System.out.println("Exception occured");
			ioe.printStackTrace();
		}
		return trendingHashTagsList;
	}
	
	private List<Map.Entry<String, Integer>> getSortedHashtagsEntries(Set<Map.Entry<String, Integer>> hashtagEntries){
		
		List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<Map.Entry<String, Integer>>(hashtagEntries);
		Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
				return b.getValue().compareTo(a.getValue());
			}
		});
		return sortedEntries;
	}
	
}