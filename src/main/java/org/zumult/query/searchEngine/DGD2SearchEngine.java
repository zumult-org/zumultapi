/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.std.TextFileSorter;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.LexUnit;
import de.tuebingen.uni.sfs.germanet.api.Synset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import mtas.codec.util.CodecInfo;
import mtas.codec.util.CodecSearchTree;
import mtas.codec.util.CodecUtil;
import mtas.search.spans.util.MtasSpanQuery;
import org.apache.commons.text.similarity.FuzzyScore;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.mvel2.MVEL;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.objects.IDList;
import org.zumult.query.Hit;
import org.zumult.query.SearchServiceException;
import org.zumult.query.searchEngine.Repetition.PositionOverlapEnum;
import org.zumult.query.searchEngine.Repetition.PositionSpeakerChangeEnum;
import org.zumult.query.searchEngine.Repetition.SimilarityTypeEnum;
import org.zumult.query.searchEngine.util.SimilarityUtilities;

/**
 *
 * @author Frick
 */
public class DGD2SearchEngine extends MTASBasedSearchEngine {
    
    private static final Logger log = Logger
            .getLogger(DGD2SearchEngine.class.getName());
    
    private static final String DONE = "DONE";
    
    /*************************************************************************/
    /*                     SEARCH REPETITIONS                                */
    /*************************************************************************/
    
  
    @Override
    public SearchEngineResponseHitList searchRepetitions(
                                    ArrayList<String> indexPaths, 
                                    String queryString, 
                                    String metadataQueryString,
                                    Integer from, 
                                    Integer to, 
                                    Boolean cutoff, 
                                    IDList metadataIDs, 
                                    ArrayList<Repetition> repetitions, 
                                    HashMap<String, HashSet> synonyms, 
                                    HashMap<String, String[]> wordLists) 
                                    throws SearchServiceException, IOException{
        
        // check queryString to avoid very long queries
        if (queryString.matches("\\[(word|lemma|norm)=\"\\.(\\*|\\+)\"\\]") || 
                queryString.matches("<(word|lemma|norm)/>")){
            throw new SearchServiceException("Your request will take too long. Please constrain the search query, "
                    + "e.g. to a certain lemma: [lemma=\"wissen\"], a certain part of speech: [pos=\"NN\"] or "
                    + "a certain grammatical structure: [pos=\"ART\"][pos=\"ADJA\"][pos=\"NN\"]. "
                    + "You can also constrain the search query by metadata, e.g. <word/> within <e_se_aktivitaet=\"Fahrstunde\"/>");
        }
        
        // check metadata before searching
        if(metadataQueryString!=null && !metadataQueryString.isEmpty()){
            throw new SearchServiceException("Parameter 'metadataQueryString' is not supported yet!");
        }

        GermaNet germanet =  null;
        
        // check search mode before searching   
        for (Repetition repetition: repetitions){
            String repetitionType = repetition.getType().name().toLowerCase();
            SimilarityTypeEnum similarityType = repetition.getSimilarityType();

            switch(similarityType){
                case DIFF_PRON:
                    if(repetitionType.equals(Constants.METADATA_KEY_MATCH_TYPE_WORD)){
                        throw new SearchServiceException("You can't search for repetitions in transcription if they should have different pronunciation!");            
                    }
                    break;
                case DIFF_NORM:
                    if(repetitionType.equals(Constants.ATTRIBUTE_NAME_NORM)){
                        throw new SearchServiceException("You can't search for repetitions in the normalized layer if normalized forms should differ!");            
                    }
                    break;
                case OWN_LEMMA_LIST:
                    if(!repetitionType.equals(Constants.ATTRIBUTE_NAME_LEMMA)){
                        throw new SearchServiceException("Please specify 'LEMMA' as repetition type if you are searching by synonyms!");            
                    }
                    break;
                case GERMANET:
                case GERMANET_HYPERNYM:
                case GERMANET_HYPONYM:
                case GERMANET_ORTH: 
                case GERMANET_PLUS:
                case GERMANET_COMPOUNDS:
                    if(germanet==null){
                        try {
                            String data_path = Configuration.getGermanetPath();
                            germanet = new GermaNet(data_path);
                                                        
                        } catch (XMLStreamException ex) {
                            throw new SearchServiceException("GermaNet could not be loaded!"); 
                        }
                    }
                    break;
                default:
            }
        }
        
        try{
           return searchRepetitions(indexPaths, queryString, from, to, cutoff, metadataIDs, repetitions, synonyms, wordLists, germanet);
        }catch(SeachRepetitionException ex){
            throw new SearchServiceException(ex.getCause().getMessage());                  
        }

    }
    
private SearchEngineResponseHitList searchRepetitions(ArrayList<String> indexPaths, String queryString,
            Integer from, Integer to, Boolean cutoff, IDList metadataIDs, 
            ArrayList<Repetition> repetitions, HashMap<String, HashSet> synonyms, HashMap<String, String[]> wordLists,
            GermaNet germanet) throws SearchServiceException, IOException {
        
        long start = System.currentTimeMillis(); 
        long end = start + TIMEOUT;
           
        IndexReader indexReader = null;
        IndexSearcher searcher = null;
        
        RepetitionSearcher repetitionSearcher = new RepetitionSearcher(indexPaths, from, to, cutoff, repetitions, synonyms, germanet);
        LinkedBlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();
        Consumer consumer = new Consumer(linkedQueue, repetitionSearcher);
        
        for (String indexPath: indexPaths){
            Directory directory;
            directory = FSDirectory.open(Paths.get(indexPath));

            if (directory != null) {

                try{   
                    MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, wordLists, null, null);
                    indexReader = DirectoryReader.open(directory);              
                    searcher = new IndexSearcher(indexReader);
                    SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);
             
                    List<LeafReaderContext> leaves = indexReader.leaves();

                    Thread thread = new Thread(consumer);
                    thread.start();
                    
                    leaves.parallelStream().forEach((LeafReaderContext lrc) -> {                        
               
                            try {
                                Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                                SegmentReader r = (SegmentReader) lrc.reader();    
                                Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);
                                CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);
                                
                                if (spans != null) {
                                    while (spans.nextDoc() != Spans.NO_MORE_DOCS) {      
                                        if (r.numDocs() == r.maxDoc() || r.getLiveDocs().get(spans.docID())) {
                                            String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG);
                                            try{
                                                repetitionSearcher.checkForRepetitions(mtasCodecInfo, r.getSegmentName(), spans, transcriptID, linkedQueue, end);  
                                            }catch (SeachRepetitionException ex){
                                               thread.interrupt();
                                               throw ex;                                               
                                            }
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        
                    });
                    
                    
                    indexReader.close();

                }catch (IndexNotFoundException ex) {
                    throw new IOException ("Search index could not be found! Please check " + indexPath, ex);
                } 
            }
        }
        try {
            linkedQueue.put(DONE);
        } catch (InterruptedException ex) {
            Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return repetitionSearcher.getResult();
    }
        

    private ArrayList<String> getPositionsOfRepetitions(CodecInfo mtasCodecInfo,
            int docID, int start, int end, String speakerID, 
            ArrayList<Repetition> repetitionSpecifications, 
            Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> positionsWithContextLeft,
            Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> positionsWithContextRight,
            String segmentName, HashMap<String, HashSet> synonyms, GermaNet germanet) throws SearchServiceException, IOException {

        String currentRepetitionLayer = repetitionSpecifications.get(0).getType().name().toLowerCase();
        
        List<String> currentPrefixListRepetitionLayer = new ArrayList<String>();
        currentPrefixListRepetitionLayer.add(currentRepetitionLayer);
        
        // get forms from the layer (word, lemma, norm) specified for repetitions                                     
        List<CodecSearchTree.MtasTreeHit<String>> termsFromCurrentRepetitiionLayer = mtasCodecInfo
            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
            docID, currentPrefixListRepetitionLayer, start,
            (end - 1));
        
        if (!termsFromCurrentRepetitiionLayer.isEmpty()){

            // create source object
            RepetitionSource rs = new RepetitionSource();
            rs.addNewLayer(currentRepetitionLayer, termsFromCurrentRepetitiionLayer); 
            int sourceSize = rs.getStringObjectsForLayer(currentRepetitionLayer).size();
            
            // store last position of the source
            int lastPositionOfTheCurrentMatch = end-1;
            
            // look for repetitions
            boolean found=false;
            //ArrayList<SearchEngineMatch> repetitions = new ArrayList();
            ArrayList<String> repetitions = new ArrayList();
            
            int cursorPosition = 0;
            int repetitionSpecificationIndex = 0;
            
            repetitions:
            for(Repetition repetitionSpecification: repetitionSpecifications){
                SimilarityTypeEnum similarity = repetitionSpecification.getSimilarityType();
                double similarityMeasureMin =  repetitionSpecification.getMinSimilarity();
                double similarityMeasureMax =  repetitionSpecification.getMaxSimilarity();
                
                Set ignoredCustomPOS = repetitionSpecification.getIgnoredCustomPOS();
                Boolean ignoreTokenOrder = repetitionSpecification.ignoreTokenOrder();
                
                repetitionSpecificationIndex++;
                
                // if not the first repetition
                if(repetitionSpecificationIndex>1){
                                                           
                    // check if repetitions before were found
                    if(!found){
                        // break because repetitions before were not found
                        break;
                    }else{
                        found=false;
                        //cursorPosition=cursorPosition+sourceSize; // TODO: does not work for "gehst du" und "gehst"
                        
                        currentRepetitionLayer = repetitionSpecification.getType().name().toLowerCase();
                        currentPrefixListRepetitionLayer.clear();
                        currentPrefixListRepetitionLayer.add(currentRepetitionLayer);
                            
                        if(!rs.containsLayer(currentRepetitionLayer)){
                            //create new source string
                            List<CodecSearchTree.MtasTreeHit<String>> termsFromNewRepetitionLayer = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, currentPrefixListRepetitionLayer, start,
                            (end - 1));
                            
                            rs.addNewLayer(currentRepetitionLayer, termsFromNewRepetitionLayer);
                        }
                    }
                }
                
                
                // set maxDistance
                int maxDistance = Constants.MAX_DISTANCE_BETWEEN_REPETITIONS;
                if (repetitionSpecification.getMaxDistance()!=null && repetitionSpecification.getMaxDistance()< maxDistance){
                    maxDistance = repetitionSpecification.getMaxDistance();
                }
                
                if (repetitionSpecification.getMinDistance()!=null){    
                    // set cursorPosition to the minDistance of the specified repetition
                    cursorPosition=cursorPosition + repetitionSpecification.getMinDistance();
                }   
                
                //get the following text
                TreeMap<Integer, String> arrayOfTheFollowingWordTokens = new TreeMap<>();
                arrayOfTheFollowingWordTokens = addTokens(arrayOfTheFollowingWordTokens, mtasCodecInfo, docID, lastPositionOfTheCurrentMatch, 
                    maxDistance + sourceSize, currentPrefixListRepetitionLayer, ignoredCustomPOS);

                // iterate through arrayOfTheFollowingWordTokens/positionsOfTheFollowingWordTokens
                List<Integer> positionsOfTheFollowingWordTokens = new ArrayList<>(arrayOfTheFollowingWordTokens.keySet());
                while(cursorPosition<=(positionsOfTheFollowingWordTokens.size()-sourceSize)){
                                                             
                    Integer firstPosition = positionsOfTheFollowingWordTokens.get(cursorPosition);
                                                            
                    // get n-gram
                    SortedMap<Integer, String> sortedMap = new TreeMap();
                    
                    
               /*     switch(similarity){
                        case EQUAL:
                        case FUZZY:
                        case FUZZY_PLUS:
                        case DIFF_PRON:
                        case DIFF_NORM:
                        case OWN_LEMMA_LIST:
                        case GERMANET:
                        case GERMANET_PLUS:*/
                            if(cursorPosition+sourceSize == positionsOfTheFollowingWordTokens.size()){ 
                                for(int i=1; i<=sourceSize; i++){
                                    int startPos = positionsOfTheFollowingWordTokens.get(positionsOfTheFollowingWordTokens.size()-i);
                                    sortedMap.put(startPos, arrayOfTheFollowingWordTokens.get(startPos));
                                    if(i==1){
                                        // store last position of the current repetition
                                        lastPositionOfTheCurrentMatch =  startPos; 
                                    }
                                }
                            }else {
                                Integer lastPosition = positionsOfTheFollowingWordTokens.get(cursorPosition+sourceSize);
                                sortedMap = arrayOfTheFollowingWordTokens.subMap(firstPosition,lastPosition);

                                // store last position of the current repetition
                                lastPositionOfTheCurrentMatch = lastPosition-1;
                            }
                                                
              //              break;
               //         default:
             //       }
                  
                    // get string of the n-gram
                    String possibleRepetitionStr = String.join(" ", sortedMap.values());
                    
                    // get the appropriate string of the source
                    String sourceStr = rs.getStringForLayer(currentRepetitionLayer);
                    
                    //compare
                    boolean isRepetition = false;
                    
                    switch(similarity){
                        case EQUAL:
                            if(possibleRepetitionStr.equals(sourceStr)){
                               // this can be a repetition
                               isRepetition=true;
                            }else{
                                if(ignoreTokenOrder){
                                    String possibleRepetitionStrSorted = sortedValues(sortedMap.values());
                                    String sourceStrSorted = sortedValues(rs.getStringObjectsForLayer(currentRepetitionLayer).values());
                                    if(possibleRepetitionStrSorted.equals(sourceStrSorted)){
                                        // this can be a repetition
                                        isRepetition=true;
                                    }
                                }
                            }
                            break;
                        
                        case DIFF_PRON:
                        case DIFF_NORM:
                            if(possibleRepetitionStr.equals(sourceStr)){
                                // this can be a repetition
                                List<String> prefixList = getPrefixListForNewLayer(similarity);                              
                                rs.addNewLayer(mtasCodecInfo, docID, prefixList, start, end);
                                isRepetition = checkAdditionalLayer(rs, prefixList, mtasCodecInfo, sortedMap, docID);                      
                            }else{
                                if(ignoreTokenOrder){
                                    String possibleRepetitionStrSorted = sortedValues(sortedMap.values());
                                    String sourceStrSorted = sortedValues(rs.getStringObjectsForLayer(currentRepetitionLayer).values());
                                    if(possibleRepetitionStrSorted.equals(sourceStrSorted)){
                                        // this can be a repetition
                                        List<String> prefixList = getPrefixListForNewLayer(similarity);                              
                                        rs.addNewLayer(mtasCodecInfo, docID, prefixList, start, end);
                                        isRepetition = checkAdditionalLayer(rs, prefixList, mtasCodecInfo, sortedMap, docID);
                                    }
                                }
                            }
                           break;
                        case JACCARD_DISTANCE:
                            double jaccardDist = SimilarityUtilities.getJaccardDistance(sourceStr, possibleRepetitionStr);
                            if (jaccardDist > similarityMeasureMin && jaccardDist < similarityMeasureMax){
                                isRepetition =  true;
                            }
                            break;
                        case JACCARD_DISTANCE_COLOGNE_PHONETIC:
                            double jaccardDistPhon = SimilarityUtilities.getJaccardDistanceOfSoundexValue(sourceStr, possibleRepetitionStr);
                            if (jaccardDistPhon > similarityMeasureMin && jaccardDistPhon < similarityMeasureMax){
                                isRepetition =  true;
                            }
                            break;
                        case JARO_WINKLER_DISTANCE:
                            double jaroWinklerDist = SimilarityUtilities.getJaroWinklerDistance(sourceStr, possibleRepetitionStr);
                            if (jaroWinklerDist > similarityMeasureMin && jaroWinklerDist < similarityMeasureMax){
                                isRepetition =  true;
                            }
                            break;
                        case LEVENSHTEIN_DISTANCE:
                            double levenshteinDist = SimilarityUtilities.getLevenshteinDistance(sourceStr, possibleRepetitionStr);
                            if (levenshteinDist > similarityMeasureMin && levenshteinDist < similarityMeasureMax){
                                isRepetition =  true;
                            }
                            break;
                        case ZUMULT_MIX:
                            ZuMultStringSimilarityMeasure measure = new ZuMultStringSimilarityMeasure();
                            isRepetition = measure.apply(sourceStr, possibleRepetitionStr);
                            break;
                        case FUZZY:
                            if(!possibleRepetitionStr.equals(sourceStr)){                          
                                isRepetition = isFuzzyRepetition(rs.getStringObjectsForLayer(currentRepetitionLayer), sortedMap, ignoreTokenOrder);
                            }
                            break;
                        case FUZZY_PLUS:
                           if(possibleRepetitionStr.equals(sourceStr)){
                               isRepetition=true;
                           }else{
                               isRepetition = isFuzzyRepetition(rs.getStringObjectsForLayer(currentRepetitionLayer), sortedMap, ignoreTokenOrder);
                           }
                           break;
                        case OWN_LEMMA_LIST:
                            isRepetition = checkSynonyms(rs.getStringObjectsForLayer(currentRepetitionLayer), sortedMap, synonyms, ignoreTokenOrder);
                            break;
                        case GERMANET_PLUS:
                            if(possibleRepetitionStr.equals(sourceStr)){
                               isRepetition=true;
                            }else{
                               isRepetition = checkGermaNet(rs.getStringObjectsForLayer(currentRepetitionLayer), sortedMap, germanet, ignoreTokenOrder, false, similarity);
                            }
                            break;
                        case GERMANET: 
                        case GERMANET_ORTH:
                        case GERMANET_HYPERNYM:
                        case GERMANET_HYPONYM:
                        case GERMANET_COMPOUNDS:
                            if(possibleRepetitionStr.equals(sourceStr)){
                               isRepetition=false;
                            }else{
                               isRepetition = checkGermaNet(rs.getStringObjectsForLayer(currentRepetitionLayer), sortedMap, germanet, ignoreTokenOrder, true, similarity);
                            }
                        default:
                    }

                        
                    if(isRepetition && checkConditions(sortedMap, speakerID, segmentName, positionsWithContextLeft, 
                            positionsWithContextRight, firstPosition, mtasCodecInfo, docID, end, repetitionSpecification)){

                        //add matches
                        for(Integer position: sortedMap.keySet()){              
                            repetitions.add(String.valueOf(position));
                        }

                        found = true;
                        break; /* repetition is found, break the search in the word list and 
                        go to next repetition specification*/
                    }

                    cursorPosition++;
                }

            }

            if(found){             
                repetitions.addAll(rs.getPositions());
                return repetitions;                             
            }else{
                return null;
            }
        }else{
            return null;
        }
        
    }
    
    private boolean checkAdditionalLayer(RepetitionSource rs, List<String> prefixList, 
            CodecInfo mtasCodecInfo, SortedMap<Integer, String> sortedMap, int docID) throws IOException{
        String sourcePron = rs.getStringForLayer(prefixList.get(0));   
        String repetitionPron = getNewRepStr(mtasCodecInfo, sortedMap, docID, prefixList);
        return !repetitionPron.equals(sourcePron);
    }
    
    private List<String> getPrefixListForNewLayer(SimilarityTypeEnum similarity){
        List<String> prefixList = new  ArrayList<String>();
        String layer="";
        switch(similarity){
            case DIFF_PRON:                                     
                layer = Constants.METADATA_KEY_MATCH_TYPE_WORD;
                prefixList.add(layer);
                break;
            case DIFF_NORM:
                layer = Constants.ATTRIBUTE_NAME_NORM;
                prefixList.add(layer);
            break;                                   
        }
        return prefixList;
                                
                    
    }
    
    private String getNewRepStr(CodecInfo mtasCodecInfo, 
            SortedMap<Integer, String> sortedMap, int docID, List<String> prefixList) throws IOException{
        //create new repetition string
        StringBuilder sb = new StringBuilder();
        for(Integer position: sortedMap.keySet()){
            List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                docID, prefixList, position, position);
                sb.append(CodecUtil.termValue(terms.get(0).data));
                sb.append(" ");
        }
                                   
        String repetitionPron = sb.toString().trim();
        return repetitionPron;
    }
    
    private String sortedValues(Collection<String> collection){
        ArrayList<String> array = new ArrayList<String>(collection);
        Collections.sort(array);
        return String.join(" ", array);
    }
    
    private Boolean checkSynonyms(SortedMap<Integer, String> source, SortedMap<Integer, String> possibleRepetitions, HashMap<String, HashSet> synonyms, Boolean ignoreTokenOrder){
        
        ArrayList<String> sourceArray = new ArrayList<String>(source.values());
        if(ignoreTokenOrder){
            Collections.sort(sourceArray);
        }
        Object[] sourceList = sourceArray.toArray();
        
        ArrayList<String> possibleRepetitionArray = new ArrayList<String>(possibleRepetitions.values());
        if(ignoreTokenOrder){
            Collections.sort(possibleRepetitionArray);
        }
        Object[] possibleRepetitionList = possibleRepetitionArray.toArray();

        boolean isRepetition=true;
        for(int i=0; i<sourceList.length;i++){
            String s1 = (String) sourceList[i];
            String s2 = (String) possibleRepetitionList[i];
            if(!s1.equals(s2)){
                if(synonyms.containsKey(s1)){
                    if (!synonyms.get(s1).contains(s2)){
                        isRepetition = false;
                        break;
                    }
                }else{
                    isRepetition= false;
                    break;
                }
            }
        }
        return isRepetition;
        /*
        String[] array1 = sourceStr.split(" ");
        String[] array2 = possibleRepetitionStr.split(" ");
        if(array1.length==array2.length){
        try{
        for(int i=0; i< array1.length; i++){ 
            if(!array1[i].equals(array2[i])){
                if(synonyms.containsKey(array1[i])){
                    if (!synonyms.get(array1[i]).contains(array2[i])){
                        return false;
                    }
                }else{
                    return false;
                }
            }
                                    
        }
        
        }catch(ArrayIndexOutOfBoundsException ex){
            for(int i=0; i< array1.length; i++){  
                System.out.println(array1[i]);
            }
            System.out.println("------");
            for(int i=0; i< array1.length; i++){  
                System.out.println(array2[i]);
            }

            Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
            return true;
        }else{
            return false;
        }*/
        
    }
    
    private Boolean checkGermaNet(SortedMap<Integer, String> source, SortedMap<Integer, String> possibleRepetitions, 
            GermaNet germanet, Boolean ignoreTokenOrder, Boolean justGermanet, SimilarityTypeEnum similarity){
        boolean isRepetition=true;
         
        ArrayList<String> sourceArray = new ArrayList<String>(source.values());
        if(ignoreTokenOrder){
            Collections.sort(sourceArray);
        }
        Object[] sourceList = sourceArray.toArray();
        
        ArrayList<String> possibleRepetitionArray = new ArrayList<String>(possibleRepetitions.values());
        if(ignoreTokenOrder){
            Collections.sort(possibleRepetitionArray);
        }
        Object[] possibleRepetitionList = possibleRepetitionArray.toArray();
        
        if(possibleRepetitionList.equals(sourceList)){
            if (justGermanet){
                isRepetition=false;
            }else{
                isRepetition=true;
            }
            
        }else{
            for(int i=0; i<sourceList.length;i++){
                String s1 = (String) sourceList[i];
                String s2 = (String) possibleRepetitionList[i];
                if(!s1.equals(s2)){
                    // get synonyms for s1
                    Set<String> synonyms = getSynonymsFromGermaNet(germanet, s1, similarity);
                    if(!synonyms.contains(s2)){
                        isRepetition = false;
                        break;
                    }
                }
            }
        }
        return isRepetition;

        
    }
    
    private class RepetitionSource {
        private ArrayList<String> layers = new ArrayList();
        private HashMap<String, String> layerMap = new HashMap();
        private HashMap<String, SortedMap<Integer, String>> layerMap2 = new HashMap();
        
        SortedMap<Integer, String> getStringObjectsForLayer(String annotationLayer){
            return layerMap2.get(annotationLayer);
        }
        
        String getStringForLayer(String annotationLayer){
            return layerMap.get(annotationLayer);
        }
        
        boolean containsLayer(String annotationLayer){
            if (layers.contains(annotationLayer)){
                return true;
            }else{
                return false;
            }
        }
        
        void addNewLayer(String currentRepetitionLayer, List<CodecSearchTree.MtasTreeHit<String>> termsFromCurrentRepetitiionLayer){
            SortedMap<Integer, String> source = new TreeMap<>();
            for (CodecSearchTree.MtasTreeHit<String> term : termsFromCurrentRepetitiionLayer) {
                source.put(term.startPosition, CodecUtil.termValue(term.data));
            }
            
            layers.add(currentRepetitionLayer);
            layerMap.put(currentRepetitionLayer, String.join(" ", source.values()));
            layerMap2.put(currentRepetitionLayer, source);
            
        }
        
        void addNewLayer(CodecInfo mtasCodecInfo, int docID, List<String> prefixList, int start, int end) throws IOException{
            if(!containsLayer(prefixList.get(0))){
                //create new source string
                List<CodecSearchTree.MtasTreeHit<String>> termsTrans = mtasCodecInfo
                    .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                        docID, prefixList, start,(end - 1));

                addNewLayer(prefixList.get(0), termsTrans);
           }                              
        }
        
        ArrayList<String> getPositions(){
            ArrayList<String> positions = new ArrayList();
            layerMap2.entrySet().iterator().next().getValue().keySet().forEach(position -> {              
                    positions.add(String.valueOf(position));
                });
            return positions;
        }
        
    }
        
    private boolean isFuzzyRepetition(
                                SortedMap<Integer, String> source, 
                                SortedMap<Integer, String> possibleRepetitions, 
                                Boolean ignoreTokenOrder){
        ArrayList<String> sourceArray = new ArrayList<String>(source.values());
        if(ignoreTokenOrder){
            Collections.sort(sourceArray);
        }
        Object[] sourceList = sourceArray.toArray();
        
        ArrayList<String> possibleRepetitionArray = new ArrayList<String>(possibleRepetitions.values());
        if(ignoreTokenOrder){
            Collections.sort(possibleRepetitionArray);
        }
        Object[] possibleRepetitionList = possibleRepetitionArray.toArray();

        boolean isRepetition=true;
        for(int i=0; i<sourceList.length;i++){
            String s1 = (String) sourceList[i];
            String s2 = (String) possibleRepetitionList[i];
            if(getFuzzyScore(s1, s2)<=15){
                // this can't be a repetition
                isRepetition = false;
                break;
            }
        }
        return isRepetition;
    }
    
    private Boolean checkConditions(SortedMap<Integer, String> sortedMap, String speakerID, 
            String segmentName, Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> positionsWithContextLeft, 
            Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> positionsWithContextRight,
            Integer firstPosition, CodecInfo mtasCodecInfo, int docID, int end, Repetition repetitionSpecification) throws IOException, SearchServiceException{
        Pattern pattern = Pattern.compile("<[^>]+>");
        
        boolean speakerCheck = false;
        boolean overlapCheck = false;
        boolean positionCheck = false;
        boolean contextCheck = false;
                                                            
        // first identify the speaker
        Set<String> speakerSetForRepetition = new HashSet();
        for(Integer position: sortedMap.keySet()){
            List<CodecSearchTree.MtasTreeHit<String>> speakerTermsForRepetition = mtasCodecInfo
            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                docID, prefixListSpeakerXMLId, position, position);
                speakerSetForRepetition.add(CodecUtil.termValue(speakerTermsForRepetition.get(0).data));
        }
                                                                
        if(speakerSetForRepetition.size()==1){
            String speakerIdOfRepetition = speakerSetForRepetition.iterator().next();
            //  System.out.println("speakerIdOfRepetition: " + speakerIdOfRepetition);

            if(repetitionSpecification.isSameSpeaker()==null){ // speaker is not specified
                speakerCheck = true;
                
            }else if (repetitionSpecification.isSameSpeaker()==true &&                              
                speakerIdOfRepetition.equals(speakerID)){  // repetition is realized by the same speaker
                
                 // check the speaker change condition
                if(repetitionSpecification.isSpeakerChangedDesired()!=null){
                    speakerCheck = checkSpeakerChange(mtasCodecInfo, docID, 
                        end, firstPosition-1, prefixListSpeakerXMLId, speakerID,
                        repetitionSpecification.isSpeakerChangedDesired());
                }else{
                    speakerCheck = true;
                }
                
            }else if(repetitionSpecification.isSameSpeaker()==false && 
                !speakerIdOfRepetition.equals(speakerID)){ // repetition is realized by another speaker

                // check speaker metadata
                String metadataQueryString = repetitionSpecification.getSpeakerMetadata();
                if(metadataQueryString!=null && !metadataQueryString.isEmpty()){
                    speakerCheck = checkSpeakerMetadata(mtasCodecInfo, docID, end, firstPosition-1, metadataQueryString, pattern);                                   
                }else{
                    speakerCheck = true;
                }
            }    
                                                          
            if(speakerCheck){
                // check position in relation to speaker change
                if(repetitionSpecification.getPositionSpeakerChangeType()!=null){
                    positionCheck = checkPositionToSpeakerChange(mtasCodecInfo, docID, sortedMap, speakerIdOfRepetition,
                    repetitionSpecification.getPositionSpeakerChangeType(), 
                    repetitionSpecification.getMinPositionSpeakerChange(),  
                    repetitionSpecification.getMaxPositionSpeakerChange(),
                    prefixListSpeakerXMLId);
                }else {
                    positionCheck= true;
                }
            }
        }else{
            // if more than one speaker
            return false;
        }
                        
        if(positionCheck){
            // check overlap condition
            if(repetitionSpecification.getPositionOverlap()!=null){
                overlapCheck = checkPositionToOverlap2(mtasCodecInfo, docID, sortedMap,
                    repetitionSpecification.getPositionOverlap());
            }else{
                overlapCheck=true;
            }
        }
                        
        if(overlapCheck){
            boolean contextCheckLeft = false;
            boolean contextCheckRight = false;
            // check context
            String contextStringLeft = repetitionSpecification.getPrecededby();
            if(contextStringLeft!=null && !contextStringLeft.isEmpty()){
                contextCheckLeft = checkContext(positionsWithContextLeft, addDistanceToLeftContext(repetitionSpecification, contextStringLeft), segmentName, docID, sortedMap.firstKey(), true);
            }else{
                contextCheckLeft=true;
            }
            
            String contextStringRight = repetitionSpecification.getFollowedby();
            if(contextStringRight!=null && !contextStringRight.isEmpty()){
                contextCheckRight = checkContext(positionsWithContextRight, addDistanceToRightContext(repetitionSpecification, contextStringRight), segmentName, docID, sortedMap.lastKey(), false);
            }else{
                contextCheckRight=true;
            }
            
            if (contextCheckLeft && contextCheckRight){
                contextCheck=true;
            }
        }
                        
        if(contextCheck){
            return true;
        }else{
            return false;
        }
    }
    
    private Boolean checkSpeakerMetadata(CodecInfo mtasCodecInfo, int docID, 
            int firstPosition, int endPosition, 
            String metadataQueryString, Pattern pattern) throws IOException{
        
        Matcher matcher = pattern.matcher(metadataQueryString);

        StringBuilder sb = new StringBuilder();
        int from = 0;
        while (matcher.find()) {
            sb.append(metadataQueryString.substring(from, matcher.start()));
            sb.append(String.valueOf(checkMetadata(mtasCodecInfo, docID, firstPosition, endPosition, matcher.group())));
            from = matcher.end();
        }
        sb.append(metadataQueryString.substring(from, metadataQueryString.length()));
        return Boolean.parseBoolean(""+MVEL.eval(sb.toString()));                                
    }
    
    private Boolean checkMetadata(CodecInfo mtasCodecInfo, int docID, int firstPosition, int endPosition, String metadataStr) throws IOException{
        String[] metadata = metadataStr.substring(1, metadataStr.length()-2).split("=", 2);
        List<String> prefixListSpeakerMetadata = new ArrayList();
        prefixListSpeakerMetadata.add(metadata[0]);

        List<CodecSearchTree.MtasTreeHit<String>> speakerTermsForRepetition = mtasCodecInfo
            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
            docID, prefixListSpeakerMetadata, firstPosition, endPosition);

        if(speakerTermsForRepetition.isEmpty()){
            return false;
        }else{
            return metadata[1].substring(1, metadata[1].length()-1).equals(CodecUtil.termValue(speakerTermsForRepetition.get(0).data));
        }
                                    
    }
    
    private Boolean checkContext(Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> positionsWithContext, 
            String context, String segmentName, int docID, int position, boolean precededBy){
        Boolean check = false;
        if(positionsWithContext.containsKey(context)){
            Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>> segmentNames = positionsWithContext.get(context);
            if(segmentNames.containsKey(segmentName)){
                Map<Integer, Set<Map.Entry<Integer, Integer>>> docIDs = segmentNames.get(segmentName);
                if(docIDs.containsKey(docID)){
                    Set<Map.Entry<Integer, Integer>> positions = docIDs.get(docID);
                    for(Map.Entry<Integer, Integer> entry: positions){
                        if (precededBy && entry.getValue()==position){
                            check = true;
                            break;
                        }
                        
                        if (!precededBy && entry.getKey()==position){
                            check = true;
                            break;
                        }
                    }
                }
            }
        }
        return check;
    }
    
    private boolean checkSpeakerChange(CodecInfo mtasCodecInfo, int docID, int firstPosition, int endPosition,
            List<String> prefixListSpeaker, String speakerOfSource,
            Boolean isSpeakerChangedDesired) throws IOException{
        
        boolean speakerChanged = false;
        
        // get all speakers between firstPosition and endPosition
        List<CodecSearchTree.MtasTreeHit<String>> speakerTermsBetween = mtasCodecInfo
                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                docID, prefixListSpeaker, firstPosition, endPosition);

        //now check if there are other speakers besides the speaker of source
        if (speakerTermsBetween.size() > 0){
            for (CodecSearchTree.MtasTreeHit<String> spakerTerm : speakerTermsBetween) {
                if(!CodecUtil.termValue(spakerTerm.data).equals(speakerOfSource)){
                    speakerChanged=true;
                    break;
                }
            }
        }
        return (isSpeakerChangedDesired && speakerChanged) || (!isSpeakerChangedDesired && !speakerChanged);
    }
    
    /* this method is based on <speaker-overlap> */
    private boolean checkPositionToOverlap(
            CodecInfo mtasCodecInfo, int docID, SortedMap<Integer, String> sortedMap,
            PositionOverlapEnum positionOverlap) throws IOException, SearchServiceException{
        boolean overlapCheck = false;
        switch(positionOverlap){
            case WITHIN:
                overlapCheck= true;
                for(Integer position: sortedMap.keySet()){
                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap, position, position);
                    if(terms.isEmpty()){
                        overlapCheck=false;
                        break;
                    }
                }
                break;  
            case NOT_WITHIN:
                overlapCheck= true;
                for(Integer position: sortedMap.keySet()){
                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap, position, position);
                    if(!terms.isEmpty()){
                        overlapCheck=false;
                        break;
                    }
                }
                break;
            case PRECEDEDBY:
                Integer firstPosition = sortedMap.firstKey()-1;
                List<CodecSearchTree.MtasTreeHit<String>> terms1 = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap, firstPosition, firstPosition);
                if(!terms1.isEmpty()){
                    overlapCheck=true;
                }

                break;
            case FOLLOWEDBY:
                Integer lastPosition = sortedMap.lastKey() + 1;
                List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap, lastPosition, lastPosition);
                if(!terms2.isEmpty()){
                    overlapCheck=true;
                }

                break;
            case INTERSECTING:
                boolean positionWithin = false;
                boolean positionOutside = false;
                for(Integer position: sortedMap.keySet()){
                   List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap, position, position);
                    if(terms.isEmpty()){
                        positionOutside=true;
                    }else{
                        positionWithin=true;
                    }
                }
                
                if(positionWithin && positionOutside){
                    overlapCheck= true;
                }
                break;
            default:
                throw new SearchServiceException("Please check the overlap condition. "
                    + "Possible values are 'within', '!within', 'precededby', 'followedby', 'intersecting'");
            }
        return overlapCheck;
    }
    
    /* this method is based on word.type */
    private boolean checkPositionToOverlap2(
            CodecInfo mtasCodecInfo, int docID, SortedMap<Integer, String> sortedMap,
            PositionOverlapEnum positionOverlap) throws IOException, SearchServiceException{
        boolean overlapCheck = false;
        switch(positionOverlap){
            case WITHIN:
                overlapCheck= true;
                for(Integer position: sortedMap.keySet()){
                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap2, position, position);
                    if(terms.isEmpty()){
                        overlapCheck=false;
                        break;
                    }else{
                        if(CodecUtil.termValue(terms.get(0).data).contains("ol-in")){
                           // within overlap 
                        }else{
                            overlapCheck=false;
                            break;
                        }
                     
                    }
                }
                break;  
            case NOT_WITHIN:
                overlapCheck= true;
                for(Integer position: sortedMap.keySet()){
                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap2, position, position);
                    if(!terms.isEmpty()){       
                        if(CodecUtil.termValue(terms.get(0).data).contains("ol-in")){
                           overlapCheck=false;
                           break;
                        }
                    }
                }
                break;
            case PRECEDEDBY:
                Integer firstPosition = sortedMap.firstKey()-1;
                List<CodecSearchTree.MtasTreeHit<String>> terms1 = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap2, firstPosition, firstPosition);
                if(!terms1.isEmpty()){ 
                    if(CodecUtil.termValue(terms1.get(0).data).contains("ol-in")){
                        overlapCheck=true;
                    }
                }

                break;
            case FOLLOWEDBY:
                Integer lastPosition = sortedMap.lastKey() + 1;
                List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap2, lastPosition, lastPosition);
                if(!terms2.isEmpty()){
                    if(CodecUtil.termValue(terms2.get(0).data).contains("ol-in")){
                        overlapCheck=true;
                    }
                }

                break;
            case INTERSECTING:
                boolean positionWithin = false;
                boolean positionOutside = false;
                for(Integer position: sortedMap.keySet()){
                   List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeakerOverlap2, position, position);
                    if(terms.isEmpty()){
                        positionOutside=true;
                    }else{
                        
                        if(CodecUtil.termValue(terms.get(0).data).contains("ol-in")){
                            positionWithin=true;
                        }else{
                            positionOutside=true;
                        }
                        
                    }
                }
                
                if(positionWithin && positionOutside){
                    overlapCheck= true;
                }
                break;
            default:
                throw new SearchServiceException("Please check the overlap condition. "
                    + "Possible values are 'within', '!within', 'precededby', 'followedby', 'intersecting'");
            }
        return overlapCheck;
    }
    
    private boolean checkPositionToSpeakerChange(CodecInfo mtasCodecInfo, int docID, SortedMap<Integer, String> sortedMap,
            String speakerIdOfRepetition, PositionSpeakerChangeEnum positionSpeakerChangeType, 
            int minPositionSpeakerChange, int maxPositionSpeakerChange, List<String> prefixListSpeaker) throws SearchServiceException, IOException{
        boolean test = false;
        int cursor = 0;
        int wordToken = 0;
        
        switch(positionSpeakerChangeType){
            case FOLLOWEDBY:
                Integer lastPosition = sortedMap.lastKey() + 1;
                while(true){
                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeaker, lastPosition, lastPosition);
                    if(!terms.isEmpty() && (!CodecUtil.termValue(terms.get(0).data).equals(speakerIdOfRepetition))){
                        // speaker change
                        if(wordToken<minPositionSpeakerChange){
                            break;
                        }else{
                            test = true;
                            break;
                        }
                    }else{
                        
                        List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, TOKEN_IDS, lastPosition, lastPosition);
                          
                        if(terms2.isEmpty()){
                            // end of document
                            break;
                        }else{
                            if(CodecUtil.termValue(terms2.get(0).data).startsWith("w")){
                              wordToken++;  
                            }
                        }
                        
                        cursor++;
                        lastPosition++;
                    }
                    
                    if(wordToken>maxPositionSpeakerChange || cursor>maxPositionSpeakerChange+20){
                        break;
                    }
                }
 
                break;  
            case PRECEDEDBY:
                Integer firstPosition = sortedMap.firstKey() - 1;

                while(true){
                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, prefixListSpeaker, firstPosition, firstPosition);
                    if(!terms.isEmpty() && (!CodecUtil.termValue(terms.get(0).data).equals(speakerIdOfRepetition))){
                        // speaker change
                        if(wordToken<minPositionSpeakerChange){
                            break;
                        }else{
                            test = true;
                            break;
                        }
                    }else{
                        
                        List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                            docID, TOKEN_IDS, firstPosition, firstPosition);
               
                        if(terms2.isEmpty()){
                            // end of document
                            break;
                        }else{
                            if(CodecUtil.termValue(terms2.get(0).data).startsWith("w")){
                              wordToken++;  
                            }
                        }
                        
                        cursor++;
                        firstPosition--;
                    }
                    
                    if(wordToken>maxPositionSpeakerChange || cursor>maxPositionSpeakerChange+20){
                        break;
                    }
                }
                
            break;
            default:
                throw new SearchServiceException("Please check the specified position in relation to speaker change. "
                   + "Possible values are 'precededby' or 'followedby'");
        }
        return test;
    }
    
    private int isFunctionalWord(CodecInfo mtasCodecInfo, int docID, int currentPosition, Set<String> ignoredCustomPOS) throws IOException{
        
        List<CodecSearchTree.MtasTreeHit<String>> termsPOS = mtasCodecInfo
            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                docID, prefixListPOS, currentPosition,
                currentPosition);

        if(ignoredCustomPOS.contains(CodecUtil.termValue(termsPOS.get(0).data))){
            return 0;
        }else{
            return 1;
        }
    }
    
    private TreeMap<Integer, String> addTokens(TreeMap<Integer, String> wordTokenMap, CodecInfo mtasCodecInfo, int docID, int startPosition,
            int maxNumber, List<String> annotationLayerPrefix, Set<String> ignoredCustomPOS) throws IOException{

        int wordTokenIndex=1;
        TreeMap<Integer, String> map = new TreeMap();
        
        // get max repetition context
        List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                docID, annotationLayerPrefix, (startPosition+1),
                (startPosition+1+Constants.MAX_DISTANCE_BETWEEN_REPETITIONS));
        
        // sort
        terms.forEach(term -> {
            map.put(term.startPosition, CodecUtil.termValue(term.data));
        });
        
        // reduce repetition context according to maxNumber
        for (Map.Entry<Integer,String> entry : map.entrySet()){
            if (wordTokenIndex<=maxNumber){               
                wordTokenMap.put(entry.getKey(), entry.getValue());
                if (ignoredCustomPOS.isEmpty()){
                    wordTokenIndex++;
                }else{
                     // functional words should not be count
                    wordTokenIndex = wordTokenIndex + isFunctionalWord(mtasCodecInfo, docID, entry.getKey(), ignoredCustomPOS);
                }
            }else{
                break;
            }     
        }

        //System.out.println(wordTokenMap);
        return wordTokenMap;
    }
    
    private String addDistanceToLeftContext(Repetition repetition, String context){
        int min = repetition.getMinDistanceToLeftContext();
        int max = repetition.getMaxDistanceToLeftContext();

        if(max>0){
            return context + "[]{" + min + "," + max + "}";
        } else{
            return context;
        }
    }
    
    private String addDistanceToRightContext(Repetition repetition, String context){
        int min = repetition.getMinDistanceToRightContext();
        int max = repetition.getMaxDistanceToRightContext();

        if(max>0){
            return "[]{" + min + "," + max + "}" + context;
        } else{
            return context;
        }
    }
    
    private Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> getPositionsForContext(
            Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> result, 
            ArrayList<String> indexPaths, 
            Boolean within, String context, String queryString) throws SearchServiceException{
        String queryStringWithin = "(" + queryString + ") within <annotationBlock/>";
        
                if(within!=null){
                    if(within){
                        result = addPositions(context, queryStringWithin, indexPaths, result);
                    }else {
                        result = addPositions(context, queryString, indexPaths, result);
                        result = deletePositions(context, queryStringWithin, indexPaths, result);
                    }
                }else {
                    result = addPositions(context, queryString, indexPaths, result);
                }
               return result;
    }
    
    private Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> getPositionsForRightContext(ArrayList<Repetition> repetitions, 
            ArrayList<String> indexPaths) throws SearchServiceException{
        Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> result = new HashMap(); /*{context string: {segment name: {docID: {start and end positions}}}}*/
        for(Repetition repetition: repetitions){
            String  context = repetition.getFollowedby();
            
            if(context!=null && !context.isEmpty()){
                context = addDistanceToRightContext(repetition, context);
                String queryString = "<word/>" + context;
                result = getPositionsForContext(result, indexPaths, repetition.isWithinSpeakerContributionRight(), context, queryString);
            }
        }
        return result;
     }
        
    private Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer,Integer>>>>> getPositionsForLeftContext(ArrayList<Repetition> repetitions, 
            ArrayList<String> indexPaths) throws SearchServiceException{
        
        Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer,Integer>>>>> result = new HashMap(); /*{context string: {segment name: {docID: {start and end positions}}}}*/
        for(Repetition repetition: repetitions){
            String  context = repetition.getPrecededby();

            if(context!=null && !context.isEmpty()){
                context = addDistanceToLeftContext(repetition, context);
                String queryString = context+ "<word/>";
                result = getPositionsForContext(result, indexPaths, repetition.isWithinSpeakerContributionLeft(), context, queryString);
            }       
        }
        return result;
     }
     
     /* this method is just called if the left or right context of the repetition is specified */
    private Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> addPositions(String context, String queryString, ArrayList<String> indexPaths, 
            Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> result)throws SearchServiceException{
        for (String indexPath: indexPaths){
                    Directory directory;
                    try {
                        directory = FSDirectory.open(Paths.get(indexPath));
                        if (directory != null) {
                            try (IndexReader indexReader = DirectoryReader.open(directory)) {
                                IndexSearcher searcher = new IndexSearcher(indexReader);
                                MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null, null);
                                SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);
                                ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();

                                while (iterator.hasNext()) {
                                    LeafReaderContext lrc = iterator.next();
                                    Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                                    SegmentReader r = (SegmentReader) lrc.reader(); 
                                    if (spans != null) {
                                        while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                                            if (r.numDocs() == r.maxDoc() || r.getLiveDocs().get(spans.docID())) {
                                                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                                                    int docID = spans.docID();    
                                                    String segmentName = r.getSegmentName();

                                                    int startPosition = spans.startPosition();
                                                    int endPosition = spans.endPosition()-1;

                                                    Set<Map.Entry<Integer, Integer>> set = new HashSet();
                                                    Map<Integer, Set<Map.Entry<Integer, Integer>>> map = new HashMap();
                                                    Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>> mainMap = new HashMap();

                                                    if(result.containsKey(context)){
                                                        mainMap = result.get(context);
                                                        if(mainMap.containsKey(segmentName)){
                                                            map = mainMap.get(segmentName);
                                                            if(map.containsKey(docID)){
                                                                set = map.get(docID);
                                                            }
                                                        }
                                                    }
                                                    set.add(new AbstractMap.SimpleEntry<>(startPosition,endPosition));
                                                    map.put(docID, set);
                                                    mainMap.put(segmentName, map);
                                                    result.put(context, mainMap);
                                                }
                                            }
                                        }
                                    }
                                }               

                            }catch (IOException ex) {
                                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
        return result;
    }
    
     /* this method is just called if the left or right context of the repetition should not be within the same contribution */
    private Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> deletePositions(String context, String queryString, ArrayList<String> indexPaths, 
            Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> result)throws SearchServiceException{
        
        for (String indexPath: indexPaths){
                    Directory directory;
                    try {
                        directory = FSDirectory.open(Paths.get(indexPath));
                        if (directory != null) {
                            try (IndexReader indexReader = DirectoryReader.open(directory)) {
                                IndexSearcher searcher = new IndexSearcher(indexReader);
                                MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null, null);
                                SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);
                                ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();

                                while (iterator.hasNext()) {
                                    LeafReaderContext lrc = iterator.next();
                                    Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                                    SegmentReader r = (SegmentReader) lrc.reader(); 
                                    if (spans != null) {
                                        while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                                            if (r.numDocs() == r.maxDoc() || r.getLiveDocs().get(spans.docID())) {
                                                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                                                    int docID = spans.docID();    
                                                    String segmentName = r.getSegmentName();
                                                    
                                                    int startPosition =spans.startPosition();
                                                    int endPosition = spans.endPosition()-1;

                                                    if(result.containsKey(context)){
                                                        Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>> mainMap = result.get(context);
                                                        if(mainMap.containsKey(segmentName)){
                                                            Map<Integer, Set<Map.Entry<Integer, Integer>>> map = mainMap.get(segmentName);
                                                            if(map.containsKey(docID)){
                                                                Set<Map.Entry<Integer, Integer>> set = map.get(docID);
                                                                for (Map.Entry<Integer, Integer> entry : set){
                                                                    if(entry.getKey()== startPosition && entry.getValue()==endPosition){
                                                                        set.remove(entry);
                                                                        break;
                                                                    }
                                                                }
                                                                //set.remove(position);
                                                                map.put(docID, set);
                                                                mainMap.put(segmentName, map);
                                                                result.put(context, mainMap);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }               

                            }catch (IOException ex) {
                                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
        return result;
    }
    
    
    private class RepetitionSearcher implements HitReader {
        
        SearchEngineResponseHitList result = new SearchEngineResponseHitList();
        ArrayList<Hit> arrayOfHits = new ArrayList();
        Integer hits_total=0;
        Integer transcripts_total=0;
        ArrayList<String> indexPaths = null;
        Integer from;
        Integer to;
        Boolean cutoff;
        boolean finished = false;        
        int hitNumber = 0;
        ArrayList<Repetition> repetitionSpecifications;
        Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> positionsWithContextLeft;
        Map<String, Map<String, Map<Integer, Set<Map.Entry<Integer, Integer>>>>> positionsWithContextRight;
        HashMap<String, HashSet> synonyms;
        GermaNet germanet;
        
        RepetitionSearcher(ArrayList<String> indexPaths, Integer from, Integer to, 
                Boolean cutoff, ArrayList<Repetition> repetitionSpecifications, HashMap<String, HashSet> synonyms, GermaNet germanet) throws SearchServiceException {
            this.indexPaths = indexPaths;
            this.from = from;
            this.to = to;
            this.cutoff = cutoff;
            this.repetitionSpecifications = repetitionSpecifications;
            this.positionsWithContextLeft = getPositionsForLeftContext(repetitionSpecifications, indexPaths);
            this.positionsWithContextRight = getPositionsForRightContext(repetitionSpecifications, indexPaths);
            this.synonyms = synonyms;
            this.germanet = germanet;
        }
        /**
        *    Reads hits from sorted file
        *
        */
        @Override
        public void readFrom(File tempFile) {
            BufferedReader br = null;
            String nextLine;
            try{
                br=new BufferedReader(new FileReader(new File(tempFile.getAbsolutePath())));
                while((nextLine=br.readLine())!=null){ 
                    hitNumber++;                   
                    if (hitNumber >= from && hitNumber<=to){
                        String[] line = nextLine.split(" ");
                        SearchEngineHit hit = new SearchEngineHit();
                        
                        hit.setDocId(line[0]);
                        hit.setPosition(hitNumber);
                        
                        int docID = Integer.parseInt(line[line.length-2]);
                        String segmentName = line[line.length-1];
                        
                        for (int i=1; i<line.length-2; i++){
                            int position = Integer. parseInt(line[i]);
                            hit.addMatch(getMatchByPosition(segmentName, docID, position));
                        }
                        arrayOfHits.add(hit);
                    }else if (hitNumber > to){
                        break;
                    }
                }
                
                writeResult();
            }catch(IOException ex){
                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }finally {
                if (br!=null){
                    try {
                        br.close();
                    } catch (IOException ex) {
                        Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                tempFile.delete();
            }
        }
        
        /**
        *   Checks if hits have repetitions and write the transcript name, match positions,
        *  segment name and docID into a LinkedBlockingQueue
        * 
        */
        void checkForRepetitions(CodecInfo mtasCodecInfo, String segmentName, Spans spans, String transcriptID, 
                LinkedBlockingQueue<String> linkedQueue, long endtime) throws IOException {
      
            List<Object[]> arrayList = new ArrayList();
            while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) { 
                if(System.currentTimeMillis() > endtime){
                    log.log(Level.INFO, ": {0}", "Server timeout: search thread was stopped");
                    throw new SeachRepetitionException(new Exception("Server timeout: the search process was stopped"));
                }
                arrayList.add( new Object[]{spans.docID(), spans.startPosition(), spans.endPosition()} );
            }
            
            int hits=0;
            boolean containsHits = false; // will be true at the end if there is at least one repetition in this transcript
            
            for (Object[] obj : arrayList){
                    int docID = (int) obj[0];
                    int start = (int) obj[1];
                    int end = (int) obj[2];
                    try {
                        // get speaker IDs
                        List<CodecSearchTree.MtasTreeHit<String>> speakerTerms = mtasCodecInfo
                                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                        docID, prefixListSpeakerXMLId, start, (end - 1));
                        if (speakerTerms.size() > 0){ // because some pauses and incidents does not have a speaker
                                                    
                            // check if matches come from the same speaker
                            Set<String> speakerSet = new HashSet();
                            speakerTerms.forEach(spakerTerm -> {
                                speakerSet.add(CodecUtil.termValue(spakerTerm.data));
                            });

                            if(speakerSet.size()==1){ // matches come from the same speaker

                                String speakerID = speakerSet.iterator().next();

                                ArrayList<String> found=null;

                                try {                               
                                    found = getPositionsOfRepetitions(mtasCodecInfo,
                                            docID, start, end, speakerID, repetitionSpecifications, 
                                            positionsWithContextLeft, positionsWithContextRight, segmentName, synonyms, germanet);
                                }catch (SearchServiceException ex) {
                                    throw new SeachRepetitionException(ex);
                                }                  
                                
                                if(found!=null){
                                                 
                                    containsHits = true;
                                    hits++;
                                    
                                    StringBuilder hitStr = new StringBuilder();
                                    hitStr.append(transcriptID);
                                    hitStr.append(" ");
                                    Collections.sort(found);
                                    hitStr.append(String.join(" ", found));
                                    hitStr.append(" ");
                                    hitStr.append(docID);
                                    hitStr.append(" ");
                                    hitStr.append(segmentName);
                                    linkedQueue.put(hitStr.toString());
                                    
                                }else{
                                    // no repetition => go to next span
                                }
                            }else {
                                // ignore because the all elements of the source should come from the same speaker
                            }
                        }
                    
                    } catch (IOException | InterruptedException ex) {
                        Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                    } 
            }    
        
            if(  containsHits  )  {    
                transcripts_total++;
                hits_total = hits_total+hits;
            }
        }

        synchronized void writeResult(){
            result.setHits(arrayOfHits);
            result.setHitsTotal(hits_total);
            result.setTranscriptsTotal(transcripts_total);
            finished = true;
            notifyAll();
        }
        
        synchronized SearchEngineResponseHitList getResult(){
            while (!finished){
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return result;
        }
        
        private SearchEngineMatch getMatchByPosition(String segmentName, int docID, int position) throws IOException{
            SearchEngineMatch match = null;
            IndexReader indexReader = null;
            search:
            for (String indexPath: indexPaths){
                Directory directory = null;
                directory = FSDirectory.open(Paths.get(indexPath));

                if (directory != null) {

                    try{   
                        indexReader = DirectoryReader.open(directory);
                        ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();

                        while (iterator.hasNext()) {
                            LeafReaderContext lrc = iterator.next();  
                            SegmentReader r = (SegmentReader) lrc.reader(); 
                            if(r.getSegmentName().equals(segmentName)){
                                Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);
                                CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);

                                List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                    .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                        docID, TOKEN_IDS, position, position);
                                if(terms.size()>0){
                                    match = createMatch(terms.get(0));

                                    // set intervals for match
                                    List<CodecSearchTree.MtasTreeHit<String>> intervals = mtasCodecInfo
                                                        .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                                docID, prefixListInterval, position, position);
                                    setIntervals(match, CodecUtil.termValue(intervals.get(0).data));
                                    break search;
                                }
                            } 
                        }
                    }catch (IndexNotFoundException ex) {
                        throw new IOException ("Search index could not be found! Please check " + indexPath, ex);
                    
                    }

                }
            }
            if(indexReader!=null){
                indexReader.close();
            }
            return match;
        }
        
    }
  
    private static class SeachRepetitionException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public SeachRepetitionException(Exception e) {
            super(e);
        }
    }
    
    private class Consumer implements Runnable {

        private BlockingQueue<String> queue;
        private HitReader reader;
        private File tempFile = null;
        private OutputStreamWriter bw = null;

        @Override
        public void run() {
            try {
                while (true) {
                    String take = queue.take();
                    if (DONE.equals(take)){
                        finish();
                        return;
                    }
                    process(take);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void finish(){
            if (bw!=null){
                try {
                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            reader.readFrom(sort(tempFile));
        }
        
        private void process(String take) {
            try {
                bw.write(take + "\n");
            } catch (IOException ex) {
                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public Consumer(BlockingQueue<String> queue, HitReader reader) {
            this.queue = queue;
            this.reader = reader;

            try {
                tempFile = File.createTempFile("tmp", ".txt");
                tempFile.deleteOnExit();
                this.bw = new OutputStreamWriter(new FileOutputStream(tempFile),"UTF-8");
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private File sort(File f){
        TextFileSorter sorter = new TextFileSorter(new SortConfig().withMaxMemoryUsage(20 * 1000 * 1000));
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("tmp", ".txt");
            tmpFile.deleteOnExit();
            try (InputStream input1 = new FileInputStream(f.getAbsolutePath())) {
                sorter.sort(input1, new FileOutputStream(tmpFile.getAbsolutePath()));
                f.delete();
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);    
        } catch (IOException ex) {
            Logger.getLogger(DGD2SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmpFile;
    }
    
    private interface HitReader{
        public void readFrom(File file);
    }
    
    private static int getFuzzyScore(String s1, String s2){
        FuzzyScore o =  new FuzzyScore(new Locale("de"));
        int fs = o.fuzzyScore(s1, s2);
        if (fs==0){
            fs = o.fuzzyScore(s2, s1);
        }
        return fs;
    }
    
    private void setIntervals(SearchEngineMatch match, String str) {
        String [ ] intervals = str
                .split(Constants.TOKEN_INTERVAL_DELIMITER);
        match.setStartInterval(Double.parseDouble(intervals[0]));
        match.setEndInterval(Double.parseDouble(intervals[1])); 
    }
    
    /*************************************************************************/
    /*         Help methods to get data from GermaNet                        */
    /*************************************************************************/   
    
    private Set<String> getSynonymsFromGermaNet(
                                            GermaNet germanet, 
                                            String str, 
                                            SimilarityTypeEnum mode){
        Set<String> result = new HashSet();
        List <Synset> synsets = germanet.getSynsets(str);
            
        for (Synset synset : synsets){
            //add orth forms, compounds info and synonyms
            switch(mode){
               case GERMANET:
               case GERMANET_PLUS:
                    result.addAll(getOrthFormsAndCompoundsForSynset(synset, str));
                    
                    for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hypernym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }
                   
                    for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hyponym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }

                   break;
               case GERMANET_ORTH:
                   result.addAll(getOrthFormsForSynset(synset));
                   break;
               case GERMANET_COMPOUNDS:
                    result.addAll(getCompoundsForSynset(synset, str));
                    break;
               case GERMANET_HYPERNYM:
                   for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hypernym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }
                   break;
               case GERMANET_HYPONYM:
                   for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hyponym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }
                   break;
               default:
            }
            
            if (result.contains(str)){
                result.remove(str);
            }
        }
            
        return result;
    }
    
    private static Set<String> getOrthFormsForSynset(Synset synset){
        Set<String> result = new HashSet();
        List<LexUnit> lexical_units = synset.getLexUnits();

        for (LexUnit lexUnit: lexical_units){
            result.addAll(lexUnit.getOrthForms());
        }
        
        return result;
    }
    
    private static Set<String> getCompoundsForSynset (Synset synset, 
                                                     String word){
        Set<String> result = new HashSet();
        List<LexUnit> lexical_units = synset.getLexUnits();

        for (LexUnit lexUnit: lexical_units){
            List<String> orth_forms = lexUnit.getOrthForms();
            
            if(orth_forms.size()==1 
                    && orth_forms.get(0).equals(word)){
                // add head of compounds
                if(lexUnit.getCompoundInfo()!=null){
                    result.add(lexUnit.getCompoundInfo().getHead());
                }
            }
        }
        
        return result;
    }
        
    private static Set<String> getOrthFormsAndCompoundsForSynset (Synset synset, 
                                                                  String word){
        Set<String> result = new HashSet();
        List<LexUnit> lexical_units = synset.getLexUnits();

        for (LexUnit lexUnit: lexical_units){
            List<String> orth_forms = lexUnit.getOrthForms();
            
            result.addAll(orth_forms);
            
            if(orth_forms.size()==1 
                    && orth_forms.get(0).equals(word)){
                // add head of compounds
                if(lexUnit.getCompoundInfo()!=null){
                    result.add(lexUnit.getCompoundInfo().getHead());
                }
            }
        }
        
        return result;
    }
    
}
