/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 * 
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 * 
 * You can leave the controller empty if you do not need it.
 */
function ($scope, widgetNameFactory) {
    
    this.name = widgetNameFactory.getName('pbInput');
    this.lastValue = $scope.properties.valueToSave;
    var ctrl = this;
    
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
    
    $scope.convertToFrenchFormat = function()
    {

       if(!$scope.properties.readOnly)
       {
            var fieldVal = $scope.properties.value;
            
            // CFR 24/11/2022 - Remove € in case it is there
            var stripped_value = fieldVal.toString().replace("€", "").trim();
            
            var num = Number(stripped_value.replace(",","."));
            // CFR 24/11/2022 avoid showing NAN value
            if (isNaN(num)) {
                $scope.properties.invalidNum = true;
                
            } else {
                var numberRound = num.toFixed(2);
                var currency = numberRound.toString().replace(".",",");
                var space = currency.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
            
                ctrl.lastValue = num;
                $scope.properties.valueToSave = num;
                $scope.properties.value = space + " €";
            }
       }
       
    };

    this.setDisplayValue = function setDisplayValue() {
        ctrl.lastValue = $scope.properties.valueToSave;
        var fieldVal = $scope.properties.valueToSave;
        var num = Number(fieldVal.toString().replace(",","."));
        var numberRound = num.toFixed(2);
        var currency = numberRound.toString().replace(".",",");
        var space = currency.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
        
        $scope.properties.value = space + " €";
    };
    
    $scope.validateAmount = function()
    {
        
        var fieldVal = $scope.properties.value;
        
        if(fieldVal.indexOf("€") != -1)
        {
            // CFR 24/11/2022 - Lot4 Sprint3 UAT corrections
            // WAS: fieldVal = fieldVal.toString().substring(0, fieldVal.toString().length-1).trim();
            fieldVal = fieldVal.replace("€", "").trim();
        }
        
        if(fieldVal.indexOf(",") != -1)
        {
            fieldVal = fieldVal.toString().replace(",",".");
        }
        
        var isNum = /^\d*\.?\d+$/.test(fieldVal);
        
        $scope.properties.invalidNum = !isNum;
    };
    
    if($scope.properties.haveDefaultVal || $scope.properties.readOnly) {
        if($scope.properties.valueToSave !== null && $scope.properties.valueToSave != 'undefined' && typeof $scope.properties.valueToSave != 'undefined') {
            ctrl.setDisplayValue();
        }
    }
    
    $scope.$watch('properties.valueToSave', function(valueToSave) {
    
            if (valueToSave != ctrl.lastValue) {
                ctrl.setDisplayValue();
                // CFR 24/11/2022 : No need to validate when value set from calculation
                // $scope.validateAmount();
                 $scope.properties.invalidNum = false;
                
            }
            
    });     
}