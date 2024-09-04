
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtas.parser.cql.MtasCQLParser;
import mtas.parser.cql.ParseException;
import mtas.parser.cql.TokenMgrError;
import mtas.search.spans.util.MtasSpanQuery;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 */
public abstract class QueryCreater {
    private static final Logger log = Logger.getLogger(QueryCreater.class.getName());

    protected MtasSpanQuery createQuery(String field, String queryString, HashMap < String, String [] > variables, MtasSpanQuery ignore, Integer maximumIgnoreLength) throws SearchServiceException, IOException {
        try{
            log.log(Level.INFO, "Searching {0}", queryString);
            Reader reader = new BufferedReader(new StringReader(queryString));
            MtasCQLParser p = new MtasCQLParser(reader);
            MtasSpanQuery msq = p.parse(field, null, variables, ignore, maximumIgnoreLength);
            reader.close();
            return msq;
        }catch (TokenMgrError | ParseException ex) {
            Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            throw new SearchServiceException("Please check the query syntax: " + ex.getMessage()); 
        }catch (IllegalArgumentException ex) {
            Logger.getLogger(MTASBasedSearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            if(ex.getMessage().contains("expected ')'") || ex.getMessage().contains("end-of-string")){
                throw new SearchServiceException("Please check the query syntax, for example, if all round brackets are closed!"); 
            }else{
                throw new SearchServiceException("Please check the query syntax: " + ex.getMessage()); 
            }
        }
    }
}
