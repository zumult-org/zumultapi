<%-- 
    Document   : zuRechtMetadataStatisticView
    Created on : 22.07.2020, 09:04:41
    Author     : Elena
--%>


<%@page import="java.util.TreeMap"%>
<%@page import="java.util.stream.Collectors"%>
<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.HashMap"%>
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
        Set<MetadataKey> metadataKeyIDs = backendInterface.getMetadataKeysForGroupingHits(corpusQuery, mode, null);
        Map<String, String> metadataKeyIDsMap = new HashMap();
        for(MetadataKey key :  metadataKeyIDs){ 
            if (!key.getLevel().equals(ObjectTypesEnum.HIT)){
                String id = key.getID();
                String name = key.getName(currentLocale.getLanguage());
                String level="";
                if (key.getLevel().equals(ObjectTypesEnum.EVENT)){
                    level = "E";
                }else if (key.getLevel().equals(ObjectTypesEnum.SPEAKER)){
                    level = "S";
                }else if (key.getLevel().equals(ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT)){
                    level = "SES";
                }else if (key.getLevel().equals(ObjectTypesEnum.SPEECH_EVENT)){
                    level = "SE";
                }else if (key.getLevel().equals(ObjectTypesEnum.TRANSCRIPT)){
                    level = "T";
                }                  
                name = level + ": " + name;
                metadataKeyIDsMap.put(id, name);
            }
        }
        Map<String, String> sorted = IOHelper.sortMapByValue(metadataKeyIDsMap);
               
        %>
        
        <label class="ml-3 mt-3" for="sel-metadata"><%=myResources.getString("AvailableMetadata")%> (<%=myResources.getString("SelectOne")%>):</label>
        <select class="ml-3 mt-3" id="sel-metadata" name="sel-metadata">
            <%  
                for(String key :  sorted.keySet()){ 
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

            function getKWIC(obj, pageLength,pageIndex, context, metadataKeyID, numberOfHits){
                var newMetadataKeyValue = $(obj).parent().parent().find('td.metadataValues').text();
                var additionalMetadata = metadataKeyID + "=" + newMetadataKeyValue;
                
                var newQuery = getNewQueryForSearchByMetadata(metadataKeyID, newMetadataKeyValue);
                
                var cell = $(obj).parent();
                $(obj).remove();
                            
                startAjaxForSearchByMetadata(pageLength, pageIndex, context, numberOfHits, additionalMetadata, newQuery, cell);
            }
            
        </script>
    </body>
</html>
