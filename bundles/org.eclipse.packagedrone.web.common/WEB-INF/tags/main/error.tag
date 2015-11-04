<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@attribute name="title" required="true" %>
<%@attribute name="icon" required="false" %>

<h:box title="${title }" type="danger" icon="${icon }"><jsp:doBody/></h:box>