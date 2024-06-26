function PbButtonCtrl($scope, $log, $window) {

  'use strict';

  this.printFrame = function printFrame() {
      try {
          var frame_elt = $window.frameElement;
          if (frame_elt == null) {
              $log.info("No frame element, printing window");
              $window.print();
          } else {
              $log.info("Print frame element");
              frame_elt.contentWindow.print();   
          }
      } catch(e) {
          $log.error("Exception printFrame :" + e);
      }
  };


}
