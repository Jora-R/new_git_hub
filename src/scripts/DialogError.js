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
            "Информация по вопросу «" + $context.request.query + "» не найдена! Попробуйте перефразировать свой вопрос."
        );
    $context.session = {};
    }
});