function educationalCenter(){
    var holidayDateList = [  
                            '29/4/2024', 
                            '30/4/2024', 
                            '1/5/2024',
                            '9/5/2024', 
                            '10/5/2024',
                            '12/6/2024',
                        ];
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
    
    var hours = +moment(time).format("H");
    var day = date.locale("ru").format("dddd");
    
    var todayCurrentDate = date.format("D/M/YYYY");
    var indexOf = holidayDateList.indexOf(todayCurrentDate);
    var isVip = $jsapi.context().request.data.isVip;
    
    if((isVip != true) && (day != 'воскресенье') && ((day != 'суббота') || (todayCurrentDate == '27/4/2024')) && (indexOf === -1) && (hours >= 10 && hours < 18)){ //если будний день будет выходным, то добавить (date.format("D/M/YYYY") != '1/1/2024')
        // $jsapi.context().session.to_division = "150ee7d9-1250-498f-ab33-2826f446b851"; // dev1;
        $jsapi.context().session.to_division = "2ed03073-02cf-4702-a47a-5b7178855100"; // УЦ prod;
    } else {
        segment();
    }

}