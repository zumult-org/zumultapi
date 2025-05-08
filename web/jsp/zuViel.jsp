<%-- 
    Document   : transcript2
    Created on : 29.12.2019, 16:42:36
    Author     : thomas.schmidt
--%>

<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.TreeMap"%>
<%@page import="org.zumult.objects.TokenList"%>
<%@page import="java.io.File"%>
<%@page import="org.zumult.backend.Configuration"%>
<%@page import="org.zumult.io.FileIO"%>
<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.io.Constants"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.objects.Transcript"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page errorPage="errorPage.jsp" %>
<!DOCTYPE html>

<!--
    Parameters passed to this JSP:

    - transcriptID : the ID of the transcript to be displayed, required
    (- transcriptIDWithHighlights : better get rid of this one) ### got rid of this one, issue #40

    - customTitle  : an additional title, optional

    - form : one of (trans|norm|lemma|pos|phon), determines which form of a <w> token to display, defaults to 'trans'
    - showNormDev : one of (TRUE|FALSE), determines whether or not to make visible deviations between transcribed and normalised form, defaults to 'FALSE'
    - visSpeechRate : one of (TRUE|FALSE), determines whether or not speech rate is visualised in the transcript text, defaults to 'FALSE'

    - startAnnotationBlockID : for a selection - the ID of the first annotation block to be displayed
    - endAnnotationBlockID : for a selection - the ID of the last annotation block to be displayed
    - aroundAnnotationBlockID : for a selection - the ID of the annotation block around which the selection is to be taken
    - aroundTokenID : for a selection - the ID of the token around which the selection is to be taken, will be translated to 'aroundAnnotationBlockID'
    - howMuchAround : for a selection - how many annotation blocks to display before or after 'aroundAnnotationBlockID'
    - startTokenID : for a selection - the ID of the first token to be displayed (plus the preceding tokens in the annotation block?) ### was firstTokenId, issue #40
    - endTokenID : for a selection - the ID of the last token to be displayed (plus the following tokens in the annotation block?) ### was lastTokenId, issue #40

    - wordlistID : one of (GOETHE_(A1|A2|B1)|HERDER(1|2|3|4|5)0000), determines against which predefined wordlist to match the transcript's wordlist
    - tokenList : ???

    (- highlightIDs : IDs of tokens to be highlighted in style 1 (e.g. because they are query results))  ### now highlightIDs1, issue #40
    (- extraHighlightIDs : IDs of tokens to be hightlighted in style 2 (e.g. because they are annotated)) ### now highlightIDs2, issue #40

    - q : a query string, resulting tokens will be highlighted via 'highlightIDs1'

    - mode : one of (PRINT|SCREEN)

-->

<% 
    
    String username = "bratislav.metulski@takkatukka.fr";
    
    BackendInterface backend = BackendInterfaceFactory.newBackendInterface(); 
    
    // get parameters
    request.setCharacterEncoding("UTF-8");
    
    String transcriptID = request.getParameter("transcriptID");
    if (transcriptID==null){
        // redirect to error page
    }
    String customTitle = request.getParameter("customTitle");

    
    /**** START: for user defined vocabulary lists ****/
    
    
    Transcript transcript = backend.getTranscript(transcriptID);
    
    String transcriptLanguage = transcript.getLanguage();
    boolean isGerman = "de".equals(transcriptLanguage);
    
    /**** END: for user defined vocabulary lists ****/
    
    String form = request.getParameter("form");
    if (form==null){
        form="trans";
    }

    String showNormDev = request.getParameter("showNormDev");
    if (showNormDev==null){
        showNormDev="FALSE";
    }
    
    String visSpeechRate = request.getParameter("visSpeechRate");
    if (visSpeechRate==null){
        visSpeechRate="FALSE";
    }
    
    String flattenSeg = request.getParameter("flattenSeg");
    if (flattenSeg==null){
        flattenSeg = "TRUE";
    }
    
    String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
    if (startAnnotationBlockID==null){
        startAnnotationBlockID = "";
    }
    
    String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");
    if (endAnnotationBlockID==null){
        endAnnotationBlockID = "";
    }
    
    String aroundAnnotationBlockID = request.getParameter("aroundAnnotationBlockID");
    if (aroundAnnotationBlockID==null){
        aroundAnnotationBlockID = "";
    }
    
    // issue #4
    String aroundTokenID = request.getParameter("aroundTokenID");   
    if (aroundTokenID==null){
        aroundTokenID = "";
    } else {
        aroundAnnotationBlockID = backend.getNearestAnnotationBlockID4TokenID(transcriptID, aroundTokenID);
    }
    
    
    String howMuchAround = request.getParameter("howMuchAround");
    if (howMuchAround==null){
        howMuchAround = "";
    }
    
    String wordlistID = request.getParameter("wordlistID");    
    String pathToWordList = "";
    if (wordlistID!=null){
        pathToWordList = new File(getServletContext().getRealPath("/data/" + wordlistID + ".xml"))
                .toURI().toString();
    } else {
        wordlistID = "";
    }
    
    // new for issue #40
    String[] highlightIDsArray = new String[9];
    for (int i=1; i<=9; i++){
        String param = request.getParameter("highlightIDs" + Integer.toString(i));
        if (param!=null){
            highlightIDsArray[i-1] = param;
        } else {
            highlightIDsArray[i-1] = "";
        }        
    }
    


    /**** START: for user defined vocabulary lists and for action sequences****/
    
    
    //String highlightIDs = request.getParameter("highlightIDs");
    String tokenList = request.getParameter("tokenList");
    if (tokenList==null){   
        String query = request.getParameter("q");
        if (query!=null && !query.isEmpty()){
            IDList tokenIDs = backend.searchTokensForTranscript(query, null, null, null, null, null, transcriptID, "id", null);
            IDList lemmas = backend.searchTokensForTranscript(query, null, null, null, null, null, transcriptID, "lemma", null);
            highlightIDsArray[0] = String.join(" ", tokenIDs);
            tokenList = String.join(";", lemmas);
        } else{
            tokenList = "";
        }
    }
    if (tokenList==null){
        tokenList="";
    }
    
    //String extraHighlightIDs = request.getParameter("extraHighlightIDs");
    //if (extraHighlightIDs==null){   
    //    extraHighlightIDs = "";
    //}
    
    String startTokenID = request.getParameter("startTokenID");
    String endTokenID = request.getParameter("endTokenID");    
    
    String makeVisibleID = request.getParameter("makeVisibleID");    
  
    //String extraHighlightIDsForCurrentTranscript= "";
    //String highlightIDsForCurrentTranscript = "";  
    //String tokenListForCurrentTranscript = "";
    
    // still don't understand what this is good for
    // what exactly happens if I take all the ... currentTranscript away? Let's see
    //if (transcriptIDWithHighlights!=null && transcriptIDWithHighlights.equals(transcriptID)){     
    //    extraHighlightIDsForCurrentTranscript = extraHighlightIDs;
    //    highlightIDsForCurrentTranscript = highlightIDs;
    //    if (tokenList!=null){
    //        tokenListForCurrentTranscript = tokenList;
    //    }
        
        if (startTokenID!=null && endTokenID!=null){
            //highlightIDstart = firstTokenId;
            //highlightIDend = lastTokenId;

            /* if firstTokenId and lastTokenId are specified, than aroundAnnotationBlockID and aroundTokenID should be ignored; */
            aroundAnnotationBlockID = "";
            aroundTokenID = "";
            startAnnotationBlockID = backend.getNearestAnnotationBlockID4TokenID(transcriptID, startTokenID);;
            endAnnotationBlockID = backend.getNearestAnnotationBlockID4TokenID(transcriptID, endTokenID);;
            
            if(howMuchAround.length()>0){
                startAnnotationBlockID = transcript.getAnnotationBlockID(startAnnotationBlockID, -Integer.parseInt(howMuchAround));
                endAnnotationBlockID = transcript.getAnnotationBlockID(endAnnotationBlockID, Integer.parseInt(howMuchAround));
                
            }
        }
        
        
    //}
    
    /**** END: for user defined vocabulary lists ****/
                    
    String corpusID = backend.getCorpus4Event(backend.getEvent4SpeechEvent(backend.getSpeechEvent4Transcript(transcriptID)));
    String speechEventID = backend.getSpeechEvent4Transcript(transcriptID); //transcriptID.substring(0,18);
    String eventID = backend.getEvent4SpeechEvent(speechEventID); // transcriptID.substring(0,12);
    IDList videos = backend.getVideos4Transcript(transcriptID);   
    IDList audios = backend.getAudios4Transcript(transcriptID);
    
    String previousTranscriptID = null;
    String followingTranscriptID = null;
    IDList allTranscriptsForThisCorpus = backend.getTranscripts4Corpus(corpusID);
    int index = allTranscriptsForThisCorpus.indexOf(transcriptID);
    if (index>0){
        previousTranscriptID = allTranscriptsForThisCorpus.get(index-1);
    } else {
        previousTranscriptID = allTranscriptsForThisCorpus.get(allTranscriptsForThisCorpus.size()-1);
    }
    if (index<allTranscriptsForThisCorpus.size()-1){
        followingTranscriptID = allTranscriptsForThisCorpus.get(index+1);    
    } else {
        followingTranscriptID = allTranscriptsForThisCorpus.get(0);
    }
    
    
    String speechEventName = backend.getSpeechEvent(speechEventID).getName();
    
    Transcript partTranscript = transcript;
    if (aroundAnnotationBlockID.length()>0 && howMuchAround.length()>0){
        startAnnotationBlockID = transcript.getAnnotationBlockID(aroundAnnotationBlockID, -Integer.parseInt(howMuchAround));
        endAnnotationBlockID = transcript.getAnnotationBlockID(aroundAnnotationBlockID, Integer.parseInt(howMuchAround));
    }
    if (startAnnotationBlockID.length()>0 && endAnnotationBlockID.length()>0){
        partTranscript = transcript.getPart(startAnnotationBlockID, endAnnotationBlockID, true);
    }
    TokenList lemmaList4Transcript = partTranscript.getTokenList("lemma");
    //TokenList posList4Transcript = partTranscript.getTokenList("pos");
    
    double startTime = partTranscript.getStartTime();
    
    
    String[][] transcriptParameters = {
        {"FORM", form},
        {"SHOW_NORM_DEV", showNormDev},
        {"VIS_SPEECH_RATE", visSpeechRate},
        {"START_ANNOTATION_BLOCK_ID", startAnnotationBlockID}, 
        {"END_ANNOTATION_BLOCK_ID", endAnnotationBlockID},
        {"AROUND_ANNOTATION_BLOCK_ID", aroundAnnotationBlockID},
        {"HOW_MUCH_AROUND", howMuchAround},
        //{"HIGHLIGHT_IDS", highlightIDsForCurrentTranscript},
        {"HIGHLIGHT_IDS_1", highlightIDsArray[0]},
        {"HIGHLIGHT_IDS_2", highlightIDsArray[1]}, 
        //{"EXTRA_HIGHLIGHT_IDS", extraHighlightIDsForCurrentTranscript}, 
        {"TOKEN_LIST_URL", pathToWordList}
    };
    
    //String transcriptHTML = new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2HTML_STYLESHEET2, transcript.toXML(), transcriptParameters); 
    //String transcriptSVG = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/isotei2score_svg.xsl", transcript.toXML(), transcriptParameters);  
    
    String[][] largeSVGParameters = {
        {"START_ANNOTATION_BLOCK_ID", startAnnotationBlockID}, 
        {"END_ANNOTATION_BLOCK_ID", endAnnotationBlockID},
        {"SIZE", "medium"},        
    };
    String largeSVG = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/isotei2score_svg.xsl", transcript.toXML(), largeSVGParameters);  
    
    String[][] wordlistParameters ={
        {"TOKEN_LIST_URL", pathToWordList},
        {"TOKEN_LIST_ARRAY", tokenList}        
    };
    //String wordListHTML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/tokenlist2html_table.xsl", lemmaList4Transcript.toXML(), wordlistParameters); 
    //String posListHTML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/tokenlist2html_table.xsl", posList4Transcript.toXML()); 

    //http://zumult.ids-mannheim.de/ProtoZumult/ZumultDataServlet?command=getVTT&transcriptID=FOLK_E_00069_SE_01_T_01
    String vttURL = Configuration.getWebAppBaseURL() + "/ZumultDataServlet?command=getVTT&transcriptID=" + transcriptID;
    
%>

<%@include file="../WEB-INF/jspf/locale.jspf" %>     

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuViel : <%= transcriptID %></title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <!-- <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js" integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script> -->
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <script src="https://kit.fontawesome.com/e215b03c17.js" crossorigin="anonymous"></script>
        <link rel="stylesheet" href="../css/transcript.css"/>
        <!-- issue #54 -->
        <link rel="stylesheet" href="../css/transcript_print.css" media="print"/>
        <!-- <script src="../js/transcript.js"></script> -->
        <%@include file="../WEB-INF/jspf/zuVielJS.jspf" %>     
        <script src="../js/metadata.js"></script>
        <script src="../js/download.js"></script>
        <script src="../js/collections.js"></script>
        
        <script>
            // probably all variables from the URL should be mirrored in this way
            var speechEventID = '<%= speechEventID %>';
            var transcriptID = '<%= transcriptID %>';
            var wordlistID = '<%= wordlistID %>';
            var form = '<%= form %>';
            var showNormDev = '<%= showNormDev %>';
            var visSpeechRate = '<%= visSpeechRate %>';

            var startSelection = '<%= startAnnotationBlockID %>';
            var endSelection = '<%= endAnnotationBlockID %>';
            
            var startAnnotationBlockID = '<%= startAnnotationBlockID %>';
            var endAnnotationBlockID = '<%= endAnnotationBlockID %>';
            var startTime = <%= startTime %>;

            var vttURL = '<%= vttURL %>';
            
            var BASE_URL = '<%= Configuration.getWebAppBaseURL() %>';
            
            var highlightIDs1 = '<%= highlightIDsArray[0] %>';
            var highlightIDs2 = '<%= highlightIDsArray[1] %>';
            var highlightIDs3 = '<%= highlightIDsArray[2] %>';
            var highlightIDs4 = '<%= highlightIDsArray[3] %>';

            var tokenList = '<%= tokenList %>';

            function init(){
                initialiseMedia();
                
                $("#tr" + startSelection).addClass("selectionStart");
                $("#tr" + endSelection).addClass("selectionEnd");
            }            
            
            $(document).ready(function(){
        
                $("#selectLang").on("change", function(){
                    var value = $(this).val();
                    var urlTest = new URL(window.location.href);
                    urlTest.searchParams.set('lang',value);
                    window.location = urlTest;
                });
            });
            
            
        </script>
        <script src="../js/media.js"></script>
        
    </head>
    <body onload="init()" id="zuviel-body">
        <%@include file="../WEB-INF/jspf/transcriptNav.jspf" %>                                                

        <!-- ************************************** -->
        <!-- ************************************** -->
        <!-- ************************************** -->
        <div class="row">
        </div>

        <div class="row">

            <!-- *********************** -->
            <!-- *****LEFT COL    ****** -->
            <!-- *********************** -->
            <div class="col-sm-2" id="columnLeft" style="overflow-y: auto;">
                <div style="position:fixed">
                    
                    <!-- **************************** -->
                    <!-- ***** WORDLIST SELECTION   * -->
                    <!-- **************************** -->
                    <div class="input-group mb-3 input-group-sm" style="padding-left:20px; padding-right:10px;visibility:hidden;">
                        <div class="input-group-prepend" style="width:90px;">
                            <span class="input-group-text" id="subtitletypelabel" title="<%=myResources.getString("ReferenceWordlist")%>"><%=myResources.getString("RefWordlist")%></span>
                        </div>
                        <select class="form-control" id="refwordlist" 
                                <% if (!isGerman){ %>
                                    disabled="disabled"
                                <% } %>
                                onchange="changeRefWordlist()">
                          <option value="NONE">None</option>
                          
                          <% String[] wordlistIDs = Constants.LEIPZIG_WORDLISTS;
                                for (String id : wordlistIDs){%>
                                <option value="<%= id %>"
                                    <% if (isGerman && wordlistID.equals(id)) {%>
                                        selected="selected"
                                    <% } %>
                                >
                                <%= id %>
                                </option>
                          <% } %>
                        </select>                          
                    </div>
                    
                        
                    <!-- **************************** -->
                    <!-- ***** WORDLIST             * -->
                    <!-- **************************** -->
                    <div class="wordlist">                    
                        <div id="wordlist-container">
                            <%//= wordListHTML %>
                            <i class="fas fa-spinner fa-spin"></i>
                            <p style="color:gray"><%=myResources.getString("WaitWordlistLoading")%></p>                            
                        </div>
                    </div>
                        
                    <!-- ******************************** -->
                    <!-- ***** DOWNLOAD /PRINT WORDLIST * -->
                    <!-- ******************************** -->
                    <!-- issue #55 -->
                    <div class="container" style="margin-top: 20px;">
                      <div class="row">
                        <div class="col">
                            <button type="button" class="btn btn-secondary btn-lg" title="<%=myResources.getString("PrintDownloadOptions")%>"
                                 data-toggle="modal" data-target="#printDownloadWordlistModal">
                                <i class="fas fa-download"></i>
                                <i class="fas fa-print"></i>                     
                            </button>
                        </div>
                      </div>
                    </div>                    
                    
                        
                        
                </div>

            </div>
                
            <!-- <div class="col-sm-1">
                <div style="position: fixed;" class="wordlist">                    
                    //posListHTML
                </div>
            </div> -->
            
            <!-- *********************** -->
            <!-- ****  CENTER COL   **** -->
            <!-- *********************** -->
            <%
                if (!videos.isEmpty() || !audios.isEmpty()){              
            %>
                    <div class="col-sm-6" id="columnCenter">
            <%
                } else {              
            %>
                    <div class="col-sm-10" id="columnCenter">
            <%
                }             
            %>
                
                <!-- *** EXPAND BEFORE *** -->
                <% if (startAnnotationBlockID!=null && startAnnotationBlockID.length()>0) { %>
                    <button type="button" 
                            onclick="expandTranscript('-1')"
                            class="btn btn-outline-dark btn-sm" 
                            style="width: 100%; border: none; margin-bottom: 5px; margin-left: 15px; margin-right: 15px; padding: 2px;">
                        <i class="fas fa-angle-double-up" aria-hidden="true"></i>
                    </button>                
                <% } %>
                
                <div class="container" id="transcript-container">
                    <%//= transcriptHTML %>
                    <i class="fas fa-spinner fa-spin"></i>
                    <p style="color:gray"><%=myResources.getString("WaitTranscriptLoading")%></p>
                </div>

                <!-- *** EXPAND AFTER *** -->                    
                <% if (endAnnotationBlockID!=null && endAnnotationBlockID.length()>0) { %>
                    <button type="button" 
                            onclick="expandTranscript('1')"
                            class="btn btn-outline-dark btn-sm" 
                            style="width: 100%; border: none;margin-top: 5px; margin-left: 15px; margin-right: 15px; padding: 2px;">
                        <i class="fas fa-angle-double-down" aria-hidden="true"></i>
                    </button>                
                <% } %>
            </div>
            
                
                
            <!-- *********************** -->
            <!-- *********************** -->
            <!-- *********************** -->
            <!-- right column --> 
            <%
                if (!videos.isEmpty() || !audios.isEmpty()){              
            %>
            <div class="col-sm-4" id="columnRight">

                <div style="position: fixed;">                    

                    <div class="svg-wrapper">
                        <div id="svg">
                            <%//= transcriptSVG %>
                            <i class="fas fa-spinner fa-spin"></i>
                            <p style="color:gray"><%=myResources.getString("WaitDensityViewerLoading")%></p>                                
                        </div>                        
                        <div class="magnify-svg">
                            <a href="#svgModal" data-toggle="modal" data-target="#svgModal" id="svgModalAnchor">
                                <i class="fas fa-search-plus"></i>
                            </a>
                        </div>                        
                    </div>


                    <div id="players">
                    <%
                       if (!videos.isEmpty()){ 
                            
                    %>
                            <table>
                                <tr>
                                    <td>
                                        <video id="masterMediaPlayer" width="480" height="270" controls="controls">
                                            <source src="<%=backend.getMedia(videos.get(0)).getURL()%>" type="video/mp4">
                                            <track label="trans" kind="subtitles" srclang="de" src="<%= vttURL %>" default="default">
                                            <track label="norm" kind="subtitles" srclang="de" src="<%= vttURL + "&subtitleType=norm"%>">
                                        </video>          
                                    </td>
                                    <td>
                                        <div style="background: #f8f9fa; height: 270px; border-radius: 3px; padding: 3px;">
                                            <a href="javascript:addVideoImageToCollection('<%= videos.get(0) %>')" 
                                               title="<%=myResources.getString("AddVideoImageCollection")%>" style="color:black">
                                                <i class="far fa-plus-square"></i>
                                            </a><br/>          
                                            <a href="javascript:getVideoImage('<%= videos.get(0) %>')" 
                                               title="<%=myResources.getString("ExtractVideoImage")%>" style="color:black">
                                                <i class="fas fa-camera-retro"></i>
                                            </a><br/>
                                            <a href="javascript:frameBackward()" title="<%=myResources.getString("PrecedingFrame")%>" style="color:black">
                                                <i class="fas fa-step-backward"></i>
                                            </a><br/>
                                            <a href="javascript:frameForward()" title="<%=myResources.getString("NextFrame")%>" style="color:black">
                                                <i class="fas fa-step-forward"></i>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                    <%      if (videos.size()>1){ %>
                            <table>
                                <tr>
                                    <td>
                                        <video id="secondaryVideoPlayer" width="480" height="270" muted="muted">
                                            <source src="<%=backend.getMedia(videos.get(1)).getURL()%>" type="video/mp4">
                                        </video>                            
                                    </td>
                                    <td>
                                        <div style="background: #f8f9fa; height: 270px; border-radius: 3px; padding: 3px;">
                                            <a href="javascript:addVideoImageToCollection('<%= videos.get(1) %>')" title="<%=myResources.getString("AddVideoImageCollection")%>" style="color:black">
                                                <i class="far fa-plus-square"></i>
                                            </a><br/>          
                                            <a href="javascript:getVideoImage('<%= videos.get(1) %>')" title="<%=myResources.getString("ExtractVideoImage")%>" style="color:black">
                                                <i class="fas fa-camera-retro"></i>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                    <%      }
                    } else { %>
                            <audio id="masterMediaPlayer" width="480" controls="controls" style="width:480px;">
                                <source src="<%=backend.getMedia(audios.get(0)).getURL()%>" type="audio/mp3">
                            </audio>                                             
                    <% } %>
                    </div>

                    
                </div>    
            </div> <!-- end right column -->
                    <%}                           
                    %>
        </div>
    </div>
        </div>

        <!-- ************************************** -->
        <!-- ************************************** -->
        <!-- ************************************** -->

        <iframe id="secretIFrame" src="" style="display:none; visibility:hidden;"></iframe>    

        <%@include file="../WEB-INF/jspf/annotationsModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/partiturModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/protocolModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/svgModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/metadataModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/parametersModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/downloadModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/collectionsModal.jspf" %>                                                
        <%@include file="../WEB-INF/jspf/downloadStopperModal.jspf" %>                                                
        <!-- issue #53 -->
        <%@include file="../WEB-INF/jspf/POSHelperModal.jspf" %>                                                
        <!-- issue #55 -->
        <%@include file="../WEB-INF/jspf/printDownloadWordlistModal.jspf" %>                                                

        <script type="text/javascript">
            reloadWordlist();
            reloadTranscript('<%= makeVisibleID %>');
            reloadSVG('small');
            jump(startTime);
            getMasterMediaPlayer().pause();
            
        </script>
    </body>
</html>
