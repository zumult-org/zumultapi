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
public abstract class AbstractSearchQuery extends AbstractQuery{
    private String queryLanguage;
    
    private String queryLanguageVersion;

    public String getQueryLanguage() {
        return queryLanguage;
    }

    public void setQueryLanguage(String queryLanguage) {
        this.queryLanguage = queryLanguage;
    }

    public String getQueryLanguageVersion() {
        return queryLanguageVersion;
    }

    public void setQueryLanguageVersion(String queryLanguageVersion) {
        this.queryLanguageVersion = queryLanguageVersion;
    }
    
}
