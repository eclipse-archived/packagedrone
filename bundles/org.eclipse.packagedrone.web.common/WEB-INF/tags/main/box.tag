<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@attribute name="title" required="true" %>
<%@attribute name="type" required="true" %>
<%@attribute name="icon" required="false" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="panel panel-${type} box">
	<div class="panel-heading"><h3 class="panel-title">
	<c:if test="${ not empty icon }"><span class="glyphicon glyphicon-${icon }"></span>&nbsp;</c:if>${fn:escapeXml (title) }</h3></div>
	<div class="panel-body"><jsp:doBody/></div>
</div>