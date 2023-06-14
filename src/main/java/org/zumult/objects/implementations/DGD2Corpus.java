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
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.AGDUtilities;
import org.zumult.io.IOHelper;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTypeEnum;
import org.zumult.objects.Corpus;
import org.zumult.objects.MetadataKey;
import org.zumult.io.Constants;
import org.zumult.objects.CrossQuantification;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.ResourceServiceException;

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
    public Set<MetadataKey> getMetadataKeys(ObjectTypesEnum objectType) {
        return AGDUtilities.getMetadataKeysFromMetadataSelection(getID(), objectType); 
    }
    
    @Override
    public Set<MetadataKey> getMetadataKeys() {
        return AGDUtilities.getMetadataKeysFromMetadataSelection(getID(), null); 
    }

    @Override
    public Set<AnnotationLayer> getAnnotationLayers() {
        return getAnnotationLayers(null);       
    }
    
    
    public Set<AnnotationLayer> getAnnotationLayers(AnnotationTypeEnum annotationType){
        Set<AnnotationLayer> result = new HashSet<>();
        try {
            
            String path = Constants.DATA_ANNOTATIONS_PATH + "AnnotationLayerSelection.xml";
            String xml = new Scanner(IOHelper.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml); 
            
            XPath xPath = XPathFactory.newInstance().newXPath();
            StringBuilder xPathString = new StringBuilder();
            xPathString.append("//key[");
            if(annotationType!=null){
                xPathString.append("@type='"+annotationType.name().toLowerCase() +"' and ");
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

    @Override
    public String getMetadataValue(MetadataKey key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    @Override
    public CrossQuantification getCrossQuantification(MetadataKey metadataKey1, MetadataKey metadataKey2, String unit) throws ResourceServiceException, IOException  {        
        if(!metadataKey1.isQuantified()){
            throw new ResourceServiceException("Parameter " + metadataKey1 +" is not quantifiable!");
        }
        
        if(!metadataKey2.isQuantified()){
            throw new ResourceServiceException("Parameter " + metadataKey2 +" is not quantifiable!");
        }
        
        if (unit==null){
            unit = "TOKENS";
        }
                    
        String[][] PARAM = {
        {"META_FIELD_1", "v_" + metadataKey1.getID()},
        {"META_FIELD_2", "v_" + metadataKey2.getID()},
        {"UNITS", unit}
        };
    
        String QUANT_FILENAME = getID() + "_QUANT.xml";

        
        try {

            String html = new IOHelper().applyInternalStylesheetToInternalFile("/org/zumult/io/Quantify2Dimensions.xsl", 
            Constants.DATA_QUANTIFICATIONS_PATH + "/" + QUANT_FILENAME, PARAM);
                
            CrossQuantification crossQuantification = new DGD2CrossQuantification(html);
            return crossQuantification;
            
        } catch (TransformerException ex) {
            throw new IOException(ex); 
        }
     
    }

    
}
