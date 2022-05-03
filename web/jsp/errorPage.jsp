<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <title>Error Page</title>
</head>
<body>
<h1>An error has occurred.</h1>

<div style="color: #F00;">
    Error message: <%= exception.toString() %> 
</div>
<div style="color: gray; font-size: smaller;">
    <% exception.printStackTrace(new java.io.PrintWriter(out)); %>
</div>
</body>
</html>