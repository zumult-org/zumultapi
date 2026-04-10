/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.zumult.objects;

/**
 *
 * @author bernd
 */
public interface Episode extends XMLSerializable {
    
    public String getFrom();
    public String getTo();
    
    public String getDescription();
    public IDList getRestrictionSpeakerIDs();
    
    public String getName();
    
    
}
