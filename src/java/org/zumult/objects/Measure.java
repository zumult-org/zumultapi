/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.HashMap;

/**
 *
 * @author Elena
 */
public interface Measure extends XMLSerializable {
    public String getType();
    public String getReference();
    public HashMap<String, String> getStatistics();
    
}
