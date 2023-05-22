/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.measures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DefaultTokenList;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Elena
 */
public class Measure_13 { // measure "pos"
    BackendInterface backendInterface;
  /*  private static final File DONWLOAD_DIRECTORY = new File(new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().getPath()).
            getParentFile().getParentFile(), "downloads");  // \ids-sample\build\web\downloads*/
    
    String[] corpusIDs = {"FOLK", "GWSS"};
    String OUTPUT_PATH = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_MEASURES_PATH;
    
    public static void main(String[] args) {
        try {
            new Measure_13().doit();
        } catch (IOException ex) {
            Logger.getLogger(Measure_13.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void doit() throws IOException{
        for (String CorpusID : corpusIDs){
            getMeasure(CorpusID);
        }
    }
    
    private void getMeasure(String corpusID) throws IOException{
        
        //File file = new File(DONWLOAD_DIRECTORY + "//Measure_13_" +corpusID+".xml");
        File file = new File(OUTPUT_PATH + "/Measure_13_" +corpusID+".xml");
        OutputStreamWriter bw = null;
        try{
            backendInterface = BackendInterfaceFactory.newBackendInterface(); 
            
            IDList speechEvents = backendInterface.getSpeechEvents4Corpus(corpusID);
            bw = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");

            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.write("<measures-document>");
            for (String speechEventID: speechEvents){
                System.out.println("---------"+ speechEventID +"--------");
                SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
                IDList transcripts = speechEvent.getTranscripts();
                
                MetadataKey e_se_sprachen = backendInterface.findMetadataKeyByID("v_e_se_sprachen");
                String e_se_sprachen_value = speechEvent.getMetadataValue(e_se_sprachen);
                System.out.println("e_se_sprachen: "+ e_se_sprachen_value);
                
                // get pos token list
                TokenList posList4SpeechEvent = new DefaultTokenList("pos");
                for (String transcriptID : transcripts){
                    Transcript transcript = backendInterface.getTranscript(transcriptID);
                    TokenList posList4Transcript = transcript.getTokenList("pos");
                    posList4SpeechEvent = posList4SpeechEvent.merge(posList4Transcript);
                }
                int originalTokens = posList4SpeechEvent.getNumberOfTokens();
                
                // search all <w>-elements 
                String query = "<word/> within <" + Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID + "=\""+speechEventID+"\"/>";                
                SearchResultPlus sr = backendInterface.search(query, null,null, "corpusSigle=\""+ corpusID + "\"", null, 0,0, null, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", null, null);
                int wordTokenTotal = sr.getTotalHits();
                
                System.out.println("originalTokens: " + originalTokens + "; wordTokenTotal: " + wordTokenTotal);
                
                bw.write("<measures speechEventID=\""+speechEventID+"\" tokens=\""+String.valueOf(wordTokenTotal)+"\">");    
               
                if (corpusID.equals("GWSS") && !e_se_sprachen_value.startsWith("Deutsch")){
                    writePOS(bw, "NN");
                    writePOS(bw, "NE");
                    writePOS(bw, "V");
                    writePOS(bw, "ADJ");
                    writePOS(bw, "ADV");
                    writePOS(bw, "PTKVZ");
                }else{
                    double tokenTotal = Double.parseDouble(String.valueOf(wordTokenTotal));   
                    
                    int hits = search("NN.*", speechEventID, corpusID);
                    double rel = getRel(hits, tokenTotal);
                    writePOS(bw, "NN", hits, rel);

                    hits = search("NE.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "NE", hits, rel);

                    hits = search("VV.*|VM.*|VA.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "V", hits, rel);

                    hits = search("ADJ(A|D).*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "ADJ", hits, rel);

                    hits = search("ADV.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "ADV", hits, rel);

                    hits = search("PTKVZ", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "PTKVZ", hits, rel);
                }
                
                bw.write("</measures>");
            }
            bw.write("</measures-document>");
        } catch (IOException | SearchServiceException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Measure_13.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            if (bw!=null){
                bw.close();
            }
        }              
    }
 
    private double getRel(int abs, double total){
        Double rel = abs*100/total;
        System.out.println("found: " + abs + " (" + rel + ") => " + String.format("%1.2f", rel) );
        return rel;
    }
    
    private int search(String pos, String speechEventID, String corpusID) throws SearchServiceException, IOException{
        String query = "[pos=\""+pos+"\"] within <"+ Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID +"=\""+speechEventID+"\"/>";                
        System.out.println(query);
        SearchResultPlus searchResult = backendInterface.search(query, null,null, "corpusSigle=\""+ corpusID + "\"", null, 0,0, null, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", null, null);
        return searchResult.getTotalHits();
    }
    
    private void writePOS(OutputStreamWriter bw, String pos, int hits, double rel) throws IOException{
        Formatter formatter = new Formatter(new StringBuilder(), Locale.US);
        String str = "<measure type=\"pos\" reference=\""+pos+"\" tokens=\""+String.valueOf(hits)+"\" tokens_ratio=\""+formatter.format("%.2f", rel).toString()+"\"/>";
        bw.write(str);
        System.out.println(str);
    }
    
    private void writePOS(OutputStreamWriter bw, String pos) throws IOException{
        String str = "<measure type=\"pos\" reference=\""+pos+"\" tokens=\"nicht verfügbar\" tokens_ratio=\"nicht verfügbar\"/>";
        bw.write(str);
        System.out.println(str);
    }
    
}
