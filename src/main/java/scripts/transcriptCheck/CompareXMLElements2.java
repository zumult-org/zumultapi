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
import org.zumult.io.IOHelper;

/**
 *
 * @author Frick
 * 
 * This script counts w-elements in each iso/tei- and in each fln-transcript and compare the numbers
 * to check the consistency of the token nummer in both transcript types.
 * 
 */
public class CompareXMLElements2 {
   private static final String DIR_IN = "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\iso-transcripts\\FOLK";
   private static final String DIR_IN2 = "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\transcripts\\FOLK";
   private static final String XML_EL = "w";

   public static void main(String[] args) {

    File folder = new File(DIR_IN);
    File[] listOfFiles = folder.listFiles();

       
        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    System.out.println("************** "+ file.getName() +" ************************");
                    Document doc = IOHelper.readDocument(file);
                    NodeList elements = doc.getElementsByTagName(XML_EL);
                    int n = elements.getLength();

                    Document doc2 = IOHelper.readDocument(new File(DIR_IN2 + "\\"+ file.getName().substring(0,29) + ".fln"));
                    NodeList elements2 = doc2.getElementsByTagName("w");
                    int m = elements2.getLength();

                    System.out.println("n: " + n);
                    System.out.println("m: " + m);
                    
                    if(n!=m){
                        for (int i=0; i<elements2.getLength(); i++){                           
                            Element element2 = (Element) elements2.item(i);
                            Element element = (Element) elements.item(i);
                            System.out.println(element2.getAttribute("id"));
                            System.out.println(element.getAttribute("xml:id"));
                            
                    
                            if (!element2.getAttribute("id").equals(element.getAttribute("xml:id"))){
                                System.out.println(element2.getAttribute("xml:id"));
                                return;
                            }
                            System.out.println(element.getAttribute("********************"));
                        }
                    }

                } catch (IOException | SAXException | ParserConfigurationException ex) {
                    Logger.getLogger(CompareXMLElements2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
