/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import org.zumult.query.AdditionalSearchConstraint;
import org.zumult.query.Hit;
import org.zumult.query.KWIC;
import org.zumult.query.KWICContext;
import org.zumult.query.MetadataQuery;
import org.zumult.query.Pagination;
import org.zumult.query.SearchQuery;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 */
public class COMAKWIC<T> extends AbstractKWIC {

    private final String type;
    private final SearchResultPlus searchResult;
    private T kwicSnippets;
    
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
    
    private void setContext(String context) throws SearchServiceException{
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    private void createKWICSnippets(String fileType) 
            throws IOException, SearchServiceException{      
    }

    @Override
    public Object getKWICSnippets() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String toXML() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<Hit> getHits() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Pagination getPagination() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Boolean getCutoff() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getTotalHits() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getTotalTranscripts() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public SearchQuery getSearchQuery() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public MetadataQuery getMetadataQuery() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getSearchMode() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public long getSearchTime() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<AdditionalSearchConstraint> getAdditionalSearchConstraints() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    

    
}
