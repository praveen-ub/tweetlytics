
var backEndHost = document.location.href.substring(0,document.location.href.length-1);

var filters = [
  // {
  //   "name":"city",
  //   "value":"Delhi"
  // }
];


function addFilter(filterName, filterValue){
  var filterFound = false;
  for (var idx in  filters){
      var filter = filters[idx];
      if(filter.name == filterName){
        filter.value.push(filterValue);
        filterFound = true;
      }
  }
  if(!filterFound){
    var filterValues = [];
    filterValues.push(filterValue);
    var filter = {
      name:filterName,
      value:filterValues
    }
    filters.push(filter);
  }
}

function removeFilter(filterName, filterValue){
  var filterFound = false;
  var filterIdx = -1;
  var filter;
  for (var idx in  filters){
      filter = filters[idx];
      if(filter.name == filterName){
        filterFound = true;
        filterIdx = idx;
        break;
      }
  }
  if(filterFound){
    var filterValues = filter.value;
    var filterValueIdx = -1;
    var filterValueFound = false;
    for (var valueIdx in filterValues){
        var value = filterValues[valueIdx];
        if(value == filterValue){
          filterValueFound = true;
          filterValueIdx = valueIdx;
          break;
        }
    }
    if(filterValueFound){
      filter.value.splice(filterValueIdx,1);
    }
    if(filter.value.length == 0){
      filters.splice(filterIdx,1);
    }
  }
}

function searchTweets(searchQuery){

    var filter = {filter:filters, query:searchQuery};
    $.ajax({
      url:`${backEndHost}/api/tweets/search`,
      type:"POST",
      data:JSON.stringify(filter),
      contentType:"application/json; charset=utf-8",
      dataType:"json",
      success: function(result){
          appendTweets(result.data.tweets);
          appendHashtags(result.data.hashtags);
      }
    });
  renderCharts();
}

function filterTweets(){

    var filter = {filter:filters};
    var searchQuery = $(".search-text").val();
    if(searchQuery){
      filter['query'] = searchQuery;
    }
    $.ajax({
      url:`${backEndHost}/api/tweets/filter`,
      type:"POST",
      data:JSON.stringify(filter),
      contentType:"application/json; charset=utf-8",
      dataType:"json",
      success: function(result){
          appendTweets(result.data.tweets);
          appendHashtags(result.data.hashtags);
      }
    });
}


function renderCharts(){

  console.log("Calling render charts");
  var requestData = {filter:filters};
  var searchQuery = $(".search-text").val();
  if(searchQuery){
    requestData['query'] = searchQuery;
  }
  $.ajax({
    url:`${backEndHost}/api/analytics/charts`,
    type:"POST",
    data:JSON.stringify(requestData),
    contentType:"application/json; charset=utf-8",
    dataType:"json",
    success: function(result){
        var charts = result.data;
        charts.forEach(function(chart){

            // var chartContainer = `<div id="${chart.dimension}-div"></div>`;
            // $(".charts_area").append(chartContainer);
            drawChart(chart, `${chart.dimension}-div`);
            console.log("Render the chart titled::"+chart.title);
        });
    }
  });

}


function fetchTweetsByHashtag(hashtagText){

  var filter = {filter:filters, hashtag:hashtagText};
  $.ajax({
    url:`${backEndHost}/api/tweets/filter_by_hashtag`,
    type:"POST",
    data:JSON.stringify(filter),
    contentType:"application/json; charset=utf-8",
    dataType:"json",
    success: function(result){
        appendTweets(result.data.tweets);
    }
  });
}

function appendTweets(tweets){
  var tweetsContainer = $("#tweets .middle_content");
  $(tweetsContainer).text(" ");
  $(tweetsContainer).html("<h2>Tweets</h2>");
  tweets.forEach(function(tweet){
    var tweetElement = `<div style=" border-bottom: 1px solid #b1b1b5; margin-bottom: 5px" id="tweet-data">
             ${tweet.text}
             <p style="color: grey" id="time">11/11/19 11:30:30am</p>
          </div>`;
    $(tweetsContainer).append(tweetElement);
  });
}

function appendHashtags(hashtags){

  var hashtagsContainer = $("#hashtags .list");
  $(hashtagsContainer).text(" ");
  hashtags.forEach(function (hashtag){
    var hashtagElement = `<li><a href="javascript:void(0)">${hashtag.name}</a></li>`;
    $(hashtagsContainer).append(hashtagElement);
    $("#hashtags .list li a").on("click",function(){
        fetchTweetsByHashtag($(this).text());
        console.log("Fetch relevant tweets for::"+$(this).text());
    });
  });

}

$(function(){

  $(".nav a").on("click", function(){
    $(".nav").find(".active").removeClass("active");
    $(this).parent().addClass("active");
    var tabClicked = $(this).text();
    if(tabClicked == 'Analytics'){
        $("#tweets").hide();
        $("#hashtags").hide();
        // $("#filter-section").hide();
        $("#analytics").show();
        renderCharts();
    }
    else{
        $("#analytics").hide();
        $("#tweets").show();
        $("#hashtags").show();
        // $("#filter-section").show();
    }
  });

  $(".lang-box").on("change",function(){
        var value = $(this).val();
        if(this.checked){
            addFilter('tweet_lang',value);
        }
        else{
            removeFilter('tweet_lang',value);
        }
        filterTweets();
        renderCharts();
  });

  $(".city-box").on("change",function(){
      var value = $(this).val();
      if(this.checked){
        addFilter('city',value);
      }
      else{
          removeFilter('city',value);
      }
      filterTweets();
      renderCharts();
  });

  $(".topic-box").on("change",function(){
      var value = $(this).val();
      if(this.checked){
        addFilter('topic',value);
      }
      else{
          removeFilter('topic',value);
      }
      filterTweets();
      renderCharts();
  });

  $('.search-text').bind("enterKey",function(e){
    var searchQuery = $(".search-text").val();
    if(searchQuery){
        searchTweets(searchQuery);
    }
  });

  $('.search-text').keyup(function(e){
    if(e.keyCode == 13)
    {
        $(this).trigger("enterKey");
    }
  });

  $(".search-button").on("click",function(){
    var searchQuery = $(".search-text").val();
    if(searchQuery){
        searchTweets(searchQuery);
    }
  });
  filterTweets();

});
