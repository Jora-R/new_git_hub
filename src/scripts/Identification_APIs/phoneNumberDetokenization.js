function phoneNumberDetokenization(){
    
    // security_link - вшитая в проект переменная
    // token - вшитая в проект переменная
    
    var url = "http://"+ $env.get("security_link") +"/api/security-manager/internal/tokens/detokenize";
    
    var value = $dialer.getCaller();
    // var value = "fervour-7oCv_IwR6kHCwX3T-XaTlA=="; // Для теста на конкретном человеке
    // var value = "3е2е3е34е5н445"; // Для теста на конкретном человеке
    // var value = "7924"; // Для теста на конкретном человеке
    // var value = "webcall"; // Для теста звонок с сайта
    
    $jsapi.context().session.specificPhoneNumber = "default";
 
    if (value != undefined) {
        if (value.length === 4){
            $jsapi.context().session.specificPhoneNumber = "short"; //задаем флаг если звонок осуществляется с добавочного номера
            return "error";
        }
        if (value == "webcall"){
            $jsapi.context().session.specificPhoneNumber = "webcall"; //задаем флаг если звонок осуществляется с добавочного номера
            return "error";
        }
    }
    
    var Authorization = "Basic ";
    
    try{
        Authorization = "Basic " + $secrets.get("token") + "";
    }catch (error) {
      // инструкции для обработки ошибок
     //  logMyErrors(e); // передать объект исключения обработчику ошибок
    }
    
    var response = $http.query(url, {
                    method: "POST",
                    body: {"tokens":[{"value": value}]},
                    headers: {
                        "Authorization" : Authorization,
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    timeout: 1000
                });
     
    if (response.isOk && response.data.values[0] != ""){
        // $reactions.answer("Ваш номер: " + JSON.stringify(response.data.values[0]));
        return response.data.values[0];
    } 
    else {
        return "error";
    }     
}