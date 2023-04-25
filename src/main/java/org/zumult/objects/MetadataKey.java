/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

/**
 *
 * @author Thomas_Schmidt
 */
public interface MetadataKey extends Identifiable {
    
    String getName(String language);
    
    ObjectTypesEnum getLevel();
    
    Class getValueClass();
    
}
