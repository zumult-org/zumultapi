/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
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
import org.zumult.query.SearchStatistics;
import org.zumult.query.StatisticEntry;

/**
 *
 * @author Elena
 */
public class Measure_14 {  // measure "oralPhenomena"
    BackendInterface backendInterface;
    private static final File DONWLOAD_DIRECTORY = new File(new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().getPath()).
            getParentFile().getParentFile(), "downloads");  // \ids-sample\build\web\downloads
    
    public static void main(String[] args) {
        try {
            new Measure_14().doit();
        } catch (IOException ex) {
            Logger.getLogger(Measure_14.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void doit() throws IOException{
        getMeasure("FOLK");
        //getMeasure("GWSS");
    }
    
    private void getMeasure(String corpusID) throws IOException{

        File file = new File(DONWLOAD_DIRECTORY + "//Measure_14_" +corpusID+".xml");
        OutputStreamWriter bw = null;
        try{
            
            backendInterface = BackendInterfaceFactory.newBackendInterface(); 
            
            System.out.println("Getting all clitic elements... Please wait!");
            
            ArrayList<String> combinations = getPosTagsForCliticAndAssimilated(corpusID);
            
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
                String query = "<word/> within <"+ Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID +"=\""+speechEventID+"\"/>";                
                SearchResultPlus sr = backendInterface.search(query, null,null, "corpusSigle=\""+ corpusID + "\"", null, 0,0, null, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", null);
                int wordTokenTotal = sr.getTotalHits();
                
                System.out.println("originalTokens: " + originalTokens + "; wordTokenTotal: " + wordTokenTotal);
                bw.write("<measures speechEventID=\""+speechEventID+"\" tokens=\""+String.valueOf(wordTokenTotal)+"\">");    
               
                if (corpusID.equals("GWSS") && !e_se_sprachen_value.startsWith("Deutsch")){
                    writePOS(bw, "NGHES");
                    writePOS(bw, "NGIRR");
                    writePOS(bw, "PTKMA");
                    writePOS(bw, "SEDM");
                    writePOS(bw, "SEQU");
                    writePOS(bw, "CLITIC");
                }else {
                    double tokenTotal = Double.parseDouble(String.valueOf(wordTokenTotal));

                    int hits = search("NGHES.*", speechEventID, corpusID);
                    double rel = getRel(hits, tokenTotal);
                    writePOS(bw, "NGHES", hits, rel);

                    hits = search("NGIRR.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "NGIRR", hits, rel);

                   /* hits = search("NGONO.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "NGONO", hits, rel);

                    hits = search("PTKIFG.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "PTKIFG", hits, rel);

                    hits = search("NGAKW.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "NGAKW", hits, rel);*/

                    hits = search("PTKMA.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "PTKMA", hits, rel);

                    hits = search("SEDM.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "SEDM", hits, rel);

                    hits = search("SEQU.*", speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "SEQU", hits, rel);

                    /*

                    for FOLK:

                    165
                    [ADJA|NN, ADJD|ART, ADJD|PPER, ADV|ADJD, ADV|ADV, ADV|APPR, ADV|ART, ADV|KON, ADV|PIS, ADV|PPER, ADV|PTKIFG, ADV|PTKMA, ADV|PTKMWL, 
                    ADV|PTKVZ, ADV|VAFIN, ADV|VMFIN, ADV|VVFIN, ADV|VVINF, APPR|ADV, APPR|ART, APPR|PDS, APPR|PPER, APPR|PPOSAT, ART|ADJA, ART|ART, 
                    ART|NN, ART|PIDAT, ART|PIDS, KOKOM|ART, KOKOM|PPER, KON|ADV, KON|APPR, KON|ART, KON|KOUS, KON|NN, KON|PDS, KON|PIS, KON|PPER, 
                    KON|PTKMA, KON|VAFIN, KON|VVFIN, KOUS|ART, KOUS|PDS, KOUS|PIS, KOUS|PPER, KOUS|PPER PPER, KOUS|PTKMA, NN|ADJA, NN|ADJD, NN|ADV, 
                    NN|ART, NN|KON, NN|PPER, NN|VAFIN, NN|VVFIN, PDS VAFIN|ART, PDS|ART, PDS|PDS, PDS|PPER, PDS|VAFIN, PDS|VMFIN, PDS|VVFIN, PIDAT|NN, 
                    PIS|ADV, PIS|ART, PIS|PDS, PIS|PPER, PIS|PTKMA, PIS|VMFIN, PPER VMFIN|PPER, PPER|ART, PPER|PDS, PPER|PPER, PPER|PRF, PPER|PTKIFG, 
                    PPER|PTKMA, PPER|VAFIN, PPER|VMFIN, PPER|VVFIN, PRELS|ART, PRELS|PPER, PRF|ART, PRF|PDS, PRF|PPER, PTKIFG|ADV, PTKIFG|APPR, PTKIFG|ART, 
                    PTKIFG|PIAT, PTKIFG|PIS, PTKIFG|PPER, PTKIFG|PTKIFG, PTKIFG|PTKMWL, PTKIFG|VVINF, PTKIFG|VVPP, PTKMA|ADV, PTKMA|APPR, PTKMA|ART, PTKMA|PIS, 
                    PTKMA|PPER, PTKMA|PTKIFG, PTKMA|PTKMA, PTKMA|PTKMWL, PTKMA|PTKVZ, PTKMWL|ADV, PTKMWL|ART, PTKMWL|PIS, PTKMWL|VVINF, PWAV|ART, PWAV|PDS, 
                    PWAV|PIAT, PWAV|PIS, PWAV|PPER, PWAV|VAFIN, PWS|ART, PWS|PPER, PWS|PTKMA, PWS|VAFIN, PWS|VAFIN PTKMA, SEDM|ART, SEDM|PDS, SEDM|PPER, 
                    TRUNC|KON|NN, VAFIN PPER|ART, VAFIN PPER|PDS, VAFIN PPER|PPER, VAFIN PPER|PTKMA, VAFIN|ADJD, VAFIN|APPR, VAFIN|ART, VAFIN|PDS, VAFIN|PIS, 
                    VAFIN|PPER, VAFIN|PPER PPER, VAFIN|PPOSAT, VAFIN|PRF, VAFIN|PTKIFG, VAFIN|PTKMA, VAFIN|VAFIN, VMFIN PPER|PPER, VMFIN PPER|PTKMA, VMFIN|ADV, 
                    VMFIN|ART, VMFIN|PDS, VMFIN|PIS, VMFIN|PPER, VMFIN|PRF, VMFIN|PTKMA, VVFIN PPER|ART, VVFIN PPER|PPER, VVFIN PPER|PTKMA, VVFIN|ADV, VVFIN|ART, 
                    VVFIN|PDS, VVFIN|PIS, VVFIN|PPER, VVFIN|PPER PPER, VVFIN|PTKMA, VVFIN|PTKVZ, VVFIN|VAFIN, VVFIN|VVFIN, VVIMP|ART, VVIMP|PPER, VVIMP|PTKMA, 
                    VVINF|PPER, VVPP|PPER]


                    for GWSS:
                    40
                    [ADV|ADV, ADV|ART, APPR|ART, APPR|PPER, ART|NN, ITJ|ITJ, KOKOM|PPER, KON|PPER, KOUS|PIS, KOUS|PPER, OS|OS, PDS|PPER, PDS|VAFIN, PIS|PPER, 
                    PPER|ART, PPER|PPER, PPER|PRF, PPER|PTKIFG, PPER|VAFIN, PPER|VVFIN, PRELS|PPER, PRF|PPER, PTKIFG|ART, PTKIFG|PIAT, PTKIFG|PIS, PTKMWL|ADV, 
                    PTKMWL|ART, PWAV|PIAT, PWAV|PIS, PWAV|PPER, SEDM|PPER, VAFIN|ART, VAFIN|PDS, VAFIN|PPER, VAFIN|PTKMA, VMFIN|PIS, VMFIN|PPER, VVB|PNP, 
                    VVFIN|PPER, VVIMP|PPER]

                    */
                    hits = searchClitic(combinations, speechEventID, corpusID);
                    rel = getRel(hits, tokenTotal);
                    writePOS(bw, "CLITIC", hits, rel);
                }
                
                bw.write("</measures>");
            }
            bw.write("</measures-document>");
        } catch (IOException | SearchServiceException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Measure_14.class.getName()).log(Level.SEVERE, null, ex);
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
        SearchResultPlus searchResult = backendInterface.search(query, null,null, "corpusSigle=\""+ corpusID + "\"", null, 0,0, null, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", null);
        return searchResult.getTotalHits();
    }
    
    private void writePOS(OutputStreamWriter bw, String pos, int hits, double rel) throws IOException{
        Formatter formatter = new Formatter(new StringBuilder(), Locale.US);
        String str = "<measure type=\"oralPhenomena\" reference=\""+pos+"\" tokens=\""+String.valueOf(hits)+"\" tokens_ratio=\""+formatter.format("%.2f", rel).toString()+"\"/>";
        bw.write(str);
        System.out.println(str);
    }
    
    private void writePOS(OutputStreamWriter bw, String pos) throws IOException{
        String str = "<measure type=\"oralPhenomena\" reference=\""+pos+"\" tokens=\"nicht verfügbar\" tokens_ratio=\"nicht verfügbar\"/>";
        bw.write(str);
        System.out.println(str);
    }
    
    private ArrayList<String> getFilter(){
        ArrayList<String> filter = new ArrayList();
        filter.add("APPR NN");
        filter.add("NN ???");
        filter.add("NN NN");
        filter.add("NN VVPP");
        filter.add("PPER ADV");
        filter.add("PTKIFG ADJD");
        filter.add("VAFIN ADV");
        return filter;
    }
    
    private int searchClitic(ArrayList<String> clitics, String speechEventID, String corpusID) throws IOException, SearchServiceException{
        int sum = 0;
        for (String str: clitics){

            int hits = search(str.replace("|", " "), speechEventID, corpusID);            
            System.out.println("found: "+ hits);
            
            int hits2 = searchAssimilated(str, speechEventID, corpusID);
            System.out.println("found: "+ hits2);
            
            sum = sum + hits + hits2;
        }
        return sum;          
    }
    
    private int searchAssimilated(String str, String speechEventID, String corpusID) throws SearchServiceException, IOException{
        String[] pos = str.split("\\|");
        String query = "([pos=\""+pos[0]+"\"][pos=\""+pos[1]+"\" & word.type=\".*assimilated.*\"]) within <"+ Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID +"=\""+speechEventID+"\"/>";                
        System.out.println(query);
        SearchResultPlus searchResult = backendInterface.search(query, null,null, "corpusSigle=\""+ corpusID + "\"", null, 0,0, null, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", null);
        return searchResult.getTotalHits();
    }
    
    
 /*   private void sortByAbc( ArrayList<StatisticEntry> array){
        Collections.sort(array, new Comparator<StatisticEntry>() {  
            @Override
            public int compare(StatisticEntry o1, StatisticEntry o2) {
                return o1.getMetadataValue().compareTo(o2.getMetadataValue());
            }
        });
    }*/
    
    private ArrayList<String> getPosTagsForCliticAndAssimilated(String corpusID){
        
        ArrayList<String> filter = getFilter();
        
        ArrayList<String> cliticAndAssimilated = new ArrayList();
        
        try {
            SearchStatistics searchStatistics1 = null;
            SearchStatistics searchStatistics2 = null;
            
            searchStatistics1 = backendInterface.getSearchStatistics("[pos=\".+ .+\"]", null, null, "corpusSigle=\""+ corpusID + "\"", null, "pos",
                    1000, 0, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", "ABS_DESC");
            
            searchStatistics2 = backendInterface.getSearchStatistics("<word/>[word.type=\".*assimilated.*\"]", null, null, "corpusSigle=\""+ corpusID + "\"", null, "pos",
                    1000, 0, "TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT", "ABS_DESC");
                        
            
            ArrayList<StatisticEntry> statistics1 = searchStatistics1.getStatistics();
            ArrayList<StatisticEntry> statistics2 = searchStatistics2.getStatistics();
            
            System.out.println();
            System.out.println("------------------------- query all clitics: [pos=\".+ .+\"] ---------------------------------------");
            System.out.println(statistics1.size());
            System.out.println(statistics1);
            System.out.println();
            
           
                        
            System.out.println();
            System.out.println("------------------------- query all assimilated: <word/>[word.type=\".*assimilated.*\"] ---------------------------------------");
            System.out.println(statistics2.size());
            System.out.println(statistics2);
            System.out.println();
            
            
            /***** join both arrays *****/
            TreeMap<String, Integer> all = join(statistics1,  statistics2);
            
            
            Iterator<String> iterator = all.keySet().iterator();
            while(iterator.hasNext()){
                String pos = iterator.next();
                if (filter.contains(pos.replace("|", " "))
                        || pos.contains("AB") || pos.contains("SPELL") || pos.contains("NGHES") || pos.contains("NGIRR") 
                        || pos.contains("XY") || pos.contains("NE") || pos.contains("CARD") || pos.contains("FM")){
                    iterator.remove();
                }
            }
            
            if (corpusID.equals("GWSS")){
                Iterator<String> iteratorGWSS = all.keySet().iterator();
                while(iteratorGWSS.hasNext()){
                String pos = iteratorGWSS.next();
                    if (pos.startsWith("AV0|") 
                            || pos.startsWith("CJS|") 
                            || pos.startsWith("CJT|")
                            || pos.startsWith("CRD|")
                            || pos.startsWith("DT0|")
                            || pos.startsWith("INT|")
                            || pos.startsWith("NN0|")
                            || pos.startsWith("NN1|")
                            || pos.startsWith("TO0|")
                            || pos.startsWith("VBI|")
                            || pos.startsWith("VDB|")
                            || pos.startsWith("VER|")
                            || pos.startsWith("VM0|")
                            || pos.startsWith("VVI|")
                            || pos.startsWith("VVN|")
                            || pos.startsWith("ZZ0|")
                            || pos.startsWith("prep:")
                            || pos.startsWith("AVQ|")
                            || pos.startsWith("EX0|")
                            || pos.startsWith("NP0|")
                            || pos.startsWith("DET:def|")
                            || pos.startsWith("DTQ|")
                            || pos.startsWith("AJ0|")
                            || pos.startsWith("PNQ|")
                            || pos.startsWith("PNI|")
                            || pos.startsWith("AV0|")
                            || pos.startsWith("VVD|")
                            || pos.startsWith("PNP|")
                            ||pos.endsWith("|ZZ0")
                            
                            ){
                        iteratorGWSS.remove();
                    }
                }
            }
            
            System.out.println();
            System.out.println("After filtering: ");
            System.out.println();
            System.out.println(all.size());
            System.out.println(all);
            
            List<Map.Entry<String, Integer>> list = new ArrayList(all.entrySet());

            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {                          
                    return o1.getValue().compareTo(o2.getValue());
                }
            });

            Map<String, Integer> sorted = new LinkedHashMap();

            for(Map.Entry<String, Integer> element : list) {
                sorted.put(element.getKey(), element.getValue());
            }
            
            System.out.println();
            System.out.println("Sorted by Value: ");
            System.out.println();
            System.out.println(sorted.size());
            System.out.println(sorted); 

            // remove pos with value 1
            Iterator<String> iterator2 = all.keySet().iterator();
            while(iterator2.hasNext()){
                String pos = iterator2.next();
                if (all.get(pos) == 1){
                    iterator2.remove();
                }
            }
            
            System.out.println();
            System.out.println("-------------------------- After removing all pos with value 1: ----------------------------");
            System.out.println();
            System.out.println(all.size());
            System.out.println(all);
                        
            int treshold = countQuantil(getSortedValues(all), 0.5);
            
            Iterator<String> iterator3 = all.keySet().iterator();
            while(iterator3.hasNext()){
                String pos = iterator3.next();
                int n = all.get(pos);
                if (n>=treshold){
                    cliticAndAssimilated.add(pos);
                }
            }
            
            
             
        }catch (SearchServiceException | IOException ex) {
            Logger.getLogger(Measure_14.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println();
        System.out.println("--------------- Clitic: ---------------");
        System.out.println();
        System.out.println(cliticAndAssimilated.size());
        System.out.println(cliticAndAssimilated);
        
        return cliticAndAssimilated;
    }
    
    private List<Integer> getSortedValues(TreeMap<String, Integer> map){
        List<Integer> values = new ArrayList(map.values());
            System.out.println();
            System.out.println("Values: ");
            System.out.println(values);
            System.out.println();
            Collections.sort(values);
            System.out.println("Sorted: ");
            System.out.println(values);
            return values;
    }
    
    private static int countQuantil(List<Integer> list, double quantil) {
        int index = (int) Math.ceil(quantil * list.size());
        System.out.println();
        System.out.println("Quantil " + quantil + ": n*p = " + index);
        return list.get(index-1);
    }
    
    
    private TreeMap<String, Integer> join( ArrayList<StatisticEntry> statistics1,  ArrayList<StatisticEntry> statistics2){
        Map<String, Integer> map = new HashMap();
            
        // add map values from statistics2
        for (StatisticEntry entry: statistics2){
            String pos = entry.getMetadataValue();
            int n = entry.getNumberOfHits();
            map.put(pos, n);
        }
            
        // add map values from statistics1
        for(StatisticEntry entry1: statistics1){
            String pos = entry1.getMetadataValue().replace(" ", "|");
            int n = entry1.getNumberOfHits();
            if(map.containsKey(pos)){
                map.put(pos, n+map.get(pos));
            }else{
                map.put(pos, n);
            }
        }
        
        TreeMap<String, Integer> sorted = new TreeMap();
        sorted.putAll(map);
        
        System.out.println("------------------------- After joining categories: -------------------------------");
        System.out.println();
        System.out.println(sorted.size());
        System.out.println(sorted);
            
        return sorted;
    }
    
}
