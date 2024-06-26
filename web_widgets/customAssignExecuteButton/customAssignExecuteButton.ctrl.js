/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 * 
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 * 
 * You can leave the controller empty if you do not need it.
 */
function ($scope,$http) {
    
    $scope.btnLabel = $scope.properties.label;
    $scope.btnStyleClass = $scope.properties.btnStyle;
    
    $scope.execute = function() {
        $scope.busy = true;
        $http({
                    url:  $scope.properties.actionUrl,
                    method: 'PUT',
                    data: $scope.properties.requestPayload
                })
                .then(function(response) {
                    $http({
                        url:  $scope.properties.executeUrl,
                        method: 'POST',
                        data: $scope.properties.executePayload
                    })
                    .then(function(response) {
                        $scope.properties.successResponse = response;
                        window.parent.location = $scope.properties.targetUrl;
                    }, 
                    function(response) { 
                        $scope.properties.errorResponse = response;
                        $scope.busy = false;
                    });
                }, 
                function(response) { 
                    $scope.properties.errorResponse = response;
                    $scope.busy = false;    
                });
        
    };
    
}