/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

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
public class ComaTranscript extends ISOTEITranscript {

    public ComaTranscript(Document transcriptDocument) {
        super(transcriptDocument);            
    }
    
    public ComaTranscript(String transcriptXML) {
        super(transcriptXML);
    }
        
    public ComaTranscript(String transcriptXML, String metadataXML) {
        super(transcriptXML, metadataXML);          
    }
    
    public ComaTranscript(Document transcriptDocument, Document metadataDocument) {
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
    public ISOTEITranscript createNewInstance(Document transcriptDocument, Document metadataDocument){
        return new ComaTranscript(transcriptDocument, metadataDocument);
    }
    
    @Override
    public ISOTEITranscript createNewInstance(Document transcriptDocument){
        return new ComaTranscript(transcriptDocument);
    }
}
