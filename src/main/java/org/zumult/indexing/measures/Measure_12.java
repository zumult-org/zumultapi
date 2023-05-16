/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.measures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zumult.indexing.Indexer;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;

/**
 *
 * @author thomas.schmidt
 */
public class Measure_12 implements Indexer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new Measure_12().index();
    }


    XPath xPath = XPathFactory.newInstance().newXPath();
    String[] corpusIDs = {"FOLK", "GWSS"};
    String OUTPUT_PATH = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_MEASURES_PATH;
    

    @Override
    public void index() throws IOException {
        for (String corpusID : corpusIDs){
            StringBuilder xml = new StringBuilder();
            xml.append("<measures-document>");        
            try {

                xPath.setNamespaceContext(new ISOTEINamespaceContext());             
                BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();

                // get all events from FOLK
                IDList folkEventIDs = backendInterface.getEvents4Corpus(corpusID);
                // iterate through them
                for (String eventID : folkEventIDs){
                    // get the metadata for this event
                    Event event = backendInterface.getEvent(eventID);
                    // get all subordinate speech event IDs
                    IDList speechEventIDs = event.getSpeechEvents();
                    // iterate through them
                    for (String speechEventID : speechEventIDs){

                        IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                        // make a new lemma list for this speech event
                        int countSyllables = 0;
                        double sumTime = 0;
                        for (String transcriptID : transcriptIDs){
                            // get the transcript...
                            Transcript transcript = backendInterface.getTranscript(transcriptID);

                            NodeList tokens = (NodeList) xPath.evaluate("//tei:w", transcript.getDocument().getDocumentElement(), XPathConstants.NODESET);    
                            for (int i=0; i<tokens.getLength(); i++){
                                Element token = (Element) tokens.item(i);
                                String phon = token.getAttribute("phon") + ".";
                                int countSyllablesForToken =
                                    phon.length() - phon.replaceAll("\\.", "").length();
                                countSyllables+=countSyllablesForToken;                                    
                            }

                            NodeList absWithW = (NodeList) xPath.evaluate("//tei:annotationBlock[descendant::tei:w]", transcript.getDocument().getDocumentElement(), XPathConstants.NODESET);    
                            for (int i=0; i<absWithW.getLength(); i++){
                                Element ab = (Element) absWithW.item(i);
                                String start = ab.getAttribute("start");
                                String end = ab.getAttribute("end");
                                //System.out.println("STart" + start);
                                Element startWhen = (Element)
                                        xPath.evaluate("//tei:when[@xml:id='" + start + "']", transcript.getDocument().getDocumentElement(), XPathConstants.NODE);     
                                Element endWhen = (Element)
                                        xPath.evaluate("//tei:when[@xml:id='" + end + "']", transcript.getDocument().getDocumentElement(), XPathConstants.NODE);     
                                double startTime = Double.parseDouble(startWhen.getAttribute("interval"));
                                double endTime = Double.parseDouble(endWhen.getAttribute("interval"));
                                double duration = endTime - startTime;

                                // deduce 0.2 per micro pause
                                NodeList pauses = (NodeList) xPath.evaluate("descendant::tei:pause", ab, XPathConstants.NODESET);     
                                duration-=pauses.getLength() * 0.2;

                                sumTime+=duration;
                            }
                        }

                        double articulationRate = countSyllables / sumTime;

                        String xmlString = "<measures speechEventID=\"" + speechEventID + "\">";
                        xmlString+="<measure type=\"articulationRate\" articulationRate=\"" +  Double.toString(articulationRate) + "\"/>";
                        xmlString+="</measures>";

                        xml.append(xmlString);

                        System.out.println(speechEventID + ": " + (countSyllables / sumTime));
                    }

                }       
                xml.append("</measures-document>");

                //String OUT_FILE_XML = "D:\\WebApplication3\\src\\java\\data\\Measure_12_" + corpusID + ".xml";        
                //String OUT_FILE_XML = data_path + "Measure_12_" + CORPUS + ".xml";        
                //Path fileXML = Paths.get(OUT_FILE_XML);
                //Files.write(fileXML, xml.toString().getBytes("UTF-8"));        
                
                String fileName = "Measure_12_" + corpusID + ".xml";
                String path = new File(OUTPUT_PATH + fileName).getPath();
                System.out.println(fileName + " is written to " + path);
                String xmlString = xml.toString();
                Files.write(Paths.get(path), xmlString.getBytes("UTF-8"));

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | XPathExpressionException ex) {
                Logger.getLogger(Measure_12.class.getName()).log(Level.SEVERE, null, ex);
                throw new IOException(ex);
            }
        }
    }
}
