/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import java.util.ArrayList;
import java.util.HashMap;
import org.zumult.query.Hit;
import org.zumult.query.Hit.Match;

/**
 *
 * @author Elena
 */
public class SearchEngineHit implements Hit {
    private int position; // hit number/row
    private String docId; // transkript id
    private final ArrayList<Match> matches = new ArrayList<>();
    private HashMap<String, String> metadata;

    @Override
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
    
        
    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getDocId() {
        return docId;
    }

    @Override
    public ArrayList<Match> getMatches() {
        return matches;
    }
    
    public void addMatch(SearchEngineMatch match){
        matches.add(match);
    }
    
    public void setPosition(int position) {
        this.position = position;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
    
    public void printMatches(){
        matches.forEach((match) -> {
            System.out.println("{" + match.getID() + "-" + match.getStartPosition() + "}");
        });
    }
    
    @Override
    public Match getFirstMatch(){
        int firstPosition = -1;
        for (Match match: matches){
            int actualPosition = match.getStartPosition();
            if (firstPosition == -1 || actualPosition < firstPosition){
                firstPosition = match.getStartPosition();
            }
        }
        
        for (Match match: matches){
            if (match.getStartPosition() == firstPosition){
                return match;
            }
        }
        return null;
    }   
    
    public Match getLastMatch(){
        int lastPosition = -1;
        for (Match match: matches){
            int actualPosition = match.getEndPosition();
            if (lastPosition == -1 || actualPosition > lastPosition){
                lastPosition = match.getEndPosition();
            }
        }
        
        for (Match match: matches){
            if (match.getEndPosition() == lastPosition){
                return match;
            }
        }
        return null;
    }

}
