bind("selectNLUResult", function($context) {
    
    checkLengthMessage($context);
   
});

function checkLengthMessage($context) {
    if(!checkQuery()) {
        return 0;
    }
    // проверка на мало текста (три и меньше)
    if($jsapi.context().request.query.length < 4) {
        // if ($context.nluResults.intents.length > 0) {
        //     // Если есть хотя бы один результат от классификатора на интентах, используем первый результат.
        //     $context.nluResults.selected = $context.nluResults.intents[0];
        //     return;
        // }
        if ($context.nluResults.patterns.length > 0) {
            // Если результата от интентов нет, используем результат от паттернов.
            $context.nluResults.selected = $context.nluResults.patterns[0];
            return;
        }
        
        $jsapi.context().temp.targetState = "/Уточнение вопроса";
    }
}