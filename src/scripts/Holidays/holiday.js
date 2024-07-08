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
    
    var allHolidaysDates = "2/7/2024 3/7/2024 4/7/2024 1/7/2024 12/6/2024";
    
    var allHolidaysArray = allHolidaysDates.split(' ');
    
    
    
    if(allHolidaysArray.indexOf(date.format("D/M/YYYY")) > -1){
        
        if(currentState == "/Доступные биржи" && countersArray[0] == true){
            
        $reactions.answer("4 июля – день Независимости в США. Торги и расчёты на американских биржах не проводятся.");
        countersArray[0] = false;
        return 0;    
        
        }
        
        if(currentState == "/Время торгов" && countersArray[1] == true){
            
        $reactions.answer("4 июля – день Независимости в США. Торги и расчёты на американских биржах не проводятся.");
        countersArray[1] = false;
        return 0;    
        
        }
        
        if(currentState == "/Режим расчетов" && countersArray[2] == true){
            
        $reactions.answer("4 июля – день Независимости в США. Торги и расчёты на американских биржах не проводятся.");
        countersArray[2] = false;
        return 0;    
        
        }
        
        // if(currentState == "/Время работы"){
            
        // $reactions.answer("12 июня – нерабочий день для всех офисов компании Фина'м.");
        // return 0;    
        // }
        
        else {
            
            return 0;
        }
    }
    
    else {
        
        return 0;
    }
    
}