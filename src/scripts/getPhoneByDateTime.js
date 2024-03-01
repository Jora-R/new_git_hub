function getPhoneByDateTime(operatorPhoneNumber){
    
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
    
    var hours = +moment(time).format("H");
    var day = date.locale("ru").format("dddd");
    //  $reactions.answer(date.format("D/M/YYYY"));
                        
    
    if(operatorPhoneNumber == '3820') {
        if((day == 'воскресенье') || (day == 'суббота')){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
    }
                        
    if(operatorPhoneNumber == '3888') {
        
        if(day == 'воскресенье'){
            operatorPhoneNumber = '1000';
        } else if(day == 'суббота'){
            if(hours < 10 || hours >= 16){
                operatorPhoneNumber = '1000';
            }
        } else {
            if(hours < 9 || hours >= 19){
                operatorPhoneNumber = '1000';
            }
        }
        
        // if((date.format("D/M/YYYY") == '1/1/2024') || (date.format("D/M/YYYY") == '2/1/2024')) {
        //     operatorPhoneNumber = '1000';
        // }
        
        // if((date.format("D/M/YYYY") == '3/1/2024') || (date.format("D/M/YYYY") == '4/1/2024') || (date.format("D/M/YYYY") == '5/1/2024') || (date.format("D/M/YYYY") == '8/1/2024')) {
        //     if(hours < 10 || hours >= 19){
        //         operatorPhoneNumber = '1000';
        //     }    
        // }
        
        // if(date.format("D/M/YYYY") == '6/1/2024') {
        //     operatorPhoneNumber = '1000';  
        // }
    }
                        
    if(operatorPhoneNumber == '3887') {
        if(day == 'воскресенье'){
            operatorPhoneNumber = '1000';
        } else if(day == 'суббота'){
            if(hours < 9 || hours >= 16){
                operatorPhoneNumber = '1000';
            }
        }
    }
                        
    if(operatorPhoneNumber == '2200') {
        if(day == 'воскресенье'){
            operatorPhoneNumber = '1000';
        } else if(day == 'суббота'){
            //if(hours > 0){
                operatorPhoneNumber = '1000';
            //}
        } else {
            if(hours < 7 && hours >= 0){
                operatorPhoneNumber = '1000';
            }
        }
    }
    
    if(operatorPhoneNumber == '3024') {
        if((day == 'воскресенье') || (day == 'суббота')){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 10 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // if((date.format("D/M/YYYY") == '1/1/2024') || (date.format("D/M/YYYY") == '2/1/2024')) {
        //     operatorPhoneNumber = '1000';
        // }
        
        // if((date.format("D/M/YYYY") == '3/1/2024') || (date.format("D/M/YYYY") == '4/1/2024') || (date.format("D/M/YYYY") == '5/1/2024') || (date.format("D/M/YYYY") == '8/1/2024')) {
        //     if(hours < 10 || hours >= 19){
        //         operatorPhoneNumber = '1000';
        //     }    
        // }
    }
    
    if(operatorPhoneNumber == '2131') {
        if((day == 'воскресенье') || (day == 'суббота')){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
    }
    
    if(operatorPhoneNumber == '3822') {
        if((day == 'воскресенье') || (day == 'суббота')){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // if((date.format("D/M/YYYY") == '1/1/2024') || (date.format("D/M/YYYY") == '2/1/2024')) {
        //     operatorPhoneNumber = '1000';
        // }
        
        // if((date.format("D/M/YYYY") == '3/1/2024') || (date.format("D/M/YYYY") == '4/1/2024') || (date.format("D/M/YYYY") == '5/1/2024') || (date.format("D/M/YYYY") == '8/1/2024')) {
        //     if(hours < 10 || hours >= 19){
        //         operatorPhoneNumber = '1000';
        //     }    
        // }
    }
    
    if(operatorPhoneNumber == '4000') {
        if((day == 'воскресенье') || (day == 'суббота')){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
         if((date.format("D/M/YYYY") == '1/1/2024') || (date.format("D/M/YYYY") == '2/1/2024')) {
            operatorPhoneNumber = '1000';
        }
        
        // if((date.format("D/M/YYYY") == '3/1/2024') || (date.format("D/M/YYYY") == '4/1/2024') || (date.format("D/M/YYYY") == '5/1/2024') || (date.format("D/M/YYYY") == '8/1/2024')) {
        //     if(hours < 10 || hours >= 19){
        //         operatorPhoneNumber = '1000';
        //     }    
        // }
    }
                        
    if(operatorPhoneNumber == '1000') {
        if(day == 'воскресенье'){
            operatorPhoneNumber = '2222';
        } else if(day == 'суббота'){
            if(hours < 10 || hours >= 16){
                operatorPhoneNumber = '2222';
            }
        } else {
            if(hours < 9 || hours >= 21){
                operatorPhoneNumber = '2222';
            }
        }
    }
    
    //operatorPhoneNumber = '3887';
    
    var departmentName = "";
    switch (operatorPhoneNumber){
        case '2222':
            departmentName = "Отдел поддержки";
            break;
        case '1000':
            departmentName = "Оператора контакт центра";
            break;
        case '3024':
            departmentName = "Отдел по работе с ДМА сервисами";
            break;
        case '2200':
            departmentName = "Отдел голосового трейдинга";
            break;
        case '3887':
            departmentName = "Отдел поддержки";
            break;
        case '3888':
            departmentName = "Оператора";
            break;
        case '3820':
            departmentName = "Оператора банка";
            break;
        case '7924':
            departmentName = "Оператора тестирования";
            break;
        case '3411':
            departmentName = "Отдел обслуживания банковских карт";
            break;    
        default:
            departmentName = "Оператора";
    }
   
    return {'phoneNumber' : operatorPhoneNumber, 'departmentName' : departmentName};
}   

    