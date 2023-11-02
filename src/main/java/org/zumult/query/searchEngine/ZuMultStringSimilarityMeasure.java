/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import org.zumult.query.searchEngine.util.SimilarityUtilities;

/**
 *
 * @author Frick
 */
public class ZuMultStringSimilarityMeasure {

    public boolean apply(String s1, String s2) {
        
        if (s1.toLowerCase().contains(s2.toLowerCase()) 
                || s2.toLowerCase().contains(s1.toLowerCase())) {
            /* this step is required because similarity measures 
            are not sufficient to identify "gut" and "supergut" as similar:
            
            JaccardDistance: 43.00000000000001% similar
            Soundex codes: gut = 42 supergut = 81742
            JaccardDistance (Soundex): 40.0% similar */
            
            return true;
        } else {
            // check Jaccard distance (case sensitive)
            if (SimilarityUtilities.getJaccardDistance (s1, s2) > 75.0){
                return true;
            } else {
                // check Jaccard distance of soundex value (case insensitive)
                if (SimilarityUtilities
                        .getJaccardDistanceOfSoundexValue (s1, s2) > 75.0){
                    return true;
                }
            }
            
            return false;
        }
    }
}
