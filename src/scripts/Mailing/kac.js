function kac(){
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
    
    if((day != 'воскресенье') && ((day != 'суббота') || (todayCurrentDate == '27/4/2024')) && (indexOf === -1) && (hours >= 10 && hours < 19)){ //если будний день будет выходным, то добавить (date.format("D/M/YYYY") != '1/1/2024')
        // $jsapi.context().session.to_division = "150ee7d9-1250-498f-ab33-2826f446b851"; // dev1;
        $jsapi.context().session.to_division = "029adcf9-a246-4742-aad0-24f9c2b35791"; // КАЦ prod;
    } else {
        segment();
    }
    // if(((date.format("D/M/YYYY") == '1/1/2024') || (date.format("D/M/YYYY") == '2/1/2024')) && (hours >= 10 && hours < 19)){
    //      $jsapi.context().session.to_division = "5ec8de6b-7786-49b5-a3e5-d7bcc938cbb4"; // dev2;
        // $jsapi.context().session.to_division = "029adcf9-a246-4742-aad0-24f9c2b35791"; // КАЦ prod;
    // } // для рабочих дней в сб и вс или особенных пн-пт

}