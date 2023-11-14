/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine.util;

import org.apache.commons.codec.language.ColognePhonetic;
import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 *
 * @author Frick
 */
public class SimilarityUtilities {
    
    public static double getJaccardDistance (String s1, String s2){
        double jaccardDistance = (1 - new JaccardDistance()
                    .apply(s1.toLowerCase(), s2.toLowerCase())) * 100;
        return jaccardDistance;
    }
    
    public static double getJaccardDistanceOfSoundexValue (String s1, 
                                                           String s2){
        ColognePhonetic soundex = new ColognePhonetic();
        
     /*   System.out.println("Soundex codes: " 
                        + s1 + " = " + soundex.encode(s1) + " " 
                        + s2 + " = " + soundex.encode(s2)); */

        double jaccardDistanceOfSoundexValue = 
                        (1 - new JaccardDistance()
                        .apply(soundex.encode(s1), 
                              soundex.encode(s2))) * 100;
        
        return jaccardDistanceOfSoundexValue;
    }
    
    public static double getJaroWinklerDistance (String s1, String s2) {
        double jaroWinklerDistance = new JaroWinklerDistance()
                .apply(s1.toLowerCase(), s2.toLowerCase()) * 100;
        
        return jaroWinklerDistance;
    }
    
    public static double getLevenshteinDistance (String s1, String s2) {
       double levenshteinDistance = LevenshteinDistance.getDefaultInstance()
               .apply(s1.toLowerCase(), s2.toLowerCase());
 
       double maxLength = Double.max(s1.length(), s2.length());       
       double result = (maxLength - levenshteinDistance) / maxLength;
       
    /*   System.out.println("LevenshteinDistance of '" 
               + s1 + "' & '" + s2
               + "': Need to change "
               + levenshteinDistance 
               + " characters to match both strings: " 
               + result + "% similar");*/

       return result * 100;
    }
}
