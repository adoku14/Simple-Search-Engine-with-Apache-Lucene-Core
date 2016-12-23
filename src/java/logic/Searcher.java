package logic;

/**
 * Ali Doku
 * 20163474
 * Information retrieval project
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.core.SimpleAnalyzer;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.HighFrequencyDictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.document.CompletionQuery;
import org.apache.lucene.search.suggest.document.FuzzyCompletionQuery;
import org.apache.lucene.search.suggest.jaspell.JaspellLookup;
import org.apache.lucene.search.suggest.jaspell.JaspellTernarySearchTrie;
import org.apache.lucene.store.FSDirectory;


/** Simple command-line based search demo. */
public class Searcher {

  public Searcher() {}
  public String [] suggestion;
  public List<Lookup.LookupResult> results;
  public ArrayList<String> data;
  public Date begin;
  public Date end;
  public int totalnumberhits;
  
  /**
   * This function check and suggest words based on the query given by the user
   * 
   * @param reader
   * @param field
   * @param iwc
   * @param query
   * @return
   * @throws Exception 
   */
  public ArrayList Suggestion(IndexReader reader, String field, IndexWriterConfig iwc, String query) throws Exception
  {
        SpellChecker sp = new SpellChecker(FSDirectory.open(Paths.get("Enter the path of index files.")));
        sp.indexDictionary(new LuceneDictionary(reader, field), iwc, false);
        sp.setAccuracy(0.3f);
        String[] words = query.split(" ");
        data = new ArrayList<>();
        data.add(Integer.toString(words.length));
        if(data.size() != 0)
        for(String word : words) {
        	String [] sugg = sp.suggestSimilar(word, 1,reader,field, SuggestMode.SUGGEST_MORE_POPULAR);
        	if(sugg.length != 0)
        	for (int i = 0; i < sugg.length; i++)
        	{
        		data.add(sugg[i]);
        	}
        }
      return data;
  }

  /**
   * This function creates a suggestion based in frequency of terms..
   * @param reader
   * @param field
   * @param query
   * @return
   * @throws Exception 
   */
  public List<Lookup.LookupResult> HighFrequencyDictionary(IndexReader reader, String field, String query) throws Exception
  {
        JaspellLookup as = new JaspellLookup();
        HighFrequencyDictionary hfd = new HighFrequencyDictionary(reader, field, 0.0f);

        as.build(hfd);
         results = new ArrayList<Lookup.LookupResult>();
        String [] word = query.split(" ");
        for (String w: word)
        {
                //System.out.println(w);
                results.addAll((as.lookup(w, true, 1)));

        }
       
        return results;
  }

  /**
   * this function search according the title of the document..
   * Query Parser is used to search according the field "title"
   * @param query
   * @param Analyzer
   * @return
   * @throws Exception 
   */
  public List<Dokument> SearchTitle(String query,String Analyzer) throws Exception
  {
      String index = "enter the path of the index and the analyzer folder name." + Analyzer.trim();  //each analyzer has a separate folder which are indexed. 1-StandardAnalyzer, 2-StopAnalyzer 3-WhiteSpaceAnalyzer 4-SimpleAnalyzers
    String field =  "title";
    List<Dokument> documents = new ArrayList<Dokument>();
    int hitsPerPage = 20;
   
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    
      query = delSpaces(query);
    Analyzer analyzer = null;
         
    if(Analyzer == "StandardAnalyzer")
    {
        analyzer = new StandardAnalyzer(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    }else if(Analyzer == "StopAnalyzer")
    {
        analyzer = new StopAnalyzer(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    }else if(Analyzer == "WhiteSpaceAnalyzer")
    {
        analyzer = new WhitespaceAnalyzer();
    }else
    {
        analyzer = new SimpleAnalyzer();
    }
    
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    // Suggestion(reader, field, iwc, query);
      HighFrequencyDictionary(reader, field, query);
            
    //StandardQueryParser sqp = new StandardQueryParser(analyzer);
    QueryParser qp = new QueryParser(field,analyzer);
     
    query = query.trim();

      if (query == null || query.length() == 0) {
        System.exit(1);
      }
      
      Query q =  null;
      
          q = qp.parse(query);
      
      System.out.println("Searching for: " + q.toString());
      Suggestion(reader, field, iwc, query);
           

    documents =  Searcher( searcher, q, hitsPerPage);
    reader.close();
    return documents;
  }
  /**
   * this function search the documents based in multi field using multi field query parser.
   * calls the function Searcher to retrieve the documents and delSpaces() when the queyr is given
   * with some white spaces.
   * @param query
   * @param Analyzer
   * @return
   * @throws Exception 
   */
   public List<Dokument> Search(String query,String Analyzer) throws Exception
  {
    String index = "enter the path of the index and the analyzer folder name." + Analyzer.trim();  //each analyzer has a separate folder which are indexed. 1-StandardAnalyzer, 2-StopAnalyzer 3-WhiteSpaceAnalyzer 4-SimpleAnalyzers
    String [] fields =  {"title", "contents"};
    List<Dokument> documents = new ArrayList<Dokument>();
    int hitsPerPage = 20;
   
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    
      query = delSpaces(query);
    Analyzer analyzer = null;
         
    if(Analyzer == "StandardAnalyzer")
    {
        analyzer = new StandardAnalyzer(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    }else if(Analyzer == "StopAnalyzer")
    {
        analyzer = new StopAnalyzer(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    }else if(Analyzer == "WhiteSpaceAnalyzer")
    {
        analyzer = new WhitespaceAnalyzer();
    }else
    {
        analyzer = new SimpleAnalyzer();
    }
    
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    Suggestion(reader, "contents", iwc, query);
     //HighFrequencyDictionary(reader, "contents", query);
      MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
      //QueryParser qp = new QueryParser(field,analyzer);
     
    query = query.trim();

      if (query == null || query.length() == 0) {
        System.exit(1);
      }
      
      Query q =  parser.parse(query);
      
      System.out.println("Searching for: " + q.toString());
      

    documents =  Searcher( searcher, q, hitsPerPage);
    reader.close();
    return documents;
  }

/**
 * this function Searcher return the documents scored by the query.
 * hitsperpage is the number which specify the number of documents 
 * to be retrieven
 * @param searcher
 * @param query
 * @param hitsPerPage
 * @return
 * @throws IOException 
 */
  public List<Dokument> Searcher( IndexSearcher searcher, Query query, 
                                     int hitsPerPage) throws IOException {
        Dokument dok = null;
        List<Dokument> doklist = new ArrayList<Dokument>();
        TopDocs result = searcher.search(query, hitsPerPage);
        ScoreDoc[] results = result.scoreDocs;

        int totalhits = result.totalHits;
        totalnumberhits = totalhits;
        System.out.println(totalhits + " Matching Documents");
           int n = Math.min(hitsPerPage, totalhits);
        System.out.format("Showing results from 1 - %d of %d total matching documents collected \n", results.length, totalhits);
        if(totalhits != 0)
        {
            results = searcher.search(query, hitsPerPage).scoreDocs;

             for(int i = 0; i < n; i++)
             {
                 Document doc = searcher.doc(results[i].doc);
                 String path = doc.get("path");
                 if(path != null)
                 {
                     dok = new Dokument((i+1),results[i].doc, doc.get("title"), results[i].score, path.trim());
                    //System.out.println((i + 1) + "." + doc.get("title") + " Total Score + " + results[i].score);
                     doklist.add(dok);

                 }
             }
        }
         return doklist;
    }
  /**
   * this function is used to filter the query input.
   * @param str
   * @return 
   */
public static String delSpaces(String str){
            int space=0;
            int dummy = 0;
            StringBuilder sb=new StringBuilder();
            for( int i=0;i<str.length();i++){
            	if (dummy == 0)
            		if(str.charAt(i) == ' ')
            			continue;
                if(str.charAt(i)!=' '){
                    sb.append(str.charAt(i));  // add character
                    space=0;
                    dummy ++;
                }else{
                    space++;
                    if(space==1){       // add 1st space
                        sb.append(" ");
                    }
                }
            }
            return new String(sb.toString());
        }
/**
 * This function calculate the datedifference and return the difference in second
 * @param start
 * @param end
 * @return 
 */
public static long DateDifferece(Date start, Date end)
{
    long second = (end.getTime() - start.getTime())/1000;
    return second;
}

}