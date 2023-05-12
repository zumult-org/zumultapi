/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;

/**
 *
 * @author thomas.schmidt
 */
public class AddMeasuresToSpeechEventIndex implements Indexer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new AddMeasuresToSpeechEventIndex().index();
        } catch (IOException ex) {
            Logger.getLogger(AddMeasuresToSpeechEventIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //String corpusID = "FOLK";
    String[] corpusIDs = {"FOLK", "GWSS"};
    String MEASURE_PATH = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_MEASURES_PATH;

    @Override
    public void index() throws IOException {
        for (String corpusID : corpusIDs){
            /*
                        String SPEECH_EVENT_INDEX_IN = "D:\\WebApplication3\\src\\java\\data\\" + corpusID + "_SpeechEventIndex.xml";
            String MEASURE_1_DOCUMENT = "D:\\WebApplication3\\src\\java\\data\\Measure_1_" + corpusID + ".xml";
            String MEASURE_7_DOCUMENT = "D:\\WebApplication3\\src\\java\\data\\Measure_7_" + corpusID + ".xml";
            String MEASURE_8_DOCUMENT = "D:\\WebApplication3\\src\\java\\data\\Measure_8_" + corpusID + ".xml";
            String MEASURE_12_DOCUMENT = "D:\\WebApplication3\\src\\java\\data\\Measure_12_" + corpusID + ".xml";
            String MEASURE_13_DOCUMENT = "D:\\WebApplication3\\src\\java\\data\\Measure_13_" + corpusID + ".xml";
            String MEASURE_14_DOCUMENT = "D:\\WebApplication3\\src\\java\\data\\Measure_14_" + corpusID + ".xml";
            */
            String SPEECH_EVENT_INDEX_IN = "C:\\Users\\Frick\\Documents\\NetBeansProjects\\zumultapi\\src\\java\\data\\" + corpusID + "_SpeechEventIndex.xml";
            String MEASURE_1_DOCUMENT = MEASURE_PATH + "Measure_1_" + corpusID + ".xml";
            String MEASURE_7_DOCUMENT = MEASURE_PATH + "Measure_7_" + corpusID + ".xml";
            String MEASURE_8_DOCUMENT = MEASURE_PATH + "Measure_8_" + corpusID + ".xml";
            String MEASURE_12_DOCUMENT = MEASURE_PATH + "Measure_12_" + corpusID + ".xml";
            String MEASURE_13_DOCUMENT = MEASURE_PATH + "Measure_13_" + corpusID + ".xml";
            String MEASURE_14_DOCUMENT = MEASURE_PATH + "Measure_14_" + corpusID + ".xml";
            try {
                Document inDocument = IOHelper.readDocument(new File(SPEECH_EVENT_INDEX_IN));
                Document measure1Document = IOHelper.readDocument(new File(MEASURE_1_DOCUMENT));
                Document measure7Document = IOHelper.readDocument(new File(MEASURE_7_DOCUMENT));
                Document measure8Document = IOHelper.readDocument(new File(MEASURE_8_DOCUMENT));
                Document measure12Document = IOHelper.readDocument(new File(MEASURE_12_DOCUMENT));
                Document measure13Document = IOHelper.readDocument(new File(MEASURE_13_DOCUMENT));
                Document measure14Document = IOHelper.readDocument(new File(MEASURE_14_DOCUMENT));
                NodeList speechEvents = inDocument.getElementsByTagName("speech-event");
                for (int i=0; i<speechEvents.getLength(); i++){
                    Node item = speechEvents.item(i);
                    Element element = (Element) item;
                    String speechEventID = element.getAttribute("id");
                    System.out.println(speechEventID);

                    XPath xPath = XPathFactory.newInstance().newXPath();    


                /*
                    <measures speechEventID="FOLK_E_00368_SE_01" lemmas="144" tokens="297">
                        <measure type="intersection" reference="GOETHE_A1" lemmas="80" tokens="226" lemmas_ratio="0,56" tokens_ratio="0,76"/>
                        <measure type="intersection" reference="GOETHE_A2" lemmas="97" tokens="247" lemmas_ratio="0,67" tokens_ratio="0,83"/>
                        <measure type="intersection" reference="GOETHE_B1" lemmas="115" tokens="266" lemmas_ratio="0,80" tokens_ratio="0,90"/>
                        <measure type="intersection" reference="HERDER_1000" lemmas="97" tokens="237" lemmas_ratio="0,67" tokens_ratio="0,80"/>
                        <measure type="intersection" reference="HERDER_2000" lemmas="104" tokens="244" lemmas_ratio="0,72" tokens_ratio="0,82"/>
                        <measure type="intersection" reference="HERDER_3000" lemmas="110" tokens="256" lemmas_ratio="0,76" tokens_ratio="0,86"/>
                        <measure type="intersection" reference="HERDER_4000" lemmas="112" tokens="259" lemmas_ratio="0,78" tokens_ratio="0,87"/>
                        <measure type="intersection" reference="HERDER_5000" lemmas="112" tokens="259" lemmas_ratio="0,78" tokens_ratio="0,87"/>
                    </measures>            
                */
                    Element measure1Element = (Element)xPath.evaluate("//measures[@speechEventID='" +  speechEventID + "']", 
                                        measure1Document.getDocumentElement(), XPathConstants.NODE);
                    NodeList nl = measure1Element.getChildNodes();
                    for (int j=0; j<nl.getLength(); j++){  
                        if (!(nl.item(j) instanceof Element)) continue;
                        Element measureElement = (Element) nl.item(j);
                        Element keyElement = inDocument.createElement("key");
                        keyElement.setAttribute("id", "measure_intersection_" + measureElement.getAttribute("reference"));
                        keyElement.setTextContent(measureElement.getAttribute("tokens_ratio").replace(",", "."));
                        element.appendChild(keyElement);                    
                    }



                    /*
                    <measures speechEventID="FOLK_E_00007_SE_01">
                        <measure normRate="21,90"/>
                    </measures>                
                    */
                    Element measure7Element = (Element)xPath.evaluate("//measures[@speechEventID='" +  speechEventID + "']/measure", 
                                        measure7Document.getDocumentElement(), XPathConstants.NODE);
                    if (true){
                        Element keyElement = inDocument.createElement("key");
                        keyElement.setAttribute("id", "measure_normalisation_rate");
                        keyElement.setTextContent(measure7Element.getAttribute("normRate").replace(",", "."));
                        element.appendChild(keyElement);                    
                    }


                    /*
                        <measures speechEventID="FOLK_E_00003_SE_01">
                            <measure perMilOverlaps="37,68"/>
                            <measure averageNrOverlappingWords="1,91"/>
                            <measure perCentOverlapsWithMoreThan2Words="24,50"/>
                            <measure perMilTokensOverlapsWithMoreThan2Words="9,23"/>
                        </measures>

                    */
                    Element measure8Element = (Element)xPath.evaluate("//measures[@speechEventID='" +  speechEventID + "']", 
                                        measure8Document.getDocumentElement(), XPathConstants.NODE);

                    NodeList nl2 = measure8Element.getChildNodes();
                    for (int j=0; j<nl2.getLength(); j++){  
                        if (!(nl2.item(j) instanceof Element)) continue;
                        Element keyElement = inDocument.createElement("key");
                        Element measureElement = (Element) nl2.item(j);
                        keyElement.setAttribute("id", "measure_overlap_" + measureElement.getAttributes().item(0).getNodeName());
                        keyElement.setTextContent(measureElement.getAttribute(measureElement.getAttributes().item(0).getNodeName()).replace(",", "."));
                        element.appendChild(keyElement);                    
                    }


                    /*
                        <measures speechEventID="FOLK_E_00061_SE_01">
                            <measure type="articulationRate" articulationRate="5.190580871861194"/>
                        </measures>                
                    */

                    Element measure12Element = (Element)xPath.evaluate("//measures[@speechEventID='" +  speechEventID + "']/measure", 
                                        measure12Document.getDocumentElement(), XPathConstants.NODE);
                    if (true){
                        Element keyElement = inDocument.createElement("key");
                        keyElement.setAttribute("id", "measure_articulation_rate");
                        keyElement.setTextContent(measure12Element.getAttribute("articulationRate"));
                        element.appendChild(keyElement);                    
                    }
                    
                    
                    
                    /*
                        <measures speechEventID="FOLK_E_00001_SE_01" tokens="6968.0">
                            <measure type="pos" reference="NN" tokens="1009" tokens_ratio="0.14"/>
                            <measure type="pos" reference="NE" tokens="143" tokens_ratio="0.02"/>
                            <measure type="pos" reference="V" tokens="751" tokens_ratio="0.11"/>
                            <measure type="pos" reference="ADJ" tokens="278" tokens_ratio="0.04"/>
                            <measure type="pos" reference="ADV" tokens="625" tokens_ratio="0.09"/>
                            <measure type="pos" reference="PTKVZ" tokens="74" tokens_ratio="0.01"/>
                        </measures>
                    */

                    Element measure13Element = (Element)xPath.evaluate("//measures[@speechEventID='" +  speechEventID + "']", 
                                        measure13Document.getDocumentElement(), XPathConstants.NODE);
                    NodeList nl3 = measure13Element.getChildNodes();
                    for (int j=0; j<nl3.getLength(); j++){  
                        if (!(nl3.item(j) instanceof Element)) continue;
                        Element measureElement = (Element) nl3.item(j);
                        Element keyElement = inDocument.createElement("key");
                        keyElement.setAttribute("id", "measure_pos_" + measureElement.getAttribute("reference"));
                        keyElement.setTextContent(measureElement.getAttribute("tokens_ratio").replace(",", "."));
                        element.appendChild(keyElement);                    
                    }
                    
                    Element measure14Element = (Element)xPath.evaluate("//measures[@speechEventID='" +  speechEventID + "']", 
                    measure14Document.getDocumentElement(), XPathConstants.NODE);
                    NodeList nl4 = measure14Element.getChildNodes();
                    for (int j=0; j<nl4.getLength(); j++){  
                        if (!(nl4.item(j) instanceof Element)) continue;
                        Element measureElement = (Element) nl4.item(j);
                        Element keyElement = inDocument.createElement("key");
                        keyElement.setAttribute("id", "measure_oral_phenomina_" + measureElement.getAttribute("reference"));
                        keyElement.setTextContent(measureElement.getAttribute("tokens_ratio").replace(",", "."));
                        element.appendChild(keyElement);                   
                    }


                }

                //System.out.println(IOHelper.DocumentToString(inDocument));

                IOHelper.writeDocument(inDocument, new File(SPEECH_EVENT_INDEX_IN));
            } catch (SAXException | ParserConfigurationException | XPathExpressionException | TransformerException ex) {
                throw new IOException(ex);
            }
        }
    }
    
}
