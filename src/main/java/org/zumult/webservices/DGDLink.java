/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.objects.Transcript;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGDLink extends HttpServlet {

    BackendInterface backendInterface;
    
    public DGDLink() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        //http://localhost:8084/DGDRESTTest/DGDLink?command=showTranscriptExcerpt&transcriptID=FOLK_E_00066_SE_01_T_01&=tokenIDs=w228
        
        String command = request.getParameter("command");
        switch (command){
            case "showTranscriptExcerpt" : 
                showTranscriptExcerpt(request, response);
                break;
            case "showSpeakerMetadata" : 
                showSpeakerMetadata(request, response);
                break;
            case "showData" : 
                showData(request, response);
                break;
            default : 
                response.setContentType("text/html;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    /* TODO output your page here. You may use following sample code. */
                    out.println("<!DOCTYPE html>");
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>Servlet DGDLink</title>");            
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Servlet DGDLink at " + request.getContextPath() + "</h1>");
                    out.println("</body>");
                    out.println("</html>");
                }
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

    private void showTranscriptExcerpt(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //http://localhost:8084/DGDRESTTest/DGDLink?command=showTranscriptExcerpt&transcriptID=FOLK_E_00066_SE_01_T_01&=tokenIDs=w228
        String transcriptID = request.getParameter("transcriptID");
        String[] tokenIDs = (request.getParameter("tokenIDs") + " ").split(" ");
        
        //String cID = backendInterface.getAnnotationBlockID4TokenID(transcriptID + "_DF_01", tokenIDs[0]);
        String cID = backendInterface.getAnnotationBlockID4TokenID(transcriptID, tokenIDs[0]);
        
        //https://dgd.ids-mannheim.de/DGD2Web/ExternalAccessServlet?command=displayTranscript&id=FOLK_E_00001_SE_01_T_01_DF_01&cID=c73&wID=w172&textSize=200&contextSize=4
        String redirectURL = "https://dgd.ids-mannheim.de/DGD2Web/ExternalAccessServlet?command=displayTranscript"
                + "&id=" + transcriptID + "_DF_01"
                + "&cID=" + cID
                + "&wID=" + tokenIDs[0]
                + "&textSize=200&contextSize=4";
        response.sendRedirect(redirectURL);
        
        
    }
    
    
    private void showSpeakerMetadata(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //http://localhost:8084/DGDRESTTest/DGDLink?command=showSpeakerMetadata&speakerInitials=HAN3&transcriptID=FOLK_E_00066_SE_01_T_01
        String transcriptID = request.getParameter("transcriptID");
        String speakerInitials = request.getParameter("speakerInitials");
        Transcript transcript = backendInterface.getTranscript(transcriptID);
        String speakerID = transcript.getSpeakerIDBySpeakerInitials(speakerInitials);
        
        String redirectURL = "https://dgd.ids-mannheim.de/DGD2Web/ExternalAccessServlet?command=displayData&id=" + speakerID;
        response.sendRedirect(redirectURL);
    }
    
    
    private void showData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //http://localhost:8084/DGDRESTTest/DGDLink?command=showData&id=FOLK_E_00066_SE_01_T_01
        String id = request.getParameter("id");     
        String redirectURL = "https://dgd.ids-mannheim.de/DGD2Web/ExternalAccessServlet?command=displayData&id=" + id;
        response.sendRedirect(redirectURL);
    }

}
