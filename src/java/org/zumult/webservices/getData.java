/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.zumult.backend.Configuration;

/**
 *
 * @author josip.batinic
 */
public class getData extends HttpServlet {

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
//            out.println("<title>Servlet getData</title>");            
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet getData at " + request.getContextPath() + "</h1>");
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
//        try {
            String filePathString = request.getParameter("path");
            
            // new 25-11-2020, for issue #22
            if (!(Configuration.getFreeData().contains(filePathString))){
                returnNotAllowed(request, response);
                return;
            }    
            String DATA_PATH = "/data/";
            
            ServletContext context = getServletContext();
            // gets real path, then the base 
            String realPathString = context.getRealPath("").replace("\\", "/").replace("build/web/", "");
            String pathToResourceString = DATA_PATH + filePathString;
            InputStream in = getClass().getResourceAsStream(pathToResourceString); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(in)); 
            String line;
            String content = "";
            while ((line = reader.readLine()) != null) {
                content+=line;
            }
            reader.close();
            in.close();            

            response.setContentType("text/plain; charset=UTF-8");
            // this one might be more correct
            //response.setContentType("application/json; charset=UTF-8");            
            // think we don't need that any longer
            //response.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
            response.getWriter().print(content);
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
        return "Custom Servlet for serving certain pieces of data to an application";
    }// </editor-fold>

    private void returnNotAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filePathString = request.getParameter("path");
        String message = "You are not allowed to retrieve " + filePathString + " in this way. Go away.";
        
        //Response.status(Status.UNAUTHORIZED).entity(message).build();
        response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
    }

}
