function holidays(){
    var holidayDateList = [ 
                            '12/7/2024', 
                            '13/7/2024', 
                            '14/7/2024', 
                            '15/7/2024', 
                            '12/6/2024',
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
