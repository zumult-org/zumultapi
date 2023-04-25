/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

import java.io.IOException;
import org.zumult.objects.IDList;

/**
 *
 * @author thomas.schmidt
 */
public interface MetadataFilter {
    
    public IDList filterSpeechEvents(String corpusID, String metadataKeyID, String regex) throws IOException;
    
    public IDList filterSpeechEvents(String corpusID, String metadataKeyID, double minValue, double maxValue) throws IOException;
    
}
