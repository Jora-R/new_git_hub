function regionalOfficeCall(officePhone, response, client) {
    
    var regionalPhones = officePhone.split(',');
    
    //проверка разбивки поля DATA на массив
    // for(var i = 0; i < regionalPhones.length ; i++){
        
    //     $reactions.answer(regionalPhones[i]);
    // }

    // инициализация звонка в соответствии с массивом номеров
    for(var i = 0; i < 1 ; i++){
        var a = getOperatorFromNumberAndCurrentTime(regionalPhones[i]);
        var b = getPhoneByDateTime(a);
        callProcessing(b.phoneNumber, response, client);
        
        return b.departmentName;
    }
    // $reactions.answer("К сожалению, не получилось соединить Вас с оператором представительства.");
    $context.session = {};
    $reactions.transition("/");
}