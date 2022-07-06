/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.HashMap;
import org.w3c.dom.Element;
import java.util.Map;
import org.w3c.dom.NodeList;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTypeEnum;

/**
 *
 * @author Frick
 */
public class DGD2AnnotationLayer implements AnnotationLayer {
    String id;
    Map<String, String> names; //e.g. {"en": "Action Formats", "de": "Handlungen und Modalverb-Formate"}
    Class dataType;
    AnnotationTypeEnum type;

    public DGD2AnnotationLayer(String id, Map<String, String> names, AnnotationTypeEnum type){
        this.id = id;
        this.names = names;
        this.dataType = String.class;
        this.type = type;
    }
    
    public DGD2AnnotationLayer(Element keyElement) {
        /*
        <key id="as" type="span">
            <name lang="en">Action Sequences</name>
            <name lang="de">Handlungssequenzen</name>
            <value freq="191">Ratschlag/Empfehlung/Instruktion/Anweisung</value>
            <value freq="135">Vorschlag/Angebot</value>
            <value freq="76">Frage/Bitte/Aufforderung</value>
            <value freq="43">Eröffnung</value>
            <value freq="43">Beendigung</value>
        </key>       
        */
        this.id = keyElement.getAttribute("id");
        Map<String, String> map = new HashMap();
        NodeList nodes = keyElement.getElementsByTagName("name");
        for (int i=0; i<nodes.getLength(); i++){
            Element el = (Element) nodes.item(i);
            map.put(el.getAttribute("lang"), el.getTextContent());              
        }
        this.names = map;
        this.type = AnnotationTypeEnum.valueOf(keyElement.getAttribute("type").toUpperCase());
    }
    
    @Override
    public String getName(String language) {
        return names.get(language);
    }

    @Override
    public Class getValueClass() {
        return dataType;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public AnnotationTypeEnum getType() {
        return type;
    }
}
