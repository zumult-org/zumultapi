/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 *
 * @author Thomas_Schmidt
 */
@ApplicationPath("api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
               
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(org.zumult.webservices.AdditionalMaterialResource.class);
        resources.add(org.zumult.webservices.CorporaResource.class);
        resources.add(org.zumult.webservices.HomeResource.class);
        resources.add(org.zumult.webservices.MediaResource.class);
        resources.add(org.zumult.webservices.MetadataResource.class);
        resources.add(org.zumult.webservices.QuantificationService.class);
        resources.add(org.zumult.webservices.ResourceService.class);
        resources.add(org.zumult.webservices.SearchService.class);
        resources.add(org.zumult.webservices.TranscriptsResource.class);
    }
    
}
