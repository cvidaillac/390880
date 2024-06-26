function ($scope, $log, widgetNameFactory, $document) {
    $scope.filterDoublon = function(requireds){
        var tabReturn = [];
        var old = null;
        for(var indice in requireds){
            var item = requireds[indice];
            if((old !== null) && (item.$name === old)){
                //skip
            }else{
                if(item.$name === ""){
                    item.$name = item.$error.required[0].$name;
                }
                old = item.$name;
                tabReturn.push(item);
            }
        }
        
        return tabReturn;
    }
    
    $scope.$watch(angular.bind(this, function () {
        $scope.myRequired = $scope.filterDoublon($scope.properties.value.$error.required);
        $scope.myErrorsNumber = $scope.filterDoublon($scope.properties.value.$error.number);
        $scope.myErrorsEmail = $scope.filterDoublon($scope.properties.value.$error.email);
    }), function (newVal, oldVal) {
        // now we will pickup changes to newVal and oldVal
    });
    
    $scope.getLabel = function(key){
        var strlabel = "";
        var selector = document.getElementsByName(key)[0].parentNode.parentNode;
        var radioInlineSelector = document.getElementsByName(key)[0].parentNode;
        var label = selector.querySelector('label');
        if(label === null){
            //DatePicker ou uploader
            selector = selector.parentNode;
            label = selector.querySelector('label');
            if (label === null){
                // file uploader
                strlabel = selector.parentNode.parentNode.parentNode.parentNode.querySelector('label').innerHTML.trim();
            } else {
                // datepicker
                if(label.className.indexOf('control-label') != -1){
                    strlabel = label.innerHTML;
                }
            }
        }else if(label.className.indexOf('control-label') != -1){
            strlabel = label.innerHTML; 
        } else if(selector.className.indexOf('checkbox') != -1){
            //checkbox
            strlabel = label.textContent;
        }else if(selector.className.indexOf('radio')!= -1 || radioInlineSelector.className.indexOf('radio-inline')!= -1){
            //radio
            selector = selector.parentNode.parentNode;
            label = selector.querySelector('label');
            strlabel = label.innerHTML;
        }else{
            strlabel = "Erreur lors de la récuperation de la valeur";
        }
        return strlabel;
    }
}