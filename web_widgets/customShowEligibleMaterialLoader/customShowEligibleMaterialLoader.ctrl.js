/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 * 
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 * 
 * You can leave the controller empty if you do not need it.
 */
function ($scope, $http, $log) {
    
    $scope.showLoder = false;
    $scope.showMsg = false;
    $scope.showError = false;
    $scope.errorMsg = '';
    ctrl = this;
    this.url = null;
    this.running = false;
    this.TRIGGER_RUNNING="Running";
    this.TRIGGER_NO="No";
    
   function getEligibleMaterial()
    {
        if (! ctrl.running) {
            $scope.showLoder = true;
            $scope.showMsg = false;
            $scope.showError = false;    
            ctrl.url = $scope.properties.apiUrl;
            ctrl.running = true;
            
            // Clear previous list
            $scope.properties.successResponse = null;
            
            $http({
               url:  $scope.properties.apiUrl,
               method: 'GET'
            })
            .then(function(resp) {
                $scope.properties.successResponse = resp.data;
                console.log("Got response from url : " + $scope.properties.apiUrl);
                $scope.showLoder = false;
                if(resp.data.length === 0)
                {
                    $scope.showMsg = true;
                }
                ctrl.running = false;
                if (typeof $scope.properties.triggerQuery !== "undefined") {
                    $scope.properties.triggerQuery = ctrl.TRIGGER_NO;
                }
            }, 
            function(resp) { 
                $scope.properties.errorResponse = resp;
                
                $scope.showLoder = false;
                $scope.showError = true;
                $scope.showMsg = false;
                ctrl.running = false;
                if (typeof $scope.properties.triggerQuery !== "undefined") {
                    $scope.properties.triggerQuery = ctrl.TRIGGER_NO;
                }
                var details = '';
                if (resp) {
                    if (typeof resp.status !== 'undefined') {
                        details += ' - Status=' + resp.status;
                    }
                    if ((typeof resp.data !== 'undefined') && (resp.data != null) && (resp.data != '')) {
                        if (typeof resp.data.message !== 'undefined') {
                            details += ' : ' + resp.data.message;
                            
                        } else if (typeof resp.data.errorMsg !== 'undefined') {
                            details += ' : ' + resp.data.errorMsg;
                        
                        } else if (typeof resp.data.error !== 'undefined') {
                            details += ' : ' + resp.data.error;
                            
                        } else if (typeof resp.data === "string") {
                            $log.debug("response is a string");
                            details += ' : ' + resp.data;
                            
                        } else {
                            $log.debug("response is not a string");
                            details += ' : ' + angular.fromJson(resp.data);
                        }
                    } else if ((typeof resp.statusText !== 'undefined') && (resp.statusText != '')) {
                        details += ' (' + resp.statusText + ')';
                    }
    
                }
                
                $scope.errorMsg = $scope.properties.errorText + details;
                $log.error("Error calling url :"+  $scope.properties.apiUrl + details);
    
            });
        } else {
            $log.info("Query is already executing");
        }
    }
    
    if ((typeof $scope.properties.apiUrl !== "undefined") && ($scope.properties.apiUrl != null) && ($scope.properties.apiUrl != "")) {
        // Do not trigger query if flag is set to No
        if ((typeof $scope.properties.triggerQuery === "undefined") || ($scope.properties.triggerQuery == null) || (($scope.properties.triggerQuery != ctrl.TRIGGER_NO) && ($scope.properties.triggerQuery != ctrl.TRIGGER_RUNNING))) {
            getEligibleMaterial();
        }
    } 
    
    $scope.$watch('properties.apiUrl', function(url) {
            if ((typeof url !== "undefined") && (url != null) && (url != "") && (url != ctrl.url)) {
                // Do not trigger query if flag is set to No or Running
                if ((typeof $scope.properties.triggerQuery === "undefined") || ($scope.properties.triggerQuery == null) || (($scope.properties.triggerQuery != ctrl.TRIGGER_NO) && ($scope.properties.triggerQuery != ctrl.TRIGGER_RUNNING))) {
                    getEligibleMaterial();
                }
            }
    });  

    $scope.$watch('properties.triggerQuery', function(value) {
        if ((value != ctrl.TRIGGER_NO) && (value != ctrl.TRIGGER_RUNNING))  {
            // Check Url is set
            if ((typeof $scope.properties.apiUrl !== "undefined") && ($scope.properties.apiUrl != null) && ($scope.properties.apiUrl != "")) {
                // Clear flag
                $scope.properties.triggerQuery = ctrl.TRIGGER_RUNNING;
                
                // Execute query
                getEligibleMaterial();
            }
        }
    });  
    
}