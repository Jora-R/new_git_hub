var want_chat = {
    // "want_chat" : "5ec8de6b-7786-49b5-a3e5-d7bcc938cbb4", //для теста dev
    "want_chat" : "afdc9b52-15bc-4175-86c4-9992a8edb701", //для теста prod
    "want_chat_glog-div1" : "150ee7d9-1250-498f-ab33-2826f446b851",
    "want_chat_glog-div2" : "5ec8de6b-7786-49b5-a3e5-d7bcc938cbb4",
    "want_chat_glog-div3" : "abffdc56-aa1f-4fa8-bb19-ee52349c4bdc",
    "want_chat_test" : "afdc9b52-15bc-4175-86c4-9992a8edb701",
    "want_chat_oik" : "fffd0642-d575-478b-aab6-4295b9e4dddf",
    "want_chat_oipfi" : "71b89035-8c06-46d2-84e4-5148ed0411d4",
    "want_chat_sales" : "32fb5923-1fd5-460b-a4e9-699455d054a0",
    "want_chat_ukf" : "2195e586-e9fb-4372-bb9e-211e23fe80a0",
    "want_chat_orbu" : "029adcf9-a246-4742-aad0-24f9c2b35791",
    "want_chat_okokk" : "beb72565-ddaa-4fe1-9958-44b1c05467ac",
    "want_chat_support" : "afdc9b52-15bc-4175-86c4-9992a8edb701"
};

var want_call = {
    // "want_call" : ["13858","5ec8de6b-7786-49b5-a3e5-d7bcc938cbb4"], //для теста dev
    "want_call" : ["13858","afdc9b52-15bc-4175-86c4-9992a8edb701"], //для теста prod
    "want_call_glog-div1" : ["13858","150ee7d9-1250-498f-ab33-2826f446b851"],
    "want_call_glog-div2" : ["13858","5ec8de6b-7786-49b5-a3e5-d7bcc938cbb4"],
    "want_call_glog-div3" : ["13858","abffdc56-aa1f-4fa8-bb19-ee52349c4bdc"],
    "want_call_test" : ["13858","afdc9b52-15bc-4175-86c4-9992a8edb701"],
    "want_call_oik" : ["14065","fffd0642-d575-478b-aab6-4295b9e4dddf"],
    "want_call_oipfi" : ["14070","71b89035-8c06-46d2-84e4-5148ed0411d4"],
    "want_call_sales" : ["14068","32fb5923-1fd5-460b-a4e9-699455d054a0"],
    "want_call_ukf" : ["14067","2195e586-e9fb-4372-bb9e-211e23fe80a0"],
    "want_call_orbu" : ["14066","029adcf9-a246-4742-aad0-24f9c2b35791"],
    "want_call_okokk" : ["14069","beb72565-ddaa-4fe1-9958-44b1c05467ac"],
    "want_call_support" : ["14071","afdc9b52-15bc-4175-86c4-9992a8edb701"]
};

var want_link = {
    "want_link" : "",
    "want_link_glog-div1" : "",
    "want_link_glog-div2" : "",
    "want_link_glog-div3" : "",
    "want_link_test" : "",
    "want_link_oik" : ["14065","fffd0642-d575-478b-aab6-4295b9e4dddf"],
    "want_link_oik_test" : ["14065","fffd0642-d575-478b-aab6-4295b9e4dddf"],
    "want_link_oipfi" : "",
    "want_link_sales" : "",
    "want_link_ukf" : "",
    "want_link_orbu" : "",
    "want_link_okokk" : "",
    "want_link_support" : ""
};

var want_like = {
    "want_like" : "",
    "want_like_glog-div1" : "",
    "want_like_glog-div2" : "",
    "want_like_glog-div3" : "",
    "want_like_test" : "",
    "want_like_oik" : ["14065","fffd0642-d575-478b-aab6-4295b9e4dddf"],
    "want_like_oik_test" : ["14065","fffd0642-d575-478b-aab6-4295b9e4dddf"],
    "want_like_oipfi" : "",
    "want_like_sales" : "",
    "want_like_ukf" : "",
    "want_like_orbu" : "",
    "want_like_okokk" : "",
    "want_like_support" : ""
};

function mailling(){
    var ID_button = $jsapi.context().request.data.pressedButtonId.split("/")[0];
    // ID_button = "want_link_oik"; // поле используется для теста нажатой кнопки рассылки
    // $reactions.answer("mailling()");
    if(want_chat[ID_button] != undefined) {
        $analytics.setMessageLabel("want_chat", "Рассылки TB");
        $jsapi.context().session.to_division = want_chat[ID_button];
        $reactions.transition("/Перевод на оператора");
        return true;
        // $reactions.answer("mailling()");
    } else if(want_call[ID_button] != undefined) {
        $analytics.setMessageLabel("want_call", "Рассылки TB");
        $jsapi.context().session.sourceId = want_call[ID_button][0];
        $jsapi.context().session.to_division = want_call[ID_button][1];
        $reactions.transition("/Рассылки_want_call");
        return true;
    } else if(want_link[ID_button] != undefined) {
        $analytics.setMessageLabel("want_link", "Рассылки TB");
        $reactions.answer("Спасибо за проявленный интерес!");
        if((ID_button == "want_link_oik") || (ID_button == "want_link_oik_test")) {
            $jsapi.context().session.sourceId = want_link[ID_button][0];
            $jsapi.context().session.to_division = want_link[ID_button][1];
            $reactions.timeout({ interval: "15 min", targetState: "/Рассылки_want_link_oik" });
            return true;
        }
        $reactions.transition("/Закрытие обращения БТ");
        return true;
    } else if(want_like[ID_button] != undefined) {
        $analytics.setMessageLabel("want_like", "Рассылки TB");
        // $reactions.answer(ID_button);
        $reactions.transition("/Закрытие обращения БТ");
        return true;
    }
    $reactions.transition("/Перевод на оператора"); //если включить все запросы уходят на оператора
    return true;
}
