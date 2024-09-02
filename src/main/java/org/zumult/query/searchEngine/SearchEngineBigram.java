/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import org.zumult.query.Bigram;

/**
 *
 * @author Frick
 */
public class SearchEngineBigram implements Bigram {
    
    private int position; // bigram number/row
    private String queryMatch;
    private String partner;
    private int numberOfHits;
    private BigramType type;
    
    public SearchEngineBigram (String queryMatch,
                               String partner,
                               int numberOfHits,
                               BigramType type,
                               int position) {
        
        this.queryMatch = queryMatch;
        this.partner = partner;
        this.numberOfHits = numberOfHits;
        this.type = type;
        this.position = position;
    }
            
    public void setQueryMatch(String queryMatch) {
        this.queryMatch = queryMatch;
    }

    public void setNumberOfHits(int numberOfHits) {
        this.numberOfHits = numberOfHits;
    }
    
    public void setPartner(String partner) {
        this.partner = partner;
    }   
    
    @Override
    public String getQueryMatch() {
        return queryMatch;
    }

    @Override
    public String getPartner() {
        return partner;
    }

    @Override
    public int getNumberOfHits() {
        return numberOfHits;
    }
    
    @Override
    public BigramType getType() {
        return type;
    }

    public void setType(BigramType type) {
        this.type = type;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int getPosition() {
        return position;
    }   

}