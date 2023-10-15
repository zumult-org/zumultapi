<%-- 
    Document   : browse
    Created on : 15.10.2023, 12:17:37
    Author     : bernd
--%>

<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="css/query.css">
        
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <script src="js/tgdp.js"></script>
        <title>Texas German Dialect Project - ZuMult Browse</title>
    </head>
    <body>
        <div class="row" id="body-row">
            <!-- Sidebar -->
            <div id="sidebar-container" class="sidebar-expanded d-none d-md-block">
                <!-- d-* hiddens the Sidebar in smaller devices. Its itens can be kept on the Navbar 'Menu' -->
                <!-- Bootstrap List Group -->
                <ul class="list-group">
                    <!-- Separator with title -->
                    <li class="list-group-item sidebar-separator-title text-muted d-flex align-items-center menu-collapsed">
                        <small>INTERVIEWS</small>
                    </li>
                    
                    <% 
                        BackendInterface backend = BackendInterfaceFactory.newBackendInterface(); 
                        IDList interviewIDs = backend.getSpeechEvents4Corpus("TGDP");
                        for (String interviewID : interviewIDs){ %>
                            <a href="#submenu_<%= interviewID %>" data-toggle="collapse" aria-expanded="false" class="bg list-group-item list-group-item-action flex-column align-items-start">
                                <div class="d-flex w-100 justify-content-start align-items-center">
                                    <span class="menu-collapsed">
                                        <img src="img/microphone-stand-duotone.png" style="margin-right:10px">
                                        <%= interviewID.substring(5) %>
                                    </span>
                                    <span class="submenu-icon ml-auto"></span>
                                </div>
                            </a>
                            <% 
                                IDList transcriptIDs = backend.getTranscripts4SpeechEvent(interviewID);
                            %>
                            <!-- Submenu content -->
                            <div id='submenu_<%= interviewID %>' class="collapse sidebar-submenu">
                                <% for (String transcriptID : transcriptIDs) { %>
                                <a href="#" class="list-group-item list-group-item-action bg-dark text-white">
                                    <span class="menu-collapsed"><%= transcriptID %></span>
                                </a>
                                <% } %>   
                            </div>
                          <% } %>
                </ul>
            </div>
            <!-- MAIN -->
            <div class="col p-4">
                <h1 class="display-4">Collapsing Sidebar Menu</h1>
                <div class="card">
                    <h5 class="card-header font-weight-light">Requirements</h5>
                    <div class="card-body">
                        <ul>
                            <li>JQuery</li>
                            <li>Bootstrap 4.3</li>
                            <li>FontAwesome</li>
                        </ul>
                    </div>
                </div>
            </div><!-- Main Col END -->
        </div><!-- body-row END -->
    </body>
</html>
