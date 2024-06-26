/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 * 
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 * 
 */
function ($scope, $log, $http, $filter) {
    var ctrl = this;
    this.listMaterialsToCheck = [];
    this.checkedMaterials = [];
    this.isChecking = true;
    this.nbPropositionsChecked = 0;
    this.listExistingPropositions = [];
	this.checkForEachProposition = [];
    this.isOverridden = false;
    this.enableOverride = true;
    this.isInitialized = false;
    this.checkboxClass = 'alert-info';
	this.existingMsgClass = 'alert-danger';
	this.existingMsg = $scope.properties.existingPropositionsMessage;
    
    this.logMsg = function logMsg(msg) {
		$log.info("checkExistingPropositions: " + msg);
	};
	
	this.logDebug = function logMsg(msg) {
		$log.debug("checkExistingPropositions: " + msg);
	};
	
	this.logError = function logError(msg) {
		$log.error("checkExistingPropositions: " + msg);
	};
	
	this.checkAllCheckDone = function() {
	    try {
            var all_check_done = true;
            var has_existing = false;
            for (var one_check of this.checkForEachProposition) {
                all_check_done = all_check_done && one_check.checkDone;
                has_existing = has_existing || one_check.hasExisting;
            }
                
            this.isChecking = !all_check_done;
            var preventSubmit = (! all_check_done) || has_existing;
            if (typeof $scope.properties.existingPropositionsCheckResult !== 'undefined') {
                $scope.properties.existingPropositionsCheckResult.hasExisting = has_existing;
                $scope.properties.existingPropositionsCheckResult.preventSubmit = preventSubmit;
            }
            ctrl.logMsg('checkAllCheckDone : all_check_done=' + all_check_done + ', has_existing=' + has_existing + ', preventSubmit=' + preventSubmit);
	        
	    }  catch(e) {
            ctrl.logError('Exception checkAllCheckDone: ' + e);
        }
	};
	
	this.setOverride = function() {
	    if (this.isOverridden) {
	       $scope.properties.existingPropositionsCheckResult.preventSubmit = false;
	       this.checkboxClass = 'alert-success';
	       
	    } else {
	       this.checkAllCheckDone();
	       this.checkboxClass = 'alert-info';
	    }
	};
	
	this.setEnableOverride = function(enable_override) {
		this.enableOverride = enable_override;
		this.existingMsgClass = (enable_override) ? 'alert-warning' : 'alert-danger';
	};
	
	this.isTypeRachatLocfi = function(one_prop) {
		return (one_prop.typeProposition.toLowerCase() === 'rachat locfi');
	
	};
	
	this.returnResultCheck = function(proposition_index, check_result) {
	    try {
	        // Store result of one check
	        if (! Array.isArray(this.checkForEachProposition)
	            || (proposition_index >= this.checkForEachProposition.length)) {
	            ctrl.logError('returnResultCheck: proposition index ' + proposition_index + ' > size of checkForEachProposition');
	            
	        } else {
	            this.checkForEachProposition[proposition_index] = check_result;

                if (check_result.hasExisting) {
                    // Add existing propositions to the list
                    this.listExistingPropositions.push.apply(this.listExistingPropositions, check_result.existingPropositions);
                    $scope.properties.existingPropositionsCheckResult.existingPropositions = this.listExistingPropositions;

                    // Check number of existing propositions
                    var nb_existings = this.listExistingPropositions.length;
                    var existing_msg = $scope.properties.existingPropositionsMessage;
                    if (nb_existings > 1) {
                        existing_msg = existing_msg.replace(' une proposition', ' ' + nb_existings + ' propositions');
                    }
                    this.existingMsg = existing_msg;

                    // Check if override should be disabled : check if current proposition is of type 'Rachat LocFi'
                    if (proposition_index < $scope.properties.listOfPropositions.length) {
                        var one_prop = $scope.properties.listOfPropositions[proposition_index];
                        if (this.isTypeRachatLocfi(one_prop)) {
							// Current proposition is Rachat LocFi, check if we already have an existing proposition of this type
							for (var one_existing of this.listExistingPropositions) {
								if (this.isTypeRachatLocfi(one_existing)) {
									this.setEnableOverride(false);
									ctrl.logMsg('returnResultCheck: disable override option when submitting a second Rachat LocFi');
									break;
								}
							}
                        }
                    }
                }
                
                // Check if all materials have been checked
                this.checkAllCheckDone();
	        }
            
	    } catch(e) {
            ctrl.logError('Exception returnResultCheck: ' + e);
        }    
	};
	
	this.updateListMaterials = function(i, resp_data) {
        try {
            if (resp_data) {
                // Remove DADRs if property ignoreDADR is set to true
                var filtered_data = ($scope.properties.ignoreDADR === true) ?
                     $filter('filter')(resp_data, {"typeWorkflow":"!DADR"}) : resp_data;
                
                // Remove current propositions ids from list
                var props = $scope.properties.listOfPropositions;   
                for (var one_prop of props) {
                    var current_proposition_id = one_prop.persistenceId;
                    var filter_current_proposition = "!" + current_proposition_id;
                    filtered_data = $filter('filter')(filtered_data, {"persistenceId": filter_current_proposition});
                }
                
                var material = this.listMaterialsToCheck[i];
                var proposition_index = material.propositionIndex;
                var existing_propositions = (filtered_data.length === 0) ? false : true;
                var check_result = { 
                    "matNumber" :  material.materialNumber,
                    "checkDone" : true,
                    "hasExisting" : existing_propositions,
                    "existingPropositions": filtered_data
                };
                
                // Return result
                this.returnResultCheck(proposition_index, check_result);
            }
            
            
        } catch(e) {
            ctrl.logError('Exception updateListMaterials: ' + e);
        }
	    
	};
	
	this.callApi = function(url, num_mat, material_index) {
        try {
			ctrl.logDebug('Calling API URL for material ' + num_mat + ' with index ' + material_index + ' : ' + url);
            $http.get(url)
			    .then(function(resp) {
				    ctrl.logMsg("Got response for material [" + num_mat + '] : ' + JSON.stringify(resp.data));
				    ctrl.updateListMaterials(material_index, resp.data);
			    }, function(resp) {
			        // No longer checking
			        ctrl.isChecking = false;
				    ctrl.logError('Error response from URL [' + url + '] :' + JSON.stringify(resp));
            });
        } catch(e) {
            ctrl.logError('Exception callApi: ' + e);
        }
 	};
	
	this.searchPropositions = function() {
        try {
            // Store list of checked materials
            this.checkedMaterials = this.listMaterialsToCheck;
            
            ctrl.logMsg('Searching existing propositions for ' + this.listMaterialsToCheck.length + ' material(s)');
            this.listExistingPropositions = [];
            this.setEnableOverride(true);
            for (var i=0; i<this.listMaterialsToCheck.length; i++) {
                var one_mat = this.listMaterialsToCheck[i];
                var num_mat = one_mat.materialNumber;
                if (num_mat) {
                    var url_params = "?role=User&f=materialNumber=" + num_mat + "=L&s=terminee=Masquer=E";
                    var url = $scope.properties.listBudgetPropositionUrl + url_params;
					var material_index = i;
					this.callApi(url, num_mat, material_index);
                }
            }
        } catch(e) {
            ctrl.logError('Exception searchPropositions: ' + e);
        }
 	};

    this.returnCheckRequired = function(num_mat) {
        var res = {
            "matNumber" :  num_mat,
            "checkDone" : false,
            "hasExisting" : false,
            "existingPropositions": []
        };
        return res;
        
    };
    
    this.returnNoCheckRequired = function() {
        var res = {
            "matNumber" :  "",
            "checkDone" : true,
            "hasExisting" : false,
            "existingPropositions": []
        };
        return res;
    };
    
    this.checkListOfMaterials = function() {
        var res = [];
        
        try {
            if ( typeof $scope.properties.existingPropositionsCheckResult === 'undefined') {
                ctrl.logMsg("checkListOfMaterials: existingPropositionsCheckResult is null, no check");
                
            } else {
                var props = $scope.properties.listOfPropositions;    
                if (props) {
                    var nb_props = props.length;
        
                    if ((nb_props > 0) && (nb_props !== this.nbPropositionsChecked)) {
                        // Initialize array of results
                        ctrl.logMsg('checkListOfMaterials: checking existing materials for ' + nb_props + ' propositions');
                        this.checkForEachProposition = Array(nb_props);
                        this.nbPropositionsChecked = nb_props;
                
                        for (var i=0; i<nb_props; i++) {
                           // Check if it is an existing material
                           var one_prop = props[i];
                           if (one_prop.existing === 'existingMaterial') {
                               var num_mat = one_prop.materialNumber;
                               if (num_mat) {
                                   res.push({"materialNumber" : num_mat, 
                                            "typeProposition" : one_prop.typeProposition,
                                            "propositionIndex" : i
                                   });
                                    this.checkForEachProposition[i] = this.returnCheckRequired(num_mat);
                               } else {
                                    // No material number for an existing material - This should not occur
                                    ctrl.logError("Missing material number for proposition " + i);
                                    this.checkForEachProposition[i] = this.returnNoCheckRequired();                           
                               }
                           } else {
                                ctrl.logMsg("Proposition " + i + " : No check required for new material");
                                this.checkForEachProposition[i] = this.returnNoCheckRequired();
                           }
                        }
                       // Check if we have a material to check
                       var nb_existing_materials = res.length;
                       if (nb_existing_materials === 0) {
                           this.isChecking = false;
                           $scope.properties.existingPropositionsCheckResult.preventSubmit = false;
                           ctrl.logMsg("checkListOfMaterials: No existing material to check, check complete");
                           
                       } else {
                           this.listMaterialsToCheck = res;
                           // Check if the list has changed
                           if (nb_existing_materials !== this.checkedMaterials.length) {
                               // Need to search existing propositions
                               this.isChecking = true;
                               ctrl.logMsg("checkListOfMaterials: checking propositions for " + nb_existing_materials + " existing materials");
                               this.searchPropositions();
                           } else {
                               ctrl.logDebug("checkListOfMaterials: check already started for " + nb_existing_materials + " existing materials");
                           }
                       }
                    } else {
                        ctrl.logDebug("checkListOfMaterials: nb_props=' + nb_props + ', no new proposition, no check");
                    }
                } else {
                    ctrl.logDebug("checkListOfMaterials: list of propositions is null, no check");
                }
            }
        } catch(e) {
            ctrl.logError('Exception checkListOfMaterials: ' + e);
        }
    };
    
    $scope.$watchCollection('properties.listOfPropositions', function(props) {
        try {
            if (props && Array.isArray(props)) {
                ctrl.logDebug('watch(listOfPropositions) : checking list of materials');
                ctrl.checkListOfMaterials();
            }
        } catch(e) {
            ctrl.logError('Exception watch(listOfPropositions): ' + e);
        }
    });
    
    var unwatchResult = $scope.$watch('properties.existingPropositionsCheckResult', function(new_val, old_val) {
        try {
            if (new_val) {
                ctrl.logDebug('watch(existingPropositionsCheckResult) : checking list of materials');
                unwatchResult();
                ctrl.checkListOfMaterials();
            }
        } catch(e) {
            ctrl.logError('Exception watch(existingPropositionsCheckResult): ' + e);
        }

    });
    
}