/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

import java.util.ArrayList;
import java.util.HashMap;
import org.zumult.objects.Identifiable;

/**
 *
 * @author Elena Frick
 */
 public interface Hit {
    public int getPosition();
    public String getDocId();
    public ArrayList<Match> getMatches();
    public Match getFirstMatch();
    public Match getLastMatch();
    public HashMap<String, String> getMetadata();
        
    public interface Match extends Identifiable{
        String getType();           // word token, pause, punctuation etc.
        Integer getStartPosition();
        Integer getEndPosition();
        Integer getStartOffset();
        Integer getEndOffset();
        double getStartInterval();
        double getEndInterval();
    }
 }
