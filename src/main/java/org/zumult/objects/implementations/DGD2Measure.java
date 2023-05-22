/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.HashMap;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.zumult.objects.Measure;

/**
 *
 * @author Elena
 */
public class DGD2Measure extends AbstractXMLObject implements Measure {

    public DGD2Measure(String xmlString) {
        super(xmlString);
    }

    @Override
    public String getType() {
        String type = getDocument().getDocumentElement().getAttribute("type");
        return type;
        
    }

    @Override
    public String getReference() {
        String type = getDocument().getDocumentElement().getAttribute("reference");
        return type;
    }

    @Override
    public HashMap<String, String> getStatistics() {
        HashMap<String, String> map = new HashMap();
        NamedNodeMap attributes = getDocument().getDocumentElement().getAttributes();
        for (int i=0; i<attributes.getLength(); i++) {
            String attribute = ((Attr)attributes.item(i)).getName();
            String value = ((Attr)attributes.item(i)).getValue();
            if (!attribute.equals("type") && !attribute.equals("reference")){
                map.put(attribute, value);
            }
        }
        return map;   
    }
    
}
