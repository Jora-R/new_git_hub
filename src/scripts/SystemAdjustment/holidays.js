function holidays(){
    var holidayDateList = [ 
                            '23/5/2024', 
                            '24/5/2024', 
                            '25/5/2024', 
                            '26/5/2024', 
                            '27/5/2024',
                        ];
                                
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
        
    var hours = +moment(time).format("H");
    var day = date.locale("ru").format("dddd"); 
        
    var todayCurrentDate = date.format("D/M/YYYY");
    var indexOf = holidayDateList.indexOf(todayCurrentDate);

    if(indexOf > -1){
        // $reactions.answer("С 22 по 25 марта 2024 года – праздничные дни в Казахстане, торги и расчеты с валютными парами с казахстанским тенге не проводятся.");
        return true;
    } 
    return false;
}
