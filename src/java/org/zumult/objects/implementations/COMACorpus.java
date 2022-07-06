/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.Corpus;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;

/**
 *
 * @author thomas.schmidt
 */
public class COMACorpus extends AbstractXMLObject implements Corpus {

    XPath xPath = XPathFactory.newInstance().newXPath();

    public COMACorpus(Document xmlDocument) {
        super(xmlDocument);
    }

    public COMACorpus(String xmlString) {
        super(xmlString);
    }

    @Override
    public String getAcronym() {
        // get /Corpus/Description/Key[@Name="hzsk:corpusPrefix"] if you can, else return empty string
        try {
            String xPathString = "/Corpus/Description/Key[@Name=\"hzsk:corpusPrefix\"]";
            Element tryElement = (Element)xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            if (tryElement!=null){
                return tryElement.getTextContent();
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACorpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @Override
    public String getName(String language) {
        // get /Corpus/@Name
        return getDocument().getDocumentElement().getAttribute("Name");
    }

    @Override
    public String getDescription(String language) {
        // get /Corpus/Description/Key[@Name="hzsk:shortDescription"] if you can, else return empty string
        try {
            String xPathString = "/Corpus/Description/Key[@Name=\"hzsk:shortDescription\"]";
            Element tryElement = (Element)xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            if (tryElement!=null){
                return tryElement.getTextContent();
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACorpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @Override
    public Set<MetadataKey> getMetadataKeys() {
        Set<MetadataKey> result = new HashSet<>();
        result.addAll(getEventMetadataKeys());
        result.addAll(getSpeechEventMetadataKeys());
        result.addAll(getSpeakerInSpeechEventMetadataKeys());
        result.addAll(getSpeakerMetadataKeys());
        return result;
    }

    @Override
    public Set<MetadataKey> getEventMetadataKeys() {
        // we don't have event metadata in COMA
        // because we don't really have events
        // just return an empty set
        return new HashSet<>();
    }

    @Override
    public Set<MetadataKey> getSpeechEventMetadataKeys() {
        // use a stylesheet with group()?
        // not now - let's keep that simple
        Set<MetadataKey> result = new HashSet<>();
        try {
            String xPathString = "//Communication/Description/Key|//Setting/Description/Key";
            NodeList allKeys = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            HashSet<String> keySet = new HashSet<>();
            for (int i=0; i<allKeys.getLength(); i++){
                Element keyElement = ((Element)(allKeys.item(i)));
                keySet.add(keyElement.getAttribute("Name"));
            }      
            for (String key: keySet){
                COMAMetadataKey comaMetadataKey = new COMAMetadataKey(key, key, ObjectTypesEnum.EVENT);
                result.add(comaMetadataKey);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACorpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;        
    }

    @Override
    public Set<MetadataKey> getSpeakerInSpeechEventMetadataKeys() {
        // we don't have speech event specific speaker metadata in COMA
        // because we don't really have speech events
        // just return an empty set
        return new HashSet<>();
    }

    @Override
    public Set<MetadataKey> getSpeakerMetadataKeys() {
        // use a stylesheet with group()?
        // not now - let's keep that simple
        Set<MetadataKey> result = new HashSet<>();
        try {
            String xPathString = "//Speaker/Description/Key";
            NodeList allKeys = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            HashSet<String> keySet = new HashSet<>();
            for (int i=0; i<allKeys.getLength(); i++){
                Element keyElement = ((Element)(allKeys.item(i)));
                keySet.add(keyElement.getAttribute("Name"));
            }      
            for (String key: keySet){
                COMAMetadataKey comaMetadataKey = new COMAMetadataKey(key, key, ObjectTypesEnum.SPEAKER);
                result.add(comaMetadataKey);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACorpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;        
    }


    @Override
    public Set<String> getSpeakerLocationTypes() {
        // use a stylesheet with group()?
        // not now - let's keep that simple
        Set<String> result = new HashSet<>();
        try {
            String xPathString = "//Speaker/Location";
            NodeList allLocations = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allLocations.getLength(); i++){
                Element locationElement = ((Element)(allLocations.item(i)));
                result.add(locationElement.getAttribute("Type"));
            }      
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACorpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;        
    }

    @Override
    public String getID() {
        // get /Corpus/@ID?
        // this is not what we do in the backend
        // there, we are using the acronym as the ID
        // so let's return the acronym for now
        return getAcronym();

    }

    @Override
    public Set<AnnotationLayer> getAnnotationLayers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<AnnotationLayer> getTokenBasedAnnotationLayers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<AnnotationLayer> getSpanBasedAnnotationLayers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
