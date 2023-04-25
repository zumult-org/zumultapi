/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import org.zumult.io.IOUtilities;

/**
 *
 * @author Thomas_Schmidt
 */
public class IDList extends ArrayList<String> implements XMLSerializable {

    public IDList(String objectName) {
        this.objectName = objectName;
    }
    
    public void readXML(String xmlString) throws IOException, SAXException, ParserConfigurationException{
        Document doc = IOHelper.DocumentFromText(xmlString);
        NodeList childNodes = doc.getElementsByTagName(objectName);
        for (int i = 0; i < childNodes.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) (childNodes.item(i));
            String id = element.getAttribute("id");
            this.add(id);
        }        
    }
    
    private String objectName;

    /**
     * Get the value of objectName
     *
     * @return the value of objectName
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Set the value of objectName
     *
     * @param objectName new value of objectName
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
  
    
    
    @Override
    public String toXML() {
        Element root = new Element("IDList");
        for (String id : this){
            Element listElement = new Element(getObjectName());
            listElement.setAttribute("id", id);
            root.addContent(listElement);
        }
        return IOUtilities.elementToString(root);
    }

    @Override
    public Document getDocument() {
        try {
            return IOHelper.DocumentFromText(toXML());
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(IDList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
