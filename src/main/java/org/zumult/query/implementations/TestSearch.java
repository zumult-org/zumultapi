/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.query.Hit;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;
import org.zumult.query.Searcher;

/**
 *
 * @author bernd
 */
public class TestSearch {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        try {
            new TestSearch().doit();
        } catch (SearchServiceException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TestSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws SearchServiceException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
        Searcher searcher = backend.getSearcher();
        searcher.setQuery("[word=\"in\"]", null, null);
        searcher.setPagination(Integer.MAX_VALUE, 0);
        searcher.setCollection("manv_corpus", null);
        //SearchResult searchResult = searcher.search("SB_manv_corpus");
        SearchResultPlus searchResultPlus = searcher.search("SB_manv_corpus", Boolean.TRUE, null);
        ArrayList<Hit> hits = searchResultPlus.getHits();
        System.out.println(searchResultPlus.getTotalHits() + " hits, but " + searchResultPlus.getHits().size() + " hits.");
        for (Hit hit : hits){
            String docID = hit.getDocId();
            String firstMatch = hit.getFirstMatch().getID();
            String lastMatch = hit.getLastMatch().getID();
            System.out.println(docID + " / "+ firstMatch + " / " + lastMatch);
        }
    }
    
}
