/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

import java.io.Serializable;
import javax.ws.rs.core.UriInfo;
import org.jdom.Element;

/**
 *
 * @author josip.batinic
 */

// this is not the best way to do this. will take care of it subsequently
public class ErrorMessage {
    String status;
    String message;
    String link;
    String devMessage;
    UriInfo context;
//    Element errorElement;

    public ErrorMessage(String status, String message, String link, String devMessage, UriInfo context) {
        this.status = status;
        this.message = message;
        this.link = link;
        this.devMessage = devMessage;
        this.context = context;
    }

    public ErrorMessage() {        
    }
    
    public Element buildErrorElement() {
        Element errorElement = new Element("error");
        Element statusElement = new Element("status");
        Element messageElement = new Element("message");
        Element homeUrlElement = new Element("home_url");
        Element devMessageElement = new Element("dev_message");

        statusElement.addContent(status);
        messageElement.addContent(message);
        homeUrlElement.addContent(context.getBaseUri().toString());
        devMessageElement.addContent(devMessage);

        errorElement.addContent(statusElement);
        errorElement.addContent(messageElement);
        errorElement.addContent(homeUrlElement);
        errorElement.addContent(devMessageElement);
        
        return errorElement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDevMessage() {
        return devMessage;
    }

    public void setDevMessage(String devMessage) {
        this.devMessage = devMessage;
    }
    

    
}
