/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.zumult.backend.Configuration;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.query.SampleQuery;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchIndexType;
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
        String soleSearchIndexPath = Configuration.getSearchIndexPath();
        ArrayList<String> result = new ArrayList<>();
        result.add(soleSearchIndexPath);
        return result;
    }

    @Override
    public SearchIndexType getSearchIndexType(String searchIndex) throws SearchServiceException {
        return new SearchIndexType(){
            @Override
            public String getValue() {
                return DGDSearchIndexType.DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name();
            }
            
        };
                
    }
}