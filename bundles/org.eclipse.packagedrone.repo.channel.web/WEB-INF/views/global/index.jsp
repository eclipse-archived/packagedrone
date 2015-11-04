<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    %>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<h:main title="Storage operations">

<div class="container-fluid">
    <div class="row">
    
        <div class="col-sm-6">
            <div class="panel panel-default">
                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-import"></span> Import channels</h3></div>
                <div class="panel-body">
                    <p>
                    Import all channels from a full export or from a channel export.
                    </p>
                    <p>
                    Depending on the import operation you have to provide the correct export file type. A full import requires a full export file and
                    a single channel import requires a single channel export file. At the moment it is not possible to do partial imports. 
                    </p>
                </div>
                <div class="panel-body text-right">
                    <a href="<c:url value="/channel/import"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-import"></span> Import Channel</a>
                    <a href="<c:url value="/channel/importAll"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-import"></span> Import Full</a>
                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-export"></span> Export all channels</h3></div>
                <div class="panel-body">
                    <p>
                    Export all channels at once. It is possible to download the archive immediately or spool it out to the file system of the server. 
                    </p>
                    <p>
                    In order to export a single channel only, go to that channel and use the <q>Export</q> action.
                    </p>
                </div>
                <div class="panel-body text-right">
                    <a href="<c:url value="/channel/export"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-export"></span> Download</a>
                    <a href="<c:url value="/system/storage/exportAllFs"/>" role="button" class="btn btn-default"><span class="glyphicon glyphicon-export"></span> Spool Out</a>
                </div>
            </div>
        </div>
        
        <div class="col-sm-6">
            <div class="panel panel-danger">
                <div class="panel-heading"><h3 class="panel-title"><span class="glyphicon glyphicon-trash"></span> Wipe storage</h3></div>
                <div class="panel-body">
                    This options allows you to wipe out the whole storage. All channels and artifacts
                    will be deleted.
                </div>
                <div class="panel-body text-right">
                    <button class="btn btn-danger" data-toggle="modal" data-target="#wipe-modal"><span class="glyphicon glyphicon-trash"></span> Really Wipe</button>
                </div>
            </div>
            
        </div>
        
        
    </div>
</div>

<div class="modal" id="wipe-modal" tabindex="-1" role="dialog"
	aria-labelledby="wipe-modal-label" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<h4 class="modal-title" id="wipe-modal-label">Confirm operation</h4>
			</div>
			<div class="modal-body">
                This will wipe your storage. If you don't have a backup, all your channels and artifacts will be gone.
			</div>
			<div class="modal-footer">
                <form action="<c:url value="/system/storage/wipe"/>" method="POST">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button id="wipe-submit" type="submit" class="btn btn-danger">
                        <span class="glyphicon glyphicon-trash"></span> Wipe storage
                    </button>
                </form>
            </div>
		</div>
	</div>
</div>

</h:main>