/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.query.SearchQuery;

/**
 *
 * @author Elena
 */
public class DGD2SearchQuery extends DGD2AbstractSearchQuery implements SearchQuery {
        private String replacedQueryString;

        public String getReplacedQueryString() {
            return replacedQueryString;
        }

        public void setReplacedQueryString(String replacedQueryString) {
            this.replacedQueryString = replacedQueryString;
        }
}
