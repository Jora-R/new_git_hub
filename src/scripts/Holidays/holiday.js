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
    
    var allHolidaysDates = "13/9/2024 15/9/2024 16/9/2024 17/9/2024 18/9/2024";
    
    var allHolidaysArray = allHolidaysDates.split(' ');
    
    
    
    if(allHolidaysArray.indexOf(date.format("D/M/YYYY")) > -1){
        
        if(currentState == "/Доступные биржи" && countersArray[0] == true){
            
        $reactions.answer("16 и 17 сентября торги и расчеты на московской бирже с юанем в режиме today и СВОП не проводятся. 18 сентября — Праздничный день в Гонконге. Торги и расчеты на бирже Гонконга не проводятся.");
        countersArray[0] = false;
        return 0;    
        
        }
        
        if(currentState == "/Время торгов" && countersArray[1] == true){
            
        $reactions.answer("16 и 17 сентября торги и расчеты на московской бирже с юанем в режиме today и СВОП не проводятся. 18 сентября — Праздничный день в Гонконге. Торги и расчеты на бирже Гонконга не проводятся.");
        countersArray[1] = false;
        return 0;    
        
        }
        
        if(currentState == "/Режим расчетов" && countersArray[2] == true){
            
        $reactions.answer("16 и 17 сентября торги и расчеты на московской бирже с юанем в режиме today и СВОП не проводятся. 18 сентября — Праздничный день в Гонконге. Торги и расчеты на бирже Гонконга не проводятся.");
        countersArray[2] = false;
        return 0;    
        
        }
        
        // if(currentState == "/Время работы"){
            
        // $reactions.answer("Обращаем ваше внимание, в пятницу 2 августа центральный офис Фина'м в Наста'сьинском переулке дом 7 строение 2 работает с 9:30 до 19 часов. В субботу 3 августа нерабочий день для центрального офиса, и для дополнительного офиса в Перово.");
        // return 0;    
        // }
        
        // else {
            
        //     return 0;
        // }
    }
    
    else {
        
        return 0;
    }
    
}