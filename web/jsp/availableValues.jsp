<%-- 
    Document   : availableValues
    Created on : 12.06.2019, 09:55:31
    Author     : Thomas.Schmidt
--%>

<%@page import="java.util.Collections"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="java.util.Set"%>
<%@page import="org.zumult.objects.MetadataKey"%>
<%@page import="org.zumult.objects.Corpus"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Available Metadata values</title>
        <style type="text/css">
            a.corpus_link {
                background: lightskyblue;
                padding: 5px;
                margin: 5px;
                border-radius: 2px;
                color: black;
                text-decoration: none;                            
            }
            a.key_link{
                font-size: 10pt;
                /*background: antiquewhite;*/
                color: black;
                padding: 3px;
                border-radius: 2px;
                text-decoration: none;     
            }
            a.selected {
                background : rgb(40,40,40);
                color : white;
            }
            div {
                padding : 5px;
                border-radius : 3px;
                border : 1px solid gray;
            }
            div#main {
               height: auto;
               overflow: hidden;
            }

            div#main_left {
                width: 180px;
                float: left;
                background: #aafed6;
            }

            div#main_right {
                background: #e8f6fe;
                /* the next props are meant to keep this block independent from the other floated one */
                width: auto;
                overflow: hidden;
            }​​            
        </style>
    </head>
    <body>
        <%
            String selectedCorpusID = request.getParameter("corpusID");
            String selectedMetadataKey = request.getParameter("metadataKey");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Corpus corpus = backendInterface.getCorpus(selectedCorpusID);
            String corpusName = corpus.getName("de");
            Set<MetadataKey> metadataKeys = corpus.getMetadataKeys();            
            IDList allCorpora = backendInterface.getCorpora();
        %>                
        <h1>Available Values</h1>
        <div id="top">
            <%
                for (String corpusID : allCorpora){
                    String selectionClass = "";
                    if (corpusID.equals(selectedCorpusID)){
                        selectionClass = " selected";
                    }
            %>
            <a class="corpus_link<%=selectionClass%>" href="availableValues.jsp?metadataKey=<%=selectedMetadataKey%>&corpusID=<%=corpusID%>">
                <%= corpusID.replaceAll("-", "") %>
            </a>
            &nbsp;
            <%        
                }
            %>
        </div>
        <div id="main">
            <div id="main_left">
                <ul>
                <% 
                    for (MetadataKey metadataKey : metadataKeys){
                        String id = metadataKey.getID();
                        String name = metadataKey.getName("de");                        
                        String selectionClass = "";
                        if (id.equals(selectedMetadataKey)){
                            selectionClass = " selected";
                        }
                %>
                    <li>    
                        <a class="key_link<%=selectionClass%>" href="availableValues.jsp?metadataKey=<%=id%>&corpusID=<%=selectedCorpusID%>">
                            <%= name %>
                        </a>
                    </li>
                
                <%
                    }
                %>
                </ul>
            </div>
            <div id="main_right">
                <% if (selectedCorpusID==null || selectedMetadataKey==null){ %>
                    <p>Incomplete selection: please select corpus and metadata key. </p>
                <% } else { %>
                    <h3>Available values for metadata key <%=selectedMetadataKey%> for corpus <%=selectedCorpusID.replaceAll("-","")%></h3>

                <%
                    IDList availableValues = backendInterface.getAvailableValues(selectedCorpusID, selectedMetadataKey); 
                    Collections.sort(availableValues);
                    for (String value : availableValues){ %>
                        <span><%=value%></span>
                        <br/>
                    <% }
                } %>
                
            </div>
        </div>
    </body>
</html>
