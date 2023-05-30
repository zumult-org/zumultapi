/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.search;

import org.zumult.io.IOUtilities;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
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
public class ISOTEI2SpeakerBasedFormat extends ISOTEITransformer {

    public static void main(String[] args) {        
        try {  
            
            corpusIDsForIndexing = new HashSet<>(Arrays.asList("FOLK"));
            DIR_IN = "C:\\Users\\Frick\\IDS\\ZuMult\\data\\input";  //iso-tei transcripts
            DIR_OUT = "C:\\Users\\Frick\\IDS\\ZuMult\\data\\output_SB_FOLK_14_07_2022";
            
            new ISOTEI2SpeakerBasedFormat().doit();
            
        } catch (IOException | JDOMException ex) {
            Logger.getLogger(ISOTEI2SpeakerBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ISOTEI2SpeakerBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void transformAndSaveTranscript(File f, Transcript t, Set<MetadataKey> metadataKeys, 
            File newFileSpeakerBased) throws IOException, JDOMException{
        
        try {
            Document transcriptDoc = IOUtilities.readDocumentFromString(t.toXML());
            String fileName = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_IDNO, ns).getText() + ".xml";
            if(f!=null){
                fileName = f.getName();
            }
            
            String transcriptID = t.getID();;
            
            // get event and speech event
            String speechEventID = null;
            SpeechEvent speechEvent = null;
            String eventID = null;
            Event event = null;
            IDList videos = null;
            
            if(ADD_METADATA){
                speechEventID = backendInterface.getSpeechEvent4Transcript(transcriptID);
                speechEvent = backendInterface.getSpeechEvent(speechEventID);
                eventID = backendInterface.getEvent4SpeechEvent(speechEventID);
                event = backendInterface.getEvent(eventID);
            }
            
            if(ADD_VIDEOS_NUMBER){
                videos = backendInterface.getVideos4Transcript(transcriptID);  
            }
            
            // find out the start and end time
            Element timeline = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns).getChild("timeline", ns);
            List timelineChildren = timeline.getChildren();
            String transcriptStartVar = ((Element)timelineChildren.get(0)).getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
            String transcriptEndVar = ((Element)timelineChildren.get(timelineChildren.size() - 1)).getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
            
            // get body element
            Element body = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns).getChild(Constants.ELEMENT_NAME_BODY, ns);
            
            // change pause duration in the body element
            if(FORMAT_PAUSE_DURATION){
                formatPauseDuration(body);
            }
            
            if (ADD_TOKEN_START_AND_END || ADD_DIFF_NORM){
                Iterator annotationBlocksInterator = body.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).iterator();
                while (annotationBlocksInterator.hasNext()){
                    Element annotationBlock = (Element) annotationBlocksInterator.next();
                    
                    if(ADD_TOKEN_START_AND_END){
                        System.out.println("Adding token start/end elements ...");
                        setTokenStartEnd(annotationBlock);
                    }

                    if (ADD_DIFF_NORM){
                        System.out.println("Checking deviations between transcribed and normalised form  ...");
                        addDiffNorm(annotationBlock);
                    }
                }
            }
            
            
            
            
            // iterate through person-elements
            List persons = transcriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEI_HEADER, ns).getChild(Constants.ELEMENT_NAME_PROFILE_DESC, ns)
                    .getChild(Constants.ELEMENT_NAME_PARTIC_DESC, ns).getChildren(Constants.ELEMENT_NAME_PERSON, ns);
            Iterator personsIiterator = persons.iterator();
            while (personsIiterator.hasNext()) {
                Element personElement = (Element) personsIiterator.next();
                
                // create new body-element for the speaker-based search
                Element newBodyForSpeakerBasedView = new Element(Constants.ELEMENT_NAME_BODY, ns);
                //String person = personElement.getAttribute(Constants.ATTRIBUTE_NAME_N).getValue();
                String person = personElement.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml);
                newBodyForSpeakerBasedView.setAttribute(Constants.ATTRIBUTE_NAME_WHO, person);
                
                System.out.println("Creating file for " + person + "...");
                
                /**************************** Step 1: transform annotationBlocks *******************************/
                /******* (add all annotationBlocks of the appropriate speaker to the new body-element) *********/
                
                System.out.println("Adding annotationBlocks of the appropriate speaker...");
                
                String pauseStartVar = transcriptStartVar;
                
                int pauseID = 0;
                List bodyChildren = body.getChildren();
                Iterator iterator2 = bodyChildren.iterator();
                boolean speakerStart = false;
                while (iterator2.hasNext()) {
                    Element child = (Element) iterator2.next();
                    if ((child.getName().equals(Constants.ELEMENT_NAME_ANNOTATION_BLOCK) && child.getAttribute(Constants.ATTRIBUTE_NAME_WHO).getValue().equals(person))
                            || (child.getName().equals(Constants.ELEMENT_NAME_PAUSE) && speakerStart)){
                        speakerStart=true;
                        
                        Element newElement = (Element) child.clone();
                        
                        //add proxy pause
                        String elementStartVar = newElement.getAttributeValue(Constants.ATTRIBUTE_NAME_START);
                        String elementEndVar = newElement.getAttributeValue(Constants.ATTRIBUTE_NAME_END);
                        double elementStart = getInterval(elementStartVar, timeline);
                        double pauseStart = getInterval(pauseStartVar, timeline);
                        if (pauseStart < elementStart){
                            double pause_dur = elementStart - pauseStart;
                            pauseID = pauseID+1;
                            addProxyPause(newBodyForSpeakerBasedView, pauseStartVar, elementStartVar, pauseID, pause_dur);
                        }
                        
                        newBodyForSpeakerBasedView.addContent(newElement);
                        pauseStartVar = elementEndVar;
                    }
                }
                
                //add proxy pause at the end
                double pauseStart = getInterval(pauseStartVar, timeline);
                double transcriptEnd = getInterval(transcriptEndVar, timeline);
                if (pauseStart < transcriptEnd){
                    double pause_dur = transcriptEnd - pauseStart;
                    pauseID = pauseID+1;
                    addProxyPause(newBodyForSpeakerBasedView, pauseStartVar, transcriptEndVar, pauseID, pause_dur);
                }
                
                if (newBodyForSpeakerBasedView.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).size()>0){
                    
                    /********************** Step 2: add segments of other speakers *************************************/
                    /******* (add segments of other speakers to the new body-element
                     * as spanGrp type="speaker-overlap", type="token-overlap" and type="another-speaker" ***********/
                    
                    if(ADD_SPEAKER_OVERLAPS){
                        addOverlaps(body, newBodyForSpeakerBasedView, timeline, person);
                    }
                    
                    /********************** Step 3: transform global annotations *************************************/
                    
                    addGlobalSpanAnnotations(body, timeline, newBodyForSpeakerBasedView, person);
                    
                    
                    /********************** Step 4: transform global events (global vocal and incident elements) *************************************/
                    
                    addGlobalElements(bodyChildren, newBodyForSpeakerBasedView, timeline);
                    
                    /********************** Step 5: add event and speaker metadata for Speaker-based search ******************************************/
                    
                    if(ADD_METADATA){
                        addMetadata(metadataKeys, event, speechEvent, newBodyForSpeakerBasedView, personElement,
                                transcriptStartVar, transcriptEndVar, eventID, speechEventID, transcriptID, videos);
                    }
                                
                    
                    /***************Step 6: add repetitions  ************/
                    
                    if(ADD_REPETITIONS){
                        addRepetitions(newBodyForSpeakerBasedView);
                    }
                    
                    /********************** Step 7: save speaker document *************************************/
                    
                    Document newTranscriptDoc = (Document) transcriptDoc.clone();
                    Element text = newTranscriptDoc.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns);
                    text.removeChild(Constants.ELEMENT_NAME_BODY, ns);
                    text.addContent(newBodyForSpeakerBasedView);
                    
                    File outFile = new File(newFileSpeakerBased, person+"_"+fileName);
                    IOUtilities.writeDocumentToLocalFile(outFile.getPath(), newTranscriptDoc);
                    System.out.println(outFile.getAbsolutePath() + " written");
                    
                }else{
                    System.out.println("No Document for " + person+" in "+fileName);
                }
                
                
            } // end of the current speaker 
        } catch (Exception ex) {
            Logger.getLogger(ISOTEI2SpeakerBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    void addOverlaps(Element body, Element newBodyForSpeakerBasedView, Element timeline, String person) throws JDOMException{
        System.out.println("Looking for speaker overlaps...");

        Element speakerOverlapSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        speakerOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_SPEAKER_OVERLAP);
        speakerOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_SUBTYPE, Constants.SPANGRP_SUBTYPE_TIME_BASED);

        Element tokenOverlapSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        tokenOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_TOKEN_OVERLAP);

        Element otherSpeakersGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        otherSpeakersGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_ANOTHER_SPEAKER);
        otherSpeakersGrp.setAttribute(Constants.ATTRIBUTE_NAME_SUBTYPE, Constants.SPANGRP_SUBTYPE_TIME_BASED);

        List annotationBlocks = body.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns);
        Iterator iterator4 = annotationBlocks.iterator();
        while (iterator4.hasNext()) {
            Element child = (Element) iterator4.next(); //<annotationBlock> element
            String personSigle = child.getAttributeValue(Constants.ATTRIBUTE_NAME_WHO);
            System.out.println("Editing annotationBlock " + child.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml));

            if (!personSigle.equals(person)){

                /** add speaker overlap **/
                                    
                double from = getInterval(child.getAttributeValue(Constants.ATTRIBUTE_NAME_START), timeline);
                double to = getInterval(child.getAttributeValue(Constants.ATTRIBUTE_NAME_END), timeline);

                Iterator annoBlockIterator =  newBodyForSpeakerBasedView.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns).iterator();
                while (annoBlockIterator.hasNext()) {
                    Element annoBlock = (Element) annoBlockIterator.next();

                    double ownStart = getInterval(annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START), timeline);
                    double ownEnd = getInterval(annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END), timeline);

                    // check for overlap
                    if (from <ownEnd && to>ownStart){
                       // this is an overlap

                       // add <annotationBlock> span to speaker-overlap group
                        Element spanElement = new Element (Constants.ELEMENT_NAME_SPAN, ns);
                        spanElement.setText(personSigle);
                        spanElement.setAttribute(Constants.ATTRIBUTE_NAME_FROM, ((from > ownStart)? child.getAttributeValue(Constants.ATTRIBUTE_NAME_START) : annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START)));
                        spanElement.setAttribute(Constants.ATTRIBUTE_NAME_TO, ((to<ownEnd) ? child.getAttributeValue(Constants.ATTRIBUTE_NAME_END) : annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END)));
                        speakerOverlapSpanGrp.addContent(spanElement);
                    }  

                }



                                    /** add tokens to token-overlap group **/
                                    
                                   /* Iterator segmentsIterator = child.getChild(ELEMENT_NAME_U, ns).getChildren().iterator();
                                    while (segmentsIterator.hasNext()) {
                                        Element seg = (Element) segmentsIterator.next();
                                        Iterator tokenIterator = seg.getChildren().iterator();

                                        String startStr = null;
                                        String endStr = null;
                                        ArrayList<Element> words = new ArrayList<Element>();
                                        String proxy_start = null;
                                        while (tokenIterator.hasNext()) {
                                            Element token = (Element) tokenIterator.next();

                                            Element word = new Element (ELEMENT_NAME_SPAN, ns);
                                            word.setAttribute(ATTRIBUTE_NAME_CLASS, token.getName());
                                            //word.setAttribute("who", personSigle);
                                            copyAttibute(token, word);

                                            if (token.getName().equals(ELEMENT_NAME_ANCHOR)){
                                                if (words.size()>0){

                                                    endStr = token.getAttributeValue(ATTRIBUTE_NAME_SYNCH);
                                                    //add all Words
                                                    for (int i=words.size(); --i >= 0;){
                                                        Element w2 = words.get(i);

                                                        Iterator overlapIterator = newBodyForSpeakerBasedView.getChildren().iterator();
                                                        while (overlapIterator.hasNext()) {

                                                            Element el = (Element) overlapIterator.next();

                                                            if (el.getName().equals(ELEMENT_NAME_PAUSE) || el.getName().equals(ELEMENT_NAME_ANNOTATION_BLOCK)){

                                                                double ownStart = getInterval(el.getAttributeValue(ATTRIBUTE_NAME_START), timeline);
                                                                double ownEnd = getInterval(el.getAttributeValue(ATTRIBUTE_NAME_END), timeline);
                                                                double start = getInterval(startStr, timeline);

                                                                if (w2.getAttribute(ATTRIBUTE_NAME_PROXY_START)!=null){
                                                                    start = getInterval(w2.getAttributeValue(ATTRIBUTE_NAME_PROXY_START), timeline);
                                                                }

                                                                double end = getInterval(endStr, timeline);

                                                                if (w2.getAttribute(ATTRIBUTE_NAME_PROXY_END)!=null){
                                                                    end = getInterval(w2.getAttributeValue(ATTRIBUTE_NAME_PROXY_END), timeline);
                                                                }
                                                                // check for overlap
                                                                if (start <ownEnd && end>ownStart){

                                                                    w2.setAttribute(ATTRIBUTE_NAME_FROM, ((start > ownStart)? startStr : el.getAttributeValue(ATTRIBUTE_NAME_START)));
                                                                    w2.setAttribute(ATTRIBUTE_NAME_TO, ((end<ownEnd) ? endStr : el.getAttributeValue(ATTRIBUTE_NAME_END)));
                                                                    tokenOverlapSpanGrp.addContent((Content) w2.clone());
                                                                }
                                                            }
                                                        }   
                                                    }
                                                    words.clear();
                                                    proxy_start=null;
                                                }

                                                if (words.isEmpty()){

                                                    startStr = token.getAttributeValue(ATTRIBUTE_NAME_SYNCH);
                                                }
                                            }
                                            else if (token.getName().equals(ELEMENT_NAME_WORD_TOKEN)){
                                                if (proxy_start != null){
                                                   word.setAttribute(ATTRIBUTE_NAME_PROXY_START, proxy_start);
                                                }
                                                if (token.getChildren(ELEMENT_NAME_ANCHOR, ns).size()>0){
                                                    int n=0;
                                                    List anchors =  token.getChildren(ELEMENT_NAME_ANCHOR, ns);
                                                    Iterator anchorIterator = anchors.iterator();
                                                    StringBuilder sb = new StringBuilder();
                                                    while (anchorIterator.hasNext()){
                                                        n= n+1;
                                                        Element anchor = (Element) anchorIterator.next();
                                                        if(n==1){
                                                            for (int i=words.size(); --i >= 0;){
                                                                words.get(i).setAttribute(ATTRIBUTE_NAME_PROXY_END, anchor.getAttributeValue(ATTRIBUTE_NAME_SYNCH));
                                                            }
                                                        }

                                                        if(n==token.getChildren(ELEMENT_NAME_ANCHOR, ns).size()){
                                                            proxy_start = anchor.getAttributeValue(ATTRIBUTE_NAME_SYNCH);
                                                        }

                                                        sb.append(anchor.getAttributeValue(ATTRIBUTE_NAME_SYNCH));
                                                        if (anchorIterator.hasNext()){
                                                            sb.append(" ");
                                                        }
                                                    }
                                                    word.setAttribute(ATTRIBUTE_NAME_SYNCH, sb.toString()); 

                                                }

                                                word.setText(token.getText());
                                                words.add(word);                                           
                                            } else if (token.getName().equals(ELEMENT_NAME_VOCAL) || token.getName().equals(ELEMENT_NAME_INCIDENT)){

                                                List desc = token.getChildren();
                                                if (desc.size() > 1){
                                                    throw new JDOMException(
                                                    "more that one <desc> element" + token.getAttributeValue(ATTRIBUTE_NAME_ID, xml));
                                                }else{
                                                    Element descElement = (Element) desc.get(0);
                                                    String text = descElement.getText();
                                                    word.setText(text);
                                                    copyAttibute(descElement, word);
                                                }

                                                words.add(word);
                                            } else if (token.getName().equals(ELEMENT_NAME_PAUSE)){
                                                words.add(word);
                                            }
                                        }
                                    }


                                    */
                /** add speaker overlaps with proxy pauses to the another-speaker group **/
                List pauses = newBodyForSpeakerBasedView.getChildren(Constants.ELEMENT_NAME_PAUSE, ns);
                Iterator pausesIterator = pauses.iterator();

                while (pausesIterator.hasNext()) {
                    Element pause = (Element) pausesIterator.next();

                    double ownStart = getInterval(pause.getAttributeValue(Constants.ATTRIBUTE_NAME_START), timeline);
                    double ownEnd = getInterval(pause.getAttributeValue(Constants.ATTRIBUTE_NAME_END), timeline);

                    // check for overlap
                    if (from <ownEnd && to>ownStart){
                        // this is an overlap

                        // add <seg> spans to another-speaker group
                        Element newSegElement = new Element (Constants.ELEMENT_NAME_SPAN, ns);
                        newSegElement.setText(personSigle);
                        newSegElement.setAttribute(Constants.ATTRIBUTE_NAME_FROM, ((from > ownStart)? child.getAttributeValue(Constants.ATTRIBUTE_NAME_START) : pause.getAttributeValue(Constants.ATTRIBUTE_NAME_START)));
                        newSegElement.setAttribute(Constants.ATTRIBUTE_NAME_TO, ((to<ownEnd) ? child.getAttributeValue(Constants.ATTRIBUTE_NAME_END) : pause.getAttributeValue(Constants.ATTRIBUTE_NAME_END)));
                        otherSpeakersGrp.addContent(newSegElement);
                    }  
                }
            }
        }
                            
        // add all created span groups to the new body 
        
        if (speakerOverlapSpanGrp.getChildren().size() > 0){
            newBodyForSpeakerBasedView.addContent(speakerOverlapSpanGrp);
        }
        
        if (tokenOverlapSpanGrp.getChildren().size() > 0){
            newBodyForSpeakerBasedView.addContent(tokenOverlapSpanGrp);
        }
        
        if (otherSpeakersGrp.getChildren().size() > 0){
            newBodyForSpeakerBasedView.addContent(otherSpeakersGrp);
        }
        
    }
    
    void addGlobalSpanAnnotations(Element body, Element timeline, Element newBodyForSpeakerBasedView, String person) throws JDOMException{
        System.out.println("Looking for global <spanGrp> elements ...");
        List globalSpanGroups = body.getChildren(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        Iterator globalSpanGrpIterator = globalSpanGroups.iterator();
        while(globalSpanGrpIterator.hasNext()){
            Element globalSpanGrp = (Element) globalSpanGrpIterator.next();
            Element newGlobalSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
                               
            if (globalSpanGrp.getAttributeValue(Constants.ATTRIBUTE_NAME_SUBTYPE)!=null && globalSpanGrp.getAttributeValue(Constants.ATTRIBUTE_NAME_SUBTYPE).equals(Constants.SPANGRP_SUBTYPE_TIME_BASED)){
                                    
                // if time-based annotations -> select overlaps
                newGlobalSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, globalSpanGrp.getAttributeValue(Constants.ATTRIBUTE_NAME_TYPE));
                newGlobalSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_SUBTYPE, globalSpanGrp.getAttributeValue(Constants.ATTRIBUTE_NAME_SUBTYPE));

                List spans = globalSpanGrp.getChildren(Constants.ELEMENT_NAME_SPAN, ns);
                Iterator spanIterator = spans.iterator();
                while (spanIterator.hasNext()){
                    Element span = (Element) spanIterator.next();
                                        
                    // get from and to of the actual span
                    String spanStartVar = span.getAttributeValue(Constants.ATTRIBUTE_NAME_FROM);
                    String spanEndVar = span.getAttributeValue(Constants.ATTRIBUTE_NAME_TO);
                    double spanStart = getInterval(spanStartVar, timeline);
                    double spanEnd = getInterval(spanEndVar, timeline);
                                        
                    // check for overlap
                    String fromVar = null;
                    String toVar = null;

                    List segments = newBodyForSpeakerBasedView.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns);
                    Iterator annoBlockIterator = segments.iterator();
                    while (annoBlockIterator.hasNext()) {
                        Element annoBlock = (Element) annoBlockIterator.next();
                        String annoBlockStartVar = annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START);
                        String annoBlockEndVar = annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END);
                        double annoBlockStart = getInterval(annoBlockStartVar, timeline);
                        double annoBlockEnd = getInterval(annoBlockEndVar, timeline);

                        // check for overlap
                        if (spanStart <annoBlockEnd && spanEnd>annoBlockStart){
                           //this is an overlap
                            if (spanStart >=annoBlockStart && spanEnd<=annoBlockStart){
                                fromVar = spanStartVar;
                                toVar = spanEndVar;
                                break;
                            } else if (spanStart <= annoBlockStart && spanEnd <= annoBlockEnd){
                                if (fromVar == null){
                                    fromVar = annoBlockStartVar;
                                }
                                toVar = spanEndVar;
                                break;
                            }else if (spanStart >=annoBlockStart && spanEnd >=annoBlockEnd){
                                fromVar = spanStartVar;
                                toVar = annoBlockEndVar;
                            }else if (spanStart <= annoBlockStart && spanEnd >=annoBlockEnd){
                                if (fromVar == null){
                                    fromVar = annoBlockStartVar;
                                }
                                toVar=annoBlockEndVar;

                            }                                       
                        }
                    }
                                        
                                        
                    if (fromVar!=null && toVar!=null){
                                            
                        // create new span
                        Element newSpan = new Element(Constants.ELEMENT_NAME_SPAN, ns);
                                           
                        // add text and all attributes
                        newSpan.setText(span.getText());
                        List attributes = span.getAttributes();
                        if (!attributes.isEmpty()) {
                            Iterator iterator = attributes.iterator();
                            while (iterator.hasNext()) {
                                Attribute attribute = (Attribute) iterator.next();
                                String name = attribute.getName();
                                if (!name.equals(Constants.ATTRIBUTE_NAME_FROM) && !name.equals(Constants.ATTRIBUTE_NAME_TO)){
                                    newSpan.setAttribute(name, attribute.getValue());
                                }
                            }
                        }
                                            
                        // add from and to 
                        newSpan.setAttribute(Constants.ATTRIBUTE_NAME_FROM, fromVar);
                        newSpan.setAttribute(Constants.ATTRIBUTE_NAME_TO, toVar);

                                           
                        newGlobalSpanGrp.addContent(newSpan);

                    }
                }
            }else{
              // if word-based annotations -> clone
                newGlobalSpanGrp = (Element) globalSpanGrp.clone();
                                    
                List spans = globalSpanGrp.getChildren(Constants.ELEMENT_NAME_SPAN, ns);
                Iterator spanIterator = spans.iterator();
                while (spanIterator.hasNext()){
                    Element span = (Element) spanIterator.next();
                    if (span.getAttributeValue(Constants.ATTRIBUTE_NAME_WHO)!=null && !span.getAttributeValue(Constants.ATTRIBUTE_NAME_WHO).equals(person)){
                        continue;
                    }else{
                        Element newSpan = (Element) span.clone();
                        newGlobalSpanGrp.addContent(newSpan);
                    }
                }
            }
                                
            if (newGlobalSpanGrp.getChildren().size()>0){
                newBodyForSpeakerBasedView.addContent(newGlobalSpanGrp);
            }
        }
    }
    
    void addGlobalElements(List bodyChildren, Element newBodyForSpeakerBasedView, Element timeline) throws JDOMException{
        System.out.println("Looking for global <vocal> and <incident> elements ...");

        Element CommonEventOverlapSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        CommonEventOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_OVERLAP_WITH_COMMON_EVENTS);
        CommonEventOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_SUBTYPE, Constants.SPANGRP_SUBTYPE_TIME_BASED);

        Iterator globalVocalsIterator = bodyChildren.iterator();
        while(globalVocalsIterator.hasNext()){

            Element child = (Element) globalVocalsIterator.next();
            if (child.getName().equals(Constants.ELEMENT_NAME_VOCAL) || child.getName().equals(Constants.ELEMENT_NAME_INCIDENT)){

                Element newElement = new Element(Constants.ELEMENT_NAME_SPAN, ns);
                       
                String spanStartVar = child.getAttributeValue(Constants.ATTRIBUTE_NAME_START);
                String spanEndVar = child.getAttributeValue(Constants.ATTRIBUTE_NAME_END);

                double spanStart = getInterval(spanStartVar, timeline);
                double spanEnd = getInterval(spanEndVar, timeline);

                // get id
                newElement.setAttribute(Constants.ATTRIBUTE_NAME_ID, child.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml), xml);
                //System.out.println(child.getAttributeValue(ID, xml));

                // get text
                Element desc = child.getChild(Constants.ATTRIBUTE_NAME_DESC, ns);
                newElement.setText(desc.getText());

                String fromVar = null;
                String toVar = null;

                List annoBlocks = newBodyForSpeakerBasedView.getChildren(Constants.ELEMENT_NAME_ANNOTATION_BLOCK, ns);
                Iterator annoBlocksIterator = annoBlocks.iterator();
                while (annoBlocksIterator.hasNext()) {
                    Element annoBlock = (Element) annoBlocksIterator.next();
                    String annoBlockStartVar = annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START);
                    String annoBlockEndVar = annoBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END);
                    double annoBlockStart = getInterval(annoBlockStartVar, timeline);
                    double annoBlockEnd = getInterval(annoBlockEndVar, timeline);

                    // check for overlap
                    if (spanStart <annoBlockEnd && spanEnd>annoBlockStart){
                       //this is an overlap
                        if (spanStart >=annoBlockStart && spanEnd<=annoBlockStart){
                            fromVar = spanStartVar;
                            toVar = spanEndVar;
                            break;
                        } else if (spanStart <= annoBlockStart && spanEnd <= annoBlockEnd){
                            if (fromVar == null){
                                fromVar = annoBlockStartVar;
                            }
                            toVar = spanEndVar;
                            break;
                        }else if (spanStart >=annoBlockStart && spanEnd >=annoBlockEnd){
                            fromVar = spanStartVar;
                            toVar = annoBlockEndVar;
                        }else if (spanStart <= annoBlockStart && spanEnd >=annoBlockEnd){
                            if (fromVar == null){
                               fromVar = annoBlockStartVar;
                            }
                            toVar=annoBlockEndVar;
                        }                                       
                    }
                }
                //add new span
                if (fromVar!=null && toVar!=null){
                    newElement.setAttribute(Constants.ATTRIBUTE_NAME_FROM, fromVar);
                    newElement.setAttribute(Constants.ATTRIBUTE_NAME_TO, toVar);
                    CommonEventOverlapSpanGrp.addContent(newElement);
                }
            }    
        }

        if (CommonEventOverlapSpanGrp.getChildren().size()>0){
            newBodyForSpeakerBasedView.addContent(CommonEventOverlapSpanGrp);
        }
    }
    
    void addMetadata(Set<MetadataKey> metadataKeys, 
            Event event, SpeechEvent speechEvent, Element newBodyForSpeakerBasedView, Element personElement,
            String transcriptStartVar, String transcriptEndVar, String eventID, String speechEventID, String transcriptID, IDList videos) throws IOException{
        
        System.out.println("Adding metadata...");
                            
        Element metaSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        metaSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_META);
        metaSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_SUBTYPE, Constants.SPANGRP_SUBTYPE_TIME_BASED);
        newBodyForSpeakerBasedView.addContent(metaSpanGrp);
                                
        //additional metadata
        addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_EVENT_DGD_ID, transcriptStartVar, transcriptEndVar, eventID);
        addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_TRANSCRIPT_DGD_ID, transcriptStartVar, transcriptEndVar, transcriptID);
        if(speechEvent!=null){
            addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID, transcriptStartVar, transcriptEndVar, speechEventID);
        }
        
        // add number of videos
        if(videos!=null){
            addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_EVENT_NUMBER_VIDEOS, transcriptStartVar, transcriptEndVar, String.valueOf(videos.size()));
        }
        
        // add "Grad der Mündlichkeit" für GWSS
        if (eventID.startsWith("GWSS_")){
            String spontaneity = getSpontaneity(event, speechEventID);    
            addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_SPEECH_EVENT_SPEECH_NOTES, transcriptStartVar, transcriptEndVar, spontaneity);
        }
                                
        if (personElement.getChild(Constants.ELEMENT_NAME_IDNO,ns) != null){
                                
            String speakerId = personElement.getChild(Constants.ELEMENT_NAME_IDNO,ns).getText();
            String speakerIdType = personElement.getChild(Constants.ELEMENT_NAME_IDNO,ns).getAttributeValue(Constants.ATTRIBUTE_NAME_TYPE);
            if(!speakerIdType.equals(Constants.RANDOM_ID)){    
                Speaker speaker = backendInterface.getSpeaker(speakerId);
                Speaker speakerInSpeechEvent = null;

                if (speechEvent!= null){  // Transcripts without speech event metadata will be indexed without speech event metadata
                    speakerInSpeechEvent = backendInterface.getSpeakerInSpeechEvent(speechEvent.getID(), speakerId);
                } 

                for (MetadataKey metadataKey : metadataKeys){
                    String value = null;
                    if (metadataKey.getLevel().equals(ObjectTypesEnum.EVENT)){
                        value = event.getMetadataValue(metadataKey);
                    }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEECH_EVENT) && speechEvent!= null){
                        value = speechEvent.getMetadataValue(metadataKey);
                    }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEAKER)){
                        value = speaker.getMetadataValue(metadataKey);
                    }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT) && speakerInSpeechEvent!=null){
                        value = speakerInSpeechEvent.getMetadataValue(metadataKey);
                    }

                    if (value!=null && !value.isEmpty() && !METADATA_VALUES_TO_BE_IGNORED.contains(value)){
                        if (metadataKey.getID().equals(Constants.METADATA_KEY_SPEAKER_BIRTH_DATE) && !SPEAKER_BIRTH_DATE_TO_BE_IGNORED.contains(value)){
                            // additional metadata
                            addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_SPEAKER_YEAR_OF_BIRTH, transcriptStartVar, transcriptEndVar, value.substring(0, 4));
                        }else if (!metadataKey.getID().equals(Constants.METADATA_KEY_SPEAKER_BIRTH_DATE)) {
                            if (!(metadataKey.getID().equals(Constants.METADATA_KEY_SPEAKER_BIRTH_AGE) && SPEAKER_BIRTH_AGE_TO_BE_IGNORED.contains(value))){
                                addMetadataSpan(metaSpanGrp, metadataKey.getID(), transcriptStartVar, transcriptEndVar, value);

                                // additional metadata                                          
                                if (metadataKey.getID().equals(Constants.METADATA_KEY_EVENT_DURATION)){
                                    addSecMetadata(metaSpanGrp, transcriptStartVar, transcriptEndVar, value);
                                }
                            }
                        }
                    }
                }

                // additional metadata
                addMetadataSpan(metaSpanGrp, Constants.METADATA_KEY_SPEAKER_DGD_ID, transcriptStartVar, transcriptEndVar, speakerId);
            }
        }else {
            for (MetadataKey metadataKey : metadataKeys){
                String value = null;
                if (metadataKey.getLevel().equals(ObjectTypesEnum.EVENT)){
                    value = event.getMetadataValue(metadataKey);  
                }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEECH_EVENT) && speechEvent!= null){
                    value = speechEvent.getMetadataValue(metadataKey);
                }
                                    
                if(value!=null && !value.isEmpty() && !METADATA_VALUES_TO_BE_IGNORED.contains(value)){
                    addMetadataSpan(metaSpanGrp, metadataKey.getID(), transcriptStartVar, transcriptEndVar, value);
                    // additional metadata
                    if (metadataKey.getID().equals(Constants.METADATA_KEY_EVENT_DURATION)){
                        addSecMetadata(metaSpanGrp, transcriptStartVar, transcriptEndVar, value);
                    }
                }
            }                                
        }
                            
    }
        
  /*  void addOverlaps(Element annotationBlock, Document transcript) throws JDOMException{
        
        Element timeline = transcript.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns).getChild("timeline", ns);
        Element body = transcript.getRootElement().getChild(Constants.ELEMENT_NAME_TEXT, ns).getChild(Constants.ELEMENT_NAME_BODY, ns);
        double ownStart = getInterval(annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START), timeline);
        double ownEnd = getInterval(annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END), timeline);
        Element speakerOverlapSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
        speakerOverlapSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_SPEAKER_OVERLAP);
                    
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
*/
    void addProxyPause(Element parent, String start, String end, int id, double dur){
        Element proxyPause = new Element (Constants.ELEMENT_NAME_PAUSE, ns);
        proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_START, start);
        proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_END, end);
        proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_CLASS, Constants.ATTRIBUTE_VALUE_PROXY);
        proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_ID, "x" + String.valueOf(id), xml); // adding new id
                                                    
        // add dur
        Formatter formatter = new Formatter(new StringBuilder(), Locale.US);
        String pauseDur = formatter.format("%.2f", dur).toString();
        proxyPause.setAttribute(Constants.ATTRIBUTE_NAME_DUR, pauseDur);
 
        parent.addContent(proxyPause);
    }
}
