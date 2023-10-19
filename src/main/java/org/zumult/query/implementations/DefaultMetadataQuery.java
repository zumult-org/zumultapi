/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.query.MetadataQuery;

/**
 *
 * @author Elena
 */
public class DefaultMetadataQuery extends AbstractQuery implements MetadataQuery {
    String corpusQuery;
    String additionalMetadata;

    @Override
    public String getCorpusQuery() {
        return corpusQuery;
    }

    public void setCorpusQuery(String corpusQuery) {
        this.corpusQuery = corpusQuery;
    }

    @Override
    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    
}
