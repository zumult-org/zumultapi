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
public class DGD2KWIC<T> extends AbstractKWIC {
    
    private static final String DEFAULT_RIGHT_CONTEXT_ITEM = 
            Constants.KWIC_DEFAULT_CONTEXT_ITEM;
    private static final String DEFAULT_LEFT_CONTEXT_ITEM = 
            Constants.KWIC_DEFAULT_CONTEXT_ITEM;
    private static final int DEFAULT_RIGHT_CONTEXT_LENGTH = 
            Constants.KWIC_DEFAULT_CONTEXT_LENGTH;
    private static final int DEFAULT_LEFT_CONTEXT_LENGTH = 
            Constants.KWIC_DEFAULT_CONTEXT_LENGTH;

    private final String type;
    private final SearchResultPlus searchResult;
    private T kwicSnippets;

    public DGD2KWIC(SearchResultPlus searchResult, String context, String type)
            throws SearchServiceException, IOException{
        this.type=type;
        this.searchResult = searchResult;
        setContext(context);
        if(type.equals(Constants.SEARCH_TYPE_DOWNLOAD)){
            this.createKWICSnippets("xml");      
        }else{
            this.createKWICSnippets();
        }      
    }
    
    public DGD2KWIC(SearchResultPlus searchResult, String context, 
                    String type, String fileType) 
                        throws SearchServiceException, IOException{
        this.type=type;
        this.searchResult = searchResult;
        setContext(context);
        this.createKWICSnippets(fileType);
    }
    
    private void createKWICSnippets(String fileType) 
            throws IOException, SearchServiceException{
        
        SearchResultSerializer searchResultSerializer = 
                new SearchResultSerializer();
        setKWICSnippets((T) searchResultSerializer.
                createKWICDownloadFileWithThreads(this, fileType));
        //setKWICSnippets((T) searchResultSerializer.createKWICDownloadFile(this, fileType));
        
    }
    
    private void createKWICSnippets() throws IOException, SearchServiceException{
            ArrayList arrayList = new ArrayList<>();
            ISOTEIKWICSnippetCreator creator = new ISOTEIKWICSnippetCreator();
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

    private String checkItemSyntax(String str) throws SearchServiceException{
        switch (str) {
            case Constants.KWIC_CONTEXT_ITEM_FOR_TOKEN, 
                 Constants.KWIC_CONTEXT_ITEM_FOR_CHARACTERS -> {
                return str;
            }
            default -> throw new SearchServiceException(str 
                    + " is not a supported context. "
                    + "The correct pattern is : "
                    + "'context=3-t,3-t' or 'context=3-c,3-c'");
        }
    }
    
    private void setContext(String context) throws SearchServiceException{
        int rightContextLength = DEFAULT_RIGHT_CONTEXT_LENGTH;
        int leftContextLength = DEFAULT_LEFT_CONTEXT_LENGTH;
        String rightContextItem = DEFAULT_RIGHT_CONTEXT_ITEM;
        String leftContextItem = DEFAULT_LEFT_CONTEXT_ITEM;  
        if (context != null && !context.isEmpty()){
                try{
                    String[] ct = context.split(Constants.KWIC_LEFT_RIGHT_CONTEXT_DELIMITER);
                    String[] lc = ct[0].split(Constants.KWIC_CONTEXT_DELIMITER);
                    String[] rc = ct[1].split(Constants.KWIC_CONTEXT_DELIMITER);

                    leftContextItem = checkItemSyntax(lc[1]);
                    rightContextItem = checkItemSyntax(rc[1]);
                    
                    if (leftContextItem.equals(DEFAULT_LEFT_CONTEXT_ITEM)){
                        if (Integer.valueOf(lc[0]) > Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX){
                            leftContextLength = Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX;
                        }else if (Integer.valueOf(lc[0]) >= 0){
                            leftContextLength = Integer.valueOf(lc[0]);
                        }
                    }else{
                        throw new SearchServiceException("Please specify the context in tokens! Characters are not supported yet.");
                    }

                    if (rightContextItem.equals(DEFAULT_RIGHT_CONTEXT_ITEM)){
                        if (Integer.valueOf(rc[0]) > Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX){
                            rightContextLength = Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX;
                        }else if (Integer.valueOf(rc[0]) >= 0){
                            rightContextLength = Integer.valueOf(rc[0]);
                        }                   
                    }else{
                        throw new SearchServiceException("Please specify the context in tokens! Characters are not supported yet.");
                    } 
                }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
                    throw new SearchServiceException("Please check the context syntax. The correct pattern is : 'context=3-t,3-t' or 'context=3-c,3-c'");
                }
        }
        
        super.setRightContext(rightContextItem, rightContextLength);
        super.setLeftContext(leftContextItem, leftContextLength);
    }


}
