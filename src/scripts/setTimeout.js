bind("postProcess", function($context) {

    if ($context.currentState == "/Start"){
        $dialer.setNoInputTimeout(25000);   //ожидание ответа клиента после приветствия
    } else {
        $dialer.setNoInputTimeout(15000); // ожидание ответа клиента после любого текста бота
    }
});