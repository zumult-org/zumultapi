/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.objects.IDList;
import org.zumult.objects.Measure;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.Transcript;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2SpeechEvent extends AbstractXMLObject implements SpeechEvent {


    XPath xPath = XPathFactory.newInstance().newXPath();
    BackendInterface backend; 

    public DGD2SpeechEvent(Document xmlDocument) {
        super(xmlDocument);
        try {
            backend = BackendInterfaceFactory.newBackendInterface();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DGD2SpeechEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }        
    

    @Override
    public String getType() {
        return getMetadataValue(backend.findMetadataKeyByID("v_e_se_art"));
    }

    @Override
    public List<String> getTopics() {
        String themen = getMetadataValue(backend.findMetadataKeyByID("v_e_se_themen"));
        String[] tokenizedThemen = themen.split(" ; ");
        ArrayList<String> returnValue = new ArrayList<>();
        returnValue.addAll(Arrays.asList(tokenizedThemen));
        return returnValue;
    }

    @Override
    public String getContentSummary() {
        return getMetadataValue(backend.findMetadataKeyByID("v_e_se_inhalt"));
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
    public String getID() {
        return getDocument().getDocumentElement().getAttribute("Kennung");
    }

    @Override
    public IDList getTranscripts() {
        IDList result = new IDList("transcript");
        try {
            String xPathString = "//Transkript";
            NodeList transcriptNodeList = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<transcriptNodeList.getLength(); i++){
                Element tElement = (Element)(transcriptNodeList.item(i));
                String tID = tElement.getAttribute("Kennung");
                result.add(tID);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Event.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getMedia() {
        IDList result = new IDList("media");
        try {
            String xPathString = "//SE-Aufnahme";
            NodeList seRecordingNodeList = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<seRecordingNodeList.getLength(); i++){
                Element rElement = (Element)(seRecordingNodeList.item(i));
                String rID = rElement.getAttribute("Kennung");
                result.add(rID);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Event.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getSpeakers() {
        IDList result = new IDList("speakerInSpeechEvent");
        try {
            String xPathString = "//Sprecher";
            NodeList speakerNodeList = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<speakerNodeList.getLength(); i++){
                Element sElement = (Element)(speakerNodeList.item(i));
                String sID = sElement.getAttribute("Kennung");
                result.add(sID);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Event.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public String getProtocol() {
        return this.getID() + "_P_01";
    }

    @Override
    public String[] getCoordinatesForTime(double time) {        
        int i=0;
        for (String transcriptID : getTranscripts()){
            try {
                Transcript transcript = backend.getTranscript(transcriptID);
                double startTime = transcript.getStartTime();
                double endTime = transcript.getEndTime();
                if ((startTime<=time) && (time<=endTime)){
                    String annotationBlockID = transcript.getFirstAnnotationBlockIDForTime(time);
                    String[] result = {transcriptID, annotationBlockID};
                    return result;
                }
                if ((i==0) && (time<startTime)){
                    String annotationBlockID = transcript.getFirstAnnotationBlockIDForTime(time);
                    String[] result = {transcriptID, annotationBlockID};
                    return result;                    
                }
                i++;
            } catch (IOException ex) {
                Logger.getLogger(DGD2SpeechEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }            
        return null;
    }

    @Override
    public Measure getMeasure(String type, String reference) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Measure getMeasureValue(String type, String reference, String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
