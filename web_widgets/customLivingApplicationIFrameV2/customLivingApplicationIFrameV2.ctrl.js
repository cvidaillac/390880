function WidgetlivingApplicationIFrameController($scope, $element, $interval, $sce) {
   
    if ($scope.properties.resizeToContent) {
    
        var iframe = $element.find('iframe')[0];
    
        var polling = $interval(function() {
            if(iframe.contentDocument.documentElement){
    			iframe.style.height = (iframe.contentDocument.documentElement.scrollHeight || 400) + "px";
    		}
        }, 100);
        
        $scope.$on('$destroy', function() {
            $interval.cancel(polling);
        });
            
    }
    
    $scope.secureUrl = function() {
        return $sce.trustAsResourceUrl($scope.properties.src);
    }
}