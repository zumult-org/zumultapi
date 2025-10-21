/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Thomas_Schmidt
 */
public interface MetadataKey extends Identifiable, Comparable<MetadataKey>  {
    
    String getName(String language);
    Map<String, String> getNamesByLanguages(); // e.g. { "de":"Nachname", "en": "Surname" }
    Set<String> getLanguages(); // e.g. ("de", "en", "it")
    
    ObjectTypesEnum getLevel();
    
    Class getValueClass();
    
    boolean isQuantified();

    @Override
    public default int compareTo(MetadataKey o) {
       return (getName("en").compareTo(o.getName("en")));
    }

    
    
    
}
