/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import org.zumult.objects.AdditionalMaterial;

/**
 *
 * @author bernd
 */
public abstract class AbstractAdditionalMaterial implements AdditionalMaterial {
    
    String id;
    String urlString;
    FILE_TYPE type;

    public AbstractAdditionalMaterial(String id, String urlString) {
        this.id = id;
        this.urlString = urlString;
    }
    
    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getURL() {
        return urlString;
    }

    @Override
    public FILE_TYPE getType() {
        return type;
    }
    
    
}
