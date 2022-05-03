<%-- 
    Document   : folkEventIndex
    Created on : 01.01.2020, 12:49:51
    Author     : thomas.schmidt
--%>

<%@page import="org.zumult.objects.Corpus"%>
<%@page import="org.zumult.objects.MetadataKey"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<% 
    BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
    String corpusID = request.getParameter("corpusID");
    Corpus corpus = backend.getCorpus(corpusID);
    String corpusAcronym = corpus.getAcronym();
    String corpusName = corpus.getName("de");
    String indexName = "/data/" + corpusID + "_SpeechEventIndex.xml";
    String groupingKey = request.getParameter("groupingKey");
    if (groupingKey==null){
        /*switch (corpusID){
            case "FOLK" : groupingKey = "e_se_interaktionsdomaene"; break;
            case "GWSS" : groupingKey = "e_land"; break;
            case "DNAM" : groupingKey = "e_se_art"; break;
            case "DH--" : groupingKey = "e_region"; break;
        }*/
        groupingKey = "";
    }    
    String columnKeys = request.getParameter("columnKeys");
    if (columnKeys==null){
        switch (corpusID){
            case "FOLK" : columnKeys = "e_se_interaktionsdomaene e_se_art e_se_lebensbereich e_se_aktivitaet e_se_konstellation"; break;
            case "GWSS" : columnKeys = "e_se_art e_land e_se_themen"; break;
            case "DNAM" : columnKeys = "e_se_art e_ort"; break;
            case "DH--" : columnKeys = "e_ort e_region e_se_art"; break;
            case "ZW--" : columnKeys = "e_ort e_kreis e_region e_planquadrat"; break;
        }        
    }
    /*
        <keys>
            <key id="e_se_art" name="Kurzbezeichnung"/>
    
    */
    String columnKeyString = "";
    for (String columnKeyID : columnKeys.split(" ")){
        MetadataKey key = backend.findMetadataKeyByID("v_" + columnKeyID);
        String columnName = columnKeyID;
        if (key!=null){
            columnName = key.getName("de");
        }
        columnKeyString+=columnKeyID + "---" + columnName + "***";
    }
    
    String[][] parameters = {
        {"GROUPING_KEY", groupingKey},
        {"COLUMN_KEYS_STRING", columnKeyString}    
    };
    String html = new IOHelper().applyInternalStylesheetToInternalFile("/org/zumult/io/speechEvents2Table.xsl", indexName, parameters);
    
    String gespraechsTypTreeJSON = new IOHelper().readInternalResource("/data/prototypeJson/gesprachstypTreeselectFOLK.json");
    String artJSON = new IOHelper().readInternalResource("/data/prototypeJson/artFOLK.json");
    String themenJSON = new IOHelper().readInternalResource("/data/prototypeJson/themenFOLK.json");
    String sprachregionenJSON = new IOHelper().readInternalResource("/data/prototypeJson/sprachregionTreeselectFOLK.json");

%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><%= corpusAcronym %>: <%= corpusName %></title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <script src="../js/metadata.js"></script>  
        <link rel="stylesheet" href="../css/overview.css"/>       
        
        <!-- <script src="https://cdn.jsdelivr.net/npm/vue@^2"></script> -->
        <!-- include vue-treeselect & its styles. you can change the version tag to better suit your needs. -->
        <!-- <script src="https://cdn.jsdelivr.net/npm/@riophae/vue-treeselect@^0.4.0/dist/vue-treeselect.umd.min.js"></script> -->
        <!-- <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@riophae/vue-treeselect@^0.4.0/dist/vue-treeselect.min.css"/>   -->
        
        <!-- <link href="../js/nouislider.min.css" rel="stylesheet"> -->
        <!-- In <body> -->
        <!-- <script src="../js/nouislider.min.js"></script>         -->
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
    </head>
    <body>
        <%@include file="../WEB-INF/jspf/overviewNav.jspf" %>                                                
        
            <div class="row">
                <!-- <div class="col-sm-2" style="margin-left:20px;">
                    <form id="filterForm" action="javascript:submitFilter()">
                        <div class="form-group">

                          <label for="interactiondomainselect">Interaktionsdomäne/Lebensbereich</label>
                          <div class="form-group" id="app">
                            <treeselect name="interactiondomainselect" v-model="value" :multiple="true" :options="options" />
                          </div>
                          
                          <label for="artselect">Kurzbezeichnung</label>
                          <div class="form-group" id="app2">
                            <treeselect name="artselect" v-model="value" :multiple="true" :options="options" />
                          </div>
                          
                          <label for="themenselect">Themen</label>
                          <div class="form-group" id="app3">
                            <treeselect name="themenselect" v-model="value" :multiple="true" :options="options" />
                          </div>

                          <label for="sprachregionenselect">Sprachregionen</label>
                          <div class="form-group" id="app4">
                            <treeselect name="sprachregionenselect" v-model="value" :multiple="true" :options="options" />
                          </div>
                          
                          <label for="slider_norm">Standardnähe</label>
                          <div class="form-group">
                            <div id="slider_norm"></div>
                          </div>

                          <label for="slider_overlap">Überlapppungen</label>
                          <div class="form-group">
                            <div id="slider_overlap"></div>
                          </div>


                          <button type="submit" class="btn btn-primary">Filtern</button>

                        </div>

                        
                    </form>   

                </div> -->
                <div class="col-sm-1">
                </div>
                <div class="col-sm-10">
                    <div class="container w-100">
                        <%= html %>
                    </div>
                </div>
                <div class="col-sm-1">
                </div>
            </div>
     

        <!-- ************************************** -->
        <!-- ************************************** -->
        <!-- ************************************** -->

        <%@include file="../WEB-INF/jspf/metadataModal.jspf" %>                                                
        <!-- <script>
          // register the component
          Vue.component('treeselect', VueTreeselect.Treeselect);
          //Vue.component('slider', VueTreeselect.Treeselect)

          new Vue({
            el: '#app',
            data: {
              // define the default value
              value: null,
              // define options
              options: <%= gespraechsTypTreeJSON %>,
            },
          });
          

          new Vue({
            el: '#app2',
            data: {
              // define the default value
              value: null,
              // define options
              options: <%= artJSON %>,
            },
          });
          
          new Vue({
            el: '#app3',
            data: {
              // define the default value
              value: null,
              // define options
              options: <%= themenJSON %>,
            },
          });

          new Vue({
            el: '#app4',
            data: {
              // define the default value
              value: null,
              // define options
              options: <%= sprachregionenJSON %>,
            },
          });

          var slider_norm = document.getElementById('slider_norm');
          var slider_overlap = document.getElementById('slider_overlap');

          noUiSlider.create(slider_norm, {
              start: [20, 80],
              connect: true,
              range: {
                  'min': 0,
                  'max': 100
              }
          });          


          noUiSlider.create(slider_overlap, {
            start: [40, 60],
            connect: true,
            range: {
                'min': 0,
                'max': 100
            }
          });          

    function submitFilter(){
              var f = $('#filterForm').serialize();
              f+=slider_norm.noUiSlider.get();
              f+=slider_overlap.noUiSlider.get();
              alert(f);
          }
          
        </script> -->

    </body>
</html>
