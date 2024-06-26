function PbDataTableCtrl($scope, $http, $log, $filter) {

	var vm = this;
	this.allSelected = false;
	var idsSelected = [];
    this.selectedItems = [];
    this.pageOffset=0;
    this.nbSelected = 0;
    
		Object.defineProperty(vm, 'jsonData', {
			'get': function () {
				//undefined for filter expression allows to bypass the null value issue that
				//filters everything
				var data = $filter('filter')(this.data || [], $scope.properties.filter || undefined);
				if (vm.sortOptions.property === undefined || vm.sortOptions.direction === undefined) {
					return data;
				}
				return $filter('orderBy')(data, vm.sortOptions.property, vm.sortOptions.direction);
			},
			'set': function (data) {
				this.data = data;
			}
		});

	this.sortOptions = {
		property: ($scope.properties.sortColumns || [])[0],
		direction: false
	};

	this.pagination = {
		currentPage: 1,
		total: 0
	};
	this.displayCounter = "";

	/**
	 * helper methods
	 */
	this.hasMultiColumns = function () {
		return Array.isArray($scope.properties.columnsKey);
	};

	this.isSelectable = function () {
		return $scope.properties.isBound('selectedRow');
	};

	/**
	 * Create a request object following $http(request)
	 * @return {Object} a request Object
	 */
	this.createRequest = function () {
		var params = {
			c: $scope.properties.pageSize,
			p: this.pagination.currentPage - 1 || 0,
			s: $scope.properties.filter
		};

		if (this.sortOptions.property) {
			params.o = this.sortOptions.property + ' ' + (this.sortOptions.direction ? 'DESC' : 'ASC');
		}
		return {
			url: this.removeHandledParams($scope.properties.apiUrl),
			transformResponse: $http.defaults.transformResponse.concat(transformResponse),
			params: angular.extend({}, $scope.properties.params || {}, params)
		};
	};

	this.removeHandledParams = function (url) {
		return ['c', 'p'].reduce(
			function (acc, param) {
			//we cannot use pbDataTable because the widget build (probably mustache) replace it
			// with the widget name...
			return acc.replace(new RegExp('(&' + param + '=[^&#]*([&#])?)', 'g'), '$2').replace(new RegExp('[?]' + param + '=[^&#]*&?', 'g'), '?');
		},
			url || '');
	};

	/**
	 *  Fire request and update results and pagination
	 */
	this.updateResultsFromAPI = function () {
		if ($scope.properties.apiUrl) {
			$http(vm.createRequest())
			.then(function (response) {
				vm.results = response.data.results;
				vm.pagination = response.data.pagination;
			})
			.catch(function (error) {
				$log.error(error);
			});
		}
	};

	this.updateResultsFromJson = function () {
		var start = (vm.pagination.currentPage - 1) * $scope.properties.pageSize;
		var end = vm.pagination.currentPage * $scope.properties.pageSize;

        vm.pageOffset = start;
		vm.results = vm.jsonData.slice(start, end);
		if (vm.jsonData.length == 0) {
			vm.displayCounter = "0-0 / 0";
		} else {
		    var nb_records = vm.jsonData.length;
			var first_record = start+1;
			var last_record = (end<nb_records) ? end : nb_records;
			vm.displayCounter = first_record + "-" + last_record + " / " + nb_records;
		}
	};

	this.sortHandler = function () {
		this.updateResults();
	};

	this.paginationHandler = function () {
		this.updateResults();
	};

	this.selectRowHandler = function (row) {
		if (this.isSelectable()) {
			$scope.properties.selectedRow = row;
		}
	};

	this.isColumnSortable = function (index) {
		return $scope.properties.type === 'Variable' ||
		(angular.isArray($scope.properties.sortColumns) &&
			$scope.properties.sortColumns.indexOf($scope.properties.columnsKey[index]) > -1);
	};
	
  this.initializeSelectedItems = function() {
      if ((vm.jsonData !== null) && (vm.selectedItems.length < vm.jsonData.length)) {
        while(vm.jsonData.length > vm.selectedItems.length) {
            vm.selectedItems.push(vm.allSelected);
        }
        vm.nbSelected = (vm.allSelected) ? vm.selectedItems.length : 0;

        $log.debug('initializeSelectedItems: found ' + vm.jsonData.length + ' items, selectedItems.length=' + vm.selectedItems.length);
      }
  };

  this.updateSelectedValues = function () {
      vm.doUpdateValues();
      vm.checkAllSelected();
  };
  
  this.doUpdateValues = function () {
    var selected_values = vm.selectedItems
      .map(function (checked, index) {
        if (checked !== true) {
          return false;
        }
        var item = vm.jsonData[index];
        return item;
      }).filter(function (item) {
        return item !== false;
      });
    vm.nbSelected = selected_values.length;
    $scope.properties.selectedValues = selected_values;  
  };

  this.checkAllSelected = function() {
      var all_selected = (vm.selectedItems.length > 0) ? true : false;
      for (var i=0; i<vm.selectedItems.length; i++) {
        if (! vm.selectedItems[i]) {
            all_selected = false;
            break;
        }
      }
      vm.allSelected = all_selected;
      $scope.properties.isAllSelected = all_selected;
  };
  
  this.changeAllSelected = function(new_value) {
      var i;
      if ((vm.allSelected === true) && (new_value === false)) {
          // Uncheck all values
          $log.debug('changeAllSelected: Unselecting all rows');
          for (i=0; i<vm.selectedItems.length; i++) {
              vm.selectedItems[i] = false;
          }
          
      } else if ((vm.allSelected === false) && (new_value === true)) {
          // Check all values
          $log.debug('changeAllSelected: Selecting all rows');
          for (i=0; i<vm.selectedItems.length; i++) {
              vm.selectedItems[i] = true;
          }
      }
      
      // Store new value
      vm.allSelected = new_value;
      
      // Return the list of selected items
      vm.doUpdateValues();
  };
  
  function transformResponse(data, header) {
		return {
			results: data,
			pagination: parseContentRange(header('Content-Range'))
		};
	}

	/**
	 * helper method which extract pagination data from Content-range HTTP header
	 * @param  {String} strContentRange Content-Range value
	 * @return {Object}                 object containing pagination
	 */
	function parseContentRange(strContentRange) {
		if (strContentRange === null) {
			return {};
		}
		var arrayContentRange = strContentRange.split('/');
		var arrayIndexNumPerPage = arrayContentRange[0].split('-');
		return {
			total: parseInt(arrayContentRange[1], 10),
			currentPage: parseInt(arrayIndexNumPerPage[0], 10) + 1
		};
	}

	vm.updateResults = function () {
		if ($scope.properties.type === 'Variable') {
			vm.updateResultsFromJson();
		} else {
			vm.updateResultsFromAPI();
		}
	};

	//watchGroup does not support object equality so we use another way to monitor all at once
	$scope.$watch('[properties.pageSize, properties.apiUrl, properties.filter, properties.params]', resetPaginationAndUpdateResults, true);

	function resetPaginationAndUpdateResults() {
		vm.pagination = {
			currentPage: 1,
			total: vm.jsonData.length
		};
		vm.pageOffset = 0;
		vm.updateResults();
	}
	
	vm.detailProduct = function (row, $event, $window) {
		$event.stopPropagation();
	    $scope.properties.numMatSelected = row.numMat;
	}


	//Fonctions Custom pour ouvrir une nouvelle proposition
	vm.createNewProposition = function (row, $event, $window) {
		//Empeche le selectedRow d'etre propagé :
		$event.stopPropagation();

        if (($scope.properties.collectionOfRowsToCreate != null)
            && Array.isArray($scope.properties.collectionOfRowsToCreate)) {
            $log.debug("Creating one proposition with mass creation function");
            var rows_selected_for_creation = [row];
            $scope.properties.collectionOfRowsToCreate.push(rows_selected_for_creation);
            
        } else {
            //Prepare the URL
    		var url = $scope.properties.createPropositionURL;
    		url += "?selectedTerritory="+row.codeTerritoire;
    		url += "&selectedAgency="+row.codeAgence;
    		url += "&materialId="+row.numMat;
    		url += "&existingProp=true";
    		url += "&uniqueAgencyName=" + row.libelleAgenceAffectation;
    		url += "&uniqueTerritoryName=" + row.libelleTerritoire;
    		
            //Redirect to Proposition Form
    		window.location.href = url;
        }
	};

	$scope.$watchCollection('properties.content', function (data) {
		if (!Array.isArray(data)) {
			return;
		}
		
		vm.jsonData = data;
		vm.initializeSelectedItems();
		resetPaginationAndUpdateResults();
		
	});
	
	 $scope.$watch('properties.isAllSelected', function() {
	     if ($scope.properties.isAllSelected != vm.allSelected) {
	         vm.changeAllSelected($scope.properties.isAllSelected);
	     }
	 });
	 
	  $scope.$watchCollection('properties.selectedValues', function(data) {
	     if ((data != null) && (Array.isArray(data))) {
	        var nb_selected_values = data.length;
	        if (nb_selected_values != vm.nbSelected) {
	            $log.debug("Number of selected values changed from " + vm.nbSelected + " to " + nb_selected_values);
    	        vm.nbSelected = nb_selected_values;
    	        var index_selected_value=0;
    	        var index_selected_item=0;
    	        
    	        // Search each of the selected items
    	        while (index_selected_value < nb_selected_values) {
        	        for (var i=index_selected_item; i<vm.selectedItems.length; i++) {
        	          if ( angular.equals(vm.jsonData[i], data[index_selected_value])) {
                            vm.selectedItems[i] = true;
                            break;
        	          } else {
        	                vm.selectedItems[i] = false;
        	          }
        	        }
        	        index_selected_item = i+1;
        	        index_selected_value++;
    	        }
    	        
    	        // Remaining items are not selected
    	         for (var i=index_selected_item; i<vm.selectedItems.length; i++) {
    	               vm.selectedItems[i] = false;
    	        }
              }
	        }
	 });
}