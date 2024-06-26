var app = angular.module ('bonitasoft.ui.extensions', []);
	
app.filter('yesNo', function () {
	  return function (input) {
		if (input === null) {
			return '';
		} else if (input) {
		  return 'Oui';
		} else {
		  return 'Non';
		}
	  };
	});

app.filter('checkMark', function () {
	  return function (input) {
		if ((input === null) || (input === 'N/A')) {
			return '';
		} else if ((input === true) || (input === 'true')) {
		   return ' \u2705';    // Green check mark
		  //return ' \u2714';   // Check mark no color
		} else {
		  return ' \u2717';
		  //return ' \u274C';     // Red cross mark
		}
	  };
	});