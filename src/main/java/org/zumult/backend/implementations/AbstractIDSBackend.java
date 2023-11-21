/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;
import org.zumult.backend.VirtualCollectionStore;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.objects.IDList;
import org.zumult.objects.Measure;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.implementations.DGD2Measure;
import org.zumult.objects.implementations.DGD2Media;
import org.zumult.objects.implementations.DGD2MetadataKey;
import org.zumult.objects.implementations.DGD2Speaker;
import org.zumult.objects.implementations.DGD2SpeechEvent;
import org.zumult.query.SearchServiceException;
import org.zumult.query.implementations.DGD2Searcher;
import org.zumult.query.implementations.DGD2KWIC;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.KWIC;
import org.zumult.query.SearchStatistics;
import org.zumult.query.Searcher;

/**
 *
 * @author thomas.schmidt
 */
public abstract class AbstractIDSBackend extends AbstractBackend {

    @Override
    public IDList getCorpora() throws IOException {
        IDList list = new IDList("corpus");

        try {
            String xml = IOHelper.readUTF8(AbstractIDSBackend.class.getResourceAsStream(Constants.ALL_CORPORA_PATH));
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("corpus");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                String id = element.getAttribute("id");
                list.add(id);
            }
            return list;
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String getID() {
        return "agd.ids-mannheim.de";
    }

    @Override
    public Media getMedia(String mediaID) throws IOException {
        // https://pragora-2.ids-mannheim.de/dgd_video/BETV/WEB/BETV_E_00001/BETV_E_00001_SE_01_V_01_DF_01.mp4
        //String urlString = Constants.DGD_URL;
        //String urlString = "http://zumult.ids-mannheim.de";
        //urlString+="/video/";
        String urlString = Configuration.getMediaPath() + "/";
        urlString += mediaID.substring(0, 4).replaceAll("\\-", "");
        urlString += "/WEB/";
        urlString += mediaID.substring(0, 12);
        urlString += "/";
        if (mediaID.contains("_V_")) {
            urlString += mediaID + "_DF_01.mp4";
        } else {
            urlString += mediaID + "_DF_01.mp3";
        }
        DGD2Media media = new DGD2Media(mediaID, urlString);
        return media;
    }
    
    @Override
    public Media getMedia(String mediaID, Media.MEDIA_FORMAT format) throws IOException {
        String urlString = "";
        String extension = "";
        switch (format){
            //     public static enum MEDIA_FORMAT {WAV, MP3, MPEG4_ARCHIVE, MPEG4_WEB};
            case MP3 :
                return getMedia(mediaID);
            case MPEG4_WEB :
                return getMedia(mediaID);
            case WAV :
                urlString = Configuration.getMediaArchivePath()+ "/";
                extension = "WAV";      
                break;
            case MPEG4_ARCHIVE : 
                urlString = Configuration.getMediaArchivePath()+ "/";
                extension = "mp4";                
                break;
        }
        urlString += mediaID.substring(0, 4).replaceAll("\\-", "");
        urlString += "/";
        urlString += mediaID.substring(0, 12);
        urlString += "/";
        urlString += mediaID + "_DF_01." + extension;

        DGD2Media media = new DGD2Media(mediaID, urlString);
        return media;                
        
        // remaining cases
        //return getMedia(mediaID);
    }
    

    

    @Override
    public String getAcronym() {
        return "AGD";
    }

    @Override
    public String getName() {
        return "Archiv f\u00fcr Gesprochenes Deutsch";
    }

    @Override
    public String getSpeechEvent4Transcript(String transcriptID) throws IOException {
        return transcriptID.substring(0,18);
    }
    
    // removed 07-07-2022, issue #45
    /*@Override
    public String getEvent4Transcript(String transcriptID) throws IOException {
        return transcriptID.substring(0,12);
    }*/

    @Override
    public String getEvent4SpeechEvent(String speechEventID) throws IOException {
        return speechEventID.substring(0,12);
    }

    @Override
    public String getCorpus4Event(String eventID) throws IOException {
        return eventID.substring(0,4).replaceAll("\\-", "");
    }

    @Override
    public Speaker getSpeakerInSpeechEvent(String speechEventID, String speakerID) {
        try {
            String eventID = speechEventID.substring(0, 12);
            Document eventDoc = getEvent(eventID).getDocument();
            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//Sprechereignis[@Kennung='" + speechEventID + "']/descendant::Sprecher[@Kennung='" + speakerID + "']";
            NodeList nodes = (NodeList) xPath.evaluate(xPathString, eventDoc.getDocumentElement(), XPathConstants.NODESET);
            Element speakerElement = (Element) nodes.item(0);
            // build a new document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Node importedNode = doc.importNode(speakerElement, true);
            doc.appendChild(importedNode);
            DGD2Speaker speaker = new DGD2Speaker(doc);
            return speaker;
        } catch (IOException | XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public SpeechEvent getSpeechEvent(String speechEventID) throws IOException {
        // get the speech event XML from the XMLof the superordinate event
        try {
            // FOLK_E_00001_SE_01
            String eventID = speechEventID.substring(0, 12);
            Document eventDoc = getEvent(eventID).getDocument();

            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//Sprechereignis[@Kennung='" + speechEventID + "']";
            NodeList nodes = (NodeList) xPath.evaluate(xPathString, eventDoc.getDocumentElement(), XPathConstants.NODESET);
            Element speechEventElement = (Element) nodes.item(0);
            if (speechEventElement != null){
                // build a new document
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = dbf.newDocumentBuilder();
                Document doc = builder.newDocument();
                Node importedNode = doc.importNode(speechEventElement, true);
                doc.appendChild(importedNode);
                DGD2SpeechEvent speechEvent = new DGD2SpeechEvent(doc);
                return speechEvent;
            }else{
                return null;
            }

        } catch (ParserConfigurationException | XPathExpressionException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    public IDList getAvailableValues(String corpusID, MetadataKey metadataKey) {
        return getAvailableValues(corpusID, metadataKey.getID());
    }

    // 07-07-2022, removed, issue #41
    /*@Override
    public MediaMetadata getMediaMetadata4Media(String eventID, String mediaID) {
        DGD2MediaMetadata mediaMetadata = null;
        try {
            Event event = getEvent(eventID);
            Document eventDoc = event.getDocument();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//SE-Aufnahme[@Kennung='" + mediaID + "']";
            NodeList nodes = (NodeList) xPath.evaluate(xPathString, eventDoc.getDocumentElement(), XPathConstants.NODESET);
            Element mediaMetadataElement = (Element) nodes.item(0);
            // build a new document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Node importedNode = doc.importNode(mediaMetadataElement, true);
            doc.appendChild(importedNode);
            mediaMetadata = new DGD2MediaMetadata(doc);
        } catch (IOException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mediaMetadata;
    }*/

    // 07-07-2022, removed, issue #41
    /*@Override
    public TranscriptMetadata getTranscriptMetadata4Transcript(String eventID, String transcriptID) {
        DGD2TranscriptMetadata transcriptMetadata = null;
        try {
            Event event = getEvent(eventID);
            Document eventDoc = event.getDocument();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//Transkript[@Kennung='" + transcriptID + "']";
            NodeList nodes = (NodeList) xPath.evaluate(xPathString, eventDoc.getDocumentElement(), XPathConstants.NODESET);
            Element transcriptMetadataElement = (Element) nodes.item(0);
            // build a new document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Node importedNode = doc.importNode(transcriptMetadataElement, true);
            doc.appendChild(importedNode);
            transcriptMetadata = new DGD2TranscriptMetadata(doc);
        } catch (IOException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException | XPathExpressionException | DOMException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        }
        return transcriptMetadata;
    }*/

    // 07-07-2022, removed, issue #41
    /*@Override
    public AdditionalMaterialMetadata getAdditionalMaterialMetadata4Corpus(String corpusID) {
        AdditionalMaterialMetadata additionalMaterialMetadata = null;
        try {
            Corpus corpus = getCorpus(corpusID);
            Document corpusDoc = corpus.getDocument();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//Zusatzmaterial";
            NodeList nodes = (NodeList) xPath.evaluate(xPathString, corpusDoc.getDocumentElement(), XPathConstants.NODESET);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement("additionalMaterial");
            doc.appendChild(root);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element materialElement = (Element) nodes.item(i);
                Node importedNode = doc.importNode(materialElement, true);
                root.appendChild(importedNode);
            }
            additionalMaterialMetadata = new DGD2AdditionalMaterialMetadata(doc);
        } catch (IOException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        }
        return additionalMaterialMetadata;
    }*/
    



    @Override
    public MetadataKey findMetadataKeyByID(String id) {
        if (id.startsWith("v_measure_")){
            String name = "";
            /*
                <key id="measure_word_tokens">3168</key>
                <key id="measure_word_types">578</key>
                <key id="measure_lemma_tokens">3190</key>
                <key id="measure_lemma_types">366</key>
                <key id="measure_intersection_GOETHE_A1">0.74</key>
                <key id="measure_intersection_GOETHE_A2">0.81</key>
                <key id="measure_intersection_GOETHE_B1">0.90</key>
                <key id="measure_intersection_HERDER_1000">0.78</key>
                <key id="measure_intersection_HERDER_2000">0.82</key>
                <key id="measure_intersection_HERDER_3000">0.83</key>
                <key id="measure_intersection_HERDER_4000">0.86</key>
                <key id="measure_intersection_HERDER_5000">0.87</key>
                <key id="measure_normalisation_rate">13.70</key>
                <key id="measure_overlap_perMilOverlaps">29.36</key>
                <key id="measure_overlap_averageNrOverlappingWords">1.80</key>
                <key id="measure_overlap_perCentOverlapsWithMoreThan2Words">18.28</key>
                <key id="measure_overlap_perMilTokensOverlapsWithMoreThan2Words">5.37</key>
                <key id="measure_articulation_rate">4.784326037964862</key>            
            */
            switch (id){
                case "v_measure_word_tokens" : name="Wort-Token"; break;
                case "v_measure_word_types" : name="Wort-Types";  break;
                case "v_measure_lemma_tokens" : name="Lemma-Token";  break;
                case "v_measure_lemma_types" : name="Lemma-Types";  break;
                case "v_measure_intersection_GOETHE_A1" : name="Abdeckung Goethe-A1"; break;
                case "v_measure_intersection_GOETHE_A2" : name="Abdeckung Goethe-A2"; break;
                case "v_measure_intersection_GOETHE_B1" : name="Abdeckung Goethe-B1"; break;
                case "v_measure_intersection_HERDER_1000" : name="Abdeckung Herder 1000"; break;
                case "v_measure_intersection_HERDER_2000" : name="Abdeckung Herder 2000"; break;
                case "v_measure_intersection_HERDER_3000" : name="Abdeckung Herder 3000"; break;
                case "v_measure_intersection_HERDER_4000" : name="Abdeckung Herder 4000"; break;
                case "v_measure_intersection_HERDER_5000" : name="Abdeckung Herder 5000"; break;
                case "v_measure_normalisation_rate" : name="Normalisierungsrate"; break;
                default : name = "xyz";
            }
            MetadataKey key = new DGD2MetadataKey(id, name, ObjectTypesEnum.SPEECH_EVENT);
            return key;
        }

        try {
            String xml = IOHelper.readUTF8(AbstractIDSBackend.class.getResourceAsStream(Constants.METADATA_SELECTION_PATH));
            Document doc = IOHelper.DocumentFromText(xml);
            
            // Query for the right element via XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            
            String xPathString = "//metadata-item[dgd-parameter-name='" + id + "']";
            
            //Elena: 19.04.2023: if metadata ID was extracted automatically, then it starts without "v_"
            if (!id.startsWith("v_")){
                xPathString = "//metadata-item[dgd-parameter-name='" + "v_"+ id + "']";
            }
            
            Node node = (Node)xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODE);
            if (node==null) return null;
            Element keyElement = (Element)node;
            MetadataKey key = new DGD2MetadataKey(keyElement);
            return key;
            //result.add(key);                
        } catch (IOException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;        
    }

    @Override
    public VirtualCollectionStore getVirtualCollectionStore() {
        return new FileSystemVirtualCollectionStore();
    }

  
    @Override
    public KWIC getKWIC(SearchResultPlus searchResultPlus, String context) throws SearchServiceException, IOException {
        KWIC kwicView = new DGD2KWIC(searchResultPlus, context, Constants.SEARCH_TYPE_STANDARD);       
        return kwicView;
    }
    
    @Override
    public KWIC exportKWIC(SearchResultPlus searchResultPlus, String context, String format) throws SearchServiceException, IOException {
        KWIC kwicView = new DGD2KWIC(searchResultPlus, context, Constants.SEARCH_TYPE_DOWNLOAD, format);       
        return kwicView;
    }  

    @Override
    public Measure getMeasure4SpeechEvent(String speechEventID, String type, String reference) {
        if(reference==null){
            reference="";
        }
        
        // find xml file with measure info
        String fileName = null;
        
        String corpusID = speechEventID.substring(0,4).replaceAll("\\-", "");
        
        String[][] measureArray = null;
        switch(corpusID){
        case "FOLK":
            measureArray = Constants.FOLK_MEASURES;
            break;
        case "GWSS":
            measureArray = Constants.GWSS_MEASURES;
            break;
        default:
            break;
        }
        if(measureArray!=null){
            for (int i = 0; i<measureArray.length; i++){
                if (measureArray[i][0].equals(type)){
                    fileName = measureArray[i][1] + ".xml";
                    break;
                }
            }  
        }
        
        // get measure from xml file
        if(fileName!=null){
            try {
                String actualPath = AbstractIDSBackend.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                //File folder = new File(IOHelper.getProjectFile(actualPath), "WEB-INF\\classes\\data");
                File folder = new File(new File(
                            new File(
                                new File(IOHelper.getProjectFile(actualPath), "WEB-INF"),
                            "classes"),
                        "data"), 
                    "measures");
                File[] listOfFiles = folder.listFiles();
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().equals(fileName)) {
                        try {
                            
                            Document doc = IOHelper.readDocument(file);
                            
                            XPath xPath = XPathFactory.newInstance().newXPath();
                            Element measure1Element = (Element)xPath.evaluate("//measures[@speechEventID='" +  speechEventID + "']",
                                    doc.getDocumentElement(), XPathConstants.NODE);
                            
                            NodeList nodeList = measure1Element.getChildNodes();
                            for (int j=0; j<nodeList.getLength(); j++){
                                if (!(nodeList.item(j) instanceof Element)) continue;
                                Element measureElement = (Element) nodeList.item(j);
                                String typeAttr = measureElement.getAttribute("type");

                                if(typeAttr.isEmpty() && !measureElement.getAttribute(type).isEmpty()){
                                    typeAttr=type;                                
                                }
                                
                                if(typeAttr.equals(type)){
                                    String referenceAttr = measureElement.getAttribute("reference");
                                    if(referenceAttr.equals(reference)){
                                        Measure m  = new DGD2Measure(IOHelper.ElementToString(measureElement));
                                        return m;
                                    }
                                }
                            }
                        } catch (XPathExpressionException | IOException | SAXException | ParserConfigurationException | TransformerException ex) {
                            Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(AbstractIDSBackend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    @Override
    public IDList getMeasures4Corpus(String corpus) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Searcher getSearcher() {
        return new DGD2Searcher();
    }

    @Override
    public SearchStatistics getSearchStatistics(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, String metadataKeyID, Integer pageLength, Integer pageIndex, String searchIndex, String sortTypeCode, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException {
        Searcher searcher = new DGD2Searcher();
        searcher.setQuery(queryString, queryLanguage, queryLanguageVersion);
        searcher.setCollection(corpusQuery, metadataQuery);
        searcher.setPagination(pageLength, pageIndex);
        searcher.setAdditionalSearchConstraints(additionalSearchConstraints);
        if (metadataKeyID != null && !metadataKeyID.isEmpty()) {
            MetadataKey mk = this.findMetadataKeyByID("v_" + metadataKeyID);
            if (mk == null) {
                mk = new DGD2MetadataKey(metadataKeyID, "", null);
            }
            return searcher.getStatistics(searchIndex, sortTypeCode, mk);
        } else {
            throw new SearchServiceException("You did not specify the metadataKey!");
        }
    }
    
}
