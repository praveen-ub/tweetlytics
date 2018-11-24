package com.ir.project4.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ir.project4.service.HashtagService;
import com.ir.project4.dto.AppResponse;

@RestController
@RequestMapping("/api/hashtags")
public class HashtagController{
	
	
	@Autowired
	private HashtagService hashtagService;
	
	@RequestMapping(value="/trending",method=RequestMethod.GET)
	public AppResponse advertise(@RequestParam("city") String city, @RequestParam("topic") String topic){
		
		System.out.println("Fetch trending hashtags for this topics and city::"+topic+"::"+city);
		List<String> hashtags = hashtagService.getTrendingHashtags(city, topic);
		return new AppResponse(200,"Success", hashtags);
	}
	
}