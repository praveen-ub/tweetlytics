
function getChart(containerId, chartType){
  var chart;
  switch(chartType){
    case 'bar':
      chart = new google.visualization.BarChart(document.getElementById(containerId));
      break;
    case 'pie':
      chart = new google.visualization.PieChart(document.getElementById(containerId));
      break;
    case 'geo':
      chart = new google.visualization.GeoChart(document.getElementById(containerId));
      break;
  }
  return chart;
}

function getChartData(rawData, chartType){

  var data = new google.visualization.DataTable();
  data.addColumn('string', 'Topping');
  data.addColumn('number', 'Tweets');
  data.addRows(rawData);
  return data;

}


function drawChart(chart, containerId){

  var chartTitle = chart.title;
  var chartType = chart.chartType;
  var rawData = chart.data;
  var chart = getChart(containerId, chartType);
  var data = getChartData(rawData, chartType);
  var options = {'title':'Volume '+chartTitle,
                 'width':400,
                 'height':300};
  chart.draw(data,options);
}
