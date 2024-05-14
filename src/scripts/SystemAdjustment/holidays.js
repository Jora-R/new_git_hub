function holidays(){
    var holidayDateList = [ 
                            '25/4/2024',
                            '26/4/2024',
                            '27/4/2024', 
                            '28/4/2024', 
                            '29/4/2024', 
                            '30/4/2024', 
                            '1/5/2024', 
                            '2/5/2024', 
                            '3/5/2024', 
                            '4/5/2024', 
                            '5/5/2024', 
                            '6/5/2024', 
                            '7/5/2024', 
                            '8/5/2024', 
                            '9/5/2024', 
                            '10/5/2024',
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
