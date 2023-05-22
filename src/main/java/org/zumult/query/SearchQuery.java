/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

/**
 *
 * @author Elena
 */
public interface SearchQuery {
    public String getQueryLanguage();
    public String getQueryLanguageVersion();
    public String getQueryString();
}
