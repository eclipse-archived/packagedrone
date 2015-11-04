<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Usage statistics" subtitle="Information">

<div class="container">
    <div class="row">
    
        <div class="col-md-6">
            <h3>Information</h3>
            <dl class="dl-horizontal">
                <dt>State</dt>
                <dd>${enabled ? 'enabled – Thank you!' : 'disabled' }</dd>
                
                <dt>Last transmission</dt>
                <dd>${fn:escapeXml(lastPingTimestamp) }</dd>
            </dl>
            
            <a href="#" data-toggle="modal" data-target="#info-modal">More information…</a>
        </div>
    
        <div class="col-md-6">
	        <h3>Transmitted information</h3>
	        <pre>${fn:escapeXml(data) }</pre>
        </div>
    </div>
</div>

<div id="info-modal" class="modal" tabindex="-1" role="dialog">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Usage transmission <span class="glyphicon glyphicon-question-sign"></span></h4>
      </div>
      <div class="modal-body">
      
        <h3>What is done?</h3>
        
        <p>
        Package Drone periodically reports aggregated, anonymous usage statistics to the project's web site.
        This is an important information for the developers to understand how people use this piece of software.
        </p>
        
        <h3>What is sent?</h3>
        
        <p>
        A few aggregated statistics and the version used are transmitted back. The full data structure can be viewed
        on body of this page. This information is sent once every 24 hours.
        </p>
        
        <p>
        In order to identify duplicates, a new random ID is generated every 7 days. This allows to aggregate the information
        in the server side to somehow identify duplicate transmissions. The random id stored, but regenerated every 7 days. 
        </p>
        
        <h3>How can I deactivate it?</h3>
        
        <p>
        So you are using Package Drone for free and don't want to help the project to understand how you are using it?
        Ok, that's fine with us. So you can set the Java system property <code>drone.usage.disable</code> to <code>true</code>
        at any time. Even without re-starting the system, it will no longer transmit this information.
        </p>
        
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-info" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

</h:main>