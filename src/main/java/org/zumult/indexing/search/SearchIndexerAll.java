/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.search;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.indexing.Indexer;
import org.zumult.io.Constants;
import org.zumult.io.TimeUtilities;
import org.zumult.query.implementations.DGD2SearchIndexTypeEnum;
import org.zumult.query.searchEngine.MTASBasedSearchEngine;
import org.zumult.query.searchEngine.SearchIndex;

/**
 *
 * @author Elena
 */
public class SearchIndexerAll implements Indexer {
    private static final String MTAS_CONFIG_FILE_PATH = "src\\main\\java\\org\\zumult\\query\\searchEngine\\parser\\config";
    private static final String INDEX_PATH = "C:\\Users\\Frick\\IDS\\ZuMult\\indicesTest";
    private static final String INPUT_DIR_SPEAKER_BASED = "C:\\Users\\Frick\\IDS\\ZuMult\\data\\output_SB";
    private static final String INPUT_DIR_TRANSCRIPT_BASED = "C:\\Users\\Frick\\IDS\\ZuMult\\data\\output_TB";   
    
    Set<String> CORPORA = new HashSet<>(Arrays.asList(
            "HMOT" /*, "HMAT", "MEND", "DNAM", "ZW--"*/));
    
    Set<String> CORPORA_WITH_PUNCTUATION = new HashSet<>(Arrays.asList(
           ));
                        
    Map<DGD2SearchIndexTypeEnum, String> OUTPUT_NAME_PREFIXES = new HashMap<DGD2SearchIndexTypeEnum, String>(){{
        put(DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX, "SB_");
        put(DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX, "TB_");
        put(DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX_WITHOUT_PUNCT, "SB_");
        put(DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT, "TB_");
    }};
    
    Map<DGD2SearchIndexTypeEnum, String> MTAS_CONFI_FILES  = new HashMap<DGD2SearchIndexTypeEnum, String>() {{
        put(DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX, "mtas_config_SB.xml");
        put(DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX, "mtas_config_TB.xml");
        put(DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX_WITHOUT_PUNCT, "mtas_config_SB_without_Punct.xml");
        put(DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT, "mtas_config_TB_without_Punct.xml");
    }};
    
    Map<DGD2SearchIndexTypeEnum, String> INPUT_PATH = new HashMap<DGD2SearchIndexTypeEnum, String>(){{
        put(DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX, INPUT_DIR_SPEAKER_BASED);
        put(DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX, INPUT_DIR_TRANSCRIPT_BASED);
        put(DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX_WITHOUT_PUNCT, INPUT_DIR_SPEAKER_BASED);
        put(DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT, INPUT_DIR_TRANSCRIPT_BASED);
    }};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {   
        try {
            new SearchIndexerAll().index();
        } catch (IOException ex) {
            Logger.getLogger(SearchIndexerAll.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    @Override
    public void index() throws IOException {
        final long timeStart = System.currentTimeMillis();
        Map<String, Integer> info = new HashMap();
        if (INDEX_PATH != null && !INDEX_PATH.isEmpty()) {   
            for (String corpusID: CORPORA){
                info = indexCorpus(corpusID, DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX, "", info);
                info = indexCorpus(corpusID, DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX, "", info);
            }

            for (String corpusID: CORPORA_WITH_PUNCTUATION){
                info = indexCorpus(corpusID, DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX, "_" + Constants.WITH_PUNCTUTION_EXT, info);
                info = indexCorpus(corpusID, DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX, "_" + Constants.WITH_PUNCTUTION_EXT, info);
                info = indexCorpus(corpusID, DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX_WITHOUT_PUNCT, "_" + Constants.WITHOUT_PUNCTUTION_EXT, info);
                info = indexCorpus(corpusID, DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT, "_" + Constants.WITHOUT_PUNCTUTION_EXT, info);
            }

            System.out.println("Done!");
            System.out.println("Indices created unter: " + INDEX_PATH);
            System.out.println("(index name: number of indexed documents)");
            for(String path: info.keySet()){
                 System.out.println(path + ": "+ info.get(path));
            }
            final long timeEnd = System.currentTimeMillis();
            long millis = timeEnd - timeStart;
            System.out.println("Indexing time: " + TimeUtilities.format(millis));
        }else {
            throw new IllegalArgumentException("You have not specified the directory for the index");
        }
    }
    
    Map<String, Integer> indexCorpus(String corpusID, DGD2SearchIndexTypeEnum indexType, String outputNameSuffix, Map<String, Integer> info) throws IOException{
        
        String inputPath = INPUT_PATH.get(indexType) + "\\" + corpusID.replace("-", "");
        Set<Path> inputDirectories = new HashSet();
        Path path = Paths.get(inputPath);
        inputDirectories.add(path);
        
        String mtasConfigFile = MTAS_CONFI_FILES.get(indexType);
        String configPath = MTAS_CONFIG_FILE_PATH + "\\" + mtasConfigFile;
        
        String outputNamePrefix = OUTPUT_NAME_PREFIXES.get(indexType);
        String indexPath = INDEX_PATH + "\\" + outputNamePrefix + corpusID.replace("-", "") + outputNameSuffix;
                
        MTASBasedSearchEngine index = new MTASBasedSearchEngine();
        SearchIndex searchIndex = index.createIndex(inputDirectories, indexPath, configPath);
        info.put(searchIndex.getName(), searchIndex.getNumberOfIndexedDocuments());
        return info;
    }

}
