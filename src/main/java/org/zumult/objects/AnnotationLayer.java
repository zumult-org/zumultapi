/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

/**
 *
 * @author Frick
 */
public interface AnnotationLayer extends Identifiable {
    String getName(String language);
    Class getValueClass();
    AnnotationTypeEnum getType(); //token, span
}
