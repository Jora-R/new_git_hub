function getGlobalID(phoneNumber){
    
    // var phoneNumber = "+7(916) 363-44-16"; //Ира
    // var phoneNumber = "+7(952) 202-32-47"; //Катя
    // var phoneNumber = "79163737777"; // Ковтун
    
    
    var response = $http.query("https://global-data-api.finam.ru/v1/globaldata/find", {
        method: "POST",
        body: { "language": "RU","type": "N","contacts": [{"typeCode": "CELLPHONE","value": phoneNumber }]},
        headers: {
            "Authorization": "eyJraWQiOiI3NjgyZGY3MS1mOWNiLTRmMDktYmUwOS1lZjBjOWY0NDgwMWMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhcmVhIjoidHQiLCJzY29udGV4dCI6IkNnc0lCeElIZFc1cmJtOTNiZ29vQ0FNU0pHRmtPRE14T1dSa0xUZGhaR1V0TkRGaE1TMWlNV1V3TFdaaFltUTFPV0kwTURreE5Rb0VDQVVTQUFvTENBQVNCM1Z1YTI1dmQyNEtLQWdDRWlSbU16ZGhaRFJoTUMwM1lUWTFMVFJtWldZdE9USmtNeTAzTldFeE5qQTJaams0WlRNS0JRZ0lFZ0V4Q2dRSUNSSUFDZ1FJQ2hJQUNpZ0lCQklrTnpZNE1tUm1OekV0WmpsallpMDBaakE1TFdKbE1Ea3RaV1l3WXpsbU5EUTRNREZqRWdVSTBROFNBQm9NQ0tTY3phNEdFTUNPZ0tZRElnd0lwTXoxd2djUXdJNkFwZ01vQWciLCJ6aXBwZWQiOnRydWUsImNyZWF0ZWQiOiIxNzA4MzQ2OTE2IiwicmVuZXdFeHAiOiIyMDE5NDczMzE2Iiwic2VzcyI6Ikg0c0lBQUFBQUFBQS8xT0s0Vkl4U2pVM05qUXhUdFUxVDB3eTFUVkpNckRVdFVoTE05Rk5UVEpJTnJNME1rODJNa2tXWXZOS0xDNXg5SlRpREhJTjFEV3pNRE16VWhKUHpNM01xMHpNek1sM1NNNHZLdEJMeTh4THpOVXJLblhpeU12WHpjbFB6OHpyWUdRQ0FNdTRlSnBoQUFBQSIsImlzcyI6InR4c2VydmVyIiwia2V5SWQiOiI3NjgyZGY3MS1mOWNiLTRmMDktYmUwOS1lZjBjOWY0NDgwMWMiLCJmaXJlYmFzZSI6IiIsInNlY3JldHMiOiJ0SVlvVXFodXFoQVBUWVFXNVVheFF3PT0iLCJwcm92aWRlciI6IklOVEVSTkFMIiwic2NvcGUiOiJDQUVRQVEiLCJ0c3RlcCI6ImZhbHNlIiwiZXhwIjoyMDE5Mzg2OTE2LCJqdGkiOiJhZDgzMTlkZC03YWRlLTQxYTEtYjFlMC1mYWJkNTliNDA5MTUifQ.LEbqhLjAgpBOEyxVLp0aXsIZoNBP-KYPMTFqJsWzRoELcoxJ2SPIhj9zSqpVrkarH7w3k4YEXX1OTFLECal7ug",
            "Content-Type":  "application/json",
            "Accept": "application/json"
        },
        timeout: 10000
    });
    

    //Задаем переменной responseError.code значение, чтобы в итоговых условных конструкциях была возможность сравнить это поле,
    //конструкция try - catch необходима, чтобы при попытке распарсить response.error не возникала ошибка.
    //Реализуется таким образом по причине того, что response.error показывает пустой, если запрос Ошибочный, но не имеет Кода.
    var responseError = {code: "default"}
    
    try{
        responseError = JSON.parse(response.error);
    }catch (error) {
      // инструкции для обработки ошибок
     //  logMyErrors(e); // передать объект исключения обработчику ошибок
    }
    


    //Итоговая проверка ошибок
    if (response.isOk){
        for (var i in response.data.data) {
            if (response.data.data[i].type == "L"){
                $analytics.setMessageLabel("ЮЛ", "IVR VB");
                $jsapi.context().client.getGlobalID = response.data.data[i];
              return response.data.data[i].actualGlobalId  
            }
        }
        if (response.data.data.length > 1) {
            return "error1";
        }
        $jsapi.context().client.getGlobalID = response.data.data[0];
        return response.data.data[0].actualGlobalId;
    } 
    else if (response.status == "404" && responseError.code == "5"){
        return "error1";
    }
    else {
        return "error2";
    }
}


// ЕСЛИ В КАРТОЧКЕ НЕСКОЛЬКО ФИЗИКОВ С РАЗНЫМИ СТАТУСАМИ И НУЖНО ВЫБРАТЬ ЛУЧШЕГО
   //Итоговая проверка ошибок
//     if (response.isOk){
//         for (var i in response.data.data) {
//             if (response.data.data[i].type == "L"){
//                 $analytics.setMessageLabel("ЮЛ", "IVR");
//                 $jsapi.context().client.getGlobalID = response.data.data[i];
//               return response.data.data[i].actualGlobalId  
//             }
//         }
//         var userArrayIndex = selectUserProfile(response.data.data);
//         $jsapi.context().client.getGlobalID = response.data.data[userArrayIndex];
//         return response.data.data[userArrayIndex].actualGlobalId;
//     } 
//     else if (response.status == "404" && responseError.code == "5"){
//         return "error1";
//     }
//     else {
//         return "error2";
//     }
// }

// function selectUserProfile(userArray){
//     $reactions.answer(1);
//     if(userArray.length === 1){
//         return 0;
//     }
//     $reactions.answer(2);
//     for (var i in userArray) {
//         var profileCRM = getProfileCRM(userArray[i].actualGlobalId);
//         $reactions.answer(22);
//         if((profileCRM == "") || (profileCRM == null) || (profileCRM == "error")){
//             userArray[i]["isVip"] = false;
//             userArray[i]["clientStatus"] = null;
//         }
//         $reactions.answer(222);
//         userArray[i]["isVip"] = profileCRM.Result.IsVip;
//         userArray[i]["clientStatus"] = profileCRM.Result.ClientStatus;
//     }
//     $reactions.answer(3);
//     for (var i in userArray) {
//         if(userArray[i]["isVip"] == true){
//             return i;
//         }
//     }
//     $reactions.answer(4);
//     for (var i in userArray) {
//         if(userArray[i]["clientStatus"] == "Престиж"){
//             return i;
//         }
//     }
//     $reactions.answer(5);
//     for (var i in userArray) {
//         if(userArray[i]["clientStatus"] != null){
//             return i;
//         }
//     }
//     $reactions.answer(6);
//     return 0;
// }