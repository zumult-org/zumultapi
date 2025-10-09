/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package org.zumult.webservices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.implementations.COMAFileSystem;
import org.zumult.io.IOHelper;
import org.zumult.objects.Corpus;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Speaker;

/**
 *
 * @author bernd
 */
public class DataTableServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String command = request.getParameter("command");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            
            // delegate depending on the command
            switch (command){
                case "speakers" :
                    if (backendInterface instanceof COMAFileSystem){
                        speakersCOMABackend(request, response);
                    } else {
                        speakers(request, response);
                    }
                    break;
                case "communications" :     
                    if (backendInterface instanceof COMAFileSystem){
                        communicationsCOMABackend(request, response);
                    } else {
                        communications(request, response);
                    }
                    break;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DataTableServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    
    // this is a version reyling only on the abstract BackendInterface
    // It is okay for retrieving an unsorted page
    // But it has performance issues when sorting
    private void speakers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String corpusID = request.getParameter("corpusID");
            String draw = request.getParameter("draw");
            int start = Integer.parseInt(request.getParameter("start"));
            int length = Integer.parseInt(request.getParameter("length"));
            
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Corpus corpus = backendInterface.getCorpus(corpusID);
            Set<MetadataKey> metadataKeys = corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER);  
            Map<String, MetadataKey> metadataNames2Keys = new HashMap<>();
            for (MetadataKey metadataKey : metadataKeys){
                metadataNames2Keys.put(metadataKey.getName("en"), metadataKey);
            }
                        
            List<Map<String, String>> columns = new ArrayList<>();

            // this is a rather complex way of getting the actual data objects from the request
            // there does not seem to be an easier way...
            int colIndex = 0;
            while (true) {
                String prefix = "columns[" + colIndex + "]";
                String data = request.getParameter(prefix + "[data]");
                if (data == null) {
                    break; // No more columns
                }

                Map<String, String> column = new HashMap<>();
                column.put("data", data);
                column.put("name", request.getParameter(prefix + "[name]"));
                column.put("searchable", request.getParameter(prefix + "[searchable]"));
                column.put("orderable", request.getParameter(prefix + "[orderable]"));
                column.put("searchValue", request.getParameter(prefix + "[search][value]"));
                column.put("searchRegex", request.getParameter(prefix + "[search][regex]"));

                columns.add(column);
                colIndex++;
            }        
            
            Set<String> metadataKeyNames = new HashSet<>();
            for (Map<String,String> column : columns){
                String metadataKeyName = column.get("data");
                metadataKeyNames.add(metadataKeyName);
            }
            
            // and this is to find the name of the column by which to order
            String orderColumnIndex = request.getParameter("order[0][column]");
            // Now find the column name using columns[<index>][data]
            String orderColumnName = request.getParameter("columns[" + orderColumnIndex + "][data]");     
            IDList speakerIDs = backendInterface.getSpeakers4Corpus(corpusID);
            
            if (orderColumnIndex!=null && orderColumnName!=null && orderColumnName.length()>0){
                String orderDirection = request.getParameter("order[0][dir]"); // "asc" or "desc"
                
                
                speakerIDs.sort(new Comparator<String>(){
                    @Override
                    public int compare(String speakerID1, String speakerID2) {
                        if (orderColumnName.equals("ID")){
                            int comparison = speakerID1.compareTo(speakerID2);
                            if (orderDirection.equals("desc")){
                                return -comparison;
                            }
                            return comparison;                        
                        }
                        try {
                            MetadataKey compareKey = metadataNames2Keys.get(orderColumnName);
                            Speaker speaker1 = backendInterface.getSpeaker(speakerID1);
                            Speaker speaker2 = backendInterface.getSpeaker(speakerID1);
                            String value1 = speaker1.getMetadataValue(compareKey);
                            String value2 = speaker2.getMetadataValue(compareKey);
                            int comparison = value1.compareTo(value2);
                            if (orderDirection.equals("desc")){
                                return -comparison;
                            }
                            return comparison;
                        } catch (IOException ex) {
                            Logger.getLogger(DataTableServlet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return 0;
                    }                
                });
            }
            
            /*
            CREATE AND WRITE THE OUTPUT NOW
            {
            "draw": 1,
            "recordsTotal": 1000,
            "recordsFiltered": 1000,
            "data": [
                {
                    "name": "John Doe",
                    "position": "Developer",
                    "office": "NY",
                    "age": 30,
                    "start_date": "2021-01-01",
                    "salary": "$100,000"
                },
                ...
            ]            
                    */
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("draw", draw);
            root.put("recordsTotal", speakerIDs.size());
            root.put("recordsFiltered", speakerIDs.size());
            ArrayNode dataArray = mapper.createArrayNode();

            
            
            for (int i=start; i<=Math.min(start+length, speakerIDs.size()-1); i++){
                String speakerID = speakerIDs.get(i);
                Speaker speaker = backendInterface.getSpeaker(speakerID);
                ObjectNode row = mapper.createObjectNode();
                row.put("ID", speakerID);
                for (MetadataKey metadataKey : metadataKeys){
                    String metadataKeyName = metadataKey.getName("en");
                    if (!(metadataKeyNames.contains(metadataKeyName))) continue;
                    String metadataValue = speaker.getMetadataValue(metadataKey);
                    row.put(metadataKeyName, metadataValue);
                    
                }
                dataArray.add(row);
            }
            
            root.set("data", dataArray);     
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Write JSON
            mapper.writeValue(response.getWriter(), root);  
            
            response.getWriter().close();            
            
            
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(DataTableServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }
    
    static Map<String, String> cachedCOMAXML = new HashMap<>();
    
    // this version is faster but relies on transforming the COMA file
    // so it is specific to the COMA Backend
    private void speakersCOMABackend(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String corpusID = request.getParameter("corpusID");
            String draw = request.getParameter("draw");
            String start = request.getParameter("start");
            String length = request.getParameter("length");
            
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Corpus corpus = backendInterface.getCorpus(corpusID);
            if (!cachedCOMAXML.containsKey(corpusID)){
                String comaXML = corpus.toXML();
                cachedCOMAXML.put(corpusID, comaXML);
            }
            
            String thisCorpusXML = cachedCOMAXML.get(corpusID);
            
            Set<MetadataKey> metadataKeys = corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER);                          
            List<Map<String, String>> columns = new ArrayList<>();

            // this is a rather complex way of getting the actual data objects from the request
            // there does not seem to be an easier way...
            int colIndex = 0;
            while (true) {
                String prefix = "columns[" + colIndex + "]";
                String data = request.getParameter(prefix + "[data]");
                if (data == null) {
                    break; // No more columns
                }

                Map<String, String> column = new HashMap<>();
                column.put("data", data);
                column.put("name", request.getParameter(prefix + "[name]"));
                column.put("searchable", request.getParameter(prefix + "[searchable]"));
                column.put("orderable", request.getParameter(prefix + "[orderable]"));
                column.put("searchValue", request.getParameter(prefix + "[search][value]"));
                column.put("searchRegex", request.getParameter(prefix + "[search][regex]"));

                columns.add(column);
                colIndex++;
            }        
            
            Set<String> metadataKeyNames = new HashSet<>();
            for (Map<String,String> column : columns){
                String metadataKeyName = column.get("data");
                if (metadataKeyName!=null && metadataKeyName.length()>0){
                    metadataKeyNames.add(metadataKeyName);
                }
            }
            
            // and this is to find the name of the column by which to order
            String orderColumnIndex = request.getParameter("order[0][column]");
            // Now find the column name using columns[<index>][data]
            String orderColumnName = request.getParameter("columns[" + orderColumnIndex + "][data]");     
            String orderDirection = request.getParameter("order[0][dir]"); // "asc" or "desc"
            
            String searchTerm = request.getParameter("search[value]");
            
            
            
            //System.out.println("*********** " + String.join(";", metadataKeyNames));
            
            String[][] parameters = {
                {"METADATA_KEY_NAMES", String.join(";", metadataKeyNames)},
                {"ORDER_COLUMN_NAME", orderColumnName},
                {"ORDER_DIRECTION", orderDirection},
                {"START", start},
                {"LENGTH", length},
                {"SEARCH_TERM", searchTerm},
            };
            
            String json = new IOHelper().applyInternalStylesheetToString("/resources/speakers2DataTableJSON.xsl", thisCorpusXML, parameters);
            IDList speakerIDs = backendInterface.getSpeakers4Corpus(corpusID);
            
            //System.out.println(json);
            
            
            /*
            CREATE AND WRITE THE OUTPUT NOW
            {
            "draw": 1,
            "recordsTotal": 1000,
            "recordsFiltered": 1000,
            "data": [
                {
                    "name": "John Doe",
                    "position": "Developer",
                    "office": "NY",
                    "age": 30,
                    "start_date": "2021-01-01",
                    "salary": "$100,000"
                },
                ...
            ]            
                    */
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("draw", draw);
            root.put("recordsTotal", speakerIDs.size());
            
            
            JsonNode jsonNode = mapper.readTree(json);
            String recordsFiltered = jsonNode.get("recordsFiltered").asText();
            root.put("recordsFiltered", recordsFiltered);

            JsonNode dataArray = jsonNode.get("data");
            root.set("data", dataArray);     

            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Write JSON
            mapper.writeValue(response.getWriter(), root);  
            
            response.getWriter().close();            
            
            
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(DataTableServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);            
        } catch (Exception ex) {
            Logger.getLogger(DataTableServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);            
        }
        
    }

    private void communications(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    

    private void communicationsCOMABackend(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String corpusID = request.getParameter("corpusID");
            String draw = request.getParameter("draw");
            String start = request.getParameter("start");
            String length = request.getParameter("length");
            
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Corpus corpus = backendInterface.getCorpus(corpusID);

            if (!cachedCOMAXML.containsKey(corpusID)){
                String comaXML = corpus.toXML();
                cachedCOMAXML.put(corpusID, comaXML);
            }
            
            String thisCorpusXML = cachedCOMAXML.get(corpusID);
            
            
            Set<MetadataKey> metadataKeys = corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER);                          
            List<Map<String, String>> columns = new ArrayList<>();

            // this is a rather complex way of getting the actual data objects from the request
            // there does not seem to be an easier way...
            int colIndex = 0;
            while (true) {
                String prefix = "columns[" + colIndex + "]";
                String data = request.getParameter(prefix + "[data]");
                if (data == null) {
                    break; // No more columns
                }

                Map<String, String> column = new HashMap<>();
                column.put("data", data);
                column.put("name", request.getParameter(prefix + "[name]"));
                column.put("searchable", request.getParameter(prefix + "[searchable]"));
                column.put("orderable", request.getParameter(prefix + "[orderable]"));
                column.put("searchValue", request.getParameter(prefix + "[search][value]"));
                column.put("searchRegex", request.getParameter(prefix + "[search][regex]"));

                columns.add(column);
                colIndex++;
            }        
            
            Set<String> metadataKeyNames = new HashSet<>();
            for (Map<String,String> column : columns){
                String metadataKeyName = column.get("data");
                if (metadataKeyName!=null && metadataKeyName.length()>0){
                    metadataKeyNames.add(metadataKeyName);
                }
            }
            
            // and this is to find the name of the column by which to order
            String orderColumnIndex = request.getParameter("order[0][column]");
            // Now find the column name using columns[<index>][data]
            String orderColumnName = request.getParameter("columns[" + orderColumnIndex + "][data]");     
            String orderDirection = request.getParameter("order[0][dir]"); // "asc" or "desc"
            
            String searchTerm = request.getParameter("search[value]");
            
            
            
            //System.out.println("*********** " + String.join(";", metadataKeyNames));
            
            String[][] parameters = {
                {"METADATA_KEY_NAMES", String.join(";", metadataKeyNames)},
                {"ORDER_COLUMN_NAME", orderColumnName},
                {"ORDER_DIRECTION", orderDirection},
                {"START", start},
                {"LENGTH", length},
                {"SEARCH_TERM", searchTerm},
            };
            
            String json = new IOHelper().applyInternalStylesheetToString("/resources/communications2DataTableJSON.xsl", thisCorpusXML, parameters);
            IDList communicationIDs = backendInterface.getSpeechEvents4Corpus(corpusID);
            
            //System.out.println(json);
            
            
            /*
            CREATE AND WRITE THE OUTPUT NOW
            {
            "draw": 1,
            "recordsTotal": 1000,
            "recordsFiltered": 1000,
            "data": [
                {
                    "name": "John Doe",
                    "position": "Developer",
                    "office": "NY",
                    "age": 30,
                    "start_date": "2021-01-01",
                    "salary": "$100,000"
                },
                ...
            ]            
                    */
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("draw", draw);
            root.put("recordsTotal", communicationIDs.size());
            
            
            JsonNode jsonNode = mapper.readTree(json);
            String recordsFiltered = jsonNode.get("recordsFiltered").asText();
            root.put("recordsFiltered", recordsFiltered);

            JsonNode dataArray = jsonNode.get("data");
            root.set("data", dataArray);     

            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Write JSON
            mapper.writeValue(response.getWriter(), root);  
            
            response.getWriter().close();            
            
            
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(DataTableServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);            
        } catch (Exception ex) {
            Logger.getLogger(DataTableServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);            
        }
        
    }
    
    
    
}
