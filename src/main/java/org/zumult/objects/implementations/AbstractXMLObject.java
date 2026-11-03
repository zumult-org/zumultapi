/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import org.zumult.objects.XMLSerializable;

/**
 *
 * @author Thomas_Schmidt
 */
public abstract class AbstractXMLObject implements XMLSerializable {
    
    private Document xmlDocument;
    private String xmlString;

    // 08-05-2019
    // Trying "lazy" initialisation:
    // As long as the object is not requested as string/document,
    // leave the fields uninitialised
    
    
    public AbstractXMLObject(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
        // new for #285
        registerXmlIds(xmlDocument);        
    }

    public AbstractXMLObject(String xmlString) {
        this.xmlString = xmlString;
    }
    
    public void setXML(Document xmlDocument){
        this.xmlDocument = xmlDocument;
        // new for #285
        registerXmlIds(xmlDocument);        
        documentChanged();
    }

    public void setXML(String xmlString){
        try {
            this.xmlString = xmlString;
            xmlDocument = IOHelper.DocumentFromText(xmlString);
            // new for #285
            registerXmlIds(xmlDocument);        
            
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(AbstractXMLObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toXML() throws TransformerConfigurationException, TransformerException {
        if (xmlString==null){
            //System.out.print("Getting xml string from document...");
            xmlString = IOHelper.DocumentToString(xmlDocument);
            //System.out.println(" Done.");
        }
        return xmlString;
    }
    
    @Override
    public Document getDocument() {
        if (xmlDocument==null){
            try {
                xmlDocument = IOHelper.DocumentFromText(xmlString);
                // new for #285
                registerXmlIds(xmlDocument);        
            } catch (IOException | SAXException | ParserConfigurationException ex) {
                Logger.getLogger(AbstractXMLObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return xmlDocument;
    }
    
    // this must be called when the document is changed
    public void documentChanged(){
        try {
            xmlString = IOHelper.DocumentToString(xmlDocument);
        } catch (TransformerException ex) {
            Logger.getLogger(AbstractXMLObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    // 03-11-2026: added for #285
    private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    protected void registerXmlIds(Document doc) {
        if (doc == null) return;

        NodeList elements = doc.getElementsByTagName("*");

        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);

            if (element.hasAttributeNS(XML_NS, "id")) {
                element.setIdAttributeNS(XML_NS, "id", true);
            }
        }
    }    
    
}
