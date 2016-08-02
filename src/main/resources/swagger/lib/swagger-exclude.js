var excludedEndpoints = {
	'wait': true
};

var removeOperatorElement = function(id) {
  var path = "//li[@class='endpoint' and descendant::a[text()='" + id + "']]";
  var operatorElement = document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
  operatorElement.style['display'] = 'none';
};

var hideExcludedOperators = function() {
  while (excludedEndpoints.wait) {
	  // wait
  }
  delete excludedEndpoints.wait;
  for (var property in excludedEndpoints) {
    if (excludedEndpoints.hasOwnProperty(property)) {
        removeOperatorElement(property);
    }
  }  
};

$.getJSON( "swagger-exclude.json", function(data) {
    $.each( data, function( key, val ) {
    	var a = 0;
    	for (var i = 0; i < val.length; i++) {
    		excludedEndpoints[val[i]] = 1;
    	}
    });
    excludedEndpoints.wait = false;
},
function(err){
	console.log(err);
});