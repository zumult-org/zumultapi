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
import org.w3c.dom.Element;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author bernd
 */
public class COMAAdditionalMaterial extends AbstractAdditionalMaterial {
    
    String fileString;
    XMLMetadata metadata;
    
    
    public COMAAdditionalMaterial(String id, String urlString){
        super(id, urlString);
    }
        

    public COMAAdditionalMaterial(String id, String urlString, String fileString){
        super(id, urlString);
        this.fileString = fileString;
    }
    
    public COMAAdditionalMaterial(String id, String urlString, String fileString, String metadataXML){
        super(id, urlString);
        this.fileString = fileString;
        metadata = new XMLMetadata(metadataXML);
    }


    public String getFileString(){
        return fileString;
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
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
    public Map<String, Set<String>> getMetadataValuesByLanguages (MetadataKey key) {
        throw new UnsupportedOperationException ("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
    
}
