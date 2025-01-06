/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.TimelineItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;

/**
 *
 * @author bernd
 */
// new for #223
public class EXBTranscript extends AbstractXMLObject implements Transcript {

    private XPath xPath;

    public EXBTranscript(Document xmlDocument) {
        super(xmlDocument);
    }

    public EXBTranscript(String xmlString) {
        super(xmlString);
    }
    
    private BasicTranscription getBasicTranscription(){
        BasicTranscription bt = new BasicTranscription();
        try {
            bt.BasicTranscriptionFromString(toXML());
        } catch (TransformerException | SAXException | JexmaraldaException ex) {
            Logger.getLogger(EXBTranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bt;
    }
    

    @Override
    public int getNumberOfTokens() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getNumberOfTypes() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public double getStartTime() {
        try {
            BasicTranscription bt = getBasicTranscription();
            String minTimeID = bt.getBody().getCommonTimeline().getMinTimeID();
            double minTime = bt.getBody().getCommonTimeline().getTimelineItemWithID(minTimeID).getTime();
            return minTime;
        } catch (JexmaraldaException ex) {
            Logger.getLogger(EXBTranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    }

    @Override
    public double getEndTime() {
        BasicTranscription bt = getBasicTranscription();
        return bt.getBody().getCommonTimeline().getMaxTime();
    }

    @Override
    public double getTimeForID(String id) {
        try {
            BasicTranscription bt = getBasicTranscription();
            TimelineItem tli = bt.getBody().getCommonTimeline().getTimelineItemWithID(id);
            if (tli.getTime()>=0){
                return tli.getTime();
            }
            return bt.getBody().getCommonTimeline().getPreviousTime(id);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(EXBTranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    }

    @Override
    public Transcript getPart(String id1, String id2, boolean expandToFullAnnotationBlock) {
        BasicTranscription bt = getBasicTranscription();
        BasicTranscription partOfTranscription = bt.getPartOfTranscription(bt.getBody().getAllTierIDs(), id1, id2);
        return new EXBTranscript(partOfTranscription.toXML());
    }

    @Override
    public Transcript getPart(double time1, double time2, boolean expandToFullAnnotationBlock) {
        BasicTranscription bt = getBasicTranscription();
        int pos1 = bt.getBody().getCommonTimeline().getPositionForTime(time1);
        int pos2 = bt.getBody().getCommonTimeline().getPositionForTime(time2);
        String id1 = bt.getBody().getCommonTimeline().getTimelineItemAt(pos1).getID();
        String id2 = bt.getBody().getCommonTimeline().getTimelineItemAt(pos2).getID();
        return getPart(id1, id2, expandToFullAnnotationBlock);        
    }

    @Override
    public TokenList getTokenList(String type) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public TokenList getTokenList(String type, TokenFilter filter) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Element getElementById(String id) {
        try {
            Element element = ((Element)xPath.evaluate("//*[@id='" + id + "']", getDocument().getDocumentElement(), XPathConstants.NODE));
            return element;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(EXBTranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public NodeList getTokensByPOS(String pos) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public NodeList getAllTokens() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public NodeList getAnchorsByAttribute(String attribute) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public NodeList getAnnotationBlocksBySpeaker(String speaker) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getSpeakerInitialsBySpeakerID(String speakerID) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getSpeakerIDBySpeakerInitials(String speakerInitials) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getAnnotationBlockID(String annotationBlockID, int distance) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getFirstAnnotationBlockIDForTime(double time) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeAnnotations() {
        BasicTranscription bt = getBasicTranscription();
        String[] tiersOfTypeA = bt.getBody().getTiersOfType("a");
        for (String tierID : tiersOfTypeA){
            try {            
                bt.getBody().removeTierWithID(tierID);
            } catch (JexmaraldaException ex) {
                Logger.getLogger(EXBTranscript.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        setXML(bt.toXML());
    }

    @Override
    public String getLanguage() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setTimelineToZero() {
        BasicTranscription bt = getBasicTranscription();
        bt.getBody().getCommonTimeline().shiftAbsoluteTimes(-getStartTime());
        setXML(bt.toXML());
        
    }


    @Override
    public String getID() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Document getXmlDocument() {
        return super.getDocument();
    }
    
}
