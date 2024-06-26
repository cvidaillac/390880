function PbDataTableCtrl($scope, $http, $log, $filter) {

	var vm = this;
	// Custom variables
	$scope.properties.listIds = [];
	$scope.selectAll = false;
	
	// Attention : (pensez a changer dans le template)
	//DEV et Studio : newDate().getTime()
	//UAT : newDate() 
	$scope.currentDate = new Date().getTime();

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
				
				// CFR-22/10/2020 BPM-417 : Return the current page
				updateCurrentPageProperty(vm.pagination.currentPage);
			})
			.catch(function (error) {
				$log.error(error);
			});
		}
	};

	this.updateResultsFromJson = function () {
		var start = (vm.pagination.currentPage - 1) * $scope.properties.pageSize;
		var end = vm.pagination.currentPage * $scope.properties.pageSize;

		vm.results = vm.jsonData.slice(start, end);
	};

	this.sortHandler = function () {
		this.updateResults();
	};

	this.paginationHandler = function () {
		this.updateResults();
		
		// CFR-22/10/2020 BPM-417 : Return the current page
		updateCurrentPageProperty(this.pagination.currentPage);
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
		
		// CFR-22/10/2020 BPM-417 : Return the current page
		updateCurrentPageProperty(vm.pagination.currentPage);
		
		vm.updateResults();
	}

	$scope.$watchCollection('properties.content', function (data) {
		if (!Array.isArray(data)) {
			return;
		}
		vm.jsonData = data;
		vm.pagination = {
			currentPage: 1,
			total: data.length
		};
		
		// CFR-22/10/2020 BPM-417 : Return the current page
		updateCurrentPageProperty(vm.pagination.currentPage);
	
		vm.updateResults();
	});

	//-----Custom developments---------------------------------------------------------------------//

	this.checkAll = function () {

		angular.forEach($scope.ctrl.results, function (row) {

			row.select = $scope.properties.selectAll;

			if ($scope.properties.selectAll) {
				$scope.allSelected = true
				vm.addId(row.taskId)
			} else {
				$scope.allSelected = false
				vm.removeId(row.taskId)
			}

		});

	};
	

	this.addId = function (id) {
		if (!$scope.properties.listIds.includes(id)) {
			$scope.properties.listIds.push(id);
		}
		if (vm.allSelected()) {
			$scope.properties.selectAll = true;
		}
	}

	this.removeId = function (id) {

		if ($scope.properties.listIds.includes(id)) {
			var indexId = $scope.properties.listIds.indexOf(id);
			if (indexId > -1) {
				$scope.properties.listIds.splice(indexId, 1);
			}
			if (!vm.allSelected()) {
				$scope.properties.selectAll = false;
				$scope.allSelected = false;
			}
		}
	}
	this.rowsetting = function (row, $event) {
	    $event.stopPropagation();
		if (row.select) {
			vm.addId(row.taskId);
		} else {
			vm.removeId(row.taskId);
		}
	}

	this.listIdEmpty = function () {
		return $scope.properties.listIds.length === 0;
	}

	this.allSelected = function () {
		return (($scope.properties.listIds.length === $scope.ctrl.results.length) && ($scope.ctrl.results.length>0));
	}


    this.askApprove = function(row, $event) {
		$event.stopPropagation();
		$scope.properties.selectedTask = {
			"taskIds" : [row.taskId],
			"actionInput" : "Validée",
			"title" : "Confirmer la validation de la proposition n°" + row.propositionId + "?",
			"commentNeeded" : false,
			"css" : "alert alert-success"
		};

    }

    this.askReject = function(row, $event) {
		$event.stopPropagation();
		$scope.properties.selectedTask = {
			"taskIds" : [row.taskId],
			"actionInput" : "Refusée",
			"title" : "Confirmer le refus de la proposition n°" + row.propositionId + "?",
			"commentNeeded" : true,
			"css" : "alert alert-danger"
		}

    }

	this.askCompletion = function (row, $event) {
		$event.stopPropagation();
		$scope.properties.selectedTask = {
			"taskIds" : [row.taskId],
			"actionInput" : "A compléter",
			"title" : "Vous avez demandé une revue de la proposition n°" + row.propositionId,
			"commentNeeded" : true,
			"css" : "alert alert-warning"
		};
	}



    this.askValidateAllSelected = function() {
		$scope.properties.selectedTask = {
			"taskIds" : $scope.properties.listIds,
			"actionInput" : "Validée",
			"title" : "Confirmer la validation " + (($scope.properties.listIds.length > 1)? " des " + $scope.properties.listIds.length + " propositions" : " de la proposition"),
			"content" : "",
			"commentNeeded" : false,
			"css" : "alert alert-success"
		};

    }

    this.askrefuseAllSelected = function() {

		$scope.properties.selectedTask = {
			"taskIds" : $scope.properties.listIds,
			"actionInput" : "Refusée",
			"title" : "Confirmer le refus " + (($scope.properties.listIds.length > 1)? " des " + $scope.properties.listIds.length + " propositions" : " de la proposition"),
			"content" : "Un commentaire est obligatoire",
			"commentNeeded" : true,
			"css" : "alert alert-danger"
		};
    }
	
	// CFR-22/10/2020 BPM-417 : Update the current page property if bound
    function updateCurrentPageProperty(current_page) {
	try {
		if ($scope.properties.isBound('currentPage')) {
			$scope.properties.currentPage = current_page;
			console.info("Current page is now " + current_page);
		}
	} catch(e) {
		console.error("Exception updateCurrentPageProperty: " + e);
	}
  }
}
