/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.std.TextFileSorter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.input.ReversedLinesFileReader;
import mtas.codec.util.CodecInfo;
import mtas.codec.util.CodecSearchTree;
import mtas.codec.util.CodecUtil;
import mtas.parser.cql.MtasCQLParser;
import mtas.parser.cql.ParseException;
import mtas.parser.cql.TokenMgrError;
import mtas.search.spans.util.MtasSpanQuery;
import mtas.analysis.token.MtasTokenString;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.store.FSDirectory;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;
import org.zumult.query.SearchServiceException;
import org.zumult.io.IOHelper;
import org.zumult.objects.implementations.ISOTEITranscript;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.zumult.io.Constants;
import org.zumult.query.Hit;
import org.zumult.query.StatisticEntry;
import org.zumult.query.searchEngine.Repetition.PositionOverlapEnum;
import org.zumult.query.searchEngine.Repetition.PositionSpeakerChangeEnum;
import org.zumult.query.searchEngine.Repetition.SimilarityTypeEnum;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.text.similarity.FuzzyScore;
import org.mvel2.MVEL;

/**
 * MTAS-based search engine for indexing and querying the contents of the XML-based ISO/TEI transcripts of spoken language. 
 * 
 * @author Elena Frick
 * @version 1.0
 * 
 */
public class MTASBasedSearchEngine implements SearchEngineInterface {
    
    private static final Logger log = Logger.getLogger(MTASBasedSearchEngine.class.getName());

    private IndexWriter writer = null;
    private IndexWriterConfig config;
    
    private static final String FIELD_TRANSCRIPT_ID_FROM_FILE_NAME = "fileName";
    private static final String FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG = Constants.METADATA_KEY_TRANSCRIPT_DGD_ID; //t_dgd_kennung
    private static final String FIELD_TRANSCRIPT_CONTENT = "content";
    private static final String FIELD_TRANSCRIPT_TOKEN_TOTAL = "tokenTotal";  // number of word tokens in transcript

    private static final String XML_FILE_FORMAT = ".XML";
    
    private static final String METADATA_NO_VALUE = "Annotation tag without value";
    private static final String METADATA_VALUE_NOT_AVAILABLE = "not available";
    
    private static final String METADATA_KEY_MATCH_TYPE_PHON_HTML ="phon_html";
    
    /**
     * Additional metadata key ids that can be searched in this search engine.
     */
    public static final String METADATA_KEY_HIT_LENGTH ="tokenSize";
    public static final String METADATA_KEY_HIT_LENGTH_IN_WORD_TOKENS = "wordTokenSize";
    public static final String METADATA_KEY_MATCH_TRANSCRIPTION ="transcription";
    
    private static final String METADATA_QUERY_DELIMITER = "&";
    
    private static final List<String> TOKEN_IDS = new  ArrayList<String>(){{Arrays.asList(Constants.TOKENS).forEach(str -> {
        if (str.equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
            str = Constants.METADATA_KEY_MATCH_TYPE_WORD;
        }
        add(str + ".id");
    });}};
    
    // token annotation layers specified in ZuMultAvailableAnnotationValues.xml
    // TODO: maybe this can be extracted from ZuMultAvailableAnnotationValues.xml
    public static final String[] BASIC_TOKEN_ANNOTATION_LAYERS = 
    {
        Constants.ATTRIBUTE_NAME_POS, Constants.ATTRIBUTE_NAME_NORM, 
        Constants.ATTRIBUTE_NAME_LEMMA, Constants.METADATA_KEY_MATCH_TYPE_WORD_TYPE,
        Constants.ATTRIBUTE_NAME_PHON
    };
    
    private static final int MAX_REPETITION_CONTEXT = 30;
    
    private static final String[] NOT_COUNT =
        {     
            "NGHES" /* äh, ähm, hä*/, "UI", "AB", "XY"
        };
    
    List<String> prefixListInterval = new  ArrayList<String>(){
        { add(Constants.TOKEN_INTERVAL);}
    };
    
    List<String> prefixListSpeakerId = new ArrayList<String>(){
        { add(Constants.METADATA_KEY_SPEAKER_DGD_ID);}
    };
    
    List<String> prefixListSpeakerXMLId = new ArrayList<String>(){
        { add(Constants.METADATA_KEY_MATCH_TYPE_ANNOTATION_BLOCK_SPEAKER);}
    };
    
    List<String> prefixListPOS = new  ArrayList<String>(){
        { add(Constants.ATTRIBUTE_NAME_POS);}
    };
    
    List<String> prefixListTRANS = new  ArrayList<String>(){
        { add(Constants.METADATA_KEY_MATCH_TYPE_WORD);}
    };
    
    List<String> prefixListSpeakerOverlap = new ArrayList<String>(){
        { add("speaker-overlap");}
    };
    
    private static final String DONE = "DONE";
    

    /**
     * Creates a search index.
     * 
     * @param inputDirectories  the set of absolute directories containing ISO-TEI transcripts to be indexed
     * @param indexPath  the absolute path to the search index to be created. 
     * 
     * <p>
     * <b>Note:</b>
     * If the output folder already exists, its content will be deleted, otherwise it will be created.
     * <p>
     * 
     * @param mtasConfigFile  the relative path to the MTAS configuration file to be used for indexing.
     *                          
     * <p>
     * <b>Note:</b>
     * More detailed information about MTAS configuration files can be found online at 
     *  <a href="https://textexploration.github.io/mtas/indexing_configuration.html">
     *  https://textexploration.github.io/mtas/indexing_configuration.html</a>.
     * <p>
     * 
     * @return the {@code SearchIndex} object
     * @throws java.io.IOException if search index cann't be created
     *  
     * @see org.zumult.query.searchEngine.parser.config
     * @see org.zumult.indexing.search.SearchIndexer
     * @see org.zumult.indexing.search.SearchIndexerAll
     * 
     * 
    */    
    public SearchIndex createIndex(Set<Path> inputDirectories, String indexPath, String mtasConfigFile) throws IOException {
        log.log(Level.INFO, "-------------- Creating index -------------");
        log.log(Level.INFO, "Index input path: {0}", inputDirectories);
        log.log(Level.INFO, "Index output path: {0}", indexPath);
        log.log(Level.INFO, "Configuration file: {0}", mtasConfigFile);
        int counter = 0;
        if (indexPath == null || indexPath.isEmpty()) {
            throw new IllegalArgumentException("You have not specified the output directory of the search index. ");      
        }else{
            Path outputPath = Paths.get(indexPath);
                
            // empty output directory if already exists
            if (Files.isDirectory(outputPath)) {
                IOHelper.emptyDir(outputPath.toFile());
            }
                
            // initialize directory for creating an index
            Directory directory;
            try {
                directory = FSDirectory.open(outputPath);
            } catch (IOException ex) {                   
                throw new IOException ("Unable to initialize the directory for creating the search index. "
                        + "Please check the directory: " + indexPath, ex);
            }

            // initialize IndexWriterConfig that holds all the configuration used to create an IndexWriter
            Map<String, String> paramsCharFilterMtas = new HashMap<>();
            paramsCharFilterMtas.put("type", "file");

            Map<String, String> paramsTokenizer = new HashMap<>();
            String configPath = new File(mtasConfigFile).getAbsolutePath();
            paramsTokenizer.put("configFile", configPath);

            Analyzer mtasAnalyzer;
            try {
                mtasAnalyzer = CustomAnalyzer
                            .builder(Paths.get("").toAbsolutePath())
                            .addCharFilter("mtas", paramsCharFilterMtas)
                            .withTokenizer("mtas", paramsTokenizer).build();
            } catch (IOException ex) {
                throw new IOException("mtasAnalyzer could not be initialized. ", ex);
            }

            Map<String, Analyzer> analyzerPerField = new HashMap<>();
            analyzerPerField.put(FIELD_TRANSCRIPT_CONTENT, mtasAnalyzer);

            PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);

            config = new IndexWriterConfig(analyzer);
            config.setUseCompoundFile(false);
            config.setCodec(Codec.forName("MtasCodec"));

            try {
                // initialize a new IndexWriter per the settings given in config
                writer = new IndexWriter(directory, config);
                    
                // delete all documents in the index directory
                writer.deleteAll();
                
            } catch (IOException ex) {
                throw new IOException("IndexWriter could not be initialized.", ex);
            }

                
            if (inputDirectories==null || inputDirectories.isEmpty()){
                throw new IllegalArgumentException("You have not specified the input directory for creating the search index.");
            }else{               
                for (Path path : inputDirectories){

                    // parse input directories for files to be added to the IndexWriter
                    File f = path.toFile();
                    if(!f.exists()){
                        throw new IllegalArgumentException("Please check the input directory: " + f.getAbsolutePath());
                    }else{
                        counter = counter + parseDirectory(f, new FilesFilter());
                    }
                }
            }
                
            // commit and close IndexWriter
            writer.commit();
            writer.close();
                
            // create search index object
            SearchIndex searchIndex = new SearchIndex(indexPath);
            searchIndex.setNumberOfIndexedDocuments(counter);
                
            log.log(Level.INFO, "Search index created: {0}", indexPath);
                
            return searchIndex;

        }
    } 

    
    private int parseDirectory (File inputDir, FileFilter filter) throws IOException {
        int counter = 0;
        File[] files = inputDir.listFiles();
        if (files != null){
            for (File file : files) {
                if (file.isDirectory()){
                    counter = counter + parseDirectory(file, filter);
                }else{
                    if (file.exists() && file.canRead()){
                        log.log(Level.INFO, "Indexing {0}", file.getPath());
                        indexFile(file);
                        counter++;
                    }
                }
            }
        }
        
        return counter;
    }
    
    private void indexFile(File f) throws IOException {
        try {
            Document doc = getDocument(f);
            writer.addDocument(doc);
        } catch (IOException ex) {
            throw new IOException("Unable to index the file " + f.getAbsolutePath(), ex);
        }
    }
    
    private Document getDocument(File f) throws IOException {
        
        Document doc = new Document();

        Transcript transcript = null;
        try {
            transcript = new ISOTEITranscript(IOHelper.readDocument(f));
        } catch (SAXException | ParserConfigurationException | IOException ex) {
            throw new IOException("Unable to parse the file " + f.getAbsolutePath(), ex);
        }
        String transcriptID = transcript.getID();
        String filename = f.getName().substring(0, f.getName().length() - XML_FILE_FORMAT.length());         
            
        doc.add(new StringField(FIELD_TRANSCRIPT_ID_FROM_FILE_NAME, filename, Field.Store.YES)); // is not used for searching
        doc.add(new StringField(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG, transcriptID, Field.Store.YES));
        doc.add(new TextField(FIELD_TRANSCRIPT_CONTENT, f.getAbsolutePath(), Field.Store.YES));
        doc.add(new TextField(FIELD_TRANSCRIPT_TOKEN_TOTAL, Integer.toString(transcript.getNumberOfTokens()), Field.Store.YES));
        
        return doc;
    }
    
    private String getDistinctValues(String queryString, String transcript, String metadataKeyID, ArrayList<String> indexPaths, 
            Integer from, Integer to, SortTypeEnum sortType, String typeName) throws SearchServiceException, IOException{
        String newQueryString = "(" + queryString + ") within <"+ metadataKeyID + "=\""+ transcript + "\"/>";
        SearchEngineResponseStatistics statistics = searchStatistics(indexPaths, newQueryString, from, to, sortType, typeName);
        return String.valueOf(statistics.getNumberOfDistinctValues());
    }

    private String getRelativeValue(ArrayList<String> indexPaths, String metadataValue, int hits, String metadataKeyID) throws SearchServiceException, IOException{
        String qs = "<word/> within <" + metadataKeyID + "=\"" + metadataValue +"\"/>";
        SearchEngineResponse sr = search(indexPaths, qs, null);
        int token_total = sr.getHitsTotal();                    
        double tokenTotal = Double.parseDouble(String.valueOf(token_total));
        Double rel = hits*100/ tokenTotal;
        return String.valueOf(rel);                
    }
    
    private static class FilesFilter implements FileFilter {
        @Override
        public boolean accept(File path) {
            return path.getName().toUpperCase().endsWith(XML_FILE_FORMAT);
        }
    }
        
    private MtasSpanQuery createQuery(String field, String queryString, MtasSpanQuery ignore, Integer maximumIgnoreLength) throws SearchServiceException {
        try{
            System.out.println("PARAMETER (CQP QUERY): " + queryString);
            Reader reader = new BufferedReader(new StringReader(queryString));
            MtasCQLParser p = new MtasCQLParser(reader);
            return p.parse(field, null, null, ignore, maximumIgnoreLength);
        }catch (TokenMgrError | ParseException | IllegalArgumentException ex) {
            throw new SearchServiceException("Please check the query syntax: " + ex.getMessage());
        }        
    }

    private void sortByAbsoluteValue( ArrayList<Map.Entry<SearchEngineStatisticEntry, Double>> array){
        Collections.sort(array, new Comparator<Map.Entry<SearchEngineStatisticEntry, Double>>() {
                
            @Override
            public int compare(Map.Entry<SearchEngineStatisticEntry, Double> o1, Map.Entry<SearchEngineStatisticEntry, Double> o2) {
                    return o2.getKey().compareTo(o1.getKey());
            }
        });
    }
    
    private void sortByRelativeValue( ArrayList<Map.Entry<SearchEngineStatisticEntry, Double>> array){
        Collections.sort(array, new Comparator<Map.Entry<SearchEngineStatisticEntry, Double>>() {
            
            @Override
            public int compare(Map.Entry<SearchEngineStatisticEntry, Double> o1, Map.Entry<SearchEngineStatisticEntry, Double> o2) {                          
                return o2.getValue().compareTo(o1.getValue());
            }
        });
    
    }
 
    private  void addStatisticEntry(ArrayList<Map.Entry<SearchEngineStatisticEntry, Double>> array, String transcriptID, int hits, double rel, SortTypeEnum sortType){
        
        //relative descending
        if (sortType.equals(SortTypeEnum.REL_DESC) || sortType.equals(SortTypeEnum.TYPES)){

            if (array.get(0).getValue() < rel){

                array.remove(array.size()-1);
                Map.Entry<SearchEngineStatisticEntry, Double> tmp = new AbstractMap.SimpleEntry<>(new SearchEngineStatisticEntry(transcriptID, hits), rel);
                array.add(0, tmp);
            }else if (array.get(0).getValue() > rel && rel > array.get(array.size()-1).getValue()){
      
                for ( int i = array.size()-2; i >= 0 ; i-- ){
                    if (array.get(i).getValue() >= rel){
                        array.remove(array.size()-1);
                        Map.Entry<SearchEngineStatisticEntry, Double> tmp = new AbstractMap.SimpleEntry<>(new SearchEngineStatisticEntry(transcriptID, hits), rel);
                        array.add(i+1, tmp);
                        break;
                    }                                                
                }
            }


        // absolute descending
        }else if (sortType.equals(SortTypeEnum.ABS_DESC)){

            if (array.get(0).getKey().getNumberOfHits() < hits){
                                                 
                array.remove(array.size()-1);
                Map.Entry<SearchEngineStatisticEntry, Double> tmp = new AbstractMap.SimpleEntry<SearchEngineStatisticEntry, Double>(new SearchEngineStatisticEntry(transcriptID, hits), rel);
                array.add(0, tmp);
                
            }else if (array.get(0).getKey().getNumberOfHits() > hits && hits > array.get(array.size()-1).getKey().getNumberOfHits()){
                                              
                for ( int i = array.size()-2; i >= 0 ; i-- ){
                    if (array.get(i).getKey().getNumberOfHits() >= hits){
                        array.remove(array.size()-1);
                        Map.Entry<SearchEngineStatisticEntry, Double> tmp = new AbstractMap.SimpleEntry<SearchEngineStatisticEntry, Double>(new SearchEngineStatisticEntry(transcriptID, hits), rel);
                        array.add(i+1, tmp);
                        break;
                    }                                                
                }
            }
        }    
    }
    
    private void setIntervals(SearchEngineMatch match, ArrayList<MtasTokenString> array){
        for (MtasTokenString str : array){
            String prefix = str.getPrefix();
            String postfix = str.getPostfix();
            if (prefix.equals(Constants.TOKEN_INTERVAL) && !postfix.isEmpty()){
                String [ ] intervals = str.getPostfix().split(Constants.TOKEN_INTERVAL_DELIMITER);
                match.setStartInterval(Double.parseDouble(intervals[0]));
                match.setEndInterval(Double.parseDouble(intervals[1]));
            }
        }
    }
    
    private void setIntervals(SearchEngineMatch match, String str){
        String [ ] intervals = str.split(Constants.TOKEN_INTERVAL_DELIMITER);
        match.setStartInterval(Double.parseDouble(intervals[0]));
        match.setEndInterval(Double.parseDouble(intervals[1])); 
    }
    
    private SearchEngineMatch createMatch(CodecSearchTree.MtasTreeHit<String> term){
        //System.out.println(term.data);
        SearchEngineMatch match = new SearchEngineMatch();
        match.setId(CodecUtil.termValue(term.data));
        match.setStartPosition(term.startPosition);
        match.setEndPosition(term.startPosition);
        return match;
    }
    
    private List<String> getMetadataPrefixList(String metadataKeyID){
        List<String> metadataPrefixList = new ArrayList<String>();
        metadataPrefixList.add(metadataKeyID);
        return metadataPrefixList;
    }
    
    private interface FunktionsWrapper1{
        public void write(BufferedWriter bw, List<CodecSearchTree.MtasTreeHit<String>> terms) throws IOException;
    }
    
    private interface FunktionsWrapper2{
        public HashMap<Integer, Integer> addHitsToArray(SpanWeight spanweight, ListIterator<LeafReaderContext> iterator, ArrayList<Hit> arrayOfHits, int hitsBefore) throws IOException;
    }
    
    private interface FunktionsWrapper3{
        public void addHitToObject(Hit hit) throws IOException;
    }
    
    private List<CodecSearchTree.MtasTreeHit<String>> sortByStartPositions(List<CodecSearchTree.MtasTreeHit<String>> array){
        
        Collections.sort(array, new Comparator<CodecSearchTree.MtasTreeHit<String>>() {
            @Override
            public int compare(CodecSearchTree.MtasTreeHit<String> o1, CodecSearchTree.MtasTreeHit<String> o2) {
                return new Integer(o1.startPosition).compareTo(new Integer(o2.startPosition));
            }
        });
        
        return array;
        
    }
     

    @Override
    public SearchEngineResponse search(ArrayList<String> indexPaths, String queryString, String metadataQueryString) throws SearchServiceException, IOException {
        IndexReader indexReader = null;
        IndexSearcher searcher = null;
        
        //System.out.println("-- method: search");
        int hits_total = 0;
        int transcripts_total = 0;
        
        if(metadataQueryString == null || metadataQueryString.isEmpty()){
            for (String indexPath: indexPaths){
                Directory directory = null;
                directory = FSDirectory.open(Paths.get(indexPath));

                if (directory != null) {

                    try{   
                        indexReader = DirectoryReader.open(directory);

                        MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null);

                        ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();
                        searcher = new IndexSearcher(indexReader);

                        SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);

                        while (iterator.hasNext()) {
                            LeafReaderContext lrc = iterator.next();  
                            Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                            SegmentReader r = (SegmentReader) lrc.reader();               

                            Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);

                            // get mtas codec info from Lucene Terms
                            CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);

                            if (spans != null) {
                                while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                                    if (r.numDocs() == r.maxDoc() || r.getLiveDocs().get(spans.docID())) {
                                        transcripts_total++;

                                        while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                                            List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                 spans.docID(), TOKEN_IDS, spans.startPosition(),
                                                   (spans.endPosition() - 1));

                                            if (terms.size() > 0){ // because of proxy pauses in the speaker-based mode, here term.size can be 0
                                                    hits_total++; 
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        indexReader.close();

                    }catch (IndexNotFoundException ex) {
                        throw new IOException ("Search index could not be found! Please check " + indexPath, ex);
                    }
                }
            }
                
        }else{
            throw new SearchServiceException("Parameter 'metadataQueryString' is not supported yet!");
        }
        
        SearchEngineResponse result = new SearchEngineResponse();
        result.setHitsTotal(hits_total);
        result.setTranscriptsTotal(transcripts_total);

        return result;
    }


    /*************************************************************************************************/
    /*                                 SEARCH KWIC                                             */
    /*************************************************************************************************/
    
  
    @Override
    public SearchEngineResponseHitList searchKWIC(ArrayList<String> indexPaths, String queryString, String metadataQueryString, Integer from, Integer to, 
            Boolean cutoff, IDList metadataIDs) throws SearchServiceException, IOException {
        
        System.out.println("-- method: searchKWIC --");
        System.out.println("metadataQueryString: " + metadataQueryString);
        /*System.out.println("PARAMETER (FROM): " + from);
        System.out.println("PARAMETER (TO): " + to);*/
        
        MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null);
        
        if (metadataQueryString == null || metadataQueryString.isEmpty()){
            // search hits without metadata
            return searchKWICStandard(indexPaths, q, from, to, cutoff, metadataIDs);
            
        }else { 
            // parse metadataQuery
            Map<String, String> metadata;
            try{
                metadata = Arrays.stream(metadataQueryString.split(METADATA_QUERY_DELIMITER))
                        .map(s -> s.split("=")).collect(Collectors.toMap(s -> s[0], s-> s[1]));
            }catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e){
                metadata = Arrays.stream(IOHelper.HTML2IPA(metadataQueryString).split(METADATA_QUERY_DELIMITER))
                        .map(s -> s.split("=")).collect(Collectors.toMap(s -> s[0], s-> s[1]));
            }
            if(metadata.size()>1){
                throw new SearchServiceException("Metadata query with multiple parameters is not supported yet!");
            }else{
                Map.Entry<String,String> entry = metadata.entrySet().iterator().next();
                String metaDataKey = entry.getKey();
                String matadataValue = entry.getValue();
                
                // TODO: this should be done in the DGD2Searcher class
                if (metaDataKey.equals(Constants.ATTRIBUTE_NAME_PHON)){
                    metaDataKey = METADATA_KEY_MATCH_TYPE_PHON_HTML;
                }
                
                if (metaDataKey.equals(METADATA_KEY_MATCH_TYPE_PHON_HTML)){
                    matadataValue = IOHelper.IPA2HTML(entry.getValue());
                }
                
                if (metaDataKey.equals(METADATA_KEY_HIT_LENGTH) || metaDataKey.equals(METADATA_KEY_HIT_LENGTH_IN_WORD_TOKENS)){
                    Integer size;
                    try {
                        size = Integer.parseInt(matadataValue);
                    }catch (NumberFormatException ex){
                        throw new SearchServiceException("Please check the syntax of the metadata query (param: " + metaDataKey + ")");
                    }
                    
                    if (metaDataKey.equals(METADATA_KEY_HIT_LENGTH)){
                        // search hits by the number of tokens  
                        return searchKWICByLength(indexPaths, q, from, to, cutoff, TOKEN_IDS, size);
                    }else {
                        // search hits by the number of word tokens  
                        return searchKWICByLength(indexPaths, q, from, to, cutoff, getMetadataPrefixList(Constants.METADATA_KEY_MATCH_TYPE_WORD + ".id"), size);
                    }
                }else {
                    // search hits by metadata
                    return searchKWICByMetadata(indexPaths, q, from, to, cutoff, metaDataKey, matadataValue);
  
                }
            }
        }
    }
    
    
    private SearchEngineResponseHitList searchKWICStandard(ArrayList<String> indexPaths, MtasSpanQuery queryString, Integer from, Integer to, 
            Boolean cutoff, IDList metadataIDs) throws SearchServiceException, IOException {
        
        System.out.println("-- METHOD: searchKWICStandard --");
        SearchEngineResponseHitList result =  searchKWIC(indexPaths, queryString, new FunktionsWrapper2 (){
            @Override
            public HashMap<Integer, Integer> addHitsToArray(SpanWeight spanweight, ListIterator<LeafReaderContext> iterator, ArrayList<Hit> arrayOfHits, int hitNumber) throws IOException{
                return searchHits(metadataIDs, spanweight, iterator, hitNumber, from, to, cutoff, new FunktionsWrapper3(){
                    @Override
                    public void addHitToObject(Hit hit) throws IOException {
                       arrayOfHits.add(hit); 
                    }
                    
                });
            }
        });

        return result;
    }
    
    private HashMap searchHits(IDList metadata, SpanWeight spanweight, ListIterator<LeafReaderContext> iterator, 
            int hitNumber, Integer from, Integer to, Boolean cutoff, FunktionsWrapper3 function) throws IOException{
        //System.out.println("-- METHOD: searchHits --");
                int hits_total = 0;
                int transcripts_total = 0;
                
                segment_loop:
                while (iterator.hasNext()) {

                    LeafReaderContext lrc = iterator.next();  
                    //System.out.println("INDEX SEGMENT NUMBER: " + lrc.docBase);

                    Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                    SegmentReader r = (SegmentReader) lrc.reader();

                    Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);

                    // get mtas codec info from Lucene Terms
                    CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);


                        /*
                        int numberOfDocs = mtasCodecInfo.getNumberOfDocs(FIELD_TRANSKRIPT_CONTENT);
                        System.out.println("Number of Docs: " + numberOfDocs);*/

                       /* Set<String> prefixes = mtasCodecInfo.getPrefixes(FIELD_TRANSCRIPT_CONTENT);
                        List<String> prefixesList = prefixes.stream().collect(Collectors.toList());
                        System.out.println("PrefixesList: " + prefixesList.toString());*/

                        /*
                        HashMap<Integer, Integer> mapAllNumberOfPositions = mtasCodecInfo.getAllNumberOfPositions(FIELD_TRANSKRIPT_CONTENT, lrc.docBase);
                        System.out.println("mapAllNumberOfPositions " + mapAllNumberOfPositions);

                        HashMap<Integer, Integer> mapAllNumberOfTokens = mtasCodecInfo.getAllNumberOfTokens(FIELD_TRANSKRIPT_CONTENT, lrc.docBase);
                        System.out.println("mapAllNumberOfTokens " + mapAllNumberOfTokens);*/

                    if (spans != null) {
                        while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                            if (r.numDocs() == r.maxDoc() || r.getLiveDocs().get(spans.docID())) {

                                    ////System.out.println("SpansToString: " + spans.toString());
                                    ////System.out.println("LUCENE DOCUMENT ID: " + spans.docID() + " (" + r.document(spans.docID()).get(FIELD_TRANSKRIPT_ID) + ")");
                                    //String corpusId = r.document(spans.docID()).get(FIELD_CORPUS_ID);
                                    

                                transcripts_total = transcripts_total + 1;
                                int hits = 0;

                                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {

                                    // collect all token ids and their positions in XML
                                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                 spans.docID(), TOKEN_IDS, spans.startPosition(),
                                                   (spans.endPosition() - 1));

                                    if (terms.size() > 0){  // because of proxy pauses in the speaker-based mode, here term.size can be 0
                                        hits++; 
                                        hitNumber++;
                                        
                                        if (hitNumber >= from && hitNumber<=to){

                                             ////   System.out.println("---------------START HIT " + hitNumber + "-------------------");
                                            SearchEngineHit hit = new SearchEngineHit();

                                            /*String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_ID_FROM_FILE_NAME);
                                            System.out.println("transcriptID: " + transcriptID);
                                            System.out.println("transcriptID.substring(transcriptID.length() - TRANSCRIPT_ID_LENGTH): " + transcriptID.substring(transcriptID.length() - TRANSCRIPT_ID_LENGTH));
                                            hit.setDocId(transcriptID.substring(transcriptID.length() - TRANSCRIPT_ID_LENGTH)); //NOTE: hit.setDocId(searcher.doc(spans.docID().getField(FIELD_TRANSKRIPT_ID).stringValue()); returns false id
                                            */
                                            String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG);
                                            hit.setDocId(transcriptID);
                                            hit.setPosition(hitNumber);

                                            for (CodecSearchTree.MtasTreeHit<String> term : terms) {
                                                SearchEngineMatch match = createMatch(term);
                                                ArrayList<MtasTokenString> array = mtasCodecInfo.getObjectsByPosition(FIELD_TRANSCRIPT_CONTENT, spans.docID(), term.startPosition);
                                                setIntervals(match, array);
                                                hit.addMatch(match);
                                            }
                                            
                                            
                                            /* add metadata for hit */
                                            HashMap<String, String> metadataMap = new HashMap();
                                            if(metadata!=null && metadata.size()>0){
                                                for (String metadataKeyID: metadata){
                                                    List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                                                     .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                                                             spans.docID(), getMetadataPrefixList(metadataKeyID), spans.startPosition(),
                                                                                             (spans.endPosition() - 1));

                                                     if (terms2.size() > 0){
                                                         Set<String> values = new HashSet<String>();
                                                         for (CodecSearchTree.MtasTreeHit<String> term : terms2) {                                                   
                                                             String value = CodecUtil.termValue(term.data);
                                                             if (value == null){
                                                                 values.add(METADATA_NO_VALUE);
                                                             }else{
                                                                 values.add(value);
                                                             }

                                                         } 

                                                         List<String> list = new ArrayList<String>(values);
                                                         Collections.sort(list); 
                                                         metadataMap.put(metadataKeyID, String.join(", ", list));   
                                                     }

                                                }
                                            }
                                            
                                            
                                            hit.setMetadata(metadataMap);
                                            function.addHitToObject(hit);
                                            //arrayOfHits.add(hit);
                                            
                                                        
                                                 ////System.out.println("-------------------END HIT--------------------");

                                        } else if (cutoff == false && hitNumber > to) {
                                            break segment_loop;
                                        }
                                    }
                                    //}

                                }
                                hits_total = hits_total + hits; 
                            }
                        }
                    }
                }     
                HashMap<Integer, Integer> hm = new HashMap();
                hm.put(hits_total, transcripts_total);
                return hm;
    }
    
    private SearchEngineResponseHitList searchKWICByMetadata(ArrayList<String> indexPaths, MtasSpanQuery queryString, Integer from, Integer to, 
            Boolean cutoff, String metaDataKey, String matadataValue) throws SearchServiceException, IOException {
        
      /*  System.out.println("-- METHOD: searchKWICByMetadata --");
        System.out.println("metaDataKey: " + metaDataKey);
        System.out.println("matadataValue: " + matadataValue);
       */
        return searchKWIC(indexPaths, queryString, new FunktionsWrapper2 (){
            @Override
            public HashMap<Integer, Integer> addHitsToArray(SpanWeight spanweight, ListIterator<LeafReaderContext> iterator, ArrayList<Hit> arrayOfHits, int hitNumber) throws IOException{
                
                int transcripts_total = 0;
                int hits_total = 0;
                
                segment_loop:
                while (iterator.hasNext()) {

                    LeafReaderContext lrc = iterator.next();  
                    Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                    SegmentReader r = (SegmentReader) lrc.reader();
                    Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);

                    // get mtas codec info from Lucene Terms
                    CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);

                    if (spans != null) {
                        while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                            if (r.numDocs() == r.maxDoc() || r.getLiveDocs().get(spans.docID())) {
                                transcripts_total = transcripts_total + 1;
                                int hits = 0;

                                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {


                                    // collect all token ids and their positions in XML
                                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                                                        .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                                                spans.docID(), TOKEN_IDS, spans.startPosition(),
                                                                                (spans.endPosition() - 1));
                                    
                                    if (terms.size() > 0){
                                        List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                                                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                                                        spans.docID(), getMetadataPrefixList(metaDataKey), spans.startPosition(),
                                                                                        (spans.endPosition() - 1));

                                        
                                        String listString = "";
                                        if (terms2.isEmpty() && matadataValue.equals(Constants.EMPTY_TOKEN)) {
                                            listString =  Constants.EMPTY_TOKEN;                            
                                        }else if (terms2.size()>0){
                                            
                                            if((metaDataKey.equals(METADATA_KEY_MATCH_TYPE_PHON_HTML)
                                                || Arrays.asList(Constants.TOKENS).contains(metaDataKey)
                                                || Arrays.asList(BASIC_TOKEN_ANNOTATION_LAYERS).contains(metaDataKey)  
                                                || Arrays.stream(Constants.PROXI_TOKEN_ANNOTATION_LAYERS) 
                                                    .collect(Collectors.toMap(entity -> entity[0], entity -> entity[1])).keySet().contains(metaDataKey)
                                                )&& terms2.size()> 1){
                                                
                                                List<CodecSearchTree.MtasTreeHit<String>> terms3 = sortByStartPositions(terms2);
                                                ArrayList<String> list = new ArrayList();
                                                for (CodecSearchTree.MtasTreeHit<String> term : terms3){
                                                    
                                                    String value = CodecUtil.termValue(term.data);   
                                                    list.add(value);
                                                }
                                                
                                                listString = String.join(Constants.TOKEN_DELIMITER, list);
                                              
                                            }else{
                                                // for metadata only distinct values per hit
                                                Set<String> values = new HashSet<String>();
                                                for (CodecSearchTree.MtasTreeHit<String> term : terms2) {                                                   
                                                    String value = CodecUtil.termValue(term.data);
                                                    if (value == null){
                                                        values.add(METADATA_NO_VALUE);
                                                    }else{
                                                        values.add(value);
                                                    }
                                                    
                                                } 

                                                List<String> list = new ArrayList<String>(values);
                                                Collections.sort(list); 
                                                listString = String.join(", ", list);
                                            }
                                            
                                        }else{
                                            listString = METADATA_VALUE_NOT_AVAILABLE;
                                        }  

                                        if (listString.equals(matadataValue)){
                                            hits++; 
                                            hitNumber++;
                                            if (hitNumber >= from && hitNumber<=to){
                                                    ////System.out.println("---------------START HIT " + hitNumber + "-------------------");
                                                    SearchEngineHit hit = new SearchEngineHit();
                                                    //String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_ID_FROM_FILE_NAME);
                                                    String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG);
                                                    //System.out.println(transcriptID);
                                                    //hit.setDocId(transcriptID.substring(transcriptID.length() - TRANSCRIPT_ID_LENGTH)); //NOTE: hit.setDocId(searcher.doc(spans.docID().getField(FIELD_TRANSKRIPT_ID).stringValue()); returns false id
                                                    hit.setDocId(transcriptID);
                                                    hit.setPosition(hitNumber);

                                                    for (CodecSearchTree.MtasTreeHit<String> term : terms) {
                                                        SearchEngineMatch match = createMatch(term);
                                                        ArrayList<MtasTokenString> array = mtasCodecInfo.getObjectsByPosition(FIELD_TRANSCRIPT_CONTENT, spans.docID(), term.startPosition);
                                                        setIntervals(match, array);
                                                        hit.addMatch(match);
                                                    }

                                                    arrayOfHits.add(hit);

                                                    ////System.out.println("-------------------END HIT--------------------");

                                            } else if (cutoff == false && hitNumber > to) {
                                                break segment_loop;
                                            }
                                        }
                                    }

                                }

                                hits_total = hits_total + hits; 
                            }
                        }
                    }
                }
                HashMap<Integer, Integer> hm = new HashMap();
                hm.put(hits_total, transcripts_total);
                return hm;
            }
        });
    }
             
    private SearchEngineResponseHitList searchKWICByLength(ArrayList<String> indexPaths, MtasSpanQuery queryString, Integer from, Integer to, 
            Boolean cutoff, List<String> prefixList, Integer size) throws SearchServiceException, IOException {

        //System.out.println("-- METHOD: searchKWICByLength");
        return searchKWIC(indexPaths, queryString, new FunktionsWrapper2 (){
            @Override
            public HashMap<Integer, Integer> addHitsToArray(SpanWeight spanweight, ListIterator<LeafReaderContext> iterator, ArrayList<Hit> arrayOfHits, int hitNumber) throws IOException{
                int transcripts_total = 0;
                int hits_total = 0;
                segment_loop:
                while (iterator.hasNext()) {

                        LeafReaderContext lrc = iterator.next();  
                        Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                        SegmentReader r = (SegmentReader) lrc.reader();
                        Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);

                        // get mtas codec info from Lucene Terms
                        CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);

                        if (spans != null) {
                            while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                                if (r.numDocs() == r.maxDoc() || r.getLiveDocs().get(spans.docID())) {
                                        transcripts_total = transcripts_total + 1;
                                        int hits = 0;

                                        while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {

                                                // collect all token ids and their positions in XML
                                                List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                                    spans.docID(), prefixList, spans.startPosition(),
                                                                    (spans.endPosition() - 1));
                                                
                                                int hitLength = terms.size();
                                                if (hitLength > 0 && hitLength==size){
                                                    
                                                    hits++; 
                                                    hitNumber++;
                                                    if (hitNumber >= from && hitNumber<=to){

                                                        ////System.out.println("---------------START HIT " + hitNumber + "-------------------");
                                                        SearchEngineHit hit = new SearchEngineHit();
                                                        String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG);
                                                        //System.out.println(transcriptID);
                                                        //hit.setDocId(transcriptID.substring(transcriptID.length() - TRANSCRIPT_ID_LENGTH)); //NOTE: hit.setDocId(searcher.doc(spans.docID().getField(FIELD_TRANSKRIPT_ID).stringValue()); returns false id
                                                        hit.setDocId(transcriptID);
                                                        hit.setPosition(hitNumber);

                                                        for (CodecSearchTree.MtasTreeHit<String> term : terms) {
                                                            SearchEngineMatch match = createMatch(term);
                                                            ArrayList<MtasTokenString> array = mtasCodecInfo.getObjectsByPosition(FIELD_TRANSCRIPT_CONTENT, spans.docID(), term.startPosition);
                                                            setIntervals(match, array);
                                                            hit.addMatch(match);
                                                        }

                                                        arrayOfHits.add(hit);
                                                        
                                                        ////System.out.println("-------------------END HIT--------------------");

                                                    } else if (cutoff == false && hitNumber > to) {
                                                            break segment_loop;
                                                    }
                                                }

                                        }

                                        hits_total = hits_total + hits; 
                                    
                                }
                            }
                        }
                }
                HashMap<Integer, Integer> hm = new HashMap();
                hm.put(hits_total, transcripts_total);
                return hm;
            }
        });
    }
    
    private SearchEngineResponseHitList searchKWIC(ArrayList<String> indexPaths, MtasSpanQuery q, 
            FunktionsWrapper2 function) throws SearchServiceException, IOException {
        
        IndexReader indexReader = null;
        IndexSearcher searcher = null;
        
        //System.out.println("-- METHOD: searchKWIC with FunktionsWrapper2");
        
        Integer hits_total = 0;
        Integer transcripts_total = 0;
        
        SearchEngineResponseHitList result = new SearchEngineResponseHitList();
        
        ArrayList<Hit> arrayOfHits = new ArrayList();
             
            for (String indexPath: indexPaths){
                
                Directory directory = null;
                try{
                    directory = FSDirectory.open(Paths.get(indexPath));
                }catch (AccessDeniedException e){
                    throw new AccessDeniedException (indexPath);
                }

                if (directory != null) {
                    try{   
                        indexReader = DirectoryReader.open(directory);

                        ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();
                        searcher = new IndexSearcher(indexReader);

                        SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);

                        HashMap<Integer, Integer> hm;
                        hm = function.addHitsToArray(spanweight, iterator, arrayOfHits, hits_total);
                        
                        Map.Entry<Integer,Integer> entry = hm.entrySet().iterator().next();
                        hits_total = hits_total+entry.getKey();
                        transcripts_total=transcripts_total+entry.getValue();

                        indexReader.close();

                    }catch (IndexNotFoundException ex) {
                        throw new IOException ("Search index could not be found!", ex);

                    }

                }
            }
        result.setHits(arrayOfHits);
        
        
        result.setHitsTotal(hits_total);
        result.setTranscriptsTotal(transcripts_total);
        return result;
        
    }
    
    
    /*************************************************************************************************/
    /*                                 SEARCH STATISTICS                                             */
    /*************************************************************************************************/
  
    
    @Override
    public SearchEngineResponseStatistics searchMetadataStatistics(ArrayList<String> indexPaths, String queryString, String metadataQueryString,
            Integer from, Integer to, SortTypeEnum sortType, String metadataKeyID) throws SearchServiceException, IOException {
        
        if(metadataQueryString==null || metadataQueryString.isEmpty()){
            
            //System.out.println("-- method: searchMetadataStatistic");
           /* System.out.println("PARAMETER (FROM): " + from);
            System.out.println("PARAMETER (TO): " + to);
            System.out.println("PARAMETER (SORT TYPE): " + sortType);
            System.out.println("PARAMETER (METADATA KEY ID): " + metadataKeyID);*/

            if (metadataKeyID.equals(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG) && (sortType.equals(SortTypeEnum.REL_DESC) || sortType.equals(SortTypeEnum.TYPES)) && from==1){
                /* This method is faster than searchStatistic, because it uses FIELD_TRANSCRIPT_TOKEN_TOTAL from the search index,
                but it should not be used with indices from the SPEAKER-BASED MODE!!!=> ERROR */
                return searchDocumentStatistics(indexPaths, queryString, to, sortType); 
            }else{
                return searchStatistics(indexPaths, queryString, from, to, sortType, metadataKeyID);
            }
        }else{
            throw new SearchServiceException("Parameter 'metadataQueryString' is not supported yet!");
        }
    }
    
    private SearchEngineResponseStatistics searchDocumentStatistics(ArrayList<String> indexPaths, String queryString,
            Integer max, SortTypeEnum sortType) throws SearchServiceException, IOException {

        IndexReader indexReader = null;
        IndexSearcher searcher = null;
        
        //System.out.println("-- method: searchDocumentStatistics");
        int hits_total = 0;
        int transcripts_total = 0;
        ArrayList<Map.Entry<SearchEngineStatisticEntry, Double>> statisticsTmp = new ArrayList<>();
        boolean arraySorted = false;

        for (String indexPath : indexPaths){
            Directory directory = FSDirectory.open(Paths.get(indexPath));
        
            if (directory != null) {
                try{
                    indexReader = DirectoryReader.open(directory);

                    MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null);

                    ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();
                    searcher = new IndexSearcher(indexReader);

                    SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);

                    while (iterator.hasNext()) {
                        LeafReaderContext lrc = iterator.next();
                        Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                        SegmentReader r = (SegmentReader) lrc.reader();
                          
                        Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);
                        CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);
                            
                        if (spans != null) {
                            while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                                if (r.numDocs()==r.maxDoc() || r.getLiveDocs().get(spans.docID())) { 
                                    String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG);
                                    int hits = 0;
                                    Set<String> setOfTypes = new HashSet();
                                    while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                                        
                                        List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                 spans.docID(), TOKEN_IDS, spans.startPosition(),
                                                   (spans.endPosition() - 1));

                                        if (terms.size() > 0){  // because of proxy pauses in the speaker-based mode, here term.size is 0
                                            hits++;    
                                            
                                            if (sortType.equals(SortTypeEnum.TYPES)){
                                                List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                                                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                                                        spans.docID(), getMetadataPrefixList(Constants.ATTRIBUTE_NAME_LEMMA), spans.startPosition(),
                                                                                        (spans.endPosition() - 1));
                                                if (terms2.size()>0){
                                                    for (CodecSearchTree.MtasTreeHit<String> term : terms2) {
                                                        setOfTypes.add(CodecUtil.termValue(term.data));
                                                    }
                                                    /*List<CodecSearchTree.MtasTreeHit<String>> term2WithoutDuplicates = new ArrayList<>(new HashSet<>(terms2));
                                                    types = term2WithoutDuplicates.size();   ...    */                                       
                                                }
                                            }
                                        }             
                                    }
               
                                    if (hits>0){
                                        transcripts_total = transcripts_total + 1;
                                        double tokenTotal = Double.parseDouble(r.document(spans.docID()).get(FIELD_TRANSCRIPT_TOKEN_TOTAL));                                      
                                        Double rel = hits*100/ tokenTotal;
                                        Double doubleValue = rel;
                                        if (sortType.equals(SortTypeEnum.TYPES)){
                                            doubleValue = Double.valueOf(setOfTypes.size());
                                        }

                                        if (statisticsTmp.size()< max){
                                            Map.Entry<SearchEngineStatisticEntry, Double> tmp = new AbstractMap.SimpleEntry<>(new SearchEngineStatisticEntry(transcriptID, hits), doubleValue);
                                            statisticsTmp.add(tmp);
                                        }else if (statisticsTmp.size() == max){ 

                                            //sort
                                            if (sortType.equals(SortTypeEnum.REL_DESC) || sortType.equals(SortTypeEnum.TYPES)){
                                                sortByRelativeValue(statisticsTmp);
                                            }else if (sortType.equals(SortTypeEnum.ABS_DESC)){
                                                sortByAbsoluteValue(statisticsTmp);
                                            }                                           
                                                /*
                                                for(Map.Entry<DGD2StatisticEntry, Double> i: statisticsTmp) {
                                                    System.out.println(i.getKey() + "-" + i.getValue());
                                                }
                                                */
                                            arraySorted = true;
                                            addStatisticEntry(statisticsTmp, transcriptID, hits, doubleValue, sortType);
                                        }else if (statisticsTmp.size()>max){
                                            addStatisticEntry(statisticsTmp, transcriptID, hits, doubleValue, sortType);
                                        }
                                    }
                                    hits_total = hits_total + hits; 
                                } 
                            }
                        }
                    }
                    indexReader.close();
                }catch (IndexNotFoundException ex) {
                    throw new IOException ("Search index could not be found under " + indexPath, ex);
                }
            }
        }
        
        
        // sort if not sorted yet
        if(!arraySorted){
            //relative descending
            if (sortType.equals(SortTypeEnum.REL_DESC)){
               sortByRelativeValue(statisticsTmp);
            }
                                            
            //absolute descending
            else if (sortType.equals(SortTypeEnum.ABS_DESC)){
                sortByAbsoluteValue(statisticsTmp);
            }
        }
        
        // delete relative values
        ArrayList<StatisticEntry>  statistics = new ArrayList<StatisticEntry>();
        statisticsTmp.forEach((entry) -> {
            statistics.add(entry.getKey());
        });
        
        // create search result object
        SearchEngineResponseStatistics result = new SearchEngineResponseStatistics();
        result.setStatistics(statistics);
        result.setHitsTotal(hits_total);
        result.setTranscriptsTotal(transcripts_total);
        result.setNumberOfDistinctValues(transcripts_total);

        return result;
    }
    
    private SearchEngineResponseStatistics searchStatistics(ArrayList<String> indexPaths, String queryString, 
            Integer from, Integer to, SortTypeEnum sortType, String metadataKeyID) throws SearchServiceException, IOException {

            //System.out.println("-- method: searchStatistic");
            MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null);
            
            int hits_total = 0;
            int transcripts_total = 0;
            int distinctValues = 0;
      
            ArrayList<StatisticEntry>  statistics = new ArrayList<>();
            
            String nextLine = null;
            String temp = "0000000000";

            File tempFile1 = File.createTempFile("tmp", ".txt");
            //System.out.println(tempFile1.getAbsolutePath());
            tempFile1.deleteOnExit();

            File tempFile2 = File.createTempFile("tmp", ".txt");
            tempFile2.deleteOnExit();

            File tempFile3 = File.createTempFile("tmp", ".txt");
            tempFile3.deleteOnExit();

            File tempFile4 = File.createTempFile("tmp", ".txt");
            tempFile4.deleteOnExit();

            // create file with all values
            Map<Integer, Integer> search;
            if (metadataKeyID.equals(METADATA_KEY_HIT_LENGTH)){
                search = searchValues(indexPaths, q, tempFile1, TOKEN_IDS, new FunktionsWrapper1() {
                        @Override
			public void write(BufferedWriter bw, List<CodecSearchTree.MtasTreeHit<String>> terms) throws IOException{
                            int hitLength = terms.size();
                            bw.write(temp.substring(0,temp.length()- String.valueOf(hitLength).length()) + hitLength);
                            bw.newLine();
			}});            
            }else if (metadataKeyID.equals(METADATA_KEY_HIT_LENGTH_IN_WORD_TOKENS)){
                search = searchValues(indexPaths, q, tempFile1, getMetadataPrefixList(Constants.METADATA_KEY_MATCH_TYPE_WORD + ".id"), new FunktionsWrapper1() {
                        @Override
			public void write(BufferedWriter bw, List<CodecSearchTree.MtasTreeHit<String>> terms) throws IOException{
                            int hitLength = terms.size();
                            bw.write(temp.substring(0,temp.length()- String.valueOf(hitLength).length()) + hitLength);
                            bw.newLine();
			}});    
            /*}else if (metadataKeyID.equals(Constants.ATTRIBUTE_NAME_PHON) || metadataKeyID.equals(Constants.METADATA_KEY_MATCH_TYPE_PHON_HTML)){
                search = searchValues(indexPaths, q, tempFile1, Constants.METADATA_KEY_MATCH_TYPE_PHON_HTML);   */         
            }
            else if (metadataKeyID.equals(METADATA_KEY_MATCH_TRANSCRIPTION)                    
                    || (!metadataKeyID.equals(Constants.ELEMENT_NAME_PAUSE) && Arrays.asList(Constants.TOKENS).contains(metadataKeyID))
                    || Arrays.asList(BASIC_TOKEN_ANNOTATION_LAYERS).contains(metadataKeyID)  
                    || Arrays.stream(Constants.PROXI_TOKEN_ANNOTATION_LAYERS) 
                        .collect(Collectors.toMap(entity -> entity[0], entity -> entity[1])).keySet().contains(metadataKeyID)
                    ){
                search = searchValues(indexPaths, q, tempFile1, metadataKeyID);
            } else{
                search = searchValues(indexPaths, q, tempFile1, getMetadataPrefixList(metadataKeyID), new FunktionsWrapper1() {
                        @Override
			public void write(BufferedWriter bw, List<CodecSearchTree.MtasTreeHit<String>> terms) throws IOException {
                            String listString = "";
                            if (terms.size()>0){
                                
                                // only distinct values per hit
                                Set<String> values = new HashSet<String>();
                                for (CodecSearchTree.MtasTreeHit<String> term : terms) {                                                   
                                    String value = CodecUtil.termValue(term.data);
                                    
                                    if (value == null){
                                        values.add(METADATA_NO_VALUE);
                                    }else{
                                        values.add(value);
                                    }
                                }
                                List<String> list = new ArrayList<String>(values); 
                                    Collections.sort(list); 
                                    listString = String.join(", ", list);
                                    
                            }else{
                                listString = METADATA_VALUE_NOT_AVAILABLE;                                          
                            }
                            bw.write(listString);
                            bw.newLine();
			}});                   
            }
            
            if (search!=null){
                int key = (int) search.keySet().toArray()[0];
                transcripts_total = transcripts_total + key;
                hits_total = hits_total + search.get(key);
            }
            
            
            
            
            if (hits_total < 100 &&  sortType.equals(SortTypeEnum.ABS_DESC)){
                BufferedReader br = null;
                HashMap<String, Integer> hm = new HashMap();
                try{
                    br=new BufferedReader(new FileReader(new File(tempFile1.getAbsolutePath())));
                    while((nextLine=br.readLine())!=null){ 
                        if (hm.containsKey(nextLine)){
                            hm.put(nextLine, hm.get(nextLine) + 1);
                        }else{
                           hm.put(nextLine, 1);
                           distinctValues++;
                        }        
                    }  
                }catch(IOException ex){
                    throw new IOException ("Error with temporary files when grouping metadata values. " + ex);
                }finally {
                    if (br!=null){
                        br.close();
                    }
                }
                
                List list=new ArrayList(hm.entrySet());

                Collections.sort(list,new Comparator(){
                    public int compare(Object obj1, Object obj2){
                        return ((Comparable)((Map.Entry)(obj1)).getValue()).compareTo(((Map.Entry)(obj2)).getValue());
                    }
                });
                
                for (Iterator it = list.iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    statistics.add(new SearchEngineStatisticEntry((String) entry.getKey(), (Integer) entry.getValue()));
                } 
                
            }else{   
                // sort values
                TextFileSorter sorter = new TextFileSorter(new SortConfig().withMaxMemoryUsage(20 * 1000 * 1000));
                InputStream input1 = new FileInputStream(tempFile1.getAbsolutePath());
                sorter.sort(input1, new FileOutputStream(tempFile2.getAbsolutePath()));


                // group values
                BufferedWriter bw2 = null;
                BufferedReader br = null;
                try{
                    bw2 = new BufferedWriter(new FileWriter(tempFile3, true)); 
                    br=new BufferedReader(new FileReader(new File(tempFile2.getAbsolutePath())));

                    String firstLine = br.readLine();
                    int index1 = 1;
                    if (metadataKeyID.equals(METADATA_KEY_HIT_LENGTH) || metadataKeyID.equals(METADATA_KEY_HIT_LENGTH_IN_WORD_TOKENS) || sortType.equals(SortTypeEnum.ABS_DESC) || sortType.equals(SortTypeEnum.ABS_ASC)){

                        while((nextLine=br.readLine())!=null){  
                            if(nextLine.equals(firstLine)){
                                index1++;
                            }else{
                                bw2.write(temp.substring(0,temp.length()- String.valueOf(index1).length()) + index1 + " " + firstLine);
                                bw2.newLine();
                                firstLine = nextLine;
                                index1=1;
                            }  
                        }
                        bw2.write(temp.substring(0,temp.length()- String.valueOf(index1).length()) + index1 + " " + firstLine);
                    }else if (sortType.equals(SortTypeEnum.REL_DESC) || sortType.equals(SortTypeEnum.REL_ASC)){
                        while((nextLine=br.readLine())!=null){  
                            if(nextLine.equals(firstLine)){
                                index1++;
                            }else{
                                String rel = getRelativeValue(indexPaths, firstLine, index1, metadataKeyID);
                                bw2.write(rel + " " + firstLine + " " + index1);
                                bw2.newLine();
                                firstLine = nextLine;
                                index1=1;
                            }  
                        }
                        String rel = getRelativeValue(indexPaths, firstLine, index1, metadataKeyID);
                        bw2.write(rel + " " + firstLine + " " + index1);
                    }else if (sortType.equals(SortTypeEnum.TYPES)){
                        while((nextLine=br.readLine())!=null){  
                            if(nextLine.equals(firstLine)){
                                index1++;
                            }else{
                                String distValues = getDistinctValues(queryString, firstLine, metadataKeyID, indexPaths, 0, 0, SortTypeEnum.ABS_DESC, Constants.ATTRIBUTE_NAME_LEMMA);
                                bw2.write(temp.substring(0,temp.length()- String.valueOf(distValues).length()) + distValues + " " + firstLine + " " + index1);
                                bw2.newLine();
                                firstLine = nextLine;
                                index1=1;
                            }  
                        }
                        String distValues = getDistinctValues(queryString, firstLine, metadataKeyID, indexPaths, 0, 0, SortTypeEnum.ABS_DESC, Constants.ATTRIBUTE_NAME_LEMMA);
                        bw2.write(temp.substring(0,temp.length()- String.valueOf(distValues).length()) + distValues + " " + firstLine + " " + index1);
                    }else{
                        throw new SearchServiceException("Sort type " + sortType + " is not supported yet!");
                    }           

                }catch(IOException ex){
                    throw new IOException ("Error with temporary files when grouping metadata values. " + ex);
                }finally {
                    if (br!=null){
                        br.close();
                    }
                    if(bw2!=null){
                        bw2.close();
                    }
                }


                // sort
                InputStream input2 = new FileInputStream(tempFile3.getAbsolutePath());
                sorter.sort(input2, new FileOutputStream(tempFile4.getAbsolutePath()));

                // read result
                /*File file2 = new File(tempFile4.getAbsolutePath());  
                FileReader fr2 = new FileReader(file2);
                BufferedReader br2 = new BufferedReader(fr2);*/

                
                ReversedLinesFileReader fr2 = null;
                try{
                    fr2 = new ReversedLinesFileReader(new File(tempFile4.getAbsolutePath()));
                    int index2 = 0;

                    //while((nextLine=br2.readLine())!=null){
                    nextLine = null;
                    while((nextLine=fr2.readLine())!=null){
                        index2++;
                        if(from <= index2 && index2 <= to){
                            String[] values = nextLine.split(" ", 2);

                            if (sortType.equals(SortTypeEnum.REL_DESC) || sortType.equals(SortTypeEnum.TYPES)){
                                Pattern r = Pattern.compile("(.+)\\s(\\d+)$");
                                Matcher m = r.matcher(values[1]);
                                if (m.find( )) {
                                   statistics.add(new SearchEngineStatisticEntry(m.group(1), Integer.parseInt(m.group(2))));
                                }   

                            } else if (sortType.equals(SortTypeEnum.ABS_DESC)){
                                statistics.add(new SearchEngineStatisticEntry(values[1], Integer.parseInt(values[0])));
                            }
                        }
                        distinctValues++;
                    }
                }catch(IOException ex){
                    throw new IOException ("Error with temporary files when reading results of metadata sort. " + ex);
                }finally {
                    if(fr2!=null){
                        fr2.close();
                    }
                }

            }
            

            // delete all temp files
            tempFile1.delete();
            tempFile2.delete();
            tempFile3.delete();
            tempFile4.delete();
            
            // create search result object
            SearchEngineResponseStatistics result = new SearchEngineResponseStatistics();
            result.setStatistics(statistics);
            result.setHitsTotal(hits_total);
            result.setTranscriptsTotal(transcripts_total);
            result.setNumberOfDistinctValues(distinctValues);

            return result;
        
    }
    
    
    /**
    * Executes the search and writes the retrieved values of the specified category ​​​​to the temporary file. 
    * <p>
    * This method is used in the {@link #searchStatistics()} to get the number of tokens for each search query hit 
    * or to retrieve the values of the specified annotation layer/metadata category if it overlap with search query hits. 
    * <p>
    * If the specified category doesn't exist for the current hit, the value is {@link #METADATA_VALUE_NOT_AVAILABLE}. 
    * If it exists, but don't have any value for the current hit (like is it the case for "pause" or "annotationBlock"),
    * the value is {@link METADATA_NO_VALUE}.
    *
    * 
    * @param indexPaths the list of absolute paths to the search indices, 
    *                   for example [C:\Users\...\ZuMult\indices\TB_FOLK, C:\Users\...\ZuMult\indices\TB_GWSS]
    * 
    * @param q          the search query already converted into the MtasSpanQuery format
    *                   (for more information see {@link #createQuery()})
    * 
    * @param tempFile   the file to which the values ​​are to be written
    * 
    * @param prefixList the list of attributes whose values ​​are to be retrieved from the search indices.
    * 
    * 
    * @param function   the function that specifies how the values ​​are to be written to the temporary file
    *                   (for more information see {@link #write()} in {@link #searchStatistics()})
    * 
    * <p>
    * <b>Note:</b>
    * It can be the id of one of the metadata categories stored in the search index 
    * like "t_dgd_kennung" or "s_geschlecht". In order to get the number of tokens for each hit,
    * it should be a list of token id attributes specified in {@link TOKEN_IDS}.
    * 
    * @throws IOException if stream to file cannot be written or if the search index could not be found under the specified path
    */
    private Map<Integer, Integer> searchValues(ArrayList<String> indexPaths, MtasSpanQuery q, File tempFile, 
            List<String> prefixList, FunktionsWrapper1 function) throws IOException {
        
        IndexReader indexReader = null;
        IndexSearcher searcher = null;
        
     /*   System.out.println("-- METHOD: searchValues with FunktionsWrapper");
        System.out.println("List<String> prefixList: " + prefixList);
        System.out.println("indexPaths: " + indexPaths);*/
        int transcripts = 0;
        int hits = 0;
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile, true))) {
                
            for (String indexPath : indexPaths){
                Directory directory = FSDirectory.open(Paths.get(indexPath));

                if (directory != null) {
                    try{
                        indexReader = DirectoryReader.open(directory);
                        ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();
                        searcher = new IndexSearcher(indexReader);
                        SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);

                        while (iterator.hasNext()) {
                            LeafReaderContext lrc = iterator.next();
                            Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                            SegmentReader r = (SegmentReader) lrc.reader();
                            Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);
                            CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);

                            if (spans != null) {
                                while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                                    if (r.numDocs()==r.maxDoc() || r.getLiveDocs().get(spans.docID())) {  
                                        //String transcriptID = r.document(spans.docID()).get(FIELD_TRANSCRIPT_ID_FROM_FILE_NAME);
                                        //System.out.println(transcriptID);
                                        
                                        int hits_aktuell = 0;
                                        while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {    
                                            
                                            List<CodecSearchTree.MtasTreeHit<String>> terms0 = mtasCodecInfo
                                            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                 spans.docID(), TOKEN_IDS, spans.startPosition(),
                                                   (spans.endPosition() - 1));

                                            if (terms0.size() > 0){  // because of proxy pauses in the speaker-based mode, here term.size=0
                                                List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                                        .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                            spans.docID(), prefixList, spans.startPosition(),
                                                                (spans.endPosition() - 1));
                                                    hits_aktuell++;
                                                    function.write(bw, terms);   
                                            }                                                                               
                                        }   
                                        
                                        if (hits_aktuell > 0 ){
                                            hits=hits+hits_aktuell;
                                            transcripts = transcripts + 1;
                                        }
                                    } 
                                }
                            }
                        }
                    indexReader.close();
                    }catch (IndexNotFoundException ex) {
                        throw new IOException ("Search index could not be found under " + indexPath, ex);
                    }finally {
                        if (indexReader!=null){
                            indexReader.close();
                        }
                    }
                }
            }
            
        }catch(IOException ex){
            throw new IOException ("Error with temporary files when collecting metadata values. " + ex);
        }

        return Collections.singletonMap(transcripts, hits);
    }
    
    /**
    * Executes the search and writes the retrieved values of the specified category ​​​​to the temporary file.
    * <p>
    * This method is used in {@link #searchStatistics()} to get the values of some 
    * special annotation layers like "word", "lemma", "word.type", "transcription" etc. 
    * The full list of these special annotation layers is specified in 
    * {@link #searchStatistics()}.
    * 
    * @param indexPaths the list of absolute paths to the search indices, 
    *                   for example [C:\Users\...\ZuMult\indices\TB_FOLK, C:\Users\...\ZuMult\indices\TB_GWSS]
    * 
    * @param q          the search query already converted into the MtasSpanQuery format
    *                   (for more information see {@link #createQuery()})
    * 
    * @param tempFile   the file to which the values ​​are to be written
    * 
    * @param type       the annotation layer whose values ​​are to be retrieved from the search indices.
    * 
    * <p>
    * <b>Note:</b>
    * It should be the id of one of the annotation layers stored in the search index or "transcription".                  
    * 
    * @throws IOException if stream to file cannot be written or if the search index could not be found under the specified path
    */
    private Map<Integer, Integer> searchValues(ArrayList<String> indexPaths, MtasSpanQuery q, File tempFile, String type) throws IOException{
        
        IndexReader indexReader = null;
        IndexSearcher searcher = null;
        
      //  System.out.println("-------------searchValues for: " + type + "-------------------");
        List<String> metadataPrefixList = getMetadataPrefixList(type);
        
        if (type.equals(METADATA_KEY_MATCH_TRANSCRIPTION)){  
            metadataPrefixList.clear();
            Arrays.asList(Constants.TOKENS).forEach(str -> {
                if (str.equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
                    str = Constants.METADATA_KEY_MATCH_TYPE_WORD;
                }

                if(!str.equals(Constants.ELEMENT_NAME_PAUSE)){
                    metadataPrefixList.add(str);
                }
            });

            metadataPrefixList.add(Constants.METADATA_KEY_MATCH_TYPE_PAUSE_DURATION);
            metadataPrefixList.add(Constants.METADATA_KEY_MATCH_TYPE_PAUSE_TYPE);
        }
                  
        if (type.equals(Constants.ATTRIBUTE_NAME_PHON)){
            metadataPrefixList.clear();
            metadataPrefixList.add(METADATA_KEY_MATCH_TYPE_PHON_HTML);
        }
        
        //System.out.println("-- method: searchValues without FunktionsWrapper"); 
        int transcripts = 0;
        int hits = 0;
        BufferedWriter bw = null;
        try{
            bw = new BufferedWriter(new FileWriter(tempFile, true));
                
            for (String indexPath : indexPaths){
                Directory directory = FSDirectory.open(Paths.get(indexPath));

                if (directory != null) {
                    try{
                        indexReader = DirectoryReader.open(directory);
                        ListIterator<LeafReaderContext> iterator = indexReader.leaves().listIterator();
                        searcher = new IndexSearcher(indexReader);
                        SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);

                        while (iterator.hasNext()) {
                            LeafReaderContext lrc = iterator.next();
                            Spans spans = spanweight.getSpans(lrc, SpanWeight.Postings.POSITIONS);
                            SegmentReader r = (SegmentReader) lrc.reader();
                            Terms t = r.terms(FIELD_TRANSCRIPT_CONTENT);
                            CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);

                            if (spans != null) {
                                while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
                                    if (r.numDocs()==r.maxDoc() || r.getLiveDocs().get(spans.docID())) {
                                        transcripts = transcripts + 1;
                                        while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                                            List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                    spans.docID(), TOKEN_IDS, spans.startPosition(),
                                                        (spans.endPosition() - 1));
                                            
                                            if (terms.size() > 0){
                                                hits++;
                                                ////System.out.println("----------------- START HIT -----------------------");
                                                StringBuilder hit = new StringBuilder(); 
                                                
                                                ArrayList<Integer> startPositions = new ArrayList();
                                                for (CodecSearchTree.MtasTreeHit<String> term : terms){
                                                    startPositions.add(term.startPosition);                                                   
                                                }
                                                Collections.sort(startPositions);
                                              
                                                for (int position : startPositions){                                                
                                                   List<CodecSearchTree.MtasTreeHit<String>> terms2 = mtasCodecInfo
                                                                                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                                                                        spans.docID(), metadataPrefixList, position, position);
                                     
                                                   //ArrayList<MtasTokenString> test = mtasCodecInfo.getObjectsByPosition(FIELD_TRANSCRIPT_CONTENT, spans.docID(), position); 
                                                   //System.out.println(test);

                                                    String form = Constants.EMPTY_TOKEN;
                                                    if (terms2.size() == 1){
                                                        form = CodecUtil.termValue(terms2.get(0).data);
                                                        
                                                        /*String currentType = CodecUtil.termPrefix(terms2.get(0).data);
                                                        /if(!currentType.equals(type)){*/
                                                        if (type.equals(METADATA_KEY_MATCH_TRANSCRIPTION)){
                                                            form=CodecUtil.termPrefix(terms2.get(0).data) +"=>"+ CodecUtil.termValue(terms2.get(0).data);                                                           
                                                        }else{
                                                            form = CodecUtil.termValue(terms2.get(0).data);
                                                        }
                                                    }
                                                    hit.append(form);
                                                    hit.append(Constants.TOKEN_DELIMITER);
                                                }
                                                ////System.out.println("----------------- END HIT -----------------------");
                                                bw.write(hit.toString().replaceAll("\\"+ Constants.TOKEN_DELIMITER +"$", ""));
                                                bw.newLine();
                                            }                                 
                                        }                                      
                                    } 
                                }
                            }
                        }
                    indexReader.close();
                    }catch (IndexNotFoundException ex) {
                        throw new IOException ("Search index could not be found under " + indexPath, ex);
                    }finally {
                        if (indexReader!=null){
                            indexReader.close();
                        }
                    }
                }
            }
            
        }catch(IOException ex){
            throw new IOException ("Error with temporary files when collecting metadata values. " + ex);
        }finally {
            if (bw!=null){
                 bw.close();
            }
        }

        return Collections.singletonMap(transcripts, hits);
    }
    
    /*************************************************************************************************/
    /*                                 SEARCH REPETITIONS                                            */
    /*************************************************************************************************/
    
    @Override
    public SearchEngineResponseHitList searchRepetitions(ArrayList<String> indexPaths, String queryString, String metadataQueryString,
            Integer from, Integer to, Boolean cutoff, IDList metadataIDs, 
            ArrayList<Repetition> repetitions) throws SearchServiceException, IOException{
        
        if(metadataQueryString!=null && !metadataQueryString.isEmpty()){
            throw new SearchServiceException("Parameter 'metadataQueryString' is not supported yet!");
        }

        // check search mode
        for (Repetition repetition: repetitions){
            if(repetition.getType().name().toLowerCase().equals(Constants.METADATA_KEY_MATCH_TYPE_WORD) && 
                    repetition.getSimilarityType().name().toLowerCase().equals(SimilarityTypeEnum.DIFF_PRON)){
                throw new SearchServiceException("You can't search for repetitions in transcription if they should have different pronunciation!");            
            }
        }
        
        try{
           return searchRepetitions(indexPaths, queryString, from, to, cutoff, metadataIDs, repetitions);
        }catch(SeachRepetitionException ex){
            throw new SearchServiceException(ex.getCause().getMessage());                  
        }

    }
    
    private SearchEngineResponseHitList searchRepetitions(ArrayList<String> indexPaths, String queryString,
            Integer from, Integer to, Boolean cutoff, IDList metadataIDs, 
            ArrayList<Repetition> repetitions) throws IOException, SearchServiceException{
        
        IndexReader indexReader = null;
        IndexSearcher searcher = null;
        
        RepetitionSearcher repetitionSearcher = new RepetitionSearcher(indexPaths, from, to, cutoff, repetitions);
        
        for (String indexPath: indexPaths){
            Directory directory;
            directory = FSDirectory.open(Paths.get(indexPath));

            if (directory != null) {

                try{   
                    MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null);
                    indexReader = DirectoryReader.open(directory);              
                    searcher = new IndexSearcher(indexReader);
                    SpanWeight spanweight = ((MtasSpanQuery) q.rewrite(indexReader)).createWeight(searcher, ScoreMode.COMPLETE, 0);
             
                    List<LeafReaderContext> leaves = indexReader.leaves();

                    LinkedBlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();

                    Consumer consumer = new Consumer(linkedQueue, repetitionSearcher);
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
                                                repetitionSearcher.checkForRepetitions(mtasCodecInfo, r.getSegmentName(), spans, transcriptID, linkedQueue);  
                                            }catch (SeachRepetitionException ex){
                                               thread.interrupt();
                                               throw ex;                                               
                                            }
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        
                    });
                    
                    linkedQueue.put(DONE);
                    indexReader.close();

                }catch (IndexNotFoundException ex) {
                    throw new IOException ("Search index could not be found! Please check " + indexPath, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return repetitionSearcher.getResult();
    }
        

    private ArrayList<String> getPositionsOfRepetitions(CodecInfo mtasCodecInfo,
            int docID, int start, int end, String speakerID, 
            ArrayList<Repetition> repetitionSpecifications, 
            Map<String, Map<String, Map<Integer, Set<Integer>>>> positionsWithContext,
            String segmentName) throws SearchServiceException, IOException {

        String currentRepetitionLayer = repetitionSpecifications.get(0).getType().name().toLowerCase();
        
        List<String> currentPrefixListRepetitionLayer = new ArrayList<String>();
        currentPrefixListRepetitionLayer.add(currentRepetitionLayer);
        
        // get forms from the layer (word, lemma, norm) specified for repetitions                                     
        List<CodecSearchTree.MtasTreeHit<String>> termsFromCurrentRepetitiionLayer = mtasCodecInfo
            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
            docID, currentPrefixListRepetitionLayer, start,
            (end - 1));
        
        if (termsFromCurrentRepetitiionLayer.size() > 0){

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
                SimilarityTypeEnum similarity = repetitionSpecifications.get(0).getSimilarityType();
                
                Boolean ignoreFunctionWords = repetitionSpecification.ignoreFunctionalWords();
                
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
                int maxDistance = MAX_REPETITION_CONTEXT;
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
                    maxDistance + sourceSize, currentPrefixListRepetitionLayer, ignoreFunctionWords);

                // iterate through arrayOfTheFollowingWordTokens/positionsOfTheFollowingWordTokens
                List<Integer> positionsOfTheFollowingWordTokens = new ArrayList<>(arrayOfTheFollowingWordTokens.keySet());
                while(cursorPosition<=(positionsOfTheFollowingWordTokens.size()-sourceSize)){
                                                             
                    Integer firstPosition = positionsOfTheFollowingWordTokens.get(cursorPosition);
                                                            
                    // get n-gram
                    SortedMap<Integer, String> sortedMap = new TreeMap();
                    
                    
                    switch(similarity){
                        case EQUAL:
                        case FUZZY:
                        case FUZZY_PLUS:
                        case DIFF_PRON:
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
                                                
                            break;
                        default:
                    }
                  
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
                           }
                           break;
                        case DIFF_PRON:
                            if(possibleRepetitionStr.equals(sourceStr)){
                                // get pronunciation values
                                if(!rs.containsLayer(Constants.METADATA_KEY_MATCH_TYPE_WORD)){
                                    //create new source string
                                    List<CodecSearchTree.MtasTreeHit<String>> termsTrans = mtasCodecInfo
                                         .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                         docID, prefixListTRANS, start,
                                         (end - 1));

                                    rs.addNewLayer(Constants.METADATA_KEY_MATCH_TYPE_WORD, termsTrans);
                                }
                               
                                String sourcePron = rs.getStringForLayer(Constants.METADATA_KEY_MATCH_TYPE_WORD);
                               
                               
                                //create new repetition string
                                StringBuilder sb = new StringBuilder();
                                for(Integer position: sortedMap.keySet()){
                                    List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                                        .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                                            docID, prefixListTRANS, position, position);
                                    sb.append(CodecUtil.termValue(terms.get(0).data));
                                    sb.append(" ");
                                }
                                   
                                String repetitionPron = sb.toString().trim();
                                    
                                // compare pronunciation
                                if(repetitionPron.equals(sourcePron)){
                                    isRepetition=false;
                                }else{
                                    // this can be a repetition
                                    isRepetition=true;
                                }                                
                            }
                           break;
                        case FUZZY:
                            if(!possibleRepetitionStr.equals(sourceStr)){                          
                                isRepetition = isFuzzyRepetition(rs.getStringObjectsForLayer(currentRepetitionLayer), sortedMap);
                            }
                            break;
                            
                        case FUZZY_PLUS:
                            isRepetition = isFuzzyRepetition(rs.getStringObjectsForLayer(currentRepetitionLayer), sortedMap);
                            break;
                        default:
                    }

                        
                    if(isRepetition && checkConditions(sortedMap, speakerID, segmentName, positionsWithContext, firstPosition,
                                            mtasCodecInfo, docID, end, repetitionSpecification)){

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
        
        ArrayList<String> getPositions(){
            ArrayList<String> positions = new ArrayList();
            layerMap2.entrySet().iterator().next().getValue().keySet().forEach(position -> {              
                    positions.add(String.valueOf(position));
                });
            return positions;
        }
        
    }
        
    private boolean isFuzzyRepetition(SortedMap<Integer, String> source, SortedMap<Integer, String> possibleRepetitions){
        Object[] sourceList = source.values().toArray();
        Object[] possibleRepetitionList = possibleRepetitions.values().toArray();
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
    
    private Boolean checkConditions(SortedMap<Integer, String> sortedMap, String speakerID, String segmentName,
            Map<String, Map<String, Map<Integer, Set<Integer>>>> positionsWithContext, Integer firstPosition,
            CodecInfo mtasCodecInfo, int docID, int end, Repetition repetitionSpecification) throws IOException, SearchServiceException{
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
                overlapCheck = checkPositionToOverlap(mtasCodecInfo, docID, sortedMap,
                    repetitionSpecification.getPositionOverlap());
            }else{
                overlapCheck=true;
            }
        }
                        
        if(overlapCheck){
            // check context
            String str = repetitionSpecification.getPrecededby();
            if(str!=null && !str.isEmpty()){
                contextCheck = checkContext(positionsWithContext, str, segmentName, docID, sortedMap.firstKey());
            }else{
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
    
    private Boolean checkContext(Map<String, Map<String, Map<Integer, Set<Integer>>>> positionsWithContext, 
            String context, String segmentName, int docID, int position){
        Boolean check = false;
        if(positionsWithContext.containsKey(context)){
            Map<String, Map<Integer, Set<Integer>>> segmentNames = positionsWithContext.get(context);
            if(segmentNames.containsKey(segmentName)){
                Map<Integer, Set<Integer>> docIDs = segmentNames.get(segmentName);
                if(docIDs.containsKey(docID)){
                    Set<Integer> positions = docIDs.get(docID);
                    if(positions.contains(position)){
                        check = true;
                    }
                }
            }
        }
        return check;
    }
    
    private Map<String, Map<String, Map<Integer, Set<Integer>>>> getPositionsForContext(ArrayList<Repetition> repetitions, ArrayList<String> indexPaths) throws SearchServiceException{
        Map<String, Map<String, Map<Integer, Set<Integer>>>> result = new HashMap();

        for(Repetition repetition: repetitions){
            String precededBy = repetition.getPrecededby();
            Boolean within = repetition.isWithinSpeakerContribution();
            
            if(precededBy!=null && !precededBy.isEmpty()){
                String queryString = "<word/> precededby " + precededBy;
                if(within){
                    //queryString = "(" + queryString + ") within <annotationBlock/>";
                    queryString = "(" + precededBy + "<word/>) within <annotationBlock/>";
                }
                
                for (String indexPath: indexPaths){
                    Directory directory;
                    try {
                        directory = FSDirectory.open(Paths.get(indexPath));
                        if (directory != null) {
                            try (IndexReader indexReader = DirectoryReader.open(directory)) {
                                IndexSearcher searcher = new IndexSearcher(indexReader);
                                MtasSpanQuery q = createQuery(FIELD_TRANSCRIPT_CONTENT, queryString, null, null);
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
                                                    int position = spans.endPosition()-1;

                                                    Set<Integer> set = new HashSet();
                                                    Map<Integer, Set<Integer>> map = new HashMap();
                                                    Map<String, Map<Integer, Set<Integer>>> mainMap = new HashMap();

                                                    if(result.containsKey(precededBy)){
                                                        mainMap = result.get(precededBy);
                                                        if(mainMap.containsKey(segmentName)){
                                                            map = mainMap.get(segmentName);
                                                            if(map.containsKey(docID)){
                                                                set = map.get(docID);
                                                            }
                                                        }
                                                    }
                                                    set.add(position);
                                                    map.put(docID, set);
                                                    mainMap.put(segmentName, map);
                                                    result.put(precededBy, mainMap);
                                                }
                                            }
                                        }
                                    }
                                }               

                            }catch (IOException ex) {
                                Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

       return result;
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
    
    private int isFunctionalWord(CodecInfo mtasCodecInfo, int docID, int currentPosition) throws IOException{
        
        List<CodecSearchTree.MtasTreeHit<String>> termsPOS = mtasCodecInfo
            .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                docID, prefixListPOS, currentPosition,
                currentPosition);

        if(!Arrays.asList(NOT_COUNT).contains(CodecUtil.termValue(termsPOS.get(0).data))){
            return 1;
        }else{
            return 0;
        }
    }
    
    private TreeMap<Integer, String> addTokens(TreeMap<Integer, String> wordTokenMap, CodecInfo mtasCodecInfo, int docID, int startPosition,
            int maxNumber, List<String> annotationLayerPrefix, Boolean ignoreFunctionWords) throws IOException{

        int wordTokenIndex=1;
        TreeMap<Integer, String> map = new TreeMap();
        
        // get max repetition context
        List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                .getPositionedTermsByPrefixesAndPositionRange(FIELD_TRANSCRIPT_CONTENT,
                docID, annotationLayerPrefix, (startPosition+1),
                (startPosition+1+MAX_REPETITION_CONTEXT));
        
        // sort
        terms.forEach(term -> {
            map.put(term.startPosition, CodecUtil.termValue(term.data));
        });
        
        // reduce repetition context according to maxNumber
        for (Map.Entry<Integer,String> entry : map.entrySet()){
            if (wordTokenIndex<=maxNumber){               
                wordTokenMap.put(entry.getKey(), entry.getValue());
                if (ignoreFunctionWords){
                    // functional words should not be count
                    wordTokenIndex = wordTokenIndex + isFunctionalWord(mtasCodecInfo, docID, entry.getKey());
                }else{
                    wordTokenIndex++;
                }
            }else{
                break;
            }     
        }

        //System.out.println(wordTokenMap);
        return wordTokenMap;
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
        Map<String, Map<String, Map<Integer, Set<Integer>>>> positionsWithContext;
        
        RepetitionSearcher(ArrayList<String> indexPaths, Integer from, Integer to, Boolean cutoff, ArrayList<Repetition> repetitionSpecifications) throws SearchServiceException {
            this.indexPaths = indexPaths;
            this.from = from;
            this.to = to;
            this.cutoff = cutoff;
            this.repetitionSpecifications = repetitionSpecifications;
            this.positionsWithContext = getPositionsForContext(repetitionSpecifications, indexPaths);
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
                Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }finally {
                if (br!=null){
                    try {
                        br.close();
                    } catch (IOException ex) {
                        Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
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
                LinkedBlockingQueue<String> linkedQueue) throws IOException {
            
            ThreadLocal threadLocal = new ThreadLocal();
            threadLocal.set(false);  // will be true at the end if there is at least one repetition in this transcript
            
            List<Object[]> arrayList = new ArrayList();
            while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {              
                arrayList.add( new Object[]{spans.docID(), spans.startPosition(), spans.endPosition()} );
            }
            arrayList.forEach( obj -> {
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
                                            docID, start, end, speakerID, repetitionSpecifications, positionsWithContext, segmentName);
                                }catch (SearchServiceException ex) {
                                    throw new SeachRepetitionException(ex);
                                }                  
                                
                                if(found!=null){
                                                 
                                    threadLocal.set(true);
                                    hits_total++;
                                    
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
                        Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                });
                 
        
            if(  (Boolean)  threadLocal.get()  )  {
                transcripts_total++;
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
                    Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
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
                    Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            reader.readFrom(sort(tempFile));
        }
        
        private void process(String take) {
            try {
                bw.write(take + "\n");
            } catch (IOException ex) {
                Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private File sort(File f){
        TextFileSorter sorter = new TextFileSorter(new SortConfig().withMaxMemoryUsage(20 * 1000 * 1000));
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("tmp", ".txt");
            tmpFile.deleteOnExit();
            InputStream input1 = new FileInputStream(f.getAbsolutePath());
            sorter.sort(input1, new FileOutputStream(tmpFile.getAbsolutePath()));
            f.delete();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);    
        } catch (IOException ex) {
            Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
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
            fs = o.fuzzyScore(s2, s1);;
        }
        return fs;
    }
    

}