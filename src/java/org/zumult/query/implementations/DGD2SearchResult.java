/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.query.AdditionalSearchConstraints;
import org.zumult.query.MetadataQuery;
import org.zumult.query.SearchQuery;
import org.zumult.query.SearchResult;

/**
 *
 * @author Elena
 */
public class DGD2SearchResult implements SearchResult {
       
    private SearchQuery searchQuery;
    private MetadataQuery metadataQuery;
    private int totalHits;
    private int totalTranscripts;
    private String searchMode;
    private long searchTime;
    private AdditionalSearchConstraints additionalSearchConstraints;
    

    @Override
    public long getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }

    @Override
    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery contentQuery) {
        this.searchQuery = contentQuery;
    }

    @Override
    public MetadataQuery getMetadataQuery() {
        return metadataQuery;
    }

    public void setMetadataQuery(MetadataQuery metadataQuery) {
        this.metadataQuery = metadataQuery;
    }

    @Override
    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    @Override
    public int getTotalTranscripts() {
        return totalTranscripts;
    }

    public void setTotalTranscripts(int totalTranscripts) {
        this.totalTranscripts = totalTranscripts;
    }

    @Override
    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    @Override
    public AdditionalSearchConstraints getAdditionalSearchConstraints() {
        return additionalSearchConstraints;
    }
    
    public void setAdditionalSearchConstraints(AdditionalSearchConstraints additionalSearchConstraints) {
        this.additionalSearchConstraints = additionalSearchConstraints;
    }

    
}
