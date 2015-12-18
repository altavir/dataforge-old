<!DOCTYPE html>
<html>
    <head>
        <title>${loaderName}</title>
        <meta charset="UTF-8">
        <meta http-equiv="refresh" content="30">
        <style>
            table {
                width:100%;
            }
            table, th, td {
                border: 1px solid black;
                border-collapse: collapse;
            }
            th, td {
                padding: 5px;
                text-align: left;
            }
            table tr:nth-child(even) {
                background-color: #eee;
            }
            table tr:nth-child(odd) {
                background-color:#fff;
            }
            table th	{
                background-color: black;
                color: white;
            }
            </style>
        </head>
    <body>
        <h1>Point loader: ${loaderName}</h1>
        <table style="width:100%">
            <!-- Table header -->
            <tr>
            <div class="header">
                    <#list data.getDataFormat().iterator() as name>
                <th>${name}</th>
                    </#list>
                </div>
            </tr>  
            <!-- Table content -->
            <#list data.iterator() as point>
                <tr>
                    <#list data.getDataFormat().iterator() as name>
                        <td>${point.getValue(name)}</td>
                    </#list>
                </tr>
            </#list>
        </table>
    </body>
</html>
