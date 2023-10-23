/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.util.stream.Collectors;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.TimeUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.query.Hit;
import org.zumult.query.SearchResult;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchStatistics;
import org.zumult.query.StatisticEntry;
import org.zumult.query.searchEngine.MTASBasedSearchEngine;
import org.zumult.query.searchEngine.Repetition;
import org.zumult.query.searchEngine.SearchEngineResponse;
import org.zumult.query.searchEngine.SearchEngineResponseHitList;
import org.zumult.query.searchEngine.SearchEngineResponseStatistics;
import org.zumult.query.searchEngine.SortTypeEnum;
import org.zumult.query.AdditionalSearchConstraint;
import org.zumult.query.Searcher;
import org.zumult.query.SearchIndexType;

/**
 *
 * @author Frick
 */
public abstract class AbstractSearcher implements Searcher {
    
    protected DefaultPagination pagination = new DefaultPagination();
    protected DefaultSearchQuery query = new DefaultSearchQuery();
    protected DefaultMetadataQuery metadataQuery = new DefaultMetadataQuery();
    protected HashMap<String, String[]> wordListsMap = null;
    protected HashMap<String, HashSet> synonymsMap = null;
    ArrayList<AdditionalSearchConstraint> searchConstraints = new ArrayList();
    
    @Override
    public void setCollection(String corpusQueryStr, String metadataQueryStr) 
                                                throws SearchServiceException{
        if (corpusQueryStr == null || corpusQueryStr.isEmpty()) {
            throw new SearchServiceException(
                    "You did not specify a corpus!");
        } else {
            metadataQuery.setCorpusQuery(corpusQueryStr);
            metadataQuery.setAdditionalMetadata(metadataQueryStr);
            //System.out.println("METADATA QUERY: " + metadataQueryStr);
            //System.out.println("CORPUS QUERY: " + corpusQueryStr);
            
            /*
            metadataQuery.setQueryString(corpusQuery);
            Pattern r = Pattern.compile("&?(corpusSigle=[^&]+)&?");
            Matcher m = r.matcher(corpusQuery);
            if (m.find( )) {             
                collection =  m.group(1);
                System.out.println("collection: " + collection);
                
                String str1 = corpusQuery.substring(0, m.start());
                String str2 = corpusQuery.substring(m.end(), 
                                                corpusQuery.length());
                
                if (!str1.isEmpty() && !str2.isEmpty()){
                    metadataString = str1 + "&" + str2;
                }else{
                    metadataString = str1 + str2;
                }
                System.out.println("metadataString: " + metadataString);
            }
            */
        }        
    }
    
    @Override
    public void setQuery (String queryString, 
                        String queryLanguage, 
                        String queryLanguageVersion) 
            throws SearchServiceException{

        if (queryString == null || queryString.isEmpty()) {
            throw new SearchServiceException(
                    "You did not specify a query!");
        } else {
            query.setQueryString(queryString);
        }        
        
        if (queryLanguage == null || queryLanguage.isEmpty() ){
            queryLanguage = Constants.DEFAULT_QUERY_LANGUAGE;
        }
        
        if (queryLanguage.equalsIgnoreCase("cqp")) {
            if (queryLanguageVersion == null 
                    || queryLanguageVersion.isEmpty() ) {
                queryLanguageVersion = 
                        Constants.DEFAULT_CQP_QUERY_LANGUAGE_VERSION;
            }
                 // TODO: check the query language syntax

        }
                /*  TODO: integrate KoralQuery
                else if ( queryLanguage.equalsIgnoreCase("poliqarp") || 
                        queryLanguage.equalsIgnoreCase("cosmas2") || 
                        queryLanguage.equalsIgnoreCase("poliqarpplus")){
                QuerySerializer qs = new QuerySerializer();
                qs.setQuery(queryString, "poliqarpplus");
                queryString = qs.toJSON();
                queryLanguage = "KoralQuery";
                }
                */
        else {
            throw new SearchServiceException (queryLanguage 
                    + " is not a supported query language!");
        }
        
        query.setQueryLanguage(queryLanguage);
        query.setReplacedQueryString(queryString);
        query.setQueryLanguageVersion(queryLanguageVersion);

    }

    @Override
    public void setAdditionalSearchConstraints(Map<String, String> constraints)
                                                throws SearchServiceException {
                        
        if (constraints!=null){
            String wordLists = constraints.get(
                    Constants.CUSTOM_WORDLISTS_KEY);
            if(wordLists!=null 
                    && !wordLists.isEmpty() 
                    && !wordLists.equals("null")){
                
                /* '&' and '|' occur in AGD lemmas 
                and should be preceded by backslash. 
                For user-friendly reasons, the correct entry is added here 
                if the user has not done so */
                wordLists = wordLists
                        .replaceAll("(?<!\\\\)&", "\\\\&")
                        //.replaceAll("(?<!\\\\)\\|", "\\\\|")
                        .replaceAll("(?<!\\\\)\"", "\\\\\"")
                        .replaceAll("(?<!\\\\)\\@", "\\\\@")
                        .replaceAll("(?<!\\\\)#", "\\\\#");
                
                // handling special characters in XML
                wordLists = wordLists
                        .replaceAll("&(?!amp)", "&amp;");
                
                try {
                    Document doc = (Document) IOHelper
                            .DocumentFromText(wordLists);
                    NodeList nodes = doc.getElementsByTagName(
                            Constants.CUSTOM_WORDLISTS_KEY);
                    Element element = ((Element)(nodes.item(0)));
                    HashMap<String, String[]> variables = 
                            (HashMap<String, String[]>) Arrays.stream(
                            element
                                    .getTextContent()
                                    .replace("&amp;", "&")
                                    .split(
                           Constants.CUSTOM_WORDLISTS_VARIABLE_DELIMITER))
                                .map(s -> s.split(
                     Constants.CUSTOM_WORDLISTS_VARIABLE_TOKEN_DELIMITER))
                                    .collect(Collectors
                                            .toMap(s -> s[0], 
                                                    s-> s[1].split(
                             Constants.CUSTOM_WORDLISTS_TOKEN_DELIMITER)));
                    wordListsMap = variables;
                /*    for (String str: wordListsMap.keySet()){
                        String[] strObj = wordListsMap.get(str);
                        for (String strObj1 : strObj) {
                            System.out.println(strObj1);
                        }
                    }*/
                    
                    searchConstraints.add(
                            new DefaultAdditionalSearchConstraint(
                                    wordLists));
                }catch(IllegalArgumentException 
                        | ArrayIndexOutOfBoundsException 
                        | SAXException 
                        | ParserConfigurationException 
                        | IOException e){
                    Logger.getLogger(AbstractSearcher.class.getName())
                            .log(Level.SEVERE, null, e);
                    throw new SearchServiceException(
                            "Please check the syntax of your wordlists!");
                }
            }
        }
    }
    
    @Override
    public void setPagination(Integer pageLength , Integer pageStartIndex){
        
         // set itemsPerPage and pageStartIndex      
        int itemsPerPage = Constants.DEFAULT_PAGE_LENGTH;
        
        if (pageLength != null && pageLength > 0){           
            itemsPerPage = pageLength;
        }

        pagination.setItemsPerPage(itemsPerPage);
        pagination.setPageStartIndex((pageStartIndex == null 
                || pageStartIndex < 0) 
                ? Constants.DEFAULT_PAGE_INDEX 
                : pageStartIndex); // starts with 0
    }
    
    @Override
    public SearchResult search(String searchIndex) 
            throws SearchServiceException, IOException{
                
        // set index
        SearchIndexType index = getSearchIndexType(searchIndex);
        
        /* Search with MTAS using cqp query language */
        
        final long timeStart_search = System.currentTimeMillis();
        
        MTASBasedSearchEngine se = getSearchEngine();
        SearchEngineResponse searchResult = se.search(
                getIndexPaths(index), 
                query.getReplacedQueryString(), 
                null, 
                wordListsMap);
        
        final long timeEnd_search = System.currentTimeMillis();
        long millis_search = timeEnd_search - timeStart_search;
        
        DefaultSearchResult result = new DefaultSearchResult();
        
        result.setSearchTime(millis_search);
        result.setSearchQuery(query);
        result.setMetadataQuery(metadataQuery);
        result.setSearchMode(index.getValue());
        result.setTotalHits(searchResult.getHitsTotal());
        result.setTotalTranscripts(
                searchResult.getTranscriptsTotal());
        result.setAdditionalSearchConstraints(
                searchConstraints);

        return result;
    }
       
    @Override
    public SearchResultPlus search(String searchIndex, 
                                   Boolean cutoff, 
                                   IDList metadataIDs) 
                                   throws IOException, SearchServiceException {
        
        // set cutoff
        Boolean count = Constants.DEFAULT_CUTOFF;
        if(cutoff!=null){
            count = cutoff;
        }

        // set index
        SearchIndexType index = getSearchIndexType(searchIndex);
        
        /* Search with MTAS using cqp query language */
        
        final long timeStart_search = System.currentTimeMillis();
        
        MTASBasedSearchEngine se = getSearchEngine(); 
        SearchEngineResponseHitList searchResult = se
                .searchKWIC(
                getIndexPaths(index), 
                query.getReplacedQueryString(), 
                metadataQuery.getAdditionalMetadata(), 
                pagination.getPageStartIndex() + 1, 
                pagination.getPageStartIndex() + pagination.getItemsPerPage(), 
                count, metadataIDs, 
                wordListsMap);
        
        final long timeEnd_search = System.currentTimeMillis();
        long millis_search = timeEnd_search - timeStart_search;
        System.out.println("Searching time: " + 
                TimeUtilities.format(millis_search));

        /* Construct search result */
        DefaultSearchResultPlus result = new DefaultSearchResultPlus();

        result.setSearchTime(millis_search);
        result.setCutoff(count);
        result.setSearchQuery(query);
        result.setMetadataQuery(metadataQuery);
        result.setSearchMode(index.getValue());
        result.setPagination(pagination);
        result.setHits(searchResult.getHits());
        result.setTotalHits(searchResult.getHitsTotal());
        result.setTotalTranscripts(
                searchResult.getTranscriptsTotal());
        result.setAdditionalSearchConstraints(
                searchConstraints);

        return result;
    }
   
        @Override
    public SearchStatistics getStatistics(
                                String searchIndex, 
                                String sortType, 
                                MetadataKey metadataKey) 
                                throws SearchServiceException, IOException{
            
            // set index
            SearchIndexType index = getSearchIndexType(searchIndex);
        
            // set sort type
            SortTypeEnum sort = SortTypeEnum.ABS_DESC;
            if(sortType!=null){
                try{
                    sort = SortTypeEnum.valueOf(sortType);
                }catch (NullPointerException ex){
                    StringBuilder sb = new StringBuilder();
                    sb.append(". Sort type ").append(sortType)
                            .append(" is not supported. "
                                    + "Supported sort types are: ");
                    for (SortTypeEnum ob : SortTypeEnum.values()){
                        sb.append(ob.name());
                        sb.append(", ");
                    }
                    throw new SearchServiceException(sb.toString()
                            .trim().replaceFirst(",$",""));
                }
            }
        
            final long timeStart_search = System.currentTimeMillis();

            MTASBasedSearchEngine se = getSearchEngine();
            SearchEngineResponseStatistics searchResult = se
                .searchMetadataStatistics(
                  getIndexPaths(index), 
                  query.getReplacedQueryString(), 
                  metadataQuery.getAdditionalMetadata(), 
                  pagination.getPageStartIndex() + 1, 
                  pagination.getPageStartIndex() + pagination.getItemsPerPage(),
                  sort, metadataKey.getID(),
                  wordListsMap);

            final long timeEnd_search = System.currentTimeMillis();
            long millis_search = timeEnd_search - timeStart_search;
            System.out.println("Searching time (getStatistics): " 
                    + TimeUtilities.format(millis_search));

            DefaultSearchStatistics result = new DefaultSearchStatistics();

            result.setSearchTime(millis_search);
            result.setMetadataKey(metadataKey);
            result.setSortType(sort.name());
            result.setSearchQuery(query);
            result.setMetadataQuery(metadataQuery);
            result.setSearchMode(index.getValue());
            result.setPagination(pagination);
            result.setStatistics(searchResult.getStatistics());
            result.setNumberOfDistinctValues(
                    searchResult.getNumberOfDistinctValues());
            result.setTotalHits(searchResult.getHitsTotal());
            result.setTotalTranscripts(
                    searchResult.getTranscriptsTotal());
            result.setAdditionalSearchConstraints(
                    searchConstraints);

            return result;
    }
    
    @Override
    public IDList searchTokensForTranscript(String searchIndex, 
                                            String tokenAttribute) 
                            throws IOException, SearchServiceException {
        
        // set index
        SearchIndexType index = getSearchIndexType(searchIndex);
        
        final long timeStart_search = System.currentTimeMillis();
        
        IDList list;
        MTASBasedSearchEngine se = getSearchEngine();
        
        if (tokenAttribute.equals("id")){
            SearchEngineResponse mtasSearchResult = se.search(
                    getIndexPaths(index), 
                    query.getReplacedQueryString(), 
                    null, 
                    wordListsMap);
            
            SearchEngineResponseHitList mtasSearchResultPlus = se.searchKWIC(
                    getIndexPaths(index), 
                    query.getReplacedQueryString(),
                    null, 
                    1, 
                    mtasSearchResult.getHitsTotal(), 
                    true, 
                    null, 
                    wordListsMap);

            final long timeEnd_search = System.currentTimeMillis();
            long millis_search = timeEnd_search - timeStart_search;
            System.out.println("Searching time: " 
                    + TimeUtilities.format(millis_search));

            list = new IDList("token");
            for (Hit hit: mtasSearchResultPlus.getHits()){
                for (Hit.Match match : hit.getMatches()){
                    list.add(match.getID());
                }
            }
        }else{
        
            SearchEngineResponseStatistics mtasSearchResultPre = se
                    .searchMetadataStatistics(
                        getIndexPaths(index), 
                        query.getReplacedQueryString(), 
                        metadataQuery.getAdditionalMetadata(), 
                        1, 
                        Constants.DEFAULT_PAGE_LENGTH, 
                        SortTypeEnum.ABS_DESC, 
                        tokenAttribute, wordListsMap);
            
            SearchEngineResponseStatistics mtasSearchResult = se
                    .searchMetadataStatistics(
                        getIndexPaths(index), 
                        query.getReplacedQueryString(), 
                        metadataQuery.getAdditionalMetadata(), 
                        1, 
                        mtasSearchResultPre.getNumberOfDistinctValues(), 
                        SortTypeEnum.ABS_DESC, 
                        tokenAttribute, 
                        wordListsMap);

            final long timeEnd_search = System.currentTimeMillis();
            long millis_search = timeEnd_search - timeStart_search;
            System.out.println("Searching time: " 
                    + TimeUtilities.format(millis_search));        

            list = new IDList(tokenAttribute);
            
            Set set = new HashSet();
            for (StatisticEntry entry: mtasSearchResult.getStatistics()){
                String[] strList = entry.getMetadataValue().split("\\|");
                for (String str: strList){
                    set.add(str);
                }
            }
            list.addAll(set);
        }
        return list;
    }
    
    @Override
    public SearchResultPlus searchRepetitions(
                                String searchIndex, 
                                Boolean cutoff, 
                                IDList metadataIDs, 
                                String repetitionsStr, 
                                String synonymStr) 
                                throws IOException, SearchServiceException {
        
        // set cutoff
        Boolean count = Constants.DEFAULT_CUTOFF;
        if(cutoff!=null){
            count = cutoff;
        }

        // set index
        SearchIndexType index = getSearchIndexType(searchIndex);

        // create a list with repetition-objects
        ArrayList<Repetition> repetitions = new ArrayList();
        
        try {
            Document doc = (Document) IOHelper
                    .DocumentFromText(repetitionsStr);
            NodeList nodes = doc
                    .getElementsByTagName(Constants.REPETITION);
            for (int i=0; i<nodes.getLength(); i++){
                Element element = ((Element)(nodes.item(i)));
                Repetition r = new Repetition(element);
                repetitions.add(r);
            }
            searchConstraints.add(
                    new DefaultAdditionalSearchConstraint(repetitionsStr));

        } catch (SAXException | ParserConfigurationException ex) {
           throw new SearchServiceException (
                "Please check the xml format of repetition-parameter!");
        }
        
        
        //create a list of synonyms
        HashMap<String, HashSet> synonymMap = new HashMap();

        if(synonymStr!=null && !synonymStr.isEmpty()){
            String synonyms = synonymStr.replaceAll("\\s+","");
        
            try {
                Document doc = (Document) IOHelper
                        .DocumentFromText(synonyms);
                NodeList nodes = doc
                     .getElementsByTagName(Constants.REPETITION_SYNONYMS);

                Element element = ((Element)(nodes.item(0)));
                String[] wordSets = element.getTextContent().split(";");
                for (String wordSet : wordSets) {
                    ArrayList<String> wordList = new ArrayList(
                            Arrays.asList(wordSet.split(",")));

                    for(String word: wordList){
                        HashSet words = new HashSet(wordList);
                        words.remove(word);
                        if(synonymMap.containsKey(word)){
                            words.addAll(synonymMap.get(word));
                        }
                        synonymMap.put(word, words);
                    }
                }

                searchConstraints.add(
                   new DefaultAdditionalSearchConstraint(synonyms));
            } catch (SAXException | ParserConfigurationException ex) {
               throw new SearchServiceException (
                       "Please check the xml format of synonyms!");
            }
        }
        
        
        /* Search with MTAS using cqp query language */
        final long timeStart_search = System.currentTimeMillis();

        MTASBasedSearchEngine se = getSearchEngine();
        SearchEngineResponseHitList searchResult = se.searchRepetitions(
                getIndexPaths(index), 
                query.getReplacedQueryString(), 
                metadataQuery.getAdditionalMetadata(), 
                pagination.getPageStartIndex() + 1, 
                pagination.getPageStartIndex() + pagination.getItemsPerPage(),
                count, 
                metadataIDs, 
                repetitions, 
                synonymMap, 
                wordListsMap);

        final long timeEnd_search = System.currentTimeMillis();
        long millis_search = timeEnd_search - timeStart_search;
        System.out.println("Searching time: " 
                + TimeUtilities.format(millis_search));

        /* Construct search result */
        DefaultSearchResultPlus result = new DefaultSearchResultPlus();
        
        result.setSearchTime(millis_search);
        result.setCutoff(count);
        result.setSearchQuery(query);
        result.setMetadataQuery(metadataQuery);
        result.setSearchMode(index.getValue());
        result.setPagination(pagination);
        result.setHits(searchResult.getHits());
        result.setTotalHits(searchResult.getHitsTotal());
        result.setAdditionalSearchConstraints(
                searchConstraints);
        result.setTotalTranscripts(
                searchResult.getTranscriptsTotal());
        
        return result;
    }

    @Override
    public IDList getCorporaForSearch(String searchIndex){
        IDList corpora = Configuration.getCorpusIDs();
        return corpora;
    }
    
    abstract ArrayList<String> getIndexPaths(SearchIndexType searchIndex) 
            throws IOException, SearchServiceException;
    
    abstract SearchIndexType getSearchIndexType(String searchIndex) 
            throws SearchServiceException;
    
    abstract MTASBasedSearchEngine getSearchEngine();
}
