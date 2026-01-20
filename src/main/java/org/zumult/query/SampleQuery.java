/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

import java.util.Map;

/**
 *
 * @author Elena
 */
public interface SampleQuery extends SearchQuery {
    public String getDescription();
    public String getDescription(String language);
    public Map<String, String> getDescriptionsByLanguages();
    public String getCorpus();
}
