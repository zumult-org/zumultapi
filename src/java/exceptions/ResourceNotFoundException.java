/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.zumult.globalServices.GlobalServices;
import org.zumult.io.IOUtilities;

/**
 *
 * @author josip.batinic
 */

// this is just initial playing around with the exceptions. They will be taken care of at a later date
public class ResourceNotFoundException extends WebApplicationException {
    public ResourceNotFoundException(UriInfo context) {
        super(Response.status(Response.Status.NOT_FOUND)
                    .entity(IOUtilities.elementToString(new ErrorMessage(
                            "404", 
                            "There is a problem with the URL, check if corpus id is correct", 
                            "www.zumult.org", 
                            "technical info for debugging", 
                            context).buildErrorElement()))
                    .type(MediaType.APPLICATION_XML)
                    .build());
    }

    
}