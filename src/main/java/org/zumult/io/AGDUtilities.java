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
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.implementations.DGD2Corpus;
import org.zumult.objects.implementations.DGD2MetadataKey;

/**
 *
 * @author Frick
 */
public class AGDUtilities {
    
    public static Set<MetadataKey> getMetadataKeysFromMetadataSelection(String corpusID, ObjectTypesEnum objectType){ 

        String xPathString = "//metadata-item[not(label='DGD-Kennung') and descendant::corpus='" + corpusID + "']";
        
        if (objectType!=null){
            String metadataLevel;
            metadataLevel = switch (objectType) {
                case EVENT -> "event-metadata";
                case SPEECH_EVENT -> "speech-event-metadata";
                case SPEAKER -> "speaker-metadata";
                case SPEAKER_IN_SPEECH_EVENT -> "speech-event-speaker-metadata";
                default -> "XXX";
            };
            
            xPathString = "//metadata-item[not(label='DGD-Kennung') and level='" + metadataLevel + "' and descendant::corpus='" + corpusID + "']";
        }
        
        Set<MetadataKey> result = new HashSet<>();

        try {
            String xml = IOHelper.readUTF8(AGDUtilities.class.getResourceAsStream(Constants.METADATA_SELECTION_PATH));
            Document doc = IOHelper.DocumentFromText(xml);
            XPath xPath = XPathFactory.newInstance().newXPath();

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
