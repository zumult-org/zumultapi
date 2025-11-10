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
 * @author Frick
 */
public interface AnnotationLayer extends Identifiable {
    String getName(String language);
    Class getValueClass();
    AnnotationTypeEnum getType(); //token, span
    
    Map<String, String> getNamesByLanguages(); // e.g. { "de":"Normalisierte Form", "en": "Normalized Form" }
    
    /* returns all available languages for this annotation layer name */
    Set<String> getLanguages(); // e.g. ("de", "en", "it")
}
