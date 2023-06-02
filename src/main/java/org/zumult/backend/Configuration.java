/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

// For Elena's part of the code
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.w3c.dom.NodeList;
import org.zumult.objects.IDList;


/**
 *
 * @author Thomas_Schmidt, Elena Frick
 */
public class Configuration {
    
    private static String backendInterfaceClassPath;
    
    private static String mediaPath;
    private static String mediaArchivePath;
    private static String mediaDistributionPath;
    private static String mediaSnippetsPath;
    private static String ffmpegPath;
    private static String wordlistPath;
    private static String germanetPath;
    
    private static String transcriptPath;
    private static String protocolPath;
    private static String metadataPath;
    private static String materialPath;    
    private static String restAPIBaseURL;
    private static String webAppBaseURL;

    static String PATH = "/org/zumult/backend/Configuration.xml";


    // Elena's part of the configuration    
    static String searchIndexPath;
    
    // new 25-11-2020, for issue #22
    static final Set<String> FREE_DATA = new HashSet<String>();

    static IDList corpusIDs = new IDList("corpus"); //ids of corpora to be indexed
    static IDList speakerBasedIndexIDs = new IDList("speakerBasedIndexIDs"); //names of indexes for speaker based search
    static IDList transcriptBasedIndexIDs = new IDList("transcriptBasedIndexIDs"); //names of indexes for transcript based search
        
    static {
        try {
            //backendInterfaceClassPath = "org.zumult.backend.implementations.DGD2Oracle";
            //mediaPath = "/srv/video";
            // read the configuration
            String xml = new Scanner(Configuration.class.getResourceAsStream(PATH), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);                      
            // this is for getting an attribute value, looks pretty clumsy...
            backendInterfaceClassPath = doc.getElementsByTagName("backend").item(0).getAttributes().getNamedItem("classPath").getTextContent();
            
            germanetPath = doc.getElementsByTagName("germanet-path").item(0).getTextContent();
            wordlistPath = doc.getElementsByTagName("wordlist-path").item(0).getTextContent();
            mediaPath = doc.getElementsByTagName("media-path").item(0).getTextContent();
            mediaArchivePath = doc.getElementsByTagName("media-archive-path").item(0).getTextContent();
            mediaDistributionPath = doc.getElementsByTagName("media-distribution-path").item(0).getTextContent();
            mediaSnippetsPath = doc.getElementsByTagName("media-snippets-path").item(0).getTextContent();
            ffmpegPath = doc.getElementsByTagName("ffmpeg-path").item(0).getTextContent();
            
            metadataPath = doc.getElementsByTagName("metadata-path").item(0).getTextContent();
            materialPath = doc.getElementsByTagName("material-path").item(0).getTextContent();            
            restAPIBaseURL = doc.getElementsByTagName("rest-api-base-url").item(0).getTextContent();
            webAppBaseURL = doc.getElementsByTagName("web-app-base-url").item(0).getTextContent();
            transcriptPath = doc.getElementsByTagName("transcript-path").item(0).getTextContent();
            protocolPath = doc.getElementsByTagName("protocol-path").item(0).getTextContent();
            
            // new 25-11-2020, for issue #22
            NodeList freePaths = doc.getElementsByTagName("free-path");
            for (int i=0; i<freePaths.getLength(); i++){
                FREE_DATA.add(freePaths.item(i).getTextContent());
            }
            
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Elena's additional read
        read();
        // read the configuration
        //backendInterfaceClassPath = "org.zumult.backend.implementations.DGD2Oracle";
    }
    
    // new 25-11-2020
    public static Set<String> getFreeData(){
        return FREE_DATA;
    }
    
    public static String getBackendInterfaceClassPath(){
        return backendInterfaceClassPath;
    }

    public static String getMediaPath(){
        return mediaPath;
    }
    
    public static String getMediaArchivePath() {
        return mediaArchivePath;
    }

    public static String getMediaDistributionPath() {
        return mediaDistributionPath;
    }
    
    
    public static String getMediaSnippetsPath() {
        return mediaSnippetsPath;
    }
    
    public static String getFfmpegPath(){
        return ffmpegPath;
    }
    
    public static String getTranscriptPath(){
        return transcriptPath;
    }
    
    public static String getWordlistPath(){
        return wordlistPath;
    }
    
    public static String getGermanetPath(){
        return germanetPath;
    }

    public static String getProtocolPath(){
        return protocolPath;
    }

    public static String getMetadataPath(){
        return metadataPath;
    }

    public static String getRestAPIBaseURL(){
        return restAPIBaseURL;
    }
    
    public static String getWebAppBaseURL() {
        return webAppBaseURL;
    }
    
    public static String getMaterialPath(){
        return materialPath;
    }
        
    public static IDList getCorpusIDs(){
        return corpusIDs;
    }    

    public static String getSearchIndexPath() {
        return searchIndexPath;
    }

    public static IDList getSpeakerBasedIndexIDs() {
        return speakerBasedIndexIDs;
    }

    public static IDList getTranscriptBasedIndexIDs() {
        return transcriptBasedIndexIDs;
    }
    
    static void read(){
        Configurations configs = new Configurations();
        try{
            File f = new File(Configuration.class.getResource(PATH).getFile());
            XMLConfiguration config = configs.xml(f.getAbsolutePath());

            searchIndexPath = config.getString("backend.search-index-path");
            
            String[] corpora = config.getString("backend.corpus-ids-for-indexing").split(";");
            for (String corpus : corpora) {
                corpusIDs.add(corpus);
            }
            
            config.getList("backend.search-index-speaker-based").forEach((o) -> {
                speakerBasedIndexIDs.add(o.toString());
            });
             
            config.getList("backend.search-index-transcript-based").forEach((o) -> {
                transcriptBasedIndexIDs.add(o.toString());
            });
   
        } catch (ConfigurationException ex){
           Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
