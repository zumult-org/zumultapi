/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;

/**
 *
 * @author thomas.schmidt
 */
public class COMAMetadataKey implements MetadataKey {

    String id;
    String name;
    ObjectTypesEnum level;
    
    public COMAMetadataKey(String id, String name, ObjectTypesEnum level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }



    @Override
    public String getName(String language) {
        return name;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public ObjectTypesEnum getLevel() {
        return level;
    }

    @Override
    public Class getValueClass() {
        return String.class;
    }
    
}
