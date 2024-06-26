function PbButtonCtrl($scope, $http, $location, $log, $window, $timeout) {

  'use strict';

  var vm = this;

  this.action  = function action() {
      vm.doAction($scope.properties.action1, 1, function(){vm.doAction2()});
  };
  
  this.doAction2 = function doAction2() {
      vm.doAction($scope.properties.action2, 2, function(){vm.doAction3()});
  };

  this.doAction3 = function doAction3() {
      vm.doAction($scope.properties.action3, 3, function(){vm.doAction4()});
  };
  
  this.doAction4 = function doAction4() {
      vm.doAction($scope.properties.action4, 4, null);
  };
  
  
  this.doNextAction = function doNextAction(do_next_callback, action_index) {
      if (do_next_callback !== null) {
          var delay_ms = 10;
          if (($scope.properties.delayBeforeLastAction !== null) && ($scope.properties.delayBeforeLastAction > 0)) {
            if ((action_index === 3)  || ((action_index === 2) &&  (($scope.properties.action4 === null) || ($scope.properties.action4 === '-')))) {
				delay_ms = $scope.properties.delayBeforeLastAction * 1000;
			}
              
          }
          $timeout(do_next_callback, delay_ms);
            
      }
  };
  
  this.updateDataFromSuccess = function updateDataFromSuccess(index, data, get_first_row_only) {
      var result_data = data;
      
      if ((get_first_row_only) && (Array.isArray(data) && (data.length>0))) {
          result_data = data[0];
      }
      if (index == 1) {
          $scope.properties.dataFromSuccess1 = result_data;
      } else if (index == 2) {
          $scope.properties.dataFromSuccess2 = result_data;
      } else if (index == 3) {
          $scope.properties.dataFromSuccess3 = result_data;
      } else {
          $scope.properties.dataFromSuccess4 = result_data;
      }
  };

  this.clearPreviousData = function clearPreviousData() {
      try {
          if ((typeof $scope.properties.dataFromSuccess1 !== 'undefined')
             && ($scope.properties.dataFromSuccess1 != null)) {
              $scope.properties.dataFromSuccess1 = null;
          }
          if ((typeof $scope.properties.dataFromSuccess2 !== 'undefined')
             && ($scope.properties.dataFromSuccess2 != null)) {
              $scope.properties.dataFromSuccess2 = null;
          }
          if ((typeof $scope.properties.dataFromSuccess3 !== 'undefined')
             && ($scope.properties.dataFromSuccess3 != null)) {
              $scope.properties.dataFromSuccess3 = null;
          }
          if ((typeof $scope.properties.dataFromSuccess4 !== 'undefined')
             && ($scope.properties.dataFromSuccess4 != null)) {
              $scope.properties.dataFromSuccess4 = null;
          }
    } catch(e) {
        vm.logMsg("Exception clearPreviousData() : " + e);
    }
  };
  
  this.logMsg = function logMsg(msg) {
     $log.log('Widget ButtonMultipleActions: ' + msg);
  };
  
  this.doAction = function doAction(action_property, action_index, do_next_callback) {
    var id;

    try {
        vm.logMsg("Executing action " + action_index + ' : ' + action_property);
        
        if (action_property === 'Remove from collection') {
          removeFromCollection($scope.properties.collectionToModify, $scope.properties.collectionPositionRemove);
          vm.doNextAction(do_next_callback, action_index);
          
        } else if (action_property === 'Remove from collection 2') {
          removeFromCollection($scope.properties.collectionToModify2, $scope.properties.collection2PositionRemove);
          vm.doNextAction(do_next_callback, action_index);
          
        } else if (action_property === 'Add to collection') {
          addToCollection();
          vm.doNextAction(do_next_callback, action_index);
          
        } else if (action_property === 'Start process') {
          if ($scope.properties.startProcessDefinitionId) {
            // Get process definition from widget parameter
            id = $scope.properties.startProcessDefinitionId;
          } else {
            // Get process definition from URL parameter on instanciation form
            id = getUrlParam('id');
          }
          if (id) {
            doRequest('POST', '../API/bpm/process/' + id + '/instantiation', getUserParam(), action_index, do_next_callback);
          } else {
            vm.logMsg('Impossible to retrieve the process definition id value from the URL');
          }
        } else if (action_property === 'Submit task') {
          if ($scope.properties.taskId) {
            // Get process definition from widget parameter
            id = $scope.properties.taskId;
          } else {
            id = getUrlParam('id');
          }
          if (id) {
            doRequest('POST', '../API/bpm/userTask/' + id + '/execution', getUserParam(), action_index, do_next_callback);
          } else {
            vm.logMsg('Impossible to retrieve the task id value from the URL');
          }
        
        } else if (action_property === 'Get human task') {
            retrieveHumanTask(action_index, do_next_callback, 0);
        
        } else if (action_property === 'Get task context') {
            retrieveTaskContext(action_index, do_next_callback, 0);
        
        } else if (action_property === 'Clear previous data') {
            vm.clearPreviousData();
            vm.doNextAction(do_next_callback, action_index);
        
        } else if (action_property === 'Set value') {
            vm.updateDataFromSuccess(action_index, $scope.properties.valueToSet, false);
            vm.doNextAction(do_next_callback, action_index);
            
        } else if (action_property === 'Set process parameter') {
            vm.setProcessParameter(action_index, do_next_callback, 0);
            
        } else if (action_property === '-') {
            // Do nothing
            
        } else if ($scope.properties.url) {
            doRequest(action_property, $scope.properties.url, null, action_index, do_next_callback);
        }
    } catch(e) {
        vm.logMsg("Exception doAction() : " + e);
    }
  };



    this.displayConfirmation = function() {
        $.confirm({
            title: 'La proposition à été dupliquée',
            content: 'Elle est maintenant accessible dans votre page principale dans l\'étape <b>Parachèvement de la demande</b>',
            autoClose: 'ok|5000',
            buttons: {
                ok: {
                    text: 'Fermer',
                    action: function () {
                    }
                }
            }
        });
        
    }

  function removeFromCollection(collection_to_modify, collection_position_remove) {
    if (collection_to_modify) {
      if (!Array.isArray(collection_to_modify)) {
        throw 'Collection property for widget button should be an array, but was ' + collection_to_modify;
      }
      var index = -1;
      if (collection_position_remove === 'First') {
        index = 0;
      } else if (collection_position_remove === 'Last') {
        index = collection_to_modify.length - 1;
      } else if (collection_position_remove === 'Item') {
        index = collection_to_modify.indexOf($scope.properties.removeItem);
      } else if (collection_position_remove === 'Index') {
        index = $scope.properties.collectionRemoveIndex;
      }

      // Only remove element for valid index
      if (index !== -1) {
        collection_to_modify.splice(index, 1);
      }
    }
  }

  function addToCollection() {
    if (!$scope.properties.collectionToModify) {
      $scope.properties.collectionToModify = [];
    }
    if (!Array.isArray($scope.properties.collectionToModify)) {
      throw 'Collection property for widget button should be an array, but was ' + $scope.properties.collectionToModify;
    }
    var item = angular.copy($scope.properties.valueToAdd);

    if ($scope.properties.collectionPositionAdd === 'First') {
      $scope.properties.collectionToModify.unshift(item);
    } else {
      $scope.properties.collectionToModify.push(item);
    }
  }

  /**
   * Execute a get/post request to an URL
   * It also bind custom data from success|error to a data
   * @return {void}
   */
  function doRequest(method, url, params, action_index, do_next_callback) {
    vm.busy = true;
    vm.logMsg('Action ' + action_index + ' - Executing ' + method + ' on URL: ' + url);
    
    var req = {
      method: method,
      url: url,
      data: angular.copy($scope.properties.dataToSend),
      params: params
    };

    return $http(req)
      .success(function(data, status) {
        //$scope.properties.dataFromSuccess = data;
        vm.updateDataFromSuccess(action_index, data, false);
        notifyParentFrame({ message: 'success', status: status, dataFromSuccess: data });
        if ($scope.properties.targetUrlOnSuccess && method !== 'GET') {
          $window.location.assign($scope.properties.targetUrlOnSuccess);
        }
        vm.doNextAction(do_next_callback, action_index);
      })
      .error(function(data, status) {
        $scope.properties.dataFromError = data;
        notifyParentFrame({ message: 'error', status: status, dataFromError: data  });
      })
      .finally(function() {
        vm.busy = false;
      });
  }
  
  this.setOneProcessParameter = function setOneProcessParameter(process_id, parameter_name, process_name, do_next_callback, action_index, process_index) {
      try {
        var url = "../API/bpm/processParameter/" + process_id + "/" + parameter_name;
        vm.busy = true;
        vm.logMsg('Action ' + action_index + ' - Setting parameter ' + parameter_name + ' on process ' + process_name + ' with id ' + process_id + ' with URL: ' + url);
        
        var req = {
          method: 'PUT',
          url: url,
          data: angular.copy($scope.properties.dataToSend)
        };
        
        var error_msg = 'Erreur : Echec de la mise à jour du paramètre sur le process ' + process_name + " avec l'id " + process_id;
        return $http(req)
          .success(function(data, status) {
                vm.logMsg('Action ' + action_index + ' - Parameter ' + parameter_name + ' updated on process ' + process_name + " avec l'id " + process_id);
                // Set next process in the list
                vm.setProcessParameter(action_index, do_next_callback, process_index +1);
          })
          .error(function(data, status) {
            $scope.properties.dataFromError = error_msg + ' (statut ' + status + ')';
            notifyParentFrame({ message: 'error', status: status, dataFromError: data  });
          })
          .finally(function() {
            vm.busy = false;
          });

      } catch(e) {
          vm.logMsg('Exception setOneProcessParameter : ' + e);
      }
      
  };
  
  this.getProcessDefinitionRecord = function getProcessDefinitionRecord(data, process_name) {
      var process_def=null;
      
      try {
            if ((data != null) && Array.isArray(data) && (data.length > 0)) {
                // Check the process name matches exactly as the search is not an exact search
                for (var i=0; (i<data.length) && (process_def ==null); i++) {
                    var one_process_def = data[i];
                    if (one_process_def.name === process_name) {
                        process_def = one_process_def;
                        break;
                    }
                }
            } 
      } catch(e) {
          vm.logMsg('Exception getProcessDefinitionRecord :' + e);
      }
      
      return process_def;
  };
  
  this.getProcessDefinition = function getProcessDefinition(one_process, parameter_name, do_next_callback, action_index, process_index) {
      try {
        var url = '../API/bpm/process?s=' + one_process + '&o=deploymentDate%20DESC&f=activationState=ENABLED';
        vm.busy = true;
        vm.logMsg('Action ' + action_index + ' - Getting process definition for process ' + one_process + ' with URL: ' + url);
    
        var req = {
          method: 'GET',
          url: url
        };
        
        var error_msg = 'Erreur : Process ' + one_process + ' non trouvé';
        return $http(req)
          .success(function(data, status) {
            var process_def = vm.getProcessDefinitionRecord(data, one_process);
            if (process_def !== null) {
                // Check the process name matches exactly as the search is not an exact search
                var process_id = process_def.id;
                vm.setOneProcessParameter(process_id, parameter_name, process_def.name, do_next_callback, action_index, process_index);
            } else {
                $scope.properties.dataFromError = error_msg;
            }
          })
          .error(function(data, status) {
            $scope.properties.dataFromError = error_msg + ' (statut ' + status + ')';
            notifyParentFrame({ message: 'error', status: status, dataFromError: data  });
          })
          .finally(function() {
            vm.busy = false;
          });

      } catch(e) {
          vm.logMsg('Exception getProcessDefinition : ' + e);
      }
      
  };
  
  this.setProcessParameter = function setProcessParameter(action_index, do_next_callback, process_index) {
      try {
         var parameter_name = $scope.properties.parameterName;
         var processes_list = $scope.properties.processNamesList;
         
         if ((parameter_name != null) && (processes_list != null)) {
            vm.logMsg('Setting parameter ' + parameter_name + ' on ' + processes_list.length + ' processes - process n°' + process_index);

            if (process_index < processes_list.length) {
                var one_process = processes_list[process_index];
                vm.getProcessDefinition(one_process, parameter_name, do_next_callback, action_index, process_index);
                
            } else {
                // No more process, do next action
                var success_msg = processes_list.length + ' processus mise à jour';
                vm.logMsg('Parameter ' + parameter_name + ' has been set on all ' + processes_list.length + ' processes set');
                vm.updateDataFromSuccess(action_index, success_msg, false);
                vm.doNextAction(do_next_callback, action_index);
            }
         }
         
      } catch(e) {
           vm.logMsg('Exception setProcessParameter : ' + e);
      }
  };
  
  function retrieveHumanTask(action_index, do_next_callback, retryCount) {
    
    var delay_between_retry_ms = 1000;
    var max_retries = Math.ceil(($scope.properties.maxDelayForAction * 1000) / delay_between_retry_ms);

    var rootCaseId = $scope.properties.taskRootCaseId;
    if (rootCaseId == null) {
        // Missing case id
        retryCount = retryCount + 1;
        vm.logMsg('Action ' + action_index + ', attempt ' + retryCount + ' - Missing rootCaseId to search human task');
        if (retryCount <= max_retries) {
            $timeout(function() {retrieveHumanTask(action_index, do_next_callback, retryCount);}, delay_between_retry_ms);
        } else {
            vm.logMsg('Action ' + action_index + ', attempt ' + retryCount+ ' - Failed to get human task with no caseId, no more retry');
            $scope.properties.dataFromError = 'Cas non trouvé pour la nouvelle proposition';
            notifyParentFrame({ message: 'error', status: status, dataFromError: $scope.properties.dataFromError  });
        }
        
    } else {
        // Try to retrieve task for case id
        var url = '../API/bpm/humanTask';
        var params = {'p': 0, 'c': 1, 'f': 'rootCaseId=' + rootCaseId};
        vm.logMsg('Action ' + action_index + ', attempt ' + retryCount + ' - Searching human task for root case id : ' + rootCaseId);
        var msg_on_error = 'Tâche non trouvée pour le cas ' + rootCaseId;
        retryRequestUntilMaxDelay(action_index, do_next_callback, retryCount, delay_between_retry_ms, max_retries, url, params, msg_on_error, true);
    }
      
  }
  
  function retrieveTaskContext(action_index, do_next_callback, retryCount) {
    var delay_between_retry_ms = 1000;
    var max_retries = Math.ceil(($scope.properties.maxDelayForAction * 1000) / delay_between_retry_ms);

    var taskId = $scope.properties.taskId;
    if (taskId === null) {
        // Missing task id
        retryCount = retryCount + 1;
        vm.logMsg('Action ' + action_index + ', attempt ' + retryCount + ' - Missing taskId to search task context');
        if (retryCount <= max_retries) {
            $timeout(function() {retrieveTaskContext(action_index, do_next_callback, retryCount);}, delay_between_retry_ms);
        } else {
            vm.logMsg('Action ' + action_index + ', attempt ' + retryCount+ ' - Failed to get task context with no taskId, no more retry');
            $scope.properties.dataFromError = 'Tâche non trouvée';
            notifyParentFrame({ message: 'error', status: status, dataFromError: $scope.properties.dataFromError  });
        }
        
    } else {
        // Try to retrieve context for task id
        var url = '../API/bpm/userTask/' + taskId + '/context';
        var params = {};
        vm.logMsg('Action ' + action_index + ', attempt ' + retryCount + ' - Searching task context for task id : ' + taskId);
        var msg_on_error = 'Tâche ' + taskId + ' non trouvée';
        retryRequestUntilMaxDelay(action_index, do_next_callback, retryCount, delay_between_retry_ms, max_retries, url, params, msg_on_error, false);
    }
      
  }

  function retryRequestUntilMaxDelay(action_index, do_next_callback, retryCount, delay_between_retry_ms, max_retries, url, params, msg_on_error, get_first_row_only) {
    vm.busy = true;
    
    retryCount = retryCount + 1;
    vm.logMsg('Action ' + action_index + ', attempt ' + retryCount + ' - Retrying request with url ' + url);
    var max_nb_retries = ($scope.properties.maxRetryCount != null) ? $scope.properties.maxRetryCount : 30;
    var delay_between_retry_ms = 1000;
    
    var req = {
          method: 'GET',
          url: url,
          params: params};
    
    return $http(req)
          .success(function(data, status) {
            if ((data === null) || (data.length === 0) || (data == '')) {
                // Empty result
                vm.logMsg('Action ' + action_index + ', attempt ' + retryCount+ ' - Empty result for request, retrying following status=' + status);
                if (retryCount < max_nb_retries) {
                    $timeout(function() {retryRequestUntilMaxDelay(action_index, do_next_callback, retryCount, delay_between_retry_ms, max_retries, url, params, msg_on_error, get_first_row_only);}, delay_between_retry_ms);
                } else {
                    vm.logMsg('Action ' + action_index + ', attempt ' + retryCount+ ' - Failed to get result for request, no more retry following status=' + status);
                    $scope.properties.dataFromError = msg_on_error;
                    notifyParentFrame({ message: 'error', status: status, dataFromError: $scope.properties.dataFromError  });
                }
                
            }  else {
                vm.logMsg('Action ' + action_index + ', attempt ' + retryCount+ ' - Got result for request');
                vm.updateDataFromSuccess(action_index, data, get_first_row_only);
                notifyParentFrame({ message: 'success', status: status, dataFromSuccess: data[0] });
                if ($scope.properties.targetUrlOnSuccess && method !== 'GET') {
                  $window.location.assign($scope.properties.targetUrlOnSuccess);
                }
                vm.doNextAction(do_next_callback, action_index);
            }
          })
          .error(function(data, status) {
            if (retryCount < max_nb_retries) {
                vm.logMsg('Action ' + action_index + ', attempt ' + retryCount+ ' - Failed to get result, retrying following status=' + status);
                $timeout(function() {retryRequestUntilMaxDelay(action_index, do_next_callback, retryCount, delay_between_retry_ms, max_retries, url, params, msg_on_error, get_first_row_only);}, delay_between_retry_ms);
                
            } else {
                vm.logMsg('Action ' + action_index + ', attempt ' + retryCount+ ' - Failed to get result, no more retry following status=' + status);
                $scope.properties.dataFromError = data;
                notifyParentFrame({ message: 'error', status: status, dataFromError: data  });
            }
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
