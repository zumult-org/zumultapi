package org.zumult.globalServices;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;


public class Link {
    private String linkurl;
    private String rel;

    public Link() {
    }

    @XmlValue
    public String getLinkUrl() {
        return linkurl;
    }

    public void setLinkUrl(String link_url) {
        this.linkurl = link_url;
    }
    
    @XmlAttribute
    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    @Override
    public String toString() {
        return "<link rel=" + rel + ">" + linkurl + "</link>";
    }

    
    
}
