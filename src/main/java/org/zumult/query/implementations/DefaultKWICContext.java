/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.query.KWICContext;

/**
 *
 * @author Elena
 */
public class DefaultKWICContext implements KWICContext {
    private String type;
    private int length;

        @Override
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setLength(int n){
            this.length = n;  
        }

        @Override
        public int getLength() {
            return length;
        }

}
