/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

/**
 *
 * @author Frick
 * Type 'TOKEN' means that the values of the annotation layer refer to individual tokens 1:1
 * Type 'SPAN' means that the annotation layer can refer to more than one token, so it is 1:n
 */



public enum AnnotationTypeEnum {
    TOKEN, SPAN
}
