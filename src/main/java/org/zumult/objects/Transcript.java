/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Thomas_Schmidt
 */
public interface Transcript extends XMLSerializable, Identifiable, Metadatable {

    
    // #223
    public enum TranscriptFormats {
        ISOTEI,
        EXB,
        EAF
    }
    
    public int getNumberOfTokens();
    public int getNumberOfTypes();
    
    public double getStartTime();
    public double getEndTime();
    
    // this will return the nearest time before the element with that ID
    public double getTimeForID(String id);
    // this will return the nearest time after the element with that ID
    public double getNextTimeForID(String id);
    
    public Transcript getPart(String id1, String id2, boolean expandToFullAnnotationBlock);
    public Transcript getPart(double time1, double time2, boolean expandToFullAnnotationBlock);
    public Transcript getPart(int startIndex, int endIndex);
    
    
    public TokenList getTokenList(String type);
    public TokenList getTokenList(String type, TokenFilter filter);
    
    public Element getElementById(String id);
    public NodeList getTokensByPOS(String pos);
    public NodeList getAllTokens();
    
    public Document getXmlDocument();
    public NodeList getAnchorsByAttribute(String attribute);
    public NodeList getAnnotationBlocksBySpeaker(String speaker);
    public String getSpeakerInitialsBySpeakerID(String speakerID);
    
    // issue #3
    public String getSpeakerIDBySpeakerInitials(String speakerInitials);   
    
    public String getAnnotationBlockID(String annotationBlockID, int distance);
    public String getFirstAnnotationBlockIDForTime(double time);

    public void removeAnnotations();
    
    // issue #69
    public String getLanguage();
    
    public void setTimelineToZero();
        
    public List<String> getRecordings();
    public void setRecordings(List<String> recordingURLs);
}
