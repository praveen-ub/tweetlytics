package com.ir.project4.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Tweet{
	
	private String id;
	
	private String city;
	
	private String topic;
	
	private String tweetLang;
	
	@JsonProperty("text")
	private String tweetText;
	
	private List<String> mentions;
	
	private List<String> hashtags;
	
	private List<String> urls;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTweetLang() {
		return tweetLang;
	}

	public void setTweetLang(String tweetLang) {
		this.tweetLang = tweetLang;
	}

	public String getTweetText() {
		return tweetText;
	}

	public void setTweetText(String tweetText) {
		this.tweetText = tweetText;
	}

	public List<String> getMentions() {
		return mentions;
	}

	public void setMentions(List<String> mentions) {
		this.mentions = mentions;
	}

	public List<String> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<String> hashtags) {
		this.hashtags = hashtags;
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}
	
	
	
}