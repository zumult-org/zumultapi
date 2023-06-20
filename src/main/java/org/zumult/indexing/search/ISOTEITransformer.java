/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.FileIO;
import org.zumult.io.IOHelper;
import org.zumult.io.TimeUtilities;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.ISOTEITranscript;

/**
 *
 * @author Frick
 */
public abstract class ISOTEITransformer {
    static List<String> METADATA_VALUES_TO_BE_IGNORED = Arrays.asList("Nicht dokumentiert", "Nicht vorhanden");
    static List<String> SPECIAL_CHARACTERS_TO_BE_IGNORED = Arrays.asList("&", "+++");
    static List<String> SPEAKER_BIRTH_DATE_TO_BE_IGNORED = Arrays.asList("9999");
    static List<String> SPEAKER_BIRTH_AGE_TO_BE_IGNORED = Arrays.asList("0");
    static List<String> SPEAKER_METADATA_TO_BE_IGNORED_IN_TRANSCRIPT_BASED_MODE = Arrays.asList(Constants.METADATA_KEY_SPEAKER_NAME, 
            Constants.METADATA_KEY_SPEAKER_OTHER_NAMES, Constants.METADATA_KEY_SPEAKER_PSEUDONYM);

    static boolean ADD_REPETITIONS = true;
    static boolean ADD_METADATA = true;
    static boolean FORMAT_PAUSE_DURATION = true;
    static boolean ADD_PROXY_PAUSES = true;
    static boolean ADD_SPEAKER_OVERLAPS = true;
    static boolean ADD_TOKEN_START_AND_END = true;
    static boolean ADD_DIFF_NORM = true;
    static boolean ADD_OCCURRENCE = false;
    static boolean ADD_VIDEOS_NUMBER = true;
    
    static Set<String> corpusIDsForIndexing;
    static String DIR_IN;
    static String DIR_OUT;
    
    static Namespace ns = Namespace.getNamespace(Constants.TEI_NAMESPACE_URL);
    static Namespace xml = Namespace.getNamespace("xml", Constants.XML_NAMESPACE_URL);

    BackendInterface backendInterface; 
    
    static final Logger log = Logger.getLogger(ISOTEITransformer.class.getName()); ;

    void getTransriptsFromBackend() throws IOException, JDOMException, Exception{

        for (String corpusId: corpusIDsForIndexing){
            
            Corpus corpus = backendInterface.getCorpus(corpusId);
            if (corpus!= null){
                
                // create new folders for the current corpus   
                System.out.println("Output folder for the current corpus: " + DIR_OUT + "\\" + corpusId.replace("-", ""));
                File newFileTranscriptBased = new File(DIR_OUT + "\\" + corpusId.replace("-", ""));
                newFileTranscriptBased.mkdir();
                
                // get corpus metadata keys
                System.out.println("Getting MetadataKeys for " + corpusId);
                Set<MetadataKey> metadataKeys = corpus.getMetadataKeys();
                IDList transcriptIDs = backendInterface.getTranscripts4Corpus(corpusId);
                log.log(Level.INFO, "Indexing transcripts from {0}", corpusId);

                for (String transcriptID: transcriptIDs){

                    Transcript transcript = backendInterface.getTranscript(transcriptID);
                    log.log(Level.INFO, "Transforming {0}", transcriptID);
                    transformAndSaveTranscript(null, transcript, metadataKeys, newFileTranscriptBased);
                }
        
            }

        }

    }
     
    void readTranscriptsLocal() throws IOException, JDOMException{
        
        HashMap<String, String> corpora = new HashMap<>();

        corpusIDsForIndexing.forEach((corpusId) -> {
            String path = DIR_IN + "\\" + corpusId.replace("-", "");
            corpora.put(corpusId, path);
        });
        
        // iterate through corpora
        for (Map.Entry<String, String> entry : corpora.entrySet()) {
            String corpusId = entry.getKey(); // e.g. ZW--
            String corpusPath = entry.getValue();
            File file = new File(corpusPath);
            
            // get corpus metadata keys
            System.out.println("Getting MetadataKeys for " + corpusId);
            Set<MetadataKey> metadataKeys = null;
            if(ADD_METADATA){
                /* Corpus corpus = backendInterface.getCorpus(corpusId);
                metadataKeys = corpus.getMetadataKeys();*/
                metadataKeys = backendInterface.getMetadataKeys4Corpus(corpusId);
            }

            // create new folders for the current corpus   
            System.out.println("Output folder for the current corpus: "+ DIR_OUT + "\\" + file.getName());
            File newFileTranscriptBased = new File(DIR_OUT + "\\" + file.getName());
            newFileTranscriptBased.mkdir();
                
            // iterate through transcript documents
            File[] transcripts = file.listFiles();            
            for (File f : transcripts){                
                System.out.println(f.getPath() + " is read");
                //Document transcriptDoc = IOUtilities.readDocumentFromLocalFile(transcript.getPath());
                //transformAndSaveTranscript(transcriptDoc, metadataKeys, newFileTranscriptBased);
                Transcript t = null;
                try {
                    t = new ISOTEITranscript(IOHelper.readDocument(f));
                } catch (SAXException | ParserConfigurationException | IOException ex) {
                    throw new IOException("Unable to parse the file " + f.getAbsolutePath(), ex);
                }
                transformAndSaveTranscript(f, t, metadataKeys, newFileTranscriptBased);

            } //end of the transcript
        }
    }
      
    void deleteAll(){
        System.out.println("Are you sure to want to delete the content of " + DIR_OUT + " ?. Print (Y)es to continue or (N)ot to stop the script:");
        Scanner scannerVariableDelete = new Scanner(System.in);    
        
        switch(scannerVariableDelete.next().charAt(0)){
        case 'Y':
            System.out.println("Deleting everything in " + DIR_OUT);
            File outputDirTranscriptBased = new File(DIR_OUT);
            IOHelper.emptyDir(outputDirTranscriptBased);
            break;
        case 'N':
            System.out.println("The script was stopped!");
            System.exit(0); 
        default:
            System.out.println("Not valid input!");
            System.exit(0); 
        } 
        
        scannerVariableDelete.close();
    }
    
    void doit() throws IOException, JDOMException, Exception {
                   
        System.out.println("Converting " + corpusIDsForIndexing + 
                " to " + DIR_OUT);
        
        // delete all files from the output directories   
        deleteAll();
            
        //measure start time
        final long timeStart = System.currentTimeMillis();     

        try {
            // initialize backend
            backendInterface = BackendInterfaceFactory.newBackendInterface();
            
            System.out.println("Print (L) to get data from the local folder specified in DIR_IN "
                + "under <transcript-converting-input>, (B) to get data from backend:");
            Scanner scannerVariable = new Scanner(System.in);    
        
            // transform transcripts
            switch(scannerVariable.next().charAt(0)){
            case 'L':
                System.out.println("Getting data from " + DIR_IN);
                readTranscriptsLocal();
                break;
            case 'B':
                System.out.println("Getting data from Backend...");
                getTransriptsFromBackend();
                break;
            default:
                System.out.println("Not valid input!");
                System.exit(0); 
            } 
            
            scannerVariable.close();
              
        }catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ISOTEI2SpeakerBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //measure end time
        final long timeEnd = System.currentTimeMillis();
        long millis = timeEnd - timeStart;
        log.log(Level.INFO, "Transformation time: {0}", TimeUtilities.format(millis));
    }
    
    abstract void transformAndSaveTranscript(File f, Transcript transcript, Set<MetadataKey> metadataKeys, 
            File newFileSpeakerBased) throws IOException, JDOMException;
       
    
    void copyAttibute(Element source, Element target){
        
        List wordAttributes = source.getAttributes();
        Iterator wordAttributeIterator = wordAttributes.iterator();
        while (wordAttributeIterator.hasNext()) {
            Attribute attr = (Attribute) wordAttributeIterator.next();
            target.setAttribute((Attribute) attr.clone());
        }
    }

    void addDiffNorm(Element annotationBlock) throws JDOMException{
        Iterator segmentsIterator = annotationBlock.getChild(Constants.ELEMENT_NAME_U, ns).getChildren().iterator();
        while (segmentsIterator.hasNext()) {
            Element seg = (Element) segmentsIterator.next();         
            Iterator tokenIterator = seg.getChildren(Constants.ELEMENT_NAME_WORD_TOKEN, ns).iterator();            
            while (tokenIterator.hasNext()) {
                Element token = (Element) tokenIterator.next();
                String norm = token.getAttributeValue(Constants.ATTRIBUTE_NAME_NORM);
                String textNorm = token.getTextNormalize();

                if(!norm.toLowerCase().equals(textNorm.toLowerCase())){
                    String type = token.getAttributeValue(Constants.ATTRIBUTE_NAME_TYPE);
                    if(type==null){
                        type = Constants.DIFF_NORM;                    
                    }else{
                        type = type + " " + Constants.DIFF_NORM;
                    }
                     token.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, type);
                }
            }
        }
    }
        
    void setTokenStartEnd(Element annotationBlock) throws JDOMException{
                                            
        Element newU = new Element(Constants.ELEMENT_NAME_U, ns);
        copyAttibute(annotationBlock.getChild(Constants.ELEMENT_NAME_U, ns), newU);
        
        Iterator segmentsIterator = annotationBlock.getChild(Constants.ELEMENT_NAME_U, ns).getChildren().iterator();
        while (segmentsIterator.hasNext()) {
            Element seg = (Element) segmentsIterator.next();
            Element newSeg = new Element(Constants.ELEMENT_NAME_SEG , ns);
            copyAttibute(seg, newSeg);
                        
            
            Iterator tokenIterator = seg.getChildren().iterator();

            String startStr = null;
            String endStr = null;
            ArrayList<Element> words = new ArrayList<Element>();
            String proxy_start = null;  
            
            while (tokenIterator.hasNext()) {
                Element token = (Element) tokenIterator.next();                
                
                Element newToken = (Element) token.clone();
                if (token.getName().equals(Constants.ELEMENT_NAME_ANCHOR)){
                    if (words.size()>0){

                        endStr = token.getAttributeValue(Constants.ATTRIBUTE_NAME_SYNCH);
                        //add all Words
                        //for (int i=words.size(); --i >= 0;){
                        for (int i=0; i < words.size(); ++i){
                            Element w = words.get(i);
                            
                            if (w.getAttribute(Constants.ATTRIBUTE_NAME_PROXY_START)!=null){
                               w.setAttribute(Constants.ATTRIBUTE_NAME_START, w.getAttributeValue(Constants.ATTRIBUTE_NAME_PROXY_START));
                            }else{
                                if (startStr != null){
                                    w.setAttribute(Constants.ATTRIBUTE_NAME_START, startStr);
                                }else{                                  
                                    w.setAttribute(Constants.ATTRIBUTE_NAME_START, annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START));
                                }
                            }

                            if (w.getAttribute(Constants.ATTRIBUTE_NAME_PROXY_END)!=null){
                                w.setAttribute(Constants.ATTRIBUTE_NAME_END, w.getAttributeValue(Constants.ATTRIBUTE_NAME_PROXY_END));
                            }else{
                                w.setAttribute(Constants.ATTRIBUTE_NAME_END, endStr);
                            }
                                                             

                            newSeg.addContent(w);
                        }
                        words.clear();
                        proxy_start=null;
                    }

                    if (words.isEmpty()){
                        startStr = token.getAttributeValue(Constants.ATTRIBUTE_NAME_SYNCH);
                    }
                    newSeg.addContent(newToken);
                    
                } else if (token.getName().equals(Constants.ELEMENT_NAME_WORD_TOKEN) 
                        || token.getName().equals(Constants.ELEMENT_NAME_VOCAL)
                        || token.getName().equals(Constants.ELEMENT_NAME_INCIDENT)
                        || token.getName().equals(Constants.ELEMENT_NAME_PC)){
                    
                    
                    if (proxy_start != null){
                        newToken.setAttribute(Constants.ATTRIBUTE_NAME_PROXY_START, proxy_start);
                    }
                    
                    List anchors= null;
                    
                    if (token.getName().equals(Constants.ELEMENT_NAME_WORD_TOKEN) && token.getChildren(Constants.ELEMENT_NAME_ANCHOR, ns).size()>0){
                        anchors =  token.getChildren(Constants.ELEMENT_NAME_ANCHOR, ns);
                    }
                    if ((token.getName().equals(Constants.ELEMENT_NAME_VOCAL)|| token.getName().equals(Constants.ELEMENT_NAME_INCIDENT)) && token.getChild(Constants.ATTRIBUTE_NAME_DESC,ns).getChildren(Constants.ELEMENT_NAME_ANCHOR, ns).size() > 0){
                        anchors =  token.getChild(Constants.ATTRIBUTE_NAME_DESC,ns).getChildren(Constants.ELEMENT_NAME_ANCHOR, ns);
                    }
                    
                    if(anchors!=null){
                        int n=0;
                       
                        Iterator anchorIterator = anchors.iterator();
                        StringBuilder sb = new StringBuilder();
                        while (anchorIterator.hasNext()){
                            n= n+1;
                            Element anchor = (Element) anchorIterator.next();
                            if(n==1){
                                for (int i=words.size(); --i >= 0;){
                                    words.get(i).setAttribute(Constants.ATTRIBUTE_NAME_PROXY_END, anchor.getAttributeValue(Constants.ATTRIBUTE_NAME_SYNCH));
                                }
                            }

                            if(n==token.getChildren(Constants.ELEMENT_NAME_ANCHOR, ns).size()){
                                proxy_start = anchor.getAttributeValue(Constants.ATTRIBUTE_NAME_SYNCH);
                            }

                            sb.append(anchor.getAttributeValue(Constants.ATTRIBUTE_NAME_SYNCH));
                            if (anchorIterator.hasNext()){
                                sb.append(" ");
                            }
                        }
                        
                        newToken.setAttribute(Constants.ATTRIBUTE_NAME_SYNCH, sb.toString()); 

                    }
                    
                    words.add(newToken);                                           
                } else if (token.getName().equals(Constants.ELEMENT_NAME_PAUSE)){
                    if (proxy_start != null){
                        newToken.setAttribute(Constants.ATTRIBUTE_NAME_PROXY_START, proxy_start);
                    }
                                        
                    words.add(newToken);
                }
            }

            if (words.size()>0){
                endStr = annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_END);

                for (int i=0; i < words.size(); ++i){
                    Element w = words.get(i);
                            
                    if (w.getAttribute(Constants.ATTRIBUTE_NAME_PROXY_START)!=null){
                        w.setAttribute(Constants.ATTRIBUTE_NAME_START, w.getAttributeValue(Constants.ATTRIBUTE_NAME_PROXY_START));
                    }else{
                        if (startStr != null){
                            w.setAttribute(Constants.ATTRIBUTE_NAME_START, startStr);
                        }else{                                  
                            w.setAttribute(Constants.ATTRIBUTE_NAME_START, annotationBlock.getAttributeValue(Constants.ATTRIBUTE_NAME_START));
                        }
                    }

                    if (w.getAttribute(Constants.ATTRIBUTE_NAME_PROXY_END)!=null){
                        w.setAttribute(Constants.ATTRIBUTE_NAME_END, w.getAttributeValue(Constants.ATTRIBUTE_NAME_PROXY_END));
                    }else{
                        w.setAttribute(Constants.ATTRIBUTE_NAME_END, endStr);
                    }
                    newSeg.addContent(w);
                }
            }

        newU.addContent(newSeg);

        }
        annotationBlock.removeChild(Constants.ELEMENT_NAME_U, ns);
        annotationBlock.addContent(0, newU);

    }
    
    void addRepetitions(Element body){
        Iterator<?> iteratorWordTokens = body.getDescendants(new ElementFilter(Constants.ELEMENT_NAME_WORD_TOKEN));

        if (iteratorWordTokens.hasNext()){
            System.out.println("Adding repetitions...");
            Element wordTokenPrevious = (Element) iteratorWordTokens.next();
            Element repetitionSpanGrp = new Element(Constants.ELEMENT_NAME_SPAN_GRP, ns);
            repetitionSpanGrp.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, Constants.SPANGRP_TYPE_REPETITION);
                
            while (iteratorWordTokens.hasNext()) {
                Element wordToken = (Element) iteratorWordTokens.next();
                if (!SPECIAL_CHARACTERS_TO_BE_IGNORED.contains(wordToken.getText()) && wordToken.getText().equals(wordTokenPrevious.getText())){
                    Element span = new Element (Constants.ELEMENT_NAME_SPAN, ns);
                    span.setAttribute(Constants.ATTRIBUTE_NAME_FROM, wordTokenPrevious.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml));
                    span.setAttribute(Constants.ATTRIBUTE_NAME_TO, wordToken.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml));
                    span.setText(Constants.METADATA_KEY_MATCH_TYPE_WORD);
                    repetitionSpanGrp.addContent(span);
                }
        
                if(!SPECIAL_CHARACTERS_TO_BE_IGNORED.contains(wordToken.getAttributeValue(Constants.ATTRIBUTE_NAME_LEMMA)) && wordToken.getAttributeValue(Constants.ATTRIBUTE_NAME_LEMMA).equals(wordTokenPrevious.getAttributeValue(Constants.ATTRIBUTE_NAME_LEMMA))){
                    Element span = new Element (Constants.ELEMENT_NAME_SPAN, ns);
                    span.setAttribute(Constants.ATTRIBUTE_NAME_FROM, wordTokenPrevious.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml));
                    span.setAttribute(Constants.ATTRIBUTE_NAME_TO, wordToken.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml));
                    span.setText(Constants.ATTRIBUTE_NAME_LEMMA);
                    repetitionSpanGrp.addContent(span);
                }

                if(!SPECIAL_CHARACTERS_TO_BE_IGNORED.contains(wordToken.getAttributeValue(Constants.ATTRIBUTE_NAME_NORM)) && wordToken.getAttributeValue(Constants.ATTRIBUTE_NAME_NORM).equals(wordTokenPrevious.getAttributeValue(Constants.ATTRIBUTE_NAME_NORM))){
                    Element span = new Element (Constants.ELEMENT_NAME_SPAN, ns);
                    span.setAttribute(Constants.ATTRIBUTE_NAME_FROM, wordTokenPrevious.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml));
                    span.setAttribute(Constants.ATTRIBUTE_NAME_TO, wordToken.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml));
                    span.setText(Constants.ATTRIBUTE_NAME_NORM);
                    repetitionSpanGrp.addContent(span);
                }

                wordTokenPrevious = (Element) wordToken.clone();
                        
            }
                   
            if (repetitionSpanGrp.getChildren().size()>0){
                body.addContent(repetitionSpanGrp);
            }
        }
    }
    
    void addMetadataSpan(Element spanGrp, String metadataKeyId, String start, String end, String value){       
        Element metaSpan = new Element(Constants.ELEMENT_NAME_SPAN, ns);
        metaSpan.setAttribute(Constants.ATTRIBUTE_NAME_FROM, start);
        metaSpan.setAttribute(Constants.ATTRIBUTE_NAME_TO, end);
        metaSpan.setAttribute(Constants.ATTRIBUTE_NAME_TYPE, metadataKeyId);
        metaSpan.setText(value);
        spanGrp.addContent(metaSpan);
    }
    
    double getInterval(String variable, Element timeline) throws JDOMException{
        List whenElements = timeline.getChildren(Constants.ELEMENT_NAME_WHEN, ns);
        Iterator whenIterator = whenElements.iterator();

        while (whenIterator.hasNext()) {
            Element when = (Element) whenIterator.next();                                
            if (variable.equals(when.getAttribute(Constants.ATTRIBUTE_NAME_ID, xml).getValue())){                                                
                return Double.valueOf(when.getAttributeValue(Constants.ATTRIBUTE_NAME_ITERVAL));
            }
        }
        throw new JDOMException("Please check the time variables. Variable " + variable + " does not exist"); 
    }
    
    void addSecMetadata(Element spanGrp, String start, String end, String value){      
        String[] time = value.split(":");
        if (time.length == 3){
            Integer sec = Integer.parseInt(time[2]) + 60 * Integer.parseInt(time[1]) + 60 * 60 * Integer.parseInt(time[0]);
            addMetadataSpan(spanGrp, Constants.METADATA_KEY_EVENT_DAUER_SEC, start, end, String.valueOf(sec));
        }
    }
    
    void formatPauseDuration(Element body) throws JDOMException{
        System.out.println("changing pause duration");
        Iterator<?> iteratorPauses = body.getDescendants(new ElementFilter(Constants.ELEMENT_NAME_PAUSE));
        while (iteratorPauses.hasNext()) {
            Element pause = (Element) iteratorPauses.next();
            String dur = pause.getAttributeValue(Constants.ATTRIBUTE_NAME_DUR);
                        
            if (dur != null){
                            
                /*String durStr = dur.substring(2, dur.length()-1);
                pause.setAttribute(ATTRIBUTE_NAME_DUR, durStr);*/
                            
                Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)"); // Regex because of errors like dur="PT1.3sS"
                Matcher matcher = pattern.matcher(dur);
                if (matcher.find()){
                    String durStr = matcher.group(1);
                    pause.setAttribute(Constants.ATTRIBUTE_NAME_DUR, durStr);
                                
                    /*double durDouble = Double.valueOf(durStr);
                                
                    int durInt = (int)Math.round(durDouble);
                    pause.setAttribute(ATTRIBUTE_NAME_DUR_ROUND, String.valueOf(durInt));
                                
                    durInt = (int)Math.ceil(durDouble);
                    pause.setAttribute(ATTRIBUTE_NAME_DUR_CEIL, String.valueOf(durInt));*/
                                
                }else{
                    throw new JDOMException("Please check the pause duration of " + pause.getAttributeValue(Constants.ATTRIBUTE_NAME_ID, xml)); 
                }
            }
        }
    }
    
    
    String getSpontaneity(Event event, String speechEventID){
        String spontaneity = null;
        try{
            org.jdom.Document eventDoc = FileIO.readDocumentFromString(event.toXML());
            List sprechEvents = eventDoc.getRootElement().getChildren("Sprechereignis");
            for (Object oSprechEvent : sprechEvents){
                org.jdom.Element sprechEventElement = (org.jdom.Element) oSprechEvent;
                if(speechEventID.equals(sprechEventElement.getAttributeValue("Kennung"))){
                    String notes = sprechEventElement.getChild("Basisdaten").getChildText("Anmerkungen");
                    String[] notesList = notes.split(" ; ");
                    spontaneity = notesList[0];
                }
            }
        }catch (JDOMException ex){
            Logger.getLogger(ISOTEI2TranscriptBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception ex){
            Logger.getLogger(ISOTEI2TranscriptBasedFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return spontaneity;
    }
    
    
}
