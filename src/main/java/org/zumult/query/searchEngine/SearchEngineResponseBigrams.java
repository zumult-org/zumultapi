/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import java.util.ArrayList;
import org.zumult.query.Bigram;

/**
 *
 * @author Frick
 */
public class SearchEngineResponseBigrams extends SearchEngineResponse {
    private int numberOfDistinctValues;
    private ArrayList<Bigram> bigrams;

    public int getNumberOfDistinctValues() {
        return numberOfDistinctValues;
    }

    public void setNumberOfDistinctValues(int numberOfDistinctValues) {
        this.numberOfDistinctValues = numberOfDistinctValues;
    }

    public ArrayList<Bigram>  getBigrams() {
        return bigrams;
    }

    public void setBigrams(ArrayList<Bigram>  bigrams) {
        this.bigrams = bigrams;
    }
}