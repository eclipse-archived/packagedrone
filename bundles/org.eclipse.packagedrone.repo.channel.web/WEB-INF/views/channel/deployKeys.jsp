
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ page import="java.util.Collections"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="org.eclipse.packagedrone.repo.channel.deploy.DeployGroup"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>


<web:define name="named">
<c:choose>
   <c:when test="${not empty named.name }">${fn:escapeXml(named.name) }</c:when>
   <c:otherwise>${fn:escapeXml(named.id) }</c:otherwise>
</c:choose>
</web:define>

<h:main title="Channel" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid form-padding">
    
	<div class="row">
	   <div class="col-md-8">
	   
	       <c:choose>
	       
	           <c:when test="${empty channelDeployGroups}">
	               <div class="well well-lg">
	                   <p>There are no deploy keys assigned to this channel. It will not be possible to deploy to this channel using <code>mvn deploy</code>.
	                   Select deploy groups on the right side and assign them to this channel.
	               </div>
	           </c:when>

	           <c:otherwise>
			       <h3>Deploy Groups</h3>
			       
			       <table class="table table-condensed table-hover">
			       
			           <thead>
			           </thead>
			           
			           <tbody>
				           <c:forEach var="dg" items="${channelDeployGroups }">
				              <tr>
			                  <td><a href="<c:url value="/deploy/auth/group/${dg.id}/view"/>"><web:call name="named" named="${dg }"/></a></td>
			                  <td>
			                    <ul>
			                          <c:forEach var="dk" items="${dg.keys}">
			                            <li>
			                               <web:call name="named" named="${dk }"/>
			                               (<a
			                                   href="#"
			                                   role="button"
			                                   data-toggle="modal"
			                                   data-target="#settings-modal"
			                                   data-token="${dk.key}"
			                                   data-channel="${channel.id }"
			                                   ><small>View Maven settings</small></a>)
			                               <br/>
			                               <code title="username">deploy</code> / <code title="password">${fn:escapeXml(dk.key) }</code> 
			                            </li>
			                        </c:forEach>
			                    </ul>
			                    </td>
			                    
			                    <td>
			                    <form action="removeDeployGroup" method="post">
			                       <input type="hidden" name="groupId" value="${fn:escapeXml(dg.id) }"/>
			                       <button title="Unassign deploy group" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span></button>
			                    </form>
			                    </td>
			                   </tr>
			               </c:forEach>       
			           </tbody>
			       </table>
	           </c:otherwise>
           </c:choose>
		</div>
		
		<div class="col-md-4">
		
            <div class="panel panel-default">
            
                <div class="panel-heading"><h3 class="panel-title">Add Deploy Group</h3></div>
                <div class="panel-body">
                
                    <c:choose>
                
                        <c:when test="${empty deployGroups and empty channelDeployGroups}">
	                        <div class="alert alert-warning">
	                            <strong>No deploy groups!</strong> You did not set up any deploy group.
	                            
	                            <a href="<c:url value="/deploy/auth/addGroup"/>" class="alert-link">Create a deploy group now</a>. 
	                        </div>
                        </c:when>
                        
                        <c:otherwise>
			                <form class="form-horizontal" method="post" action="addDeployGroup">
			    
						        <div class="form-group">
						            <label class="col-sm-2 control-label" for="groupId">Group</label>
						            <div class="col-sm-10">
							            <select name="groupId" class="form-control" id="groupId">
							                <c:forEach var="dg" items="${deployGroups }">
							                    <option value="${dg.id }">
							                       <web:call name="named" named="${dg }"/>
							                    </option>
							                </c:forEach>    
							            </select>
						            </div>
						        </div>
						        
						        <div class="form-group">
								    <div class="col-sm-offset-2 col-sm-10">
								      <button type="submit" class="btn btn-primary" ${ ( empty deployGroups ) ? 'disabled="disabled"': '' } >Add</button>
								    </div>
								  </div>
						    </form>
					    </c:otherwise>
				    
                    </c:choose>
				    
                </div>
		  </div>
		  
		</div>
	</div>

</div>

<div class="modal" tabindex="-1" id="settings-modal" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Deploy settings</h4>
      </div>
      <div class="modal-body">
      
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">pom.xml</h4>
            </div>
            <div>
        <pre>…
&lt;distributionManagement&gt;
    &lt;repository&gt;
        &lt;id&gt;pdrone.<span class="data-channel"></span>&lt;/id&gt;
        &lt;url&gt;${fn:escapeXml(sitePrefix) }/maven/<span class="data-channel"></span>&lt;/url&gt;
    &lt;/repository&gt;
&lt;/distributionManagement&gt;
…</pre>
            </div>
        </div>
        
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">settings.xml</h4>
            </div>
            <div>
        <pre>…
&lt;server&gt;
    &lt;id&gt;pdrone.<span class="data-channel"></span>&lt;/id&gt;
    &lt;username&gt;deploy&lt;/username&gt;
    &lt;password&gt;<span class="data-token"></span>&lt;/password&gt;
&lt;/server&gt;
…</pre>
            </div>
        </div>
        
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
      </div>
    </div><%-- /.modal-content --%>
  </div><%-- /.modal-dialog --%>
</div><%-- /.modal --%>

<script type="text/javascript">
$('#settings-modal').on('show.bs.modal', function (event) {
	  var button = $(event.relatedTarget);

	  var modal = $(this)
	  modal.find('.data-token').text(button.data('token'));
	  modal.find('.data-channel').text(button.data('channel'));
	});
</script>

</h:main>
