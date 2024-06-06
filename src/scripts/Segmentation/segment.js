function segment(phoneNumber){
    var phoneNumberFinal = phoneNumber;
    
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
    var day = date.locale("ru").format("dddd");
    var hours = +moment(time).format("H");
    var minutes = +moment(time).format("m");
    
    var exceptionPhoneNumber = ["3400", "2200", "3411", "3887", "1111", "3333"];//перечень отделов исключений, на них сегментация не распространяется
    
    var holidayDayList =    [
                            '29/4/2024', 
                            '30/4/2024', 
                            '1/5/2024',
                            '9/5/2024', 
                            '10/5/2024',
                            ];
    var indexOf = holidayDayList.indexOf(date.format("D/M/YYYY"));
    // Проверка на наличие флага принудительной отмены сегментации
    if($jsapi.context().session.noSegment){
        return phoneNumberFinal;
    }
    // Проверка что выбранный добавочный не является исключением
    if(exceptionPhoneNumber.indexOf(phoneNumberFinal) !== -1){
        return phoneNumberFinal;
    }
    
    if((day != 'воскресенье') && ((day != 'суббота') || (date.format("D/M/YYYY") == "27/4/2024")) && (indexOf === -1) && (hours >= 20 && hours < 21)){
        var resultDetokenization = phoneNumberDetokenization();
        if(resultDetokenization == "error"){
            $analytics.setMessageLabel("Детокенизация не прошла VB", "Сегмент");
            $jsapi.context().session.officePhone = "Оператора поддержки";
            $jsapi.context().session.departmentName = $jsapi.context().session.officePhone;
            phoneNumberFinal = "2222";
        } else {
            var resultGetGlobalID = getGlobalID(resultDetokenization);
            if((resultGetGlobalID == "error1") || (resultGetGlobalID == "error2")){
                $analytics.setMessageLabel("Отсутствует GlobalID VB", "Сегмент");
                $jsapi.context().session.officePhone = "Оператора поддержки";
                $jsapi.context().session.departmentName = $jsapi.context().session.officePhone;
                phoneNumberFinal = "2222";
            } else {
                var resultGetProfileCRM = getProfileCRM(resultGetGlobalID);
                if(resultGetProfileCRM == "error"){
                    $analytics.setMessageLabel("Нет профиля CRM VB", "Сегмент");
                    $jsapi.context().session.officePhone = "Оператора поддержки";
                    $jsapi.context().session.departmentName = $jsapi.context().session.officePhone;
                    phoneNumberFinal = "2222";
                } else {
                    if((resultGetProfileCRM != undefined) && (resultGetProfileCRM.Result.ClientStatus != null)){
                        $analytics.setMessageLabel("Клиент со статусом VB", "Сегмент");
                        $jsapi.context().session.officePhone = "Оператора поддержки";
                        $jsapi.context().session.departmentName = $jsapi.context().session.officePhone;
                        phoneNumberFinal = "2222";    
                    } else {
                        $analytics.setMessageLabel("Клиент без статуса VB", "Сегмент");
                        $jsapi.context().session.officePhone = "Оператора поддержки";
                        $jsapi.context().session.departmentName = $jsapi.context().session.officePhone;
                        phoneNumberFinal = "3666";
                    }
                }
            }
        }
    } 
    $jsapi.context().session.operatorPhoneNumber = phoneNumberFinal;
    return phoneNumberFinal;
}