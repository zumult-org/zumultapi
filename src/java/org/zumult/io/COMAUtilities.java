/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

/**
 *
 * @author thomasschmidt
 */
public class COMAUtilities {
    
    public static String getZumultNameForComaName(String comaName){
        switch (comaName){
            case "Communication" : return "SpeechEvent";
            case "Speaker" : return "Speaker";
            case "Transcription" : return "Transcript";
            case "Media" : return "Media";
            default : return "";
        }
    }
    
    public static String getComaNameForZumultName(String zumultName){
        switch (zumultName){
            case "SpeechEvent" : return "Communication";
            case "Speaker" : return "Speaker";
            case "Transcript" : return "Transcription";
            case "Media" : return "Media";
            default : return "";
        }
    }

}
