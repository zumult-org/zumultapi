/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import net.sf.saxon.lib.NamespaceConstant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import org.zumult.objects.IDList;
import org.zumult.query.MetadataFilter;

/**
 *
 * @author thomas.schmidt
 */
public class DGDMetadataFilter implements MetadataFilter {
    
    static {
        System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");        
    }

    @Override
    public IDList filterSpeechEvents(String corpusID, String metadataKeyID, String regex) throws IOException {
        IDList result = new IDList("speech-event");
        String indexName = "/data/" + corpusID + "_SpeechEventIndex.xml";
        try {
            String xml = IOHelper.readUTF8(DGDMetadataFilter.class.getResourceAsStream(indexName));
            Document document = IOHelper.DocumentFromText(xml);
            
            String xPathString = "//speech-event[matches(key[@id='" + metadataKeyID + "'], '" + regex + "')]";            
            XPath xPath = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON).newXPath();    
            NodeList nl = (NodeList) xPath.evaluate(xPathString, document, XPathConstants.NODESET);
            
            for (int i=0; i<nl.getLength(); i++){
                Element e = (Element) nl.item(i);
                String speechEventID = e.getAttribute("id");
                result.add(speechEventID);
            }
        } catch (SAXException | ParserConfigurationException | XPathExpressionException | XPathFactoryConfigurationException ex) {
            Logger.getLogger(DGDMetadataFilter.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        return result;

    }

    @Override
    public IDList filterSpeechEvents(String corpusID, String metadataKeyID, double minValue, double maxValue) throws IOException {
        IDList result = new IDList("speech-event");
        String indexName = "/data/" + corpusID + "_SpeechEventIndex.xml";
        try {
            String xml = IOHelper.readUTF8(DGDMetadataFilter.class.getResourceAsStream(indexName));
            Document document = IOHelper.DocumentFromText(xml);
            
            String xPathString = "//speech-event["
                    + "key[@id='" + metadataKeyID + "']>=" + Double.toString(minValue) + " and "
                    + "key[@id='" + metadataKeyID + "']<=" + Double.toString(maxValue) 
                    + "]";            
            XPath xPath = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON).newXPath();    
            NodeList nl = (NodeList) xPath.evaluate(xPathString, document, XPathConstants.NODESET);
            
            for (int i=0; i<nl.getLength(); i++){
                Element e = (Element) nl.item(i);
                String speechEventID = e.getAttribute("id");
                result.add(speechEventID);
            }
        } catch (SAXException | ParserConfigurationException | XPathExpressionException | XPathFactoryConfigurationException ex) {
            Logger.getLogger(DGDMetadataFilter.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        return result;
    }
    
}
