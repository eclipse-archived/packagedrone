<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<h:main title="System" subtitle="Information">

<div class="container-fluid">

    <div class="row">
    
    
        <div class="col-sm-6">
            <h3 class="details-heading">Storage</h3>
            
            <c:if test="${empty storageName }">
                <div class="well">Storage manager not configured.</div>
            </c:if>
            
            <c:if test="${not empty storageName }">
            
            <dl class="dl-horizontal details">
                <dt>Free</dt>
                <dd><web:bytes amount="${storageFree }"/></dd>
                <dt>Total</dt>
                <dd><web:bytes amount="${storageTotal }"/></dd>
                <dt>Partition</dt>
                <dd>${fn:escapeXml(storageName) }</dd>
            </dl>
            
            <div class="progress">
              <fmt:formatNumber var="percent" type="number" maxFractionDigits="1" minFractionDigits="1" value="${(storageUsed / storageTotal) * 100.0}" />
              <div class="progress-bar" role="progressbar" aria-valuenow="${percent }" aria-valuemin="0" aria-valuemax="100" style="min-width: 4em; width: ${percent}%;">
                 ${fn:escapeXml(percent) }%
              </div>
            </div>
            
            </c:if>
        </div>
        
        <div class="col-sm-6">
            <h3 class="details-heading">Memory</h3>
            
            <dl class="dl-horizontal details">
                <dt>Free</dt>
                <dd><web:bytes amount="${freeMemory }"/></dd>
                <dt>Total</dt>
                <dd><web:bytes amount="${totalMemory }"/></dd>
                <dt>Max</dt>
                <dd><web:bytes amount="${maxMemory }"/></dd>
            </dl>
            
            <div class="progress">
              <fmt:formatNumber var="percent" type="number" maxFractionDigits="1" minFractionDigits="1" value="${(usedMemory / totalMemory) * 100.0}" />
			  <div class="progress-bar" role="progressbar" aria-valuenow="${percent }" aria-valuemin="0" aria-valuemax="100" style="min-width: 4em; width: ${percent}%;">
			     ${fn:escapeXml(percent) }%
			  </div>
			</div>
			
			<div>
                <a class="btn btn-default" href="<c:url value="/system/info/gc"/>">Perform GC</a>
			</div>
        </div>
        
        <div class="col-sm-6">
            <h3 class="details-heading">CPU</h3>
            <dl class="dl-horizontal details">
                <dt>Processors</dt>
                <dd>${availableProcessors }</dd>
            </dl>
        </div>
    
        <div class="col-sm-6">
            <h3 class="details-heading">Java</h3>
            <dl class="dl-horizontal details">
                <c:forEach var="entry" items="${java }">
                    <dt>${fn:escapeXml(entry.key) }</dt>
                    <dd>${fn:escapeXml(entry.value) }</dd>
                </c:forEach>
            </dl>
        </div>
    
    </div>
    
</div>

</h:main>