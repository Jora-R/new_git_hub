require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: dateTime/dateTime.sc
  module = sys.zb-common
require: name/name.sc
  module = sys.zb-common 
# Установка периода ожидания ответа клиента
require: ./scripts/SystemAdjustment/setTimeout.js
# Вывод ошибки при отсутствии сценария
require: ./scripts/Errors/DialogError.js
# Вывод ошибки в скрипте
require: ./scripts/Errors/ScriptError.js
# Функция зачистки перед распознаванием обращения
require: ./scripts/preMatch/preMatch.js
# Определение ответа по движению ДС и ЦБ
require: ./scripts/Intents/moneyTransfer.js
# Определение времени работы отдела
require: ./scripts/AllCallProcesses/departmentPhoneNumbers.js
# Определение времени работы офисов в регионах
require: ./scripts/AllCallProcesses/regionalCallProcessing.js
# Пребор добавочных региона + функция проверки и перевода
require: ./scripts/AllCallProcesses/regionalPhoneNumbers.js
# Расшифровка номера телефона из токена клиента в базе JustAI
require: ./scripts/Identification_APIs/phoneNumberDetokenization.js
# Получение глобал ID по номеру телефона клиента
require: ./scripts/Identification_APIs/getGlobalID.js
# Получение данных профиля клиента из СРМ
require: ./scripts/Identification_APIs/getProfileCRM.js
# Функция проверки сегмента клиента для перевода на оператора (с 20 до 21 мск)
require: ./scripts/Segmentation/segment.js
# Функция перевода на оператора (тут указан канал)
require: ./scripts/AllCallProcesses/callProcessing.js
# Функция активации праздничного текста для некоторых интентов. Для отключения - закомментировать.
require: ./scripts/Holidays/holiday.js
# Функция отправки сообщения в чат TxChat
require: ./scripts/MessagesTo_TXchat/sendMessageTxchatHttp.js
# Функция проверки пренадлежит ли телефон клиенту АО
require: ./scripts/Identification/AOcheck.js
# Функция идентификации клиента (объединяет все функции проверки данных клиента)
require: ./scripts/Identification/identification.js


theme: /
    
    state: IVR меню бота тест
        q!: меню бота тест
        script:
            
            $client.resultIdentification = identification();
            
            $reactions.transition("/IVR меню бота"); // IVR меню бота
    
    state: IVR меню бота
        q!: меню бота

        script:
            $analytics.setMessageLabel("Всего", "IVR VB");

            if ($client.profileCRM == undefined){
                //$reactions.answer("не клиент");
                $reactions.transition("/IVR меню бота/НЕ клиент");
            } else if ($client.profileCRM.Result.IsVip == true){
                //$reactions.answer("премиум");
                $reactions.transition("/IVR меню бота/ПРЕМИУМ");
            } else if ($client.profileCRM.Result.ClientStatus == null){
                //$reactions.answer("нет статуса");
                $reactions.transition("/IVR меню бота/НЕТ статуса");
            } else if ($client.profileCRM.Result.ClientStatus == "Престиж"){
                //$reactions.answer("статус есть и = престиж");
                $reactions.transition("/IVR меню бота/Престиж");
            } else {
                //$reactions.answer("статус есть и НЕ = престиж");
                $reactions.transition("/IVR меню бота/НЕ Престиж");
            }
        
        state: НЕ клиент
            a: Вас приветствует голосовой помощник, группы компаний фина'м.
            script:
                $analytics.setMessageLabel("НЕ клиент", "IVR VB");
                $session.needBot =  'false';
                $reactions.transition("/Ввод добавочного");
        
        state: ПРЕМИУМ
            if: (($client.getGlobalID.firstName != "") && ($client.getGlobalID.middleName != ""))
                a: {{$client.getGlobalID.firstName}} {{$client.getGlobalID.middleName}}
            a: Вас приветствует, выделенная линия поддержки, для ПРЕМИУМ клиентов, группы компаний фина'м.
            script:
                $analytics.setMessageLabel("ПРЕМИУМ", "IVR VB");
                $session.needBot =  'false';
                $reactions.transition("/Ввод добавочного");
            
        state: НЕТ статуса
            if: (($client.getGlobalID.firstName != "") && ($client.getGlobalID.middleName != ""))
                a: {{$client.getGlobalID.firstName}} {{$client.getGlobalID.middleName}}
            a: Вас приветствует голосовой помощник, группы компаний фина'м.
            script:
                $analytics.setMessageLabel("НЕТ статуса", "IVR VB");
                $session.needBot =  'true';
                $reactions.transition("/Ввод добавочного");
            
        state: Престиж
            if: (($client.getGlobalID.firstName != "") && ($client.getGlobalID.middleName != ""))
                a: {{$client.getGlobalID.firstName}} {{$client.getGlobalID.middleName}}
            a: Вас приветствует, выделенная линия поддержки, для ПРЕСТИЖ клиентов, группы компаний фина'м.
            script:
                $analytics.setMessageLabel("Престиж", "IVR VB");
                $session.needBot =  'false';
                $reactions.transition("/Ввод добавочного");
                
        state: НЕ Престиж
            if: (($client.getGlobalID.firstName != "") && ($client.getGlobalID.middleName != ""))
                a: {{$client.getGlobalID.firstName}} {{$client.getGlobalID.middleName}}
            a: Вас приветствует голосовой помощник, группы компаний фина'м.
            script:
                $analytics.setMessageLabel("НЕ Престиж", "IVR VB");
                $session.needBot =  'false';
                $reactions.transition("/Ввод добавочного");
            
    state: Ввод добавочного
        a: Пожалуйста, наберите в тональном режиме номер сотрудника, после звукового сигнала; или оставайтесь на линии.
        audio: https://www.finam.ru/files/u/dw/files/chatbot/shortsignal/aba071e65865c3d.wav
        script:
            $response.replies = $response.replies || [];
            $response.replies.push({
                "type": "dtmf",
                "max": 4,
                "timeout": 10000
             });
    
        state: Digits
            q: $regexp<\d+>
            # a: Вы набрали номер {{$parseTree.text}}.
            a: Обращаем ваше внимание, представленная в телефонном разговоре информация не является индивидуальной инвестиционной рекомендацией.
            script:
                if(($request.query == "1945") || ($request.query == "*194") || ($request.query.length < 4)){
                    $reactions.answer("Мне не удалось найти указанный номер");
                    $analytics.setMessageLabel("Добавочный с ошибкой", "IVR VB");
                    $session.operatorPhoneNumber = '1000';
                    $reactions.transition("/Оператор/Оператор по номеру");
                } else {
                    $analytics.setMessageLabel("Ввел добавочный", "IVR VB");
                    $session.operatorPhoneNumber = $parseTree.text;
                    $reactions.transition("/Оператор/Оператор по номеру");
                }
    
        state: NoInput
            event: noDtmfAnswerEvent
            a: Обращаем ваше внимание, представленная в телефонном разговоре информация не является индивидуальной инвестиционной рекомендацией.
            script:
                $analytics.setMessageLabel("НЕ ввел добавочный", "IVR VB");
                if ($session.needBot == 'false'){
                   $session.operatorPhoneNumber =  '1000';
                   $reactions.transition("/Оператор/Оператор по номеру");
                } else {
                    $reactions.transition("/YL_FL");
                }
    
    state: Start
        q!: $regex</start>
        script:
            $client.resultIdentification = identification();
            
            $reactions.transition("/IVR меню бота"); // IVR меню бота
            
            # $reactions.answer("Вас приветствует голосовой помощник фина'м!"); // БЕЗ IVR меню бота
            # $reactions.transition("/YL_FL"); // БЕЗ IVR меню бота
            
            
    # state: Очистка данных клиента
    #     q!: обнули
    #     a: Данные клиента очищены
    #     script:
    #         $context.client = {};


    state: YL_FL
        a: Уточните, пожалуйста, вы обращаетесь как физическое или юридическое лицо?
        
        q: * @FL * ||toState = "/FL"
        q: * @YL * ||toState = "/YL"
        q: * @choice_1 * ||toState = "/FL"
        q: * @choice_2 * ||toState = "/YL"
        q: * @choice_last * ||toState = "/YL"
        q: @repeat_please * ||toState = "."
        
    state: YL
        script:
            $analytics.setMessageLabel("ЮЛ", "Тип клиента VB");
            $session.operatorPhoneNumber =  '1000';
            $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario
        
    state: FL
        a: Пожалуйста, опишите коротко суть вопроса.
        script:
            $analytics.setMessageLabel("ФЛ", "Тип клиента VB");
        
    state: Hello
        q!: @hello
        a: Здравствуйте! Уточните, пожалуйста, ваш вопрос.
        
    state: Другой_вопрос
        q!: -
        q!: * @another_question
        random:
            a: Пожалуйста, опишите коротко суть вопроса.
            a: Позвольте мне вам помочь. Какой у вас вопрос?
        
    state: Я робот
        q!: * @robot *    
        a: Я голосовой помощник компании Фина'м. Какой у вас вопрос?
        
    state: Открытие_закрытие_счета
        intent!: /016 Открытие_закрытие_счета
        script:
            $analytics.setMessageLabel("016 Открытие_закрытие_счета", "Интенты");
                        
            if ( typeof $parseTree._open_close != "undefined" ){
                $session.open_close = $parseTree._open_close;
            }
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }            
            if ( typeof $session.open_close == "undefined" ){
                $reactions.transition("/Открытие_закрытие_счета/Уточнение открыть_закрыть");
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Открытие_закрытие_счета/Уточнение компании");
            } else {

                var phoneNumber = $session.company.phoneNumber; //Фиксация данных во временных переменных, т.к. далее зачищаем значения всех переменных сессии
                var open_closeName = $session.open_close.name; //Используем данный алгоритм, так как даже при последовательной записи действие/зачистка данных сама зачистка происходит раньше
                var companyName = $session.company.name;
                $session = {}; //Зачистка данных сессии
                $session.operatorPhoneNumber = phoneNumber; //Записываем обратно добавочный из временной переменной в сессию, т.к. в функции перевода на оператора стандартизированое название переменной
                $reactions.transition("/Открытие_закрытие_счета/" + open_closeName + "_" + companyName);
            }
            
        state: Уточнение открыть_закрыть
            a: Уточните, пожалуйста, вы хотели бы открыть или закрыть счет?
            
            state: Ожидание ответа
                q: * @open_close *
                script:
                    $session.open_close = $parseTree._open_close;
                    $reactions.transition("/Открытие_закрытие_счета")
                
        state: Уточнение компании
            a: Операции с каким счетом вас интересуют; С БРОКЕРСКИМ счетом; Со счетом в банке, в управляющей компании, или счётом форекс.
            q: @repeat_please * ||toState = "."
                 
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Открытие_закрытие_счета")
        
        state: open_Банк
            a: Открыть банковский счет, депозит, или карту в банке фина'м, можно в офисе компании. Или дистанционно, если ранее, вы открывали, брокерский счет, лично, в офисе компании.
            a: Дистанционно подать заявку на открытие счета, можно в личном кабинете на сайте фина'м точка ру, кнопка открыть новый счет, находится под списком ваших открытых счетов.
            a: Далее выберите раздел, банковские продукты, далее выберите, карты, или тип счета.
            a: Обращаем ваше внимание, если вы не открывали ранее брокерский счет, или открывали его дистанционно, то открыть банковский счет, или карту, можно только при личном визите в офис компании фина'м.
            a: Получить банковскую карту можно в офисе компании, или курьерской доставкой, в зависимости от города получения. Доступный способ получения отображается при заказе банковской карты при выборе города.
            a: Хотите получить консультацию у оператора по открытию банковского счёта?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer

        state: open_Брокер
            a: Открыть брокерский счет в компании фина'м, можно дистанционно, или в офисе компании. Дистанционно подать заявку на открытие счета, можно на сайте, фина'м точка ру.
            a: Желтая кнопка, открыть счет, находится в верхнем правом углу страницы. Для заполнения анкеты понадобится мобильный телефон, и гражданский паспорт. Открыть дополнительный счет можно дистанционно в личном кабинете.
            a: Количество действующих стандартных брокерских счетов неограниченно, НО счет ИИС у физического лица может быть только один.
            a: Брокерские счета полностью независимы, по ним могут быть разные тарифы и торговые системы. Новый счет будет доступен для торговли через несколько часов, после подписания документов об открытии.
            a: Обращаем ваше внимание: открыть дистанционно, первичный счет, могут только совершеннолетние физические лица, граждане эРэФ и дружественных государств.
            a: Лицам до 18 лет, открытие счёта доступно только при личном посещении офиса, с родителем или опекуном.
            a: Хотите получить консультацию у оператора по открытию брокерского счёта?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
            
        state: open_УК
            a: Открыть счет доверительного управления в управляющей компании Фина'м, можно дистанционно в личном кабинете брокера, на сайте фина'м точка ру.
            a: Кнопка открыть новый счёт, находится слева под списком ваших открытых счетов. Далее выберите раздел Доверительное управление, и следуйте инструкциям сайта.
            a: Хотите получить консультацию у оператора по выбору стратегии управления активами и открытию счёта?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer

        state: open_Форекс
            a: Открытие счёта фина'м Форекс доступно совершеннолетним гражданам Российской федерации.
            a: Открыть форекс счет в компании фина'м, можно дистанционно, или в офисе компании, адрес ближайшего офиса можно посмотреть на сайте Фина'м точка ру, в разделе контактная информация, внизу страницы.
            a: Дистанционно подать заявку на открытие счета, можно на сайте форекс точка фина'м точка ру.
            a: Если у вас есть брокерский счет в компании Фина'м, вы можете дистанционно открыть счет форекс в личном кабинете брокера, на сайте едо'кс точка Фина''м точка ру.
            a: Для этого выберите, открыть новый счет, выберите тип компании форекс-диллер, далее следуйте инструкциям. Дополнительные счета «фина'м Форекс» можно открыть в личном кабинете Форекс на сайте форекс кабинет точка фина'м точка ру.
            a: Хотите получить консультацию у оператора?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
    
        state: close_Банк
            a: Закрыть банковский счет или карту можно в личном кабинете банка фина'м, на сайте айбанк точка фина'м точка ру.
            a: Для этого выберите нужный счет, в поле справа выберите, закрыть счет. Обращаем ваше внимание, что закрытие счетов, открытых до две тысячи двадцать третьего года, может быть доступно только в офисе компании.
            a: Хотите получить консультацию у оператора?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
        state: close_Брокер
            a: Закрыть брокерский счет можно дистанционно в личном кабинете едо'кс точка фина'м точка ру, для этого, в разделе, Услуги, выберите меню, Прочие операции.
            a: Договор расторгается на 5-й рабочий день с момента подписания заявления.
            a: Обращаем ваше внимание, что в рамках брокерского договора может быть несколько счетов. При расторжении все они будут закрыты.
            a: Закрытие счетов доступно при отсутствии на них активов, открытых позиций и задолженностей.
            a: Способ закрытия счёта ИИС зависит от желаемого типа налогового вычета. Хотите получить консультацию у оператора?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
 
        state: close_УК
            a: Закрыть счет доверительного управления в Управляющей компании можно дистанционно в личном кабинете едо'кс точка фина'м точка ру.
            a: Для этого, в разделе, Услуги, выберите меню, Операции по договорам доверительного управления, расторжение договора.
            a: Договор расторгается на 3-й рабочий день с момента подписания заявления, продажа активов с последующими расчетами, осуществляется в течение 10 дней с даты расторжения договора.
            a: В случае, если у вас в договоре заранее была указана дата расторжения, не забудьте предоставить реквизиты для перечисления средств в личном кабинете на сайте едо'кс точка фина'м точка ру, в разделе, Информация.
            a: Хотите получить консультацию у оператора?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
        state: close_Форекс
            a: Закрыть дополнительный счет форекс, можно дистанционно в личном кабинете на сайте форекс точка фина'м точка ру. 
            a: Чтобы закрыть единственный счет форекс, обратитесь к менеджеру фина'м.
            a: Хотите получить консультацию у оператора?
            # script: 
            #     $context.session = {};
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
    
    state: Заказ_документов
        intent!: /017 Заказ_документов
        
        script:
            $analytics.setMessageLabel("017 Заказ_документов", "Интенты");
            
            if ( typeof $parseTree._document != "undefined" ){
                $session.document = $parseTree._document;
            }            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.document == "undefined" ){
                $reactions.transition("/Заказ_документов/Уточнение типа документа");
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Заказ_документов/Уточнение компании");
            }
             else {
                $reactions.transition("/Заказ_документов/Заказ_" + $session.document.name + "_" + $session.company.name);
            }

        state: Уточнение типа документа
            a: Какой документ вас интересует?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @document *
                script:
                    $session.document = $parseTree._document;
                    $reactions.transition("/Заказ_документов");
    
        state: Уточнение компании
            a: Документы для какого счёта вас интересуют; Брокерского счёта; Банковского, счёта в Управляющей компании, или для счёта Форекс.
            q: @repeat_please * ||toState = "."    
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Заказ_документов");
            
        state: Заказ_справка_Брокер
            a: Заказать справку по брокерскому счету, можно в личном кабинете на сайте, фина'м точка ру; для этого выберите меню документы, далее выберите раздел, налоги и справки.
            a: Максимальный интервал получения справки по счету, 92 дня. При необходимости получить годовой отчет, справку можно сформировать 4 раза.
            a: В разделе, брокерский отчет, автоматически выгружаются отчеты брокера на подпись.
            a: Также, историю операций по счету, можно посмотреть в личном кабинете, для этого выберите нужный счет, далее выберите вкладку, история.
            a: Для заказа брокерского отчета на бумажном носителе обратитесь к менеджеру поддержки.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Заказ_счет_фактура_Брокер
            script: 
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_выписка_депо_Брокер
            a: Заказать выписки из депозитария, можно в личном кабинете на сайте, фина'м точка ру, для этого выберите меню документы, далее выберите раздел, налоги и справки, далее выберите раздел, депозитарий.
            a: Заказ документов оплачивается по тарифам депозитария, Выписка со счёта ДЕПО, или Выписка об операциях по счёту ДЕПО, 200 рублей.
            a: Изготовление, в течение трех рабочих дней. Заказ выписки из национального расчетного депозитария, 500 рублей, изготовление, в течение месяца.
            script:
                 if (identificationAO($client.profileCRM) == "success"){
                        $context.session.lastState = $context.currentState;
                        $session.questionText = "Хотите получить в чат ссылку для заказа в личном кабинете Выписки со счёта Депо?";
                        $session.insrtuctionText = "В продолжение разговора направляю вам [ссылку для заказа Выписки со счета ДЕПО|https://edox.finam.ru/Orders/DepoReport]";
                        $reactions.transition("/Отправка инструкции в чат");
                    }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Заказ_налог_справка_Брокер
            a: Заказать справку о доходах 2НДФЛ, и справку об убытках, можно за отчетный период, то есть один календарный год, в личном кабинете на сайте, фина'м точка ру.
            a: Для этого выберите меню, документы, далее выберите раздел, налоги и справки, далее выберите раздел, налоги.
            a: Электронный формат справки будет доступен в личном кабинете в течение трех рабочих дней. Изготовление справки на бумажном носителе в течение одной рабочей недели.
            a: Вы хотите узнать подробнее о содержимом справки 2НДФЛ?
            script: 
                $context.session = {};
            q: @agree ||toState = "/Заказ_документов/Заказ_налог_справка_Брокер/Подробнее_2НДФЛ"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
                
            state: Подробнее_2НДФЛ
                a: В справке 2НДФЛ, в разделе доход, содержится общая стоимость сделок продажи за отчетный период. В разделе Вычет, общая стоимость сделок покупки за отчетный период, а также комиссии, соответствующие коду дохода. 
                a: В разделе НалогООблага'емая база, указана итоговая прибыль, которая рассчитывается как разница дохода и вычета.
                a: Если в данной графе указано ноль, то за текущий отчетный период отсутствуют доходы и необходимо проверить справку об убытках.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                q: @repeat_please * ||toState = "."
                # final answer
    
        state: Заказ_справка_актив_Брокер
            a: Заказать справку об активах можно в личном кабинете на сайте, фина'м точка ру; для этого выберите меню, документы, далее выберите раздел, налоги и справки, далее выберите, Запрос на предоставление справки об активах. Изготовление справки занимает до двух рабочих дней.
            script:
                 if (identificationAO($client.profileCRM) == "success"){
                        $context.session.lastState = $context.currentState;
                        $session.questionText = "Хотите получить в чат ссылку для заказа в личном кабинете Справки об активах?";
                        $session.insrtuctionText = "В продолжение разговора направляю вам [ссылку для заказа Справки об активах|https://edox.finam.ru/orders/AssetStatement]";
                        $reactions.transition("/Отправка инструкции в чат");
                    }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Заказ_документы_откр_Брокер
            a: Заказать документы об открытии брокерского счета, можно в личном кабинете на сайте, едо'кс точка фина'м точка ру, для этого выберите меню отчетность, далее выберите раздел, основные документы. 
            a: Основными документами являются, Заявление о выборе условий обслуживания, Уведомление о заключении договора присоединения, Заявление о присоединении к регламенту, Уведомление для ИФНС.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Заказ_справка_госслуж_Брокер
            a: Заказать справку для гос служащего по форме 57 98 уу, можно в личном кабинете на сайте, фина'м точка ру, для этого выберите меню, документы, далее выберите раздел, налоги и справки, справка для гос служащих.
            a: Изготовление справки до пяти рабочих дней.
            a: Вы хотите узнать подробнее о содержимом справки для госслужащих?
            script: 
                $context.session = {};
            q: @agree ||toState = "/Заказ_документов/Заказ_справка_госслуж_Брокер/Подробнее_госслуж"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
                
            state: Подробнее_госслуж
                a: В первом разделе справки для гос служащего указаны сведения по банковским счета'м, соответственно при получении данной справки от брокера, раздел не заполняется. 
                a: Для получения сведений об остатках средств на брокерских счетах, можно заказать справку по счету в личном кабинете на сайте, фина'м точка ру, для этого выберите меню документы, далее выберите раздел, налоги и справки. 
                a: Во втором разделе указана информация о поставленных ценных бумагах, а также, сведения о доходах, налогах, дивидендах и купонах. Важно. Производные финансовые инструменты, фьючерсы и опционы, не являются ценными бумагами. 
                a: В третьем разделе указана информация об иных доходах, процентах на остаток, доходах от продажи валюты без учета расходов, доходах по драгоценным металлам и прочее. 
                a: В четвертом разделе указана информация о займах, сделках РЕПО' и иных обязательствах клиента и брокера перед клиентом, если они превышали сумму 1000000 рублей.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                q: @repeat_please * ||toState = "."
                # final answer
        
        state: Заказ_1042s_Брокер
            a: Справку формы 10 42 ЭС, формируют национальный расчетный депозитарий и СПБ-биржа, и направляют брокеру. 
            a: Готовые формы загружаются автоматически в личный кабинет на сайте, фина'м точка ру. Справка формируется за отчетный период, то есть за один календарный год. 
            a: Чтобы заказать справку в личном кабинете, выберите меню, документы, далее выберите раздел, налоги и справки, далее выберите раздел, налоги. 
            a: Электронный формат справки доступен на следующий рабочий день. Изготовление справки на бумажном носителе в течение одной рабочей недели.
            script:
             if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить в чат ссылку заказа в личном кабинете Формы 10 42 эс?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [ссылку для заказа Формы 1042S|https://edox.finam.ru/orders/Form1042S]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            # script:
            #     $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Заказ_w8_Брокер
            a: Подписать форму дабл ю 8 бэн, можно в личном кабинете на сайте, фина'м точка ру, для этого выберите меню, документы, далее выберите раздел, налоги и справки, Форма дабл ю 8 бэн. 
            a: Далее выберите биржу, сформируйте заявление, распечатайте, поставьте подпись, отсканируйте документ и вложите скан в сформированный вами документ в личном кабинете. 
            a: Сформировать документ, распечатать, подписать и прикрепить заявление необходимо в течение одного дня. Форма рассматривается 30 календарных дней.
            script:
             if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить в чат ссылку для заказа в личном кабинете Формы дабл ю 8 бэн?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [ссылку для заказа Формы W-8BEN|https://edox.finam.ru/Orders/FormW8BEN]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Заказ_выписка_квал_Брокер
            a: Если вы являетесь квалифицированным инвестором в Фина'м, вы можете заказать выписку из реестра квалифицированных лиц,
            a: в личном кабинете на сайте, едо'кс точка Фина'м точка ру. для этого выберите меню, Услуги, далее выберите раздел, Налоги, выписки, справки, в поле меню другОе, выберите, Выписка из реестра квалифицированных лиц.
            script:
             if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить в чат ссылку для заказа в личном кабинете Выписки из реестра квалифицированных инвесторов?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [ссылку для заказа Выписки из реестра квалифицированных лиц|https://edox.finam.ru/orders/QualifiedInvestorRequestStatementStatus]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Заказ_регламент_Брокер
            a: Регламент брокерского обслуживания представлен на сайте фина'м точка ру. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # заказ документов по Банку    
        state: Заказ_справка_Банк
            a: Заказать выписку по договору банковского счёта, срочного вклада или по банковской карте, можно в личном кабинете банка Фина'м, на сайте айбанк точка фина'м точка ру.
            a: Для этого выберите нужный счет, в поле справа выберите, выписка по счету. Если у вас есть также брокерский счет, то заказать выписку можно и в личном кабинете брокера на сайте, едо'кс точка Фина'м точка ру.
            a: Для этого выберите меню, Услуги, далее выберите раздел, Налоги выписки справки, в поле меню, Запрос в Банк на предоставление документов выберите нужное.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer

        state: Заказ_счет_фактура_Банк
            script: 
                $session.operatorPhoneNumber =  '3820';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
        state: Заказ_выписка_депо_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Заказ_налог_справка_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
    
        state: Заказ_справка_актив_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_документы_откр_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_справка_госслуж_Банк
            a: Заказать справку для гос служащего, можно в личном кабинете брокера на сайте, фина'м точка ру. Авторизуйтесь в личный кабинет, и выберите меню, документы, далее выберите раздел, налоги и справки, справка для гос служащих.
            a: Выберите компанию идентификации, Банк фина'м. Изготовление справки до пяти рабочих дней.
            a: Чем могу еще помочь?
            script:
                $context.session = {};
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
        state: Заказ_1042s_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_w8_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_выписка_квал_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_регламент_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_справка_Форекс
            a: Заказать справку по счету можно в личном кабинете форекс точка фина'м точка ру, для этого в левом вертикальном меню выберите вкладку, документы. Изготовление справки занимает до двух рабочих дней.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Заказ_налог_справка_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_документы_откр_Форекс
            a: Заказать документы об открытии форекс счёта, можно в личном кабинете брокера фина'м на сайте, едо'кс точка фина'м точка ру, для этого выберите меню отчетность, далее выберите раздел, основные документы.
            a: Основными документами об открытии форекс счёта являются, Заявление о присоединении к регламенту, Уведомление о заключении Рамочного договора, Уведомление о рисках к Рамочному договору.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Заказ_справка_госслуж_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Заказ_1042s_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_выписка_квал_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_регламент_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        # заказ документов по УК   
        state: Заказ_справка_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_выписка_депо_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Заказ_налог_справка_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
    
        state: Заказ_справка_актив_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_документы_откр_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_справка_госслуж_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Заказ_1042s_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_w8_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_выписка_квал_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Заказ_регламент_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario

            
            
    state: MoneyTransfer || modal = true
        # Движение ДС (сущность компании обязательна и определяется при первичном обращении только для ФФ и УК)
        intent!: /005 Движение_ДС 
        #|| toState = "/MoneyTransfer", onlyThisState = true
        script:
            $analytics.setMessageLabel("005 Движение_ДС", "Интенты");
            
            $session.moneyTransfer = {
                company : "undefined",
                assetType : "undefined",
                type : "undefined", //ввод, вывод, перевод
                method : "undefined", //для ввода, вывода актуально: СБП, наличные, реквизиты, карта
                transferCBbetweenАccounts : "undefined", //между своими, между разными клиентами, между разделами
                companyPhoneNumber : "undefined", //для банка и фф автоперевод на добавочный компании
                isCommission : false,
                isPeriod : false
            };
            
            moneyTransferRun(true);
        
        state: MoneyTransferGetCompany
            a: Операции с каким счётом вас интересуют; С БРОКЕРСКИМ счетом; Со счётом в банке; в управляющей компании; или счётом форекс.

            state: Уточнение_MoneyTransferGetCompany
                # q: * {[@commission] * [@assetType] * [@period] * [@moneyTransferType] * [@moneyTransferMethod]} 
                q: * @company *
                script: 
                    # $reactions.answer("3");
                    moneyTransferRun(false);
                    
            state: LocalCatchAll
                event: noMatch
                a: Это не похоже на компанию. Попробуйте еще раз.
           
        state: MoneyTransferGetAssetType
            a: Уточните, вас интересуют операции с денежными средствами, или с ценными бумагами?

            state: Уточнение_MoneyTransferGetAssetType
                q: * {[@commission] * [@period] * [@moneyTransferType] * [@moneyTransferMethod] * @assetType} *
                script: 
                    moneyTransferRun(false);
                    
            state: LocalCatchAll
                event: noMatch
                a: Это не похоже на компанию. Попробуйте еще раз.

        state: MoneyTransferGetType
            if: $session.moneyTransfer.assetType === 'ДС'
                a: Уточните, какой тип операции Вас интересует, пополнение счёта, вывод средств или перевод между своими счетами?
            
            if: $session.moneyTransfer.assetType === 'ЦБ'
                a: Уточните, вы хотите перевести бумаги от другого брокера в фина'м, вывести бумаги к другому брокеру, или перевести бумаги между счетами внутри фина'м?

            state: Уточнение_MoneyTransferGetType
                q: * {[@commission] * [@period] * [@moneyTransferMethod] * [@transferCBbetweenАccounts] * @moneyTransferType} *
                script: 
                    moneyTransferRun(false);
                    
            
            state: LocalCatchAll
                event: noMatch
                a: Это не похоже на тип операции. Попробуйте еще раз.

        state: MoneyTransferGetBetweenAccounts
            a: Какой тип перевода Вас интересует, перевод между своими счетами, перевод между разными клиентами фина'м, или перевод между разделами.

            state: Уточнение_MoneyTransferGetBetweenAccounts
                q: * {[@commission] * [@period] * @transferCBbetweenАccounts} *
                script: 
                    moneyTransferRun(false);
                    
            state: LocalCatchAll
                event: noMatch
                a: Это не похоже на тип операции. Попробуйте еще раз.

        state: MoneyTransferGetMethod
            if: $session.moneyTransfer.type === 'Ввод'
                # a: В преддверии новогодних праздников, рекомендуем пополнять брокерский счет, и счет ИИС, заблаговременно, учитывая срок зачисления средств. Последний день зачисления средств в 2023 году - 29 декабря.
                # a: Обращаем ваше внимание, что пополнение по реквизитам, занимает до 3х рабочих дней. Если у вас осталось менее трех дней для пополнения счета, то рекомендуем выбрать другие способы; например, системой быстрых платежей, или наличными, в офисе фина'м.
                a: Какой способ пополнения вас интересует? Системой быстрых платежей, СБП? Банковской картой, По реквизитам счёта, или Наличными в кассе.
            
            if: $session.moneyTransfer.type === 'Вывод'
                # a: 29 декабря, выводы денежных средств в рублях эрэф доступны до 15:00 по московскому времени; и срочные выводы по реквизитам доступны до 12:00 часов, и только в размере свободного остатка на момент исполнения поручения.
                # a: Выводы, поданные позднее, будут исполнены уже 3 января. В январе выводы средств доступны с третьего по пятое, и с 8ого января в штатном режиме. Выводы валюты будут исполняться с 9 января.
                a: Какой способ вывода средств Вас интересует? Системой быстрых платежей, СБП? Банковской картой, По реквизитам счёта, или Наличными в кассе.
            
            # if: $session.moneyTransfer.type === 'Перевод'
            #     a: Уточните, какой способ перевода Вас интересует?

            state: Уточнение_MoneyTransferGetMethod
                q: * {[@commission] * [@period] * [@moneyTransferType] * @moneyTransferMethod} *
                # a: {{ $parseTree._moneyTransferMethod.name }}
                script: 
                    moneyTransferRun(false);
                    
            state: LocalCatchAll
                event: noMatch
                a: Это не похоже на способ {{ $session.moneyTransfer.type }}a. Попробуйте еще раз.

        state: MoneyTransferText
            script:
                $response.replies = $response.replies || [];
                $session.moneyTransfer.text = getTestForMoneyTransfer($session.moneyTransfer, $response.replies);
            a: {{ $session.moneyTransfer.text['a'] ? $session.moneyTransfer.text['a'] : $session.moneyTransfer.text }}
            script:
                if(($session.moneyTransfer.company == "Брокер") && ($session.moneyTransfer.assetType == "ДС") && ($session.moneyTransfer.type == "Ввод") && ($session.moneyTransfer.method == "СБП")){
                    $reactions.transition("/MoneyTransfer/MoneyTransfer Инструкция пополнение СБП");
                    }
                if(($session.moneyTransfer.company == "Брокер") && ($session.moneyTransfer.assetType == "ДС") && ($session.moneyTransfer.type == "Вывод") && ($session.moneyTransfer.method == "СБП")){
                    $reactions.transition("/MoneyTransfer/MoneyTransfer Инструкция вывод СБП");
                    }
                if(($session.moneyTransfer.company == "Брокер") && ($session.moneyTransfer.assetType == "ДС") && ($session.moneyTransfer.type == "Ввод") && ($session.moneyTransfer.method == "Реквизиты") && ($session.moneyTransfer.isCommission == false)){
                    $reactions.transition("/MoneyTransfer/MoneyTransfer Инструкция пополнение Реквизиты");
                    }
                if(($session.moneyTransfer.company == "Брокер") && ($session.moneyTransfer.assetType == "ДС") && ($session.moneyTransfer.type == "Вывод") && ($session.moneyTransfer.method == "Реквизиты") && ($session.moneyTransfer.isCommission == false)){
                    $reactions.transition("/MoneyTransfer/MoneyTransfer Инструкция вывод Реквизиты");
                    }
                if(($session.moneyTransfer.company == "Брокер") && ($session.moneyTransfer.assetType == "ДС") && ($session.moneyTransfer.type == "Ввод") && ($session.moneyTransfer.method == "Карта") && ($session.moneyTransfer.isCommission == false)){
                    $reactions.transition("/MoneyTransfer/MoneyTransfer Инструкция пополнение Карта");
                    }
                if(($session.moneyTransfer.company == "Брокер") && ($session.moneyTransfer.assetType == "ДС") && ($session.moneyTransfer.type == "Вывод") && ($session.moneyTransfer.method == "Карта") && ($session.moneyTransfer.isCommission == false)){
                    $reactions.transition("/MoneyTransfer/MoneyTransfer Инструкция вывод Карта");
                    }    
                        
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # script:
            #     unset($session.moneyTransfer);
            # final answer
            
        state: MoneyTransfer Инструкция пополнение Реквизиты
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите я направлю ссылку на пополнение по реквизитам вам в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам ссылку на [пополнение счёта по реквизитам в личном кабинете|https://lk.finam.ru/deposit/bank/requisites]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Хотите, я расскажу подробнее про комиссии?
            q: * @agree *  ||toState = "/MoneyTransfer/MoneyTransfer Комиссии пополнение Реквизиты"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            
            
        state: MoneyTransfer Инструкция вывод Реквизиты
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите я направлю ссылку на вывод средств по реквизитам вам в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам ссылку на [вывод средств по реквизитам в личном кабинете|https://lk.finam.ru/withdraw/bank/requisites]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Хотите, я расскажу подробнее про комиссии?
            q: * @agree *  ||toState = "/MoneyTransfer/MoneyTransfer Комиссии вывод Реквизиты"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"    
            
            
        state: MoneyTransfer Комиссии пополнение Реквизиты
            a: Фина'м не удерживает комиссию за пополнение по реквизитам, брокерского счёта в валюте рубль РФ, с банковских счетов физических лиц. Комиссия за пополнение брокерского счёта в валюте доллар или евро, 0,6%, но не менее 25 и не более 4000 единиц валюты.
            a: За пополнение в других валютах комиссия не удерживается. При пополнении с банков Казахстана, может понадобиться подписать дополнительное соглашение в личном кабинете. Комиссия за ввод долларов/евро со счетов Банка Фина'м не удерживается.
            a: Прямое пополнение с брокерского счёта другой компании производятся по реквизитам. Информацию об ограничениях, и комиссиях за отправку средств, рекомендуем уточнять у брокера-отправителя. За перевод средств со счетов индивидуальных предпринимателей и юридических лиц, дополнительная комиссия составляет 6% от суммы зачисления, при вводе более 4000 рублей.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            
        state: MoneyTransfer Комиссии вывод Реквизиты
            a: Вывод валюты рубль РФ без комиссии. Вывод иностранной валюты на счета в банке фина'м, без комиссии, но есть комиссия за зачисление валюты доллар США, со стороны банка фина'м, 3% от суммы, но не менее 300 долларов и не более суммы зачисления.
            a: бесплатное хранение долларов сша в банке фина'м, до 10000 долларов включительно. За вывод иностранной валюты на счета в других банках российской федерации, взимаются следующие комиссии, За вывод в валюте доллар США — 0,3% от суммы вывода, но не менее 30 и не более 150 долларов, дополнительная комиссия за обработку поручений на вывод долларов сша: 0,4% от суммы вывода, но не менее 1500 и не более 250000 рублей.
            a: За вывод валюты Юань и Гонконгский доллар — 0,07% от суммы вывода, но не менее 25 € и не более 100 €. Дополнительно при оформлении вывода по реквизитам, можно воспользоваться опцией срочного вывода рублей рф. Деньги поступят на один день раньше. стоимость данной опции 300 рублей. по тарифам Дневной СПБ, Консультационный СПБ, Стратег Ю эС, комиссия 7 с половиной долларов. Ограничение по сумме срочного вывода, от 1000 до 5000000 рублей, но не более 80% от оценки вашего счета. 
            a: Обращаем ваше внимание, вывод происходит не ранее проведения биржевых расчётов по сделкам в соответствии с режимом торгов.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            
        state: MoneyTransfer Инструкция пополнение Карта
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите я направлю ссылку на пополнение по карте вам в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам ссылку на [пополнение счёта по карте в личном кабинете|https://lk.finam.ru/deposit/card/new]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Хотите, я расскажу подробнее про комиссии?
            q: * @agree *  ||toState = "/MoneyTransfer/MoneyTransfer Комиссии пополнение Карта"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            
            
        state: MoneyTransfer Инструкция вывод Карта
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите я направлю ссылку на вывод средств по реквизитам карты вам в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам ссылку на [вывод средств по реквизитам карты в личном кабинете|https://lk.finam.ru/withdraw/bank/requisites]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Хотите, я расскажу подробнее про комиссии?
            q: * @agree *  ||toState = "/MoneyTransfer/MoneyTransfer Комиссии вывод Карта"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            
        state: MoneyTransfer Комиссии пополнение Карта
            a: Пополнение с карты банка Фина'м, без комиссии. Комиссия за пополнение с карт других банков, один процент от суммы пополнения, но не менее пятидесяти рублей. Первичное пополнение счёта, без комиссии.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            
        state: MoneyTransfer Комиссии вывод Карта
            a: Вывод валюты рубль РФ без комиссии. Вывод иностранной валюты на счета в банке фина'м, без комиссии, но есть комиссия за зачисление валюты доллар США, со стороны банка фина'м, 3% от суммы, но не менее 300 долларов и не более суммы зачисления.
            a: бесплатное хранение долларов сша в банке фина'м, до 10000 долларов включительно. За вывод иностранной валюты на счета в других банках российской федерации, взимаются следующие комиссии, За вывод в валюте доллар США — 0,3% от суммы вывода, но не менее 30 и не более 150 долларов, дополнительная комиссия за обработку поручений на вывод долларов сша: 0,4% от суммы вывода, но не менее 1500 и не более 250000 рублей. 
            a: За вывод валюты Юань и Гонконгский доллар — 0,07% от суммы вывода, но не менее 25 € и не более 100 €. Дополнительно при оформлении вывода по реквизитам, можно воспользоваться опцией срочного вывода рублей рф. Деньги поступят на один день раньше. стоимость данной опции 300 рублей. по тарифам Дневной СПБ, Консультационный СПБ, Стратег Ю эС, комиссия 7 с половиной долларов.
            a: Ограничение по сумме срочного вывода, от 1000 до 5000000 рублей, но не более 80% от оценки вашего счета. Обращаем ваше внимание, вывод происходит не ранее проведения биржевых расчётов по сделкам в соответствии с режимом торгов.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"    
    
        state: MoneyTransfer Инструкция пополнение СБП
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить иллюстрированную инструкцию по пополнению счета в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [инструкцию по пополнению счета с помощью СБП|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyapopopolneniyuschetacherezsbp]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        
        state: MoneyTransfer Инструкция вывод СБП
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить иллюстрированную инструкцию по выводу средств со счета в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [инструкцию по выводу средств с помощью СБП|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyavyvodcherezsbp]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"

        state: MoneyTransferOperator
            a: Информацию по данному вопросу, вам предоставит специалист профильного отдела.
            script:
                $session.operatorPhoneNumber = $session.moneyTransfer.companyPhoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
            
    
    
    state: Аналитика
        intent!: /006 Аналитика
        # a: Инфа по аналитике.
        script:
            $analytics.setMessageLabel("006 Аналитика", "Интенты");
            $session.operatorPhoneNumber = '1000';
            $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario
            
            
    state: Отзыв
        intent!: /039 Отзыв о работе
        # a: Клиент хочет оставить отзыв.
        script:
            $analytics.setMessageLabel("039 Отзыв о работе", "Интенты");
            $session.operatorPhoneNumber = '1000';
            $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario
            


    state: Установка_ИТС
        intent!: /011 Установка_ИТС

        script:
            $analytics.setMessageLabel("011 Установка_ИТС", "Интенты");
            
            if ( typeof $parseTree._ITS != "undefined" ){
                $session.ITS = $parseTree._ITS;
            }
            if ( typeof $session.ITS == "undefined" ){
                $reactions.transition("/Установка_ИТС/Уточнение ИТС");
            } else {
                $reactions.transition("/Установка_ИТС/Установка_ИТС_" + $session.ITS.name);
            }
        
        state: Уточнение ИТС
            a: Какую торговую систему вы хотели бы установить?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @ITS *
                script:
                    $session.ITS = $parseTree._ITS;
                    $reactions.transition("/Установка_ИТС");
        
        state: Установка_ИТС_Quik
            a: Торговая система КВИК предназначена для установки на устройства с системой Windows. Терминал предоставляется бесплатно. 
            a: Скачать торговую систему КВИК можно на сайте фина'м точка ру. Для этого, в верхней части страницы выберите раздел Инвестиции, далее выберите Торговые платформы, КВИК. 
            a: Здесь вы можете скачать дистрибутив КВИК, генератор ключей Кей Ген, и инструкцию по его установке.
            a: Чтобы продолжить настройки, убедитесь в доступности терминала по вашему брокерскому счету.
            a: Если при открытии брокерского счета, вы не подключали терминал квик к счету, то вы можете подключить КВИК в личном кабинете, на сайте едо'кс точка фина'м точка ру, в разделе, 
            a: Торговля, выберите пункт меню, информационно торговые системы, И Т эС, и подключите терминал к желаемому счету.
            a: Обучающее видео по работе с терминалом КВИК, вы можете запросить в чате поддержки на сайте фина'м точка ру или в терминале фина'м трейд.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить в чат подробную инструкцию по установке торговой системы Квик?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам подробную иллюстрированную [инструкцию по установке торговой системы QUIK| https://www.finam.ru/dicwords/file/files_chatbot_instrukciyaquik]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Установка_ИТС_Transaq
            a: Торговые системы ТРАНЗАК или ТРАНЗА'К Ю ЭС, предназначены для установки на устройства с системой Windows.
            a: Терминал предоставляется бесплатно. Скачать дистрибутив ТРАНЗАК или ТРАНЗА'К Ю ЭС, можно на сайте фина'м точка ру.
            a: Для этого, в верхней части страницы выберите раздел Инвестиции, далее выберите Торговые платформы, система ТРАНЗА'К, и скачайте нужную версию.
            a: Чтобы продолжить настройки, убедитесь в доступности терминала по вашему брокерскому счёту. При открытии нового брокерского счёта вы можете сразу выбрать терминал ТРАНЗАК.
            a: А также, вы можете открыть доступ к системе ТРАНЗАК, для уже имеющегося счета, зайдите в личный кабинет на сайте едо'кс точка фина'м точка ру.
            a: В разделе, торговля, выберите пункт меню, информационно торговые системы, И Т эС, и подключите терминал к желаемому счету. 
            a: В работе с терминалом ТРАНЗА'К Ю ЭС, обращаем ваше внимание, что торговый сервер ТРАНЗА'К Ю ЭС запускается в 11 часов 30 минут по московскому времени, подключение до этого времени недоступно.
            a: Обучающее видео по работе с терминалом ТРАНЗАК, вы можете запросить в чате поддержки на сайте фина'м точка ру или в терминале фина'м трейд.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить в чат подробную инструкцию по установке торговой системы транза'к?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам подробную иллюстрированную [инструкцию по установке торговой системы TRANSAQ|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyapoustanovketransaq]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Установка_ИТС_FT
            a: Торговая система фина'м трейд не требует установки. Чтобы воспользоваться вэб версией терминала, зайдите в личный кабинет на сайте фина'м точка ру, далее перейдите в раздел, трейдинГ. 
            a: Мобильное приложение, можно скачать на Android или АйОс в магазине на вашем устройстве, либо на сайте фина'м точка ру. Для этого, в верхней части страницы выберите раздел Инвестиции; далее выберите Торговые платформы; Мобильное приложение фина'м трейд.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Установка_ИТС_TrConnector
            a: Установка терминала не требуется. Для подключения к серверу через сторонние системы достаточно подключенного счета. 
            a: Зайдите в личный кабинет на сайте едо'кс точка фина'м точка ру. в разделе, торговля, выберите пункт меню, информационно торговые системы, И Т эС, и подключите желаемый счет. 
            a: Доступ к системе бесплатный. После того, как вы подпишете заявление на подключение терминала, вам придет СМС с паролем от системы. 
            a: Логин находится в личном кабинете на сайте едо'кс точка фина'м точка ру, выберите нужный счет, далее разверните раздел, торговые программы.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Установка_ИТС_MT5
            a: Торговая система Meta Trader 5 предназначена для установки на устройства с системой Windows. Терминал предоставляется бесплатно. 
            a: Скачать дистрибутив Meta Trader 5 можно на сайте фина'м точка ру. Для этого, в верхней части страницы выберите раздел Инвестиции; далее выберите Торговые платформы; MetaTrader 5. 
            a: Также, подключить брокерский счет к терминалу Мета Трейдер 5, можно в личном кабинете на сайте едо'кс точка фина'м точка ру.
            a: В разделе, Торговля, выберите пункт меню, информационно торговые системы, И Т эС, и подключите терминал к желаемому счету. 
            a: К одному идентификатору, или ло'гину, можно подключить только один брокерский счет. Обращаем ваше внимание, что Договоры с раздельными брокерскими счетами недоступны для подключения к Meta Trader 5.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Установка_ИТС_MT4
            a: Клиентский терминал мета трейдер 4 предназначен для торговли и технического анализа в режиме реального времени при работе на рынке форекс. Торговая система мета трейдер 4 предназначена для установки на устройство с системой Windows.
            a: Терминал предоставляется бесплатно. Ознакомиться с техническими характеристиками, и установить дистрибутив, можно на сайте фина’м точка ру. Для этого вверху страницы сайта выберите раздел Форекс.
            a: Далее выберите раздел Торговля, платформы. В случае, если у вас уже есть счёт форекс, то установить терминал на персональный компьютер или мобильное приложение, вы можете в личном кабинете форекс, во вкладке, Программы для торговли.
            a: Хотите посмотреть обучающее видео по установке и работе с терминалом мета трейдер 4?
            script:
                $context.session = {};
            q: @agree ||toState = "/Обучение_ИТС/ИТС_MT4"    
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
            
        state: Установка_ИТС_Интернет_банк
            a: Скачать мобильное приложение Фина'м Банк можно в магазинах Google плэй и App Store. По умолчанию, логином от личного кабинета является номер телефона в международном формате.
            a: Для России, номер начинается с цифры, 7. Пароль вы задавали самостоятельно, при открытии счёта или при изменении пароля в личном кабинете. А также, зайти в интернетбанк можно на сайте фина'м точка ру, в разделе банк, интернетбанк.
            a: Чем я могу еще помочь? 
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
    
    state: ИТС
        intent!: /013 ИТС
        script:
            $analytics.setMessageLabel("013 ИТС", "Интенты");
            
        a: Фина'м предоставляет бесплатный доступ к следующим торговым системам.  Торговая система фина'м трейд представлена в формате веб-версии не требующей установки; и мобильного приложения для андроид и ios.
        a: А также информационно торговые системы для установки на персональный компьютер пользователя: ТРАНЗАК, или ТРАНЗА'К Америка; квик; Meta Trader 5. Мобильное приложение для Meta Trader 5 не предоставляется.
        a: Для торговли на рынке Форекс, предоставляется торговая система, и мобильное приложение, Meta Trader 4. Клиентам Фина'м также доступны платное мобильное приложение КВИК ИКС, и вэб-версия КВИК, за 420 рублей в месяц.
        a: Подключить стороннее программное обеспечение можно через ТРАНЗА'К Connector, Комо'н Trade апи, и квик. Если вам интересно узнать подробнее, назовите тему: установка торговых систем, или обучающие видео курсы.
 
        q: * @installation_its_u * ||toState = "/Установка_ИТС"
        q: * @learning_ITS_u * ||toState = "/Обучение_ИТС"
        q: * @choice_1 * ||toState = "/Установка_ИТС"
        q: * @choice_2 * ||toState = "/Обучение_ИТС"
        q: * @choice_last * ||toState = "/Обучение_ИТС"
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?"
        # final answer
        
        
    state: NLK
        intent!: /012 NLK
        
        script:
            $analytics.setMessageLabel("012 NLK", "Интенты");
            
            if ( typeof $parseTree._personalData != "undefined"){
                $session.personalData = $parseTree._personalData;
            }
            if ( typeof $session.personalData == "undefined" ){
                $reactions.transition("/NLK/Уточнение данных для замены");
            } else {
                $reactions.transition("/NLK/ЗаменаДанных_" + $session.personalData.name)
            } 
            
        state: Уточнение данных для замены
            a: Какие данные нужно заменить? Паспортные данные. Адрес регистрации. Номер телефона. Или электронную почту.
            q: * @choice_1 ||toState = "/NLK/ЗаменаДанных_passport"
            q: * @choice_2 ||toState = "/NLK/ЗаменаДанных_registration"
            q: * @choice_3 ||toState = "/NLK/ЗаменаДанных_phoneNumber"
            q: * @choice_4 ||toState = "/NLK/ЗаменаДанных_email"
            q: * @choice_last ||toState = "/NLK/ЗаменаДанных_email"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @personalData *
                script:
                    $session.personalData = $parseTree._personalData;
                    $reactions.transition("/NLK");    
    
        state: ЗаменаДанных_passport
            a: Подать поручение на смену паспортных данных, можно в офисе компании, или в личном кабинете на сайте фина'м точка ру.
            a: Для этого авторизуйтесь в личном кабинете, далее в верхнем правом углу нажмите на иконку профиля, далее выберите, персональные данные, внизу страницы выберите, редактировать данные.
            a: В зависимости от внесенных изменений, вам перезвонит менеджер поддержки фина'м, чтобы задать три контрольных вопроса, для подтверждения вашей личности.
            a: Обращаем ваше внимание, для замены паспортных данных, нужно вкладывать копии полных страниц документа, подтверждающих смену данных. Копии должны хорошо читаться, не иметь бликов, посторонних надписей, или рисунков.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: ЗаменаДанных_registration
            a: Подать поручение на смену адреса регистрации можно в офисе компании, или в личном кабинете на сайте фина'м точка ру.
            a: Для этого авторизуйтесь в личном кабинете, далее в верхнем правом углу нажмите на иконку профиля, далее выберите, персональные данные, внизу страницы выберите, редактировать данные.
            a: В зависимости от внесенных изменений, вам перезвонит менеджер поддержки фина'м, чтобы задать три контрольных вопроса, для подтверждения вашей личности.
            a: Обращаем ваше внимание, для замены паспортных данных, нужно вкладывать копии полных страниц документа, подтверждающих смену данных. Копии должны хорошо читаться, не иметь бликов, посторонних надписей, или рисунков.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: ЗаменаДанных_phoneNumber
            a: Подать поручение на смену или добавление номера телефона, можно в личном кабинете на сайте фина'м точка ру.
            a: Для этого авторизуйтесь в личном кабинете, далее в верхнем правом углу нажмите на иконку профиля, далее выберите, персональные данные, внизу страницы выберите, редактировать данные.
            a: У каждого пользователя должен быть уникальный номер телефона, одновременное использование одного номера для двух аккаунтов невозможно.
            a: В зависимости от внесенных изменений, вам перезвонит менеджер поддержки фина'м, чтобы задать три контрольных вопроса, для подтверждения вашей личности.
            a: Обращаем ваше внимание, что смс подпись, будет приходить только на один номер телефона. Выбрать номер для получения смс подписи можно в личном кабинете.
            a: Для этого авторизуйтесь в личном кабинете на сайте фина'м точка ру, в верхнем правом углу нажмите на квадратный значок меню, и перейдите в личный кабинет. Далее в разделе Сервис, выберите раздел, СМС подпись.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: ЗаменаДанных_email
            a: Подать поручение на смену или добавление адреса электронной почты, можно в личном кабинете на сайте фина'м точка ру.
            a: Для этого авторизуйтесь в личном кабинете, далее в верхнем правом углу нажмите на иконку профиля, далее выберите, персональные данные, внизу страницы выберите, редактировать данные.
            a: В зависимости от внесенных изменений, вам перезвонит менеджер поддержки фина'м, чтобы задать три контрольных вопроса, для подтверждения вашей личности.
            a: Обращаем ваше внимание, что у каждого клиента должен быть свой уникальный адрес электронной почты, одновременное использование одного адреса для двух и более аккаунтов невозможно.
            a: После подписания заявления, появится уведомление с просьбой подтвердить указанный адрес электронной почты.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: ЗаменаДанных_fullName
            a: Подать поручение на смену фамилии, имени, отчества, и других паспортных данных, можно в офисе компании, или в личном кабинете на сайте фина'м точка ру.
            a: Для этого авторизуйтесь в личном кабинете, далее в верхнем правом углу нажмите на иконку профиля, далее выберите, персональные данные, внизу страницы выберите, редактировать данные.
            a: В зависимости от внесенных изменений, вам перезвонит менеджер поддержки фина'м, чтобы задать три контрольных вопроса, для подтверждения вашей личности.
            a: Обращаем ваше внимание, для замены паспортных данных, нужно вкладывать копии полных страниц документа, подтверждающих смену данных.
            a: Копии должны хорошо читаться, не иметь бликов, посторонних надписей, или рисунков. Если ваш вопрос касается отображения вашего ФИ'Оо в справке w 8 бэн, то нужно написать в поддержку, указав свое полное имя как в загран паспорте.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
    state: NLK_Номер счета
        intent!: /012 NLK/NLK_Номер счета
        
        go!: /NLK_Номер счета/Ответ
        
        state: Ответ
            a: Ваш текущий тарифный план, а так же номер счета, номер договора и торговый код  отображаются в личном кабинете, в разделе, Детали по счету.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            q: @repeat_please * ||toState = "."
            # final answer
        
    
    state: Авторизация
        intent!: /015 Авторизация
        
        script:
            if (typeof $parseTree._application != "undefined " ){
                $session.application = $parseTree._application;
            }
            if ( typeof $parseTree._LK != "undefined" ){
                $session.LK = $parseTree._LK;
            }
        
        a: Ваш вопрос связан с логином и паролем, или с получением СМС кода?
        
        q: * @LoginPassword * ||toState = "/Авторизация_Логин - Пароль"
        q: * @SMS_delivery * ||toState = "/Авторизация_Не приходит СМС"
        q: * @choice_1 * ||toState = "/Авторизация_Логин - Пароль"
        q: * @choice_2 * ||toState = "/Авторизация_Не приходит СМС"
        q: * @choice_last * ||toState = "/Авторизация_Не приходит СМС"
        q: @repeat_please * ||toState = "."
        
                
    state: Авторизация_Логин - Пароль
        intent!: /015 Авторизация/Авторизация_Логин - Пароль
            
        script:
            
            if ( typeof $parseTree._application != "undefined" ){
                $session.application = $parseTree._application;
            } if ( typeof $session.application == "undefined" ){
                $reactions.transition("/Авторизация_Логин - Пароль/Уточнение системы");
            } else {
                $reactions.transition("/Авторизация_Логин - Пароль/" + $session.application.name);
            }
                    
        state: Уточнение системы
            a: В какой системе или на каком сайте вы хотите авторизоваться?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @application *
                script:
                    $session.application = $parseTree._application;
                    $reactions.transition("/Авторизация_Логин - Пароль");
                
            state: LocalCatchAll
                event: noMatch
                a: Я не поняла вас. Уточните наименование системы или сайта где нужна авторизация?
             
                    
        state: Quik
            a: Чтобы зайти в торговую систему КВИК, после запуска системы, в появившемся диалоговом окне Идентификация пользователя, используйте логин и пароль, которые вы задали на этапе регистрации ключей для Квик в программе генераторе ключей, кей ген.
            a: Для восстановления данных для входа, нужно сгенерировать новую пару ключей, в программе генераторе ключей, кей ген.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: QuikX
            a: Подключить торговую систему веб квик к брокерскому счету, можно в личном кабинете старого дизайна на сайте едо'кс точка фина'м точка ру.
            a: Для этого в разделе Торговля, выберите, информационно торговые системы, далее выберите, Подключение платных сервисов. Торговая система платная, 420 рублей в месяц.
            a: Пароль к терминалу вы получите в СМС при подключении. Чтобы посмотреть Логин в личном кабинете. Нажмите на счет, по которому необходимо уточнить логин. Вы увидите список подключенных ко счету платформ.
            a: Найдите в открывшемся списке идентификатор терминала квик. В квик икс используются те же логин и пароль, что и в веб квик. После того, как вы подклю'чите счет к терминалу, свяжитесь с менеджером Фина'м. Он поможет активировать квик икс.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Transaq
            a: Чтобы зайти в торговую систему транзак, после запуска системы, в появившемся диалоговом окне Идентификация пользователя, используйте пароль, который приходил вам в смс при получении торговой системы.
            a: Если сообщение было утеряно, восстановить пароль можно в личном кабинете, в разделе Торговля – Информационно торговые системы – Смена пароля на терминал. После первого входа нужно поменять пароль в настройках транзак.
            a: Посмотреть логин можно в личном кабинете старого дизайна на сайте, едо'кс точка фина'м точка ру. Нажмите на счет, к которому нужен логин от транзак. Вы увидите список подключенных ко счету платформ. Найдите в нем идентификатор транзак.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: TransaqConnector
            a: Подключить счет к транзак коннектор можно в личном кабинете старого дизайна, на сайте едо'кс точка фина'м точка ру, в разделе Торговля – Информационно торговые системы – Подключение счёта на терминал.
            a: После того, как вы подпишите заявление на подключение терминала, вам придет СМС с паролем от системы.
            a: Если сообщение утеряно, вы можете восстановить пароль в личном кабинете, в разделе Торговля – Информационно торговые системы – Смена пароля на терминал.
            a: Логин находится в этом же личном кабинете. Нажмите на счет, к которому нужен логин от транзак коннектор. Вы увидите список подключенных ко счету платформ. Найдите в нем идентификатор транзак коннектор.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: FT
            a: Чтобы зайти в терминал Фина'м трейд, авторизуйтесь в личный кабинет на сайте фина'м точка ру. далее выберите раздел, торговля. По умолчанию, логином является номер телефона в международном формате. Для России, номер начинается с цифры, 7.
            a: Пароль вы задавали самостоятельно, при открытии счёта или при изменении пароля в личном кабинете. Если вы не помните учетные данные, то для восстановления доступа, под формой для ввода логина и пароля, нажмите кнопку, Забыли логин или пароль.
            a: После выполнения инструкций сайта, на вашу электронную почту придет письмо с логином, и ссылкой на создание нового пароля.
            a: Если у вас нет доступа к электронной почте и паспортным данным, обратитесь в ближайший офис компании. Если у вас нет доступа только к номеру телефона, обратитесь к менеджеру компании.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: MT4
            a: Логин, пароль и имя сервера, для торговой системы мета трейдер 4, можно посмотреть в личном кабинете форекс, в разделе Мои счета'.  Логином для терминала является пятизначный номер счета. Что бы посмотреть пароль, нажмите на номер счёта, или на раздел Подробнее о счёте.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: MT5
            a: Чтобы зайти в торговую систему мета трейдер 5, после запуска системы, используйте для входа пароль, который приходил в виде СМС после на получение торговой системы.
            a: Если сообщение утеряно, вы можете восстановить пароль в личном кабинете, в разделе Торговля – Информационно торговые системы – Смена пароля на терминал.
            a: Логин отображается в личном кабинете старого дизайна на сайте, едо'кс точка фин''ам точка ру.
            a: Нажмите на счет, по которому необходимо уточнить логин. Вы увидите список подключенных к счету платформ. Найдите в открывшемся списке идентификатор терминала мета трейдер.
            a: Идентификатор терминала является логином. К одному идентификатору может быть подключен только один брокерский счет.
            a: Обращаем ваше внимание, что договор с раздельными брокерскими счетами, то есть моносчета'ми, недоступен для подключения к мета трейдер 5.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Comon
            a: Чтобы зайти в личный кабинет сервиса Автоследования, зайдите на сайт комо'н точка ру. далее в верхнем правом углу нажмите, Вход. Для авторизации используйте логин и пароль от личного кабинета брокера Фина'м.
            a: По умолчанию, логином от личного кабинета является номер телефона в международном формате. Для России, номер начинается с цифры, 7. Пароль вы задавали самостоятельно, при открытии счёта или при изменении пароля в личном кабинете.
            a: Если вы не помните учетные данные, то для восстановления доступа, под формой для ввода логина и пароля, нажмите кнопку, Забыли логин или пароль.
            a: После выполнения инструкций сайта, на вашу электронную почту придет письмо с логином, и ссылкой на создание нового пароля.
            a: Если у вас нет доступа к электронной почте и паспортным данным, обратитесь в ближайший офис компании. Если у вас нет доступа только к номеру телефона, обратитесь к менеджеру компании.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: ComonTradeAPI
            script:
                $session.operatorPhoneNumber = '2222';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: DMA
            script:
                $session.operatorPhoneNumber = '3024';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: FinamRU
            a: Чтобы авторизоваться на сайте фина'м точка ру, зайдите на сайт. В верхнем правом углу страницы сайта, нажмите на квадратную иконку меню, и выберите, вход в, фина'м точка ру.
            a: После регистрации на сайте фина'м точка ру; или при регистрации в акции фина'м бонус; автоматически поступает письмо с логином и паролем на электронную почту пользователя.
            a: Для авторизации на сайте, также можно использовать номер телефона в международном формате, электронную почту, или данные от личного кабинета брокера фина'м. Восстановить пароль можно по номеру телефона или по электронной почте.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: FinamBonus
            a: Чтобы перейти в личный кабинет участника  акции Фина'м бонус, зайдите на сайт фина'м точка ру. В верхней части страницы сайта выберите раздел Программа лояльности; далее выберите вкладку Бонусный счёт.
            a: Обращаем ваше внимание; увидеть накопленные бонусы, участник акции может только если он авторизован на сайте фина'м точка ру. В противном случае, участнику будет отображаться нулевой баланс.
            a: Хотите узнать как авторизоваться на сайте фина'м точка ру?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @agree  ||toState = "/Авторизация_Логин - Пароль/FinamRU"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
            
        state: Dist
            script:
                $session.operatorPhoneNumber = '2222';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: InternetBank
            a: Безопасно управлять счетами и картами онлайн можно в кабинете интернет-банка и в мобильном приложении. Зайти в интернет-банк можно на сайте фина'м точка ру. Для этого, в верхней части страницы сайта выберите раздел Банк; Частное или юридическое лицо; войти в интернет-банк.
            a: Скачать мобильное приложение Фина'м Банка можно в магазинах Google плэй и App Store. По умолчанию, логином от личного кабинета является номер телефона в международном формате.
            a: Для России, номер начинается с цифры, 7. Пароль вы задавали самостоятельно, при открытии счёта или при изменении пароля в личном кабинете.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: LK
            go!: /Авторизация_Личный кабинет    
        
    # Логин - Пароль - Личный кабинет
    state: Авторизация_Личный кабинет
        intent!: /015 Авторизация/Авторизация_Личный кабинет
        
        script:
            if ( typeof $parseTree._LK != "undefined" ){
                $session.LK = $parseTree._LK;
            }
            if ( typeof $session.LK == "undefined" ){
                $reactions.transition("/Авторизация_Личный кабинет/Уточнение типа ЛК");
            } else {
                $reactions.transition("/Авторизация_Личный кабинет/Ответ_" + $session.LK.name);
            } 
        

        state: Уточнение типа ЛК
            a: В какой личный кабинет нужно зайти? Личный кабинет брокера; интернет-банка; кабинет для агента; или личный кабинет форекс.
            q: * @choice_1 ||toState = "/Авторизация_Личный кабинет/Ответ_Брокер"
            q: * @choice_2 ||toState = "/Авторизация_Личный кабинет/Ответ_Банк"
            q: * @choice_3 ||toState = "/Авторизация_Личный кабинет/Ответ_Агент"
            q: * @choice_4 ||toState = "/Авторизация_Личный кабинет/Ответ_ФФ"
            q: * @choice_last ||toState = "/Авторизация_Личный кабинет/Ответ_ФФ"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @LK *
                script:
                    $session.LK = $parseTree._LK;
                    $reactions.transition("/Авторизация_Личный кабинет");  

            
        state: Ответ_Брокер
            a: Зайти в личный кабинет брокера фина'м, можно на сайте фина'м точка ру. в верхнем правом углу страницы сайта нажмите на квадратный значок меню, и перейдите в личный кабинет.
            a: По умолчанию, логином от личного кабинета является номер телефона в международном формате. Для России, номер начинается с цифры, 7.
            a: Пароль вы задавали самостоятельно, при открытии счёта или при изменении пароля в личном кабинете.
            a: Некоторые услуги и сервисы временно доступны в старой версии личного кабинета с доменом едо'кс. Чтобы в него перейти, в личном кабинете выберите раздел, Помощь. Далее слева нажмите кнопку перейти в старый дизайн. 
            a: Если вы не помните учетные данные, то для восстановления доступа, под формой для ввода логина и пароля, нажмите кнопку, Забыли логин или пароль.
            a: После выполнения инструкций сайта, на вашу электронную почту придет письмо с логином, и ссылкой на создание нового пароля. Если у вас нет доступа к электронной почте и паспортным данным, обратитесь в ближайший офис компании.
            a: Если у вас нет доступа только к номеру телефона, обратитесь к менеджеру компании.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber = '3888';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario            
        
        state: Ответ_Агент
            script:
                $session.operatorPhoneNumber = '1000';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_ФФ
            script:
                $session.operatorPhoneNumber = '3887';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
            
    state: Авторизация_Не приходит СМС
        intent!: /015 Авторизация/Авторизация_Не приходит СМС

        go!: /Авторизация_Не приходит СМС/Общая информация

                    
        state: Общая информация
            a: Если вам не пришел смс код, попробуйте перезагрузить устройство, сделать очистку СМС, переставить сим карту в другое устройство, проверить услугу черный список. 
            a: Если Вы подписываете поручение в личном кабинете, проверьте смс-код в уведомлениях приложения фина'м трейд.
            a: Вам удалось выполнить эти рекомендации?
            
            q: * @agree *  ||toState = "/Авторизация_Не приходит СМС/Перевод на оператора"
            q: * @disagree * ||toState = "/Авторизация_Не приходит СМС/Повторить?"
            # final answer
            
        state: Повторить?    
            a: Хотите услышать рекомендации еще раз?
            
            q: @agree ||toState = "/Авторизация_Не приходит СМС/Общая информация"
            q: @disagree ||toState = "/Могу еще чем то помочь?"

            
        state: Перевод на оператора
            script:
                $session.operatorPhoneNumber = '1000';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
            
            
    state: Автоследование 
        intent!: /019 Автоследование
        
        script:
            $analytics.setMessageLabel("019 Автоследование", "Интенты");
    
        a: Просьба уточнить, какая информация Вас интересует? Создание аккаунта, комиссии, синхронизация, или подключение и отключение счетов от стратегий?
        
        q: * @creating_account_comon_u * ||toState = "/Автоследование_Создание аккаунта"
        q: * @comission_comon_u * ||toState = "/Автоследование_Комиссии"
        q: * @synchronization * ||toState = "/Автоследование_Синхронизация"
        q: * @open_comon_u * ||toState = "/Автоследование_Подключение стратегии"
        q: * @close_comon_u * ||toState = "/Автоследование_Отключение стратегии"
        q: * @choice_1 * ||toState = "/Автоследование_Создание аккаунта"
        q: * @choice_2 * ||toState = "/Автоследование_Комиссии"
        q: * @choice_3 * ||toState = "/Автоследование_Синхронизация"
        q: * @choice_4 * ||toState = "/Автоследование_Подключение стратегии"
        q: * @choice_last * ||toState = "/Автоследование_Отключение стратегии"
        q: @repeat_please * ||toState = "."
    
    
    state: Автоследование_Создание аккаунта
        intent!: /019 Автоследование/Автоследование_Создание аккаунта
        
        go!: /Автоследование_Создание аккаунта/Ответ
         
        state: Ответ
            a: Для использования сервиса Автоследование, необходимо зарегистрироваться и создать учетную запись на сайте комо'н точка ру, с помощью данных для входа в Личный кабинет брокера фина'м.
            a: Обращаем ваше внимание, что договор с раздельными счетами по секциям, то есть, моносчета'ми, а также счета с установленными нестандартными настройками, недоступны для подключения сервиса.
            a: Ознакомиться с Детальной информацией и правилами сервиса фина'м Автоследование, можно на сайте коммо'н точка ру в разделе, Правила.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
    state: Автоследование_Комиссии
        intent!: /019 Автоследование/Автоследование_Комиссии
        
        go!: /Автоследование_Комиссии/Ответ
        
        state: Ответ
            a: Чтобы узнать тариф по конкретной стратегии Автоследования, на сайте коммо'н точка ру выберите из списка интересующую вас стратегию. Далее откройте вкладку показатели, и нажмите на название тарифа.
            a: Также, со стоимостью сервиса фина'м Автоследование, по каждому тарифу можно ознакомиться в разделе Правила.
            a: Обращаем ваше внимание, списание комиссии по тарифам автоследования, рассчитываемым от суммы чистых активов проходит ежедневно;
            a: Списание комиссии по тарифам, рассчитываемым от инвестиционного дохода может быть раз в месяц, раз в квартал, или раз в год, в зависимости от тарифа; а также при пополнении счёта, или выводе средств. Списания отображаются в справке по счету, как Вознаграждение компании согласно пункту 16 регламента брокерского обслуживания.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
    state: Автоследование_Синхронизация
        intent!: /019 Автоследование/Автоследование_Синхронизация
        
        go!: /Автоследование_Синхронизация/Ответ
            
        state: Ответ
            a: Для синхронизации со стратегией автора, нужно авторизоваться на сайте коммо'н точка ру.  Далее нажать на значок персоны, в верхнем правом углу, и перейти в раздел, Мои подписки.
            a: В этом разделе отображается информация о подключенных стратегиях. Далее нажмите на значок шестеренки, отобразится меню, синхронизировать портфель.
            a: Рекомендуется также проставить галочки, чтобы при пополнении вашего торгового счета, автоматически наращивать позиции по стратегии автоследования, без подачи дополнительных команд.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
    state: Автоследование_Подключение стратегии
        intent!: /019 Автоследование/Автоследование_Подключение стратегии
        
        go!: /Автоследование_Подключение стратегии/Ответ
        
        state: Ответ    
            a: Подключить к сервису автоследование можно единый брокерский счет, открытый в фина'м. Для подключения стратегии, нужно авторизоваться на сайте коммо'н точка ру. 
            a: Далее выбрать нужную стратегию, перейти на страницу с ее описанием, и нажать кнопку, подключить. Далее нужно установить синхронизацию, и подписать документы кодом из смс. 
            a: В зависимости от стратегии, дополнительно может понадобиться пройти тест на инвестиционный профиль, пройти тестирование для неквалифицированных инвесторов, иметь статус квалифицированного инвестора, или статус клиента с повышенным уровнем риска. 
            a: Обращаем ваше внимание, что не все стратегии подходят для счетов ИИС. А также, договор с раздельными счетами по секциям, то есть, моносчета'ми, а также счета с установленными нестандартными настройками, недоступны для подключения сервиса.
            a: Ознакомиться с Детальной информацией и правилами сервиса фина'м Автоследование, можно на сайте коммо'н точка ру в разделе, Правила.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
    state: Автоследование_Отключение стратегии
        intent!: /019 Автоследование/Автоследование_Отключение стратегии
        
        go!: /Автоследование_Отключение стратегии/Ответ
        
        state: Ответ    
            a: Для отключения стратегии Автоследования, нужно авторизоваться на сайте коммо'н точка ру. Далее нажать на значок персоны в верхнем правом углу, и перейти в раздел, Мои подписки. 
            a: Далее нужно выбрать стратегию и нажать на значок шестеренки, и выбрать, отключить автоследование. 
            a: Если нужно также закрыть все позиции, то проставьте соответствующие галочки в открывшемся меню. Позиции будут закрыты во время активной торговой сессии на бирже. Если биржа закрыта, то подписка перейдет в статус удаления. 
            a: Дальнейшие действия будут доступны только после закрытия позиций. Ознакомиться с Детальной информацией и правилами сервиса фина'м Автоследование, можно на сайте коммо'н точка ру в разделе, Правила. 
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
    
    state: Тарифы 
        intent!: /020 Тарифы
        
        script:
            $analytics.setMessageLabel("020 Тарифы", "Интенты");
            
            if ( typeof $parseTree._tarifs != "undefined"){
                $session.tarifs = $parseTree._tarifs;
                //$session.company = 'Брокер';
            } if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            } if ( typeof $parseTree._rateInformationType != "undefined" ){
                $session.rateInformationType = $parseTree._rateInformationType;
            } if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Тарифы/Уточнение компании");
            } else if ( typeof $session.rateInformationType == "undefined" ){
                $reactions.transition("/Тарифы/Уточнение типа информации");
            } else {
                $reactions.transition("/Тарифы/" + $session.company.name + '_' + $session.rateInformationType.name);
            }    
            
            # $response.replies = $response.replies || [];
            #                 $response.replies.push({
            #                     "type": "text",
            #                     "text": JSON.stringify($parseTree)
            #                 });
            
        state: Уточнение компании
            a: Уточните, вас интересуют тарифы брокера? Тарифы банка; Управляющей компании; или тарифы Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Тарифы");
                    
        state: Уточнение типа информации
            a: Какая информация вас интересует? Смена тарифа, сравнение тарифов, или информация по тарифу?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @rateInformationType *
                script:
                    $session.rateInformationType = $parseTree._rateInformationType;
                    $reactions.transition("/Тарифы");            
                
        #Информация по Брокеру    
        state: Брокер_AllRatesList
            a: Ознакомиться с описанием тарифных планов можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы. 
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м. Регламент представлен на сайте фина'м точка ру. 
            a: Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана. Списание комиссии происходит в 23 часа 59 минут по московскому времени.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: Брокер_ChoosingBestRate
            a: Сравнить тарифы и выбрать лучший, можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы, Сравнение тарифов. 
            a: Сравнительная таблица подробно отображает комиссии пяти наиболее популярных у клиентов Фина'м тарифов. Выбирая тариф, учитывайте количество, и объем сделок которые планируете совершать, а также стоимость обслуживания счета. 
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м.
            a: Регламент представлен на сайте фина'м точка ру. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Брокер_HowToChangeRate
            a: Изменить тариф можно в личном кабинете, на сайте Фина'м точка ру. Выберите нужный счет, далее выберите раздел, Детали. Чтобы сменить тариф, кликните на текущий тариф по счету, и выберите новый из предложенного списка. 
            a: Действие нового тарифа начинается, со следующего рабочего дня после подписания заявления на смену тарифа. Количество заявок на смену тарифа неограниченно. Действующим устанавливается тариф из последней подписанной заявки.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить иллюстрированную инструкцию по смене тарифного плана в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [инструкцию по смене тарифного плана|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyapoizmeneniyutarifa]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Брокер_ClientPersonalRate
            a: Ваш текущий тарифный план отображается в личном кабинете, на сайте Фина'м точка ру. Просто выберите нужный счет, далее выберите, Детали, на открывшейся странице будет указан ваш тариф в строке, тарифный план. 
            a: Обращаем ваше внимание, что по каждому счету тариф устанавливается отдельно.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Брокер_IPOTarifs
            # a: Тарифы IPO
            go!: /IPO_Условия участия
            
        state: Брокер_RateInformation
            script:
                if ( typeof $session.tarifs == 'undefined'){
                    $reactions.transition("/Тарифы/ОпределениеТарифа");
                }
            go!: /Тарифы/Тариф_{{ $session.tarifs.name }}
            
        state: ОпределениеТарифа
            a: Уточните, пожалуйста, какой тариф Вы подразумеваете?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа от клиента
                q: * @tarifs *
                script:
                    $session.tarifs = $parseTree._tarifs;
                go!: /Тарифы/Тариф_{{ $session.tarifs.name }} 
                
        #Информация по Банку
        state: Банк_AllRatesList
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
        state: Банк_ChoosingBestRate
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario

        state: Банк_HowToChangeRate
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario

        state: Банк_ClientPersonalRate
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Банк_RateInformation
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
            
        #Информация по Управляющей Компании
        state: УК_AllRatesList
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: УК_ChoosingBestRate
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: УК_HowToChangeRate
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: УК_ClientPersonalRate
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario

        state: УК_RateInformation
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario

            
        #Информация по Форекс    
        state: Форекс_AllRatesList
            a: Тарифный план при торговле через компанию Фина'м Форекс един для всех клиентов. Чтобы ознакомиться с условиями тарифа, на сайте Фина'м точка ру, зайдите в раздел, Форекс, далее выберите, Торговые условия.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: Форекс_ChoosingBestRate
            a: Тарифный план при торговле через компанию Фина'м Форекс един для всех клиентов. Чтобы ознакомиться с условиями тарифа, на сайте Фина'м точка ру, зайдите в раздел, Форекс, далее выберите, Торговые условия.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Форекс_HowToChangeRate
            a: Тарифный план при торговле через компанию Фина'м Форекс един для всех клиентов. Чтобы ознакомиться с условиями тарифа, на сайте Фина'м точка ру, зайдите в раздел, Форекс, далее выберите, Торговые условия.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Форекс_ClientPersonalRate
            a: Тарифный план при торговле через компанию Фина'м Форекс един для всех клиентов. Чтобы ознакомиться с условиями тарифа, на сайте Фина'м точка ру, зайдите в раздел, Форекс, далее выберите, Торговые условия.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Форекс_RateInformation
            a: Тарифный план при торговле через компанию Фина'м Форекс един для всех клиентов. Чтобы ознакомиться с условиями тарифа, на сайте Фина'м точка ру, зайдите в раздел, Форекс, далее выберите, Торговые условия.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer

        #Информация по конкретному тарифу    
        state: Тариф_Стратег
            a: Абонентская плата в месяц, по тарифу, Стратег, 0 рублей. Ознакомиться с описанием тарифного плана можно на сайте Фина'м точка ру.
            a: Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м. Регламент представлен на сайте фина'м точка ру.
            a: Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Тариф_Дневной
            a: Абонентская плата в месяц, по тарифу, Дневной, 177 рублей. И 400 рублей, в случае если сумма чистых активов на счете менее 2х тысяч рублей. Комиссия за обслуживание уменьшается на сумму других уплаченных в текущем месяце брокерских комиссий.
            a: Ознакомиться с описанием тарифного плана можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м. Регламент представлен на сайте фина'м точка ру.
            a: Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # state: Тариф_ЕдиныйДневной
        #     a: Информация по Тариф_ЕдиныйДневной
        #     a: Чем я могу еще помочь?
        #     q: @repeat_please * ||toState = "."
        
        state: Тариф_ЕдиныйКонсультационный
            a: Тарифный план, Единый Консультационный, предполагает дополнительное информационное и консультационное обслуживание.
            a: Абонентская плата в месяц, по тарифу, Единый Консультационный, 177 рублей. И 400 рублей, в случае если сумма чистых активов на счете менее 2х тысяч рублей.
            a: Комиссия за обслуживание уменьшается на сумму других уплаченных в текущем месяце брокерских комиссий. Ознакомиться с описанием тарифного плана можно на сайте Фина'м точка ру.
            a: Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м. Регламент представлен на сайте фина'м точка ру.
            a: Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Тариф_ЕдиныйТест-Драйв
            a: При открытии своего первого брокерского счёта в Фина'м, вы можете подключить выгодный тариф с бесплатным обслуживанием, Тест Драйв, сроком на один месяц.
            a: Через 30 дней с момента открытия счёта с тарифом, Тест Драйв, тариф автоматически сменится на другой, также без абонентской платы, тариф Стратег.
            a: Ознакомиться с описанием тарифного плана можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м. 
            a: Регламент представлен на сайте фина'м точка ру. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Тариф_ЕдиныйФиксированный
            a: Тарифный план, Единый Фиксированный, предполагает пониженную ставку от торгового дневного оборота, и фиксированную комиссию, 3540 рублей, при совершении одной и более сделок в течение месяца.
            a: Абонентская плата в месяц, по тарифу, Единый Фиксированный, 177 рублей. И 400 рублей, в случае если сумма чистых активов на счете менее 2х тысяч рублей.
            a: Комиссия за обслуживание уменьшается на сумму других уплаченных в текущем месяце брокерских комиссий.
            a: Полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м.
            a: Регламент представлен на сайте фина'м точка ру. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Тариф_Инвестор
            a: Абонентская плата в месяц, по тарифу, Инвестор, 200 рублей. И 400 рублей, в случае если сумма чистых активов на счете менее 2х тысяч рублей. Комиссия за обслуживание уменьшается на сумму других уплаченных в текущем месяце брокерских комиссий.
            a: Ознакомиться с описанием тарифного плана можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м.
            a: Регламент представлен на сайте фина'м точка ру. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # state: Тариф_Консультационный
        #     a: Информация по  Тариф_Консультационный
        #     a: Чем я могу еще помочь?
        #     q: @repeat_please * ||toState = "."
            
        state: Тариф_СтандартныйФортс
            a: Абонентская плата в месяц, по тарифу, Стандартный Фортс, 177 рублей. И 400 рублей, в случае если сумма чистых активов на счете менее 2х тысяч рублей.
            a: Комиссия за обслуживание уменьшается на сумму других уплаченных в текущем месяце брокерских комиссий.
            a: Полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м. Регламент представлен на сайте фина'м точка ру.
            a: Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        # Тариф удален из сущности    
        # state: Тариф_Тест-Драйв
        #     a: Информация по  Тариф_Тест-Драйв
        #     q: @repeat_please * ||toState = "."
            
        state: Тариф_ФриТрейд
            a: При открытии своего первого брокерского счёта в Фина'м, вы можете подключить выгодный тариф с бесплатным обслуживанием, Фри Трейд, сроком на один месяц.
            a: Через 30 дней с момента открытия счёта с Фри Трейд, тариф автоматически сменится на другой, также без абонентской платы, тариф Стратег.
            a: Ознакомиться с описанием тарифного плана можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м. Регламент представлен на сайте фина'м точка ру.
            a: Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Тариф_ЕдиныйOptions
            a: Абонентская плата в месяц, по тарифу, Единый Дневной Опшенс, 177 рублей. И 400 рублей, в случае если сумма чистых активов на счете менее 2х тысяч рублей.
            a: Комиссия за обслуживание уменьшается на сумму других уплаченных в текущем месяце брокерских комиссий. При подключении тарифа на cчет, Сегрегированный Глобал, стоимость обслуживания составит 4,5 доллара США.
            a: Ознакомиться с описанием тарифного плана можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Также, полные условия тарифных планов можно изучить в Приложении номер 7, к Регламенту брокерского обслуживания Фина'м.
            a: Регламент представлен на сайте фина'м точка ру. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # Тариф удален из сущности     
        # state: Тариф_ФриТрейд2.0
        #     a: Информация по Тариф_ФриТрейд2.0
        #     q: @repeat_please * ||toState = "."

    
                
    state: Налоговые вычеты
        intent!: /021 Налоговые вычеты

        script:
            $analytics.setMessageLabel("021 Налоговые вычеты", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }            
            if ( typeof $parseTree._deductionType != "undefined" ){
                $session.deductionType = $parseTree._deductionType;
            }
            if ( typeof $parseTree._IISType != "undefined" ){
                $session.IISType = $parseTree._IISType;
            }            
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Налоговые вычеты/Уточнение компании");
            }
            if ( typeof $session.deductionType == "undefined" ){
                $reactions.transition("/Налоговые вычеты/Уточнение типа вычета");
            } else {
                $reactions.transition("/Налоговые вычеты/" + $session.company.name + "_" + $session.deductionType.name);
            }
        
        state: Уточнение компании
            a: Вас интересует информация о налоговых вычетах по счета'м брокера или по счета'м в Управляющей компании.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Налоговые вычеты");
                    
        state: Уточнение типа вычета
            a: Информация по какому типу налогового вычета вас интересует; Вычет по счёту ИИС; Трёхгодичная льгота; Льгота по бумагам инновационного сектора; Пятилетняя льгота по операциям с эмитентом чьи активы состоят из недвижимости на территории эРэФ не более чем на 50%.
           
            q: * @choice_1 ||toState = "/Налоговые вычеты/Брокер_IISDeduction"
            q: * @choice_2 ||toState = "/Налоговые вычеты/Брокер_3YearsBenefit"
            q: * @choice_3 ||toState = "/Налоговые вычеты/Брокер_InnovationSector"
            q: * @choice_4 ||toState = "/Налоговые вычеты/Брокер_50%Benefit"
            q: * @choice_last ||toState = "/Налоговые вычеты/Брокер_50%Benefit"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @deductionType *
                script:
                    $session.deductionType = $parseTree._deductionType;
                    $reactions.transition("/Налоговые вычеты");                    
        
        state: Определение типа вычета по ИИС
            a: Информация по какому типу вычета, вас интересует? Тип, А, или Бэ?
            q: * @type_a_u * ||toState = "/Налоговые вычеты/Брокер_IISDeduction_TypeA"
            q: * @type_b_u * ||toState = "/Налоговые вычеты/Брокер_IISDeduction_TypeB"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @IISType *
                script:
                    $session.IISType = $parseTree._IISType;
                    $reactions.transition("/Налоговые вычеты/" + $session.company.name + "_IISDeduction");                    
                     
            
        #проверка на наличие информации по типу вычета ИИС
        state: Брокер_IISDeduction
            script:
                if ( typeof $session.IISType == "undefined"){
                    $reactions.transition("/Налоговые вычеты/Определение типа вычета по ИИС");
                } else {
                    $reactions.transition("/Налоговые вычеты/" + $session.company.name + "_IISDeduction_" + $session.IISType.name);
                }
                
                
        state: Брокер_IISDeduction_TypeA
            a: Максимальная сумма для вычета по типу а, за календарный год составляет 400000 ₽. В зависимости от ставки налога на ваш доход, Государство вернет вам 13% или 15% от той суммы, которую вы внесли на ИИС в отчетном году. 
            a: Таким образом, максимальная сумма налога, подлежащая возврату, составит до 52000 или до 60000 рублей соответственно. С 2020 года вычет по типу А, можно оформлять в упрощенном порядке. 
            a: Скачать пакет документов для самостоятельной подачи, или подать заявку на получение вычета в упрощенном порядке, можно в личном кабинете на сайте, фина'м точка ру. 
            a: Для этого зайдите в личный кабинет, далее выберите раздел, документы, далее выберите пункт меню, налоги и справки, далее выберите нужное.
            a: Обращаем ваше внимание, что для подачи заявления в упрощенном порядке за 2021й год нужно обратиться к менеджеру.
            a: Заявления будут отправлены до 25 февраля 2024 года, после размещения налоговой службой данных для заполнения в кабинете налогоплательщика. До момента отправки будет отображаться статус, Ожидает отправки в ФэНэ эС.
            a: Если в личном кабинете налогоплательщика пришел отказ по упрощенной процедуре, а также при оформлении вычета, по стандартной процедуре за более ранние периоды, вам потребуется собрать следующие документы и обратиться в налоговую.
            a: Справка 2 НДФЛ с места работы. Платежное поручение об отправке денежных средств на ИИС. Пакет документов об открытии счёта и брокерский отчет.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: Брокер_IISDeduction_TypeB  
            a: Максимальная сумма для вычета по типу Бэ, равна доходу, полученному от торговых операций, учтенных на договоре ИИ'С, данная инвестиционная прибыль при вычете по типу Бэ, налогом не облагается. 
            a: Чтобы оформить вычет по типу Бэ, можно подать заявку на получение вычета в упрощенном порядке, в личном кабинете на сайте, фина'м точка ру. 
            a: Для этого зайдите в личный кабинет, далее выберите раздел, документы, далее выберите пункт меню, налоги и справки, далее выберите нужное. После подачи заявления в течение двух рабочих дней, ожидайте новый статус заявления, Принято к исполнению. 
            a: После получение данного статуса, в течение 30 дней нужно вывести средства со счёта ИИС и закрыть счет ИИС. Доход, полученный на счете ИИС, не будет облагаться налогом.    
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Брокер_InnovationSector
            a: Инвестор освобождается от уплаты 13% НДФЛ по операциям с ценными бумагами высокотехнологичного инновационного сектора. Актуальный перечень таких бумаг представлен на сайте московской биржи.
            a: Условиями получения такой льготы являются приобретение бумаг не ранее включения эмитента в перечень, и их продажа до исключения из этого перечня.
            a: И непрерывное владение бумагами более одного года. Льгота предоставляется брокером по запросу в отдел поддержки.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: Брокер_3YearsBenefit
            a: Инвесторы могут не платить НДФЛ с дохода от продажи ценных бумаг, которыми владели более трех лет. Если у вас в портфеле, за исключением договора ИИС, есть бумаги, приобретенные после 1 января 2014 года.
            a: И вы владеете ими непрерывно более трех лет, то вы можете претендовать на трех годичную льготу. 
            a: Проверить наличие бумаг, попадающих под трех годичную льготу на счетах в Фина'м, а также подать заявление на ее предоставление, можно в личном кабинете брокера на сайте, едо'кс точка Фина'м точка ру.
            a: Для этого выберите меню, Услуги, далее выберите раздел, Налоги, выписки, справки, в поле меню налоги, выберите нужное. 
            a: Заявление на получение льготы нужно подписать до вывода средств от продажи ценных бумаг. Оно действует в течение одного календарного года.
            a: Обращаем ваше внимание, если бумаги были приобретены через другого брокера, или получены в дар, и по ним отсутствует возможность подачи заявления в личном кабинете, то для оформления льготы нужно обратиться в налоговую.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Брокер_50%Benefit
            a: Инвестор освобождается от уплаты 13% НДФЛ по операциям с акциями российских и иностранных организаций, если активы эмитента состоят из недвижимости на территории РФ не более чем на 50%.
            a: Условиями получения льготы являются непрерывное владение бумагами более 5 лет и отсутствие сделок займа или РЭ'ПО.
            a: А также необходима справка от эмитента, что на последний день месяца предшествующего месяцу продажи ЦБ, активы эмитента состояли из недвижимости на территории эРэФ не более чем на 50%. отсутствуют.
            a: Воспользоваться льготой можно через Фина'м, до 31 января года, следующего за годом продажи бумаг. Через ИФНС обращаться можно в течение трёх лет, следующих за отчетным периодом, в котором произошла реализация бумаг.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        #Информация по УК
        state: УК_IISDeduction
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: УК_IISDeduction_TypeA
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: УК_IISDeduction_TypeB
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: УК_InnovationSector
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: УК_3YearsBenefit
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario

 
    state: ИИС  
        intent!: /022 ИИС
        script:
            $analytics.setMessageLabel("022 ИИС", "Интенты");
        
        a: На индивидуальных инвестиционных счетах, инвесторам доступны операции с российскими ценными бумагами на Московской и СПБ Бирже; На Московской бирже также доступны такие инструменты как облигации, паи инвестиционных фондов, фьючерсы, опционы, и валюты.
        a: Какая информация по ИИС вас интересует? Дата открытия ИИС. Пополнение. Перевод ИИС от брокера к брокеру, или статус налогового вычета.
            
        q: * @IIS_OpeningDate * ||toState = "/ИИС_Дата открытия"
        q: * @IIS_Replenishment * ||toState = "/ИИС_Пополнение"
        q: * @IIS_Transfer * ||toState = "/ИИС_Перевод"
        q: * @IIS_Status_u * ||toState = "/ИИС_Статус вычета"
        q: * @IIS_Open * ||toState = "/ИИС_Открытие_закрытие/Ответ_Открытие"
        q: * @IIS_Close * ||toState = "/ИИС_Открытие_закрытие/Ответ_Закрытие"
        q: * @choice_1 * ||toState = "/ИИС_Дата открытия"
        q: * @choice_2 * ||toState = "/ИИС_Пополнение"
        q: * @choice_3 * ||toState = "/ИИС_Перевод"
        q: * @choice_4 * ||toState = "/ИИС_Статус вычета"
        # # choice 5 открытие закрытие?
        q: * @choice_last * ||toState = "/ИИС_Статус вычета"
        q: @repeat_please * ||toState = "."
               
        
    state: ИИС_Дата открытия
        intent!: /022 ИИС/ИИС_Дата открытия
        
        script:
            $analytics.setMessageLabel("ИИС_Дата открытия", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/ИИС_Дата открытия/Уточнение компании");
            } else {
                $reactions.transition("/ИИС_Дата открытия/Ответ_" + $session.company.name);
            } 
        

        state: Уточнение компании
            a: Вас интересует Информация по счету ИИС, в брокерской? или в Управляющей компании.
            # q: * @choice_1 ||toState = "/ИИС_Дата открытия/Ответ_Брокер"
            # q: * @choice_2 ||toState = "/ИИС_Дата открытия/Ответ_УК"
            # q: * @choice_last ||toState = "/ИИС_Дата открытия/Ответ_УК"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/ИИС_Дата открытия");  
        
        state: Ответ_Брокер
            a: Перечень действующих счетов доступен в личном кабинете на сайте, фина'м точка ру. Проверить дату открытия договора ИИС, и актуальный тариф, можно в личном кабинете, для этого выберите счет с названием КЛФ ИИС, далее выберите раздел, детали.
            a: Если счет ИИС переведен от другого брокера, первичную дату открытия можно уточнить у менеджера фина'м, или в личном кабинете брокера на сайте, едо'кс точка Фина'м точка ру, кликнув на счет с названием КЛФ ИИС. 
            a: Историю пополнения договора ИИС, вы можете посмотреть в истории операций по счету в личном кабинете на сайте, фина'м точка ру. для этого рядом с разделом портфель, выберите вкладку история.
            a: Также посмотреть историю пополнения договора ИИС, можно в справке по счету. Заказать справку по брокерскому счету, можно в личном кабинете на сайте, фина'м точка ру, для этого выберите меню документы, далее выберите раздел, налоги и справки.
            a: Максимальный интервал получения справки по счету, 92 дня. При необходимости получить годовой отчет, справку можно сформировать 4 раза. Или запросить заверенный брокерский отчет у менеджера фина'м.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_УК
            script:
                //1000
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
    
    state: ИИС_Пополнение
        intent!: /022 ИИС/ИИС_Пополнение
        
        script:
            $analytics.setMessageLabel("ИИС_Пополнение", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/ИИС_Пополнение/Уточнение компании");
            } else {
                $reactions.transition("/ИИС_Пополнение/Ответ_" + $session.company.name);
            } 
        
        # Можно вынести отдельно чтобы не дублировать в каждом интенте
        state: Уточнение компании
            a: Вас интересует Информация по счету ИИС, в брокерской? или в Управляющей компании.
            # q: * @choice_1 ||toState = "/ИИС_Пополнение/Ответ_Брокер"
            # q: * @choice_2 ||toState = "/ИИС_Пополнение/Ответ_УК"
            # q: * @choice_last ||toState = "/ИИС_Пополнение/Ответ_УК"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/ИИС_Пополнение");  
        
        state: Ответ_Брокер
            # a: В преддверии новогодних праздников, рекомендуем пополнять счет ИИС заблаговременно, учитывая срок зачисления средств. Последний день зачисления средств в 2023 году - 29 декабря.
            # a: Обращаем ваше внимание, что пополнение по реквизитам, занимает до 3х рабочих дней. Если у вас осталось менее трех дней для пополнения счета, то рекомендуем выбрать другие способы; например, системой быстрых платежей, или наличными, в офисе фина'м.
            a: Пополнять счет ИИС можно только в валюте рубль РФ, на сумму не более 1000000 рублей в год, отправителем средств на ИИС должен являться владелец этого счета.
            a: Рекомендуем пополнять счет ИИС наличными в кассе, безналичным платежом по реквизитам или через систему быстрых платежей.
            a: При оформлении вычета, налоговая имеет право запросить платежное поручение с подтверждением внесения средств на ИИС.
            a: Чтобы пополнить счет ИИС по реквизитам, в личном кабинете на сайте, фина'м точка ру, выберите вкладку, пополнение, далее выберите, переводом из другого банка. Под суммой пополнения выберите способ, по реквизитам. Деньги поступят в течение дня.
            a: Срок может быть увеличен до трех рабочих дней, в зависимости от исполнения платежа банком-отправителем. За данную операцию, фина'м не взимает комиссию. 
            a: Однако возможна комиссия со стороны банка-отправителя. Через кассу представительства фина'м, можно пополнить ИИС наличными средствами, без комиссии. Для этого вам понадобится действующий паспорт гражданина Российской Федерации.
            a: Адрес ближайшего офиса можно посмотреть на сайте Фина'м точка ру, в разделе контактная информация, внизу страницы.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
    
    state: ИИС_Перевод
        intent!: /022 ИИС/ИИС_Перевод
        
        script:
            $analytics.setMessageLabel("ИИС_Перевод", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/ИИС_Перевод/Уточнение компании");
            } else {
                $reactions.transition("/ИИС_Перевод/Ответ_" + $session.company.name);
            } 
        
        # Можно вынести отдельно чтобы не дублировать в каждом интенте
        state: Уточнение компании
            a: Вас интересует Информация по счету ИИС, в брокерской? или в Управляющей компании.
            # q: * @choice_1 ||toState = "/ИИС_Перевод/Ответ_Брокер"
            # q: * @choice_2 ||toState = "/ИИС_Перевод/Ответ_УК"
            # q: * @choice_last ||toState = "/ИИС_Перевод/Ответ_УК"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/ИИС_Перевод");  
        
        state: Ответ_Брокер
            # a: 222
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
                
        
    state: ИИС_Статус вычета
        intent!: /022 ИИС/ИИС_Статус вычета
        
        script:
            $analytics.setMessageLabel("ИИС_Статус вычета", "Интенты");
                        
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/ИИС_Статус вычета/Уточнение компании");
            } else {
                $reactions.transition("/ИИС_Статус вычета/Ответ_" + $session.company.name);
            } 
        
        # Можно вынести отдельно чтобы не дублировать в каждом интенте
        state: Уточнение компании
            a: Вас интересует Информация по счету ИИС, в брокерской? или в Управляющей компании.
            # q: * @choice_1 ||toState = "/ИИС_Статус вычета/Ответ_Брокер"
            # q: * @choice_2 ||toState = "/ИИС_Статус вычета/Ответ_УК"
            # q: * @choice_last ||toState = "/ИИС_Статус вычета/Ответ_УК"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/ИИС_Статус вычета");  
        
        state: Ответ_Брокер
            # a: 111
            a: Статус отправки сведений в ФНС, по упрощенному порядку для получения вычета типа А, можно отслеживать в личном кабинете на сайте, фина'м точка ру.
            a: Для этого выберите раздел, документы, далее выберите пункт меню, налоги и справки, далее выберите, Упрощенный порядок получения вычета.
            a: Если заявление принято в работу, то федеральная налоговая служба сформирует для вас предсоставленное заявление, которое необходимо подписать в личном кабинете налогоплательщика. Срок камеральной проверки после подписания декларации, один месяц. 
            a: Статус отправки сведений в ФНС, по упрощенному порядку для получения вычета типа Б, можно отслеживать в личном кабинете на сайте, фина'м точка ру, для этого выберите меню, документы, далее выберите раздел, документы, журнал поручений.
            a: Если заявление было принято, и подтверждено федеральной налоговой службой, то в течение 30 дней, нужно вывести средства и закрыть счет ИИС. Доход, полученный на счете ИИС, не будет облагаться налогом.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
    
            
    state: ИИС_Открытие_закрытие
        intent!: /022 ИИС/ИИС_Открытие_закрытие
        
        script:
            $analytics.setMessageLabel("ИИС_Открытие_закрытие", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $parseTree._IIS_Open != "undefined" ){
                $reactions.transition("/ИИС_Открытие_закрытие/Ответ_Открытие");
            }            
            if ( typeof $parseTree._IIS_Close != "undefined" ){
                $reactions.transition("/ИИС_Открытие_закрытие/Ответ_Закрытие");
            }

        
        state: Уточнение компании
            a: Вас интересует Информация по счету ИИС, в брокерской? или в Управляющей компании.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/ИИС_Открытие_закрытие/Ответ_" + $session.open_close);  
        
        state: Ответ_Открытие
            script:
                if ( typeof $session.company == "undefined" ){
                    $session.open_close = 'Открытие'; 
                    $reactions.transition("/ИИС_Открытие_закрытие/Уточнение компании");
                } else {
                    var phoneNumber = $session.company.phoneNumber;
                    var companyName = $session.company.name;
                    $session = {};
                    $session.operatorPhoneNumber = phoneNumber;
                    $reactions.transition("/ИИС_Открытие_закрытие/Ответ_Открытие/" + companyName);
                }
                    
            state: Брокер        
                a: Совершеннолетние граждане РФ могут открыть счет ИИС в компании фина'м, как дистанционно, так и в офисе компании. С 1 января 2024 года доступно открытие до трех счетов нового типа ИИС.
                a: Дистанционно подать заявку на открытие счета, можно на сайте, фина'м точка ру. Желтая кнопка, открыть счет, находится в верхнем правом углу страницы. А также подать заявку на открытие ИИС можно в личном кабинете. Для заполнения анкеты понадобится мобильный телефон, и гражданский паспорт.
                a: Хотите получить консультацию у оператора по открытию брокерского счёта?
                # script: 
                #     $context.session = {};
                q: @agree ||toState = "/Оператор/Оператор по номеру"    
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?"
                # final answer
                
            state: УК
                script:
                    # $session.operatorPhoneNumber =  $session.company.phoneNumber;
                    $reactions.transition("/Оператор/Оператор по номеру");
                    # final scenario

        state: Ответ_Закрытие
            script:
                if ( typeof $session.company == "undefined" ){
                    $session.open_close = 'Закрытие';
                    $reactions.transition("/ИИС_Открытие_закрытие/Уточнение компании");
                } else {
                    var phoneNumber = $session.company.phoneNumber;
                    var companyName = $session.company.name;
                    $session = {};
                    $session.operatorPhoneNumber = phoneNumber;
                    $reactions.transition("/ИИС_Открытие_закрытие/Ответ_Закрытие/" + companyName);
                }
                    
            state: Брокер
                a: Способ закрытия счёта ИИС зависит от желаемого типа налогового вычета. Хотите получить консультацию у оператора?
                # script: 
                #     $context.session = {};
                q: @agree ||toState = "/Оператор/Оператор по номеру"    
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?"
                # final answer
                
            state: УК
                script:
                    # $session.operatorPhoneNumber =  $session.company.phoneNumber;
                    $reactions.transition("/Оператор/Оператор по номеру");
                    # final scenario

                    
    state: ИИС 3
        intent!: /048 ИИС 3
        
        a: Расскажу про отличия нового типа ИИС от старого. Инвестор может открывать и владеть тремя ИИС нового типа одновременно. При этом, счёт ИИС, открытый до 31 декабря 2023 года, по-прежнему можно иметь только один.
        a: Если у инвестора уже есть счёт ИИС старого образца, открытый до 31 декабря 2023 года, то при желании дополнительно открыть ИИС нового образца, нужно трансформировать старый тип ИИС в новый.
        a: Трансформация ИИС доступна в личном кабинете на сайте едо'кс точка фина'м точка ру. В разделе личного кабинета Услуги; Прочие услуги. Договоры ИИС, открытые в период с 2024ого по 2026й год, нельзя закрыть раньше, чем через 5 лет.
        a: Каждый последующий год, минимальный срок владения ИИС будет увеличиваться, и к 2031ому году составит 10 лет. В то время как для счетов, открытых до 31 декабря 2023 года, доступно пополнение до 1000000 рублей в год, у ИИС нового типа нет ограничений на сумму пополнения.
        a: К ИИС, открытым до 31 декабря 2023 года, можно применить один налоговый вычет на выбор. По ИИС третьего типа, можно будет получать обе льготы сразу, при этом по льготе в размере финансового результата (тип Б) максимальный лимит составит 30000000 рублей.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
        
    
    state: Маржинальная торговля
        intent!: /024 Маржинальная торговля
        script:
            $analytics.setMessageLabel("024 Маржинальная торговля", "Интенты");
        
        # a: Обращаем ваше внимание! В ближайшее время ставки риска по ценным бумагам на иностранных биржах, и валютам, будут повышаться в связи с требованием рыночной конъектуры.
        a: Какая информация по маржинальной торговле Вас интересует? Открытие маржинальной позиции, отключение или подключение маржинальной торговли, где посмотреть уровень маржи' по счету, уровни риска КПУР, КСУР, или ставки риска по инструментам?
        
        q: * @open_margin * ||toState = "/Маржинальная торговля_открытие позиции"
        q: * @open_close_margin_u * ||toState = "/Маржинальная торговля_подключение|отключение"
        q: * @level_margin * ||toState = "/Маржинальная торговля_уровень маржи"
        q: * @KPUR_KSUR * ||toState = "/Маржинальная торговля_КПУР|КСУР"
        q: * @risk_rate * ||toState = "/Маржинальная торговля_ставка риска"
        q: * @choice_1 * ||toState = "/Маржинальная торговля_открытие позиции"
        q: * @choice_2 * ||toState = "/Маржинальная торговля_подключение|отключение"
        q: * @choice_3 * ||toState = "/Маржинальная торговля_уровень маржи"
        q: * @choice_4 * ||toState = "/Маржинальная торговля_КПУР|КСУР"
        q: * @choice_5 * ||toState = "/Маржинальная торговля_ставка риска"
        q: * @choice_last * ||toState = "/Маржинальная торговля_ставка риска"
        q: @repeat_please * ||toState = "."
        
    
    state: Маржинальная торговля_открытие позиции
        intent!: /024 Маржинальная торговля/Маржинальная торговля_открытие позиции
        
        script:
            $analytics.setMessageLabel("Маржинальная торговля_открытие позиции", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Маржинальная торговля_открытие позиции/Уточнение компании");
            } else {
                $reactions.transition("/Маржинальная торговля_открытие позиции/Ответ_" + $session.company.name);
            } 
        
        # Можно вынести отдельно чтобы не дублировать в каждом интенте
        state: Уточнение компании
            a: Уточните, ваш вопрос по брокерскому счёту, или по счёту форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Маржинальная торговля_открытие позиции");  

            
        state: Ответ_Брокер
            a: Для открытия маржинальной позиции нужно подключить возможность маржинальной торговли по счету. Чтобы подключить маржинальную торговлю авторизуйтесь в личном кабинете на сайте, едо'кс точка фина'м точка ру.
            a: Далее в разделе, Торговля, выберите, тестирование для неквалифицированного инвестора по категории Необеспеченные сделки.
            a: Для квалифицированных инвесторов доступ предоставляется автоматически. Обращаем ваше внимание, что маржинальная торговля может быть ограничена по ряду инструментов. Например, на Гонконгской бирже маржинальная торговля не доступна.
            a: Ознакомиться со списком доступных инструментов для длинных и коротких позиций, а также ставками риска, можно на сайте фина'м точка ру. В верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, и в выпадающем меню выберите пункт Список маржинальных бумаг.
            a: Ставки риска могут отличаться на сайте и в торговых системах в зависимости от рыночной ситуации. Самую актуальную информацию по ставкам риска можно узнать в торговой системе Транза'к в Описании инструмента, а также у менеджера Фина'м.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
    state: Маржинальная торговля_подключение|отключение
        intent!: /024 Маржинальная торговля/Маржинальная торговля_подключение|отключение
        
        script:
            $analytics.setMessageLabel("Маржинальная торговля_подключение|отключение", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Маржинальная торговля_подключение|отключение/Уточнение компании");
            } else {
                $reactions.transition("/Маржинальная торговля_подключение|отключение/Ответ_" + $session.company.name);
            }
        
        state: Уточнение компании
            a: Уточните, ваш вопрос по брокерскому счёту, или по счёту форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Маржинальная торговля_подключение|отключение");     
        
        state: Ответ_Брокер
            a: Маржинальная торговля или необеспеченные сделки, это операции с использованием заемных средств брокера, которые одновременно повышают потенциальный риск и потенциальную доходность операции.
            a: Чтобы подключить маржинальную торговлю, нужно пройти тестирование для неквалифицированного инвестора по категории Необеспеченные сделки. Это можно сделать в личном кабинете на сайте фина'м точка ру.
            a: Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус.
            a: Далее выберите, пройти тестирование. Для квалифицированных инвесторов доступ предоставляется автоматически. Чтобы отключить маржинальную торговлю обратитесь к менеджеру фина'м.
            a: После отключения будет заблокирована возможность использования заемных средств брокера по счету, а также доступ к коротким позициям. Узнать, подключена ли у вас маржинальная торговля можно у менеджера.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        
    state: Маржинальная торговля_уровень маржи
        intent!: /024 Маржинальная торговля/Маржинальная торговля_уровень маржи
        
        script:
            $analytics.setMessageLabel("Маржинальная торговля_уровень маржи", "Интенты");
             
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Маржинальная торговля_уровень маржи/Уточнение компании");
            } else {
                $reactions.transition("/Маржинальная торговля_уровень маржи/Ответ_" + $session.company.name);
            }
        
        state: Уточнение компании
            a: Уточните, ваш вопрос по брокерскому счёту, или по счёту форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Маржинальная торговля_уровень маржи");    

        state: Ответ_Брокер
            a: Информацию о состоянии портфеля, значениях маржи и запасе портфеля до принудительного закрытия можно посмотреть в личном кабинете на сайте фина'м точка ру.
            a: Для этого выберите нужный счет, в разделе, детали по счету, раскройте строку, показатели риска.
            a: В терминале фина'м трейд, начальные требования, суммарную оценку денежных средств, ценных бумаг и обязательств, можно посмотреть в разделе, Аналитика по счету. в мобильном приложении фина'м трейд, в разделе, Детали по счету.
            a: В терминале КВИК, следить за маржинальными требованиями можно с помощью таблицы, Клиентский портфель. Для этого выберите на панели инструментов, Создать окно, Все типы окон, Клиентский портфель.
            a: В терминале Meta Trader 5, в строке Баланс, показатели Активы Маржа' Уровень маржи' и другие, будут отображаться только при открытых позициях на фондовой и валютной секциях.
            a: В случае, если торговля ведется только по фьючерсным контрактам, то за показателями риска можно следить через личный кабинет.
            a: Хотите узнать, как закрыть задолженность? 
            script: 
                $context.session = {};
            q: @agree ||toState = "/Закрыть задолженность"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        
    state: Маржинальная торговля_КПУР|КСУР
        intent!: /024 Маржинальная торговля/Маржинальная торговля_КПУР|КСУР
        
        script:
            $analytics.setMessageLabel("Маржинальная торговля_КПУР|КСУР", "Интенты");
            
        go!: /Маржинальная торговля_КПУР|КСУР/Ответ_Брокер
        

        state: Ответ_Брокер
            a: При открытии брокерского счёта, инвестору по умолчанию присваивается стандартный уровень риска, или КСУР. Уровни риска влияют на величину кредитного плеча, которое будет доступно при подключении маржинальной торговли.
            a: Инвестор может получить категорию повышенного уровня риска, или КПУР, если сумма его активов на его брокерских счетах не менее трех миллионов рублей.
            a: Либо, сумма активов более 600000 рублей, и он является клиентом брокера в течение последних 180 дней и заключал сделки с ценными бумагами или производными финансовыми инструментами на протяжении пяти и более дней.
            a: Для КПУР применяются ставки маржинального обеспечения ниже, чем для КСУР. Таким образом, статус клиента с повышенным уровнем риска дает больше возможностей для наращивания маржинальных позиций, или размера плеча, но и повышает финансовые риски.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
    state: Маржинальная торговля_ставка риска
        intent!: /024 Маржинальная торговля/Маржинальная торговля_ставка риска
        
        script:
            $analytics.setMessageLabel("Маржинальная торговля_ставка риска", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Маржинальная торговля_ставка риска/Уточнение компании");
            } else {
                $reactions.transition("/Маржинальная торговля_ставка риска/Ответ_" + $session.company.name);
            }
        
        
        state: Уточнение компании
            a: Уточните, ваш вопрос по брокерскому счёту, или по счёту форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Маржинальная торговля_ставка риска");
        

        state: Ответ_Брокер
            a: Ознакомиться со списком доступных инструментов для длинных и коротких позиций, а также ставками риска, можно на сайте фина'м точка ру.
            a: В верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, и в выпадающем меню выберите пункт Список маржинальных бумаг.
            a: Ставки риска могут отличаться на сайте и в торговых системах в зависимости от рыночной ситуации. Самую актуальную информацию по ставкам можно узнать в торговой системе Транза'к в Описании инструмента, а также у менеджера Фина'м.
            # a: Обращаем ваше внимание! В ближайшее время ставки риска по ценным бумагам на иностранных биржах, и валютам, будут повышаться в связи с требованием рыночной конъектуры.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
    
    
    
    state: Срочный рынок  
        intent!: /026 Срочный рынок
        script:
            $analytics.setMessageLabel("026 Срочный рынок", "Интенты");
        
        a: Какая информация о торговле на срочном рынке Вас интересует? Как узнать гарантийное обеспечение? как уменьшить гарантийное обеспечение? Как посмотреть свой финансовый результат? Узнать подробнее о торговле фьючерсами.
        
        q: * @which_GO * ||toState = "/Срочный рынок_гарантийное обеспечение по счету"
        q: * @what_profit_loss * ||toState = "/Срочный рынок_прибыль|убыток по счету"
        q: * @lower_GO * ||toState = "/Срочный рынок_уменьшение гарантийного обеспечения"
        q: * @futures_trading * ||toState = "/Срочный рынок_покупка|продажа фьючерса"
        q: * @choice_1 * ||toState = "/Срочный рынок_гарантийное обеспечение по счету"
        q: * @choice_2 * ||toState = "/Срочный рынок_уменьшение гарантийного обеспечения"
        q: * @choice_3 * ||toState = "/Срочный рынок_прибыль|убыток по счету"
        q: * @choice_4 * ||toState = "/Срочный рынок_покупка|продажа фьючерса"
        q: * @choice_last * ||toState = "/Срочный рынок_покупка|продажа фьючерса"
        q: @repeat_please * ||toState = "."
        
        
    state: Срочный рынок_гарантийное обеспечение по счету
        intent!: /026 Срочный рынок/Срочный рынок_гарантийное обеспечение по счету
        script:
            $analytics.setMessageLabel("Срочный рынок_гарантийное обеспечение по счету", "Интенты");
        
        go!: /Срочный рынок_гарантийное обеспечение по счету/Ответ_Брокер
        
        state: Ответ_Брокер
            a: При открытии позиции по фьючерсу, на счете блокируется гарантийное обеспечение, или ГэО'. При закрытии позиции, заблокированные средства освобождаются.
            a: Проверить актуальное ГэО' по счету можно в системе Транза'к в информации по инструменту, либо уточнить у менеджера. Величина ГэО' устанавливается биржей и публикуется на сайте мо'екс точка ком.
            a: По единым брокерским счета'м, Размер гарантийного обеспечения формируется на основании ставок риска по инструментам, и категории риска клиента, КСУР или КПУР.
            a: По умолчанию для счетов со статусом КСУР, ГэО' почти в два раза больше биржевого, в связи с действующими требованиями к риск-менеджменту.
            a: При выставлении рыночной заявки гарантийное обеспечение увеличивается в 1,5 раза. Рекомендуется использовать лимитные заявки.
            a: Хотите узнать способы, как уменьшить гарантийное обеспечение?
            script:
                $context.session = {};
            q: @agree ||toState = "/Срочный рынок_уменьшение гарантийного обеспечения"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
    
    state: Срочный рынок_прибыль|убыток по счету
        intent!: /026 Срочный рынок/Срочный рынок_прибыль|убыток по счету
        script:
            $analytics.setMessageLabel("Срочный рынок_прибыль|убыток по счету", "Интенты");
        
        go!: /Срочный рынок_прибыль|убыток по счету/Ответ_Брокер
        
        state: Ответ_Брокер
            a: Прибыль или убыток по фьючерсам и опционам, зачисляется или списывается в виде вариационной маржи'. Позиционная вариационная маржа' начисляется на контракты, которые есть в портфеле на утро.
            a: Посделочная вариационная маржа' начисляется в день открытия позиции по фьючерсу или опциону. На следующий день, и до момента закрытия позиции начисляется позиционная вариационная маржа'.
            a: Если позиция открыта и закрыта внутри торговой сессии, то будет зачислена посделочная маржа'.
            a: Фактическое зачисление вариационной маржи' на счет происходит в основной клиринг в 19 ноль пять по московскому времени. Движение маржи' отображается в справке по счету, а также в истории операций по счету.
            a: Параметры инструментов для расчета вариационной маржи' доступны на сайте Московской биржи.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
    
    state: Срочный рынок_уменьшение гарантийного обеспечения
        intent!: /026 Срочный рынок/Срочный рынок_уменьшение гарантийного обеспечения
        
        script:
            $analytics.setMessageLabel("Срочный рынок_уменьшение гарантийного обеспечения", "Интенты");
            
            if ( typeof $parseTree._GOreductionType != "undefined" ){
                $session.GOreductionType = $parseTree._GOreductionType;
            }
            if ( typeof $session.GOreductionType == "undefined" ){
                $reactions.transition("/Срочный рынок_уменьшение гарантийного обеспечения/Уточнение способа уменьшения");
            } else {
                $reactions.transition("/Срочный рынок_уменьшение гарантийного обеспечения/Ответ_" + $session.GOreductionType.name);
            }
        
            
        state: Ответ_Открытие моносчета
            a: В рамках договора с раздельными моносчета'ми, по счету для срочного рынка размер гарантийного обеспечения равен биржевому. Для открытия моносче'та, нужно авторизоваться в личном кабинете на сайте фина'м точка ру.
            a: Нажать кнопку, Открыть новый счет, далее выбрать, Показать все продукты, далее выбрать Брокерскую компанию, Договор с отдельными брокерскими счетами.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
                
        state: Ответ_Отключение ФС и ВС
            a: Чтобы отключить фондовую и валютную секцию по единому счету, нужно проверить счет на соответствие следующим требованиям. Сумма средств на счете должна быть более 10000 рублей.
            a: По счету отсутствуют сделки с ценными бумагами и валютой. Ваш инвестиционный профиль должен быть умеренный или агрессивный.
            a: Для смены инвест профиля, нужно авторизоваться в личном кабинете на сайте фина'м точка ру, в правом верхнем углу личного кабинета нажать на значок персоны, далее выбрать, Инвестиционный профиль.
            a: Если счет соответствует всем требованиям, обратитесь к менеджеру фина'м для отключения фондовой и валютной секции.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer

            
        state: Ответ_Подключение ПГО
            # a: Услуга пониженного гарантийного обеспечения может быть недоступна 29 декабря и 3 января.
            # a: Также обращаем ваше внимание, что в период новогодних праздников, по части инструментов могут быть повышены ставки риска и увеличен коэффициент залога, с которым валюта ю ань принимается в обеспечение. Про'сим учитывать данную информацию при планировании торговых операций.
            a: Обращаем ваше внимание, Услуга пониженного гарантийного обеспечения может быть недоступна в ближайшие дни в связи с возможной повышенной волатильностью курса рубля. Про'сим учитывать данную информацию при планировании торговых операций.
            a: Узнать условия подключения, подключить или отключить услугу пониженного гарантийного обеспечения, можно в личном кабинете на сайте едо'кс точка фина'м точка ру.
            a: Для этого в разделе, Услуги, выберите пункт меню Прочие операции, далее выберите, Услуга Пониженное ГэО'.
            a: Услуга действует в будние дни, с девяти утра до девятнадцати тридцати по московскому времени, по ограниченному списку инструментов.
            a: Ознакомиться со списком ставок риска по ценным бумагам при подключенной услуге Пониженное ГэО' можно на сайте фина'м точка ру.
            a: В верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, и в выпадающем меню выберите пункт Список маржинальных бумаг.
            a: Далее скачайте файл с названием, Параметры используемые фина'м для обслуживания по Единому счету и счета'м срочного рынка.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
        state: Ответ_Получение КПУР
            a: Инвестор может получить категорию повышенного уровня риска, или КПУР, если сумма его активов на его брокерских счетах не менее трех миллионов рублей.
            a: Либо, сумма активов более 600000 рублей, и он является клиентом брокера в течение последних 180 дней и заключал сделки с ценными бумагами или производными финансовыми инструментами на протяжении пяти и более дней.
            a: Для КПУР гарантийное обеспечение ниже, чем для КСУР. Таким образом, статус клиента с повышенным уровнем риска дает больше возможностей для наращивания маржинальных позиций, или размера плеча, но и повышает финансовые риски.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
                
 
        state: Уточнение способа уменьшения
            a: Есть четыре способа снизить гарантийное обеспечение.
            a: Использовать услугу Пониженное гарантийное обеспечение; открыть договор с раздельными моносчета'ми, или отключить фондовую и валютную секцию на едином счете; также можно получить статус клиента с повышенным уровнем риска, КПУР.
            a: Какой способ вас интересует?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @GOreductionType *
                script:
                    $session.GOreductionType = $parseTree._GOreductionType;
                    $reactions.transition("/Срочный рынок_уменьшение гарантийного обеспечения");
            
        
    state: Срочный рынок_покупка|продажа фьючерса
        intent!: /026 Срочный рынок/Срочный рынок_покупка|продажа фьючерса
        script:
            $analytics.setMessageLabel("Срочный рынок_покупка|продажа фьючерса", "Интенты");
        
        go!: /Срочный рынок_покупка|продажа фьючерса/Ответ_Брокер
        
        state: Ответ_Брокер
            a: Для торговли инструментами срочного рынка нужно пройти тестирование для неквалифицированных инвесторов по категории Производные финансовые инструменты.
            a: Пройти тестирование можно в личном кабинете на сайте фина'м точка ру. Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус. Далее выберите, пройти тестирование.
            a: Для квалифицированных инвесторов доступ предоставляется автоматически.
            a: Выставить заявку на покупку или продажу фьючерса можно через любую торговую систему, с учетом параметров фьючерсного контракта, таких как гарантийное обеспечение, шаг цены и другие.
            a: Ознакомиться со спецификацией фьючерсных контрактов можно на сайте московской биржи.
            a: Обращаем ваше внимание, что Торговая сессия на срочном рынке начинается вечером и длится с 19:05 до 23:50, и продолжается на следующий день — с 10:00 до 14:00 и с 14:05 до 18:50 по московскому времени.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"  
            # final answer
            
    
    
    state: Валюта
        intent!: /027 Валюта
        script:
            $analytics.setMessageLabel("027 Валюта", "Интенты");
        
        a: Какая информация по валютным операциям Вас интересует? Купить или продать валюту. Купить или продать неполный лот валюты. Комиссии за хранение валют.
        
        q: * @currency_buy_sell * ||toState = "/Валюта_покупка|продажа"
        q: * @incomplete_lot * ||toState = "/Валюта_неполный лот"
        q: * @currency_storage * ||toState = "/Валюта_стоимость хранения"
        q: * @choice_1 * ||toState = "/Валюта_покупка|продажа"
        q: * @choice_2 * ||toState = "/Валюта_неполный лот"
        q: * @choice_3 * ||toState = "/Валюта_стоимость хранения"
        q: * @choice_last * ||toState = "/Валюта_стоимость хранения"
        q: @repeat_please * ||toState = "."
        
        
    state: Валюта_покупка|продажа
        intent!: /027 Валюта/Валюта_покупка|продажа
        
        script:
            $analytics.setMessageLabel("Валюта_покупка|продажа", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Валюта_покупка|продажа/Уточнение компании");
            } else {
                $reactions.transition("/Валюта_покупка|продажа/Ответ_" + $session.company.name);
            }
        
        
        state: Уточнение компании
            a: Уточните, вас интересуют операции с валютой, в отделении банка; или на брокерском счёте; или на счёте форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Валюта_покупка|продажа");    


        state: Ответ_Брокер
            a: Валютные пары торгуются на валютной секции московской биржи. Торговля валютными парами также доступна для счетов ИИС.
            a: Чтобы купить или продать валюту, нужно воспользоваться поиском по инструменту в любой торговой системе, и выставить заявку с учетом лотности контракта.
            a: В системе фина'м трейд можно выбрать инструмент из раздела, Валюты. В разделе, Мировые валюты, транслируются индикативные форекс-котировки, торги такими валютными парами недоступны.
            a: Размер одного лота валюты отображается в поле выставления заявки, и в информации по инструменту в торговой системе. Стандартный размер одного лота валюты равен одной тысяче условных единиц, но есть исключения.
            a: Полные лоты валюты доступны в виде контрактов с окончанием ТОДЪ, то есть биржевые расчеты пройдут в текущий рабочий день после 23 часов 50 минут по московскому времени.
            a: Также полные лоты валюты доступны в виде контрактов с окончанием TOM, то есть расчеты пройдут на следующий рабочий день после 23 часов 50 минут.
            a: Неполные лоты валют доступны в виде контрактов с окончанием ТэМэ ЭС, такие контракты торгуются кратно 0,01 единицы валюты.
            a: Минимальная заявка от одной единицы валюты, расчеты на следующий рабочий день после 23 часов 50 минут по московскому времени.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
            
        state: Ответ_Банк  
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
    state: Валюта_покупка|продажа_Брокер
        intent!: /027 Валюта/Валюта_покупка|продажа_Брокер
        go!: /Валюта_покупка|продажа/Ответ_Брокер
        
    state: Валюта_покупка|продажа_Банк
        intent!: /027 Валюта/Валюта_покупка|продажа_Банк
        script:
            $session.operatorPhoneNumber =  '1000';
            $reactions.transition("/Оператор/Оператор по номеру");
        
        
        
    state: Валюта_неполный лот
        intent!: /027 Валюта/Валюта_неполный лот
        
        script:
            $analytics.setMessageLabel("Валюта_неполный лот", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Валюта_неполный лот/Уточнение компании");
            } else {
                $reactions.transition("/Валюта_неполный лот/Ответ_" + $session.company.name);
            }
        
        
        state: Уточнение компании
            a: Уточните, вас интересуют операции с валютой, в банке; на брокерском счёте; или счёте форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Валюта_неполный лот");    


        state: Ответ_Брокер
            a: Валютные пары торгуются на валютной секции московской биржи. Торговля валютными парами также доступна для счетов ИИС. Чтобы купить или продать валюту, нужно воспользоваться поиском по инструменту в любой торговой системе, и выставить заявку с учетом лотности контракта.
            a: Неполные лоты валют доступны в виде контрактов с окончанием ТэМэ ЭС, такие контракты торгуются кратно 0,01 единицы валюты. Минимальная заявка от одной единицы валюты. Приведу пример; чтобы продать 20 центов, следует купить 1 доллар США в виде контракта доллар рубль ТэМэ ЭС; а затем продать 1 доллар 20 центов тем же контрактом.
            a: Расчеты по сделке пройдут на следующий рабочий день после 23 часов 50 минут по московскому времени.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
            
        state: Ответ_Банк  
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        
        
    state: Валюта_стоимость хранения
        intent!: /027 Валюта/Валюта_стоимость хранения
        
        script:
            $analytics.setMessageLabel("Валюта_стоимость хранения", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Валюта_стоимость хранения/Уточнение компании");
            } else {
                $reactions.transition("/Валюта_стоимость хранения/Ответ_" + $session.company.name);
            }
        
        
        state: Уточнение компании
            a: Уточните, вас интересуют операции с валютой, в банке; на брокерском счёте; или счёте форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Валюта_стоимость хранения");    


        state: Ответ_Брокер
            a: Обращаем ваше внимание, в связи с повышенными рисками хранения валюты в российской юрисдикции; с 26 февраля 2024 года, повышены комиссии за хранение более 10000 единиц валюты в долларах США и британских фунтах.
            a: Комиссия за хранение менее 10000 долларов или фунтов по-прежнему не взымается. Таким образом, при свободном остатке валюты от 10 до 100000 единиц, комиссия увеличена с 5 до 10% годовых, а при остатках валюты более 100000 единиц комиссия увеличена с 3 до 6% годовых.
            a: Клиенты Фина'м могут сохранить сниженные ставки за хранение валюты; для этого, нужно подписать соглашение с рисками хранения валюты, по запросу через менеджера поддержки. После подписания согласия, ставки останутся прежними, комиссии за хранение составят;
            a: 5% годовых, если сумма хранения от 10 до 100000 единиц валюты; И 3% годовых, если сумма хранения свыше 100000 единиц валюты.
            a: Комиссия за хранение удерживается в рублях по курсу Банка России на дату списания, расчет осуществляется исходя из количества валюты на счете по состоянию на конец календарного дня. Списание происходит не позднее окончания соответствующего дня.
            a: Комиссия за хранение долларов СэШэА не взимается по счета'м Сегрегированный Global.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        state: Ответ_Форекс
            script:
               $session.operatorPhoneNumber =  $session.company.phoneNumber;
               $reactions.transition("/Оператор/Оператор по номеру");
               # final scenario
            
            
        state: Ответ_Банк  
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
            
            
        
    state: Контакты  
        intent!: /030 Контакты
        script:
            $analytics.setMessageLabel("030 Контакты", "Интенты");
        
        a: Какая информация о компании вас интересует? электронная почта или чат с поддержкой? адреса офисов? реквизиты компании? лицензии компании фина'м.
        
        q: * @pochta_chat_u * ||toState = "/Контакты_Почта"
        q: * @requisites_u * ||toState = "/Контакты_Реквизиты"
        q: * @YL_address_u * ||toState = "/Контакты_Юридический адрес"
        q: * @license_u * ||toState = "/Контакты_Лицензии"
        q: * @choice_1 * ||toState = "/Контакты_Почта"
        q: * @choice_2 * ||toState = "/Контакты_Юридический адрес"
        q: * @choice_3 * ||toState = "/Контакты_Реквизиты"
        q: * @choice_4 * ||toState = "/Контакты_Лицензии"
        q: * @choice_last * ||toState = "/Контакты_Лицензии"
        q: @repeat_please * ||toState = "."
        
        
    state: Контакты_Почта
        intent!: /030 Контакты/Контакты_Почта
        
        script:
            $analytics.setMessageLabel("Контакты_Почта", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Контакты_Почта/Уточнение компании");
            } else {
                $reactions.transition("/Контакты_Почта/Ответ_" + $session.company.name);
            }
        
        
        state: Уточнение компании
            a: Контакты какой компании группы Фина'м, Вас интересуют. Брокер; управляющая компания; Банк; или компания Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Контакты_Почта");    
        
        state: Ответ_Брокер
            a: Служба технической поддержки работает в режиме 24 на 7. Написать сообщение или направить документы в поддержку брокера фина'м можно в чат или на электронную почту.
            a: Адрес электронной почты поддержки доступен на сайте фина'м точка ру, в разделе сайта, о компании, в поле меню контакты и информация.
            a: Чтобы написать в чат с поддержкой, можно воспользоваться чатом в торговой системе фина'м трейд. Либо воспользоваться чатом на сайте фина'м точка ру, для этого в верхнем правом углу нажмите, Ещё.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Банк
            a: Адрес электронной почты, часы работы и прочая контактная информация банка представлены на сайте фина'м точка ру. В разделе сайта Банк, контактная информация.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Форекс
            a: Служба технической поддержки работает в режиме 24 на 7. Адрес электронной почты и прочая контактная информация фина'м форекс представлены на сайте фина'м точка ру. В разделе сайта Форекс, контакты.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_УК
            a: Адрес электронной почты и прочая контактная информация Управляющей компании фина'м, представлены на сайте фина'м точка ру. В разделе сайта Управление активами, о компании.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
    
    state: Контакты_Реквизиты
        intent!: /030 Контакты/Контакты_Реквизиты
        
        script:
            $analytics.setMessageLabel("Контакты_Реквизиты", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Контакты_Реквизиты/Уточнение компании");
            } else {
                $reactions.transition("/Контакты_Реквизиты/Ответ_" + $session.company.name);
            }
        
        
        state: Уточнение компании
            a: Информация о какой компании группы Фина'м, Вас интересует. Брокер; управляющая компания; Банк; или компания Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Контакты_Реквизиты");    
        
        state: Ответ_Брокер
            a: Реквизиты для перевода денежных средств и ценных бумаг на брокерские счета фина'м, можно найти в личном кабинете на сайте, фина'м точка ру, в разделе, детали по счету.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Банк
            a: Реквизиты ваших банковских счетов отображаются как в личном кабинете брокера так и в интернет банке фина'м. Юридические реквизиты Банка фина'м, представлены на сайте фина'м точка ру. В разделе сайта Банк, О Банке.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Форекс
            a: Реквизиты для пополнения фина'м форекс, представлены в личном кабинете на сайте, фина'м форекс. В разделе мои счета, пополнение счета.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_УК
            a: Реквизиты Управляющей компании фина'м, для перевода средств по договорам доверительного управления, можно найти в личном кабинете на сайте, едо'кс фина'м точка ру, в разделе помощь.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
    state: Контакты_Юридический адрес
        intent!: /030 Контакты/Контакты_Юридический адрес
        script:
            $analytics.setMessageLabel("Контакты_Юридический адрес", "Интенты");
        
        go!: /Контакты_Юридический адрес/Ответ_Брокер
        
        state: Ответ_Брокер
            a: Юридический адрес группы компаний фина'м. Москва, почтовый индекс 12 70 06, Настасьинский переулок, дом 7 строение 2. Адреса и режим работы офисов компании представлены на сайте, фина'м точка ру.
            a: В разделе сайта, о компании, контакты. Перед визитом в центральный офис в Москве, на Настасьинском переулке дом 7, строение 2, можно заказать парковочное место, обратившись к менеджеру компании.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            

    state: Контакты_Лицензии
        intent!: /030 Контакты/Контакты_Лицензии
        script:
            $analytics.setMessageLabel("Контакты_Лицензии", "Интенты");
        
        go!: /Контакты_Лицензии/Ответ_Брокер
        
        state: Ответ_Брокер
            a: Перечень лицензий компаний группы фина'м представлен на сайте фина'м точка ру, внизу страницы. А также в разделе сайта, о компании. Для фина'м форекс лицензия находится в разделе, форекс.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
    
    state: Комиссии
        intent!: /007 Комиссии
        script:
            $analytics.setMessageLabel("007 Комиссии", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Комиссии/Уточнение компании");
            } else {
                $reactions.transition("/Комиссии_" + $session.company.name);
            }

        state: Уточнение компании
            a: Уточните, вас интересуют комиссии брокера? Комиссии банка; Управляющей компании; или комиссии Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Комиссии");

    state: Комиссии_Брокер
        intent!: /007 Комиссии/Комиссии_Брокер
        script:
            $analytics.setMessageLabel("Комиссии_Брокер", "Интенты");
            
            if (typeof $parseTree._comission_type != "undefined"){
                $session.comission_type = $parseTree._comission_type;
                $reactions.transition("/Комиссии_Брокер/Комиссия_" + $session.comission_type.name)
            }
            
        a: Размер биржевых, брокерских, депозитарных или комиссий за кредитование, зависит от тарифного плана установленного по счету, рынка ценных бумаг, даты открытия счета.
        a: Ознакомиться с описанием тарифных планов: ФриТрэйд, Стратег, Инвестор, Единый дневной, Единый консультационный, можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
        a: Списание комиссии происходит в 23:59 по московскому времени. Размер комиссии за обслуживание брокерского счёта уменьшается на сумму других уплаченных брокерских комиссий.
        a: Депозитарный тариф зависит от даты открытия счёта и даты последней смены тарифа. То есть по счета'м, открытым или измененным после 26 ноября 2020 года применяется депозитарный Тарифный план номер 2 с бесплатным обслуживанием.
        a: Полные условия всех тарифных планов приведены в Приложении № 7 Регламента брокерского обслуживания Фина'м. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
        a: Про какую комиссию вы хотите узнать подробнее? Про комиссию за обслуживание брокерского счета, или про комиссии за депозитарий.
        q: * @commission_transaction_u * ||toState = "/Комиссии_Брокер/Комиссия_сделки"
        q: * @commission_service_u * ||toState = "/Комиссии_Брокер/Комиссия_обслуживание"
        q: * @commission_depositary_u * ||toState = "/Комиссии_Брокер/Комиссия_депо"
        q: * @choice_1 * ||toState = "/Комиссии_Брокер/Комиссия_обслуживание"
        q: * @choice_2 * ||toState = "/Комиссии_Брокер/Комиссия_депо"
        q: * @choice_last * ||toState = "/Комиссии_Брокер/Комиссия_депо"
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?"
        # final answer

        state: Комиссия_сделки
            a: Размер биржевых, брокерских, депозитарных или комиссий за кредитование, зависит от тарифного плана установленного по счету, рынка ценных бумаг, даты открытия счета.
            a: Ознакомиться с описанием тарифных планов: ФриТрэйд, Стратег, Инвестор, Единый дневной, Единый консультационный, можно на сайте Фина'м точка ру.
            a: Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы. Списание комиссии происходит в 23:59 по московскому времени.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Комиссия_обслуживание
            a: Размер комиссии за обслуживание брокерского счета, зависит от выбранного тарифного плана, и уменьшается на размер брокерской комиссии, удержанной за операции, совершенные в течение месяца.
            a: Списание в последний день месяца. Комиссии за обслуживание самых популярных тарифов. Для счетов новых клиентов Фина'м, в первые 30 дней обслуживания применяется тариф фри трейд, без абонентской платы.
            a: Через 30 дней с момента открытия счета, тариф Фри трейд автоматически меняется на тариф стратег. Обслуживание по тарифному плану, Стратег, ноль рублей. Тарифный план Инвестор, 200 рублей.
            a: Тарифные планы, Дневной, Консультационный и другие, 177 рублей в месяц.
            a: Обращаем ваше внимание, если на момент удержания комиссии за обслуживание, сумма чистых активов на счете менее 2000 рублей, то комиссия взымается в размере 400 рублей в месяц вне зависимости от тарифного плана.
            a: Ознакомиться подробнее можно на сайте Фина'м точка ру. Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы.
            a: Полные условия всех тарифных планов приведены в Приложении № 7 Регламента брокерского обслуживания Фина'м. Чтобы открыть регламент, в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, документы и регламенты.
            a: Хотите узнать подробнее про комиссии депозитария?
            script:
                $context.session = {};
            q: @agree ||toState = "/Комиссии_Брокер/Комиссия_депо"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
                
        state: Комиссия_депо
            a: Депозитарный тариф зависит от даты открытия счёта и даты последней смены тарифа. То есть по счета'м, открытым или измененным после 26 ноября 2020 года применяется депозитарный Тарифный план номер 2, без абонентской платы.
            a: С тарифами на услуги депозитария можно ознакомиться на сайте Фина'м точка ру. В верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Информация, Услуги депозитария.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Комиссия_айпио
            # a: Комиссии IPO
            go!: /IPO_Условия участия 
        
    state: Комиссии_Форекс
        intent!: /007 Комиссии/Комиссии_Форекс
        script:
            $analytics.setMessageLabel("Комиссии_Форекс", "Интенты");
            
        a: При торговле с Фина'м Форекс всегда выгодные спрэды, и отсутствуют комиссии за сделки и обслуживание счёта. Обращаем ваше внимание на условия торговли, такие как спрэд, то есть разница покупки и продажи.
        a: И своп, иными словами форвардные пункты; то есть перенос позиции через ночь, выходные или праздничные дни.
        a: Актуальные условия торговли можно посмотреть на сайте Фина'м точка ру, в разделе сайта Форекс, в поле меню Трейдерам, Торговые условия.
        a: А также информация об актуальном спрэде транслируется в терминале Meta Trader 4, в разделе Обзор рынка.
        a: Чем я могу еще помочь?
        script:
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
    state: Комиссии_Банк
        intent!: /007 Комиссии/Комиссии_Банк
        script:
            $analytics.setMessageLabel("Комиссии_Банк", "Интенты");
            $session.operatorPhoneNumber =  '3888';
            $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario
        
    state: Комиссии_УК
        intent!: /007 Комиссии/Комиссии_УК
        script:
            $analytics.setMessageLabel("Комиссии_УК", "Интенты");
            $session.operatorPhoneNumber =  '1000';
            $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario

    state: EDOX
        intent!: /008 EDOX
        script:
            $analytics.setMessageLabel("008 EDOX", "Интенты");
            
        a: Некоторые услуги и сервисы временно доступны в старой версии личного кабинета с доменом едо'кс. Зайти в новую, или старую версию личного кабинета можно на сайте фина'м точка ру.
        a: В верхнем правом углу сайта нажмите, Личный кабинет, и авторизуйтесь. По умолчанию, логином от личного кабинета является номер телефона в международном формате. Для России, номер начинается с цифры, 7.
        a: Далее выберите раздел кабинета Помощь. Далее слева нажмите кнопку перейти в старый дизайн.
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer

  
   
    state: Обучение
        intent!: /028 Обучение
        script:
            $analytics.setMessageLabel("028 Обучение", "Интенты");
        
        a: Учебный центр фина'м предоставляет видео курсы по торговым системам, услуги онлайн обучения, и услуги очного обучения и встреч для инвесторов. О чем вы хотите узнать подробнее?
        q: * @ITS * ||toState = "/Обучение_ИТС"
        q: * @learning_online * ||toState = "/Обучение_онлайн"
        q: * @learning_offline * ||toState = "/Обучение_офлайн"
        q: * @choice_1 * ||toState = "/Обучение_ИТС"
        q: * @choice_2 * ||toState = "/Обучение_онлайн"
        q: * @choice_3 * ||toState = "/Обучение_офлайн"
        q: * @choice_last * ||toState = "/Обучение_офлайн"
        q: @repeat_please * ||toState = "." 
    
    
    state: Обучение_ИТС
        intent!: /028 Обучение/Обучение_ИТС
        script:
            $analytics.setMessageLabel("Обучение_ИТС", "Интенты");
            
            if ( typeof $parseTree._ITS != "undefined" ){
                $session.ITS = $parseTree._ITS;
            }
            if ( typeof $session.ITS == "undefined" ){
                $reactions.transition("/Обучение_ИТС/Уточнение по ИТС");
            } else {
                $reactions.transition("/Обучение_ИТС/ИТС_" + $session.ITS.name);
            }
        
        state: Уточнение по ИТС
            a: Какая торговая система вас интересует? Фина'м трейд, Транза'к, Квик, сервис транза'к коннектор,  мета трейдер 4, или мета трейдер 5.
            state: Ожидание ответа
                q: * @ITS *
                script:
                    $session.ITS = $parseTree._ITS;
                    $reactions.transition("/Обучение_ИТС");
                
        state: ИТС_Quik
            a: Просмотреть бесплатный видео курс по торговой системе КВИК, можно на портале учебного центра фина'м.
            a: Для этого, на сайте фина'м точка ру, выберите раздел Обучение, и нажмите на название раздела Дистанционное обучение.
            a: После авторизации на портале учебного центра, пролистайте страницу сайта вниз, и выберите видео курс, Как настроить торговый терминал КВИК.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: ИТС_Transaq
            a: Просмотреть бесплатный видео курс по торговой системе Транза'к, можно на портале учебного центра фина'м.
            a: Для этого, на сайте фина'м точка ру, выберите раздел Обучение, и нажмите на название раздела Дистанционное обучение. После авторизации на портале учебного центра выберите раздел База знаний.
            a: Видео курс по торговой системе Транза'к находится внизу страницы.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: ИТС_FT
            a: Просмотреть бесплатный видео курс по торговой системе Фина'м трейд, можно на портале учебного центра фина'м.
            a: Для этого, на сайте фина'м точка ру, выберите раздел Обучение, и нажмите на название раздела Дистанционное обучение.
            a: После авторизации на портале учебного центра, пролистайте страницу сайта вниз, и выберите видео курс, Как начать пользоваться Фина'м трейд, или отдельный подробный курс по Мобильному приложению Фина'м трейд.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: ИТС_TrConnector
            a: Подробную информацию по сервису транза'к коннектор можно посмотреть на сайте фина'м точка ру. Для этого, в верхней части страницы выберите раздел Инвестиции; далее выберите Торговые платформы, Все платформы.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: ИТС_MT5
            script:
                $session.operatorPhoneNumber = '1000'
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: ИТС_MT4
            a: Просмотреть бесплатные видео курсы по торговой системе мета трейдер 4, и по работе на рынке форекс, можно на портале учебного центра фина'м. Для этого, на сайте фина'м точка ру, выберите раздел Обучение.
            a: После авторизации на портале учебного центра; выберите вкладку Курсы; пролистайте страницу сайта вниз, и выберите нужный видео курс. Названия соответствующих видео: Торговый терминал Мета трейдер 4, Возможности торговли в Мета трейдер 4, Первые сделки на форекс, технический анализ рынка форекс.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        
    
    state: Обучение_онлайн
        intent!: /028 Обучение/Обучение_онлайн
        script:
            $analytics.setMessageLabel("Обучение_онлайн", "Интенты");
        a: На портале учебного центра фина'м представлены бесплатные и платные курсы и вебинары, для начинающих и опытных инвесторов. Для начинающих инвесторов доступен бесплатный Online-курс Первые шаги.
        a: Чтобы ознакомиться с курсами и их расписанием, на сайте фина'м точка ру, выберите раздел Обучение, и нажмите на название раздела Дистанционное обучение.
        a: Чтобы открыть расписание видеосеминаров и присоединиться, на сайте фина'м точка ру, выберите раздел Обучение, и в поле меню,  Дистанционное обучение, выберите, вебинары.
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
    state: Обучение_офлайн
        intent!: /028 Обучение/Обучение_офлайн
        script:
            $analytics.setMessageLabel("Обучение_офлайн", "Интенты");
        a: В учебном центре фина'м проводятся очные занятия, курсы и встречи для начинающих и опытных инвесторов.
        a: Чтобы посмотреть расписание и присоединиться, на сайте фина'м точка ру, выберите раздел Обучение, и в поле меню, Очное обучение, выберите, секреты инвестирования.
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer



    state: Overnight
        intent!: /029 Overnight
        script:
            $analytics.setMessageLabel("029 Overnight", "Интенты");
            
        a: Какая информация по займу ценных бумаг брокером, или операции овернайт, вас интересует? Информация по займу ценных бумаг брокером? отключение займа ценных бумаг? информация о сделках РЕПО'.
        q: * @margin_info_u * ||toState = "/Overnight_info"
        q: * @margin_close_u * ||toState = "/Overnight_off"
        q: * @repo_info_u * ||toState = "/Overnight_REPO"
        q: * @choice_1 * ||toState = "/Overnight_info"
        q: * @choice_2 * ||toState = "/Overnight_off"
        q: * @choice_3 * ||toState = "/Overnight_REPO"
        q: * @choice_last * ||toState = "/Overnight_REPO"
        q: @repeat_please * ||toState = "."
        
    
    state: Overnight_info
        intent!: /029 Overnight/Overnight_info
        script:
            $analytics.setMessageLabel("Overnight_info", "Интенты");
            
        a: Согласно пункту 17 точка 12 регламента брокерского обслуживания, брокер имеет право брать бумаги клиентов для внутреннего учета. Это не приводит к потере права совершать действия с ценными бумагами.
        a: Данная операция отображается в справке по счету, в графе, Сделки РЕПО', сделки СВОП, сделки займа ЦБ! За предоставление бумаг для внутреннего учета, вы получаете дополнительное вознаграждение, 0,05% годовых от стоимости ценных бумаг.
        a: Если ценные бумаги находились на внутреннем учете компании в момент дивидендной отсечки, брокер возместит вам сумму дивидендов, увеличенную в 1,15 раза.
        a: Если вы планируете участвовать в собрании акционеров, то за несколько дней до даты фиксации обратитесь к менеджеру, и установите запрет на использование ваших ценных бумаг на период корпоративного события.
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
    
    state: Overnight_off
        intent!: /029 Overnight/Overnight_off
        script:
            $analytics.setMessageLabel("Overnight_off", "Интенты");
            
        a: Согласно пункту 17 точка 12 регламента брокерского обслуживания, брокер имеет право брать бумаги клиентов для внутреннего учета. Это не приводит к потере права совершать действия с ценными бумагами.
        a: Данная операция отображается в справке по счету, в графе, Сделки РЕПО', сделки СВОП, сделки займа ЦБ! За предоставление бумаг для внутреннего учета, вы получаете дополнительное вознаграждение, 0,05% годовых от стоимости ценных бумаг.
        a: Если ценные бумаги находились на внутреннем учете компании в момент дивидендной отсечки, брокер возместит вам сумму дивидендов, увеличенную в 1,15 раза.
        a: Если вы планируете участвовать в собрании акционеров, то за несколько дней до даты фиксации обратитесь к менеджеру, и установите запрет на использование ваших ценных бумаг на период корпоративного события.
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
    state: Overnight_REPO
        intent!: /029 Overnight/Overnight_REPO
        script:
            $analytics.setMessageLabel("Overnight_REPO", "Интенты");
            
        a: Сделки РЕПО', являются сделками переноса ваших необеспеченных позиций.
        a: В брокерском отчете отображаются две сделки: сделка предоставления займа, то есть продажа или покупка ценных бумаг, и сделка возврата займа, то есть сделка обратного откупа, или продажи.
        a: С помощью данных сделок вы получаете возможность взять в займ ценные бумаги у брокера, либо денежные средства под покупку ценных бумаг.
        a: Сделки РЕПО' проводятся брокером автоматически, и фактически в них заложена комиссия по тарифу за займ денежных средств и ценных бумаг.
        a: Обращаем ваше внимание, что с помощью сделок РЕПО', брокер не берет ваши ценные бумаги в займ.
        a: Хотите получить информацию по займу брокером Ценных бумаг?
        q: @agree ||toState = "/Overnight_info"
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?"
        # final answer
        
        
    #Нецензурная брань
    state: Censored
        intent!: /031 Censored
        script:
            $analytics.setMessageLabel("031 Censored", "Интенты");
            $session.operatorPhoneNumber = '1000'
            $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario


   
    state: КВАЛ
        intent!: /023 КВАЛ
        script:
            $analytics.setMessageLabel("023 КВАЛ", "Интенты");
            $session.company = $parseTree._company;
        a: Уточните ваш вопрос, вы хотите узнать: как получить статус квалифицированного инвестора? как перенести статус от другого брокера? как проверить свой инвестиционный статус в фина'м? или как пройти тестирование?
        q: * @kval_get_u * ||toState = "/КВАЛ_документы"
        q: * @kval_perenos_u * ||toState = "/КВАЛ_перенос"
        q: * @kval_sootvetstvie_u * ||toState = "/КВАЛ_соответствие"
        q: * @kval_testirovanie_u * ||toState = "/КВАЛ_тестирование"
        q: * @choice_1 * ||toState = "/КВАЛ_документы"
        q: * @choice_2 * ||toState = "/КВАЛ_перенос"
        q: * @choice_3 * ||toState = "/КВАЛ_соответствие"
        q: * @choice_4 * ||toState = "/КВАЛ_тестирование"
        q: * @choice_last * ||toState = "/КВАЛ_тестирование"
        q: @repeat_please * ||toState = "."
        
    state: КВАЛ_документы
        intent!: /023 КВАЛ/КВАЛ_документы
        script:
            $analytics.setMessageLabel("КВАЛ_документы", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if (  typeof $session.company  == "undefined" ){
                $reactions.transition("/КВАЛ_документы/Уточнение компании");
            } else { 
                $reactions.transition("/КВАЛ_документы/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Уточните, Вас интересует информация по статусу квалифицированного инвестора по счета'м брокера? управляющей компании; или счета'м Форекс.
            q: @repeat_please * ||toState = "."
            q: * @company * ||toState = "/КВАЛ_документы/Уточнение компании/Ожидание ответа"
            
            state: Ожидание ответа
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/КВАЛ_документы");
            
        state: Ответ_Брокер
            a: Есть четыре способа получить статус квалифицированного инвестора в фина'м.
            a: По торговому обороту от шести миллионов рублей; по сумме активов от шести миллионов рублей; по образованию; или по опыту работы. Так же вы можете перенести статус от другого брокера. Какой способ вас интересует?
            q: @repeat_please * ||toState = "."
            
            state: Оборот
                q: * @kval_conditions_Оборот *
                a: Чтобы получить статус квалифицированного инвестора по обороту, за последние четыре квартала, должен быть выполнен торговый оборот на сумму более 6 миллионов рублей, в фина'м или у другого брокера, оборот можно суммировать из разных организаций.
                a: А также должно быть совершено не менее одной сделки в месяц, и не менее 10 сделок в квартал.
                a: Для подтверждения оборота нужно предоставить заверенный брокерский отчет в электронном виде, и договор об открытии счета, содержащий номер брокерского счёта и паспортные данные.
                a: Отправить документы в отдел поддержки можно в чате или на электронную почту.
                a: Проверить свой инвестиционный статус можно в личном кабинете на сайте фина'м точка ру. Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
            
            state: Активы
                q: * @kval_conditions_Активы *
                a: Чтобы получить статус квалифицированного инвестора по сумме активов более шести миллионов, нужно предоставить соответствующие документы в отдел поддержки, в чате или на электронную почту.
                a: Чтобы заявить денежные средства на банковских счетах нужно предоставить выписку с банковского счёта с паспортными данными.
                a: Чтобы заявить денежные средства на брокерских счетах нужно предоставить заверенный брокерский отчет в электронном виде, и договор об открытии счета, содержащий номер брокерского счёта и паспортные данные.
                a: Чтобы заявить активы на счетах, нужно предоставить выписку по счету ДЕПО, либо выписку по лицевому счету в реестре. Все документы должны быть на одну дату, и не старше 5 рабочих дней.
                a: Проверить свой инвестиционный статус можно в личном кабинете на сайте фина'м точка ру. Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
            
            state: Образование
                q: * @kval_conditions_Образование *
                a: Чтобы получить статус квалифицированного инвестора по образованию или квалификации, нужно предоставить соответствующие документы в отдел поддержки, в чате или на электронную почту.
                a: Диплом о высшем экономическом образовании государственного образца выданный организацией, которая на момент выдачи диплома осуществляла аттестацию граждан в сфере профессиональной деятельности на рынке ценных бумаг.
                a: А также можно предоставить свидетельство о квалификации, или международный сертификат.
                a: Проверить свой инвестиционный статус можно в личном кабинете на сайте фина'м точка ру. Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
                
            state: Работа
                q: * @kval_conditions_Работа *
                a: Чтобы получить статус квалифицированного инвестора по опыту работы, нужно предоставить соответствующие документы в отдел поддержки, в чате или на электронную почту.
                a: Рассматривается опыт работы от двух лет, непосредственно связанный с совершением сделок с финансовыми инструментами, или подготовкой индивидуальных инвестиционных рекомендаций.
                a: А также опыт работы от трех лет в должности, при назначении на которую требовалось согласование с Банком России.
                a: Предоставить нужно скан подтверждающих документов, таких как трудовая книжка, трудовой договор с описанием деятельности, или уведомление о согласовании Банком России кандидата на должность.
                a: Проверить свой инвестиционный статус можно в личном кабинете на сайте фина'м точка ру. Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer

            
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
    state: КВАЛ_перенос
        intent!: /023 КВАЛ/КВАЛ_перенос
        script:
            $analytics.setMessageLabel("КВАЛ_перенос", "Интенты");
             
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if (  typeof $session.company  == "undefined" ){
                $reactions.transition("/КВАЛ_перенос/Уточнение компании");
            } else { 
                $reactions.transition("/КВАЛ_перенос/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Уточните, Вас интересует информация по статусу квалифицированного инвестора по счета'м брокера? управляющей компании; или счета'м Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/КВАЛ_перенос");

        state: Ответ_Брокер    
            a: Чтобы перенести статус квалифицированного инвестора от другого брокера, нужно предоставить в отдел поддержки, в чате или на электронную почту, заверенную выписку из реестра квалифицированных инвесторов.
            a: В выписке должны быть указаны ваши паспортные данные, должно быть незаполненное поле Исключен из реестра, а также должно быть указание на совершение Всех видов сделок со Всеми финансовыми инструментами для квалифицированного инвестора.
            a: Срок выписки не старше 30 дней. Проверить свой инвестиционный статус можно в личном кабинете на сайте фина'м точка ру. Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            q: @repeat_please * ||toState = "."
            # final answer
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
    state: КВАЛ_соответствие
        intent!: /023 КВАЛ/КВАЛ_соответствие
        script:
            $analytics.setMessageLabel("КВАЛ_соответствие", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if (  typeof $session.company  == "undefined" ){
                $reactions.transition("/КВАЛ_соответствие/Уточнение компании");
            } else { 
                $reactions.transition("/КВАЛ_соответствие/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Уточните, Вас интересует информация по статусу квалифицированного инвестора по счета'м брокера? управляющей компании; или счета'м Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/КВАЛ_соответствие");
                    
        state: Ответ_Брокер  
            a: Проверить свой инвестиционный статус можно в личном кабинете на сайте фина'м точка ру. Для этого в правом верхнем углу нажмите на значок персоны, далее выберите, Инвестиционный статус. 
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
    state: КВАЛ_тестирование
        intent!: /023 КВАЛ/КВАЛ_тестирование
        a: Прохождение всех тестирований для неквалифицированных инвесторов открывает доступ ко многим категориям инструментов; но не подразумевает присвоение статуса квалифицированного инвестора. Пройти тестирование можно в личном кабинете на сайте фина'м точка ру.
        a: Авторизуйтесь в личном кабинете. В правом верхнем углу нажмите на значок персоны; далее выберите раздел Инвестиционный статус.  Под информацией о вашем инвестиционном статусе, выберите ссылку пройти тестирование.
        a: Доступ к инструментам и сделкам предоставляется сразу после прохождения тестирования; Для этого нужно подписать результат прохождения тестирования в разделе Результаты. Обращаем ваше внимание, в торговой системе КВИК, результаты тестирования принимаются со следующей торговой сессии.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
    
    state: Налоги
        intent!: /025 Налоги
        script:
            $analytics.setMessageLabel("025 Налоги", "Интенты");
            
        a: Уточните ваш вопрос. Вас интересуют, Документы для налоговой? налоговые ставки? предварительный расчет налога? методика расчета налога? или возврат налога.
        q: * @ndfl_documents_for_tax * ||toState = "/Документы для налоговой"
        q: * @ndfl_tax_rates * ||toState = "/Налоговые ставки"
        q: * @ndfl_tax_calculation * ||toState = "/Предварительный расчет"
        q: * @ndfl__tax_calculation_method * ||toState = "/Методика расчета ндфл"
        q: * @ndfl_tax_refund * ||toState = "/Возврат ндфл"
        q: * @ndfl_resident * ||toState = "/Налоговое резидентство"
        q: * @choice_1 * ||toState = "/Документы для налоговой"
        q: * @choice_2 * ||toState = "/Налоговые ставки"
        q: * @choice_3 * ||toState = "/Предварительный расчет"
        q: * @choice_4 * ||toState = "/Методика расчета ндфл"
        q: * @choice_5 * ||toState = "/Возврат ндфл"
        q: * @choice_last * ||toState = "/Возврат ндфл"
        q: @repeat_please * ||toState = "."
        
    state: Документы для налоговой
        intent!: /025 Налоги/Документы для налоговой
        script:
            $analytics.setMessageLabel("Документы для налоговой", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
           
            if (  typeof $session.company  == "undefined" ){
                $reactions.transition("/Документы для налоговой/Уточнение компании");
            } else { 
                $reactions.transition("/Документы для налоговой/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Налоговые документы для какого счёта вас интересуют? Брокерского счёта; Банковского; счёта в Управляющей компании; или счёта Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    
                    $session.company = $parseTree._company;
                    $reactions.transition("/Документы для налоговой");
            
        state: Ответ_Брокер
            a: Для оформления налоговой декларации 3-НДФЛ через налоговый орган, Вам могут потребоваться документы от брокера, которые легко заказать в личном кабинете на сайте, фина'м точка ру, в разделе, документы, Налоги и справки.
            a: Например. Для получения налогового вычета по ИИС по стандартной процедуре можно заказать готовый Пакет документов для налогового вычета, содержащий заверенные документы об открытии ИИС и брокерский отчет.
            a: Дополнительно могут понадобиться: Справка 2 НДФЛ с места работы, и платежное поручение об отправке денежных средств на ИИС.
            a: А также, при необходимости просальдировать налог за счет убытков прошлых лет по обычному брокерскому счету, могут потребоваться такие документы, как 2 НДФЛ и справка об убытках.
            a: Для отчетности по доходам по иностранным ценным бумагам на московской и спб биржах, нужны документы об открытии брокерского счета, и справка по форме 10 42 эс.
            a: Но, если доход был получен через иностранные биржи, дополнительно запросите у менеджера уведомление о присвоении торгового кода, и уведомление о дивидендах налогах и комиссиях.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
        # state: Ответ_undefined
        #     script:
                
        #         $context.session = {};
        #         $reactions.transition("/NoMatch");        
        
    state: Налоговые ставки
        intent!: /025 Налоги/Налоговый ставки
        script:
            $analytics.setMessageLabel("Налоговый ставки", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Налоговые ставки/Уточнение компании");
            } else { 
                $reactions.transition("/Налоговые ставки/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Налоговые ставки на доходы физического лица, для какого счёта вас интересуют? Брокерского счёта; Банковского; счёта в Управляющей компании; или счёта Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Налоговые ставки");
            
        state: Ответ_Брокер
            a: Расчет налога по доходу физических лиц, полученного от инвестиций, производится по следующим ставкам. Для налоговых резидентов российской федерации, налоговая ставка составляет 13% на доход до пяти миллионов рублей включительно.
            a: Если суммарно доходы превышают 5 миллионов рублей, то налоговая ставка 15%. Прогрессивная ставка налога 15% применяется только к той сумме дохода, которая превышает 5 миллионов рублей в отчетном периоде.
            a: Для налоговых нерезидентов российской федерации, ставка НДФЛ составляет 30%.
            a: Хотите узнать подробную информацию о ставках налога при получении купонов и дивидендов?
            script: 
                $context.session = {};
            q: @agree ||toState = "/Налоговые ставки/Уточнение_налоги_купоны_дивиденды"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
        state: Уточнение_налоги_купоны_дивиденды
            a: По купонам, выплаченным и в рублях и в иностранной валюте, брокер удерживает и уплачивает налоги. Отчитываться в налоговую самостоятельно нет необходимости.
            a: Налоговая ставка по купонам для резидентов эРэФ составляет 13%, и 30% для нерезидентов. По дивидендам, выплаченным в рублях, брокер удерживает и уплачивает налоги.
            a: Отчитываться в налоговую самостоятельно нет необходимости. Налоговая ставка по дивидендам для резидентов эРэФ составляет 13%, и 15% для нерезидентов.
            a: Но по дивидендам выплаченным в иностранной валюте до 2023-го года, включительно, брокер не удерживал и не уплачивал налоги. Отчитаться в налоговую службу нужно самостоятельно. С 2024-го года брокер будет самостоятельно удерживать налог с таких доходов.
            a: Ставки налога по дивидендам в иностранной валюте могут отличаться в зависимости от биржи.
            a: Для отчетности по доходам по иностранным ценным бумагам на московской и спб биржах, нужны документы об открытии брокерского счета, и справка по форме 10 42 эс.
            a: Но, если доход был получен через иностранные биржи, дополнительно запросите у менеджера уведомление о присвоении торгового кода, и уведомление о дивидендах налогах и комиссиях.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
        state: Ответ_undefined
            script:
                $context.session = {};
                $reactions.transition("/NoMatch");
        
    state: Предварительный расчет
        intent!: /025 Налоги/Предварительный расчет
        script:
            $analytics.setMessageLabel("Предварительный расчет", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Предварительный расчет/Уточнение компании");
            } else { 
                $reactions.transition("/Предварительный расчет/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Предварительный расчёт налога, для какого счёта вас интересует? Брокерского счёта; Банковского; счёта в Управляющей компании; или счёта Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Предварительный расчет");
            
        state: Ответ_Брокер
            a: Текущий предварительный расчет налога доступен в личном кабинете на сайте фина'м точка ру. Для этого выберите меню, документы, далее выберите раздел, налоги и справки, во вкладке налоги, расчет налога по эмитентам.
            a: Документ будет сформирован в течение нескольких минут. Рекомендуем обновить страницу.
            a: Обращаем ваше внимание, что результаты расчета носят предварительный характер, и могут отличаться от фактического финансового результата для целей налогообложения.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
        # state: Ответ_undefined
        #     script:
        #         $context.session = {};
        #         $reactions.transition("/NoMatch");
        
    state: Методика расчета ндфл
        intent!: /025 Налоги/Методика расчета ндфл
        script:
            $analytics.setMessageLabel("Методика расчета ндфл", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Методика расчета ндфл/Уточнение компании");
            } else { 
                $reactions.transition("/Методика расчета ндфл/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Информация о налогах для какого счёта вас интересует? Брокерского счёта; Банковского; счёта в Управляющей компании; или счёта Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Методика расчета ндфл");
            
        state: Ответ_Брокер
            a: Фина'м является налоговым агентом в отношении инвестиционных доходов, кроме доходов, полученных на валютной секции Московской биржи, и кроме дивидендов и купонов по иностранным ценным бумагам.
            a: Брокер сам рассчитывает и удерживает налог. По итогам года, при выводе денег, либо ценных бумаг со счета, или при расторжении брокерского договора.
            a: Налог рассчитывается отдельно за каждый календарный год. Обращаем ваше внимание, что по счета'м ИИС, нет ежегодной отчетности. Налог рассчитывается и удерживается при расторжении договора ИИС.
            a: Подробнее про методику расчёта НДФЛ можно прочитать в личном кабинете на сайте, едо'кс точка Фина'м точка ру, в разделе сайта, Помощь, Инструкции шаблоны, расчет ндфл.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
                
        # state: Ответ_undefined
        #     script:
        #         $context.session = {};
        #         $reactions.transition("/NoMatch");
        
    state: Возврат ндфл
        intent!: /025 Налоги/Возврат ндфл
        script:
            $analytics.setMessageLabel("Возврат ндфл", "Интенты");
            
            if ( typeof $parseTree._company != "undefined" ){
                $session.company = $parseTree._company;
            }
            if ( typeof $session.company == "undefined" ){
                $reactions.transition("/Возврат ндфл/Уточнение компании");
            } else { 
                $reactions.transition("/Возврат ндфл/Ответ_" + $session.company.name);
            }
                
        state: Уточнение компании
            a: Информация для какого счёта вас интересует? Брокерского счёта; Банковского; счёта в Управляющей компании; или счёта Форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @company *
                script:
                    $session.company = $parseTree._company;
                    $reactions.transition("/Возврат ндфл");
            
        state: Ответ_Брокер
            a: При пересчете актуальной налоговой базы, может возникнуть ситуация излишне удержанного налога. Брокер обязательно уведомит об этом в личном кабинете клиента на сайте Фина'м точка ру.
            a: После получения такого Уведомления об излишне удержанном налоге, можно сформировать Заявление на его возврат, в разделе личного кабинета документы, в меню Налоги и справки.
            a: Подписать заявление можно в течение трёх лет с момента завершения отчетного периода. Средства поступят по указанным в заявлении реквизитам в течение трёх месяцев.
            a: Чтобы вернуть налог за счет убытков прошлых лет, нужно обратиться в налоговую службу. Налоговый кодекс позволяет учитывать убытки, образовавшиеся за предыдущие десять лет.
            a: Для этого нужно заказать у брокера справку об убытках, и справку 2-НДФЛ в личном кабинете, в разделе документы. По необходимости вы можете выбрать получение справки в электронном или бумажном виде.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ответ_Форекс
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
        
        state: Ответ_Банк
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_УК
            script:
                $session.operatorPhoneNumber =  $session.company.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
    
    state: Налоговое резидентство
        intent!: /025 Налоги/Налоговое резидентство
        script:
            $analytics.setMessageLabel("Налоговое резидентство", "Интенты");
        a: Пожалуйста, уточните, вы являетесь гражданином рф?
        q: @agree ||toState = "/Налоговое резидентство/РФ"
        q: @disagree ||toState = "/Налоговое резидентство/НЕ РФ" 
        q: @repeat_please * ||toState = "."
        
        state: РФ
            a: Если вы получили гражданство РФ более 183 дней назад, то вы можете получить статус налогового резидента в брокере фина'м.
            a: Для этого нужно лично посетить офис компании, подписать заявление, и предоставить паспорт с датой прописки более 183 дней.
            a: Если вы хотите стать налоговым не' резидентом РФ в брокере фина'м, то нужно предоставить в электронном виде, в чате с поддержкой или на электронную почту, скан-копии всех страниц загранпаспорта или документ подтверждающий статус налогового резидента в другой стране.
            a: В ответ, менеджер направит вам шаблон заявления; его нужно будет подписать на бумажном носителе и отсканировать.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: НЕ РФ
            a: Для получения статуса налогового резидента РФ у брокера фина'м, нужно лично обратится в офис компании, подписать заявление, предоставить паспорт и дополнительные документы.
            a: Если в вашем паспорте стои'т отметка о пересечении границы, то понадобится миграционная карта. Если отметка о пересечении границы в паспорте отсутствует;
            a: то нужно предоставить: справку с места работы; копию трудовой книжки или трудового договора; табели учета рабочего времени за год.
            a: Для упрощения процедуры, можно предоставить документ о признании налогового резидентства, оформленный самостоятельно в налоговой службе.
            a: Предоставлять документы и подписывать заявление для подтверждения статуса налогового резидента необходимо до расчета налоговой базы;
            a: То есть до момента вывода средств или активов, или до конца календарного года.
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
    state: Санкции_СПБ_биржа
        intent!: /033 Санкции_СПБ биржа
        script:
            $analytics.setMessageLabel("033 Санкции_СПБ биржа", "Интенты");
        a: На СПБ Бирже приостановлены торги Иностранными ценными бумагами. Доступно закрытие позиций по бумагам российских эмитентов. Актуальная информация размещается на официальном сайте СПБ Биржи в разделе, новости.
        a: С 28 ноября СПБ биржа перевела иностранные ценные бумаги на неторговый раздел счета, торги такими активами приостановлены.
        a: После перевода на неторговый раздел, бумаги исключены из торговых лимитов биржи, поэтому не отображаются в терминале, но их наличие отражено в личном кабинете брокера во вкладке Портфель, а также в справке по счету.
        a: Касательно выплат в рамках Указа номер 665, депозитарии используют рубли РФ для выплат по заблокированным активам.
        a: Обращаем ваше внимание, при торговле, на иностранных биржах, через брокера Фина'м, инфраструктура СПБ Биржи не задействована. Вышестоящий брокер партнёр не раскрывает перед американскими биржами гражданство своих клиентов, поэтому риски в данном направлении минимальны.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
    
        
    state: Котировки
        intent!: /034 Котировки
        
        script:
            $session.operatorPhoneNumber = '1000';
            
            if ( typeof $parseTree._list_stocks != "undefined" ){
                $session.list_stocks = $parseTree._list_stocks;
            }
            if ( typeof $session.list_stocks == "undefined" ){
                $reactions.transition("/Котировки/Уточнение актива");
            } else {
                $reactions.transition("/Котировки/Повторное уточнение актива");
            }
        
        state: Уточнение актива
            a: На данный момент я могу подсказать котировки по самым популярным российским акциям. Назовите наименование акции.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @list_stocks *
                script:
                    $session.list_stocks = $parseTree._list_stocks;
                    $reactions.transition("/Котировки");
                    
        state: LocalCatchAll
                event: noMatch
                a: К сожалению, я еще не могу подсказать котировку по данному инструменту.
                script:
                    $session.operatorPhoneNumber =  '1000';
                    $reactions.transition("/Оператор/Оператор по номеру");
                    # final scenario
                    
        state: Повторное уточнение актива
            a: Уточните, пожалуйста, Вы назвали акцию {{$session.list_stocks.name}} ?
            q: * @agree * ||toState = "/Котировки/Отправка запроса"
            q: * @disagree * ||toState = "/Оператор/Оператор по номеру"
            q: @repeat_please * ||toState = "."
        
            
        state: Уточнение АО или АП
            a: Вас интересует информация, по обыкновенным или привилегированным акциям?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @SS_SP *
                script:
                    $session.SS_SP = $parseTree._SS_SP;
                    $reactions.transition($context.session.lastState + "/" + $session.SS_SP.name);    
            
        state: Отправка запроса
            script:
                if ( $session.list_stocks.preferenceOrstandart == "-" ){
                    $context.session.lastState = $context.currentState;
                    $reactions.transition("/Котировки/Уточнение АО или АП");
                } else {
                    $reactions.transition("/Котировки/Отправка запроса" + "/" + $session.list_stocks.preferenceOrstandart);
                }
            
            state: АО
                HttpRequest: 
                    url = https://ftrr01.finam.ru/grpc-json/marketdata/v1/get_quotes
                    method = PUT
                    body = { "securities": { "id": { "security_id": {{$session.list_stocks.securityId}} } } }
                    timeout = 100
                    headers = [{"name":"Authorization","value": "eyJraWQiOiI4Nzg1ZTQxMS05NzFlLTQ0MWQtOTFkYS0zZDgyZWFmNWVlNDMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhcmVhIjoidHQiLCJzY29udGV4dCI6IkNnc0lCeElIZFc1cmJtOTNiZ29vQ0FNU0pHWXlOak00T1RFM0xUazJZVGN0TkdVMlpDMWlOR1kzTFRBMFlqbG1NV1k1WWpCaVl3b0VDQVVTQUFvTENBQVNCM1Z1YTI1dmQyNEtLQWdDRWlRM05tRmxNekpsTkMwMVl6UTRMVFJtT1dJdE9URTVOeTFpTVRVd04yWTNOV1l3WTJRS0JRZ0lFZ0V4Q2dRSUNSSUFDZ1FJQ2hJQUNpZ0lCQklrT0RjNE5XVTBNVEV0T1RjeFpTMDBOREZrTFRreFpHRXRNMlE0TW1WaFpqVmxaVFF6R2d3SXZxbVZyQVlRd0xuOXd3RWlEQWkrMmIzQUJ4REF1ZjNEQVNnQyIsInppcHBlZCI6dHJ1ZSwiY3JlYXRlZCI6IjE3MDMyMzY3OTgiLCJyZW5ld0V4cCI6IjIwMTQzNjMxOTgiLCJzZXNzIjoiSDRzSUFBQUFBQUFBLzVQYXk4akJwTVNneGNiRjRoamtIQUdpQXp6OXZFRjBoSjlqTUlTT0JOUCtJYzVPRUw0N21PL3BHdUVPRlErRTByNGcyc2t4SkJocURzUzhZSGV3ZUlSanNDdUlkbzBJOElYS080UEZnUm9oNXZwQzdJSHpuV0hxZkxUWXVWampnMEtDd1FiNmVrSm9FRjhwbFV2RnhOQWt6ZExjd0VqWE9OVWtXZGZFMURoRjE5SWlPVTAzMVNETlBEbkozRExOM0RoWmlPdkNoQXU3TCt5NHNQZkNCaW0rQzNOQXJJdjlGM1lDeGZZcGlTZm1adVpWSm1ibTVEc2s1eGNWNktWbDVpWG02aFdWT25IazVldm01S2RuNW5Vd01nRUFqUVo1UWlvQkFBQSIsImlzcyI6InR4c2VydmVyIiwia2V5SWQiOiI4Nzg1ZTQxMS05NzFlLTQ0MWQtOTFkYS0zZDgyZWFmNWVlNDMiLCJmaXJlYmFzZSI6IiIsInNlY3JldHMiOiIzZ3NBVm0remwycGlUQktHeFdsYmFRPT0iLCJwcm92aWRlciI6IklOVEVSTkFMIiwic2NvcGUiOiJDQUVRQVEiLCJ0c3RlcCI6ImZhbHNlIiwiZXhwIjoyMDE0Mjc2Nzk4LCJqdGkiOiJmMjYzODkxNy05NmE3LTRlNmQtYjRmNy0wNGI5ZjFmOWIwYmMifQ.aG6-gjjHFzZl14OYitScvBl7eV_sxFjsnL3Tti7YuVoTujhgfYz7ii2N8Wk541ap35U0FF0o1-gRh_cLdoAJ1Q"},{"name":"Content-Type","value":"application\/json"}]
                    vars = [ { "name": "quotes_resault", "value": "$httpResponse" } ]
                    okState = /Котировки/Отправка запроса/Ответ на запрос
                    errorState = /Котировки/Отправка запроса/Нет ответа на запрос
                
            state: АП    
                HttpRequest: 
                    url = https://ftrr01.finam.ru/grpc-json/marketdata/v1/get_quotes
                    method = PUT
                    body = { "securities": { "id": { "security_id": {{$session.list_stocks.securityId_SP}} } } } 
                    timeout = 100
                    headers = [{"name":"Authorization","value": "eyJraWQiOiI4Nzg1ZTQxMS05NzFlLTQ0MWQtOTFkYS0zZDgyZWFmNWVlNDMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhcmVhIjoidHQiLCJzY29udGV4dCI6IkNnc0lCeElIZFc1cmJtOTNiZ29vQ0FNU0pHWXlOak00T1RFM0xUazJZVGN0TkdVMlpDMWlOR1kzTFRBMFlqbG1NV1k1WWpCaVl3b0VDQVVTQUFvTENBQVNCM1Z1YTI1dmQyNEtLQWdDRWlRM05tRmxNekpsTkMwMVl6UTRMVFJtT1dJdE9URTVOeTFpTVRVd04yWTNOV1l3WTJRS0JRZ0lFZ0V4Q2dRSUNSSUFDZ1FJQ2hJQUNpZ0lCQklrT0RjNE5XVTBNVEV0T1RjeFpTMDBOREZrTFRreFpHRXRNMlE0TW1WaFpqVmxaVFF6R2d3SXZxbVZyQVlRd0xuOXd3RWlEQWkrMmIzQUJ4REF1ZjNEQVNnQyIsInppcHBlZCI6dHJ1ZSwiY3JlYXRlZCI6IjE3MDMyMzY3OTgiLCJyZW5ld0V4cCI6IjIwMTQzNjMxOTgiLCJzZXNzIjoiSDRzSUFBQUFBQUFBLzVQYXk4akJwTVNneGNiRjRoamtIQUdpQXp6OXZFRjBoSjlqTUlTT0JOUCtJYzVPRUw0N21PL3BHdUVPRlErRTByNGcyc2t4SkJocURzUzhZSGV3ZUlSanNDdUlkbzBJOElYS080UEZnUm9oNXZwQzdJSHpuV0hxZkxUWXVWampnMEtDd1FiNmVrSm9FRjhwbFV2RnhOQWt6ZExjd0VqWE9OVWtXZGZFMURoRjE5SWlPVTAzMVNETlBEbkozRExOM0RoWmlPdkNoQXU3TCt5NHNQZkNCaW0rQzNOQXJJdjlGM1lDeGZZcGlTZm1adVpWSm1ibTVEc2s1eGNWNktWbDVpWG02aFdWT25IazVldm01S2RuNW5Vd01nRUFqUVo1UWlvQkFBQSIsImlzcyI6InR4c2VydmVyIiwia2V5SWQiOiI4Nzg1ZTQxMS05NzFlLTQ0MWQtOTFkYS0zZDgyZWFmNWVlNDMiLCJmaXJlYmFzZSI6IiIsInNlY3JldHMiOiIzZ3NBVm0remwycGlUQktHeFdsYmFRPT0iLCJwcm92aWRlciI6IklOVEVSTkFMIiwic2NvcGUiOiJDQUVRQVEiLCJ0c3RlcCI6ImZhbHNlIiwiZXhwIjoyMDE0Mjc2Nzk4LCJqdGkiOiJmMjYzODkxNy05NmE3LTRlNmQtYjRmNy0wNGI5ZjFmOWIwYmMifQ.aG6-gjjHFzZl14OYitScvBl7eV_sxFjsnL3Tti7YuVoTujhgfYz7ii2N8Wk541ap35U0FF0o1-gRh_cLdoAJ1Q"},{"name":"Content-Type","value":"application\/json"}]
                    vars = [ { "name": "quotes_resault", "value": "$httpResponse" } ]
                    okState = /Котировки/Отправка запроса/Ответ на запрос
                    errorState = /Котировки/Отправка запроса/Нет ответа на запрос
                
            state: Ответ на запрос
                script:
                    if ($session.quotes_resault.quotes[0].status.message != ""){
                        $reactions.transition("/Котировки/Отправка запроса/Нет информации"); 
                    } else {
                    
                    $session.lastPrice = $session.quotes_resault.quotes[0].quote.last.num / Math.pow(10, $session.quotes_resault.quotes[0].quote.last.scale);
                    $session.lastHigh = $session.quotes_resault.quotes[0].quote.lastHigh.num / Math.pow(10, $session.quotes_resault.quotes[0].quote.lastHigh.scale);
                    $session.lastLow = $session.quotes_resault.quotes[0].quote.lastLow.num / Math.pow(10, $session.quotes_resault.quotes[0].quote.lastLow.scale);
                    $session.lastChangePercent = $session.quotes_resault.quotes[0].quote.lastChangePercent.num / Math.pow(10, $session.quotes_resault.quotes[0].quote.lastChangePercent.scale);
                    $session.lastChangePercent = ($session.lastChangePercent+"").replace("-", "- ");
                    }  
                a: Ожидайте, пожалуйста, проверяю    
                a: Цена последней сделки: {{$session.lastPrice}}, 
                a: максимальная за день: {{$session.lastHigh}}, 
                a: минимальная за день: {{$session.lastLow}}, 
                a: Изменение от цены открытия: {{$session.lastChangePercent}}%
                a: Желаете узнать информацию по другой бумаге?
                script: 
                    $context.session = {};
                q: @list_stocks ||toState = "/Котировки"   
                q: @agree ||toState = "/Котировки"
                q: @disagree ||toState = "/Могу еще чем то помочь?"
                q: @repeat_please * ||toState = "."
                # final answer
                    
            state: Нет информации
                a: Я не смогла уточнить информацию.
                script:
                    $session.operatorPhoneNumber =  '1000';
                    $reactions.transition("/Оператор/Оператор по номеру");
                    # final scenario
                    
            state: Нет ответа на запрос
                a: На данный момент сервис недоступен. 
                script:
                    $session.operatorPhoneNumber =  '1000';
                    $reactions.transition("/Оператор/Оператор по номеру");
                    # final scenario

    state: Доступные биржи
        intent!: /035 Доступные биржи
        
        script:
            $analytics.setMessageLabel("035 Доступные биржи", "Интенты");
            
            holiday($context, $session.countersArray, $context.currentState);
            
            if ( typeof $parseTree._exchanges != "undefined" ){
                $session.exchanges = $parseTree._exchanges;
            }
            if ( typeof $parseTree._section != "undefined" ){
                $session.section = $parseTree._section;
            }            
            if ( typeof $session.exchanges == "undefined" ){
                $reactions.transition("/Доступные биржи/Уточнение биржи");
            } else {
                $reactions.transition("/Доступные биржи/" + $session.exchanges.name);
            }
    
        state: Уточнение биржи
            a: Клиентам фина'м доступны торги на Московской и СПБ Биржах. Американских биржах, Бирже Гонконга, и площадка форекс. Назовите биржу, чтобы узнать подробнее.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @exchanges *
                script:
                    $session.exchanges = $parseTree._exchanges;
                    $reactions.transition("/Доступные биржи");
            
         
        state: Уточнение секции на Московской бирже
            a: Какая секция Московской биржи Вас интересует? Фондовая, срочная, или валютная секция.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @section *
                script:
                    $session.section = $parseTree._section;
                    $reactions.transition("/Доступные биржи/Московская биржа/" + $session.section.name);

                    
        # state: Уточнение секции Американского рынка
        #     a: Уточните, пожалуйста, какая секция Американского рынка Вас интересует Nyse/Nasdaq или CME/CBOE?
        #     q: @repeat_please * ||toState = "."
        #     state: Ожидание ответа
        #         q: * @section *
        #         script:
        #             $session.section = $parseTree._section;
        #             $reactions.transition("/Доступные биржи/Американский рынок/" + $session.section.name);                    
            
         
        state: Московская биржа
            script:
                if ( typeof $session.section == "undefined" ){
                    $reactions.transition("/Доступные биржи/Уточнение секции на Московской бирже");
                } else {
                    $reactions.transition("/Доступные биржи/Московская биржа/" + $session.section.name);
                }

            
            state: Фондовая секция
                a: На Московской бирже предоставляется доступ к Фондовому рынку, где торгуются преимущественно российские ценные бумаги, акции, облигации, фонды, и депозитарные расписки.
                a: Так же клиентам Фина'м доступна внебиржевая торговля заблокированными ценными бумагами.
                a: Если вам интересно узнать подробнее, назовите тему. Время торгов на бирже, или режим расчетов по сделкам.
                script:
                    $context.session = {};
                q: @trading_time ||toState = "/Время торгов/Московская биржа/Фондовая секция"
                q: @calculation_mode ||toState = "/Режим расчетов/Московская биржа"
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?"
                # final answer
            
            state: Срочная секция
                a: Фина'м предоставляет доступ к фьючерсным и опционным контрактам московской биржи. Торги фьючерсами и опционами не имеют отложенных расчетов.
                a: Фактическое начисление вариационной маржи' происходит только в основной клиринг с 18:50 по 19:00 по московскому времени.
                a: Если вам интересно узнать подробнее, назовите тему, время торгов на бирже, или подробнее о торговле на срочном рынке?
                script:
                    $context.session = {};
                q: @trading_time ||toState = "/Время торгов/Московская биржа/Срочная секция"
                q: @forts_details ||toState = "/Срочный рынок"    
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?"
                # final answer
            
            state: Валютная секция
                go!: /Валюта_покупка|продажа/Ответ_Брокер
                # a: Информация по валютной секции ММВБ
                # script:
                #     $context.session = {};
                # q: @repeat_please * ||toState = "."
                # q: @disagree ||toState = "/Могу еще чем то
            
        state: СПБ биржа
            a: На СПБ Бирже приостановлены торги Иностранными ценными бумагами. Доступно закрытие позиций по бумагам российских эмитентов. Актуальная информация размещается на официальном сайте СПБ Биржи в разделе, новости.
            a: С 28 ноября СПБ биржа перевела иностранные ценные бумаги на неторговый раздел счета, торги такими активами приостановлены.
            a: После перевода на неторговый раздел, бумаги исключены из торговых лимитов биржи, поэтому не отображаются в терминале, но их наличие отражено в личном кабинете брокера во вкладке Портфель, а также в справке по счету.
            a: Касательно выплат в рамках Указа номер 665, депозитарии используют рубли РФ для выплат по заблокированным активам.
            a: Обращаем ваше внимание, при торговле, на иностранных биржах, через брокера Фина'м, инфраструктура СПБ Биржи не задействована. Вышестоящий брокер партнёр не раскрывает перед американскими биржами гражданство своих клиентов, поэтому риски в данном направлении минимальны.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Форекс
            a: Фина'м Форекс предоставляет возможность торговли более чем 20 видами валютных пар. При торговле с Фина'м Форекс всегда выгодные спрэды, и отсутствуют комиссии за сделки и обслуживание счёта.
            a: Обращаем ваше внимание на условия торговли, такие как спрэд, то есть разница покупки и продажи. И своп, иными словами форвардные пункты; то есть перенос позиции через ночь, выходные или праздничные дни.
            a: Актуальные условия торговли можно посмотреть на сайте Фина'м точка ру, в разделе сайта Форекс, в поле меню Трейдерам, Торговые условия.
            a: А также информация об актуальном спрэде транслируется в терминале Meta Trader 4, в разделе Обзор рынка.
            a: Хотите узнать подробнее о расписании торговых сессий на площадке форекс?
            script:
                $context.session = {};
            q: @agree ||toState = "/Время торгов/Форекс"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
            
        state: Американский рынок
            a: В рамках американских бирж найс, насд'ак и си би оуи, предоставляется доступ к иностранным акциям и фондам, и опционам.
            a: Торговля на американских биржах доступна квалифицированным инвесторам со счетами типа, Единый счет, счет U S Market Options, счет Сегрегированный Global, и со счетом Иностранные биржи.
            a: Все расчеты производятся в долларах США, автоконвертация валюты при покупке не осуществляется. Торги бумагами на иностранных биржах проходят в режиме Т+2, то есть расчеты проходят через день после заключения сделки.
            a: Хотите узнать подробнее о расписании торговых сессий на американских биржах?
            script:
                $context.session = {};
            q: @agree ||toState = "/Время торгов/Американский рынок"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
            
            # script:
            #     if ( typeof $session.section == "undefined" ){
            #         $reactions.transition("/Доступные биржи/Уточнение секции Американского рынка");
            #     } else {
            #         $reactions.transition("/Доступные биржи/Американский рынок/" + $session.section.name);
            #     }
            
            # state: Nyse_Nasdaq
            #     a: Информация по Nyse/Nasdaq
            #     # script: 
            #     #     $context.session = {};
            #     # q: @repeat_please * ||toState = "."
            #     # q: @disagree ||toState = "/Могу еще чем то

            # state: CME_CBOE
            #     a: Информация по CME/CBOE
            #     # script: 
            #     #     $context.session = {};
            #     # q: @repeat_please * ||toState = "."
            #     # q: @disagree ||toState = "/Могу еще чем то
                
        state: Гонконгская биржа
            a: Торговля на бирже Гонконга доступна со статусом квалифицированного инвестора. Все активы торгуются в гонконгских долларах, автоконвертация валюты при покупке не происходит. Минимальный объем заявки, 8000 гонконгских долларов.
            a: Маржинальная торговля недоступна. Основная торговая сессия проходит с 8:00 до 11:00 по московскому времени. В выходные дни торги не проводятся.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Внебиржевой рынок
            a: Фина'м предоставляет сервис по продаже и покупке на Московской и СПБ Биржах иностранных ценных бумаг, ранее заблокированных европейскими депозитариями Euroclear и Clearstream.
            a: В рамках сервиса заблокированные ИЦБ представляют собой торговый инструмент с тикером, состоящим из оригинального торгового кода бумаги и окончания, SPBZ, либо MM Бэ Зэ.
            a: Торги доступны в дни работы бирж с 11:00 до 17:00 по московскому времени, через торговые системы фина'м трейд и ТРАНЗА'К.
            a: В терминале фина'м трейд список доступных инструментов находится в левом вертикальном меню в разделе Рынки, в подборках Заблокированные инструменты.
            a: Все поручения на сделки являются неторговыми и проводятся исключительно между клиентами Фина'м. Валюта расчетов, рубли РФ. Комиссия за сделку, 0,8%. Сервис не доступен для ИИС.
            a: Для покупки заблокированных бумаг, нужен статус квалифицированного инвестора. Для продажи - статус не требуется.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # state: Европейские биржи (добавить сущность на каждую биржу)
        #     a: Информация по Европейским биржам.
        #     # script: 
        #     #     $context.session = {};
        #     # q: @repeat_please * ||toState = "."
        #     # q: @disagree ||toState = "/Могу еще чем то
        
    
        
    state: Время торгов
        intent!: /036 Время торгов

        script:
            $analytics.setMessageLabel("036 Время торгов", "Интенты");
            
            holiday($context, $session.countersArray, $context.currentState);
            
            if ( typeof $parseTree._exchanges != "undefined" ){
                $session.exchanges = $parseTree._exchanges;
                $session.tempExchanges = $parseTree._exchanges;
            }
            if ( typeof $parseTree._section != "undefined" ){
                $session.section = $parseTree._section;
                $session.tempSection = $parseTree._section;
            }            
            if ( typeof $session.exchanges == "undefined" ){
                $reactions.transition("/Время торгов/Уточнение биржи");
            } else {
                $reactions.transition("/Время торгов/" + $session.exchanges.name);
            }    
    
        state: Уточнение биржи
            a: Расписание торговых сессий на какой бирже Вас интересует? На Московской, СПБ Бирже, на Американских биржах, Бирже Гонконга, или на площадке форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @exchanges *
                script:
                    $session.exchanges = $parseTree._exchanges;
                    $session.tempExchanges = $parseTree._exchanges;
                    $reactions.transition("/Время торгов");
            
         
        state: Уточнение секции на Московской бирже
            a: Какая секция Московской биржи Вас интересует? Фондовая, срочная, или валютная секция.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @section *
                script:
                    $session.section = $parseTree._section;
                    $session.tempSection = $parseTree._section;
                    $reactions.transition("/Время торгов/Московская биржа/" + $session.section.name);

                    
        # state: Уточнение секции Американского рынка
        #     a: Уточните, пожалуйста, какая секция Американского рынка Вас интересует Nyse/Nasdaq или CME/CBOE?
        #     q: @repeat_please * ||toState = "."
        #     state: Ожидание ответа
        #         q: * @section *
        #         script:
        #             $session.section = $parseTree._section;
        #             $reactions.transition("/Время торгов/Американский рынок/" + $session.section.name);                    
            

         
        state: Московская биржа
            script:
                if ( typeof $session.section == "undefined"){
                    $reactions.transition("/Время торгов/Уточнение секции на Московской бирже");
                } else {
                    $reactions.transition("/Время торгов/Московская биржа/" + $session.tempSection.name);
                }
                
            
            state: Фондовая секция
                a: Основная торговая сессия, на фондовом рынке акций и облигаций московской биржи, проходит с 10:00 до 18:40 по московскому времени. Премаркет основной сессии с 9:50 до 10:00, и постмаркет основной сессии с 18:40 до 18:50.
                a: Премаркет вечерней сессии проходит с 19:00 до 19:05, и вечерняя сессия с 19:05 до 23:50 по московскому времени. В выходные дни торги не проводятся.
                a: Чем я могу еще помочь?
                script:
                    # SessionClearing036();
                    $session.exchanges = undefined;
                    $session.section = undefined;
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
            
            state: Срочная секция
                a: Торговая сессия начинается вечером и длится с 19:05 до 23:50, и продолжается на следующий день с 9 до 14:00 и с 14:05 до 18:50 по московскому времени. Клиринг проходит с 14 до 14:05, и с 18:50 до 19:05.
                a: Фактическое начисление вариационной маржи' происходит только в основной клиринг с 18:50 до 19:00 по московскому времени.
                a: Чем я могу еще помочь?
                script:
                    # SessionClearing036();
                    $session.exchanges = undefined;
                    $session.section = undefined;
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
            
            state: Валютная секция
                a: Торги драгоценными металлами в режиме TOM проводятся с 6:50 до 19:00 по московскому времени. Торги валютными парами в режиме SPТ, ТОМ, и ТМС проводятся с 6:50 до 19:00 по московскому времени.
                a: Торги валютными парами в режиме TOD и СВОП проводятся согласно регламенту брокерского обслуживания, приложение 24 точка 1.
                a: То есть торговая сессия для пар в режиме TOD начинается с 6:50, а заканчивается в зависимости от валютной пары.
                a: Торговая сессия валютной па'ры доллар рубль длится до 17:25; па'ры евро рубль и евро доллар торгуются до 14:45; па'ры доллар юань, и юань рубль до 11:50. Пары с белорусским рублём, турецкой лирой, и казахстанским тенге' к рублю эРэФ, торгуются до 11:45; и пара гонконгский доллар рубль, торгуется до 10:25 по московскому времени.
                a: Чем я могу еще помочь?
                script:
                    # SessionClearing036();
                    $session.exchanges = undefined;
                    $session.section = undefined;
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
            
        state: СПБ биржа
            a: На СПБ Бирже Российские ценные бумаги торгуются в основную сессию с 10:00 до 18:50 по московскому времени. В выходные дни торги не проводятся.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Форекс
            a: На внебиржевом валютном рынке форекс почти круглосуточные торги. В зимнее время торговая сессия открывается в понедельник, в 01:06 ночи, и закрывается в субботу в 00:58 по московскому времени.
            a: В летнее время торговая сессия открывается в понедельник, в 00:06 ночи, и закрывается в пятницу, в 23:58 по московскому времени. В будние дни перерыв в торгах с 23:59 до 00:05 по московскому времени.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
            
        state: Американский рынок
            a: В летнее время основная торговая сессия на американских биржах проходит с 16:30 до 23:00 по московскому времени. Опционы торгуются только в основную сессию. Премаркет для фондового рынка акций с одиннадцати ноль ноль до 16:29, и пост маркет с 23:00 до 00:00 по московскому времени.
            a: Во время премаркета рыночные заявки не принимаются. Пост маркет недоступен для сегрегированных счетов. В выходные дни торги не проводятся.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer

            # state: CME_CBOE
            #     a: Информация по CME/CBOE
            #     # script: 
            #     #     $context.session = {};
            #     # q: @repeat_please * ||toState = "."
            #     # q: @disagree ||toState = "/Могу еще чем то 
           
            # state: Nyse_Nasdaq
            #     a: Информация по Nyse/Nasdaq
            #     # script: 
            #     #     $context.session = {};
            #     # q: @repeat_please * ||toState = "."
            #     # q: @disagree ||toState = "/Могу еще чем то 

            
        state: Гонконгская биржа
            a: На гонконгской бирже основная торговая сессия проходит с 8:00 до 11:00 по московскому времени. В выходные дни торги не проводятся.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Внебиржевой рынок
            a: Внебиржевые торги заблокированными ценными бумагами на Московской и СПБ биржах проходят с 11:00 до 17:00 по московскому времени.
            a: В выходные дни торги не проводятся. Основная сессия внебиржевых торгов на Московской бирже с центральным контрагентом проходит с 10:00 до 18:40 по московскому времени.
            a: Вечерняя сессия с 19:05 до 23:50 по московскому времени. В выходные дни торги не проводятся.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # state: Европейские биржи (добавить сущность на каждую биржу)
        #     a: Информация по Европейским биржам.
        #     # script: 
        #     #     $context.session = {};
        #     # q: @repeat_please * ||toState = "."
        #     # q: @disagree ||toState = "/Могу еще чем то
        
        
    state: Режим расчетов
        intent!: /037 Режим расчетов
        
        script:
            $analytics.setMessageLabel("037 Режим расчетов", "Интенты");
            
            holiday($context, $session.countersArray, $context.currentState);
            
            if ( typeof $parseTree._exchanges != "undefined" ){
                $session.exchanges = $parseTree._exchanges;
                $session.tempExchanges = $parseTree._exchanges;
            }
            if ( typeof $session.exchanges == "undefined" ){
                $reactions.transition("/Режим расчетов/Уточнение биржи");
            } else {
                $reactions.transition("/Режим расчетов/" + $session.exchanges.name);
            }    
    
        state: Уточнение биржи
            a: Торги на биржах осуществляются в разных режимах расчетов. Таких как, T+0, то есть расчеты в день сделки. Т+1, то есть расчеты на следующий день.
            a: И Т+2, то есть расчеты через день после сделки. Это значит, что регистрация прав на ценные бумаги или валюту может происходить не в момент заключения сделки, а позднее, в зависимости от биржи и торгуемого актива.
            a: Назовите биржу, чтобы узнать подробнее. Московская биржа. СПБ Биржа. Американские биржи, Гонконг, или площадка форекс.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @exchanges *
                script:
                    $session.exchanges = $parseTree._exchanges;
                    $session.tempExchanges = $parseTree._exchanges;
                    $reactions.transition("/Режим расчетов");
                    # final answer
         
        state: Московская биржа
            a: Торги на Московской бирже акциями, инвестиционными паями, ETF, и облигациями, проводятся в режиме Т+1. А накопленный купонный доход от облигаций считается на дату расчетов по сделке и перечисляется продавцу в тот же день.
            a: Торги валютой на бирже осуществляются в разных режимах, понять режим торгов валютной пары можно по окончанию тикера.
            a: То есть у валютной пары с окончанием TOD, расчеты проходят день в день, с окончанием TOM, или TMS расчеты пройдут на следующий день, и у пары с окончанием SPT расчеты пройдут через день после сделки.
            a: Торги фьючерсами и опционами на срочном рынке не имеют отложенных расчетов. Фактическое начисление вариационной маржи' происходит только в основной клиринг с 18:50 по 19:00 по московскому времени.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
        state: СПБ биржа
            a: Торги на СПБ Бирже российскими и квазироссийскими акциями проводятся в режиме Т+1, то есть расчеты проходят на следующий день после заключения сделки.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Форекс
            a: При торговле на форекс, валюта не поставляется на счет. Расчеты проходят мгновенно по рыночной цене.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Американский рынок
            a: Торги международными ценными бумагами на иностранных биржах проходят в режиме Т+2, то есть расчеты проходят через день после заключения сделки.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
        state: Гонконгская биржа
            a: Торги международными ценными бумагами на иностранных биржах проходят в режиме Т+2, то есть расчеты проходят через день после заключения сделки.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Внебиржевой рынок
            a: Расчеты по внебиржевым торгам заблокированными ценными бумагами проходят в режиме T+2, то есть актив поставляется через день после заключения сделки.
            a: Внебиржевые торги на ММВБ ОТС с ЦК осуществляются в режиме Т+1, то есть расчеты проходят на следующий день после заключения сделки.
            a: Чем я могу еще помочь?
            script:
                $session.exchanges = undefined;
                $session.section = undefined;
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # state: Европейские биржи (добавить сущность на каждую биржу)
        #     a: Информация по Европейским биржам.
        #     # script: 
        #     #     $context.session = {};
        #     # q: @repeat_please * ||toState = "."
        #     # q: @disagree ||toState = "/Могу еще чем то                
        

    state: Демо счет
        intent!: /038 Демо счет
        script:
            $analytics.setMessageLabel("038 Демо счет", "Интенты");
        
        a: Назовите торговую систему, чтобы узнать подробнее о демо счете; Фина'м трейд; квик; транза'к; платформа форекс.
        
        q: * @demo_FT_u * ||toState = "/Демо счет_информация по ИТС/FT"
        q: * @demo_Quik_u * ||toState = "/Демо счет_информация по ИТС/Quik"
        q: * @demo_transaq_u * ||toState = "/Демо счет_информация по ИТС/Transaq"
        q: * @demo_MT4_u * ||toState = "/Демо счет_информация по ИТС/MT4"
        q: * @choice_1 * ||toState = "/Демо счет_информация по ИТС/FT"
        q: * @choice_2 * ||toState = "/Демо счет_информация по ИТС/Quik"
        q: * @choice_3 * ||toState = "/Демо счет_информация по ИТС/Transaq"
        q: * @choice_4 * ||toState = "/Демо счет_информация по ИТС/MT4"
        q: @repeat_please * ||toState = "."
        state: Ожидание ответа
                q: * @ITS *
                script:
                    $session.ITS = $parseTree._ITS;
                    $reactions.transition("/Демо счет_информация по ИТС");
        
    state: Демо счет_информация по ИТС
        intent!: /038 Демо счет/Демо счет_информация по ИТС    

        script:
            $analytics.setMessageLabel("Демо счет_информация по ИТС", "Интенты");
            
            if ( typeof $session.ITS != "undefined" ){
                $session.ITS_demo = $parseTree._ITS;
            }
            if ( typeof $parseTree._ITS_demo != "undefined" ){
                $session.ITS_demo = $parseTree._ITS_demo;
            }
            if ( typeof $session.ITS_demo == "undefined" ){
                $reactions.transition("/Демо счет");
            } else {
                $reactions.transition("/Демо счет_информация по ИТС/" + $session.ITS_demo.name);
            }         
  
            
        state: Quik
            a: Подать заявку на открытие учебного демо счёта квик джуниор, можно на сайте фина'м точка ру. Для этого, в верхней части страницы сайта выберите раздел Инвестиции; далее выберите раздел Торговые платформы; КВИК. Далее нажмите желтую кнопку демо счет.
            a: После подтверждения заявки на вашу электронную почту придет письмо с логином, паролем и ссылка на загрузку торговой системы. На одну электронную почту регистрация возможна один раз в год.
            a: Торги на демо счете доступны в рабочие часы бирж. В выходные и праздничные дни торги не проводятся, и демо счета недоступны для торговли. На Фондовом рынке торги идут с 10 до 18:45.
            a: На срочном рынке, с 09:00 до 22:00 по московскому времени, с перерывами на клиринг.
            a: Если необходимо подключить срочную секцию в учебный терминал квик джуниор, после установки программы и выполнения настроек, обратитесь к менеджеру Фина'м, и сообщите ему логин своего учебного счета.
            a: Логин состоит из цифр и начинается с шести нолей.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer

                    
        state: FT
            a: Открыть демо счет в системе фина'м трейд можно на сайте фина'м точка ру. Кнопка Демо счет находится на странице сайта, справа от большой желтой кнопки открыть счет. Далее нажмите на кнопку Открыть демо-счёт, и заполните форму заявки.
            a: После подтверждения заявки на вашу электронную почту придет письмо с логином, паролем и ссылкой на загрузку торговой системы. На одну электронную почту регистрация возможна один раз в год.
            a: Торги на демо счете доступны в рабочие часы бирж. В выходные и праздничные дни торги не проводятся, и демо счета недоступны для торговли. На Фондовом рынке торги идут с 10 до 18:45.
            a: На срочном рынке, с 09:00 до 22:00 по московскому времени, с перерывами на клиринг.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: Transaq
            a: Открыть демо счет в системе транза'к можно на сайте фина'м точка ру. Кнопка Демо счет находится на странице сайта, справа от большой желтой кнопки открыть счет. Далее нажмите на кнопку Открыть демо-счёт, и заполните форму заявки.
            a: После подтверждения заявки на вашу электронную почту придет письмо с логином, паролем и ссылкой на загрузку торговой системы. На одну электронную почту регистрация возможна один раз в год.
            a: Торги на демо счете доступны в рабочие часы бирж. В выходные и праздничные дни торги не проводятся, и демо счета недоступны для торговли. На Фондовом рынке торги идут с 10 до 18:45.
            a: На срочном рынке, с 09:00 до 22:00 по московскому времени, с перерывами на клиринг.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: MT4
            a: Подать заявку на открытие учебного демо счёта форекс, можно на сайте фина'м точка ру. Для этого, в разделе сайта, форекс, выберите Платформа Meta Trader четыре. Далее выберите демо-счёт, и заполните форму заявки.
            a: После подтверждения заявки на вашу электронную почту придет письмо с логином, паролем и ссылкой на загрузку торговой системы. На одну электронную почту регистрация возможна один раз в год. Период действия дэмо счёта – две недели.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: TrConnector
            a: Подать заявку на открытие учебного демо счёта транза'к конэктор, можно на сайте фина'м точка ру. Для этого, в верхней части страницы выберите раздел Инвестиции; далее выберите Торговые платформы, Все платформы.
            a: Далее выберите сервис транза'к конэктор и заполните заявку на демо счет. После подтверждения заявки на вашу электронную почту придет письмо с логином и паролем. Загрузка дистрибутива не требуется.
            a: Для подключения к стороннему программному обеспечению, достаточно указать логин и пароль от сервера.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
                
        state: MT5
            a: На текущий момент открытие демо-счёта Meta Trader пять, через фина'м невозможно. Вы можете подключить демо-счёт через сайт разработчика.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
    state: Демо счет_ошибка в ИТС
        intent!: /038 Демо счет/Демо счет_ошибка в ИТС
        script:
            $analytics.setMessageLabel("Демо счет_ошибка в ИТС", "Интенты");
        
        go!: /Демо счет_ошибка в ИТС/Ответ
        
        state: Ответ
            a: Демо-счета' помогают ознакомиться с функционалом торговой системы. Торги на демо счете доступны только в рабочие часы бирж. В выходные и праздники торги не проводятся, и демо счета недоступны для торговли.
            a: На Фондовом рынке торги идут с 10 до 18:45. На срочном рынке, с 09:00 до 22:00 по московскому времени, с перерывами на клиринг. Виртуальные средства доступны только для учебной торговли, и недоступны для снятия.
            a: Регистрация демо счёта на одну электронную почту возможна только один раз в год. Если у вас возникла техническая проблема в торговле на демо счете, и перезагрузка торгового терминала не помогла, то обратитесь к менеджеру поддержки.
            script:
                $context.session = {};
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            q: @repeat_please * ||toState = "."
            # final answer
        
        
    
    state: Демо счет_подключение срочного рынка
        intent!: /038 Демо счет/Демо счет_подключение срочного рынка
        script:
            $analytics.setMessageLabel("Демо счет_подключение срочного рынка", "Интенты");
        
        go!: /Демо счет_подключение срочного рынка/Ответ
        
        state: Ответ
            a: Чтобы подключить срочную секцию в учебный терминал квик джуниор, после установки программы и выполнения настроек, обратитесь к менеджеру Фина'м, и сообщите ему логин своего учебного счета. Логин состоит из цифр и начинается с шести нолей.
            a: Чем я могу еще помочь?
            script:
                $context.session = {};
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            q: @repeat_please * ||toState = "."
            # final answer
            

            
    state: 040 Сегрегированные счета
        intent!: /040 Сегрегированные счета
        script:
            $analytics.setMessageLabel("040 Сегрегированные счета", "Интенты");
        
        a: Квалифицированным инвесторам со счетом Сегрегированный Global, доступна торговля Акциями и опционами на американских биржах; хранение активов в зарубежном депозитарии, а также хранение валюты без комиссии.
        a: Какую информацию о сегрегированном счете рассказать подробнее; Открытие счета; пополнение; вывод средств, или налогообложение.
        
        q: * @smma_open_u * ||toState = "/Сегрегированные счета_Открытие счета"
        q: * @SMMA_Replenishment_u * ||toState = "/Сегрегированные счета_Пополнение ДС_ЦБ"
        q: * @SMMA_Withdrawal_u * ||toState = "/Сегрегированные счета_Вывод ДС_ЦБ"
        q: * @SMMA_tax_u * ||toState = "/Сегрегированные счета_Уведомление налоговой"
        q: * @choice_1 * ||toState = "/Сегрегированные счета_Открытие счета"
        q: * @choice_2 * ||toState = "/Сегрегированные счета_Пополнение ДС_ЦБ"
        q: * @choice_3 * ||toState = "/Сегрегированные счета_Вывод ДС_ЦБ"
        q: * @choice_4 * ||toState = "/Сегрегированные счета_Уведомление налоговой"
        q: * @choice_last * ||toState = "/Сегрегированные счета_Уведомление налоговой"
        q: @repeat_please * ||toState = "."
        
    
    state: Сегрегированные счета_Открытие счета
        intent!: /040 Сегрегированные счета/Сегрегированные счета_Открытие счета
        
        script:
            $analytics.setMessageLabel("Сегрегированные счета_Открытие счета", "Интенты");
            $reactions.transition("/Сегрегированные счета_Открытие счета/Ответ_Брокер");
            
        state: Ответ_Брокер
            a: Открытие счёта Сегрегированный Global, с хранением активов за рубежом, доступно только квалифицированным инвесторам, при личном посещении офиса или дистанционно.
            a: Для открытия счёта нужен паспорт РФ, и второй документ на выбор из перечисленных: заграничный паспорт, водительское удостоверение, справка из банка, или счет за коммунальные услуги сроком не старше 6 месяцев, и с указанием ФИО и а'дреса.
            a: Открытие счёта дистанционно, доступно только для действующих клиентов, квалифицированных инвесторов, у которых уже были ранее открыты счета в компании фина'м при личном посещении офиса компании.
            a: Авторизуйтесь в личном кабинете на сайте фина'м точка ру, и выберите открыть счет, далее выберите иностранные рынки, Сегрегированный Global. Счет открывается в течение одного дня.
            a: Торговля доступна с момента пополнения счёта. Так как счет открывается в иностранной компании, то в течение одного месяца с даты открытия счета, нужно уведомить налоговую службу.
            a: Например, через сайт налог точка ру, в разделе Жизненные ситуации, выбрать, информировать о счете в банке расположенном за пределами РФ.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить иллюстрированную инструкцию по открытию счета в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам иллюстрированную [инструкцию по открытию Сегрегированного счета|https://www.finam.ru/dicwords/file/files_chatbot_instrukciysegregopen]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
    
    state: Сегрегированные счета_Пополнение ДС_ЦБ
        intent!: /040 Сегрегированные счета/Сегрегированные счета_Пополнение ДС_ЦБ
        
        script:
            $analytics.setMessageLabel("Сегрегированные счета_Пополнение ДС_ЦБ", "Интенты");
            $reactions.transition("/Сегрегированные счета_Пополнение ДС_ЦБ/Ответ_Брокер");
     

        state: Ответ_Брокер
            a: Валюта счёта Сегрегированный Global - доллар США. Пополнить сегрегированный счет по реквизитам можно как долларами США, так и рублями РФ. При пополнении счёта рублями РФ, конвертация в доллар США, происходит по текущему курсу плюс 4%.
            a: Перед пополнением счёта, а также при необходимости перевода ценных бумаг, рекомендуем проконсультироваться с менеджером поддержки.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить в чат подробную инструкцию, как пополнить сегрегированный счёт?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам подробную иллюстрированную [инструкцию, как пополнить Сегрегированный счёт|https://www.finam.ru/dicwords/file/files_chatbot_instrukciysegreg]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        
    state: Сегрегированные счета_Вывод ДС_ЦБ
        intent!: /040 Сегрегированные счета/Сегрегированные счета_Вывод ДС_ЦБ
        
        script:
            $analytics.setMessageLabel("Сегрегированные счета_Вывод ДС_ЦБ", "Интенты");
            $reactions.transition("/Сегрегированные счета_Вывод ДС_ЦБ/Ответ_Брокер");
        

        state: Ответ_Брокер
            a: Поручение на вывод денежных средств со счёта Сегрегированный Global можно подать через личный кабинет брокера партнера Lime Trading. Вывод доступен от 20 долларов США, евро или от 3000 рублей.
            a: Вывод средств на счета банка фина'м без комиссии, однако, есть комиссия за зачисление долларов и евро на банковский счет в фина'м банке: 3% от суммы операции, но не менее 300 долларов США или евро.
            a: Подробнее о способах вывода средств и комиссиях можно узнать на сайте брокера партнера Lime Trading, или у менеджера поддержки брокера фина'м.
            a: Хотите получить консультацию?
            script: 
                # $context.session = {};
                $session.operatorPhoneNumber = '1000';
            q: @agree ||toState = "/Оператор/Оператор по номеру"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
        
        
        
    state: Сегрегированные счета_Уведомление налоговой
        intent!: /040 Сегрегированные счета/Сегрегированные счета_Уведомление налоговой
        
        script:
            $analytics.setMessageLabel("Сегрегированные счета_Уведомление налоговой", "Интенты");
            $reactions.transition("/Сегрегированные счета_Уведомление налоговой/Ответ_Брокер");

        state: Ответ_Брокер
            a: Так как счет открывается в иностранной компании, то в течение одного месяца с даты открытия счета, нужно уведомить налоговую службу.
            a: Например, через сайт налог точка ру, в разделе Жизненные ситуации, выбрать, информировать о счете в банке расположенном за пределами РФ.
            a: По доходам полученным от торговли в рамках счёта Сегрегированный Global, отчитываться не нужно, Брокер фина'м является налоговым агентом.
            a: Однако, по дивидендам, полученным в иностранной валюте, нужно отчитываться самостоятельно в федеральную налоговую службу.
            a: Чем я могу еще помочь?
            script: 
                # $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
    
    
    state: Опционы
        intent!: /041 Опционы
        
        script:
            $analytics.setMessageLabel("041 Опционы", "Интенты");
            
            if ( typeof $parseTree._derivatives_markets != "undefined" ){
                $session.derivatives_markets = $parseTree._derivatives_markets;
            }
        
        a: Уточните, какая информация вас интересует; доступы к торговле опционами; виды опционов; доска опционов; поставка базового актива.
        
        q: * @access_to_options * ||toState = "/Опционы_Получение доступа"
        q: * @type_options * ||toState = "/Опционы_Виды опционов"
        q: * @options_board * ||toState = "/Опционы_Доска опционов"
        q: * @delivery * ||toState = "/Опционы_Поставка"
        q: * @choice_1 * ||toState = "/Опционы_Получение доступа"
        q: * @choice_2 * ||toState = "/Опционы_Виды опционов"
        q: * @choice_3 * ||toState = "/Опционы_Доска опционов"
        q: * @choice_4 * ||toState = "/Опционы_Поставка"
        q: * @choice_last * ||toState = "/Опционы_Поставка"
        q: @repeat_please * ||toState = "."
        
    
    state: Опционы_Получение доступа
        intent!: /041 Опционы/Опционы_Получение доступа
        
        script:
            $analytics.setMessageLabel("Опционы_Получение доступа", "Интенты");
            
            if ( typeof $parseTree._derivatives_markets != "undefined" ){
                $session.derivatives_markets = $parseTree._derivatives_markets;
            }
            if ( typeof $session.derivatives_markets == "undefined" ){
                $reactions.transition("/Опционы_Получение доступа/Уточнение рынка");
            } else {
                $reactions.transition("/Опционы_Получение доступа/" + $session.derivatives_markets.name);
            } 
        
        
        state: Уточнение рынка
            a: Вас интересуют опционы на московской, или на американской бирже.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @derivatives_markets *
                script:
                    $session.derivatives_markets = $parseTree._derivatives_markets;
                    $reactions.transition("/Опционы_Получение доступа");  

            
        state: FORTS
            a: На Московской бирже представлены расчетные опционы на акции; и поставочные опционы на фьючерсы; статус квалифицированного инвестора для торговли не требуется.
            a: Торговля опционами, доступна с раздельными брокерскими моно счетами, в торговых системах фина'м трейд, транза'к и квик; С единым брокерским счетом, доступны, только опционы на акции, и только в торговой системе Квик.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        

        state: MMA
            script:
                $reactions.transition("/Доступные биржи/Американский рынок");

        
    
    state: Опционы_Виды опционов
        intent!: /041 Опционы/Опционы_Виды опционов
        
        script:
            $analytics.setMessageLabel("Опционы_Виды опционов", "Интенты");
            
            if ( typeof $parseTree._derivatives_markets != "undefined" ){
                $session.derivatives_markets = $parseTree._derivatives_markets;
            }
            if ( typeof $session.derivatives_markets == "undefined" ){
                $reactions.transition("/Опционы_Виды опционов/Уточнение рынка");
            } else {
                $reactions.transition("/Опционы_Виды опционов/" + $session.derivatives_markets.name);
            } 
        
        
        state: Уточнение рынка
            a: Вас интересуют опционы на московской, или на американской бирже.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @derivatives_markets *
                script:
                    $session.derivatives_markets = $parseTree._derivatives_markets;
                    $reactions.transition("/Опционы_Виды опционов");  

            
        state: FORTS
            a: На Московской бирже представлены два типа опционов; премиальные расчетные опционы на акции; и маржи'руемые поставочные опционы на фьючерсы; статус квалифицированного инвестора для торговли не требуется.
            a: Премиальный опцион на акции – немаржи'руемый, и подразумевает единоразовую уплату премии покупателем; а также он расчетный, и не предполагает поставки базового актива, и исполняется биржей автоматически в день исполнения.
            a: При покупке маржи'руемого опциона на фьючерсы, уплата премии распределяется на весь срок жизни контракта, в виде ежедневного перечисления вариационной маржи'.
            a: Опционы на фьючерсы исполняются автоматически в день исполнения контракта, дополнительные заявки не требуются; если нужно исполнить досрочно или отказаться от исполнения опциона, обратитесь в отдел голосового трейдинга.
            a: На договорах с раздельными брокерскими моно счета'ми, доступна торговля обоими видами опционов в торговых системах фина'м трейд, транза'к и квик; На едином брокерском счете, доступна торговля только опционами на акции, и только в торговой системе Квик.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        state: MMA
            a: Фина'м предоставляет доступ к торгам американскими опционами на Чикагской бирже опционов, Chicago Board Options Exchange. Торговля доступна в торговых системах фина'м трейд и транзак ю эс. 
            a: Базовый актив опционов - американские акции. Режим торгов поставочный. При открытии позиции списывается премия в полном объеме; гарантийное обеспечение не блокируется.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer

        
        
    state: Опционы_Доска опционов
        intent!: /041 Опционы/Опционы_Доска опционов
        
        script:
            $analytics.setMessageLabel("Опционы_Доска опционов", "Интенты");
            
            if ( typeof $parseTree._ITS != "undefined" ){
                $session.ITS = $parseTree._ITS;
            }
            if ( typeof $session.ITS == "undefined" ){
                $reactions.transition("/Опционы_Доска опционов/Уточнение ИТС");
            } else {
                $reactions.transition("/Опционы_Доска опционов/" + $session.ITS.name);
            }
        
        
        state: Уточнение ИТС
            a: Назовите, доска опционов в какой торговой системе вас интересует; фина'м трейд, транзак, или квик.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @ITS *
                script:
                    $session.ITS = $parseTree._ITS;
                    $reactions.transition("/Опционы_Доска опционов");    


        state: FT
            a: Чтобы открыть доску опционов в торговой системе фина'м трейд; авторизуйтесь в системе; в левом вертикальном меню выберите раздел Рынки; выберите нужный фьючерс, то есть базовый актив для опциона; после этого справа от кнопки Заявка, будет доступна кнопка Опционы.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        state: Quik
            a: Чтобы открыть доску опционов в торговой системе квик; авторизуйтесь в системе; сверху на панели инструментов нажмите, Создать окно, Все типы окон, Доска опционов.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Transaq
            a: Чтобы открыть доску опционов в торговой системе транзак; авторизуйтесь в системе; сверху на панели инструментов нажмите Таблицы, Финансовые инструменты. Правой кнопкой мыши по таблице инструментов, с помощью поиска, добавьте в таблицу нужный базовый актив опциона – фьючерс. Правой кнопкой мыши по добавленному фьючерсу выберите меню, Доска опционов.
            a: Хотите узнать про доску опционов в транзак Ю эс?
            script: 
                $context.session = {};
            q: @agree ||toState = "/Опционы_Доска опционов/Transaq/Transaq US"    
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
            
            state: Transaq US
                a: Чтобы открыть доску опционов в торговой системе транзак Ю эс; авторизуйтесь в системе; Сверху на панели инструментов нажмите Таблицы, Финансовые инструменты Financial Instruments.
                a: Для нужной бумаги из таблицы Финансовые инструменты выберите Option Chain. Откроется окно Option Families. Выберите нужную серию опционов по дате экспирации.
                a: Откроется доска опционов, где можно выбрать конкретный опцион и выставить заявку.
                a: Чем я могу еще помочь?
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
        
        
    state: Опционы_Поставка
        intent!: /041 Опционы/Опционы_Поставка
        
        script:
            $analytics.setMessageLabel("Опционы_Поставка", "Интенты");
            
            if ( typeof $parseTree._derivatives_markets != "undefined" ){
                $session.derivatives_markets = $parseTree._derivatives_markets;
            }
            if ( typeof $session.derivatives_markets == "undefined" ){
                $reactions.transition("/Опционы_Поставка/Уточнение рынка");
            } else {
                $reactions.transition("/Опционы_Поставка/" + $session.derivatives_markets.name);
            } 
        
        
        state: Уточнение рынка
            a: Вас интересуют опционы на московской, или на американской бирже.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @derivatives_markets *
                script:
                    $session.derivatives_markets = $parseTree._derivatives_markets;
                    $reactions.transition("/Опционы_Поставка");  

            
        state: FORTS
            a: На Московской бирже, опционы на акции - расчетные, они не предполагают поставки базового актива, и исполняются биржей автоматически в день экспирации.
            a: Опционы на фьючерсы, исполняются автоматически в день экспирации; и инвестор получает открытую позицию по фьючерсу. Исполнить опцион досрочно, либо отказаться от его исполнения можно через отдел голосового трейдинга.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        state: MMA
            a: Американский опцион на акции, можно исполнить в любой торговый день через отдел голосового трейдинга.
            a: В последний день обращения, исполнение опциона происходит автоматически, но только в том случае, если опцион в деньгах; то есть его исполнение выгодно покупателю.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
    
    state: Время работы
        intent!: /042 Время работы
        
        script:
            $analytics.setMessageLabel("042 Время работы", "Интенты");
            
            holiday($context, $session.countersArray, $context.currentState);
            
            $reactions.transition("/Время работы/Рабочие дни");

        state: Рабочие дни
            a: Дистанционная поддержка клиентов работает 24 на 7. Часы работы и адреса офисов компании, представлены на сайте, фина'м точка ру. В разделе сайта, о компании, контакты.
            a: Хотите узнать о времени торгов на биржах? Я могу рассказать вам.
            script: 
                $context.session = {};
            q: @agree ||toState = "/Время торгов"
            q: @repeat_please * ||toState = "/Время работы"
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
            
    
    state: Тест 7924
        q!: Тестовый текст
        a: Перевожу вас на оператора. Пожалуйста, оставайтесь на линии.
        script: 
                $session.operatorPhoneNumber =  '7924';
                $reactions.transition("/Оператор/Оператор по номеру");
                
    
    state: Тест Invite
        q!: Тест первый
        a: Перевожу вас на оператора. Пожалуйста, оставайтесь на линии.
        script: 
            $response.replies = $response.replies || [];
            $response.replies.push({
                "type": "switch",
                "sipUri": "1000@10.77.102.30",
                "continueCall": false,
                "continueRecording": false
            });
                
    state: Блокировка карт
        intent!: /018 Блокировка карт
        script: 
                $analytics.setMessageLabel("018 Блокировка карт", "Интенты");
                $session.operatorPhoneNumber =  '3411';
                $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario
                
    state: Юридическое лицо
        intent!: /044 Юридическое лицо
        script: 
                $analytics.setMessageLabel("044 Юридическое лицо", "Интенты");
                $session.operatorPhoneNumber =  '1000';
                $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario    
                
    state: Удаление данных
        intent!: /043 Удаление данных
        script: 
                $analytics.setMessageLabel("043 Удаление данных", "Интенты");
                $session.operatorPhoneNumber =  '1000';
                $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario    
                
    
        
    state: Доходы
        intent!: /045 Доходы
        
        a: Какие доходы Вас интересуют. Дивиденды или купоны?
        
        q: * @dividend * ||toState = "/Доходы_Дивиденды"
        q: * @coupon * ||toState = "/Доходы_Купоны"
        q: * @choice_1 * ||toState = "/Доходы_Дивиденды"
        q: * @choice_2 * ||toState = "/Доходы_Купоны"
        q: * @choice_last * ||toState = "/Доходы_Купоны"
        q: @repeat_please * ||toState = "."
        
    
    state: Доходы_Дивиденды
        intent!: /045 Доходы/Доходы_Дивиденды
        
        a: Вас интересуют. Подробности выплаты дивидендов, дивидендный календарь, или налоговая отчетность?
        
        q: * @dividend_payment_u * ||toState = "/Дивиденды_Выплаты по дивидендам"
        q: * @dividend_calendar_u * ||toState = "/Дивиденды_Дивидендный календарь"
        q: * @dividend_taxes_u * ||toState = "/Дивиденды_Налоги"
        q: * @choice_1 * ||toState = "/Дивиденды_Выплаты по дивидендам"
        q: * @choice_2 * ||toState = "/Дивиденды_Дивидендный календарь"
        q: * @choice_3 * ||toState = "/Дивиденды_Налоги"
        q: * @choice_last * ||toState = "/Дивиденды_Налоги"
        q: @repeat_please * ||toState = "."
        

        
    state: Дивиденды_Выплаты по дивидендам
        intent!: /045 Доходы/Доходы_Дивиденды/Дивиденды_Выплаты по дивидендам
        
        go!: /Дивиденды_Выплаты по дивидендам/Ответ
        
        state: Ответ
            a: Максимальный срок выплаты дивидендов со стороны эмитентов составляет 10 рабочих дней. Со стороны брокера данный процесс занимает еще до 7 рабочих дней. 
            a: На практике, фина'м производит выплаты клиентам в течение одного рабочего дня с момента получения средств от эмитента. Перечисление выплат дивидендов в иностранной валюте может занимать больше времени, так как в переводе средств участвуют банки-корреспонденты.
            a: Выплата дивидендов предусмотрена на тот брокерский счет, где учитывалась данная ценная бумага.  Вы можете оформить или отменить выплату дохода на другой брокерский или банковский счет.
            a: Чтобы выплаты доходов автоматически зачислялись на другой счет, нужно подать заявку в личном кабинете.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        
    state: Дивиденды_Дивидендный календарь
        intent!: /045 Доходы/Доходы_Дивиденды/Дивиденды_Дивидендный календарь
        
        script:
            if ( typeof $parseTree._list_stocks != "undefined" ){
                $session.list_stocks = $parseTree._list_stocks;
                $reactions.transition("/Дивиденды_Дивидендный календарь/Повторное уточнение актива");
            }
            else if ( typeof $session.list_stocks == "undefined" ){
                $reactions.transition("/Дивиденды_Дивидендный календарь/Уточнение актива");
            } else {
                $reactions.transition("/Дивиденды_Дивидендный календарь/Отправка запроса");
            }
        
        state: Уточнение актива
            a: На данный момент я знаю ограниченное число российских акций. Уточните наименование акции, которая вас интересует.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @list_stocks *
                script:
                    $session.list_stocks = $parseTree._list_stocks;
                    $reactions.transition("/Дивиденды_Дивидендный календарь/Повторное уточнение актива");
                    
            state: LocalCatchAll
                event: noMatch
                a: К сожалению, информация по данной ценной бумаге не найдена. Я только учусь и знаю ограниченное число голубых фишек.        
                    
        state: Повторное уточнение актива
            a: Правильно ли я Вас услышала. Вы назвали акцию {{$session.list_stocks.name}} ?
            q: * @agree * ||toState = "/Дивиденды_Дивидендный календарь"
            q: * @disagree * ||toState = "/Дивиденды_Дивидендный календарь/Уточнение актива"
            q: @repeat_please * ||toState = "."

                
                
        state: Уточнение АО или АП
            a: Вас интересует информация, по обыкновенным или привилегированным акциям?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @SS_SP *
                script:
                    $session.SS_SP = $parseTree._SS_SP;
                    # $reactions.answer($context.session.lastState + "/" + $session.SS_SP.name);
                    $reactions.transition($context.session.lastState + "/" + $session.SS_SP.name);
          

        state: Отправка запроса
            script:
                if ( $session.list_stocks.preferenceOrstandart == "-" ){
                    $context.session.lastState = $context.currentState;
                    $reactions.transition("/Дивиденды_Дивидендный календарь/Уточнение АО или АП");
                } else {
                    $reactions.transition("/Дивиденды_Дивидендный календарь/Отправка запроса" + "/" + $session.list_stocks.preferenceOrstandart);
                }
                
            state: АО
                HttpRequest: 
                    url = https://ftrr01.finam.ru/grpc-json/dividends/v1/future_dividends
                    method = PUT
                    body = { "filter": { "securityList": { "id": [ "{{$session.list_stocks.securityId}}" ] } } }
                    timeout = 100
                    headers = [{"name":"Authorization","value": "eyJraWQiOiIxMGEyMTFjMi0wMWE5LTRjZTQtOGJlYi0wYmQ3OWQ1YzMzNjYiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhcmVhIjoidHQiLCJzY29udGV4dCI6IkNnc0lCeElIZFc1cmJtOTNiZ29vQ0FNU0pEazBPV1EwTjJZMUxUazVaamd0TkRnMFpDMWhaamcyTFRkbU1XUTJZMlE0TVRVM1lnb0VDQVVTQUFvTENBQVNCM1Z1YTI1dmQyNEtLQWdDRWlReU5HVm1ORGhqWmkxaE5UZGtMVFE0TXpjdE9UWTJNaTAwWkdReE9HTTVZVEV4TWpZS0JRZ0lFZ0V4Q2dRSUNSSUFDZ1FJQ2hJQUNpZ0lCQklrTVRCaE1qRXhZekl0TURGaE9TMDBZMlUwTFRoaVpXSXRNR0prTnpsa05XTXpNelkyRWdRSUh4SUFHZ3NJaW9LbHJBWVFnTjd6YUNJTENJcXl6Y0FIRUlEZTgyZ29BZyIsInppcHBlZCI6dHJ1ZSwiY3JlYXRlZCI6IjE3MDM0OTM4OTgiLCJyZW5ld0V4cCI6IjIwMTQ2MjAyOTgiLCJzZXNzIjoiSDRzSUFBQUFBQUFBLzFOSzVWSXhzelF6TkxKSU1kZTFURFF5MGpVeFNqTFd0VEJMc2RSTk5ESTNzalEwc2JBMHNVd1Y0cm93NGNMdUN6c3U3TDJ3UVlydndod1E2MkwvaFoxQXNYMUs0b201bVhtVmlaazUrUTdKK1VVRmVtbVplWW01ZWtXbFRoeDUrYm81K2VtWmVSMk1UQURJNW0vT2FnQUFBQSIsImlzcyI6InR4c2VydmVyIiwia2V5SWQiOiIxMGEyMTFjMi0wMWE5LTRjZTQtOGJlYi0wYmQ3OWQ1YzMzNjYiLCJmaXJlYmFzZSI6IiIsInNlY3JldHMiOiIzUXdZQjhoMzZhZkYwRFhZYU01cEZBPT0iLCJwcm92aWRlciI6IklOVEVSTkFMIiwic2NvcGUiOiJDQUVRQVEiLCJ0c3RlcCI6ImZhbHNlIiwiZXhwIjoyMDE0NTMzODk4LCJqdGkiOiI5NDlkNDdmNS05OWY4LTQ4NGQtYWY4Ni03ZjFkNmNkODE1N2IifQ.EOPuGUkPzR0pc8MujtUjmnHBug2ETi2AMKwtfpqA0BaHVk4JVDfZnxtgqZAyKbksDLe2YZeSk45C9pArrKBabA"},{"name":"Content-Type","value":"application\/json"}]
                    vars = [ { "name": "future_dividends_resault", "value": "$httpResponse" } ]
                    okState = /Дивиденды_Дивидендный календарь/Отправка запроса/АО/Ответ на запрос
                    errorState = /Дивиденды_Дивидендный календарь/Отправка запроса/АО/Нет ответа на запрос
                
                state: Ответ на запрос
                    script:
                        if ($session.future_dividends_resault == ""){
                          $reactions.transition("/Дивиденды_Дивидендный календарь/Отправка запроса/АО/Ответ на запрос/Нет информации"); 
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].lastBuyDate.day) < 10){
                            $session.future_dividends_resault[0].item[0].lastBuyDate.day = "0" + $session.future_dividends_resault[0].item[0].lastBuyDate.day;
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].reestrCloseDate.day) < 10){
                            $session.future_dividends_resault[0].item[0].reestrCloseDate.day = "0" + $session.future_dividends_resault[0].item[0].reestrCloseDate.day;
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].lastBuyDate.month) < 10){
                            $session.future_dividends_resault[0].item[0].lastBuyDate.month = "0" + $session.future_dividends_resault[0].item[0].lastBuyDate.month;
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].reestrCloseDate.month) < 10){
                            $session.future_dividends_resault[0].item[0].reestrCloseDate.month = "0" + $session.future_dividends_resault[0].item[0].reestrCloseDate.month;
                        }
                    a: Ближайшая дата дивидендной отсечки по акциям {{$session.list_stocks.name}}: {{$session.future_dividends_resault[0].item[0].reestrCloseDate.day}}.{{$session.future_dividends_resault[0].item[0].reestrCloseDate.month}}.{{$session.future_dividends_resault[0].item[0].reestrCloseDate.year}}, последний день для покупки с дивидендами: {{$session.future_dividends_resault[0].item[0].lastBuyDate.day}}.{{$session.future_dividends_resault[0].item[0].lastBuyDate.month}}.{{$session.future_dividends_resault[0].item[0].lastBuyDate.year}}
                    a: Чем я могу еще помочь?
                    script: 
                        $context.session = {};
                    q: @repeat_please * ||toState = "."
                    q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                    # final answer
                    
                    state: Нет информации
                        a: Информации о датах дивидендной отсечки по акциям {{$session.list_stocks.name}} пока в календаре нет.
                        a: Чем я могу еще помочь?
                        script: 
                            $context.session = {};
                        q: @repeat_please * ||toState = "."
                        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                        # final answer
                    
                state: Нет ответа на запрос
                    a: На данный момент сервис недоступен, рекомендуем обратиться к менеджеру. 
                    a: Чем я могу еще помочь?
                    script: 
                        $context.session = {};
                    q: @repeat_please * ||toState = "."
                    q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                    # final answer
                    
            
            state: АП    
                HttpRequest: 
                    url = https://ftrr01.finam.ru/grpc-json/dividends/v1/future_dividends
                    method = PUT
                    body = { "filter": { "securityList": { "id": [ "{{$session.list_stocks.securityId_SP}}" ] } } }
                    timeout = 100
                    headers = [{"name":"Authorization","value": "eyJraWQiOiIxMGEyMTFjMi0wMWE5LTRjZTQtOGJlYi0wYmQ3OWQ1YzMzNjYiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhcmVhIjoidHQiLCJzY29udGV4dCI6IkNnc0lCeElIZFc1cmJtOTNiZ29vQ0FNU0pEazBPV1EwTjJZMUxUazVaamd0TkRnMFpDMWhaamcyTFRkbU1XUTJZMlE0TVRVM1lnb0VDQVVTQUFvTENBQVNCM1Z1YTI1dmQyNEtLQWdDRWlReU5HVm1ORGhqWmkxaE5UZGtMVFE0TXpjdE9UWTJNaTAwWkdReE9HTTVZVEV4TWpZS0JRZ0lFZ0V4Q2dRSUNSSUFDZ1FJQ2hJQUNpZ0lCQklrTVRCaE1qRXhZekl0TURGaE9TMDBZMlUwTFRoaVpXSXRNR0prTnpsa05XTXpNelkyRWdRSUh4SUFHZ3NJaW9LbHJBWVFnTjd6YUNJTENJcXl6Y0FIRUlEZTgyZ29BZyIsInppcHBlZCI6dHJ1ZSwiY3JlYXRlZCI6IjE3MDM0OTM4OTgiLCJyZW5ld0V4cCI6IjIwMTQ2MjAyOTgiLCJzZXNzIjoiSDRzSUFBQUFBQUFBLzFOSzVWSXhzelF6TkxKSU1kZTFURFF5MGpVeFNqTFd0VEJMc2RSTk5ESTNzalEwc2JBMHNVd1Y0cm93NGNMdUN6c3U3TDJ3UVlydndod1E2MkwvaFoxQXNYMUs0b201bVhtVmlaazUrUTdKK1VVRmVtbVplWW01ZWtXbFRoeDUrYm81K2VtWmVSMk1UQURJNW0vT2FnQUFBQSIsImlzcyI6InR4c2VydmVyIiwia2V5SWQiOiIxMGEyMTFjMi0wMWE5LTRjZTQtOGJlYi0wYmQ3OWQ1YzMzNjYiLCJmaXJlYmFzZSI6IiIsInNlY3JldHMiOiIzUXdZQjhoMzZhZkYwRFhZYU01cEZBPT0iLCJwcm92aWRlciI6IklOVEVSTkFMIiwic2NvcGUiOiJDQUVRQVEiLCJ0c3RlcCI6ImZhbHNlIiwiZXhwIjoyMDE0NTMzODk4LCJqdGkiOiI5NDlkNDdmNS05OWY4LTQ4NGQtYWY4Ni03ZjFkNmNkODE1N2IifQ.EOPuGUkPzR0pc8MujtUjmnHBug2ETi2AMKwtfpqA0BaHVk4JVDfZnxtgqZAyKbksDLe2YZeSk45C9pArrKBabA"},{"name":"Content-Type","value":"application\/json"}]
                    vars = [ { "name": "future_dividends_resault", "value": "$httpResponse" } ]
                    okState = /Дивиденды_Дивидендный календарь/Отправка запроса/АО/Ответ на запрос
                    errorState = /Дивиденды_Дивидендный календарь/Отправка запроса/АО/Нет ответа на запрос
                
                state: Ответ на запрос
                    script:
                        if ($session.future_dividends_resault == ""){
                          $reactions.transition("/Дивиденды_Дивидендный календарь/Отправка запроса/АО/Ответ на запрос/Нет информации"); 
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].lastBuyDate.day) < 10){
                            $session.future_dividends_resault[0].item[0].lastBuyDate.day = "0" + $session.future_dividends_resault[0].item[0].lastBuyDate.day;
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].reestrCloseDate.day) < 10){
                            $session.future_dividends_resault[0].item[0].reestrCloseDate.day = "0" + $session.future_dividends_resault[0].item[0].reestrCloseDate.day;
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].lastBuyDate.month) < 10){
                            $session.future_dividends_resault[0].item[0].lastBuyDate.month = "0" + $session.future_dividends_resault[0].item[0].lastBuyDate.month;
                        }
                        if(parseInt($session.future_dividends_resault[0].item[0].reestrCloseDate.month) < 10){
                            $session.future_dividends_resault[0].item[0].reestrCloseDate.month = "0" + $session.future_dividends_resault[0].item[0].reestrCloseDate.month;
                        }
                    a: Ближайшая дата дивидендной отсечки по акциям {{$session.list_stocks.name}}: {{$session.future_dividends_resault[0].item[0].reestrCloseDate.day}}.{{$session.future_dividends_resault[0].item[0].reestrCloseDate.month}}.{{$session.future_dividends_resault[0].item[0].reestrCloseDate.year}}, последний день для покупки с дивидендами: {{$session.future_dividends_resault[0].item[0].lastBuyDate.day}}.{{$session.future_dividends_resault[0].item[0].lastBuyDate.month}}.{{$session.future_dividends_resault[0].item[0].lastBuyDate.year}}
                    a: Чем я могу еще помочь?
                    script: 
                        $context.session = {};
                    q: @repeat_please * ||toState = "."
                    q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                    # final answer
                    
                    state: Нет информации
                        a: Информации о датах дивидендной отсечки по акциям {{$session.list_stocks.name}} пока в календаре нет.
                        a: Чем я могу еще помочь?
                        script: 
                            $context.session = {};
                        q: @repeat_please * ||toState = "."
                        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                        # final answer
                    
                state: Нет ответа на запрос
                    a: На данный момент сервис недоступен, рекомендуем обратиться к менеджеру. 
                    a: Чем я могу еще помочь?
                    script: 
                        $context.session = {};
                    q: @repeat_please * ||toState = "."
                    q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                    # final answer
                
        
        
    state: Дивиденды_Налоги
        intent!: /045 Доходы/Доходы_Дивиденды/Дивиденды_Налоги
        
        go!: /Налоговые ставки/Уточнение_налоги_купоны_дивиденды
            
            
                
    state: Доходы_Купоны
        intent!: /045 Доходы/Доходы_Купоны
        
        a: Вас интересуют. Даты выплаты купонов, способы получения, или налоговая отчетность?
        
        q: * @coupon_payment_u * ||toState = "/Купоны_Дата выплат по купонам"
        q: * @coupon_method_u * ||toState = "/Купоны_Способы получения"
        q: * @coupon_taxes_u * ||toState = "/Купоны_Налоги"
        q: * @choice_1 * ||toState = "/Купоны_Дата выплат по купонам"
        q: * @choice_2 * ||toState = "/Купоны_Способы получения"
        q: * @choice_3 * ||toState = "/Купоны_Налоги"
        q: * @choice_last * ||toState = "/Купоны_Налоги"
        q: @repeat_please * ||toState = "."
        
        
    state: Купоны_Дата выплат по купонам
        intent!: /045 Доходы/Доходы_Купоны/Купоны_Дата выплат по купонам
        
        go!: /Купоны_Дата выплат по купонам/Ответ
        
        state: Ответ
            a: Максимальный срок выплаты купонов со стороны эмитента составляет 10 рабочих дней. Со стороны брокера данный процесс занимает еще до 7 рабочих дней. 
            a: На практике, фина'м производит выплаты клиентам в течение одного рабочего дня с момента получения средств от эмитента. Перечисление выплат купонов в иностранной валюте может занимать больше времени, так как в переводе средств участвуют банки-корреспонденты.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
    
            
    state: Купоны_Способы получения
        intent!: /045 Доходы/Доходы_Купоны/Купоны_Способы получения
        
        go!: /Купоны_Способы получения/Ответ
        
        state: Ответ
            a: Выплата купонов предусмотрена на тот брокерский счет, где учитывалась данная ценная бумага.  Вы можете оформить или отменить выплату дохода на другой брокерский или банковский счет. 
            a: Чтобы выплаты доходов автоматически зачислялись на другой счет, нужно подать заявку в личном кабинете.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
    state: Купоны_Налоги
        intent!: /045 Доходы/Доходы_Купоны/Купоны_Налоги
        
        go!: /Налоговые ставки/Уточнение_налоги_купоны_дивиденды
    
    state: Заказ денег в кассе
        intent!: /046 Заказ денег в кассе
        script: 
                //$analytics.setMessageLabel("044 Юридическое лицо", "Интенты");
                $session.operatorPhoneNumber =  '1000';
                $reactions.transition("/Оператор/Оператор по номеру");
            # final scenario
    
    
    state: Обезличенные сделки
        intent!: /047 Обезличенные сделки
        
        a: Подключить или отключить поток обезличенных сделок можно в личном кабинете старого дизайна на сайте едо'кс точка фина'м точка ру.
        a: Выберите раздел Торговля; Далее выберите пункт Информационно-торговые системы; разверните вкладку Ещё, выберите Подключение обезличенных сделок. Выполните подключение. 
        a: Через час после подписания заявления активируется поток данных. Обязательно переподключи'тесь к торговому серверу. Обращаем ваше внимание, что для подключения потока обезличенных сделок по классу Индексы, нужно обратиться к менеджеру поддержки.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
    
    
    
    state: Заявки 
        intent!: /049 Заявки
        
        a: Уточните, вы хотите узнать; Как купить или продать актив; Узнать о видах заявок;  Узнать статус или редактировать заявку; или у вас возникает ошибка в торговой системе.
        
        q: * @buy_sell_u * ||toState = "/Заявки_Покупка - Продажа"
        q: * @order_working_u * ||toState = "/Заявки_Редактирование_Снятие_Статус заявки"
        q: * @order_type_u * ||toState = "/Заявки_Виды заявок"
        q: * @order_error_u * ||toState = "/Заявки_Ошибки"
        q: * @choice_1 * ||toState = "/Заявки_Покупка - Продажа"
        q: * @choice_2 * ||toState = "/Заявки_Редактирование_Снятие_Статус заявки"
        q: * @choice_3 * ||toState = "/Заявки_Виды заявок"
        q: * @choice_4 * ||toState = "/Заявки_Ошибки"
        q: * @choice_last * ||toState = "/Заявки_Ошибки"
        q: @repeat_please * ||toState = "."
        
        
    state: Заявки_Покупка - Продажа
        intent!: /049 Заявки/Заявки_Покупка - Продажа
        
        script:
            if ( typeof $parseTree._asset_type != "undefined" ){
                $session.asset_type = $parseTree._asset_type;
            }
            if ( typeof $session.asset_type == "undefined" ){
                $reactions.transition("/Заявки_Покупка - Продажа/Уточнение актива");
            } else {
                $reactions.transition("/Заявки_Покупка - Продажа/"+ $session.asset_type.type);
            }
        
        state: Уточнение актива
            a: Операции с каким видом активов вас интересуют? Например, акции, облигации, фьючерсы, опционы или валюта?
            q: * @choice_1 * ||toState = "/Заявки_Покупка - Продажа/Ценные бумаги"
            q: * @choice_2 * ||toState = "/Заявки_Покупка - Продажа/Ценные бумаги"
            q: * @choice_3 * ||toState = "/Заявки_Покупка - Продажа/Срочный рынок/Фьючерсы"
            q: * @choice_4 * ||toState = "/Заявки_Покупка - Продажа/Срочный рынок/Опционы"
            q: * @choice_last * ||toState = "/Заявки_Покупка - Продажа/Валюта"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @asset_type *
                script:
                    $session.asset_type = $parseTree._asset_type;
                    $reactions.transition("/Заявки_Покупка - Продажа");
                    
            state: LocalCatchAll
                event: noMatch
                a: Возможно, я не так вас поняла. Уточните вид актива, с которым планируете торговые операции.       
    
        
        state: Ценные бумаги
            a: Чтобы открыть позицию в терминале фина'м трейд, можно найти желаемый инструмент через поиск, или уже готовую подборку инструментов, и нажать кнопку заявка. Также открыть позицию можно нажатием на стакан, или нажатием правой кнопки мыши на свечу на графике.
            a: В других торговых системах, открытие позиции доступно из таблицы инструментов, нажатием правой кнопки мыши по графику, или стакану. В любой торговой системе можно закрыть позицию, перейдя в раздел «Портфель».
            a: Например, в терминале фина'м трейд достаточно нажать на строку с нужным активом, а в транза'к и квик, нажать правой кнопкой мыши по позиции в портфеле и выбрать действие. Также закрыть позицию можно с помощью новой заявки, то есть купленные инструменты нужно продать, а проданные в шорт позиции нужно откупить.
            a: Прежде чем открыть или закрыть позицию, убедитесь, что она активна, что вы выбрали верный счет и на нем есть средства, обратите внимание на торговое время на бирже, и проверьте наличие уже выставленных ранее заявок.
            go!: /Заявки_Покупка - Продажа/Ценные бумаги/Общая информация
            # final answer

            
            state: Общая информация
                a: Для покупки иностранных торговых инструментов недружественных стран-эмитентов требуется статус квал инвестора, при необходимости закрыть уже имеющуюся позицию по иностранным активам используйте транза'к, квик или отдел голосового трейдинга.
                a: Выберите и назовите тему, чтобы узнать подробнее; как продать дробный лот акции; или как торговать заблокированными иностранными ценными бумагами.
                script: 
                    $context.session = {};
                q: * @incomplete_lot_u * ||toState = "/Заявки_Покупка - Продажа/Ценные бумаги/Дробный лот"
                q: * @blocking_icb_u * ||toState = "/Заявки_Покупка - Продажа/Ценные бумаги/Заблокированные ЦБ"
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?"
                # final answer
                
            state: Дробный лот
                a: Режим торгов в неполном лоте, доступен в торговых системах квик и транза'к. В системе транза'к, в форме ввода заявки нужно выбрать режим неполные лоты;
                a: В системе Квик, в профиле поиска инструментов, нужно выбрать инструмент с припиской неполный лот. Чтобы продать неполный лот ценных бумаг, также можно обратиться в отдел голосового трейдинга.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
                
            state: Заблокированные ЦБ
                a: Фина'м предоставляет сервис по продаже и покупке на Московской бирже, иностранных ценных бумаг, ранее заблокированных европейскими депозитариями. Торги на СПБ Бирже временно приостановлены.
                a: В рамках сервиса, заблокированные бумаги представляют собой торговый инструмент с уникальным тикером, состоящим из оригинального торгового кода бумаги, и окончания эМ эМ Бэ Зэт. Торги в рабочее время биржи с 11 до 17:00 по москве, через системы транза'к и фина'м трейд.
                a: В терминале фина'м трейд список доступных инструментов находится в левом вертикальном меню в разделе Рынки, в подборках Заблокированные инструменты. Все поручения на сделки являются неторговыми и проводятся исключительно между клиентами Фина'м.
                a: Расчёты в валюте рубль РФ. Комиссия за сделку 0,8%. Недоступно для ИИС. Для покупки заблокированных бумаг нужен статус квалифицированного инвестора, для продажи - статус не требуется.
                a: Перед совершением сделок нужно подписать Согласие на торговые операции с заблокированными бумагами. Для этого, в личном кабинете на сайте едо'кс точка фина'м точка ру, в разделе Услуги, выберите пункт Операции с ценными бумагами, далее выберите Торговые операции с заблокированными ИЦБ.
                a: Узнать подробнее об услуге можно на сайте фина'м точка ру, в разделе сайта, Инвестиции, во вкладке Во что инвестировать, Заблокированные иностранные ценные бумаги.
                a: Чем я могу еще помочь?
                script: 
                    $context.session = {};
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer

            
        state: Срочный рынок
            
            script:
                $reactions.transition("/Заявки_Покупка - Продажа/Срочный рынок/"+ $session.asset_type.name);
            
            state: Фьючерсы
                a: Чтобы открыть позицию в терминале фина'м трейд, можно найти желаемый инструмент через поиск, или уже готовую подборку инструментов, и нажать кнопку заявка. Также открыть позицию можно нажатием на стакан, или нажатием правой кнопки мыши на свечу на графике.
                a: В других торговых системах, открытие позиции доступно из таблицы инструментов, нажатием правой кнопки мыши по графику, или стакану. В любой торговой системе можно закрыть позицию, перейдя в раздел «Портфель».
                a: Например, в терминале фина'м трейд достаточно нажать на строку с нужным активом, а в транза'к и квик, нажать правой кнопкой мыши по позиции в портфеле и выбрать действие.
                a: Также закрыть позицию можно с помощью новой заявки, то есть купленные инструменты нужно продать, а проданные в шорт позиции нужно откупить.
                a: Прежде чем открыть или закрыть позицию, убедитесь, что она активна, что вы выбрали верный счет и на нем есть средства, обратите внимание на торговое время на бирже, и проверьте наличие уже выставленных ранее заявок.
                go!: /Срочный рынок_покупка|продажа фьючерса
                # final answer
                
            state: Опционы   
                a: Удобный вариант для поиска опционов в торговой системе – это доска опционов. При выставлении заявки в торговой системе важно учитывать доступное время торгов на бирже, лотность инструмента, и его доступность на данный момент.
                a: В любой торговой системе можно закрыть позицию, перейдя в раздел «Портфель». Например, в терминале фина'м трейд достаточно нажать на строку с нужным активом, а в транза'к и квик – нажать правой кнопкой мыши по позиции в портфеле и выбрать действие.
                a: Также закрыть позицию можно с помощью новой заявки, то есть купленные инструменты нужно продать, а проданные позиции нужно откупить. Прежде чем открыть или закрыть позицию, убедитесь, что она активна, что вы выбрали верный счет и на нем есть средства, и проверьте наличие уже выставленных заявок.
                go!: /Опционы_Виды опционов
                # final answer
                
        state: Валюта
            a: Чтобы открыть позицию в терминале фина'м трейд, можно найти желаемый инструмент через поиск, или уже готовую подборку инструментов, и нажать кнопку заявка. Также открыть позицию можно нажатием на стакан, или нажатием правой кнопки мыши на свечу на графике.
            a: В других торговых системах, открытие позиции доступно из таблицы инструментов, нажатием правой кнопки мыши по графику, или стакану. В любой торговой системе можно закрыть позицию, перейдя в раздел «Портфель».
            a: Например, в терминале фина'м трейд достаточно нажать на строку с нужным активом, а в транза'к и квик, нажать правой кнопкой мыши по позиции в портфеле и выбрать действие.
            a: Также закрыть позицию можно с помощью новой заявки, то есть купленные инструменты нужно продать, а проданные в шорт позиции нужно откупить.
            a: Прежде чем открыть или закрыть позицию, убедитесь, что она активна, что вы выбрали верный счет и на нем есть средства, обратите внимание на торговое время на бирже, и проверьте наличие уже выставленных ранее заявок.
            go!: /Валюта_покупка|продажа
            # final answer
            
        state: Форекс
            a: Компания Фина'м, не предоставляет доступ к торговле форвардными и CFD контрактами.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
    
    state: Заявки_Редактирование_Снятие_Статус заявки
        intent!: /049 Заявки/Заявки_Редактирование_Снятие_Статус заявки
        
        go!: /Заявки_Редактирование_Снятие_Статус заявки/Ответ_Брокер
        
        state: Ответ_Брокер
            a: Редактировать или снять заявку можно в торговой системе на странице отображения портфеля в разделе Заявки. Необходимо нажать на интересующую заявку и выбрать действие.
            a: Заявки со статусами исполнена, отклонена или снята, не переносятся на следующую торговую сессию, и информация по ним очищается в системе. Статус заявки можно также проверить в торговом терминале.
            a: Условные и стоп заявки до момента их исполнения хранятся на сервере торговой системы. Поэтому ордера, выставленные в одном терминале, не отображаются в другом до тех пор, пока не будут активированы.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
    
    state: Заявки_Ошибки
        intent!: /049 Заявки/Заявки_Ошибки
        
        script:
            if ( typeof $parseTree._ITS_errors != "undefined" ){
                $session.ITS_errors = $parseTree._ITS_errors;
            }
            if ( typeof $session.ITS_errors == "undefined" ){
                $reactions.transition("/Заявки_Ошибки/Уточнение ошибки");
            } else {
                $reactions.transition("/Заявки_Ошибки/" + $session.ITS_errors.name);
            }
        
        state: Уточнение ошибки
            a: Пожалуйста, назовите текст из уведомления об ошибке.
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @ITS_errors *
                script:
                    $session.ITS_errors = $parseTree._ITS_errors;
                    $reactions.transition("/Заявки_Ошибки");
                    
            # state: LocalCatchAll
            #     event: noMatch
            #     script:
            #         $reactions.answer("noMatch");
            #         # $session.operatorPhoneNumber = '1000';
            #         # $reactions.transition("/Оператор/Оператор по номеру");        
            
        state: BAD_CLIENTID
            a: Уведомление с текстом бэд кла'ент айди', или Попытка операции на несуществующий код клиента, может возникать в случае, если счет с данным торговым кодом не зарегистрирован. Регистрация торгового кода возможна до трех рабочих дней с момента отправки заявки.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Error 2 reading file
            a: Уведомление с текстом Error to reading file означает, что при соединении с сервером, программа Квик не может найти файлы с публичной или секретной частью ключей.
            a: Для устранения проблемы, нужно выполнить следующие настройки; открыть меню Система, перейти в настройки; Основные настройки; далее выбрать Основные; Программа; Шифрование; и нажать на кнопку в поле Настройки по умолчанию.
            a: В появившейся форме, Текущие настройки, в полях Файл с публичными ключами, и Файл с секретными ключами, при нажатии на кнопки с многоточием в квадратных скобках, нужно указать местоположение публичного ключа pubring, и секретного ключа secring, соответственно.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: General protection fault
            a: Уведомление с ошибкой General protection fault; Internal exception happened, означает что произошел программный сбой, и программа была завершена аварийно. В большинстве случаев работоспособность программы, можно восстановить путем удаления из директории, где хранится программа Квик, всех файлов с расширением Лог и Дат.
            a: Если вышеприведенные рекомендации не помогут, то это означает, что файлы повреждены, и нужно повторно установить программу. Перед удалением можно сохранить файл с настройками вэ эн дэ, и ключи pubring и secring, в отдельную папку.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: HALT_INSTRUMENT
            a: Уведомление с текстом HALT INSTRUMENT, может возникать в том случае, если по торговому инструменту приостановлены, заблокированы, или прекращены торги. 
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
        state: Вы используете ключи, не зарегистрированные на сервере
            a: Данное сообщение об ошибке может возникать если пользователь пытается установить соединение с сервером Квик с ключами, незарегистрированными на сервере.
            a: После подписания заявления на регистрацию публичной части ключа pubring, необходимо ожидать не менее часа. Регистрация новой пары ключей к одному идентификатору Квик, деактивирует предыдущую пару ключей.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Вы уже работаете в системе
            a: Сервер Квик не допускает одновременную работу двух пользователей с одинаковыми ключами доступа. Для одновременной работы с одной парой ключей можно выбирать подключение к разным серверам.
            a: Если такое сообщение получено при восстановлении соединения после обрыва, то достаточно повторить попытку через несколько секунд, когда сервер Квик прекратит обработку предыдущего соединения.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Дежурный режим
            a: При выставлении Лимитных и Рыночных заявок в неторговое время, возникает сообщение Торговые операции недоступны в дежурном режиме; В этот период, доступно выставление только условных заявок, и стоп-лосса либо тэйк-про'фита. Торги на разных торговых площадках проводятся в разный период времени.
            a: В выходные и праздничные дни, торги не проводятся, либо осуществляются в ограниченном формате. В рамках учебных демо счетов в неторговый период выставление всех типов заявок недоступно. Сервера учебных счетов начинают работать с 10:00 по москве. В выходные и праздничные дни торги не проводятся.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Доступное количество в портфеле 0
            a: При продаже уже рассчитанных и поставленных валютных пар, или металлов, с помощью контрактов TOM или TMS, в портфеле может отображаться количество ноль, при этом выставление заявки будет доступно. Поставленные валюты и металлы отображаются как контракты TOD или TMS.
            a: Проверить доступное количество для продажи можно в портфеле. При выставлении заявки на сумму, не превышающую остаток валюты/металлов в портфеле, короткая позиция не возникнет.
            a: Для продажи валюты, доллар США, евро, или юань, в количестве менее 1000 единиц, то есть менее одного лота валюты, можно использовать инструменты с окончанием TMS.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Запрет трейдера на открытие позиций
            a: Уведомление с текстом, Запрет трейдера на открытие позиций, означает, что открытие позиций в поставочных фьючерсах, в последний день обращения, за день до экспирации и поставки, недоступно. Используйте последующие контракты.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Ключ сервера или пользователя не найден
            a: Данное уведомление возникает, когда пользователь совершает ошибку при наборе своего логина. В поле «Введите Ваше Имя» можно ввести только один первый символ, а не весь логин полностью, учитывайте раскладку клавиатуры (английская или русская).
            a: Например, если логин «Смирнов», то можно ввести только букву «эС»; или, если логин состоит из цифр, например, 2 0 8 1 и так далее, можно ввести только первую цифру «2».
            a: Пароль нужно вводить полностью. Обращаем ваше внимание, что верхний и нижний регистр (большие и маленькие буквы) программой идентифицируются как разные символы.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Недопустимое значение для данного инструмента
            a: Уведомление с текстом, Недопустимое значение для данного инструмента, может возникать в том случае, если в форме заявки указано некорректное значение. Цена заявки должна соответствовать шагу цены по инструменту, информация указана в описании инструмента.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Не найден доступный маршрут
            a: Уведомление с текстом, Не найден доступный маршрут, может возникать случае, если нарушены условия выставления заявки.
            a: Например, на бирже Гонконга нужно соблюдать минимальный объем заявок от 8000 гонконгских долларов.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Маркетные заявки в условных поручениях не разрешены
            a: Уведомление с текстом, Ма'ркетные заявки в условных поручениях не разрешены, может возникать, например, при выставлении заявки на американских биржах;
            a: Для исключения ошибок при срабатывании отложенных заявок, на американских биржах запрещено выставление условных заявок, тэйк про'фита и стоп ло'сса, с исполнением по рынку; рекомендуется указывать цену исполнения вручную.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Для данного инструмента невозможно выставление рыночных заявок
            a: Уведомление с текстом, для данного инструмента невозможно выставление рыночных заявок, может возникать в следующих ситуациях;
            a: По ряду инструментов выставление рыночных заявок ограничено биржей или брокером, временно, либо на постоянной основе; например, такое ограничение возможно для низко ликвидных или высо’ко волатильных инструментов.
            a: А также, во время премаркета или постмаркета на американских биржах, недоступно выставление рыночных заявок. При возникновении данного уведомления, для совершения сделок рекомендуется работать лимитными заявками.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer    
            
            
        state: Не пройдено тестирование
            a: Уведомления с текстом: Не пройдено тестирование, Не подтвержден квалификационный уровень, или Вам запрещены сделки с инструментами, возникают в том случае если у вас отсутствует квалификационный уровень для работы с данным инструментом.
            a: То есть нужно либо получить статус квалифицированного инвестора, либо пройти тестирование для неквалифицированных инвесторов, соответствующее категории инструмента;
            a: Например, для торговли на срочной секции Мосбиржи, потребуется пойти тестирование на тему Производные финансовые инструменты, и Необеспеченные сделки. В то время как для торговли иностранными инструментами недружественных стран-эмитентов, нужен исключительно статус квал инвестора.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
        state: Нет кнопки Заявка (Замок)
            a: По недоступным для торговли инструментам, кнопка Заявка заблокирована; в терминале Фина'м трейд, такие инструменты отмечены символом Замо'к. В торговых системах есть как торговые, так и индикативные инструменты.
            a: Индикативные инструменты, такие как индексы, крипто валюты, или сырье, не торгуются, а несут информационный характер. Также, инструменты могут быть временно ограничены, на период корпоративных событий, или по инициативе вышестоящих организаций.
            a: Фиолетовый символ Замка' в терминале Фина'м трейд говорит о том, что инструмент доступен со статусом квалифицированного инвестора. Также торговля может быть недоступна, по причине запрета торгов по счету; например, первичная активация счета происходит в течение нескольких часов после его пополнения от 150 рублей.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
        state: Не хватило памяти под объекты
            a: Причиной уведомления, не хватило памяти под объекты, является недостаток ресурсов компьютера или программный сбой. Первым делом нужно проверить потребление оперативной памяти и загруженность ЦП в диспетчере задач Windows.
            a: Для очистки временных файлов в системе Квик рекомендуется выполнить следующие действия: Первое, Закрыть программу Квик, если она при этом открыта.
            a: Второе, в директории, где хранится программа Квик, удалить файлы с расширением log и Дат, кроме файлов Дат, в которых хранятся настройки внешних систем технического анализа если такие подключены. И запустить программу Квик.
            a: Если приведенная рекомендация не поможет, то это означает, что файл с настройками, по умолчанию именуемый фина'м точка в н д, поврежден.
            a: В таком случае нужно удалить файл с настройками; запустить программу без файла; и создать настройки заново, либо перезапустить настройки из предыдущего сохранения из папки вэ эн дэ сав.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        

        state: Нехватка средств (Недостаточно обеспечения)
            a: Уведомление с текстом, Нехватка средств, Недостаточно обеспечения, возникает, если средств на счёте недостаточно для выставления заявки. В первую очередь проверьте свободный денежный остаток в портфеле, а также убедитесь в отсутствии лишних активных заявок по каким-либо инструментам.
            a: Под активные ордера блокируется обеспечение, что может помешать выставлению новой заявки. Обеспечение для открытия позиций с займом, или плечом, рассчитывается как разница оценки портфеля и заблокированной начальной маржи' по счету.
            a: При выставлении рыночной заявки, требуется больше обеспечения по сравнению с лимитной; то есть для фьючерсов при выставлении рыночной заявки, блокируется в 1,5 раза больше гарантийного обеспечения. Рекомендуется работать лимитными заявками.
            a: А также, по счета'м типа Единая денежная позиция, со стандартным уровнем риска КСУР, на срочном рынке может блокироваться гарантийное обеспечение в 1,5 - 2 раза выше биржевого.
            a: Хотите узнать подробнее о способах снизить гарантийное обеспечение?
            script: 
                $context.session = {};
            q: @agree ||toState = "/Срочный рынок_уменьшение гарантийного обеспечения"
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
            
        state: Сделки по данному инструменту запрещены
            a: Уведомление с текстом: Сделки по данному инструменту запрещены, или Данная ценная бумага не допущена к заключению сделок, может возникать в случае если заявка выставляется со счета нерезидента российской федерации. Торговля резидентам недружественных стран недоступна. Данная проверка производится со стороны биржи.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Цена сделки вне лимита
            a: По каждому инструменту существует свой диапазон выставления заявок. По инструментам срочного рынка, в спецификации контракта на бирже, указываются значения верхнего и нижнего лимита на момент последнего клиринга.
            a: По ценным бумагам данный диапазон составляет от 5 до 15% от текущей стоимости инструмента. Биржа может ограничивать диапазон выставления заявок на свое усмотрение в случае резкого изменения цены инструмента.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        # .......    
        # state: Не обновляется программа QUIK
        #     a: Заглушка. Информация по ошибке Не обновляется программа QUIK
        #     script: 
        #         $context.session = {};
        #     q: @repeat_please * ||toState = "."
        #     q: @disagree ||toState = "/Могу еще чем то
            
        # state: Не обновляются данные на графиках/в таблицах
        #     a: Заглушка. Информация по ошибке Не обновляются данные на графиках/в таблицах
        #     script: 
        #         $context.session = {};
        #     q: @repeat_please * ||toState = "."
        #     q: @disagree ||toState = "/Могу еще чем то
        
        # state: Нет данных в таблицах Сделки/Заявки
        #     a: Заглушка. Информация по ошибке Нет данных в таблицах Сделки/Заявки
        #     script: 
        #         $context.session = {};
        #     q: @repeat_please * ||toState = "."
        #     q: @disagree ||toState = "/Могу еще чем то
        
        # state: Нет торгового кода в заявке
        #     a: Заглушка. Информация по ошибке Нет торгового кода в заявке
        #     script: 
        #         $context.session = {};
        #     q: @repeat_please * ||toState = "."
        #     q: @disagree ||toState = "/Могу еще чем то
        
        # state: Вам запрещены сделки с инструментами
        #     a: Заглушка. Информация по ошибке Вам запрещены сделки с инструментами
        #     script: 
        #         $context.session = {};
        #     q: @repeat_please * ||toState = "."
        #     q: @disagree ||toState = "/Могу еще чем то
        
        # state: Не подтвержден квалификационный уровень
        #     a: Заглушка. Информация по ошибке Не подтвержден квалификационный уровень
        #     script: 
        #         $context.session = {};
        #     q: @repeat_please * ||toState = "."
        #     q: @disagree ||toState = "/Могу еще чем то
            
    
    state: Заявки_Виды заявок
        intent!: /049 Заявки/Заявки_Виды заявок
        
        script:
            if ( typeof $parseTree._order_type != "undefined" ){
                $session.order_type = $parseTree._order_type;
            }
            if ( typeof $session.order_type == "undefined" ){
                $reactions.transition("/Заявки_Виды заявок/Уточнение типа заявки");
            } else {
                $reactions.transition("/Заявки_Виды заявок/" + $session.order_type.name);
            }
            
        state: Уточнение типа заявки
            a: Назовите, какой вид заявок вас интересует? Например, рыночная; условная; лимитная; стоп лосс, тэйк про'фит, или связанные заявки.
            q: * @choice_1 * ||toState = "/Заявки_Виды заявок/Рыночная"
            q: * @choice_2 * ||toState = "/Заявки_Виды заявок/Условная"
            q: * @choice_3 * ||toState = "/Заявки_Виды заявок/Лимитная"
            q: * @choice_4 * ||toState = "/Заявки_Виды заявок/Стоп-лосс"
            q: * @choice_last * ||toState = "/Заявки_Виды заявок/Связанные"
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @order_type *
                script:
                    $session.order_type = $parseTree._order_type;
                    $reactions.transition("/Заявки_Виды заявок");    
        
        state: Лимитная
            a: Лимитная заявка исполняется по принципу лучшего исполнения. Для покупки, исполнение происходит по цене не выше указанной, то есть по значению меньше или равно; для продажи, исполнение происходит по цене не ниже указанной цены, то есть по значению больше или равно.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Условная
            a: В торговых системах существуют несколько условий для исполнения заявок. Условие, время исполнения, означает, что заявка будет активирована и отправлена на биржу в указанное время.
            a: Условие, Сделка выше или равна, считается выполненным, если сервер получил данные о появлении на рынке хотя бы одной сделки по цене выше или равно заданной в условии, при выполнении условия, заявка будет выставлена на биржу по цене заданной в поле цена исполнения.
            a: Условие, Сделка ниже или равна, считается выполненным, если сервер получит данные о появлении на рынке хотя бы одной сделки по цене ниже или равно заданной в условии, при выполнении условия заявка будет выставлена на биржу по цене заданной в поле цена исполнения.
            a: Условие, Срок действия заявки, можно выбрать, до отмены, до конца дня, до указанной даты.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Рыночная
            a: Рыночная заявка исполняется по принципу лучшего исполнения; то есть при покупке автоматически будет выбрана наименьшая доступная цена, а при продаже наибольшая доступная цена. Обращаем ваше внимание на ряд важных моментов;
            a: При выставлении рыночной заявки, требуется больше обеспечения по сравнению с лимитной; то есть для фьючерсов при выставлении рыночной заявки, блокируется в 1,5 раза больше гарантийного обеспечения.
            a: Для некоторых инструментов рыночные заявки недоступны. Во время премаркета и пост маркета на американских биржах, недоступно выставление рыночных заявок; рекомендуем работать лимитными.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Стоп-лосс
            a: Стоп-заявка предполагает, что инвестор заранее выбирает условия, при которых заявка активируется, и выставится либо лимитная либо рыночная заявка. При выставлении Стоп лосс нужно задать Цену активации и Цену исполнения.
            a: При активации, на биржу будет выставлена заявка по цене, заданной в поле Цена исполнения. Заявка может быть выставлена со сроком действия, до отмены, до конца дня, до указанной даты.
            a: Для закрытия коротких позиций следует выставлять стоп заявки на покупку, для закрытия длинных позиций, на продажу. Стоп лосс на продажу, активируется, когда цена на рынке станет меньше, либо равна, цене активации.
            a: Стоп лосс на покупку, активируется, когда цена на рынке станет больше, либо равна, цене активации.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить иллюстрированную инструкцию про стоп лосс, и тэйк про'фит в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [инструкцию по выставлению стоп-заявок в терминале FinamTrade|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyastopzayavkifinamtrade]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Тейк-профит
            a: Тэйк про'фит предполагает, что инвестор заранее выбирает условия, при которых заявка активируется - и выставится лимитная либо рыночная. Для закрытия коротких позиций следует выставлять стоп заявки на покупку, для закрытия длинных позиций, на продажу.
            a: Заявка может быть выставлена со сроком действия: до отмены, до конца дня, до указанной даты. Тэйк про'фит на продажу, активируется, когда цена на рынке станет больше либо равна цене активации.
            a: Тэйк про'фит на покупку, активируется, когда цена на рынке станет меньше либо равна цене активации.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить иллюстрированную инструкцию про стоп лосс, и тэйк про'фит в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [инструкцию по выставлению стоп-заявок в терминале FinamTrade|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyastopzayavkifinamtrade]";
                    $reactions.transition("/Отправка инструкции в чат");
                    }
            a: Вы хотите узнать подробнее про защитный спрэд и коррекцию?
            script: 
                $context.session = {};
            q: @agree ||toState = "/Заявки_Виды заявок/Защитный спрэд и уровень коррекции"    
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?"
            # final answer
            
        state: Защитный спрэд и уровень коррекции
                a: Можно увеличить вероятность совершения сделки при исполнении тейка, задав условие, Защитный спрэд, либо использовав галочку, Рыночная. Если указать ноль, в поле Защитный спрэд, то на Биржу будет отправлена заявка с ценой, равной цене первой сделки на рынке, которая удовлетворяет цене активации.
                a: Таким образом, для определения цены заявки, исполняющей тэйк про'фит на покупку, защитный спрэд прибавляется к цене рынка, в то время как для определения цены заявки исполняющей тэйк про'фит на продажу, защитный спрэд вычитается из цены рынка. Условие Коррекция, используется для того, чтобы включить механизм отслеживания тренда.
                a: Данное условие используется следующим образом; Для тэйк про'фит на продажу, считается, что растущий тренд заканчивается в тот момент, когда после того, как рынок вырос до уровня цены активации или выше, он снизится на величину коррекции от максимальной цены.
                a: В то время как для тэйк про'фит на покупку считается, что нисходящий тренд заканчивается в тот момент, когда после того, как рынок снизился до уровня цены активации или ниже, он вырастет на величину коррекции от минимальной цены.
                script:
                    if (identificationAO($client.profileCRM) == "success"){
                        $context.session.lastState = $context.currentState;
                        $session.questionText = "Хотите получить иллюстрированную инструкцию про стоп лосс, и тэйк про'фит в чат?";
                        $session.insrtuctionText = "В продолжение разговора направляю вам [инструкцию по выставлению стоп-заявок в терминале FinamTrade|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyastopzayavkifinamtrade]";
                        $reactions.transition("/Отправка инструкции в чат");
                    }
                a: Чем я могу еще помочь?
                q: @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
                # final answer
            
        state: Связанные
            a: В веб терминале фина'м трейд, есть возможность выставить с графика лимитную заявку на закрытие позиции со связанными стоп-заявками, стоп-лоссом и тэйк-про'фитом. Обращаем ваше внимание, что привязать заявки к уже существующей заявке, невозможно.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить иллюстрированную инструкцию про стоп лосс, и тэйк про'фит в чат?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам [инструкцию по выставлению стоп-заявок в терминале FinamTrade|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyastopzayavkifinamtrade]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
            
            
    state: IPO  
        intent!: /050 IPO
        
        a: Инвесторам фина'м, доступно участие в публичных размещениях ценных бумаг, и в аукционе облигаций федерального займа. Посмотреть календарь доступных размещений, подать или отменить заявку, узнать ее статус и номер, можно в личном кабинете, и в терминале фина'м трейд.
        a: При желании редактировать уже поданную заявку, её нужно отменить и подать заново. В личном кабинете, раздел Заявки айпио, находится внизу главной страницы личного кабинета, под перечнем активов. Нажмите на название раздела Заявки айпио, и разверните список.
        a: После этого, справа от названия раздела, также появится переход в раздел доступных размещений, под названием, Публичные размещения акций и облигаций.
        a: В терминале фина'м трейд, в левом вертикальном меню в разделе Первичные размещения, отмеченном значком ракеты; также содержится информация как о предстоящих размещениях, так и об уже поданных заявках, во вкладке Мои заявки.
        a: Обращаем ваше внимание, что номер заявки размещения, появляется и отображается в списке поданных заявок, только в день технического размещения ценной бумаги.
        a: Хотите узнать подробнее о комиссиях и условиях участия в первичном размещении?
        script:
            $context.session = {};
        q: @agree ||toState = "/IPO_Условия участия"    
        q: @disagree ||toState = "/Могу еще чем то помочь?"
        q: @repeat_please * ||toState = "."
        # final answer
            
            
    state: IPO_Условия участия
        
        script:
            if ( typeof $parseTree._exchanges_IPO != "undefined" ){
                $session.exchanges_IPO = $parseTree._exchanges_IPO;
            }
            if ( typeof $session.exchanges_IPO == "undefined" ){
                $reactions.transition("/IPO_Условия участия/Уточнение типа рынка");
            } else {
                $reactions.transition("/IPO_Условия участия/" + $session.exchanges_IPO.name);
            }
            
        state: Уточнение типа рынка
            a: Вас интересует размещение на Российских, или Американских биржах?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @exchanges_IPO *
                script:
                    $session.exchanges_IPO = $parseTree._exchanges_IPO;
                    $reactions.transition("/IPO_Условия участия");    
        
        state: Российские
            a: Для участия в российских размещениях, Статус квалифицированного инвестора не требуется, если иное не установлено эмитентом. Заявки принимаются минимально от 1000 рублей для размещений бумаг Финама, и от 10000 рублей для сторонних размещений, если иное не установлено эмитентом.
            a: За участие в размещениях взымается комиссия от оборота на фондовой секции по вашему тарифному плану. А также следующие комиссии в зависимости от актива и биржи.
            a: За участие в размещении облигаций на московской бирже, комиссия 0,04%, при обороте до 1000000 рублей в день; дополнительно взымается комиссия биржи за урегулирование в размере 0,015%.
            a: За участие в размещении акций на московской бирже, комиссия 0,236% от суммы сделки; дополнительно взымается комиссия биржи за урегулирование 0,03%. За участие в размещении акций на СПБ бирже, взымается комиссия биржи за урегулирование 0,01%.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Американские
            a: Размещение на зарубежных площадках доступно только со статусом квалифицированного инвестора. Заявки принимаются минимально от 1000 долларов США, если иное не установлено эмитентом.
            a: Комиссия за участие составляет 5% от размещенной суммы, и взымается двумя частями: 2,5% взымается в рублях эРэФ, и 2,5% в долларах США. На момент подачи заявки по счету необходимо обеспечить свободные доллары США в сумме, достаточной для подачи заявки.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            

            
    state: Финам бонус
        intent!: /051 Финам бонус
        a: Участвуйте в акции фина'м бонус. Пройдите регистрацию, и получи'те 500 приветственных бонусов. До конца июня 2024 года, накопите 3000 бонусов за использование сервисов Фина'м, и получи'те их на брокерский счет.
        a: Один бонус равен одному рублю. Зарегистрироваться в акции и получить детальную информацию можно на сайте фина'м точка ру. Для этого, в верхней части страницы сайта, выберите раздел, Программа лояльности.
        a: Обращаем ваше внимание, Бонусный баланс в кабинете участника акции, обновляется один раз в сутки. Выплата Вознаграждения за участие во втором этапе акции, с апреля по июнь 2024 года, будет выплачено до 15 июля 2024 года, если участник не предпочтёт обменять бонусы на образовательный онлайн курс.
        a: Хотите узнать как авторизоваться в кабинет участника акции фина'м бонус?
        script:
            $context.session = {};
        q: @agree ||toState = "/Авторизация_Логин - Пароль/FinamBonus"    
        q: @disagree ||toState = "/Могу еще чем то помочь?"
        q: @repeat_please * ||toState = "."
        # final answer
        
        
            
    state: Справка по счету 
        intent!: /052 Справка по счету
        
        a: Посмотреть историю начислений, и списаний; изменение баланса за выбранный период; цены покупок и продаж; корпоративные действия по бумагам находящимся в портфеле; можно самостоятельно в личном кабинете, в истории операций или в справке по счету.
        a: Заказать справку по брокерскому счету, можно в личном кабинете на сайте, фина'м точка ру; для этого выберите меню документы, далее выберите раздел, налоги и справки. Максимальный интервал получения справки по счету, 92 дня. При необходимости получить годовой отчет, справку можно сформировать 4 раза.
        a: В разделе, брокерский отчет, автоматически выгружаются отчеты брокера на подпись. Также, историю операций по счету, можно посмотреть в личном кабинете, для этого выберите нужный счет, далее выберите вкладку, история.  Для заказа брокерского отчета на бумажном носителе обратитесь к менеджеру поддержки.
        a: Назовите тему, чтобы узнать подробнее; я могу рассказать; про начисление пени; о сделках Своп; операциях репо'; или про торговый оборот.
        
        q: * @oborot_u * ||toState = "/Справка по счету_Торговый оборот"
        q: * @penalties_u * ||toState = "/Справка по счету_Начисление пени"
        q: * @swap_u * ||toState = "/Справка по счету_Сделки СВОП"
        q: * @repo_u * ||toState = "/Справка по счету_Сделки РЕПО"
        q: * @choice_1 * ||toState = "/Справка по счету_Начисление пени"
        q: * @choice_2 * ||toState = "/Справка по счету_Сделки СВОП"
        q: * @choice_3 * ||toState = "/Справка по счету_Сделки РЕПО"
        q: * @choice_4 * ||toState = "/Справка по счету_Торговый оборот"
        q: * @choice_last * ||toState = "/Справка по счету_Торговый оборот"
        q: @repeat_please * ||toState = "."
        # final answer
        
    state: Справка по счету_Торговый оборот
        intent!: /052 Справка по счету/Справка по счету_Торговый оборот
        a: Торговый оборот за выбранный период, можно изучить в справке по счету в разделе справки, Виды движений денежных средств. Заказать справку по брокерскому счету, можно в личном кабинете на сайте, фина'м точка ру; для этого выберите меню документы, далее выберите раздел, налоги и справки.
        a: Также, торговый оборот за последние 4 завершенных квартала, с целью получения статуса квалифицированного инвестора, можно проверить в личном кабинете на сайте фина'м точка ру. Для этого, в правом верхнем углу личного кабинета, нажмите на значок персоны, далее выберите, Инвестиционный статус.
        a: Обращаем ваше внимание, согласно требованиям Центрального банка эРэФ, валютные операции не учитываются при подсчете оборота для присвоения статуса квалифицированного инвестора.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
        
    state: Справка по счету_Начисление пени
        intent!: /052 Справка по счету/Справка по счету_Пени
        a: Пеня, это комиссия за займ денежных средств, которая образовалась в результате списаний по тарифам. За каждый день немаржинального займа, Инвестор уплачивает Брокеру пеню, на сумму неисполненных обязательств.
        a: Для того чтобы пеня не списывалась, нужно иметь свободные денежные средства на брокерском счете, для удержания комиссий, сопутствующих торговле. В базу немаржинальной задолженности, попадают все списания в минус, на практике, это начисленные комиссии за сделки и переносы займа.
        a: С этой базы берется пеня по единой ставке, соответствующей комиссии за займ по тарифному плану.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer

    state: Справка по счету_Сделки СВОП
        intent!: /052 Справка по счету/Справка по счету_СВОП
        a: Брокер осуществляет перенос валютных позиций инвестора в том случае, если на брокерском счете не могут пройти расчеты за сделки с валютой. Такие технические операции называются СВОП, или, сделки переноса необеспеченных валютных позиций.
        a: Фактически, на брокерских счетах, одна валюта выступает обеспечением для другой; И, если на дату расчетов по сделке, на брокерском счёте инвестора, отрицательная чистая позиция по рублям или другой валюте, то возникнет СВОП. Наглядно изучить расчет комиссии за сделки СВОП, можно на сайте Фина'м точка ру.
        a: Для этого в верхней части страницы сайта выберите раздел Инвестиции, далее выберите раздел Тарифы, Сравнение тарифов.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
    state: Справка по счету_Сделки РЕПО
        intent!: /052 Справка по счету/Справка по счету_РЕПО
        go!: /Overnight_REPO
        # script:
        #     $reactions.transition("/Overnight_REPO");
        
        
    state: Закрыть задолженность
        intent!: /053 Закрыть задолженность
        a: Если у вас в портфеле появилась графа' Обязательства, это говорит о том, что у вас возникла маржинальная позиция, или задолженность перед брокером. Погасить задолженность на брокерском счете, можно следующими способами.
        a: Внести денежные средства на брокерский счет в количестве не менее суммы задолженности; перевести денежные средства с одного брокерского счёта на другой; изменить структуру портфеля, например, закрыв часть позиций; а также если у вас на счёте есть задолженность по валюте, то вы можете приобрести соответствующую валюту.
        a: Хотите узнать подробнее, как купить или продать неполный лот валюты?
        script: 
            $context.session = {};
        q: @agree ||toState = "/Валюта_неполный лот/Ответ_Брокер"    
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?"
        # final answer
        
        
       
    
    state: Подписание документов
        intent!: /054 Подписание документов
        
        a: Назовите тему, чтобы узнать подробнее; настройка смс-подписи; настройка электронной подписи; или что делать если не удается подписать документ.
        
        q: * @ASP_SMS * ||toState = "/Подписание документов_СМС подпись"
        q: * @ECP * ||toState = "/Подписание документов_ЭЦП"
        q: * @document_errors_u * ||toState = "/Подписание документов_Трудности подписания"
        q: * @choice_1 * ||toState = "/Подписание документов_СМС подпись"
        q: * @choice_2 * ||toState = "/Подписание документов_ЭЦП"
        q: * @choice_3 * ||toState = "/Подписание документов_Трудности подписания"
        q: * @choice_last * ||toState = "/Подписание документов_Трудности подписания"
        q: @repeat_please * ||toState = "."
        
    
    state: Подписание документов_СМС подпись
        intent!: /054 Подписание документов/Подписание документов_СМС подпись
        
        script:
            $reactions.transition("/Подписание документов_СМС подпись/Ответ_Брокер");
            
        state: Ответ_Брокер
            a: Чтобы подписывать электронные документы и поручения в личном кабинете, подключите SMS подпись. Это удобная и современная альтернатива ключу электронной подписи; она бессрочна, и не требует перевыпуска; для подписания документа, достаточно ввести код, полученный в виде СМС на указанный мобильный телефон.
            a: Настроить СМС подпись можно в Личном кабинете, на сайте едо'кс точка фина'м точка ру; в разделе Сервис. Услуга предоставляется бесплатно.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
    
    state: Подписание документов_ЭЦП
        intent!: /054 Подписание документов/Подписание документов_ЭЦП
        
        script:
            $reactions.transition("/Подписание документов_ЭЦП/Ответ_Брокер");
     
        state: Ответ_Брокер
            a: Перед созданием ключа электронной подписи, скачайте и установите специальный Плагин для генерации электронной подписи; он доступен на устройствах с системой Windows, рекомендуется использовать браузер Google Chrome.
            a: Чтобы скачать плагин, авторизуйтесь в личном кабинете на сайте едо'кс точка фина'м точка ру, и перейдите в раздел Помощь, далее выберите пункт, Инструкции шаблоны ПэО. Скачайте Плагин и выполните настройку.
            a: После установки плагина для генерации электронной подписи, можно приступить к ее выпуску. Для этого, в личном кабинете, на сайте едо'кс точка фина'м точка ру, перейдите в раздел Сервис; далее выберете, Электронная подпись, Создание ключей, Создать сертификат.
            a: В открывшемся диалоговом окне выберите пустую папку для хранения нового ключа, и нажмите Старт. Шевелите мышкой до заполнения индикатора. После успешного создания ключа нажмите, Активировать, а затем Сохранить.
            a: Плагин должен быть включен в настройках браузера, проверить можно, нажав три точки справа вверху браузера, выбрать меню Дополнительные инструменты, Расширения.
            script:
                if (identificationAO($client.profileCRM) == "success"){
                    $context.session.lastState = $context.currentState;
                    $session.questionText = "Хотите получить в чат подробную инструкцию по созданию ключа электронной подписи?";
                    $session.insrtuctionText = "В продолжение разговора направляю вам подробную иллюстрированную [инструкцию по созданию ключа электронной подписи|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyaposozdaniyuczp]";
                    $reactions.transition("/Отправка инструкции в чат");
                }
            a: Чем я могу еще помочь?
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
        
        
        
    state: Подписание документов_Трудности подписания
        intent!: /054 Подписание документов/Подписание документов_Трудности подписания

        script:
            if ( typeof $parseTree._signing_difficulties != "undefined" ){
                $session.signing_difficulties = $parseTree._signing_difficulties;
            }
            if ( typeof $session.signing_difficulties == "undefined" ){
                $reactions.transition("/Подписание документов_Трудности подписания/Уточнение типа ошибки");
            } else {
                $reactions.transition("/Подписание документов_Трудности подписания/" + $session.signing_difficulties.name);
            }
            
        state: Уточнение типа ошибки
            a: Уточните, вам не приходит смс код, или вы не можете найти документ?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @signing_difficulties *
                script:
                    $session.signing_difficulties = $parseTree._signing_difficulties;
                    $reactions.transition("/Подписание документов_Трудности подписания");
                    
        state: Не получается найти
            a: Как правило, подписанные и неподписанные документы, можно найти в личном кабинете на сайте фина'м точка ру; в разделе документы.
            a: А также, если вам нужно отследить статус поданного поручения на отправку или получение документа, нужно зайти в раздел, где вы оформляли поручение, и выбрать правильный временной интервал дат.
            a: Чем я могу еще помочь?
            script: 
                $context.session = {};
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            # final answer
            
        state: Не приходит смс
            go!: /Авторизация_Не приходит СМС
    
    state: Разблокировка ЦБ
        intent!: /055 Разблокировка ЦБ
        a: Какая информация по заблокированным активам вас интересует? Обмен бумаг по указу номер 844; сервис торговли заблокированными бумагами на внебирже; или вы хотите узнать новости о разблокировке ценных бумаг.

        q: * @844_u * ||toState = "/Разблокировка ЦБ_Обмен по указу 844"
        q: * @trading_blocked_icb_u * ||toState = "/Разблокировка ЦБ_Торговля ЗИЦБ"
        q: * @news_blocking_icb_u * ||toState = "/Разблокировка ЦБ_Новости разблокировки"       
        q: * @choice_1 * ||toState = "/Разблокировка ЦБ_Обмен по указу 844"
        q: * @choice_2 * ||toState = "/Разблокировка ЦБ_Торговля ЗИЦБ"
        q: * @choice_3 * ||toState = "/Разблокировка ЦБ_Новости разблокировки"
        q: * @choice_last * ||toState = "/Разблокировка ЦБ_Новости разблокировки"
        q: @repeat_please * ||toState = "."
    
    state: Разблокировка ЦБ_Обмен по указу 844
        intent!: /055 Разблокировка ЦБ/Разблокировка ЦБ_Обмен по указу 844
        a: Прием заявок на обмен активов в рамках Указа номер 844 – завершён 6 мая в 9:00 по московскому времени.
        a: Статус своей поданной заявки на обмен активов, можно посмотреть в личном кабинете; для этого авторизуйтесь в личный кабинет на сайте фина'м точка ру; перейдите в раздел документы, далее выберите Журнал поручений.
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
        
    state: Разблокировка ЦБ_Торговля ЗИЦБ
        intent!: /055 Разблокировка ЦБ/Разблокировка ЦБ_Торговля ЗИЦБ
        go!: /Заявки_Покупка - Продажа/Ценные бумаги/Заблокированные ЦБ
        
    state: Разблокировка ЦБ_Новости разблокировки
        intent!: /055 Разблокировка ЦБ/Разблокировка ЦБ_Новости разблокировки
        go!: /Санкции_СПБ_биржа
        


    state: Умный старт
        intent!: /056 Умный старт
        a: Научитесь инвестировать на реальном рынке с помощью уникальной программы Умный старт. После регистрации в программе получи'те виртуальные 50000 рублей. Торгуйте акциями на Московской бирже в течение 5 рабочих дней, с возможностью получить денежное вознаграждение, ничего не теряя в случае убытка.
        a: Участие в программе бесплатное, и доступно только для новых клиентов компании фина'м. Узнать больше о программе Умный старт, и зарегистрироваться, можно на сайте фина'м точка ру. Для этого в верхней части сайта выберите раздел Инвестиции; далее выберите раздел, Счета’; тэст драйв реального счёта.
        a: Чем я могу еще помочь?
        script: 
            $context.session = {};
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
    
        
    state: Уведомления
        intent!: /057 Уведомления 
        
        script:
            $analytics.setMessageLabel("057 Уведомления", "Интенты");
            
            if ( typeof $parseTree._notifications_type != "undefined" ){
                $session.notifications_type = $parseTree._notifications_type;
            }
            if ( typeof $session.notifications_type == "undefined" ){
                $reactions.transition("/Уведомления/Уточнение типа уведомления");
            } else {
                $reactions.transition("/Уведомления/Ответ_" + $session.notifications_type.name);
            }
        
    
        state: Уточнение типа уведомления   
            a: Уточните, уведомление с каким текстом вам пришло?
            q: @repeat_please * ||toState = "."
            state: Ожидание ответа
                q: * @notifications_type *
                script:
                    $session.notifications_type = $parseTree._notifications_type;
                    $reactions.transition("/Уведомления");
                    
            state: LocalCatchAll
                event: noMatch
                # a: Вы назвали уведомление неверно, либо информация по данному уведомлению на данный момент отсутствует.
                script:
                    $session.operatorPhoneNumber = '1000';
                    $reactions.transition("/Оператор/Оператор по номеру");
        
        
        state: Ответ_Уровень маржи
            go!: /Маржинальная торговля_уровень маржи
            
        state: Ответ_Уведомление КПУР
            go!: /Маржинальная торговля_КПУР|КСУР
            
        state: Ответ_Уведомление КВАЛ
            go!: /КВАЛ_соответствие
            
        state: Ответ_Расторжение договора
            script:
                $session.operatorPhoneNumber = '1000';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_Получение карты
            script:
                $session.operatorPhoneNumber = '3400';
                $reactions.transition("/Оператор/Оператор по номеру");
                # final scenario
            
        state: Ответ_Возврат НДФЛ
            go!: /Возврат ндфл
            
    
    state: Яндекс
        intent!: /058 Яндекс
        a: Акционерам Yandex эН Вэ, с регистрацией в Нидерландах; предлагается обменять свои акции на акции эМ Ка ПАО ЯНДЕКС, с регистрацией в России; с коэффициентом один к одному, через инфраструктуру Московской и СПБ бирж.
        a: Участвовать в обмене могут инвесторы, которые приобрели акции Яндекса на Московской или СПБ бирже. Посмотреть, на какой бирже хранится ценная бумага в портфеле, можно в личном кабинете едо'кс точка фина'м точка ру, в разделе брокерского счёта Портфель онлайн.
        a: А также в терминале фина'м трейд, нажмите правой кнопкой мыши по активу в портфеле, выберите пункт Детали позиции; под названием актива отображается биржа.
        a: Срок подачи заявок до 15:00 по московскому времени 19 июня. Брокер Фина'м автоматически подаст заявку на обмен акций Yandex эН Вэ для своих клиентов. В настоящее время, ведутся работы над функционалом автоматической подачи заявки.
        # script:
        #     if (identificationAO($client.profileCRM) == "success"){
        #         $context.session.lastState = $context.currentState;
        #         $session.questionText = "Хотите получить ссылку на подачу заявки на обмен в чат?";
        #         $session.insrtuctionText = "Подать заявку на обмен можно в личном кабинете по [ссылке|https://edox.finam.ru/Ipo/Securities]. Актуальная информация об участии в выкупе на [сайте  эмитента|https://yatenderoffer.ru/%D0%BE%D0%B1%D1%89%D0%B8%D0%B5-%D0%B2%D0%BE%D0%BF%D1%80%D0%BE%D1%81%D1%8B/].";
        #         $reactions.transition("/Отправка инструкции в чат");
        #     }
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer
        
    state: Вакансии
        intent!: /059 Вакансии
        a: Ознакомиться с актуальными вакансиями в финансовой группе фина'м, и направить своё резюмэ, можно на сайте фина'м точка ру. В верхней части страницы выберите раздел Группа фина'м; ниже выберите раздел Информация и контакты, Вакансии.
        script:
            if (identificationAO($client.profileCRM) == "success"){
                $context.session.lastState = $context.currentState;
                $session.questionText = "Хотите я направлю ссылку на раздел Вакансии, вам в чат?";
                $session.insrtuctionText = "В продолжение разговора направляю вам ссылку на [раздел «Вакансии»|https://job.finam.ru/]";
                $reactions.transition("/Отправка инструкции в чат");
            }
        a: Чем я могу еще помочь?
        q: @repeat_please * ||toState = "."
        q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
        # final answer    
        

    state: Оператор
        intent!: /010 Оператор
        script:
            $analytics.setMessageLabel("Запрос оператора клиентом", "Оператор VB");
            # final scenario
            if( typeof $parseTree._company != "undefined"){
                    $session.operatorPhoneNumber =  $parseTree._company.phoneNumber;
                    $reactions.transition("./Оператор по номеру");
                }  
            else if( typeof $parseTree._department != "undefined"){
                $reactions.transition("/Оператор/Отдел");
            }
            else if( typeof $parseTree._additionalTelephone != "undefined"){
                $reactions.transition("./Личные добавочные");
            }
            else if( typeof $parseTree._officePhone != "undefined"){
                $reactions.transition("./Регион добавочные");
            }
            else if( typeof $parseTree._officePhone_question != "undefined"){
                $reactions.transition("./Регион добавочные_уточнение улицы");
            }
         
            else {
                $reactions.transition("./Уточнение отдела");
            }
            
        state: Уточнение отдела
            a: Уточните название отдела, с которым хотите связаться; поддержка клиентов; голосовой трейдинг; или отдел форекс.
            q: * @officePhone * ||toState = "/Оператор/Регион добавочные"
            q: * @officePhone_question * ||toState = "/Оператор/Регион добавочные_уточнение улицы"
            q: * @additionalTelephone * ||toState = "/Оператор/Личные добавочные"
            q: * @company * ||toState = "/Оператор/Уточнение отдела/Выбрана компания"
            q: * @department * ||toState = "/Оператор/Отдел"
            q: * @repeat_please * ||toState = "."
            q: финам* ||toState = "/Оператор/Под_Финам"
            q: @disagree ||toState = "/Другой_вопрос"
            q: сорок ||toState = "/Оператор/Уточнение отдела/Форекс"
            q: * @choice_1 * ||toState = "/Оператор/Уточнение отдела/Поддержка брокера"
            q: * @choice_2 * ||toState = "/Оператор/Уточнение отдела/Голосовой трейдинг"
            q: * @choice_3 * ||toState = "/Оператор/Уточнение отдела/Банковские услуги"
            q: * @choice_4 * ||toState = "/Оператор/Уточнение отдела/Управляющая компания"
            q: * @choice_5 * ||toState = "/Оператор/Уточнение отдела/Форекс"
            q: * @choice_last * ||toState = "/Оператор/Уточнение отдела/Форекс"
            
            state: Поддержка брокера
                script:
                    $session.operatorPhoneNumber = '1000';
                    $reactions.transition("../../Оператор по номеру");
            state: Голосовой трейдинг
                script:
                    $session.operatorPhoneNumber = '2200';
                    $reactions.transition("../../Оператор по номеру");
            state: Банковские услуги
                script:
                    $session.operatorPhoneNumber = '1000';
                    $reactions.transition("../../Оператор по номеру");
            state: Управляющая компания
                script:
                    $session.operatorPhoneNumber = '1000';
                    $reactions.transition("../../Оператор по номеру");
            state: Форекс
                script:
                    $session.operatorPhoneNumber = '3887';
                    $reactions.transition("../../Оператор по номеру");
            state: Выбрана компания
                script:
                    $session.operatorPhoneNumber =  $parseTree._company.phoneNumber;
                    $reactions.transition("../../Оператор по номеру");
            # Для того, что бы не сработало распознание родительского (не было цикла) 
            state: Оператор
                intent: /010 Оператор
                go!: ../../NoMatchOperator
            # state: noMatch
            #     event: noMatch
            #     go!: ../../NoMatchOperator
                    
        state: Личные добавочные
            script:
                $analytics.setMessageLabel("Добавочный сотрудника", "Оператор VB"); 
                # $session.operatorPhoneNumber = $parseTree._additionalTelephone.phoneNumber;
                $session.operatorPhoneNumber = '1000'; //На данный момент все переводы на личные добавочные проходят через КЦ
                $reactions.transition("/Оператор/Оператор по номеру");
        
        state: Регион добавочные_уточнение улицы
            script:
                if( $parseTree._officePhone_question.name  == 'Москва'){
                    $reactions.transition("/Оператор/Регион добавочные_уточнение улицы/Москва");
                } else if( $parseTree._officePhone_question.name  == 'Уфа'){
                    $reactions.transition("/Оператор/Регион добавочные_уточнение улицы/Уфа");
                } else if( $parseTree._officePhone_question.name  == 'Челябинск'){
                    $reactions.transition("/Оператор/Регион добавочные_уточнение улицы/Челябинск");
                } else if( $parseTree._officePhone_question.name  == 'Казань'){
                    $reactions.transition("/Оператор/Регион добавочные_уточнение улицы/Казань");
                }
            
            state: Москва
                a: Уточните, с каким офисом в Москве вы хотите связаться. Центральное отделение в Наста'сьинском переулке; Офис в Перово; офис на Кутузовском; офис у Нахимовского; офис у метро Университет.
                q: * @officePhone * ||toState = "/Оператор/Регион добавочные"
                q: * @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Другой_вопрос"  
            
            state: Уфа
                a: Назовите, с каким офисом в Уфе вы хотите связаться. Офис на улице Цюрупы, или офис на Пушкина.
                q: * @officePhone_ufa * ||toState = "/Оператор/Регион добавочные_уточнение улицы/Уфа/Перевод на добавочный Уфа"
                q: * @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Другой_вопрос"
                
                state: Перевод на добавочный Уфа
                    script:
                        $analytics.setMessageLabel("Добавочный региона", "Оператор VB");
                        $session.officePhone = $parseTree._officePhone_ufa.phoneNumber;
                        $session.PhoneNumber = $parseTree._officePhone_ufa.phoneNumber;
                        regionalOfficeCall($session.officePhone);
                        $analytics.setSessionResult("Факт перевода VB");
                    a: Пожалуйста, оставайтесь на линии. Перевожу вас на {{ $session.officePhone }}. 
                
            state: Челябинск
                a: Назовите, с каким офисом в Челябинске вы хотите связаться. Офис на Красной улице, или офис на 40–летия Победы.
                q: * @officePhone * ||toState = "/Оператор/Регион добавочные"
                q: * @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Другой_вопрос"
                
            state: Казань
                a: Назовите, с каким офисом в Казани вы хотите связаться. Офис на улице Декабристов, или офис на Пушкина.
                q: * @officePhone_kazan * ||toState = "/Оператор/Регион добавочные_уточнение улицы/Казань/Перевод на добавочный Казань"
                q: * @repeat_please * ||toState = "."
                q: @disagree ||toState = "/Другой_вопрос"    
                
                state: Перевод на добавочный Казань
                    script:
                        $analytics.setMessageLabel("Добавочный региона", "Оператор VB");
                        $session.officePhone = $parseTree._officePhone_kazan.phoneNumber;
                        $session.PhoneNumber = $parseTree._officePhone_kazan.phoneNumber;
                        regionalOfficeCall($session.officePhone);
                        $analytics.setSessionResult("Факт перевода VB");
                    a: Пожалуйста, оставайтесь на линии. Перевожу вас на {{ $session.officePhone }}. 
        
        state: Регион добавочные
            script:
                $analytics.setMessageLabel("Добавочный региона", "Оператор VB");
                $session.officePhone = $parseTree._officePhone.phoneNumber;
                $session.PhoneNumber = $parseTree._officePhone.phoneNumber;
                regionalOfficeCall($session.officePhone);
                $analytics.setSessionResult("Факт перевода VB");
            a: Пожалуйста, оставайтесь на линии. Перевожу вас на {{ $session.officePhone }}. 
            
        state: Отдел
            script:
                $analytics.setMessageLabel("Добавочный отдела", "Оператор VB");    
                $session.operatorPhoneNumber = $parseTree._department.phoneNumber;
                $reactions.transition("/Оператор/Оператор по номеру");
        
        state: Под_Финам
            
            script:
                $session.operatorPhoneNumber = '1000';
            go!: ../Оператор по номеру
        
        state: NoMatchOperator
            event: noMatch
            script:
                $analytics.setMessageLabel("ОператорNoMatch", "Оператор VB"); 
                $session.operatorPhoneNumber = '1000';
            go!: ../Оператор по номеру
        
        state: Оператор по номеру
            # Текст не убирать, без него проскакивает обработка функции перевода на оператора callProcessing
            a: Пожалуйста, оставайтесь на линии.
            script:
                var getPhoneByDateTimeResault = getPhoneByDateTime($session.operatorPhoneNumber);
                $session.operatorPhoneNumber = getPhoneByDateTimeResault.phoneNumber;
                $session.departmentName = getPhoneByDateTimeResault.departmentName;
                callProcessing($session.operatorPhoneNumber);
                $analytics.setSessionResult("Факт перевода VB");
                $analytics.setMessageLabel($session.operatorPhoneNumber, "Добавочные VB")
                
            if: (($session.operatorPhoneNumber == '7924') || ($session.operatorPhoneNumber == '1000') || ($session.operatorPhoneNumber == '3024') || ($session.operatorPhoneNumber == '3887') || ($session.operatorPhoneNumber == '3888') || ($session.operatorPhoneNumber == '3820') || ($session.operatorPhoneNumber == '3411') || ($session.operatorPhoneNumber == '3400') || ($session.operatorPhoneNumber == '3889') || ($session.operatorPhoneNumber == '3891'))
                a: Перевожу вас на {{ $session.departmentName }}.
            
            elseif: $session.operatorPhoneNumber == '2200'
                a: Для выставления заявки через голосовой трейдинг, подготовьте и назовите оператору ваш уникальный торговый код, который находится в личном кабинете в разделе Детали по счету. Перевожу вас на {{ $session.departmentName }}.
            
            elseif: (($session.operatorPhoneNumber == '2222') || ($session.operatorPhoneNumber == '3666') || ($session.operatorPhoneNumber == '3777'))
                a: Чтобы получить персональную информацию по вашему брокерскому счету, подготовьте и назовите оператору ваш уникальный торговый код, который находится в личном кабинете в разделе Детали по счету. Перевожу вас на {{ $session.departmentName }}.
            else:    
                a: Соединяю
            # a: Перевожу вас на {{ $session.operatorPhoneNumber }}. // Для теста
            
            script:
                 $context.session = {};
            go!: / 
    
    state: Bye    
        q!: @goodbye
        go!: /Могу еще чем то помочь?/NO/Bye
                     
    state: Могу еще чем то помочь?
        a: Я могу еще чем то помочь?
        
        state: Yes
            q: * @agree *
            a: Уточните, пожалуйста, ваш вопрос.

        state: NO
            q: * @disagree *
            a: Пожалуйста, оцените консультацию от одного до пяти, если пять это отлично.
                
            state: Оценка
                q: * @grade *
                a: Были рады помочь вам! Если понадобится помощь, пожалуйста, позвоните снова. Всего доброго, до свидания!
                script:
                    $session.gradeValue = $parseTree._grade.value;
                    $analytics.setMessageLabel($session.gradeValue, "Оценки VB");
                    $dialer.hangUp();
               
            state: Bye
                q: * @goodbye *
                a: Спасибо за обращение! Если понадобится помощь, пожалуйста, позвоните снова. Всего доброго, до свидания!
                script:
                    $analytics.setMessageLabel("Нет оценки", "Оценки VB");
                    $dialer.hangUp();
                    
            state: NoMatch
                event: noMatch
                a: Спасибо за обращение! Если понадобится помощь, пожалуйста, позвоните снова. Всего доброго, до свидания!
                script:
                    $analytics.setMessageLabel("NoMatch", "Оценки VB");
                    $dialer.hangUp();
                    
    state: NoMatch || noContext = true
        event!: noMatch
        a: Возможно я не так вас поняла. Пожалуйста, перефразируйте свой вопрос.
        script:
            $analytics.setMessageLabel("Не распознан 1", "Теги действий");
        
           
    state: Match
        event!: match
        script:
            $analytics.setMessageLabel("Нет четкого распознавания VB", "Теги действий");
            $reactions.transition($nlp.match($request.query, "/").targetState);
            
         
     # Нераспознанная речь   
    state: VoiceNoInput || noContext = true
        event!: speechNotRecognized
        script:
            $session.noInputCounter = $session.noInputCounter || 0;
            $session.noInputCounter++;
        if: $session.noInputCounter >= 10
            a: Похоже проблема со связью. Перезвоните, пожалуйста, еще раз. 
            script:
                $analytics.setSessionResult("Плохая связь VB");
                $dialer.hangUp();
        else:
            random:
                a: Пожалуйста, опишите коротко суть вопроса.
                a: Позвольте мне вам помочь. Какой у вас вопрос?
                a: Повторите пожалуйста!
    
    
                
    state: TransferEvent
        event!: transfer
        if: $dialer.getTransferStatus().status === 'FAIL'
            a: Приносим свои извинения мы вынуждены завершить звонок. Нам важна каждая минута вашего времени. Сейчас все операторы заняты. 
            a: Пожалуйста, обратитесь к нам в чат поддержки на сайте фина'м ру, или в терминале фина'м трейд. Или перезвоните позднее.
            script:
                $analytics.setSessionResult("Ошибка перевода на оператора VB");
                $dialer.hangUp();
        else: 
            script:
                $context.client = {};

            
                
    state: ClientHangup
        event!: hangup
        script:
            $analytics.setMessageLabel("Сброс клиентом", "Результат звонка VB");
            $context.client = {};
                
    state: BotHangup
        event!: botHangup
        script:
            $analytics.setMessageLabel("Завершено ботом", "Результат звонка VB");
            $context.client = {};
                
    state: TimeLimit
        event!: timeLimit
        script:
            $analytics.setMessageLabel("TimeLimit", "Теги действий");
            $session.operatorPhoneNumber =  '1000';
            $reactions.transition("/Оператор/Оператор по номеру");
            
            
    state: Отправка инструкции в чат
        go!:/Отправка инструкции в чат/Инструкция?
                
        state: Инструкция?
            a: {{$session.questionText}}
            q: @agree ||toState = "/Отправка инструкции в чат/Инструкция?/Отправка"   
            q: @repeat_please * ||toState = "."
            q: @disagree ||toState = "/Отправка инструкции в чат/Чем могу помочь?"
                
            state: Отправка
                script:
                    $analytics.setMessageLabel("Клиент дал согласие", "Инструкции VB");
                    $session.resultSendMessage = sendMessageTxchatHttp($client.personGlobalID, $session.insrtuctionText);
                    $reactions.transition("/Отправка инструкции в чат/Чем могу помочь?");
                    
        state: Чем могу помочь?
            a: Чем я могу еще помочь?
            # q: @repeat_please * ||toState = 
            q: @disagree ||toState = "/Могу еще чем то помочь?/NO"
            state: Ожидание ответа
                q: @repeat_please *
                script:
                    $reactions.transition($context.session.lastState);
    
        
    state: Тест
        event!: тестовый тест
        script:
            $reactions.answer("Результат идентификации " + $client.resultIdentification);
            $reactions.answer("Ваш глобал айди " + $client.personGlobalID);
            $reactions.answer("Профиль СРМ " + $client.profileCRM);    