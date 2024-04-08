function phoneNumberDetokenization(){
    
    // security_link - вшитая в проект переменная
    // token - вшитая в проект переменная
    
    var url = "http://"+ $env.get("security_link") +"/api/security-manager/internal/tokens/detokenize";
    
    var value = $dialer.getCaller();
    // var value = "lonely-HhS_yNOGfHVeQpaOY9eblw=="; // Для теста
    
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