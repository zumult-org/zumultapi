/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.zumal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.zumult.indexing.Indexer;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;

/**
 *
 * @author thomas.schmidt
 */
public class SpeechEventIndex2Json implements Indexer {

    String[] corpusIDs = {"FOLK", "GWSS"};
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {    
            new SpeechEventIndex2Json().index();
        } catch (IOException ex) {
            Logger.getLogger(SpeechEventIndex2Json.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void index() throws IOException {
        String XSL = Constants.SPEECHEVENTXML2JSON;
        for (String corpusID : corpusIDs){
            System.out.println("Processing " + corpusID);
            try {
                //String XML_IN = "/data/" + corpusID + "_SpeechEventIndex.xml";
                //String JSON_STRING = new IOHelper().applyInternalStylesheetToInternalFile(XSL, XML_IN);
                String XML_IN = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_PATH + corpusID + "_SpeechEventIndex.xml";
                String xml_string = IOHelper.readUTF8(new File(XML_IN));
                String JSON_STRING = new IOHelper().applyInternalStylesheetToString(XSL, xml_string);
                String JSON_OUT = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_PATH + "prototypeJson/" + corpusID + ".json";
                Files.write(new File(JSON_OUT).toPath(), JSON_STRING.getBytes("UTF-8"));        
            } catch (TransformerException ex) {
                Logger.getLogger(SpeechEventIndex2Json.class.getName()).log(Level.SEVERE, null, ex);
                throw new IOException(ex);
            }
            
        }
    }
    
}
