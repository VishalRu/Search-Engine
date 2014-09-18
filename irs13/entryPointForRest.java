package edu.asu.irs13;

import java.io.File;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

@Path("/search")
public class entryPointForRest {
	
	@GET @Path("query")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces("plain/text")
	public ArrayList<String> getTfIDFdocs(String query) throws Exception
	{
		System.out.println("af");
		ArrayList<String> resultsDocs=new ArrayList<String>();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		SearchFiles searchObj=new SearchFiles();
		searchObj.geneateMagnitudeOfDoc();
		searchObj.tf_idf_search(query);
		ArrayList<Integer> TFdocs=new ArrayList<Integer>(searchObj.getTopDocOfTFIDF());
		for(int doc=0;doc<TFdocs.size();doc++)
		{
			resultsDocs.add(r.document(doc).getFieldable("path").stringValue().replace("%%", "/"));
		}
		return resultsDocs;
		
	}

}
