/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import java.io.File;

/**
 *
 * @author Frick
 */
public class SearchIndex extends File {
    private int numberOfIndexedDocuments;

    SearchIndex(String indexPath) {
        super(indexPath);
    }

    public int getNumberOfIndexedDocuments() {
        return numberOfIndexedDocuments;
    }

    public void setNumberOfIndexedDocuments(int numberOfIndexedDocuments) {
        this.numberOfIndexedDocuments = numberOfIndexedDocuments;
    }

}
