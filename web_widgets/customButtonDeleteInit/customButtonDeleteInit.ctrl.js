function PbButtonCtrl($scope, $http, $location, $log, $window) {

  'use strict';

  var vm = this;
  
  this.displayConfirmation = function(title, content, type, callbackOnSuccess) {
        
        $.confirm({
            title: title, type : type, closeIcon: true, content: content,
            buttons: {
                confirmer: function () {
                    callbackOnSuccess();
                },
                annuler: function () {
                }
            }
        });
    }
  
  this.action = function action() {
        var MyCtrl = this;
        this.displayConfirmation(
            'Confirmation de supression', 
            'Vous avez sélectionné la proposition ' + $scope.properties.currentPropositionId + '. Confirmer la supression?',
            "blue", function() {MyCtrl.remove()})
    }
    
    this.remove = function () {
        if ($scope.properties.collectionToModify) {
          if (!Array.isArray($scope.properties.collectionToModify)) {
            throw 'Collection property for widget button should be an array, but was ' + $scope.properties.collectionToModify;
          }
          var index = -1;
          if ($scope.properties.collectionPosition === 'First') {
            index = 0;
          } else if ($scope.properties.collectionPosition === 'Last') {
            index = $scope.properties.collectionToModify.length - 1;
          } else if ($scope.properties.collectionPosition === 'Item') {
            index = $scope.properties.collectionToModify.indexOf($scope.properties.removeItem);
          }
    
          // Only remove element for valid index
          if (index !== -1 ) {
                $scope.properties.collectionToModify.splice(index, 1);
                $scope.$apply();
          }
          
    }
  }
  
  /**
   * Execute a get/post request to an URL
   * It also bind custom data from success|error to a data
   * @return {void}
   */
  function doRequest(method, url, params) {
    vm.busy = true;
    var req = {
      method: method,
      url: url,
      data: angular.copy($scope.properties.dataToSend),
      params: params
    };

    return $http(req)
      .success(function(data, status) {
        $scope.properties.dataFromSuccess = data;
        notifyParentFrame({ message: 'success', status: status, dataFromSuccess: data });
        if ($scope.properties.targetUrlOnSuccess && method !== 'GET') {
          $window.location.assign($scope.properties.targetUrlOnSuccess);
        }
      })
      .error(function(data, status) {
        $scope.properties.dataFromError = data;
        notifyParentFrame({ message: 'error', status: status, dataFromError: data  });
      })
      .finally(function() {
        vm.busy = false;
      });
  }

  function notifyParentFrame(additionalProperties) {
    if ($window.parent !== $window.self) {
      var dataToSend = angular.extend({}, $scope.properties, additionalProperties);
      $window.parent.postMessage(JSON.stringify(dataToSend), '*');
    }
  }
}
