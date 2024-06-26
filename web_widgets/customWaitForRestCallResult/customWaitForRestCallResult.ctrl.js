/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 * 
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 * 
 * You can leave the controller empty if you do not need it.
 */
function ($scope, $timeout, $log, $http) {
    var ctrl = this;
    ctrl.widgetName = 'waitForRestCallResult';
    ctrl.retryCounter = 0;
    ctrl.isVariableSet = false;
    ctrl.$scope = $scope;

    ctrl.debug = function(functionName, msg) {
        $log.debug(ctrl.widgetName +'.' + functionName + ' : ' + msg);   
    };
    
    ctrl.info = function(functionName, msg) {
        $log.info(ctrl.widgetName +'.' + functionName + ' : ' + msg);   
    };
    
    ctrl.exception = function(functionName, ex) {
        $log.error(ctrl.widgetName +'.' + functionName + ' : ' + ex);   
    };
    
    // Call the URL if variable is still null and we do not exceed the maximum number of retries
    ctrl.callUrl = function() {
        try {
            if ((typeof $scope.properties.urlCalled !== "undefined") 
                    && ($scope.properties.urlCalled != null)) {
                ctrl.retryCounter = ctrl.retryCounter + 1;
                ctrl.debug("callUrl", 'New attempt n°' + ctrl.retryCounter);

                // Check if variable is null
                if (! isCheckedVariableNull(ctrl, $scope.properties.variableCheckedAtInterval)) {
                    ctrl.info("callUrl", 'variableCheckedAtInterval is not empty, no more retry');
                    
                } else {
                    // Check retry count
                    if (ctrl.retryCounter >= $scope.properties.maxNumberOfRetries) {
                        ctrl.info("callUrl", "Reached the maximum number of retries " + $scope.properties.maxNumberOfRetries + ", no more retry");
                    } else {
                        // Update counter for next retry
                        $scope.properties.retryCount = ctrl.retryCounter;

                        var onSuccessAction = function (data, status, headers, config) {
                            ctrl.info("callUrl", "Attempt n°" + ctrl.retryCounter + " is sucessful");
                            ctrl.$scope.busy = false;    
                                
                            // Return result
                            ctrl.$scope.properties.restCallResult = data;
                            ctrl.debug("callUrl", "Scheduling next attempt");
                            triggerNewAttemptAfterDelay(ctrl, ctrl.$scope);
                        };
                        
                        var onErrorAction = function (data, status, headers, config) {
                            ctrl.info("callUrl", "Attempt n°" + ctrl.retryCounter + " failed with status=" + status);
                                ctrl.$scope.busy = false;
                                
                                // Trigger next attempt
                                triggerNewAttemptAfterDelay(ctrl, ctrl.$scope);
                        };
                        
                        // Call URL
                        $scope.busy = true;
                        $http.get($scope.properties.urlCalled)
                            .success(onSuccessAction)
                            .error(onErrorAction);
                    }
                }
            }
            
        } catch(e) {
            ctrl.exception("callUrl", e);
        }
    };
    
    // Watch for URL to be updated
    $scope.$watch('properties.urlCalled', function() {
        if ((typeof $scope.properties.urlCalled !== "undefined") 
                    && ($scope.properties.urlCalled != null)) {
            ctrl.debug("urlCalled is now : " + $scope.properties.urlCalled);
            if (ctrl.retryCounter === 0) {
                // Trigger first attempt
                ctrl.callUrl();
            }
        }
    });
    
     $scope.$watch('properties.variableCheckedAtInterval', function() {
        if ((typeof $scope.properties.variableCheckedAtInterval !== "undefined") 
                    && ($scope.properties.variableCheckedAtInterval != null)) {
            ctrl.isVariableSet = true;
            ctrl.info("$watch()", "Variable is no longer null : " + $scope.properties.variableCheckedAtInterval);
        }
     });
     
    // Trigger first attempt
    ctrl.callUrl();
    
    function triggerNewAttemptAfterDelay(ctrl, $scope) {
        $timeout( ctrl.callUrl, $scope.properties.retryInterval );
    }
    
    function isCheckedVariableNull(ctrl, varValue) {
        var res = true;
        
        try {
            if (ctrl.isVariableSet
                || ((typeof varValue !== "undefined")
                    && (varValue != null))) {
                    ctrl.info("isCheckedVariableNull", "Variable is no longer null : " + varValue);
                    res = false;
            }
            
        } catch(e) {
            ctrl.exception("isCheckedVariableNull", e);
        }
        return res;
    }
}