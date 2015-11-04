<%@ page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<%
response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
%>

<h:main title="Not found" subtitle="${fn:escapeXml(id) }">

<div class="container"><div class="row">

<div class="col-md-offset-2 col-md-8">

<div class="alert alert-danger" role="alert">
    <strong>Error!</strong> ${fn:escapeXml(web:toFirstUpper(type))}&nbsp;${fn:escapeXml(id) } could not be found.
</div>

Try to <a href="#" onclick="history.back(-1);">go back</a>.

</div>

</div></div>

</h:main>