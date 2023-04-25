/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.query.SampleQuery;

/**
 *
 * @author Elena
 */
public class DGD2SampleQuery extends DGD2AbstractSearchQuery implements SampleQuery {
        private String corpus;
        private String description;

        @Override
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String getCorpus() {
            return corpus;
        }

        public void setCorpus(String corpus) {
            this.corpus = corpus;
        }

    }
