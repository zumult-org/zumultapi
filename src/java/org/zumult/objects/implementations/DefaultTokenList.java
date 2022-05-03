/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.HashMap;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.zumult.objects.TokenList;

/**
 *
 * @author Thomas_Schmidt
 */
public class DefaultTokenList extends HashMap<String, Integer> implements TokenList {

    String type;

    public DefaultTokenList(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    @Override
    public Integer add(String token) {
        //System.out.println("Putting: " + token);
        if (!(containsKey(token))){
            put(token, 0);
        }
        Integer newFrequency = put(token, get(token)+1);
        return newFrequency;
    }

    /**
     *
     * @param otherTokenList
     */
    @Override
    public TokenList merge(TokenList otherTokenList) {
        TokenList result = new DefaultTokenList(getType());
        result.putAll(this);
        for (String token : otherTokenList.keySet()){
            if (this.containsKey(token)){
                result.put(token, get(token) + otherTokenList.get(token));
            } else {
                result.put(token, otherTokenList.get(token));
            }
        }
        return result;
    }

    @Override
    public TokenList intersect(TokenList otherTokenList) {
        TokenList result = new DefaultTokenList(getType());
        result.putAll(this);
        Set intersectedKeys = result.keySet();
        Set otherKeys = otherTokenList.keySet();
        intersectedKeys.retainAll(otherKeys);
        for (String key : keySet()){
            if (!(intersectedKeys.contains(key))){
                result.remove(key);
            }
        }        
        return result;
    }
    
    @Override
    public TokenList remove(TokenList otherTokenList) {
        TokenList result = new DefaultTokenList(getType());
        result.putAll(this);
        Set reducedKeySet = result.keySet();
        Set otherKeys = otherTokenList.keySet();
        reducedKeySet.removeAll(otherKeys);
        for (String key : keySet()){
            if (!(reducedKeySet.contains(key))){
                result.remove(key);
            }
        }     
        return result;
    }
    

    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<tokenList type='").append(type).append("'>");        
        for (String token : keySet()){
            sb.append("<token form='").append(StringEscapeUtils.escapeXml(token)).append("' frequency='").append(get(token).toString()).append("'/>");
        }
        sb.append("</tokenList>");
        return sb.toString();
    }

    @Override
    public int getNumberOfTokens() {
        int count=0;
        for (String key : keySet()){
            count+=get(key);
        }
        return count;
    }

    @Override
    public int getNumberOfTypes() {
        return keySet().size();
    }




}
