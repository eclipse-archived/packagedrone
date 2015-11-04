<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    
<!DOCTYPE html>
<html>

<head>
    <title>P2 repository | ${fn:escapeXml(p2Title) }</title>
</head>

<body>
    <header>
        <h1>${fn:escapeXml(p2Title) }</h1>    
        <h2>Channel: ${fn:escapeXml( ( empty name) ? id : name) }<c:if test="${not empty name }"> <small>(${fn:escapeXml(id) })</small></c:if></h2>
        <c:if test="${ not empty description}">${fn:escapeXml(description) }</c:if>
    </header>
    
    <section>
    
        <ul>
            <li><a href="content.xml">content.xml</a> (<a href="content.jar">content.jar</a>)</li>
            <li><a href="artifacts.xml">artifacts.xml</a> (<a href="artifacts.jar">artifacts.jar</a>)</li>
            <li><a href="p2.index">p2.index</a></li>
            <li><a href="repo.zip">repo.zip</a></li>
        </ul>
    
    </section>
    
</body>


</html>