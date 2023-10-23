/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.zumult.objects.IDList;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 */
public class COMASearchEngine extends MTASBasedSearchEngine {

    @Override
    public SearchEngineResponseHitList searchRepetitions(ArrayList<String> indexPaths, String queryString, String metadataQueryString, Integer from, Integer to, Boolean cutoff, IDList metadataIDs, ArrayList<Repetition> repetitions, HashMap<String, HashSet> synonyms, HashMap<String, String[]> customWordLists) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
