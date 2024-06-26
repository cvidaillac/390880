function PbAutocompleteCtrl($scope, $parse, $log, widgetNameFactory) {

  'use strict';
 
//  if($scope.properties.availableValues.length === 0 || $scope.properties.availableValues.length === null)
//  {
//     $scope.properties.displayedKey = "";
//  }else {
//      $scope.properties.displayedKey = "firstName+' '+lastName+' - '+userName";
//  }
//  if($scope.properties.displayedKey === "firstName+' '+lastName+' - '+userName"){
     
//      $scope.properties.displayedKey = "";
//  }
 $scope.properties.displayedKey = "firstName+' '+lastName+' - '+userName";
 
  function createGetter(accessor) {
    return accessor && $parse(accessor);
  }

  this.getLabel = createGetter($scope.properties.displayedKey) || function (item) {
    return typeof item === 'string' ? item : JSON.stringify(item);
  };

  this.getValue = createGetter($scope.properties.returnedKey) || function (item) {
    return item;
  };

  this.onSelectedCallback = function ($item, $model, $label) {
    this.selectedItem = $item;
  };

  this.formatLabel = function ($model) {
    if (this.selectedItem) {
      return this.getLabel(this.selectedItem);
    } else {
      return this.getLabel($model);
    }
  };

  this.name = widgetNameFactory.getName('pbAutocomplete');

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbAutocomplete property named "value" need to be bound to a variable');
  }
}
