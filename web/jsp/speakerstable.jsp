<%-- 
    Document   : eventstable
    Created on : 01.05.2018, 14:54:21
    Author     : Thomas_Schmidt
--%>

<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.List"%>
<%@page import="org.zumult.objects.Location"%>
<%@page import="org.zumult.objects.MetadataKey"%>
<%@page import="org.zumult.objects.Speaker"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="java.util.Set"%>
<%@page import="org.zumult.objects.Corpus"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Speakers</title>
        <style type="text/css">
            tr:nth-child(even) {background: #DDD}
            tr:nth-child(odd) {background: #FFF}
            body{font-size:smaller;}
        </style>
        
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <link rel="stylesheet" href="../css/overview.css"/>       

        <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.19/css/jquery.dataTables.css">
        <script
          src="https://code.jquery.com/jquery-3.3.1.js"
          integrity="sha256-2Kok7MbOyxpgUVvAk/HJ2jigOSYS2auK4Pfzbm7uH60="
          crossorigin="anonymous"></script>
        <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/1.10.19/js/jquery.dataTables.js"></script>
        <script type="text/javascript">
            $(document).ready( function () {
                $('#myTable').DataTable();
            } );            
        </script>
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
        
    </head>
    <body>
        <%
            String corpusID = request.getParameter("corpusID");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Corpus corpus = backendInterface.getCorpus(corpusID);
            String corpusName = corpus.getName("de");            
            Set<MetadataKey> metadataKeys = corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER);
            Set<String> locationTypes = corpus.getSpeakerLocationTypes();
            IDList speakerIDs = backendInterface.getSpeakers4Corpus(corpusID);           
        %>
        
        <% String pageName = "ZuMult"; %>
        <% String pageTitle = "Sprecherübersicht Korpus " + corpusID  + " - " + corpusName; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        
        <div class="row">
            <div class="col-sm-1">
            </div>
            <div class="col-sm-10">
        <table id="myTable" style="font-size:smaller;" class="stripe compact">
            <thead>
                <tr>
                    <th></th>
                    <%
                        for (MetadataKey metadataKey : metadataKeys){
                            String keyName = metadataKey.getName("de");

                    %>
                        <th><%=keyName%></th>    
                    <%
                        }
                    %>
                    <th>Locations</th> 
                    <!-- <th>Time</th> -->
                </tr>
            </thead>
            <%
             //   Long time = System.currentTimeMillis();
                for (String speakerID : speakerIDs){
               //     Long dur = System.currentTimeMillis() - time;
                //    time = System.currentTimeMillis();
                    Speaker speaker = backendInterface.getSpeaker(speakerID);
            %>
                    <tr>
                        <th><%=speakerID%></th>
                        <% 
                            for (MetadataKey metadataKey : metadataKeys){ 
                            String metadataValue = speaker.getMetadataValue(metadataKey);
                        %>
                        <td><%=metadataValue%></td>
                        <%}%>  
                        <td>
                        <%
                            for (String locationType : locationTypes){
                                List<Location> locations = speaker.getLocations(locationType);
                                for (Location location : locations){
                                    String locationString = locationType + ": " + location.getPlacename() + " / " + location.getCountry();
                                    %>
                                    <%= locationString %><br/>
                                    <%
                                }
                            }
                        %>
                        </td>
                    </tr>
            <%
                }
            %>
        </table>
            </div>
        <div class="col-sm-1">
        </div>
            </div>
        
    </body>
</html>
