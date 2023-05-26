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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;

/**
 *
 * @author Frick
 */
public class FindEmptyXMLElements {
  private static final String DIR_IN = "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\iso-transcripts\\FOLK";
  private static final String XML_EL = "w";
  
  public static void main(String[] args) {

    File folder = new File(DIR_IN);
    File[] listOfFiles = folder.listFiles();
            
    for (File file : listOfFiles) {
        if (file.isFile()) {
            try {
                
                Document doc = IOHelper.readDocument(file);
                NodeList elements = doc.getElementsByTagName(XML_EL);
                for (int i=0; i<elements.getLength(); i++){
                    Element element = (Element) elements.item(i);
                    if (element.getTextContent()==null || element.getTextContent().isEmpty()){
                        System.out.println("************** "+ file.getName() +" ************************");
                        System.out.println(element.getAttribute("xml:id"));
                    }
                }
           } catch (IOException | SAXException | ParserConfigurationException ex) {
                Logger.getLogger(FindEmptyXMLElements.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
    }
  }
}
