/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.query.KWIC;
import org.zumult.query.KWICContext;

/**
 *
 * @author Elena
 */
public abstract class AbstractKWIC implements KWIC {

    private final DefaultKWICContext leftContext = new DefaultKWICContext();
    private final DefaultKWICContext rightContext = new DefaultKWICContext();
    
    @Override
    public KWICContext getLeftContext() {
        return leftContext;
    }

    @Override
    public KWICContext getRightContext() {
        return rightContext;
    }
        
    public void setLeftContext(String type, int length){
        leftContext.setType(type);
        leftContext.setLength(length);
    }
    
    public void setRightContext(String type, int length){
        rightContext.setType(type);
        rightContext.setLength(length);
    }

}
