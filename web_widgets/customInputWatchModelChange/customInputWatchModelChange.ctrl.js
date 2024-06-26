function PbInputCtrl($scope, $log, widgetNameFactory) {

  'use strict';

  this.name = widgetNameFactory.getName('pbInputWatchModelChange');
  var ctrl = this;

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbInput property named "value" need to be bound to a variable');
  } 
  this.value = $scope.properties.value;
  
  this.updateValue = function updateValue() {
      $scope.properties.value = ctrl.value;
  };
  
  $scope.$watch('properties.value', function() {
      $log.log('New value for ' +  $scope.properties.label + ' : ' + $scope.properties.value);
	     if (($scope.properties.value != ctrl.value) 
	            && (typeof $scope.properties.value !== 'undefined')
	            && ($scope.properties.value != null)) {
	         ctrl.value = $scope.properties.value;
	         $log.log('New value updated');
	     }
	 });
}
    

