<html>
<head>
    <title>${loaderName}</title>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="30">
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type="text/javascript">
        google.charts.load('current', {'packages':['table']});
        google.charts.setOnLoadCallback(drawTable);

        function initialize() {
            var opts = {sendMethod: 'auto'};
            // Replace the data source URL on next line with your data source URL.
            var query = new google.visualization.Query(${dataSource}, opts);

            // Optional request to return only column C and the sum of column B, grouped by C members.
            // query.setQuery('select C, sum(B) group by C');

            // Send the query with a callback function.
            query.send(handleQueryResponse);
        }
          
        function handleQueryResponse(response) {
            if (response.isError()) {
                alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
                return;
            }

            var data = response.getDataTable();
            var table = new google.visualization.Table(document.getElementById('table_div'));
            table.draw(data, {showRowNumber: false, width: '100%', height: '100%'});
        }
    </script>
</head>
<body>
    <h1>Point loader: ${loaderName}</h1>
    ${dataSource}
    <div id="debug"></div>
    <br/>
    <div id="plot_div"></div>
    <br/>
    <div id="table_div"></div>
</body>
</html>
