/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.objects.Transcript;
import org.zumult.query.AdditionalSearchConstraint;
import org.zumult.query.Hit;
import org.zumult.query.KWICSnippet;
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
    
    BackendInterface backendInterface;
    
    
    
    public COMAKWIC (SearchResultPlus searchResult, String context, String type) throws SearchServiceException, IOException{
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(COMAKWIC.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        this.type=type;
        this.searchResult = searchResult;
        setContext(context);
        if(type.equals(Constants.SEARCH_TYPE_DOWNLOAD)){
            this.createKWICSnippets("xml");      
        } else{
            this.createKWICSnippets();
        }      
    }
    
    public COMAKWIC (SearchResultPlus searchResult, String context, String type, String fileType) throws SearchServiceException, IOException{
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(COMAKWIC.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
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
                        if (Integer.parseInt(lc[0]) > Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX){
                            leftContextLength = Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX;
                        }else if (Integer.parseInt(lc[0]) >= 0){
                            leftContextLength = Integer.parseInt(lc[0]);
                        }
                    } else {
                        throw new SearchServiceException("Please specify the context in tokens! Characters are not supported yet.");
                    }

                    if (rightContextItem.equals(DEFAULT_RIGHT_CONTEXT_ITEM)){
                        if (Integer.parseInt(rc[0]) > Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX){
                            rightContextLength = Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX;
                        } else if (Integer.parseInt(rc[0]) >= 0){
                            rightContextLength = Integer.parseInt(rc[0]);
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
        DefaultQuerySerializer searchResultSerializer = new DefaultQuerySerializer(); // better throw this, or not (?)
        setKWICSnippets((T) searchResultSerializer.createKWICDownloadFile(this, fileType, backendInterface));           
    }
    
    private void createKWICSnippets() throws IOException, SearchServiceException{
        ArrayList arrayList = new ArrayList<>();
        ISOTEIKWICSnippetCreator creator = new ISOTEIKWICSnippetCreator();
        //BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
        String lastDocID = null;
        Transcript transcript = null;
        Document transcriptDoc = null;
        for (Hit row : searchResult.getHits()){
            String thisDocID = row.getDocId();
            // only get a new transcript if necessary
            // this seems to speed up everything by 30% to 50%
            if (lastDocID==null || (!lastDocID.equals(thisDocID))){
                transcript = backendInterface.getTranscript(row.getDocId());
                transcriptDoc = transcript.getDocument();
                lastDocID = thisDocID;
            }
            KWICSnippet snippetObj = creator.apply(transcriptDoc, row.getFirstMatch().getID(), row.getMatches(), getLeftContext(), getRightContext());
            arrayList.add(snippetObj);
        }
        setKWICSnippets((T) arrayList);
    }
    



    @Override
    public String toXML(){
        DefaultQuerySerializer searchResultSerializer = new DefaultQuerySerializer();
        return searchResultSerializer.displayKWICinXML(this);
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
