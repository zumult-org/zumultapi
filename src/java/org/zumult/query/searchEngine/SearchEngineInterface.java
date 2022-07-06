/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.zumult.objects.IDList;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Elena Frick
 */
public interface SearchEngineInterface {
    
     /** 
     * Executes the search in the MTAS search indices according to the specified parameters and
     * returns the search result containing the number of hits and the number of the relevant documents.
     * 
     * @param indexPaths            the list of absolute paths to the search indices, 
     *                              for example [C:\Users\...\ZuMult\indices\TB_FOLK, C:\Users\...\ZuMult\indices\TB_GWSS]
     * @param queryString           the search query string. It should be formulated in MTAS QL
     * @param metadataQueryString   the matadata query string, for example tokenSize=6
     * 
     * @return the {@code SearchEngineResponse} object
     * @throws SearchServiceException  if the query string cannot be parsed
     * @throws IOException  if there are problems with accessing the search index
     */
    public SearchEngineResponse search (ArrayList<String> indexPaths, String queryString, String metadataQueryString) throws SearchServiceException, IOException;
    
     /** 
     * Executes the search in the MTAS search indices according to the specified parameters and
     * returns the search result containing the number of hits, the number of the relevant documents 
     * and the list of the required number of {@code SearchEngineHit} objects.
     * 
     * 
     * @param indexPaths            the list of absolute paths to the search indices, 
     *                              for example [C:\Users\...\ZuMult\indices\TB_FOLK, C:\Users\...\ZuMult\indices\TB_GWSS]
     * @param queryString           the search query string. It should be formulated in MTAS QL
     * @param metadataQueryString   the matadata query string, for example tokenSize=6
     * @param from                  the number of the first {@code SearchEngineHit} to be returned
     * @param to                    the number of the last {@code SearchEngineHit} to be returned
     * @param cutoff                the boolean expression used to switch between two search modes.
     *                              Use true (dafault case) to count all hits in the search index.
     *                              Use false to retrieve just the number of hits specified by {@code from} and {@code to},
     *                              the total number of hits and the total number of documents are set to 1.
     * @param metadataIDs           the list of metadata ids whose values should be retrieved for each hit. 
     *                              It can be used when downloading KWIC inclusive metadata
     * 
     * @return the {@code SearchEngineResponseHitList} object
     * @throws SearchServiceException  if the query string or the metadata query string cannot be parsed
     * @throws IOException  if there are problems with accessing the search index
     */
    public SearchEngineResponseHitList searchKWIC (ArrayList<String> indexPaths, String queryString, String metadataQueryString,
            Integer from, Integer to, Boolean cutoff, IDList metadataIDs) throws SearchServiceException, IOException;
    
     /**
     * Executes the search in the MTAS search indices according to the specified parameters and
     * returns the hit distribution for the given {@code metadataKeyID}.
     * <p>
     * To learn more about the presentation form of statistics see {@link SearchEngineStatisticEntry}.
     * 
     * @param indexPaths            the list of absolute paths to the MTAS search indices, 
     *                              for example [C:\Users\...\ZuMult\indices\TB_FOLK, C:\Users\...\ZuMult\indices\TB_GWSS]
     * @param queryString           the search query string formulated in the MTAS QL
     * @param metadataQueryString   the matadata query string, for example tokenSize=6
     * @param from                  the number of the first {@link StatisticEntry} object to be stored in the {@link SearchEngineResponseStatistics}
     * @param to                    the number of the last {@code StatisticEntry} object to be stored in the {@code SearchEngineResponseStatistics}
     * @param sortType              one of the {@link SortTypeEnum} values that specifies what algorithm is used to sort the hit statistics
     * @param metadataKeyID         the metadata key id for which the hit statistics are to be calculated.
     *                              Possible values are all annotation layers and metadata categories stored in the search indices, 
     *                              but also "tokenSize" (=the number of matched tokens in the hit) and "transcription" 
     *                              (=all transcribed forms inclusive word tokens, pauses, incidents, pc etc)
     * 
     * @return the {@code SearchEngineResponseStatistics} object
     * @throws SearchServiceException  if the query string cannot be parsed
     * @throws IOException  if there are problems with accessing the search index or with creating and writing temporal files
     */
    public SearchEngineResponseStatistics searchMetadataStatistics (ArrayList<String> indexPaths, String queryString, String metadataQueryString,
            Integer from, Integer to, SortTypeEnum sortType, String metadataKeyID) throws SearchServiceException, IOException;
    
    
     /** 
     * Executes the search for repetitions in the MTAS search indices according to the specified parameters and
     * returns the search result containing the number of hits, the number of the relevant documents 
     * and the list of the required number of {@code SearchEngineHit} objects. 
     * 
     * @param indexPaths            the list of absolute paths to the search indices, 
     *                              for example [C:\Users\...\ZuMult\indices\TB_FOLK, C:\Users\...\ZuMult\indices\TB_GWSS]
     * @param queryString           the search query string. It should be formulated in MTAS QL
     * @param metadataQueryString   the matadata query string, for example tokenSize=6
     * @param from                  the number of the first {@code SearchEngineHit} to be returned
     * @param to                    the number of the last {@code SearchEngineHit} to be returned
     * @param cutoff                the boolean expression used to switch between two search modes.
     *                              Use true (dafault case) to count all hits in the search index.
     *                              Use false to retrieve just the number of hits specified by {@code from} and {@code to},
     *                              the total number of hits and the total number of documents are set in this case to 1.
     * @param metadataIDs           the list of metadata IDs whose values should be retrieved for each hit. 
     *                              It can be used when downloading KWIC inclusive metadata
     * @param repetitions           the array of {@code Repetition} objects
     * @param synonyms              the map  of synonym sets, e.g. {Mann=[Kerl, Junge], Fernseher=[Glotze], Katze=[Mieze, Kätzchen], ...}
     * 
     * @return the {@code SearchEngineResponseHitList} object
     * @throws SearchServiceException  if the query string or the metadata query string cannot be parsed
     * @throws IOException  if there are problems with accessing the search index
     */
    public SearchEngineResponseHitList searchRepetitions(ArrayList<String> indexPaths, String queryString, String metadataQueryString,
            Integer from, Integer to, Boolean cutoff, IDList metadataIDs, ArrayList<Repetition> repetitions, HashMap<String, HashSet> synonyms) throws SearchServiceException, IOException;
}
