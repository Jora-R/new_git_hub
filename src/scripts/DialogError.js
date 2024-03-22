bind("onDialogError", function($context) {
    // $reactions.answer(
    //         "Есть ошибка"
    //     );
    // $reactions.answer(
    //         $context.exception.message
    //     );    
    if ($context.exception.message
            && $context.exception.message.startsWith("State not found for path")) {
        $analytics.setMessageLabel("Нет стейта", "Теги действий");       
        $reactions.answer(
            "Информация о «" + $context.request.query + "» не найдена! Пожалуйста, перефразируйте свой вопрос, или скажите оператор."
        );
    $context.session = {};
    }
});