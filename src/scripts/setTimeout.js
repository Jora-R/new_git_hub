bind("postProcess", function($context) {

    if ($context.currentState == "/Start"){
        $dialer.setNoInputTimeout(25000);   
    } else {
        $dialer.setNoInputTimeout(15000);
    }
});