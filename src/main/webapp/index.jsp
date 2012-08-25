<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<c:set var="rootPath" value="${pageContext.request.requestURL}"/>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <title>Filesophy</title>
    <noscript>Please enable JavaScript to use file uploader.</noscript>
    <link type="text/css" href="<c:url value='resources/css/appcss.css' />" rel="stylesheet"/>
    <link type="text/css" href="<c:url value='resources/jquery-ui-1.8.22.custom/css/start/jquery-ui-1.8.22.custom.css' />" rel="stylesheet"/>
    <script type="text/javascript" src="<c:url value='resources/jquery-ui-1.8.22.custom/js/jquery-1.7.2.min.js' />"></script>
    <script type="text/javascript" src="<c:url value='resources/jquery-ui-1.8.22.custom/js/jquery-ui-1.8.22.custom.min.js' />"></script>
    <script type="text/javascript" src="<c:url value='resources/js/resumable.js' />"></script>
</head>
<body>
<div id="filter"></div>
<div id="float">
    <div id="openlogger"></div>
    <div id="top">
        <p id="hint">Drop files or <span id="directuploadbutton">click here.</span></p>
    </div>
    <div class="bar" style="height: 5px;"></div>
    <div id="bottom">
        <textarea id="logger" readonly>    Uploading used to be fragile. Filesophy can withstand a full internet disconnection and continue afterwards.</textarea>
    </div>
    <p id="faqlink" onclick="window.location.href='${contextPath}/faq';">FAQ</p>
</div>
<div id="torch"></div>

<script>
    $(document).ready(function () {
        $(".bar").progressbar({ value:0 });
        
        $("#filter").animate({
            opacity:0.0
        }, 20000);

        $(document).bind("drop dragover", function (e) {
            e.preventDefault();
        });
        
        var logger = $("#logger");

        var r = new Resumable({
            target:"${contextPath}/upload",
            chunkSize:1 * 1024 * 1024 //1mb
        });

        if (!r.support) {
            alert("Chrome or Firefox is required.");
    	}

        r.assignBrowse(document.getElementById("directuploadbutton"));
        r.assignDrop(document.getElementById("top"));

        r.on("fileAdded", function (file) {
            if ($("#logger").val().toLowerCase().indexOf("fragile") >= 0) {
                $("#logger").val("Sending...\n");
            }
            r.upload();
            logger.val(logger.val() + "\n" + "Added: " + file.fileName);
            logger.scrollTop = logger.scrollHeight;
        });
        r.on("fileSuccess", function (file, message) {
        	logger.val(logger.val() + "\n" + "File done: " + file.fileName + "\n" + "Download: ${rootPath}download?id=" + file.fileName);
        	logger.scrollTop = logger.scrollHeight;
        });
        r.on("fileRetry", function (file) {
        	logger.val(logger.val() + "\n" + "Retrying: " + file.fileName);
        	logger.scrollTop = logger.scrollHeight;
            r.upload();
        });
        r.on("fileError", function (file, message) {
        	logger.val(logger.val() + "\n" + "Error: " + file.fileName);
        	logger.scrollTop = logger.scrollHeight;
            r.files[0].retry();
        });
        r.on("progress", function () {
            $(".bar").progressbar({ value:r.progress() * 100.0 });
        });
        r.on("complete", function () {
        	logger.val(logger.val() + "\n\n" + "Completed.");
        });
    });
</script>

</body>
</html>
