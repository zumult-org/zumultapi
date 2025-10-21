/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.Set;

/**
 *
 * @author thomasschmidt
 */
public interface Metadatable {
    
    public String getMetadataValue(MetadataKey key);
    public Set<String> getMetadataValues(MetadataKey key, String language);
    public Set<String> getMetadataValues(MetadataKey key);
    
    
}
