/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.io.IOException;
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
    public Set<MetadataKey> getMetadataKeys(ObjectTypesEnum objectType);
    
    /*public <Set>String getValuesForEventMetadataKey(String key);
    public <Set>String getValuesForSpeechEventMetadataKey(String key);
    public <Set>String getValuesForSpeakerInSpeechEventMetadataKey(String key);
    public <Set>String getValuesForSpeakerMetadataKey(String key);*/
    
    public Set<AnnotationLayer> getAnnotationLayers();
    public Set<AnnotationLayer> getAnnotationLayers(AnnotationTypeEnum annotationType);

    public Set<String> getSpeakerLocationTypes();
    
    public CrossQuantification getCrossQuantification(MetadataKey metadataKey1, MetadataKey metadataKey2, String unit) throws IOException, ResourceServiceException ;
    
    // new 08-09-2025: issue #262
    public CorpusStatistics getCorpusStatistics();
    
}
