/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.measures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.XMLReader;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.AndFilter;
import org.zumult.objects.implementations.DefaultTokenList;
import org.zumult.objects.implementations.NegatedFilter;
import org.zumult.objects.implementations.TokenListTokenFilter;

/**
 *
 * @author Thomas_Schmidt
 */
public class Measure_1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TransformerException {
        try {
            new Measure_1().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    String[] WORDLISTS = {  "GOETHE_A1", "GOETHE_A2", "GOETHE_B1", 
                            "HERDER_1000", "HERDER_2000", "HERDER_3000", "HERDER_4000", "HERDER_5000"};


    String[] corpusIDs = {"FOLK", "GWSS"};   
    String OUTPUT_PATH = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_MEASURES_PATH;

    
    public void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException, TransformerException {
        // Connect to DGD
        for (String CORPUS : corpusIDs){
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();

            StringBuilder sb = new StringBuilder();
            StringBuilder xml = new StringBuilder();
            xml.append("<measures-document>");


            ArrayList<TokenList> tokenLists = new ArrayList<>();
            for (String WL : WORDLISTS){
                if(WL.startsWith("GOETHE")){
                    tokenLists.add(XMLReader.readTokenListFromFile(new File(Constants.WORDLISTS_GOETHE_PATH + "/" + WL + ".xml")));
                }else if (WL.startsWith("HERDER")){
                    tokenLists.add(XMLReader.readTokenListFromFile(new File(Constants.WORDLISTS_HERDER_PATH + "/" + WL + ".xml")));
                }else{
                    throw new ClassNotFoundException(WL + " could not be found!");
                }
            }


            // read the list with POS for filtering...
            TokenList posFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/MEASURE_1_POS_FILTER.xml");
            // ... make a filter from it and negate it (since we want these POS to be excluded, not included)
            TokenFilter posFilter = new NegatedFilter(new TokenListTokenFilter("lemma", posFilterTokenList));

            // read the list with NGIRR tokens for filtering...
            TokenList ngirrFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/FOLK_NGIRR_OHNE_DEREWO.xml");
            // ... make a filter from it and negate it (since we want these forms to be excluded, not included)
            TokenFilter ngirrFilter = new NegatedFilter(new TokenListTokenFilter("lemma", ngirrFilterTokenList));

            // combine the two filters into one
            TokenFilter filter = new AndFilter(posFilter, ngirrFilter);


            // get all events from FOLK
            IDList folkEventIDs = backendInterface.getEvents4Corpus(CORPUS);
            // iterate through them
            for (String eventID : folkEventIDs){
                // get the metadata for this event
                Event event = backendInterface.getEvent(eventID);
                // get all subordinate speech event IDs
                IDList speechEventIDs = event.getSpeechEvents();
                // iterate through them
                for (String speechEventID : speechEventIDs){
                    System.out.println(speechEventID);
                    sb.append(speechEventID);
                    // keep track of processing time
                    long start = System.currentTimeMillis();
                    // get IDs for all transcripts belonging to the current speech event
                    IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                    // make a new lemma list for this speech event
                    TokenList lemmaList4SpeechEvent = new DefaultTokenList("lemma");
                    for (String transcriptID : transcriptIDs){
                        // get the transcript...
                        Transcript transcript = backendInterface.getTranscript(transcriptID);
                        // ... and get its lemma list, applying the filter defined above
                        TokenList lemmaList4Transcript = transcript.getTokenList("lemma", filter);
                        // merge this transcript's lemma list with the lemmalist for the entire speech event
                        lemmaList4SpeechEvent = lemmaList4SpeechEvent.merge(lemmaList4Transcript);
                    }
                    // how many types do we have in the original (filtered) lemma list for the speech event?
                    int originalLemmas = lemmaList4SpeechEvent.getNumberOfTypes();
                    int originalTokens = lemmaList4SpeechEvent.getNumberOfTokens();

                    String xmlString = "<measures speechEventID=\"" + speechEventID + "\"";
                    xmlString+=" lemmas=\"" + originalLemmas + "\"";
                    xmlString+=" tokens=\"" + originalTokens + "\">";
                    
                    // get value for e_se_sprachen, important for non-German data
                    SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
                    MetadataKey e_se_sprachen = backendInterface.findMetadataKeyByID("v_e_se_sprachen");
                    String e_se_sprachen_value = speechEvent.getMetadataValue(e_se_sprachen);           

                    int i = 0;
                    for (TokenList tl : tokenLists){                    
                        // intersect the lemmalist for the speech event with the reference lemma list 
                        System.out.println("   " + WORDLISTS[i]);
                        
                        if (CORPUS.equals("GWSS") && !e_se_sprachen_value.startsWith("Deutsch")){
                            xmlString+="<measure type=\"intersection\" reference=\"" +  WORDLISTS[i] + "\"";
                            xmlString+=" lemmas=\"nicht verfügbar\"";
                            xmlString+=" tokens=\"nicht verfügbar\"";
                            xmlString+=" lemmas_ratio=\"nicht verfügbar\"";
                            xmlString+=" tokens_ratio=\"nicht verfügbar\"/>";
                        }else{
                            TokenList intersect = lemmaList4SpeechEvent.intersect(tl);  
                            //System.out.println("intersect");
                            //System.out.println(intersect);
                            int intersectionLemmas = intersect.getNumberOfTypes();
                            int intersectionTokens = intersect.getNumberOfTokens();
                            double lemmasRatio = (double) intersectionLemmas / originalLemmas;
                            double tokensRatio = (double) intersectionTokens / originalTokens;

                            xmlString+="<measure type=\"intersection\" reference=\"" +  WORDLISTS[i] + "\"";
                            xmlString+=" lemmas=\"" + intersectionLemmas + "\"";
                            xmlString+=" tokens=\"" + intersectionTokens + "\"";
                            xmlString+=" lemmas_ratio=\"" + String.format(Locale.US, "%.2f", lemmasRatio) + "\"";
                            xmlString+=" tokens_ratio=\"" + String.format(Locale.US, "%.2f", tokensRatio) + "\"/>";
                        }
                        i++;
                    }


                    // stopwatch
                    long end = System.currentTimeMillis();

                    // output numbers for this speech event
                    xmlString+="</measures>";

                    //System.out.println(xmlString);
                    xml.append(xmlString);

                }
            }

            xml.append("</measures-document>");
            
            String fileName = "Measure_1_" + CORPUS + ".xml";
            String path = new File(OUTPUT_PATH + fileName).getPath();
            System.out.println(fileName + " is written to " + path);
            String xmlString = xml.toString();
            Files.write(Paths.get(path), xmlString.getBytes("UTF-8"));
        }        

    }
    
}
