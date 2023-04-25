/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import java.util.ArrayList;
import org.zumult.query.StatisticEntry;

/**
 *
 * @author Elena
 */
public class SearchEngineResponseStatistics extends SearchEngineResponse {
    private int numberOfDistinctValues;
    private ArrayList<StatisticEntry> statistics;

    public int getNumberOfDistinctValues() {
        return numberOfDistinctValues;
    }

    public void setNumberOfDistinctValues(int numberOfDistinctValues) {
        this.numberOfDistinctValues = numberOfDistinctValues;
    }

    public ArrayList<StatisticEntry>  getStatistics() {
        return statistics;
    }

    public void setStatistics(ArrayList<StatisticEntry>  infos) {
        this.statistics = infos;
    }
}
