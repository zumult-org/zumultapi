/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

import java.util.ArrayList;

/**
 *
 * @author Elena Frick
 */
public interface SearchResultPlus extends SearchResult {
    public ArrayList<Hit> getHits();
    public Pagination getPagination();
    public Boolean getCutoff();
}