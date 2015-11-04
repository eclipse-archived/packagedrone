<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script>
var url = "<c:url value="/channel/${channel.id}/drop"/>";
var dz = new Dropzone ( "#dropzone", {
    url: url,
    paramName: "file",
    previewsContainer: "#upload",
    uploadMultiple: false,
    clickable: false,
    previewTemplate: "<div class='panel panel-default'><div class='panel-heading'><div class='dz-filename' data-dz-name></div> <div class='dz-size' data-dz-size></div> </div> <div class='panel-body'><div class='progress'><div class='progress-bar progress-bar-striped' role='progressbar' data-dz-uploadprogress></div></div> <div class='dz-error-message bg-danger' data-dz-errormessage></div></div></div>"
});

dz.on ( "queuecomplete", function () {
    document.getElementById("upload-refresh").setAttribute("style", "");
});

dz.on ( "addedfile", function () {
    $( "#upload-container" ).show ();
});
</script>
