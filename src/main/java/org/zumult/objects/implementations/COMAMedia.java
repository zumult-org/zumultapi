/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.zumult.backend.Configuration;
import org.zumult.io.MediaUtilities;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author thomas.schmidt
 */
public class COMAMedia extends AbstractMedia {

    
    String fileString;
    XMLMetadata metadata;
    
    
    public COMAMedia(String id, String urlString){
        super(id, urlString);
    }
        

    public COMAMedia(String id, String urlString, String fileString){
        super(id, urlString);
        this.fileString = fileString;
    }
    
    public COMAMedia(String id, String urlString, String fileString, String metadataXML){
        super(id, urlString);
        this.fileString = fileString;
        metadata = new XMLMetadata(metadataXML);
    }


    public String getFileString(){
        return fileString;
    }

    @Override
    public MEDIA_TYPE getType() {
        String url = super.getURL();
        int index = url.lastIndexOf(".");
        String suffix = url.substring(index+1).toLowerCase();
        switch(suffix){
            case "mp3" :
            case "wav" : 
                return MEDIA_TYPE.AUDIO;
            default :
                return MEDIA_TYPE.VIDEO;
        }
    }
    
    @Override
    public Media getPart(double startInSeconds, double endInSeconds) {
        String[] idAndUrl = cut(startInSeconds, endInSeconds);
        return new COMAMedia(idAndUrl[0], idAndUrl[1]);
    }
    
    @Override
    public Media getVideoImage(double positionInSeconds) {
        String[] idAndUrl = still(positionInSeconds); 
        if (idAndUrl!=null){
            return new COMAMedia(idAndUrl[0], idAndUrl[1]);            
        }
        return null;
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
    public MediaUtilities getMediaUtilities(){
        return new MediaUtilities(Configuration.getFfmpegPath(), Configuration.getFfprobePath());
    }

    @Override
    public double getDuration() {
        XPath theXPath = XPathFactory.newInstance().newXPath();
        try {
            // look for existing ffprobe metadata - video?
            Element keyElement = ((Element)theXPath.evaluate("//Key[@Name='ffprobe-video-duration']", 
                           metadata.getDocument().getDocumentElement(), 
                           XPathConstants.NODE));
            if (keyElement!=null) {
                double value = Double.parseDouble(keyElement.getTextContent());
                return value;
            }
            // look for existing ffprobe metadata - audio?
            keyElement = ((Element)theXPath.evaluate("//Key[@Name='ffprobe-audio-duration']", 
                           metadata.getDocument().getDocumentElement(), 
                           XPathConstants.NODE));
            if (keyElement!=null) {
                double value = Double.parseDouble(keyElement.getTextContent());
                return value;
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return super.getDuration();
    }

    @Override
    public Map<String, Set<String>> getMetadataValuesByLanguages (MetadataKey key) {
        throw new UnsupportedOperationException ("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    
    
    
}
