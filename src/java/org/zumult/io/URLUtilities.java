/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Elena
 */
public class URLUtilities {
    
    public static String getWebApplicationURL(HttpServletRequest request){
       return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
        
}
