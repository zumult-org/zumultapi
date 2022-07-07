/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.Set;

/**
 *
 * @author Thomas_Schmidt
 */
public interface Corpus extends XMLSerializable, Identifiable, Metadatable {
    
    
    public String getAcronym();
    public String getName(String language);
    public String getDescription(String language);
    
    public Set<MetadataKey> getMetadataKeys();
    public Set<MetadataKey> getEventMetadataKeys();
    public Set<MetadataKey> getSpeechEventMetadataKeys();
    public Set<MetadataKey> getSpeakerInSpeechEventMetadataKeys();
    public Set<MetadataKey> getSpeakerMetadataKeys();
    
    /*public <Set>String getValuesForEventMetadataKey(String key);
    public <Set>String getValuesForSpeechEventMetadataKey(String key);
    public <Set>String getValuesForSpeakerInSpeechEventMetadataKey(String key);
    public <Set>String getValuesForSpeakerMetadataKey(String key);*/
    
    //public Set<String> getAnnotationTypes();
    public Set<AnnotationLayer> getAnnotationLayers();
    public Set<AnnotationLayer> getTokenBasedAnnotationLayers();
    public Set<AnnotationLayer> getSpanBasedAnnotationLayers();

    public Set<String> getSpeakerLocationTypes();
   
    
    
}
