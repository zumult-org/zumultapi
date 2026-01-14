/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.fcs;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bernd
 */
public class ClarinFCSEndpoint {
    
    @Path("/fcs/capability/basic-search")
    @Produces(MediaType.APPLICATION_XML)
    public class EndpointDescription {
    }
    
    
}
