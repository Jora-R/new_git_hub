function sendMessageTxchatHttp(personGlobalID, text){
    
    // $reactions.answer("Попал в функцию");
    
    // var text = "Ссылка на открытие счета https://www.finam.ru/landings/freetrade";
    
    var response = $http.query("https://ftrr01.finam.ru/txchat/send_message/", {
        method: "POST",
        body: {"client_id": personGlobalID,"message_text": text},
        headers: {
            "Authorization": "eyJraWQiOiJhMjcxMDA1OS1lM2RkLTRiYmQtYTNhNC05NjQwNmUwMDA5MmYiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhcmVhIjoidHQiLCJzY29udGV4dCI6IkNnc0lCeElIZFc1cmJtOTNiZ29vQ0FNU0pEaGhaV1ZpT0RZMkxXSXlNVGt0TkdOa05pMDVPRGRpTFdRM1pEaGlOR015TW1abFpRb0VDQVVTQUFvTENBQVNCM1Z1YTI1dmQyNEtLQWdDRWlRME56Wm1NRFZoTlMwMU56bGhMVFF4WWpBdE9UUmpOUzB3T0RNMU5EWXpabU16TURJS0JRZ0lFZ0V4Q2dRSUNSSUFDZ1FJQ2hJQUNpZ0lCQklrWVRJM01UQXdOVGt0WlROa1pDMDBZbUprTFdFellUUXRPVFkwTURabE1EQXdPVEptRWdRSUNCSUFHZ3dJdGJiUnJnWVFnTFhKdlFJaURBaTE1dm5DQnhDQXRjbTlBaWdDIiwiemlwcGVkIjp0cnVlLCJjcmVhdGVkIjoiMTcwODQxNTc5NyIsInJlbmV3RXhwIjoiMjAxOTU0MjE5NyIsInNlc3MiOiJINHNJQUFBQUFBQUEvMVBLNUZJeE5EUkpUREl3Tk5FMU5FcE0walV4U2pQUnRUUTBOOWMxVGs0enNUUklOamN3TjAwVDRya3c2OEtPaXcwWGRsellmV0czbE1DRlNSYzJYR3dFOFM3MlhOaDZZWk9TZUhaWlluRm1UbVZxbVVOeWZsR0JYbHBtWG1LdVhsR3BFMGRldm01T2ZucG1YZ2NqRXdCVGZYbVJiZ0FBQUEiLCJpc3MiOiJ0eHNlcnZlciIsImtleUlkIjoiYTI3MTAwNTktZTNkZC00YmJkLWEzYTQtOTY0MDZlMDAwOTJmIiwiZmlyZWJhc2UiOiIiLCJzZWNyZXRzIjoiM1lXOGNsVVI1cS94ak0wYkMxRXA5Zz09IiwicHJvdmlkZXIiOiJJTlRFUk5BTCIsInNjb3BlIjoiQ0FFUUFRIiwidHN0ZXAiOiJmYWxzZSIsImV4cCI6MjAxOTQ1NTc5NywianRpIjoiOGFlZWI4NjYtYjIxOS00Y2Q2LTk4N2ItZDdkOGI0YzIyZmVlIn0.GyV8_IAA1YCuk946-O56NuMFEetsg0gXt7uYu0T4fjYZ7czt_bLtrjW--DCji5ptNkOV0qP-bFUFgJFqGq0gxA",
            "Content-Type":  "application/json"
        },
        timeout: 1000
    });
    
    // $reactions.answer("Прошел функцию");
    
    if (response.isOk){
        $analytics.setMessageLabel("Отправлена", "Инструкции VB");
        $reactions.answer("Сообщение успешно отправлено; проверьте чат поддержки в личном кабинете, или в терминале фина'м трейд.");
        return 1;
    } 
    else {
        $analytics.setMessageLabel("Ошибка отправки", "Инструкции VB");
        $reactions.answer("Не удалось отправить сообщение. Для получения необходимой информации, пожалуйста, обратитесь в чат поддержки в личном кабинете, или в терминале фина'м трейд ");
        return "error";
    }
}