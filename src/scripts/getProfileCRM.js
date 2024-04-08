function getProfileCRM(personGlobalID){
    //text_bot dev -- вся инфа по запросу + парсинг
    
    var url_profileCRM = "https://crm.finam.ru/ISV/api/Finam.MSCRM.PersonDetailsService/Details?GlobalId=";
    
    var url = url_profileCRM + personGlobalID;
    
    
    var response = $http.query(url, {
                    method: "GET",
                    headers: {
                        "X-ApiKey": "B46D99343B9C45A29DFC24550CEA21D7",
                        "Accept": "application/json"
                    },
                    timeout: 10000
                });
     
        if (response.isOk ){
        } else {
            $analytics.setMessageLabel("Запрос CRM", "Ошибка");
            return "error";
        }
        // $reactions.answer(JSON.stringify(response.data));
        return response.data;     
}