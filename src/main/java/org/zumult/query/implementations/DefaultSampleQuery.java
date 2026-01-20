/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.util.HashMap;
import java.util.Map;
import org.zumult.query.SampleQuery;

/**
 *
 * @author Elena
 */
public class DefaultSampleQuery extends AbstractSearchQuery implements SampleQuery {
    
        String DEFAULT_LANGUAGE = "de";
        
        private String corpus;
        private String description;
        Map<String, String> descriptions = new HashMap<>(); //e.g. {"en": "...", "de": "..."}


        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getCorpus() {
            return corpus;
        }

        public void setCorpus(String corpus) {
            this.corpus = corpus;
        }
        
        @Override
        public Map<String, String> getDescriptionsByLanguages () {
            return descriptions;
        }

        public void setDescriptions (Map<String, String> descriptions) {
            this.descriptions = descriptions;
        }

        @Override
        public String getDescription (String language) {
            if (descriptions.get(language)!=null){
                return descriptions.get(language);
            }else{
                return descriptions.get(DEFAULT_LANGUAGE);
            }
        }

    }
