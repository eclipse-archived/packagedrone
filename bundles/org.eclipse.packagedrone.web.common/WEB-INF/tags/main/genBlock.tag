<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<div class="container-fluid">

<div class="row">

<div class="col-sm-3 col-lg-2">
<h:genMenu />
</div>

<%-- content area --%>

<div class="col-sm-9 col-lg-10">
    <div style="margin-left: 1em;">
        <jsp:doBody />
    </div> <%-- inner content --%>
</div> <%-- content area --%>

</div> <%-- row --%>

</div> <%-- global --%>