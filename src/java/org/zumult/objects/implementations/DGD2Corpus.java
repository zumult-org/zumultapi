/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

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
import org.zumult.io.IOHelper;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTypeEnum;
import org.zumult.objects.Corpus;
import org.zumult.objects.MetadataKey;
import org.zumult.io.Constants;
import java.io.File;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2Corpus extends AbstractXMLObject implements Corpus {

    public DGD2Corpus(Document corpusDocument) {
        super(corpusDocument);
    }

    public DGD2Corpus(String corpusXML) {
        super(corpusXML);
    }

    @Override
    public String getID() {
        // e.g. 'ZW--' 
        // get it from the document: <Korpus Kennung="ZW--">
        return getDocument().getDocumentElement().getAttribute("Kennung");
    }

    @Override
    public String getAcronym() {
        // if 'ZW--' is the Kennung, then 'ZW' is the acronym
        return getID().replaceAll("-+", "");
    }

    @Override
    public String getName(String language) {
        try {
            // e.g. 'Forschungs- und Lehrkorpus Gesprochenes Deutsch' or 'Deutsche Mundarten: Zwirner Korpus'
            // get it from the document: <Name lang="de">Deutsche Mundarten: Zwirner-Korpus</Name>
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "/Korpus/Name[@lang='" + language + "']";
            Element element = (Element)xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            if (element!=null) {
                return element.getTextContent();
            }
            // fallback 
            xPathString = "/Korpus/Name";
            element = (Element)xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            return element.getTextContent();            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Error retrieving corpus name";
    }
    
    @Override
    public String getDescription(String language) {
        try {
            // a prose description of the corpus
            // get it from the document: <Korpus_Projekt_Kurzbeschreibung>Das Korpus Deutsche Mundarten: Zwirner-Korpus (ZW--) wurde in Rahmen eines Projekts [...]
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "/Korpus/Korpus_Projekt_Kurzbeschreibung[@lang='" + language + "']";
            Element element = (Element)xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            if (element!=null) {
                return element.getTextContent();
            }
            // fallback
            xPathString = "/Korpus/Korpus_Projekt_Kurzbeschreibung";
            element = (Element)xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            return element.getTextContent();
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Error retrieving corpus description";
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
            String xPathString = "//metadata-item[not(label='DGD-Kennung') and level='event-metadata' and descendant::corpus='" + getID() + "']";
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

    @Override
    public Set<MetadataKey> getSpeechEventMetadataKeys() {
        // see above
        Set<MetadataKey> result = new HashSet<>();
        try {
            // currently, for an AGD corpus, this information is represented outside the Oracle DB
            // in a file MetadataSelection.xml (as part of the DGD Tomcat Webapp)
            String path = "/data/MetadataSelection.xml";
            String xml = new Scanner(DGD2Corpus.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            
            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//metadata-item[level='speech-event-metadata' and descendant::corpus='" + getID() + "']";
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

    @Override
    public Set<MetadataKey> getSpeakerInSpeechEventMetadataKeys() {
        // see above
        // see above
        Set<MetadataKey> result = new HashSet<>();
        try {
            // currently, for an AGD corpus, this information is represented outside the Oracle DB
            // in a file MetadataSelection.xml (as part of the DGD Tomcat Webapp)
            String path = "/data/MetadataSelection.xml";
            String xml = new Scanner(DGD2Corpus.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            
            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//metadata-item[level='speech-event-speaker-metadata' and descendant::corpus='" + getID() + "']";
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

    @Override
    public Set<MetadataKey> getSpeakerMetadataKeys() {
        // see above
        Set<MetadataKey> result = new HashSet<>();
        try {
            // currently, for an AGD corpus, this information is represented outside the Oracle DB
            // in a file MetadataSelection.xml (as part of the DGD Tomcat Webapp)
            String path = "/data/MetadataSelection.xml";
            String xml = new Scanner(DGD2Corpus.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            
            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            //String xPathString = "//metadata-item[not(label='DGD-Kennung') and level='speaker-metadata' and not(starts-with(xpath,'Ortsdaten')) and descendant::corpus='" + getID() + "']";
            String xPathString = "//metadata-item[not(label='DGD-Kennung') and level='speaker-metadata' and descendant::corpus='" + getID() + "']";
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
    
    @Override
    public Set<AnnotationLayer> getAnnotationLayers() {
        //return IOHelper.getAnnotationLayersForCorpus(getID(), null );
        return getAnnotationLayersForType(null);
        
    }
    
    @Override
    public Set<AnnotationLayer> getTokenBasedAnnotationLayers(){
        Set<AnnotationLayer> result = new HashSet();
        result.addAll(getAnnotationLayersForType(AnnotationTypeEnum.TOKEN));
        return result;
    }
    
    @Override
    public Set<AnnotationLayer> getSpanBasedAnnotationLayers(){
        Set<AnnotationLayer> result = new HashSet();
        result.addAll(getAnnotationLayersForType(AnnotationTypeEnum.SPAN));
        return result;
    }
    
    private Set<AnnotationLayer> getAnnotationLayersForType(AnnotationTypeEnum type){
        Set<AnnotationLayer> result = new HashSet<>();
        try {
            
            String path = Constants.DATA_ANNOTATIONS_PATH + "AnnotationLayerSelection.xml";
            String xml = new Scanner(IOHelper.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml); 
            
            XPath xPath = XPathFactory.newInstance().newXPath();
            StringBuilder xPathString = new StringBuilder();
            xPathString.append("//key[");
            if(type!=null){
                xPathString.append("@type='"+type.name().toLowerCase() +"' and ");
            }
                xPathString.append("descendant::corpus='" + getID() + "']");

            NodeList nodes = (NodeList)xPath.evaluate(xPathString.toString(), doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){
                Element keyElement = ((Element)(nodes.item(i)));
                AnnotationLayer key = new DGD2AnnotationLayer(keyElement);
                result.add(key);                
            }
            
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(DGD2Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    
    @Override
    public Set<String> getSpeakerLocationTypes() {
        Set<String> result = new HashSet<>();
        try {
            // currently, for an AGD corpus, this information is represented outside the Oracle DB
            // in a file MetadataSelection.xml (as part of the DGD Tomcat Webapp)
            String path = "/data/LocationTypes.xml";
            String xml = new Scanner(DGD2Corpus.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            
            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//corpus[@id='" + getID() + "']/location-type";
            NodeList nodes = (NodeList)xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){
                Element ltElement = ((Element)(nodes.item(i)));
                result.add(ltElement.getTextContent());                
            }
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(DGD2Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }



    
}
