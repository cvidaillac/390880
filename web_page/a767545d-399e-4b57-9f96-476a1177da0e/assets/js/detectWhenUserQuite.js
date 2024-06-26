var isTriggered = false;


window.onload = function get_body() {


    var body = document.getElementsByTagName("body")[0];

    body.addEventListener('mouseleave', function(){
        /*
        if(!isTriggered) {
            isTriggered = true;
            setTimeout(function(){ isTriggered = false }, 10000);
            result = window.confirm("Fermer la fenetre?");
            if (result) {
                window.top.close();
            }
        }
        */

    }, false);
}