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

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.13.1/font/bootstrap-icons.min.css"
        integrity="sha512-t7Few9xlddEmgd3oKZQahkNI4dS6l80+eGEzFQiqtyVYdvcSG2D3Iub77R20BdotfRPA9caaRkg1tyaJiPmO0g=="
        crossorigin="anonymous" referrerpolicy="no-referrer" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.24.2/bootstrap-table.min.css"
        integrity="sha512-ThXPnGXIJOi9huerzcKWpEqyPCaCECA9/Z3Sn5P8T37S2cm57p8Zz8g5r7woyEg+F5u7n5sRYg/LW/pCmpZnDA=="
        crossorigin="anonymous" referrerpolicy="no-referrer" />
        
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

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"
    integrity="sha512-v2CJ7UaYy4JwqLDIrZUI/4hqeoQieOmAZNXBeQyjo21dadnwR+8ZaIJVT8EE2iyI61OV8e6M8PP2/4hpQINQ/g=="
    crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.8/js/bootstrap.bundle.min.js"
    integrity="sha512-HvOjJrdwNpDbkGJIG2ZNqDlVqMo77qbs4Me4cah0HoDrfhrbA+8SBlZn1KrvAQw7cILLPFJvdwIgphzQmMm+Pw=="
    crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.24.2/bootstrap-table.min.js"
    integrity="sha512-GS/lQJ1AiKWEDCgf1yKepN4m/xewSX6jEVL06KQU6jdAZb3FprSXR2cdBL7FHmo6t5M2gcspMi8I6DqzfPGVew=="
    crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.24.2/locale/bootstrap-table-pl-PL.min.js"
    integrity="sha512-vpnWYku7hbJv3KHAoVix8rsUUlcmMU51EfgaNFJsSyr8Bx8TEPAUPApprSe0xJ2nGBvf+VARW6ZrhcVz3W/YfA=="
    crossorigin="anonymous" referrerpolicy="no-referrer"></script>

<script src="${pageContext.request.contextPath}/js/pauseOtherAudios.js" defer></script>

</body>
</html>
