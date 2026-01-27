/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.fcs;

import org.zumult.io.Constants;

/**
 *
 * @author bernd
 */
public class FCSQL2CQPTranslator {
    
    /*    Einfache Anfragen
        
        Suche nach einzelnen Begriffen
        "Semmel"
        
        Einbindung von regulären Ausdrücken

        "gie(ss|ß)en"
    
        Suche ohne Beachtung der Groß- und Kleinschreibung
        "essen"/c
    
        Komplexe Anfragen
        Einbeziehung von Annotationsebenen
        Suche nach einzelnen Begriffen auf Basis der Annotationsebene für Oberflächenformen

        [text = "Semmel"]
        [text = "essen"/c]
        Suche nach Adverbien laut Standardfoundry.
        [pos="ADV"]    
    */

    public static String translateFCSQL2CQP(String fcsqlString){        
        if (fcsqlString.trim().startsWith("\"")){
            // this is a simple query
            String cqpString = fcsqlString.trim();
            // get rid of a traling /c which would mean ignore case
            cqpString = cqpString.replaceAll("/c", "");
            cqpString = "[" + Constants.BASIC_LAYER_NAME + "=" + cqpString + "]";
            return cqpString;
        }
        
        // this is what comes out of the FCS query builder
        // pos, however, might mean udpos
        // [ pos = "ADJ" & word = "nice" & lemma = "nice" & norm = "nice" ]
        String cqpString = fcsqlString.replaceAll("\\n", "\"");
        cqpString = cqpString.replaceAll("/c", "");
        return cqpString;
    }
    
}
