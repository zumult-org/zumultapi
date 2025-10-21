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
    
    /* returns the name of this key (e.g. maiden name) in the given language
     should return the name in the default language when language is not
     available */
    String getName(String language);
    
    
    
    /* returns all names mapped from all available languages */
    Map<String, String> getNamesByLanguages(); // e.g. { "de":"Nachname", "en": "Surname" }
    
    /* returns all available languages for this key */
    Set<String> getLanguages(); // e.g. ("de", "en", "it")
    
    ObjectTypesEnum getLevel();
    
    Class getValueClass();
    
    boolean isQuantified();

    @Override
    public default int compareTo(MetadataKey o) {
       return (getName("en").compareTo(o.getName("en")));
    }

    
    
    
}
