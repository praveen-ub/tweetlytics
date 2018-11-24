package com.ir.project4.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class HashtagService{
	
	
	public List<String> getTrendingHashtags(String city, String topic){
		
		List<String> trendingHashTags = new ArrayList<String>();
		trendingHashTags.add("Modi");
		trendingHashTags.add("Rahul");
		trendingHashTags.add("BusinessIndia");
		return trendingHashTags;
		
	}
	
}