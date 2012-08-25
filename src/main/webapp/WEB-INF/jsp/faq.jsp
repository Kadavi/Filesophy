<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ page session='false' %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <title>Filesophy</title>
    <link type="text/css" href="<c:url value='resources/css/appcss.css' />" rel="stylesheet"/>
    <script type="text/javascript" src="<c:url value='resources/jquery-ui-1.8.22.custom/js/jquery-1.7.2.min.js' />"></script>
</head>
<body>
<div id="container">
    FAQ <a id="faq" href="${contextPath}/">[go back]</a><br><br>
    <ul>
        <li>
            <a href='#' class='head'>What is Filesophy?</a>

            <div class='content'>
                It is a file uploader that makes intelligent decisions when bad things happen.
                Try turning your internet off during an upload then back on.
                It will resume normally, all without using Applets or Flash.
                <br><br>It also accepts multiple files in a single drag and drop.
            </div>
        </li>
        <li>
            <a href='#' class='head'>What technologies were used in creating Filesophy?</a>

            <div class='content'>
                What: Java backend and HTML5 frontend.<br>
                How: Sends 1mb chunks to server and combines them.<br><br>
                HTML5, CSS3, Spring MVC, JSP, Servlet, JQuery, Heroku, Git, Commons IO, DOM XHR, AJAX, Tomcat,
                Resumable.js, Impress.js, Maven, Bootstrap, Java.
            </div>
        </li>
        <li>
            <a href="#" class='head'>Does it work in all browsers?</a>

            <div class='content'>Currently for Chrome and Firefox only.</div>
        </li>
        <li>
            <a href="#" class='head'>When will uploaded files expire?</a>

            <div class='content'>In several minutes.</div>
        </li>
    </ul>
</div>

<div id="torch"></div>

<script>
    $(document).ready(function () {
        $('.head').each(function () {
            var $content = $(this).closest('li').find('.content');
            $(this).click(function (e) {
                e.preventDefault();
                $content.not(':animated').slideToggle();
            });
        });
    });
</script>

</body>
</html>
