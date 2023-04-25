/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import org.zumult.query.Hit.Match;

/**
 *
 * @author Elena
 */
 public class SearchEngineMatch implements Match {
    
        String type;
        String id;
        Integer startPosition;
        Integer endPosition;
        Integer startOffset;
        Integer endOffset;
        double startInterval;
        double endInterval;

        public void setType(String type) {
            this.type = type;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setStartPosition(Integer startPosition) {
            this.startPosition = startPosition;
        }

        public void setEndPosition(Integer endPosition) {
            this.endPosition = endPosition;
        }

        public void setStartOffset(Integer startOffset) {
            this.startOffset = startOffset;
        }

        public void setEndOffset(Integer endOffset) {
            this.endOffset = endOffset;
        }

        public void setStartInterval(double startInterval) {
            this.startInterval = startInterval;
        }

        public void setEndInterval(double endInterval) {
            this.endInterval = endInterval;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public Integer getStartPosition() {
            return startPosition;
        }

        @Override
        public Integer getEndPosition() {
            return endPosition;
        }

        @Override
        public Integer getStartOffset() {
            return startOffset;
        }

        @Override
        public Integer getEndOffset() {
            return endOffset;
        }

        @Override
        public double getStartInterval() {
            return startInterval;
        }

        @Override
        public double getEndInterval() {
            return endInterval;
        }

        @Override
        public String getID() {
           return id;
        }

}
