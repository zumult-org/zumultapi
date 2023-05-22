<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"     
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:import href="isotei2html_table.xsl"/>
    <xsl:template match="/">
        <html>
            <head>
                <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
                <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js" integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
                <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
                <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
                <style type="text/css">
                    body { padding-top: 70px; }
                    td {
                        vertical-align: top;
                    }
                    td.speaker {
                        font-weight: bold;
                    }
                    td.cont {
                        color: rgb(200,200,200);
                    }
                    .pause, .desc {
                        color: rgb(100,100,100);
                    }
                    tr.selectionStart {
                        border-top: thick solid gray;
                        border-left: thick solid gray;
                        border-right: thick solid gray;
                    }
                    tr.selectionEnd {
                        border-bottom: thick solid gray;
                        border-left: thick solid gray;
                        border-right: thick solid gray;
                    }
                    table.transcript {
                        /*font-family: Consolas, Courier New, mono-spaced;*/
                    }
                    td.color-1{color : navy;}
                    td.color-2{color : maroon;}
                    td.color-3{color : green;}
                    td.color-4{color : teal;}
                    td.color-5{color : purple;}
                    td.color-6{color : orange;}
                    td.color-7{color : olive;}
                    
                    
                </style>
                <script>
                    var startSelection = "";
                    var endSelection = "";
                    
                    function setStartSelection(element){
                        var id = element.closest("tr").dataset.annotationBlockId;   
                        startSelection = id;                        
                        $("tr").removeClass("selectionStart");
                        $("#tr" + id).addClass("selectionStart");
                        if ($("tr").index($("#tr" + startSelection)) &gt; $("tr").index($("#tr" + endSelection))){
                            $("#tr" + endSelection).removeClass("selectionEnd");
                            endSelection="";
                        }
                        if (endSelection===""){
                            $("#tr" + id).addClass("selectionEnd");
                        }
                    }
                    
                    function setEndSelection(element){
                        if (startSelection===""){
                            alert("Bitte erst eine Startauswahl vornehmen");
                            return;
                        }
                        var id = element.closest("tr").dataset.annotationBlockId;
                        endSelection = id;                        
                        $("tr").removeClass("selectionEnd");
                        $("#tr" + id).addClass("selectionEnd");
                        if ($("tr").index($("#tr" + startSelection)) &gt;= $("tr").index($("#tr" + endSelection))){
                            $("tr").removeClass("selectionStart");
                            $("#tr" + id).addClass("selectionStart");  
                            startSelection = id;
                        }
                    }
                </script>
            </head>
            <body>
                <nav class="navbar navbar-expand-lg navbar-light bg-light fixed-top">
                    <a class="navbar-brand" href="http://agd.ids-mannheim.de/folk.shtml">
                        <img src="C:\Users\thomas.schmidt\Desktop\folk1.png" height="30" class="d-inline-block align-top" alt=""/>
                    </a>
                    <div class="collapse navbar-collapse" id="navbarSupportedContent">
                        <ul class="navbar-nav mr-auto">
                            <li class="nav-item active">
                                <a class="nav-link" href="#">Home <span class="sr-only">(current)</span></a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#myModal" data-toggle="modal">Metadaten</a>
                            </li>
                            <li class="nav-item dropdown">
                                <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                    Optionen
                                </a>
                                <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                                    <a class="dropdown-item" href="#">Action</a>
                                    <a class="dropdown-item" href="#">Another action</a>
                                    <div class="dropdown-divider"></div>
                                    <a class="dropdown-item" href="#">Something else here</a>
                                </div>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link disabled" href="#" tabindex="-1" aria-disabled="true">Disabled</a>
                            </li>
                        </ul>
                    </div>
                </nav>                
                
                <div class="row">
                    <div class="col-sm-1"></div>
                    <div class="col-sm-6">
                        <div class="container">
                            <xsl:apply-templates select="//tei:text"/>
                        </div>
                    </div>
                    <div class="col-sm-5">
                        <div style="position: fixed;">
                            <video src="D:\Dropbox\IDS\VIDEO\cut3.mp4" controls="controls">
                                Sorry, your browser doesn't support embedded videos, 
                                but don't worry, you can <a href="videofile.ogg">download it</a>
                                and watch it with your favorite video player!
                            </video>     
                            <div>
                                
                            </div>
                            <video src="D:\Dropbox\IDS\VIDEO\cut3.mp4" controls="controls">
                                Sorry, your browser doesn't support embedded videos, 
                                but don't worry, you can <a href="videofile.ogg">download it</a>
                                and watch it with your favorite video player!
                            </video>           
                        </div>
                    </div>
                </div>
                
                <div class="modal" tabindex="-1" role="dialog" id="myModal">
                    <div class="modal-dialog" role="document">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">Metadaten</h5>
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                    <span aria-hidden="true">x</span>
                                </button>
                            </div>
                            <div class="modal-body">
                                <p>Irgendein Gespräch ist das.</p>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                                <button type="button" class="btn btn-primary">Save changes</button>
                            </div>
                        </div>
                    </div>
                </div>                
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>