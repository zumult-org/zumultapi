<%-- 
    Document   : eventstable
    Created on : 01.05.2018, 14:54:21
    Author     : Thomas_Schmidt
--%>

<%@page import="java.util.Comparator"%>
<%@page import="java.util.TreeSet"%>
<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.zumult.objects.SpeechEvent"%>
<%@page import="org.zumult.objects.MetadataKey"%>
<%@page import="org.zumult.objects.Event"%>
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
        <title>Speech Events</title>
        <style type="text/css">
            tr:nth-child(even) {background: #DDD}
            tr:nth-child(odd) {background: #FFF}
            body{font-size:smaller;}
        </style>

        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <script src="https://kit.fontawesome.com/ed5adda70b.js" crossorigin="anonymous"></script>
        <link rel="stylesheet" href="../css/overview.css"/>       


        <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.19/css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/1.6.1/css/buttons.dataTables.min.css">
        <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/1.10.19/js/jquery.dataTables.js"></script>
        <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/buttons/1.6.1/js/dataTables.buttons.min.js"></script>
        <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/buttons/1.6.1/js/buttons.colVis.min.js"></script>
        

        <script type="text/javascript">
            var BASE_URL = '<%= Configuration.getWebAppBaseURL() %>';

            $(document).ready( function () {
                //$('#myTable').DataTable();
                $('#myTable').DataTable( {
                    dom: 'Bfrtip',
                    buttons: [
                        {
                            extend: 'colvis',
                            columns: ':not(.noVis)'
                        }
                    ]
                } );
            } );          

            function openTranscript(transcriptID){
                let url = "./zuViel.jsp?transcriptID=" + transcriptID;
                window.open(url, '_blank');    
            }
            
            function openMetadata(speechEventID){
                $.post(
                    BASE_URL + "/ZumultDataServlet",
                    { 
                        command: 'getEventHTML',
                        speechEventID : speechEventID
                    },
                    function( data ) {
                        $("#metadata-body").html(data);
                        $("#metadata-title").html(speechEventID);
                        $('#metadataModal').modal("toggle");
                    }
                );                                    
            }
        </script>
        
        
        
    </head>
    <body>
        <%
            String corpusID = request.getParameter("corpusID");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Corpus corpus = backendInterface.getCorpus(corpusID);
            String corpusName = corpus.getName("en");
            Set<MetadataKey> metadataKeys = corpus.getMetadataKeys(ObjectTypesEnum.SPEECH_EVENT);
            List<MetadataKey> sortedMetadataKeys = new ArrayList<>(metadataKeys);   
            sortedMetadataKeys.sort(new Comparator<MetadataKey>(){
                @Override
                public int compare(MetadataKey m1, MetadataKey m2){
                    return m1.getName("en").compareTo(m2.getName("en"));
                }
            });
            IDList eventIDs = backendInterface.getEvents4Corpus(corpusID);           
            IDList speechEventIDs = new IDList("speechEvent");
            for (String eventID : eventIDs){
                speechEventIDs.addAll(backendInterface.getEvent(eventID).getSpeechEvents());
            }
        %>
        <% String pageName = "ZuMult"; %>
        <% String pageTitle = "Speech event overview corpus  - " + corpusName; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        
        <div class="row">
            <div class="col-sm-1">
            </div>
            <div class="col-sm-10">
                <table id="myTable" style="font-size:smaller;" class="stripe compact">
                    <thead>
                        <tr>
                            <th></th>
                            <th>Name</th>
                            <%
                                for (MetadataKey metadataKey : sortedMetadataKeys){
                                    String keyName = metadataKey.getName("en");
                            %>
                                <th><%=keyName%></th>    
                            <%
                                }
                            %>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        //Long time = System.currentTimeMillis();
                        for (String speechEventID : speechEventIDs){
                          //  Long dur = System.currentTimeMillis() - time;
                          //  time = System.currentTimeMillis();
                            SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
                            String firstTranscriptID = speechEvent.getTranscripts().get(0);
                            String name = speechEvent.getName();
                    %>
                            <tr>
                                <th>
                                    <button onclick="openMetadata('<%= speechEventID %>')" type="button" class="btn btn-sm py-0 px-1" title="Show all metadata">
                                        <i class="fas fa-info-circle"></i>
                                    </button>
                                    <button onclick="openTranscript('<%= firstTranscriptID %>')" type="button" class="btn btn-sm py-0 px-1" 
                                            title="Open first transcript (<%= firstTranscriptID %>) in ZuViel">
                                        <i class="fa-regular fa-file-lines"></i>
                                    </button>
                                </th>
                                <th><%=name %></th>
                                <% 
                                    for (MetadataKey metadataKey : sortedMetadataKeys){ 
                                    String metadataValue = speechEvent.getMetadataValue(metadataKey);
                                %>
                                <td class="metadata"
                                    title="<%=metadataValue%>">
                                    <%=metadataValue%>
                                </td>
                                <%
                                    }
                                %>
                            </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </div>
            <div class="col-sm-1">
            </div>
        </div>
        <%@include file="../WEB-INF/jspf/metadataModal.jspf" %>
                    
    </body>
</html>
