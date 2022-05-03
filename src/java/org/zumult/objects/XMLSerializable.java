/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import org.w3c.dom.Document;

/**
 *
 * @author Thomas_Schmidt
 */
public interface XMLSerializable {
    
    
    public String toXML() throws Exception;
    
    public Document getDocument();
    
}
