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
    
    var allHolidaysDates = "21/3/2024 22/3/2024 23/3/2024 24/3/2024 25/3/2024 28/3/2024 2/4/2024 3/4/2024 4/4/2024 5/4/2024";
    
    var allHolidaysArray = allHolidaysDates.split(' ');
    
    
    
    if(allHolidaysArray.indexOf(date.format("D/M/YYYY")) > -1){
        
        if(currentState == "/Доступные биржи" && countersArray[0] == true){
            
        $reactions.answer("4 апреля, торги и расчёты не проводятся на бирже Гонконга, а также не проводятся торги и расчёты с валютами гонконгский доллар и китайский юань. 5 апреля, не проводятся торги и расчёты с валютой китайский юань.");
        countersArray[0] = false;
        return 0;    
        
        }
        
        if(currentState == "/Время торгов" && countersArray[1] == true){
            
        $reactions.answer("4 апреля, торги и расчёты не проводятся на бирже Гонконга, а также не проводятся торги и расчёты с валютами гонконгский доллар и китайский юань. 5 апреля, не проводятся торги и расчёты с валютой китайский юань.");
        countersArray[1] = false;
        return 0;    
        
        }
        
        if(currentState == "/Режим расчетов" && countersArray[2] == true){
            
        $reactions.answer("4 апреля, торги и расчёты не проводятся на бирже Гонконга, а также не проводятся торги и расчёты с валютами гонконгский доллар и китайский юань. 5 апреля, не проводятся торги и расчёты с валютой китайский юань.");
        countersArray[2] = false;
        return 0;    
        
        }
        
        // if(currentState == "/Время работы"){
            
        // $reactions.answer("В период с 8 по 10 марта - Выходные дни в офисах Фина'м");
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