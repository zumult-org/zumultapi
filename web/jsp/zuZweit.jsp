<%-- 
    Document   : crossquantification
    Created on : 04.05.2020, 18:59:06
    Author     : Thomas.Schmidt
--%>

<%@page import="org.zumult.objects.CrossQuantification"%>
<%@page import="org.zumult.objects.ObjectTypesEnum"%>
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
    
    String backendParam = request.getParameter("backendID");
    if (backendParam==null){
        backendParam = "ZUMULT";
    }
    
    String corpusID = request.getParameter("corpusID");
    if (corpusID==null){
        corpusID = "FOLK";
    }
   
System.out.println("Berechnung für: " + backendParam + " " + corpusID + " " + metaField1 + " " + metaField2);
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
            IDList corporaZumult = backendInterface.getCorporaForSearch(null);
            IDList corporaDGD = backendInterface.getCorpora();
            IDList corpora = null;

            MetadataKey metadataKey1 = backendInterface.findMetadataKeyByID(metaField1);
            MetadataKey metadataKey2 = backendInterface.findMetadataKeyByID(metaField2);
            
            String html  = null;
          
            if(backendParam.equals("DGD")){
                corpora = corporaDGD;
                BackendInterface backendInterfaceDGD = BackendInterfaceFactory.newBackendInterface("org.zumult.backend.implementations.DGD2");
                html  = backendInterfaceDGD.getCrossQuantification4Corpus(corpusID, metadataKey1, metadataKey2, units).toXML();
            }else{
                corpora = corporaZumult;
                html  = backendInterface.getCrossQuantification4Corpus(corpusID, metadataKey1, metadataKey2, units).toXML();
            }

            Corpus corpus = backendInterface.getCorpus(corpusID);
            List<MetadataKey> eventMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.EVENT), "de");                
            List<MetadataKey> speechEventMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.SPEECH_EVENT), "de");
            List<MetadataKey> speakerMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER), "de");
            List<MetadataKey> speakerInSpeechEventMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT), "de");
            
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
                        <label for="corpusSelector">Backend</label>
                        <select class="form-control" id="backendSelector" name="backendID" onchange="backendSelectionChanged()">
                            <option value="ZUMULT" 
                                <% if ("ZUMULT".equals(backendParam)){%> selected="selected" <%} %>>ZUMULT</option>
                            
                            <option value="DGD" 
                                <% if ("DGD".equals(backendParam)){%> selected="selected" <%} %>>DGD</option>                 
                        </select>
                    </div> 
                        
                    <div class="form-group">
                        <label for="corpusSelector"><%=myResources.getString("Corpus")%></label>
                        <select class="form-control" id="corpusSelector" name="corpusID" onchange="corpusSelectionChanged()">
                            <% for (String cID : corpora){ %>
                            <option value="<%= cID %>" 
                                <% if (cID.equals(corpusID)){%> selected="selected" <%} %>>
                                <%= cID %></option>
                            <% } %>                            
                        </select>
                    </div>                    

                    <div class="form-group">
                        <label for="metaField1Selector"><%=myResources.getString("Parameter")%> 1</label>
                        <select class="form-control" id="metaField1Selector" name="metaField1" onchange="meta1Changed()">
                            <% String metaField = metaField1; %>
                            <%@include file="../WEB-INF/jspf/metadataOptions.jspf" %>      
                        </select>
                    </div>                    
                    
                    <div class="form-group">
                        <label for="metaField2Selector"><%=myResources.getString("Parameter")%> 2</label>
                        <select class="form-control" id="metaField2Selector" name="metaField2" onchange="meta2Changed()">
                            <% metaField = metaField2; %>
                            <%@include file="../WEB-INF/jspf/metadataOptions.jspf" %>
                        </select>
                    </div>    
                        
                    <div class="form-group">
                        <label for="unitSelector"><%=myResources.getString("Unit")%></label>
                        <select class="form-control" id="unitSelector" name="units" onchange="unitChanged()">
                            <option value="TOKENS"
                                <% if (units.equals("TOKENS")){%> selected="selected" <%} %>>                            
                                Tokens
                            </option>
                         <!--   <option value="OBJECTS"
                                <% if (units.equals("OBJECTS")){%> selected="selected" <%} %>>                            
                                Speech Events / Speakers
                            </option>
                            <option value="TIME"
                                <% if (units.equals("TIME")){%> selected="selected" <%} %>>                            
                                Duration
                            </option>-->
                        </select>
                    </div>                    
                        
                        
                    <button type="submit" class="btn btn-primary mb-2"><%=myResources.getString("CalculateQuantification")%></button>                        


                </form>
            </div>
            <div class="col-sm-8" id="htmlTabelle">
                <p style="font-size:smaller;color:#DC4C64;" class="border border-danger rounded text-center py-1 px-1">
                    <%=myResources.getString("ZuZweitWarning")%>
                </p>
                <%= html %>
            </div>
            <div class="col-sm-1">
            </div>
        </div>
        <%@include file="../WEB-INF/jspf/zuzweit.jspf" %> 
    </body>
</html>
