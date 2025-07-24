/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;
import org.zumult.backend.MetadataFinderInterface;
import org.zumult.backend.VirtualCollectionStore;
import org.zumult.io.COMAUtilities;
import org.zumult.io.Constants;
import org.zumult.io.FileIO;
import org.zumult.io.IOHelper;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Measure;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Protocol;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.COMACommunication;
import org.zumult.objects.implementations.COMACorpus;
import org.zumult.objects.implementations.COMAMedia;
import org.zumult.objects.implementations.COMASpeaker;
import org.zumult.objects.implementations.COMATranscript;
import org.zumult.objects.implementations.DefaultTokenList;
import org.zumult.objects.implementations.EXBTranscript;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.KWIC;
import org.zumult.query.SearchStatistics;
import org.zumult.query.Searcher;
import org.zumult.query.implementations.COMAKWIC;
import org.zumult.query.implementations.COMASearcher;

/**
 *
 * @author thomas.schmidt
 */
public class COMAFileSystem extends AbstractBackend implements MetadataFinderInterface {
    
    XPath xPath = XPathFactory.newInstance().newXPath();
    File topFolder = new File(Configuration.getMetadataPath());
    
    static final Map<String, String> id2Corpus = new HashMap<>();
    static final Map<String, String> id2parentID = new HashMap<>();
    
    static final Map<String, Corpus> corpusID2Corpus = new HashMap<>();
    
    // added for #177
    static final Map<String, MetadataKey> foundMetadataKeys = new HashMap<>();
    
    static {
        try {
            // Make a map with IDs
            COMAFileSystem comaFileSystem = new COMAFileSystem();
            IDList corpora = comaFileSystem.getCorpora();
            for (String corpusID : corpora){
                System.out.println("[COMAFileSystem] Indexing IDs for " + corpusID + " started. ");
                comaFileSystem.indexIDs(corpusID, id2Corpus, id2parentID);                
                System.out.println("[COMAFileSystem] Calculating statistics for " + corpusID + " started. ");
                comaFileSystem.calculateStatistics(corpusID);
            }
        } catch (IOException | XPathExpressionException | SAXException | ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getID() {
        return "exmaralda.org.coma";
    }

    @Override
    public String getName() {
        return "COMA Backend";
    }

    @Override
    public String getAcronym() {
        return "CoMa";
    }

    @Override
    public String getDescription() {
        return "Backend using EXMARaLDA Coma as the central database and the plain file system";        
    }

    @Override
    public Corpus getCorpus(String corpusID) throws IOException {
        //File topFolder = new File("N:\\Workspace\\HZSK");
        // this is way too slow, the corpus is read over and over again
        if (corpusID2Corpus.containsKey(corpusID)){
            return corpusID2Corpus.get(corpusID);
        }
        System.out.println("Getting corpus: " + corpusID);
        File corpusFolder = new File(topFolder, corpusID);
        File comaFile = new File(corpusFolder, corpusID + ".coma");
        //System.out.println("Coma file: " + comaFile.getAbsolutePath());
        String comaXML = IOHelper.readUTF8(comaFile);
        Corpus corpus = new COMACorpus(comaXML);
        corpusID2Corpus.put(corpusID, corpus);
        return corpus;
    }

    @Override
    public Event getEvent(String eventID) throws IOException {
        // can this be right? can we just return the speech event 
        // because it is a 1:1 relation? Well, let's see
        return (COMACommunication) getSpeechEvent(eventID);        
    }

    @Override
    public SpeechEvent getSpeechEvent(String speechEventID) throws IOException {
        try {
            String corpusID = findCorpusID(speechEventID);
            //System.out.println("CorpusID " + corpusID);
            // make this robust
            if (corpusID==null){
                //System.out.println("Error: No corpus found for: " + transcriptID);
                throw new IOException("Error: No corpus found for: " + speechEventID);
            }
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();
            
            System.out.println("Looking for speech event " + speechEventID + " in corpus " + corpusID);
            
            String xp = "//Communication[@Id='" + speechEventID + "']";
            Element communicationElement = (Element) (Node) xPath.evaluate(xp, corpusDocument.getDocumentElement(), XPathConstants.NODE);

            // build a new document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Node importedNode = doc.importNode(communicationElement, true);
            doc.appendChild(importedNode);
            COMACommunication speechEvent = new COMACommunication(doc);
            return speechEvent;
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Speaker getSpeaker(String speakerID) throws IOException {
        try {
            String corpusID = findCorpusID(speakerID);
            // make this robust
            if (corpusID==null){
                //System.out.println("Error: No corpus found for: " + transcriptID);
                throw new IOException("Error: No corpus found for: " + speakerID);
            }
            
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();
            
            String xp = "//Speaker[@Id='" + speakerID + "']";
            Element communicationElement = (Element) (Node) xPath.evaluate(xp, corpusDocument.getDocumentElement(), XPathConstants.NODE);

            // build a new document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Node importedNode = doc.importNode(communicationElement, true);
            doc.appendChild(importedNode);
            COMASpeaker speaker = new COMASpeaker(doc);
            return speaker;
        } catch (XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Media getMedia(String mediaID) throws IOException {
        try {
            String corpusID = findCorpusID(mediaID);
            // make this robust
            if (corpusID==null){
                //System.out.println("Error: No corpus found for: " + transcriptID);
                throw new IOException("Error: No corpus found for: " + mediaID);
            }
            
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();
            
            /* <Media Id="MID6410DC54-06CA-E52C-F05A-A16E867ECB5D">
            <NSLink>Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin.mp3</NSLink> */
            
            String xp = "//Media[@Id='" + mediaID + "']";
            Element mediaElement = (Element) (Node) xPath.evaluate(xp, corpusDocument.getDocumentElement(), XPathConstants.NODE);
            if (mediaElement!=null){
                Element descriptionElement = (Element) mediaElement.getElementsByTagName("Description").item(0);
                String descriptionXML = IOHelper.ElementToString(descriptionElement);
                String nsLink = mediaElement.getElementsByTagName("NSLink").item(0).getTextContent();

                // 2025-04-08 change for issue #246
                if (!COMAUtilities.isHttpLink(nsLink)){
                    File corpusFolder = new File(topFolder, corpusID);
                    String fileString = corpusFolder.toPath().resolve(nsLink).toString();
                    String urlString = Configuration.getMediaPath() + "/" + corpusID + "/" + nsLink;
                    return new COMAMedia(mediaID, urlString, fileString, descriptionXML);
                } else {
                    return new COMAMedia(mediaID, nsLink, null, descriptionXML);                    
                }
                                
            } else {
                // 07-06-2024
                // this is a fallback in case the ID of the recording, not the ID of the media was provided
                // not sure if this is a good idea
                String xp2 = "//Recording[@Id='" + mediaID + "']/Media[1]";
                Element mediaElement2 = (Element) (Node) xPath.evaluate(xp2, corpusDocument.getDocumentElement(), XPathConstants.NODE);
                if (mediaElement2!=null){
                    Element descriptionElement = (Element) mediaElement2.getElementsByTagName("Description").item(0);
                    String descriptionXML = IOHelper.ElementToString(descriptionElement);
                    String nsLink = mediaElement2.getElementsByTagName("NSLink").item(0).getTextContent();
                    //File corpusFolder = new File(topFolder, corpusID);
                    //String urlString = corpusFolder.toPath().resolve(nsLink).toUri().toURL().toString();

                    // 2025-04-08 change for issue #246
                    if (!COMAUtilities.isHttpLink(nsLink)){
                        File corpusFolder = new File(topFolder, corpusID);
                        String fileString = corpusFolder.toPath().resolve(nsLink).toString();
                        String urlString = Configuration.getMediaPath() + "/" + corpusID + "/" + nsLink;
                        return new COMAMedia(mediaID, urlString, fileString, descriptionXML);
                    } else {
                        return new COMAMedia(mediaID, nsLink, null, descriptionXML);                    
                    }
                }
            }
            
        } catch (XPathExpressionException | TransformerException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Transcript getTranscript(String transcriptID) throws IOException {
        try {
            // Let's do it like this:
            // get the transcript with this ID, then resolve the path
            // but assume that the file suffix is xml
            // trouble is: we do not know in which corpus to look
            // we may need an indexer again...
            // or: we can iterate through all COMA files?
            // Shouldn't be that bad, so let's try
            String corpusID = findCorpusID(transcriptID);            
            if (corpusID==null){
                //System.out.println("Error: No corpus found for: " + transcriptID);
                throw new IOException("Error: No corpus found for: " + transcriptID);
            }
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();

            /* <Transcription Id="CIDID93045167-C4FF-8EF6-36B8-F08AC9F9E331">
               <NSLink>Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin.exb</NSLink> */

            String xp = "//Transcription[@Id='" + transcriptID + "']";
            Element transcriptionElement = (Element) (Node) xPath.evaluate(xp, corpusDocument.getDocumentElement(), XPathConstants.NODE);
            String nsLink = transcriptionElement.getElementsByTagName("NSLink").item(0).getTextContent();
            
            // 2025-04-08 change for issue #246
            if (!(COMAUtilities.isHttpLink(nsLink))){
                File corpusFolder = new File(topFolder, corpusID);
                String nsLinkModified = nsLink.substring(0, nsLink.lastIndexOf(".")) + ".xml";
                File resolvedPath = corpusFolder.toPath().resolve(nsLinkModified).toFile();

                if (!(resolvedPath.exists())){
                    throw new IOException("Error: No transcript found for: " + transcriptID);
                }

                String xmlString = IOHelper.readUTF8(resolvedPath);
                String metadataString = IOHelper.ElementToString(transcriptionElement);

                return new COMATranscript(xmlString, metadataString);
            } else {
                String xmlString = IOHelper.httpReadUTF8(nsLink); 
                String metadataString = IOHelper.ElementToString(transcriptionElement);

                return new COMATranscript(xmlString, metadataString);                
            }

        } catch (XPathExpressionException | TransformerException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public Transcript getTranscript(String transcriptID, Transcript.TranscriptFormats transcriptFormat) throws IOException {
        try {
            switch (transcriptFormat){
                case ISOTEI :
                    return getTranscript(transcriptID);
                case EXB :
                    String corpusID = findCorpusID(transcriptID);
                    if (corpusID==null){
                        throw new IOException("Error: No corpus found for: " + transcriptID);
                    }
                    Corpus corpus = getCorpus(corpusID);
                    Document corpusDocument = corpus.getDocument();
                    String xp = "//Transcription[@Id='" + transcriptID + "']";
                    Element transcriptionElement = (Element) (Node) xPath.evaluate(xp, corpusDocument.getDocumentElement(), XPathConstants.NODE);
                    String nsLink = transcriptionElement.getElementsByTagName("NSLink").item(0).getTextContent();
                    File corpusFolder = new File(topFolder, corpusID);
                    String nsLinkModified = nsLink.substring(0, nsLink.lastIndexOf(".")) + ".exb";
                    File resolvedPath = corpusFolder.toPath().resolve(nsLinkModified).toFile();
                    
                    if (!(resolvedPath.exists())){
                        throw new IOException("Error: No transcript found for: " + transcriptID);
                    }
                    
                    String xmlString = IOHelper.readUTF8(resolvedPath);                    
                    return new EXBTranscript(xmlString);
                case EAF : 
                    
            }
            return super.getTranscript(transcriptID, transcriptFormat); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }
    
    

    @Override
    public IDList getCorpora() throws IOException {
        File[] listFiles = topFolder.listFiles(new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                if (!(pathname.isDirectory())) return false;
                File findComa = new File(pathname, pathname.getName() + ".coma" );
                return (findComa.exists());
            }
            
        });
        IDList result = new IDList("Corpus");
        for (File file : listFiles){
            result.add(file.getName());
        }
        return result;        
    }

    @Override
    public IDList getEvents4Corpus(String corpusID) throws IOException {
        IDList result = new IDList("Event");
        try {
            Corpus corpus = getCorpus(corpusID);               
            NodeList allCommunications = (NodeList) xPath.evaluate("//Communication", corpus.getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allCommunications.getLength(); i++){
                Element communicationElement = ((Element)(allCommunications.item(i)));
                result.add(communicationElement.getAttribute("Id"));
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getSpeakers4Corpus(String corpusID) throws IOException {
        IDList result = new IDList("Speaker");
        try {
            Corpus corpus = getCorpus(corpusID);
            NodeList allSpeakers = (NodeList) xPath.evaluate("//Speaker", corpus.getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allSpeakers.getLength(); i++){
                Element speakerElement = ((Element)(allSpeakers.item(i)));
                result.add(speakerElement.getAttribute("Id"));
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getSpeechEvents4Event(String eventID) throws IOException {
        // haha, this is simple:
        IDList list = new IDList("SpeechEvent");
        list.add(eventID);
        return list;        
    }

    @Override
    public IDList getTranscripts4SpeechEvent(String speechEventID) throws IOException {
        try {
            IDList result = new IDList("Transcript");
            SpeechEvent communication = getSpeechEvent(speechEventID);
            /* <Transcription Id="CIDID74BCDD4E-1A7D-05AB-22B3-C0792A3848EC">
            <Name>MT_270110_Shirin</Name>
            <Filename>MT_270110_Shirin_s.exs</Filename>
            */
            // N.B.: ends-with is not present in XPath 1.0, and we don't have support for XPath 2.0
            /*NodeList allTranscripts = 
                    (NodeList) xPath.evaluate("//Transcription[substring(Filename, string-length(Filename)-3)='.exb' or substring(Filename, string-length(Filename)-3)='.EXB']", 
                            communication.getDocument().getDocumentElement(), XPathConstants.NODESET);*/
            String xpath = "//Transcription[substring(Filename, string-length(Filename)-3)='.xml' or substring(Filename, string-length(Filename)-3)='.XML']";
            NodeList allTranscripts = 
                    (NodeList) xPath.evaluate(xpath, 
                            communication.getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allTranscripts.getLength(); i++){
                Element transcriptElement = ((Element)(allTranscripts.item(i)));
                result.add(transcriptElement.getAttribute("Id"));
            }
            return result;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getAudios4SpeechEvent(String speechEventID) throws IOException {
        try {
            // need to distinguish audio and video -- not so easy...
            // let's say for now that we only need WAV files
            IDList result = new IDList("Media");
            SpeechEvent communication = getSpeechEvent(speechEventID);
            /*
            <Media Id="MID7D516D45-D94A-24EF-8D21-4B0A4DC891CA">
            <Description>
            <Key Name="Type">digital</Key>
            </Description>
            <NSLink>Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin.wav</NSLink>
            <Filename>MT_270110_Shirin.wav</Filename>
            
            */
            NodeList allAudios = (NodeList)
                    xPath.evaluate("//Media[substring(Filename, string-length(Filename)-3)='.wav' "
                            + "or substring(Filename, string-length(Filename)-3)='.WAV' "
                            + "or substring(NSLink, string-length(NSLink)-3)='.WAV' "
                            + "or substring(NSLink, string-length(NSLink)-3)='.wav' "
                            + "or substring(Filename, string-length(Filename)-3)='.MP3' "
                            + "or substring(Filename, string-length(Filename)-3)='.mp3' "
                            + "or substring(NSLink, string-length(NSLink)-3)='.MP3' "
                            + "or substring(NSLink, string-length(NSLink)-3)='.mp3' "
                            + "]", communication.getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allAudios.getLength(); i++){
                Element mediaElement = ((Element)(allAudios.item(i)));
                result.add(mediaElement.getAttribute("Id"));
            }
            return result;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getVideos4SpeechEvent(String speechEventID) throws IOException {
        try {
            // need to distinguish audio and video -- not so easy...
            // let's say for now that we only need WAV files
            IDList result = new IDList("Media");
            SpeechEvent communication = getSpeechEvent(speechEventID);
            /*
            <Media Id="MID7D516D45-D94A-24EF-8D21-4B0A4DC891CA">
            <Description>
            <Key Name="Type">digital</Key>
            </Description>
            <NSLink>Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin.wav</NSLink>
            <Filename>MT_270110_Shirin.wav</Filename>
            
            */
            NodeList allVideos = (NodeList)
                    xPath.evaluate("//Media[substring(NSLink, string-length(NSLink)-3)='.mp4' or substring(NSLink, string-length(NSLink)-3)='.MP4']", communication.getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allVideos.getLength(); i++){
                Element mediaElement = ((Element)(allVideos.item(i)));
                result.add(mediaElement.getAttribute("Id"));
            }
            return result;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getTranscripts4Audio(String audioID) throws IOException {
        // This may be difficult because there is no mapping for this
        // it's two up, it seems
        String speechEventID = id2parentID.get(id2parentID.get(audioID));
        return getTranscripts4SpeechEvent(speechEventID);
    }

    @Override
    public IDList getTranscripts4Video(String videoID) throws IOException {
        // This may be difficult because there is no mapping for this
        // it's two up, it seems
        String speechEventID = id2parentID.get(id2parentID.get(videoID));
        return getTranscripts4SpeechEvent(speechEventID);
    }

    @Override
    public IDList getAudios4Transcript(String transcriptID) throws IOException {
        // This may be difficult because there is no mapping for this
        // oh yes, it is difficult
        // for the time being, let us look if the audio for the speech event 
        // contains a file with the same name as the transcript?
        //System.out.println("Getting audios 4 speech event: " + transcriptID);
        return getAudios4SpeechEvent(getSpeechEvent4Transcript(transcriptID));
    }

    @Override
    public IDList getVideos4Transcript(String transcriptID) throws IOException {
        // This may be difficult because there is no mapping for this
        return getVideos4SpeechEvent(getSpeechEvent4Transcript(transcriptID));
    }

    @Override
    public IDList getSpeakers4SpeechEvent(String speechEventID) throws IOException {
        try {
            /* <Communication Id="CID06C693EF-494A-913E-EBF1-6C9618CDCC46" Name="MT_270110_Shirin">
            <Setting>
            <Person>SID7BB74AB1-B14F-C3A7-AE69-DC9613EA0C6F</Person>
            <Person>SIDD6FCCC2F-B476-548E-140C-55C87444CEBD</Person>
            <Description/>
            </Setting>*/
            IDList result = new IDList("Speaker");
            SpeechEvent communication = getSpeechEvent(speechEventID);
            NodeList allSpeakers = (NodeList) xPath.evaluate("//Person", communication.getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allSpeakers.getLength(); i++){
                Element personElement = ((Element)(allSpeakers.item(i)));
                result.add(personElement.getTextContent());
            }
            return result;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getAvailableValues(String corpusID, MetadataKey metadataKey) {
        //return getAvailableValues(corpusID, metadataKey.getID());
        // changed for #178
        return getAvailableValues(corpusID, metadataKey.getName("en"));
    }

    @Override
    public IDList getAvailableValues(String corpusID, String metadataKeyID) {
        try {
            Corpus corpus = getCorpus(corpusID);
            Document comaDocument = corpus.getDocument();
            Set<String> valueSet = new HashSet<>();
            // this will go wrong, for example if both Transcript and Speaker have a key "name"!
            String xp = "//Key[@Name='" + metadataKeyID + "']";
            //System.out.println("Evaluating " + xp);
            NodeList allKeys = (NodeList) xPath.evaluate(xp, comaDocument, XPathConstants.NODESET);
            for (int i=0; i<allKeys.getLength(); i++){
                Element keyElement = ((Element)(allKeys.item(i)));
                valueSet.add(keyElement.getTextContent());
            }
            IDList result = new IDList("AvailableValue");
            result.addAll(valueSet);
            return result;
            
            
        } catch (IOException | XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // 07-07-2022, removed, issue #41
    /* @Override
    public MediaMetadata getMediaMetadata4Media(String eventID, String mediaID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    // 07-07-2022, removed, issue #41
    /* @Override
    public TranscriptMetadata getTranscriptMetadata4Transcript(String eventID, String transcriptID) {
        try {
            String corpusID = findCorpusID(eventID);
            Document comaDocument = getCorpus(corpusID).getDocument();
            /*
                <Transcription Id="CIDID74BCDD4E-1A7D-05AB-22B3-C0792A3848EC">
                   <Name>MT_270110_Shirin</Name>
                   <Filename>MT_270110_Shirin_s.exs</Filename>
                   <NSLink>Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin_s.exs</NSLink>
                   <Availability>
                      <Available>false</Available>
                      <ObtainingInformation/>
                   </Availability>
                   <Description>
                      <Key Name="# EXB-SOURCE">Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin.exb</Key>
                      <Key Name="# e">642</Key>
                      <Key Name="# sc">71</Key>
                      <Key Name="Alignment status">fully aligned</Key>
                      <Key Name="Annotation type: disfluency"            
            */
        /*} catch (IOException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        // difficulty here: need to implement TranscriptMetadata for Coma
        // not sure how to do this yet
        return null;
    }*/

    // 07-07-2022, removed, issue #41
    /* @Override
    public AdditionalMaterialMetadata getAdditionalMaterialMetadata4Corpus(String corpusID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } */



    @Override
    public Speaker getSpeakerInSpeechEvent(String speechEventID, String speakerID) {
        // COMA doesn't have this, so let's just return an empty speaker with this ID
        String xmlString ="<Speaker Id='" + speakerID + "'/>";
        return new COMASpeaker(xmlString);
    }

    private String findCorpusID(String someID) throws IOException {
        //System.out.println("Trying to find corpus ID for " + someID);
        return id2Corpus.get(someID);
        /*for (String corpusID : getCorpora()){            
            try {
                Corpus tryCorpus = getCorpus(corpusID);
                Document tryCorpusDocument = tryCorpus.getDocument();
                String xp = "//*[@Id='" + someID + "']";
                Element tryElement = (Element) (Node) xPath.evaluate(xp, tryCorpusDocument.getDocumentElement(), XPathConstants.NODE);
                if (tryElement!=null){
                    return corpusID;
                }
            } catch (XPathExpressionException ex) {
                Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        return null;*/
    }


    @Override
    public MetadataKey findMetadataKeyByID(String id) {
        // changed for #177
        if (foundMetadataKeys.containsKey(id)){
            return foundMetadataKeys.get(id);
        }
        try {
            IDList corpora = getCorpora();
            for (String corpusID : corpora){
                Corpus corpus = getCorpus(corpusID);
                for (MetadataKey key : corpus.getMetadataKeys()){
                    if (key.getID().equals(id)) {
                        foundMetadataKeys.put(id, key);
                        return key;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getSpeechEvent4Transcript(String transcriptID) throws IOException {
        try {
            String corpusID = findCorpusID(transcriptID);
            System.out.println("CorpusID for " + transcriptID + "=" + corpusID);
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();
            String xp = "//Transcription[@Id='" + transcriptID + "']/ancestor::Communication";
            Element tryElement = (Element) (Node) xPath.evaluate(xp, corpusDocument, XPathConstants.NODE);
            if (tryElement!=null){
                return tryElement.getAttribute("Id");
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getEvent4SpeechEvent(String speechEventID) throws IOException {
        return speechEventID;
    }

    @Override
    public VirtualCollectionStore getVirtualCollectionStore() {
        return new FileSystemVirtualCollectionStore();
    }

    @Override
    public String getCorpus4Event(String eventID) throws IOException {
        return id2Corpus.get(eventID);
    }
    
    @Override
    public Media getMedia(String mediaID, Media.MEDIA_FORMAT format) throws IOException {
        return getMedia(mediaID);
    }


    // removed 07-07-2022, issue #45
    /*@Override
    public String getEvent4Transcript(String transcriptID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/


    /*@Override
    public Set<MetadataKey> getMetadataKeysForCorpus(String corpusID, String type) {
        try {
            // within a given object type, key names in COMA should be unique
            Corpus corpus = getCorpus(corpusID);
            
            String comaNameOfObject = COMAUtilities.getComaNameForZumultName(type);

            NodeList allKeys = (NodeList) xPath.evaluate("//Key[ancestor::*[@Id][1]/name()='" + comaNameOfObject + "']", 
                    corpus.getDocument(), XPathConstants.NODESET);
            Set<String> allKeyNames = new HashSet<>();
            for (int i=0; i<allKeys.getLength(); i++){
                Element keyElement = ((Element)(allKeys.item(i)));
                String keyName = keyElement.getAttribute("Name");
                allKeyNames.add(keyName);
            }
            
            Set<MetadataKey> returnValue = new HashSet<>();
            // to do
            
            
            
            return returnValue;
        } catch (IOException | XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }*/

    @Override
    public Protocol getProtocol(String protocolID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getProtocol4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Measure getMeasure4SpeechEvent(String speechEventID, String type, String reference) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getMeasures4Corpus(String corpusID) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    @Override
    public KWIC getKWIC(SearchResultPlus searchResultPlus, String context) throws SearchServiceException, IOException {
        KWIC kwicView = new COMAKWIC(searchResultPlus, context, Constants.SEARCH_TYPE_STANDARD);       
        return kwicView;
    }
    
    @Override
    public KWIC exportKWIC(SearchResultPlus searchResultPlus, String context, String format) throws SearchServiceException, IOException {
        KWIC kwicView = new COMAKWIC(searchResultPlus, context, Constants.SEARCH_TYPE_DOWNLOAD, format);       
        return kwicView;
    }

    @Override
    public Searcher getSearcher() {
        return new COMASearcher();
    }

    
    public File getStatsFile(String corpusID){
        File topFolder = new File(Configuration.getMetadataPath());
        File corpusFolder = new File(topFolder, corpusID);
        File STATS_FILE = new File(corpusFolder, corpusID + ".stats");
        return STATS_FILE;
        
    }
    
    private void calculateStatistics(String corpusID) throws IOException{
        File topFolder = new File(Configuration.getMetadataPath());
        File corpusFolder = new File(topFolder, corpusID);
        File STATS_FILE = new File(corpusFolder, corpusID + ".stats");
        
        boolean statsFileExists = STATS_FILE.exists();
        
        if (statsFileExists){
            File comaFile = new File(corpusFolder, corpusID + ".coma");
            Path fileComa = Paths.get(comaFile.getAbsolutePath());
            BasicFileAttributes attrComa = Files.readAttributes(fileComa, BasicFileAttributes.class);
            FileTime lastModifiedTimeComa = attrComa.lastModifiedTime();


            Path fileIndex = Paths.get(STATS_FILE.getAbsolutePath());
            BasicFileAttributes attrIndex = Files.readAttributes(fileIndex, BasicFileAttributes.class);
            FileTime lastModifiedTimeIndex = attrIndex.lastModifiedTime();
            
            boolean comaModifiedAfterStats = (lastModifiedTimeComa.compareTo(lastModifiedTimeIndex)>0);
            if (comaModifiedAfterStats) {
                System.out.println("[COMAFileSystem] Coma was modified after stats. ");
            } else {
                System.out.println("[COMAFileSystem] Stats exist and are up-to-date.");
                return;
            }
        } else {
            System.out.println("[COMAFileSystem] There are no stats yet. ");
        }
            
        System.out.println("[COMAFileSystem] Calculating stats for " + corpusID);
        
        //=========================
        COMAFileSystem backend = new COMAFileSystem();
        
        org.jdom.Document outDocument = new org.jdom.Document(new org.jdom.Element("corpus-statistics").setAttribute("id", corpusID));
        
        
        TokenList allTokenList = new DefaultTokenList("transcription");
        IDList speechEventIDs = backend.getSpeechEvents4Corpus(corpusID);

        IDList speakerIDs = backend.getSpeakers4Corpus(corpusID);
        int countSpeakers = speakerIDs.size();
        int countSpeechEvents = speechEventIDs.size();
        int countTranscripts = backend.getTranscripts4Corpus(corpusID).size();
        outDocument.getRootElement().setAttribute("speakers", Integer.toString(countSpeakers));
        outDocument.getRootElement().setAttribute("speech-events", Integer.toString(countSpeechEvents));
        outDocument.getRootElement().setAttribute("transcripts", Integer.toString(countTranscripts));
        
        for (String speechEventID : speechEventIDs){
            TokenList seTokenList = new DefaultTokenList("transcription");
            org.jdom.Element seElement = new org.jdom.Element("speech-event");
            seElement.setAttribute("id", speechEventID);
            outDocument.getRootElement().addContent(seElement);
            IDList transcriptIDs = backend.getTranscripts4SpeechEvent(speechEventID);
            for (String transcriptID : transcriptIDs){
                //if (transcriptID.equals("TRS_1-167-1-22-a")) continue;
                Transcript transcript = backend.getTranscript(transcriptID);        
                int types = transcript.getNumberOfTypes();
                int tokens = transcript.getNumberOfTokens();
                TokenList thisTokenList = transcript.getTokenList("transcription");
                allTokenList = allTokenList.merge(thisTokenList);
                seTokenList = seTokenList.merge(thisTokenList);
                

                org.jdom.Element transcriptElement = new org.jdom.Element("transcript");
                transcriptElement.setAttribute("id", transcriptID);
                transcriptElement.setAttribute("tokens", Integer.toString(tokens));
                transcriptElement.setAttribute("types", Integer.toString(types));

                double duration = transcript.getEndTime() - transcript.getStartTime();
                transcriptElement.setAttribute("duration", Double.toString(duration));                    

                seElement.addContent(transcriptElement);
            }
            seElement.setAttribute("types", Integer.toString(seTokenList.getNumberOfTypes()));
            
        }
        
        outDocument.getRootElement().setAttribute("types", Integer.toString(allTokenList.getNumberOfTypes()));
        
        FileIO.writeDocumentToLocalFile(STATS_FILE, outDocument);
        
        System.out.println("[COMAFileSystem] Stats for " + corpusID + " calculated and written to " + STATS_FILE.getAbsolutePath());
        
        
    }
    
    
    private void indexIDs(String corpusID, Map<String, String> id2Corpus, Map<String, String> id2parentID) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException, TransformerException {
        File corpusFolder = new File(topFolder, corpusID);
        File comaFile = new File(corpusFolder, corpusID + ".coma");
        File comaIndexFile = new File(corpusFolder, corpusID + ".comaindex");
        boolean comaModifiedAfterIndex = false;
        if (comaIndexFile.exists()){
            Path fileComa = Paths.get(comaFile.getAbsolutePath());
            BasicFileAttributes attrComa = Files.readAttributes(fileComa, BasicFileAttributes.class);
            FileTime lastModifiedTimeComa = attrComa.lastModifiedTime();


            Path fileIndex = Paths.get(comaIndexFile.getAbsolutePath());
            BasicFileAttributes attrIndex = Files.readAttributes(fileIndex, BasicFileAttributes.class);
            FileTime lastModifiedTimeIndex = attrIndex.lastModifiedTime();
            
            comaModifiedAfterIndex = (lastModifiedTimeComa.compareTo(lastModifiedTimeIndex)>0);
            if (comaModifiedAfterIndex) {
                System.out.println("Coma was modified after index. ");
            } else {
                System.out.println("Index exists and is up-to-date.");
            }
        } else {
            System.out.println("There is no index yet. ");
        }
        if (!(comaIndexFile.exists()) || comaModifiedAfterIndex){
            System.out.println("Calculating index. ");
            String xp = "//*[@Id]";
            XPath xPath2 = XPathFactory.newInstance().newXPath();
            Element root = IOHelper.readDocument(comaFile).getDocumentElement();
            NodeList allIDElements = (NodeList) xPath2.evaluate(xp, root, XPathConstants.NODESET);
            System.out.println("Processing " + allIDElements.getLength() + " elements with ID. ");
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<coma-index>");
            for (int i=0; i<allIDElements.getLength(); i++){
                Element idElement = ((Element)(allIDElements.item(i)));
                String thisID = idElement.getAttribute("Id");
                id2Corpus.put(thisID, corpusID);
                sb.append("<index id=\"").append(thisID).append("\" corpus=\"").append(corpusID).append("\"");
                Element parentElement = (Element) (Node) xPath2.evaluate("ancestor::*[@Id][1]", idElement, XPathConstants.NODE);
                if (parentElement!=null){
                    String parentID = parentElement.getAttribute("Id");
                    id2parentID.put(thisID, parentID);
                    sb.append(" parent=\"").append(parentID).append("\"");
                }
                sb.append("/>");
                if (i%1000==0){
                    System.out.println("[" + i + "/" + allIDElements.getLength() + "]");
                }
            }
            sb.append("</coma-index>");
            
            // write index
            Document indexDocument = IOHelper.DocumentFromText(sb.toString());
            IOHelper.writeDocument(indexDocument, comaIndexFile);
            System.out.println("Index written to " + comaIndexFile.getAbsolutePath());
        } else {
            // read index
            System.out.println("Reading index from " + comaIndexFile.getAbsolutePath());
            Document indexDocument = IOHelper.readDocument(comaIndexFile);
            NodeList childNodes = indexDocument.getDocumentElement().getChildNodes();
            for (int i=0; i<childNodes.getLength(); i++){
                Element item = (Element)childNodes.item(i);
                /*
                    <index corpus="TGDP" id="CIDIDCA77E918-392E-DC22-A0A2-5A25F6084C37"
                        parent="CIDE2EAFD16-32BD-43BF-6161-D4E1774950BC"/>                
                */
                String id = item.getAttribute("id");
                String corpus = item.getAttribute("corpus");
                String parent = item.getAttribute("parent");
                id2Corpus.put(id, corpus);
                id2parentID.put(id, parent);
                
            }
        }
    }

    @Override
    public SearchStatistics getSearchStatistics(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, String metadataKeyID, Integer pageLength, Integer pageIndex, String searchIndex, String sortType, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    

    @Override
    public IDList findSpeechEventsByMetadataValue(String corpusID, MetadataKey metadataKey, String metadataValue) {
        IDList list = new IDList("SpeechEvent");
        try {
            Corpus corpus = getCorpus(corpusID);
            Document comaDocument = corpus.getDocument();
            String xp = "//Communication[Description/Key[@Name='" + metadataKey.getName("en") + "']='"
                    + metadataValue + "']";
            //System.out.println("Evaluating " + xp);
            NodeList allKeys = (NodeList) xPath.evaluate(xp, comaDocument, XPathConstants.NODESET);
            for (int i=0; i<allKeys.getLength(); i++){
                Element communicationElement = ((Element)(allKeys.item(i)));
                list.add(communicationElement.getAttribute("Id"));
            }
        } catch (XPathExpressionException | IOException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    
    @Override
    public IDList findEventsByMetadataValue(String corpusID, MetadataKey metadataKey, String metadataValue) {
        IDList list = findSpeechEventsByMetadataValue(corpusID, metadataKey, metadataValue);
        list.setObjectName("Event");
        return list;
    }
    
    @Override
    public IDList findSpeakersByMetadataValue(String corpusID, MetadataKey metadataKey, String metadataValue) {
        IDList list = new IDList("Speaker");
        try {
            Corpus corpus = getCorpus(corpusID);
            Document comaDocument = corpus.getDocument();
            String xp = "//Speaker[Description/Key[@Name='" + metadataKey.getName("en") + "']='"
                    + metadataValue + "']";
            //System.out.println("Evaluating " + xp);
            NodeList allKeys = (NodeList) xPath.evaluate(xp, comaDocument, XPathConstants.NODESET);
            for (int i=0; i<allKeys.getLength(); i++){
                Element communicationElement = ((Element)(allKeys.item(i)));
                list.add(communicationElement.getAttribute("Id"));
            }
        } catch (XPathExpressionException | IOException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
  
}
