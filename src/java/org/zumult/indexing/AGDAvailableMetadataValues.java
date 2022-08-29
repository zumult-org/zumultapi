/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zumult.backend.BackendInterface;
import org.zumult.io.IOUtilities;
import org.zumult.objects.Corpus;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Thomas.Schmidt
 */
public class AGDAvailableMetadataValues implements Indexer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new AGDAvailableMetadataValues().index();
        } catch (IOException ex) {
            Logger.getLogger(AGDAvailableMetadataValues.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //String METADATA_SELECTION = "/data/MetadataSelection.xml";
    String FILE_NAME = "AGDAvailableMetadataValues.xml";  
    String OUTPUT = System.getProperty("user.dir") + "/src/java/data/" + FILE_NAME;

    @Override
    public void index() throws IOException {        
        try {
            BackendInterface backend = new org.zumult.backend.implementations.AGDFileSystem();
            IDList corpora = backend.getCorpora();
                       
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            
            // root element
            Element root = document.createElement("available-values");
            document.appendChild(root);
            
            
            for (String corpusID : corpora){
                System.out.println("Indexing corpus " + corpusID);
                Element corpusE = document.createElement("corpus");
                corpusE.setAttribute("corpus", corpusID);
                root.appendChild(corpusE);
                Corpus corpus = backend.getCorpus(corpusID);

                IDList eventIDs = backend.getEvents4Corpus(corpusID);
                Set<MetadataKey> eventMetadataKeys = corpus.getEventMetadataKeys();
                for (MetadataKey eventMetadataKey : eventMetadataKeys){
                    System.out.println("   Key: " + eventMetadataKey.getName("de"));
                    Map<String, Integer> mapForThisKey = new HashMap<>();
                    for (String eventID : eventIDs){
                        String value = backend.getEvent(eventID).getMetadataValue(eventMetadataKey);
                        if (eventMetadataKey.getValueClass()==String[].class){
                            for (String singleValue : (value + " ; ").split(" ; ")){
                                increaseFreq(mapForThisKey, singleValue);
                            }
                        } else {
                            increaseFreq(mapForThisKey, value);
                        }
                    }
                    Element keyE = makeKeyElement(document, eventMetadataKey, mapForThisKey);
                    corpusE.appendChild(keyE);
                }
                
                Set<MetadataKey> speechEventMetadataKeys = corpus.getSpeechEventMetadataKeys();
                for (MetadataKey speechEventMetdataKey : speechEventMetadataKeys){
                    System.out.println("   Key: " + speechEventMetdataKey.getName("de"));
                    Map<String, Integer> mapForThisKey = new HashMap<>();
                    for (String eventID : eventIDs){
                        IDList speechEventIDs = backend.getEvent(eventID).getSpeechEvents();
                        for (String speechEventID : speechEventIDs){
                            String value = backend.getSpeechEvent(speechEventID).getMetadataValue(speechEventMetdataKey);
                            if (speechEventMetdataKey.getValueClass()==String[].class){
                                for (String singleValue : (value + " ; ").split(" ; ")){
                                    increaseFreq(mapForThisKey, singleValue);
                                }
                            } else {
                                increaseFreq(mapForThisKey, value);
                            }
                        }
                    }
                    Element keyE = makeKeyElement(document, speechEventMetdataKey, mapForThisKey);
                    corpusE.appendChild(keyE);
                }


                Set<MetadataKey> speakerInSpeechEventMetadataKeys = corpus.getSpeakerInSpeechEventMetadataKeys();
                for (MetadataKey speakerInSpeechEventMetdataKey : speakerInSpeechEventMetadataKeys){
                    System.out.println("   Key: " + speakerInSpeechEventMetdataKey.getName("de"));
                    Map<String, Integer> mapForThisKey = new HashMap<>();
                    for (String eventID : eventIDs){
                        IDList speechEventIDs = backend.getEvent(eventID).getSpeechEvents();
                        for (String speechEventID : speechEventIDs){
                            IDList speakersInSpeechEvent = backend.getSpeechEvent(speechEventID).getSpeakers();
                            for (String speakerInSpeechEvent : speakersInSpeechEvent){
                                String value = backend.getSpeakerInSpeechEvent(speechEventID, speakerInSpeechEvent).getMetadataValue(speakerInSpeechEventMetdataKey);
                                if (speakerInSpeechEventMetdataKey.getValueClass()==String[].class){
                                    for (String singleValue : (value + " ; ").split(" ; ")){
                                        increaseFreq(mapForThisKey, singleValue);
                                    }
                                } else {
                                    increaseFreq(mapForThisKey, value);
                                }
                            }
                        }
                    }
                    Element keyE = makeKeyElement(document, speakerInSpeechEventMetdataKey, mapForThisKey);
                    corpusE.appendChild(keyE);
                }


                IDList speakerIDs = backend.getSpeakers4Corpus(corpusID);
                Set<MetadataKey> speakerMetadataKeys = corpus.getSpeakerMetadataKeys();
                for (MetadataKey speakerMetadataKey : speakerMetadataKeys){
                    System.out.println("   Key: " + speakerMetadataKey.getName("de"));
                    Map<String, Integer> mapForThisKey = new HashMap<>();
                    for (String speakerID : speakerIDs){
                        String value = backend.getSpeaker(speakerID).getMetadataValue(speakerMetadataKey);
                        if (speakerMetadataKey.getValueClass()==String[].class){
                            for (String singleValue : (value + " ; ").split(" ; ")){
                                increaseFreq(mapForThisKey, singleValue);
                            }
                        } else {
                            increaseFreq(mapForThisKey, value);
                        }
                    }
                    Element keyE = makeKeyElement(document, speakerMetadataKey, mapForThisKey);
                    corpusE.appendChild(keyE);
                }
                
                
            }
            String xmlString = IOUtilities.documentToString(document);
            System.out.println(xmlString);
            
            //Files.write(new File(OUT).toPath(), xmlString.getBytes("UTF-8"));
            
            String path = new File(OUTPUT).getPath();
            Files.write(Paths.get(path), xmlString.getBytes("UTF-8"));
            System.out.println(FILE_NAME + " is written to " + path);
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(AGDAvailableMetadataValues.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    private Element makeKeyElement(Document document, MetadataKey metadataKey, Map<String, Integer> mapForThisKey) {
        Element keyE = document.createElement("key");
        keyE.setAttribute("id", metadataKey.getID());
        keyE.setAttribute("name", metadataKey.getName("de"));
        for (String value : mapForThisKey.keySet()){
            Element valueE = document.createElement("value");
            valueE.setTextContent(value);
            valueE.setAttribute("freq", Integer.toString(mapForThisKey.get(value)));
            keyE.appendChild(valueE);
        }
        return keyE;
    }

    private void increaseFreq(Map<String, Integer> map, String value) {
        if (!(map.containsKey(value))){
            map.put(value, 0);
        }
        map.put(value, map.get(value)+1);                                                            
    }
    
}
