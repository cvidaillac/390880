function PbButtonCtrl($scope, $http) {
    
   $scope.searchData = function() 
   {
        $http({
               url:  $scope.properties.apiUrl,
               method: 'GET'
            })
            .then(function(response) {
                
                $scope.properties.apiSuccess = response.data;
                
                $http({
                   url:  $scope.properties.urlList,
                   method: 'GET'
                })
                .then(function(resp) {
                    
                    $scope.busy = false;
                    $scope.properties.successResponse = resp.data;
                }, 
                function(resp) { 
                    $scope.busy = false;
                    $scope.properties.errorResponse = resp;
                    console.log("error inner:"+response);
                });
            }, 
            function(response) { 
                $scope.busy = false;
                $scope.properties.errorResponse = response;
                console.log("error main:"+response);
            });
        
   };
  
}
