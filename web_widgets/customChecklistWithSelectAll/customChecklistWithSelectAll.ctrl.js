function PbChecklistCtrl($scope, $parse, widgetNameFactory, $log) {

  'use strict';
  var ctrl = this;
  
  this.selectAll = false;

  /**
   * Watch the data source and set wrapChoices and $scope.properties.selectedValues
   */
  function comparator(item, initialValue) {
    return angular.equals(item, initialValue);
  }

  function createGetter(accessor) {
    return accessor && $parse(accessor);
  }

  this.getLabel = createGetter($scope.properties.displayedKey) || function (item) {
      return typeof item === 'string' ? item : JSON.stringify(item);
    };

  this.getValue = createGetter($scope.properties.returnedKey) || function (item) {
      return item;
    };


  /**
   * update the scope.properties.selectedValues with the selected items
   */
  this.updateValues = function () {
      ctrl.doUpdateValues();
      ctrl.checkAllSelected();
  };
  
  this.doUpdateValues = function () {
    $scope.properties.selectedValues = ctrl.selectedItems
      .map(function (checked, index) {
        if (checked !== true) {
          return false;
        }
        var item = $scope.properties.availableValues[index];
        return ctrl.getValue(item);
      }).filter(function (item) {
        return item !== false;
      });
  };
  
  this.updateSelectAll = function() {
      for (var i=0; i<ctrl.selectedItems.length; i++) {
        ctrl.selectedItems[i] = ctrl.selectAll;
      }
      ctrl.doUpdateValues();
  };
  
  this.checkAllSelected = function() {
      var all_selected = (ctrl.selectedItems.length > 0) ? true : false;
      for (var i=0; i<ctrl.selectedItems.length; i++) {
        if (! ctrl.selectedItems[i]) {
            all_selected = false;
            break;
        }
      }
      ctrl.selectAll = all_selected;
  };

  function updateSelectedValues() {
    ctrl.selectedItems = ($scope.properties.availableValues || []).map(function (item) {
      if (Array.isArray($scope.properties.selectedValues)) {
        return $scope.properties.selectedValues.some(comparator.bind(null, ctrl.getValue(item)));
      }
      return false;
    });
  }

  $scope.$watchCollection('properties.availableValues', updateSelectedValues);
  $scope.$watchCollection('properties.selectedValues', updateSelectedValues);

  this.name = widgetNameFactory.getName('pbChecklist');

  if (!$scope.properties.isBound('selectedValues')) {
    $log.error('the pbCheckList property named "selectedValues" need to be bound to a variable');
  }
}
