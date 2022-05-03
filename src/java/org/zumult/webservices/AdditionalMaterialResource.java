/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.jdom.Element;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;

/**
 *
 * @author josip.batinic
 */
@Path("/additional-material")
@Produces(MediaType.APPLICATION_XML)
public class AdditionalMaterialResource {
    BackendInterface backendInterface;
    Response buildResponse = null;
    final String zumultApiBaseURL = Configuration.getRestAPIBaseURL();
    final String materialsPath = Configuration.getMaterialPath();
    IDList corpora;
    String xmlString;
    Element response = new Element("response");
    Element xmlDocument = new Element("xml_document");


    @Context
    private UriInfo context;

    public AdditionalMaterialResource() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
            this.corpora = backendInterface.getCorpora();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(AdditionalMaterialResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    public Response getCorpora() {
        for (String id : corpora) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(id).build().toString();
            Element listElement = new Element(corpora.getObjectName().toLowerCase() + "_url");
            listElement.setAttribute("id", id).addContent(linkUrl);
            response.addContent(listElement);
        }
        xmlString = IOUtilities.elementToString(response);
        return Response.ok(xmlString).build();
    }
    
    
    @GET
    @Path("/{corpusID}")
    public Response getAdditionalMaterials4Corpus(@PathParam("corpusID") String corpusID) throws MalformedURLException, IOException {
        Element additionalMaterials = new Element("additionalMaterials");
        String path = UriBuilder.fromPath(materialsPath).path(corpusID).build().toString();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        
        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(file.getName()).build().toString();
            Element material = new Element("additionalMaterial");
            material.setAttribute("id", file.getName()).addContent(linkUrl);
            additionalMaterials.addContent(material);
        }
        response.addContent(additionalMaterials);
//
        xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/{additionalMaterialID}.{ext}")
    public Response getAdditionalMaterialPDF4Corpus(@PathParam("corpusID") String corpusID,
                                                    @PathParam("ext") String ext,
                                                    @PathParam("additionalMaterialID") String additionalMaterialID) throws FileNotFoundException {
        String folderPath = UriBuilder.fromPath(materialsPath).path(corpusID).build().toString();
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        List fileNames = new ArrayList();
        for (File listOfFile : listOfFiles) {
            String fileName = listOfFile.getName();
            fileNames.add(fileName);
        }
        String path = UriBuilder.fromPath(materialsPath).path(corpusID).path(additionalMaterialID + "." + ext).build().toString();
        // some files have spaces in their names...
        path = path.contains("%20")
                ? path.replace("%20", " ")
                : path;
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        return !corpora.contains(corpusID) || !fileNames.contains(additionalMaterialID + "." + ext)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : ext.equals("pdf")
                        ? Response.ok(fis).type("application/pdf").build()
                        : Response.ok(fis).type(MediaType.TEXT_PLAIN).build();
    }
}
