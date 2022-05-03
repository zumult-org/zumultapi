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
public interface Location extends XMLSerializable {
    
    public String getType();
    public String getPlacename();
    public String getCountry();
    public double getLatitude();
    public double getLongitude();
    
    
}
