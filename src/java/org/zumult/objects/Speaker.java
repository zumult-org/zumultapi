/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.List;

/**
 *
 * @author Thomas_Schmidt
 */
public interface Speaker extends XMLSerializable, Identifiable {
    
    public String getMetadataValue(MetadataKey key);  
    public List<Location> getLocations(String locationType);
    public String getOccupation(String speakerID);
}
