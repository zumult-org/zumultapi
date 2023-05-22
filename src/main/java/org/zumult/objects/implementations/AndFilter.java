/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.w3c.dom.Element;
import org.zumult.objects.TokenFilter;

/**
 *
 * @author Thomas_Schmidt
 */
public class AndFilter implements TokenFilter {

    TokenFilter filter1;
    TokenFilter filter2;

    public AndFilter(TokenFilter filter1, TokenFilter filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }
    
    @Override
    public String getPreFilterXPath() {
        return filter1.getPreFilterXPath();
    }

    @Override
    public boolean accept(Element tokenNode) {
        return filter1.accept(tokenNode) && filter2.accept(tokenNode);
    }
    
}
