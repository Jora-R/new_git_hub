// function segment(){
//     var time = $jsapi.timeForZone("Europe/Moscow");
//     var date = currentDate();
    
//     var hours = +moment(time).format("H");
//     var day = date.locale("ru").format("dddd");
//     var isVip = $jsapi.context().request.data.isVip;
//     var ID_button = $jsapi.context().request.data.pressedButtonId;
//     // isVip = true; // поле используется для теста нажатой кнопки рассылки
//     if(ID_button == "" && $jsapi.context().session.to_division == undefined ){
//         if((isVip == true) && ((day != 'воскресенье') && (day != 'суббота')) && (date.format("D/M/YYYY") != '3/3/2024') && (date.format("D/M/YYYY") != '8/3/2024') && (hours >= 10 && hours < 21)){ //если будний день будет выходным, то добавить (date.format("D/M/YYYY") != '1/1/2024')
//             // $jsapi.context().session.to_division = "150ee7d9-1250-498f-ab33-2826f446b851"; // премиум дев1;
//             $jsapi.context().session.to_division = "beb72565-ddaa-4fe1-9958-44b1c05467ac"; // премиум прод
//         } else {
//             // $jsapi.context().session.to_division = "abffdc56-aa1f-4fa8-bb19-ee52349c4bdc"; //кц дев3
//             $jsapi.context().session.to_division = "2dec6d3f-1def-42ee-a6ec-a5e19addab04"; //кц прод
//         }
//         // if((isVip == true) && ((date.format("D/M/YYYY") == '1/1/2024') || (date.format("D/M/YYYY") == '2/1/2024')) && (hours >= 10 && hours < 19)){
//         //     $jsapi.context().session.to_division = "150ee7d9-1250-498f-ab33-2826f446b851"; // премиум дев1;
//         //     // $jsapi.context().session.to_division = "beb72565-ddaa-4fe1-9958-44b1c05467ac"; // премиум прод
//         // } // для рабочих дней в сб и вс или особенных пн-пт
//     }
// }

function segment(){
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
    
    var hours = +moment(time).format("H");
    var day = date.locale("ru").format("dddd");
    var isVip = $jsapi.context().request.data.isVip;
    var ID_button = $jsapi.context().request.data.pressedButtonId;
    // isVip = true; // поле используется для теста нажатой кнопки рассылки
    if(ID_button == "" && $jsapi.context().session.to_division == undefined ){
        if((isVip == true) && ((day != 'воскресенье') && (day != 'суббота')) && (date.format("D/M/YYYY") != '3/3/2024') && (date.format("D/M/YYYY") != '8/3/2024') && (hours >= 10 && hours < 21)){ //если будний день будет выходным, то добавить (date.format("D/M/YYYY") != '1/1/2024')
            // $jsapi.context().session.to_division = "150ee7d9-1250-498f-ab33-2826f446b851"; // премиум дев1;
            $jsapi.context().session.to_division = "beb72565-ddaa-4fe1-9958-44b1c05467ac"; // премиум прод
        } else {
            // $reactions.answer(JSON.stringify(profileCRM().Result.ClientStatus));
            if(((profileCRM() != undefined) && (profileCRM().Result.ClientStatus == null)) && ((day != 'воскресенье') && (day != 'суббота')) && (date.format("D/M/YYYY") != '3/3/2024') && (date.format("D/M/YYYY") != '8/3/2024') && (hours >= 10 && hours < 19)){
                // $jsapi.context().session.to_division = "5ec8de6b-7786-49b5-a3e5-d7bcc938cbb4"; //поддержка дев2
                $jsapi.context().session.to_division = "ee7dc4b0-81f9-40dd-8c35-d424fc087649"; //поддержка прод
            } else {
                // $jsapi.context().session.to_division = "abffdc56-aa1f-4fa8-bb19-ee52349c4bdc"; //кц дев3
                $jsapi.context().session.to_division = "2dec6d3f-1def-42ee-a6ec-a5e19addab04"; //кц прод
                
            }
        }
        // if((isVip == true) && ((date.format("D/M/YYYY") == '1/1/2024') || (date.format("D/M/YYYY") == '2/1/2024')) && (hours >= 10 && hours < 19)){
        //     $jsapi.context().session.to_division = "150ee7d9-1250-498f-ab33-2826f446b851"; // премиум дев1;
        //     // $jsapi.context().session.to_division = "beb72565-ddaa-4fe1-9958-44b1c05467ac"; // премиум прод
        // } // для рабочих дней в сб и вс или особенных пн-пт
    }
}
