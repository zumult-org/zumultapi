/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query;

/**
 *
 * @author Frick
 */
public interface Bigram {
    public int getPosition();
    public String getQueryMatch();
    public String getPartner();
    public int getNumberOfHits();
    public BigramType getType();
    
    /**************************************************************/
    /*              BigramType class                    */
    /**************************************************************/     
    public enum BigramType {
         LEFT, 
         RIGHT;
    }
}