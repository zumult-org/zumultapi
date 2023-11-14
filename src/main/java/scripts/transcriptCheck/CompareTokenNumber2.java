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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;
import org.zumult.io.IOHelper;

/**
 *
 * @author Frick
 * 
 * This script counts w-elements in each iso/tei- and in each fln-transcript 
 * and compare the numbers to check the consistency of the token nummer 
 * in both transcript types. The script stops when a discrepancy is found.
 * 
 */
public class CompareTokenNumber2 {
   private static final String ISO = Configuration.getTranscriptPath();
   private static final String FLN = 
           "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\transcripts";
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
     * Counts word token elements in each iso/tei and in each fln transcript
     * and compares the numbers.
     * 
     * @param corpusID corpus id
     */
    public static void doit(final String corpusID) {

        File folder = new File(ISO, corpusID);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    System.out.println("****** "+ file.getName() +" ******");
                    Document doc = IOHelper.readDocument(file);
                    NodeList elements = doc.getElementsByTagName(XML_EL);
                    int n = elements.getLength();

                    String fileName = file.getName()
                            .substring(0,29);
                    File flnFile = new File(
                            new File(FLN, corpusID), 
                            fileName + ".fln");
                    Document doc2 = IOHelper.readDocument(flnFile);    
                    NodeList elements2 = doc2.getElementsByTagName("w");
                    int m = elements2.getLength();

                    System.out.println(n + " (iso)");
                    System.out.println(m + " (fln)");
                    
                    if(n!=m){
                        
                        System.out.println ("A discrepancy was discovered in"
                                + " transcript " + file.getName());
                        
                        for (int i=0; i<elements2.getLength(); i++){                           
                            Element element2 = (Element) elements2.item(i);
                            Element element = (Element) elements.item(i);
                                            
                            if (!element2.getAttribute("id")
                                    .equals(element
                                            .getAttribute("xml:id"))){
                                
                                System.out.println(element
                                        .getAttribute("xml:id") + " (iso)");
                                System.out.println(element2
                                        .getAttribute("id") + "(fln)");
                                return;
                            }
                        }
                    }
                } catch (IOException 
                        | SAXException 
                        | ParserConfigurationException ex) {
                    Logger.getLogger(CompareTokenNumber2.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
