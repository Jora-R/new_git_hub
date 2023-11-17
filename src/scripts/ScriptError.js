bind("onScriptError", function($context) {
    $analytics.setSessionData("Ошибка скрипта", $context.exception.message);
    // $reactions.answer("Ошибка скрипта");
});
