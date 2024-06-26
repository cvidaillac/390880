function PbDatePickerCtrl($scope, $log, widgetNameFactory, $element, $locale, $bsDatepicker) {
  'use strict';
    

    this.customlabel = function(MaterialExisting, typeProposition) {
        var returnValue = ""

        if (MaterialExisting == "newMaterial") {
            returnValue = "livraison";
        } else {
            // CFR 12/10/2020-BPM-384: New motif 'A prolonger'
            var dataCorrespondance = 
            {
                "A renouveler" : "livraison",
                "Rachat LocFi" : "rachat",
                "A vendre" : "vente",
                "A restituer" : "restitution",
                "A rebuter" : "mise au rebut",
                "Transfert" : "transfert",
                "A prolonger":"Nouvelle date de fin du contrat"
            };
            returnValue = dataCorrespondance[typeProposition];
        }
    if (typeProposition !== "A prolonger") {
        returnValue = "Date de " + returnValue + " prévue";
    }

        return returnValue;


        /*RULES : 
        Si "Nouveau matériel" ou "Nouvel équipement" ou "A renouveler" --> "Date de livraison prévue"
        Si "Rachat de LocFi" --> "Date de rachat prévue"
        Si "A vendre" --> "Date de vente prévue"
        Si "A restituer" --> "Date de restitution prévue"
        Si "A rebuter" --> "Date de mise au rebut prévue"
        Si "Transfert" --> "Date de transfert prévue"
        */
        
    };
    this.name = widgetNameFactory.getName('pbDatepicker');
    this.firstDayOfWeek = ($locale && $locale.DATETIME_FORMATS && $locale.DATETIME_FORMATS.FIRSTDAYOFWEEK) || 0;

  $bsDatepicker.defaults.keyboard = false;

  this.setDateToToday = function() {
    var today = new Date();
    if(today.getDay() !== today.getUTCDay()) {
      //we need to add this offset for the displayed date to be correct
      if(today.getTimezoneOffset() > 0) {
        today.setTime(today.getTime() - 1440 * 60 * 1000);
      } else if(today.getTimezoneOffset() < 0) {
        today.setTime(today.getTime() + 1440 * 60 * 1000);
      }
    }
    today.setUTCHours(0);
    today.setUTCMinutes(0);
    today.setUTCSeconds(0);
    today.setUTCMilliseconds(0);
    $scope.properties.value = today;
  };

  this.openDatePicker = function () {
    $element.find('input')[0].focus();
  };


  if (!$scope.properties.isBound('value')) {
    $log.error('the pbDatepicker property named "value" need to be bound to a variable');
  }


}
