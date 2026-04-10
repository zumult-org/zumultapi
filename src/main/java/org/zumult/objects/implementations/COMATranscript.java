/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zumult.objects.Episode;
import org.zumult.objects.IDList;
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

    Map<String, List<Episode>> episodes; // cache
    // new for #271
    @Override
    public Map<String, List<Episode>> getEpisodes() {
        if (episodes!=null) {
            return episodes;
        } // cache
        episodes = new HashMap<>();
        try {
            NodeList nodes = (NodeList)xPath.evaluate("//tei:body/tei:spanGrp[@type='episodes']", getDocument(), XPathConstants.NODESET);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            for (int i=0; i<nodes.getLength(); i++){                
                Element node = (Element) nodes.item(i);
                String name = ((Element)node).getAttribute("subtype");
                if (name==null) {
                    name="default";
                }
                NodeList nodes2 = node.getChildNodes();
                List<Episode> theseEpisodes = new ArrayList<>();
                for (int j=0; j<nodes.getLength(); j++){                
                    Element node2 = (Element) nodes2.item(j);
                    // Create a new empty document
                    Document episodeDoc = builder.newDocument();
                    // Import the node into the new document
                    Node importedNode = episodeDoc.importNode(node2, true); // true = deep copy
                    // Append as root element
                    episodeDoc.appendChild(importedNode);
                    ISOTEIEpisode episode = new ISOTEIEpisode(episodeDoc, name);
                    theseEpisodes.add(episode);
                }
                episodes.put(name, theseEpisodes);
            }
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(COMATranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return episodes;

    }

    // new for #271
    @Override
    public List<Episode> getEpisodesByName(String name) {
        return getEpisodes().get(name);
    }

    // new for #271
    @Override
    public IDList getEpisodeNames() {
        Set<String> keySet = getEpisodes().keySet();
        IDList episodeNames = new IDList("episodes");
        episodeNames.addAll(keySet);
        return episodeNames;
    }

}