/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Frick
 */
public class COMATranscript extends ISOTEITranscript {

    public COMATranscript(Document transcriptDocument) {
        super(transcriptDocument);            
    }
    
    public COMATranscript(String transcriptXML) {
        super(transcriptXML);
    }
        
    public COMATranscript(String transcriptXML, String metadataXML) {
        super(transcriptXML, metadataXML);          
    }
    
    public COMATranscript(Document transcriptDocument, Document metadataDocument) {
        super(transcriptDocument, metadataDocument);
    }
    
    @Override
    public String getMetadataValue(MetadataKey key) {
        // for issue #146
        // this is tricky because we can only assume that the Metadata is some XML fragment
        // the exact implementation of MetadataKey can help to determine
        // how to do this, though
        // for the moment, will only provide a value for COMA
        if (key instanceof COMAMetadataKey comaKey){
            /*
            <Description>
                <Key Name="transcription-name">60-414-1-3-a</Key>
                <Key Name="Section ID">60-414-1-3-a</Key>
                <Key Name="Section Title">Spoke German with husband, How they met</Key>
                <Key Name="Section Type">Open-ended</Key>
              </Description>            
            */
            XPath theXPath = XPathFactory.newInstance().newXPath();
            try {
                Element keyElement = 
                        ((Element)theXPath.evaluate("//Key[@Name='" 
                                + comaKey.getName("en") + "']", 
                               metadata.getDocument().getDocumentElement(), 
                               XPathConstants.NODE));
                if (keyElement==null) return null;
                return keyElement.getTextContent();
            } catch (XPathExpressionException ex) {
                Logger.getLogger(ISOTEITranscript.class.getName())
                        .log(Level.SEVERE, null, ex);
            }            
        } 
        return null;   
    }
    

    @Override
    public Set<String> getMetadataValues (MetadataKey key, String language) {
        // this is preliminary until COMA supports multilingual metadata and multiple values per key
        Set<String> result = new HashSet<>();
        result.add(getMetadataValue(key));
        return result;
    }

    @Override
    public Set<String> getMetadataValues (MetadataKey key) {
        // this is preliminary until COMA supports multilingual metadata 
        Set<String> result = new HashSet<>();
        result.add(getMetadataValue(key));
        return result;
    }


    @Override
    public ISOTEITranscript createNewInstance(Document transcriptDocument, Document metadataDocument){
        return new COMATranscript(transcriptDocument, metadataDocument);
    }
    
    @Override
    public ISOTEITranscript createNewInstance(Document transcriptDocument){
        return new COMATranscript(transcriptDocument);
    }

    @Override
    public Map<String, Set<String>> getMetadataValuesByLanguages (MetadataKey key) {
        throw new UnsupportedOperationException ("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}