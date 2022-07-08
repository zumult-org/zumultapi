/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.List;

/**
 *
 * @author Thomas_Schmidt
 */
public interface SpeechEvent extends Identifiable, XMLSerializable, Metadatable {
    
    public String getType();
    
    public String getName();
    
    public String getContentSummary();
    
    public List<String> getTopics();
    
    //public String getMetadataValue(MetadataKey key);
    
    public IDList getTranscripts();
    public IDList getMedia();
    public IDList getSpeakers();
    
    public String getProtocol();
    public String[] getCoordinatesForTime(double time);
    
    public Measure getMeasure(String type, String reference);
    public Measure getMeasureValue(String type, String reference,String key);
}
