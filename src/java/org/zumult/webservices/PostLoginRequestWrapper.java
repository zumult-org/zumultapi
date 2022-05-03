/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.exmaralda.exakt.utilities.FileIO;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.zumult.io.HTTPMethodHelper;

/**
 *
 * @author Thomas.Schmidt
 */
public class PostLoginRequestWrapper extends HttpServletRequestWrapper  {
    

    public PostLoginRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String username = request.getParameter("j_username");
        String password = request.getParameter("j_password");
        
        String[][] parameters = {
            {"v_user", username},
            {"v_pass", password}
        };
        String result = HTTPMethodHelper.callCommand("getNewSessionID", parameters);
        try {
            Document resultDoc = FileIO.readDocumentFromString(result);
            // CHECK IF THE RESULT CONTAINS A VALID SESSION ID
            if (resultDoc.getRootElement().getChild("error")==null){
                // THE RESULT CONTAINS A VALID SESSION ID
                // WE WRITE IT INTO THE SESSION COOKIE
                String oracle_session_id = resultDoc.getRootElement().getChildText("session_id");
                request.getSession().setAttribute("oracle_session_id", oracle_session_id);
                request.getSession().setAttribute("oracle_username", username);
                //SessionValidator.validateSessionID(oracle_session_id, request.getSession(true));
            } else {
                // THE RESULT DOES NOT COTAIN A VALID SESSION ID
                // WE REFER THE REQUEST TO THE EXTERNAL LOGIN PAGE
                //requestExternalLogin(request, response);
                // todo: send error message somehow
                return;
            }
        } catch (JDOMException ex) {
            // SOMETHING WENT COMPLETELY WRONG
            // WE THROW AN EXCEPTION
            Logger.getLogger(PostLoginRequestWrapper.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }            
        
    }


}    
