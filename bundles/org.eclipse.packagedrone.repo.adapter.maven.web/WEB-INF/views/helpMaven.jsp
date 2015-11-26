<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web"%>
<%@ taglib uri="http://eclipse.org/packagedrone/security" prefix="sec" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${pm:channel(channel) }">

    <h:buttonbar menu="${menuManager.getActions(channel) }"/>
    <h:nav menu="${menuManager.getViews(channel) }"/>
    
    <div class="container-fluid form-padding">
    
        <div class="row">
            <div class="col-sm-6">
                <h3>Inbound</h3>
                
                <p>Pushing artifacts to this channel using Maven.</p>
                
                <c:choose>
                    <c:when test="${empty deployGroups }">
                        <div class="alert alert-info">
                            <strong>Not configured!</strong> Channel is not configured for receiving artifacts.
                            <c:if test="${manager}">
                                <a href="<c:url value="/channel/${channel.id }/deployKeys"/>" class="alert-link">Add some deploy groups and keys</a> to enable this.
                            </c:if>
                        </div>
                    </c:when>
                    
                    <c:otherwise>

<h4>In a project</h4>
<div class="panel panel-default">
    <div class="panel-heading">pom.xml</div>
    <div class="">
        <pre>…
&lt;distributionManagement&gt;
    &lt;repository&gt;
        &lt;id&gt;pdrone.${channel.id }&lt;/id&gt;
        &lt;url&gt;${sitePrefix }/maven/${channel.id }&lt;/url&gt;
    &lt;/repository&gt;
&lt;/distributionManagement&gt;
…</pre>
    </div>
</div>

<h4>From the command line</h4>
<div>
    <pre>mvn deploy -DaltDeploymentRepository=pdrone.${channel.id }::default::${sitePrefix }/maven/${channel.id }</pre>
</div>

<h4>Authentication</h4>
<p>
Package Drone requires a Maven to authenticate to Package Drone before uploading artifacts.
For this there are special <q>deploy keys</q>, in order to not use actual user credentials.
The user name always is <code>deploy</code> while the password is the actual deploy key to use.
Deploy keys are assigned to groups and groups get assigned to channels, for easier management of keys.
</p>

<div class="panel panel-default">
    <div class="panel-heading">settings.xml</div>
    <div class="">
        <pre>…
&lt;server&gt;
    &lt;id&gt;pdrone.6873B646-A92B-490F-ABB1-53685427466E&lt;/id&gt;
    &lt;username&gt;deploy&lt;/username&gt;
    &lt;password&gt;<span title="Replace with actual deploy key" style="cursor: help; color: red; background-color: yellow;">deploy-key</span>&lt;/password&gt;
&lt;/server&gt;
…</pre>
    </div>
</div>

<c:if test="${manager }">

	<p>
	The following deploy keys are configured for this channel:
	</p>
	<ul>
	<c:forEach var="dg" items="${deployGroups }">
	    <c:forEach var="dk" items="${dg.keys }">
	        <li><code>${fn:escapeXml(dk.key) }</code></li>
	    </c:forEach>
	</c:forEach>
	</ul>

</c:if>
<c:if test="${not manager }">
    <p>The deploy keys are only visible to users with the role <code>MANAGER</code>.</p>
</c:if>
                    
                    </c:otherwise>
                    
                </c:choose>
                
            </div>
            
            <div class="col-sm-6">
                <h3>Outbound</h3>
                
                <p>Consuming artifacts using Maven.</p>
                
                <c:choose>
                    <c:when test="${not mavenRepo }">
                        <div class="alert alert-info">
                            <strong>Not configured!</strong> Channel is not configured for providing a Maven repository.
                            <c:if test="${manager}">
                                <a href="<c:url value="/channel/${channel.id }/aspects"/>" class="alert-link">Add the Maven Repository aspect</a> to enable this.
                            </c:if>
                        </div>
                    </c:when>
                    <c:otherwise>

<h4>In a project</h4>
<div class="panel panel-default">
    <div class="panel-heading">pom.xml <small>by ID</small></div>
    <div class="">
        <pre>…
&lt;repositories&gt;
    &lt;repository&gt;
        &lt;id&gt;pdrone.${channel.id }&lt;/id&gt;
        &lt;url&gt;${sitePrefix }/maven/${channel.id }&lt;/url&gt;
    &lt;/repository&gt;
&lt;/repositories&gt;
…</pre>
    </div>
</div>

<c:if test="${not empty channel.name }">

<div class="panel panel-default">
    <div class="panel-heading">pom.xml <small>by Name</small></div>
    <div class="">
        <pre>…
&lt;repositories&gt;
    &lt;repository&gt;
        &lt;id&gt;pdrone.${fn:escapeXml(channel.name) }&lt;/id&gt;
        &lt;url&gt;${sitePrefix }/maven/${web:encode(channel.name)}&lt;/url&gt;
    &lt;/repository&gt;
&lt;/repositories&gt;
…</pre>
    </div>
</div>

</c:if>

                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    
    </div>

</h:main>