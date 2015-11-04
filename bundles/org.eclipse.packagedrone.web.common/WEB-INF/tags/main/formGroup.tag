<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@attribute name="path" required="false" type="java.lang.String"%>
<%@attribute name="label" required="false" type="java.lang.String" %>
<%@attribute name="additionalCssClass" required="false" type="java.lang.String" %>

<%@attribute name="body" required="true" fragment="true" %>
<%@attribute name="errors" required="false" fragment="true" %>

<div class="form-group ${additionalCssClass }">
    <c:choose>
        <c:when test="${empty label }">
            <div class="col-sm-2"></div>
        </c:when>
        <c:otherwise>
            <form:label path="${path }" cssClass="col-sm-2 control-label">${fn:escapeXml(label) }</form:label>
        </c:otherwise>
    </c:choose>
    
    <div class="col-sm-10">
        <jsp:invoke fragment="body"/>
    </div>
    
    <div class="col-sm-10 col-sm-offset-2">
        <jsp:invoke fragment="errors"/>
    </div>
</div>