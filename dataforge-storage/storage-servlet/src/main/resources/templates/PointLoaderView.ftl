<!DOCTYPE html>
<html>
<head>
    <title>${loaderName}</title>
    <meta charset="UTF-8">
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type="text/javascript">
        google.charts.load('current', {'packages':['table','corechart']});
        google.charts.setOnLoadCallback(startUpdate);
    
        function startUpdate() {
            var opts = {sendMethod: 'auto'};
//            alert('sending request to ${dataSource}')
            var query = new google.visualization.Query("${dataSource}", opts);
            query.setRefreshInterval(30);
                

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
            table.draw(data, {showRowNumber: false, page: 'enable', width : '100%', pageSize: 50, sort : 'disable'});
                
            var options = {
                title: '${loaderName}',
                curveType: 'function',
                legend: { 
                    position: 'bottom' 
                }
            }
            
            var chart = new google.visualization.LineChart(document.getElementById('plot_div'));
            chart.draw(data, options);
        }
    </script>
</head>
<body>
    <h1>Point loader: ${loaderName}</h1>
    <br/>
    <div id="plot_div"></div>
    <br/>
    <div id="table_div"></div>
</body>
</html>
