bind("onScriptError", function($context) {
    // $reactions.answer(
    //         "Есть ошибка"
    //     );
    // $reactions.answer(
    //         $context.exception.message
    //     );   
    $analytics.setSessionData("Ошибка скрипта", "Теги действий");
    $reactions.answer("Во время обработки вашего запроса произошла ошибка. Пожалуйста, перефразируйте свой вопрос, или напишите оператор.");
});