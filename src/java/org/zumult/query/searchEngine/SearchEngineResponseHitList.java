/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import java.util.ArrayList;
import org.zumult.query.Hit;

/**
 *
 * @author Elena
 */
public class SearchEngineResponseHitList extends SearchEngineResponse {
    private ArrayList<Hit> hits;

    public ArrayList<Hit> getHits() {
        return hits;
    }

    public void setHits(ArrayList<Hit> hits) {
        this.hits = hits;
    }

    
}
