/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package scripts;

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
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 * 
 * This script counts w-elements in each xml-transcript and compare the number with the query result
 * searching for word-elements in the same transcript
 * 
 */
public class CompareXMLElements {
   private static final String DIR_IN = "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\iso-transcripts\\FOLK";
   private static final String XML_EL = "w";

   public static void main(String[] args) {
    BackendInterface backendInterface; 
       try {
           backendInterface = BackendInterfaceFactory.newBackendInterface();

    File folder = new File(DIR_IN);
    File[] listOfFiles = folder.listFiles();
       
    for (File file : listOfFiles) {
        if (file.isFile()) {
            try {
                System.out.println("************** "+ file.getName() +" ************************");
                Document doc = IOHelper.readDocument(file);
                NodeList elements = doc.getElementsByTagName(XML_EL);
                int n = elements.getLength();
                
                String query = "<word/> within <"+ Constants.METADATA_KEY_TRANSCRIPT_DGD_ID +"=\""+file.getName().substring(0,23)+"\"/>";                
                System.out.println(query);
                SearchResultPlus searchResult = backendInterface.search(query, null,null, "corpusSigle=\"FOLK\"", null, 0,0, null, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", null, null);
                int m = searchResult.getTotalHits();
                
                System.out.println("n: " + n);
                System.out.println("m: " + m);
                if(n!=m){
                    System.out.println ("HERE!!!");
                    return;
                }

           } catch (IOException | SAXException | ParserConfigurationException | SearchServiceException ex) {
                Logger.getLogger(CompareXMLElements.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
           } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
           Logger.getLogger(CompareXMLElements.class.getName()).log(Level.SEVERE, null, ex);
       }
   }
    
}
