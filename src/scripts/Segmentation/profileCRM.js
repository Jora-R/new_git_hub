function profileCRM(){
    // var clientGlobalId = "47a818f7-ce8b-4528-99a3-b166ca26dca0"; //Тындык
    // var clientGlobalId = "46f99ae2-46ba-4cac-99fc-225b6d85790b"; //Анисенкова
    // var clientGlobalId = "88efaf2e-c09f-4168-a3b4-28ecad0ea755"; //Мостовая
    // var clientGlobalId = "88efaf2e-c09f-4168-a3b4"; //Аноним
    // var url_profileCRM = $jsapi.context().injector.url_profileCRM + clientGlobalId; //Для теста
    // var url_profileCRM = "https://crm.finam.ru/ISV/api/Finam.MSCRM.PersonDetailsService/Details?"; //Для теста, поломан запрос
    
    var url_profileCRM = $jsapi.context().injector.url_profileCRM + $jsapi.context().request.data.clientGlobalId; //Prod
    var response_crm = $http.query(url_profileCRM, {
                    method: "GET",
                    headers: {
                        "X-ApiKey": $jsapi.context().injector.api_key_callCRM,
                        "Accept": "application/json"
                    },
                    timeout: 10000
                });
        if (response_crm.isOk ){
            // $reactions.answer("ok");
        } else {
            // $reactions.answer("error");
            $analytics.setMessageLabel("Запрос CRM", "Ошибка");
        }        
        // $reactions.answer(JSON.stringify(response_crm.data));
        return response_crm.data;
}
