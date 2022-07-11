/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class getSpeechEvents extends HttpServlet {
    BackendInterface backendInterface;
    String DATA_PATH = "src/java/data/";
    String IDLISTS_PATH = DATA_PATH + "IDLists/";
    
    public getSpeechEvents() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        backendInterface = BackendInterfaceFactory.newBackendInterface();
    }
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("text/html;charset=UTF-8");
//        try (PrintWriter out = response.getWriter()) {
//            /* TODO output your page here. You may use following sample code. */
//            out.println("<!DOCTYPE html>");
//            out.println("<html>");
//            out.println("<head>");
//            out.println("<title>Servlet getSpeechEvents</title>");            
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet getSpeechEvents at " + request.getContextPath() + "</h1>");
//            out.println("</body>");
//            out.println("</html>");
//        }
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String corpusID = request.getParameter("corpusID");
            System.out.println(request.getRequestURI());
            System.out.println(request.getServletPath());
            // same problem with the path as the one elena had
//            Path speechEventsFilePath = new File(UriBuilder.fromPath(IDLISTS_PATH + corpusID).path(corpusID + "_speechEvents.txt").build().toString()).toPath();
//            Path speechEventsFilePath = new File("/DGDRESTTest/" + IDLISTS_PATH + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
//            IDList speechEventIDs = new IDList("speechEvents");
//            speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));

//            System.out.println(speechEventIDs);

            String speechEvenetsListString = "";
            for (String eventID : backendInterface.getEvents4Corpus(corpusID)){
                for (String speechEvent : backendInterface.getSpeechEvents4Event(eventID)) {
                    System.out.println("speechEvent: " + speechEvent);
                    speechEvenetsListString += speechEvent + " ";
                }
            }

            response.setContentType("text/html");
            response.getWriter().print(speechEvenetsListString);
        } catch (Exception ex) {
            Logger.getLogger(getMoreTranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
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

}
