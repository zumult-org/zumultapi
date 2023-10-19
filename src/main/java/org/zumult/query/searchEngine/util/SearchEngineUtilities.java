/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine.util;

/**
 *
 * @author Frick
 */
public class SearchEngineUtilities {
    
    public static String IPA2HTML(String s){
        String result = s.replace("ɔ", "&#596;")
                .replace("ɛ", "&#603;")
                .replace("ʔ", "&#660;")
                .replace("ɪ", "&#618;")
                .replace("ʃ", "&#643;")
                .replace("ʊ", "&#650;")
                .replace("ə", "&#601;")
                .replace("ɐ", "&#592;")
                .replace("ː", "&#058;")
                .replace("ʏ", "&#655;")
                .replace("ŋ", "&#331;")
                .replace("ɡ", "&#609;");
                //.replace("ç", "&#231;");
        return result;
    }
    
    public static String HTML2IPA(String s){
        String result = s.replace("&#596;", "ɔ")
                .replace("&#603;", "ɛ")
                .replace("&#660;", "ʔ")
                .replace("&#618;", "ɪ")
                .replace("&#643;", "ʃ")
                .replace("&#650;", "ʊ")
                .replace("&#601;","ə")
                .replace("&#592;", "ɐ")
                .replace("&#058;", "ː")
                .replace("&#655;", "ʏ")
                .replace("&#331;", "ŋ")
                .replace("&#609;", "ɡ");
                //.replace("&#231;", "ç");
        return result;
    }
}
