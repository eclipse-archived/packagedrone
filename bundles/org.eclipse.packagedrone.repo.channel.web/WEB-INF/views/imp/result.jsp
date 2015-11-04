<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/job" prefix="job"%>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web"%>

<web:define name="entry">
<c:forEach var="entry" items="${items }">
    <tr>
        <td style="padding-left: ${level/2}em;">
           <a href="/artifact/${entry.id }/view">${entry.id }</a>
        </td>
        <td>${fn:escapeXml(entry.name) }</td>
        <td>${entry.size }</td>
    </tr>
    
    <web:call name="entry" items="${entry.children }" level="${level+1 }"/>
</c:forEach>
</web:define>

<h:main title="Import complete">

<div class="container-fluid">

	<div class="row">
	
	    <div class="col-md-6">
	        
	        <h3 class="details-heading">Overview</h3>
	        
	        <dl class="dl-horizontal details">
	            <dt>Artifacts imported</dt>
	            <dd>${result.entries.size() }</dd>
	            
	            <dt>Bytes imported</dt>
	            <dd>${result.totalBytes }</dd>
	            
	            <c:if test="${not empty channel }">
	            <dt>Channel</dt>
	            <dd><a href="/channel/${channel.id }/view">${pm:channel(channel) }</a></dd>
	            </c:if>
	        </dl>
	        
	    </div>
	    
	</div>
	
	<div class="row">
	
	   <div class="col-md-12">

            <h3 class="details-heading">Manifest</h3>
            
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Name</th>
                            <th>Bytes</th>
                        </tr>
                    </thead>
                    
                    <tbody>
	                   <web:call name="entry" items="${result.entries }" level="0"/>
                    </tbody>
                
                </table>
            </div>	   
	   </div>
	
	</div>


</div>

</h:main>