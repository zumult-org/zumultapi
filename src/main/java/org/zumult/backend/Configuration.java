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
import org.zumult.io.Constants;
import org.zumult.objects.IDList;


/**
 *
 * @author Thomas_Schmidt, Elena Frick
 */
public class Configuration {
    
    // 2024-01-11 : for issue #169
    // This is the name of the environment variable which holds the path
    // to the configuration file. It has to be set via the appropriate system
    // command, i.e.
    // - setx ZUMULT_CONFIG_PATH c:\mypath\zumult-configuration.xml \m (on Windows)
    // - export ZUMULT_CONFIG_PATH c:\mypath\zumult-configuration.xml (on Linux)
    static final String SYS_ENV_KEY = "ZUMULT_CONFIG_PATH";
    
    
    // 2024-01-11 : get rid of this for issue #169
    //static String PATH = "/org/zumult/backend/Configuration.xml";
        
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
    
    // new for #174
    private static String isoTei2HtmlStylesheet;
    // new for #175
    private static String event2HtmlStylesheet;
    private static String speechEvent2HtmlStylesheet;
    private static String speaker2HtmlStylesheet;
    private static String eventTitleMetadataKey;


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
    
    // new for #174
    public static String getIsoTei2HTMLStylesheet(){
        if (isoTei2HtmlStylesheet!=null){
            return isoTei2HtmlStylesheet;
        }
        return Constants.ISOTEI2HTML_STYLESHEET2;
    }

    // new for #175
    public static String getEvent2HTMLStylesheet(){
        if (event2HtmlStylesheet!=null){
            return event2HtmlStylesheet;
        }
        return Constants.EVENT2HTML_STYLESHEET;
    }
    
    // new for #175
    public static String getSpeechEvent2HTMLStylesheet(){
        if (speechEvent2HtmlStylesheet!=null){
            return speechEvent2HtmlStylesheet;
        }
        return Constants.SPEECHEVENT2HTML_STYLESHEET;
    }

    // new for #175
    public static String getSpeaker2HTMLStylesheet(){
        if (speaker2HtmlStylesheet!=null){
            return speaker2HtmlStylesheet;
        }
        return Constants.SPEAKER2HTML_STYLESHEET;
    }
    
    // new for #175
    public static String getEventTitleMetadataKey(){
        if (eventTitleMetadataKey!=null){
            return eventTitleMetadataKey;
        }
        return Constants.EVENT_TITLE_METADATAKEY;
    }
    
    static XMLConfiguration config;
    
    // new for #192
    public static String getConfigurationVariable(String variableName){
        return config.getString("backend." + variableName);
    } 
    
    


    static void read(){
        
        Configurations configs = new Configurations();
        try{
            // 2024-01-11 : changed for issue #169
            String PATH = System.getenv(SYS_ENV_KEY);
            if (PATH==null){
                System.out.println("Could not find an environment variable named ZUMULT_CONFIG_PATH.");
                System.out.println("Cannot read ZuMult configuration.");
                System.out.println("Exiting.");
                System.exit(1);
            }
            
            System.out.println("Reading configuration from " + PATH);
            File f = new File(PATH);
            // changed for #192
            //XMLConfiguration config = configs.xml(f.getAbsolutePath());
            config = configs.xml(f.getAbsolutePath());

            backendInterfaceClassPath = config.getString("backend[@classPath]");
            System.out.println("backendInterfaceClassPath: " + backendInterfaceClassPath );
            
            // new for #174
            isoTei2HtmlStylesheet = config.getString("backend.isotei2html-xsl");
            // new for #175
            event2HtmlStylesheet = config.getString("backend.event2html-xsl");
            speechEvent2HtmlStylesheet = config.getString("backend.speechevent2html-xsl");
            speaker2HtmlStylesheet = config.getString("backend.speaker2html-xsl");
            eventTitleMetadataKey = config.getString("backend.event-title-metadatakey");
            
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
