bind("preMatch", function($context) {
    // var query = $jsapi.context().request.query;
    // var entities = $jsapi.context().entities;
    // // var numberEntity = entities.find(function(entity) {
    //     return entity.pattern == "duckling.number";
    // });
    // if (numberEntity) {
    //     var numberValue = parseFloat(numberEntity.value);
    //     query = replaceWordsWithNumber(query, numberValue);
    // }
    // $jsapi.context().request.query = query;

    removeSymbols();
    // $reactions.answer($context.request.query);
    // log();
});

var regexp_trash_all = /(пожалуйста|пжл|извините|извините пожалуйста|извиняюсь|простите|простите пожалуйста|прошу прощения|будьте добры|не могли бы вы|если вас не затруднит|будьте любезны|будьте так любезны|разрешите|подскажите|помогите|сорри|сорян|уточните|уточнить|хотел бы уточнить|конкретно|проясните|проясните|ситуацию|помогите прояснить ситуацию|спросить|суть | ах | ох |а что ж| ай |ай да|ась | ау |ах ты|ахти|ба |баста|бац | бр |бугага|вау|вах|вуаля|гля|дык| ё |ё-моё|ей-богу|ёклмн|ёксель-моксель|ёлы-палы|ёпрст|ёшкин кот|йоу|как бы не так|кис-кис|ко-ко-ко|крутяк| ля |ля-ля-ля| м |м-да|м-м|мама миа|мяу|н-да| ну |ну-ка|ну-кась|ну-кася|ну-кось|ну-ну| о |о-го-го|о-ля-ля|о-о|о-хо-хо|оба-на| ого | ой |ой-ой| опа |опаньки|опля| оу | ох |охти|подожди|постой|пфу|пшш|слышь|тик-так| тс |тсс|тьфу| тю |увы|упс|уф|ух|фи|фу-ты|фу-фу|фух фьють| ха |ха-ха|ха-ха-ха| хе |хе-хе|хех| хи |хи-хи| хм |хо-хо|чур| э |э-ге-ге|э-хе-хе|э-э|э-э-э|скажите|напомните|собственно|вопрос|вопрос|возник|тем не менее|вдруг|ведь|весь|вообще|вопрос| вот|впрочем|затем|зато|каждая|каждое|каждые|каждый|кажется|казаться|лишь|многочисленная|многочисленное|многочисленные|многочисленный|наиболее|наконец|недавно|непрерывно|нередко|обычно|однажды|однако|опять|особенно|отовсюду|очень|пора|почти|просто|совсем|теперь|чуть|абсолютно|в общем|вдобавок)/gi;


function replaceWordsWithNumber(query, numberValue) {
    var words = query.split(" ");
    for (var i = 0; i < words.length; i++) {
        var word = words[i];
        if (!isNaN(word)) {
            words[i] = numberValue.toString();
        }
    }
    return words.join(" ");
}

function removeSymbols() {
    if(!checkQuery()) {
        return 0;
    }
    
    if($jsapi.context().request.query == "/start") {
        return 0;
    }
    $jsapi.context().request.query = $jsapi.context().request.query.replace(/[\!\«\»\"\#\%\&\'\(\)\*\+\,\-\.\/\:\;\<\=\>\?\@\[\\\]\^\_\`\{\|\}\~\n]/g, "");
    $jsapi.context().request.query = $jsapi.context().request.query.replace(regexp_trash_all, ".");

}

function checkQuery(){
    if($jsapi.context().request.query === undefined) {
        return false;
    }
    else {
        return true;
    }
}