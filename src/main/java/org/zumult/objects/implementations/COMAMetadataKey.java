/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.zumult.objects.ObjectTypesEnum;

/**
 *
 * @author thomas.schmidt
 */
public class COMAMetadataKey extends AbstractMetadataKey {
    
    public COMAMetadataKey(String id, String name, ObjectTypesEnum level) {
        this.id = id;
        addName(DEFAULT_LANGUAGE, name);
        this.level = level;
    }

    @Override
    public Class getValueClass() {
        return String.class;
    }
    
}
