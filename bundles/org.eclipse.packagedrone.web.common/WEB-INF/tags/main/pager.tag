<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="value" type="org.eclipse.packagedrone.web.common.page.PaginationResult" %>

<c:if test="${ value.previous or value.next }">

<nav>
  <ul class="pager">
    <c:if test="${value.previous }">
        <c:url var="prevUrl" value="${pageContext.request.contextPath }">
            <c:param name="start" value="${value.previousPage }" />
        </c:url>
        <li class="previous"><a href="${prevUrl }"><span aria-hidden="true">&larr;</span> Prev</a></li>
    </c:if>
    
    <c:if test="${ not value.previous }">
        <li class="previous disabled"><a><span aria-hidden="true">&larr;</span> Prev</a></li>
    </c:if>
    
    <c:if test="${value.next }">
        <c:url var="nextUrl" value="${pageContext.request.contextPath }">
            <c:param name="start" value="${value.nextPage }" />
        </c:url>
        <li class="next"><a href="${nextUrl }">Next <span aria-hidden="true">&rarr;</span></a></li>
    </c:if>
    <c:if test="${not value.next }">
        <li class="next disabled"><a>Next <span aria-hidden="true">&rarr;</span></a></li>
    </c:if>
  </ul>
</nav>

</c:if>

