/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 * 
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 * 
 * You can leave the controller empty if you do not need it.
 */
	var app = angular.module('App', []);
	app.controller('MainCtroller', function($scope) {	
	
	        $scope.model = {};
	
	        // This property will be bound to checkbox in table header
	        $scope.model.allItemsSelected = false;
	
	        // Here first initialize all name list
	        $scope.model.entities = [
	            { "key": 1, "value": "Java Honk" },
	            { "key": 2, "value": "Angular JS" },
	            { "key": 3, "value": "Multiple Check box" }
	        ];
	
	        // This executes when entity in table is checked
	        $scope.selectEntity = function () {
	            // If any entity is not checked, then uncheck the "allItemsSelected" checkbox
	            for (var i = 0; i < $scope.model.entities.length; i++) {
	                if (!$scope.model.entities[i].isChecked) {
	                    $scope.model.allItemsSelected = false;
	                    return;
	                }
	            }
	
	            //If not the check the "allItemsSelected" checkbox
	            $scope.model.allItemsSelected = true;
	        };
	
	        // This executes when checkbox in table header is checked
	        $scope.selectAll = function () {
	            // Loop through all the entities and set their isChecked property
	            for (var i = 0; i < $scope.model.entities.length; i++) {
	                $scope.model.entities[i].isChecked = $scope.model.allItemsSelected;
	            }
	        };
	  
	
	    
	});