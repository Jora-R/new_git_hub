function testsPassedCheck(authenticationId, key){

    var resultText;
    
    var isQualifiedTemp = isQualified(authenticationId);
    
    if (isQualifiedTemp == true) {
        
        // $reactions.answer("Вы являетесь квалом - все тесты пройдены");
        
        return "isQualified";

    }
    else if (isQualifiedTemp == "error") {
        
        // $reactions.answer("Ошибка отработки функции");
        
        return "error";
    }
    
    
    // Все тесты в в ЛК (визуально удобная расшифровка)
    var allTests = [
    { key : "1", code : "ALL_INSTRUMENTS", decode : "Инструменты доступные всем"  },
    { key : "2", code : "UNDELIVERED_DEALS", decode : "Необеспеченные сделки" },
    { key : "3", code : "PFI", decode : "Производные финансовые инструменты"  },
    { key : "4", code : "CONTRACT_REPO", decode : "Договора РЕПО"  },
    { key : "5", code : "BONDS_STRUCT", decode : "Структурные облигации"  },
    { key : "6", code : "CLOSE_PAI", decode : "Закрытые инвестиционные паи"  },
    { key : "7", code : "BONDS_RUS", decode : "Облигации РФ, не соответствующие кредитному рейтингу"  },
    { key : "8", code : "BONDS_FGN", decode : "Иностранные облигации, не соответствующие кредитному рейтингу"  },
    { key : "9", code : "BONDS_STRUCT_INCOME", decode : "Облигации со структурным доходом"  },
    { key : "10", code : "SHARE_NOT_LIST", decode : "Акции РФ, требующие тестирования"  },
    { key : "11", code : "SHARE_FGN_TEST", decode : "ИЦБ, требующие тестирования"  },
    { key : "12", code : "ETF", decode : "Паи иностранных ETF, допущенные к торгам при наличии договора организатора торговли"  },
    { key : "13", code : "FOREX", decode : "Forex"  },
    { key : "14", code : "BONDS_RUS_NOPLACE", decode : "Облигации РФ, выпущенные не в соответствии с законодательством РФ или правом иностранного государства"  },
    { key : "15", code : "BONDS_FGN_NOPLACE", decode : "Иностранные облигации, эмитентом которых не является иностранное государство или иностранная организация"  },
    { key : "16", code : "ETF_NOMARKET", decode : "Паи иностранных ETF, допущенные к торгам при отсутствии договора организатора торговли"  },
    { key : "17", code : "BONDS_CONVERT", decode : "Конвертируемые облигации"  },
    { key : "18", code : "FI_UNFRIENDLY_DEPO", decode : "Инструменты с недружественным местом хранения ЦБ"  },
    //Необходимые тесты в связке с ключами
    { key : "Маржа", code : "UNDELIVERED_DEALS", decode : "Необеспеченные сделки"  },
    ]
    
    
    //Запись необходимых для данного стейта (интента) тестов в массив
    var requiredTests = [];
    for (var i in allTests) {
            if (allTests[i].key == key){
            requiredTests.push(allTests[i]);
            }
        }
    
    
    //Проверка пройденных клиентом тестов и запись в массив
    var clientsPassedTests = qualifiedAspects(authenticationId);
    
    if (clientsPassedTests == "error") {
        
        // $reactions.answer("Ошибка при отработки функции проверки кваласпектов");
        
        return "error";
    }
    
    clientsPassedTests = clientsPassedTests.qualifiedAspects;

    
    // //Вывод сданных тестов
    // for (var i in clientsPassedTests) {
    //     $reactions.answer("тест сдан" + clientsPassedTests[i].code);
    // }
    
    
    //Сравнение необходимых тестов с пройденными клиентом (если есть пересечение, в массиве необходимых тестов, элемент получает значение 0)
    for (var i in requiredTests) {
        for (var x in clientsPassedTests){
            if (clientsPassedTests[x].code == requiredTests[i].code){
                requiredTests[i].code = 0;
            }
        }
    }

    //Перезапись данных о необходимых тестах в основной массив через временный массив => без элементов равных 0
    var tempRequiredTests = [];
    for (var i in requiredTests) {
        if (requiredTests[i].code != 0){
            tempRequiredTests.push(requiredTests[i]);
        }
    }
    requiredTests = tempRequiredTests;
    
    //Проверка наличия массива необходимых для прохождения тестов => если таковой отсутствует, клиент прошел все тесты
    if (requiredTests == false){
        return "complied";
    }
    
    
    //Тесты, которые необходимо пройти клиенту
    var tests = '';
    
    for (var i in requiredTests) {
        tests += "'" + requiredTests[i].decode + "'";
        if (i < requiredTests.length - 1){
            tests += ", "; // Запись запятой в строке после всех элементов кроме последнего
        }
    }
    
    //возврат статуса и запись значения в глобальную переменную
    $jsapi.context().client.testsRequired = tests;
    return "notComplied";
}