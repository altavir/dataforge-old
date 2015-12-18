<!DOCTYPE html>
<html>
    <head>
        <title>${loaderName}</title>
        <meta charset="UTF-8">
        <meta http-equiv="refresh" content="30">
    </head>
    <body>
        <h1> State loader: ${loaderName}</h1>
        <#list states?keys as name>
            <p>${name} = ${states[name]};</p> 
        </#list>
    </body>
</html>
