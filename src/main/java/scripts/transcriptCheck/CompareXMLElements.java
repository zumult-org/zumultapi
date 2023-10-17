/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package scripts.transcriptCheck;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 * 
 * This script counts w-elements in each xml-transcript and compare
 * the number with the query result searching for word-elements
 * in the same transcript
 * 
 */
public class CompareXMLElements {

    /** Corpus folder location of the iso transcripts. */
    private static final String ISO = Configuration.getTranscriptPath();

    /** XML name for the word token element*/
    private static final String XML_EL = "w";

    /**
    * Main method.
    * Calls doit() to compare the word token number.
    *
    * @param args The command line arguments
    */
    public static void main(String[] args) {
       doit("FOLK");
    }

    /**
     * Counts word token elements in each xml transcript and compares
     * the number with the search results for
     * &lt;word&gt; in the same transcript.
     * 
     * @param corpusID corpus ID
     */
    public static void doit(final String corpusID) {
       BackendInterface backendInterface; 
       try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();

            File folder = new File(ISO, corpusID);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    
                    System.out.println("******* "+ file.getName() +" ********");
                    Document doc = IOHelper.readDocument(file);
                    NodeList elements = doc.getElementsByTagName(XML_EL);
                    int n = elements.getLength();

                    String query = "<word/> within <"
                            + Constants.METADATA_KEY_TRANSCRIPT_DGD_ID 
                            +"=\""+file.getName().substring(0,23)
                            +"\"/>";                
                    SearchResultPlus searchResult = backendInterface
                        .search(query, 
                                null,
                                null,
                                "corpusSigle=\"FOLK\"", 
                                null, 
                                0,
                                0, 
                                null, 
                                "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", 
                                null, 
                                null);
                    int m = searchResult.getTotalHits();

                    System.out.println(n + " (trascript document)");
                    System.out.println(m + " (search index)");
                    
                    if(n!=m){
                        System.out.println ("A discrepancy was discovered in"
                                + " transcript" + file.getName());
                        return;
                    }
                }
            }
       } catch (ClassNotFoundException 
               | InstantiationException 
               | IllegalAccessException 
               | IOException 
               | SAXException 
               | ParserConfigurationException 
               | SearchServiceException ex) {
           Logger.getLogger(CompareXMLElements.class.getName())
                   .log(Level.SEVERE, null, ex);
       }
   } 
}
