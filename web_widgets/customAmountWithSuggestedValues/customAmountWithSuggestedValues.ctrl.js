/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 * 
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 * 
 * You can leave the controller empty if you do not need it.
 */
function ($scope, $parse, $log, widgetNameFactory, $timeout, $window, $element, $http) {
    
    var ctrl = this;
    this.name = widgetNameFactory.getName('pbInput');
    
    // Select box data
    this.selectName = widgetNameFactory.getName('pbSelect');
    this.selectValue = null;
    this.suggestedValues = [];
    this.loading = true;
    this.showSelect = false;
    
    // calculate width
    this.fieldWidth = ~~((12 - ((!$scope.properties.labelHidden && ($scope.properties.labelPosition === 'left')) ? $scope.properties.labelWidth : 0))/2);

    // Web service loader data
    this.showLoder = false;
    this.showError = false;
    this.showEmpty = false;
    this.showDefault = false;
    this.errorMsg = '';
    this.emptyMsg = '';
    this.webServiceParameters = [];
    this.displaySuggestedValues = $scope.properties.showSuggestedValues;
    
    // Set default value only once
    this.defaultValueAlreadySet = false;
    
	this.logMsg = function logMsg(msg) {
		$log.debug("AmountWithSuggestedValues: " + msg);
	};
	
	this.logError = function logError(msg) {
		$log.error("AmountWithSuggestedValues: " + msg);
	};
	
    // *** Handle Web service call loader ***
    this.buildParameters = function buildParameters(collection_params) {
        var map_params = {};
        var nb_params = (collection_params == null) ? 0 : collection_params.length;
        
        for (var i=0; i<nb_params; i++) {
            var one_param = collection_params[i];
            for (var [key, value] of Object.entries(one_param)) {
                ctrl.logMsg('buildParameters: Adding parameter ' + key);
                if (value != null) {
                    map_params[key] = value;
                } else {
                    ctrl.logError("buildParameters: missing value for parameter " + key);
                }
            }
        }
        return map_params;
    };

    this.showSuggestedValues = function showSuggestedValues(resp_data) {
        if ((typeof resp_data.errorMsg !== 'undefined') && (resp_data.errorMsg !== null) && (resp_data.errorMsg.length>0)) {
            // Show error
            ctrl.errorMsg = $scope.properties.errorText + ' - ' + resp_data.errorMsg;
            ctrl.showError = true;
            ctrl.showSelect = false;
            ctrl.showEmpty = false;
            
        } else if ((typeof resp_data.suggestedValues !== 'undefined') && (resp_data.suggestedValues !== null) && (Array.isArray(resp_data.suggestedValues))) {
            if (resp_data.suggestedValues.length === 0) {
                // No suggestions
                ctrl.showSelect = false;
                ctrl.showError = false;
                ctrl.showEmpty = $scope.properties.showMsgWhenEmpty;
                ctrl.emptyMsg = $scope.properties.emptyMsgText;
                
            } else {
                // Display suggestions
                ctrl.suggestedValues = resp_data.suggestedValues;
                if ($scope.properties.manualSelectionValue != null) {
                    ctrl.suggestedValues.unshift($scope.properties.manualSelectionValue);
                }
                ctrl.showSelect = true;
                ctrl.showEmpty = false;
                ctrl.showError = false;
            }
        } else {
            // Empty response - considered as error as it should not occur
            ctrl.errorMsg = $scope.properties.errorText + "(réponse vide)";
            ctrl.showError = true;
            ctrl.showSelect = false;
            ctrl.showEmpty = false;
        }
    };
    
    this.hideSuggestions = function hideSuggestions() {
        ctrl.showSelect = false;
        ctrl.showError = false;
        ctrl.showEmpty = false;
        ctrl.showLoder = false;
		ctrl.suggestedValues = []; 
    };
    
    function loadSuggestedValues() {
        ctrl.displaySuggestedValues = $scope.properties.showSuggestedValues;
        
        if (! $scope.properties.showSuggestedValues) {
            ctrl.logMsg("loadSuggestedValues: Do not load suggested values");
            ctrl.hideSuggestions();
            
        } else if ((typeof $scope.properties.apiUrl === 'undefined')
                    || ($scope.properties.apiUrl === null)) {
            ctrl.logMsg("loadSuggestedValues: API URL not set yet");
            ctrl.hideSuggestions();
            
        } else {
			ctrl.logMsg("loadSuggestedValues: loading suggested values");
			ctrl.showLoder = true;
			ctrl.showError = false;   
			ctrl.showEmpty = false;
			ctrl.showDefault = false;

			// Save current value of input parameters
			ctrl.webServiceParameters = $scope.properties.suggestionParameters;
			
			// Empty list of suggested values
			ctrl.suggestedValues = []; 
			ctrl.showSelect = false;
			var url_params = ctrl.buildParameters($scope.properties.suggestionParameters);
			$http({
			   url:  $scope.properties.apiUrl,
			   method: 'GET',
			   params: url_params
			})
			.then(function(resp) {
				ctrl.logMsg("Got response from url : " + $scope.properties.apiUrl);
				ctrl.showLoder = false;
				ctrl.showSuggestedValues(resp.data);
			}, 
			function(resp) { 
				$scope.properties.errorResponse = resp;
				
				ctrl.showLoder = false;
				ctrl.showEmpty = false;
				ctrl.showError = true;
				var details = '';
				if (resp) {
					if (typeof resp.status !== 'undefined') {
						details += ' - Status=' + resp.status;
					}
					if ((typeof resp.statusText !== 'undefined') && (resp.statusText != '')) {
						details += ' (' + resp.statusText + ')';
					}
					if ((typeof resp.data !== 'undefined') && (resp.data != null) && (resp.data != '')) {
					    if (typeof resp.data.message !== 'undefined') {
					        details += ' : ' + resp.data.message;
					        
					    } else if (typeof resp.data.errorMsg !== 'undefined') {
					        details += ' : ' + resp.data.errorMsg;

					    } else {
						    details += ' : ' + angular.fromJson(resp.data);
					    }
					}
				}
				
				ctrl.errorMsg = $scope.properties.errorText + details;
				ctrl.logError("Error calling url :"+  $scope.properties.apiUrl + details);

			});
		}
    }
    
    
    $scope.$watch('properties.apiUrl', loadSuggestedValues, true);
    $scope.$watch('properties.showSuggestedValues', function(new_value) {
        if (ctrl.displaySuggestedValues != $scope.properties.showSuggestedValues) {
            loadSuggestedValues();
        }
        
    }, true);
     
    $scope.$watchCollection('properties.suggestionParameters', function(items) {
        if (Array.isArray(items)) {
          if (! angular.equals(ctrl.webServiceParameters, $scope.properties.suggestionParameters)) {
              // Web service parameters have changed, need to call web service again
              loadSuggestedValues();
          }
        }
      });
      
    loadSuggestedValues();

    // *** Handle French Number Formatinput field  ***
    $scope.showActualVal = function()
    {
        if(!$scope.properties.readOnly && $scope.properties.valueToSave !== null && $scope.properties.valueToSave != 'undefined' && typeof $scope.properties.valueToSave != 'undefined')
        {
            
            var strVal = $scope.properties.valueToSave+"";
            
            if(strVal.indexOf(".") == -1)
            {
                $scope.properties.value = strVal;
            }
            else{
                
                $scope.properties.value = strVal.replace(".",",");
            }
        }
    };
    
     $scope.validateAmount = function()
    {
        var fieldVal = $scope.properties.value;
        
        // CFR 24/11/2022 - Lot4 Sprint3 UAT corrections : remove euro if present
        if(fieldVal.indexOf("€") != -1)
        {
            fieldVal = fieldVal.replace("€", "").trim();
        }
        
        if(fieldVal.indexOf(",") != -1)
        {
            fieldVal = fieldVal.toString().replace(",",".");
        }
        
        var isNum = /^\d*\.?\d+$/.test(fieldVal);
        
        $scope.properties.invalidNum = !isNum;
    };
    
    this.formatCurrencyAmount = function formatCurrencyAmount(amount) {
        var displayed_value = "";
        
        try {
            if (amount != null) {
                displayed_value = "" + amount;  // keep value in case we fail
                
                // CFR 24/11/2022 - Remove € in case it is there
                var stripped_value = displayed_value.replace("€", "").trim();
                
                var num = Number(stripped_value.replace(",","."));
                
                // CFR 24/11/2022 avoid showing NAN value
                if (isNaN(num)) {
                    $scope.properties.invalidNum = true;
                
                } else {
                    var numberRound = num.toFixed(2);
                    var currency = numberRound.toString().replace(".",",");
                    var space = currency.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
                    
                    displayed_value = space + " €";
                }
            }
        } catch(e) {
            ctrl.logError("Exception formatCurrencyAmount; " + e);
        }
        return displayed_value;
    };

    $scope.convertToFrenchFormat = function() {
       if (!$scope.properties.readOnly) {
            var fieldVal = $scope.properties.value;
            if (fieldVal != null) {
                
                $scope.properties.value = ctrl.formatCurrencyAmount(fieldVal);
                if ($scope.properties.value != "") {
                
                    // CFR 24/11/2022 - Remove € in case it is there
                    var displayed_value = "" + fieldVal;
                    var stripped_value = displayed_value.replace("€", "").trim();
                    var num = Number(stripped_value.replace(",","."));
                    if (! isNaN(num)) {
                        $scope.properties.valueToSave = num;
                    }
                }
            }
       }
    };

    this.setAmountValue = function setAmountValue(new_value) {
        $scope.properties.valueToSave = new_value;
        $scope.properties.value = "" + new_value;
        $scope.convertToFrenchFormat();
        ctrl.logMsg("Amount=" + new_value + ", display=" + $scope.properties.value);
    };
    
    // Initialize display
    if((typeof $scope.properties.valueToSave !== 'undefined') && ($scope.properties.valueToSave !== null) && ($scope.properties.valueToSave != 'undefined')) {
            $scope.properties.value = ctrl.formatCurrencyAmount($scope.properties.valueToSave);
    }

    // *** Handle select box  ***
    function comparator(initialValue, item) {
        return angular.equals(initialValue, ctrl.getValue(item));
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
    
    this.findSelectedItem = function (items) {
        return items.filter(comparator.bind(null, ctrl.selectValue))
          .map(function (item) {
            return ctrl.getValue(item);
          })[0];
      };
    
      this.setSelectedValue = function (foundItem) {
        $timeout(function () {
            ctrl.selectValue = angular.isDefined(foundItem) ? foundItem : null ;
        }, 50);
      };
      
      this.updateWithSelectedValue = function updateWithSelectedValue() {
        try {
            ctrl.logMsg("updateWithSelectedValue: Selected suggsted value: " + ctrl.selectValue);
            if (($scope.properties.manualSelectionValue != null)
                && (angular.equals(ctrl.selectValue, ctrl.getValue($scope.properties.manualSelectionValue)))) {
                ctrl.logMsg("updateWithSelectedValue: Manual selection, no update");
            } else {
                // Update budget amount value and display it
                ctrl.logMsg("updateWithSelectedValue: updating budget amount with value " + ctrl.selectValue);
                ctrl.setAmountValue(ctrl.selectValue);
            }
        } catch(e) {
            ctrl.logError("Exception updateWithSelectedValue: " + e);
        }
      };
    
      $scope.$watchCollection('ctrl.suggestedValues', function(items) {
        if (Array.isArray(items)) {
          var foundItem = ctrl.findSelectedItem(items);
    
          //force IE9 to rerender option list
          if ($window.navigator && $window.navigator.userAgent && $window.navigator.userAgent.indexOf('MSIE 9') >= 0) {
            var option = document.createElement('option');
            var select = $element.find('select')[0];
            select.add(option,null);
            select.remove(select.options.length-1);
          }
    
          // terrible hack to force the select ui to show the correct options
          // so we change it's value to undefined and then delay to the correct value
          ctrl.selectValue = undefined;
          ctrl.setSelectedValue(foundItem);
        }
      });
    
      $scope.$watch('ctrl.selectValue', function(value) {
        if (angular.isDefined(value) && value !== null) {
          var items = ctrl.suggestedValues;
          if (Array.isArray(items)) {
            var foundItem = ctrl.findSelectedItem(items);
            ctrl.setSelectedValue(foundItem);
          }
        }
      });
      
      this.setDefaultValue = function setDefaultValue(value) {
        try {
            // Check if we have a value
            if (angular.isDefined(value) && value !== null) {
                // Check if we need to set a default value
                if ($scope.properties.haveDefaultVal === true) {
                    // Display info message with the value
                    if (($scope.properties.defaultValueMsgText != null) && ($scope.properties.defaultValueMsgText !== "")) {
                        ctrl.emptyMsg = $scope.properties.defaultValueMsgText + " : " + value;
                        ctrl.showDefault = true;
                    }
                    
                    // Check if there is already a value
                    if ( (! ctrl.defaultValueAlreadySet)
                          && (($scope.properties.value == null) || ($scope.properties.value === ""))) {
                            // Set default value
                            ctrl.logMsg("Set default value=" + value);
                            ctrl.setAmountValue(value);
                            
                            // Set default only once
                            ctrl.defaultValueAlreadySet = true;
                    }                    
                } else {
                    ctrl.showDefault = false;
                }
            } else {
                ctrl.showDefault = false;
            }

        } catch(e) {
            ctrl.logError("Exception setDefaultValue; " + e);
        } 
      };
      
      $scope.$watch('properties.defaultValue', function(value) {
           ctrl.setDefaultValue(value);
      });

      $scope.$watch('properties.haveDefaultVal', function(have_default) {
           if (have_default === true) {
                ctrl.setDefaultValue($scope.properties.defaultValue);
           }
      });
    
}