/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

/**
 *
 * @author Elena Frick
 */
public interface SearchResult {
    public int getTotalHits();
    public int getTotalTranscripts();
    public SearchQuery getSearchQuery();
    public MetadataQuery getMetadataQuery();
    public String getSearchMode();
    public long getSearchTime();
    public AdditionalSearchConstraints getAdditionalSearchConstraints();
    
}
