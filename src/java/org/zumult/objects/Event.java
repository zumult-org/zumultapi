/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.Date;

/**
 *
 * @author Thomas_Schmidt
 */
public interface Event extends XMLSerializable, Identifiable, Metadatable {
    
    public Date getDate();
    
    public Location getLocation();
    
    //public String getMetadataValue(MetadataKey key);
    
    public IDList getSpeechEvents();
    
    
}
