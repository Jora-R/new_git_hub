bind("preMatch", function($context) {
    
    if(($context.session.mailing != true) && ($jsapi.context().request.query != "/start") && ($jsapi.context().request.data.pressedButtonId != "") && ($jsapi.context().request.data.pressedButtonId != undefined)){
        mailling();
        $context.session.mailing = true;
        $context.temp.targetState = "/Пустота";
    }
    
    checkNewDay();
    checkHello();
    // checkLengthMessage();
    checkDoubleMessage();
    removeSymbols();
    
    // if($jsapi.context().session.crashed == undefined){checkCrashed();} // включить для активации сценария "Сбой"
    // crash(); // включить для активации сценария "Сбой"
});


var regexp = /(привет|приветствую|приветики|приветик|здравствуйте|здрасьте|здравствуй|доброе утро|доброго утра|утро доброе|утра доброго|добрый день|доброго дня|дня доброго|день добрый|добрый вечер|доброго вечера|вечер добрый|вечера доброго|доброй ночи|добрый ночи|ночи доброй|доброго времени суток|доброе время суток|салам алейкум|алейкум масалам|бонжур|хелоу|добый день|доблый день|добррый день|добрый|дд|прив|драсте|превед|здрасти|здр|start|ghbdtn|lj,hsq ltym|lj,hjt enhj|lj,hsq dtxth|re re|ку ку|hi|hello)/gi;
var regexp_trash = /(если|интересует|интересуют|напишите|напомните|например|объясни|объясните|объясните|планирую|подскажи|подскажите|подскажите|пожалуйста|помогите|посмотрите|посоветуйте|поясните|простите|просьба|прошу|разъясните|скажи|скажите|скажите|случайно|сориентируйте|тогда|уточните)/gi;

function checkCrashed(){
    $jsapi.context().session.crashed = true;
}

function crash() {
    
    if($jsapi.context().session.crashed){
        $reactions.transition("/Сбой");
        $jsapi.context().session.crashed = false;
        $jsapi.context().request.query = " "; //результат распознавания запроса клиента не учитывается
    }
}



function checkNewDay() {
    // $jsapi.context().client.currentDay = ""; //Если нужно принудительно сбросить день для теста
    if($jsapi.context().request.query != "/start" && $jsapi.context().request.query != "Подробнее" && $jsapi.context().request.query != "Перейдите по ссылке") {
        var day = currentDate().format("YYYY-M-D");
        if($jsapi.context().client.currentDay != day){
            $reactions.answer("Здравствуйте!");
            $jsapi.context().client.currentDay = day;
            $jsapi.context().session.currentDayHello = true;
        }else {
            $jsapi.context().session.currentDayHello = false;
        }
        
    }
}

function checkHello() {
    if(($jsapi.context().request.query != "/start") && !$jsapi.context().session.currentDayHello && $jsapi.context().request.query != undefined) {
        var result = $jsapi.context().request.query.match(regexp);
        if(result != undefined) {
            $reactions.answer("Здравствуйте!");
        }
    }
}

function removeSymbols() {
    if(!checkQuery()) {
        return 0;
    }
    
    if($jsapi.context().request.query == "/start") {
        return 0;
    }
    $jsapi.context().request.query = $jsapi.context().request.query.replace(/[\!\«\»\"\#\%\&\'\(\)\*\+\,\-\.\/\:\;\<\=\>\?\@\[\\\]\^\_\`\{\|\}\~\n]/g, " ");
    $jsapi.context().request.query = $jsapi.context().request.query.replace(regexp, " ");
    $jsapi.context().request.query = $jsapi.context().request.query.replace(regexp_trash, " ");

}

function checkDoubleMessage() {
    
    if($jsapi.context().request.query == "/start" || $jsapi.context().request.query == "Назад" || $jsapi.context().request.query == "В основное меню" || $jsapi.context().request.query == "Анекдот") {
        $jsapi.context().session.prevMessage = '';
        return 0;
    }
    $jsapi.context().session.prevMessage = $jsapi.context().session.prevMessage || '';
    
    //сравнение и выходж из функции
    if( $jsapi.context().session.prevMessage == $jsapi.context().request.query){
        $jsapi.context().temp.targetState = "/Перевод на оператора";
    }
    $jsapi.context().session.prevMessage = $jsapi.context().request.query;
}

function checkQuery(){
    if($jsapi.context().request.query === undefined) {
        return false;
    }
    else {
        return true;
    }
}