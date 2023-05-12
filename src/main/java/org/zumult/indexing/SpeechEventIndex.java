/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.IOUtilities;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DefaultTokenList;

/**
 *
 * @author thomas.schmidt
 */
public class SpeechEventIndex implements Indexer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new SpeechEventIndex().index();
    }

    BackendInterface backend;
    //String corpusID = "FOLK";
    //String[] corpusIDs = {"FOLK", "GWSS", "DNAM", "ZW--", "DH--"};
    //String[] corpusIDs = {"DH--"};
    String[] corpusIDs = {"FOLK", "GWSS"};
    String OUT = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_PATH;

    @Override
    public void index() throws IOException {
        for (String corpusID : corpusIDs){
            try {
                backend = BackendInterfaceFactory.newBackendInterface();
                Corpus corpus = backend.getCorpus(corpusID);
                Set<MetadataKey> eventMetadataKeys = corpus.getMetadataKeys(ObjectTypesEnum.EVENT);
                Set<MetadataKey> speechEventMetadataKeys = corpus.getMetadataKeys(ObjectTypesEnum.SPEECH_EVENT);

                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                Document document = documentBuilder.newDocument();
                            // root element
                Element root = document.createElement("speech-events");
                document.appendChild(root);



                IDList events4Corpus = backend.getEvents4Corpus(corpusID);
                for (String eventID : events4Corpus){
                    System.out.println(eventID);
                    Event event = backend.getEvent(eventID);
                    IDList speechEvents4Event = event.getSpeechEvents();
                    for (String speechEventID : speechEvents4Event){
                        Element speechEventE = document.createElement("speech-event");                
                        SpeechEvent speechEvent = backend.getSpeechEvent(speechEventID);

                        IDList mediaIDs = backend.getAudios4SpeechEvent(speechEventID);
                        double totalDuration = 0;
                        for (String mediaID : mediaIDs){
                            totalDuration+=backend.getMedia(mediaID, Media.MEDIA_FORMAT.WAV).getDuration();
                        }

                        speechEventE.setAttribute("id", speechEventID);

                        Element durationKeyElement = document.createElement("key");                
                        durationKeyElement.setAttribute("id", "e_se_duration");
                        durationKeyElement.setTextContent(Double.toString(totalDuration));
                        speechEventE.appendChild(durationKeyElement);


                        for (MetadataKey eventMetadataKey : eventMetadataKeys){
                            String value = event.getMetadataValue(eventMetadataKey);
                            //speechEventE.setAttribute(eventMetadataKey.getID(), value);
                            Element keyElement = document.createElement("key");                
                            keyElement.setAttribute("id", eventMetadataKey.getID());
                            keyElement.setTextContent(value);
                            speechEventE.appendChild(keyElement);

                        }

                        for (MetadataKey speechEventMetadataKey : speechEventMetadataKeys){
                            String value = speechEvent.getMetadataValue(speechEventMetadataKey);
                            Element keyElement = document.createElement("key");                
                            keyElement.setAttribute("id", speechEventMetadataKey.getID());
                            keyElement.setTextContent(value);
                            speechEventE.appendChild(keyElement);
                        }

                        IDList m = speechEvent.getMedia();
                        boolean hasVideo = false;
                        for (String id : m){
                            hasVideo = hasVideo || id.contains("_V_");
                        }
                        speechEventE.setAttribute("video", Boolean.toString(hasVideo));


                        // get IDs for all transcripts belonging to the current speech event
                        IDList transcriptIDs = backend.getTranscripts4SpeechEvent(speechEventID);
                        // make a new lemma list for this speech even
                        TokenList lemmaList4SpeechEvent = new DefaultTokenList("lemma");
                        TokenList wordList4SpeechEvent = new DefaultTokenList("word");
                        for (String transcriptID : transcriptIDs){
                            // get the transcript...
                            Transcript transcript = backend.getTranscript(transcriptID);
                            // ... and get its lemma list, applying the filter defined above
                            TokenList lemmaList4Transcript = transcript.getTokenList("lemma");
                            TokenList wordList4Transcript = transcript.getTokenList("transcription");
                            // merge this transcript's lemma list with the lemmalist for the entire speech event
                            lemmaList4SpeechEvent = lemmaList4SpeechEvent.merge(lemmaList4Transcript);
                            wordList4SpeechEvent = wordList4SpeechEvent.merge(wordList4Transcript);
                        }
                        // how many types do we have in the original lemma list for the speech event?
                        int lemma_types = lemmaList4SpeechEvent.getNumberOfTypes();        
                        int lemma_tokens = lemmaList4SpeechEvent.getNumberOfTokens();

                        int word_types = wordList4SpeechEvent.getNumberOfTypes();        
                        int word_tokens = wordList4SpeechEvent.getNumberOfTokens();

                        //System.out.println(speechEventID + ": " + lemma_types + " " + lemma_tokens + " " + word_types + " " + word_tokens);

                        Element keyElement1 = document.createElement("key");
                        keyElement1.setAttribute("id", "measure_word_tokens");
                        keyElement1.setTextContent(Integer.toString(word_tokens));
                        speechEventE.appendChild(keyElement1);                    

                        Element keyElement2 = document.createElement("key");
                        keyElement2.setAttribute("id", "measure_word_types");
                        keyElement2.setTextContent(Integer.toString(word_types));
                        speechEventE.appendChild(keyElement2);                    

                        Element keyElement3 = document.createElement("key");
                        keyElement3.setAttribute("id", "measure_lemma_tokens");
                        keyElement3.setTextContent(Integer.toString(lemma_tokens));
                        speechEventE.appendChild(keyElement3);                    

                        Element keyElement4 = document.createElement("key");
                        keyElement4.setAttribute("id", "measure_lemma_types");
                        keyElement4.setTextContent(Integer.toString(lemma_types));
                        speechEventE.appendChild(keyElement4);                    

                        root.appendChild(speechEventE);
                    }


                }
                String xmlString = IOUtilities.documentToString(document);
                //System.out.println(xmlString);
                //String OUT = "D:\\WebApplication3\\src\\java\\data\\" + corpusID + "_SpeechEventIndex.xml";
                String OUTPUT = OUT  + corpusID + "_SpeechEventIndex.xml";
                Files.write(new File(OUTPUT).toPath(), xmlString.getBytes("UTF-8"));

                

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ParserConfigurationException ex) {
                Logger.getLogger(SpeechEventIndex.class.getName()).log(Level.SEVERE, null, ex);
                throw new IOException(ex);
            }
        }
    }

    
}
