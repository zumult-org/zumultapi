/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

/**
 *
 * @author Elena
 */
public abstract class AbstractQuery {

    String queryString;

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    public String getQueryString() {
        return queryString;
    }
}