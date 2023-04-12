/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.implementations.DGD2Corpus;
import org.zumult.objects.implementations.DGD2MetadataKey;

/**
 *
 * @author Frick
 */
public class AGDUtilities {
        
    public static Set<MetadataKey> getMetadataKeysFromMetadataSelection(String corpusID){
        Set<MetadataKey> result = new HashSet<>();
        try {
            // currently, for an AGD corpus, this information is represented outside the Oracle DB
            // in a file MetadataSelection.xml (as part of the DGD Tomcat Webapp)
            String path = "/data/MetadataSelection.xml";
            String xml = new Scanner(DGD2Corpus.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            
            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            //String xPathString = "//metadata-item[level='event-metadata' and descendant::corpus='" + getID() + "']/dgd-parameter-name";
//            String xPathString = "//metadata-item[not(label='DGD-Kennung') and not(starts-with(xpath,'Basisdaten/Ort')) and level='event-metadata' and descendant::corpus='" + getID() + "']";
            String xPathString = "//metadata-item[not(label='DGD-Kennung') and descendant::corpus='" + corpusID + "']";
            NodeList nodes = (NodeList)xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){
                //Element parameterNameElement = ((Element)(nodes.item(i)));
                //result.add(parameterNameElement.getTextContent().substring(2));                
                Element keyElement = ((Element)(nodes.item(i)));
                DGD2MetadataKey key = new DGD2MetadataKey(keyElement);
                result.add(key);
            }
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(DGD2Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public static Set<MetadataKey> getMetadataKeysFromMetadataSelection(String corpusID, String metadataLevel){ 
    //possible values for metadataLevel: event-metadata, speech-event-metadata, speech-event-speaker-metadata, speaker-metadata
        Set<MetadataKey> result = new HashSet<>();
        try {
            String path = "/data/MetadataSelection.xml";
            String xml = new Scanner(DGD2Corpus.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//metadata-item[not(label='DGD-Kennung') and level='" + metadataLevel + "' and descendant::corpus='" + corpusID + "']";
            NodeList nodes = (NodeList)xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){              
                Element keyElement = ((Element)(nodes.item(i)));
                DGD2MetadataKey key = new DGD2MetadataKey(keyElement);
                result.add(key);
            }
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(DGD2Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
