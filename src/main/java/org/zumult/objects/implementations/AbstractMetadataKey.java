/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;

/**
 *
 * @author Frick
 */
public abstract class AbstractMetadataKey implements MetadataKey {
        
    String DEFAULT_LANGUAGE = "de";
    
    String id;
    Map<String, String> names = new HashMap<>(); //e.g. {"en": "Year of birth", "de": "Geburtsjahr"}
    ObjectTypesEnum level;
    boolean quantified;
    
    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName(String language) {
        if (names.get(language)!=null){
            return names.get(language);
        }else{
            return names.get(DEFAULT_LANGUAGE);
        }
    }

    @Override
    public ObjectTypesEnum getLevel() {
        return level;
    }
    
    @Override
    public Map<String, String> getNamesByLanguages () {
        return this.names;
    }
    
    @Override
    public Set<String> getLanguages () {
        throw new UnsupportedOperationException ("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    public boolean isQuantified() {
        return quantified;
    }
    
    protected void setId (String id) {
        this.id = id;
    }

    protected void setNames (Map<String, String> names) {
        this.names = names;
    }
    
    protected void addName (String language, String name) {
        this.names.put(language, name);
    }

    protected void setLevel (ObjectTypesEnum level) {
        this.level = level;
    }
    
    protected void setQuantified (boolean quantified) {
        this.quantified = quantified;
    }
    
}
