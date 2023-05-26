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
import org.zumult.io.IOHelper;

/**
 *
 * @author Frick
 */
public class CountXMLElements {
   // private static final String DIR_IN = "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\iso-transcripts\\DH";
    private static final String DIR_IN = "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\transcripts\\DH";
    private static final String XML_EL = "w";

  public static void main(String[] args) {
    File folder = new File(DIR_IN);
    File[] listOfFiles = folder.listFiles();
    int count=0;
            
    for (File file : listOfFiles) {
        if (file.isFile()) {
            try {
                System.out.println("************** "+ file.getName() +" ************************");
                Document doc = IOHelper.readDocument(file);
                NodeList elements = doc.getElementsByTagName(XML_EL);
                int n = elements.getLength();
                count = count + n;
                System.out.println(n);
           } catch (IOException | SAXException | ParserConfigurationException ex) {
                Logger.getLogger(CountXMLElements.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    System.out.println("Total: " + count);
  }
}
