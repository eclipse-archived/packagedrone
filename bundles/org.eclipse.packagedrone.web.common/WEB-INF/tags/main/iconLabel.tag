<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel/web" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="label" required="false" type="java.lang.String"%>
<%@attribute name="icon" required="false" type="java.lang.String"%>

<c:if test="${not empty icon }"><span class="glyphicon glyphicon-${fn:escapeXml(icon) }"></span>${ ' ' }</c:if>${fn:escapeXml(label)}
