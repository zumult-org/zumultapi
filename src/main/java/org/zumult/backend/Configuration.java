/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.zumult.objects.IDList;


/**
 *
 * @author Thomas_Schmidt, Elena Frick
 */
public class Configuration {
    static String PATH = "/org/zumult/backend/Configuration.xml";
        
    private static String backendInterfaceClassPath;
    
    private static String mediaPath;
    private static String mediaArchivePath;
    private static String mediaDistributionPath;
    private static String mediaSnippetsPath;
    private static String ffmpegPath;
    private static String wordlistPath;
    private static String germanetPath;
    
    private static String quantificationPath;
    private static String transcriptPath;
    private static String protocolPath;
    private static String metadataPath;
    private static String materialPath;    
    private static String restAPIBaseURL;
    private static String webAppBaseURL;
    private static String searchIndexPath;


    // new 25-11-2020, for issue #22
    static Set<String> FREE_DATA = new HashSet<>();  // for issue #22

    static IDList corpusIDs = new IDList("corpus"); //ids of corpora to be indexed
    static IDList speakerBasedIndexIDs = new IDList("speakerBasedIndexIDs"); //names of indexes for speaker based search
    static IDList transcriptBasedIndexIDs = new IDList("transcriptBasedIndexIDs"); //names of indexes for transcript based search
        
    static {
        read();
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
    
    public static String getQuantificationPath(){
        return quantificationPath;
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

            backendInterfaceClassPath = config.getString("backend[@classPath]");
            System.out.println("backendInterfaceClassPath: " + backendInterfaceClassPath );
            
            germanetPath = config.getString("backend.germanet-path");
            wordlistPath = config.getString("backend.wordlist-path");
            mediaPath = config.getString("backend.media-path");
            mediaArchivePath = config.getString("backend.media-archive-path");
            mediaDistributionPath = config.getString("backend.media-distribution-path");
            mediaSnippetsPath = config.getString("backend.media-snippets-path");
            ffmpegPath = config.getString("backend.ffmpeg-path");
            
            metadataPath = config.getString("backend.metadata-path");
            materialPath = config.getString("backend.material-path");            
            restAPIBaseURL = config.getString("backend.rest-api-base-url");
            webAppBaseURL = config.getString("backend.web-app-base-url");
            transcriptPath = config.getString("backend.transcript-path");
            quantificationPath = config.getString("backend.quantification-path");
            protocolPath = config.getString("backend.protocol-path");
            
            searchIndexPath = config.getString("backend.search-index-path");
            
            String[] corpora = config.getString("backend.corpus-ids-for-indexing").split(";");
            Arrays.sort(corpora);
            corpusIDs.addAll(Arrays.asList(corpora));
            
            config.getList("backend.search-index-speaker-based").forEach((o) -> {
                speakerBasedIndexIDs.add(o.toString());
            });
             
            config.getList("backend.search-index-transcript-based").forEach((o) -> {
                transcriptBasedIndexIDs.add(o.toString());
            });
            
            config.getList("backend.free-data.free-path").forEach((o) -> {
                FREE_DATA.add(o.toString());
            });
   
        } catch (ConfigurationException ex){
           Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
