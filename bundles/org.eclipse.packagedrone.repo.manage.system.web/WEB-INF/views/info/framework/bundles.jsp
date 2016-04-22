<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<h:main title="System" subtitle="OSGi Bundles">

<web:define name="state">
<c:choose>
    <c:when test="${bundle.state == 1 }">UNINSTALLED</c:when>
    <c:when test="${bundle.state == 2 }">INSTALLED</c:when>
    <c:when test="${bundle.state == 4 }">RESOLVED</c:when>
    <c:when test="${bundle.state == 8 }">STARTING</c:when>
    <c:when test="${bundle.state == 16 }">STOPPING</c:when>
    <c:when test="${bundle.state == 32 }">ACTIVE</c:when>
    <c:otherwise>unknown</c:otherwise>
</c:choose>
</web:define>

<div class="container-fluid">

<div class="table-responsive">

<table class="table table-condensed table-hover">
    <thead>
        <tr>
            <th>Symbolic Name</th>
            <th>Name</th>
            <th>Version</th>
            <th>State</th>
            <th>Legal</th>
            <th>ID</th>
            <th></th>
        </tr>
    </thead>
    
    <tbody>
        <c:forEach var="bundle" items="${bundles }">
            <tr class="${bundle.state == 32 ? 'success' : '' } ">
                <td>
                    ${fn:escapeXml(bundle.symbolicName) }
                    <c:if test="${bundle.fragment }">
                        <span class="label label-default">Fragment</span>
                    </c:if>
                </td>
                <td>${fn:escapeXml(bundle.name) }</td>
                <td>${fn:escapeXml(bundle.version) }</td>
                <td><web:call name="state" bundle="${bundle }"/></td>
                <td>
                  <ul class="link-list">
                    <c:forEach items="${bundle.licenses }" var="license">
                      <li>
                      <c:choose>
                        <c:when test="${not empty license.url }"><a href="${license.url }" target="_blank">${fn:escapeXml(license.license) }</a></c:when>
                        <c:otherwise>${fn:escapeXml(license.license) }</c:otherwise>
                      </c:choose>
                      </li>
                    </c:forEach>
                    <c:if test="${not empty bundle.aboutHtml }">
                      <li><a href="#" data-about="<c:url value="/system/info/framework/bundles/${bundle.bundleId }/about.html"/>" data-toggle="modal" data-target="#aboutModal">about.html</a></li>
                    </c:if>
                    <c:if test="${not empty bundle.licenseTxt }">
                      <li><a href="#" data-about="<c:url value="/system/info/framework/bundles/${bundle.bundleId }/LICENSE.txt"/>" data-toggle="modal" data-target="#aboutModal">LICENSE.txt</a></li>
                    </c:if>
                    <c:if test="${not empty bundle.noticeTxt }">
                      <li><a href="#" data-about="<c:url value="/system/info/framework/bundles/${bundle.bundleId }/NOTICE.txt"/>" data-toggle="modal" data-target="#aboutModal">NOTICE.txt</a></li>
                    </c:if>
                  </ul>
                </td>
                <td>${fn:escapeXml(bundle.bundleId) }</td>
                <td>
                    <c:if test="${!bundle.fragment }">
	                    <c:if test="${bundle.state == 32 }">
	                    <form action="<c:url value="/system/info/framework/bundles/${bundle.bundleId }/stop"/>" method="post" id="stop_${bundle.bundleId }"></form><a href="#" onclick="$('#stop_${bundle.bundleId}').submit(); return false;">Stop</a>
	                    </c:if>
	                    <c:if test="${bundle.state < 32 }">
	                    <form action="<c:url value="/system/info/framework/bundles/${bundle.bundleId }/start"/>" method="post" id="start_${bundle.bundleId }"></form><a href="#" onclick="$('#start_${bundle.bundleId}').submit(); return false;">Start</a>
	                    </c:if>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
    
</table>
    
</div>

</div>

<%-- about modal --%>

<div class="modal" id="aboutModal" tabindex="-1" role="dialog" aria-labelledby="aboutModalLabel">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="aboutModalLabel">About Bundle</h4>
      </div>
      <div class="modal-body" style="height: 60vh;">
        <iframe id="aboutFrame" src="" width="100%" height="100%" title="About Text"></iframe>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

<script>
$('#aboutModal').on('show.bs.modal', function(event) {
  var source = $(event.relatedTarget);
  var about = source.data('about');
  
  var frame = $('#aboutFrame');
  frame.attr("src",about);
  $('#aboutModalLabel').text("Bundle: " + source.text());
});
$('#aboutModal').on('hide.bs.modal', function(event) {
  var frame = $('#aboutFrame');
  frame.attr("src","");
});
</script>


</h:main>