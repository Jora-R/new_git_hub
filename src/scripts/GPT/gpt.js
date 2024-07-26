var copilot = new function () {
    // URL необходимый для запросов к CoPilot.
    var baseUrl = "https://app.jaycopilot.com/api/appsAdapter/conversations/"
    
    
    // Функция создания диалога с приложением "Прямой доступ к нейросетям. ChatGPT".
    function initConversation(systemPrompt) {
        var headers = {
            // Получение токена от CoPilot из раздела "Токены и переменные".
            "x-api-key": $secrets.get("gptToken", "Токен не найден")
        };
        var body = {
            "app": {
                "template": "yandexGpt",
                "params": {
                    "modelName": "YandexGPT",
                    "temperature": 0.5,
                    "topP": 1,
                    "presencePenalty": 0,
                    "frequencyPenalty": 0,
                    "systemPrompt": systemPrompt,
                    "maxTokens": 1536
                    // "maxInputTokensGpt4": 6000,
                    // "maxTokensGpt4": 1024
                }
            }
        };
        var res = $http.post(baseUrl, { headers: headers, body: body });
        if (!res.isOk || !res.data) throw new Error("Error calling Jay Copilot API");
        return res.data;
    };
    
    // Функция отправки запроса в созданный диалог.
    function conversate(conversationId) {
        var headers = {
            "x-api-key": $secrets.get("gptToken", "Токен не найден")
        };
        var form = {
            "text": $jsapi.context().request.query
        };
        var res = $http.post(baseUrl + conversationId + "/message", { headers: headers, form: form, timeout: 25000 });
        if (!res.isOk || !res.data) return "К сожалению, я не нашел ответ на ваш вопрос. Задайте другой вопрос или напишите \"оператор\"."
        return res.data.content[0].text;
    };

    // Функция удаления созданного диалога.
    function deleteConversation(conversationId) {
        var headers = {
            "x-api-key": $secrets.get("gptToken", "Токен не найден")
        };
        var res = $http.delete(baseUrl + conversationId, { headers: headers });
        if (!res.isOk) throw new Error("Error calling Jay Copilot API");
        return res;
    };
    
    this.requestAnswerGpt = function () {
        var systemPrompt = "Представь что ты работник службы поддержки российской брокерской компании Финам. Твоя задача отвечать на вопросы клиента и помогать ему разобраться с тороговлей на фондовой и срочной биржах. Пытайся давать максимально лаконичные и простые ответы на вопросы от клиента. Тебя зовут Виртуальный консультант"
        var conversation = initConversation(systemPrompt);
        // Id диалога с приложением, который мы получили при создании диалога.
        var conversationId = conversation.id;

        var res = conversate(conversationId);
        deleteConversation(conversationId);
        return res;
    };
};
