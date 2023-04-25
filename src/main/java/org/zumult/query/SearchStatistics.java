/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

import java.util.ArrayList;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Elena Frick
 */
public interface SearchStatistics extends SearchResult {
    
    public ArrayList<StatisticEntry> getStatistics();
    public MetadataKey getMetadataKey();
    public int getNumberOfDistinctValues();
    public Pagination getPagination();
    public String getSortType();
    public String toXML();
}
