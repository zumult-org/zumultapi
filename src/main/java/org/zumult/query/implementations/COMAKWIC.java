/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.query.AdditionalSearchConstraint;
import org.zumult.query.Hit;
import org.zumult.query.MetadataQuery;
import org.zumult.query.Pagination;
import org.zumult.query.SearchQuery;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;
import org.zumult.query.serialization.DefaultQuerySerializer;

/**
 *
 * @author Frick
 */
public class COMAKWIC<T> extends AbstractKWIC {

    private final String type;
    private final SearchResultPlus searchResult;
    private T kwicSnippets;
    
    private static final String DEFAULT_RIGHT_CONTEXT_ITEM = Constants.KWIC_DEFAULT_CONTEXT_ITEM;
    private static final String DEFAULT_LEFT_CONTEXT_ITEM = Constants.KWIC_DEFAULT_CONTEXT_ITEM;
    private static final int DEFAULT_RIGHT_CONTEXT_LENGTH = Constants.KWIC_DEFAULT_CONTEXT_LENGTH;
    private static final int DEFAULT_LEFT_CONTEXT_LENGTH = Constants.KWIC_DEFAULT_CONTEXT_LENGTH;
    
    
    public COMAKWIC (SearchResultPlus searchResult, String context, String type)
            throws SearchServiceException, IOException{
        this.type=type;
        this.searchResult = searchResult;
        setContext(context);
        this.createKWICSnippets("xml");      
    }
    
    public COMAKWIC (SearchResultPlus searchResult, String context, 
                    String type, String fileType) 
                        throws SearchServiceException, IOException{
        this.type=type;
        this.searchResult = searchResult;
        setContext(context);
        this.createKWICSnippets(fileType);
    }
    
    public void setKWICSnippets(T kwicSnippets){
        this.kwicSnippets = kwicSnippets;
    }
    
    @Override
    public Object getKWICSnippets() {
        return kwicSnippets;
    }

    @Override
    public String getType() {
        return type;
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
                    } else {
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

    
    
    private void createKWICSnippets(String fileType) throws IOException, SearchServiceException{      
        try {
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();     
            DefaultQuerySerializer searchResultSerializer = new DefaultQuerySerializer();
            setKWICSnippets((T) searchResultSerializer.createKWICDownloadFile(this, fileType, backendInterface));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DGD2KWIC.class.getName()).log(Level.SEVERE, null, ex);
            // better throw this, or not (?)
            throw new IOException(ex);
        }           
    }



    @Override
    public String toXML() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    // the following methods just seem to be passing through properties of the searchResult    
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
    

    
}
