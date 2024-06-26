function PbDataTableCtrl($scope, $http, $log, $filter) {

	var vm = this;
	// Custom variables
	$scope.listIds = [];
	$scope.selectAll = false;

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
		vm.updateResults();
	});

	//-----Custom developments---------------------------------------------------------------------//

	this.checkAll = function () {

		angular.forEach($scope.ctrl.results, function (row) {

			row.select = $scope.selectAll;

			if ($scope.selectAll) {
				$scope.allSelected = true
				vm.addId(row.taskId)
			} else {
				$scope.allSelected = false
				vm.removeId(row.taskId)
			}

		});

	};
	

	this.addId = function (id) {
		if (!$scope.listIds.includes(id)) {
			$scope.listIds.push(id);
		}
		if (vm.allSelected()) {
			$scope.selectAll = true;
		}
	}


	this.removeId = function (id) {

		if ($scope.listIds.includes(id)) {
			var indexId = $scope.listIds.indexOf(id);
			if (indexId > -1) {
				$scope.listIds.splice(indexId, 1);
			}
			if (!vm.allSelected()) {
				$scope.selectAll = false;
				$scope.allSelected = false;
			}
		}
	}
	this.rowsetting = function (id, $event) {
	    $event.stopPropagation();
	   var found = $scope.listIds.indexOf(id);
    
    if(found == -1) $scope.listIds.push(id);
    
    else $scope.listIds.splice(found, 1);
	}


	this.listIdEmpty = function () {
		return $scope.listIds.length === 0;
	}

	this.allSelected = function () {
		return $scope.listIds.length === $scope.ctrl.results.length;
	}

	this.displayValidation = function() {
	    return true;
	}


    this.popUpvalidateAllSelected = function() {
        this.displayConfirmation(
            'Confirmation des validations', 
            'Vous avez sélectionné ' + $scope.listIds.length + ' proposition(s). Confirmer les validations?',
            "blue", this.validateAllSelected)
    }


	//Fonction Custom pour traiter toutes les lignes selectionnées
	this.validateAllSelected = function () {

		var url = " ../API/extension/executeTask?listIds=" + $scope.listIds + $scope.properties.urlGenerate + "&commande=" + $scope.properties.commande;

		$http.post(url, {})
		.success(function (data, status, headers, config) {
			if (!Array.isArray(data)) {
				return;
			}
			$scope.listIds = [];
			vm.jsonData = data;
			vm.pagination = {
				currentPage: 1,
				total: data.length
			};
			vm.updateResults();
		})
	}
	

	//Fonction Custom pour traiter la tache
	this.approve = function (row, $event) {
		//Empeche le selectedRow d'etre propagé :
		$event.stopPropagation();

		var url = "  ../API/extension/executeTask?taskId=" + row.taskId + $scope.properties.urlGenerate  + "&commande=" + $scope.properties.commande;
		var sentObject = {
			"actionInput": "Validée",
			"commentInput": ""
		};

		$http.post(url, sentObject)
		.success(function (data, status, headers, config) {
			if (!Array.isArray(data)) {
				return;
			}
			vm.jsonData = data;
			vm.pagination = {
				currentPage: 1,
				total: data.length
			};
			vm.updateResults();
		})
	}

	this.displayProposition = function (row, $event) {
		//Empeche le selectedRow d'etre propagé :
		$event.stopPropagation();
		$scope.properties.selectedRow = {};
		$scope.properties.selectedRow = row;
	}
	
	$scope.properties.selectedRows = $scope.listIds;
}