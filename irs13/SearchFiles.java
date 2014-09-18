package edu.asu.irs13;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;




import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;

import java.io.File;
import java.util.Scanner;

public class SearchFiles {
	private ArrayList<Integer> tf_idf_docs=new ArrayList<Integer>();// to return the top documents
	private HashMap<String,Double> idf_val=new HashMap<String,Double>();		//to store idf values for terms
	private HashMap<Integer, Double> tf_map=new HashMap<Integer, Double>(); // to store tf_idf values
	private HashMap<Integer,Double> finalTF_IDF=new HashMap<Integer, Double>();
	private HashMap<Integer,String> docURLMAP=new HashMap<Integer,String>();
	 /****** Part of Project 01******************
	public static void main(String[] args) throws Exception
	{
		// the IndexReader object is the main handle that will give you 
		// all the documents, terms and inverted index
	//	IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
	//	tf_idf_search(r);
	 
		System.out.println("Press 1 for TF method");
		System.out.println("Press	2 for TF-IDF method");
		System.out.println("Press q  to quit");
		Scanner sc1 = new Scanner(System.in);
		String str1 = "";
		while(!(str1 = sc1.nextLine()).equals("q")){
				
					
			switch(str1){
					case "1":System.out.println("Please wait");
								tf_search(r);
								System.out.println("Press 1 for TF method");
								System.out.println("Press	2 for TF-IDF method");
								System.out.println("Press q  to quit");
									break;
					case "2": System.out.println("Please wait");
									tf_idf_search(r); 
									System.out.println("Press 1 for TF method");
									System.out.println("Press	2 for TF-IDF method");
									System.out.println("Press q  to quit");
									break;	
						
								}//end of switch 
	      	}//end of while loop
		
		
		// You can figure out the number of documents using the maxDoc() function
		System.out.println("The number of documents in this index is: " + r.maxDoc());
	//System.out.print(r.docFreq);
		
		int i = 0;
		// You can find out all the terms that have been indexed using the terms() function
		TermEnum t = r.terms();
		while(t.next())
		{
			// Since there are so many terms, let us try printing only term #100000-#100010
			if (i > 100000) System.out.println("["+i+"] " + t.term().text());
			if(i>100000){
				System.out.println("Doc Frequency is: "+ t.docFreq()+ " for term "+ t.term().text());
				}
			if (++i > 100010) break;
		}
				
		// You can create your own query terms by calling the Term constructor, with the field 'contents'
		 //In the following example, the query term is 'brute'
		Term te = new Term("contents", "brute");
		
		
		
	//	 You can also quickly find out the number of documents that have term t
		System.out.println("Number of documents with the word 'brute' is: " + r.docFreq(te));
		
		// You can use the inverted index to find out all the documents that contain the term 'brute'
		//  by using the termDocs function
		TermDocs td = r.termDocs(te);
		while(td.next())
		{
			
				System.out.println("Document number ["+td.doc()+"] contains the term 'brute' " + td.freq() + " time(s).");
		}
		
		// You can find the URL of the a specific document number using the document() function
		// For example, the URL for document number 14191 is:
		Document d = r.document(19000);
		String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
		System.out.println(url.replace("%%", "/"));
		
		// -------- Now let us use all of the functions above to make something useful --------
		// The following bit of code is a worked out example of how to get a bunch of documents
		// in response to a query and show them (without ranking them according to TF/IDF)
		Scanner sc = new Scanner(System.in);
		String str = "";
		System.out.print("query> ");
		while(!(str = sc.nextLine()).equals("quit"))
		{
			String[] terms = str.split("\\s+");
			for(String word : terms)
			{
				Term term = new Term("contents", word);
				TermDocs tdocs = r.termDocs(term);
				while(tdocs.next())
				{
					String d_url = r.document(tdocs.doc()).getFieldable("path").stringValue().replace("%%", "/");
					System.out.println("["+tdocs.doc()+"] " + d_url);
				}
			}
			System.out.print("query> ");
		}
		
		
	}
	*********************************************************/
	public static void tf_search(IndexReader r)  throws Exception {
								
				HashMap<Integer,Integer> doc_mag1=new HashMap<Integer,Integer>(30000); //to store term occurences in documents
				HashMap<Integer,Double> doc_mag=new HashMap<Integer,Double>(30000); //to store Square root of docs
				TermEnum t = r.terms();
				Date Checkpoint1 = new Date();
				
				//Calculation of Magnitude of documents
				while(t.next())
				{
					Term term=new Term("contents",t.term().text());
					TermDocs tdocs = r.termDocs(term);
						while(tdocs.next())
						{ 
							int docs_freq= tdocs.freq()*tdocs.freq();
							if(doc_mag1.containsKey(tdocs.doc())){
								int  temp=doc_mag1.get(tdocs.doc());
									temp+= docs_freq;
								doc_mag1.put(tdocs.doc(), temp);
							}
							else{
									doc_mag1.put(tdocs.doc(), docs_freq);
									}
					    	}	
				}
				Set<Integer> doc_keyset=doc_mag1.keySet();
				for(Integer i:doc_keyset){
					double k=Math.sqrt(doc_mag1.get(i));
					doc_mag.put(i, k);
				}
								
				//Checking time taken to calculate document magnitude
				Date Checkpoint2 = new Date();
				System.out.println("Pre processing time "+(Checkpoint2.getTime() - Checkpoint1.getTime())+ " Milliseconds");
				
				Scanner sc = new Scanner(System.in);
				String str = "";
				System.out.print("query> ");
				
				while(!(str = sc.nextLine()).equals("quit"))
				{
					
					String[] terms = str.split("\\s+");
					HashMap<String,Integer> query_mag=new HashMap<String,Integer>(30000); //to store query magnitude
					HashMap<Integer,Integer> dotproduct=new HashMap<Integer,Integer>(30000); // to store dot product of document and query
					Date Checkpoint3=new Date();
					
					for(String word : terms)
						{
							if(query_mag.containsKey(word)){
								query_mag.put(word, query_mag.get(word)+1);
							}else{
								query_mag.put(word, 1);
							 }
							
							Term term = new Term("contents", word);
							TermDocs tdocs = r.termDocs(term);
								while(tdocs.next())
								{
									if(dotproduct.containsKey(tdocs.doc())){
										int dot_temp=dotproduct.get(tdocs.doc())+ tdocs.freq();
										dotproduct.put(tdocs.doc(), dot_temp);
									}	else{
										dotproduct.put(tdocs.doc(), tdocs.freq());
										}
								}
						}
					Set<String> query_keyset=query_mag.keySet(); // keyset of query 
					int sum=0;
					for(String j: query_keyset){
						int query_sq=query_mag.get(j);
						sum+=query_sq*query_sq;
						}
					double query_magnitude=Math.sqrt(sum);
									
					Set<Integer> key_docProduct=dotproduct.keySet();
					HashMap<Double,Integer> tf_val=new HashMap<Double,Integer>(30000); // to store the tf values and corresponding document no.
														
					for(Integer k: key_docProduct){
							double dotProduct=(double)dotproduct.get(k);
						//	double doc_magnitude=Math.sqrt(doc_mag.get(k));
							double doc_magnitude=doc_mag.get(k);
							double tf_value=dotProduct/(doc_magnitude*query_magnitude);
							tf_val.put(tf_value, k);
					 }
					Date Checkpoint4 = new Date();
					System.out.println("Time taken to generate tf values "+(Checkpoint4.getTime() - Checkpoint3.getTime())+ " Milliseconds");
				
					LinkedList<Map.Entry<Double,Integer>> tf_linkedlist = new LinkedList<Map.Entry<Double,Integer>>(tf_val.entrySet());
					Collections.sort(tf_linkedlist, new Comparator<Map.Entry<Double,Integer>>() {
						@Override
						public int compare(Entry<Double, Integer> o1,
								Entry<Double, Integer> o2) {

							return -(o1.getKey().compareTo(o2.getKey()));
						}

						});
					Date Checkpoint5 = new Date();
					System.out.println("Time taken for sorting "+(Checkpoint5.getTime() - Checkpoint4.getTime())+ " Milliseconds");
					int i=0;
					for(Map.Entry<Double, Integer> ele : tf_linkedlist) {
						if(i++>9)break;
						int doc_id=ele.getValue();
						//String d_url = r.document(doc_id).getFieldable("path").stringValue().replace("%%", "/");
						
						System.out.println("["+doc_id+"] " );
					}
								
					System.out.print("query> ");
				}
				
	}

	public  void tf_idf_search(String str) throws Exception{
			
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			
		//Calculation of Magnitude of documents
		geneateMagnitudeOfDoc();
		 this.tf_idf_docs.clear();
		 this.finalTF_IDF.clear();
		String[] terms = str.split("\\s+");
		HashMap<String,Integer> query_mag=new HashMap<String,Integer>(); //to store query magnitude
		HashMap<Integer,Double> dotproduct=new HashMap<Integer,Double>(); // to store dot product of document and query
		for(String word : terms)
			{
				if(query_mag.containsKey(word))
					{
						query_mag.put(word, query_mag.get(word)+1);
					}
				else
					{
						query_mag.put(word, 1);
					 }
				
				Term term = new Term("contents", word);
				TermDocs tdocs = r.termDocs(term);
				while(tdocs.next())
					{
						double idf_data=idf_val.get(word);
						if(dotproduct.containsKey(tdocs.doc()))
							{
								idf_data=tdocs.freq()*idf_data;
								dotproduct.put(tdocs.doc(), dotproduct.get(tdocs.doc())+ idf_data);
							}
						else
							{
								dotproduct.put(tdocs.doc(), tdocs.freq()*idf_data);
							}
						}//end of inner while
	     	}//end of external while
				
		Set<String> query_keyset=query_mag.keySet();
		int sum=0;
		for(String j: query_keyset)
			{
				int temp_query=query_mag.get(j);
				sum+=(temp_query * temp_query);
			}
		double query_magnitude=Math.sqrt(sum);
							
		Set<Integer> key_docProduct=dotproduct.keySet();
		HashMap<Double,Integer> tf_val=new HashMap<Double,Integer>(); // to store the tf values and corresponding document no.
			
		for(Integer k: key_docProduct) //Calculating the Cosine similarity-Distance Measure
			{
					double dotProduct=(double)dotproduct.get(k);
					double doc_magnitude=Math.sqrt(tf_map.get(k));
					double tf_value=dotProduct/(doc_magnitude*query_magnitude);
					tf_val.put(tf_value, k);
					this.finalTF_IDF.put(k, tf_value);
			}
				
		LinkedList<Map.Entry<Double,Integer>> tf_linkedlist = new LinkedList<Map.Entry<Double,Integer>>(tf_val.entrySet());
			Collections.sort(tf_linkedlist, new Comparator<Map.Entry<Double,Integer>>() {
				@Override
				public int compare(Entry<Double, Integer> o1,
						Entry<Double, Integer> o2) {
					return -(o1.getKey().compareTo(o2.getKey()));
				}
				});
				
			int i=0;
			for(Map.Entry<Double, Integer> ele : tf_linkedlist)
				{
					if(++i>10)break;
					int doc_id=ele.getValue();
					String d_url = r.document(doc_id).getFieldable("path").stringValue().replace("%%", "/");
				//	System.out.println(doc_id + " "+ d_url);
					docURLMAP.put(doc_id, d_url);
					this.tf_idf_docs.add(doc_id);
				}
	
			
	}
	
	public HashMap<Integer,String> getDocURLmap()
	{
		return docURLMAP;
	}
	
	public ArrayList<Integer> getTopDocOfTFIDF()
	{
		return tf_idf_docs;
	}
	
	public HashMap<Integer,Double> getTfIDF()
	{
		return finalTF_IDF;
	}

	public HashMap<String,Double> getIdfMap()
	{
		return idf_val;
	}
	public HashMap<Integer	,Double> getTermFreqMap()
	{
		return tf_map;
	}
	public void geneateMagnitudeOfDoc() throws Exception
	{
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		TermEnum t = r.terms();
		
				//Calculation of Magnitude of documents
		while(t.next())
			{		
		    		double idf_data=Math.log((new Double(r.maxDoc())/new Double(t.docFreq())));
		    		idf_val.put(t.term().text(), idf_data); // Storing IDF value for respective term
		    								
					Term term=new Term("contents",t.term().text());				
					TermDocs tdocs = r.termDocs(term);
						while(tdocs.next())
						{ 
						 double tf=tdocs.freq();
						 double Tf_idf=tf*idf_data;
						 if(tf_map.containsKey(tdocs.doc()))
							 {
								 tf_map.put(tdocs.doc(), tf_map.get(tdocs.doc())+(Tf_idf * Tf_idf));
							 }
						 else
							 {
								 tf_map.put(tdocs.doc(), (Tf_idf * Tf_idf));
							 }
												 
						}	
			}
			
	}
	
	/******************************
	 * Test Function to check function in class
	 * @param args
	 * @throws Exception
	 *******************/
	/*
	public static void main(String[] args) throws Exception
	{
		SearchFiles temp1=new SearchFiles();
		temp1.geneateMagnitudeOfDoc();
		Scanner sc = new Scanner(System.in);
		String str ="";
		System.out.print("query> ");
		
		while(!(str = sc.nextLine()).equals("quit"))
		{	
			Date Checkpoint1=new Date();
			temp1.tf_idf_search(str);
			ArrayList<Integer> results=new ArrayList<Integer>(temp1.getTopDocOfTFIDF());
			Date Checkpoint2=new Date();
			System.out.println("Time Taken is 1st:" + (Checkpoint2.getTime()-Checkpoint1.getTime()) + " Milliseconds");
			for (Integer doc : results)
			{
				System.out.println("DOC is :" + doc);
			}
			Date Checkpoint3=new Date();
			System.out.println("Time Taken is :" + (Checkpoint3.getTime()-Checkpoint2.getTime()) + " Milliseconds");
			HashMap<Integer,Double> results2=new HashMap<Integer, Double>(temp1.getTfIDF());
			Set<Integer> keyResults2=results2.keySet();
			for (Integer doc : keyResults2)
			{
				System.out.println("DOC is : " + doc + " with values " + results2.get(doc));
			}
			Date Checkpoint4=new Date();
			System.out.println("Time Taken is :" + (Checkpoint4.getTime()-Checkpoint3.getTime()) + " Milliseconds");
			
		}
		
	}*/
	
}
