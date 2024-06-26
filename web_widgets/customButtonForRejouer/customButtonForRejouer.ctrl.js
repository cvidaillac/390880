function PbFinishCommandCtrl($scope, $http, $location, $log, $window) {

  'use strict';

  var vm = this;
  
  this.choice = function choice() {
    if ($scope.properties.currentProposition.commandes.length === 0) {
       this.message();
    } else {
        this.action();
    }
}
  
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
  
  this.message = function message() {
        var MyCtrl = this;
        this.displayConfirmation(
            'Confirmation de suppression', 
            'Vous êtes sur le point de supprimer la proposition n°' + $scope.properties.currentProposition.persistenceId + '. <br/><br/>Confirmez-vous ?',
            "blue", function() {MyCtrl.action()}
        )
  }

  this.action = function action() {
    var id;

    if ($scope.properties.action === 'Submit task') {
      id = getUrlParam('id');
      if (id) {
        doRequest('POST', '../API/bpm/userTask/' + getUrlParam('id') + '/execution', getUserParam());
      } else {
        $log.log('Impossible to retrieve the task id value from the URL');
      }
    } else if ($scope.properties.url) {
      doRequest($scope.properties.action, $scope.properties.url);
    }
  };

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

  function getUserParam() {
    var userId = getUrlParam('user');
    if (userId) {
      return { 'user': userId };
    }
    return {};
  }

  /**
   * Extract the param value from a URL query
   * e.g. if param = "id", it extracts the id value in the following cases:
   *  1. http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?id=8880000
   *  2. http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?param=value&id=8880000&locale=en
   *  3. http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?param=value&id=8880000&locale=en#hash=value
   * @returns {id}
   */
  function getUrlParam(param) {
    var paramValue = $location.absUrl().match('[//?&]' + param + '=([^&#]*)($|[&#])');
    if (paramValue) {
      return paramValue[1];
    }
    return '';
  }
}
