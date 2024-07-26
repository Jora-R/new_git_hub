function qualifiedAspects(authenticationId){
    
    // $reactions.answer("Проверка на пройденные клиентом тесты (квал аспекты)");
    
    var response = $http.query($jsapi.context().injector.url_edox_QA, {
        method: "GET",
        headers: {
            "Authorization": $jsapi.context().injector.token_edox_userinfo,
            "accept": "application/json",
            "x-authentication-Id": authenticationId
        },
        timeout: 1000
    });
    
     
    if (response.isOk){
        
        // $reactions.answer("Запрос прошел успешно");
        
    } else {
        
        // $reactions.answer("Ошибка запроса");

        return "error";
    }
    return response.data;
}