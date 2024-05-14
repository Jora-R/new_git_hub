//Проверка на постоврение фразы в шапке интента и в уточнении, проставляется флаг при прохождении функции

function countersArrayCheckAndCreate($context, countersArray, currentState){
    
    if( !countersArray ){
    
    $context.session.countersArray = [3];
    
    $context.session.countersArray[0] = true;
    
    $context.session.countersArray[1] = true;
    
    $context.session.countersArray[2] = true;
    
    return $context.session.countersArray;
    
    }
    else{

       return countersArray;
    }
    
}

// Функция проверяет дату запуска скрипта с текстом праздника (Если для интента праздник не влияет, то необходимо закомментировать)

function holiday($context, countersArray, currentState){

    var countersArray = countersArrayCheckAndCreate($context, countersArray, currentState);
    
    var date = currentDate();
    
    var allHolidaysDates = "24/4/2024 25/4/2024 26/4/2024 27/4/2024 28/4/2024 29/4/2024 30/4/2024 1/5/2024 2/5/2024 3/5/2024 4/5/2024 5/5/2024 6/5/2024 7/5/2024 8/5/2024 9/5/2024 10/5/2024";
    
    var allHolidaysArray = allHolidaysDates.split(' ');
    
    
    
    if(allHolidaysArray.indexOf(date.format("D/M/YYYY")) > -1){
        
        if(currentState == "/Доступные биржи" && countersArray[0] == true){
            
        $reactions.answer("Девятого мая - торги и расчёты на Московской бирже не проводятся. 10 мая, торги и расчёты на валютной секции Московской биржи, не проводятся в режимах тудэй, и своп.");
        countersArray[0] = false;
        return 0;    
        
        }
        
        if(currentState == "/Время торгов" && countersArray[1] == true){
            
        $reactions.answer("Девятого мая - торги и расчёты на Московской бирже не проводятся. 10 мая, торги и расчёты на валютной секции Московской биржи, не проводятся в режимах тудэй, и своп.");
        countersArray[1] = false;
        return 0;    
        
        }
        
        if(currentState == "/Режим расчетов" && countersArray[2] == true){
            
        $reactions.answer("Девятого мая - торги и расчёты на Московской бирже не проводятся. 10 мая, торги и расчёты на валютной секции Московской биржи, не проводятся в режимах тудэй, и своп.");
        countersArray[2] = false;
        return 0;    
        
        }
        
        if(currentState == "/Время работы"){
            
        $reactions.answer("Девятого мая - нерабочий день для всех офисов компании Фина'м. 10 мая, в Москве открыты для обслуживания клиентов, центральный офис Фина'м в Наста'сьинском и дополнительный офис на Кутузовском, с 10 до 19 часов. В других городах, офисы компании работают в дежурном режиме; перед посещением офиса рекомендуем связаться с менеджером.");
        return 0;    
        }
        
        else {
            
            return 0;
        }
    }
    
    else {
        
        return 0;
    }
    
}