/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.util.ArrayList;
import org.zumult.objects.MetadataKey;
import org.zumult.query.StatisticEntry;
import org.zumult.query.SearchStatistics;
import org.zumult.query.serialization.SearchResultSerializer;

/**
 *
 * @author Elena
 */
public class DefaultSearchStatistics extends AbstractSearchResultPlus implements SearchStatistics {
    
    private ArrayList<StatisticEntry> statistics;
    private MetadataKey metadataKey;
    private int numberOfDistinctValues;
    private String sortType;

    @Override
    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    @Override
    public int getNumberOfDistinctValues() {
        return numberOfDistinctValues;
    }

    public void setNumberOfDistinctValues(int numberOfDistinctValues) {
        this.numberOfDistinctValues = numberOfDistinctValues;
    }

    @Override
    public ArrayList<StatisticEntry> getStatistics() {
        return statistics;
    }

    public void setStatistics(ArrayList<StatisticEntry> statistics) {
        this.statistics = statistics;
    }

    @Override
    public MetadataKey getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(MetadataKey metadataKey) {
        this.metadataKey = metadataKey;
    }
    
    @Override
    public String toXML() {
        SearchResultSerializer searchResultSerializer = new SearchResultSerializer();
        return searchResultSerializer.displayStatiscticsInXML(this);
    }

}
