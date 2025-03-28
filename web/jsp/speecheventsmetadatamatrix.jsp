<%-- 
    Document   : speecheventsmetadatamatrix
    Created on : 02.05.2018, 14:40:38
    Author     : Thomas_Schmidt
--%>

<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="org.zumult.objects.MetadataKey"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="org.zumult.objects.Corpus"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Speech Event metadata matrix</title>
        <style type="text/css">
            tr:nth-child(even) {background: #DDD}
            tr:nth-child(odd) {background: #FFF}
            td {border: 1px solid; font-weight:bold; text-align:center;}
        </style>
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                        
    </head>
    <body>
        <h1>Speech Event metadata matrix</h1>
        <%
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            IDList corpora = backendInterface.getCorpora();
            Set<MetadataKey> metadataForAllCorpora = new HashSet<>();
            for (String corpusID : corpora){
                Corpus corpus = backendInterface.getCorpus(corpusID);
                Set<MetadataKey> metadataForThisCorpus = corpus.getMetadataKeys(ObjectTypesEnum.SPEECH_EVENT);
                metadataForAllCorpora.addAll(metadataForThisCorpus);
            }
            List<MetadataKey> metadataList = new ArrayList<>();
            metadataList.addAll(metadataForAllCorpora);            
        %>
        <table>
            <tr>
                <th></th>
                <%
                    for (MetadataKey metadata : metadataList){
                        String metadataName = metadata.getName("de");
                %>
                <th><%=metadataName%></th>        
                <%    
                    }                   
                %>
            </tr>
            <%
            for (String corpusID : corpora){
                Corpus corpus = backendInterface.getCorpus(corpusID);
                Set<MetadataKey> metadataForThisCorpus = corpus.getMetadataKeys(ObjectTypesEnum.SPEECH_EVENT);                
            %>
            <tr>
                <th><%=corpusID%></th>
            <%    
                    for (MetadataKey metadata : metadataList){ 
                        String s = "-";
                        if (metadataForThisCorpus.contains(metadata)){
                            s = "+";
                        }
                        %>
                <td><%=s%></td>    
                <%    }%>
                        
            <%      }
            %>
            </tr>
            
        </table>
    </body>
</html>
