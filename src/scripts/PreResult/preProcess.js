bind("preProcess", function($context) {
    var appName = $jsapi.context().request.data.appName;
    var appNameCheck = "finambank";
    // appNameCheck = "testing"; // тест приложения novoadvisor finambank superapp txchat_operator finamtrade finam_open finamru common t2bf_landing finam_lk fbi widget testing unknown finamterminal edox education_lk
    
    if ($context.session.checkBankChat == undefined && appName != undefined && appName == appNameCheck && $context.session.mailing != true) {
        $context.session.checkBankChat = true;
        $reactions.transition("/Приложение банка")
    }
    // $reactions.answer(JSON.stringify($caila.getEntity()));
    
});