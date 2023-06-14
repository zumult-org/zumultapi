/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zumult.objects.IDList;
import org.zumult.objects.Location;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Speaker;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2Speaker extends AbstractXMLObject implements Speaker {

    XPath xPath = XPathFactory.newInstance().newXPath();

    public DGD2Speaker(Document xmlDocument) {
        super(xmlDocument);
    }
    
    public DGD2Speaker(String xmlString) {
        super(xmlString);
    }
    
    
    @Override
    public String getID() {
        return getDocument().getDocumentElement().getAttribute("Kennung");
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
        try {
            String xPathString = ((DGD2MetadataKey)key).xpath;
            String valueString = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            return valueString;
            //return metadata.get(key.getID());
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Event.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
 
    @Override
    public IDList getSpeechEvents(){
        IDList allSpeechEvents = new IDList("speech-event");
        try {
            String xPathString = "/Sprecher/In_Sprechereignis/SE-Kennung";
            //System.out.println(xPathString);
            NodeList speechEvents = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<speechEvents.getLength(); i++){
                Element speechEvent = (Element) speechEvents.item(i);
                String id = speechEvent.getTextContent();
                allSpeechEvents.add(id);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Speaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return allSpeechEvents;
    }
    
    @Override
    public List<Location> getLocations(String locationType) {
        List<Location> result = new ArrayList<>();
        try {
            String xPathString = "/Sprecher/Ortsdaten[@Typ='" + locationType + "']";
            //System.out.println(xPathString);
            NodeList locationElements = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<locationElements.getLength(); i++){
                Node locationElement = locationElements.item(i);
                Node clonedNode = locationElement.cloneNode(true);

                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();                
                document.appendChild(document.importNode(clonedNode, true));
                Location location = new DGD2Location(document);
                result.add(location);
            }
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(DGD2Speaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    // removed 07-07-2022, issue #40
    /*@Override
    public String getOccupation(String speakerID) {
        return getDocument().getElementsByTagName("Berufe").item(0).getTextContent();
    }*/ 
    
}
