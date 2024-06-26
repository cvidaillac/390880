function listFilters($scope) {
    $scope.properties.urlGenerate = "";
    
	$scope.searchBufferEmpty = {}
    // The element that store the actual inputs 
	$scope.searchBuffer = {}
    // The element that store the final inputs hat are used to filter the table 
	$scope.finalSearch = {}

    // Actions for the onclick button : Activate the filtering 
	this.startFilters = function () {
        this.generateCustomUrl();
    }

    // Actions for the onclick button : Reset the filtering
	this.resetFilters = function () {
		angular.copy($scope.searchBufferEmpty, $scope.searchBuffer);
		this.startFilters();
 	}
 	
 	 this.resetValue = function (filter) {
        $scope.searchBuffer[filter.data] = "";
    } 

    this.generateCustomUrl = function() {
        $scope.properties.urlGenerate = "";
        var urlToGenerate = "";
        
        angular.forEach($scope.searchBuffer, function(value, key) {
            
            if(value !== ""){
                
                var searchKey = "test";
                
                var searchKey = $scope.properties.filtersLabels.find(function(e) {
                    return e.data == key && e.searchKey;
                });
                
                var operatorSearchKey = searchKey ? searchKey.searchKey : "f"

                urlToGenerate += "&"+ operatorSearchKey +"=" + key + "=" + value;
                
                var filter = $scope.properties.filtersLabels.find(function(e) {
                    return e.data == key && e.operator;
                });

                var operator = filter ? filter.operator : "E"
                
                urlToGenerate += "=" + operator
                
            }
        })
        if(urlToGenerate===""){
                    $scope.properties.urlGenerate = "N";

            
        }
        else
        {
                    $scope.properties.urlGenerate = urlToGenerate;

        }

        
    } 

}