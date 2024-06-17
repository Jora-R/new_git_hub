//Результаты работы функций записываются во внешние переменные, а итоговый ВОЗВРАЩАЕМЫЙ результат работы функций либо "success", либо "error".
function identification(){
    
    // $reactions.answer("Попал в функцию идентификации");
    
    var resultDetokenization = phoneNumberDetokenization();
        if(resultDetokenization == "error"){
            $analytics.setMessageLabel("Детокенизация не прошла VB", "Сегмент");
            // $reactions.answer("Ошибка");
            return "error";
        } else {
            // $reactions.answer("Прошел детокенизацию");
            var resultGetGlobalID = getGlobalID(resultDetokenization);
            // $reactions.answer("получил глобал айди");
            $jsapi.context().client.personGlobalID = resultGetGlobalID;
            // $reactions.answer("получил глобал айди 2");
            if((resultGetGlobalID == "error1") || (resultGetGlobalID == "error2")){
                $analytics.setMessageLabel("Отсутствует GlobalID VB", "Сегмент");
               return "error";
            } else {
                // $reactions.answer("Прошел глобал айди");
                var resultGetProfileCRM = getProfileCRM(resultGetGlobalID);
                if((resultGetProfileCRM == "") || (resultGetProfileCRM == null) || (resultGetProfileCRM == "error")){
                    $analytics.setMessageLabel("Нет профиля CRM VB", "Сегмент");
                    return "error";
                }
                $jsapi.context().client.profileCRM = resultGetProfileCRM;
                // $reactions.answer("прошел получение профиля");
                return "success";
            }
        }
    }

// $client.profileCRM = "error"; - не влияет, так как внешняя profileCRM может быть только undefined
// $client.profileCRM = undefined; - не влияет, так как в identificationAO уход в AOcheck с проверкой undefined
// $client.personGlobalID = "error"; - не влияет, так как в таком случае profileCRM будет undefined с проверкой на AOcheck
    
function identificationAO(profileCRM){
    // $reactions.answer("Попал в функцию идентификацииАО");
    var isAOclient = AOcheck(profileCRM);
    if((isAOclient == false) || (isAOclient == 0)){
        return "error"; 
    }
    return "success";
}