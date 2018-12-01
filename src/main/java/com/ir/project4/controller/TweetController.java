package com.ir.project4.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.ir.project4.dto.AppResponse;
import com.ir.project4.entity.Tweet;
import com.ir.project4.service.TweetService;

@RestController
@RequestMapping("/api/tweets")
public class TweetController{
	
	@Autowired
	private TweetService tweetService;
	
	
	@RequestMapping(value="/filter",method=RequestMethod.POST)
	public AppResponse filterTweets(@RequestBody JsonNode request){
		
		JsonNode filter = request.get("filter");
		HashMap<String, List> filterResults = tweetService.filterTweets(filter);
		return new AppResponse(200,"Success", filterResults);
	}
	
	@RequestMapping(value="/filter_by_hashtag",method=RequestMethod.POST)
	public AppResponse getRelevantTweetsByHashtag(@RequestBody JsonNode request){
		
		JsonNode filter = request.get("filter");
		String hashtag = request.get("hashtag").asText();
		List<Tweet> tweets = tweetService.getRelevantTweetsByHashtags(hashtag,filter);
		return new AppResponse(200, "Success",tweets);
	}
	
	@RequestMapping(value="/search",method=RequestMethod.GET)
	public AppResponse searchTweets(@RequestParam("query") String query){
		
		HashMap<String, List> searchResults = tweetService.searchTweets(query); 
		return new AppResponse(200,"Success", searchResults);
	}
	
}