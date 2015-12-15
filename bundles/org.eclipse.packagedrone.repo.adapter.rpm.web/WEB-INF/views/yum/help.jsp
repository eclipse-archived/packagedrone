<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<%
pageContext.setAttribute ( "NL", "\n" );
%>

<h:main title="YUM Repository Help" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-6">
            <h3>Using <code>yum-repository-manager</code></h3>
            
            Using the command line tool <code>yum-repository-manager</code>:
            
            <h4>By Channel ID</h4>
            <pre>yum-config-manager --add-repo ${fn:escapeXml(sitePrefix) }/yum/${fn:escapeXml(channel.id) }/config.repo</pre>
            
            <c:if test="${not empty channel.name }">
            <h4>By Channel Name</h4>
            <pre>yum-config-manager --add-repo ${fn:escapeXml(sitePrefix) }/yum/${fn:escapeXml(channel.name) }/config.repo</pre>
            </c:if>
        </div>
        
        <div class="col-md-6">
            <h3>Manually</h3>
            
            <p>
            Create the following file:
            </p>
            
			<div class="panel panel-default">
			    <div class="panel-heading">/etc/yum/repo.repos.d/${ fn:escapeXml(channel.id ) }.repo</div>
			    <div class="">
			        <pre>[${fn:escapeXml(channel.id) }]
<c:if test="${not empty channel.description }">name=${fn:escapeXml(fn:contains(channel.description,NL) ? fn:substringBefore(channel.description,NL) : channel.description) }${NL }
</c:if>baseurl=${fn:escapeXml(sitePrefix) }/yum/${fn:escapeXml(channel.id) }
enabled=1
gpgcheck=0</pre>
			    </div>
			</div>
			
			<p>
			Or download the file by issuing the following command:
			</p>
            
            <pre>wget "${fn:escapeXml(sitePrefix) }/yum/${fn:escapeXml(channel.id) }/config.repo" -O "/etc/yum.repos.d/${fn:escapeXml(channel.id) }.repo"</pre>
            
        </div>
        
    </div>
</div>

</h:main>

