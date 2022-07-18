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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.indexing.Indexer;
import org.zumult.io.TimeUtilities;
import org.zumult.query.searchEngine.MTASBasedSearchEngine;
import org.zumult.query.searchEngine.SearchIndex;

/**
 *
 * @author Elena
 */
public class SearchIndexer implements Indexer {
    
    private static final String MTAS_CONFIG_FILE_PATH = "src\\java\\org\\zumult\\query\\searchEngine\\parser\\config";
    private static final String MTAS_CONFIG_FILE_NAME = "mtas_config_SB.xml";
    private static final String INDEX_PATH = "C:\\Users\\Frick\\IDS\\ZuMult\\indicesTest";
    private static final String INDEX_NAME = "SB_FOLK";
    public static final String[] INPUT_DIRECTORIES = 
        {
            //"C:\\Users\\Frick\\IDS\\ZuMult\\data\\input\\FOLK", 
            //"C:\\Users\\Frick\\IDS\\ZuMult\\data\\input\\GWSS"
            "C:\\Users\\Frick\\IDS\\ZuMult\\data\\output_SB_FOLK_14_07_2022" // 12.07.2022
        };
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new SearchIndexer().index();
        } catch (IOException ex) {
            Logger.getLogger(SearchIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void index() throws IOException {
        final long timeStart = System.currentTimeMillis();
        
        Set<Path> inputDirectories = new HashSet();  
        Arrays.asList(INPUT_DIRECTORIES).stream().map(str -> Paths.get(str)).forEachOrdered(path -> {
            inputDirectories.add(path);
        });
        
        MTASBasedSearchEngine index = new MTASBasedSearchEngine();
        SearchIndex searchIndex = index.createIndex(inputDirectories, INDEX_PATH + "\\"+ INDEX_NAME, MTAS_CONFIG_FILE_PATH + "\\" +MTAS_CONFIG_FILE_NAME);

        System.out.println("Done!");
        System.out.println(searchIndex.getName() + " created unter: " + INDEX_PATH);
        System.out.println(searchIndex.getNumberOfIndexedDocuments()+ " documents that have been indexed");

        final long timeEnd = System.currentTimeMillis();
        long millis = timeEnd - timeStart;
        System.out.println("Indexing time: " + TimeUtilities.format(millis));
    }

}
