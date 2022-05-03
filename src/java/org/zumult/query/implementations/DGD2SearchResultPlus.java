/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.util.ArrayList;

import org.zumult.query.Hit;
import org.zumult.query.SearchResultPlus;

/**
 *
 * @author Elena
 */
public class DGD2SearchResultPlus extends DGD2SearchResultView implements SearchResultPlus {

    private ArrayList<Hit> hits;
    private Boolean cutoff;
    
    @Override
    public Boolean getCutoff() {
        return cutoff;
    }

    public void setCutoff(Boolean cutoff) {
        this.cutoff = cutoff;
    }

    @Override
    public ArrayList<Hit> getHits() {
        return hits;
    }

    public void setHits(ArrayList<Hit> hits) {
        this.hits = hits;
    }

}
