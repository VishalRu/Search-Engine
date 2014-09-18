package edu.asu.irs13;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.safety.Whitelist;
/**************
 * Program to generate snippets of URL's
 * @author vishalruhela
 *
 */
public class snipetGeneration {
	private ArrayList<Integer> TFdocs;
	private static String loc="/Users/vishalruhela/Desktop/Downloads_New/Projectclass/result3/";
	BufferedReader br;
	String streamOfFile;
	boolean titleFlag=false;
	private HashMap<String, Integer> queryWordFreqMap;
	private HashMap<String,Integer> lineFreqMaping;
	private HashMap<Integer, String> docLineMaping; 
	private HashMap<Integer, String> docURLmap; 
	private HashMap<Integer, String> docTitleMap;
	private SearchFiles TFobj;
	public HashMap<Integer, String> getDocTitleMap() {
		return docTitleMap;
	}


	/*******************
	 * Constructor to set up variables
	 * @throws Exception
	 */
	public snipetGeneration() throws Exception {
		lineFreqMaping=new HashMap<String,Integer>();
		queryWordFreqMap=new HashMap<String,Integer>();
		docLineMaping=new HashMap<Integer,String>();
		docTitleMap=new HashMap<Integer,String>();
		docURLmap=new HashMap<Integer,String>();
		TFdocs=new ArrayList<Integer>();
		 TFobj=new SearchFiles();
		TFobj.geneateMagnitudeOfDoc();
		}
	
/**************************
 * Function to read the lines of files 
 * @param path
 * @param doc
 * @throws IOException
 */
	public void  readLinesOfFile(String path, int doc) throws IOException
	{
		 br = new BufferedReader(new FileReader(loc+path));
		 StringBuilder strb=new StringBuilder();
		 StringBuilder wholeSource=new StringBuilder();
		while ((streamOfFile = br.readLine()) != null) 
		{
			if(titleFlag==false)
				checkTitle(streamOfFile,doc);
			wholeSource.append(streamOfFile);
			
			strb.append(Jsoup.parse(streamOfFile).text().toLowerCase());
				strb.append("\n");
		}
		if(titleFlag==false)
			setHead(wholeSource.toString(), doc);
		
		String comString=strb.toString();
		CheckOccurenceInQuery(comString);
		br.close();
	}

	/***************
	 * Function to fetch the title if it presents in a line
	 * @param line
	 * @param doc
	 */
	public void checkTitle( String line,int doc)
	{
		final Pattern pattern = Pattern.compile("<title>(.+?)</title>");
		final Matcher matcher = pattern.matcher(line);
		matcher.find();
		
		final Pattern partialPattern = Pattern.compile("<title>(.+?)");
		final Matcher partialMatcher = partialPattern.matcher(line);
		partialMatcher.find();
		if(matcher.matches())
		{
		//System.out.println(matcher.group(1));
			this.docTitleMap.put(doc, matcher.group(1));
			titleFlag=true;
		}
		else if(partialMatcher.matches())
		{
			//System.out.println("HI this  is a partial match");
			//System.out.println(partialMatcher.group(1));
			this.docTitleMap.put(doc, partialMatcher.group(1));
			titleFlag=true;
		}
		
	}

	
	/***************
	 * Function to fetch the data inside the header tag of a file in case program unables to read the title
	 * @param line
	 * @param doc
	 */
	public void setHead(String line, int doc)
	{
		final Pattern headPattern = Pattern.compile("<head>(.+?)<head>");
		final Matcher headMatcher = headPattern.matcher(line);
		headMatcher.find();
		if(headMatcher.matches())
		{
			System.out.println("HEAD FUNCTION FOR doc "+ doc);
			System.out.println(headMatcher.group(1));
			this.docTitleMap.put(doc, headMatcher.group(1));
			}
		
	}
	
	/******************
	 * Function to check the if query words present in a line
	 * @param arr
	 */
	public void CheckOccurenceInQuery(String arr)
 	{
 		String[] lineArray=arr.split("\n");
		for(int i=0; i<lineArray.length;i++)
		{				
			String[] str =lineArray[i].split(" ");
			for(int j=0;j<str.length;j++)
			{
				if(queryWordFreqMap.containsKey(str[j]))
				{
					raiseScoreForLine(lineArray[i],queryWordFreqMap.get(str[j]));
				}
			}
		}
	}
	
	/*****************
	 * Function to raise the score of line if query words are present in a line
	 * @param line
	 * @param val
	 */
	public void raiseScoreForLine(String line, int val)
	{
		if(lineFreqMaping.containsKey(line)==true)
		{
			lineFreqMaping.put(line, lineFreqMaping.get(line)+val);
			}
		else
		{
			lineFreqMaping.put(line, val);
		}
	}
	
	/*************
	 * Function to get the top line of a document
	 * @return
	 */
	public String getTopLine()
	{
		int tempScore=0;
		int tempScore1=0;
		String resLine="";
		String seconResLine="";
		for(Entry<String,Integer> l: lineFreqMaping.entrySet())
		{
			 int score=l.getValue();
			 if(score>tempScore){
				 tempScore1=tempScore;
				tempScore=score;
				 resLine=l.getKey();
			 }
			 else if(score>tempScore1)
			 {
				 seconResLine=l.getKey();
				 tempScore1=score;
			 }
				 			 
		}
		if(resLine.length()<40)
				resLine=resLine + "\n" +seconResLine;
		else if (resLine.length()>150)
			resLine=resLine.substring(0, 188);
		return resLine;	
	}
	
	
	/**************
	 * Function to get the top k TFIDF documents
	 * @param query
	 * @throws Exception
	 */
	public void getTFdocs(String query) throws Exception
	{
		//System.out.println("IN TF IDF FUNCTION ");
	//	SearchFiles TFobj=new SearchFiles();
	//	TFobj.geneateMagnitudeOfDoc();
		this.TFobj.tf_idf_search(query);
		this.TFdocs=TFobj.getTopDocOfTFIDF();
		this.docURLmap=TFobj.getDocURLmap();
		
		
		//System.out.println("OUT OF TF IDF FUNCTION ");
	}


	private void setQueryWordFreq(String query) {
		String[] W=query.split(" ");
		for(int i=0; i<W.length;i++)
		{
			//System.out.println("One query word "+ W[i]);
			//System.out.println("Length "+ W.length);
			if(queryWordFreqMap.containsKey(W[i])==true)
				queryWordFreqMap.put(W[i], queryWordFreqMap.get(W[i])+1   );
			else
				queryWordFreqMap.put(W[i],1);
		}
	}
	
	public ArrayList<Integer> getTFdocs() {
		return TFdocs;
	}

	/******************
	 * Handler to control the flow the program
	 * @throws IOException
	 */
	
	public void SnipetHandler() throws IOException
	{
		for(int docId=0;docId<this.TFdocs.size();docId++)
		{
			int doc=TFdocs.get(docId);
			String url=docURLmap.get(doc).replace("/", "%%");
			
			readLinesOfFile(url, doc); //extract the URL's path from "Heaven"
			String topLine=getTopLine();
			this.docLineMaping.put(doc, topLine);			
			clearVar();
		}
		
		for(int doc=0;doc<this.TFdocs.size();doc++)
		{
			int DocID=TFdocs.get(doc);
		
			System.out.println("For Document ID "+			DocID  +" URL is: "+ this.docURLmap.get(DocID))  ;
			System.out.println("Title is "+ this.docTitleMap.get(DocID));
			System.out.println(WordUtils.wrap(docLineMaping.get(DocID),80));
			System.out.println("#############################################################");
			}
	//	docLineMaping.clear();
		
		
	}
	
	public HashMap<Integer, String> getDocLineMaping() {
		return docLineMaping;
	}

	public void setDocLineMaping(HashMap<Integer, String> docLineMaping) {
		this.docLineMaping = docLineMaping;
	}

	public void clearVar()
	{
		lineFreqMaping.clear();
		titleFlag=false;
	}
	
	public void MasterClearVar()
	{
		lineFreqMaping.clear();
		titleFlag=false;
		queryWordFreqMap.clear();
		
		 docLineMaping.clear();
		 docURLmap.clear(); 
		 docTitleMap.clear();
	}
	/**********
	 * Function for other classes to fetch the URL's
	 * @param query
	 * @throws Exception
	 */
	public void getSnipet(String query) throws Exception
	{
		getTFdocs(query);
		setQueryWordFreq(query);
		SnipetHandler();
	}
		
	/**************
	 * Driver program
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		String str ="";
		System.out.print("query> ");
		snipetGeneration tempObj =new snipetGeneration();
		
		while(!(str = sc.nextLine()).equals("quit"))
		{	
			Date Checkpoint1=new Date();
			tempObj.getTFdocs(str);
			tempObj.setQueryWordFreq(str);
			Date Checkpoint2=new Date();
			tempObj.SnipetHandler();
			Date Checkpoint3=new Date();
			System.out.println(Checkpoint2.getTime()-Checkpoint1.getTime());
			System.out.println(Checkpoint3.getTime()-Checkpoint1.getTime());
			System.out.println(Checkpoint3.getTime()-Checkpoint2.getTime());
			tempObj.MasterClearVar();
		}
		
	}

}
