package logic;

/**
 * Ali Doku
 * 20163474
 * Information retrieval project
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.codecs.compressing.CompressionMode;
import org.apache.lucene.codecs.lucene62.Lucene62Codec;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */

public class IndexFiles {

   
    public IndexFiles(){
    }
    
    public static void main(String [] args)throws Exception
    {
        String IndexPath = "enter the path of index directory to store."; //Path of index.
        String docsPath = "enter the path of files to be indexed"; //file collection
        boolean create = true;
        final Path path = Paths.get(docsPath);
       if(!Files.isReadable(path))
       {
           System.out.println("This directory " + path.toAbsolutePath() + " does not exist");
           System.exit(1);
       }
       
       Date start = new Date();
       
       try
       {
           Directory directory = FSDirectory.open(Paths.get(IndexPath));
         //  Analyzer analyzer = new StandardAnalyzer(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
           //Analyzer analyzer1 = new WhitespaceAnalyzer();
           Analyzer analyzer2 = new StopAnalyzer(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
          // Analyzer analyzer3 = new SimpleAnalyzer();
           IndexWriterConfig iwc = new IndexWriterConfig(analyzer2);
              
           
           if(create)
           {
               iwc.setOpenMode(OpenMode.CREATE);
           }else
           {
               iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
           }
           
           IndexWriter writer = new IndexWriter(directory, iwc);
           writer.forceMerge(10);
          
           
           
         
           
           Indexing(writer, path);
           

      writer.close();
      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");
       }catch(IOException e)
       {
            System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
       }
        
    }
    /**
     * Indexing function walk in every file of the collection of documents to be indexed.
     * Tika library is used to extract the text from html file, extracting the title and contents,
     * and call the IndexDocument to make the indexing.
     * @param writer
     * @param path
     * @throws IOException 
     */
    static void Indexing(final IndexWriter writer, Path path) throws IOException
    {
        if (Files.isDirectory(path))
        {
             Files.walkFileTree(path, new SimpleFileVisitor<Path>() 
             {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                 try {
        	  InputStream input = Files.newInputStream(file);
              BodyContentHandler textHandler = new BodyContentHandler(-1);
              Metadata metadata = new Metadata();
              AutoDetectParser parser = new AutoDetectParser();
              ParseContext context = new ParseContext();
              parser.parse(input, textHandler, metadata, context);
            //  System.out.println("Title: " + metadata.get(metadata.TITLE));
             // System.out.println("Body: " + textHandler.toString());
              IndexDocument(writer, file, metadata, textHandler);
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          } catch (IOException e) {
              e.printStackTrace();
          } catch (SAXException e) {
              e.printStackTrace();
          } catch (TikaException e) {
              e.printStackTrace();
          }
                     return FileVisitResult.CONTINUE;
                }
            });
         } 
    }
    /**
     * IndexDocument Function creates the lucene documents, new field like title and contents, and writes it to the index.
     * @param writer
     * @param path
     * @param metadata
     * @param textHandler
     * @throws IOException 
     */
    static void IndexDocument(IndexWriter writer, Path path, Metadata metadata, BodyContentHandler textHandler) throws IOException
    {
        try(InputStream stream = Files.newInputStream(path))
        {
            Document doc = new Document();
            Field field = new StringField("path", path.toString(), Field.Store.YES);
            doc.add(field);
            doc.add(new TextField("title",metadata.get(metadata.TITLE), Field.Store.YES ));
            doc.add(new TextField("contents", textHandler.toString(),Field.Store.YES));
            
            if(writer.getConfig().getOpenMode() == OpenMode.CREATE)
            {
                System.out.println("indexing " + path);
                writer.addDocument(doc);
            }else
            {
                System.out.println("updating " + path);
                writer.updateDocument(new Term("path", path.toString()), doc);
            }
        }
    }
}









