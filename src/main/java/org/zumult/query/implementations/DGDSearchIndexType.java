/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.implementations;

import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchIndexType;

/**
 *
 * @author Frick
 */
public class DGDSearchIndexType implements SearchIndexType {
    DGD2SearchIndexTypeEnum type;

    DGDSearchIndexType(String searchIndexType) throws SearchServiceException{
        if(searchIndexType==null || searchIndexType.isEmpty() || searchIndexType.equals("null")){
            type = DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX;
        }else {
            try{
                type = DGD2SearchIndexTypeEnum.valueOf(searchIndexType);
            }catch (NullPointerException ex){
                    StringBuilder sb = new StringBuilder();
                    sb.append(". Search index ").append(searchIndexType).append(" is not supported. Supported search indexes are: ");
                    for (DGD2SearchIndexTypeEnum ob : DGD2SearchIndexTypeEnum.values()){
                        sb.append(ob.name());
                        sb.append(", ");
                    }
                    throw new SearchServiceException(sb.toString().trim().replaceFirst(",$",""));
            }
        }
    }

    @Override
    public String getValue() {
        return type.name();
    }

    public enum DGD2SearchIndexTypeEnum {
        SPEAKER_BASED_INDEX, TRANSCRIPT_BASED_INDEX, SPEAKER_BASED_INDEX_WITHOUT_PUNCT, TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT, 
    }
}
