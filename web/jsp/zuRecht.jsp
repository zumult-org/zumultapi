<%-- 
    Document   : zuRecht
    Created on : 10.01.2020, 14:07:25
    Author     : Elena_Frick
--%>



<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.objects.AnnotationTagSet"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Arrays"%>
<%@page import="org.zumult.query.searchEngine.SortTypeEnum"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.io.FileNotFoundException"%>
<%@page import="java.util.Scanner"%>
<%@page import="java.io.File"%>
<%@page import="org.zumult.query.SampleQuery"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.zumult.io.Constants"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.backend.Configuration"%>
<%@page import="org.zumult.objects.IDList"%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    
String pageParam_q = request.getParameter("q");
String pageParam_cq = request.getParameter("cq"); // e.g. &cq=GWSS
List userSpecifiedCorpora = null;   
if (pageParam_cq!=null){
   String[] pageParam_corpora = pageParam_cq.split("\\|");
   userSpecifiedCorpora = Arrays.asList(pageParam_corpora);
}

String pageParam_form = request.getParameter("form");
String pageParam_context = request.getParameter("context");
String pageParam_leftContext = null;
String pageParam_rightContext = null;
if (pageParam_context!=null && Pattern.matches("\\d{1,2}-t,\\d{1,2}-t", pageParam_context)){
    String[] ct = pageParam_context.split(Constants.KWIC_LEFT_RIGHT_CONTEXT_DELIMITER);
    String[] lc = ct[0].split(Constants.KWIC_CONTEXT_DELIMITER);
    
    if (Integer.valueOf(lc[0]) > Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX){
        pageParam_leftContext = String.valueOf(Constants.KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX);
    }else if (Integer.valueOf(lc[0]) >= 0){
        pageParam_leftContext = lc[0];
    }
    
    String[] rc = ct[1].split(Constants.KWIC_CONTEXT_DELIMITER);
    if (Integer.valueOf(rc[0]) > Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX){
        pageParam_rightContext = String.valueOf(Constants.KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX);
    }else if (Integer.valueOf(rc[0]) >= 0){
        pageParam_rightContext = rc[0];
    }    
}

String pageParam_mode = request.getParameter("mode"); // e.g. &mode=SPEAKER_BASED_INDEX

// get part-of-speech tagset
BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
AnnotationTagSet annotationTagSet = backend.getAnnotationTagSet(Constants.DEFAULT_POS_TAGSET);
String annotationTagSetString = annotationTagSet.toXML().replaceAll("[\\t\\n\\r]+","").replaceAll("\\s+"," ").replaceAll("> <", "><");
String annotationTagSetXML = annotationTagSetString.replace("\"", "\\\"").replace("\'", "\\\'");

%>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuRecht</title>
        <!--<meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">-->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
                
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"></script>
        <script src="https://kit.fontawesome.com/ed5adda70b.js" crossorigin="anonymous"></script>
        
        
        <!-- <script src="https://unpkg.com/wavesurfer.js"></script>
        <script src="https://unpkg.com/wavesurfer.js/dist/plugin/wavesurfer.regions.min.js"></script>
        <script src="https://unpkg.com/wavesurfer.js/dist/plugin/wavesurfer.cursor.min.js"></script>
        <script src="https://unpkg.com/wavesurfer.js/dist/plugin/wavesurfer.timeline.min.js"></script> -->

        <script src="../js/jquery.twbsPagination.js" type="text/javascript"></script>
        <script src="../js/zuRecht.collapsible.js" type="text/javascript"></script>
        <script src="../js/query.stringConverter.js" type="text/javascript"></script>
        <script src="../js/zuRecht.corpusCheckbox.js" type="text/javascript"></script>
        <script src="../js/xslTransformation.js" type="text/javascript"></script>
        <link rel="stylesheet" type="text/css" href="../css/query.css" />
        
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                

    </head>
    <body>

        <div id="autocompleteForQueryInputField" class="list-group"></div>

        <% String restAPIBaseURL = Configuration.getRestAPIBaseURL(); %>
        <% String webAppBaseURL = Configuration.getWebAppBaseURL(); %>
        <% String defaultNumberForDownload =  String.valueOf(Constants.DEFAUL_NUMBER_FOR_KWIC_DOWNLOAD); %>
        <% String maxNumberForDownload =  String.valueOf(Constants.MAX_NUMBER_FOR_KWIC_DOWNLOAD); %>
      
        <!-- navigation  -->
        <%@include file="../WEB-INF/jspf/zuRechtNavBar.jspf" %>

        <br/>
        <div class="container-fluid">
            <div class="row">
                
                <!-- corpora -->
                <div class="col-md-2"> 
                    <%@include file="../WEB-INF/jspf/zuRechtCorpora.jspf" %>
                </div>

                <!-- workspace -->
                <div class="col-md-10">

                    <!-- Nav tabs -->
                    <ul class="nav nav-tabs small" role="tablist">
                        <li class="nav-item"><a class="nav-link active" data-toggle="tab" id="query-tab" href="#query-tab-content" role="tab"><%=myResources.getString("Query")%></a></li>
                    </ul>

                    <!-- Tab panes -->
                    <div class="tab-content">

                        <!-- Query tab -->
                        <div class="tab-pane mt-3 active" id="query-tab-content">
                            <h2><%=myResources.getString("SearchByQuery")%></h2>
                            <%@include file="../WEB-INF/jspf/zuRechtKWICSearchForm.jspf" %>
                            <%@include file="../WEB-INF/jspf/zuRechtKWICSearchOptionsModal.jspf" %>
                            <div id="kwic-search-result-area" class="searchResultArea"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <%@include file="../WEB-INF/jspf/metadataModal.jspf" %>
        <%@include file="../WEB-INF/jspf/videoModal.jspf" %>
  
        <%@include file="../WEB-INF/jspf/zuRechtConstants.jspf" %>
        <script type="text/javascript">
            var BASE_URL = '<%= Configuration.getWebAppBaseURL() %>';
            var languageTag = '<%=currentLocale.toLanguageTag()%>';
            var ajaxSearchRequest = null;
            var ajaxDownLoadRequest = null;
            var ajaxDownLoadMetadataRequest = null;
            var wavesurfer = null;
            var playMessage = true;
            var metadataValuesURL = '<%=restAPIBaseURL%>' + "/SearchService/metadataKeys/values";
            var annotationLayerValuesURL = '<%=restAPIBaseURL%>' + "/SearchService/annotationLayers/values";
            var downloadURL = "../downloads/";
            var DEFAULT_QUERY_LENGTH = 1000;
            var DEFAULT_KWIC_RIGHT_CONTEXT_FOR_REPETITIONS = 20;
            var zuRechtKWICResultView = "zuRechtKWICResultView.html";
            var printStyleForQueryHelp = "  <link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css'>\n\
                                            <style>\n\
                                                .query-example {background-color: #f3f3f3; color: #337AB7;}\n\
                                                .query-note span {background-color: #FFF7C8; display: table; padding: 10px; border-radius: 5px; border: 2px solid red;}\n\
                                                .pattern {color: #337AB7;}\n\
                                                .query-description {color: black;}\n\
                                            </style>";

                
            var posTagSetXML = new DOMParser().parseFromString('<%=annotationTagSetXML%>', "text/xml");
            var arrayWithPosToBeIgnored = ['AB','XY','NGIRR', 'ITJ', 'NGHES', 'ART'];
            let customVarMap = new Map();
    
            $(document).ready(function(){
                //alert("Page loaded");
                                
                // add tooltips
                $('[data-toggle="tooltip"]').tooltip();


                $('#corpus-info-button').on("click", function(){
                    var url = "https://exmaralda.org/de/exmaralda-demokorpus/";
                    window.open(url); 
                });
        
                $("#selectLang").on("change", function(){
                    var value = $(this).val();
                    var urlTest = new URL(window.location.href);
                    urlTest.searchParams.set('lang',value);
                    window.location = urlTest;
                });
 
                /************ click events for cancel buttons **********************************/
                
                $("#modal-searchTabOptions").find('.btnCancel').on('click',function(){
                    abortKWICSearchOptions(this, '#kwic-search-form');               
                });                

                /************ click events for ok buttons **********************************/
                                
                $("#modal-searchTabOptions").find('.btnOK').on('click',function(){
                    setKWICSearchOptions(this, '#kwic-search-form');
                });
                
                /********** click events for buttons with search options **********************/

                $('#kwic-search-form').find('.btn-open-search-options').on("click", function(){
                    $('#modal-searchTabOptions').modal('show');
                });
                
                /************* sample queries ****************/
                addSampleQueries();
                
                $('input[type=checkbox]').change(function () {
                    updateSampleQueries();
                });
                
                /* This function displays sample queries in the query input field */
                (function(){
                    var show = document.getElementById("queryInputField");
                    var instructions = document.getElementById("instructions");
                    var sample = document.getElementById("sampleQueries");
                    sample.addEventListener("change", function(){
                        var str = sample.value;
                        var txt = str.replace(/%22/g, "\"").replace(/%26/g, " & ")
                                .replace(/%3C/g, "<").replace(/%3E/g, ">").replace(/%2B/g, "+").replace(/%23/g, "#");
                        show.value=txt;
                        instructions.innerHTML="";
                    });
                })();


                // load html for displaying kwic results
                $("#kwic-search-result-area").load(zuRechtKWICResultView, function() {
             
                    $("#kwic-search-result-area").find(".wait-query-tab").html(searchLoadingIndicatorText).append(searchLoadingIndicatorAbortButton);
 

                    $("#kwic-search-form").submit(function(){
                                                
                        if(ajaxSearchRequest){
                            ajaxSearchRequest.abort();
                            ajaxSearchRequest = null;
                        }
          
           
                            // get input parameter
                            var q = $('#queryInputField').val().replace(/\“/g, "\"").replace(/\„/g, "\"");                    
                            var pageLength = $(this).find('.pageLength').val();
                            var pageIndex = $(this).find('.pageIndex').val();
                            var leftContext = $(this).find('.leftContext').val();
                            var rightContext = $(this).find('.rightContext').val();                
                            var context = leftContext+ "-t," + rightContext + "-t";
   
                                var corpusQueryStr = getCorpusQueryStr();

                                if (corpusQueryStr!==''){

    
                                    // delete search results of the previous query                
                                    $("#kwic-search-result-area").find('.paging_container').empty();     // delete paging
                                    $("#kwic-search-result-area").find(".query_summary").empty();        // delete the number of total hits
                                    $("#kwic-search-result-area").find('.KWICSearch-result').empty();    // delete the summary of the performed search 
                                    emptyPage('#kwic-search-result-area');                        // delete search results


                            
                                    // start loding indicator for ajax requests
                                    $("#kwic-search-result-area").find(".wait-query-tab").css("display", "block");

                                    // send request
                                    ajaxCallToGetKWIC('#kwic-search-result-area', kwicURL, q, corpusQueryStr, pageLength, pageIndex, context);
                                }
                            
                        
                        // prevent page reload
                        return false;

                    });      
               
                });
                
                                    
            });
            
                    
            function abortSearch(obj){
                $(obj).closest('.wait-query-tab').css("display", "none");
                var parentId = $(obj).closest('.searchResultArea').attr('id');
                if (parentId === "kwic-search-result-area"){
                    if(ajaxSearchRequest){
                            ajaxSearchRequest.abort();
                            ajaxSearchRequest = null;
                    }
                }
                
            }
            
            /**********************************************************/
            /*                      ajax calls                        */
            /**********************************************************/
            
            function ajaxCallToGetKWIC(selector, url, q, corpusQueryStr, pageLength, pageIndex, context){                        

                ajaxSearchRequest = $.ajax({
                    type: "POST",
                    url: url,
                    data: { q: q, cq :corpusQueryStr, count : pageLength, offset : pageIndex, context : context},
                    dataType: "text",

                    success: function(xml, status) { 
                        //alert(xml);
                        viewResult(selector, xml, url, context);
   
                    },
                    error: function(xhr, status, error){
                        if(xhr.status === 400 && !q.startsWith("[") && !q.startsWith("(") && !q.startsWith("<")){
                           ajaxSearchRequest = ajaxCallToGetKWIC(selector, url, completeSearchQuerySyntax(q, selector), corpusQueryStr, pageLength, pageIndex, context);                          
                        }else {
                            $(selector).find(".wait-query-tab").css("display", "none");
                            if (status === "abort"){
                                //ignore
                            }else {
                                if (xhr.responseText.startsWith("Please check the query syntax")){
                                    var errorMessage = xhr.responseText +  "\n\n<%=myResources.getString("QueryHelpMessage")%>";
                                    alert(errorMessage);
                                }else{
                                    // show error message
                                    var errorMessage = xhr.status + ' (' + xhr.statusText + ") " + xhr.responseText;
                                    alert('Error: ' + errorMessage);
                                }
                            }
                        }
                    }
                });
           }
    
            function ajaxCallToGetMoreKWIC(selector, url, queryString, corpusQueryStr, itemsPerPage, pageIndex, context){
                ajaxSearchRequest= $.ajax({
                        type: "POST",
                        url: url,
                        data: { q: queryString, cq :corpusQueryStr, count : itemsPerPage, offset : pageIndex, context : context},                      
                        dataType: "text",

                        success: function(xml, status) {
                            $(selector).find(".wait-query-tab").css("display", "none");                                                      
                            displayKWIC(selector, xml);                                    
                        },
                        error: function(xhr, status, error){
                            $(selector).find(".wait-query-tab").css("display", "none");
                            processError(xhr, status);                            
                        }
                    });
            }
            
            /*********************************************/
            /*             display kwic methods          */
            /*********************************************/
            
            function viewResult(selector, xml, url, context){
           
                $(selector).find(".wait-query-tab").css("display", "none");
                
                // get parameters of the search query
                var xmlDocument = $.parseXML(xml);
                var $xmlObject = $(xmlDocument);
                
                var totalHits = $xmlObject.find('total').text();
                var queryStr=$xmlObject.find('query').html();
                var corpusQueryStr=$xmlObject.find('corpusQuery').text(); 
                var corpora = getCorporaFromCorpusQuery(corpusQueryStr);  
                var itemsPerPage = $xmlObject.find('itemsPerPage').text();
                var repetitions = createXMLElement('<%= Constants.REPETITIONS%>', $xmlObject.find('repetitions').html());
                var synonyms = createXMLElement('<%= Constants.REPETITION_SYNONYMS%>', $xmlObject.find('synonyms').text());
                
                //display summary + button for opening metadata view (GET)

                var query = "<i>" + queryStr + "</i>" + " (in " + corpora.replace(/\s+\|\s+/g, ", ") + ")";
                var longQuery = query+ "<span class='link' id='showLessQuery'> (<%=myResources.getString("ShowLess")%>) </span>";
                var shortQuery = "<i>" + queryStr.substring(0, DEFAULT_QUERY_LENGTH) + "</i> ..." + 
                                                "<span class='link' id='showMoreQuery'> (<%=myResources.getString("ShowMore")%>) </span>";
                var length = queryStr.length;
                if(length>DEFAULT_QUERY_LENGTH){
                    query = shortQuery;
                }                              
                        
                addResultsHead(selector, query, queryStr, corpusQueryStr, longQuery, shortQuery);
                        
                if (parseInt(totalHits) > 0){
                    $(selector).find('.query_summary').append("<span id='search-results'><%=myResources.getString("Total")%>: </span>" + "<span id='total-hits'>" + totalHits + "</span>");
                            
                    // add pagination
                    addPagination(selector, url, totalHits, itemsPerPage, decodeHTMLQuery(queryStr), corpusQueryStr, context, repetitions, synonyms);

                    // add results
                    displayKWIC(selector, xml);
                }else {
                    $(selector).find('.KWICSearch-result h4').prepend('<%=myResources.getString("No")%>' + " ");
                    $(selector).find('.KWICSearch-result').find("a").remove();
                }
            }
            
            function displayKWIC(selector, xml){
                $(selector).find(".openXML-KWICSearch-area").css("display", "block");
                $(selector).find('.rowData-KWICSearch').text(xml);
                
                var data = new FormData();
                data.append('speakerInitialsToolTip', '<%=myResources.getString("ShowSpeakerMetadata")%>');
                data.append('transcriptIdToolTip', '<%=myResources.getString("ShowEventMetadata")%>');
                data.append('zuMultToolTip', '<%=myResources.getString("ShowExcerptInZuMult")%>');
                data.append('dgdToolTip', '<%=myResources.getString("ShowExcerptInDGD")%>');

                var fragment = transform(xml, "zuRechtKwic2Html.xsl", data); // implemented in xslTransformation.js
                $(selector).find('.myKWIC').html(fragment);
                
                $(selector).find(".kwic-tab").css("display", "block");
                $(selector).find('.KWICSearch-result').css("display", "block");

            }
                                  
            function addResultsHead(selector, query, queryStr, corpusQueryStr, longQuery, shortQuery){
           
                $(selector).find('.KWICSearch-result').empty();

                //display summary

                // display buttons for opening metadata view, grouping hits and download
                $(selector).find('.KWICSearch-result').append("<h4><%=myResources.getString("Results")%></h4><div class='clearfix'>\n\
                        <div class='float-left'><%=myResources.getString("ForSearching")%> " + query +"</div>");
                
                $("#showMoreQuery").on('click', function(){
                    addResultsHead(selector, longQuery, queryStr, corpusQueryStr, longQuery, shortQuery);
                });
                                    
                $("#showLessQuery").on('click', function(){
                    addResultsHead(selector, shortQuery, queryStr, corpusQueryStr, longQuery, shortQuery);
                });
            }
           
            function addPagination(selector, url, totalHits, itemsPerPage, queryString, corpusQueryStr, context, repetitions, synonyms){
                var paging = $("<ul></ul>").addClass("pagination-sm justify-content-center kwic-pagination");
                $(selector).find('.paging_container').append(paging);

                var totalPages_pre = (totalHits/itemsPerPage);
                var totalPages = (totalHits % itemsPerPage) === 0 ? totalPages_pre : totalPages_pre + 1;

                $(selector).find('.kwic-pagination').twbsPagination({
                    totalPages: totalPages,
                    visiblePages: 5,
                    initiateStartPageClick: false,
                    first: firstButtonLabel,
                    prev: prevButtonLabel,
                    next: nextButtonLabel,
                    last: lastButtonLabel,
                    onPageClick: function (event, page) {

                        //hide summary of the query and delete previous page
                        $(selector).find('.KWICSearch-result').css("display", "none");
                        emptyPage(selector);
                                                
                        // start loding indicator for ajax requests
                        $(selector).find(".wait-query-tab").css("display", "block");

                        var pageIndex = ((page-1)*itemsPerPage);
                                  
                        // send request
                        //if(selector==="#repetition-search-result-area"){

                        if(selector==="#kwic-search-result-area"){
                            ajaxCallToGetMoreKWIC(selector, url, queryString, corpusQueryStr, itemsPerPage, pageIndex, context);                  
                        }else{
                            ajaxCallToGetMoreKWICForRepetitions(selector, url, queryString, corpusQueryStr, itemsPerPage, pageIndex, context, repetitions, synonyms);
                        }
                      }
                });            
            }
                            
            function openMetadata(obj){
                let transcriptID = $(obj).data('transcriptid');
                $.post(
                    BASE_URL + "/ZumultDataServlet",
                    { 
                        command: 'getEventMetadataHTML',
                        transcriptID: transcriptID
                    },
                    function( data ) {
                        $("#metadata-body").html(data);
                        $("#metadata-title").html(transcriptID);
                        $('#metadataModal').modal("toggle");
                    }
                );                                    
            }
                         

            function openSpeakerMetadata(obj){
                let speakerID = $(obj).data('speakerid');
                let transcriptID = $(obj).data('transcriptid');
                $.post(
                    BASE_URL + "/ZumultDataServlet",
                    { 
                        command: 'getSpeakerMetadataHTML',
                        speakerID: speakerID,
                        transcriptID: transcriptID
                    },
                    function( data ) {
                        $("#metadata-body").html(data);
                        $("#metadata-title").html(speakerID);
                        $('#metadataModal').modal("toggle");
                    }
                );                    
            }

            function openTranscript(obj){
                $(obj).closest('form').submit();    
            }
            
            function playbackAudio(obj){
                let transcriptID = $(obj).data('transcriptid');
                let tokenID = $(obj).data('tokenid');
                $.post(
                    BASE_URL + "/ZumultDataServlet",
                    { 
                        command: 'getAudio',
                        transcriptID: transcriptID,
                        tokenID: tokenID
                    },
                    function( data ) {
                        let time = $(data).find("time").text();
                        let audioURL = $(data).find("audio").first().text();
                        if (audioURL.length === 0){
                            alert('No audio for ' + transcriptID);                            
                        } else {
                            insertAudioPlayer(obj, audioURL, Math.max(0.0, time - 1.0));
                        }
                    }
                );                    
        
            }
            
            function insertAudioPlayer(parent, audioURL, time){
                let randomID = 'id-' + Date.now() + '-' + Math.random().toString(36).substr(2, 5);
                let audioHTML = "<audio type=\"audio/x-wav\" src=\"" + audioURL + "\" id=\"" + randomID + "\"></audio>";
                let pauseHTML = "<i class=\"fa-solid fa-pause\"></i>";
                $(parent).html(audioHTML + pauseHTML);
                const audio = $('#' + randomID)[0];
                // Check if the audio is ready to play
                if (audio.readyState >= 2) { // 2 = HAVE_CURRENT_DATA
                    audio.currentTime = time; // Set the playback position
                    audio.play(); // Start playing
                } else {
                    // If the audio is not ready, wait until it is loaded
                    audio.addEventListener('canplay', function onCanPlay() {
                        audio.currentTime = time; // Set the playback position
                        audio.play(); // Start playing
                        audio.removeEventListener('canplay', onCanPlay); // Remove the event listener
                    });
                }  
                parent.onclick = function(){
                    stopAudio(this, randomID);
                };                
            }
            
            function stopAudio(parent, audioID){
                const audio = $('#' + audioID)[0];
                audio.pause();
                let playHTML = "<i class=\"fa-solid fa-play\"></i>";
                $(parent).html(playHTML);
                parent.onclick = function(){
                    playbackAudio(this);
                };                
            }
            
            
            function playbackVideo(obj){
                let transcriptID = $(obj).data('transcriptid');
                let tokenID = $(obj).data('tokenid');
                $.post(
                    BASE_URL + "/ZumultDataServlet",
                    { 
                        command: 'getVideo',
                        transcriptID: transcriptID,
                        tokenID: tokenID
                    },
                    function( data ) {
                        let time = $(data).find("time").text();
                        let videoURL = $(data).find("video").first().text();
                        if (videoURL.length === 0){
                            alert('No video for ' + transcriptID);                            
                        } else {
                            insertVideoPlayer(obj, videoURL, Math.max(0.0, time - 1.0));
                        }
                    }
                );                    
        
            }
            
            function insertVideoPlayer(parent, videoURL, time){
                //let randomID = 'id-' + Date.now() + '-' + Math.random().toString(36).substr(2, 5);
                let videoHTML = "<video id=\"modal-video\" controls=\"controls\" type=\"video/mp4\" src=\"" + videoURL +  "\"></video>";
                //let pauseHTML = "<i class=\"fa-solid fa-pause\"></i>";
                //$(parent).html(pauseHTML);
                $('#video-div').html(videoHTML);                
                const video = $('#modal-video')[0];
                // Check if the video is ready to play
                if (video.readyState >= 2) { // 2 = HAVE_CURRENT_DATA
                    video.currentTime = time; // Set the playback position
                    video.play(); // Start playing
                } else {
                    // If the audio is not ready, wait until it is loaded
                    video.addEventListener('canplay', function onCanPlay() {
                        video.currentTime = time; // Set the playback position
                        video.play(); // Start playing
                        video.removeEventListener('canplay', onCanPlay); // Remove the event listener
                    });
                }  
                $('#videoModal').modal("toggle");
                //parent.onclick = function(){
                    //stopVideo(this, randomID);
                //};                
            }
            
            function stopVideo(){
                const video = $('#modal-video')[0];
                video.pause();
                video.remove();
            }
      
            /**************************************************/
            /*             other help methods          */
            /**************************************************/

            function setKWICSearchOptions(obj, formSelector){
                var modalWindow = $(obj).closest('.modal');  
                configurePageLength(modalWindow, formSelector);
                configureContext(modalWindow, formSelector);
                configureSimpleSearchOption(modalWindow, formSelector);
                $(modalWindow).modal('hide');
            }
            
            function abortKWICSearchOptions(obj, formSelector){
                var modalWindow = $(obj).closest('.modal'); 
                abortConfigPageLength(modalWindow, formSelector);
                abortConfigContext(modalWindow, formSelector);
                abortSimpleSearchOption(modalWindow, formSelector);
            }
            
            function createXMLElement(name, content){
                if(content!==''){
                    content = "<" + name 
                        + ">" + content + "</" + name + ">";
                }
                return content;
            }            

            function getCorporaFromCorpusQuery(corpusQueryStr){
                var pattern = /corpusSigle=(.*)/;
                var match = pattern.exec(corpusQueryStr);
                var str = match[1].replace(/\"/g, "");
                return str;
            }            

            function configureSimpleSearchOption(selectorModal, selectorForm){ 
                var selectedLevel = $(selectorModal).find('.customSimpleQuerySyntaxLevel option:selected').val();
                $(selectorForm).find('.simpleQuerySyntaxLevel').val(selectedLevel);
            }
            
            function configurePageLength(selectorModal, selectorForm){
                var selectedOption = $(selectorModal).find('.customPageLength option:selected').text();
                $(selectorForm).find('.pageLength').val(selectedOption);       
            }
            
            
            function configureContext(selectorModal, selectorForm){
                var defaultContextLength = 5; // changed that from 3
                var regex = /^(0?\d|1\d|2[0-5])$/;
                var left = $(selectorModal).find(":text.customLeftContextLength").val();
                if (!left.match(regex)) {                        
                    $(selectorModal).find(":text.customLeftContextLength").val(defaultContextLength);
                    $(selectorForm).find(".leftContext").val(defaultContextLength);
                }else{
                    $(selectorForm).find(".leftContext").val(left); 
                }
                  
                var right = $(selectorModal).find(":text.customRightContextLength").val(); 
                
                if(selectorModal==="#modal-repetitionsTabOptions"){
                    regex = /^(0?\d|[1-3]\d|4[0-5])$/;
                    defaultContextLength = 20;
                }

                if (!right.match(regex)) {                        
                    $(selectorModal).find(":text.customRightContextLength").val(defaultContextLength);
                    $(selectorForm).find('.rightContext').val(defaultContextLength);
                }else{
                    $(selectorForm).find('.rightContext').val(right);
                }       
            }
   
            function abortSimpleSearchOption(selectorModal, selectorForm){
                var selectedOption = $(selectorForm).find('.simpleQuerySyntaxLevel').val();
                    $(selectorModal).find('.customSimpleQuerySyntaxLevel').val(selectedOption);
            }

            function abortConfigPageLength(selectorModal, selectorForm){
                var selectedOption = $(selectorForm).find('.pageLength').val();
                $(selectorModal).find('.customPageLength').val(selectedOption);
            }
            
            function abortConfigContext(selectorModal, selectorForm){
                var selectedOptionLeft = $(selectorForm).find(".leftContext").val(); 
                $(selectorModal).find(".customLeftContextLength").val(selectedOptionLeft);
                
                var selectedOptionRight = $(selectorForm).find(".rightContext").val(); 
                $(selectorModal).find(".customRightContextLength").val(selectedOptionRight);   
            }
         
            function completeSearchQuerySyntax(queryStr, selector){
                var simpleQuerySyntaxLevel = $(selector).parent().find("form:first").find('.simpleQuerySyntaxLevel').val();
                var q = "";        
                if(queryStr.startsWith('$')){
                    q = "["+simpleQuerySyntaxLevel+"="+ queryStr + "]";
                }else{
                    var split = queryStr.split(" ");
                    
                    for (var i = 0; i < split.length; i++) {
                        var w = split[i];
                        if(!w.startsWith("\"")){
                            w = "\""+w; 
                        }
                        if(!w.endsWith("\"")){
                            w = w+"\""; 
                        }
                        q = q+"["+simpleQuerySyntaxLevel+"="+ w + "]";
                        if (i<q.length-1){
                            q=q+" "; 
                        }
                    }
                }
                return q;
            }
            
            /* This function is used in updateSampleQueries() and in $(document).ready(function(){...})*/
            function addSampleQueries(){

                <% ArrayList<SampleQuery> queries2 = IOHelper.getQueriesFromFile(Constants.SAMPLE_QUERIES_FOR_TRASCRIPT_BASED_SEARCH);
                        for (int i = 0; i < queries2.size(); i++) { 
                            SampleQuery query = queries2.get(i); 
                        %>

                        addQuery('<%=query.getCorpus() %>', '<%=query.getQueryString() %>', '<%=query.getQueryString().replaceAll("\"", "%22") %>', '<%=query.getDescription().replaceAll("\'", "%22") %>');


                <%}%>
            }
  
            /* This function is used in addSampleQueries() */
            function addQuery(corpusStr, queryString, queryValue, description){

                var test = false;

                if(corpusStr === ""){
                    test=true;
                }else{

                    var checkedCorpora = [];

                    $.each($("input[name='corpus']:checked"), function(){
                        checkedCorpora.push($(this).val());
                    });


                    var corpora = corpusStr.split("|");
                    for (i = 0; i < corpora.length; i++) {

                        if(checkedCorpora.indexOf(corpora[i])>=0){
                            test=true;
                        }
                    }
                }


                if(test===true){                         
                    title = description.replace(/%22/g, "\'");

                    $('#sampleQueries').append($('<option>', {
                        value: queryValue,
                        text: queryString,
                        title: title    
                    }));
                }

            }

            /* This function updates sample queries depending on the selected corpus and search mode */
            function updateSampleQueries(){
                $('#sampleQueries').empty();
                addSampleQueries();
            }
     
            function emptyPage(selector){
               $(selector).find(".openXML-KWICSearch-area").css("display", "none");
               $(selector).find(".kwic-tab").css("display", "none"); 
               $("#wait-audio").css("display", "none");
               $(selector).find(".rowData-KWICSearch").empty();
               $(selector).find('.myKWIC').empty();
            }
            
            /* This function copies the search query string from the modal window and set it into the query input field */
            function copyQuery(obj) {

                // set corpora
                var corpora = getCorporaNames(obj);      
                setCorpora(corpora);
                
                // set query
                var str = $(obj).children(".float-left").text();
                var show = document.getElementById("queryInputField");
                show.value=str;
                
                // swith to the search tab
                $('#query-tab').tab('show');
                
                // close modal
                $('.search-help-modal').modal('hide');
            }
            
            function getCorporaNames(obj){
                var corporaText = $(obj).children(".float-right").text();
                var myRegexp = /Corp(ora|us):\s(.*)/;
                var match = myRegexp.exec(corporaText);
                var mySplitResult = match[2].split(",");
                return mySplitResult;
            }
                       
            function includeHTML(obj) {
                $(obj).find('[data-include]').each(function() {
                    var fileName = $(this).attr("data-include");
                    $(this).load(fileName);
                 });
            }

            function processError(xhr, status){
                if (status === "abort"){
                    //ignore
                }else {
                    // show error message
                    var errorMessage = xhr.status + ' (' + xhr.statusText + ") " + xhr.responseText;
                    alert('Error: ' + errorMessage);
                }
            }
  
            function setDefaulValue(obj){
                var value = $(obj).prop('defaultValue');
                $(obj).val(value);
            }

            function deselectOtherOptions(obj){
                if ($(obj).is(':checked')) {
                    if ( $(obj).hasClass( "withinContributionCheckBox" ) ){
                        $(obj).parent().parent().find('.outsideContributionCheckBox').prop("checked", false);
                    }else{
                        $(obj).parent().parent().find('.withinContributionCheckBox').prop("checked", false);
                    }
                }
            }
                       
            function setDefaultValueForSelect(obj){
                $(obj).find('option:selected').removeAttr("selected");
                $(obj).find("option[value=null]").prop('selected', 'selected');
            }

            // Add an event listener for when the modal is fully hidden
            $(document).on('hidden.bs.modal','#videoModal', function () {
                stopVideo();
            });            

        </script>
    </body>
</html>