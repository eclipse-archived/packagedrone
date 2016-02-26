<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="command" required="false" type="java.lang.String"%>
<%@attribute name="path" required="false" type="java.lang.String"%>
<%@attribute name="label" required="false" type="java.lang.String" %>
<%@attribute name="optional" required="false" type="java.lang.Boolean" %>
<%@attribute name="id" required="false" type="java.lang.String" %>

<h:formGroup
    label="${label }" 
    path="${path }"
    id="${id }"
    additionalCssClass="${ optional ? ' optional' : '' } ${' ' } ${ (empty path) ?  '' : form:validationState ( pageContext, ( ( empty command ) ? 'command' : command ), path, '', 'has-error')}">

    <jsp:attribute name="body">
        <jsp:doBody/>
    </jsp:attribute>

    <jsp:attribute name="errors">
        <form:errorList path="${path }" cssClass="help-block" />
    </jsp:attribute>
</h:formGroup>