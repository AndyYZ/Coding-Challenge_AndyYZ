Insight Data Engineering - Coding Challenge - AndyYZ
===========================================================

In this coding challenge, tools are that help analyze the community of Twitter users.   

## Challenge Summary

Two features are implemented for the analysis of tweets:

1. Extract and clean the text from the raw JSON tweets that come from the Twitter Streaming API, and track the number of tweets that contain unicode.

  Three steps are taken to fulfill the first feature:  
  (1) Read JSON tweets from raw data, one at a time. Some JOSN tweets are not standard Tweet payloads and need to be skipped. One Example of nonstandard payloads are limit notices, like "{"limit":{"track":5,"timestamp_ms":"1446218985743"}}". More examples can be found on https://dev.twitter.com/streaming/overview/messages-types#user_stream_messsages.  
  (2) Eextract the "text" and "created_at" fileds from JSON tweets.  
  (3) Clean the "text" filed:  
    --First replace all non-ASCII unicode characters will an empty string. Non-ASCII unicode characters are those not in the range[0X0000, 0X007F]. <i><b>If the length of the text get reduced after removing the unicode characters, this tweet is counted as one that contains unicode.</i> </b>   
    --Then replace whitespace characters( as in the set [ \t\n\x0B\f\r]) with a single space.  
    The text is considered to be clean after this step.  
  (4) Generate the output string with the "clean text" and timestamp extracted from "created_at" field with the format of  
  
	  <contents of "text" field> (timestamp: <contents of "created_at" field>)  
  (5) Output the output string to the text file named ft1.txt in the directory named tweet_input.  
  (6) write the following message at the bottom of the output file (with a newline preceding it):

      <number of tweets that had unicode> tweets contained unicode.
The task with implementation of feature one is completed.  


2. Calculate the average degree of a vertex in a Twitter hashtag graph for the last 60 seconds, and update this each time a new tweet appears.

  A HashTagGraph class is constructed to implment this second feature. The HashTagGraph class represents an undirected dynamic graph. Its vertices are named after hashtags extracted from JSON files of twitter. For each edge in the graph, an edge field is created to store its two ends' names and a time field is created to record when it is added to the graph or updated. 
  Each time a new tweet comes in, the HashTageGraph is updated:
  	(a) For each edge formed by the hashtages in the latest tweet, if it is not present in the current graph, this edge and the time it is formed are added; if it is already present in the graph, simply update the time field associated the edge. No weight is assgined to the edges in the hashtag graph.  
  	(b) For those edges added or updated more than 60 seconds ago by the time a new tweet comes in, they will be removed.
  	
  	
  	
  

