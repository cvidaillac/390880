function($scope, $interval, $window, $http, $modal) {
  var ctrl = this;

  // Hide the message to notify the user that we searching for next task
  $scope.display = "display:none";

  // Is it the first time the value of "submit button successful response value" change?
  // We need to ignore the first change
  ctrl.firstTime = true;

  // Current user id
  var userId;

  // Current case id
  var caseId;
  
  // Progress message case id
  var progressCaseId;
  var showProgessMessage=$scope.properties.showProgress;
  var progressMessageVariable=$scope.properties.showProgressProcessVariable;
  var retrieveProgressInChildProcess = $scope.properties.progressVariableInChildProcess;

  // Get current user id by performing a call to /API/system/session
  $http.get('/' + $scope.properties.bonitaWebAppName + '/API/system/session/1').
  success(function(data, status, header, config) {
    // Get the user id from REST API call answer
    userId = data.user_id;
  }).
  error(function(data, status, headers, config) {
    console.error("Fail to get user id");
    $scope.redirectFallback($scope.properties.fallbackURL);
    return;
  });

  // If task id is provided use it to get case id
  if($scope.properties.taskId) {
    // Get the current case id using the provide task id
    // MOD-CFR-13/03/2017: Use HumanTask instead of flowNode to avoid REST API authorisation issues
    // $http.get('/' + $scope.properties.bonitaWebAppName + '/API/bpm/flowNode/' + $scope.properties.taskId).
    $http.get('/' + $scope.properties.bonitaWebAppName + '/API/bpm/humanTask/' + $scope.properties.taskId).
    success(function(data, status, header, config) {
      // Get the user id from REST API call answer
      caseId = data.caseId;
    }).
    error(function(data, status, headers, config) {
      console.error("Fail to get case id");
      $scope.redirectFallback($scope.properties.fallbackURL);
      return;
    });
  }

  // We watch the variable to trigger code execution when we get a successful response from the submit task REST call
  $scope.$watch('properties.submitButtonSuccessfulResponseValue', function() {

    // Ignore the first modification of the watched variable
    if ((!ctrl.firstTime) && ($scope.properties.submitButtonSuccessfulResponseValue !== null)) {
        
      // Check if cancel button has been used
      console.debug ("Submit Response: " + $scope.properties.submitButtonSuccessfulResponseValue);
      if ($scope.properties.submitButtonSuccessfulResponseValue == "CANCEL") {
        // Manage cancel of process start
        console.debug("Canceling action");
        $scope.redirectFallback("");
        
      } else {
        
          // Display the message to the user while we searching for next task
          $scope.display = "";
          $scope.warningModal();
    
          // If we don't alrady get case id using task id we should be on instantiation form and so we can find case id in reponse value
          if(!caseId) {
            caseId = $scope.properties.submitButtonSuccessfulResponseValue.caseId;
          }
      
          // Try to find next task for current user until we reach the configured numberOfRetries
          // Wait "timeToWait" between each try
          var counter = 0, interval = $interval(function() {
    
              // Increment number of tries
              counter++;
    
              if ( $scope.properties.logoutAndCloseWindow === true) {
                  // Cancel the periodic execution of the code to search for next task
                  $interval.cancel(interval);
                  
                // Logout and close browser window immediately
                $scope.logoutAndClose($http, $window);
                
              } else if ( $scope.properties.noMoreTask === true) {
                // Redirect immediately to fallbackURL if no more task to display
                  console.debug("Flag noMoreTask set to true");
                  // Cancel the periodic execution of the code to search for next task
                  $interval.cancel(interval);
                
                  $scope.redirectFallback($scope.properties.fallbackURL);
              } else {
                  console.debug("Flag noMoreTask set to false");
          
                // If we reach the maximum number of retries
                if (counter > $scope.properties.numberOfRetries) {
    
                    // Cancel the periodic execution of the code to search for next task
                    $interval.cancel(interval);
    
                    // Redirect user to Portal
                    $scope.redirectFallback($scope.properties.fallbackURL);
                } else {
    
                    // Call API to get pending task (limit result to one task) for current user in current case
                    $http.get('/' + $scope.properties.bonitaWebAppName + '/API/bpm/humanTask?c=1&p=0&f=state=ready&f=user_id=' + userId + '&f=caseId=' + caseId).
                    success(function(data, status, headers, config) {
    
                        // If we found one task for current user
                        if (data[0]) {
    
                            // Assign  next task to current user
                            var task_id = data[0].id;
                            console.log("Assigning task " + task_id + " to userid " + userId);
                            $http.put('/' + $scope.properties.bonitaWebAppName + '/API/bpm/humanTask/' + task_id, {
                                assigned_id: userId
                            })
                            .then(function(response){
                                // Success callback Redirect user to task form
                                var target_task_url='/' + $scope.properties.bonitaWebAppName + '/portal/form/taskInstance/' + data[0].id;
                                console.log("Showing next task: " + target_task_url);
                                $window.location.href = target_task_url;
                                
                            }, function(response){
                                // Failed to assign task
                                console.log("Failed to assign the task " + task_id + " : status=" + response.status + " : " + response.statusText);

                                var target_task_url='/' + $scope.properties.bonitaWebAppName + '/portal/form/taskInstance/' + data[0].id;
                                console.log("Showing next task: " + target_task_url);
                                $window.location.href = target_task_url;
                                
                            });
    
    
                        } else {
                            // Not finished, retrieving progress message
                            if (showProgessMessage) {
                                if (!progressCaseId) {
                                    if (retrieveProgressInChildProcess) {
                                        // Retrive case id of child process
                                        $http.get('/' + $scope.properties.bonitaWebAppName + '/API/bpm/activity?p=0&c=10&f=rootCaseId=' + caseId + '&o=parentCaseId%20DESC').
                                        success(function(data, status, headers, config) {
                                            // If we found one task
                                            if (data[0]) {
                                                var nb_activities=data.length;
                                                for (var i=0; i<nb_activities; i++) {
                                                    if (data[i].parentCaseId != caseId) {
                                                        // Get the first child case
                                                       progressCaseId = data[i].parentCaseId;
                                                    }
                                                }
                                            }
                                    
                                        }).
                                        error(function(data, status, headers, config) {
                                            console.debug("Fail to get child case for progress message");
                                        });
                                    } else {
                                        progressCaseId=caseId;
                                    }
                                }
                                if (progressCaseId) {
                                    // Retrieve progress message
                                    $http.get('/' + $scope.properties.bonitaWebAppName + '/API/bpm/caseVariable/' + progressCaseId + '/' + progressMessageVariable).
                                            success(function(data, status, headers, config) {
                                                // If we found one task
                                                if (data) {
                                                    if (data.value.length > 0) {
                                                        console.debug("Progress message is : [" + data.value + "]");
                                                        $scope.progressMessage=data.value;
                                                    } else {
                                                        console.debug("Progress message is empty");
                                                    }
                                                } else {
                                                    console.debug("No progress message found");
                                                }
                                            }).
                                            error(function(data, status, headers, config) {
                                                console.debug("Fail to get progress message from variable " + progressMessageVariable + ' of case ' + progressCaseId);
                                            });
                                }
                            }
                        }
                    }).
                    error(function(data, status, headers, config) {
                        console.debug("Fail to get next task");
                        $scope.redirectFallback($scope.properties.fallbackURL);
                    });
                }
              }
            }, $scope.properties.timeToWait);
      }
    } else {
      // This is the first time submitButtonSuccessfulResponseValue changed
      ctrl.firstTime = false;
    }
  });

  // Display the modal window to the user to notify that we search for next task
  $scope.warningModal = function() {

    var html = "<div class=' panel-body alert alert-success' style='margin-bottom:0;'>" + "<div class='col-md-4 col-md-offset-4 ' style='top: 50%;'>" + "<H5 style='text-align:center'><i class='fa fa-spinner fa-spin'></i>&nbsp;" + $scope.properties.messageDisplayed + "<div ng-bind-html='progressMessage'></div></H5></div>" + "</div>";

    var local_scope = $scope.$new();
    local_scope.params = {progressMessage: $scope.progressMessage};
    
    $scope.myModal = $modal.open({
      template: html,
      backdrop: 'static',
      scope: local_scope
    });
  };

  // If we cannot find any task for current user or if searching for task failed redirect user to Portal
  $scope.redirectFallback = function (target_url) {
      try {
          if ($scope.properties.closeDialogWindow) {
              if (typeof $scope.$parent.closeThisDialog === 'function') {
                console.debug("Closing Dialog window");
                $scope.$parent.closeThisDialog();
              } else {
                  // Get parent scope if we are within an iframe
                  var parentScope = $window.parent.angular.element($window.frameElement).scope();
                  if (parentScope == null) {
                      console.debug("No Dialog and no parent iframe found");
                  } else {
                      if (typeof parentScope.closeThisDialog === 'function') {
                        console.debug("Closing Dialog window from child frame");
                        parentScope.closeThisDialog();
                      } else if (typeof parentScope.$parent.closeThisDialog === 'function') {
                        console.debug("Closing Dialog window from parent scope retrieved from child frame");
                        parentScope.$parent.closeThisDialog();                          
                      } else {
                        console.debug("No Dialog window to close");
                      }
                  }
              }
          }
      } catch(e) {
          console.debug("Exception trying to close window : " + e.toString());
      }
      
    // Redirect to Bonita Portal as we didn't find task for current user
    // WAS: $window.top.location.href = '/' + $scope.properties.bonitaWebAppName;
    if ((target_url !== null) && (target_url !== "")) {
		// Check if we redirect current window or top window
		if ($scope.properties.closeDialogWindow || (! $scope.properties.noMoreTask)){
			console.debug("Redirecting parent window to : " + target_url);
			$window.top.location.href = target_url;
		} else {
			console.debug("Redirecting current window to : " + target_url);
			$window.location.href = target_url;		
		}
    } else {
        console.debug("Canceling action, no rediction");
    }
  };
  
  $scope.closeModalMessage = function () {
      try {
          if ($scope.myModal) {
              console.log("Closing dialog message");
              $scope.myModal.close();
          }
      } catch(e) {
        console.debug("Exception trying to close dialog : " + e.toString());
    }
  };
  
  $scope.closeWindow = function ($window) {
    try {
        // Closing modal dialog
        $scope.closeModalMessage();
        
        // Check if window was opened by an opener
        if ($window.opener === null) {
            var redirect_url = $scope.properties.fallbackURL;
            if ((redirect_url == null) || (redirect_url == "")) {
                redirect_url = '/' + $scope.properties.bonitaWebAppName + '/login.jsp';
            }
            console.log("No existing opener for window, redirecting to : " + redirect_url);
            $window.location.href=redirect_url;
        } else {
            console.log("Closing window");
            $window.close();
        }
    } catch(e) {
        console.debug("Exception trying to close window : " + e.toString());
        console.log("Redirecting to blanck page...");
        $window.location.href="about:blank";
    }
  };
  
  $scope.logoutAndClose = function ($http, $window) {
      try {
        // Send logout request
        var logout_request='/' + $scope.properties.bonitaWebAppName + '/logoutservice';
        $http.get(logout_request)
            .then(function(response){
                // Success logout : close window
                console.log("Logout successful, closing window");
                $scope.closeWindow($window);
                                
            }, function(response){
                // Failed to logout, clowe window anyway
                console.log("Failed to logout, status=" + response.status + " : " + response.statusText);
                console.log("Closing window");
                $window.close();

            });
      
          
      } catch(e) {
          console.debug("Exception trying to logout and close window : " + e.toString());
      }
  };
}
