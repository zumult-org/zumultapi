/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Location;
import org.zumult.objects.Measure;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.SpeechEvent;

/**
 *
 * @author thomas.schmidt
 */
public class COMACommunication extends AbstractXMLObject implements Event, SpeechEvent {

    XPath xPath = XPathFactory.newInstance().newXPath();


    public COMACommunication(Document xmlDocument) {
        super(xmlDocument);
    }
    

    @Override
    public Date getDate() {
        try {
            String xPathString = "/descendant::Location/Period/PeriodStart";
            String dateString = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            Date date = Date.from(Instant.parse(dateString + "Z"));
            return date;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;        
    }

    @Override
    public Location getLocation() {
        try {
            String xPathString = "/descendant::Location";
            Node locationElement = (Node) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODE);
            Node clonedNode = locationElement.cloneNode(true);
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.appendChild(document.importNode(clonedNode, true));
            Location location = new COMALocation(document);
            
            return location;
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(COMACommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
        if (key==null) return "null";
        try {
            String xPathString = "descendant::Key[@Name='" + key.getName("en") + "']/text()";
            String value = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            return value;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @Override
    public IDList getSpeechEvents() {
        IDList result = new IDList("speechEvent");
        result.add(getID());
        return result;
    }


    @Override
    public String getID() {
        return getDocument().getDocumentElement().getAttribute("Id");
    }

    @Override
    public String getType() {
        return "Type";
    }

    @Override
    public List<String> getTopics() {
        return new ArrayList<>();
    }

    @Override
    public IDList getTranscripts() {
        IDList result = new IDList("transcript");
        try {
            //String xPathString = "descendant::Transcription";
            String xPathString = "descendant::Transcription[substring(Filename, string-length(Filename)-3)='.xml' or substring(Filename, string-length(Filename)-3)='.XML']";
            NodeList transcriptionNodeList = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<transcriptionNodeList.getLength(); i++){
                Element transcriptionElement = (Element)(transcriptionNodeList.item(i));
                String tID = transcriptionElement.getAttribute("Id");
                result.add(tID);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getMedia() {
        IDList result = new IDList("media");
        try {
            String xPathString = "descendant::Media";
            NodeList mediaNodeList = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<mediaNodeList.getLength(); i++){
                Element mediaElement = (Element)(mediaNodeList.item(i));
                String mID = mediaElement.getAttribute("Id");
                result.add(mID);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getSpeakers() {
        IDList result = new IDList("speaker");
        try {
            String xPathString = "descendant::Person";
            NodeList personNodeList = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<personNodeList.getLength(); i++){
                Element personElement = (Element)(personNodeList.item(i));
                String pID = personElement.getTextContent();
                result.add(pID);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMACommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public String getContentSummary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getCoordinatesForTime(double time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Measure getMeasure(String type, String reference) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Measure getMeasureValue(String type, String reference, String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return this.getDocument().getDocumentElement().getAttribute("Name");
    }

    @Override
    public Set<String> getMetadataValues (MetadataKey key, String language) {
        throw new UnsupportedOperationException ("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<String> getMetadataValues (MetadataKey key) {
        throw new UnsupportedOperationException ("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
