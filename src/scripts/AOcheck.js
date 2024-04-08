function AOcheck(crm_data){
    //text_bot dev -- вся инфа по запросу + парсинг
    
    // $reactions.answer("Попал в функцию проверки профиля АО");
    
    var isAOclient;
     
    //Парсинг данных клиента
    if (crm_data != 0){
        for (var i in crm_data.Result.ClientProfiles) {
        // $reactions.answer(JSON.stringify(crm_data.Result.ClientProfiles[i].Name));
        if (crm_data.Result.ClientProfiles[i].Name == "АО"){
            isAOclient = true;
            break;
        }
        isAOclient = false;
        }
    }
    
    
    
    // Отправка ответа
    switch(isAOclient) {
            case true:
                // $reactions.answer("Вы клиент компании АО");
                return true;
            case false:
                // $reactions.answer("Вы являетесь клиентом одного из подразделений компании, но не АО");
                return false;
            case undefined:  
                // $reactions.answer("Вы не являетесь клиентом компании");
                return 0;
    }
}