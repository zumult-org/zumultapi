/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.zumal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import org.zumult.indexing.Indexer;
import org.zumult.indexing.measures.Measure_1;
import org.zumult.indexing.measures.Measure_12;
import org.zumult.indexing.measures.Measure_13;
import org.zumult.indexing.measures.Measure_14;
import org.zumult.indexing.measures.Measure_7;
import org.zumult.indexing.measures.Measure_8;

/**
 *
 * @author thomas.schmidt
 */
public class IndexAll implements Indexer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new IndexAll().index();
        } catch (IOException ex) {
            Logger.getLogger(IndexAll.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void index() throws IOException {
        try {
            // calculate measures needed for Josip's prototype
            new Measure_1().doit();                         // ==> data/Measure_1.xml -- Wordlist intersections, takes long
            new Measure_7().doit();                         // ==> data/Measure_7.xml -- Normalisation rate     
            new Measure_8().doit();                         // ==> data/Measure_8.xml -- Overlap measures, this one takes long, about 3 hours!
            new Measure_12().index();                       // ==> data/Measure_12.xml -- Speech rate, this one takes long, about 1 hour!     
            new Measure_13().doit();                       // ==> added this, don't know what it is yet -- pos
            new Measure_14().doit();                        // ==> added this, don't know what it is yet -- oral phenomena
            
            // Metadata indices for corpora
            new SpeechEventIndex().index();                 // ==> data/FOLK_SpeechEventIndex.xml -- takes a little more than a minute
            new AddMeasuresToSpeechEventIndex().index();    // ==> data/FOLK_SpeechEventIndex.xml -- takes seconds
            new SpeechEventIndex2Json().index();            // ==> data/prototypeJson/FOLK.json -- takes seconds
            
            /*
            [From https://docs.google.com/document/d/1jeMpzoNZjE1mw5MdUpK0JAlQYmnD4fxqjkNKmA-CR24/edit?usp=sharing]
            There are six scripts that generate the files necessary for running Prototype: Preselection.
            These are
            -- OutputIDLists.java,                      (don't need this one any longer)
            -- OutputPrototypeJson.java,                (don't need this one any longer)
            -- OutputArtListAsJson.java,
            -- OutputGespraechstypListAsJson.java,
            -- OutputSprachregionListJson.java, and
            -- OutputThemenListAsJson.java [...].
            They need to be executed in that order in order to
            ensure that the final output data will be up to date.
            */
            
            new OutputArtListAsJson().index();              // ==> data/prototypeJson/artFOLK.json
            new OutputGespraechstypListAsJson().index();    // ==> data/prototypeJson/gesprachstypTreeselectFOLK.json
            new OutputSprachregionListJson().index();       // ==> data/prototypeJson/sprachregionTreeselectFOLK.json
            new OutputThemenListAsJson().index();           // ==> data/prototypeJson/themenFOLK.json
            new OutputLanguageListAsJson().index();           // ==> data/prototypeJson/langFOLK.json
            
        } catch (IOException ex) {
            throw new IOException(ex);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SAXException | ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(IndexAll.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        
        
    }
    
}
