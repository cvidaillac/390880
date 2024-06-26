
function ($scope,$http) {
   
   
   function getFinParams()
   {
       var prop = $scope.properties.proposition;
       
       var baseUrl = $scope.properties.apiUrl+"?entity=" + prop.entiteCode;
       
       if(prop.budgetYear != null && prop.budgetYear != '')
       {
          baseUrl += "&budgetYear=" + prop.budgetYear;
       }
       
       if(prop.typeMaterial != null && prop.typeMaterial != '')
       {
           baseUrl += "&materialType=" + prop.typeMaterial;
       }
       
       if(prop.subTypeMaterial != null && prop.subTypeMaterial != '')
       {
          baseUrl += "&materialSubType=" + prop.subTypeMaterial;
       }
       
       if(prop.typeAcquisition != null && prop.typeAcquisition != '')
       {
           baseUrl += "&requestType=" + prop.typeAcquisition;
       }
       
       if(prop.typeAcquisition != undefined)
       {
           if(prop.typeAcquisition == 'LocFi' || prop.typeAcquisition == 'LLD' || 
                prop.typeAcquisition == 'LCD' || prop.typeAcquisition == 'LLDAC' || 
                prop.typeAcquisition == 'LCDAC' || prop.typeAcquisition == 'Crédit-Bail')
            {
                $http({
                        url: baseUrl,
                        method: 'GET',
                        
                    })
                    .then(function(response) {
                        
                        if(typeof response.data != 'undefined')
                        {
                            if(response.data.length > 0)
                            {
                                $scope.properties.months = parseInt(response.data[0].nombreMois);
                                
                                if(prop.typeAcquisition == 'LocFi' || prop.typeAcquisition == 'Crédit-Bail')
                                {
                                    $scope.properties.percentage = parseFloat(response.data[0].pourcentageNegocie.replace(",", "."));
                                }    
                            }
                            
                        }
                        
                    }, 
                    function(error) { 
                       console.log("Error while geting Param Fin");
                       console.log(error);
                    });
            }    
       }
   }
   
   
   
   $scope.$watch('[properties.proposition.typeAcquisition, properties.proposition.budgetYear, properties.proposition.typeMaterial, properties.proposition.subTypeMaterial]', getFinParams, true);
}