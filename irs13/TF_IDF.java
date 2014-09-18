package edu.asu.irs13;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

/***************
 * Program to search the the documents on the basis of TF-IDF similarity
 * @author vishalruhela
 *
 */

public class TF_IDF {
 private static HashMap<Integer, Double> TfIdfMap=new HashMap<Integer, Double>();
	
	public  static ArrayList<Integer> tf_idf_search(String query) throws Exception{
			System.out.println("----------TF IDF FUNCTION-------------");
			ArrayList<Integer> resultDocIDs=new ArrayList<Integer>();
			HashMap<String,Double> idf_val=new HashMap<String,Double>();		//to store idf values for terms
			HashMap<Integer, Double> tf_map=new HashMap<Integer, Double>(); // to store tf_idf values
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
							 if(tf_map.containsKey(tdocs.doc())){
								 tf_map.put(tdocs.doc(), tf_map.get(tdocs.doc())+(Tf_idf * Tf_idf));
							 }
							 else{
								 tf_map.put(tdocs.doc(), (Tf_idf * Tf_idf));
							 }
													 
							}	
				}
		
		
			//Checking time taken to calculate document magnitude
			
			
					
		//	Scanner sc = new Scanner(System.in);
			String str = query;
		//	System.out.print("query> ");
			
			//while(!(str = sc.nextLine()).equals("quit"))
			//{
			
			String[] terms = str.split("\\s+");
			HashMap<String,Integer> query_mag=new HashMap<String,Integer>(); //to store query magnitude
			HashMap<Integer,Double> dotproduct=new HashMap<Integer,Double>(); // to store dot product of document and query
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
						double idf_data=idf_val.get(word);
						if(dotproduct.containsKey(tdocs.doc())){
							idf_data=tdocs.freq()*idf_data;
							dotproduct.put(tdocs.doc(), dotproduct.get(tdocs.doc())+ idf_data);
								}	else{
							dotproduct.put(tdocs.doc(), tdocs.freq()*idf_data);
						// 	System.out.println("doc "+tdocs.doc()+" "+  tdocs.freq()*idf_data+term.text());
							}
					}
			}
			Set<String> query_keyset=query_mag.keySet();
			int sum=0;
			
			for(String j: query_keyset){
				int temp_query=query_mag.get(j);
				sum+=(temp_query * temp_query);
			}
			double query_magnitude=Math.sqrt(sum);
						
			Set<Integer> key_docProduct=dotproduct.keySet();
			HashMap<Double,Integer> tf_val=new HashMap<Double,Integer>(); // to store the tf values and corresponding document no.
		
				int counter=0;			
			for(Integer k: key_docProduct){
					double dotProduct=(double)dotproduct.get(k);
					double doc_magnitude=Math.sqrt(tf_map.get(k));
					double tf_value=dotProduct/(doc_magnitude*query_magnitude);
					tf_val.put(tf_value, k);counter++;
					TfIdfMap.put(k, tf_value);
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
			for(Map.Entry<Double, Integer> ele : tf_linkedlist) {
				//System.out.println("Doc"+ele.getValue()+": "+ele.getKey());
				i++;
				if(i>10)break;
				int doc_id=ele.getValue();
				resultDocIDs.add(doc_id);
			String d_url = r.document(doc_id).getFieldable("path").stringValue().replace("%%", "/");
				System.out.println("["+doc_id+"] "  );
				System.out.println("URL: "+ d_url );
			}
			
		//	System.out.print("query> ");
		//	}
			return resultDocIDs;

	}

	public void CalculatePageRank()
	{
		/*
		Set<Integer> key_docProduct=TfIdfMap.keySet();
		for(Integer k: key_docProduct){
			BufferedReader br = new BufferedReader(new FileReader("Rank.txt"));
			String s="";
			while ((s = br.readLine())!=null)
			{
				String[] words = s.split(" "); // split the src->dest1,dest2,dest3 string
				int src = Integer.parseInt(words[0]);
			}
			
			double dotProduct=(double)dotproduct.get(k);
			double doc_magnitude=Math.sqrt(tf_map.get(k));
			double tf_value=dotProduct/(doc_magnitude*query_magnitude);
			tf_val.put(tf_value, k);counter++;
			TfIdfMap.put(k, tf_value);
		}
		*/
	}
	
	public static void main(String[] args)  throws Exception
	{
				
		/*
				Scanner sc = new Scanner(System.in);
				String str ="";
				System.out.print("query> ");
				while(!(str = sc.nextLine()).equals("quit"))
				{		
						ArrayList<Integer> resultTF=new ArrayList<Integer>(tf_idf_search(str));
							for(Integer t : resultTF)
							{
								System.out.println(t);
							}
				}
		*/
	}
}
