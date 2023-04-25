/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.time.Instant;
import java.util.Date;
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
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Location;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2Event extends AbstractXMLObject implements Event {

    XPath xPath = XPathFactory.newInstance().newXPath();
    
    public DGD2Event(Document xmlDocument) {
        super(xmlDocument);
    }
    
    public DGD2Event(String xmlString) {
        super(xmlString);
    }

    @Override 
    public Date getDate() {
        try {
            // Query for the right element via XPath
            String xPathString = "/Ereignis/Basisdaten/Datum/YYYY-MM-DD";
            String dateString = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            /*NodeList nodes = (NodeList)xPath.evaluate(xPathString, xmlDocument.getDocumentElement(), XPathConstants.NODESET);
            Element dateElement = (Element) nodes.item(0);
            String dateString = dateElement.getTextContent() + "T00:00:00Z";*/
            Date date = Date.from(Instant.parse(dateString + "T00:00:00Z"));           
            return date;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Event.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Location getLocation() {
        try {
            String xPathString = "/Ereignis/Basisdaten/Ort";
            Node locationElement = (Node) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            Node clonedNode = locationElement.cloneNode(true);
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.appendChild(document.importNode(clonedNode, true));
            Location location = new DGD2Location(document);
            
            return location;
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(DGD2Event.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
    public IDList getSpeechEvents() {
        IDList result = new IDList("speechEvent");
        try {
            String xPathString = "//Sprechereignis";
            NodeList speechEventNodeList = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<speechEventNodeList.getLength(); i++){
                Element seElement = (Element)(speechEventNodeList.item(i));
                String seID = seElement.getAttribute("Kennung");
                result.add(seID);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Event.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
}
