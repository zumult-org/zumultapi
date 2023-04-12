/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;
import org.zumult.io.IOHelper;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Protocol;
import org.zumult.objects.Speaker;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DGD2Corpus;
import org.zumult.objects.implementations.DGD2Event;
import org.zumult.objects.implementations.DGD2Protocol;
import org.zumult.objects.implementations.DGD2Speaker;
import org.zumult.objects.implementations.ISOTEITranscript;


/**
 *
 * @author Thomas_Schmidt
 */
public class AGDFileSystem extends AbstractIDSBackend {
    
    File transcriptRootFolder;
    File protocolRootFolder;
    File corpusMetadataRootFolder;
    File eventMetadataRootFolder;
    File speakerMetadataRootFolder;

    public AGDFileSystem() {
        transcriptRootFolder = new File(Configuration.getTranscriptPath());
        protocolRootFolder = new File(Configuration.getProtocolPath());
        corpusMetadataRootFolder = new File(new File(new File(Configuration.getMetadataPath()), "corpora"), "extern");
        eventMetadataRootFolder = new File(new File(new File(Configuration.getMetadataPath()), "events"), "extern");
        speakerMetadataRootFolder = new File(new File(new File(Configuration.getMetadataPath()), "speakers"), "extern");
    }


    @Override
    public IDList getEvents4Corpus(String corpusID) throws IOException {
        // Alternative: get from Josip's index
        /*try {
            String path = "/IDLists/" + corpusID + "/" + corpusID + "_events.xml";
            String xml = new Scanner(DGD2Oracle.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            IDList result = new IDList("Event");
            result.readXML(xml);
            return result;
        } catch (SAXException | ParserConfigurationException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }*/
        IDList result = new IDList("Event");
        File eventMetadataFolder = new File(eventMetadataRootFolder, corpusID.replaceAll("(\\-)+", ""));
        System.out.println(eventMetadataFolder.getAbsolutePath());
        File[] files = eventMetadataFolder.listFiles((File dir, String name) -> name.endsWith("_extern.xml"));
        for (File f : files){
            // FOLK_E_00001_extern.xml
            String eventID = f.getName().substring(0, 12);
            result.add(eventID);
        }
        return result;
        
    }

    @Override
    public IDList getSpeakers4Corpus(String corpusID) throws IOException {
        IDList result = new IDList("Speaker");
        File eventMetadataFolder = new File(speakerMetadataRootFolder, corpusID.replaceAll("(\\-)+", ""));
        File[] files = eventMetadataFolder.listFiles((File dir, String name) -> name.endsWith("_extern.xml"));
        if (files==null){
            // N.B. a few corpora don't have speakers at all
            return new IDList("Speaker");
        }
        for (File f : files){
            // FOLK_E_00001_extern.xml
            String speakerID = f.getName().substring(0, 12);
            result.add(speakerID);
        }
        return result;
    }

    @Override
    public IDList getSpeechEvents4Event(String eventID) throws IOException {        
        return getEvent(eventID).getSpeechEvents();
    }

    @Override
    public Corpus getCorpus(String corpusID) throws IOException {
        File corpusFile = new File(corpusMetadataRootFolder, corpusID + "_extern.xml");
        String corpusXML = IOHelper.readUTF8(corpusFile);
        Corpus corpus = new DGD2Corpus(corpusXML);
        return corpus;
    }

    @Override
    public Event getEvent(String eventID) throws IOException {
        File corpusFolder = new File(eventMetadataRootFolder, eventID.substring(0,4).replaceAll("(\\-)+", ""));
        File metadataFile = new File(corpusFolder, eventID + "_extern.xml");
        String metadataXML = IOHelper.readUTF8(metadataFile);
        Event event = new DGD2Event(metadataXML);
        return event;
    }

    @Override
    public Speaker getSpeaker(String speakerID) throws IOException {
        File corpusFolder = new File(speakerMetadataRootFolder, speakerID.substring(0,4).replaceAll("(\\-)+", ""));
        File metadataFile = new File(corpusFolder, speakerID + "_extern.xml");
        String metadataXML = IOHelper.readUTF8(metadataFile);
        Speaker speaker = new DGD2Speaker(metadataXML);
        return speaker;
    }
        

    @Override
    public Transcript getTranscript(String transcriptID) throws IOException {
        File corpusFolder = new File(transcriptRootFolder, transcriptID.substring(0,4).replaceAll("-", ""));
        //File corpusFolder = new File(transcriptRootFolder, transcriptID.substring(0,4));
        File transcriptFile = new File(corpusFolder, transcriptID + "_DF_01.xml");
        
        if (!transcriptFile.exists()) throw new IOException("No transcript with ID " + transcriptID + " in " + corpusFolder);
        
        String transcriptXML = IOHelper.readUTF8(transcriptFile);
        Transcript transcript = new ISOTEITranscript(transcriptXML);
        return transcript;
    }

    @Override
    public IDList getTranscripts4SpeechEvent(String speechEventID) throws IOException {
        return getSpeechEvent(speechEventID).getTranscripts();
    }

    @Override
    public IDList getAudios4SpeechEvent(String speechEventID) throws IOException {
        IDList allMedia = getSpeechEvent(speechEventID).getMedia();
        IDList result = new IDList("media");
        for (String id : allMedia){
            if (id.contains("_A_")) result.add(id);
        }
        return result;
    }

    @Override
    public IDList getVideos4SpeechEvent(String speechEventID) throws IOException {
        IDList allMedia = getSpeechEvent(speechEventID).getMedia();
        IDList result = new IDList("media");
        for (String id : allMedia){
            if (id.contains("_V_")) result.add(id);
        }
        return result;
    }

    @Override
    public IDList getTranscripts4Audio(String audioID) throws IOException {
        IDList result = new IDList("media");
        try {
            //FOLK_E_00001_SE_01
            String speechEventID = audioID.substring(0, 18);
            Document speechEventDocument = getSpeechEvent(speechEventID).getDocument();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xpathToAudioReferences = "//Transkript[descendant::Relation_zu_SE-Aufnahme[@Kennung_SE-Aufnahme='" + audioID + "']]";
            NodeList nodes = (NodeList)xPath.evaluate(xpathToAudioReferences, speechEventDocument.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){
                Element element = ((Element)(nodes.item(i)));
                result.add(element.getAttribute("Kennung"));
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        return result;    
    }

    @Override
    public IDList getTranscripts4Video(String videoID) throws IOException {
        // there's just no difference...
        return getTranscripts4Audio(videoID);
    }

    @Override
    public IDList getAudios4Transcript(String transcriptID) throws IOException {
        IDList result = new IDList("media");
        try {
            //FOLK_E_00001_SE_01
            String speechEventID = transcriptID.substring(0, 18);
            Document speechEventDocument = getSpeechEvent(speechEventID).getDocument();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xpathToAudioReferences = "//Transkript[@Kennung='" + transcriptID + "']/descendant::Relation_zu_SE-Aufnahme[contains(@Kennung_SE-Aufnahme, '_A_')]";
            NodeList nodes = (NodeList)xPath.evaluate(xpathToAudioReferences, speechEventDocument.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){
                Element element = ((Element)(nodes.item(i)));
                result.add(element.getAttribute("Kennung_SE-Aufnahme"));
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        return result;    
    }

    @Override
    public IDList getVideos4Transcript(String transcriptID) throws IOException {
        IDList result = new IDList("media");
        try {
            //FOLK_E_00001_SE_01
            String speechEventID = transcriptID.substring(0, 18);
            Document speechEventDocument = getSpeechEvent(speechEventID).getDocument();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xpathToAudioReferences = "//Transkript[@Kennung='" + transcriptID + "']/descendant::Relation_zu_SE-Aufnahme[contains(@Kennung_SE-Aufnahme, '_V_')]";
            NodeList nodes = (NodeList)xPath.evaluate(xpathToAudioReferences, speechEventDocument.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){
                Element element = ((Element)(nodes.item(i)));
                result.add(element.getAttribute("Kennung_SE-Aufnahme"));
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        return result;    
    }

    @Override
    public IDList getSpeakers4SpeechEvent(String speechEventID) throws IOException {
        return getSpeechEvent(speechEventID).getSpeakers();
    }

    
    @Override
    public IDList getAvailableValues(String corpusID, String metadataKeyID) {
        IDList list = new IDList("AvailableValue");
        try {
            String path = "/data/AGDAvailableMetadataValues.xml";
            String xml = new Scanner(AGDFileSystem.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);

            // Query for the right elements via XPath
            /*
               <corpus corpus="BR--">
                    <key id="e_sonstige_bezeichnungen" name="Sonstige Bezeichnungen">
                        <value freq="1">B54W</value>
            */
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//corpus[@corpus='" + corpusID + "']/key[@id='" + metadataKeyID + "']/value";
            NodeList nodes = (NodeList)xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){
                //Element parameterNameElement = ((Element)(nodes.item(i)));
                //result.add(parameterNameElement.getTextContent().substring(2));  
                Element element = ((Element)(nodes.item(i)));
                list.add(element.getTextContent());
            }
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;

    }
    
    /*@Override
    public String getMetadataValue(String level, String DGDObjectID, String metadatum) {
        // level = v_se_id
        // DGDObjectID = FOLK_E_00001_SE_01
        // metadatum = 
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    /*@Override
    public String getMetadataValue(String level, String DGDObjectID, String metadatum, String speakerID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/


    @Override
    public String getDescription() {
        return "AGD@IDS Backend using the plain file system";        
    }

    @Override
    public Protocol getProtocol(String protocolID) throws IOException {
        File corpusFolder = new File(protocolRootFolder, protocolID.substring(0,4).replaceAll("-", ""));
        //File corpusFolder = new File(transcriptRootFolder, transcriptID.substring(0,4));
        File protocolFile = new File(corpusFolder, protocolID + "_DF_01.xml");
        
        if (!protocolFile.exists()) throw new IOException("No protocol with ID " + protocolID);
        
        String protocolXML = IOHelper.readUTF8(protocolFile);
        Protocol protocol = new DGD2Protocol(protocolXML);
        return protocol;
    }

    @Override
    public String getProtocol4SpeechEvent(String speechEventID) throws IOException {
        return getSpeechEvent(speechEventID).getProtocol();
    }

    

}
