<%-- 
    Document   : mapdemo
    Created on : 13.12.2019, 09:50:33
    Author     : Thomas.Schmidt
--%>

<%@page import="org.zumult.objects.SpeechEvent"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.objects.Event"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.objects.Transcript"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
        <%
            String eventID = request.getParameter("eventID");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Event event = backendInterface.getEvent(eventID);
            IDList speechEventIDs = event.getSpeechEvents();
            IDList allSpeakers = new IDList("speaker");
            for (String speechEventID : speechEventIDs){
                SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
                IDList speakers = speechEvent.getSpeakers();
                allSpeakers.addAll(speakers);
            }
            double lat = event.getLocation().getLatitude();
            double lon = event.getLocation().getLongitude();

        %>



        
 <html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Map Demo</title>
<style type="text/css">
      html, body {
          width: 100%;
          height: 100%;
          margin: 0;
      }
      #basicMap {
          width: 500px;
          height: 500px;
      }
    </style>
    <script src="http://www.openlayers.org/api/OpenLayers.js"></script>
    <script>
      function init() {
        map = new OpenLayers.Map("basicMap");
        var mapnik = new OpenLayers.Layer.OSM();
        map.addLayer(mapnik);
        map.setCenter(new OpenLayers.LonLat(<%= lon %>,<%= lat %>) // Center of the map
          .transform(
            new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
            new OpenLayers.Projection("EPSG:900913") // to Spherical Mercator Projection
          ), 15 // Zoom level
        );
        map.fitBounds([
          [-4.8587000, 39.8772333],
          [-6.4917667, 39.0945000]
        ]);
      }
    </script>
  </head>
    </head>
  <body onload="init();">
        <h1><%= eventID %></h1>
        <div id="basicMap"></div>
        
    </body>
</html>
