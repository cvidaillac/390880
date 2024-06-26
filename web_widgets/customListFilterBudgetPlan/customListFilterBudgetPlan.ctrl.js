function listFilters($scope, $log) {
    $scope.properties.urlGenerate = "";
    
	$scope.searchBufferEmpty = {}
    // The element that store the actual inputs 
	$scope.searchBuffer = {}
    // The element that store the final inputs hat are used to filter the table 
	$scope.finalSearch = {}
	
	// Class to show filters on start
	$scope.showFilterClass = ($scope.properties.showFilters === true) ? "show" : "";

    // Hide / show the collapse button
    this.showHideButton = ($scope.properties.showButtonHide == true) ? true : false;
    this.isCollapsed = ($scope.properties.showFilters == true) ? false : true;
    
    // Label for checkboxes
    this.chekboxLabel = ($scope.properties.filtersCheckbox) ? $scope.properties.filtersCheckbox.label + " :" : "";
    
    // Summary of filters
    this.checkboxSummaryText = "";
    this.filtersSummaryText = "";

    // Separator for multi-select values - avoid possible characters within multi select options
    this.summaryMultipleValuesSeparator = ' | ';
	
	this.restapiMultipleValuesSeparator = ',';

    // Disabled buttons when query is running
    this.isRunning = false;
    
    // Store multiSelect index for each selected value
    this.multiSelectIndexes = [];
    
    // Set minimum year start value
    this.minYearStart = new Date().getFullYear();

    // Initialize start year
    $scope.properties.yearStart = this.minYearStart;
    $scope.properties.yearEnd = $scope.properties.yearStart + 1;

    // Mask for all checkbox values
    this.checkboxValues = 0;
    var ctrl = this;
    
    this.initFilters = function initFilters() {
        // Initialize checkboxes
        if ($scope.properties.filtersCheckbox) {
            var nb_options = $scope.properties.filtersCheckbox.choices.length;
            $log.debug("initFilters: nb checboxes = " + nb_options);
            for (var i=0; i<nb_options ; i++) {
                var one_option = $scope.properties.filtersCheckbox.choices[i];
                // Keep existing value if already set
                if ((typeof one_option.value === "undefined") || (one_option.value == null)) {
                    $scope.searchBuffer[one_option.data] = one_option.defaultValue;
                    one_option.value = (one_option.defaultValue == one_option.checkedValue) ? true : false;
                    $log.debug("initFilters: " + one_option.data + "=" + one_option.defaultValue);
                }
                // Update checkbox mask
                ctrl.updateCheckbox(one_option, i);
            }
        }
        
        // Initialize radio buttons
        if ($scope.properties.filtersLabels) {
            for (var one_filter of $scope.properties.filtersLabels) {
                if (typeof one_filter.defaultValue !== 'undefined') {
                    $scope.searchBuffer[one_filter.data] = one_filter.defaultValue;
                }
            }
        }
    };
    
    this.getHideButtonLabel = function getHideButtonLabel() {
        var hide_label = (ctrl.isCollapsed) ? "Afficher" : "Masquer";  
        $log.debug("isCollapsed=" + ctrl.isCollapsed + ", filter button: " + hide_label + ", show button=" + ctrl.showHideButton);
        
        return hide_label;
    };
    
    this.getMultiSelectAttribute = function(options_values, attribute_name, separator, quote_char='') {
        var selected_values='';
        try {
            if (options_values) {
                for (var one_selected_index of this.multiSelectIndexes) {
                    if (one_selected_index < options_values.length) {
                        var one_selected = options_values[one_selected_index];
                        if (selected_values) {
                            selected_values += separator;
                        }
                        selected_values += quote_char + one_selected[attribute_name] + quote_char;
                    } else {
                        $log.error("Exception getMultiSelectAttribute: invalid index " + one_selected_index);
                    }
                } 
            } else {
                $log.error("Exception getMultiSelectAttribute: no option_values !");
            }
        } catch(e) {
            $log.error("Exception getMultiSelectAttribute: " + e);
        }
        return selected_values;
    };

    this.getMultiSelectArray = function(options_values, attribute_name) {
        var selected_array = [];

        try {
            if (options_values) {
                for (var one_selected_index of this.multiSelectIndexes) {
                    if (one_selected_index < options_values.length) {
                        var one_selected = options_values[one_selected_index];
                        selected_array.push(one_selected[attribute_name]);
                    }
                }
            }
        } catch(e) {
            $log.error("Exception getMultiSelectArray: " + e);
        }
        return selected_array;
    };
        
    this.setFiltersSummary = function() {
        try {
            var summary = "";
            var nb_filters = $scope.properties.filtersLabels.length;
            for (var i=0; i<nb_filters ; i++) {
                var one_filter = $scope.properties.filtersLabels[i];
                var one_value='';
                if (one_filter.type === 'selectMulti') {
                    if (this.multiSelectIndexes) {
                        one_value = this.getMultiSelectAttribute(one_filter.values, 'label', this.summaryMultipleValuesSeparator);
                    }
                } else {
                    one_value = $scope.searchBuffer[one_filter.data];
                }
                if (one_value) {
                    if (summary != "") {
                        summary += ", ";
                    }
                    summary += one_filter.label + ": " + one_value;        
                }
            }
            if (summary) {
                ctrl.filtersSummaryText = "Filtres : " + summary;
            } else {
                ctrl.filtersSummaryText = "";
            }
        } catch(e) {
            $log.error("Exception setFiltersSummary: " + e);
        }   
    };
    
    // CFR 18/05/2022 -BPM-814 : Get values from properties.multipleSelect for selectMulti type
    this.getMultipleSelectValues = function() {
        try {
            if (this.multiSelectIndexes) {
                // We have a multi select value, add it to searchBuffer filters
                var nb_filters = $scope.properties.filtersLabels.length;
                for (var i=0; i<nb_filters ; i++) {
                    var one_filter = $scope.properties.filtersLabels[i];
                    if (one_filter.type === 'selectMulti') {
                        $scope.searchBuffer[one_filter.data] = this.getMultiSelectAttribute(one_filter.values, 'value', this.restapiMultipleValuesSeparator, "'");
                        $scope.properties.multipleSelect = this.getMultiSelectArray(one_filter.values, 'value');
                    }
                }
            }
        } catch(e) {
            $log.error("Exception getMultipleSelectValues: " + e);
        }   
    }
    
    this.setCheckboxSummary = function() {
        try {
            var summary = "";
            var nb_options = $scope.properties.filtersCheckbox.choices.length;
            for (var i=0; i<nb_options ; i++) {
                var one_option = $scope.properties.filtersCheckbox.choices[i];
                if (one_option.value) {
                    if (summary != "") {
                        summary += ", ";
                    }
                    summary += one_option.label;        
                }
            }
            ctrl.checkboxSummaryText = this.chekboxLabel + summary + ' de ' + $scope.properties.yearStart + ' à ' + $scope.properties.yearEnd;
        } catch(e) {
            $log.error("Exception setCheckboxSummary: " + e);
        }
    };
    
    this.toggleCollapseButon = function toggleCollapseButon() {
        ctrl.isCollapsed = ! ctrl.isCollapsed;
        ctrl.hideButtonLabel = ctrl.getHideButtonLabel();
    };
    
    this.updateCheckbox = function updateCheckbox(option, index) {
          var option_value = option.value;
          var mask_value = Math.pow(2, index);
          
          // Check if several checkbox can be selected
          if ($scope.properties.allowOnlyOneCheckbox) {
              // Unchekc all other checkboxes
              ctrl.checkboxValues = 0;
              var nb_options = $scope.properties.filtersCheckbox.choices.length;
              for (var i=0; i<nb_options ; i++) {
                  if (i !== index) {
                    var one_option = $scope.properties.filtersCheckbox.choices[i];
                    one_option.value = false;
                    $scope.searchBuffer[one_option.data] = one_option.uncheckedValue;
                  }
              }
          }
          
          if (option_value || (option_value == option.checkedValue)) {
                ctrl.checkboxValues = ctrl.checkboxValues | mask_value;
                $scope.searchBuffer[option.data] = option.checkedValue;
          } else {
                ctrl.checkboxValues = ctrl.checkboxValues & (~mask_value);
                $scope.searchBuffer[option.data] = option.uncheckedValue;
          }
          $log.debug("Selected checkboxes with " + option.data + "=" + $scope.searchBuffer[option.data]  + ": " + ctrl.checkboxValues);
    };

    // Actions for the onclick button : Activate the filtering 
	this.startFilters = function startFilters() {
	     // Hide button so that we collapse the filter when query completed
	     ctrl.showHideButton = false;
	     
	     // Disable buttons while running
	     ctrl.isRunning = true;
	     
         // CFR 18/05/2022 -BPM-814 : Get values from properties.multipleSelect for selectMulti type
         ctrl.getMultipleSelectValues();
	     
         ctrl.generateCustomUrl();
         ctrl.setCheckboxSummary();
         ctrl.setFiltersSummary();
         if (typeof $scope.properties.triggerQuery !== "undefined") {
              $scope.properties.triggerQuery = "Yes";
        }
    };
    
    this.isMissingMandatoryFields = function isMissingMandatoryFields() {
        // Check at least one checkbox is selected
        var is_missing = (ctrl.checkboxValues==0);
        
        if (!is_missing) {
            // Check exercice is set for rachatLocFi
            if ($scope.properties.filtersCheckbox) {
                var nb_options = $scope.properties.filtersCheckbox.choices.length;
                for (var i=0; i<nb_options ; i++) {
                    var one_option = $scope.properties.filtersCheckbox.choices[i];
                    if ((one_option.value) && (one_option.relatedField != null) && (one_option.relatedField.length > 0)) {
                         var related_field = one_option.relatedField[0];
                         if (($scope.searchBuffer[related_field.data] == null) || ($scope.searchBuffer[related_field.data] == "")) {
                             is_missing = true;
                         } 
                    }
                }
            }
        }
        
        return is_missing;
    };

    // Actions for the onclick button : Reset the filtering
	this.resetFilters = function resetFilters() {
	    // Do not clear exercice value
	    var exercice_value = $scope.searchBuffer["exercice"];
	    
		angular.copy($scope.searchBufferEmpty, $scope.searchBuffer);
		ctrl.multiSelectIndexes = [];
		$scope.properties.multipleSelect = [];  //Added by Amit for BPM-40 to reset multiselect value 
		ctrl.initFilters();
		
	    // Do not clear exercice value
		$scope.searchBuffer["exercice"] = exercice_value;
 	};
 	
 	 this.resetValue = function resetValue(filter) {
        $scope.searchBuffer[filter.data] = "";
    };
    
    this.resetValueMulti = function resetValueMulti(filter) {
		ctrl.multiSelectIndexes = [];
        $scope.properties.multipleSelect = [];
    };

    this.generateCustomUrl = function generateCustomUrl() {
        $scope.properties.urlGenerate = "";
        var urlToGenerate = "";

        // Add search criteria
        angular.forEach($scope.searchBuffer, function(value, key) {
            
            if(value !== ""){
                
                var searchKey = "test";
                
                var searchKey = $scope.properties.filtersLabels.find(function(e) {
                    return e.data == key && e.searchKey;
                });
                
                // CFR 19/05/2022 BPM-814 : encode search value
                urlToGenerate += "&"+ key + "=" + encodeURIComponent(value);
                
            }
        })
        $scope.properties.urlGenerate = urlToGenerate;
        
        return urlToGenerate;
    };
    
    this.yearStartChanged = function(new_year_start) {
      try {
        // Check if we need to update year end
        if ((! $scope.properties.yearEnd) || (new_year_start > $scope.properties.yearEnd)) {
            $scope.properties.yearEnd = new_year_start;
        }
        
      }  catch(e) {
            $log.error("Exception yearStartChanged: " + e);
      }
    };

    // Initialize values
    this.hideButtonLabel = ctrl.getHideButtonLabel();
    ctrl.initFilters();
    
    $scope.$watch('properties.showButtonHide', function(value) {
        if ((value) && (! ctrl.showHideButton)) {
            // Hiding filters when list shown
            ctrl.isCollapsed = true;
        }
        if ((! value) && ctrl.isCollapsed) {
            // Do not stay collapsed if we hide the button
            ctrl.isCollapsed = false;
        }
	     ctrl.showHideButton = value;
	     ctrl.hideButtonLabel = ctrl.getHideButtonLabel();
        $log.debug("showHideButton=" + value);
	 });
	 
	 $scope.$watch('properties.triggerQuery', function(value) {
	     if (ctrl.isRunning && (value == "No")) {
	         $log.debug("Query is no longer running");
	         ctrl.isRunning = false;
	     }
     });    
}