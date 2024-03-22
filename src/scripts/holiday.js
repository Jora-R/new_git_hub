function countersArrayCheckAndCreate($context, countersArray, currentState){
    
    if( !countersArray ){
    
    $context.session.countersArray = [3];
    
    $context.session.countersArray[0] = true;
    
    $context.session.countersArray[1] = true;
    
    $context.session.countersArray[2] = true;
    
    // $reactions.answer(JSON.stringify($context.session.countersArray));
    
    return $context.session.countersArray;
    
    }
    else{

       return countersArray;
    }
    
}


function holiday($context, countersArray, currentState){

    var countersArray = countersArrayCheckAndCreate($context, countersArray, currentState);
    
    
    var date = currentDate();
    
    var allHolidaysDates = "21/3/2024 22/3/2024 23/3/2024 24/3/2024 25/3/2024 28/3/2024 29/3/2024 30/3/2024 31/3/2024 1/4/2024";
    
    var allHolidaysArray = allHolidaysDates.split(' ');
    
    
    
    if(allHolidaysArray.indexOf(date.format("D/M/YYYY")) > -1){
        
        if(currentState == "/Доступные биржи" && countersArray[0] == true){
            
        $reactions.answer("С 22 по 25 марта 2024 года – праздничные дни в Казахстане, торги и расчеты с валютными парами с казахстанским тенге не проводятся.");
        countersArray[0] = false;
        return 0;    
        
        }
        
        if(currentState == "/Время торгов" && countersArray[1] == true){
            
        $reactions.answer("С 22 по 25 марта 2024 года – праздничные дни в Казахстане, торги и расчеты с валютными парами с казахстанским тенге не проводятся.");
        countersArray[1] = false;
        return 0;    
        
        }
        
        if(currentState == "/Режим расчетов" && countersArray[2] == true){
            
        $reactions.answer("С 22 по 25 марта 2024 года – праздничные дни в Казахстане, торги и расчеты с валютными парами с казахстанским тенге не проводятся.");
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