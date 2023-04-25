/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.Map;

/**
 *
 * @author Thomas_Schmidt
 */
public interface TokenList extends Map<String, Integer> {
       
    public String getType();
    
    public int getNumberOfTokens();
    public int getNumberOfTypes();    
    
    public Integer add(String token);
    
    /** 
     * merges the two token lists, i.e. performs a union of types
     * adding the frequencies of types that occur in both lists
     * @param otherTokenList 
     * @return  
     */
    public TokenList merge(TokenList otherTokenList);
    /**
     * 
     * @param otherTokenList 
     * @return  
     */
    public TokenList intersect(TokenList otherTokenList);
    /**
     * 
     * @param otherTokenList 
     * @return  
     */
    public TokenList remove(TokenList otherTokenList);
    
    public String toXML();
    
}
