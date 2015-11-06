package src;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;



/**
 * The HashTagGraph class represents an undirected unweighted dynamic graph. Its vertices 
 * are named after hashtags extracted from JSON files of twitter. Each time the HashTageGraph
 * is updated, newly incoming edges are added and edges staying longer than 60 seconds in 
 * the graph will be deleted. The concept of Least Recently Used (LRU) is used to update 
 * the list of edges in the graph.
 * 
 * It supports the following operations: add an edge to the graph, remove an edge from the
 * graph. It also provides methods for returning the number of vertices and the number of 
 * edges, as well as the average degree of a vertex in the graph.
 * 
 * The implementation of HashTagGraph uses an edge-list representation. The edge list is a 
 * doubly linked-list. Each Node in the edge list consists of two part: an edge object and 
 * the time when the edge is added or last updated. the edge list is arranged in the order
 * of time, where the head of the list stores the information of the latest edge and the tail
 * of the list stores the information of the oldest edge. 
 * 
 * Each time a new tweet comes in, the HashTageGraph is updated:
 * (a) For each edge formed by the hashtages in the latest tweet, if any, if it is not present 
 * in the current graph, this edge and the time it is formed are added; if it is already present
 *  in the graph, simply update the time field associated the edge. 
 * (b) For those edges added or updated more than 60 seconds ago by the time the new tweet comes 
 * in, they will be removed from the linked list. 
 * 
 */

public class HashTagGraph {
	
	/**
	 * The Edge class represents an edge in the graph.  
	 *
	 */
	class Edge{
		String one;
		String two;
		String pair;

		/**
		 * Initializes an edge from two vertex strings. 
		 * 
		 * String one is lexicographically smaller than string two.
		 * 
		 * @param one  one vertex in the edge
		 * @param two  the other vertex in the edge
		 * @param pair the string representation of the edge given by concatenating
		 *             two vertex strings in the edge. During the concatenation, 
		 *             string one precedes the string two. 
		 */
		public Edge(String one, String two) {
			this.one = one;
			this.two = two;
			StringBuilder sb = new StringBuilder();
			this.pair = sb.append("#").append(one).append("#").append(two).toString();	
		}
	}
	

	/**
	 * the EdgeNode class represents a node in the linked-list of edges.
	 *
	 */
	class EdgeNode {
		EdgeNode pre;
		EdgeNode next;
		Edge edge;
		Date date;
		
		
		/**
		 * Initializes an edgenode with an edge and its incoming time
		 * @param edge  one edge in this graph
		 * @param date  time this edge is added or last updated
		 */
		public EdgeNode(Edge edge, Date date) {
			this.edge = edge;
			this.date = date;
		}
	}
	
	private EdgeNode head = null;
	private EdgeNode tail = null;
	
	//vertexMap key: the vertex or hashtag;  value: degree of the vertex
	private TreeMap<String, Integer> vertexMap;
	
	//edgeMap key: edge; value: edgenode associated with the edge
	private TreeMap<String, EdgeNode> edgeMap = new TreeMap<String, EdgeNode>();

	
	/**
	 * Initialize an empty graph.
	 * The keys of the vertexMap are the string representation of the vertices.
	 * The values of the vertexMap are the degrees of the vertices.
	 * The keys of the edgeMap are the string representation of the edges.
	 * The values of the edgeMap are EdgeNode objects.
	 */
	public HashTagGraph() {
		this.vertexMap = new TreeMap<String, Integer>();
		this.edgeMap = new TreeMap<String, EdgeNode>();
	}
	
	
	/**
	 * Updates this graph.
	 * Before the update, the array of vertices is preprocessed: the 
	 * duplicated elements are removed.
	 * 
	 * @param vertices an array of vertices to be added or updated
	 * @param date     time when the array of vertices are generated
	 */
	public void updateGraph(String[] vertices, Date date) {
		String[] vertexDedup = null;
		if (vertices.length > 1) {
			vertexDedup = arrayDedup(vertices);
			addVertexEdges(vertexDedup, date);
		}
		
		//remove edges older than 60 seconds and update the degree of 
		//vertices associated with the removed edges
		removeVertexEdges(date);
	}
	
	
	/**
	 * @return the number of edges in this graph
	 */
	public int edgeNumber() {
		return edgeMap.size();
	}
	
	
	/**
	 * @return the number of vertices in this graph
	 */
	public int vertexNumber() {
		return vertexMap.size();
	}
	
	
	/**
	 * @param   vertex the vertex whose degree is returned
	 * @return  the degree of this vertex. If the vertex is not present in
	 *          the graph, return -1
	 */
	public int getDegree(String vertex) {
		Integer degree = vertexMap.get(vertex);
		if (degree == null) {
			throw new NullPointerException("Hashtag \"" + vertex + "\" is not present in the graph.");
		}
		return degree;
	}
	
	
	/**
	 * @return the average degree of this graph
	 */
	public double getAverageDegree() {
		if (vertexMap.size() == 0) {
			return 0;
		}
		return (double)edgeMap.size() * 2.0 / (double)vertexMap.size();
	}
	
	
	/**
	 * Returns the vertices in this graph
	 * 
	 * @return the vertices, as an Iterable
	 */
	public Iterable<String> vertexSet() {
		if (vertexMap.isEmpty()) {
			return null;
		}
		Set<String> keySet = vertexMap.keySet();
		return keySet;
	}
	
	
	/**
	 * Adds and updates vertices and edges in this graph.
	 * If the number of vertices to be added is smaller than two, no edge will
	 * be formed and added into this graph.
	 * 
	 * @param vertexLowercase an array of vertices to be added or updated.
	 *                        The array is sorted, with no duplicate elements.
	 * @param date            time when the array of vertices are generated.
	 */
	private void addVertexEdges(String[] vertexDedup, Date date) {
		if(vertexDedup.length < 2) {
			return;
		}
		
		//get the degree of each vertex of this graph before update
		//if the vertex is not present yet in the graph, set its degree to zero
		Integer[] degreeVertex = new Integer[vertexDedup.length];
		for (int i = 0; i < vertexDedup.length; i++) {
			degreeVertex[i] = vertexMap.get(vertexDedup[i]);
			if (degreeVertex[i] == null) {
				degreeVertex[i] = 0;
			}
		}
	
		for (int i = 0; i < vertexDedup.length; i++) {
			for (int j = i + 1; j < vertexDedup.length; j++) {
				
				//add or update the formed edge from the coming new tweet 
				Edge edge = new Edge(vertexDedup[i], vertexDedup[j]);
				boolean isNewEdge = setEdge(edge, date);
				
				//increase the degree of its two ends by one if the edge was
				//not present in the graph 
				if (isNewEdge) {
					degreeVertex[i]++;
					degreeVertex[j]++;
				}
			}
			
			//update the degree of the vertex in the vertexMap
			vertexMap.put(vertexDedup[i], degreeVertex[i]);

			
			
		}
		return;
	}
	
	
	/**
	 * Removes edges staying longer than 60 seconds in this graph 
	 * when the graph is updated.
	 * When an edge is removed, the degrees of two vertices in the
	 * edge are reduced by one, respectively.
	 * if the degree of an vertex is reduced to zero, this vertex is 
	 * removed from the graph .
	 * 
	 * @param date time when the graph is updated
	 */
	private void removeVertexEdges(Date date) {
		while (tail != null) {
			if (date.getTime() - tail.date.getTime() >= 60000) {
				
				//reduce the number of corresponding vertices from vertexMap
				String one = tail.edge.one;
				String two = tail.edge.two;
				int countOne = vertexMap.get(one) - 1;
				int countTwo = vertexMap.get(two) - 1;
				
				if (countOne == 0) {
					vertexMap.remove(one);
				} else {
					vertexMap.put(one, countOne);
				}
				if (countTwo == 0) {
					vertexMap.remove(two);
				} else {
					vertexMap.put(two, countTwo);
				}
				
				//remove edges staying longer than 60s from edgeMap
				edgeMap.remove(tail.edge.pair);
				
				//remove the tail from the edge list and reset the new tail
				remove(tail);
			} else {
				break;
			}
		}
		return;
	}

	
	/**
	 * Returns true if the edge is not present in the graph.
	 * Create a new EdgeNode for the edge if it is not present in the graph. 
	 * Update the date field in the EdgeNode associated with the edge if it is 
	 * present in the graph.
	 * Set the EdgeNode associated with the edge as the new head in the edge list.
	 * 
	 * @param edge  edge to be added or updated in this graph
	 * @param date  time when this edge is generated
	 * @return      true if the edge was not present in the graph; false otherwise
	 */
	private boolean setEdge(Edge edge, Date date) {
		EdgeNode edgeNode = edgeMap.get(edge.pair);
		
		//if the edge is not in the graph, add it and set it as the new head
		//in the edge list. return true
		if (edgeNode == null) {
			edgeNode = new EdgeNode(edge, date);
			edgeMap.put(edge.pair, edgeNode);
			setHead(edgeNode);
			return true;
			
		//if the edge is already in the graph, update its generation time
		//set it as the new head and return false
		} else {
			edgeNode.date = date;
			remove(edgeNode);
			setHead(edgeNode);
			return false;
		}
	}
	
	
	/**
	 * Sets the new head of the edge list.
	 * 
	 * @param edgeNode the new head of the edge list
	 */
	private void setHead(EdgeNode edgeNode) {
		if (head == null) {
			head = tail = edgeNode;
		} else {
			edgeNode.pre = null;
			edgeNode.next = head;
			head.pre = edgeNode;
			head = edgeNode;
		}
		return;
	}
	
	/**
	 * Removes an EdgeNode from the edge list
	 * 
	 * @param edgeNode the EdgeNode to be removed
	 */
	private void remove(EdgeNode edgeNode) {
		if (edgeNode.pre != null) {
			edgeNode.pre.next = edgeNode.next;
		}
		if (edgeNode.next != null) {
			edgeNode.next.pre = edgeNode.pre;
		}
		if (edgeNode == head) {
			head = edgeNode.next;
		}
		if (edgeNode == tail) {
			tail = edgeNode.pre;
		}
		edgeNode.next = edgeNode.pre = null;
	}
	
	/**
	 * Remove the duplicate elements from the array
	 * 
	 * @param s the array to be processed
	 * @return  the array without duplicate or empty string elements
	 */
	private String[] arrayDedup(String[] s) {
		if (s == null || s.length == 0) {
			return s;
		}
		Arrays.sort(s);
		int i = -1;
		for (int j = 0; j < s.length; j++) {
			if (i == -1 || !s[j].equals(s[i])) {
				s[++i] = s[j];
			}
		}
		return Arrays.copyOfRange(s, 0, i + 1);
		
	}
	

	public static void main(String[] arg) {
		HashTagGraph HTGraph = new HashTagGraph();
		Date[] sd = new Date[7];
		SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
		try {
			sd[0] = format.parse("Thu Oct 29 12:51:01 +0000 2015");
			sd[1] = format.parse("Thu Oct 29 17:51:30 +0000 2015");
			sd[2] = format.parse("Thu Oct 29 17:51:55 +0000 2015");
			sd[3] = format.parse("Thu Oct 29 17:51:56 +0000 2015");
			sd[4] = format.parse("Thu Oct 29 17:51:59 +0000 2015");
			sd[5] = format.parse("Thu Oct 29 17:52:05 +0000 2015");
			sd[6] = format.parse("Thu Oct 29 17:52:31 +0000 2015");
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String[] s0 = new String[]{"spark", "apache"};
		String[] s1 = new String[]{"apache", "hadoop", "storm"};
		String[] s2 = new String[]{"apache"};
		String[] s3 = new String[]{"spark", "flink"};
		String[] s4 = new String[]{"spark", "hbase"};
		String[] s5= new String[]{"apache", "hadoop", "apache"};
		String[] s6 = new String[]{"apache"};
		
		for (int i = 0; i < s0.length; i++) {
			System.out.print(s0[i] + " ");
		}
		HTGraph.updateGraph(s0, sd[0]);
		System.out.println(HTGraph.getAverageDegree());
		
		
		for (int i = 0; i < s1.length; i++) {
			System.out.print(s1[i] + " ");
		}
		HTGraph.updateGraph(s1, sd[1]);
		System.out.println(HTGraph.getAverageDegree());
	
		for (int i = 0; i < s2.length; i++) {
			System.out.print(s2[i] + " ");
		}
		HTGraph.updateGraph(s2, sd[2]);
		System.out.println(HTGraph.getAverageDegree());
		
		for (int i = 0; i < s3.length; i++) {
			System.out.print(s3[i] + " ");
		}
		HTGraph.updateGraph(s3, sd[3]);
		System.out.println(HTGraph.getAverageDegree());
	 
		for (int i = 0; i < s4.length; i++) {
			System.out.print(s4[i] + " ");
		}
		HTGraph.updateGraph(s4, sd[4]);
		System.out.println(HTGraph.getAverageDegree());
		 
		for (int i = 0; i < s5.length; i++) {
			System.out.print(s5[i] + " ");
		}
		HTGraph.updateGraph(s5, sd[5]);
		System.out.printf("%.2f%n", HTGraph.getAverageDegree());
	 
		for (int i = 0; i < s6.length; i++) {
			System.out.print(s6[i] + " ");
		}
		HTGraph.updateGraph(s6, sd[6]);
		System.out.printf("%.2f%n", HTGraph.getAverageDegree());
		
		System.out.println(HTGraph.vertexNumber());
		System.out.println(HTGraph.edgeNumber());

		EdgeNode cur = HTGraph.head;
		while (cur != null){
			Date test11 = cur.date;
			String edgeStr = cur.edge.pair;
			System.out.println(edgeStr + "  " + test11);
			cur = cur.next;
		}

	}

}

