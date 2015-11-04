<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="entry" required="true" type="org.eclipse.packagedrone.web.common.menu.Entry"%>
<%@attribute name="cssClass" required="false" type="java.lang.String" %>
<%@attribute name="role" required="false" type="java.lang.String" %>

<c:if test="${not empty entry }">
<c:set var="url" value="${entry.target.renderFull(pageContext)}" />

<a
    <c:if test="${not empty entry.modal }">${' ' } data-toggle="modal" data-target="#modal-${entry.id }" href="#"</c:if>
    <c:if test="${empty entry.modal }">${' ' } href="${url }"</c:if>
    <c:if test="${not empty role }">${' ' } role="${role }"</c:if>
    <c:if test="${not empty cssClass }">${' ' } class="${cssClass }"</c:if>
    <c:if test="${entry.newWindow }"> target="_blank"</c:if>
><h:menuEntry entry="${entry }"></h:menuEntry></a>

<c:if test="${not empty entry.modal }">
<web:push name="modal">
<div class="modal" id="modal-${entry.id }" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">${fn:escapeXml(entry.modal.title) }</h4>
      </div>
      <div class="modal-body">
        ${entry.modal.body }
      </div>
      <div class="modal-footer">
        <c:forEach var="button" items="${entry.modal.buttons }">
            <c:choose>
                <c:when test="${button.function eq 'CLOSE' }">
                    <button type="button" class="btn ${pm:modifier('btn-', button.modifier) }" data-dismiss="modal"><h:iconLabel label="${button.label }" icon="${button.icon }" /></button>
                </c:when>
                <c:when test="${button.function eq 'SUBMIT' }">
                    <a type="button" class="btn ${pm:modifier('btn-', button.modifier) }" href="${url }"><h:iconLabel label="${button.label }" icon="${button.icon }" /></a>
                </c:when>
            </c:choose>
        </c:forEach>
      </div>
    </div><%-- /.modal-content --%>
  </div><%-- /.modal-dialog --%>
</div><%-- /.modal --%>
</web:push>
</c:if>

</c:if>
