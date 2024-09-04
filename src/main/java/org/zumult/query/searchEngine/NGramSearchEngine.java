/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.std.TextFileSorter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtas.codec.util.CodecInfo;
import mtas.codec.util.CodecSearchTree;
import mtas.codec.util.CodecUtil;
import mtas.search.spans.util.MtasSpanQuery;
import org.apache.commons.io.input.ReversedLinesFileReader;
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
import org.zumult.io.Constants;
import org.zumult.query.Bigram;
import org.zumult.query.Bigram.BigramType;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 */
public class NGramSearchEngine extends QueryCreater {
    private static final String DONE = "DONE";
    private static final String SEPARATOR = "  ";
    private static final String NULL_VALUE = "NULL_VALUE";
    private static final int LIMIT = 4;
    protected static final String TRANSCRIPT_CONTENT_FIELD = SearchIndexFieldEnum.TRANSCRIPT_CONTENT.toString();

    // deafult prefixList
    List<String> prefixList = new ArrayList<> (Arrays.asList(
                                Constants.ATTRIBUTE_NAME_POS, 
                                Constants.ATTRIBUTE_NAME_LEMMA, 
                                Constants.ATTRIBUTE_NAME_NORM));
    
    List<String> elementsThatShouldBeIgnored = 
            new ArrayList<> (Arrays.asList(Constants.ELEMENT_NAME_VOCAL));
    
    public SearchEngineResponseBigrams searchBigrams (
                                ArrayList<String> indexPaths, 
                                String queryString, 
                                String metadataQueryString,
                                Integer from, 
                                Integer to,
                                Integer minFreq,
                                Integer maxFreq,
                                SortTypeEnum sortType, 
                                HashMap<String, String[]> wordLists,
                                List<String> annotationLayerIDs,
                                String within,
                                List<String> elementsInBetween) 
                                throws SearchServiceException, IOException {
        
        if (metadataQueryString!=null && !metadataQueryString.isEmpty()){
            throw new SearchServiceException("Parameter "
                    + "'metadataQueryString' is not supported yet!");   
        }
        
        long start = System.currentTimeMillis(); 

        // create MTAS query
        MtasSpanQuery q = createQuery(TRANSCRIPT_CONTENT_FIELD, 
                                        queryString, 
                                        null, 
                                        null, 
                                        null);
        
        // overwrite the default prefixList with the custom settings
        if(annotationLayerIDs!=null &&  !annotationLayerIDs.isEmpty()) {
            prefixList = annotationLayerIDs;
        }
        
        if (within!=null && (within.equals(Within.CONTRIBUTION.toString())
                || within.equals(Within.SPEAKER.toString()))) {                               
            prefixList.add(within);
        }
        
        // overwrite the default elementsInBetween list with the custom settings
        if(elementsInBetween!=null &&  !elementsInBetween.isEmpty()) {
            this.elementsThatShouldBeIgnored = elementsInBetween;
        }
        
        // initialize lucene index search and reader
        IndexReader indexReader;
        IndexSearcher indexSearcher;
        
        // initialize zumult ngram searcher 
        SearchManager searcher;
        searcher = new SearchManager(from, to, minFreq, maxFreq, sortType);
        
        // this is required for parallel search
        LinkedBlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();
        Consumer consumer = new Consumer(linkedQueue, searcher);
        
        for (String indexPath: indexPaths){

            Directory directory;
            directory = FSDirectory.open(Paths.get(indexPath));

            if (directory != null) {

                try{   
                    indexReader = DirectoryReader.open(directory);              
                    indexSearcher = new IndexSearcher(indexReader);
                    
                    SpanWeight spanweight = ((MtasSpanQuery) q
                            .rewrite(indexReader))
                            .createWeight(indexSearcher, 
                                            ScoreMode.COMPLETE, 
                                            0);
             
                    List<LeafReaderContext> leaves = indexReader.leaves();

                    Thread thread = new Thread(consumer);
                    thread.start();
                    
                    retrieveSearchIndex(leaves, 
                                spanweight, 
                                thread, 
                                new FunktionsWrapper() {
                                    
                        @Override
                        public void search(CodecInfo mtasCodecInfo,
                                           SegmentReader r,
                                           Spans spans) 
                                                        throws IOException{
                           
                        if (within!=null 
                            && (within.equals(Within.CONTRIBUTION.toString())
                            || within.equals(Within.SPEAKER.toString()))) {
                                
                                searcher.check(mtasCodecInfo,
                                           spans,
                                           linkedQueue,
                                           within);
                            } else {
                                searcher.check(mtasCodecInfo,
                                    spans,
                                    linkedQueue);
                            }
                        }
                    });
     
                    indexReader.close();

                }catch (IndexNotFoundException ex) {
                    throw new IOException ("Search index could not be found! "
                            + "Please check " + indexPath, ex);
                } 
            }
        }
        
        try {
            linkedQueue.put(DONE);
        } catch (InterruptedException ex) {
            Logger.getLogger(NGramSearchEngine.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return searcher.getResult();
    }
    
    private void retrieveSearchIndex (List<LeafReaderContext> leaves,
                              SpanWeight spanweight,
                              Thread thread,
                              FunktionsWrapper function){
        
        leaves.parallelStream().forEach((LeafReaderContext lrc) -> {                        
               
            try {
                Spans spans = spanweight.getSpans(lrc, 
                                            SpanWeight.Postings.POSITIONS);
                SegmentReader segmentReader = (SegmentReader) lrc.reader();    
                Terms t = segmentReader.terms(TRANSCRIPT_CONTENT_FIELD);
                CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);
                                
                if (spans != null) {
                    while (spans.nextDoc() != Spans.NO_MORE_DOCS) {      
                        if (segmentReader.numDocs() == segmentReader.maxDoc()
                                || segmentReader
                                        .getLiveDocs()
                                        .get(spans.docID())) {
                          
                   /*     String transcriptID = segmentReader
                            .document(spans.docID())
                            .get(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG);*/
                          
                            try{
                                function.search(mtasCodecInfo,
                                                segmentReader,
                                                spans);
                            }catch (NGramException ex){
                                thread.interrupt();
                                throw ex;                                               
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(NGramSearchEngine.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
                        
        });
    }
    
    
    /**************************************************************/
    /*                       Search Managers                      */
    /**************************************************************/
    
    private interface HitReader {
         
        /**
        *    Reads hits from sorted file
        *
        */
        public void readFrom(File file);
        
    }

    private class SearchManager implements HitReader {
        
        SearchEngineResponseBigrams result = 
                new SearchEngineResponseBigrams();
        
        ArrayList<Bigram> statistics = new ArrayList();
        Integer hits_total=0;
        Integer distinctValues = 0;
        Integer transcripts_total=0;
        Integer from = 0;
        Integer to;
        Integer minFreq = 0;
        Integer maxFreq;
        boolean finished = false;        
        int hitNumber = 0;
        SortTypeEnum sortType;
        
        SearchManager(Integer from,
                      Integer to,
                      Integer minFreq,
                      Integer maxFreq,
                      SortTypeEnum sortType) {
            
            if (from != null && from > 0) {
                this.from = from;
            }
            if (to != null) {
                if(to==0) {
                    to=null;
                } else if (to >= from) {
                    this.to = to;
                } else {
                    to=null;
                }
            }
            
            if (minFreq != null && minFreq > 0) {
                this.minFreq = minFreq;
            }
            if (maxFreq != null && maxFreq > 0) {
                this.maxFreq = maxFreq;
            }
            
            this.sortType = sortType;
        }

        synchronized void writeResult(){
            result.setBigrams(statistics);
            result.setNumberOfDistinctValues(distinctValues);
            result.setHitsTotal(hits_total);
            result.setTranscriptsTotal(transcripts_total);
            finished = true;
            notifyAll();
        }
        
        synchronized SearchEngineResponseBigrams getResult(){
            while (!finished){
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(NGramSearchEngine.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
            return result;
        }
        
        public void check (CodecInfo mtasCodecInfo,
                          Spans spans,
                          LinkedBlockingQueue<String> linkedQueue) 
                                        throws IOException {
      
            int hits_aktuell = 0;
            int distinctValues_aktuell = 0;
            while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) { 
                hits_aktuell++;
                distinctValues_aktuell = distinctValues_aktuell+2;
                int startPosition = spans.startPosition();
                int end = spans.endPosition();
                int inbetween = end-startPosition;
                  
                StringBuilder queryMatch = new StringBuilder();

                for (int i=0; i<inbetween; i++){
                    
                    Map<String, String> map 
                            = getTokenMap(spans.startPosition()+i,
                                                 mtasCodecInfo,
                                                 spans,
                                                 prefixList);
                    String str = map.toString();
                    queryMatch.append(str);
                }

                // get left bigram
                Map<String, String> tokenLeft 
                        = getTokenMapBefore(startPosition-1,
                                             mtasCodecInfo,
                                             spans);
     
                String bigramLeft = getBigramString(tokenLeft.toString(), 
                                              queryMatch.toString(),
                                              NULL_VALUE);
                // get right bigram
                Map<String, String> tokenRight
                        = getTokenMapAfter(spans.endPosition(),
                                mtasCodecInfo,
                                spans);

                String bigramRight = getBigramString(NULL_VALUE, 
                                              queryMatch.toString(),
                                              tokenRight.toString());
                
                try {
                    linkedQueue.put(bigramLeft);
                    linkedQueue.put(bigramRight);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NGramSearchEngine.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                                                                                        
            }   
                                        
           if (hits_aktuell > 0 ){
                transcripts_total++;
                hits_total = hits_total + hits_aktuell;
                distinctValues = distinctValues + distinctValues_aktuell;
           }
            
        }
        
        public void check (CodecInfo mtasCodecInfo,
                          Spans spans,
                          LinkedBlockingQueue<String> linkedQueue,
                          final String specialID) 
                                        throws IOException {
      
            int hits_aktuell = 0;
            int distinctValues_aktuell = 0;
            spans:
            while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) { 
                
                int startPosition = spans.startPosition();
                int end = spans.endPosition();
                int inbetween = end-startPosition;
                String withinID = null;
                StringBuilder queryMatch = new StringBuilder();

                for (int i=0; i<inbetween; i++){
                    
                    Map<String, String> map 
                            = getTokenMap(spans.startPosition()+i,
                                                 mtasCodecInfo,
                                                 spans,
                                                 prefixList);
                    String special = map.get(specialID);
                    if (withinID!=null && !withinID.equals(special)) {
                        // this hit does not suit
                        continue spans;
                    } else { 
                        hits_aktuell++;
                        distinctValues_aktuell = distinctValues_aktuell + 2;
                        withinID = special;
                    }
                    
                    map.remove(specialID);
                    
                    String str = map.toString();
                    queryMatch.append(str);
                }

                // get left bigram
                Map<String, String> tokenLeft 
                        = getTokenMapBefore(startPosition-1,
                                             mtasCodecInfo,
                                             spans,
                                             specialID,
                                             withinID);
                
                String bigramLeft = getBigramString(tokenLeft.toString(), 
                                              queryMatch.toString(),
                                              NULL_VALUE);
                // get right bigram
                Map<String, String> tokenRight
                        = getTokenMapAfter(spans.endPosition(),
                                mtasCodecInfo,
                                spans,
                                specialID,
                                withinID);

                String bigramRight = getBigramString(NULL_VALUE, 
                                              queryMatch.toString(),
                                              tokenRight.toString());
                
                try {
                    linkedQueue.put(bigramLeft);
                    linkedQueue.put(bigramRight);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NGramSearchEngine.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                                                                                        
            }   
                                        
           if (hits_aktuell > 0 ){
                transcripts_total++;
                hits_total = hits_total+hits_aktuell;
                distinctValues = distinctValues + distinctValues_aktuell;
           }
            
        }

        
        @Override
        public void readFrom(File tempFile) {
            
            if (sortType.name().endsWith("DESC")){
                ReversedLinesFileReader br = null;
                String nextLine;
                try{
                    br = new ReversedLinesFileReader(tempFile, 
                                            StandardCharsets.UTF_8);

                    while((nextLine=br.readLine())!=null){
                        String[] values = nextLine
                                    .split(SEPARATOR, LIMIT);
                        int abs = Integer.parseInt(values[0]);
                        if (abs >= minFreq 
                            && (maxFreq==null || abs <= maxFreq)){     
                            
                            hitNumber++;                   
                            if (hitNumber >= from
                                && (to==null || hitNumber<=to)){
                                
                                statistics.add(getBigram(values, hitNumber));
                            }else {
                                if (to!=null && hitNumber > to){
                                    break;
                                }
                            }
                        }
                    }
                        
                    writeResult();
                }catch(IOException ex){
                    Logger.getLogger(NGramSearchEngine.class.getName())
                                .log(Level.SEVERE, null, ex);
                }finally {
                    if(br!=null){
                        try {
                            br.close();
                        } catch (IOException ex) {
                            Logger.getLogger(NGramSearchEngine.class.getName())
                                    .log(Level.SEVERE, null, ex);
                            }
                        }
                    }
            } else {
            
                BufferedReader br = null;
                String nextLine;
                try{
                    br=new BufferedReader(new FileReader(tempFile, 
                                            StandardCharsets.UTF_8));
                    
                    while((nextLine=br.readLine())!=null){ 
                        
                        String[] values = nextLine
                                .split(SEPARATOR, LIMIT);
                        int abs = Integer.parseInt(values[0]);
                        if (abs >= minFreq
                            && (maxFreq==null || abs <= maxFreq)){
                            
                            hitNumber++;    
                            if (hitNumber >= from
                                && (to==null || hitNumber<=to)){
                                
                                statistics.add(getBigram(values, hitNumber));
                            }else if (to!=null && hitNumber > to){
                                break;
                            }    
                        }
                    }

                    writeResult();
                }catch(IOException ex){
                    Logger.getLogger(NGramSearchEngine.class.getName())
                            .log(Level.SEVERE, null, ex);
                }finally {
                    if (br!=null){
                        try {
                            br.close();
                        } catch (IOException ex) {
                            Logger.getLogger(NGramSearchEngine.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    }
                    tempFile.delete();
                }
            }
        }
    }
      
    /**************************************************************/
    /*                        Consumer                            */
    /**************************************************************/
    
    private class Consumer implements Runnable {

        private BlockingQueue<String> queue;
        private HitReader reader;
        private File tempFile = null;
       // private OutputStreamWriter bw = null;
        private BufferedWriter bw = null;


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
                    Logger.getLogger(NGramSearchEngine.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
            reader.readFrom(sort(group(sort(tempFile))));
        }
        
        private void process(String take) {
            try {
                bw.write(take + "\n");
            } catch (IOException ex) {
                Logger.getLogger(NGramSearchEngine.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }

        public Consumer(BlockingQueue<String> queue, HitReader reader) {
            this.queue = queue;
            this.reader = reader;

            try {
                tempFile = File.createTempFile("tmp", ".txt");
                //System.out.println(tempFile.getAbsoluteFile());
                tempFile.deleteOnExit();
                bw = new BufferedWriter(
                            new OutputStreamWriter(
                               new FileOutputStream(tempFile, true), 
                                     StandardCharsets.UTF_8));
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(NGramSearchEngine.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(NGramSearchEngine.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(NGramSearchEngine.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    
        
    /**************************************************************/
    /*                          Help methods                      */
    /**************************************************************/
    
    private File sort(File f){
        
        TextFileSorter sorter = new TextFileSorter(new SortConfig()
                .withMaxMemoryUsage(20 * 1000 * 1000));
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("tmp", ".txt");
            //System.out.println(tmpFile.getAbsoluteFile());
            tmpFile.deleteOnExit();
            try (InputStream input =
                    new FileInputStream(f.getAbsolutePath())) {
                
                sorter.sort(input, 
                        new FileOutputStream(tmpFile.getAbsolutePath()));
                input.close();
                f.delete();
                
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NGramSearchEngine.class.getName())
                    .log(Level.SEVERE, null, ex);    
        } catch (IOException ex) {
            Logger.getLogger(NGramSearchEngine.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return tmpFile;
    }
    
    private File group(File f) {
        BufferedWriter bw2 = null;
        BufferedReader br = null;
        File tmpFile = null;
        
        try{
            tmpFile = File.createTempFile("tmp", ".txt");
            //System.out.println(tmpFile.getAbsoluteFile());
            tmpFile.deleteOnExit();
            bw2 = new BufferedWriter(
                    new FileWriter(tmpFile, true)); 
            br = new BufferedReader(
                    new FileReader(
                        new File(f.getAbsolutePath())));

            String firstLine = br.readLine();
            String nextLine = null;
            int index1 = 1;
      
            while((nextLine=br.readLine())!=null){  
                if(nextLine.equals(firstLine)){
                    index1++;
                }else{
                    bw2.write(addIndex (firstLine, index1));
                    bw2.newLine();
                    firstLine = nextLine;
                                index1=1;
                }  
            }
            bw2.write(addIndex (firstLine, index1));
                    
        }catch(IOException ex){
            Logger.getLogger(NGramSearchEngine.class.getName())
                    .log(Level.SEVERE, null, ex);
        }finally {
            if (br!=null){
                try {
                    br.close();
                    f.delete();
                } catch (IOException ex) {
                    Logger.getLogger(NGramSearchEngine.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
            if(bw2!=null){
                try {
                    bw2.close();
                } catch (IOException ex) {
                    Logger.getLogger(NGramSearchEngine.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
        return tmpFile;
    }
    

    private Map<String, String> getTokenMap(int position,
                                CodecInfo mtasCodecInfo,
                                Spans spans,
                                List<String> prefixList) throws IOException{
        
        List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                        .getPositionedTermsByPrefixesAndPosition(TRANSCRIPT_CONTENT_FIELD,
                          spans.docID(), 
                          prefixList,
                          position);
                
        Map<String, String> map = new HashMap<>();
        for (CodecSearchTree.MtasTreeHit<String> term : terms) {       
            String prefix = CodecUtil.termPrefix(term.data);
            String value = CodecUtil.termValue(term.data);
            map.put(prefix, value);                      
        }
        return map;
    
    }
    
    private Map<String, String> getTokenMapBefore(int position,
                                CodecInfo mtasCodecInfo,
                                Spans spans) throws IOException{
        
        Map<String, String> map = getTokenMap(position,
                                mtasCodecInfo,
                                spans,
                                prefixList);
        
        if(map.isEmpty()){
             Map<String, String> specialElement =
                    getTokenMap(position,
                                mtasCodecInfo,
                                spans,
                                elementsThatShouldBeIgnored);

            if(!specialElement.isEmpty()){
                position = position-1;
                return getTokenMapBefore(position,
                                mtasCodecInfo,
                                spans);
            }
        } 
        
        return map;
    }
    
    
    private Map<String, String> getTokenMapAfter(int position,
                                CodecInfo mtasCodecInfo,
                                Spans spans) throws IOException{
        
        Map<String, String> map = getTokenMap(position,
                                mtasCodecInfo,
                                spans,
                                prefixList);
        
        if(map.isEmpty()){
            Map<String, String> specialElement =
                    getTokenMap(position,
                                mtasCodecInfo,
                                spans,
                                elementsThatShouldBeIgnored);
            if(!specialElement.isEmpty()){
                position = position+1;
                return getTokenMapAfter(position,
                                mtasCodecInfo,
                                spans);
            }
        } 
        
        return map;
    }
   
    
    private Map<String, String> getTokenMapBefore(int position,
                        CodecInfo mtasCodecInfo,
                        Spans spans,
                        String id,
                        String expectedValue) throws IOException {
        
        Map<String, String> map = getTokenMap(position,
                                              mtasCodecInfo,
                                              spans,
                                              prefixList);
        
        if(map.isEmpty()){
            Map<String, String> specialElement =
                    getTokenMap(position,
                                mtasCodecInfo,
                                spans,
                                elementsThatShouldBeIgnored);
            if(!specialElement.isEmpty()){
                position = position-1;
                return getTokenMapAfter(position,
                                mtasCodecInfo,
                                spans);
            }
        } else {
            String special = map.get(id);
        
            if (expectedValue.equals(special)) {
                map.remove(id);
                return map;
            }
        }
        
        return new HashMap<>();

    }
    
    private Map<String, String> getTokenMapAfter(int position,
                        CodecInfo mtasCodecInfo,
                        Spans spans,
                        String id,
                        String expectedValue) throws IOException {
        
        Map<String, String> map = getTokenMap(position,
                                              mtasCodecInfo,
                                              spans,
                                              prefixList);
        
        if(map.isEmpty()){
            Map<String, String> specialElement =
                    getTokenMap(position,
                                mtasCodecInfo,
                                spans,
                                elementsThatShouldBeIgnored);
            if(!specialElement.isEmpty()){
                position = position+1;
                return getTokenMapAfter(position,
                                mtasCodecInfo,
                                spans);
            }
        } else {
            String special = map.get(id);
        
            if (expectedValue.equals(special)) {
                map.remove(id);
                return map;
            }
        } 
        
        return new HashMap<>();

    }

    private SearchEngineBigram getBigram (String[] values, int row) {
        if (values[1].equals(NULL_VALUE)){
            return new SearchEngineBigram(values[2], values[3],
                            Integer.parseInt(values[0]), BigramType.RIGHT, row);
        } else {
            return new SearchEngineBigram(values[2], values[1], 
                            Integer.parseInt(values[0]), BigramType.LEFT, row);
        }
    }
    
    private String addIndex (String str, int index) {
        String temp = "0000000000";
        return temp.substring(0, temp.length()
                - String.valueOf(index).length()) 
                + index 
                + SEPARATOR + str;
    }
    
    private String getBigramString(String before, String match, String after){
        StringBuilder sb = new StringBuilder();
        sb.append(before)
           .append (SEPARATOR)
           .append(match)
           .append (SEPARATOR)
           .append(after);
        return sb.toString();
    }
    
    /**************************************************************/
    /*              FunktionsWrapper methods                      */
    /**************************************************************/
    
    private interface FunktionsWrapper {
        public void search (CodecInfo mtasCodecInfo, 
                            SegmentReader segmentReader,
                            Spans spans ) throws IOException;
    }

    /**************************************************************/
    /*              NGram Explorer Exception                    */
    /**************************************************************/
    private static class NGramException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public NGramException(Exception e) {
            super(e);
        }
    }
    
    /**************************************************************/
    /*              Within class                    */
    /**************************************************************/     
    public enum Within {
         TRANS, 
         SPEAKER,
         CONTRIBUTION;
         
        @Override
        public String toString() {
             return switch (this) {
                 case SPEAKER -> Constants
                     .METADATA_KEY_MATCH_TYPE_ANNOTATION_BLOCK_SPEAKER;
                 case CONTRIBUTION -> Constants
                     .METADATA_KEY_MATCH_TYPE_ANNOTATION_BLOCK_ID;
                 default -> null;
             };
        }
    }
}