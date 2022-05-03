/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.IOUtilities;
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class getMoreTranscript extends HttpServlet {
    BackendInterface backendInterface;

    public getMoreTranscript() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
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
//            out.println("<title>Servlet getMoreTranscript</title>");            
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet getMoreTranscript at " + request.getContextPath() + "</h1>");
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
            String transcriptID = request.getParameter("transcriptID");
            double passedEndTime = Double.parseDouble(request.getParameter("endTime"));
            String wordlistID = request.getParameter("wordlistID");
            String pathToWordList = new File(getServletContext().getRealPath("/data/" + wordlistID + ".xml")).toURI().toString();
            String corpusID = transcriptID.substring(0, 4);
            String speechEventID = transcriptID.substring(0, 18);            
            String audioID = backendInterface.getAudios4Transcript(transcriptID).get(0);
            Transcript transcript = backendInterface.getTranscript(transcriptID);

            double startTime = transcript.getStartTime();
//            double newEndTime = transcript.getEndTime() > passedEndTime + 60.0
//                    ? passedEndTime + 60.0
//                    : transcript.getEndTime();
            double endTime = transcript.getEndTime() > passedEndTime
                    ? passedEndTime
                    : transcript.getEndTime();
//            Transcript partTranscript = transcript.getPart(passedEndTime, newEndTime, true);
            Transcript partTranscript = transcript.getPart(startTime, endTime, true);
            String partTranscriptXML = partTranscript.toXML();
            System.out.println(partTranscriptXML);

            String[][] xslParameters = {
                                {"TOKEN_LIST_URL", pathToWordList},
                                {"CORPUS_ID", corpusID},
                                {"SPEECH_EVENT_ID", speechEventID},
                                {"AUDIO_ID", audioID}
                            };
                                                
            String partTranscriptHTML = 
                    new IOHelper().applyInternalStylesheetToString(
                            Constants.ISOTEI2HTML_HIGHLIGHT_TOKENS_STYLESHEET, 
                            partTranscriptXML,
                            xslParameters);
            response.setContentType("text/html");
            response.getWriter().print(partTranscriptHTML);
//            processRequest(request, response);
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
