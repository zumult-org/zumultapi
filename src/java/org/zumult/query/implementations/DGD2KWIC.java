/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import org.zumult.io.Constants;
import org.zumult.query.Hit;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.KWIC;
import org.zumult.query.KWICSnippet;
import org.zumult.query.MetadataQuery;
import org.zumult.query.Pagination;
import org.zumult.query.SearchQuery;
import org.zumult.query.serialization.SearchResultSerializer;
import org.zumult.query.AdditionalSearchConstraint;

/**
 *
 * @author Elena
 */
public class DGD2KWIC<T> extends DGD2AbstractKWIC implements KWIC {            

    private final String type;
    private final SearchResultPlus searchResult;
    private T kwicSnippets;

    public DGD2KWIC(SearchResultPlus searchResult, String context, String type) throws SearchServiceException, IOException{
        this.type=type;
        this.searchResult = searchResult;
        super.setContext(context);
        if(type.equals(Constants.SEARCH_TYPE_DOWNLOAD)){
            this.createKWICSnippets("xml");      
        }else{
            this.createKWICSnippets();
        }      
    }
    
    public DGD2KWIC(SearchResultPlus searchResult, String context, String type, String fileType) throws SearchServiceException, IOException{
        this.type=type;
        this.searchResult = searchResult;
        super.setContext(context);
        this.createKWICSnippets(fileType);
    }
    
    private void createKWICSnippets(String fileType) throws IOException, SearchServiceException{
        SearchResultSerializer searchResultSerializer = new SearchResultSerializer();
        setKWICSnippets((T) searchResultSerializer.createKWICDownloadFileWithThreads(this, fileType));
        //setKWICSnippets((T) searchResultSerializer.createKWICDownloadFile(this, fileType));
        
    }
    
    private void createKWICSnippets() throws IOException, SearchServiceException{
            ArrayList arrayList = new ArrayList<>();
            DGD2KWICSnippetCreator creator = new DGD2KWICSnippetCreator();
            for (Hit row : (ArrayList<Hit>) searchResult.getHits()){
                    KWICSnippet snippetObj = creator.apply(row.getDocId(), row.getFirstMatch().getID(), row.getMatches(), getLeftContext(), getRightContext());
                    arrayList.add(snippetObj);
            }
            setKWICSnippets((T) arrayList);
    }
    
    @Override
    public String toXML(){
        if(type.equals(Constants.SEARCH_TYPE_DOWNLOAD)){
             SearchResultSerializer searchResultSerializer = new SearchResultSerializer();
            return searchResultSerializer.displayKWICExportInXML(this);
        }else{
            SearchResultSerializer searchResultSerializer = new SearchResultSerializer();
            return searchResultSerializer.displayKWICinXML(this);
        }
    }


    @Override
    public T getKWICSnippets() {
        return this.kwicSnippets;
    }
    
    public void setKWICSnippets(T kwicSnippets){
        this.kwicSnippets = kwicSnippets;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ArrayList<Hit> getHits() {
        return this.searchResult.getHits();
    }

    @Override
    public Pagination getPagination() {
        return this.searchResult.getPagination();
    }

    @Override
    public Boolean getCutoff() {
        return this.searchResult.getCutoff();
    }
    
    @Override
    public long getSearchTime(){
        return this.searchResult.getSearchTime();
    }

    @Override
    public int getTotalHits() {
        return this.searchResult.getTotalHits();
    }

    @Override
    public int getTotalTranscripts() {
        return this.searchResult.getTotalTranscripts();
    }

    @Override
    public SearchQuery getSearchQuery() {
        return this.searchResult.getSearchQuery();
    }

    @Override
    public MetadataQuery getMetadataQuery() {
        return this.searchResult.getMetadataQuery();
    }

    @Override
    public String getSearchMode() {
        return this.searchResult.getSearchMode();
    }

    @Override
    public ArrayList<AdditionalSearchConstraint> getAdditionalSearchConstraints() {
        return this.searchResult.getAdditionalSearchConstraints();
    }



}
