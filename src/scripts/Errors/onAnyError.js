bind("onAnyError", function($context) {
    $analytics.setSessionData("Ошибка прочее", "Теги действий");
    var answers = [
        "Во время обработки вашего запроса произошла ошибка. Пожалуйста, перефразируйте свой вопрос, или напишите оператор.",
        "Информация по вашему запросу не найдена! Пожалуйста, перефразируйте свой вопрос, или напишите «оператор»."
    ];
    var randomAnswer = answers[$reactions.random(answers.length)];
    $reactions.answer(randomAnswer);
});