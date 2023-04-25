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
    }

    public AbstractXMLObject(String xmlString) {
        this.xmlString = xmlString;
    }

    @Override
    public String toXML() throws TransformerConfigurationException, TransformerException {
        if (xmlString==null){
            xmlString = IOHelper.DocumentToString(xmlDocument);
        }
        return xmlString;
    }
    
    @Override
    public Document getDocument() {
        if (xmlDocument==null){
            try {
                xmlDocument = IOHelper.DocumentFromText(xmlString);
            } catch (IOException | SAXException | ParserConfigurationException ex) {
                Logger.getLogger(AbstractXMLObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return xmlDocument;
    }
    
}
