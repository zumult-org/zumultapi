/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

/**
 *
 * @author Elena
 */
public interface AnnotationBlock extends XMLSerializable, Identifiable {
    
    public String getSpeaker();
    public String getStart();
    public String getEnd();
    
    public String getWordText();

}
