/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.zumult.backend;

import java.io.IOException;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author bernd
 */
public interface MetadataFinderInterface extends BackendInterface {
    
    
    public IDList findEventsByMetadataValue(String corpusID, MetadataKey metadataKey, String metadataValue) 
            throws IOException;
    
    public IDList findSpeechEventsByMetadataValue(String corpusID, MetadataKey metadataKey, String metadataValue) 
            throws IOException;
    
    public IDList findSpeakersByMetadataValue(String corpusID, MetadataKey metadataKey, String metadataValue) 
            throws IOException;
    
}
