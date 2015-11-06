package src;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * The CodeChallengeAZ class implements the two features as required by Insight Data
 * Engineering Coding Challenge:
 * 1. Clean and extract the text from the raw JSON tweets that come from the Twitter
 *    Streaming API, and track the number of tweets that contain unicode.
 *    
 *    <i><b>How to determine if a tweet contains unicode?</i></b>
 *    For each tweet, compare the length of the text before and after removing the non
 *    ASCII unicode. If the length is reduced, this tweet is counted to be one that
 *    contains unicode.
 *    
 * 2. Calculate the average degree of a vertex in a Twitter hashtag graph for the last
 *    60 seconds, and update this each time a new tweet appears.
 *    
 *    The average degree of a vertex in a hashtag graph is obtained by dividing the two 
 *    times the total number of edges by the total number of vertices.
 *   
 *    The implementation of a hashtag graph uses an edge-list representation. For
 *    further details please refer to HashTagGraph class.
 *      
 *    
 * The results of this first feature is output to a text file named ft1.txt in a 
 * directory named tweet_output. The results of the second feature is output to a
 * text file named ft2.txt in the directory.
 * 
 *
 */


public class CodeChallengeAZ {
	public static void main(String[] arg) {
		In in;
		Out out1, out2;
		
		//Initialize an empty hashtag graph
		HashTagGraph htGraph = new HashTagGraph();
		
		//Set the output location
		out1 = new Out(arg[0]);
		out2 = new Out(arg[1]);
		try {
			in = new In(arg[2]);
			
			//use numUnicode to record the number of tweet containing unicode
			int numUnicode = 0;
			
			while (!in.isEmpty()) {
				//read one JSON object
				String s = in.readLine();
				
				//if the line read-in is not a valid JSON object, skip it
				if (s.length() == 0 || s.charAt(0) != '{') {
					continue;
				} 
				
				JSONObject one_tweet = new JSONObject(s);
				
				//If the string read-in is not a standard tweet, skip this string
				if (!one_tweet.has("text") || !one_tweet.has("created_at")) {
					continue;
				}
				
				/*
				 * Feature 1: clean the tweet text
				 */
				//extract text and timestamp entities from JSON object
				String text = one_tweet.getString("text");
				String timestamp = one_tweet.getString("created_at");

				//Compare the text length before and after removing the 
				//non-ASCII unicode. The cleaned text contains unicode 
				//if its length get reduced.
				int orgLen = text.length();
				text = text.replaceAll("[^\\x00-\\x7F]", "");
				if (text.length() < orgLen) {
					numUnicode++;
				}
				
				//replace all whitespace characters with a single space
				text = text.replaceAll("\\s+", " ");
				
				StringBuilder toOutput = new StringBuilder();
				toOutput.append(text).append(" (timestamp: ");
				toOutput.append(timestamp).append(")");
				out1.println(toOutput.toString());
				
				
				/*
				 * Featue 2: update hashtag graph when a new tweet comes in
				 * and calculate the average degree of the vertex.
				 * 
				 */
				
				//Step 1: get all hashtag array from raw JSON data
				JSONArray hashtagArray = one_tweet.getJSONObject("entities").getJSONArray("hashtags");
				String[] hashtags = new String[hashtagArray.length()];
				
			
				//step 2: extract hashtag string and clean it, i.e. remove unicodes,
				//covert all hashtag strings to lower case because hashtags 
				//are case insensitive.
				for (int i = 0; i < hashtags.length; i++) {
					hashtags[i] = hashtagArray.getJSONObject(i).getString("text");
					hashtags[i] = hashtags[i].replaceAll("[^\\x00-\\x7F]", "").toLowerCase(); 
					
				}
				
				//step 3: format the timestamp to convert it to Date object
				SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
				Date curTime = format.parse(timestamp);
				
				//step 4: update the hashtag graph with the hashtag array and 
				//the timestamp of the incoming tweet
				//it is possible some hashtags after cleaning will end up empty string. 
				//it is also possible there are duplicate hashtags in one tweet.
				//these cases will be handled during the update of the hashtag graph
				htGraph.updateGraph(hashtags, curTime);
				
				//get average degree of the vertex and output it to ft2.txt
				double avrDegree = htGraph.getAverageDegree();
				out2.printf("%.2f%n", avrDegree);
			}
			
			out1.println();
			out1.println(numUnicode + " tweets contained unicode.");
			in.close();
			out1.close();
			out2.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
}