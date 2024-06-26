function PbAutocompleteCtrl($scope, $parse, $log, widgetNameFactory) {

  'use strict';
  var vm=this;
  

  function createGetter(accessor) {
    return accessor && $parse(accessor);
  }

  this.expl='';
  this.getvalue = function () {

      if ( ! $scope.properties.returnKey ) {
        $scope.properties.value = this.valuebrut;
        this.expl="noreturnkey";
        return this.valuebrut;
      } else {
        $scope.properties.value = this.valuebrut[ $scope.properties.returnKey ];
        this.expl="returnkey["+$scope.properties.returnKey+"]";
        return this.valuebrut[ $scope.properties.returnKey ];
      }
  }
  this.setvalue = function ( value ) {
      if (! $scope.properties.returnKey) {
        // we can not do that
      } else if (! $scope.properties.availableValues ) {
          console.log( "availableValues is undefined");
      } else {
    
        console.log( "Set Value Avaialable value=["+JSON.stringify($scope.properties.availableValues)+"]");
              
          for (var i=0; i<  $scope.properties.availableValues.length; i++) {
              var item =  $scope.properties.availableValues[ i ];
              if (item[ $scope.properties.returnKey ] == value)
              {
                   // console.log( "Set valuebrut=["+JSON.stringify(item)+"]");
                  this.valuebrut = item;
              }
          }
      }
  }
  
  
  this.getLabel = createGetter($scope.properties.displayedKey) || function (item) {
    return typeof item === 'string' ? item : JSON.stringify(item);
  };

  this.name = widgetNameFactory.getName('customAutocompleteExt');

  if (!$scope.properties.isBound('value')) {
    $log.error('the customAutocompleteExt property named "value" need to be bound to a variable');
  }
  
  
  // wait available value and when it's arrive, use it
 $scope.$watch('properties.availableValues', function() {
    // console.log( "CALL Avaialable value=["+JSON.stringify($scope.properties.availableValues)+"]");
     vm.setvalue( $scope.properties.value );
     
 });
  
  this.setvalue( $scope.properties.value);
  
 // $scope.$watch('properties.value', function() {
 //    this.setvalue( $scope.properties.value );
  // });
 
 
}
