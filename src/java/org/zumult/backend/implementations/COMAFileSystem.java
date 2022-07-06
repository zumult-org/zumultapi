/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
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
import org.zumult.backend.Configuration;
import org.zumult.backend.VirtualCollectionStore;
import org.zumult.io.IOHelper;
import org.zumult.objects.AdditionalMaterialMetadata;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTagSet;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Measure;
import org.zumult.objects.Media;
import org.zumult.objects.MediaMetadata;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Protocol;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.Transcript;
import org.zumult.objects.TranscriptMetadata;
import org.zumult.objects.implementations.COMACommunication;
import org.zumult.objects.implementations.COMACorpus;
import org.zumult.objects.implementations.COMAMedia;
import org.zumult.objects.implementations.COMASpeaker;
import org.zumult.objects.implementations.ISOTEITranscript;
import org.zumult.query.SearchResult;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchStatistics;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.KWIC;
import org.zumult.query.SampleQuery;

/**
 *
 * @author thomas.schmidt
 */
public class COMAFileSystem extends AbstractBackend {
    
    XPath xPath = XPathFactory.newInstance().newXPath();
    File topFolder = new File(Configuration.getMetadataPath());
    

    @Override
    public String getID() {
        return "TO DO";
    }

    @Override
    public String getName() {
        return "TO DO";
    }

    @Override
    public String getAcronym() {
        return "TO DO";
    }

    @Override
    public String getDescription() {
        return "Backend using EXMARaLDA Coma as the central database and the plain file system";        
    }

    @Override
    public Corpus getCorpus(String corpusID) throws IOException {
        //File topFolder = new File("N:\\Workspace\\HZSK");
        File corpusFolder = new File(topFolder, corpusID);
        File comaFile = new File(corpusFolder, corpusID + ".coma");
        String comaXML = IOHelper.readUTF8(comaFile);
        Corpus corpus = new COMACorpus(comaXML);
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
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();
            
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
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();
            
            /* <Media Id="MID6410DC54-06CA-E52C-F05A-A16E867ECB5D">
            <NSLink>Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin.mp3</NSLink> */
            
            String xp = "//Media[@Id='" + mediaID + "']";
            Element mediaElement = (Element) (Node) xPath.evaluate(xp, corpusDocument.getDocumentElement(), XPathConstants.NODE);
            String nsLink = mediaElement.getElementsByTagName("NSLink").item(0).getTextContent();
            File corpusFolder = new File(topFolder, corpusID);
            String urlString = corpusFolder.toPath().resolve(nsLink).toUri().toURL().toString();
            return new COMAMedia(mediaID, urlString);
            
        } catch (XPathExpressionException ex) {
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
            Corpus corpus = getCorpus(corpusID);
            Document corpusDocument = corpus.getDocument();

            /* <Transcription Id="CIDID93045167-C4FF-8EF6-36B8-F08AC9F9E331">
               <NSLink>Shirin_Zhi_Zhi/MT_270110_Shirin/MT_270110_Shirin.exb</NSLink> */

            String xp = "//Transcription[@Id='" + transcriptID + "']";
            Element transcriptionElement = (Element) (Node) xPath.evaluate(xp, corpusDocument.getDocumentElement(), XPathConstants.NODE);
            String nsLink = transcriptionElement.getElementsByTagName("NSLink").item(0).getTextContent();
            File corpusFolder = new File(topFolder, corpusID);
            String nsLinkModified = nsLink.substring(0, nsLink.lastIndexOf(".")) + ".xml";
            File resolvedPath = corpusFolder.toPath().resolve(nsLinkModified).toFile();
            
            String xmlString = IOHelper.readUTF8(resolvedPath);
            
            return new ISOTEITranscript(xmlString);

        } catch (XPathExpressionException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;

    }

    @Override
    public IDList getCorpora() throws IOException {
        IDList result = new IDList("Corpus");
        result.add("hamatac");
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
            NodeList allTranscripts = (NodeList) xPath.evaluate("//Transcription[substring(Filename, string-length(Filename)-3)='.exb' or substring(Filename, string-length(Filename)-3)='.EXB']", communication.getDocument().getDocumentElement(), XPathConstants.NODESET);
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
                    xPath.evaluate("//Media[substring(Filename, string-length(Filename)-3)='.wav' or substring(Filename, string-length(Filename)-3)='.WAV']", communication.getDocument().getDocumentElement(), XPathConstants.NODESET);
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
        // This may be difficult because there is no mapping for this
        return new IDList("video");
    }

    @Override
    public IDList getTranscripts4Audio(String audioID) throws IOException {
        // This may be difficult because there is no mapping for this
        return new IDList("transcript");
    }

    @Override
    public IDList getTranscripts4Video(String videoID) throws IOException {
        // This may be difficult because there is no mapping for this
        return new IDList("transcript");
    }

    @Override
    public IDList getAudios4Transcript(String transcriptID) throws IOException {
        // This may be difficult because there is no mapping for this
        return new IDList("audio");
    }

    @Override
    public IDList getVideos4Transcript(String transcriptID) throws IOException {
        // This may be difficult because there is no mapping for this
        return new IDList("video");
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
        return getAvailableValues(corpusID, metadataKey.getID());
    }

    @Override
    public IDList getAvailableValues(String corpusID, String metadataKeyID) {
        try {
            Corpus corpus = getCorpus(corpusID);
            Document comaDocument = corpus.getDocument();
            Set<String> valueSet = new HashSet<>();
            NodeList allKeys = (NodeList) xPath.evaluate("//Key[@Name='" + metadataKeyID + "']", comaDocument, XPathConstants.NODESET);
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

    @Override
    public MediaMetadata getMediaMetadata4Media(String eventID, String mediaID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
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
        } catch (IOException ex) {
            Logger.getLogger(COMAFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        // difficulty here: need to implement TranscriptMetadata for Coma
        // not sure how to do this yet
        return null;
    }

    @Override
    public AdditionalMaterialMetadata getAdditionalMaterialMetadata4Corpus(String corpusID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



    @Override
    public Speaker getSpeakerInSpeechEvent(String speechEventID, String speakerID) {
        // COMA doesn't have this, so let's just return an empty speaker with this ID
        String xmlString ="<Speaker Id='" + speakerID + "'/>";
        return new COMASpeaker(xmlString);
    }

    private String findCorpusID(String someID) throws IOException {
        for (String corpusID : getCorpora()){            
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
        return null;
    }


    @Override
    public MetadataKey findMetadataKeyByID(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSpeechEvent4Transcript(String transcriptID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getEvent4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VirtualCollectionStore getVirtualCollectionStore() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCorpus4Event(String eventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Media getMedia(String mediaID, Media.MEDIA_FORMAT format) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}


    @Override
    public String getEvent4Transcript(String transcriptID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SearchResult search(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, String mode) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDList searchTokensForTranscript(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, String mode, String transcriptID, String tokenAttribute) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<SampleQuery> getSampleQueries(String corpusID, String mode) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Protocol getProtocol(String protocolID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProtocol4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SearchResultPlus search(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, Integer pageLength, Integer pageIndex, Boolean cutoff, String indexType, IDList metadataIDs) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SearchStatistics getSearchStatistics(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, String metadataKeyID, Integer pageLength, Integer pageIndex, String indexType, String sortType) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public KWIC exportKWIC(SearchResultPlus searchResult, String context, String fileType) throws SearchServiceException, IOException, ParserConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public KWIC getKWIC(SearchResultPlus searchResult, String context) throws SearchServiceException, IOException, ParserConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Measure getMeasure4SpeechEvent(String speechEventID, String type,String reference) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDList getMeasures4Corpus(String corpus) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDList getAvailableValuesForAnnotationLayer(String corpusID, String annotationLayerID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationTagSet getAnnotationTagSet(String annotationTagSetID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<AnnotationLayer> getAnnotationLayersForCorpus(String corpusID, String lang) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<AnnotationLayer> getAnnotationLayersForSearch(String corpusQuery, String searchIndex, String annotationLayerType) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDList getCorporaForSearch(String searchIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<MetadataKey> getMetadataKeysForSearch(String corpusQuery, String searchIndex, String metadataKeyType) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<MetadataKey> getMetadataKeysForCorpus(String corpusID, String type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<MetadataKey> getMetadataKeysForGroupingHits(String corpusQuery, String searchIndex, String metadataKeyType) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<AnnotationLayer> getAnnotationLayersForGroupingHits(String corpusQuery, String searchIndex, String annotationLayerType) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public SearchResultPlus searchRepetitions(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, Integer pageLength, Integer pageIndex, Boolean cutoff, String searchIndex, IDList metadataIDs, String repetitions, String synonyms) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
}
