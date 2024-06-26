function PbAutocompleteCtrl($scope, $parse, $log, widgetNameFactory) {

  'use strict';

  function createGetter(accessor) {
    return accessor && $parse(accessor);
  }

  this.getLabel = createGetter($scope.properties.displayedKey) || function (item) {
    return typeof item === 'string' ? item : JSON.stringify(item);
  };

  this.name = widgetNameFactory.getName('pbAutocomplete');
  this.displayed_value = null;
  this.selectedRecord = null;
  var ctrl = this;

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbAutocomplete property named "value" need to be bound to a variable');
  }
  
  this.clearValue = function clearValue() {
        $log.debug("Clearing value for " + $scope.properties.label);
        if ($scope.properties.isBound('triggerClearValue')) {
            $scope.properties.triggerClearValue = false;
        }
        ctrl.displayed_value = null;
        $scope.properties.value = null;
        ctrl.updateSelectedObject();
  };
  
  this.updateSelectedObject = function updateSelectedObject() {
      try {
        if ((ctrl.displayed_value != null) && (ctrl.displayed_value != "") ) {
            if ($scope.properties.availableValues) {
                var nb_values = $scope.properties.availableValues.length;
                for (var i=0; i<nb_values; i++) {
                    var item = $scope.properties.availableValues[i];
                    if (ctrl.getLabel(item) == ctrl.displayed_value) {
                        ctrl.selectedRecord = item;
                        if ($scope.properties.isBound('selectedObject')) {
                            $scope.properties.selectedObject = item;
                        }
                        break;
                    }
                }
            }
        } else {
            ctrl.selectedRecord = null;
            if ($scope.properties.isBound('selectedObject')) {
                $scope.properties.selectedObject = null;
            }
        }
      } catch(e) {
          $log.error("Exception updateSelectedObject: " + e);
      }
  };

  if ($scope.properties.isBound('selectedObject')) {
    $scope.$watch('properties.value', function(displayed_value) {
            if ((typeof displayed_value !== "undefined") && (displayed_value != ctrl.displayed_value)) {
                    // Check that we have not cleared the selected record
                    if ($scope.properties.isBound('triggerClearValue') && ($scope.properties.triggerClearValue == true)) {
                        $log.debug("Ignoring display value changed for " + $scope.properties.label);
                        ctrl.clearValue();                 
                    } else {
                        $log.debug("Display value changed for " + $scope.properties.label + ": " + displayed_value);
                          ctrl.displayed_value = displayed_value;
                          ctrl.updateSelectedObject();
                    }
                }
    });
        
    $scope.$watch('properties.selectedObject', function(selected_record) {
      try {
            $log.debug("selectedObject changed for " + $scope.properties.label);
            if (($scope.properties.selectedObject == null) || (! angular.equals($scope.properties.selectedObject, ctrl.selectedRecord))) {
                      ctrl.selectedRecord = $scope.properties.selectedObject;
                      ctrl.displayed_value = ($scope.properties.selectedObject == null) ? null : ctrl.getLabel($scope.properties.selectedObject);
                      $scope.properties.value = ctrl.displayed_value;
                      var debug_value = (ctrl.displayed_value == null) ? "null" : ctrl.displayed_value;
                      $log.debug("selectedRecord changed to " + debug_value);
            }
      } catch(e) {
          $log.error("Exception selectedObject change: " + e);
      }
    });
    
    $scope.$watch('properties.triggerClearValue', function(clear_value) {
      try {
          if (clear_value) {
            ctrl.clearValue();
          }
      } catch(e) {
          $log.error("Exception triggerClearValue change: " + e);
      }
    });

  }
  
}
