/**
 * Author: David Bravo
 */
function MyCtrl($scope, $window) {
    window.parent.location = $scope.properties.URL;
    $scope.wT = $scope.properties.waitText;
}