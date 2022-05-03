<%-- 
    Document   : crossquantification
    Created on : 04.05.2020, 18:59:06
    Author     : Thomas.Schmidt
--%>

<%@page import="org.zumult.backend.Configuration"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.objects.Corpus"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    
    String metaField1 = request.getParameter("metaField1");
    String metaField2 = request.getParameter("metaField2");
    
    String units = request.getParameter("units");
    if (units==null){
        units = "TOKENS";
    }
    
    String corpusID = request.getParameter("corpusID");
    if (corpusID==null){
        corpusID = "FOLK";
    }
    
    String[][] PARAM = {
        {"META_FIELD_1", metaField1},
        {"META_FIELD_2", metaField2},
        {"UNITS", units}
    };
    
    String QUANT_FILENAME = corpusID + "_QUANT.xml";
    
    String html = new IOHelper().applyInternalStylesheetToInternalFile("/org/zumult/io/Quantify2Dimensions.xsl", 
            "/data/" + QUANT_FILENAME, PARAM);
%>
<!DOCTYPE html>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     

<html>
    <head>  
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuZweit : <%=myResources.getString("ZuZweitTitle")%></title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                

        <script src="../js/zuzweit.js"></script>
        
        <script>
            var BASE_URL = '<%= Configuration.getWebAppBaseURL() %>';   

            $(document).ready(function(){
        
                $("#selectLang").on("change", function(){
                    var value = $(this).val();
                    var urlTest = new URL(window.location.href);
                    urlTest.searchParams.set('lang',value);
                    window.location = urlTest;
                });
            });
            
        </script>
        
       
        <link rel="stylesheet" href="../css/overview.css"/>       
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
        
    </head>
    <body>
        <% 
            String pageTitle = myResources.getString("ZuZweitTitle"); 
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Corpus corpus = backendInterface.getCorpus(corpusID);
            Set<MetadataKey> eventMetadataKeys = corpus.getEventMetadataKeys();                
            Set<MetadataKey> speechEventMetadataKeys = corpus.getSpeechEventMetadataKeys();
            Set<MetadataKey> speakerMetadataKeys = corpus.getSpeakerMetadataKeys();
            Set<MetadataKey> speakerInSpeechEventMetadataKeys = corpus.getSpeakerInSpeechEventMetadataKeys();
        %>
            
        <% String pageName = "ZuZweit"; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        <div class="row">
            <div class="col-sm-1"></div>
            <div class="col-sm-2">
                <p style="font-size:smaller;">
                    <%=myResources.getString("ZuZweitExplain")%>
                </p>
                <form action="./zuZweit.jsp">
                    <div class="form-group">
                        <label for="corpusSelector"><%=myResources.getString("Corpus")%></label>
                        <select class="form-control" id="corpusSelector" name="corpusID" onchange="corpusSelectionChanged()">
                            <% for (String cID : backendInterface.getCorpora()){ %>
                            <option value="<%= cID %>" 
                                <% if (cID.equals(corpusID)){%> selected="selected" <%} %>>
                                <%= cID %></option>
                            <% } %>                            
                        </select>
                    </div>                    

                    <div class="form-group">
                        <label for="metaField1Selector"><%=myResources.getString("Parameter")%> 1</label>
                        <select class="form-control" id="metaField1Selector" name="metaField1" onchange="meta1Changed()">
                            <% for (MetadataKey metadataKey : eventMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>" 
                                <% if (metaField1.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                E: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            <% for (MetadataKey metadataKey : speechEventMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>"
                                <% if (metaField1.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                SE: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            <% for (MetadataKey metadataKey : speakerMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>"
                                <% if (metaField1.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                S: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            <% for (MetadataKey metadataKey : speakerInSpeechEventMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>"
                                <% if (metaField1.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                SES: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            
                        </select>
                    </div>                    
                    
                    <div class="form-group">
                        <label for="metaField2Selector"><%=myResources.getString("Parameter")%> 2</label>
                        <select class="form-control" id="metaField2Selector" name="metaField2" onchange="meta2Changed()">
                            <% for (MetadataKey metadataKey : eventMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>" 
                                <% if (metaField2.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                E: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            <% for (MetadataKey metadataKey : speechEventMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>"
                                <% if (metaField2.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                SE: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            <% for (MetadataKey metadataKey : speakerMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>"
                                <% if (metaField2.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                S: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            <% for (MetadataKey metadataKey : speakerInSpeechEventMetadataKeys){ %>
                            <option value="v_<%= metadataKey.getID()%>"
                                <% if (metaField2.endsWith(metadataKey.getID())){%> selected="selected" <%} %>>
                                SES: <%=metadataKey.getName("de") %></option>
                            <% } %>
                            
                        </select>
                    </div>    
                        
                    <div class="form-group">
                        <label for="unitSelector"><%=myResources.getString("Unit")%></label>
                        <select class="form-control" id="unitSelector" name="units" onchange="unitChanged()">
                            <option value="TOKENS"
                                <% if (units.equals("TOKENS")){%> selected="selected" <%} %>>                            
                                Tokens
                            </option>
                            <option value="OBJECTS"
                                <% if (units.equals("OBJECTS")){%> selected="selected" <%} %>>                            
                                Speech Events / Speakers
                            </option>
                            <option value="TIME"
                                <% if (units.equals("TIME")){%> selected="selected" <%} %>>                            
                                Duration
                            </option>
                        </select>
                    </div>                    
                        
                        
                    <button type="submit" class="btn btn-primary mb-2"><%=myResources.getString("CalculateQuantification")%></button>                        


                </form>
            </div>
            <div class="col-sm-8">
                <%= html %>
            </div>
            <div class="col-sm-1">
            </div>
        </div>
</html>
