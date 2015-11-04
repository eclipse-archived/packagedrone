<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="command" required="false" type="java.lang.String"%>
<%@attribute name="path" required="false" type="java.lang.String"%>
<%@attribute name="label" required="false" type="java.lang.String" %>
<%@attribute name="optional" required="false" type="java.lang.Boolean" %>

<h:formGroup
    label="${label }" 
    path="${path }"
    additionalCssClass="${ optional ? ' optional' : '' } ${' ' } ${ (empty path) ?  '' : form:validationState ( pageContext, ( ( empty command ) ? 'command' : command ), path, '', 'has-error')}">

    <jsp:attribute name="body">
        <jsp:doBody/>
    </jsp:attribute>

    <jsp:attribute name="errors">
        <form:errorList path="${path }" cssClass="help-block" />
    </jsp:attribute>
</h:formGroup>