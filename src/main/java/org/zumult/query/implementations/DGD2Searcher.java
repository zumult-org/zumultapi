 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.File;
import org.zumult.query.searchEngine.MTASBasedSearchEngine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTypeEnum;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.implementations.DGD2AnnotationLayer;
import org.zumult.objects.implementations.DGD2MetadataKey;
import org.zumult.query.SampleQuery;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Elena Frick
 */
public class DGD2Searcher extends AbstractSearcher {
    ResourceBundle myResourcesEN = ResourceBundle.getBundle("resources.MessageBundle", new Locale.Builder().setLanguage("en").setRegion("US").build());
    ResourceBundle myResourcesGER = ResourceBundle.getBundle("resources.MessageBundle", new Locale.Builder().setLanguage("de").setRegion("DE").build());
    
    private static final int SEARCH_INDEX_PREXIF_LENGTH = 3;
    
    @Override
    public Set<MetadataKey> filterMetadataKeysForGroupingHits(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException{
        metadataKeys = filterMetadataKeysForSearch(metadataKeys, searchIndex, type);
        
        // add sorting by the number of tokens in the hit (key id "tokenSize")
        if(type==null || type.isEmpty() || type.toUpperCase().equals(ObjectTypesEnum.HIT.name())){

            MetadataKey metadataKeySizeInTokens = new DGD2MetadataKey(MTASBasedSearchEngine.METADATA_KEY_HIT_LENGTH, createLangMap("SizeInTokens"), ObjectTypesEnum.HIT);
            metadataKeys.add(metadataKeySizeInTokens);
            
            MetadataKey metadataKeySizeInWordTokens = new DGD2MetadataKey(MTASBasedSearchEngine.METADATA_KEY_HIT_LENGTH_IN_WORD_TOKENS, createLangMap("SizeInWordTokens"), ObjectTypesEnum.HIT);
            metadataKeys.add(metadataKeySizeInWordTokens);
        }
        return metadataKeys;
    }
     
    @Override
    public Set<MetadataKey> filterMetadataKeysForSearch(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException{
        // TODO: implement localization
        DGD2SearchIndexTypeEnum index = getSearchIndex(searchIndex);
        
        // remove some metadata
        metadataKeys = removeFromMetadata(metadataKeys, Constants.METADATA_KEY_SPEAKER_BIRTH_DATE);
        
        if(index.equals(DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX)){
            metadataKeys = removeFromMetadata(metadataKeys, Constants.METADATA_KEY_SPEAKER_NAME);
            metadataKeys = removeFromMetadata(metadataKeys, Constants.METADATA_KEY_SPEAKER_OTHER_NAMES);
            metadataKeys = removeFromMetadata(metadataKeys, Constants.METADATA_KEY_SPEAKER_PSEUDONYM);
        }
        
        // add some metadata
        if(type==null || type.isEmpty() || type.toUpperCase().equals(ObjectTypesEnum.EVENT.name())){
            MetadataKey metadataKeyEventID = new DGD2MetadataKey(Constants.METADATA_KEY_EVENT_DGD_ID, myResourcesGER.getString("Event") + " ID", ObjectTypesEnum.EVENT);
            metadataKeys.add(metadataKeyEventID);
            
            MetadataKey metadataKeyEventDurationSec = new DGD2MetadataKey(Constants.METADATA_KEY_EVENT_DAUER_SEC, myResourcesGER.getString("DurationSec"), ObjectTypesEnum.EVENT);
            metadataKeys.add(metadataKeyEventDurationSec); 
        }
        
        if(type==null || type.isEmpty() || type.toUpperCase().equals(ObjectTypesEnum.SPEAKER.name())){
            MetadataKey metadataKeySpeakerID = new DGD2MetadataKey(Constants.METADATA_KEY_SPEAKER_DGD_ID, myResourcesGER.getString("Speaker") + " ID", ObjectTypesEnum.SPEAKER);
            metadataKeys.add(metadataKeySpeakerID);
            
            MetadataKey metadataKeySpeakerYearOfBirth = new DGD2MetadataKey(Constants.METADATA_KEY_SPEAKER_YEAR_OF_BIRTH, myResourcesGER.getString("YearOfBirth"), ObjectTypesEnum.SPEAKER);
            metadataKeys.add(metadataKeySpeakerYearOfBirth);    
        }
        
        if(type==null || type.isEmpty() || type.toUpperCase().equals(ObjectTypesEnum.SPEECH_EVENT.name())){
            MetadataKey metadataKeySpeechEventID = new DGD2MetadataKey(Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID, myResourcesGER.getString("SpeechEvent") + " ID", ObjectTypesEnum.SPEECH_EVENT);
            metadataKeys.add(metadataKeySpeechEventID);
        }
        
        if(type==null || type.isEmpty() || type.toUpperCase().equals(ObjectTypesEnum.TRANSCRIPT.name())){
            MetadataKey metadataKeyTranscriptID = new DGD2MetadataKey(Constants.METADATA_KEY_TRANSCRIPT_DGD_ID, myResourcesGER.getString("Transcript") + " ID", ObjectTypesEnum.TRANSCRIPT);
            metadataKeys.add(metadataKeyTranscriptID);
            
            MetadataKey metadataKeyEventVideosNumber = new DGD2MetadataKey(Constants.METADATA_KEY_EVENT_NUMBER_VIDEOS, myResourcesGER.getString("VideoNumber"), ObjectTypesEnum.TRANSCRIPT);
            metadataKeys.add(metadataKeyEventVideosNumber); 
        }
                
        return metadataKeys;
    }
    
    private Set<MetadataKey> removeFromMetadata(Set<MetadataKey> metadataKeys, String id){
        for (MetadataKey metadataKey: metadataKeys){
            if(metadataKey.getID().equals(id)){
                metadataKeys.remove(metadataKey);
                break;
            }
        }
        return metadataKeys;
    }
    
    @Override
    public Set<AnnotationLayer> filterAnnotationLayersForGroupingHits(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException{
        annotationLayers = filterAnnotationLayersForSearch(annotationLayers, searchIndex, annotationLayerType);
        
        // TODO: add sorting by the transcribed form inclusive pause, incident, vocal and pc
        
        return annotationLayers;
    }
    
    @Override
    public Set<AnnotationLayer> filterAnnotationLayersForSearch(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException{
        //DGD2SearchIndexTypeEnum index = getSearchIndex(searchIndex);
        // TODO: implement the dependency on the search index

        // replace some annotation ids
      /*  for (AnnotationLayer annotationLayer: annotationLayers){
            if(annotationLayer.getID().equals("type")){
                Map<String, String> map = new HashMap();
                map.put(Locale.GERMAN.getLanguage(), annotationLayer.getName(Locale.GERMAN.getLanguage()));
                map.put(Locale.ENGLISH.getLanguage(), annotationLayer.getName(Locale.ENGLISH.getLanguage())); 
                annotationLayers.remove(annotationLayer);
                annotationLayers.add(new DGD2AnnotationLayer(Constants.METADATA_KEY_MATCH_TYPE_WORD_TYPE, map, annotationLayer.getType()));
                break;
            }
        }*/
        
        // add some anotations
        if (annotationLayerType==null || annotationLayerType.isEmpty() || annotationLayerType.toUpperCase().equals(AnnotationTypeEnum.TOKEN.name())){
            Map<String, String> proxiTokens = Arrays.stream(Constants.PROXI_TOKEN_ANNOTATION_LAYERS)
            .collect(Collectors.toMap(entity -> entity[0], entity -> entity[1]));
            for(String str: proxiTokens.keySet()){
                AnnotationLayer annotationLayer = new DGD2AnnotationLayer(str, createLangMap(proxiTokens.get(str)), AnnotationTypeEnum.TOKEN);
                annotationLayers.add(annotationLayer);
            }  
        }
        
        if (annotationLayerType==null || annotationLayerType.isEmpty() || annotationLayerType.toUpperCase().equals(AnnotationTypeEnum.SPAN.name())){
            Map<String, String> proxiTokens = Arrays.stream(Constants.PROXI_SPAN_ANNOTATION_LAYERS)
            .collect(Collectors.toMap(entity -> entity[0], entity -> entity[1]));
            for(String str: proxiTokens.keySet()){               
                AnnotationLayer annotationLayer = new DGD2AnnotationLayer(str, createLangMap(proxiTokens.get(str)), AnnotationTypeEnum.SPAN);
                annotationLayers.add(annotationLayer);
            }  
        }         
        return annotationLayers;
    }
        
    @Override
    public ArrayList<SampleQuery> getSampleQueries (String corpusID, String searchIndex) throws SearchServiceException{
        ArrayList<SampleQuery> queries = new ArrayList();
        
        DGD2SearchIndexTypeEnum index = getSearchIndex(searchIndex);
        String path=null;
        switch(index){
            case TRANSCRIPT_BASED_INDEX, TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT 
                -> path = Constants.SAMPLE_QUERIES_FOR_TRASCRIPT_BASED_SEARCH;
            case SPEAKER_BASED_INDEX, SPEAKER_BASED_INDEX_WITHOUT_PUNCT 
                -> path = Constants.SAMPLE_QUERIES_FOR_SPEAKER_BASED_SEARCH;
            default -> {         // do nothing;
            }
        }

        try{
            ArrayList<SampleQuery> allQueries = IOHelper.getQueriesFromFile(path);

            for(int i=0; i<allQueries.size(); i++){           
                SampleQuery q = allQueries.get(i);
                String corpus = q.getCorpus();
                if (corpus.isEmpty() || corpus.contains(corpusID)){
                    queries.add(q);
                }
            }
            
            
        }catch (ParserConfigurationException | SAXException | IOException ex){
            throw new SearchServiceException("Problem: Sample queries could not be found!");
        }     
        
        return queries;
    }   
    
    @Override
    protected ArrayList<String> getIndexPaths(DGD2SearchIndexTypeEnum searchMode) throws IOException, SearchServiceException{

        //System.out.println("PARAMETER (SEARCH MODE): " + index);
        Pattern r = Pattern.compile(Constants.CORPUS_SIGLE_PATTERN);
        Matcher m = r.matcher(metadataQuery.getCorpusQuery());
        if (m.find( )) {
            String[] corpora = m.group(1).split(Pattern.quote(Constants.CORPUS_DELIMITER));
   
            ArrayList<String> indexIDs = new ArrayList();
            String str = Constants.WITH_PUNCTUTION_EXT;
            switch(searchMode){
                case TRANSCRIPT_BASED_INDEX:
                    indexIDs = Configuration.getTranscriptBasedIndexIDs();
                    str = Constants.WITHOUT_PUNCTUTION_EXT;
                    break;
                case SPEAKER_BASED_INDEX:
                    indexIDs = Configuration.getSpeakerBasedIndexIDs();
                    str = Constants.WITHOUT_PUNCTUTION_EXT;
                    break;
                case TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT:
                    indexIDs = Configuration.getTranscriptBasedIndexIDs();
                    break;
                case SPEAKER_BASED_INDEX_WITHOUT_PUNCT:
                    indexIDs = Configuration.getSpeakerBasedIndexIDs();
                    break;    
                default: // do nothing;
                    break;
                    
            }
            
            //System.out.println(indexIDs);

            if (!indexIDs.isEmpty()){
                ArrayList<String> indexPaths = new ArrayList();

                for (String corpusID: corpora){
                    System.out.println("Looking for " + corpusID);
                    File file = null;
                    for (String indexID: indexIDs){
                        if (indexID.substring(SEARCH_INDEX_PREXIF_LENGTH).startsWith(corpusID.replace("\"", "").trim()) && !indexID.endsWith(str)){
                            file = new File(Configuration.getSearchIndexPath(), indexID);
                            break;
                        }
                    }
                    if (file==null){
                        throw new IOException("Search index for " + corpusID + " does not exist. Please check the configuration file.");
                    }else{
                        indexPaths.add(file.getAbsolutePath());
                    }
                }
                
                //System.out.println("Index:" + String.join(",", indexPaths));
                return indexPaths;

            }else{
                throw new IOException("Search index is not specified. Please check the configuration file.");
            }

        } else {
           throw new IOException("You have not specified a valid corpus ID (search param 'corpusSigle=' in metadata query)");     
        }  
        
    }
    
    @Override
    protected DGD2SearchIndexTypeEnum getSearchIndex(String searchIndex) throws SearchServiceException {
        if(searchIndex==null || searchIndex.isEmpty() || searchIndex.equals("null")){
            return DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX;
        }else {
            try{
                DGD2SearchIndexTypeEnum index = DGD2SearchIndexTypeEnum.valueOf(searchIndex);
                return index;
            }catch (NullPointerException ex){
                    StringBuilder sb = new StringBuilder();
                    sb.append(". Search index ").append(searchIndex).append(" is not supported. Supported search indexes are: ");
                    for (DGD2SearchIndexTypeEnum ob : DGD2SearchIndexTypeEnum.values()){
                        sb.append(ob.name());
                        sb.append(", ");
                    }
                    throw new SearchServiceException(sb.toString().trim().replaceFirst(",$",""));
            }
        }
    }
  
    private Map<String, String> createLangMap(String str){
        Map<String, String> map = new HashMap();
        map.put(Locale.GERMAN.getLanguage(), myResourcesGER.getString(str));
        map.put(Locale.ENGLISH.getLanguage(),myResourcesEN.getString(str));
        return map;
    }
}
