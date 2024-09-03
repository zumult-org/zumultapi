/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.util.ArrayList;
import org.zumult.query.Bigram;
import org.zumult.query.serialization.DefaultQuerySerializer;
import org.zumult.query.SearchResultBigrams;

/**
 *
 * @author Elena
 */
public class DefaultSearchResultBigrams extends AbstractSearchResultPlus implements SearchResultBigrams {
    
    private ArrayList<Bigram> bigrams;

    public void setBigrams(ArrayList<Bigram> bigrams) {
        this.bigrams = bigrams;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }
    private String sortType;

    @Override
    public ArrayList<Bigram> getBigrams() {
        return bigrams;
    }

    @Override
    public String getSortType() {
        return sortType;
    }

    @Override
    public String toXML() {
        DefaultQuerySerializer serializer = new DefaultQuerySerializer();
        return serializer.displayBigramsInXML(this);
    }
}
