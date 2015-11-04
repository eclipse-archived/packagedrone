<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:main title="Channel not found" subtitle="${channelId }">

<h:buttonbar>
    <jsp:attribute name="before">
        <li><a class="btn btn-default" href="<c:url value="/channel"/>">All channels</a></li>
    </jsp:attribute>
</h:buttonbar>

<h:error title="Not found">Channel ${channelId } does not exists!</h:error>

</h:main>