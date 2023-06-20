/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.search;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;
import org.zumult.io.Constants;
import org.zumult.io.FileIO;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.Transcript;

/**
 *
 * @author Elena
 */
public class ISOTEI2TranscriptBasedFormat extends ISOTEITransformer {
    
    public static void main(String[] args) {        
        try {
            
            corpusIDsForIndexing = new HashSet<>(Arrays.asList("GWSS"));
            DIR_IN = "C:\\Users\\Frick\\IDS\\ZuMult\\data\\input"; //iso-tei transcripts
            DIR_OUT = "C:\\Users\\Frick\\IDS\\ZuMult\\data\\lucene_9_daten_für_neue_indizes\\output_TB_GWSS_31_05_2023";
            
            new ISOTEI2TranscriptBasedFormat().doit();
            
        } catch (IOException | JDOMException ex) {
            Logger.getLogger(ISOTEI2TranscriptBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ISOTEI2TranscriptBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void transformAndSaveTranscript(File f, Transcript t, Set<MetadataKey> metadataKeys, 
            File newFileTranscriptBased) throws IOException, JDOMException{
        
        try {
            Document transcriptDoc = FileIO.readDocumentFromString(t.toXML());
            
            String fileName = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_IDNO, ns).getText() + ".xml";
            if(f!=null){
                fileName = f.getName();
            }

            // get event and speech event
            String transcriptID = null;
            String speechEventID = null;
            SpeechEvent speechEvent = null;
            String eventID = null;
            Event event = null;
            IDList videos = null;
            
            if(ADD_METADATA){
                transcriptID = t.getID();
                speechEventID = backendInterface.getSpeechEvent4Transcript(transcriptID);
                speechEvent = backendInterface.getSpeechEvent(speechEventID);
                eventID = backendInterface.getEvent4SpeechEvent(speechEventID);
                event = backendInterface.getEvent(eventID);
            }
            
            if(ADD_VIDEOS_NUMBER){
                videos = backendInterface.getVideos4Transcript(transcriptID);  
            }
            
            // get body element
            Element body = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns).getChild(Constants.ELEMENT_NAME_BODY, ns);
            
            // find out the first and last token id
            String firstTokenId = null;
            String lastTokenId = null;
            String lastElementId = null;
            String firstTimeObject = null;
            String lastTimeObject = null;
            
            Iterator allBodyChildrenIterator = body.getChildren().iterator();
            searchForFirstElement:
            while(allBodyChildrenIterator.hasNext()){
                Element firstElement = (Element) allBodyChildrenIterator.next();
                firstTimeObject = firstElement.getAttributeValue(Constants.ATTRIBUTE_NAME_START);
                
                if (firstElement.getName().equals(Constants.ELEMENT_NAME_ANNOTATION_BLOCK)){
                    Iterator iterator = firstElement.getChild(Constants.ELEMENT_NAME_U, ns).getChild(Constants.ELEMENT_NAME_SEG, ns).getChildren().iterator();
                    while(iterator.hasNext()){
                        Element child = (Element) iterator.next();
                        if (!child.getName().equals(Constants.ELEMENT_NAME_ANCHOR)){
                            firstTokenId = child.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
                            break searchForFirstElement;
                        }
                    }
                    
                }else if (firstElement.getName().equals(Constants.ELEMENT_NAME_PAUSE) ||
                        firstElement.getName().equals(Constants.ELEMENT_NAME_INCIDENT) ||
                        firstElement.getName().equals(Constants.ELEMENT_NAME_VOCAL)){
                    firstTokenId =  firstElement.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
                    break;
                }
                
            }
            
            List allBodyChildren = body.getChildren();
            int size = body.getChildren().size();
            
            while (size>0){
                size = size-1;
                Element lastElement = (Element) allBodyChildren.get(size);
                lastTimeObject = lastElement.getAttributeValue(Constants.ATTRIBUTE_NAME_END);
                if (lastElement.getName().equals(Constants.ELEMENT_NAME_ANNOTATION_BLOCK)){
                    List segments = lastElement.getChild(Constants.ELEMENT_NAME_U, ns).getChildren();
                    Element lastSeg = (Element) segments.get(segments.size()-1);
                    List tokens = lastSeg.getChildren();
                    for (int j=tokens.size(); j>0; j--){
                        Element lastToken = (Element) tokens.get(j - 1);
                        if (!lastToken.getName().equals(Constants.ELEMENT_NAME_ANCHOR)){
                            lastTokenId = lastToken.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
                            lastElementId = lastElement.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
                            j=0;
                            size=0;
                        }
                    }
                    
                    
                }else if (lastElement.getName().equals(Constants.ELEMENT_NAME_PAUSE) ||
                        lastElement.getName().equals(Constants.ELEMENT_NAME_INCIDENT) ||
                        lastElement.getName().equals(Constants.ELEMENT_NAME_VOCAL)){
                    lastTokenId =  lastElement.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
                    lastElementId = lastTokenId;
                    size=0;
                }
            }
            
            /*   System.out.println("firstTokenId: " + firstTokenId);
            System.out.println("lastTokenId: " + lastTokenId);
            System.out.println("firstTimeObject: " + firstTimeObject);
            System.out.println("lastTimeObject: " + lastTimeObject);
            System.out.println("lastElementId: " + lastElementId);*/
            
            
            // get the first time variable
            Element timeline = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns).getChild("timeline", ns);
            String firstTimeVariable = getFirstTimeVariable(timeline);
            System.out.println("firstTimeVariable: " + firstTimeVariable);
            String lastTimeVariable = getLastTimeVariable(timeline);
            System.out.println("lastTimeVariable: " + lastTimeVariable);
            
            if(ADD_PROXY_PAUSES){
                // add proxy pause  <pause start="TLI_0" end="TLI_8" class="proxy" xml:id="x1" dur="6.37"/>
                addProxyPause(body, transcriptDoc, lastElementId, firstTimeVariable, lastTimeVariable, firstTimeObject, lastTimeObject);
            }
            
            // change pause duration in the body element
            if(FORMAT_PAUSE_DURATION){
                formatPauseDuration(body);
            }

            if(ADD_TOKEN_START_AND_END){
                System.out.println("Adding token start/end elements ...");
                Iterator annotationBlocksInterator = body.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).iterator();
                while (annotationBlocksInterator.hasNext()){
                    Element annotationBlock = (Element) annotationBlocksInterator.next();
                    setTokenStartEnd(annotationBlock);
                }
            }
            
            if (ADD_DIFF_NORM){
                System.out.println("Checking deviations between transcribed and normalised form  ...");
                Iterator annotationBlocksInterator = body.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).iterator();
                while (annotationBlocksInterator.hasNext()){
                    Element annotationBlock = (Element) annotationBlocksInterator.next();
                    addDiffNorm(annotationBlock);
                }
            }
                                        
            if(ADD_SPEAKER_OVERLAPS){
                System.out.println("Adding speaker overlaps ...");
                Iterator annotationBlocksInterator = body.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).iterator();
                while (annotationBlocksInterator.hasNext()){
                    Element annotationBlock = (Element) annotationBlocksInterator.next();
                    addOverlaps(annotationBlock, timeline, body);
                }
            }
            
            if (ADD_OCCURRENCE){
                System.out.println("Adding token occurence ...");
                Iterator annotationBlocksInterator = body.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).iterator();
                while (annotationBlocksInterator.hasNext()){
                    Element annotationBlock = (Element) annotationBlocksInterator.next();
                    addOccurrence(annotationBlock, body);
                }
            }
                    
                
            
            // create new body-element for the transcript-based search
            Element newBodyForTranscriptBasedView = (Element) body.clone();
            
            if(ADD_METADATA){
                addMetadata(transcriptDoc, metadataKeys, event, speechEvent, newBodyForTranscriptBasedView,
                        firstTimeVariable, lastTimeVariable, eventID, speechEventID, transcriptID, videos);
            }
                       
            // add repetitions
            if(ADD_REPETITIONS){
                addRepetitions(newBodyForTranscriptBasedView);
            }
            
            /************************ Create the document for Transcript-based search ***********************************/
            
            Document newTranscriptDoc = (Document) transcriptDoc.clone();
            Element text = newTranscriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns);
            text.removeChild(Constants.ELEMENT_NAME_BODY, ns);
            text.addContent(newBodyForTranscriptBasedView);
            
            File outFile = new File(newFileTranscriptBased, fileName);
            FileIO.writeDocumentToLocalFile(outFile.getPath(), newTranscriptDoc);
            System.out.println(outFile.getAbsolutePath() + " written");
        } catch (Exception ex) {
            Logger.getLogger(ISOTEI2TranscriptBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void addProxyPause(Element body, Document transcriptDoc, String lastElementId, 
            String firstTimeVariable, String lastTimeVariable, String firstTimeObject, String lastTimeObject) throws JDOMException{
        
        if (!firstTimeVariable.equals(firstTimeObject)){
            System.out.println("Adding proxy pause at the beginning...");
            Element proxyPause = new Element (Constants.ELEMENT_NAME_PAUSE, ns);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_START, firstTimeVariable);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_END, firstTimeObject);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_CLASS, Constants.ATTRIBUTE_VALUE_PROXY);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_ID, "x0", xml); // adding new id
            body.addContent(0, proxyPause);
        }
        
        if (!lastTimeVariable.equals(lastTimeObject)){
            System.out.println("Adding proxy pause at the end...");
            Element proxyPause = new Element (Constants.ELEMENT_NAME_PAUSE, ns);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_START, lastTimeObject);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_END, lastTimeVariable);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_CLASS, Constants.ATTRIBUTE_VALUE_PROXY);
            proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_ID, "x1", xml); // adding new id
            
            String xpathString = "//*[@xml:id='" + lastElementId + "']";
            XPath xpath = XPath.newInstance(xpathString);
            xpath.addNamespace(ns);
            Element el = (Element) xpath.selectSingleNode(transcriptDoc);
            int index = body.indexOf(el);
            System.out.println("Adding after: " + el.getName() + " at position " + index+1);
        
           // body.addContent(body.getChildren().size()-1, proxyPause); does not work because of line breaks in xml files
            body.addContent(index+1, proxyPause);
        }
    }
    
            
    private void addMetadata(Document transcriptDoc, Set<MetadataKey> metadataKeys, 
            Event event, SpeechEvent speechEvent, Element newBodyForTranscriptBasedView,
            String firstTimeVariable, String lastTimeVariable, 
            String eventID, String speechEventID, String transcriptID, IDList videos) throws IOException{
        
        System.out.println("Adding metadata...");
        
        /******* Add speaker metadata ********/
        
        // iterate through person-elements 
        List persons = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEI_HEADER, ns).getChild(Constants.ELEMENT_NAME_PROFILE_DESC, ns)
                    .getChild(Constants.ELEMENT_NAME_PARTIC_DESC, ns).getChildren(Constants.ELEMENT_NAME_PERSON, ns);              
        Iterator personsIiterator = persons.iterator();
        
        while (personsIiterator.hasNext()) {
            Element personElement = (Element) personsIiterator.next();
            String person = personElement.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
    
            if (personElement.getChild(Constants.ELEMENT_NAME_IDNO,ns) != null){
                            
                // get speaker and speaker in speechEvent
                String speakerId = personElement.getChild(Constants.ELEMENT_NAME_IDNO,ns).getText();
                String speakerIdType = personElement.getChild(Constants.ELEMENT_NAME_IDNO,ns).getAttributeValue(Constants.ATTRIBUTE_NAME_TYPE);
            if(!speakerIdType.equals(Constants.RANDOM_ID)){    
                Speaker speaker = backendInterface.getSpeaker(speakerId);
                            
                Speaker speakerInSpeechEvent = null;
                            
                if (speechEvent!= null){  // Transcripts without speech event metadata will be indexed without speech event metadata
                    speakerInSpeechEvent = backendInterface.getSpeakerInSpeechEvent(speechEvent.getID(), speakerId);
                } 
                
                // Add metadata
                Iterator newBody1Iterator = newBodyForTranscriptBasedView.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).iterator();
                while (newBody1Iterator.hasNext()){
                    Element annotationBlock = (Element) newBody1Iterator.next();
                    if (annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_WHO).equals(person)){         
                                    
                        for (MetadataKey metadataKey : metadataKeys){
                                       
                            if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEAKER)){
                                if (!SPEAKER_METADATA_TO_BE_IGNORED_IN_TRANSCRIPT_BASED_MODE.contains(metadataKey.getID())){
                                    String value = speaker.getMetadataValue(metadataKey);
                                    if (!value.isEmpty() && !METADATA_VALUES_TO_BE_IGNORED.contains(value)){
                                        if (!metadataKey.getID().equals(Constants.METADATA_KEY_SPEAKER_BIRTH_DATE)) {
                                            annotationBlock.setAttribute(metadataKey.getID(), value);
                                        }else if (metadataKey.getID().equals(Constants.METADATA_KEY_SPEAKER_BIRTH_DATE) && !SPEAKER_BIRTH_DATE_TO_BE_IGNORED.contains(value)){
                                            // additional metadata
                                            annotationBlock.setAttribute(Constants.METADATA_KEY_SPEAKER_YEAR_OF_BIRTH, value.substring(0, 4));
                                        }
                                    }
                                }
                                                
                            }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT) && speakerInSpeechEvent!=null){                                          
                                String value = speakerInSpeechEvent.getMetadataValue(metadataKey);
                                if (!value.isEmpty() && !METADATA_VALUES_TO_BE_IGNORED.contains(value)){
                                    if (!(metadataKey.getID().equals(Constants.METADATA_KEY_SPEAKER_BIRTH_AGE) && SPEAKER_BIRTH_AGE_TO_BE_IGNORED.contains(value))){
                                        annotationBlock.setAttribute(metadataKey.getID(), value);
                                    }
                                }
                            }     
                        }
                                    
                        // additional metadata
                        annotationBlock.setAttribute(Constants.METADATA_KEY_SPEAKER_DGD_ID, speakerId);

                    }
                }
            }
                            
            }
                           
        }
                    
                    
        /******* Add event and speech event metadata ********/
        
        Element metaSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        metaSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_META);
        metaSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_SUBTYPE, Constants.SPANGRP_SUBTYPE_TIME_BASED);
        newBodyForTranscriptBasedView.addContent(metaSpanGrp);
                    
        for (MetadataKey metadataKey : metadataKeys){
            String value = null;
            if (metadataKey.getLevel().equals(ObjectTypesEnum.EVENT)){
                value = event.getMetadataValue(metadataKey);  
            }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEECH_EVENT) && speechEvent!=null){
                value = speechEvent.getMetadataValue(metadataKey);
            }

            if(value!=null && !value.isEmpty() && !METADATA_VALUES_TO_BE_IGNORED.contains(value)){
                addMetadataSpan(metaSpanGrp, metadataKey.getID(), firstTimeVariable, lastTimeVariable, value);
                if (metadataKey.getID().equals(Constants.METADATA_KEY_EVENT_DURATION)){
                    addSecMetadata(metaSpanGrp, firstTimeVariable, lastTimeVariable, value);
                }
            }

        }
                    
        // additional metadata
        addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_EVENT_DGD_ID, firstTimeVariable, lastTimeVariable, eventID);
        addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_TRANSCRIPT_DGD_ID, firstTimeVariable, lastTimeVariable, transcriptID);
        if(speechEvent!=null){
            addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID, firstTimeVariable, lastTimeVariable, speechEventID);
        }
        
        // add number of videos
        if(videos!=null){
            addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_EVENT_NUMBER_VIDEOS, firstTimeVariable, lastTimeVariable, String.valueOf(videos.size()));
        }
        
        // add "Grad der Mündlichkeit" für GWSS
        if (eventID.startsWith("GWSS_")){
            String spontaneity = getSpontaneity(event, speechEventID);            
            addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_SPEECH_EVENT_SPEECH_NOTES, firstTimeVariable, lastTimeVariable, spontaneity);

        }
    }
          
    void addOccurrence(Element annotationBlock, Element body) throws JDOMException{
        Iterator segmentsIterator = annotationBlock.getChild(Constants.ELEMENT_NAME_U, ns).getChildren().iterator();
        while (segmentsIterator.hasNext()) {
            Element seg = (Element) segmentsIterator.next();                    
            Iterator tokenIterator = seg.getChildren(Constants.ELEMENT_NAME_WORD_TOKEN, ns).iterator();            
            while (tokenIterator.hasNext()) {
                Element token = (Element) tokenIterator.next();
                String norm = token.getAttributeValue(Constants.ATTRIBUTE_NAME_NORM);
                String lemma = token.getAttributeValue(Constants.ATTRIBUTE_NAME_LEMMA);
                String textNorm = token.getTextNormalize();
                int norm_count = 0;
                int lemma_count = 0;
                int textNorm_count = 0;
                
                // check occurence
                Iterator allWords = body.getDescendants(new ElementFilter(Constants.ELEMENT_NAME_WORD_TOKEN));
                while(allWords.hasNext()){
                    Element w = (Element) allWords.next();
                    String n = w.getAttributeValue(Constants.ATTRIBUTE_NAME_NORM);
                    String l = w.getAttributeValue(Constants.ATTRIBUTE_NAME_LEMMA);
                    String t = w.getTextNormalize();
                    if (norm.equals(n)){norm_count++;}                   
                    if(lemma.equals(l)){lemma_count++;}
                    if(textNorm.equals(t)){textNorm_count++;}
                }
                
                token.setAttribute("norm_n", String.valueOf(norm_count));
                token.setAttribute("lemma_n", String.valueOf(lemma_count));
                token.setAttribute("word_n", String.valueOf(textNorm_count));
            }
        }
    }
    
    void addOverlaps(Element annotationBlock, Element timeline, Element body) throws JDOMException{
        
        double ownStart = getInterval(annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START), timeline);
        double ownEnd = getInterval(annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END), timeline);
        Element speakerOverlapSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        speakerOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_SPEAKER_OVERLAP);
        speakerOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_SUBTYPE, Constants.SPANGRP_SUBTYPE_TIME_BASED);
                    
        String person = annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_WHO);
        Iterator annotationBlocksInterator = body.getChildren().iterator();
        while (annotationBlocksInterator.hasNext()){
            Element child = (Element) annotationBlocksInterator.next();
            if((child.getName().equals(Constants.ELEMENT_NAME_ANNOTATION_BLOCK) && !child.getAttributeValue(Constants.ATTRIBUTE_NAME_WHO).equals(person)) 
                || child.getName().equals(Constants.ELEMENT_NAME_VOCAL) || child.getName().equals(Constants.ELEMENT_NAME_INCIDENT)){
                
                
                double from = getInterval(child.getAttributeValue(Constants.ATTRIBUTE_NAME_START), timeline);
                double to = getInterval(child.getAttributeValue(Constants.ATTRIBUTE_NAME_END), timeline);

                // check for overlap
                if (from <ownEnd && to>ownStart){
                    // this is an overlap

                    // add <annotationBlock> span to speaker-overlap group
                    Element spanElement = new Element (Constants.ELEMENT_NAME_SPAN, ns);
                    
                    spanElement.setAttribute(Constants.ATTRIBUTE_NAME_FROM, ((from > ownStart)? child.getAttributeValue(Constants.ATTRIBUTE_NAME_START) : annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START)));
                    spanElement.setAttribute(Constants.ATTRIBUTE_NAME_TO, ((to<ownEnd) ? child.getAttributeValue(Constants.ATTRIBUTE_NAME_END) : annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END)));
                    speakerOverlapSpanGrp.addContent(spanElement);
                    
                    if(child.getName().equals(Constants.ELEMENT_NAME_ANNOTATION_BLOCK)){
                        spanElement.setText(child.getAttributeValue(Constants.ATTRIBUTE_NAME_WHO));
                    }
                }  
                
            }
            
        }
        if (speakerOverlapSpanGrp.getChildren().size()>0){
            annotationBlock.addContent(speakerOverlapSpanGrp);
        }
        
    }
    
    String getFirstTimeVariable(Element timeline) {
        List whenElements = timeline.getChildren(Constants.ELEMENT_NAME_WHEN, ns);
        Iterator whenIterator = whenElements.iterator();

        if (whenIterator.hasNext()) {
            Element firstWhen = (Element) whenIterator.next();                                
            return firstWhen.getAttribute(Constants.ATTRIBUTE_NAME_ID, xml).getValue();
        }
        return null;
    }
    
    String getLastTimeVariable(Element timeline) {
        List whenElements = timeline.getChildren(Constants.ELEMENT_NAME_WHEN, ns);
        int size = whenElements.size();
        while (size>0){
            Element lastWhen = (Element) whenElements.get(size-1);
            return lastWhen.getAttribute(Constants.ATTRIBUTE_NAME_ID, xml).getValue();
        }
        return null;
    }

}
