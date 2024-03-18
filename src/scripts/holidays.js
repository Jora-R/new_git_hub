function holidays(){
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
    
    var hours = +moment(time).format("H");
    var day = date.locale("ru").format("dddd");

    if((date.format("D/M/YYYY") == '28/2/2024') || (date.format("D/M/YYYY") == '6/3/2024') || (date.format("D/M/YYYY") == '7/3/2024') || (date.format("D/M/YYYY") == '8/3/2024')){
        // $reactions.answer("");
        return true;
    } 
    return false;
}