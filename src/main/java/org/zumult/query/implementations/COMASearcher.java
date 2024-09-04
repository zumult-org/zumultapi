/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.zumult.backend.Configuration;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.query.SampleQuery;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchIndexType;
import org.zumult.query.SearchResultBigrams;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.searchEngine.COMASearchEngine;
import org.zumult.query.searchEngine.MTASBasedSearchEngine;

/**
 *
 * @author bernd
 */
public class COMASearcher extends AbstractSearcher {

    @Override
    public ArrayList<SampleQuery> getSampleQueries(String corpusID, String searchIndex) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<MetadataKey> filterMetadataKeysForGroupingHits(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<MetadataKey> filterMetadataKeysForSearch(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<AnnotationLayer> filterAnnotationLayersForGroupingHits(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<AnnotationLayer> filterAnnotationLayersForSearch(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public MTASBasedSearchEngine getSearchEngine(){
        return new COMASearchEngine();
    }

    @Override
    public IDList getCorporaForSearch(String searchIndex) {
        IDList corpora = Configuration.getCorpusIDs(); // corpora available for search
        return corpora;
   }

    @Override
    public ArrayList<String> getIndexPaths(SearchIndexType searchIndex) throws IOException, SearchServiceException {
        ArrayList<String> result = new ArrayList<>();
        for (String indexID : Configuration.getTranscriptBasedIndexIDs()){
            File file = new File(Configuration.getSearchIndexPath(), indexID);
            result.add(file.getAbsolutePath());
        }
        return result;
    }
    
    @Override
    public SearchIndexType getSearchIndexType(String searchIndex) throws SearchServiceException {
        return new SearchIndexType(){
            @Override
            public String getValue() {
                return null;
            }
            
        };
                
    }

    @Override
    public SearchResultBigrams searchBigrams(String searchIndex, String sortType, String bigramType, List<String> annotationLayerIDs4BigramGroups, List<String> elementsInBetweenToBeIgnored, String scope, Integer minFreq, Integer maxFreq) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public SearchResultPlus searchRepetitions(String searchIndex, Boolean cutoff, IDList metadataIDs, String repetitions, String synonyms) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}