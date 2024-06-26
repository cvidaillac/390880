function WidgetlivingApplicationMenuController($scope, $http, $window) {
    var ctrl = this;
    var pathArray = $window.location.pathname.split( '/' );
    ctrl.applicationToken =  pathArray[pathArray.length-3]; 
    ctrl.pageToken =  pathArray[pathArray.length-2];

    function getApplication() {
        return $http.get('../API/living/application/?c=1&f=token%3D'+ctrl.applicationToken);
    }
    
    this.filterChildren = function (parentId) {
        return (ctrl.applicationMenuList||[]).filter(function(menu){
            return menu.parentMenuId === '' + parentId;
        });
        
    }
   
    function getApplicationMenuList(applicationId) {
        
        $http.get('../API/living/application-menu/?c=100&f=applicationId%3D'+applicationId+'&d=applicationPageId&o=menuIndex+ASC')
            .success(function(data) { 
                ctrl.applicationMenuList = data;
            });
        return applicationId;
    }

    function setTargetedUrl() {
        $scope.properties.targetUrl = "../../../portal/resource/app/"+ctrl.applicationToken+"/"+ ctrl.pageToken+"/content/"+ $window.location.search + ctrl.searchSeparator() + "app=" + ctrl.applicationToken;
    }

    ctrl.searchSeparator = function() {
        return $window.location.search ? "&" : "?";
    };

    ctrl.isParentMenu= function(menu) {
        return menu.parentMenuId==-1 && menu.applicationPageId==-1;
    };
    
    getApplication().then(function(response) {
        var application = response.data[0];
        ctrl.applicationName = application.displayName;
        $window.document.title = application.displayName;
        return application.id;
    }).then(getApplicationMenuList).then(setTargetedUrl);
    
}