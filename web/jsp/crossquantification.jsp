<%-- 
    Document   : crossquantification
    Created on : 04.05.2020, 18:59:06
    Author     : Thomas.Schmidt
    Changed on : 09.08.2023 by Elena Frick
--%>
<%
    
    String metaField1 = request.getParameter("metaField1");
    if (metaField1==null){
        metaField1 = "v_e_se_interaktionsdomaene";
    }
    
    String metaField2 = request.getParameter("metaField2");
    if (metaField2==null){
        metaField2 = "v_s_geschlecht";
    }
    
    String units = request.getParameter("units");
    if (units==null){
        units = "TOKENS";
    }
    
    String backendParam = request.getParameter("backendID");
    if (backendParam==null){
        backendParam = "ZUMULT";
    }
    
    String corpusID = request.getParameter("corpusID");
    if (corpusID==null){
        corpusID = "FOLK";
    }
   
%>

<html>
   <head>
      <title>Page Redirection to ZuZweit</title>              
   </head>
   
   <body>
      <%
         response.setStatus(response.SC_MOVED_PERMANENTLY);
         response.setHeader("Location", new String("zuZweit.jsp?backendID="+backendParam+"&corpusID="+corpusID+"&metaField1="+metaField1+"&metaField2="+metaField2+"&units="+units));
      %>
   </body>
</html>