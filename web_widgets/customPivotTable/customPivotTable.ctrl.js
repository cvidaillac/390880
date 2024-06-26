
function ($scope) {

  var data = $scope.properties.data;
 
  // pivot grid options
  var config = {
    dataSource: data,
    canMoveFields: false, 
    dataHeadersLocation: 'rows',
    theme: $scope.properties.theme,
    toolbar: {
        visible: $scope.properties.toolbar
    },
    grandTotal: {
        rowsvisible: true,
        columnsvisible: true
    },
    subTotal: {
        visible: true,
        collapsed: true
    },
    fields: $scope.properties.fields,
    rows    : $scope.properties.rows ,
    //columns : ['Class'],
    data    : [ 'Amount' ],
    /*preFilters : {
        'Manufacturer': { 'Matches': /n/ },
        'Amount'      : { '>':  40 }
    } */ //,
    width: 1110,
    height: 645
  };

  // instantiate and show the pivot grid
  new orb.pgridwidget(config).render(document.getElementById('pgrid'));
}