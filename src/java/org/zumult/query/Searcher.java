/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Frick
 */
public interface Searcher {
    public IDList getCorporaForSearch(String searchIndex);
    public ArrayList<SampleQuery> getSampleQueries (String corpusID, String searchIndex) throws SearchServiceException;
    public void setCollection(String corpusQueryStr, String metadataQueryStr) throws SearchServiceException;
    public void setQuery(String queryString, String queryLanguage, String queryLanguageVersion) throws SearchServiceException;
    public void setPagination(Integer pageLength , Integer pageStartIndex);
    public SearchResult search(String searchIndex) throws SearchServiceException, IOException;
    public SearchResultPlus search(String searchIndex, Boolean cutoff, IDList metadataIDs) throws SearchServiceException, IOException;
    public SearchStatistics getStatistics(String searchIndex, String sortType, MetadataKey metadataKey) throws SearchServiceException, IOException;
    public IDList searchTokensForTranscript(String searchIndex, String tokenAttribute) throws SearchServiceException, IOException;
    public Set<MetadataKey> filterMetadataKeysForGroupingHits(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException;
    public Set<MetadataKey> filterMetadataKeysForSearch(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException;
    public Set<AnnotationLayer> filterAnnotationLayersForGroupingHits(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException;
    public Set<AnnotationLayer> filterAnnotationLayersForSearch(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException;
    public SearchResultPlus searchRepetitions(String searchIndex, Boolean cutoff, IDList metadataIDs, String repetitions) throws SearchServiceException, IOException;
}
