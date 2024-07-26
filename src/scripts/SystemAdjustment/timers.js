bind("postProcess", function($context) {
    var buttonExeptionsText = ["Перевод на оператора", "Назад", "Детальнее у специалиста"], // Название кнопок исключений, для этих кнопок отработает таймер 5 мин
        stateTimerExeptionsText = [
            "/Отправка запроса КВАЛ",
            "/Котировки",
            "/Перевод на оператора",
            "/Перевод на оператора/Отправка запроса",
            "/Перевод на оператора/Отправка запроса/okState",
            "/Перевод на оператора/Отправка запроса/errorState",
            "/Закрытие обращения/Отправка запроса",
            "/Закрытие обращения",
            "/Закрытие обращения/Отправка запроса/okState",
            "/Закрытие обращения/Отправка запроса/errorState",
            "/Закрытие обращения БТ",
            "/Закрытие обращения БТ/Отправка запроса",
            "/Закрытие обращения БТ/Отправка запроса/okState",
            "/Закрытие обращения БТ/Отправка запроса/errorState",
            "/Рассылки_want_call",
            "/Рассылки_want_call/Отправка запроса",
            "/Рассылки_want_call/Отправка запроса/okState",
            "/Рассылки_want_call/Отправка запроса/errorState",
            "/Пустота",
            "/Перс данные/Получение GlobalID",
            "/Перс данные/Проверка кодового слова",
            "/Отклик срм",
            "/Отклик срм тест",
            "/Отклик свагер",
            "/Получение данных по профилю клиента",
            "/Получение данных по профилю клиента/Отправка запроса",
            "/Перевод на оператора/Перевод на оператора noMatch",
            "/Рекомендации",
            "/Рекомендации/Отправка запроса",
            "/Рекомендации/Предоставление рекомендации",
            "/Рекомендации/Error",
            "/Рекомендации (тест)/Отправка запроса",
            "/Рекомендации (тест)/Предоставление рекомендации",
            "/Рекомендации (тест)/Error",
            "/Рассылки_want_link_oik/Закрыть обращение oik",
            "/Перевод на оператора/Отправка запроса/errorState/finishState",
            "/Закрытие обращения/Отправка запроса/errorState/finishState",
            "/Закрытие обращения БТ/Отправка запроса/errorState/finishState",
            "/"], //"/"" - ОБЯЗАТЕЛЬНО!!!! Список стейтов в которых cуществует собственный таймер (таймаут для запросов!!!)
        endState = "/Закрытие обращения"; // "/"" - ОБЯЗАТЕЛЬНО!!!! Путь к финалному стейту
    
    
    // Для исключений, cо стейтами с собственными таймерами (таймаут для запросов!!!)
    if(stateTimerExeptionsText.indexOf($context.currentState) > -1){
        return 0;
    }

    var replies = $jsapi.context().response.replies;
    var timerFlag = false;
    // цикл по тому, что выводиться пользователю
    
    if($context.currentState == "/Рассылки_want_link_oik"){
        $analytics.setMessageLabel("Таймер 1 час", "Обращение закрыто TB");
        $reactions.timeout({ interval: "59 min", targetState: "/Закрытие обращения БТ" });
        return 0;
    }
    for (var i in replies) {
        // проверка на то, что есть кнопки для пользователя
        if(replies[i].type == "buttons") {
            if( 
                // проверка что кнопок больше чем 1
                (replies[i].buttons.length > 1)
                || ( 
                    // проверка что если кнопка 1, 
                    (replies[i].buttons.length == 1) 
                    // то она не попадает в список кнопок исключений (buttonExeptionsText)
                    && (buttonExeptionsText.indexOf(replies[i].buttons[0].text) == -1 )
                ) 
            ) {
                    $analytics.setMessageLabel("Таймер 50 минут", "Обращение закрыто TB");
                    // $reactions.timeout({ interval: "50 min", targetState: endState }); //Если БЕЗ рекомендаций
                    $jsapi.context().session.clientGlobalId = $jsapi.context().request.data.clientGlobalId; //Если С рекомендацией
                    $reactions.timeout({ interval: "50 min", targetState: "/Рекомендации" }); //Если С рекомендацией
                    return 0;
            }
        }
    }
    // срабатывает или когда кнопок не было или когда была 1 кнопка и эта кнопка входит в список ислкючений (buttonExeptionsText)
        $analytics.setMessageLabel("Таймер 5 минут", "Обращение закрыто TB");
        // $reactions.timeout({ interval: "5 min", targetState: endState }); //Если БЕЗ рекомендаций
        $jsapi.context().session.clientGlobalId = $jsapi.context().request.data.clientGlobalId; //Если С рекомендацией
        $reactions.timeout({ interval: "5 min", targetState: "/Рекомендации" }); //Если С рекомендацией
        return 0;

});