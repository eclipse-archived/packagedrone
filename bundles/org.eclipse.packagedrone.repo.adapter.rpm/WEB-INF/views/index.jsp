<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>

<head>
    <title>YUM repository adapter</title>
    
    
        <style type="text/css">
body {
    font-family: sans-serif;
}
header {
    border-bottom: 1pt solid black;
    padding-bottom: 5pt;
    padding-left: 10pt;
    padding-right: 10pt;
}
footer {
    font-size: small;
    border-top: 1pt solid black;
    padding-top: 5pt;
    padding-left: 10pt;
    padding-right: 10pt;
}
    </style>
    
</head>

<body>
    <header>
        <h1>YUM repository adapter</h1>    
        <c:if test="${ not empty channel.description}">${fn:escapeXml(channel.description) }</c:if>
    </header>
    
    <section>
    
        <p>
            This is the YUM repository adapter of Package Drone.
        </p>
    
    </section>

<footer>
    <a href="http://packagedrone.org" target="_blank">Package Drone</a> ${version}
</footer>
    
</body>


</html>