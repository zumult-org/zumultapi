/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import org.zumult.query.StatisticEntry;

/**
 *
 * @author Elena
 */
public class SearchEngineStatisticEntry implements StatisticEntry, Comparable<SearchEngineStatisticEntry>  {
        private String metadataValue;
        private int numberOfHits;

        public SearchEngineStatisticEntry(String metadataValue, int numberOfHits) {
            this.metadataValue = metadataValue;
            this.numberOfHits = numberOfHits;
        }

        @Override
        public String getMetadataValue() {
            return metadataValue;
        }

        public void setMetadataValue(String metadataValue) {
            this.metadataValue = metadataValue;
        }

        @Override
        public int getNumberOfHits() {
            return numberOfHits;
        }

        public void setNumberOfHits(int value) {
            this.numberOfHits = value;
        }

        @Override
        public int compareTo(SearchEngineStatisticEntry entry) {
            return this.numberOfHits > entry.numberOfHits ? 1 : this.numberOfHits < entry.numberOfHits ? -1 : 0;
        }

        @Override
        public String toString() {
            return (this.metadataValue + "=" + this.numberOfHits);
       }
    
}
