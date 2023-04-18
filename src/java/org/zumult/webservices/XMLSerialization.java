/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.webservices;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.jdom.Element;
import org.zumult.io.IOUtilities;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Frick
 */
public class XMLSerialization {

    public static Element createElementForMetadataKeys(Set<MetadataKey> metadataKeys, Locale locale){
        Element metadata = new Element("metadata");
        
        List<MetadataKey> metadataKeysList = metadataKeys.stream().sorted((o1, o2) -> 
        o1.getID().compareTo(o2.getID())).collect(Collectors.toList());
        
        for (MetadataKey key : metadataKeysList) {
            Element listElement = new Element("metadata-key");
            listElement.setAttribute("id", key.getID());
            listElement.setAttribute("name", key.getName(locale.getLanguage()));
            listElement.setAttribute("type", key.getLevel().name());
            metadata.addContent(listElement);
        }
        return metadata;
    }
}
