<!DOCTYPE html>
<html>
<head>
    <title>DataForge context::${contextName}</title>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="120">

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<#if navigation??>
    <#include "Navigation.ftl">
</#if>

<div class="container">

    <div class="page-header">
        <h1>DataForge context view: ${contextName}</h1>
    </div>

    <h2>Context properties:</h2>
    <div class="well">
        <#list properties as entry>
            <p>${entry.getKey()} : <code>${entry.getValue()}</code></p>
        </#list>
    </div>

    <h2>Context plugins:</h2>
    <div class="well">
    <#list plugins?sort_by("key") as entry>
        <p><a href="${entry.getValue()}">${entry.getKey()}</a></p>
    </#list>
    </div>

</div>
<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="js/bootstrap.min.js"></script>
</body>
</html>

