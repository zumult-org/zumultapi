/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.zumult.backend.Configuration;
import org.zumult.io.IOHelper;

/**
 *
 * @author Frick
 * 
 * This script is used to count xml elements in xml transcripts. Please specify
 * the desired xml element and the corpud folder name. 
 * 
 * To switch between iso- and .fln-transcripts, please adapt the location path.
 * 
 */
public final class CountXMLElements {
    /** XML element to be count. */
    private static final String XML_ELEMENT = "w";
    
    /** Corpus folder name. */
    private static final String CORPUS = "DH";
    
    /** Corpus folder location of transcripts. */
    private static final String PATH = Configuration.getTranscriptPath();
    /* private static final String TRANSCRIPT_PATH = 
    "C:\\Users\\Frick\\IDS\\GitLab\\dgd-data\\transcripts" ;*/

    private CountXMLElements() {
      //not called
    }

    /**
    * Main method.
    * Calls doit() to count xml elements
    *
    * @param args The command line arguments
    */
    public static void main(final String[] args) {
        doit(XML_ELEMENT, CORPUS, PATH);
    }

    /**
    * Counts specified xml elements.
    *
    * @param str wanted XML element
    * @param corpusID corpus ID
    * @param transcriptPath path to the transcript folder
    */
    private static void doit(final String str,
                             final String corpusID,
                             final String transcriptPath) {

        File folder = new File(transcriptPath, corpusID);
        System.out.println(folder.getAbsoluteFile());
        File[] listOfFiles = folder.listFiles();
        int count = 0;

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    System.out.println("***** " + file.getName() + " *****");
                    Document doc = IOHelper.readDocument(file);
                    NodeList elements = doc.getElementsByTagName(str);
                    int n = elements.getLength();
                    count = count + n;
                    System.out.println(n);
               } catch (IOException
                       | SAXException
                       | ParserConfigurationException ex) {
                    Logger.getLogger(CountXMLElements.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }

        System.out.println("Total: " + count);
    }
}
