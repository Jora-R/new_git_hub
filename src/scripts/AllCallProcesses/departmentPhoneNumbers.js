function getPhoneByDateTime(operatorPhoneNumber){
    
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
    
    var hours = +moment(time).format("H");
    var day = date.locale("ru").format("dddd");
    //  $reactions.answer(date.format("D/M/YYYY"));
                        
    // Юр лица банка ИСКЛЮЧЕН
    if(operatorPhoneNumber == '3820') {
        
        if((day == 'воскресенье') || ((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024'))){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
    }
    
    // Банк ПК ТОЛЬКО в открытии счетов                
    if(operatorPhoneNumber == '3888') {
        
        if(day == 'воскресенье'){
            operatorPhoneNumber = '1000';
        } else if((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024')){
            if(hours < 10 || hours >= 16){
                operatorPhoneNumber = '1000';
            }
        } else {
            if(hours < 9 || hours >= 19){
                operatorPhoneNumber = '1000';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
        
        // праздники - сокращенный день в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024')) {
            if(hours < 10 || hours >= 19){
                operatorPhoneNumber = '1000';
            }    
        }
    }
    
    // Голосовой трейдинг                    
    if(operatorPhoneNumber == '2200') {
        if(day == 'воскресенье'){
            operatorPhoneNumber = '1000';
        } else if((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024')){
            //if(hours > 0){
                operatorPhoneNumber = '1000';
            //}
        } else {
            if(hours < 7 && hours >= 0){
                operatorPhoneNumber = '1000';
            }
        }
    }
    
    // DMA сервисы
    if(operatorPhoneNumber == '3024') {
        if((day == 'воскресенье') || ((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024'))){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 10 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
        
        // праздники - сокращенный день в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024')) {
            if(hours < 10 || hours >= 19){
                operatorPhoneNumber = '1000';
            }    
        }
    }
    
    // УК ТОЛЬКО в открытии счетов
    if(operatorPhoneNumber == '2131') {
        if((day == 'воскресенье') || ((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024'))){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
        
        // праздники - сокращенный день в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024')) {
            if(hours < 10 || hours >= 19){
                operatorPhoneNumber = '1000';
            }    
        }
    }
    
    // КАЦ
    if(operatorPhoneNumber == '3822') {
        if((day == 'воскресенье') || ((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024'))){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
        
        // праздники - сокращенный день в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024')) {
            if(hours < 10 || hours >= 19){
                operatorPhoneNumber = '1000';
            }    
        }
    }
    
    // КУ Лайт и прочее
    if(operatorPhoneNumber == '4000') {
        if((day == 'воскресенье') || ((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024'))){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 9 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
        
        // праздники - сокращенный день в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024')) {
            if(hours < 10 || hours >= 19){
                operatorPhoneNumber = '1000';
            }    
        }
    }
    
    // Контакт центр                    
    if(operatorPhoneNumber == '1000') {
        if(day == 'воскресенье'){
            operatorPhoneNumber = '2222';
        } else if((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024')){
            if(hours < 10 || hours >= 16){
                operatorPhoneNumber = '2222';
            }
        } else {
            if(hours < 9 || hours >= 21){
                operatorPhoneNumber = '2222';
            }
        }
    }
    
    // Брокер АО Поддержка - для клиентов без статуса
    if(operatorPhoneNumber == '3666') {
        if(day == 'воскресенье'){
            operatorPhoneNumber = '2222';
        } else if((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024')){
            if(hours < 10 || hours >= 16){
                operatorPhoneNumber = '2222';
            }
        } else {
            if(hours < 10 || hours >= 21){
                operatorPhoneNumber = '2222';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024') || (date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
    }
    
    // Брокер АО Поддержка - сложные вопросы
    if(operatorPhoneNumber == '3777') {
        if(day == 'воскресенье'){
            operatorPhoneNumber = '2222';
        } else if((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024')){
            if(hours < 10 || hours >= 16){
                operatorPhoneNumber = '2222';
            }
        } else {
            if(hours < 9 || hours >= 21){
                operatorPhoneNumber = '2222';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024') || (date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
    }
    
    // Брокер Переводы ББ
    if(operatorPhoneNumber == '3889') {
        if((day == 'воскресенье') || ((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024'))){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 10 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
        
        // праздники - сокращенный день в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024')) {
            if(hours < 10 || hours >= 19){
                operatorPhoneNumber = '1000';
            }    
        }
    }
    
    // Форекс Продажи
    if(operatorPhoneNumber == '3891') {
        if((day == 'воскресенье') || ((day == 'суббота') && (date.format("D/M/YYYY") != '27/4/2024'))){
            operatorPhoneNumber = '1000';
        } else {
            if(hours < 10 || hours >= 19) {
                operatorPhoneNumber = '1000';
            }
        }
        
        // праздники - выходные дни в дату
        if((date.format("D/M/YYYY") == '29/4/2024') || (date.format("D/M/YYYY") == '30/4/2024') || (date.format("D/M/YYYY") == '10/5/2024') || (date.format("D/M/YYYY") == '1/5/2024') || (date.format("D/M/YYYY") == '9/5/2024')) {
            operatorPhoneNumber = '1000';
        }
    }
    
    //operatorPhoneNumber = '2222'; // Брокер АО - Поддержка общая
    //operatorPhoneNumber = '3411'; // Банк - Карты
    //operatorPhoneNumber = '3400'; // Банк - Карты
    //operatorPhoneNumber = '1111'; // Тех поддержка 1-я линия
    //operatorPhoneNumber = '3333'; // Тех поддержка 2-я линия
    //operatorPhoneNumber = '7924'; // ТЕСТ
    //operatorPhoneNumber = '3887'; // Форекс Поддержка
    
    // Наименование отдела при переводе на оператора
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
        case '3666':
            departmentName = "Отдел поддержки";
            break;
        case '3777':
            departmentName = "Отдел поддержки";
            break;
        case '3889':
            departmentName = "Оператора";
            break;
        case '3891':
            departmentName = "Оператора";
            break;    
        default:
            departmentName = "Оператора"; // если в списке добавочного нет, то озвучиваем стандартное наименование
    }
   
    return {'phoneNumber' : operatorPhoneNumber, 'departmentName' : departmentName};
}   

    