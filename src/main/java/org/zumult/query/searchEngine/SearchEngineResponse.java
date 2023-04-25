/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

/**
 *
 * @author Elena
 */
public class SearchEngineResponse {
    private int transcripts_total; // number of transcripts containing hits
    private int hits_total; //number of hits
    
    public int getTranscriptsTotal() {
        return transcripts_total;
    }

    public void setTranscriptsTotal(int transcripts_total) {
        this.transcripts_total = transcripts_total;
    }
    
    public int getHitsTotal() {
        return hits_total;
    }

    public void setHitsTotal(int hits_total) {
        this.hits_total = hits_total;
    }
}
