<!DOCTYPE html>
<html>
<head>
    <title>DataForge storage::${storageName}</title>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="30">

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<#if navigation??>
    <#include "Navigation.ftl">
</#if>

<#include "BreadCrumbs.ftl">

<div class="container">
    <div class="page-header">
        <h1>Storage: ${storageName}</h1>
    </div>
    ${content}
</div>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="js/bootstrap.min.js"></script>
</body>
</html>

