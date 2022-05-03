/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.w3c.dom.Element;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2MetadataKey implements MetadataKey {

    String id;
    String name;
    String xpath;
    ObjectTypesEnum level;
    Class dataType;
    
    
    public DGD2MetadataKey(String id, String name, ObjectTypesEnum level){
        this.id = id;
        this.name = name;
        this.level = level;
        this.dataType = String.class;
        this.xpath = "";
    }
    
    public DGD2MetadataKey(Element keyElement) {
        /*
        <metadata-item quantify="false" data-type="String">              
        <label>DGD-Kennung</label> 
        <xpath>/_/@Kennung</xpath> 
        <level>event-metadata</level>
        <dgd-parameter-name>v_e_id</dgd-parameter-name> 
        <example>ZW--E_00008</example>         
        */
        id = keyElement.getElementsByTagName("dgd-parameter-name").item(0).getTextContent().substring(2);
        name = keyElement.getElementsByTagName("label").item(0).getTextContent();
        xpath = keyElement.getElementsByTagName("xpath").item(0).getTextContent();
        String levelString = keyElement.getElementsByTagName("level").item(0).getTextContent();
        level = ObjectTypesEnum.EVENT;
        switch (levelString){
            case "event-metadata" : 
                level = ObjectTypesEnum.EVENT;
                break;
            case "speaker-metadata" : 
                level = ObjectTypesEnum.SPEAKER;
                break;
            case "speech-event-metadata" : 
                level = ObjectTypesEnum.SPEECH_EVENT;
                break;
            case "speech-event-speaker-metadata" :
                level = ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT;
                break;
        }
        String dataTypeString = keyElement.getAttribute("data-type");
        dataType = String.class;
        switch (dataTypeString){
            case "String[]" :
                dataType = String[].class;
                break;
            case "Date" :
                dataType = java.util.Date.class;
                break;
        }
    }

    
    
    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName(String language) {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        // obj is the same if it is also a metadataKey and its ID is the same
        return (obj instanceof MetadataKey && ((MetadataKey)obj).getID().equals(this.getID()));
    }

    @Override
    public int hashCode() {
        return getID().hashCode(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectTypesEnum getLevel() {
        return level;
    }

    @Override
    public Class getValueClass() {
        return dataType;
    }
    
    
    
}
