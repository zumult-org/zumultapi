/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTagSet;
import org.zumult.objects.AnnotationTypeEnum;
import org.zumult.objects.Corpus;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DGD2AnnotationTagSet;
import org.zumult.objects.implementations.DGD2MetadataKey;
import org.zumult.objects.implementations.ISOTEIAnnotationBlock;
import org.zumult.query.SearchResult;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchStatistics;
import org.zumult.query.Searcher;
import org.zumult.query.implementations.DGD2Searcher;

/**
 *
 * @author thomasschmidt
 */
public abstract class AbstractBackend implements BackendInterface {


    @Override
    public AnnotationBlock getAnnotationBlock(String transcriptID, String annotationBlockId) throws IOException {
        try {
            Transcript transcript = getTranscript(transcriptID);
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new ISOTEINamespaceContext());
            // 05-05-2021 - it seems that this is called not only for annotationBlocks 
            // but for highest level elements in general
            // it is inaptly named then... Ignoring this for the time being
            //String xpathString = "//tei:annotationBlock[@xml:id='" + annotationBlockId + "']";
            String xpathString = "//tei:body/*[@xml:id='" + annotationBlockId + "']";
            //System.out.println(xpathString);
            Element annotationBlock = (Element)xPath.evaluate(xpathString,
                    transcript.getDocument().getDocumentElement(), XPathConstants.NODE);
            if (annotationBlock==null){
                throw new IOException("No element with ID " + annotationBlockId);
            }
            annotationBlock.getParentNode().removeChild(annotationBlock);
            AnnotationBlock ab = new ISOTEIAnnotationBlock(IOHelper.ElementToString(annotationBlock));
            return ab;
        } catch (XPathExpressionException | TransformerException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }


    @Override
    public IDList getTranscripts4Corpus(String corpusID) throws IOException {
        IDList allTranscripts = new IDList("transcript");
        IDList events = getEvents4Corpus(corpusID);
        for (String eventID : events) {
            IDList speechEvents = getEvent(eventID).getSpeechEvents();
            for (String speechEventID : speechEvents) {
                IDList transcripts = getSpeechEvent(speechEventID).getTranscripts();
                allTranscripts.addAll(transcripts);
            }
        }
        return allTranscripts;
    }

    @Override
    public IDList getSpeechEvents4Corpus(String corpusID) throws IOException {
        IDList allSpeechEvents = new IDList("speech-event");
        IDList events = getEvents4Corpus(corpusID);
        for (String eventID : events) {
            IDList speechEvents = getEvent(eventID).getSpeechEvents();
            allSpeechEvents.addAll(speechEvents);
        }
        return allSpeechEvents;
    }

    @Override
    public String getAnnotationBlockID4TokenID(String transcriptID, String tokenID) throws IOException {
        try {
            Transcript transcript = getTranscript(transcriptID);
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new ISOTEINamespaceContext());
            String xpathString = "//tei:annotationBlock[descendant::*[@xml:id='" + tokenID + "']]";
            Element annotationBlock = (Element) xPath.evaluate(xpathString, transcript.getDocument().getDocumentElement(), XPathConstants.NODE);
            if (annotationBlock != null) {
                return annotationBlock.getAttribute("xml:id");
            }
            return null;
        } catch (XPathExpressionException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String getNearestAnnotationBlockID4TokenID(String transcriptID, String tokenID) throws IOException {
        try {
            Transcript transcript = getTranscript(transcriptID);
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new ISOTEINamespaceContext());
            String xpathString = "//tei:annotationBlock[descendant::*[@xml:id='" + tokenID + "']]";
            Element annotationBlock = (Element) xPath.evaluate(xpathString, transcript.getDocument().getDocumentElement(), XPathConstants.NODE);
            if (annotationBlock != null) {
                return annotationBlock.getAttribute("xml:id");
            } else {
                // if the element with the id is not part of an ab, return the first preceding or following annotation block
                xpathString = "//*[@xml:id='" + tokenID + "']/preceding-sibling::tei:annotationBlock[1]";
                annotationBlock = (Element) xPath.evaluate(xpathString, transcript.getDocument().getDocumentElement(), XPathConstants.NODE);
                if (annotationBlock != null) {
                    return annotationBlock.getAttribute("xml:id");
                } else {
                    xpathString = "//*[@xml:id='" + tokenID + "']/following-sibling::tei:annotationBlock[1]";
                    annotationBlock = (Element) xPath.evaluate(xpathString, transcript.getDocument().getDocumentElement(), XPathConstants.NODE);
                    if (annotationBlock != null) {
                        return annotationBlock.getAttribute("xml:id");
                    }
                }
                return null;
            }
        } catch (XPathExpressionException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    public IDList searchTokensForTranscript(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, 
            String metadataQuery, String searchIndex, String transcriptID, String tokenAttribute, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException{
        Searcher searcher = getSearcher();
        searcher.setQuery("("+ queryString + ")"+ " within <"+ Constants.METADATA_KEY_TRANSCRIPT_DGD_ID +"=\"" + transcriptID + "\"/>", queryLanguage, queryLanguageVersion);
        // changed 07-07-2022, issue #45
        String eventID = getEvent4SpeechEvent(getSpeechEvent4Transcript(transcriptID));        
        //searcher.setCollection("corpusSigle=" + getCorpus4Event(getEvent4Transcript(transcriptID)), metadataQuery);
        searcher.setCollection("corpusSigle=" + getCorpus4Event(eventID), metadataQuery);
        searcher.setAdditionalSearchConstraints(additionalSearchConstraints);
        return searcher.searchTokensForTranscript(searchIndex, tokenAttribute);

    }

    @Override
    public SearchResult search(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, String searchIndex, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException {
        Searcher searcher = getSearcher();
        searcher.setQuery(queryString, queryLanguage, queryLanguageVersion);
        searcher.setCollection(corpusQuery, metadataQuery);
        searcher.setAdditionalSearchConstraints(additionalSearchConstraints);
        return searcher.search(searchIndex);
    }
    
    @Override
    public SearchResultPlus search(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, Integer pageLength, Integer pageIndex, 
            Boolean cutoff, String searchIndex, IDList metadataIDs, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException {
                        
        Searcher searcher = getSearcher();
        searcher.setQuery(queryString, queryLanguage, queryLanguageVersion);
        searcher.setCollection(corpusQuery, metadataQuery);
        searcher.setPagination(pageLength , pageIndex);
        searcher.setAdditionalSearchConstraints(additionalSearchConstraints);
        return searcher.search(searchIndex, cutoff, metadataIDs);
    }
    
    @Override
    public SearchResultPlus searchRepetitions(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, Integer pageLength, Integer pageIndex, 
            Boolean cutoff, String searchIndex, IDList metadataIDs, String repetitions, String synonyms, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException {
                        
        Searcher searcher = getSearcher();
        searcher.setQuery(queryString, queryLanguage, queryLanguageVersion);
        searcher.setCollection(corpusQuery, metadataQuery);
        searcher.setPagination(pageLength , pageIndex);
        searcher.setAdditionalSearchConstraints(additionalSearchConstraints);
        return searcher.searchRepetitions(searchIndex, cutoff, metadataIDs, repetitions, synonyms);
    }
    
    
    

    @Override
    public AnnotationTagSet getAnnotationTagSet(String annotationTagSetID) throws IOException {
        try {
            String path = Constants.DATA_POS_PATH + annotationTagSetID + ".xml";
            String xml = new Scanner(AbstractBackend.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            AnnotationTagSet annotationTagSet = new DGD2AnnotationTagSet(doc);
            return annotationTagSet;
        } catch (NullPointerException ex) {
            throw new IOException("Tagset for " + annotationTagSetID + " does not exist!");
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            throw new IOException("Tagset for " + annotationTagSetID + " could not be loaded!");
        }
    }

    @Override
    public IDList getAvailableValuesForAnnotationLayer(String corpusID, String annotationLayerID) {
        IDList list = new IDList("AvailableValue");
        try {
            String path = Constants.DATA_ANNOTATIONS_PATH + "AvailableAnnotationValues.xml";
            String xml = new Scanner(AbstractBackend.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//corpus[@corpus='" + corpusID + "']/key[@id='" + annotationLayerID + "']/value";
            NodeList nodes = (NodeList) xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) (nodes.item(i));
                list.add(element.getTextContent());
            }
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(AGDFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @Override
    public Set<MetadataKey> getMetadataKeysForGroupingHits(String corpusQuery, String searchIndex, String type) throws SearchServiceException, IOException {
        // get all available metadata Keys
        Set<MetadataKey> metadataKeys = new HashSet(); 
        Set<String> corporaIDs = IOHelper.getCorporaIDsFromCorpusQuery(corpusQuery);
        if (!corporaIDs.isEmpty()) {
            for (String corpusID : corporaIDs) {
                metadataKeys.addAll(getMetadataKeysForCorpus(corpusID, type));
            }
        }
        // check if metadata can be used for grouping hits
        Searcher searcher = new DGD2Searcher();
        Set<MetadataKey> metadataKeysForSearch = searcher.filterMetadataKeysForGroupingHits(metadataKeys, searchIndex, type);
        return metadataKeysForSearch;
    }

    public Set<MetadataKey> getMetadataKeysForSearch(String corpusQuery, String searchIndex, String type) throws SearchServiceException, IOException {
        // get all available metadata Keys
        Set<MetadataKey> metadataKeys = new HashSet();
        Set<String> corporaIDs = IOHelper.getCorporaIDsFromCorpusQuery(corpusQuery);
        if (!corporaIDs.isEmpty()) {
            for (String corpusID : corporaIDs) {
                metadataKeys.addAll(getMetadataKeysForCorpus(corpusID, type));
            }
        }
        // check if metadata can be searched
        Searcher searcher = new DGD2Searcher();
        Set<MetadataKey> metadataKeysForSearch = searcher.filterMetadataKeysForSearch(metadataKeys, searchIndex, type);
        return metadataKeysForSearch;
    }

    protected Set<MetadataKey> getMetadataKeysForCorpus(String corpusID, String type) {
        Set<MetadataKey> metadataKeys = new HashSet();
        ObjectTypesEnum objectTypesEnum = null;
        try {
            Corpus corpus = getCorpus(corpusID);
            // check if metadataKey type exists
            if (type != null) {
                objectTypesEnum = ObjectTypesEnum.valueOf(type.toUpperCase());
            }
            metadataKeys.addAll(corpus.getMetadataKeys(objectTypesEnum));
            return metadataKeys;
        } catch (NullPointerException ex) {
            StringBuilder sb = new StringBuilder();
            sb.append(". There is no metadata for ").append(type).append(". Supported types are: ");
            for (ObjectTypesEnum ob : ObjectTypesEnum.values()) {
                sb.append(ob.name());
                sb.append(", ");
            }
            throw new NullPointerException(sb.toString().trim().replaceFirst(",$", ""));
        } catch (IOException ex) {
            throw new NullPointerException(corpusID + "cound not be found!");
        }
    }

    @Override
    public Set<AnnotationLayer> getAnnotationLayersForSearch(String corpusQuery, String searchIndex, String annotationLayerType) throws SearchServiceException, IOException {
        // get all available annotation layers
        Set<AnnotationLayer> annotationLayers = new HashSet();
        Set<String> corporaIDs = IOHelper.getCorporaIDsFromCorpusQuery(corpusQuery);
        if (!corporaIDs.isEmpty()) {
            for (String corpusID : corporaIDs) {
                annotationLayers.addAll(getAnnotationLayersForCorpus(corpusID, annotationLayerType));
            }
        }
        // check if annotation layers can be searched
        Searcher searcher = new DGD2Searcher();
        Set<AnnotationLayer> annotationLayersForSearch = searcher.filterAnnotationLayersForSearch(annotationLayers, searchIndex, annotationLayerType);
        return annotationLayersForSearch;
    }

    @Override
    public Set<AnnotationLayer> getAnnotationLayersForGroupingHits(String corpusQuery, String searchIndex, String annotationLayerType) throws SearchServiceException, IOException {
        // get all available annotation layers
        Set<AnnotationLayer> annotationLayers = new HashSet();
        Set<String> corporaIDs = IOHelper.getCorporaIDsFromCorpusQuery(corpusQuery);
        if (!corporaIDs.isEmpty()) {
            for (String corpusID : corporaIDs) {
                annotationLayers.addAll(getAnnotationLayersForCorpus(corpusID, annotationLayerType));
            }
        }
        // check if annotation layers can be searched
        Searcher searcher = new DGD2Searcher();
        Set<AnnotationLayer> annotationLayersForSearch = searcher.filterAnnotationLayersForGroupingHits(annotationLayers, searchIndex, annotationLayerType);
        return annotationLayersForSearch;
    }

    protected Set<AnnotationLayer> getAnnotationLayersForCorpus(String corpusID, String annotationType) {
        Set<AnnotationLayer> annotationLayers = new HashSet();
        try {
            Corpus corpus = getCorpus(corpusID);
            if (annotationType != null) {
                try {
                    AnnotationTypeEnum annotationTypeEnum = AnnotationTypeEnum.valueOf(annotationType.toUpperCase());
                    annotationLayers.addAll(corpus.getAnnotationLayers(annotationTypeEnum));
                } catch (NullPointerException ex) {
                    throw new NullPointerException(annotationType + "cound not be found!");
                }
            } else {
                annotationLayers.addAll(corpus.getAnnotationLayers(AnnotationTypeEnum.TOKEN));
            }
        } catch (IOException ex) {
            throw new NullPointerException(corpusID + "cound not be found!");
        }
        return annotationLayers;
    }

    /*   @Override
    public KWIC exportKWIC(String queryString, String queryLanguage, String queryLanguageVersion,
    String corpusQuery, String metadataQuery, Integer pageLength, Integer pageIndex,
    Boolean cutoff, String searchIndex, String context, String fileType, IDList metadataIDs) throws SearchServiceException, IOException {
    final long timeStart_search = System.currentTimeMillis();
    SearchResultPlus result = search(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery,
    pageLength, pageIndex, cutoff, searchIndex, metadataIDs);
    KWIC kwicView = new DGD2KWIC(result, context, Constants.SEARCH_TYPE_DOWNLOAD, fileType);
    final long timeEnd_search = System.currentTimeMillis();
    long millis_search = timeEnd_search - timeStart_search;
    System.out.println("exportKWIC: " + TimeUtilities.format(millis_search));
    return kwicView;
    }
     */
    
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
                mk = new DGD2MetadataKey(metadataKeyID, null, null);
            }
            return searcher.getStatistics(searchIndex, sortTypeCode, mk);
        } else {
            throw new SearchServiceException("You did not specify the metadataKey!");
        }
    }
    
    @Override
    public Set<MetadataKey> getMetadataKeys4Corpus(String corpusID, ObjectTypesEnum metadataLevel) throws IOException{
        Corpus corpus = getCorpus(corpusID);
        return corpus.getMetadataKeys(metadataLevel);
    }
    
    @Override
    public Set<MetadataKey> getMetadataKeys4Corpus(String corpusID) throws IOException{
        Corpus corpus = getCorpus(corpusID);
        return corpus.getMetadataKeys();
    }
    
}
