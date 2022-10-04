/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.zumult.backend.BackendInterface;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.ISOTEIAnnotationBlock;
import org.zumult.query.SearchResult;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;
import org.zumult.query.Searcher;

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
    
    
    
    public abstract Searcher getSearcher();
    
}
