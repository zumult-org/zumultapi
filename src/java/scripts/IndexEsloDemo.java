/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package scripts;

import java.io.IOException;
import org.zumult.indexing.search.SearchIndexer;

/**
 *
 * @author bernd
 */
public class IndexEsloDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new IndexEsloDemo().doit();
    }

    private void doit() throws IOException {
        String[] inputDirectories = new String[] {"C:\\Users\\bernd\\Dropbox\\work\\ZUMULT-COMA\\ESLO-DEMO\\iso-transcripts"};
        SearchIndexer searchIndexer = new SearchIndexer(
                "C:\\zumult\\zumultapi\\src\\java\\org\\zumult\\query\\searchEngine\\parser\\config", 
                "eslo_mtas_config_SB.xml", 
                "C:\\Users\\bernd\\Dropbox\\work\\ZUMULT-COMA\\ESLO-DEMO\\MTAS_INDEX_SB",
                "SB_ESLO-DEMO",
                inputDirectories
        );
        
        searchIndexer.index();
    }
    
}
