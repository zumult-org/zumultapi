<%-- 
    Document   : zuRechtHitStatisticView
    Created on : 22.07.2020, 09:04:41
    Author     : Elena
--%>


<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="org.zumult.query.searchEngine.MTASBasedSearchEngine"%>
<%@page import="org.zumult.objects.AnnotationTypeEnum"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.zumult.objects.AnnotationLayer"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="org.zumult.objects.MetadataKey"%>
<%@page import="org.zumult.objects.Corpus"%>
<%@page import="org.zumult.backend.Configuration"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.lang.Integer"%>
<%@page import="org.zumult.io.Constants"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.query.serialization.SearchResultSerializer"%>
<%@page import="org.zumult.query.searchEngine.SortTypeEnum"%>
<%@page import="org.zumult.query.SearchStatistics"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Statistic View Page</title>

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
        
        <script src="../js/xslTransformation.js" type="text/javascript"></script>
        <script src="../js/tableSorter.js" type="text/javascript"></script>
        
        <script src="../js/zuRecht.collapsible.js" type="text/javascript"></script>
        <script src="../js/query.stringConverter.js" type="text/javascript"></script>
        
        <link rel="stylesheet" type="text/css" href="../css/query.css" />
        
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
    </head>
    <body>
        <%@include file="../WEB-INF/jspf/locale.jspf" %> 
        <%@include file="../WEB-INF/jspf/zuRechtStatisticViewJava.jspf" %> 
        <%        
        Set<AnnotationLayer> anntationLayersForToken = backendInterface.getAnnotationLayersForGroupingHits(corpusQuery, mode, AnnotationTypeEnum.TOKEN.name());
        Set<AnnotationLayer> anntationLayersForSpan = backendInterface.getAnnotationLayersForGroupingHits(corpusQuery, mode, AnnotationTypeEnum.SPAN.name());
        Set<MetadataKey> metadataKeyIDs = backendInterface.getMetadataKeysForGroupingHits(corpusQuery, mode, ObjectTypesEnum.HIT.name());
        
        Map<String, String> anntationLayersForTokenMap = new HashMap();

        for(AnnotationLayer key :  anntationLayersForToken){ 
            String id = key.getID();
            String name = key.getName(currentLocale.getLanguage());
            anntationLayersForTokenMap.put(id, name);
        }
        
        Map<String, String> anntationLayersMap = new HashMap(); 
        anntationLayersMap.putAll(anntationLayersForTokenMap);
                
        for(AnnotationLayer key :  anntationLayersForSpan){ 
            String id = key.getID();
            String name = key.getName(currentLocale.getLanguage());
            anntationLayersMap.put(id, name);
        }
        
        for(MetadataKey key :  metadataKeyIDs){ 
            String id = key.getID();
            String name = key.getName(currentLocale.getLanguage());
            anntationLayersMap.put(id, name);
        }
           
        anntationLayersMap.remove(Constants.ELEMENT_NAME_PC);
        anntationLayersMap.remove(Constants.ELEMENT_NAME_VOCAL);
        anntationLayersMap.remove(Constants.ELEMENT_NAME_INCIDENT);
        anntationLayersMap.remove(Constants.METADATA_KEY_MATCH_TYPE_PARA);
        
        anntationLayersMap.remove(Constants.METADATA_KEY_MATCH_TYPE_PAUSE_DURATION);
        anntationLayersMap.remove(Constants.METADATA_KEY_MATCH_TYPE_PAUSE_TYPE);       
        anntationLayersMap.remove(Constants.METADATA_KEY_MATCH_TYPE_PAUSE_DURATION_CEIL);
        
        anntationLayersMap.remove(Constants.METADATA_KEY_MATCH_TYPE_WORD + Constants.METADATA_KEY_MATCH_LOWERCASE);
        anntationLayersMap.remove(Constants.ATTRIBUTE_NAME_NORM + Constants.METADATA_KEY_MATCH_LOWERCASE);
        anntationLayersMap.remove(Constants.ATTRIBUTE_NAME_LEMMA + Constants.METADATA_KEY_MATCH_LOWERCASE);
        
        anntationLayersMap.remove(Constants.ELEMENT_NAME_ANNOTATION_BLOCK);
        anntationLayersMap.remove(Constants.METADATA_KEY_MATCH_TYPE_ANNOTATION_BLOCK_SPEAKER);
        anntationLayersMap.remove(Constants.SPANGRP_TYPE_SPEAKER_OVERLAP);
        
        Map<String, String> sorted = IOHelper.sortMapByValue(anntationLayersMap);

        %>
        <label class="ml-3 mt-3" for="sel-metadata"><%=myResources.getString("GroupedBy")%> (<%=myResources.getString("SelectOne")%>):</label>
        <select class="ml-3 mt-3" id="sel-metadata" name="sel-metadata">
            <%  for(String key :  sorted.keySet()){ 
                    String name = sorted.get(key);
            %>
            <option value="<%=key %>"   <% if(key.equals(metadataKeyID)){ metadataKeyName=name; %> selected <%}  %>  ><%=name %> </option>
            
           <% } %>
        </select>
        
               
        <!-- loading indicator -->
        <div id="wait-page" class="ml-3">Loading... <img src='../images/loading.gif' width="64" height="64" alt="Loading indicator"/></div>
        
        <%@include file="../WEB-INF/jspf/zuRechtConstants.jspf" %>
        <%@include file="../WEB-INF/jspf/zuRechtStatisticViewJS.jspf" %>
        <script type="text/javascript">
            
            const tokenAnnotations = new Array(<%
                List<String> tokenAnnotationsIDs = new ArrayList();
                tokenAnnotationsIDs.addAll(anntationLayersForTokenMap.keySet());
                System.out.println(tokenAnnotationsIDs);

                int i = 0;
                for(String str : tokenAnnotationsIDs) {
                  out.print("\""+str+"\"");
                  i++;
                  if(i < tokenAnnotationsIDs.size()) {
                    out.print(",");
                  }
                }
                %>);

            function getNewQueryForSearchByTokenAnnotations(metadataKeyID, metadataKeyValue){
                var newQuery =  "(" + query + ") fullyalignedwith (";
                var array = metadataKeyValue.split('<%=Constants.TOKEN_DELIMITER %>');
                for (var i = 0; i < array.length; i++) {
                    if (array[i]==="<%=Constants.EMPTY_TOKEN %>"){
                        newQuery += "[!"+metadataKeyID+"=\".*\"]";
                    }else{
                        newQuery += "["+metadataKeyID+"=\""+ array[i].replace(/&/g, "\\&").replace(/\#/g, "\\\#").replace(/\+/g, "\\+") + "\"]"; 
                    }
                }
                newQuery +=  ")";
                return newQuery;
            }

            function getKWIC(obj, pageLength,pageIndex, context, metadataKeyID, numberOfHits){                
                var newMetadataKeyValue = $(obj).parent().parent().find('td.metadataValues').attr('data-value');
                //alert("newMetadataKeyValue: " + newMetadataKeyValue + ", metadataKeyID: " + metadataKeyID);
                
                var cell = $(obj).parent();
                $(obj).remove();   
                
                var meta = null;
                var newQuery = query;
                
                if(metadataKeyID===tokenSizeKeyID){
                    meta = metadataKeyID + "=" + escapeParentheses(newMetadataKeyValue);
                    startAjaxForSearchByTokenAnnotations(pageLength, pageIndex, context, numberOfHits, meta, newQuery, cell, metadataKeyID, newMetadataKeyValue);
                }else if (tokenAnnotations.includes(metadataKeyID)){
                    //alert("metadataKeyID: " + metadataKeyID);
                    newQuery = getNewQueryForSearchByTokenAnnotations(metadataKeyID, newMetadataKeyValue);
                    //alert("newMetadataKeyValue " + newMetadataKeyValue);
                    startAjaxForSearchByTokenAnnotations(pageLength, pageIndex, context, numberOfHits, meta, newQuery, cell, metadataKeyID, newMetadataKeyValue);
                }else{                     
                    meta = metadataKeyID + "=" + newMetadataKeyValue;
                    newQuery = getNewQueryForSearchByMetadata(metadataKeyID, newMetadataKeyValue);
                    startAjaxForSearchByMetadata(pageLength, pageIndex, context, numberOfHits, meta, newQuery, cell);
                }
            }

        </script>
    </body>
</html>
