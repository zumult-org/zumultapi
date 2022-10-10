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
<%@page import="org.zumult.query.implementations.DGD2SearchIndexTypeEnum"%>


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
String pageParam_count = request.getParameter("count");
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
        
        <script src="https://unpkg.com/wavesurfer.js"></script>
        <script src="https://unpkg.com/wavesurfer.js/dist/plugin/wavesurfer.regions.min.js"></script>
        <script src="https://unpkg.com/wavesurfer.js/dist/plugin/wavesurfer.cursor.min.js"></script>
        <script src="https://unpkg.com/wavesurfer.js/dist/plugin/wavesurfer.timeline.min.js"></script>

        <script src="../js/jquery.twbsPagination.js" type="text/javascript"></script>
        <script src="../js/zuRecht.collapsible.js" type="text/javascript"></script>
        <script src="../js/query.stringConverter.js" type="text/javascript"></script>
        <script src="../js/zuRecht.corpusCheckbox.js" type="text/javascript"></script>
        <script src="../js/xslTransformation.js" type="text/javascript"></script>
        <link rel="stylesheet" type="text/css" href="../css/query.css" />
        
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                

    </head>
    <body>
        <%@include file="../WEB-INF/jspf/zuRechtAutocompleteJS.jspf" %> 

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
                    <%@include file="../WEB-INF/jspf/zuRechtCorporaColumn.jspf" %>
                </div>

                <!-- workspace -->
                <div class="col-md-10">

                    <!-- Nav tabs -->
                    <ul class="nav nav-tabs small" role="tablist">
                        <li class="nav-item"><a class="nav-link active" data-toggle="tab" id="query-tab" href="#query-tab-content" role="tab"><%=myResources.getString("Query")%></a></li>
                        <li class="nav-item"><a class="nav-link" data-toggle="tab" id="vocabulary-tab" href="#vocabulary-tab-content" role="tab"><%=myResources.getString("Vocabulary")%></a></li>
                        <li class="nav-item"><a class="nav-link" data-toggle="tab" id="repetitions-tab" href="#repetitions-tab-content" role="tab"><%=myResources.getString("Repetitions")%></a></li>
                    </ul>

                    <!-- Tab panes -->
                    <div class="tab-content">

                        <!-- Query tab -->
                        <div class="tab-pane mt-3 active" id="query-tab-content">
                            <h2><%=myResources.getString("SearchByQuery")%></h2>
                            <%@include file="../WEB-INF/jspf/zuRechtKWICSearchForm.jspf" %>
                            <%@include file="../WEB-INF/jspf/zuRechtKWICSearchHelpModal.jspf" %>
                            <%@include file="../WEB-INF/jspf/zuRechtCustomVocabularyLists.jspf" %>
                            <%@include file="../WEB-INF/jspf/zuRechtKWICDownloadOptions.jspf" %>
                            <%@include file="../WEB-INF/jspf/zuRechtKWICSearchOptionsModal.jspf" %>
                            <%@include file="../WEB-INF/jspf/zuRechtAudioPlayInfoModal.jspf" %>
                            <div id="kwic-search-result-area" class="searchResultArea"></div>
                        </div>


                        <!-- Vocabulary tab -->
                        <div class="tab-pane mt-3" id="vocabulary-tab-content">
                            <div class="row">
                                <div class="col">

                                    <h2><%=myResources.getString("SearchVocabularyLists")%></h2>
                                    <%@include file="../WEB-INF/jspf/zuRechtVocabularySearchForm.jspf" %>

                                    <div id="wait-vocabulary-tab" style="display:none;"><%=myResources.getString("LoadingSearch")%> <img src='../images/loading.gif' width="64" height="64" alt="Loading indicator"/></div>

                                    <div class="openXML-StatisticSearch-area" style="display:none;">
                                        <p class="collapsible" onclick="openContent(this)"><%=myResources.getString("OpenXML")%></p>
                                        <div class="content rowData-Response"><p><pre id="rowData-StatisticSearch"></pre></div>
                                    </div>
                                    <div class="table-wrapper table-responsive" id="statistics-result"></div>

                                    <%@include file="../WEB-INF/jspf/zuRechtVocabularySearchHelpModal.jspf" %>
                                    <%@include file="../WEB-INF/jspf/zuRechtVocabularySearchOptionsModal.jspf" %>
                                    <%@include file="../WEB-INF/jspf/zuRechtThematicVocabularyLists.jspf" %>
                                              
                                </div>
                            </div>
                        </div>

                        <!-- Repetitions tab -->
                        <div class="tab-pane mt-3" id="repetitions-tab-content">
                            <h2><%=myResources.getString("SearchForRepetitions")%></h2>
                            <%@include file="../WEB-INF/jspf/zuRechtRepetitionSearchForm.jspf" %>
                            <%@include file="../WEB-INF/jspf/zuRechtRepetitionSearchOptionsModal.jspf" %>
                            <div id="repetition-search-result-area" class="searchResultArea"></div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
  
        <%@include file="../WEB-INF/jspf/zuRechtConstants.jspf" %>
        <%@include file="../WEB-INF/jspf/zuRechtRepetitionXMLCreator.jspf" %>
        <script type="text/javascript">
            var languageTag = '<%=currentLocale.toLanguageTag()%>';
            var ajaxSearchRequest = null;
            var ajaxDownLoadRequest = null;
            var ajaxDownLoadMetadataRequest = null;
            var ajaxSearchStatisticsRequest= null;
            var ajaxRepetitionSearchRequest = null;
            var wavesurfer = null;
            var playMessage = true;
            var repetitionURL = '<%=restAPIBaseURL%>' + "/SearchService/repetitions";
            var kwicExportURL = '<%=restAPIBaseURL%>' + "/SearchService/kwic/download";
            var metadataDownLoadURL = '<%=restAPIBaseURL%>' + "/SearchService/metadataKeys/IDs";
            var annotationTiersURL = '<%=restAPIBaseURL%>' + "/SearchService/annotationLayers";
            var metadataValuesURL = '<%=restAPIBaseURL%>' + "/SearchService/metadataKeys/values";
            var annotationLayerValuesURL = '<%=restAPIBaseURL%>' + "/SearchService/annotationLayers/values";
            var downloadURL = "../downloads/";
            var DEFAULT_QUERY_LENGTH = 1000;
            var DEFAULT_KWIC_RIGHT_CONTEXT_FOR_REPETITIONS = 20;
            var zuRechtQueryTabHelp = "zuRechtQueryHelp.html";
            var zuRechtVocabularyTabHelp = "zuRechtVocabularyHelp.html";
            var zuRechtKWICResultView = "zuRechtKWICResultView.html";
            var printStyleForQueryHelp = "  <title>Query Help</title>\n\
                                            <link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css'>\n\
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
                
                // add query builder to input fields                
                $('.inputFieldWithAutocomplete').each(function(){
                    addAutocompleteToInputField(this);
                });
                
                
                addPOSToBeIgnoredWhenSearchingRepetitions('.ignorePOSforDistance');

                $('#corpus-info-button').on("click", function(){
                    var url = '<%=webAppBaseURL%>' + "/jsp/corpusoverview.jsp#FOLK";
                    window.open(url); 
                });
        
                $("#selectLang").on("change", function(){
                    var value = $(this).val();
                    var urlTest = new URL(window.location.href);
                    urlTest.searchParams.set('lang',value);
                    window.location = urlTest;
                });
                             
                
                
                /********** setting context and page options according to url parameters **************/
                    <%    
                    
                    if(pageParam_count!=null){
                        %>
                        var countValue = '<%= pageParam_count%>';
                        $('#modal-searchTabOptions').find('.customPageLength option').each(function(){
                            if (this.value == countValue) {
                                $('#kwic-search-form').find('.pageLength').val(countValue);
                                $(this).parent().val(countValue);
                                return false;
                            }
                        });
                        <%
                    }
                    
                    
                    if(pageParam_leftContext!=null){
                        %>
                        var leftContextValue = '<%=pageParam_leftContext%>';
                        $('#modal-searchTabOptions').find(":text.customLeftContextLength").val(leftContextValue);
                        $('#kwic-search-form').find('.leftContext').val(leftContextValue);
                        <%
                    }

                    if(pageParam_rightContext!=null){
                    %>
                        var rightContextValue = '<%=pageParam_rightContext%>';
                        $('#modal-searchTabOptions').find('.customRightContextLength').val(rightContextValue);
                        $('#kwic-search-form').find('.rightContext').val(rightContextValue);
                        <%
                    }
                    %>
 
 
                /************ click events for cancel buttons **********************************/
                
                $("#modal-repetitionsTabOptions").find('.btnCancel').on('click',function(){
                    abortKWICSearchOptions(this, '#repetition-search-form');
                });
                
                $("#modal-searchTabOptions").find('.btnCancel').on('click',function(){
                    abortKWICSearchOptions(this, '#kwic-search-form');               
                });
                
                $("#modal-vocabularyTabOptions").find('.btnCancel').on('click',function(){
                    var selectedOption = $('#numberOfDocs').val();
                    $('#customNumberOfDocs').val(selectedOption);
                    
                    
                    var radioValue = $("#sortType").val();
                    
                    $('input[name=customSortType]', '#modal-vocabularyTabOptions').each(function () {
                        if($(this).val()===radioValue){
                            $(this).prop('checked', true);
                        }else{
                            $(this).prop('checked', false);
                        }
                    });

                });
                

                /************ click events for ok buttons **********************************/
                
                $("#modal-repetitionsTabOptions").find('.btnOK').on('click',function(){
                    setKWICSearchOptions(this, '#repetition-search-form');
                });
                
                $("#modal-searchTabOptions").find('.btnOK').on('click',function(){
                    setKWICSearchOptions(this, '#kwic-search-form');
                });

                $("#modal-myVocabularyLists").find('.btnOK').on('click',function(){
                    checkAndSaveCustomVariables("#modal-myVocabularyLists");
                });
                

                
                $("#modal-audioPlayNotShowAnymore-btn").on('click', function(){
                    playMessage = false;
                    $('#modal-audioPlayNotShowAnymore').modal('hide');
                });
                                
                $("#modal-vocabularyTabOptions").find('.btnOK').on('click',function(){
                    var selectedOption = $('#customNumberOfDocs option:selected').text();
                    $("#numberOfDocs").val(selectedOption);
                    
                    var radioValue = $('input[name=customSortType]:checked', '#modal-vocabularyTabOptions').val();
                    $("#sortType").val(radioValue);
                    
                    $('#modal-vocabularyTabOptions').modal('hide');
                });
                
                /********** click events for buttons with help infos **********************/
                
                $('#kwic-search-form').find('.btn-open-help').on("click", function(){
                    $('#modal-queryHelp').modal('show').find('.modal-content').load(zuRechtQueryTabHelp);
                }); 
                
                $('#vocabulary-search-form').find('.btn-open-help').on("click", function(){
                    $('#modal-vocabulary-queryHelp').modal('show').find('.modal-content').load(zuRechtVocabularyTabHelp);
                });
                
                /********** click events for buttons with search options **********************/

                $('#vocabulary-search-form').find('.btn-open-search-options').on("click", function(){
                    $('#modal-vocabularyTabOptions').modal('show');
                });
                
                $('#kwic-search-form').find('.btn-open-search-options').on("click", function(){
                    $('#modal-searchTabOptions').modal('show');
                });
                
                $('#repetition-search-form').find('.btn-open-search-options').on("click", function(){
                    $('#modal-repetitionsTabOptions').modal('show');
                });
                                
                /************* click events for vocabulary buttons ****************/
                $("#open-my-vocabulary-lists-btn").on('click', function(){
                    $("#modal-myVocabularyLists").find('.customVariableGroup').each(function(){
                        $(this).find('.customVariable').val('');
                        $(this).find('.customFileInput').val('');
                        $(this).find('.customFile').val('');
                       });
                    
                    var i=0;
                    for (let [key, value] of customVarMap) {           
                        $("#modal-myVocabularyLists").find('.customVariableGroup').eq(i).find('.customVariable').val(key);
                        $("#modal-myVocabularyLists").find('.customVariableGroup').eq(i).find('.customFileInput').val(value.name);
                        i=i+1;
                    }
                    
                    $("#modal-myVocabularyLists").modal('show');

                });
                
                $("#modal-thematicVocabularyLists-btnQuery").on('click', function(){

                    // get the query with vocabulary list
                    var radioValue = $('input[name=vocabulary-list-name]:checked', '#modal-thematicVocabularyLists').val();                
                    var content = $("#"+radioValue).html();
                    var query = "(" + decodeHTMLQuery(content.replace(/<br>/g, " | ").replace(/\|\s*$/g,'').replace(/\s+/g,' ')) + ")";

                    // close modal
                    $('#modal-thematicVocabularyLists').modal('hide');
                    
                    // set vocabulary list type
                    $('input:radio[name=vocabulary-list-type]', '#vocabulary-search-form').val(['query']);
                    
                    var corpusQueryStr = getCorpusQueryStr();
                    
                    var title = "<%=myResources.getString("FromPreselectedThematicVocabulary")%> <i>\'" + radioValue + "\'</i>"; 
                    if (corpusQueryStr!==''){
                        // submit search request
                        getStatistics(title, query, corpusQueryStr);
                    }

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
                
                /********** setting query string and search mode according to url parameters **************/
                 
                <%
                if (pageParam_q!=null){
                %>
                      var qValue = '<%=pageParam_q%>';
                      $('#queryInputField').val(qValue);
                <%
                }

                if(pageParam_mode!=null){
                %>
                     var modeValue = '<%=pageParam_mode%>';
                     if(modeValue==="<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name()%>"){
                        var searchTypeSelect = $('.searchTypeSelect');
                        var text = "<%=myResources.getString("SearchByIndividualSpeakers")%>";
                        var val = "<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name()%>";
                        updateSearchButton(searchTypeSelect, val, text);
                    }
                    
                <%
                }
                %>
                      
                //provides the ability to the search button to select between different modes
                $(".searchTypeSelect .dropdown-menu li a").on('click', function(e){
                    e.preventDefault();

                    var elem = $(this),
                    searchTypeSelect = elem.parents('.searchTypeSelect'),
                    val = elem.data('value'),
                    text = elem.data('text');
                    
                    updateSearchButton(searchTypeSelect, val, text);
                    
                });
                
                //
                $(".custom-file-input").on("change", function() {
                    var fileName = $(this).val();
                    $(this).siblings(".custom-file-label").addClass("selected").html(fileName);
                });
    
                // 
                $("#vocabulary-search-form").submit(function(){
                                        
                    var corpusQueryStr = getCorpusQueryStr();
                    
                    if (corpusQueryStr!==''){
                        
                        // get the type of the vocabulary list
                        var radioValue = $('input[name=vocabulary-list-type]:checked', '#vocabulary-search-form').val();
                
                        var x = $("#customFile").val();
                        if(x===""){
                            alert("Please upload your vocabulary list!");
                        }else{
                        
                            var txt = x.substring(x.length-4);
                            if (txt===".txt"){

                                // Check for the various File API support.
                                if (window.File && window.FileReader && window.FileList && window.Blob) {

                                    // read file
                                    var files = document.getElementById("customFile").files;
                                    var file = files[0];
                                    if(file){

                                        var reader = new FileReader();

                                        reader.onload = function (evt) {
                                            var text = evt.target.result;

                                            // By lines
                                            var lines = text.split('\n');

                                            var query = "";

                                            if (radioValue==="lemma"){

                                                query += "[";
                                                var i;
                                                for (i = 0; i < lines.length; i++) {
                                                    query += "lemma=\"" + lines[i].replace(/\n/g, "\"").trim() + "\"";
                                                    if(i!==lines.length-1){
                                                       query +=  " | ";
                                                    }
                                                }
                                                query += "]";
                                           

                                            }else if (radioValue==="query"){                                       
                                                query += "(";
                                                var i;
                                                for (i = 0; i < lines.length; i++) {
                                                    query += lines[i];
                                                    if(i!==lines.length-1){
                                                       query +=  "|";
                                                    }
                                                }
                                                query += ")";

                                            }

                                            var title = "<%=myResources.getString("FromTheFile")%>" + " <i>" +x + "</i>";
                                            getStatistics(title, query, corpusQueryStr);
                                        };

                                        reader.onerror = function (evt) {
                                            alert("An error ocurred reading the file");
                                        };

                                        reader.readAsText(file, "UTF-8"); 


                                    }


                                } else {
                                  alert('The File APIs are not fully supported in this browser.');
                                }
                            }else{
                                alert("Only txt-files are supported!");
                            }
                        
                        }
                    
                    }

                    // prevent page reload
                    return false;

                });

                // load html for displaying kwic results
                $("#kwic-search-result-area").load(zuRechtKWICResultView, function() {
             
                    $("#kwic-search-result-area").find(".wait-query-tab").html(searchLoadingIndicatorText).append(searchLoadingIndicatorAbortButton);
 
                    // add html for audio
                   /* $(".kwic-card").append("<div class='card-footer'>\n\
                    <div class='row'><div class='col-12' id='audio-name'></div></div><div id='waveform'></div><div id='wave-timeline'></div>\n\
                    <div id='btn-audio-stop-area' class='row justify-content-center'></div></div>");                

                    */

                    $("#kwic-search-form").submit(function(){
                                                
                        if(ajaxSearchRequest){
                            ajaxSearchRequest.abort();
                            ajaxSearchRequest = null;
                        }
           
                            // get input parameter
                            var q = $('#queryInputField').val().replace(/\“/g, "\"").replace(/\„/g, "\"");                    
                            var pageLength = $(this).find('.pageLength').val();
                            var pageIndex = $(this).find('.pageIndex').val();
                            var searchType = getSearchType();
                            var leftContext = $(this).find('.leftContext').val();
                            var rightContext = $(this).find('.rightContext').val();                
                            var context = leftContext+ "-t," + rightContext + "-t";
                            
                            $.when(writeCustomVarForQuery(this, q)).done(function(){
                                var wordLists = createXMLElement('<%= Constants.CUSTOM_WORDLISTS_KEY%>', $('#kwic-search-form').find('.customWordLists').val());
                                
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
                                    ajaxCallToGetKWIC('#kwic-search-result-area', kwicURL, q, corpusQueryStr, wordLists, pageLength, pageIndex, searchType, context);
                                }
                            });
                        
                        // prevent page reload
                        return false;

                    });      
               
                });
                
                // set right context for repetition search
                $('#modal-repetitionsTabOptions').find('.customRightContextLength').val(DEFAULT_KWIC_RIGHT_CONTEXT_FOR_REPETITIONS);
                $('#repetition-search-form').find('.rightContext').val(DEFAULT_KWIC_RIGHT_CONTEXT_FOR_REPETITIONS);
                
                // hide file upload for repetition form
                $('#repetition-search-form').find('.customFileGroup').css({"display":"none", "width":"350px"}).addClass("mr-2");
                
                // load html for displaying kwic results for repetition search
                $("#repetition-search-result-area").load(zuRechtKWICResultView, function() {
             
                    $("#repetition-search-result-area").find(".wait-query-tab").html(searchLoadingIndicatorText).append(searchLoadingIndicatorAbortButton);

                    $("#repetition-search-form").submit(function(){

                        if(ajaxRepetitionSearchRequest){
                            ajaxRepetitionSearchRequest.abort();
                            ajaxRepetitionSearchRequest = null;
                        }                      
                        
                        var customFileGroup = $('#repetition-search-form').find('.customFileGroup');
                        if($(customFileGroup).css('display')==="none"){
                            searchRepetitions(null);
                        }else{
                            var x = $(customFileGroup).find('.customFile').val();
                            if(x===""){
                                alert("Please upload your lemma list!");
                            }else{
                                var txt = x.substring(x.length-4);
                                if (txt!=='.txt'){
                                    alert("Only .txt files are supported!");
                                }else{

                                    // Check for the various File API support.
                                    if (window.File && window.FileReader && window.FileList && window.Blob) {

                                        // read file
                                        const file = $(customFileGroup).find('.customFile')[0].files[0];

                                        if(file){
                                            var reader = new FileReader();

                                            reader.onload = function (evt) {
                                                var text = evt.target.result;
                                                var synonyms = "<" + '<%= Constants.REPETITION_XML_ELEMENT_NAME_SYNONYMS%>' 
                                                        + ">" + text + "</" + '<%= Constants.REPETITION_XML_ELEMENT_NAME_SYNONYMS%>' + ">";
                                                searchRepetitions(synonyms);
                                            };

                                            reader.onerror = function (evt) {
                                                alert("An error ocurred reading the file");
                                            };

                                            reader.readAsText(file, "UTF-8"); 
                                        }

                                    } else {
                                        alert('The File APIs are not fully supported in this browser.');
                                    }
                                }             
                            }
                        }

                        
                        
                        // prevent page reload
                        return false;

                    });          

                });
                    
            });
            

            function searchRepetitions(synonyms){
            
                //alert(synonyms);
            
                // get input parameter
                var q = $('#repetitionQueryInputField').val().replace(/\“/g, "\"").replace(/\„/g, "\"");                    
                var searchType = getSearchType();     
                var pageLength = $('#repetition-search-form').find('.pageLength').val();
                var pageIndex = $('#repetition-search-form').find('.pageIndex').val();
                var leftContext = $('#repetition-search-form').find('.leftContext').val();
                var rightContext = $('#repetition-search-form').find('.rightContext').val();                
                var context = leftContext+ "-t," + rightContext + "-t";
                var repetitions = getXMLForRepetitions();
                        
                if(repetitions){
                                      
                    $.when(writeCustomVarForQuery('#kwic-search-form', q)).done(function(){               
                    var wordLists = createXMLElement('<%= Constants.CUSTOM_WORDLISTS_KEY%>', $('#kwic-search-form').find('.customWordLists').val());

                    var corpusQueryStr = getCorpusQueryStr();

                    if (corpusQueryStr!==''){

                        // delete search results of the previous query                
                        $("#repetition-search-result-area").find('.paging_container').empty();     // delete paging
                        $("#repetition-search-result-area").find('.query_summary').empty();        // delete the number of total hits
                        $("#repetition-search-result-area").find('.KWICSearch-result').empty();    // delete the summary of the performed search 

                        emptyPage('#repetition-search-result-area');                        // delete search results                    

                        // start loding indicator for ajax requests
                        $("#repetition-search-result-area").find(".wait-query-tab").css("display", "block");

                        // send request
                        ajaxCallToGetKWICForRepetitions('#repetition-search-result-area', repetitionURL, q, corpusQueryStr, pageLength, pageIndex, searchType, context, repetitions, synonyms, wordLists);
                    }
                    
                });
                }
            }
                    
            function abortSearch(obj){
                $(obj).parents('.wait-query-tab').css("display", "none");
                var parentId = $(obj).parents('.searchResultArea').attr('id');
                if(parentId === "repetition-search-result-area"){
                    if(ajaxRepetitionSearchRequest){
                        ajaxRepetitionSearchRequest.abort();
                        ajaxRepetitionSearchRequest = null;
                    }
                }else if (parentId === "kwic-search-result-area"){
                    if(ajaxSearchRequest){
                            ajaxSearchRequest.abort();
                            ajaxSearchRequest = null;
                    }
                }
                
            }
            
            /**********************************************************/
            /*                      ajax calls                        */
            /**********************************************************/
            
            function ajaxCallToGetKWICForRepetitions(selector, url, q, corpusQueryStr, pageLength, pageIndex, searchType, context, repetitions, synonyms, wordLists){                        
                            
                ajaxRepetitionSearchRequest = $.ajax({
                    type: "POST",
                    url: url,
                    timeout: 300000,
                    data: { q: q, cq :corpusQueryStr, count : pageLength, offset : pageIndex, mode : searchType, context : context, repetitions: repetitions, synonyms: synonyms, customWordLists: wordLists},
                    dataType: "text",

                    success: function(xml, status) { 
                        //alert(xml);
                        viewResult(selector, xml, url, context);
   
                    },
                    error: function(xhr, status, error){
                        if(xhr.status === 400 && !q.startsWith("[") && !q.startsWith("(") && !q.startsWith("<")){
                           ajaxRepetitionSearchRequest = ajaxCallToGetKWICForRepetitions(selector, url, completeSearchQuerySyntax(q, selector), corpusQueryStr,  pageLength, pageIndex, searchType, context, repetitions, synonyms, wordLists);                          
                        }else{
                        
                            $(selector).find(".wait-query-tab").css("display", "none");
                            if (status === "timeout"){
                                alert('Your request will take a long time.' +
                                        ' Please constrain the search query, ' + 
                                        'e.g. to a certain lemma: [lemma=\"wissen\"],'+
                                        ' a certain part of speech: [pos=\"NN\"] or '+
                                        'a certain grammatical structure: [pos=\"ART\"][pos=\"ADJA\"][pos=\"NN\"]. '+
                                        'You can also constrain the search query by metadata, e.g. <word/> within <e_se_aktivitaet=\"Fahrstunde\"/>');
                            } else if (status === "abort"){
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

            function ajaxCallToGetKWIC(selector, url, q, corpusQueryStr,  wordLists, pageLength, pageIndex, searchType, context){                        
            
                ajaxSearchRequest = $.ajax({
                    type: "POST",
                    url: url,
                    data: { q: q, cq :corpusQueryStr, count : pageLength, offset : pageIndex, mode : searchType, context : context, customWordLists: wordLists},
                    dataType: "text",

                    success: function(xml, status) { 
                        //alert(xml);
                        viewResult(selector, xml, url, context);
   
                    },
                    error: function(xhr, status, error){
                        if(xhr.status === 400 && !q.startsWith("[") && !q.startsWith("(") && !q.startsWith("<")){
                           ajaxSearchRequest = ajaxCallToGetKWIC(selector, url, completeSearchQuerySyntax(q, selector), corpusQueryStr,  wordLists, pageLength, pageIndex, searchType, context);                          
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
           
            function ajaxCallToGetMetadataForDownload(corpusQueryStr, searchType){
                
                ajaxDownLoadMetadataRequest = $.ajax({
                    type: "GET",
                    url: metadataDownLoadURL,
                    data: { cq: corpusQueryStr, mode: searchType, locale: '<%=currentLocale.toLanguageTag()%>'},
                    dataType: "xml",
                    success: function(xml, status) { 
                        //alert(xml);
                        $(xml).find("metadata").children("metadata-key").each(function () {
                            var id = $(this).attr("id");
                            var name = $(this).attr("name");
                            var type = $(this).attr("type");

                            var level="";
                            if (type==='<%=ObjectTypesEnum.EVENT.name()%>'){
                                level = "E";
                            }else if (type==='<%=ObjectTypesEnum.SPEAKER.name()%>'){
                                level = "S";
                            }else if (type==='<%=ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT.name()%>'){
                                level = "SES";
                            }else if (type==='<%=ObjectTypesEnum.SPEECH_EVENT.name()%>'){
                                level = "SE";
                            }else if (type==='<%=ObjectTypesEnum.TRANSCRIPT.name()%>'){
                                level = "T";
                            }

                            name = level + ": " + name;

                            $('#customMetadataForDownload').append($('<option>', {
                                value: id,
                                text: name  
                            }));
                        });
                    },
                error: function(xhr, status, error){                         
                    processError(xhr, status);
                }

                });
            }
            
            function ajaxCallForKWICDownload(queryStr, corpusQueryStr, to, from, searchType, context, format, customMetadataForDownload, wordLists){
                
                ajaxDownLoadRequest = $.ajax({
                    type: "POST",
                    url: kwicExportURL,
                    data: { q: decodeHTMLQuery(queryStr), cq :corpusQueryStr, count : to, offset : from, mode : searchType, context : context, format: format, addMeta: customMetadataForDownload, customWordLists: wordLists},
                    dataType: "xml",
                    success: function(xml, status) { 
                        //alert(xml);
                        $("#modal-kwicDownloadOptions-spinner").css("display", "none");
                        $("#modal-kwicDownloadOptions-btnOK").prop('disabled', false);
                        $('#modal-kwicDownloadOptions').modal('hide');

                        var file = $(xml).find("file").text();
                        var a = $('<a />', {
                            'href': downloadURL + file,
                            'download': file,
                            'text': "click"
                            }).hide().appendTo("body")[0].click();

                        a.remove();

                    },
                    error: function(xhr, status, error){
                        $("#modal-kwicDownloadOptions-spinner").css("display", "none");
                        $("#modal-kwicDownloadOptions-btnOK").prop('disabled', false);
                        $('#modal-kwicDownloadOptions').modal('hide');

                        processError(xhr, status);
                    }

                });
            }
              
            function ajaxCallToGetMoreKWIC(selector, url, queryString, corpusQueryStr, itemsPerPage, pageIndex, searchType, context, wordLists){
                 ajaxSearchRequest= $.ajax({
                        type: "POST",
                        url: url,
                        data: { q: queryString, cq :corpusQueryStr, count : itemsPerPage, offset : pageIndex, mode : searchType, context : context, customWordLists: wordLists},                      
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
            
            function ajaxCallToGetMoreKWICForRepetitions(selector, url, queryString, corpusQueryStr, itemsPerPage, pageIndex, searchType, context, repetitions, synonyms, wordLists){
                ajaxRepetitionSearchRequest = $.ajax({
                        type: "POST",
                        url: url,
                        data: { q: queryString, cq :corpusQueryStr, count : itemsPerPage, offset : pageIndex, mode : searchType, context : context, repetitions: repetitions, synonyms: synonyms, customWordLists: wordLists},                      
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
                var searchType = $xmlObject.find('code').text();    
                var itemsPerPage = $xmlObject.find('itemsPerPage').text();
                var repetitions = createXMLElement('<%= Constants.REPETITION_XML_ELEMENT_NAME_REPETITIONS%>', $xmlObject.find('repetitions').html());
                var synonyms = createXMLElement('<%= Constants.REPETITION_XML_ELEMENT_NAME_SYNONYMS%>', $xmlObject.find('synonyms').text());
                var wordLists = createXMLElement('<%= Constants.CUSTOM_WORDLISTS_KEY%>', $xmlObject.find('wordLists').text());

                //display summary + button for opening metadata view (GET)

                var query = "<i>" + queryStr + "</i>" + " (in " + corpora.replace(/\s+\|\s+/g, ", ") + ")";
                var longQuery = query+ "<span class='link' id='showLessQuery'> (<%=myResources.getString("ShowLess")%>) </span>";
                var shortQuery = "<i>" + queryStr.substring(0, DEFAULT_QUERY_LENGTH) + "</i> ..." + 
                                                "<span class='link' id='showMoreQuery'> (<%=myResources.getString("ShowMore")%>) </span>";
                var length = queryStr.length;
                if(length>DEFAULT_QUERY_LENGTH){
                    query = shortQuery;
                }                              
                        
                addResultsHead(selector, query, queryStr, corpusQueryStr, searchType, longQuery, shortQuery, wordLists);
                        
                if (parseInt(totalHits) > 0){
                    $(selector).find('.query_summary').append("<span id='search-results'><%=myResources.getString("Total")%>: </span>" + "<span id='total-hits'>" + totalHits + "</span>");
                            
                    // add pagination
                    addPagination(selector, url, totalHits, itemsPerPage, decodeHTMLQuery(queryStr), corpusQueryStr, searchType, context, repetitions, synonyms, wordLists);

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
                data.append('speakerInitialsToolTip', '<%=myResources.getString("ShowSpeakerMetadataInDGD")%>');
                data.append('transcriptIdToolTip', '<%=myResources.getString("ShowEventMetadataInDGD")%>');
                data.append('zuMultToolTip', '<%=myResources.getString("ShowExcerptInZuMult")%>');
                data.append('dgdToolTip', '<%=myResources.getString("ShowExcerptInDGD")%>');

                var fragment = transform(xml, "zuRechtKwic2Html.xsl", data); // implemented in xslTransformation.js
                $(selector).find('.myKWIC').html(fragment);
                
                $(selector).find(".kwic-tab").css("display", "block");
                $(selector).find('.KWICSearch-result').css("display", "block");

                addEventListenerToOscillogramButtons();
            }
                                  
            function addResultsHead(selector, query, queryStr, corpusQueryStr, searchType, longQuery, shortQuery, wordLists){
           
                $(selector).find('.KWICSearch-result').empty();
                
                //display summary
                if(selector==='#repetition-search-result-area'){
                    $(selector).find('.KWICSearch-result').append("<h4><%=myResources.getString("Results")%></h4><div class='clearfix'>\n\
                    <div class='float-left'><%=myResources.getString("ForSearching")%> " + query +"</div></div>");
                }else{
                    // display buttons for opening metadata view, grouping hits and download
                    $(selector).find('.KWICSearch-result').append("<h4><%=myResources.getString("Results")%></h4><div class='clearfix'>\n\
                        <div class='float-left'><%=myResources.getString("ForSearching")%> " + query +"</div>\n\
                        <div class='float-right clearfix'>\n\
                        <div class='float-left'>\n\
                        <form target='_blank' action='../jsp/zuRechtHitStatisticView.jsp' method='post'>\n\
                        <input type='hidden' name='q' value='"+ encodeSpecialUmlauts(queryStr) +"' />\n\
                        <input type='hidden' name='cq' value='"+ corpusQueryStr+"' />\n\
                        <input type='hidden' name='mode' value='"+ searchType +"' />\n\
                        <input type='hidden' name='wordLists' value='"+ wordLists +"' />\n\
                        <input type='hidden' name='metadataKeyID' value='"+ tokenSizeKeyID +"' />\n\
                        <a class='btn btn-outline-secondary btn-sm py-0'  href='#' \n\
                        onclick='openMetadata(this)'>"+'<%=myResources.getString("GroupHits")%>' +"</a>\n\
                        </form>\n\
                        </div>\n\
                        <div class='float-right'>\n\
                        <a class='ml-3 btn btn-outline-secondary btn-sm py-0'  data-backdrop='static' data-keyboard='false' href='#' data-toggle='modal' data-target='#modal-kwicDownloadOptions'><%=myResources.getString("DownloadKWIC")%></a>\n\
                        </div>\n\
                        <div class='float-right'>\n\
                        <form class='ml-3' target='_blank' action='../jsp/zuRechtMetadataStatisticView.jsp' method='post'>\n\
                        <input type='hidden' name='q' value='"+ encodeSpecialUmlauts(queryStr) +"' />\n\
                        <input type='hidden' name='cq' value='"+ corpusQueryStr+"' />\n\
                        <input type='hidden' name='mode' value='"+ searchType +"' />\n\
                        <input type='hidden' name='wordLists' value='"+ wordLists +"' />\n\
                        <input type='hidden' name='metadataKeyID' value='<%= Constants.METADATA_KEY_TRANSCRIPT_DGD_ID %>' />\n\
                        <a class='btn btn-outline-secondary btn-sm py-0'  href='#' \n\
                        onclick='openMetadata(this)'><%=myResources.getString("OpenMetadataView")%></a>\n\
                        </form>\n\
                        </div>\n\
                        </div></div>");


                    $("#modal-kwicDownloadOptions").off().on('shown.bs.modal', function (){
                        ajaxCallToGetMetadataForDownload(corpusQueryStr, searchType);
                    });

                    $("#modal-kwicDownloadOptions").on("hidden.bs.modal", function () {
                            if(ajaxDownLoadRequest){
                                ajaxDownLoadRequest.abort();
                                ajaxDownLoadRequest = null;
                            }

                            if(ajaxDownLoadMetadataRequest){
                                ajaxDownLoadMetadataRequest.abort();
                                ajaxDownLoadMetadataRequest = null;
                            }

                            $("#modal-kwicDownloadOptions-spinner").css("display", "none");
                            $("#modal-kwicDownloadOptions-btnOK").prop('disabled', false);
                            $('#customMetadataForDownload').find('option').remove();
                    });

                    $("#modal-kwicDownloadOptions-btnOK").off().on( 'click', function(){

                            if(ajaxDownLoadRequest){
                                ajaxDownLoadRequest.abort();
                                ajaxDownLoadRequest = null;
                            }

                            //event.stopPropagation();

                            var left = $('#modal-kwicDownloadOptions').find(":text.customLeftContextLength").val();
                            if (!left.match(/^(0?\d|1\d|2[0-5])$/)) {
                                alert("The specified number exceeds the maximum of kwic context");
                               $('#modal-kwicDownloadOptions').find(":text.customLeftContextLength").val("3");
                            }else{
                                var right = $('#modal-kwicDownloadOptions').find(":text.customRightContextLength").val();
                                if (!right.match(/^(0?\d|1\d|2[0-5])$/)) {
                                    alert("The specified number exceeds the maximum of kwic context");
                                    $('#modal-kwicDownloadOptions').find(":text.customRightContextLength").val("3");
                                }else{
                                    var context = left + "-t," + right + "-t";
                                    //var context = '3-t,3-t';
                                    var format = 'xml';
                                    var from = '0';
                                    var to = '<%= defaultNumberForDownload%>';
                                    var max = '<%= maxNumberForDownload%>'

                                    var customNumberOfHitsForDownload = parseInt($("#customNumberOfHitsForDownload").val());
                                    if(!isNaN(customNumberOfHitsForDownload)){
                                        if(customNumberOfHitsForDownload > max){
                                            $("#customNumberOfHitsForDownload").val(max);
                                            alert("A maximum of " +'<%=Constants.MAX_NUMBER_FOR_KWIC_DOWNLOAD%>'+ " items can be downloaded!")
                                        }else{
                                            if (customNumberOfHitsForDownload > 0){
                                                to = customNumberOfHitsForDownload;

                                                var customMetadataForDownload = $("#customMetadataForDownload").val().join(' ');
                                                //alert(customMetadataForDownload);

                                                $(this).prop('disabled', true);
                                                $("#modal-kwicDownloadOptions-spinner").css("display", "block");

                                                // send request
                                                ajaxCallForKWICDownload(queryStr, corpusQueryStr, to, from, searchType, context, format, customMetadataForDownload, wordLists);
                                                
                                            }else{
                                                alert("A minimum of 1 item can be downloaded!")
                                                $("#customNumberOfHitsForDownload").val(to);
                                            }
                                        }
                                    }else{
                                        $("#customNumberOfHitsForDownload").val(to);
                                    }
                                }
                            }

                                    //return false;
                    });
    
                }
                
                $("#showMoreQuery").on('click', function(){
                    addResultsHead(selector, longQuery, queryStr, corpusQueryStr, searchType, longQuery, shortQuery, wordLists);
                });
                                    
                $("#showLessQuery").on('click', function(){
                    addResultsHead(selector, shortQuery, queryStr, corpusQueryStr, searchType, longQuery, shortQuery, wordLists);
                });
            }
           
            function addPagination(selector, url, totalHits, itemsPerPage, queryString, corpusQueryStr, searchType, context, repetitions, synonyms, wordLists){
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
                            ajaxCallToGetMoreKWIC(selector, url, queryString, corpusQueryStr, itemsPerPage, pageIndex, searchType, context, wordLists);                  
                        }else{
                            ajaxCallToGetMoreKWICForRepetitions(selector, url, queryString, corpusQueryStr, itemsPerPage, pageIndex, searchType, context, repetitions, synonyms, wordLists);
                        }
                      }
                });            
            }
                            
            function openMetadata(obj){
                $(obj).parents('form').append("<input type='hidden' name='lang' value='"+ '<%=currentLocale.getLanguage()%>' +"' />");
                $(obj).parents('form').submit();    
            }
                         
            function openTranscript(obj){
                $(obj).parents('form').submit();    
            }
            
            /**************************************************/
            /*             vocabulary search methods          */
            /**************************************************/
            function getStatistics(title, query, corpusQueryStr){
            
                if(ajaxSearchStatisticsRequest){
                    ajaxSearchStatisticsRequest.abort();
                    ajaxSearchStatisticsRequest = null;
                }                
                
                var count = $('#numberOfDocs').val();
                var sortType = $('#sortType').val();
                       
                var metadataKeyID = "<%= Constants.METADATA_KEY_TRANSCRIPT_DGD_ID %>";
                
                // delete previous results
                $('#statistics-result').empty();
                $(".openXML-StatisticSearch-area").css("display", "none");
                $("#rowData-StatisticSearch").empty();
                
                // start loding indicator for ajax requests
                $("#wait-vocabulary-tab").css("display", "block");
                                        
                // send request
                ajaxSearchStatisticsRequest= $.ajax({
                
                    type: "POST",
                    url: statisticURL,
                    data: { q: query, cq :corpusQueryStr, count : count, offset: 0, metadataKeyID : metadataKeyID, sort: sortType },
                    dataType: "text",
                    
                    success: function(xml, status) {
                        //alert(xml);
                                                
                        $("#wait-vocabulary-tab").css("display", "none");
                        
                        displayStatistics(title, xml);

                    },
                    error: function(xhr, status, error){
                        $("#wait-vocabulary-tab").css("display", "none");
                                          
                        if (status === "abort"){
                            //ignore
                        }else {                          
                            if (xhr.statusText === "Bad Request"){
                                alert('Error: ' + xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText + 
                                        ". Or select the correct type of your vocabulary list (lemma list vs. query list)");
                            }else{
                                var errorMessage = xhr.status + ': ' + xhr.statusText + ": " + xhr.responseText;
                                alert('Error: ' + errorMessage);
                            }
                             
                        }
                            
                    }
                });
                          
            }    

            function displayStatistics(title, xml){
                //alert(xml);
                
                //view result in row xml
                $(".openXML-StatisticSearch-area").css("display", "block");
                $('#rowData-StatisticSearch').text(xml);
                
                var data = new FormData();
                data.append('openTranscriptInZuMultToolTipParam', '<%=myResources.getString("OpenTranscriptInZuMult")%>');
                data.append('transcriptIdToolTipParam', '<%=myResources.getString("ShowEventMetadataInDGD")%>');
                data.append('openTranscriptParam', '<%=myResources.getString("OpenTranscript")%>');
                data.append('viewHitsAsKWICOnTheQueryTabParam', '<%=myResources.getString("ViewHitsAsKWICOnTheQueryTab")%>');
                data.append('viewHitsAsLemmaTableParam', '<%=myResources.getString("ViewHitsAsLemmaTable")%>');
                data.append('totalColumnNameParam', '<%=myResources.getString("Total")%>');
                data.append('hitsParam', '<%=myResources.getString("Hits")%>');
                data.append('transcriptIDParam', '<%=myResources.getString("TranscriptID")%>');
                
                // transform result
                var fragment = transform(xml, "zuRechtVocabularySearchStatistics2Html.xsl", data); // implemented in xslTransformation.js

                // get parameters of the search query
                var xmlDocument = $.parseXML(xml);
                var $xmlObject = $(xmlDocument);
                var total = $xmlObject.find('total').text();
                var corpusQueryStr=$xmlObject.find('corpusQuery').text();  
                var queryStr=$xmlObject.find('query').html();                
                var corpora = getCorporaFromCorpusQuery(corpusQueryStr);

                // view result
                $('#statistics-result').html(fragment);
                
                // add summary
                $('#statistics-result').prepend("<div><h4><%=myResources.getString("Results")%></h4> <%=myResources.getString("ForSearchingVocabulary")%> " + 
                        title + " (in "+ corpora.replace(/\s+\|\s+/g, ", ") + ")</div>");

                if (total === "0"){
                    $('#statistics-result h4').prepend('<%=myResources.getString("No")%>' + " ");
                }
                
                
                addMetadataToTranscriptTable();
                addRelativeNumberOfHits();
                addTypesOfHits(queryStr, corpusQueryStr);
                             
                $('button.btn-open-lemma-table').on('click', function(){    
                     var form = $(this).parents('form');
                     var transcriptID = this.getAttribute('data-value-source');
                     var newQueryStr = encodeSpecialUmlauts(queryStr) + " within <<%= Constants.METADATA_KEY_TRANSCRIPT_DGD_ID %>=\"" + transcriptID + "\"&#47;>";
                     form.append("<input type='hidden' name='q' value='"+ newQueryStr +"' />");
                     form.append("<input type='hidden' name='cq' value='"+ corpusQueryStr+"' />");
                     form.append("<input type='hidden' name='metadataKeyID' value='<%= Constants.ATTRIBUTE_NAME_LEMMA %>' />");
                     form.append("<input type='hidden' name='mode' value='"+ '<%= DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX %>' +"' />");
                     form.append("<input type='hidden' name='lang' value='"+ '<%=currentLocale.getLanguage()%>' +"' />");
                     form.submit();
                });

                $('button.btn-open-kwic-tab').on('click', function(){
             
                    // switch tab
                    $('#query-tab').tab('show');
                    
                    // get transcript id
                    var transcriptID = this.getAttribute('data-value-source');
                    
                    // set query                       
                    $('#queryInputField').val(queryStr.replace(/&#xD;/g, "").replace(/&lt;/g, "<").replace(/&gt;/g, ">") +" within <<%= Constants.METADATA_KEY_TRANSCRIPT_DGD_ID %>=\"" + transcriptID + "\"/>");
                    
                    // set corpora
                    
                    var mySplitResult = corpora.split("|");
                    setCorpora(mySplitResult);
                    
                    // set search mode
                    var searchTypeSelect = $('.searchTypeSelect');
                    var text = "<%=myResources.getString("SearchInTranscript")%>";
                    var val = "<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX.name()%>";
                    updateSearchButton(searchTypeSelect, val, text);
                    
                    // start the search
                    $('form#kwic-search-form').submit();
                });
            }
            
            function addMetadataToTranscriptTable(){
                $('#statistic_table thead tr').find('th.transcript').after("<th class='metadataValue'>"+'<%=myResources.getString("ShortDescription")%>'+"</th>");
                $('#statistic_table tbody tr').find('td.transcript').after("<td class='metadataValue'></td>");
                
                $('#statistic_table tbody tr').each(function (rowIndex, row){
                    var transcriptID = $(this).find('td.transcript').text();
                    var cell = $(this).find('td.metadataValue');
        
                    var metadataKeyID = '<%= Constants.DEFAULT_METADATA_KEY_SPEECH_EVENT %>'; //TODO: add from options
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
                            processError(xhr, status);
                        }
                    }); 
                });
            }
          
            function addTypesOfHits(query, corpusQuery){
                $('#statistic_table thead tr').find('th.numberOfAbsAndRelHits').after("<th class='numberOfTypesOfHits'>Types</th>");
                $('#statistic_table tbody tr').find('td.numberOfAbsAndRelHits').after("<td class='numberOfTypesOfHits'></td>");
                $('#statistic_table tbody tr').each(function (rowIndex, row){
                    var cell1 = $(this).find('td.numberOfTypesOfHits');
                    cell1.html("<div id='wait-page' class='ml-3'>"+"<%=myResources.getString("Loading")%>"+" <img src='../images/loading.gif' width='64' height='64' alt='Loading indicator'/></div>");
                    var transcriptID = $(this).find('td.transcript').text();
                    var queryStr = query.replace(/&#xD;/g, ""). replace(/&lt;/g, "<").replace(/&gt;/g, ">") + " within <<%= Constants.METADATA_KEY_TRANSCRIPT_DGD_ID %>=\"" + transcriptID + "\"/>";                   
                    var url = '<%=restAPIBaseURL%>' + "/SearchService/statistics/distinctValues";        
                    var count = 10;
                    var metadataKeyID= '<%= Constants.ATTRIBUTE_NAME_LEMMA %>';
                  
                  // send request
                    $.ajax({
                        type: "POST",
                        url: url,
                        data: { q: queryStr, cq :corpusQuery, count : count, metadataKeyID : metadataKeyID},
                        dataType: "text",

                        success: function(text, status) {
                            //alert(text);
                            cell1.empty();
                            cell1.text(text);
                        },
                        error: function(xhr, status, error){
                            processError(xhr, status);
                        }
                    });
                });
            }
            
            function addRelativeNumberOfHits(){
                $('#statistic_table thead tr').find('th.transcript').after("<th class='numberOfTokens'>" + '<%=myResources.getString("Tokens")%>' + " (" +'<%=myResources.getString("Total").toLowerCase()%>'+")</th>");
                $('#statistic_table tbody tr').find('td.transcript').after("<td class='numberOfTokens'></td>");
                
                $('#statistic_table tbody tr').each(function (rowIndex, row){
                    var transcriptID = $(this).find('td.transcript').text();
                    var numberOfHits = $(this).find('td.numberOfAbsAndRelHits').text();
                    
                    var cell1 = $(this).find('td.numberOfTokens');
                    var cell3 = $(this).find('td.numberOfAbsAndRelHits');
                                  
                    var url = '<%=restAPIBaseURL%>' + "/transcripts/transcript/" + transcriptID + "/numberOfTokens";
                    
                    // send request
                    $.ajax({
                        type: "GET",
                        url: url,
                        dataType: "text",

                        success: function(text, status) {
                            //alert(text);
                            var x = parseInt(numberOfHits);
                            var y = parseInt(text);
                            var rel = x *100 / y;
                            cell1.text(text);
                            var relStr = " (" + rel.toFixed(2) + "%)";
                            cell3.text(numberOfHits + relStr);
                        },
                        
                        error: function(xhr, status, error){
                            processError(xhr, status);
                        }
                    });
                });
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

            function updateSearchButton(searchTypeSelect, val, text){
                searchTypeSelect.find('.icon').remove();
                    if (text==="<%=myResources.getString("SearchByIndividualSpeakers")%>"){
                        searchTypeSelect.find('.currentVal').before("<span class='icon mr-1'><i class='fa fa-user'></i></span>");
                    }else if (text==="<%=myResources.getString("SearchInTranscript")%>"){
                        searchTypeSelect.find('.currentVal').before("<span class='icon mr-1'><i class='fa fa-copy'></i></span>");
                    }
                    searchTypeSelect.find('.currentVal').text(text);
                    $("#searchType").val(val);
                    
                    updateSampleQueries();
            }
               

            function abortSimpleSearchOption(selectorModal, selectorForm){
                var selectedOption = $(selectorForm).find('.simpleQuerySyntaxLevel').val();
                    $(selectorModal).find('.customSimpleQuerySyntaxLevel').val(selectedOption);
            }

            function abortConfigPageLength(selectorModal, selectorForm){
                var selectedOption = $(selectorForm).find('.pageLength').val();
                $(selectorModal).find('.customPageLength').val(selectedOption);
            }

            function configureSimpleSearchOption(selectorModal, selectorForm){ 
                var selectedLevel = $(selectorModal).find('.customSimpleQuerySyntaxLevel option:selected').val();
                $(selectorForm).find('.simpleQuerySyntaxLevel').val(selectedLevel);
            }
            
            function configurePageLength(selectorModal, selectorForm){
                var selectedOption = $(selectorModal).find('.customPageLength option:selected').text();
                $(selectorForm).find('.pageLength').val(selectedOption);       
            }
            
            function abortConfigContext(selectorModal, selectorForm){
                var selectedOptionLeft = $(selectorForm).find(".leftContext").val(); 
                $(selectorModal).find(".customLeftContextLength").val(selectedOptionLeft);
                
                var selectedOptionRight = $(selectorForm).find(".rightContext").val(); 
                $(selectorModal).find(".customRightContextLength").val(selectedOptionRight);   
            }
            
            function configureContext(selectorModal, selectorForm){
                var defaultContextLength = 3;
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
        
                var searchType = $('#searchType').val();
                if(searchType==="<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX.name()%>"){
                    <% ArrayList<SampleQuery> queries2 = IOHelper.getQueriesFromFile(Constants.SAMPLE_QUERIES_FOR_TRASCRIPT_BASED_SEARCH);
                        for (int i = 0; i < queries2.size(); i++) { 
                            SampleQuery query = queries2.get(i); 
                        %>

                        addQuery('<%=query.getCorpus() %>', '<%=query.getQueryString() %>', '<%=query.getQueryString().replaceAll("\"", "%22") %>', '<%=query.getDescription().replaceAll("\'", "%22") %>');


                        <%}%>
                }else if (searchType==="<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name()%>"){
                    <% ArrayList<SampleQuery> queries1 = IOHelper.getQueriesFromFile(Constants.SAMPLE_QUERIES_FOR_SPEAKER_BASED_SEARCH);
                        for (int i = 0; i < queries1.size(); i++) { 
                            SampleQuery query = queries1.get(i); 
                        %>

                        addQuery('<%=query.getCorpus() %>', '<%=query.getQueryString() %>', '<%=query.getQueryString().replaceAll("\"", "%22") %>', '<%=query.getDescription().replaceAll("\'", "%22") %>');


                        <%}%>
                }
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
                emptyWavesurfer();
            }
             
            function emptyWavesurfer(){
                $("#audio-name").empty();
                $("#waveform").empty();
                $('#wave-timeline').empty();
                $('#btn-audio-stop-area').empty();
                if (wavesurfer!==null){
                    wavesurfer.cancelAjax();
                    wavesurfer.pause();
                    wavesurfer = null;
                }
            }
            
            /* This function adds an event listener to the oscillogram-button that should load the appropriate audio file, display its oscillogram and match the appropriate match intervals */
            function addEventListenerToOscillogramButtons(){
               
                $("button.btn-open-oscillogram").on('click', function(){
                    alert("This functionality is temporarily disabled");
                    
                });
            }
            
            function getSearchType(){
                var searchType = $('#searchType').val();
                //alert(searchType);
                        
                if($('#punctCheck').prop('checked')){
                            if(searchType==='<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX.name()%>'){
                                searchType='<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX_WITHOUT_PUNCT.name()%>';
                            }else if (searchType==='<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name()%>') {
                                searchType='<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX_WITHOUT_PUNCT.name()%>';
                            }else{
                                alert('Error: Unknown search mode!');
                            }

                            
                        }
                //alert(searchType);    
                return searchType;
            }
            
            /* This function copies the search query string from the modal window and set it into the query input field */
            function copyQuery(obj) {

                // set corpora
                var corporaText = $(obj).children(".float-right").text();
                var myRegexp = /Corp(ora|us):\s(.*)/;
                var match = myRegexp.exec(corporaText);
                var mySplitResult = match[2].split(",");
                
                setCorpora(mySplitResult);
                
                // set query
                var str = $(obj).children(".float-left").text();
                var show = document.getElementById("queryInputField");
                show.value=str;
                
                // set search mode
                var searchTypeSelect = $('.searchTypeSelect');
       
                var mode = $(obj).children(".mode").val();
               
                if (mode==="<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name()%>"){
                    var text = "<%=myResources.getString("SearchByIndividualSpeakers")%>";
                    var val = "<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name()%>";
                    updateSearchButton(searchTypeSelect, val, text);
                    $('#punctCheck').prop('checked', false);
                }else if (mode==="<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX_WITHOUT_PUNCT.name()%>"){
                    var text = "<%=myResources.getString("SearchByIndividualSpeakers")%>";
                    var val = "<%=DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name()%>";
                    $('#punctCheck').prop('checked', true);
                    updateSearchButton(searchTypeSelect, val, text);
                }else if (mode==="<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX.name()%>"){
                    var text = "<%=myResources.getString("SearchInTranscript")%>";
                    var val = "<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX.name()%>";
                    updateSearchButton(searchTypeSelect, val, text);
                    $('#punctCheck').prop('checked', false);
                }else{
                    var text = "<%=myResources.getString("SearchInTranscript")%>";
                    var val = "<%=DGD2SearchIndexTypeEnum.TRANSCRIPT_BASED_INDEX.name()%>";
                    updateSearchButton(searchTypeSelect, val, text);
                    $('#punctCheck').prop('checked', true);
                }
                
                // swith to the search tab
                $('#query-tab').tab('show');
                
                // close modal
                $('#modal-queryHelp').modal('hide');
                $('#modal-vocabulary-queryHelp').modal('hide');
            }
           
            function printQueryHelp() {
                var content = $('#modal-queryHelp .modal-content').clone();
                content.find('img').each(function () {
                    var src = $(this).attr("src");
                    var srcNew = '<%=webAppBaseURL%>' + src.substring(2, src.length); //http://zumult.ids-mannheim.de/ProtoZumult/images/query_help/kwic_003.png
                    $(this).attr("src",srcNew);
                    
                });
                
                var w = window.open();
                $(w.document.head).append(printStyleForQueryHelp);
                $(w.document.body).html(content);
            }
            
            function encodeSpecialUmlauts(queryStr){
                var queryString = queryStr
                    .replace(/Ö/g, "%C3%96")
                    .replace(/ß/g, "%C3%9F");
                return queryString;
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
            
            function displayMoreRepetitionProperties(obj){
                var parent = $(obj).parents('.repetitionPropertiesForm');
                var secondForm = $(parent).next();
                var addRepetitionProperties = $(parent).find('.additionalRepetitionProperties');
                if ($(addRepetitionProperties).css("display") === "none"){
                    $(addRepetitionProperties).css("display", "block");
                    $(obj).text('<%=myResources.getString("ShowLess")%>');
                    if(secondForm!==null){
                        $(secondForm).css("display", "block");
                    }
                }else{
                    $(addRepetitionProperties).css("display", "none");
                    $(obj).text('<%=myResources.getString("ShowMore")%>');
                    if(secondForm!==null){
                        $(secondForm).css("display", "none");
                    }
                }
                
            }
            
            function displayOneMoreRepetitionPropertiesForm(obj){
                var parent = $(obj).parents('.repetitionPropertiesForm');
                $(parent).find(".addOneMoreRepetitionPropertiesForm").css("display", "none");
                
                var copy = $(parent).clone();
                $(copy).find("i").remove();
                $(copy).find(".showMore").remove();
                $(copy).find('.deleteCurrentRetitionPropertiesForm').css('display', 'block');
                $(copy).addClass("mt-5");
                setDefaultSettingsForRepetitionForm(copy);

                $(copy).find('.inputFieldWithAutocomplete').each(function(){
                    addAutocompleteToInputField(this);
                });
                
                var repetitionForm = $(parent).parent();
                $(copy).appendTo(repetitionForm);
            }
            
            function deleteCurrentRepetitionPropertiesForm(obj){
                var parent = $(obj).parents('.repetitionPropertiesForm');
                var repetitionForm = $(parent).prev();
                $(parent).remove();
                $(repetitionForm).find(".addOneMoreRepetitionPropertiesForm").css("display", "block");
                
            }
            
            function setDefaultSettingsForRepetitionForm(obj){
                $(obj).find('.speakerMetadataInputField').val('');
                $(obj).find('.speakerMetadataGroup').css('display', 'none');
                $(obj).find('.speakerChangeSelectGroup').css('display', 'none');

                $(obj).find('.withinContributionCheckBox').prop( "checked", false);
                
                $(obj).find("input[name='posToBeIgnored']").each(function () {
                    $(this).prop( "checked", false);;
                });

                setDefaulValue($(obj).find('.minDistance'));
                setDefaulValue($(obj).find('.maxDistance'));
                
                setDefaulValue($(obj).find('.positionToSpeakerChangeMin'));
                setDefaulValue($(obj).find('.positionToSpeakerChangeMax'));
                $(obj).find('.positionToSpeakerChangeMin').prop('disabled', true);
                $(obj).find('.positionToSpeakerChangeMax').prop('disabled', true);
            }
            
            function setDefaulValue(obj){
                var value = $(obj).prop('defaultValue');
                $(obj).val(value);
            }
                        
            function addDistanceToSpeakerChange(obj){
                var parent = $(obj).parents('.repetitionPropertiesForm');
                var min = $(parent).find('.positionToSpeakerChangeMin');
                var max = $(parent).find('.positionToSpeakerChangeMax');
                if($(obj).val()==='null'){
                    $(min).prop('disabled', true);
                    $(max).prop('disabled', true);
                }else{
                    $(min).prop('disabled', false);
                    $(max).prop('disabled', false);
                }
            }
            
            function enableFileUploadForRepetitionSearch(obj){
                if($(obj).val()==='8'){
                    $('#repetition-search-form').find('.customFileGroup').css("display", "block");               
                }else{
                    // check the value of the other form
                    var parent = $(obj).parents('.repetitionPropertiesForm');
                    var index = $(parent).index();
                    var anotherValue;
                    if(index<=2){
                        // check if the second form is displayed
                        var anotherParent = $(parent).next('.repetitionPropertiesForm');
                        if (anotherParent!==null){
                            // get value of the second form
                            anotherValue = $(anotherParent).find('.repetitionSearchModeSelect').val();
                        }
                    }else{
                        // get value of the first form
                        anotherValue = $(parent).prev('.repetitionPropertiesForm').find('.repetitionSearchModeSelect').val();
                    }
                    if(anotherValue!=='8'){
                        $('#repetition-search-form').find('.customFileGroup').css("display", "none");
                    }
                }
            }
            
            function enableSpeakerMetadata(obj){
                var parent = $(obj).parents('.repetitionPropertiesForm');
                var index = $(parent).index();
                var speakerMetadataGroup = $(parent).find('.speakerMetadataGroup');
                var speakerChangeSelectGroup = $(parent).find('.speakerChangeSelectGroup');
                var metadataInputField = $(parent).find('.speakerMetadataInputField');
                var speakerChangeSelect = $(parent).find('.speakerChangeSelect');
                
                if($(obj).val()==='false'){
       
                    $(speakerMetadataGroup).css('display', "block");
                    
                    $(speakerChangeSelectGroup).css('display', "none");
                    setDefaultValueForSelect(speakerChangeSelect);
                    
                    if(index<=2){
                        //check the second form
                        $(parent).next('.repetitionPropertiesForm').find('.speakerChangeSelectGroup').css('display', "none");
                        setDefaultValueForSelect($(parent).next('.repetitionPropertiesForm').find('.speakerChangeSelect'));
                    }
                } else if ($(obj).val()==='true'){
                    if(index>2){
                        var valueBefore =  $(parent).prev('.repetitionPropertiesForm').find('.speakerSelect').val();
                        if (valueBefore==='true'){
                            $(speakerChangeSelectGroup).css('display', "block");
                        }
                    }else{
                        $(speakerChangeSelectGroup).css('display', "block");
                      
                        // check the second form
                        if ($(parent).next('.repetitionPropertiesForm').find('.speakerSelect').val()==='true'){
                            $(parent).next('.repetitionPropertiesForm').find('.speakerChangeSelectGroup').css('display', "block");
                        }
                    }
                    
                    $(speakerMetadataGroup).css('display', "none");
                    $(metadataInputField).val('');
 
                }else{
                    $(speakerMetadataGroup).css('display', "none");
                    $(metadataInputField).val('');
                    
                    $(speakerChangeSelectGroup).css('display', "none");
                    setDefaultValueForSelect(speakerChangeSelect);
                    
                    if(index<=2){
                        //check the second form
                        $(parent).next('.repetitionPropertiesForm').find('.speakerChangeSelectGroup').css('display', "none");
                        setDefaultValueForSelect($(parent).next('.repetitionPropertiesForm').find('.speakerChangeSelect'));
                    }
                }
            }
                       
            function setDefaultValueForSelect(obj){
                $(obj).find('option:selected').removeAttr("selected");
                $(obj).find("option[value=null]").prop('selected', 'selected');
            }
            
            /**************************************************/
            /*                pos methods                     */
            /**************************************************/
            function addPOSToBeIgnoredWhenSearchingRepetitions(selector){
                
                //add pos to be ignored for repetitions
                $(posTagSetXML).find("[name='POS-tags']").children("category").each(function () {
                    $(this).children("category").each(function () {
                        addPOSToBeIgnored(this, selector);
                    });
                });
                
                $(posTagSetXML).find("[name='Extra-tags']").children("category").each(function () {
                    addPOSToBeIgnored(this, selector);
                });
            }
            
            function addPOSToBeIgnored(obj, selector){
                var subCategoryTag = $(obj).find("tag").attr("name");

                if (jQuery.inArray(subCategoryTag, arrayWithPosToBeIgnored) > -1){
                    
                    var subCategoryName = $(obj).attr("name");
                    var subCategoryDescription = " (<em style='background:#f3f3f3;'>"+ $(obj).find("description").text() + "</em>)";  
                    
                    if(subCategoryTag==="XY"){
                        subCategoryDescription="";
                    }
                    
                    var label = $('<label/>')
                            .addClass('mb-0')
                            .html("<input name='posToBeIgnored' class='ml-2' type='checkbox' value='"+subCategoryTag+"'/> "+subCategoryTag+" <small>"+subCategoryName + subCategoryDescription + "</small>")
                            .appendTo($(selector));                   
                }
                
            }
            
            /**************************************************/
            /*             query builder methods          */
            /**************************************************/
            
            function addAutocompleteToInputField(obj){
                
                $(obj).click(function(){
                    $("#autocompleteForQueryInputField").empty();
                    addAutocomplete(obj, "");
                });

                $(obj).keydown(function(e){
                    if (e.which === 37 || e.which === 39) {  // left and right arrows
                       $("#autocompleteForQueryInputField").empty();
                    }
                });

                $(obj).keypress(function(e) {
                    $("#autocompleteForQueryInputField").empty();
                    if (e.which !== 0) {      
                                    
                        // get character
                        var str = String.fromCharCode(e.which);
                        if(str.localeCompare("/")===0 ){
                             //add ">";
                        }
                        if(str.localeCompare("<")===0 
                                || str.localeCompare("[")===0
                                || str.localeCompare("=")===0 
                                || str.localeCompare("&")===0
                                || str.localeCompare(" ")===0){
                            addAutocomplete(obj, str); 
                        }
                    }
                });
            }
            

            
            /**************************************************/
            /*             methods for file upload               */
            /**************************************************/
            
            function emulateClickOnInputFile(obj){
                $(obj).parents('.customFileGroup').find('.customFile').click();
            }
            
            function displayFileName(obj){
                const file = $(obj)[0].files[0];
                $(obj).parents('.customFileGroup').find('.customFileInput').val(file.name);
            }
            
            function deleteFile(obj){
                $(obj).prev().val('');
                $(obj).parents('.customFileGroup').find('.customFile').val('');
            }
            
            
            /***************************************************************************/
            /*             methods for using variables and custom wordlists            */
            /***************************************************************************/
            
            function checkAndSaveCustomVariables(selectorModal){
                var finished = true;
                var map  = new Map();
                $(selectorModal).find('.customVariableGroup').each(function(){
                    var customVariable = $(this).find('.customVariable').val();
                    var customFile = $(this).find('.customFile').val();
                    
                    if (customVariable===''){
                        // check if file uploaded
                        if(customFile!==''){
                            finished = false;
                            alert("You habe loaded wordlists. Please define variables for them!");
                            return false;
                        }else{
                            var customFileInput = $(this).find('.customFileInput').val();
                            if(customFileInput!==''){
                                $(this).find('.customFileInput').val('');
                            }
                        }
                        
                    }else{
                        // variable is not null -> parse
                        if (!customVariable.match(/^\\$[A-Za-z0-9]+$/g)) {
                            finished = false;
                            alert("\""+customVariable + "\" is not a valid variable. Der Variablenname soll mit '$' beginnen" +
                                   " und darf keine Leer- und Sonderzeichen enthalten, "+
                                   "z.B. '$1', '$var1' oder '$words'.");
                            return false;
                        }else{
                            if(map.has(customVariable)){
                                finished = false;
                                alert("Jede Variable darf nur einmal vorkommen.");
                                return false;
                            }
                        }

                        // check if file uploaded
                        if(customFile!==''){
                            const file = $(this).find('.customFile')[0].files[0];
                            map.set(customVariable, file);
                        }else{
                            var customFileInput = $(this).find('.customFileInput').val();
                            if(customFileInput!==''){
                                // check in customVarMap
                                if(!customVarMap.has(customVariable)){
                                    finished = false;
                                    alert("Can't read file " + customFileInput +". Please upload it again!");  
                                    return false;
                                }else{
                                    const file = customVarMap.get(customVariable);
                                    map.set(customVariable, file);
                                } 
                            }else{
                                finished = false;
                                alert("You have defined some variables. Please load wordlists for them!");  
                                return false;
                            }
                        }
                    }
                });
                if(finished){
                    customVarMap = map;
                    $(selectorModal).modal('hide');
                }
            }

            async function writeCustomVarForQuery(selectorForm, query){
                var result ="";
                    for (let [key, value] of customVarMap) {           
                        if (query.includes(key + " ") || query.includes(key + "]") || query===key ){
                            let text = await readWordListAsCommaSeparated(key, value);
                            result = result + key.substring(1) + ":" + text + ";";
                        }
                    }
                    result = result.substring(0, result.length - 1);
                    $(selectorForm).find('.customWordLists').val(result);
            }
            
            function addCommas(text){
                var lines = text.split(/\r?\n/);
                var wordList = ''; 
                var i;
                for (i = 0; i < lines.length; i++) {
                    wordList += lines[i].trim();
                    if(i!==lines.length-1){
                        wordList +=  ",";
                    }
                }
                return wordList;
            }
                       
            async function readWordListAsCommaSeparated(variable, file) {
                let text = await new Promise((resolve) => {
                    let reader = new FileReader();
                    reader.onload = (evt) => resolve(addCommas(evt.target.result));
                    reader.onerror = (evt) => alert("An error ocurred reading the file \"" + file.name + "\" for variable \""+variable+"\"");
                    reader.readAsText(file, "UTF-8");
                });

                return text;
            }
 
            
        </script>
    </body>
</html>

