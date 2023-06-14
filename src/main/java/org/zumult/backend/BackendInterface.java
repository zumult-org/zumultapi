/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTagSet;
import org.zumult.objects.Corpus;
import org.zumult.objects.CrossQuantification;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Measure;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Protocol;
import org.zumult.objects.ResourceServiceException;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.Transcript;
import org.zumult.query.SearchResult;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchStatistics;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.KWIC;
import org.zumult.query.SampleQuery;
import org.zumult.query.Searcher;

/**
 *
 * @author Thomas_Schmidt
 */
public interface BackendInterface {
    
    public VirtualCollectionStore getVirtualCollectionStore();
    
    public String getID();
    public String getName();
    public String getAcronym();
    public String getDescription();
    
    public Corpus getCorpus(String corpusID) throws IOException;    
    public Event getEvent(String eventID) throws IOException;    
    public SpeechEvent getSpeechEvent(String speechEventID) throws IOException;    
    public Speaker getSpeaker(String speakerID) throws IOException;    
    public Speaker getSpeakerInSpeechEvent(String speechEventID, String speakerID);
    public Media getMedia(String mediaID) throws IOException;    
    public Media getMedia(String mediaID, Media.MEDIA_FORMAT format) throws IOException;    

    public Transcript getTranscript(String transcriptID) throws IOException;    
    
    public Protocol getProtocol(String protocolID) throws IOException;

    public IDList getCorpora() throws IOException;    
    public IDList getEvents4Corpus(String corpusID) throws IOException;    
    public IDList getSpeechEvents4Corpus(String corpusID) throws IOException;    
    public IDList getSpeakers4Corpus(String corpusID) throws IOException;    
    public IDList getSpeechEvents4Event(String eventID) throws IOException;
    public IDList getSpeechEvents4Speaker(String speakerID) throws IOException;
    public IDList getTranscripts4SpeechEvent(String speechEventID) throws IOException;
    public IDList getAudios4SpeechEvent(String speechEventID) throws IOException;
    public IDList getVideos4SpeechEvent(String speechEventID) throws IOException;
    public IDList getTranscripts4Audio(String audioID) throws IOException;
    public IDList getTranscripts4Video(String videoID) throws IOException;
    public IDList getAudios4Transcript(String transcriptID) throws IOException;
    public IDList getVideos4Transcript(String transcriptID) throws IOException;
   
    public Set<MetadataKey> getMetadataKeys4Corpus(String corpusID) throws IOException;
    public Set<MetadataKey> getMetadataKeys4Corpus(String corpusID, ObjectTypesEnum metadataLevel) throws IOException;
    
    public String getProtocol4SpeechEvent(String speechEventID) throws IOException;
    
    public String getSpeechEvent4Transcript(String transcriptID) throws IOException;
    public String getEvent4SpeechEvent(String speechEventID) throws IOException;
    public String getCorpus4Event(String eventID) throws IOException;
    //new 10-07-2020, Elena
    // removed 07-07-2022, issue #45
    //public String getEvent4Transcript(String transcriptID) throws IOException; // we need this because getSpeechEvent4Transcript can return null
    // ... (and so forth)
    
    public MetadataKey findMetadataKeyByID(String id);
    
    // Kommentar Test Josip 31-07-2018

    public IDList getSpeakers4SpeechEvent(String speechEventID) throws IOException;

    public IDList getAvailableValues(String corpusID, MetadataKey metadataKey);
    public IDList getAvailableValues(String corpusID, String metadataKeyID);

    //public String getMetadataValue(String level, String DGDObjectID, String metadatum);
    //public String getMetadataValue(String level, String DGDObjectID, String metadatum, String speakerID);
    
    // 07-07-2022, removed, issue #41
    /*public MediaMetadata getMediaMetadata4Media(String eventID, String mediaID);    
    public TranscriptMetadata getTranscriptMetadata4Transcript(String eventID, String transcriptID);
    public AdditionalMaterialMetadata getAdditionalMaterialMetadata4Corpus(String corpusID);*/
    
    // new 18-03-2019, TS
    public String getAnnotationBlockID4TokenID(String transcriptID, String tokenID) throws IOException;
    // new 12-06-2019, TS
    public String getNearestAnnotationBlockID4TokenID(String transcriptID, String tokenID) throws IOException;

    // new 08-04-2019, Elena 
    public AnnotationBlock getAnnotationBlock(String transcriptID, String annotationBlockId) throws IOException;
    
    //new 29-10-2019, Elena
    public IDList getTranscripts4Corpus(String corpusID) throws IOException;
    
    //new 30.01.2020, Elena
    public Measure getMeasure4SpeechEvent(String speechEventID, String type, String reference);
    public IDList getMeasures4Corpus(String corpusID);

    //new 21.07.2020, Elena
    public IDList getAvailableValuesForAnnotationLayer(String corpusID, String annotationLayerID);   
    public AnnotationTagSet getAnnotationTagSet(String annotationTagSetID) throws IOException;
    //public IDList getAvailableAnnotatationTagSets();
    
    //new 18.04.2023, Elena
    public CrossQuantification getCrossQuantification4Corpus(String corpusID, 
            MetadataKey metadataKey1, MetadataKey metadataKey2,
            String unit) throws ResourceServiceException, IOException;
    
    /**********************************************************************************/
    /*                            search methods                                      */ 
    /**********************************************************************************/
                 
    /**
     * Searches in the specified search index according to the specified parameters.
     * 
     * @param queryString           the search query string, e.g. [word="test"], not null
     * @param queryLanguage         the query language in which {@code queryString} is interpreted, e.g. CQP. 
     *                              If null, the default query language is used.
     * @param queryLanguageVersion  the version of {@code queryLanguage} in which {@code queryString} is interpreted.
     *                              If null, the default version of the specified query language is used.
     * @param corpusQuery           the corpus query string that specifies in which corpora to search for {@code queryString}, 
     *                              e.g. corpusSigle="FOLK|GWSS|DH", not null
     * @param metadataQuery         the metadata query string that constraints the search result to specified metadata, e.g. tokenSize=2, may be null
     * @param searchIndex           the string that can be an ID, name or type of a search index or a group of search indices
     *                              (that depends on the backend implementation, see e.g. 
     *                              {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                              If null, the default search index is used.
     * 
     * @return the {@code SearchResult} object
     * @throws SearchServiceException if input parameters can not be parsed 
     * @throws IOException 
     */ 
    public SearchResult search(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, String searchIndex, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException;
    
    /**
     * Searches in the specified search index according to the specified parameters.
     * 
     * @param queryString           the search query string, e.g. [word="test"], not null
     * @param queryLanguage         the query language in which {@code queryString} is interpreted, e.g. CQP. 
     *                              If null, the default query language is used.
     * @param queryLanguageVersion  the version of {@code queryLanguage} in which {@code queryString} is interpreted.
     *                              If null, the default version of the specified query language is used.
     * @param corpusQuery           the corpus query string that specifies in which corpora to search for {@code queryString}, 
     *                              e.g. corpusSigle="FOLK|GWSS|DH", not null
     * @param metadataQuery         the metadata query string that constraints the search result to specified metadata, e.g. tokenSize=2, may be null
     * @param pageLength            the number of the {@code Hit} objects that should be returned with {@code SearchResultPlus}. 
     *                              If null, the default value is used.
     * @param pageIndex             the number of the first {@code Hit} object that should be returned with {@code SearchResultPlus}, 
     *                              Please note, that the numbering starts with '0'. If null, the default value '0' is used.
     * @param cutoff                the boolean expression used to switch between two search modes.
     *                              Use true (dafault case) to count all hits in the search index.
     *                              Use false to retrieve just the number of hits specified by {@code pageLength},
     *                              the total number of hits and the total number of documents are set in this case 
     *                              to 1 in the {@code SearchResultPlus} object.
     * @param searchIndex           the string that can be an ID, name or type of a search index or a group of search indices
     *                              (that depends on the backend implementation, see e.g. 
     *                              {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                              If null, the default search index is used.
     * @param metadataIDs           the list of metadata IDs whose values should be retrieved for each hit 
     *                              It can be used when downloading KWIC inclusive metadata
     * 
     * @return the {@code SearchResultPlus} object
     * @throws SearchServiceException if input parameters can not be parsed 
     * @throws IOException 
     */
    public SearchResultPlus search(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, Integer pageLength, 
            Integer pageIndex, Boolean cutoff, String searchIndex, IDList metadataIDs, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException;
    
    /**
     * Searches for repetitions in the specified search index according to the specified parameters.
     * 
     * @param queryString           the search query string, e.g. [word="test"], not null
     * @param queryLanguage         the query language in which {@code queryString} is interpreted, e.g. CQP. 
     *                              If null, the default query language is used.
     * @param queryLanguageVersion  the version of {@code queryLanguage} in which {@code queryString} is interpreted.
     *                              If null, the default version of the specified query language is used.
     * @param corpusQuery           the corpus query string that specifies in which corpora to search for {@code queryString}, 
     *                              e.g. corpusSigle="FOLK|GWSS|DH", not null
     * @param metadataQuery         the metadata query string that constraints the search result to specified metadata, e.g. tokenSize=2, may be null
     * @param pageLength            the number of the {@code Hit} objects that should be returned with {@code SearchResultPlus}. 
     *                              If null, the default value is used.
     * @param pageIndex             the number of the first {@code Hit} object that should be returned with {@code SearchResultPlus}, 
     *                              Please note, that the numbering starts with '0'. If null, the default value '0' is used.
     * @param cutoff                the boolean expression used to switch between two search modes.
     *                              Use true (dafault case) to count all hits in the search index.
     *                              Use false to retrieve just the number of hits specified by {@code pageLength},
     *                              the total number of hits and the total number of documents are set in this case 
     *                              to 1 in the {@code SearchResultPlus} object.
     * @param searchIndex           the string that can be an ID, name or type of a search index or a group of search indices
     *                              (that depends on the backend implementation, see e.g. 
     *                              {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                              If null, the default search index is used.
     * @param metadataIDs           the list of metadata IDs whose values should be retrieved for each hit 
     *                              It can be used when downloading KWIC inclusive metadata
     * @param repetitions           the specification of desired repetitions. e.g. in the xml-format
     *                              &lt;repetitions&gt;
     *                                   &lt;repetition&gt;
     *                                      &lt;repetitionType&gt;LEMMA&lt;/repetitionType&gt;
     *                                      &lt;repetitionSimilarityType&gt;OWN_LEMMA_LIST&lt;/repetitionSimilarityType&gt;
     *                                      &lt;speaker&gt;null&lt;/speaker&gt;
     *                                      &lt;minDistance&gt;0&lt;/minDistance&gt;
     *                                      &lt;maxDistance&gt;5&lt;/maxDistance&gt;
     *                                      ...
     *                                    &lt;repetition&gt;
     *                                  ...
     *                              &lt;repetitions&gt;
     * @param synonyms              the specification of synonyms, e.g. in the xml-format 
     *                              &lt;synonyms&gt;Katze,Mieze,Kätzchen;Junge,Kerl,Mann;Fernseher,Glotze;&lt;/synonyms&gt;
     * 
     * @return the {@code SearchResultPlus} object
     * @throws SearchServiceException if input parameters can not be parsed
     * @throws IOException 
     */
    public SearchResultPlus searchRepetitions(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, Integer pageLength, 
            Integer pageIndex, Boolean cutoff, String searchIndex, IDList metadataIDs, String repetitions, String synonyms, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException;
    
    /**
     * Searches in the specified search index according to the specified parameters 
     * and calculates the distribution of hits for the specified {@code metadataKeyID}
     * 
     * @param queryString           the search query string, e.g. [word="test"], not null
     * @param queryLanguage         the query language in which {@code queryString} is interpreted, e.g. CQP. 
     *                              If null, the default query language is used.
     * @param queryLanguageVersion  the version of {@code queryLanguage} in which {@code queryString} is interpreted.
     *                              If null, the default version of the specified query language is used.
     * @param corpusQuery           the corpus query string that specifies in which corpora to search for {@code queryString}, 
     *                              e.g. corpusSigle="FOLK|GWSS|DH", not null
     * @param metadataQuery         the metadata query string that constraints the search result to specified metadata, e.g. tokenSize=2, may be null
     * @param metadataKeyID         the metadata key ID for which the hit distribution is to be calculated, e.g. 'pos'
     * @param pageLength            the number of the {@code StatisticEntry} objects that should be returned with {@code SearchStatistics}. 
     *                              If null, the default value is used.
     * @param pageIndex             the number of the first {@code StatisticEntry} object that should be returned with {@code SearchStatistics}, 
     *                              Please note, that the numbering starts with '0'. If null, the default value '0' is used.
     * @param searchIndex           the string that can be an ID, name or type of a search index or a group of search indices
     *                              (that depends on the backend implementation, see e.g. 
     *                              {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                              If null, the default search index is used.
     * @param sortType              the algorithm name that should be used to sort the hit statistics.
     *                              (depends on the backend implementation, see e.g. 
     *                              {@link org.zumult.query.searchEngine.SortTypeEnum SortTypeEnum})
     * 
     * @return the {@code SearchStatistics} object
     * @throws SearchServiceException if input parameters can not be parsed
     * @throws IOException 
     */
    public SearchStatistics getSearchStatistics(String queryString, String queryLanguage, 
            String queryLanguageVersion, String corpusQuery, String metadataQuery, String metadataKeyID, 
            Integer pageLength, Integer pageIndex, String searchIndex, String sortType, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException;
    
    /**
     * Searches in the specified transcript according to the specified parameters, 
     * analyzes matches with regard to the specified {@code tokenAttribute}
     * and returns a list of distinct values {@code tokenAttribute}
     * 
     * @param queryString           the search query string, e.g. [word="test"], not null
     * @param queryLanguage         the query language in which {@code queryString} is interpreted, e.g. CQP. 
     *                              If null, the default query language is used.
     * @param queryLanguageVersion  the version of {@code queryLanguage} in which {@code queryString} is interpreted.
     *                              If null, the default version of the specified query language is used.
     * @param corpusQuery           the corpus query string that specifies in which corpora to search for {@code queryString}, 
     *                              e.g. corpusSigle="FOLK|GWSS|DH", not null
     * @param metadataQuery         the metadata query string that constraints the search result to specified metadata, e.g. tokenSize=2, may be null
     * @param searchIndex           the string that can be an ID, name or type of a search index or a group of search indices
     *                              (that depends on the backend implementation, see e.g. 
     *                              {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                              If null, the default search index is used.
     * @param transcriptID          the transcript ID
     * @param tokenAttribute        word token attribute, whose values should be extracted for the match, e.g. "id", "pos", "lemma" or "norm"
     * 
     * @return {@code IDList} of distinct values for the specified {@code tokenAttribute}
     * @throws SearchServiceException if input parameters can not be parsed
     * @throws IOException 
     */
    public IDList searchTokensForTranscript(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, 
            String searchIndex, String transcriptID, String tokenAttribute, Map<String, String> additionalSearchConstraints) throws SearchServiceException, IOException;
    
    /**********************************************************************************/
    /*                          kwic methods                                          */ 
    /**********************************************************************************/
    
    /** 
     * Creates a {@code KWIC} object from {@code SearchResultPlus} according to the specified {@code context} string.
     * 
     * @param searchResult  the {@code SearchResultPlus} object, not null
     * @param context       the string of type [number]-[type],[number]-[type] where type can be 't' (for token) or 'c' (for char), 
     *                      and number defines the number of occurrences in left context (left of the comma) 
     *                      and right context (right of the comma), e.g. "3-t,3-t", not null
     * 
     * @return {@code KWIC} object of type {@code Constants.SEARCH_TYPE_STANDARD}
     * @throws SearchServiceException if {@code context} can not be parsed
     * @throws IOException
     * @throws ParserConfigurationException 
     */
    public KWIC getKWIC(SearchResultPlus searchResult, String context) throws SearchServiceException, IOException, ParserConfigurationException;
    

    /**
     * Creates a {@code KWIC} object from {@code SearchResultPlus} according to the specified {@code context} string.
     * 
     * @param searchResult  the {@code SearchResultPlus} object, not null
     * @param context       the string of type [number]-[type],[number]-[type] where type can be 't' (for token) or 'c' (for char), 
     *                      and number defines the number of occurrences in left context (left of the comma) 
     *                      and right context (right of the comma), e.g. "3-t,3-t", not null
     * @param fileFormat    the file type extension of the kwic document that should be prepared for download, e.g. "xml". 
     *                      If null, the dafault fileFormat will be returned. 
     * 
     * @return {@code KWIC} object of type {@code Constants.SEARCH_TYPE_DOWNLOAD}
     * @throws SearchServiceException if {@code context} can not be parsed
     * @throws IOException
     * @throws ParserConfigurationException 
     */
    public KWIC exportKWIC(SearchResultPlus searchResult, String context, String fileFormat) throws SearchServiceException, IOException, ParserConfigurationException;

    /**********************************************************************************/
    /*                          search info methods                                   */ 
    /**********************************************************************************/
    
    /**
     * Returns {@code IDList} of corpora stored in the search index.
     * 
     * @param searchIndex   the string that can be an ID, name or type of a search index or a group of search indices
     *                      (that depends on the backend implementation, 
     *                      see e.g. {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                      If null, IDs of all corpora available for search will be returned. 
     * 
     * @return the {@code IDList} object
     */
    public IDList getCorporaForSearch(String searchIndex);
    
    /**
     * Returns the set of {@code MetadataKey} objects which can be used for grupping hits in the corpora specified in {@code corpusQuery}.
     * 
     * @param corpusQuery       the corpus query string containing IDs of corpora that should be searched for metadata, 
     *                          e.g. corpusSigle="FOLK | GWSS", not null
     * @param searchIndex       the string that can be an ID, name or type of a search index or a group of search indices
     *                          (that depends on the backend implementation, 
     *                          see e.g. {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                          If null, all search indices are taken into account.
     * @param metadataKeyType   the type of {@code MetadataKey} objects which should be returned, e.g. "event", "speaker" etc.
     *                          (for more information about types see {@link org.zumult.objects.ObjectTypesEnum ObjectTypesEnum}). If null or empty, 
     *                          all available {@code MetadataKey} objects will be returned. 
     * 
     * @return the set of {@code MetadataKey} objects
     * @throws SearchServiceException if {@code searchIndex} could not be parsed or does not exist
     * @throws IOException 
     */
    public Set<MetadataKey> getMetadataKeysForGroupingHits(String corpusQuery, String searchIndex, String metadataKeyType) throws SearchServiceException, IOException;
    
    
    /**
     * Returns the set of {@code MetadataKey} objects which can be used for searching corpora specified in {@code corpusQuery}.
     * 
     * @param corpusQuery       the corpus query string containing IDs of corpora that should be searched for metadata, 
     *                          e.g. corpusSigle="FOLK | GWSS", not null
     * @param searchIndex       the string that can be an ID, name or type of a search index or a group of search indices
     *                          (that depends on the backend implementation, 
     *                          see e.g. {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                          If null, all search indices are taken into account.
     * @param metadataKeyType   the type of {@code MetadataKey} objects which should be returned, e.g. "event", "speaker" etc.
     *                          (for more information about types see {@link org.zumult.objects.ObjectTypesEnum ObjectTypesEnum}). If null or empty, 
     *                          all available {@code MetadataKey} objects will be returned. 
     * 
     * @return the set of {@code MetadataKey} objects
     * @throws SearchServiceException if {@code searchIndex} could not be parsed or does not exist
     * @throws IOException 
     */
    public Set<MetadataKey> getMetadataKeysForSearch(String corpusQuery, String searchIndex, String metadataKeyType) throws SearchServiceException, IOException;    
    
    
    /**
     * Returns the set of {@code AnnotationLayer} objects which can be used for grupping hits in the corpora specified in {@code corpusQuery}.
     * 
     * @param corpusQuery           the corpus query string containing IDs of corpora that should be searched for metadata, 
     *                              e.g. corpusSigle="FOLK | GWSS", not null
     * @param searchIndex           the string that can be an ID, name or type of a search index or a group of search indices
     *                              (that depends on the backend implementation, 
     *                              see e.g. {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                              If null, all search indices are taken into account.
     * @param annotationLayerType   the type of {@code AnnotationLayer} objects which should be returned, e.g. "token" or "span".
     *                              (for more information about types see {@link org.zumult.objects.AnnotationTypeEnum AnnotationTypeEnum}). 
     *                              If null or empty, all available {@code AnnotationLayer} objects will be returned. 
     * 
     * @return the set of {@code AnnotationLayer} objects
     * @throws SearchServiceException if {@code searchIndex} could not be parsed or does not exist
     * @throws IOException 
     */
    public Set<AnnotationLayer> getAnnotationLayersForGroupingHits(String corpusQuery, String searchIndex, String annotationLayerType) throws SearchServiceException, IOException;   
    
    /**
     * Returns the set of {@code AnnotationLayer} objects which can be used for searching corpora specified in {@code corpusQuery}.
     * 
     * @param corpusQuery           the corpus query string containing IDs of corpora that should be searched for metadata, 
     *                              e.g. corpusSigle="FOLK | GWSS", not null
     * @param searchIndex           the string that can be an ID, name or type of a search index or a group of search indices
     *                              (that depends on the backend implementation, 
     *                              see e.g. {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                              If null, all search indices are taken into account.
     * @param annotationLayerType   the type of {@code AnnotationLayer} objects which should be returned, e.g. "token" or "span".
     *                              (for more information about types see {@link org.zumult.objects.AnnotationTypeEnum AnnotationTypeEnum}). 
     *                              If null or empty, all available {@code AnnotationLayer} objects will be returned. 
     * 
     * @return the set of {@code AnnotationLayer} objects
     * @throws SearchServiceException if {@code searchIndex} could not be parsed or does not exist
     * @throws IOException 
     */
    public Set<AnnotationLayer> getAnnotationLayersForSearch(String corpusQuery, String searchIndex, String annotationLayerType) throws SearchServiceException, IOException;   
    
    /**
     * Returns a list of {@code SampleQuery} objects for the specified corpus and searchIndex
     * 
     * @param corpusID      the corpus ID
     * @param searchIndex   the string that can be an ID, name or type of a search index or a group of search indices
     *                      (that depends on the backend implementation, 
     *                      see e.g. {@link org.zumult.query.implementations.DGD2SearchIndexTypeEnum DGD2SearchIndexTypeEnum}). 
     *                      If null, all search indices are taken into account.
     * 
     * @return the list of {@code SampleQuery} objects
     * @throws SearchServiceException if {@code searchIndex} could not be parsed or does not exist
     */
    public List<SampleQuery> getSampleQueries (String corpusID, String searchIndex) throws SearchServiceException;
    
    //public IDList getSearchIndices();
    //pubic IDList getQueryLanguages();

    public Searcher getSearcher();

    
    
}
