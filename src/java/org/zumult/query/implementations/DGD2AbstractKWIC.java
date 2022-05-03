/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.io.Constants;
import org.zumult.query.KWICContext;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Elena
 */
public abstract class DGD2AbstractKWIC {
    private static final String DEFAULT_RIGHT_CONTEXT_ITEM = Constants.KWIC_DEFAULT_CONTEXT_ITEM;
    private static final String DEFAULT_LEFT_CONTEXT_ITEM = Constants.KWIC_DEFAULT_CONTEXT_ITEM;
    private static final int DEFAULT_RIGHT_CONTEXT_LENGTH = Constants.KWIC_DEFAULT_CONTEXT_LENGTH;
    private static final int DEFAULT_LEFT_CONTEXT_LENGTH = Constants.KWIC_DEFAULT_CONTEXT_LENGTH;
    
    private DGD2KWICContext leftContext = new DGD2KWICContext();
    private DGD2KWICContext rightContext = new DGD2KWICContext();
    
    public KWICContext getLeftContext() {
        return leftContext;
    }

    public void setLeftContext(DGD2KWICContext leftContext) {
        this.leftContext = leftContext;
    }

    public KWICContext getRightContext() {
        return rightContext;
    }

    public void setRightContext(DGD2KWICContext rightContext) {
        this.rightContext = rightContext;
    }
    
    private String checkItemSyntax(String str) throws SearchServiceException{
        switch (str) {
            case Constants.KWIC_CONTEXT_ITEM_FOR_TOKEN:
            case Constants.KWIC_CONTEXT_ITEM_FOR_CHARACTERS:
                return str;
            default:
                throw new SearchServiceException(str + " is not a supported context. The correct pattern is : 'context=3-t,3-t' or 'context=3-c,3-c'");
        }
    }
    
    public void setContext(String context) throws SearchServiceException{
        int rightContextLength = DEFAULT_RIGHT_CONTEXT_LENGTH;
        int leftContextLength = DEFAULT_LEFT_CONTEXT_LENGTH;
        String rightContextItem = DEFAULT_RIGHT_CONTEXT_ITEM;
        String leftContextItem = DEFAULT_LEFT_CONTEXT_ITEM;  
        if (context != null && !context.isEmpty()){
                try{
                    String[] ct = context.split(Constants.KWIC_LEFT_RIGHT_CONTEXT_DELIMITER);
                    String[] lc = ct[0].split(Constants.KWIC_CONTEXT_DELIMITER);
                    String[] rc = ct[1].split(Constants.KWIC_CONTEXT_DELIMITER);

                    leftContextItem = checkItemSyntax(lc[1]);
                    rightContextItem = checkItemSyntax(rc[1]);
                    
                    if (leftContextItem.equals(DEFAULT_LEFT_CONTEXT_ITEM)){
                        if (Integer.valueOf(lc[0]) > Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX){
                            leftContextLength = Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX;
                        }else if (Integer.valueOf(lc[0]) >= 0){
                            leftContextLength = Integer.valueOf(lc[0]);
                        }
                    }else{
                        throw new SearchServiceException("Please specify the context in tokens! Characters are not supported yet.");
                    }

                    if (rightContextItem.equals(DEFAULT_RIGHT_CONTEXT_ITEM)){
                        if (Integer.valueOf(rc[0]) > Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX){
                            rightContextLength = Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX;
                        }else if (Integer.valueOf(rc[0]) >= 0){
                            rightContextLength = Integer.valueOf(rc[0]);
                        }                   
                    }else{
                        throw new SearchServiceException("Please specify the context in tokens! Characters are not supported yet.");
                    } 
                }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
                    throw new SearchServiceException("Please check the context syntax. The correct pattern is : 'context=3-t,3-t' or 'context=3-c,3-c'");
                }
        }
        
        rightContext.setType(rightContextItem);
        rightContext.setLength(rightContextLength);
        leftContext.setType(leftContextItem);
        leftContext.setLength(leftContextLength);
    }
}
