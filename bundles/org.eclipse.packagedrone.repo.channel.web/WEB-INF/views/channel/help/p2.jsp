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
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<c:set var="idUrl" value="${ fn:escapeXml(sitePrefix.concat ( '/p2/' ).concat ( channel.id )) }"/>

<h:main title="Channel" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid form-padding">

<c:choose>
    <c:when test="${not p2Active }">
        <div class="well well-lg">This is not a P2 repository channel</div>
    </c:when>
    
    <c:otherwise>
        <div class="row">
            <div class="col-xs-12">
            
            <div class="panel panel-info">
                <div class="panel-heading"><h3 class="panel-title">Eclipse P2 repository</h3></div>
                <div class="panel-body">
		            This channel can be accessed using the Eclipse P2 repository manager system, also known as the <q>Eclipse Install new software dialog</q>. For more information see:
		            <ul>
		                <li><a href="https://eclipse.org/equinox/p2/" target="_blank">https://eclipse.org/equinox/p2/</a></li>
		                <li><a href="https://wiki.eclipse.org/Equinox_p2_Getting_Started" target="_blank">https://wiki.eclipse.org/Equinox_p2_Getting_Started</a></li>
		            </ul>
		            
		            <p>
		            The main URL for this repository is <code>${idUrl }</code>.
		            </p>
		            
		            <c:if test="${not empty channel.names }">
		            	<p>The channel has the following alias URLs:</p>
		            	<ul>
		            	<c:forEach var="name" items="${channel.names }">
		            		<li><code>${ fn:escapeXml(sitePrefix.concat ( '/p2/' ).concat ( web:encode(name) )) }</code></li>
		            	</c:forEach>
		            	</ul>
		            </c:if>
		            
                </div>
            </div>
            
                  </div>
        </div>
        
        <div class="row">
            <div class="col-sm-6">
                <div class="panel panel-default">
                    <div class="panel-heading"><h3 class="panel-title">Maven Tycho</h3></div>
                    <div class="panel-body">
                    Add the following repository configuration to your setup:
                    </div>
                    
<div class="">
                    <pre>…
&lt;repositories&gt;
    …
    &lt;repository&gt;
        &lt;id&gt;package.drone.runtime&lt;/id&gt;
<c:if test="${not empty channel.name }">        &lt;name&gt;${fn:escapeXml(channel.name) }&lt;/name&gt;</c:if>
        &lt;layout&gt;p2&lt;/layout&gt;
        &lt;url&gt;${idUrl }&lt;/url&gt;
    &lt;/repository&gt;
    …
&lt;/repositories&gt;
…</pre>
                    
                    </div>
                    
                    <div class="panel-footer">
                        For more information about the Maven Tycho see
                        <a href="https://eclipse.org/tycho/" target="_blank">https://eclipse.org/tycho/</a> and
                        <a href="https://wiki.eclipse.org/Tycho/Reference_Card#Repository_providing_the_context_of_the_build" target="_blank">Repository providing the context of the build</a> in the wiki.
                    </div>
                    
                </div>
            </div>
            
            <div class="col-sm-6">
                <div class="panel panel-default">
                    <div class="panel-heading"><h3 class="panel-title">Eclipse Target Platform DSL</h3></div>
                    <div class="panel-body">
                    
                    Create an new target platform DSL file
                    
                    </div>
                    
<div>
                    <pre>
target "My target platform"

with source, allEnvironments

location "${idUrl }" {
    /* your installable units here */
}</pre>
                    
                                        
                    </div>
                    
                    <div class="panel-footer">
                        For more information about the target platform DSL editor see
                        <a href="https://github.com/mbarbero/fr.obeo.releng.targetplatform" target="_blank">https://github.com/mbarbero/fr.obeo.releng.targetplatform</a>.
                    </div>
                </div>
            </div>
      
        </div>
    </c:otherwise>
</c:choose>

</div>

</h:main>