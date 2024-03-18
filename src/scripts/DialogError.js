bind("onDialogError", function($context) {
    if ($context.exception.message
            && $context.exception.message.startsWith("State not found for path")) {
        $analytics.setMessageLabel("Нет стейта", "Теги действий");       
        $reactions.answer("Информация по вашему запросу не найдена! Пожалуйста, перефразируйте свой вопрос, или напишите «оператор».");
    $context.session = {};
    }
});