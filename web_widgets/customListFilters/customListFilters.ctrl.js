function listFilters($scope, $log) {
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
		$scope.properties.multipleSelect = [];  //Added by Amit for BPM-40 to reset multiselect value 
		this.startFilters();
 	}
 	
 	this.initFilterValue = function initFilterValue(filter) {
 	    var init_value = "";
 	    
 	    try {
     	    if ((typeof filter.defaultValue === "undefined") || (filter.defaultValue == null)) {
     	        // Default to first value
     	        init_value = filter.values[0];
     	    } else {
     	        init_value = filter.defaultValue;
     	    }
 	    } catch(e) {
 	        $log.error("Exception initFilterValue: " + e);
 	    } 	    
 	    return init_value;
 	};
 	
 	 this.resetValue = function (filter) {
        $scope.searchBuffer[filter.data] = "";
    } 
    
     this.resetValueMulti = function (filter) {
        $scope.properties.multipleSelect = [];
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
        $scope.properties.urlGenerate = urlToGenerate;
        
    } 

}