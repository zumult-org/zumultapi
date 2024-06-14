/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.search;

import java.io.File;
import java.io.FileFilter;
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
import org.zumult.query.searchEngine.COMASearchEngine;
import org.zumult.query.searchEngine.MTASBasedSearchEngine;
import org.zumult.query.searchEngine.SearchIndex;

/**
 *
 * @author Elena
 */
public class SearchIndexer implements Indexer {
    
    private String MTAS_CONFIG_FILE_PATH = "src\\main\\java\\org\\zumult\\query\\searchEngine\\parser\\config";
    private String MTAS_CONFIG_FILE_NAME = "demo_mtas_config_SB.xml";
    private String INDEX_PATH = "D:\\ZUMULT\\INDICES";
    private String INDEX_NAME = "SB_EXMARaLDA-Demokorpus-Small";
    private static String CORPUS_TOP_LEVEL_FOLDER = "D:\\ZUMULT\\EXMARaLDA-DemoKorpus-Small";
    
    private String[] INPUT_DIRECTORIES =
        {
            //"C:\\Users\\Frick\\IDS\\ZuMult\\data\\input\\FOLK", 
            //"C:\\Users\\Frick\\IDS\\ZuMult\\data\\input\\GWSS"
            //"C:\\Users\\Frick\\IDS\\ZuMult\\data\\output_SB_FOLK_14_07_2022" // 12.07.2022
            //"C:\\Users\\bernd\\Dropbox\\work\\2021_MARGO_TEXAS_GERMAN\\ZUMULT\\TGDP\\1-20-1"
        };

    
    // New 29-12-2022: Make this configurable from the outside
    public SearchIndexer(String mtasConfigFilePath, String mtasConfigFileName, String indexPath, String indexName, String[] inputDirectories) {
        MTAS_CONFIG_FILE_PATH = mtasConfigFilePath;
        MTAS_CONFIG_FILE_NAME = mtasConfigFileName;
        INDEX_PATH = indexPath;
        INDEX_NAME = indexName;
        INPUT_DIRECTORIES = inputDirectories;        
    }

    public SearchIndexer() {
    }


    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            File[] INPUT_DIRECTORY_FILES = new File(CORPUS_TOP_LEVEL_FOLDER).listFiles(new FileFilter(){
                @Override
                public boolean accept(File pathname) {
                    System.out.println(pathname.getAbsolutePath());
                    return pathname.isDirectory();
                }
                
            });    
            String[] MY_INPUT_DIRECTORIES = new String[INPUT_DIRECTORY_FILES.length];
            for (int i=0; i<MY_INPUT_DIRECTORIES.length; i++){
                MY_INPUT_DIRECTORIES[i] = INPUT_DIRECTORY_FILES[i].getAbsolutePath();
            }
            SearchIndexer searchIndexer = new SearchIndexer();
            searchIndexer.INPUT_DIRECTORIES = MY_INPUT_DIRECTORIES; 
            searchIndexer.index();
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
        
        MTASBasedSearchEngine index = new COMASearchEngine();
        String path = new File(INDEX_PATH, INDEX_NAME).getAbsolutePath();
        String mtasConfigPath = new File(MTAS_CONFIG_FILE_PATH, MTAS_CONFIG_FILE_NAME).getAbsolutePath();
        SearchIndex searchIndex = index.createIndex(inputDirectories, path, mtasConfigPath);

        System.out.println("Done!");
        System.out.println(searchIndex.getName() + " created unter: " + INDEX_PATH);
        System.out.println(searchIndex.getNumberOfIndexedDocuments()+ " documents that have been indexed");

        final long timeEnd = System.currentTimeMillis();
        long millis = timeEnd - timeStart;
        System.out.println("Indexing time: " + TimeUtilities.format(millis));
    }

}
