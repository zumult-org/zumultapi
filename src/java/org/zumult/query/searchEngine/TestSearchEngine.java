/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.query.Hit;
import org.zumult.query.Hit.Match;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 */
public class TestSearchEngine {
    public static void main(String[] args) {
        try {
            new TestSearchEngine().doit();
        } catch (SearchServiceException ex) {
            Logger.getLogger(TestSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void doit() throws SearchServiceException, IOException{
        ArrayList<Repetition> twoRepetitions = new ArrayList();
        twoRepetitions.add(new Repetition("WORD", "EQUAL", false, null, new HashSet<String>(), null, null, null, null, "s_geschlecht=\"Männlich\"&ses_rolle_s=\"Partner/in\"", "FOLLOWEDBY", 0, 5, null, null));
        twoRepetitions.add(new Repetition("WORD", "EQUAL", true, null, new HashSet<String>(), null, null, null, null, null, null, null, null, null, null));
        
        ArrayList<Repetition> repetition = new ArrayList();
        repetition.add(new Repetition("WORD", "EQUAL", true, null, new HashSet<String>(), null, null, null, 10, null, null, null, null, null, null));
                
        MTASBasedSearchEngine se = new MTASBasedSearchEngine();
        SearchEngineResponseHitList result= se.searchRepetitions(
                new ArrayList<String>(Arrays.asList("C:\\Users\\Frick\\IDS\\ZuMult\\indices\\TB_FOLK")), 
                //"[word=\".+\" & !norm=\"das\"] precededby [norm=\"was\"][norm=\"heißt\"][norm=\"denn\"]?", null, 0, 10, null, null, RepetitionTypeEnum.WORD, false, repetition);
                "[pos=\"NN\"]", null, 0, 10, null, null, repetition, null, null);
                //"[word=\"vier\"][word=\"krawatten\"]", null, 0, 10, null, null, repetition);
                //"[word=\"fronleichnam\"] within <s_geschlecht=\"Weiblich\"/>", null, 0, 10, RepetitionTypeEnum.WORD, true, twoRepetitions);
                //"([]{3,5}) fullyalignedwith <annotationBlock/>", null, 0, 10, RepetitionTypeEnum.WORD, false, repetition);
                
        System.out.println();
        System.out.println("hits_total: " + result.getHitsTotal());
        System.out.println("transcripts_total: " + result.getTranscriptsTotal());
        ArrayList<Hit> hits = result.getHits();
        for (Hit hit: hits){
            System.out.println();
            System.out.println("Transcript: " + hit.getDocId());
            ArrayList<Match> matches = hit.getMatches();
            for(Match match: matches){
                System.out.println("match.getID(): " + match.getID());
            }
        }
    }
}
