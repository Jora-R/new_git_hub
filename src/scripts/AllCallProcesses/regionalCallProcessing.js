// общая функция для окончательного определения добавочного при переводе на регион
function regionalOfficeCall(officePhone) {
    
    var regionalPhones = officePhone.split(',');

    // инициализация звонка в соответствии с массивом номеров
    for(var i = 0; i < 1 ; i++){
        var a = getOperatorFromNumberAndCurrentTime(regionalPhones[i]);
        var b = getPhoneByDateTime(a);
        
        $jsapi.context().session.officePhone = b.departmentName;
        $jsapi.context().session.noSegment = true;
        callProcessing(b.phoneNumber);
        
        return 0;
    }
    
    $context.session = {};
    $reactions.transition("/");
}