<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<nav>
  <ul class="pager">
    <c:set var="previousPositionTarget" value="${pageContext.request.servletPath }"/>
    <c:if test="${ position-pageSize > 0 }">
        <c:set var="previousPositionTarget" value="${pageContext.request.servletPath }?position=${position-pageSize }"/>
    </c:if>
    <c:set var="nextPositionTarget" value="${pageContext.request.servletPath }?position=${position+pageSize }" />
    <li class="previous <c:if test="${not prev }">disabled</c:if>"><a href="${prev ? previousPositionTarget : '' }"><span aria-hidden="true">&larr;</span> Prev</a></li>
    <li class="next <c:if test="${not next }">disabled</c:if>"><a href="${next ? nextPositionTarget : '' }">Next <span aria-hidden="true">&rarr;</span></a></li>
  </ul>
</nav>