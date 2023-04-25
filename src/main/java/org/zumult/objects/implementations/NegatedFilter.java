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
public class NegatedFilter implements TokenFilter {
    
    TokenFilter filter;

    public NegatedFilter(TokenFilter filter) {
        this.filter = filter;
    }

    
    @Override
    public String getPreFilterXPath() {
        return filter.getPreFilterXPath();
    }

    @Override
    public boolean accept(Element tokenNode) {
        return (!(filter.accept(tokenNode)));
    }
    
}
