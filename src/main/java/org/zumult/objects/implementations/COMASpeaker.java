/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.objects.IDList;
import org.zumult.objects.Location;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Speaker;

/**
 *
 * @author thomas.schmidt
 */
public class COMASpeaker extends AbstractXMLObject implements Speaker {

    XPath xPath = XPathFactory.newInstance().newXPath();

    public COMASpeaker(Document xmlDocument) {
        super(xmlDocument);
    }

    public COMASpeaker(String xmlString) {
        super(xmlString);
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
        try {
            if (key.getName("en").equals("Sex")){
                String xPathString = "descendant::Sex/text()";
                String value = xPath.evaluate(xPathString, getDocument().getDocumentElement());
                return value;
            }
            String xPathString = "descendant::Key[@Name='" + key.getName("en") + "']/text()";
            String value = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            return value;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMASpeaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    @Override
    public Set<String> getMetadataValues (MetadataKey key, String language) {
        // this is preliminary until COMA supports multilingual metadata and multiple values per key
        Set<String> result = new HashSet<>();
        result.add(getMetadataValue(key));
        return result;
    }

    @Override
    public Set<String> getMetadataValues (MetadataKey key) {
        // this is preliminary until COMA supports multilingual metadata 
        Set<String> result = new HashSet<>();
        result.add(getMetadataValue(key));
        return result;
    }
    

    @Override
    public List<Location> getLocations(String locationType) {
        List<Location> result = new ArrayList<>();
        try {
            String xPathString = "/descendant::Location[@Type='" + locationType + "']";
            //System.out.println(xPathString);
            NodeList locationElements = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<locationElements.getLength(); i++){
                Node locationElement = locationElements.item(i);
                Node clonedNode = locationElement.cloneNode(true);

                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();                
                document.appendChild(document.importNode(clonedNode, true));
                Location location = new COMALocation(document);
                result.add(location);
            }
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(COMASpeaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public String getID() {
        return getDocument().getDocumentElement().getAttribute("Id");
    }

    @Override
    public IDList getSpeechEvents() {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            return backend.getSpeechEvents4Speaker(getID());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(COMASpeaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new IDList("SpeechEvents");
    }
    
    
}
