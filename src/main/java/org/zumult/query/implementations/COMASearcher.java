/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.MetadataKey;
import org.zumult.query.SampleQuery;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchIndexType;
import org.zumult.query.implementations.DGDSearchIndexType.DGD2SearchIndexTypeEnum;
import org.zumult.query.searchEngine.COMASearchEngine;
import org.zumult.query.searchEngine.MTASBasedSearchEngine;

/**
 *
 * @author bernd
 */
public class COMASearcher extends AbstractSearcher {

    int SEARCH_INDEX_PREXIF_LENGTH = 4;
    
    @Override
    protected SearchIndexType getSearchIndexType(String searchIndex) throws SearchServiceException {
        return new DGDSearchIndexType(searchIndex);
    }

    @Override
    protected ArrayList<String> getIndexPaths(SearchIndexType searchMode) throws IOException, SearchServiceException{

        //System.out.println("PARAMETER (SEARCH MODE): " + index);
        Pattern r = Pattern.compile(Constants.CORPUS_SIGLE_PATTERN);
        Matcher m = r.matcher(metadataQuery.getCorpusQuery());
        if (m.find( )) {
            String[] corpora = m.group(1).split(Pattern.quote(Constants.CORPUS_DELIMITER));
   
            ArrayList<String> indexIDs = new ArrayList();
            String str = Constants.WITH_PUNCTUTION_EXT;
            switch(DGD2SearchIndexTypeEnum.valueOf(searchMode.getValue())){
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
    public ArrayList<SampleQuery> getSampleQueries(String corpusID, String searchIndex) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<MetadataKey> filterMetadataKeysForGroupingHits(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<MetadataKey> filterMetadataKeysForSearch(Set<MetadataKey> metadataKeys, String searchIndex, String type) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<AnnotationLayer> filterAnnotationLayersForGroupingHits(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<AnnotationLayer> filterAnnotationLayersForSearch(Set<AnnotationLayer> annotationLayers, String searchIndex, String annotationLayerType) throws SearchServiceException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    MTASBasedSearchEngine getSearchEngine(){
        return new COMASearchEngine();
    }
}
