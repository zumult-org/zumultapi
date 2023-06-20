package org.zumult.indexing.metadata;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.Configuration;
import org.zumult.indexing.Indexer;
import org.zumult.io.Constants;
import org.zumult.io.IOUtilities;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.implementations.ISOTEITranscript;

/**
 *
 * @author  Elena Frick
 * 
 * This sciript creates _QUANT.xml files for the specified corpora. 
 * It is based on QuantifyCorporaForDGD.java (written by Thomas Schmitt), but works on iso-tei transcripts.
 * 
 */
public class QuantifyCorpora implements Indexer {

    String OUT_DIRECTORY = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH +Constants.DATA_QUANTIFICATIONS_PATH; 
    IDList CORPORA_FOR_QUANTIFICATION = Configuration.getCorpusIDs();

    javax.xml.xpath.XPath myXPath = XPathFactory.newInstance().newXPath();
    
    DocumentBuilder db;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new QuantifyCorpora().index();
        } catch (IOException ex) {
            Logger.getLogger(QuantifyCorpora.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void index() throws IOException {
                       
        try {
            BackendInterface backend = new org.zumult.backend.implementations.AGDFileSystem();
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            
            myXPath.setNamespaceContext(new ISOTEINamespaceContext());
            
            double  totalDurationAll=0;
            double  totalDurationTranscribedEventsOnly=0;
            int     totalCorpusTokensAll=0;
            
            for (String corpus : CORPORA_FOR_QUANTIFICATION){
                int totalCorpusTokens = 0;
                double totalCorpusDuration = 0;
                
                Document resultDocument = db.newDocument();
                Element root = resultDocument.createElement("quantification");
                root.setAttribute("corpus", corpus);

                // Get all metadata fields, grouped by level: e, se, e_se, speaker
                Set<MetadataKey> eventMetadata = backend.getMetadataKeys4Corpus(corpus, ObjectTypesEnum.EVENT);
                Set<MetadataKey> speechEventMetadata = backend.getMetadataKeys4Corpus(corpus, ObjectTypesEnum.SPEECH_EVENT);
                Set<MetadataKey> speakerInSpeechEventMetadata = backend.getMetadataKeys4Corpus(corpus, ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT);
                Set<MetadataKey> speakerMetadata = backend.getMetadataKeys4Corpus(corpus, ObjectTypesEnum.SPEAKER);
       
                // Go over all event names
                IDList events = backend.getEvents4Corpus(corpus);    
                for (String event : events){
                        
                    System.out.println("\n"+event);

                    IDList speechEvents = backend.getSpeechEvents4Event(event);
  
                    // Create output element for next event to be written to quant xml
                    // This elem will receive an audio-duration and tokens attribute as the aggregate of
                    // all its speechEvent children
                    Element eventElement = resultDocument.createElement("event");
                        
                    // These will be written to event element
                    double totalDurationForEvent = 0.0;
                    // This might be 0 if event does not contains any transcribed speechEvents
                    int totalTokenCountForEvent = 0;

                    // Go over all speech events for current event in event xml.
                    // *All* will have a duration, but only *some* will have tokens
                    for (String speechEventID : speechEvents){
                        System.out.println("  "+speechEventID);
                        HashMap<String, Integer> speakerTokenCounts = new HashMap<>();
                        // Create output speechEvent element as child of current event element
                        Element speechEventElement = resultDocument.createElement("speechEvent");
                        // Append speechEvent to event
                        eventElement.appendChild(speechEventElement);
                        speechEventElement.setAttribute("id", speechEventID);

                        // AUDIO
                        IDList audioFiles = backend.getAudios4SpeechEvent(speechEventID);

                        double totalDuration = 0.0;
                        for(String audioID:audioFiles)  {                         

                        // ?? Why exclude ZW _A_02_?? --> Some ZW events contain >1 wav, which seem to be alternatives

                            //audioPlayer.setSoundFile(f.getAbsolutePath());
                            //totalDuration+=audioPlayer.getTotalLength();

                            Media audio = backend.getMedia(audioID, Media.MEDIA_FORMAT.WAV);
                            double currDuration=audio.getDuration();
                            totalDuration+=currDuration;
                            System.out.println(audioID+" with duration "+currDuration);
                            // MCM: For all of DGD
                            totalDurationAll+=currDuration;
                        }
                        // Totalduration is the duration of the current event, regardless of its being transcribed or not
                        speechEventElement.setAttribute("audio-duration", Double.toString(totalDuration));
                        totalCorpusDuration+=totalDuration;
                        totalDurationForEvent+=totalDuration;

                        // TRANSCRIPTS
                        int totalTokenCount = 0;
                        HashSet<String> types = new HashSet<>();

                        IDList transcriptsFiles = backend.getTranscripts4SpeechEvent(speechEventID);

                        for (String transcriptID : transcriptsFiles){   

                            ISOTEITranscript isoTeiTranscriptionDoc = (ISOTEITranscript) backend.getTranscript(transcriptID);
                            NodeList annotationBlocks = (NodeList)myXPath.evaluate("//tei:annotationBlock", isoTeiTranscriptionDoc.getDocument().getDocumentElement(), XPathConstants.NODESET);

                            for (int i=0; i<annotationBlocks.getLength(); i++){
                                Element annotationBlock = ((Element)(annotationBlocks.item(i)));
                                NodeList words = (NodeList)myXPath.evaluate("descendant::tei:w", annotationBlock, XPathConstants.NODESET);
                                totalTokenCount+=words.getLength(); 
                                totalCorpusTokensAll+=words.getLength();
                                types.addAll(getTypes(words));

                                String sID = isoTeiTranscriptionDoc.getSpeakerIDBySpeakerInitials(annotationBlock.getAttribute("who"));
                                if (!(sID==null) || ("???").equals(sID)){
                                    if (!speakerTokenCounts.containsKey(sID)){
                                        speakerTokenCounts.put(sID, 0);
                                    }
                                    int now = speakerTokenCounts.get(sID);
                                    speakerTokenCounts.put(sID, now + words.getLength());
                                }
                            }
                        }
                        if (totalTokenCount > 0){
                            totalDurationTranscribedEventsOnly+=totalDuration;
                        }

                        speechEventElement.setAttribute("tokens", Integer.toString(totalTokenCount));
                        speechEventElement.setAttribute("types", Integer.toString(types.size()));
                        totalCorpusTokens+=totalTokenCount;
                        totalTokenCountForEvent+=totalTokenCount;

                        // SPRECHEREIGNISSE
                        SpeechEvent speechEvent = backend.getSpeechEvent(speechEventID);

                        for (MetadataKey mk : speechEventMetadata){
                            if(mk.isQuantified()){
                                Element e = resultDocument.createElement("property");
                                e.setAttribute("label", mk.getName("de"));
                                e.setAttribute("dgd-parameter", "v_" + mk.getID());
                                e.setTextContent(speechEvent.getMetadataValue(mk));
                                speechEventElement.appendChild(e);
                            }
                        }

                        // SPRECHER IN SPRECHEREIGNIS
                        IDList speakers = speechEvent.getSpeakers();
                        for (String speakerID: speakers){
                            Element sElement = resultDocument.createElement("speaker");
                            sElement.setAttribute("id", speakerID);
                            if (speakerTokenCounts.containsKey(speakerID)){
                                sElement.setAttribute("tokens", Integer.toString(speakerTokenCounts.get(speakerID)));
                            } else {
                                sElement.setAttribute("tokens", "0");
                            }
                            speechEventElement.appendChild(sElement);

                            for (MetadataKey mk : speakerInSpeechEventMetadata){
                                if(mk.isQuantified()){
                                   Element e = resultDocument.createElement("property");
                                   e.setAttribute("label", mk.getName("de"));
                                   e.setAttribute("dgd-parameter", "v_" + mk.getID());
                                   e.setTextContent(backend.getSpeakerInSpeechEvent(speechEventID,speakerID).getMetadataValue(mk));
                                   sElement.appendChild(e);
                                }
                            }
                        }
                    }

                    int hours = (int) totalDurationForEvent / 3600;
                    int minutes = (int) (totalDurationForEvent % 3600) / 60;
                    int seconds = (int) totalDurationForEvent % 60;
                    String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    System.out.println("Total duration for event: "+totalDurationForEvent);
                    System.out.println(timeString);

                    eventElement.setAttribute("audio-duration", Double.toString(totalDurationForEvent));
                    eventElement.setAttribute("tokens", Integer.toString(totalTokenCountForEvent));
                    eventElement.setAttribute("id", event);
                    System.out.println("Tokens in event: " + totalTokenCountForEvent);

                    // EREIGNISSE
                    // Go over all meta data fields for event-level
                    for (MetadataKey mk : eventMetadata){
                        if(mk.isQuantified()){

                            String parameterName = "v_" + mk.getID();
                            String value = backend.getEvent(event).getMetadataValue(mk);

                            // Special treatment for e_mit_video:
                            if (parameterName.equalsIgnoreCase("v_e_mit_video")){
                                if (value.equalsIgnoreCase("ja")){
                                    value="Video";
                                }else{
                                    value="nur Audio";
                                }
                            }

                            Element e = resultDocument.createElement("property");
                            e.setAttribute("label", mk.getName("de"));
                            e.setAttribute("dgd-parameter", parameterName);
                            e.setTextContent(value);
                            eventElement.appendChild(e);
                        }
                    }

                    root.appendChild(eventElement);
                } // end iteration over events (includes GWSS-filter for events < 319)

                root.setAttribute("totalTokens", Integer.toString(totalCorpusTokens));
                root.setAttribute("totalDuration", Double.toString(totalCorpusDuration));

                // SPRECHER
                IDList speakersForCorpus = backend.getSpeakers4Corpus(corpus);

                for (String speakerID : speakersForCorpus){
                    System.out.println(speakerID);
                    IDList speechEvents4Speaker = backend.getSpeechEvents4Speaker(speakerID);
                    HashSet<String> speakerTypes = new HashSet<>();
                    int totalTokenCount = 0;
                    int validSpeechEventsForSpeaker=0;

                    for (String speechEvent4Speaker : speechEvents4Speaker){
                        System.out.println(speechEvent4Speaker);

                        // The following will only be executed for the current speaker if they appear in at least one valid se
                        validSpeechEventsForSpeaker+=1;

                        IDList transcriptsForSpeaker = backend.getTranscripts4SpeechEvent(speechEvent4Speaker);
                        if (transcriptsForSpeaker!=null){
                            for (String transcriptID : transcriptsForSpeaker){   

                                ISOTEITranscript isoTeiTranscriptionDoc = (ISOTEITranscript) backend.getTranscript(transcriptID);
                                NodeList words = (NodeList)myXPath.evaluate("//tei:w[ancestor::tei:annotationBlock[@who='" + isoTeiTranscriptionDoc.getSpeakerInitialsBySpeakerID(speakerID) + "']]", isoTeiTranscriptionDoc.getDocument().getDocumentElement(), XPathConstants.NODESET);

                                totalTokenCount+=words.getLength();
                                speakerTypes.addAll(getTypes(words));
                            }
                        }
                    }

                    // Here, either validSpeechEventsForSpeaker is 0 or
                    // validSpeechEventsForSpeaker and speechEventsForSpeaker.size() are identical
                    System.out.println(validSpeechEventsForSpeaker+" -- "+speechEvents4Speaker.size() );
                    if (validSpeechEventsForSpeaker >0){
                        assert validSpeechEventsForSpeaker == speechEvents4Speaker.size();
                    }

                    // If validspeecheventsforspeaker is 0, the current speaker does not have se that was *not* filtered
                    // So ignore it in speaker count
                    Element speakerElement = resultDocument.createElement("speaker");
                    speakerElement.setAttribute("id", speakerID);
                    speakerElement.setAttribute("speechEvents", Integer.toString(speechEvents4Speaker.size()));
                    speakerElement.setAttribute("tokens", Integer.toString(totalTokenCount));
                    speakerElement.setAttribute("types", Integer.toString(speakerTypes.size()));

                    for (MetadataKey mk : speakerMetadata){
                        if(mk.isQuantified()){

                            String parameterName = "v_" + mk.getID();
                            String value = backend.getSpeaker(speakerID).getMetadataValue(mk);

                            // Special treatment for v_s_geb_jahr:
                            if (parameterName.equalsIgnoreCase("v_s_geb_jahr")){
                                value=value.substring(0,4);
                            }

                            Element e = resultDocument.createElement("property");
                            e.setAttribute("label", mk.getName("de"));
                            e.setAttribute("dgd-parameter", parameterName);
                            e.setTextContent(value);
                            speakerElement.appendChild(e);
                        }
                    }

                    if (validSpeechEventsForSpeaker!=0){
                        // Include speaker node in quant only if speaker has at least one se ot filtered out
                        root.appendChild(speakerElement);
                    }
                }

                File xmlFile = new File(new File(OUT_DIRECTORY), corpus + "_QUANT.xml");

                resultDocument.appendChild(root);
                String xmlString = IOUtilities.documentToString(resultDocument);
                Files.write(Paths.get(xmlFile.getPath()), xmlString.getBytes("UTF-8"));
            }
            
            int hours = (int) totalDurationAll / 3600;
            int minutes = (int) (totalDurationAll % 3600) / 60;
            int seconds = (int) totalDurationAll % 60;
            int totalAccountedSeconds = (hours*3600)+(minutes*60)+seconds;
            double milliRest=totalDurationAll-totalAccountedSeconds;
            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            System.err.println("ALL duration: "+ totalDurationAll);
            System.out.println(timeString+" "+milliRest);
            
            System.err.println("Transcribed duration: "+totalDurationTranscribedEventsOnly);
            System.err.println("ALL tokens: "+ totalCorpusTokensAll);
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(QuantifyCorpora.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private HashSet<String> getTypes(NodeList words) {
        HashSet<String> result = new HashSet<>();
        for (int i=0; i<words.getLength(); i++){
            Element w = ((Element)(words.item(i)));
            result.add(w.getTextContent());
        }
        return result;
    }
}
