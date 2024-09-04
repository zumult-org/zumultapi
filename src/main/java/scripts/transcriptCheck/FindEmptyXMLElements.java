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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;
import org.zumult.io.IOHelper;

/**
 *
 * @author Frick
 * 
 * This script is used for finding empty word token elements in iso transcripts.
 * 
 * Empty word token elements that still have lemma and pos annotations cause
 * inconsistencies in the search index because they are not indexed
 * while their lemma and pos form are indexed and can be searched.
 * 
 * To use this script, please specify the corpus folder name.
 * 
 */
public final class FindEmptyXMLElements {
    /** Corpus folder name. */
    private static final String CORPUS = "FOLK";
    
    /** Corpus folder location of the iso transcripts. */
    private static final String DATA_PATH = Configuration.getTranscriptPath();

    private FindEmptyXMLElements() {
      //not called
    }

     /**
     * Main method.
     * Calls doit() to find and print empty xml elements
     *
     * @param args The command line arguments
     */
    public static void main(final String[] args) {
        doit("w", CORPUS);
    }

    /**
    * Finds empty xml elements and print their IDs.
    *
    * @param str wanted XML element
    * @param corpusID corpus ID
    */
    private static void doit(final String str, final String corpusID) {

        File folder = new File(DATA_PATH, corpusID);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    Document doc = IOHelper.readDocument(file);
                    NodeList elements = doc.getElementsByTagName(str);
                    for (int i = 0; i < elements.getLength(); i++) {
                        Element element = (Element) elements.item(i);
                        if (element.getTextContent() == null
                            || element.getTextContent().isEmpty()) {
                            printResult(file, element);
                        }
                    }
                } catch (IOException
                        | SAXException
                        | ParserConfigurationException ex) {
                    Logger.getLogger(FindEmptyXMLElements.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
    * Prints the file name and the IDs of empty xml elements.
    *
    * @param file file with empty xml elements
    * @param element empty element
    */
    private static void printResult(final File file, final Element element) {
        System.out.println(file.getName()
                           + ": "
                           + element.getAttribute("xml:id"));
    }
}