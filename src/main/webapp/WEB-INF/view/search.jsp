<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"
        integrity="sha512-Evv84Mr4kqVGRNSgIGL/F/aIDqQb7xQ2vcrdIwxfjThSH8CSR7PBEakCr51Ck+w+/U6swU2Im1vVX0SVk9ABhg=="
        crossorigin="anonymous" referrerpolicy="no-referrer" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-LN+7fdVzj6u52u30Kp6M/trliBMCMKTyK833zpbD+pXdCLuTusPj697FH4R/5mcr" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-table@1.24.2/dist/bootstrap-table.min.css">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <title>Lista utworów</title>
</head>
<body>
<div class="wrapper">
    <div class="usertoolbar clearfix">
        <div class="right">
            <p>
                <span class="bold">Zalogowany jako:</span> ${userString}
            </p>

            <div class="group">
                <button class="btn btn-primary"
                        onclick="window.location.href = '../../logout'">Wyloguj się
                </button>
            </div>
        </div>
    </div>
    <h1 id="header">Wyszukiwarka utworów</h1>
    <div id="toolbar" class="clearfix">
        <form:form action="" method="GET" class="search-form">
            <input id="search-input" class="form-control search-input" type="search" placeholder="Szukaj" name="query" value="${query}" />
            <button type="submit" class="btn btn-success">Szukaj!</button>
        </form:form>
    </div>
    <c:choose>
        <c:when test="${not empty errorMessage}">
            <p class="empty">${errorMessage}</p>
        </c:when>
        <c:otherwise>
            <table aria-describedby="header"
                data-toggle="table" data-toolbar="#toolbar" data-show-columns="true" data-locale="pl-PL"
                data-show-columns-toggle-all="true">

                <thead class="table-dark">
                <tr>
                    <th class="content-column" scope="col" data-sortable="true">Nazwa utworu</th>
                    <th class="content-column" scope="col" data-sortable="true">Album</th>
                    <th class="content-column" scope="col" data-sortable="true">Wykonawcy</th>
                    <th scope="col">Okładka albumu</th>
                    <th scope="col">Podgląd audio</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="track" items="${searchResult}">
                    <tr>
                        <td>${track.trackName}</td>
                        <td>${track.albumName}</td>
                        <td>${track.artistsAsString}</td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty track.imageUrl}">
                                    <img class="album-image"
                                        src="${track.imageUrl}" alt="Okładka albumu: ${track.albumName}">
                                </c:when>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty track.audioPreviewUrl}">
                                    <audio controls preload="none" controlsList="nodownload">
                                        <source src="${track.audioPreviewUrl}" type="audio/mpeg">
                                    </audio>
                                </c:when>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

        </c:otherwise>
    </c:choose>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap-table@1.24.2/dist/bootstrap-table.min.js"></script>

<script src="${pageContext.request.contextPath}/js/pauseOtherAudios.js" defer></script>

</body>
</html>
