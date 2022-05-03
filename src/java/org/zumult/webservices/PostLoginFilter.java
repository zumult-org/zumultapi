/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Thomas.Schmidt
 */
public class PostLoginFilter implements Filter {

    protected FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
       this.filterConfig = filterConfig;
    }
    
    @Override
    public void destroy() {
        this.filterConfig = null;
    }
      // Called for every request that is mapped to this filter. 
     // If mapped to j_security_check, 
     // called for every  j_security_check action
    @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException, ServletException   {
        
        chain.doFilter(new PostLoginRequestWrapper((HttpServletRequest) request), response);         
     }
     
}

