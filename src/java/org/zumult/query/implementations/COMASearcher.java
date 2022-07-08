/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author bernd
 */
public class COMASearcher extends AbstractSearcher {

    int SEARCH_INDEX_PREXIF_LENGTH = 4;
    
    @Override
    DGD2SearchIndexTypeEnum getSearchIndex(String searchIndex) throws SearchServiceException {
        if(searchIndex==null || searchIndex.isEmpty() || searchIndex.equals("null")){
            return DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX;
        }else {
            try{
                DGD2SearchIndexTypeEnum index = DGD2SearchIndexTypeEnum.valueOf(searchIndex);
                return index;
            } catch (NullPointerException ex){
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

    @Override
    ArrayList<String> getIndexPaths(DGD2SearchIndexTypeEnum searchMode) throws IOException, SearchServiceException {
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

            if (indexIDs.size() > 0){
                ArrayList<String> indexPaths = new ArrayList();

                for (String corpusID: corpora){
                    String index_path = null;
                    for (String indexID: indexIDs){
                        if (indexID.substring(SEARCH_INDEX_PREXIF_LENGTH).startsWith(corpusID.replace("\"", "").trim()) && !indexID.endsWith(str)){
                            index_path = new StringBuilder(Configuration.getSearchIndexPath()) + indexID;
                            break;
                        }

                    }
                    if (index_path==null){
                        throw new IOException("Search index for " + corpusID + " does not exist. Please check the configuration file.");
                    }else{
                        indexPaths.add(index_path);
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
    
}
