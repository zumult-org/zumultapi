<%-- 
    Document   : zuHand
    Created on : 11.03.2020, 11:34:14
    Author     : Elena
--%>

<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.HashSet"%>
<%@page import="org.zumult.objects.implementations.ZumultVirtualCollection"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="java.io.File"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.zumult.backend.Configuration"%>
<%@page import="org.zumult.query.implementations.DGD2SearchIndexTypeEnum"%>
<%@page import="org.zumult.query.searchEngine.SortTypeEnum"%>
<%@page import="org.zumult.query.SearchStatistics"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.objects.Transcript"%>
<%@page import="org.zumult.io.Constants"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%
    String restAPIBaseURL = Configuration.getRestAPIBaseURL();
    String webAppBaseURL = Configuration.getWebAppBaseURL();
    String imageURL = "../images/thematic_prototype/";
    String url_zumal = webAppBaseURL + "/jsp/zuMal.jsp";
    String url_zuviel = webAppBaseURL + "/jsp/zuViel.jsp";
    
    String introTextShort = "Diese ZuMult-Seite richtet sich in erster Linie an DaF-/DaZ-LehrerInnen und bietet einen Zugang "
            + "zu annotierten <b>Handlungssequenzen</b> und "
            + "<b>themenbasierten Gesprächsausschnitten</b>, "
            + "die inklusive Audiodatei heruntergeladen und im Unterricht verwendet werden können. "
            + "Es handelt sich um manuell und selektiv aus dem FOLK-Korpus ausgewählte Ausschnitte aus unterschiedlichen privaten, "
            + "institutionellen und öffentlichen Interaktionen zwischen ...";

    String introText = "Diese ZuMult-Seite richtet sich in erster Linie an DaF-/DaZ-LehrerInnen und bietet einen Zugang "
            + "zu annotierten <b>Handlungssequenzen</b> und "
            + "<b>themenbasierten Gesprächsausschnitten</b>, "
            + "die inklusive Audiodatei heruntergeladen und im Unterricht verwendet werden können. "
            + "Es handelt sich um manuell und selektiv aus dem FOLK-Korpus ausgewählte Ausschnitte aus unterschiedlichen privaten, "
            + "institutionellen und öffentlichen Interaktionen zwischen zwei oder mehr SprecherInnen. <br/><br/>"
            + "Die <b>Handlungssequenzen</b> beinhalten: <br/><br/>"
            +"1. Sequenzen von <b>Gesprächseröffnungen und -beendigungen</b> mit (oder ohne) verschiedene(n) Formen zur <b>Begrüßung</b> und <b>Verabschiedung</b>, <br/>"
            + "2. Sequenzen mit verschiedenen <b>Modalverb-Verwendungen</b> im Präsens. Diese Sequenzen wurden mittels einer der folgenden kombinierten "
            + "Bezeichnungen für <b>Sprachhandlungen</b> vor-kategorisiert: <br/><br/>"
            + "<b>Frage/Bitte/Aufforderung</b> bezüglich Hilfe, Erklärungen, Informationen, Objekten, <br/>"
            + "<b>Vorschlag/Angebot</b> bezüglich Handlungen/Aktivitäten, Dienstleistungen, Objekten, <br/>"
            + "<b>Ratschlag/Empfehlung/Instruktion/Anweisung</b> bezüglich Informationen oder Handlungen/Aktivitäten (inklusive Beispielsequenzen mit Bitten um Rat). <br/><br/>"
            + "Über die Filter ist eine gezielte Suche nach den einzelnen Sequenztypen und nach den in ihnen enthaltenen einzelnen annotierten Formaten möglich. "
            + "Die jeweils gesuchten Formate und darauffolgende Reaktionen werden im Transkript-Prototypen ZuViel in der angezeigten Sequenz durch Unterstreichungen markiert. <br/><br/>"
            + "Die <b>Themenausschnitte</b> beinhalten: <br/><br/>"
            + "1. Ausschnitte mit Wortschatz zu den drei Themenbereichen <b>Schule und Ausbildung</b>, "
            + "<b>Essen</b>, <b>Haus und Wohnung</b> (vgl. die Wortschatzlisten im Vocabulary-Tab des ZuRecht-Prototypen) <br/>"
            + "2. Ausschnitte mit Wortschatz zu den drei Themenbereichen und zugleich einer annotierten Handlungssequenz "
            + "der Kategorie <b>Vorschlag/Angebot</b>, <b>Frage/Bitte/Aufforderung</b> oder <b>Ratschlag/Empfehlung/Instruktion/Anweisung</b> (vgl. die Auswahl von Handlungssequenzen).<br/><br/>"
            + "Über die Filter ist eine gezielte Suche nach Ausschnitten zu den einzelnen Themenbereichen "
            + "(auch kombiniert mit annotierten Handlungssequenzen) möglich. Die Ausschnitte werden mit einem Kurztitel aufgeführt und im Transkript-Prototypen ZuViel angezeigt. ";
    
    TreeMap<String, String> hm1 = new TreeMap();
    hm1.put("Darf ich X?", "<af=\"Darf ich X\\\\?\"/>");
    hm1.put("Dürfte ich X?", "<af=\"Dürfte ich X\\\\?\"/>");
    hm1.put("Ich mag X", "<af=\"Ich mag X\"/>");
    hm1.put("Ich möchte X", "<af=\"Ich möchte X\"/>");
    hm1.put("Ich will X", "<af=\"Ich will X\"/>");
    hm1.put("Kann ich X?", "<af=\"Kann ich X\\\\?\"/>");
    hm1.put("Kannst Du X?", "<af=\"Kannst Du X\\\\?\"/>");
    hm1.put("Können Sie X?", "<af=\"Können Sie X\\\\?\"/>");
    hm1.put("Könnte ich X?", "<af=\"Könnte ich X\\\\?\"/>");
    hm1.put("Magst Du (mir) X?", "<af=\"Magst Du \\\\(mir\\\\) X\\\\?\"/>");
    hm1.put("Sie könnten X", "<af=\"Sie könnten X\"/>");
    hm1.put("Willst Du (mir) X?", "<af=\"Willst Du \\\\(mir\\\\) X\\\\?\"/>");
    
    TreeMap<String, String> hm2 = new TreeMap();
    hm2.put("Du brauchst nicht X", "<af=\"Du brauchst nicht X\"/>");
    hm2.put("Du darfst X", "<af=\"Du darfst X\"/>");
    hm2.put("Du kannst X", "<af=\"Du kannst X\"/>");
    hm2.put("Du könntest X", "<af=\"Du könntest X\"/>");
    hm2.put("Du musst X", "<af=\"Du musst X\"/>");
    hm2.put("Du sollst X", "<af=\"Du sollst X\"/>");
    hm2.put("Du solltest X", "<af=\"Du solltest X\"/>");
    hm2.put("Ich brauche nicht X", "<af=\"Ich brauche nicht X\"/>");
    hm2.put("Ihr braucht nicht X", "<af=\"Ihr braucht nicht X\"/>");
    hm2.put("Ihr sollt X", "<af=\"Ihr sollt X\"/>");
    hm2.put("Ihr solltet X", "<af=\"Ihr solltet X\"/>");
    hm2.put("Ihr müsst X", "<af=\"Ihr müsst X\"/>");
    hm2.put("Man soll X", "<af=\"Man soll X\"/>");
    hm2.put("Man sollte X", "<af=\"Man sollte X\"/>");
    hm2.put("Sie brauchen nicht X", "<af=\"Sie brauchen nicht X\"/>");
    hm2.put("Sie dürfen X", "<af=\"Sie dürfen X\"/>");
    hm2.put("Sie können X", "<af=\"Sie können X\"/>");
    hm2.put("Sie könnten X", "<af=\"Sie könnten X\"/>");
    hm2.put("Sie müssen X ", "<af=\"Sie müssen X\"/>");
    hm2.put("Sie müssten X ", "<af=\"Sie müssten X\"/>");
    hm2.put("Soll ich X?", "<af=\"Soll ich X\\\\?\"/>");
    hm2.put("Wir müssen X", "<af=\"Wir müssen X\"/>");
    hm2.put("Wir wollen X", "<af=\"Wir wollen X\"/>");
    
    TreeMap<String, String> hm3 = new TreeMap();
    hm3.put("(Was) kann ich X?", "<af=\"\\\\(Was\\\\) kann ich X\\\\?\"/>");
    hm3.put("Du kannst X", "<af=\"Du kannst X\"/>");
    hm3.put("Du könntest X", "<af=\"Du könntest X\"/>");
    hm3.put("Ich kann X", "<af=\"Ich kann X\"/>");
    hm3.put("Lass uns X", "<af=\"Lass uns X\"/>");
    hm3.put("Lasst uns X", "<af=\"Lasst uns X\"/>");
    hm3.put("Magst Du X?", "<af=\"Magst Du X\\\\?\"/>");
    hm3.put("Möchten Sie X?", "<af=\"Möchten Sie X\\\\?\"/>");
    hm3.put("Möchtest Du X?", "<af=\"Möchtest Du X\\\\?\"/>");
    hm3.put("Soll ich X?", "<af=\"Soll ich X\\\\?\"/>");
    hm3.put("Sollen wir X?", "<af=\"Sollen wir X\\\\?\"/>");  
    hm3.put("Sollten wir X?", "<af=\"Sollten wir X\\\\?\"/>");  
    hm3.put("Was darf ich X?", "<af=\"Was darf ich X\\\\?\"/>");
    hm3.put("Willst Du X?", "<af=\"Willst Du X\\\\?\"/>");
    hm3.put("Wir können X", "<af=\"Wir können X\"/>");
    hm3.put("Wir könnten X", "<af=\"Wir könnten X\"/>");
    hm3.put("Wollen wir X?", "<af=\"Wollen wir X\\\\?\"/>");

    TreeMap<String, String> hm4 = new TreeMap();
    hm4.put("mit Begrüßung","<af=\"Begrüßung\"/>");
    hm4.put("ohne Begrüßung","!containing <af=\"Begrüßung\"/>");

    TreeMap<String, String> hm5 = new TreeMap();
    hm5.put("mit Verabschiedung","<af=\"Verabschiedung\"/>");
    hm5.put("ohne Verabschiedung","!containing <af=\"Verabschiedung\"/>");

    BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface(); 
    MetadataKey metadataKey_art = backendInterface.findMetadataKeyByID("v_" + "e_se_art");
    MetadataKey metadataKey_inhalt = backendInterface.findMetadataKeyByID("v_" + "e_se_inhalt");
        
    //get virtual collections
    File folder = new File(getServletContext().getRealPath("/"), "data");
    File[] listOfFiles = folder.listFiles();      
        
    ArrayList<ZumultVirtualCollection> collections = new ArrayList();
    for (File file : listOfFiles) {
        if (file.isFile() && file.getName().startsWith("VirtualCollection")) {
            Document doc = IOHelper.readDocument(file);
            String str = IOHelper.DocumentToString(doc);
            ZumultVirtualCollection collection = new ZumultVirtualCollection(str);
            collections.add(collection);
        }
    }
%>

<%@include file="../WEB-INF/jspf/locale.jspf" %>     

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuHand</title>
    
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
        
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"></script>
        
        <link rel="stylesheet" href="../css/overview.css"/>    
        
        <script src="../js/xslTransformation.js" type="text/javascript"></script>
        <script src="../js/query.searchResultXmlProcessor.js" type="text/javascript"></script>
        <script src="../js/query.stringConverter.js" type="text/javascript"></script>
        <style>

            .link {
                color:blue;
                cursor:pointer;
            }
            
            .nav-tabs > li > a.active {
                font-weight: bold;
            }
            
            .tooltip > .tooltip-inner {
                min-width: 300px;
                padding:0px;
            }

            .tooltip > .tooltip-inner td{
                padding-top: 0px;
                padding-bottom: 0px;
                width: 50%;
            }
  
        </style>
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                        
    </head>
    <body>
        <% String pageName = "ZuHand"; %>
        <% String pageTitle = "Auswahl von Handlungssequenzen und Themenausschnitten aus FOLK"; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>     
        
        <!-- loading indicator -->
        <div id="wait-page" class="ml-3">Loading... <img src='../images/loading.gif' width="64" height="64" alt="Loading indicator"/></div>
        
        <!-- page content -->
        <div id="page" style="display:none">
            
            <div class="row">

                <div class="col-sm-3">

                <!-- nav tabs -->
                    <div class="ml-3">
                        <ul class="nav nav-tabs justify-content-center pills-info" role="tablist" style="position: absolute; bottom: 0;">
                            <li class="nav-item">
                                <a class="nav-link active" data-toggle="tab" href="#actionAnno">Handlungssequenzen</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab" href="#topics">Themenausschnitte</a>
                            </li>
                        </ul>
                    </div>
                </div>

                <!-- intro -->
                <div class="col-sm-8">
                    <p class="lead pl-3" id="intro"><%=introTextShort%><span class="link" onclick='readMore(this)'>(weiter lesen)</span></p>
                </div>

                <!-- empty column -->
                <div class="col-sm-1"></div>

            </div>
        
        
            <div class="tab-content">
                
                <!-- tab for action annotations -->
                <div class="tab-pane active" id="actionAnno">
                    <div class="row">
                        
                        <!-- column for filters -->
                        <div class="col-sm-3 ml-3">                
                            <form class="mt-5">
                                <div class="form-group">
                                    <label for="filter1">Handlungssequenz</label>
                                    <select class="form-control" id="filter1">
                                        <option value="&lt;as=&quot;Beendigung&quot;/&gt;">Beendigung</option>
                                        <option value="&lt;as=&quot;Eröffnung&quot;/&gt;">Eröffnung</option>
                                        <option value="&lt;as=&quot;Frage/Bitte/Aufforderung&quot;/&gt;" selected>Frage/Bitte/Aufforderung</option>
                                        <option value="&lt;as=&quot;Ratschlag/Empfehlung/Instruktion/Anweisung&quot;/&gt;">Ratschlag/Empfehlung/Instruktion/Anweisung</option>
                                        <option value="&lt;as=&quot;Vorschlag/Angebot&quot;/&gt;">Vorschlag/Angebot</option>
                                    </select>
                                </div> 

                                <div class="form-group">
                                    <label for="filter2">Handlungsformat<br/>(Modalverb-Verwendung oder Grußformat)</label>
                                    <select class="form-control" id="filter2" name="corpusID"></select>
                                </div>
                            </form>
                        </div>
                        
                        <!-- column for result table -->
                        <div class="col-sm-8">
                            <!-- loading indicator -->
                            <div id="wait-tab-actionAnno" class="ml-3">Loading... <img src='../images/loading.gif' width="64" height="64" alt="Loading indicator"/></div>
                            <div class="row">
                                <div class="container" id ="result-actionAnno"></div>
                            </div>
                        </div>
                        
                        <!-- empty column -->
                        <div class="col-sm-1"></div>
                    </div>
                    
                </div>
            
                
                <!-- tab for thematic excerpts -->
                <div class="tab-pane" id="topics">
                <div class="row">
                    
                    <!-- column for filters -->
                    <div class="col-sm-3 ml-3">
                        <form class="mt-5">
                           <div class="form-group">
                                <label for="filter3">Themen</label>
                                <select class="form-control" id="filter3">

                                    <%  boolean selected = false;
                                    for (ZumultVirtualCollection collection : collections) {
                                         String title = collection.getTitle();
                                         String name = collection.getName();
                                            %>
                                            <option value="<%=name%>" <% if (!selected){ %> selected <% selected = true;}%>><%= title %></option>
                                            <%                
                                    }       
                                     %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="filter4">Annotierte Handlungssequenzen</label>
                                <select class="form-control" id="filter4"></select>
                            </div>
                        </form>

                    </div>
                                
                    <!-- column for speech events -->
                    <div class="col-sm-8">
                            <%  
                                int eventNumber = 0;
                                for (ZumultVirtualCollection collection : collections){
                                    String name = collection.getName();
                                    String collectionTitle = collection.getTitle();
                                    Map<String, Set<String>> map = new HashMap();
                                    Set<String> actions = new HashSet();
                                    for(VirtualCollectionItem item : collection){
                                        Document virtualCollectionItemDoc = item.getDocument();
                                        Element element = virtualCollectionItemDoc.getDocumentElement();
                                        String transcriptID = element.getAttribute("transcriptID");
                                        NodeList notes = element.getElementsByTagName("note");

                                        if(notes.getLength()== 0){
                                            String speechEventID = backendInterface.getSpeechEvent4Transcript(transcriptID);
                                            if (map.containsKey(speechEventID)){
                                                map.get(speechEventID).add(transcriptID);
                                            }else{
                                                Set<String> newSet = new HashSet();
                                                newSet.add(transcriptID);
                                                map.put(speechEventID, newSet);
                                            }           
                                        }else{
                                            String action = notes.item(0).getTextContent().split(";")[0];
                                            actions.add(action);
                                        }
                                    }

                                    for(String speechEventID :  map.keySet()){
                                        String action = "";
                                        List<String> list = new ArrayList(map.get(speechEventID)); 
                                        Collections.sort(list);

                                        %>
                                        <%@include file="../WEB-INF/jspf/zuHandSpeechEventView.jspf" %> 
                                        <% 
                                    }

                                    for (String action : actions){ 
                                        Map<String, Set<String>> mapForAction = new HashMap(); 
                                        for(VirtualCollectionItem item : collection){
                                            Document virtualCollectionItemDoc = item.getDocument();
                                            Element element = virtualCollectionItemDoc.getDocumentElement();
                                            String transcriptID = element.getAttribute("transcriptID");
                                            NodeList notes = element.getElementsByTagName("note");
                                            if(notes.getLength()> 0 && action.equals(notes.item(0).getTextContent().split(";")[0])){
                                                String speechEventID = backendInterface.getSpeechEvent4Transcript(transcriptID);
                                                if(mapForAction.containsKey(speechEventID)){
                                                    mapForAction.get(speechEventID).add(transcriptID);
                                                }else{
                                                    Set<String> newSet = new HashSet();
                                                    newSet.add(transcriptID);
                                                    mapForAction.put(speechEventID, newSet);
                                                }

                                            }
                                        }

                                        for(String speechEventID :  mapForAction.keySet()){
                                            List<String> list = new ArrayList(mapForAction.get(speechEventID)); 
                                            Collections.sort(list);                
                                       %>

                                        <%@include file="../WEB-INF/jspf/zuHandSpeechEventView.jspf" %> 

                                        <%

                                        }
                                    }       
                                }%>
                    </div>
                            
                    <!-- empty column -->
                    <div class="col-sm-1"></div>
                        
                </div>
                    
            </div>
        </div>    
         

        </div>    
        <div id="tmp1" hidden></div>
        <div id="tmp2" hidden></div>
        <div id="tmp3" hidden></div>
        
        <script type="text/javascript">
                        
            var corpusQueryStr = "corpusSigle=\"FOLK\"";
            var offset = 0;
            var mode = '<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX.name() %>';
            var speakerMode = '<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name() %>';
            var kwicURL = '<%=restAPIBaseURL%>' + "/SearchService/kwic";
            var pageLength = "250";
            var zuHandStyleSheet = "zuHand.xsl";
            var zuHandStyleSheetOpenTable = "zuHandOpenTable.xsl";
            var zuHandStyleSheetSpeakerTable = "zuHandSpeakerTable.xsl";
            var zuHandStyleSheetTranscriptTable = "zuHandTranscriptTable.xsl";
            var zuHandStyleSheetTokenIds = "zuHandTokenIds.xsl";

            $(document).ready(function(){  
                $("#selectLang").html("<option value='de' selected>Deutsch</option>");
                $("#selectLang").prop('disabled', true);
            
                addFilter2();
                addFilter4();
                
                $('#filter1').change(function () {
                    addFilter2();
                    search();
                });
                
                $('#filter2').change(function () {
                    search();
                });
                
                $('#filter3').change(function () {
                    addFilter4();
                    displayEvents();
                });
                
                $('#filter4').change(function () {
                    displayEvents();
                });
                               
                $("#wait-page").css("display", "none");
                $("#page").css("display", "block");
                
                displayEvents();
                search();
                        
                });
            
            function displayEvents(){
                var sel3 = $('#filter3').val();
                var sel4 =  $('#filter4').val(); 
                if(sel4!==""){
                    sel4 = "Handlungssequenz_" + sel4.replace(/\//g, "_");;
                }
                var eventClass = "myEvent-" + sel3 + "-" + sel4;
                $(".myEvent").each(function(){                  
                    $(this).css("display", "none");                
                });
               
                $("." + eventClass).each(function(){                  
                    $(this).css("display", "block");
                    
                    

                    // test if measures should be added
                    var test = $(this).find("table").find(".eventTable > tr").length;                  
                    if(test>2){
                        return;
                    }
                    
                    // add images
                    $(this).find(".myImage").each(function(){
                        loadImage(this);
                    });
                                        
                    $(this).find('.linkWithToolTip').each(function(){
                        var card = $(this).find(".myToolTip").html();                        
                        $(this).tooltip({title: card, html: true, trigger: "hover", sanitize: false, placement: 'left'}); 
                    });
                    
                    
                    // add measures
                    var obj = $(this).find("table").find(".eventTable");
                    var eventID = $(this).find(".eventTitle").attr('data-eventID');
                    var url = '<%=restAPIBaseURL%>' + "/metadata/speechEvent/"+eventID+"/measureValue";
                    $.ajax({
                        type: "GET",
                        url: url,
                        data: { type: "normRate", key : "normRate" },
                        dataType: "text",

                        success: function(text1, status) {
                            var a = Number(text1);
                            text1 = a.toFixed(0) + "%";

                            $.ajax({
                                type: "GET",
                                url: url,
                                data: { type: "intersection", reference: "GOETHE_B1", key : "tokens_ratio" },
                                dataType: "text",

                                success: function(text2, status) {
                                    var b = Number(text2*100);
                                    text2 = b.toFixed(0) + "%";
                                    $.ajax({
                                        type: "GET",
                                        url: url,
                                        data: { type: "articulationRate", key : "articulationRate" },
                                        dataType: "text",

                                        success: function(text3, status) {


                                            $.ajax({
                                                type: "GET",
                                                url: url,
                                                data: { type: "perMilTokensOverlapsWithMoreThan2Words", key : "perMilTokensOverlapsWithMoreThan2Words" },
                                                dataType: "text",

                                                success: function(text4, status) {


                                                    obj.prepend("<tr class='measure'><td style='width:250px'>Maße der ges. Interaktion:</td>\n\
                                                    \n\<td>Normalisierungsrate: "+ text1+", Überlappungen: "+text4+", Artikulationsrate: "+text3+", Deckung Goethe B1: "+text2+"\n\
                                                    <a target='_blank' href='<%=url_zumal%>' class='btn btn-outline-info btn-sm ml-1 mb-1 pt-0 pb-0' style='font-size:small;' role='button'>\n\
                                                    <span class='fa fa-info-circle'></span> ZuMal</a></td></tr>");

                                                },
                                                error: function(xhr, status, error){
                                                            var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                                            alert('Error: ' + errorMessage); 
                                                }
                                            }); 

                                        },
                                        error: function(xhr, status, error){
                                                    var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                                    alert('Error: ' + errorMessage); 
                                        }
                                    }); 


                                },
                                error: function(xhr, status, error){
                                            var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                            alert('Error: ' + errorMessage); 
                                }
                            }); 

                        },
                        error: function(xhr, status, error){
                                    var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                    alert('Error: ' + errorMessage); 
                        }
                    }); 
  
                });
                
            }
            
            function addFilter4(){
                $('#filter4').empty();
                $('#filter4').append($('<option>', {value: "", text: "keine"}));
                $('#filter4').append($('<option>', {value: "Frage/Bitte/Aufforderung",text: "Frage/Bitte/Aufforderung"}));
                
                var topic = $('#filter3').find("option:selected").text();
                if(topic!=="Essen"){
                    $('#filter4').append($('<option>', {value: "Ratschlag/Empfehlung/Instruktion/Anweisung", text: "Ratschlag/Empfehlung/Instruktion/Anweisung" }));
                }
                
                $('#filter4').append($('<option>', {value: "Vorschlag/Angebot", text: "Vorschlag/Angebot" }));
            }
            
            function addFilter2(){
                $('#filter2').empty();
                var as = $('#filter1').find("option:selected").text();
                
                if(as==="Frage/Bitte/Aufforderung"){         
                    <%  
                    for (Map.Entry<String, String> entry : hm1.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue(); 
                    
                    %>
                        $('#filter2').append($('<option>', {
                            value: '<%= value %>',
                            text: '<%= key%>' 
                        }));
                        
                    <% }%>
                }else if (as==="Ratschlag/Empfehlung/Instruktion/Anweisung"){
                    <%  
                    for (Map.Entry<String, String> entry : hm2.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue(); 
                    
                    %>
                        $('#filter2').append($('<option>', {
                            value: '<%= value %>',
                            text: '<%= key%>' 
                        }));
                        
                    <% }%>
                    
                }else if (as==="Vorschlag/Angebot"){
                    <%  
                    for (Map.Entry<String, String> entry : hm3.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue(); 
                    
                    %>
                        $('#filter2').append($('<option>', {
                            value: '<%= value %>',
                            text: '<%= key%>' 
                        }));
                        
                    <% }%>
                    
                }
                else if (as==="Eröffnung"){
                    <%  
                    for (Map.Entry<String, String> entry : hm4.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue(); 
                    
                    %>
                        $('#filter2').append($('<option>', {
                            value: '<%= value %>',
                            text: '<%= key%>' 
                        }));
                        
                    <% }%>
                    
                }
                else if(as==="Beendigung"){
                    <%  
                    for (Map.Entry<String, String> entry : hm5.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue(); 
                    
                    %>
                        $('#filter2').append($('<option>', {
                            value: '<%= value %>',
                            text: '<%= key%>' 
                        }));
                        
                    <% }%>
                    
                }
            }
            
            function createQuery(sel1, sel2){
                // e.g. (<as="Frage/Bitte.*"/>  fullyalignedwith <as.target="Ich mag X"/>) containing <af="Ich mag X"/>
                var query = sel1;
                if (sel2!==""){
                    if(sel2.lastIndexOf("!", 0) === 0){
                        query = query + " " + sel2;
                        
                    }else{
                        var target = getTarget(sel2);
                        query = "(" + sel1 + " fullyalignedwith <as.target=\".*" + target + ".*\"/>) containing " + sel2;
                    }
                }
                return query;
            }
            
            function getTarget(af){
               var pattern = /<af="(.+)"\/>/;
               var target = af.match(pattern, "$1")[1];
               return target;
            }
            
            function createQueryForSpeechEvent(sel1, sel2, speechEventID){
                var query = createQuery(sel1, sel2);
                var speechEventMetadataKey = '<%=Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID%>';
                return "(" + query + ")" + " within <"+ speechEventMetadataKey +"=\"" + speechEventID + "\"/>";
            }
            
            function search(){
                var sel1 = $('#filter1').val();
                var sel2 =  $('#filter2').val(); 
                var query = createQuery(sel1, sel2);                
                //alert(query);
                
                var count = 100;
                
                var speechEventMetadataKey = '<%=Constants.METADATA_KEY_SPEECH_EVENT_DGD_ID%>';
                
                var sortType = '<%= SortTypeEnum.ABS_DESC.name() %>';
            
                var statisticURL = '<%=restAPIBaseURL%>' + "/SearchService/statistics";
                $.ajax({
                    //example for GET: http://localhost:8084/DGDRESTTest/jsp/zuRechtMetadataStatisticView.jsp?q=<af/>&cq=corpusSigle=%22FOLK%22&metadataKeyID=t_dgd_kennung
                    type: "POST",
                    url: statisticURL,
                    data: { q: query, cq :corpusQueryStr, count : count, offset: offset, metadataKeyID : speechEventMetadataKey, mode: mode, sort: sortType },
                    dataType: "text",

                    success: function(xmlText, status) {
                        //alert(xmlText);
                                 
                        var fragment = transform(xmlText, zuHandStyleSheet, null);                       
                        $('#result-actionAnno').html(fragment);
                        
                        $("#wait-tab-actionAnno").css("display", "none");
                        addMetadata();
                        addTranscripts();
                        
                       

                    },
                    error: function(xhr, status, error){
                                var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                alert('Error: ' + errorMessage); 
                    }
                }); 
               
                   
                }
                                        
            function addMetadata(){
                $('#metadata-table thead tr').find('th.transcript').after("<th class='metadataValue'>Kurzbezeichnung (\"Art\")</th>");
                $('#metadata-table tbody tr').find('td.transcript').after("<td class='metadataValue' style='width: 350px; text-align: center;'></td>");               
                $('#metadata-table tbody tr').each(function (rowIndex, row){
                    var transcriptID = $(this).find('td.transcript').text();
                    var cell = $(this).find('td.metadataValue');
        
                    var metadataKeyID = "e_se_art";
                    var url = '<%=restAPIBaseURL%>' + "/metadata/transcript/" + transcriptID + "/" + metadataKeyID;
                    
                    // send request
                    $.ajax({
                        type: "GET",
                        url: url,
                        dataType: "text",

                        success: function(text, status) {
                            //alert(text);
                            cell.text(text);
                        },
                        
                        error: function(xhr, status, error){
                            var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                            alert('Error: ' + errorMessage);
                        }
                    });

                    
                });
            }
            
            function readMore(obj){
                var div = $(obj).parent().parent();
                div.empty();
                var text = '<%=introText%>';
                div.html("<p class='lead' id='intro'>" + text + "<span class='link' onclick='readLess(this)'>(weniger anzeigen)</span></p>");
            }
            
            function readLess(obj){
                var div = $(obj).parent().parent();
                div.empty();
                var text = '<%=introTextShort%>';
                div.html("<p class='lead' id='intro'>" + text + "<span class='link' onclick='readMore(this)'>(weiter lesen)</span></p>");
            }
            
            function openThematicExcerpt(obj){
                var form = $(obj).parent('form');
                var topic = $('#filter3').find("option:selected").text().toUpperCase().replace(/ /g, '_');
                form.append("<input type='hidden' name='wordlistID' value='"+topic+"'/>");
                
                var as = $('#filter4').find("option:selected").text();
                var af=$(obj).find(".badge").text();
                var myRegexp = /Format: (.*)/;
                var match = myRegexp.exec(af);
                var query = "<af=\""+ match[1].replace(/\?/g, "\\?") + "\"/> within <as=\"" + as + "\"/>";
                var transcriptID = $('input[name ="transcriptID"]', form).val();
                var url = '<%=restAPIBaseURL%>' + "/SearchService/transcript";
                $.ajax({
                    type: "POST",
                    url: url,
                    data: { q: query, cq :corpusQueryStr, transcriptID : transcriptID, mode: speakerMode, tokenAttribute: "id" },
                    dataType: "text",

                    success: function(text, status) {
                        //alert(text);
                        var fragment = transform(text, zuHandStyleSheetTokenIds, null);
                        $("#tmp3").html(fragment);
                        var highlightAF = $("#tmp3").text();
                        var query2 = "( <ar/> fullyalignedwith <ar.target=\".*" + match[1].replace(/\?/g, "\\?") + ".*\"/>) within <as=\"" + as + "\"/>";

                        $.ajax({
                            type: "POST",
                            url: url,
                            data: { q: query2, cq :corpusQueryStr, transcriptID : transcriptID, mode: speakerMode, tokenAttribute: "id" },
                            dataType: "text",

                            success: function(text, status) {
                                //alert(text);
                                var fragment = transform(text, zuHandStyleSheetTokenIds, null);
                                $("#tmp3").html(fragment);
                                var highlightAR = $("#tmp3").text();
                                form.append("<input type='hidden' name='highlightIDs2' value='"+ highlightAF +"'></input>"); 
                                form.append("<input type='hidden' name='highlightIDs3' value='"+ highlightAR+"'></input>");
                                form.submit();
                                },

                                error: function(xhr, status, error){
                                    var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                    alert('Error: ' + errorMessage);
                                }
                        });
                        
                        
                        },

                        error: function(xhr, status, error){
                            var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                            alert('Error: ' + errorMessage);
                        }
                });
              
            }
            
            function openExcerpt(obj){
                var form = $(obj).parent('form');
                form.append("<span id='wait' class='ml-3'>Loading...</span>");
                
                var as =  $('#filter1').val();
                var as_text = $('#filter1 option:selected').text();
                
                var af =  $('#filter2').val();
                var af_text = $('#filter2 option:selected').text();
                                
                if(af!==""){ 
                    if(af.lastIndexOf("!", 0) === 0){
                        form.append("<input type='hidden' name='customTitle' value='"+ as_text + " (" + af_text +")'></input>");
                        form.submit();
                    }else{
                        var transcriptID = $('input[name ="transcriptID"]', form).val();

                        // send request
                        var url = '<%=restAPIBaseURL%>' + "/SearchService/transcript";   
                        var query = af + " within " + as;
                        //alert(query);
                        
                        $.ajax({
                            type: "POST",
                            url: url,
                            data: { q: query, cq :corpusQueryStr, transcriptID : transcriptID, mode: mode, tokenAttribute: "id" },
                            dataType: "text",

                            success: function(text, status) {
                                //alert(text);
                                var fragment = transform(text, zuHandStyleSheetTokenIds, null);
                                $("#tmp1").html(fragment);
                                var extraHighlightIDs = $("#tmp1").text();
                                var target = getTarget(af);
                                var query2 = "( <ar/> fullyalignedwith <ar.target=\".*" + target + ".*\"/>) within " + as;
                                
                                $.ajax({
                                    type: "POST",
                                    url: url,
                                    data: { q: query2, cq :corpusQueryStr, transcriptID : transcriptID, mode: speakerMode, tokenAttribute: "id" },
                                    dataType: "text",

                                    success: function(text, status) {
                                        //alert(text);
                                        var fragment2 = transform(text, zuHandStyleSheetTokenIds, null);
                                        $("#tmp2").html(fragment2);
                                        var extraHighlightIDs2 = $("#tmp2").text();

                                        form.append("<input type='hidden' name='highlightIDs3' value='"+ extraHighlightIDs2 +"'></input>");
                                        form.append("<input type='hidden' name='highlightIDs2' value='"+ extraHighlightIDs +"'></input>");                                        
                                        form.append("<input type='hidden' name='customTitle' value='"+ as_text + " (" + af_text +")'></input>");
                                        form.find("#wait").remove();
                                        form.submit();
                                    },

                                    error: function(xhr, status, error){
                                        var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                        alert('Error: ' + errorMessage);
                                    }
                                });
                                
                            },

                            error: function(xhr, status, error){
                                var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                alert('Error: ' + errorMessage);
                            }
                        });
                    }
                }else{
                    form.append("<input type='hidden' name='customTitle' value='"+ as_text + "'></input>");
                    form.submit();
                }
                
            }   
  
            function addTranscripts(){                
                var sel1 = $('#filter1').val();                 
                var sel2 =  $('#filter2').val(); 
                
                $('#metadata-table tbody tr').each(function (rowIndex, row){
                    var speechEventID = $(this).find('td.transcript').text();
                    var cell = $(this).find('td.open');
                    var speakerCell = $(this).find('td.speaker');
                    var transcriptCell = $(this).find('td.transcriptID');
                    var query = createQueryForSpeechEvent(sel1, sel2, speechEventID);  
                    cell.empty();
                    cell.html("<div class='wait-for-annotations' style='display:block;'>Loading... <img src='../images/loading.gif' width='64' height='64' alt='Loading indicator'/></div>");
                    $.ajax({
                        type: "POST",
                        url: kwicURL,
                        data: { q: query, cq :corpusQueryStr, count : pageLength, offset : offset, mode : mode, context : "1-t,1-t" },
                        dataType: "text",

                        success: function(xml, status) {
                            var fragment = transform(xml, zuHandStyleSheetSpeakerTable, null);
                            var fragment2 = transform(xml, zuHandStyleSheetTranscriptTable, null);
                            var fragment3 = transform(xml, zuHandStyleSheetOpenTable, null);
                                
                            cell.empty();
                            speakerCell.html(fragment);
                            transcriptCell.html(fragment2);
                            cell.html(fragment3); 
                        },
                        
                        error: function(xhr, status, error){
                            var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                            alert('Error: ' + errorMessage);
                        }
                    });
                    
                });
            }
            
            function loadImage(obj){
                var span = $(obj);
                var transcriptID = obj.getAttribute('data-docID');
                var startAnnotationBlockID = obj.getAttribute('data-startAnnotationBlockID');

                // get video ID
                var url = '<%=restAPIBaseURL%>' + "/transcripts/transcript/" + transcriptID + "/video";
                $.ajax({
                    type: "GET",
                    url: url,
                    dataType: "text",
                    success: function(mediaID, status) { 
                        if(mediaID!==""){

                            // get start time for annotationBlock
                            var url2 = '<%=restAPIBaseURL%>' + "/transcripts/transcript/"+transcriptID+"/annotationBlocks/"+startAnnotationBlockID+"/startTime";
                            
                            $.ajax({
                                type: "GET",
                                url: url2,
                                dataType: "text",
                                success: function(time, status) { 
                                    
                                    var file = '<%=imageURL%>' + mediaID + "_" + time + ".png";
                                    span.append("<img class='mt-3' src='" + file + "' alt='image' width='70%' height='70%'/>");
                                      /*      var tmpImg = new Image() ;
                                            tmpImg.src = span.find('img').prop('src') ;
                                            tmpImg.onload = function() {
                                                span.find('.mySpinner').css("display", "none");
                                            } ;*/
                                  
                                },
                                error: function(xhr, status, error){
                                    var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                    alert('Error: ' + errorMessage);
                                }
                            });
                        }else{
                            span.find('.mySpinner').css("display", "none");
                        }


                    },
                    error: function(xhr, status, error){
                        var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                        alert('Error: ' + errorMessage);
                    }

                });

                
            }


        </script>
    </body>
</html>
