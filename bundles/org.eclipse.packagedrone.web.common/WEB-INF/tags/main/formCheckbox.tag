<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<%@attribute name="command" required="true" %>
<%@attribute name="path" required="true" %>
<%@attribute name="label" required="true" %>

<div class='form-group ${form:validationState(pageContext,command,path, "", "has-error")}'>

    <div class="col-sm-offset-2 col-sm-10">
        <div class="checkbox">
            <form:label path="${path }"><form:checkbox path="${path }" />${fn:escapeXml(label) }</form:label>
        </div>
        <jsp:doBody/>
    </div>
    
    <div class="col-sm-10 col-sm-offset-2">
        <form:errorList path="${path }" cssClass="help-block" />
    </div>
</div>