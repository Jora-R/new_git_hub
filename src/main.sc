require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: dateTime/moment.min.js
    module = sys.zb-common
require: dateTime/dateTime.sc
  module = sys.zb-common
require: name/name.sc
  module = sys.zb-common
require: scripts/Mailing/fetch.js
    type = scriptEs6
    name = fetch 
# Получение профиля клиента в СРМ
require: scripts/Segmentation/profileCRM.js    
# Проверка нажата ли рассылка
require: scripts/Mailing/mailing.js
# Обработка фразы клиента до распознавания (сбой/приветствие/удаление символов и типовых фраз/проверка на дубль/провека на короткий текст)
require: scripts/PreResult/preMatch.js
# Проверка на длину символов (патерн имеет приоритет)
require: scripts/PreResult/selectNLUResult.js
# Проверка на источник обращения - банк
require: scripts/PreResult/preProcess.js
# Обработка завершения диалога
require: scripts/SystemAdjustment/timers.js
# Проверка на премиальность клиента при переводе на оператора
require: scripts/Segmentation/segment.js
# Проверка времени для кац при переводе на оператора
require: scripts/Mailing/kac.js
# Вывод ошибки при отсутствии сценария
require: ./scripts/Errors/DialogError.js
# Вывод ошибки в скрипте
require: ./scripts/Errors/ScriptError.js
# Функция вызова текста в тех перерыв
require: scripts/SystemAdjustment/technicalBreak.js
# Функция активирует текст в праздники
require: scripts/SystemAdjustment/holidays.js
# Проверка тикера на соответствие формату для КВАЛ ЦБ
require: tikerCheck.js


theme: /
    # Раскоментировать функцию в файле preMatch.js (15,16 строки)
    state: Сбой
        a: На данный момент ведутся работы по восстановлению работоспособности систем. Приносим извинения за доставленные неудобства. Вы можете продолжить диалог с виртуальным консультантом, либо обратиться к оператору.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            "Продолжить диалог" -> /Уточнение вопроса
    
    state: Рассылки_want_call
        a: Спасибо за обратную связь!
        script:
            if ($session.sourceId == undefined){
                    $session.sourceId = "14071"; //прод сопровождение, страховка если пришлют неизвестную кнопку
                    //$session.sourceId = "13858"; //прод сопровождение, страховка если пришлют неизвестную кнопку
                }
            $session.request = $request;
            $reactions.timeout({ interval: 2, targetState: "/Рассылки_want_call/Отправка запроса"});
            
        
        state: Отправка запроса
            
            scriptEs6:
                $session.response_crm = await fetch.postData($injector.url_callCRM, {"sourceId":$session.sourceId,"new_website":"Other","DemoFormId":$session.request.data.requestId,"new_globalid":$session.request.data.clientGlobalId})
                # $reactions.answer($session.response_crm.ok);
            
                if ($session.response_crm.ok == true){
                    $reactions.answer("В течение дня с вами свяжется наш специалист.");
                    $analytics.setSessionResult("Отклик CRM TB");
                    $reactions.timeout({ interval: 2, targetState: "/Закрытие обращения БТ"});
                } else {
                    $analytics.setMessageLabel("Отклик не сформирован", "Ошибка TB");
                    $reactions.timeout({ interval: 2, targetState: "/Перевод на оператора"});
                }
    
    state: Рассылки_want_link_oik
        a: Уважаемый клиент, Вы посетили страницу услуги «Персональный брокер». Хотите ли Вы, чтобы наш специалист связался с Вами для более подробной консультации по услуге?
        buttons:
            "Да" -> /Рассылки_want_call
            "Нет" -> /Рассылки_want_link_oik/Закрыть обращение oik
    
        state: Закрыть обращение oik
            a: Если у вас есть вопросы, вы можете задать их в чате.
            script:
                $reactions.timeout({ interval: 2, targetState: "/Закрытие обращения БТ"});
    
    state: Рекомендации
        q!: крякозябла
        script:
            $analytics.setMessageLabel("Проверка рекомендации", "Рекомендации TB");
            # $session.clientGlobalId = "017c982e-7e37-4207-994a-5504325a3f5c"; //Тестовая персона
            # $session.clientGlobalId = "46f99ae2-46ba-4cac-99fc-225b6d85790b"; //Анисенкова
            # $session.clientGlobalId = "88efaf2e-c09f-4168-a3b4-28ecad0ea755"; //Мостовая
            # $session.clientGlobalId = "88efaf2e-c09f-4168-a3b4"; //Аноним
    
            # $session.url_profileCRM = "http://msa-vcbtu1-db01:3000/do_recommendations?globalid=eq." + $session.clientGlobalId + "&type_info=eq.product_fin&order=probability.desc&limit=1"; //TEST
            $session.url_profileCRM = "http://msa-vcbtr1-db01:3000/do_recommendations?globalid=eq." + $session.clientGlobalId + "&type_info=eq.product_fin&order=probability.desc&limit=1"; //PROD
        go!: /Рекомендации/Отправка запроса
        
        state: Отправка запроса
            HttpRequest:
                url = {{$session.url_profileCRM}}
                method = GET
                timeout = 15000
                vars = [{ "name": "recommendation", "value": "$httpResponse[0]"}]
                okState = /Рекомендации/Предоставление рекомендации
                errorState = /Рекомендации/Error
    
        state: Предоставление рекомендации
            script:
                var difference = moment().diff($client.sendRecommendation,'days') || 0
                # log(difference)
                if($session.recommendation.name != null && (!$client.sendRecommendation || difference >=7)){
                    $client.sendRecommendation = moment()
                    $reactions.answer("Искусственный интеллект «Финам» предполагает, что вам может быть интересна услуга: [{{$session.recommendation.name}}|{{$session.recommendation.landing}}]");
                    $analytics.setMessageLabel("Рекомендация отправлена", "Рекомендации TB");
                    $reactions.timeout({ interval: 2, targetState:  "/Закрытие обращения" });
                } else {
                    $reactions.timeout({ interval: 2, targetState:  "/Закрытие обращения" });
                }
            
        state: Error
            script:
                $analytics.setMessageLabel("Ошибка отправки рекомендации", "Рекомендации TB");
                $reactions.timeout({ interval: 2, targetState:  "/Закрытие обращения" });
                    
    state: Пустота
        
    state: тест
        q!: тест
        go!: /Я-робот_ЖП
        
    state: Перевод на оператора || modal = true
        a: Перевожу вас на оператора.
        script:
            $analytics.setMessageLabel("Факт перевода TB", "Тех метки");
            segment();
            if ($session.to_division == undefined){
                //$session.to_division = "5ec8de6b-7786-49b5-a3e5-d7bcc938cbb4"; //div2, для теста
                $session.to_division = "2dec6d3f-1def-42ee-a6ec-a5e19addab04"; //прод КЦ, страховка если пришлют неизвестную кнопку
            }
            $session.url_txchat_redirect_request = $injector.url_txchat + "redirect_request";
            $session.url_txchat_error_report = $injector.url_txchat + "error_report";
            $reactions.timeout({ interval: 2, targetState:  "/Перевод на оператора/Отправка запроса" });
        
        state: Отправка запроса
            
            HttpRequest: 
                url = {{$session.url_txchat_redirect_request}}
                method = POST
                body = { "token": "{{$injector.token_txchat}}", "clientId": "{{$request.rawRequest.clientId}}", "to_division": "{{$session.to_division}}" }
                timeout = 15000
                okState = /Перевод на оператора/Отправка запроса/okState
                errorState = /Перевод на оператора/Отправка запроса/errorState
    
            state: okState
                script:
                    $analytics.setSessionResult("Перевод на оператора TB");
                EndSession:
            
            state: errorState
                script:
                    $analytics.setMessageLabel("Ошибка перевода на оператора", "Ошибка TB");
                    
                a: На данный момент перевод на оператора недоступен. Напишите нам позже, либо обратитесь по контактам: 
                    +7 (495) 1-346-346 — доб. 2222
                    *1945 — доб. 2222 (Бесплатно по РФ для МТС, Билайн, МегаФон и Tele2)
                    клиентская поддержка — service@corp.finam.ru
                    
                HttpRequest: 
                    url = {{$session.url_txchat_error_report}}
                    method = POST
                    body = { "token": "{{$injector.token_txchat}}", "error_type": "redirect_error", "clientId": "{{$request.rawRequest.clientId}}"}
                    timeout = 1000
                    okState = /Перевод на оператора/Отправка запроса/errorState/finishState
                    errorState = /Перевод на оператора/Отправка запроса/errorState/finishState
            
                state: finishState
                    EndSession:
    
        state: Перевод на оператора noMatch
            event: noMatch
            a: Пожалуйста, ожидайте перевод на оператора.
            script:
                $analytics.setMessageLabel("Клиент перебил перевод", "Ошибка TB");
                $reactions.timeout({ interval: 2, targetState:  "/Перевод на оператора/Отправка запроса" });
    
    state: Перевод на оператора КАЦ
        script:
            kac();
            $analytics.setMessageLabel("Перевод на оператора КАЦ", "КАЦ TB");
        go!: /Перевод на оператора     
                
    state: Закрытие обращения
        a: Благодарим за обращение! Пожалуйста, оцените консультацию.
            Если понадобится помощь, пожалуйста, напишите снова.
        script:
            $session.rate_operator = true;
            $session.url_txchat_close_request = $injector.url_txchat + "close_request";
            $session.url_txchat_error_report = $injector.url_txchat + "error_report";
            $reactions.timeout({ interval: 2, targetState:  "/Закрытие обращения/Отправка запроса" });
        
        state: Отправка запроса
           
            HttpRequest: 
                url = {{$session.url_txchat_close_request}}
                method = POST
                body = { "token": "{{$injector.token_txchat}}", "rate_operator": {{$session.rate_operator}}, "clientId": "{{$request.rawRequest.clientId}}" }
                timeout = 15000
                okState = /Закрытие обращения/Отправка запроса/okState
                errorState = /Закрытие обращения/Отправка запроса/errorState
    
            state: okState
                script:
                    $analytics.setSessionResult("Обращение закрыто TB");
                EndSession:
                
            state: errorState
                script:
                    $analytics.setMessageLabel("Ошибка закрытия обращений", "Ошибка TB");
               
                HttpRequest: 
                    url = {{$session.url_txchat_error_report}}
                    method = POST
                    body = { "token": "{{$injector.token_txchat}}", "error_type": "close_error", "clientId": "{{$request.rawRequest.clientId}}"}
                    timeout = 1000
                    okState = /Закрытие обращения/Отправка запроса/errorState/finishState
                    errorState = /Закрытие обращения/Отправка запроса/errorState/finishState
            
                state: finishState
                    EndSession:

    
    # Закрытие обращения без текста и без оценок
    state: Закрытие обращения БТ
        script:
            $session.rate_operator = false;
            $session.url_txchat_close_request = $injector.url_txchat + "close_request";
            $session.url_txchat_error_report = $injector.url_txchat + "error_report";
            $reactions.timeout({ interval: 2, targetState:  "/Закрытие обращения БТ/Отправка запроса" });
        
        state: Отправка запроса
          
            HttpRequest: 
                url = {{$session.url_txchat_close_request}}
                method = POST
                body = { "token": "{{$injector.token_txchat}}", "rate_operator": {{$session.rate_operator}}, "clientId": "{{$request.rawRequest.clientId}}" }
                timeout = 15000
                okState = /Закрытие обращения БТ/Отправка запроса/okState
                errorState = /Закрытие обращения БТ/Отправка запроса/errorState
    
            state: okState
                EndSession:
                
            state: errorState
                script:
                    $analytics.setMessageLabel("Ошибка закрытия обращений", "Ошибка TB");
               
                HttpRequest: 
                    url = {{$session.url_txchat_error_report}}
                    method = POST
                    body = { "token": "{{$injector.token_txchat}}", "error_type": "close_error", "clientId": "{{$request.rawRequest.clientId}}"}
                    timeout = 1000
                    okState = /Закрытие обращения БТ/Отправка запроса/errorState/finishState
                    errorState = /Закрытие обращения БТ/Отправка запроса/errorState/finishState
            
                state: finishState
                    EndSession:
        
    state: Проверка на вложенный файл
        event!: fileEvent
        script:
            $analytics.setMessageLabel("Отправлен Файл TB", "Теги действий");
        go!: /Перевод на оператора
        
    state: Проверка на длину текста
        event!: lengthLimit
        script:
            $analytics.setMessageLabel("Больше 200 символов TB", "Теги действий");
        go!: /Перевод на оператора
    
    state: Приложение банка || modal = true 
        intent!: /БанкАпп
        script:
            $analytics.setMessageLabel("Меню банка", "Банк TB");
        a: Полная информация по тарифам и услугам АО «Банк ФИНАМ» доступна на сайте: https://www.finambank.ru/person/rates/ 
            Ответы на самые популярные вопросы:
        buttons:
            "Банковский счет" -> /Приложение банка/Приложение банка_Счет
            "Банковская карта" -> /Приложение банка/Приложение банка_Карта
            "Конвертация валюты" -> /Приложение банка/Приложение банка_Конвертация валюты
            "Переводы за границу SWIFT" -> /Приложение банка/Приложение банка_За границу
            "Платежи по СБП" -> /Приложение банка/Приложение банка_СБП
            "Перевод на оператора" -> /Перевод на оператора
    
        state: Приложение банка_Счет
            a: ✅ Счета в Банке «Финам» открываются в рублях РФ, долларах США, евро, китайских юанях, казахстанских тенге, армянских драмах.
                    Открытие бесплатное, ведение счета по условиям тарифов.
                    ✅ Актуальные тарифы представлены на сайте Банка, в разделе «Рассчетно-кассовое обслуживание» или по ссылке: https://www.finambank.ru/person/rates 
                    Пожалуйста, выберите один из предложенных вариантов:
            buttons:
                "Как открыть банковский счет" -> /Приложение банка/Приложение банка_Счет_Как открыть
                "Получение наличных в кассе" -> /Приложение банка/Приложение банка_Счет_Наличные
                "Зачисление-хранение валюты" -> /Приложение банка/Приложение банка_Счет_Валюта
                "Вклады" -> /Приложение банка/Приложение банка_Счет_Вклады
                "Назад" -> /Приложение банка
        
        state:  Приложение банка_Счет_Как открыть
            a: Если вы ранее открывали брокерский счет в «Финам» при личном визите в офисе компании, то для вас доступно открытие банковского счета дистанционно в личном кабинете по ссылке: https://lk.finam.ru/open/bank/savings 
                    ❗ Если вы не открывали ранее брокерский счет или открывали его дистанционно, то открыть банковский счет можно только при личном посещении офиса.
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Счет
        
        state: Приложение банка_Счет_Наличные
            a: ✅ Получение наличных рублей со счетов/вкладов, открытых в рублях на сумму более 100000 ₽ осуществляется Банком при условии их предварительного заказа клиентом до 12:30 МСК рабочего дня, предшествующего дню получения.
                    ✅ Получение наличных рублей со счетов/вкладов, открытых в иностранной валюте на сумму более 100000 ₽ осуществляется Банком при условии их предварительного заказа клиентом не менее, чем за 5 рабочих дней до даты их получения (день приема заявки не учитывается).﻿
                    ✅ Получение наличной иностранной валюты (доллары США, евро) возможно только в объеме, находящемся на счетах клиента в Банке (брокерские счета сюда не входят!) до 09.03.2022 (00:00), но не более 10000 $.
                    ✅ Если валюта находится на брокерском счете (в АО или в Банке), при переводе на счета в Банке (после: 09.09.2022) получение валюты наличными доступно в рублях по курсу Банка. 
                    ✅ Средства, размещенные на банковских валютных счетах (в евро и долларах США) до: 09.09.2022 включительно, можно получить в рублях (ограничений нет) по курсу ЦБ на дату выплаты.
                    ✅ Получение наличной иностранной валюты в китайских юанях возможно в отделениях банка «Финам» в г. Москва на Настасьинском пер. дом 7, стр.2, в г. Благовещенск и г. Владивосток.
                    ❗ Предварительно необходимо уточнять наличие средств в кассе Банка: https://www.finambank.ru/about/offices
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Счет
        
        state: Приложение банка_Счет_Валюта
            a: 1. Комиссия за зачисление долларов США и евро на банковские счета Банка «Финам»:
                    ✅ По счетам в USD/EUR – 3% от суммы операции, но не менее 300 USD/EUR и не более суммы операции.
                    ✅ По счетам в иных валютах – не взимается.
                        2. Комиссия за обслуживание банковских счетов в долларах США и евро:
                    ✅ Если совокупный остаток не превышает 3000 единиц валюты – комиссия не списывается. 
                    ✅ Если совокупный остаток равен либо превышает 3000 единиц валюты – 0,013 % в день от остатка.
                    ❗ Комиссия удерживается ежедневно на сумму остатка на начало дня: учитывается совокупный (суммарный) остаток денежных средств по всем валютным текущим счетам/карточным счетам, открытым после 15.08.2022  включительно в долларах США/евро.
                    ❗ Комиссия взимается в валюте Счета отдельно с каждого счета (счетов) в долларах США/евро.
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Счет
        
        state: Приложение банка_Счет_Вклады
            a: С информацией о вкладах и накопительных счетах можно ознакомиться по ссылке: https://www.finambank.ru/person/deposits/ 
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Счет
        
        state: Приложение банка_Карта
            a: «Банк Финам» выпускает банковские карты платежной системы МИР по пакетам услуг «Комфорт», «Премиум», «Корпоративный».
                    ✅ Данные пакеты услуг принимают участие в Программе лояльности «CASHBACK», действующей в Банке.
                    Пожалуйста, выберите один из предложенных вариантов:
            buttons:
                "Как открыть банковскую карту" -> /Приложение банка/Приложение банка_Карта_Как открыть
                "Тарифы на обслуживание карт" -> /Приложение банка/Приложение банка_Карта_Тарифы
                "Банкоматы-Снятие наличных" -> /Приложение банка/Приложение банка_Карта_Наличные
                "Назад" -> /Приложение банка
        
        state: Приложение банка_Карта_Как открыть
            a: ✅ Если вы ранее дистанционно открывали брокерский счет в «Финам», то для вас доступно открытие банковской карты по тарифу «Комфорт» дистанционно в личном кабинете по ссылке https://lk.finam.ru/open/bank/card  
                ❗ Виртуальная карта и личный кабинет интернет-банка https://ibank.finam.ru/ активируются в момент выпуска карты.
                ❗ Пластиковая карта доступна к получению в офисе компании, изготовление до 5 рабочих дней.
                ✅ Если ранее вы не открывали брокерский счет в компании «Финам», или желаете оформить банковскую карту по тарифам «Премиум» или «Корпоративный», то оформить банковскую карту можно только при личном посещении офиса компании.
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Карта
        
        state: Приложение банка_Карта_Тарифы
            a: ✅ Актуальные тарифы «Корпоративный», «Комфорт» и «Премиум» представлены по ссылке: https://www.finambank.ru/person/cards/ 
                ❗ Информация по обслуживанию архивных пакетов услуг по банковским картам доступна на сайте Банка по ссылке https://www.finambank.ru/person/rates - в разделе «Архив тарифов».
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Карта
        
        state: Приложение банка_Карта_Наличные
            a: ✅ Снять денежные средства можно в кассе офиса «Финам»: https://www.finambank.ru/about/offices
                ✅ А также в пунктах выдачи наличных/банкоматах сторонних банков.
                ✅ Актуальные тарифы на снятие наличных представлены на сайте Банка https://www.finambank.ru/person/rates в разделах «Пакет услуг Корпоративный» и «Пакеты услуг Комфорт и Премиум».
                ❗ Информация по обслуживанию архивных пакетов услуг по банковским картам - в разделе «Архив тарифов».
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Карта    
                
        state: Приложение банка_Конвертация валюты
            a: ✅ На данный момент конвертация валюты в рамках банковского обслуживания возможна только в личном кабинете «Банка Финам» по ссылке https://ibank.finam.ru
                    ✅ Если у вас есть брокерский счет, вы можете приобретать необходимую валюту на Московской бирже. 
                    ❗ При планировании операций в иностранной валюте, просим учесть, что на сегодняшний день в кредитных организациях РФ действуют ограничения по выдаче наличной иностранной валюты со счетов физических лиц, открытых в иностранной валюте. Выплаты осуществляются в рублях в наличной форме без ограничений по курсу, определяемому АО «Банк Финам».
                    *Банки могут продавать гражданам доллары США и евро, поступившие в их кассы с 9 апреля 2022 года.
                    Предварительно необходимо уточнять наличие средств в кассе Банка.
            buttons:
                "Перевод на оператора" -> /Перевод на оператора
                "В основное меню" -> /Приложение банка
                
        
        state: Приложение банка_За границу
            a: Через «Банк Финам» доступны переводы заграницу следующих валют:
                    ✅ рубли РФ
                    ✅ армянские драмы
                    ✅ китайские юани
                    ✅ казахстанские тенге
                    Ознакомиться с тарифами можно по ссылке: https://www.finambank.ru/person/rates в разделе «Рассчетно-кассовое обслуживание»
            buttons:
                "В основное меню" -> /Приложение банка
                "Назад" -> /Приложение банка/Приложение банка_Карта
                
        state: Приложение банка_СБП
            a: ✅ Максимальная сумма одного перевода/платежа с использованием Системы быстрых платежей (СБП) составляет 1000000 ₽
                    ✅ Максимальная сумма переводов в месяц - 5000000 ₽
                    ✅ Максимальная сумма переводов в месяц на свой банковский счет (вклад), открытый в другой кредитной организации - 30000000 ₽
                    ✅ Лимиты и комиссии на исходящие переводы через Систему быстрых платежей (СБП) по тарифам:
                    1. «Комфорт» — до 200000 ₽ в месяц — без комиссии
                    свыше 200000 ₽ в месяц — 0,5% от суммы перевода, не более 1500 ₽
                    2. «Премиум» — до 5000000 ₽, без комиссий
                    3. «Корпоративный» — до 300000 ₽ в месяц - без комиссии, свыше 300000 ₽ в месяц - 0,5% от суммы перевода, не более 1500 ₽
                    ✅ При расчете максимальной суммы переводов, совершенных в течение календарного месяца, а также при расчете комиссии за исходящий перевод, учитывается совокупный объем денежных средств по исходящим переводам с использованием СБП за текущий календарный месяц по всем счетам клиента, открытым в Банке.
                    ✅ Актуальные тарифы на сайте Банка: https://www.finambank.ru/person/rates в разделах «Пакет услуг Корпоративный» и «Пакеты услуг Комфорт и Премиум»
            buttons:
                "В основное меню" -> /Приложение банка
        
        state: Закрытие чата_ЖП
            q: @closeChat_HC
            go!: /Закрытие обращения
            
        state: Дежурный режим_ЖП
            q: @standbyMode_HC  
            go!: /Перевод на оператора
            
        state: СБП_ЖП   
            q: @SBP_HC
            go!: /Приложение банка/Приложение банка_СБП    
            
        state: Новый вопрос по банку
            event: noMatch
            script:
                $analytics.setMessageLabel("noMatch банка", "Банк TB");
            go!: /Перевод на оператора

    state: Start
        q!: $regex</start>
        
    state: Высокодоходный портфель_HC
        q!: * @highly_profitable_case_HC *
        go!: /Высокодоходный портфель
    
    state: Защитный портфель_HC
        q!: * @protective_case_HC *
        go!: /Защитный портфель
    
    state: Высокодоходный портфель + Защитный портфель
        q!: * @profitable_case_protective_case_HC *
        script:
            $analytics.setMessageLabel("Общее", "КАЦ TB");
        a: В ФинамТрейд представлены несколько видов продуктов-конструкторов: Защитный портфель и Высокодоходный портфель (раньше оба этих продукта были объединены под общим названием ИИП). «Защитный портфель» призван обеспечивать сохранность ваших средств, предлагая умеренный доход и защиту от рыночных колебаний, а «Высокодоходный портфель» дает возможность удвоить вложенный капитал, но и несет в себе повышенные риски. Выберите нужный продукт.
        buttons:
            "Высокодоходный портфель" -> /Высокодоходный портфель
            "Защитный портфель" -> /Защитный портфель
            "Детальнее у специалиста" -> /Перевод на оператора КАЦ
    
    state: ИИ Советник
        q!: @ii_sovetnyk_HC
        go!: /Услуги компании_Помощники_ИИ советник
    
    state: Инвестиционное сопровождение
        q!: @invest_soprovojdenye_HC
        go!: /Услуги компании_Готовые решения_Сопровождение
        
    state: Robo-Advisor_ЖП
        q!: * @Robo-Advisor_HC *
        go!: /Услуги компании_Помощники_ИИ советник    
    
    state: ИИП_ЖП
        q!: * @iip_HC *
        go!: /Услуги компании_Готовые решения
    
    # один клик в Рассылках КАЦ
    state: Рассылки КАЦ
        q!: @mailing_KAC_HC
        script:
            $analytics.setMessageLabel("Рассылки КАЦ", "КАЦ TB");
        go!: /Перевод на оператора КАЦ
        

    state: Отмена вывода_ЖП
        q!: * @otmenaVivoda_HC *
        go!: /Перевод на оператора
        
    state: Finex_ЖП
        q!: * @finex_HC *
        go!: /Ограничение ЦБ_FinEX
            
    state: ИМЯ_ФИО_ЖП
        q!: * @nameFIO_HC *
        go!: /Перевод на оператора
    
    state: Вармаржа_ЖП
        q!: * @varMarg_HC *
        go!: /Срочный рынок
            
    state: W8_ЖП
        q!: * @W8_HC *
        go!: /Форма W8BEN
            
    state: КД_ЖП
        q!: * @korporativnoeDeistvie_HC *
        go!: /Корпоративные действия
    
    state: Валюта_ЖП 
        q!: * @valyuta_HC *
        go!: /Валютный рынок
            
    state: Золотые слитки_ЖП
        q!: * @zolotieSlitki_HC *
        go!: /Драгметаллы_Комиссии_Купить физическое золото
            
    state: 1042S_ЖП
        q!: * @1042S_HC *
        go!: /Документы_Налоговые_Справка 1042S
            
    state: Крипта_ЖП
        q!: * @crypto_HC *
        go!: /Криптовалюты
            
    state: Ускорение_ЖП
        q!: * @uskorenieVivoda_HC *
        go!: /Перевод на оператора
    
        # НЕ ИИС учесть в интенте ИИС
    state: НЕ ИИС_ЖП
        q!: * @notIIS_HC *
        go!: /Перевод на оператора
            
    state: НЕ решен_НЕ помогли_ЖП
        q!: * @neResheno_HC *
        go!: /Перевод на оператора
    
    #Отключаем из за приложения банка        
    state: Дежурный режим_ЖП
        q!: @standbyMode_HC
        go!: /Ошибки заявок_Дежурный режим
            
    state: Праздники_ЖП
        q!: * @prazdniki_HC *
        go!: /Праздники
            
    state: Овернайт_ЖП
        q!: * @overnight_HC *
        go!: /Займ ЦБ
            
    state: Аристократы_ЖП
        q!: * @aristocrats_HC *
        go!: /Аристократы Финам
            
    state: Замок_ЖП
        q!: * @zamok_HC *
        go!: /Ошибки заявок_Нет кнопки
            
    state: Пеня_ЖП
        q!: * @penya_HC *
        go!: /Справка_Начисление пени
            
    state: Форекс_ЖП
        q!: * @forex_HC *
        go!: /Финам Форекс 
            
    state: Бонус_ЖП
        q!: * @bonus_HC *
        go!: /Финам-бонус
            
    state: Pre-IPO_Проверка_ЖП
        q!: * @Pre-IPO_HC *
        go!: /Pre-IPO
            
    state: Дивы_ЖП
        q!: * @dividends_HC *
        go!: /Выплата дохода
            
    state: DMA_ЖП
        q!: * @DMA_HC *
        go!: /Услуги компании_Помощники_Прямой доступ
            
    state: Магазин_ЖП
        q!: * @magazin_HC *
        go!: /Услуги компании_Акции в подарок
                
    state: Структурные облигации_ЖП
        q!: * @strukturnieObligacii_HC *
        go!: /Услуги компании_Готовые решения_Структурные облигации
        
    state: БКлФ_ЖП
        q!: * @BKLF_HC *
        go!: /Депозитарное поручение_Ещё_Перевод активов
            
    state: Госслужащий_ЖП
        q!: * @gossluj_HC *
        go!: /Документы_Налоговые_Справка госслужащего
                
    state: Авторизация_ЖП
        q!: * @authorization_HC *
        go!: /Авторизация
                
    state: Все решено_ЖП
        q!: * @vseResheno_HC *
        go!: /Прощание
                
    state: Отзыв_обратная связь_ЖП
        q!: * @feedback_HC *
        go!: /Отзыв
            
    state: Хорошие оценки_ЖП
        q!: @goodFeedback_HC
        go!: /Оценки
                
    state: Плохие оценки_ЖП
        q!: @badFeedback_HC
        go!: /Перевод на оператора
            
    state: Залоговые инструменты_ЖП
        q!: * @zalogovieInstrumenti_HC *
        go!: /Ограничение ЦБ
            
    state: Comon_ЖП
        q!: * @comon_HC *
        go!: /Comon
                
    state: Голосовая заявка_ЖП
        q!: * @golosovayaZayavka_HC *
        go!: /Голосовой трейдинг
                
    state: Регламент_ЖП
        q!: * @reglament_HC *
        go!: /Документы_Общие_Регламент
                
    state: Обязательства
        q!: * @obyazatelstva_HC *
        go!: /Как закрыть позиции_Закрыть задолженность
            
    state: Демо-счета_ЖП
        q!: * @demo_HC *
        go!: /Демо-счет
                
    state: ФИНАМ_ЖП   
        q!: @finam_HC
        go!: /О компании
            
    state: Учебные центры-вебинары_ЖП   
        q!: * @webinars_HC *
        go!: /Обучение на сайте_Учебный центр

    state: Счет Иностранные биржи_ЖП   
        q!: * @inostrannieBirji_HC *
        go!: /Счет Иностранные биржи
            
    state: Стоп-лосс_ЖП   
        q!: * @stopLoss_HC *
        go!: /Заявки_Типы_Стоп_Тейк_SL
        
    state: Запрет трейдера_ЖП   
        q!: * @zapretTreidera_HC *
        go!: /Ошибки заявок_Запрет трейдера
            
    state: ПГО_ЖП   
        q!: * @PGO_HC *
        go!: /Срочный рынок_Обеспечение_ПГО

    state: IPO_ЖП   
        q!: * @IPO_HC *
        go!: /Корпоративные действия_Размещение
            
    state: QUIKX-WebQUIK_ЖП   
        q!: * @XWebQUIK_HC *
        go!: /ИТС_Другие_QUIK X        
                
    state: TRANSAQ Connector_ЖП   
        q!: * @TRConnector_HC *
        go!: /Стороннее ПО_TRANSAQ Connector
            
    state: MT5_ЖП   
        q!: * @MT5_HC *
        go!: /ИТС_Другие

    state: QUIK_ЖП   
        q!: * @QUIK_HC *
        go!: /ИТС_QUIK
            
    state: TRANSAQ_ЖП   
        q!: * @TRANSAQ_HC *
        go!: /ИТС_TRANSAQ           
                
    state: ИТС_ЖП   
        q!: * @ITS_HC *
        go!: /ИТС
            
    state: КПУР-КСУР_ЖП   
        q!: * @KPUR-KSUR_HC *
        go!: /Маржа_Уровни риска

    state: АИ-скринер_ЖП   
        q!: * @AIscreener_HC *
        go!: /Услуги компании_Помощники_AI-cкринер
            
    state: Сегрегированный-NSR_ЖП   
        q!: * @SegregatedNSR_HC *
        go!: /Сегрегированный    
                
    state: Американский турнир_ЖП   
        q!: * @amerikanskiiTurnir_HC *
        go!: /Американский турнир
            
    state: ИИС_ЖП   
        q!: @IIS_HC
        go!: /ИИС

    state: Нужен человек_ЖП   
        q!: @nuzhenChelovek_HC
        go!: /Перевод на оператора
            
    state: ЛЧИ_ЖП   
        q!: * @LCHI_HC *
        go!: /ЛЧИ        
                
    state: Алерт_ЖП   
        q!: * @alert_HC *
        go!: /ИТС_FinamTrade_Дополнительные функции_Алерты
            
    state: Поток обезличенных сделок_ЖП   
        q!: * @potokObezlichennihSdelok_HC *
        go!: /ИТС_QUIK_Настройки_Поток сделок

    state: Оферта по облигации_ЖП   
        q!: * @offertaPoObligacii_HC *
        go!: /Корпоративные действия_Оферта
            
    state: Замещение облигаций_ЖП   
        q!: * @zamechenieObligacii_HC *
        go!: /Корпоративные действия_Замещение          
                
    state: Стороннее ПО_ЖП   
        q!: * @storonneePO_HC *
        go!: /Стороннее ПО
            
    state: Вывод онлайн 24-7_ЖП   
        q!: * @vivod24-7_HC *
        go!: /Движение ДС_Вывод_24-7

    state: ЭП_ЖП   
        q!: * @EP_HC *
        go!: /Подпись
            
    state: Иностранные облигации_ЖП   
        q!: * @inostrannieObligacii_HC *
        go!: /Иностранные облигации        
                
    # Из за банка отключаем
    # state: Документы об открытии_ЖП   
    #     q!: @openingDocuments_HC
    #     go!: /Документы_Общие_Открытие счета
            
    state: Архив котировок_ЖП   
        q!: * @arhivKotirovok_HC *
        go!: /Экспорт котировок

    state: Обучение на сайте_ЖП   
        q!: * @obuchenieNaSaite_HC *
        go!: /Обучение на сайте
            
    state: Драги_ЖП   
        q!: * @drags_HC *
        go!: /Драгметаллы          
                
    state: Finam Invest_ЖП   
        q!: * @finamInvest_HC *
        go!: /Finam Invest
            
    state: ИПИФ Алгоритм роста_ЖП   
        q!: * @IPIFAlgoritmRosta_HC *
        go!: /ИПИФ «Алгоритм роста»

    state: Банк_ЖП   
        q!: * @bank_HC *
        go!: /Банк
            
    state: Согласие с рисками   
        q!: * @soglasieRisk_HC *
        go!: /Документы_Общие_Согл для торговли иностранными активами           
                
    state: Ставки риска_ЖП   
        q!: * @stavkiRiska_HC *
        go!: /Маржа_Ставки риска
            
    state: Персона холдинга_ЖП   
        q!: * @personaHoldinga_HC *
        go!: /Клиент с такими данными уже существует

    state: Инвест-профиль_ЖП   
        q!: * @investProfile_HC *
        go!: /КВАЛ_Смена профиля
            
    state: СВОП_ЖП   
        q!: @SVOP_HC
        go!: /Валютный рынок_Комиссия СВОП       
                
    state: РЕПО_ЖП   
        q!: @REPO_HC
        go!: /Справка_Сделки РЕПО
            
    state: Я-робот_ЖП   
        q!: * @yarobot_HC *
        a: Я – виртуальный консультант «Финам».
            Быстро помогу в любое время. Какой у вас вопрос?
            Обычно меня спрашивают:
        buttons:
            "Как открыть счет?" -> /Открытие_счета
            "Как пополнить счет?" -> /Движение ДС_Пополнение
            "Как вывести деньги?" -> /Движение ДС_Вывод
            "Как начать инвестировать?" -> /Как начать
            "Заказ справки 2НДФЛ" -> /Документы_Налоговые_2-НДФЛ
            "Выкуп ИЦБ по указу № 844" -> /Ограничение ЦБ_844        

    state: Короткий вопрос_ЖП   
        q!: @korotkiiVopros_HC
        a: Извините, я вас не понимаю.
            Пожалуйста, перефразируйте свой вопрос.
            
    state: Связаться с пресс-службой_ЖП   
        q!: * @press_HC *
        go!: /Пресса
            
    state: Умный старт_ЖП   
        q!: * @umniiStart_HC *
        go!: /Умный старт
            
    state: СБП_ЖП   
        q!: @SBP_HC
        go!: /Движение ДС
            
    state: Закрытие чата_ЖП
        q!: * @closeChat_HC *
        go!: /Закрытие обращения
    
    state: Пропали кнопки_ЖП
        q!: * @propaliKnopki_HC *
        a: Пожалуйста, напишите ваш вопрос снова.
        
    state: Завершение диалога (спасибо)_ЖП
        q!: @spasibo_HC
        go!: /Закрытие обращения
        
    state: Завершение диалога (до свидания)_ЖП
        q!: * @dosvidaniya_HC *
        go!: /Закрытие обращения
        
    state: ИИС3_ЖП
        q!: * @IIS3_HC *
        go!: /ИИС_Третьего типа
        
    state: Кодовое слово_ЖП
        q!: * @kodovoeSlovo_HC *
        go!: /Личный кабинет_Торговый код_Кодовое слово
        
    state: Запрет приобретения на ИИС_ЖП
        q!: * @zapretNaIIS_HC *
        go!: /Ошибки заявок_Запрет на ИИС
    
    state: ЛДВ_ЖП
        q!: * @LDV_HC *
        go!: /Налоги_Налоговые льготы
    
    state: Указ 844_ЖП
        q!: * @844_HC *
        go!: /Ограничение ЦБ_844
    
    state: Рекламные баннеры_ЖП
        q!: * @reklamnieBanneri_HC *
        go!: /Отзыв

    state: Указ_665
        q!: * @665_HC *
        go!: /Ограничение ЦБ_665
        
    state: Сервис Intelinvest_ЖП
        q!: * @Intelinvest_HC *
        go!: /Сервис Intelinvest  
    
    state: Стороннее ПО 2_ЖП
        q!: * storonneePO_2_HC *
        go!: /Стороннее ПО 2

    state: NoMatch
        event!: noMatch
        script:
            $analytics.setMessageLabel("Не распознан 1", "Теги действий");
        a: Возможно я не так вас понял. Пожалуйста, перефразируйте свой вопрос.
        
        state: InnerNoMatch
            event: noMatch
            script:
                $analytics.setMessageLabel("Не распознан 2", "Теги действий");
            a: По данному вопросу вас проконсультирует менеджер.
            go!: /Перевод на оператора

    state: TimeLimit
        event!: timeLimit
        script:
            $analytics.setMessageLabel("TimeLimit", "Теги действий");
        go!: /Перевод на оператора

    state: Комиссии || sessionResultColor = "#15952F"
        intent!: /001 Комиссии
        
        script:
            if ( typeof $parseTree._tipKomissii != "undefined" ){
                $session.tipKomissii = $parseTree._tipKomissii;
                $reactions.transition("/Комиссии_" + $session.tipKomissii.name);
            }
            
        a: Ознакомиться с описанием тарифных планов, брокерских и биржевых комиссий можно на сайте по ссылке: https://www.finam.ru/landings/tariffs/ 
            ✅ Сравнительная таблица с описанием тарифов: https://www.finam.ru/landings/tariff-learn-more/ 
            ✅ Также, полные условия тарифных планов приведены в Приложении № 7 к Регламенту брокерского обслуживания АО «ФИНАМ», с которым можно ознакомиться по ссылке: http://zaoik.finam.ru/broker/regulations 
            ✅ Пожалуйста, выберите тип комиссии:

        buttons:
            "Комиссия биржи/Урегулирование сделок" -> /Комиссии_Биржа
            "Комиссия брокера за сделку" -> /Комиссии_За сделку
            "Комиссия брокера за обслуживание" -> /Комиссии_За обслуживание
            "Другие комиссии" -> /Комиссии_Другие
            "Справка по счету" -> /Справка по счету

    state: Комиссии_Биржа
        
        script:
            if ( typeof $parseTree._tipKomissiiBirja != "undefined" ){
                $session.tipKomissiiBirja = $parseTree._tipKomissiiBirja;
                $reactions.transition("/Комиссии_Биржа_" + $session.tipKomissiiBirja.name);
            }
        
        a: Брокер удерживает и передает биржевую комиссию по завершению торгового дня. В Справке по счету данный пункт отображается как комиссия за урегулирование сделок.
                Фактическое списание комиссии происходит в 23:59 МСК (внутри торгового дня происходит только блокировка необходимой суммы).
                Выберите секцию:
        buttons:
            "Фондовый рынок МБ" -> /Комиссии_Биржа_ФондовыйМБ
            "Срочный рынок МБ FORTS" -> /Комиссии_Биржа_СрочныйМБ
            "Валютный рынок МБ" -> /Комиссии_Биржа_ВалютныйМБ
            "Биржа СПБ" -> /Комиссии_Биржа_СПБ
            "NYSE/NASDAQ" -> /Комиссии_Биржа_NYSENASDAQ
            "Срочный рынок США" -> /Комиссии_Биржа_СрочныйСША
            "Гонконг (HKEX)" -> /Комиссии_Биржа_Гонконг
            "Внебиржевые сделки" -> /Комиссии_Биржа_Внебиржевые
            "Назад" -> /Комиссии
   
    state: Комиссии_За сделку
        
        script:
            if ( typeof $parseTree._tipKomissiiTarif != "undefined" ){
                $session.tipKomissiiTarif = $parseTree._tipKomissiiTarif;
                $reactions.transition("/Комиссии_За сделку_" + $session.tipKomissiiTarif.name);
            }
        
        a: Брокерская комиссия за сделки зависит от выбранного рынка и тарифного плана.
                Списание комиссии происходит в 23:59 МСК.
                Выберите тарифный план:
        buttons:
            "ФриТрейд" -> /Комиссии_За сделку_ФриТрейд
            "Стратег" -> /Комиссии_За сделку_Стратег
            "Инвестор" -> /Комиссии_За сделку_Инвестор
            "Единый Дневной" -> /Комиссии_За сделку_Единый дневной
            "Единый Консультационный" -> /Комиссии_За сделку_Единый Консультационный
            "Другие тарифы" -> /Комиссии_За сделку_Другие
            "Назад" -> /Комиссии

    state: Комиссии_За обслуживание
        
        script:
            if ( typeof $parseTree._tipKomissiiTarif != "undefined" ){
                $session.tipKomissiiTarif = $parseTree._tipKomissiiTarif;
                $reactions.transition("/Комиссии_За обслуживание_" + $session.tipKomissiiTarif.name);
            }
        
        a: Размер комиссии зависит от выбранного тарифного плана, но не превышает оценку счета на дату списания комиссии. При этом сумма списания уменьшается на размер брокерской комиссии, удержанной за операции, совершенные в течение календарного месяца. 
            ✅ Списание происходит в последний день месяца. 
            ❗ Комиссия за обслуживание не удерживается по моносчетам рынка СПБ.
            Выберите тарифный план:
        buttons:
            "ФриТрейд" -> /Комиссии_За обслуживание_ФриТрейд
            "Стратег" -> /Комиссии_За обслуживание_Стратег
            "Инвестор" -> /Комиссии_За обслуживание_Инвестор
            "Единый Дневной" -> /Комиссии_За обслуживание_Единый дневной
            "Единый Консультационный" -> /Комиссии_За обслуживание_Единый Консультационный
            "Другие тарифы" -> /Комиссии_За обслуживание_Другие
            "Назад" -> /Комиссии

    state: Комиссии_Другие
        a: Пожалуйста, выберите тип комиссии:
        buttons:
            "Комиссии за ввод/вывод/хранение средств" -> /Комиссии_Другие_ВводВыводХранение
            "Комиссия за маржинальную торговлю" -> /Комиссии_Другие_Маржинальная
            "Комиссия за депозитарий" -> /Комиссии_Другие_Депозитарий
            "Комиссия за автоследование" -> /Комиссии_Другие_Автоследование
            "Назад" -> /Комиссии

    state: Комиссии_Биржа_ФондовыйМБ
        a: 0,03% от оборота - за урегулирование сделок, заключенных на фондовой секции ММВБ, кроме сделок с облигациями.
                0,015 % от оборота - за урегулирование сделок с облигациями.

    state: Комиссии_Биржа_СрочныйМБ
        a: ✅ За исполнение лимитных заявок, создающих ликвидность рынка (формирующих стакан спроса/предложения), комиссия со стороны биржи не удерживается.
                ✅ За исполнение рыночных заявок, а также лимитных ордеров с мгновенным исполнением, комиссии взимаются согласно тарифам, указанным в спецификации каждого срочного контракта: https://www.moex.com/ru/derivatives/

    state: Комиссии_Биржа_ВалютныйМБ
        a: 1. Валютные пары:
                ✅ За торговлю полными лотами (контракты TOD и TOM) — 0% для мейкеров и 0,0045% от оборота для тейкеров, при этом минимальная комиссия за сделку 50 ₽ (исключение USDRUB, EURRUB – 100 ₽), если заявка на совершение сделки подана объемом менее 50 лотов; если более 50 лотов – минимальная комиссия 0,02 ₽ для мейкеров и 1 ₽ для тейкеров. 
                ✅ Комиссия за сделки СВОП составляет 0,0006% от суммы первой части сделки СВОП, но не менее 1 ₽ за сделку.
                ✅ За торговлю мелкими лотами (контракты _TMS) — 0% для мейкеров и 0,075% от оборота для тейкеров, при этом минимальная комиссия за сделку 1 ₽.
                2. Драгоценные металлы:
                ✅ при покупке серебра 0,006375%, но не менее 1 ₽,
                ✅ при продаже серебра 0,017875%, но не менее 1 ₽,
                ✅ при покупке золота 1 ₽,
                ✅ при продаже золота 0,02%, но не менее 1 ₽.

    state: Комиссии_Биржа_СПБ
        a: ✅ За совершение сделок с российскими и иностранными ценными бумагами (за исключением ценных бумаг гонконгского рынка) - 0,01% от оборота + 0,004 $ за каждую иностранную ценную бумагу.
                ✅ За совершение сделок с ценными бумагами гонконгского рынка - 0,03 % от оборота. 
                – при обороте в рублях РФ рублях – в рублях РФ
                – при обороте в долларах США – в долларах США
                – при обороте в иностранной валюте, отличной от долларов США – в долларах США (производится пересчет по курсу Банка России на дату совершения сделки)

    state: Комиссии_Биржа_NYSENASDAQ
        a: Внешние расходы включают:
                ✅ Клиринговый сбор — 0,0005 $ за акцию, мин. 0,14 $; 
                ✅ NSCC — 0,000175 $ за акцию, мин. 0,01 $; 
                ✅ SEC — 0,00008 % от объема при продаже, мин. 0,01 $;
                ❗ Премаркет и постмаркет — 0,003 $ за акцию, мин. 0,01 $.

    state: Комиссии_Биржа_СрочныйСША
        a: Внешние расходы включают:
                ✅ Клиринговый сбор — 0,0005 $ за акцию, мин. 0,14 $
                ✅ NSCC — 0,000175 $ за акцию, мин. 0,01 $
                ✅ SEC — 0,00008 % от объема при продаже, мин. 0,01 $
                ❗ Премаркет и постмаркет — 0,003 $ за акцию, мин. 0,01 $

    state: Комиссии_Биржа_Гонконг
        a: Внешние расходы включают:
                ✅ Биржевой сбор: 0.1377%, мин. 1 HKD
                ✅ Клиринговый сбор: 0.092%, мин. 40 HKD

    state: Комиссии_Биржа_Внебиржевые
        a: ✅ Внебиржевые сделки (кроме указанных ниже) - 0,118 %, но не менее 1450 ₽ (заявка формируется через менеджера поддержки) 
                ✅ Московская биржа: двусторонние сделки с ЦК, непрерывный аукцион с ЦК, адресные сделки с ЦК — 0,2%, комиссия за урегулирование — 0,03%
                ✅ Сервис торговли заблокированными ИЦБ - 0,8%

    state: Комиссии_За сделку_ФриТрейд
        a: С детальным описанием тарифа можно ознакомиться по ссылке: https://www.finam.ru/documents/commissionrates/unified/freetrade

    state: Комиссии_За сделку_Стратег
        a: С детальным описанием тарифа можно ознакомиться по ссылке: https://www.finam.ru/documents/commissionrates/unified/strategist

    state: Комиссии_За сделку_Инвестор
        a: С детальным описанием тарифа можно ознакомиться по ссылке: https://www.finam.ru/documents/commissionrates/unified/investor

    state: Комиссии_За сделку_Единый дневной
        a: С детальным описанием тарифа можно ознакомиться по ссылке: https://www.finam.ru/documents/commissionrates/unified/daily

    state: Комиссии_За сделку_Единый Консультационный
        a: С детальным описанием тарифа можно ознакомиться по ссылке: https://www.finam.ru/documents/commissionrates/unified/consult

    state: Комиссии_За сделку_Другие
        a: Полные условия тарифных планов приведены в Приложении № 7 к Регламенту брокерского обслуживания «Финам», который доступен для ознакомления по ссылке: https://zaoik.finam.ru/broker/regulations/

    state: Комиссии_За обслуживание_ФриТрейд
        a: Комиссия 0 ₽.

    state: Комиссии_За обслуживание_Стратег
        a: Комиссия 0 ₽.

    state: Комиссии_За обслуживание_Инвестор
        a: Комиссия 200 ₽.
            Если в последний рабочий день месяца оценка счета менее 2000 ₽ — 400 ₽.

    state: Комиссии_За обслуживание_Единый дневной
        a: Комиссия 177 ₽. Если в последний рабочий день месяца оценка счета менее 2000 ₽ — 400 ₽.

    state: Комиссии_За обслуживание_Единый Консультационный
        a: Комиссия 177 ₽. Если в последний рабочий день месяца оценка счета менее 2000 ₽ — 400 ₽.

    state: Комиссии_За обслуживание_Другие
        a: Пожалуйста, выберите тариф:
        buttons:
            "Единый Фиксированный" -> //Комиссии_За обслуживание_Единый дневной
            "Единый Оптимум" -> //Комиссии_За обслуживание_Единый дневной
            "Тест-Драйв" -> /Комиссии_За обслуживание_ФриТрейд
            "Долгосрочный инвестор" -> /Комиссии_За обслуживание_ФриТрейд
            "Стандартный ФОРТС" -> /Комиссии_За обслуживание_ФриТрейд
            "Другие тарифы" -> /Комиссии_За обслуживание_Другие_Другие тарифы
            "Назад" -> /Комиссии_За обслуживание

    state: Комиссии_За обслуживание_Другие_Другие тарифы
        a: Пожалуйста, выберите тариф:
        buttons:
            "Консультационный ФОРТС" -> /Комиссии_За обслуживание_Другие_Другие тарифы_Консультационный ФОРТС
            "Дневной СПБ" -> /Комиссии_За обслуживание_Другие_Другие тарифы_Дневной СПБ
            "Консультационный СПБ" -> /Комиссии_За обслуживание_Другие_Другие тарифы_Консультационный СПБ
            "Назад" -> /Комиссии_За обслуживание_Другие
    #    "Перевод на оператора" -> /Перевод на оператора
    state: Комиссии_За обслуживание_Другие_Другие тарифы_Консультационный ФОРТС
        go!: /Комиссии_За обслуживание_Единый дневной

    state: Комиссии_За обслуживание_Другие_Другие тарифы_Дневной СПБ
        a: Комиссия 4,5 $
            Если в последний рабочий день месяца оценка счета менее 2000 ₽ — комиссии 400 ₽ в эквиваленте USD по курсу ЦБ.

    state: Комиссии_За обслуживание_Другие_Другие тарифы_Консультационный СПБ
        go!: /Комиссии_За обслуживание_Другие_Другие тарифы_Дневной СПБ

    state: Комиссии_Другие_ВводВыводХранение
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Комиссии за ввод средств" -> /Движение ДС_Пополнение
            "Комиссии за вывод средств" -> /Движение ДС_Вывод
            "Комиссии за хранение валюты" -> /Комиссии_Другие_ВводВыводХранение_Хранение

    state: Комиссии_Другие_ВводВыводХранение_Хранение
    #под вопросом
    state: Комиссии_Другие_Маржинальная
        a: Расчет и удержание комиссии за займ производится ежедневно из расчета 365 дней.
            Размер комиссии зависит от выбранного тарифного плана:
        buttons:
            "ФриТрейд" -> /Комиссии_Другие_Маржинальная_ФриТрейд
            "Стратег" -> /Комиссии_Другие_Маржинальная_Стратег
            "Инвестор" -> /Комиссии_Другие_Маржинальная_Стратег
            "Единый дневной" -> /Комиссии_Другие_Маржинальная_ЕдиныйДневной
            "Единый Консультационный" -> /Комиссии_Другие_Маржинальная_ЕдиныйКонсульт
            "Другие" -> /Комиссии_Другие_Маржинальная_Другие
            
    state: Комиссии_Другие_Маржинальная_ФриТрейд
        a: За маржинальные позиции, открытые и закрытые внутри одной торговой сессии, комиссия не удерживается, так как обязательства не переносятся на следующую торговую сессию.
                ✅ Комиссии за займ по тарифу «ФриТрейд 2.0»: 
                — в рублях — ключевая ставка ЦБ РФ + 15,5%
                — в долларах — 26% 
                — в гонконгских долларах — 50%
                — в иной валюте — 9% (при сумме займа до 25 000 ед.)
                — ценных бумаг (Московская биржа) — 13%
                — ценных бумаг (СПБ Биржа) — 14%
                — ценных бумаг (иностранные биржи) — 13,5%
                ✅ Отличительные условия по архивному тарифу «ФриТрейд»: 
                — в рублях — ключевая ставка ЦБ РФ + 16,5% (при сумме займа до 800 000 ₽)
                — ценных бумаг (Московская биржа) — 22%
                — в иной валюте — 15,5%

    state: Комиссии_Другие_Маржинальная_Стратег
        a: За маржинальные позиции, открытые и закрытые внутри одной торговой сессии, комиссия за займ не удерживается, так как обязательства не переносятся на следующую торговую сессию.
            Комиссии за займ:
                ✅ в рублях — ключевая ставка ЦБ РФ + 8%; 
                ✅ в долларах — 15% (при сумме займа до 50000 $); 
                ✅ в гонконгских долларах — 50%;
                ✅ в иной валюте — 9% (при сумме займа до 25000 $); 
                ✅ ценных бумаг (Московская биржа) — 13%; 
                ✅ ценных бумаг (СПБ Биржа) — 14%; 
                ✅ ценных бумаг (иностранные биржи) — 8%.

    state: Комиссии_Другие_Маржинальная_ЕдиныйДневной
        a: За маржинальные позиции, открытые и закрытые внутри одной торговой сессии, комиссия за займ не удерживается, так как обязательства не переносятся на следующую торговую сессию.
            Комиссии за займ:
                ✅ в рублях — ключевая ставка ЦБ РФ + 9,5% (при сумме займа до 800 000 ₽); 
                ✅ в долларах — 15%; 
                ✅ в гонконгских долларах — 50%;
                ✅ в иной валюте — 9%; 
                ✅ ценных бумаг (Московская биржа) — 13%; 
                ✅ ценных бумаг (СПБ Биржа) — 14%; 
                ✅ ценных бумаг (иностранные биржи) — 8%.

    state: Комиссии_Другие_Маржинальная_ЕдиныйКонсульт
        a: За маржинальные позиции, открытые и закрытые внутри одной торговой сессии, комиссия за займ не удерживается, так как обязательства не переносятся на следующую торговую сессию.
            Комиссии за займ:
                ✅ в рублях — ключевая ставка ЦБ РФ + 16%; 
                ✅ в долларах — 15%; 
                ✅ в гонконгских долларах — 50%;
                ✅ в иной валюте — 9%; 
                ✅ ценных бумаг (Московская биржа) — 28,55%; 
                ✅ ценных бумаг (СПБ Биржа) — 14%; 
                ✅ ценных бумаг (иностранные биржи) — 8%.

    state: Комиссии_Другие_Маржинальная_Другие
        a: За маржинальные позиции, открытые и закрытые внутри одной торговой сессии, комиссия за займ не удерживается, так как обязательства не переносятся на следующую торговую сессию. Подробная информация о комиссиях на всех тарифах: http://zaoik.finam.ru/documents/commissionrates/otheroperations

    state: Комиссии_Другие_Депозитарий
        a: Депозитарный тариф зависит от даты открытия счета и даты последней смены тарифа по счету.
            По счетам, открытым после 26.11.2020, а также по счетам, с измененными самостоятельно после указанной даты тарифными планами, применяется Тарифный план 2. 
            Выберите свой тариф:
        buttons:
            "Тарифный план 1" -> /Комиссии_Другие_Депозитарий_Тариф1
            "Тарифный план 2" -> /Комиссии_Другие_Депозитарий_Тариф2
            "Назад" -> /Комиссии_Другие

    state: Комиссии_Другие_Автоследование
        a: Чтобы узнать тариф и комиссии за использование сервиса:
            ✅ выберите из списка интересующую вас [стратегию|https://www.comon.ru/strategies/]
            ✅ откройте вкладку «показатели» и выберите название тарифа в разделе «тариф автоследования» 
            ✅ [Стоимость сервиса «Финам Автоследование» по каждому тарифу|https://docs.comon.ru/general-information/tariffs/]
            ❗ Списание комиссии по тарифам, рассчитываемым от суммы чистых активов (СЧА) – ежедневно, списание комиссии по тарифам, рассчитываемым от инвестиционного дохода (ИД) может быть раз в месяц, раз в квартал или раз в год, в зависимости от тарифа, а также при пополнении счёта или выводе средств.
            ✅ В справке по счету комиссия отображена, как «Вознаграждение компании согласно п. 16 Регламента брокерского обслуживания»

    state: Комиссии_Другие_Депозитарий_Тариф1
        a: 177 ₽ при наличии движения средств/активов на счете в течение календарного месяца.

    state: Комиссии_Другие_Депозитарий_Тариф2
        a: Комиссия 0 ₽.

    state: Открытие_закрытие счетов || sessionResultColor = "#15952F"
        intent: /002 Открытие_закрытие счетов
        
        script:
            if ( typeof $parseTree._open_close != "undefined" ){
                $session.open_close = $parseTree._open_close;
                $reactions.transition("/" + $session.open_close.name + "_счета");
            }
            
        a: Выберите вариант действия:
        buttons:
            "Открытие брокерского счета" -> /Открытие_счета
            "Ошибки при открытии счета" -> /Ошибки при открытии
            "Типы брокерских счетов" -> /Типы счетов
            "Мой брокерский счет" -> /Мой брокерский счет
            "Закрытие счета" -> /Закрытие_счета
            "Другие счета" -> /Другие счета

    state: Открытие_счета
        a: Дистанционное открытие первичного счета доступно физическим лицам гражданам РФ и гражданам дружественных государств (Беларуси, Казахстана, Азербайджана, Армении, Молдовы, Таджикистана, Туркменистана, Узбекистана).
                Новый счет будет доступен для торговли через несколько часов после подписания документов об открытии. 
                Выберите интересующий вас способ открытия счета:
        buttons:
            "В офисе компании лично" -> /Открытие_счета лично
            "Дистанционно" -> /Открытие_счета дистанционно
            "Открытие счета до 18 лет" -> /Открытие_счета до 18
            "Назад" -> /Открытие_закрытие счетов

    state: Открытие_счета лично
        a: Вам понадобится мобильный телефон и документ удостоверяющий личность.
                Перечень документов для иностранных граждан представлен на сайте: https://www.finam.ru/services/OpenAccount0000A/ 
                Перед посещением офиса предварительно согласуйте время и цель визита с менеджером. 
                Время работы и адреса офисов: https://www.finam.ru/about/contacts

    state: Открытие_счета дистанционно
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Открытие счета для новых клиентов" -> /Открытие_счета для новых клиентов
            "Открытие счета для действующих клиентов" -> /Открытие_счета для действующих клиентов
            "Открытие счета с ФриТрейд" -> /Открытие_счета с ФриТрейд
            "Перевод на оператора" -> /Перевод на оператора
            "Назад" -> /Открытие_счета

    state: Открытие_счета до 18
        a: Выберите вариант действия:
        buttons:
            "Малолетним лицам до 14 лет" -> /Открытие_счета до 18_до 14
            "Несовершеннолетним лицам с 14 до 18 лет" -> /Открытие_счета до 18_14-18
            "Перевод на оператора" -> /Перевод на оператора
            "Назад" -> /Открытие_счета
    
    state: Открытие_счета до 18_до 14
        a: Открытие брокерских договоров лицам до 14 лет возможно только при вступлении в права наследования.
                Перечень необходимых документов: https://www.finam.ru/services/OpenAccount0000A/
        buttons:
            "Перевод на оператора" -> /Перевод на оператора        

    state: Открытие_счета до 18_14-18
        a: Несовершеннолетним гражданам открытие брокерского счета доступно только при личном посещении офиса с официальным представителем (родителем, опекуном). В присутствии сотрудника компании родителем/опекуном должно быть составлено разрешение на открытие брокерского счета и совершение торговых операций.
                Перечень необходимых документов: https://www.finam.ru/services/OpenAccount0000A/
        buttons:
            "Перевод на оператора" -> /Перевод на оператора        

    state: Открытие_счета с ФриТрейд
        a: С 16 июня 2023 года вы можете подключить тариф «ФриТрейд 2.0» при открытии своего первого брокерского счета в «Финам».
                ✅ Срок действия тарифного плана — 30 дней,
                ✅ 0 ₽ — абонентская плата за месяц,
                ✅ По истечению 30 дней с момента открытия счета с «ФриТрейд 2.0» предоставляется тариф «Стратег».
                Открыть счет и узнать подробнее можно по ссылке: https://www.finam.ru/landings/freetrade/ 
                ❗ Архивный тариф «ФриТрейд» недоступен для подключения. Владельцы такого тарифа сохраняют условия до момента смены тарифного плана. Условия «ФриТрейд» можно изучить в регламенте брокерского обслуживания.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Открытие_счета для новых клиентов
        a: Дистанционное открытие счета доступно совершеннолетним гражданам Российской Федерации, а также гражданам дружественных государств (Беларуси, Казахстана, Азербайджана, Армении, Молдовы, Таджикистана, Туркменистана, Узбекистана).
                Открыть счет можно по ссылке: https://account.finam.ru/Registration
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Открытие_счета для действующих клиентов
        a: Вы можете иметь неограниченное количество действующих брокерских счетов.
                ✅ Открыть дополнительный брокерский счет «Единая денежная позиция» можно дистанционно в личном кабинете по ссылке: https://lk.finam.ru/open/brokerage 
                ❗ Для открытия моносчетов воспользуйтесь этой ссылкой: https://edox.finam.ru/NewAccount/Product?ContextId=337373a9-7941-42da-98c5-aeed1f208dd6 
                ✅ Ваши брокерские счета полностью независимы, вы можете использовать по ним разные тарифы и торговые системы.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Ошибки при открытии
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Не загружается паспорт" -> /Не загружается паспорт
            "Отсутствует выбор адреса" -> /Отсутствует выбор адреса
            "Клиент с такими данными существует" -> /Клиент с такими данными уже существует
            "Как указать банк при открытии?" -> /Как указать банк при открытии
            "Другая ошибка" -> /Другая ошибка
            "Назад" -> /Открытие_закрытие счетов

    state: Не загружается паспорт
        a: Если вам не удается загрузить паспорт при заполнении анкеты, пожалуйста, используйте следующие рекомендации:
                ✅ Использовать другой браузер
                ✅ Использовать режим инкогнито в браузере
                ✅ Использовать VPN-сервисы с локацией на РФ (Если вы находитесь за границей РФ)
                ✅ Очистить кэш в вашем браузере (могут быть потеряны персональные настройки)
                ✅ Использовать другую версию личного кабинета: https://lk.finam.ru/ или https://edox.finam.ru/ 
                ❗ Если рекомендации не помогли, для скорейшего решения вопроса, пожалуйста, нажмите кнопку «Перевод на оператора» и направьте в чат следующие данные:
                ✅ ваши ФИО
                ✅ адрес электронной почты, используемый для регистрации
        buttons:
            "Другая ошибка" -> /Ошибки при открытии
            "Перевод на оператора" -> /Перевод на оператора

    state: Отсутствует выбор адреса
        a: 1. Если не удается внести почтовый адрес при заполнении анкеты, пожалуйста, убедитесь, что названия объектов актуальны, вносите адрес до номера квартиры полностью, далее выберите вариант из выпадающего списка.
                2. Если адрес в анкете указан ошибочно, и нет возможности его изменить в рамках анкеты, то вы сможете изменить его после открытия счета в разделе «Изменение анкетных данных» по ссылке: https://edox.finam.ru/Client/EditInfo 
                ❗ Если рекомендации не помогли, для скорейшего решения вопроса, пожалуйста, нажмите кнопку «Перевод на оператора» и направьте в чат следующие данные:
                ✅ ваши ФИО
                ✅ адрес электронной почты, используемый для регистрации       
        buttons:
            "Другая ошибка" -> /Ошибки при открытии
            "Перевод на оператора" -> /Перевод на оператора        

    state: Клиент с такими данными уже существует
        a: Уведомление о том, что вы являетесь персоной холдинга «Финам» или «Клиент с указанными данными уже существует» говорит о том, что вы когда-либо уже регистрировались в компании «Финам», и повторная регистрация недоступна.  
            ✅ Вы можете [войти в личный кабинет|https://lk.finam.ru/], при необходимости восстановить данные для входа - нажать «Забыли логин и пароль?» и продолжить открытие счёта уже из вашего личного кабинета. 
            ✅ Также для восстановления доступа и открытия счёта можно обратиться в ближайший удобный для вас [офис компании|https://www.finam.ru/about/contacts] с документом, удостоверяющим личность.
        buttons:
            "Восстановить доступ в личный кабинет" -> /Личный кабинет_Восстановить доступ
            "Другая ошибка" -> /Ошибки при открытии
            "Перевод на оператора" -> /Перевод на оператора

    state: Как указать банк при открытии
        a: Необходимо ввести запрошенную информацию о том банке (российском или зарубежном), через который будет впоследствии совершаться ввод денежных средств на открываемый счет и вывод денежных средств с открываемого счета (см. выделенное на странице предупреждение).
                ❗ Ввод и вывод денежных средств будет возможен только по реквизитам счета Банка, указанного здесь!
                ❗ Полные реквизиты банковского счета клиента не нужны, только наименование Банка.
                После ввода данных на странице нужно нажать на кнопку «Продолжить» и перейти к следующему шагу.
        buttons:
            "Другая ошибка" -> /Ошибки при открытии
            "Перевод на оператора" -> /Перевод на оператора

    state: Другая ошибка
        a: Общие рекомендации при возникновении технических ограничений при открытии счёта: 
            ✅ Использовать другой браузер
            ✅ Использовать режим инкогнито в браузере
            ✅ Использовать VPN-сервисы с локацией на РФ (Если вы находитесь за границей РФ)
            ✅ Очистить кэш в вашем браузере (могут быть потеряны персональные настройки)
            ✅ Использовать другую версию личного кабинета: https://lk.finam.ru/ или https://edox.finam.ru/ 
            ❗ Если рекомендации не помогли, для скорейшего решения вопроса, пожалуйста, нажмите кнопку «Перевод на оператора» и направьте в чат следующие данные:
            ✅ ваши ФИО
            ✅ адрес электронной почты, используемый для регистрации
        buttons:
            "Назад" -> /Ошибки при открытии
            "Перевод на оператора" -> /Перевод на оператора

    state: Мой брокерский счет
        a: ✅ Перечень действующих счетов доступен в личном кабинете: https://lk.finam.ru/
            После авторизации и выбора счета можно перейти в раздел «детали» и проверить дату открытия брокерского счета, актуальный тариф и тип счёта.
            ✅ Если счет ИИС переведен от другого брокера, первичную дату открытия можно уточнить у менеджера «Финам».
            ✅ Историю движения средств на брокерском счете можно посмотреть в справке по счету по ссылке: https://lk.finam.ru/reports/tax

    state: Закрытие_счета
        a: Поручение на расторжение брокерского договора можно подать только по пустым счетам (на них не должно быть активов и задолженностей).
            ✅ Сформировать поручение можно самостоятельно в личном кабинете по ссылке: https://edox.finam.ru/orders/contractAbrogation/Default.aspx 
            Договор будет расторгнут на 5-й рабочий день с момента подписания заявления.
            ❗ Для счетов ИИС отдельная процедура расторжения, рекомендуем ознакомиться подробнее.
            ❗ В рамках брокерского договора может быть несколько счетов. При расторжении все они будут закрыты.
            ❗ Предварительно рекомендуем обсудить причины расторжения договора с менеджером компании в целях улучшения сервиса и возможного решения вашей проблемы.
        buttons:
            "Расторжение ИИС" -> /ИИС_Еще_Расторжение ИИС
            "Перевод на оператора" -> /Перевод на оператора

    state: Другие счета
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Открытие счета в банке" -> /Банк_Банковский счет_Как открыть
            "Открытие счета Форекс" -> /Финам Форекс
            "Открытие учебного счета" -> /Открытие учебного счета
            "Назад" -> /Открытие_закрытие счетов

    state: Открытие учебного счета
        a: Выберите интересующую вас торговую систему:
        buttons:
            "TRANSAQ" -> /Открытие учебного счета_TRANSAQ
            "FinamTrade" -> /Открытие учебного счета_TRANSAQ
            "QUIK" -> /Открытие учебного счета_QUIK
            "MetaTrader 5" -> /Открытие учебного счета_MT5
            "TRANSAQ Connector" -> /Открытие учебного счета_TRConnector
            "Назад" -> /Открытие_закрытие счетов
    
    state: Открытие учебного счета_TRANSAQ
        a: Сформируйте заявку на открытие учебного счета по ссылке: https://www.finam.ru/landings/demoaccount/
            ✅ Нажмите на кнопку «Открыть демо-счет» и заполните форму заявки.
            ✅ После подтверждения заявки на вашу электронную почту придет письмо с логином, паролем и ссылкой на загрузку торговой системы.

    state: Открытие учебного счета_QUIK
        a: Сформируйте заявку на открытие учебного счета по ссылке: https://www.finam.ru/howtotrade/demos00006/
            ✅ После подтверждения заявки на вашу электронную почту придет письмо с логином, паролем и ссылкой на загрузку торговой системы.
            ❗ К дистрибутиву «привязан» логин/пароль от учебного счета. Для использования ранее установленной программы необходимо перенести файлы с ключами.

    state: Открытие учебного счета_MT5
        a: На текущий момент открытие демо-счета в MetaTrader 5 через «Финам» невозможно. Вы можете подключить демо-счет через сайт разработчика.

    state: Открытие учебного счета_TRConnector
        a: Сформируйте заявку на открытие учебного демо-счета по ссылке: https://www.finam.ru/howtotrade/tconnector00002/?program=Transaq%20Connector
            ✅ После подтверждения заявки на вашу электронную почту придет письмо с логином и паролем.
            ❗ Загрузка дистрибутива не требуется. Для подключения к стороннему ПО достаточно указать логин/пароль от сервера.

    state: Выбор_смена тарифа || sessionResultColor = "#15952F"
        intent!: /003 Выбор_смена тарифа
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Сравнение тарифов" -> /Выбор_смена тарифа_Сравнение
            "Как изменить тариф" -> /Выбор_смена тарифа_Смена
            "Мой тариф по счету" -> /Выбор_смена тарифа_Мой тариф
            "Как получить тариф ФриТрейд" -> /Открытие_счета с ФриТрейд
            "Описание тарифных планов" -> /Комиссии_За сделку
           
    state: Выбор_смена тарифа_Сравнение
        a: Сравнительная таблица пяти наиболее популярных тарифов у клиентов «Финам»: https://www.finam.ru/landings/tariff-learn-more
            Выбирая тариф, учитывайте количество и объем сделок, которые планируете совершать, а также стоимость обслуживания счета. Подобрать и подключить оптимальный тариф поможет менеджер.

    state: Выбор_смена тарифа_Смена
        a: Сменить тариф – легко!
            Подайте поручение в личном кабинете и выберите из списка нужный тариф: https://lk.finam.ru/details  
            Подключение тарифа «ФриТрейд» доступно только при первичном открытии брокерского счета в «Финам».  
            ❗ Действие нового тарифа начинается со следующего рабочего дня после подписания заявления на смену тарифа. Количество заявок на смену тарифа неограниченно. Действующим устанавливается тариф из заявки, последней по времени подписания.

    state: Выбор_смена тарифа_Мой тариф
        a: Текущий тарифный план отображается в личном кабинете, в разделе [«Детали по счету»|https://lk.finam.ru/details]

    state: Типы счетов || sessionResultColor = "#15952F"
        intent!: /004 Типы счетов
        
        # script:
        #     if ( typeof $parseTree._account_type != "undefined" ){
        #         $session.account_type = $parseTree._account_type;
        #         $reactions.transition("/Типы счетов_" + $session.account_type.name);
        #     }
                
        a: Перечень ваших действующих счетов с наименованием доступен в личном кабинете: https://lk.finam.ru/
            В «Финам» доступны следующие типы счетов:
        buttons:
            "Единый счет (ЕДП)" -> /Типы счетов_Единый счет (ЕДП)
            "Раздельные моносчета" -> /Типы счетов_Раздельные моносчета
            "Индивидуальный инвестиционный счет (ИИС)" -> /Типы счетов_ИИС
            "Мои счета" -> /Мой брокерский счет
            "Ещё" -> /Типы счетов_еще

    state: Типы счетов_Единый счет (ЕДП)
        a: «Единая денежная позиция», «Единый счет» — универсальный счет для торговли на российских и иностранных биржах.
                Предоставляется доступ к:
                — Московской бирже (фондовый, срочный, валютный рынки)
                — Бирже СПБ (российские ЦБ) 
                — NYSE/NASDAQ (фондовый рынок)
                — SEHK (Гонконг, фондовый рынок)
                На ЕДП также доступна торговля опционами на акции РФ через ИТС QUIK. Доступ к американским опционам на СВОЕ (Чикаго) реализован только в рамках открытых договоров до 15.08.2022. 
                По счетам ЕДП, открытым с 15.08.2022 по 13.02.2023, доступ к иностранным биржам не предоставляется.
        buttons:
            "Открыть счет" -> /Открытие_счета

    state: Типы счетов_Раздельные моносчета
        a: ✅ В рамках «Моносчета» открывается договор с нижеперечисленными раздельными счетами:
                — фондовый рынок Московской биржи
                — срочный рынок Московской биржи
                — валютный рынок Московской биржи
                — рынок ценных бумаг СПБ Биржи
                ✅ По каждому клиентскому счету проставляется отдельный тарифный план, который клиент может изменять через личный кабинет. Перевод денежных средств между счетами возможен через личный кабинет. Средства одного счета не могут быть обеспечением по другим счетам, что может увеличить затраты за использование заемных средств.
        buttons:
            "Открыть счет" -> /Открытие_счета
    
    state: Типы счетов_ИИС
        a: ИИС, или индивидуальный инвестиционный счет — это счет для покупки акций, облигаций, валюты и других инструментов на бирже с возможностью ежегодно получать налоговую льготу от государства.
                ИИС можно открыть в виде:
                ✅ Единого счета — универсальный счет для торговли на российских биржах. На ЕДП также доступна торговля опционами на акции РФ через ИТС QUIK. В рамках Единого счета на весь договор проставляется один тарифный план.
                ✅ Моносчетов — в рамках одного договора открываются раздельные счета (фондовый, срочный, валютный рынки Московской биржи и фондовый рынок СПБ Биржи). По каждому счету проставляется отдельный тарифный план. Средства одного счета не могут быть обеспечением по другим счетам, что может увеличить затраты за использование заемных средств.
        buttons:
            "Открыть счет ИИС" -> /ИИС_Открытие ИИС
            
    state: Типы счетов_еще
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Сегрегированный Global" -> /Сегрегированный
            "US Market Options" -> /Типы счетов_US Market options
            "Счет Иностранные биржи" -> /Счет Иностранные биржи
            "Назад" -> /Типы счетов

    state: Типы счетов_US Market options
        a: Пакет US Market Options — счет для торговли на иностранных биржах, в рамках данного счета доступна торговля акциями и опционами на американские акции на торговых площадках CBOE, NYSE, NASDAQ.
                ✅ Счет доступен только для квалифицированных инвесторов.
                ✅ Открыть счет можно в личном кабинете:
                1. перейти по ссылке: https://edox.finam.ru/NewAccount/Product?ContextId=c67e0f9d-0adf-4ec9-8b1e-4d7dbe8342c3
                2. выбрать «Брокерская компания» → «Счет Иностранные рынки» → «пакет US Market Options»
                ✅ Для торговли по счету доступны торговые системы FinamTrade, TRANSAQ US
                ✅ Тарифный план по счету: «Единый Дневной Options»
                Подробнее: https://broker.finam.ru/landings/usaoptions/
        buttons:
            "Терминал TRANSAQ US" -> /ИТС_TRANSAQ
            "Тариф Дневной Options" -> /Срочный рынок_Ещё_Комиссия_Единый Options
            "Назад" -> /Типы счетов_еще

    state: ИИС || sessionResultColor = "#15952F"
        intent!: /005 ИИС
        
        script:
            if (typeof $parseTree._iis_type != "undefined"){
                $session.iis_type = $parseTree._iis_type;
                $reactions.transition("/ИИС_" + $session.iis_type.name);
            }
            
        a: Индивидуальный инвестиционный счет (ИИС) — счет для инвестиций на бирже с возможностью ежегодно получать налоговую льготу от государства.
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Открытие ИИС" -> /ИИС_Открытие ИИС
            "Пополнение ИИС" -> /ИИС_Пополнение ИИС
            "Мой ИИС" -> /ИИС_Мой ИИС
            "Налоговый вычет ИИС" -> /ИИС_Налоговый вычет ИИС
            "ИИС третьего типа" -> /ИИС_Третьего типа
            "Доступные инструменты" -> /ИИС_Доступные инструменты
            "Ещё" -> /ИИС_Еще

    state: ИИС_Открытие ИИС
        a: Открытие индивидуального инвестиционного счета доступно новым и действующим клиентам. Выберите нужный вариант:
        buttons:
            "Открытие для новых клиентов" -> /ИИС_Открытие ИИС_Открытие для новых клиентов
            "Открытие для действующих клиентов" -> /ИИС_Открытие ИИС_Открытие для действующих клиентов
            "Назад" -> /ИИС

    state: ИИС_Пополнение ИИС
        a: ❗ При оформлении вычета налоговая имеет право запросить платежное поручение с подтверждением внесения средств на ИИС. Не рекомендуется переводить средства с брокерских счетов, которые были пополнены с банковской карты и со счетов третьих лиц для исключения проблем оформления вычетов.
                ✅ На ИИС вы можете переводить только рубли. Поручение на перевод можно сформировать в [личном кабинете|https://lk.finam.ru/deposit/finam]
                ✅ Минимальный порог пополнения отсутствует. Для инвестиционных счетов, открытых до 31 декабря 2023 года, доступно пополнение до 1000000 ₽ в год. У ИИС нового типа ограничений на пополнение нет.
                ✅ ИИС можно пополнить тремя способами:
        buttons:
            "Наличными в кассе представительства" -> /ИИС_Пополнение ИИС_Наличными
            "По реквизитам" -> /ИИС_Пополнение ИИС_По реквизитам
            "Через СБП (с помощью QR-кода)" -> /Движение ДС_Пополнение_СБП
            "Назад" -> /ИИС

    state: ИИС_Мой ИИС
        a: ✅ Перечень действующих счетов доступен в личном кабинете: https://lk.finam.ru/
            После авторизации и выбора счета с названием КЛФ-ИИС****** можно перейти в раздел «детали» и проверить дату открытия договора ИИС и актуальный тариф.
            ✅ Если счет ИИС переведен от другого брокера, первичную дату открытия можно уточнить у менеджера «Финам».
            ✅ Историю пополнения договора ИИС Вы можете посмотреть в справке по счету по ссылке https://lk.finam.ru/reports/tax или уточнить у менеджера «Финам».

    state: ИИС_Налоговый вычет ИИС
        a: При оформлении вычета налоговая имеет право запросить платежное поручение с подтверждением внесения средств на ИИС.
                Получателем налогового вычета и отправителем средств на ИИС должен являться владелец этого счета. 
                Выберите тип налогового вычета, который хотите получить:
        buttons:
            "Вычет в размере внесенной суммы средств на счет ИИС (Вычет А)" -> /ИИС_Налоговый вычет ИИС_A
            "Вычет в размере финансового результата по счету ИИС (Вычет Б)" -> /ИИС_Налоговый вычет ИИС_Б
            "Назад" -> /ИИС

    state: ИИС_Доступные инструменты
        a: ✅ На счетах ИИС доступны операции с инструментами российских компаний на Московской бирже: 
                — акции, облигации, паи ПИФов на фондовой секции
                — на срочной секции доступны фьючерсы и опционы (опционы на фьючерсы только в рамках пакета моносчетов)
                — валютная секция биржи
                ✅ На СПБ Бирже доступно закрытие позиций по бумагам эмитентов РФ
                ❗ В рамках ИИС недоступно приобретение:
                - ценных бумаг иностранных эмитентов (ИЦБ)
                - ценных бумаг, эмитентами которых являются иностранные государства, таких как CIAN, ETLN, FIXP, GLTR, HHRU, MDMG, OKEY, OZON, POLY, AGRO, FIVE и др.
                - сделки РЕПО с вышеуказанными инструментами
                - выход на поставку ИЦБ по поставочным производным финансовым инструментам
                ✅ На данный момент доступны поддержание (хранение) и закрытие позиций по ИЦБ
            
    state: ИИС_Еще
        a: Выберите вариант действия:
        buttons:
            "Комиссии на ИИС" -> /ИИС_Еще_Комиссии ИИС
            "Расторжение ИИС" -> /ИИС_Еще_Расторжение ИИС
            "Перевод ИИС с сохранением срока" -> /ИИС_Еще_Перевод ИИС
            "Назад" -> /ИИС

    state: ИИС_Открытие ИИС_Открытие для новых клиентов
        a: Для дистанционного открытия ИИС понадобится только действующий паспорт совершеннолетнего гражданина РФ: https://www.finam.ru/open/order/iis/
            ✅ Если вы хотите открыть ИИС с тарифом «ФриТрейд», переходите по ссылке: https://www.finam.ru/landings/freetrade-new  
            ✅ Также вы можете открыть ИИС лично посетив ближайший офис компании.

    state: ИИС_Открытие ИИС_Открытие для действующих клиентов
        a: Для действующих клиентов компании открытие ИИС доступно на главной странице личного кабинета: https://lk.finam.ru/open/brokerage
            ❗ Для работы с опционами необходимо открыть брокерский договор с отдельными счетами (пакет моносчетов).

    state: ИИС_Пополнение ИИС_Наличными
        a: Для пополнения счета наличными, обратитесь в офис компании и воспользуйтесь услугами кассы.
                Адреса офисов: https://www.finam.ru/about/contacts

    state: ИИС_Пополнение ИИС_По реквизитам
        a: Вы можете пополнить ИИС переводом денежных средств по банковским реквизитам счета, указанным в личном кабинете: https://lk.finam.ru/deposit/bank/requisites
                За данную операцию «Финам» не взимает комиссию, однако возможна комиссия со стороны банка-отправителя. 
                ❗ Пополнение ИИС с карты недоступно.

    state: ИИС_Налоговый вычет ИИС_A
        a: Максимальная сумма для вычета по типу «А», за календарный год составляет 400000 ₽. В зависимости от ставки налога на ваш доход, государство вернет вам 13% или 15% от той суммы, которую вы внесли на ИИС в отчетном году. Таким образом, максимальная сумма налога, подлежащая возврату, составит до 52000 ₽ или до 60000 ₽ соответственно.
                ✅ С 2021 года вычет по типу «А», можно оформлять в упрощенном порядке. Для подачи заявления за 2021 год нужно обратиться к менеджеру.
                ✅ Скачать пакет документов для самостоятельной подачи или подать заявку (предоставить сведения о ИИС) для получения вычета в упрощенном порядке, можно в личном кабинете по ссылке: https://lk.finam.ru/reports/tax 
                ✅ После принятия заявления на получение вычета в упрощенном порядке ФНС обрабатывает полученные данные и отправляет уведомление в личный кабинет налогоплательщика (от 2-х до 20 дней). ФНС сформирует для вас предварительную декларацию, которую можно подписать также в личном кабинете налогоплательщика. Срок камеральной проверки после подписания декларации - один месяц.
                ❗ Если в личном кабинете налогоплательщика пришел отказ по упрощенной процедуре, а также при оформлении вычета по стандартной процедуре за более ранние периоды, вам потребуется собрать следующие документы и обратиться в налоговую:
                ✅ Платежное поручение об отправке денежных средств на ИИС
                ✅ Пакет документов об открытии счета и брокерский отчет можно скачать в личном кабинете по ссылке: https://lk.finam.ru/reports/tax в разделе «Пакет документов для налогового вычета по ИИС». Для заказа пакета документов в бумажном варианте необходимо обратиться к менеджеру компании.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            
    state: ИИС_Налоговый вычет ИИС_Б
        a: Максимальная сумма для вычета по типу «Б» равна доходу, полученному от торговых операций на договоре ИИС. Данная инвестиционная прибыль при вычете по типу «Б» налогом не облагается. Претендовать на вычет можно по истечению 3х лет с открытия ИИС.
                ✅ Скачать пакет документов для самостоятельной подачи или подать заявку (предоставить сведения о ИИС в ФНС) для получения вычета по типу «Б» в упрощенном порядке, можно в личном кабинете по ссылке: https://lk.finam.ru/reports/tax 
                ✅ После подачи заявления в течение двух рабочих дней, ожидайте новый статус заявления - «Принято к исполнению».
                После получение данного статуса, в течение 30 дней нужно вывести средства с ИИС и закрыть его. Доход, полученный на ИИС, не будет облагаться налогом.
                ✅ Если в течении 30 дней после подтверждения заявления со стороны ФНС счет не будет расторгнут, необходимо подать заявление повторно.
                ✅ Если на момент оформления вычета счет расторгнут, его можно оформить только после завершения календарного года, обратившись в налоговую. Для этого потребуются:
                1. Пакет документов об открытии счета и брокерский отчет. В электронном виде их можно заказать в личном кабинете по ссылке: https://lk.finam.ru/reports/tax 
                Пакет будет подготовлен с заверением и выгружен в личном кабинете в разделе «Журнал поручений» по ссылке: https://lk.finam.ru/reports/documents 
                Для заказа пакета документов в бумажном варианте необходимо обратиться к менеджеру компании.
                2. Справка 2-НДФЛ. Заказать ее можно в личном кабинете по ссылке: https://lk.finam.ru/reports/tax в разделе «Налоги».
                Справка будет подготовлена с заверением и выгружена в личном кабинете в разделе «Журнал поручений» по ссылке https://lk.finam.ru/reports/documents 
                Способ получения документа можно выбрать при оформлении заявки.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
    
    state: ИИС_Еще_Комиссии ИИС
        a: Для счетов ИИС не применяются отдельные тарифы. Условия обслуживания идентичны обычным брокерским счетам.
        go!: /Комиссии
            
    state: ИИС_Еще_Расторжение ИИС
        a: Расторжение индивидуальных инвестиционных счетов происходит автоматически после исполнения вывода/перевода активов (средств и ценных бумаг). Счет считается расторгнутым на пятый рабочий день.
            Выберите интересующее действие:
        buttons:
            "Вывод средств" -> /ИИС_Еще_Расторжение ИИС_Вывод средств
            "Вывод ЦБ" -> /ИИС_Еще_Расторжение ИИС_Вывод ЦБ
            "Перевод средств" -> /ИИС_Еще_Расторжение ИИС_Перевод средств
            "Перевод ЦБ" -> /ИИС_Еще_Расторжение ИИС_Вывод ЦБ
            "Расторжение пустого счета" -> /ИИС_Еще_Расторжение ИИС_Расторжение пустого
            "Расторжение с целью перевода к другому брокеру" -> /ИИС_Еще_Расторжение ИИС_Расторжение перевод
            "Назад" -> /ИИС_Еще

    state: ИИС_Еще_Перевод ИИС
        a: Перевод активов в рамках ИИС с сохранением срока действия осуществляется следующим образом:
                1. Откройте ИИС в «Финам», скачайте уведомление об открытии счета и предоставьте его брокеру, у которого находится ваш действующий ИИС. Скачать документы можно в личном кабинете: https://lk.finam.ru/reports/documents 
                2. Подайте у вашего прежнего брокера поручения на: 
                2.1. Вывод ценных бумаг. 
                2.2. Вывод денежных средств. 
                3. Перевод денежных средств и ценных бумаг на ИИС в «Финам» осуществляется только напрямую. После вывода активов закройте ИИС у прежнего брокера. 
                4. Встречное поручение на прием ценных бумаг в «Финам» сформирует менеджер. Для этого вам необходимо через обратную связь в личном кабинете предоставить реквизиты ИИС, который был открыт у прежнего брокера. 
                ❗ За перевод активов прежний брокер может взимать комиссию. Рекомендуем уточнить эту информацию заранее. 
                5. После закрытия договора возьмите справку «Сведения о ФЛ и его ИИС» и предоставьте ее оригинал в офис «Финама» в течение 30 дней с момента поступления первого актива на ИИС.

    state: ИИС_Еще_Расторжение ИИС_Вывод средств
        a: Поручение на вывод денежных средств можно сформировать в личном кабинете: https://edox.finam.ru/orders/MoneyOut/MoneyOut/Default.aspx

    state: ИИС_Еще_Расторжение ИИС_Вывод ЦБ
        a: Поручение на перевод/вывод ценных бумаг поможет сформировать менеджер компании.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            
    state: ИИС_Еще_Расторжение ИИС_Перевод средств
        a: Поручение на перевод денежных средств между брокерскими счетами поможет сформировать менеджер компании.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            
    state: ИИС_Еще_Расторжение ИИС_Расторжение пустого
        a: Расторжение пустого счета доступно в личном кабинете: https://edox.finam.ru/orders/contractAbrogation/Default.aspx
                Пустой счет считается расторгнутым на четвертый рабочий день.

    state: ИИС_Еще_Расторжение ИИС_Расторжение перевод
        a: Перевод активов в рамках ИИС с сохранением срока действия осуществляется следующим образом:
                1. Откройте ИИС у нового брокера, скачайте уведомление об открытии счета и предоставьте его в «Финам». 
                2. Подайте у вашего нового брокера поручения на ввод ценных бумаг. 
                Реквизиты депозитарного счета для ввода ценных бумаг доступны в личном кабинете: https://edox.finam.ru/global/Requisites/DepoAdmission.aspx
                3. Встречное поручение на вывод ценных бумаг в «Финаме» сформирует менеджер. Для этого вам необходимо через обратную связь в личном кабинете предоставить реквизиты ИИС, который был открыт у нового брокера.
                ❗ За вывод ценных бумаг депозитарий «Финам» удерживает 1000 ₽ за каждое поручение. Дополнительные комиссии взимаются вышестоящим депозитарием при переводе активов в депозитарий другого брокера:
                ✅ 65 ₽ — за поручение при переводе в рамках Мосбиржи;
                ✅ 75 ₽ — за поручение при переводе в рамках Биржи СПБ.
                4. Перевод денежных средств и ценных бумаг между ИИС счетами осуществляется только напрямую. После вывода активов автоматически закроется ИИС в «Финам». 
                Поручение на перевод средств можно сформировать в личном кабинете: https://edox.finam.ru/orders/MoneyOut/MoneyOut/Default.aspx (вывод на счета третьих лиц, обязательно необходимо вложить документы об открытии ИИС у нового брокера).
                5. После закрытия договора (на пятый рабочий день от даты исполнения поручения на перевод активов) возьмите справку «Сведения о ФЛ и его ИИС» и предоставьте новому брокеру в течение 30 дней с момента поступления первого актива на ИИС.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            
    state: ИИС_Третьего типа || sessionResultColor = "#CD4C2B"
        a: Отличия нового вида ИИС от старого типа:
            ✅ Инвестор сможет открывать и владеть тремя ИИС нового типа одновременно. Счет ИИС, открытый до 31 декабря 2023 года по-прежнему можно иметь только один. Если у инвестора уже есть счет ИИС старого образца, открытый до 31 декабря 2023 года, то при желании дополнительно открыть ИИС нового образца, нужно будет трансформировать старый тип ИИС в новый, обратившись в отдел поддержки. При трансформации срок владения ИИС будет считаться с момента открытия, но не более трех лет.
            ✅ Срок владения ИИС. Открытые с 2024 года ИИС, нельзя будет закрыть раньше, чем через 5 лет. Каждый следующий год минимальный срок владения будет увеличиваться и к 2031 году составит 10 лет.
            ✅ Сумма пополнения ИИС. Для счетов, открытых до 31 декабря 2023 года, доступно пополнение до 1000000 ₽ в год. У ИИС нового типа ограничений на пополнение нет.
            ✅ Типы льгот. К ИИС, открытым до 31 декабря 2023 года, можно применить один налоговый вычет на выбор. По ИИС третьего типа можно будет получать обе льготы сразу, при этом по льготе в размере финансового результата (тип Б) максимальный лимит составит 30000000 ₽
            ✅ Вывод средств с ИИС. Вывести деньги до конца срока можно будет, только чтобы оплатить дорогостоящее лечение.

    state: Налоги || sessionResultColor = "#15952F"
        intent!: /006 Налоги
        
        # script:
        #     if ( typeof $parseTree._tax_type != "undefined" ){
        #         $session.tax_type = $parseTree._tax_type;
        #         $reactions.transition("/Налоги_" + $session.tax_type.name);
        #     }
            
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Документы для налоговой" -> /Документы и справки
            "Расчет/списание НДФЛ" -> /Налоги_Расчет НДФЛ
            "Излишне удержанный налог" -> /Налоги_Излишне удержанный налог
            "Налоговые льготы" -> /Налоги_Налоговые льготы
            "Статус налогового резидента" -> /Налоги_Статус налогового резидента
            "Налог при продаже паев PTP" -> /Налоги_Налог при продаже паев PTP

    state: Налоги_Расчет НДФЛ
        a: Расчет налога по закрытым позициям доступен в личном кабинете «Расчет налога по эмитентам» по ссылке: https://lk.finam.ru/reports/tax
                1. Расчет налога по доходу физических лиц, полученного от инвестиций, производится по ставкам: 
                ✅ 13% — для резидентов (по доходам свыше 5 млн. рублей - 15%);
                ✅ 30% — для нерезидентов.
                Налог рассчитывается отдельно за каждый календарный год. 
                ❗ Исключение составляют индивидуальные инвестиционные счета. По ним нет ежегодной отчетности. Налог рассчитывается и удерживается при расторжении договора. 
                2. По стандартным брокерским договорам расчет налога и его списание происходит:
                ✅ При выводе денежных средств и ценных бумаг с брокерского счета (в размере зафиксированного дохода с 1 января текущего года).
                ✅ При расторжении брокерского договора.
                ✅ После завершения календарного года.

    state: Налоги_Излишне удержанный налог
        a: ✅ Заявление на возврат излишне удержанного налога можно сформировать в личном кабинете при наличии актуального уведомления об излишне удержанном налоге по ссылке  https://lk.finam.ru/reports/tax  в разделе «Налоги»
                Подписать документ можно в течение трех лет с момента завершения отчетного периода. 
                ✅ Чтобы вернуть налог за счет убытков прошлых лет, необходимо обратиться в налоговую службу. Для этого нужно заказать у брокера справку об убытках и 2-НДФЛ в личном кабинете: https://lk.finam.ru/reports/tax  в разделе «Налоги»
                Форму предоставления справки можно выбрать при оформлении заявки.

    state: Налоги_Налоговые льготы || sessionResultColor = "#CD4C2B"
        a: Пожалуйста, выберите интересующую льготу:
        buttons:
            "Трехгодичная льгота" -> /Налоги_Трехгодичная льгота
            "Льгота на бумаги иновационного сектора" -> /Налоги_Льгота на бумаги иновационного сектора
            "Вычеты по ИИС" -> /ИИС_Налоговый вычет ИИС
            "Льгота на долгосрочное хранение" -> /Налоги_Льгота на долгосрочное хранение

    state: Налоги_Статус налогового резидента
        a: Физическое лицо, фактически находящееся в Российской Федерации не менее 183 календарных дней в течение последних 12 следующих подряд месяцев, является налоговым резидентом. Статус налогового резидента даёт гражданам иностранных государств возможность применения льготного режима налогообложения.
            Расчет налога по доходу физических лиц, полученного от инвестиций (сделки купли/продажи), производится по ставкам: 
            ✅ 13% — для резидентов (по доходам свыше 5 млн. рублей - 15%);
            ✅ 30% — для нерезидентов.
        buttons:
            "Как получить статус налогового резидента" -> /Налоги_Как получить статус налогового резидента
            "Отказ от статуса налогового резидента" -> /Налоги_Отказ от статуса налогового резидента
            "Назад" -> /Налоги
        # if: (typeof $parseTree._rezident_type != "undefined")

    state: Налоги_Налог при продаже паев PTP
        a: С 1 января 2023 года введен новый налог на продажу паев PTP (Publicly Traded Partnerships) в размере 10% для нерезидентов США.
                ✅ Официальная новость: https://www.irs.gov/individuals/international-taxpayers/partnership-withholding 
                ✅ Актуальный список паев PTP, по которым введен налог: https://www.finam.ru/documents/commissionrates/marginal/#marj

    state: Налоги_Как получить статус налогового резидента
        a: Доступно четыре способа получения статуса резидента:
                1. Граждане РФ получают статус налогового резидента по умолчанию, пока не будет доказано обратное. Для смены статуса нерезидента в Финам, нужно предоставить паспорт с датой прописки более 183 дней. Повторное подтверждение не нужно. 
                2. Для присвоения статуса по общему порядку нужны следующие документы:
                ✅ копия паспорта с отметками о пересечении границы РФ
                ✅ миграционная карта
                3. Для граждан государств, с которыми у РФ свободное сообщение (нет отметок в паспорте) нужны следующие документы:
                ✅ копия паспорта
                ✅ справка с места работы по рекомендованной компанией «Финам» форме 
                ✅ копию трудовой книжки, заверенной работодателем 
                ✅ табели учета рабочего времени за год, предшествующий дате подачи заявления на присвоение статуса. 
                4. Можно обратиться в налоговый орган и получить документ, подтверждающий статус налогового резидента, через ИФНС. Статус предоставляется на один календарный год.
                ❗ Наличие вида на жительство, временной или постоянной регистрации на территории РФ или наличие договора аренды жилья не является подтверждением факта нахождения на территории РФ.
                ❗ Предоставлять документы и подписывать заявление для подтверждения статуса налогового резидента необходимо при каждом расчете НДФЛ (до момента вывода средств и активов, или до конца календарного года) лично в офис Финам

    state: Налоги_Отказ от статуса налогового резидента
        a: Для отказа от статуса налогового резидента необходимо предоставить подтверждение получения статуса резидента другого государства. Личное присутствие в офисе не требуется, достаточно предоставить скан копию документа.

    state: Налоги_Трехгодичная льгота
        a: ✅ Если у вас в портфеле (за исключением ИИС) есть бумаги, приобретенные после 01.01.2014, и вы владеете ими непрерывно более трех лет, то можете претендовать на трехгодичную льготу.
                ✅ Если бумаги были приобретены через другого брокера или получены в дар, и по ним отсутствует возможность подачи заявления в личном кабинете, то для оформления льготы нужно обратиться в налоговую.
                ✅ Проверить наличие бумаг, попадающих под льготу на счетах в «Финам» можно в личном кабинете по ссылке: https://edox.finam.ru/journals/ThreeYearPrivilegeRestsJournal 
                ✅ Заявление на получение льготы необходимо подписать до вывода средств от продажи ценных бумаг. Оно действует в течение одного календарного года.
                ✅ Узнать подробнее о трехгодичной льготе можно по ссылке: https://www.finam.ru/landings/tax-break

    state: Налоги_Льгота на бумаги иновационного сектора
        a: Инвестор освобождается от уплаты 13% НДФЛ по операциям с ценными бумагами высокотехнологичного (инновационного) сектора, актуальный перечень: https://www.moex.com/ru/markets/rii/rii.aspx
                Условия получения:
                ✅ приобретение не ранее включения эмитента в перечень, продажа до исключения из списка,
                ✅ срок владения: 1 год,
                ✅ льгота предоставляется брокером по запросу в отдел поддержки.

    state: Налоги_Льгота на долгосрочное хранение
        a: Инвестор освобождается от уплаты 13% НДФЛ по операциям с акциями российских и иностранных организаций (если активы эмитента состоят из недвижимости на территории РФ не более чем на 50%).
                Условия получения:
                ✅ срок владения: 5 лет,
                ✅ необходима справка от эмитента, что на последний день месяца, предшествующего месяцу продажи ЦБ, активы эмитента состояли из недвижимости на территории РФ не более чем на 50%,
                ✅ отсутствуют сделки займа/РЕПО.
                Способы получения:
                ✅ через «Финам» до 31.01 года, следующего за годом продажи ЦБ (обязательно предоставление справки от эмитента),
                ✅ через ИФНС - в течение 3-х лет, следующих за отчетным периодом, в котором произошла реализация этих ЦБ.

    state: Выплата дохода || sessionResultColor = "#15952F"
        intent: /007 Выплата дохода
        a: Клиентам «Финам» начисляются дивиденды и купонные выплаты в рублях — по российским ценным бумагам и в валюте — по еврооблигациям, ETF фондам, а также по иностранным ценным бумагам.
            ✅ Доходы в валюте от СПБ Биржи поступают в рублях РФ, распределение поступивших сумм происходит пропорционально количеству ценных бумаг на всех владельцев, остальная часть выплаты не поступает из-за блокировки цепочки с Euroclear/Clearstream. СПБ Биржа по причине санкций осуществляет конверсионные операции с валютой, на основаниях, предусмотренных ее [регламентом|https://spbbank.ru/ru/depobsl/usl_os_depdeyat/files/171123/Usloviia_osushchestvleniia_depozitarnoi_deiatelnosti.pdf]. Изменить валюту выплаты или отказаться от выплаты в рублях РФ – невозможно.
            ✅ [Новости депозитария СПБ Биржи|https://spbbank.ru/ru/depobsl/Soobshcheniia_Depozitariia]
            ✅ Исключением являются дивиденды по китайским бумагам на бирже СПБ (выплаты поступают в HKD).
            ❗ Денежные средства от участий в выкупах бумаг поступают на предоставленные в [личном кабинете|https://edox.finam.ru/orders/depoBankAccountDetails.aspx] реквизиты. 
            ✅ Выберите нужный вариант:
        buttons:
            "Даты фиксации для получения дохода" -> /Выплата дохода_Даты фиксации
            "Срок выплаты дохода" -> /Выплата дохода_Срок выплаты
            "Налог на купоны/дивиденды" -> /Выплата дохода_Налог
            "Вывод купонов/дивидендов" -> /Выплата дохода_Вывод
            "Ещё" -> /Выплата дохода_Ещё

    state: Выплата дохода_Даты фиксации
        a: 1. Список российских эмитентов с датами проведения собраний акционеров, даты фиксации владельцев ценных бумаг для получения дивидендов доступны по ссылке: https://www.finam.ru/analysis/assembly/
            2. Дату фиксации, дату выплаты купонов и погашения можно проверить по каждой облигации на сайте https://bonds.finam.ru/issue/info/ (выбрать нужную бумагу и раскрыть меню «Платежи» под ее описанием).

    state: Выплата дохода_Срок выплаты
        a: ✅ Максимальный срок выплаты дивидендов и купонов со стороны эмитентов составляет 10 рабочих дней.
            Перечисление средств со стороны брокера может занимать еще до 7 рабочих дней. На практике «Финам» производит выплаты клиентам в течение дня с момента получения средств от эмитента.
            ❗ Начисление выплат в валюте может занимать больше времени, так как в переводе средств участвуют банки-корреспонденты.
            ✅ По облигациям, приобретенным через сделку РЕПО, начисление происходит в течение пяти рабочих дней с момента основной выплаты.
            ✅ Сроки выплат и зачислений после корпоративных действий эмитент указывает в спецификации корпоративного действия (информация публикуется на сайте биржи, а также на сайте брокера в   разделе «Новости депозитария» по ссылке: https://www.finam.ru/publications/section/deponews )
            ❗ Выплаты и зачисления в валюте по корп. действиям с иностранными бумагами могут занимать от двух недель до нескольких месяцев, в связи с ограничениями внешних депозитариев.
            ❗ Выплаты от корпоративных действий приходят на банковские реквизиты, которые необходимо предоставить в личном кабинете по ссылке: https://edox.finam.ru/orders/depoBankAccountDetails.aspx

    state: Выплата дохода_Налог
        a: 1. По купонам, выплаченным в рублях и иностранной валюте, брокер самостоятельно удерживает и уплачивает налоги. Вам не требуется предоставлять дополнительную информацию в налоговую. 
            Налоговая ставка по купонам:
            ✅ для резидентов - 13%, 
            ✅ для нерезидентов - 30%. 
            2. По дивидендам, выплаченным в рублях, брокер самостоятельно удерживает и уплачивает налоги. Вам не требуется предоставлять дополнительную информацию в налоговую. 
            Налоговая ставка по дивидендам: 
            ✅ для резидентов - 13%, 
            ✅ для нерезидентов - 15%. 
            По дивидендам, выплаченным в иностранной валюте до 2023 года включительно, «Финам» не удерживал и не уплачивал налоги. Информацию в налоговую службу вам необходимо подать самостоятельно. С 2024 года брокер будет самостоятельно удерживать налог с таких доходов.
            Ставки налога по дивидендам в иностранной валюте на разных биржах:
        buttons:
            "Московская биржа" -> /Выплата дохода_Налог_Московская биржа
            "Биржа СПБ" -> /Выплата дохода_Налог_Биржа СПБ
            "NYSE/NASDAQ" -> /Выплата дохода_Налог_NYSE и NASDAQ
            "Гонконг (HKEX)" -> /Выплата дохода_Налог_Гонконг
            "Назад" -> /Выплата дохода

    state: Выплата дохода_Налог_Московская биржа
        a: 30% (без подписанной формы W8BEN) или 10% (с формой W8BEN) + 3% (необходимо оплатить самостоятельно).
            Форму W8BEN можно оформить в личном кабинете: https://lk.finam.ru/reports/tax  
            Подписанная форма действует три года.

    state: Выплата дохода_Налог_Биржа СПБ
        a: ✅ До 2023 года включительно:
            30% (без подписанной формы W8BEN) или 10% (с формой W8BEN) + 3% (необходимо оплатить самостоятельно).
            ✅ С 1 января 2024 года:
            Все дивидендные выплаты по акциям американских компаний, приобретенных на СПБ Бирже, облагаются налогом 30%.
            Ставка повышается в результате действий Налоговой службы США (IRS), которая приостановила соглашение с СПБ Банком, в связи с чем банк утратил статус участника FATCA.
            В связи с этим больше нельзя подать форму W-8BEN для СПБ Биржи, которая позволяла снизить ставку налога для уплаты в США и избежать двойного налогообложения. Ранее направленные формы также перестают действовать. 
            Как следствие, из дохода по дивидендам с американских акций, купленных на СПБ Бирже, также будет вычтен НДФЛ, предусмотренный российским законодательством для резидентов физических лиц (13% или 15%).
            С подробностями можно ознакомиться по ссылке: https://spbbank.ru/ru/news/?news=7842&utm_source=email&utm_medium=mass&utm_campaign=info

    state: Выплата дохода_Налог_NYSE и NASDAQ
        a: Ставка налога по дивидендам на ЦБ, приобретенным на биржах США, составляет 15% и удерживается эмитентом в момент выплаты. Однако, есть и исключения. Например, инвестиционные фонды недвижимости (REIT - Real Estate Investment Trust) - ставка 30%.
            Налоговые резиденты РФ также должны заплатить налог с полученного дохода по ставкам 13% или 15% (если доход свыше 5 млн. руб.).
            С 1 января 2024 года у физических лиц резидентов РФ удержание налога производит брокер, самостоятельно декларировать такой доход не требуется. По дивидендам, полученным до 2023 года включительно, необходимо отчитаться самостоятельно.

    state: Выплата дохода_Налог_Гонконг
        a: Ставка налога по дивидендам на ЦБ, приобретенным на бирже Гонконга (HKEX) - 0%.
            Но налоговые резиденты РФ должны заплатить налог с полученного дохода по ставкам 13% или 15% (если доход свыше 5 млн. руб.).
            С 1 января 2024 года у физических лиц резидентов РФ удержание налога производит брокер, самостоятельно декларировать такой доход не требуется. По дивидендам, полученным до 2023 года включительно, необходимо отчитаться самостоятельно.

    state: Выплата дохода_Вывод
        a: Выплата дивидендов, купонов и прочих доходов по ценным бумагам по умолчанию предусмотрена на счет учета бумаг.
            Клиенты «Финам» могут оформить или отменить выплату дохода на другой брокерский или банковский счет.
            Чтобы выплаты доходов автоматически зачислялись на другой счет, нужно подать заявку в личном кабинете по ссылке: https://edox.finam.ru/List/Extracts 
            В разделе «Депозитарий» выбрать нужное:
            ✅ Заявка на выплату доходов по ЦБ на другие счета
            ✅ Заявка на ОТМЕНУ перечисления дохода по ЦБ на другие счета.
            ❗ При выборе банковского счета для получения выплаты дохода обязательно нужно заполнить банковские реквизиты, для этого нужно нажать кнопку «Добавить реквизиты».
            ❗ Чтобы перенаправить зачисление средств от погашений облигаций на другой счет, нужно обратиться к менеджеру.

    state: Выплата дохода_Ещё
        a: Выберите нужный вариант
        buttons:
            "Как проверить начисление" -> /Выплата дохода_Ещё_Проверить начисление
            "Документы для отчетности по дивидендам" -> /Выплата дохода_Ещё_Документы для отчетности
            "Комиссии за начисление дохода" -> /Выплата дохода_Ещё_Комиссии за начисление
            "Назад" -> /Выплата дохода

    state: Выплата дохода_Ещё_Проверить начисление
        a: Сумма начисленных дивидендов и купонов отображается в виде свободного остатка или уменьшает сумму займа по счету в соответствующей валюте.
            Сумму начислений также можно проверить в личном кабинете:
            ✅ в истории операций https://lk.finam.ru/history 
            ✅ в справке по счету с детальным описанием операций и сделок: https://lk.finam.ru/reports/tax (можно загрузить только за закрытый торговый период).

    state: Выплата дохода_Ещё_Документы для отчетности
        a: ✅ Дивиденды в рублях поступают на счет уже очищенными от налога. В этом случае вам не нужно подавать документы в налоговую.
            ✅ По дивидендам, полученным в валюте, вам необходимо самостоятельно отчитаться перед налоговой. Вам нужно подготовить пакет документов в зависимости от площадки, на которой вы покупали ценные бумаги.
        buttons:
            "Московская биржа" -> /Выплата дохода_Документы_Московская биржа
            "Биржа СПБ" -> /Выплата дохода_Документы_Биржа СПБ
            "Иностранные биржи" -> /Выплата дохода_Документы_Иностранные биржи
            "Назад" -> /Выплата дохода_Ещё

    state: Выплата дохода_Документы_Московская биржа
        a: Для отчетности в налоговую по валютным дивидендам от иностранных компаний загрузите справку 1042S в личном кабинете по ссылке: https://lk.finam.ru/reports/tax в разделе «Налоги».
            ✅ Справка предоставляется депозитарием за завершенный отчетный период (календарный год).
            ✅ По депозитарным распискам детальная информация о зачислениях отображается в уведомлениях о выплате дохода (по данному инструменту форма 1042S не предоставляется).
            ✅ Уведомления доступны в личном кабинете по ссылке: https://lk.finam.ru/reports/documents

    state: Выплата дохода_Документы_Биржа СПБ
        a: Для отчетности в налоговую по валютным дивидендам от иностранных компаний загрузите в личном кабинете справку 1042S в личном кабинете по ссылке: https://lk.finam.ru/reports/tax  в разделе «Налоги».
            ✅ Справка предоставляется депозитарием за завершенный отчетный период (календарный год).
            ✅ Детальная информация по каждому зачислению отображается в уведомлениях о выплате дохода в личном кабинете по ссылке: https://lk.finam.ru/reports/documents

    state: Выплата дохода_Документы_Иностранные биржи
        a: Для отчетности в налоговую по валютным дивидендам от иностранных компаний, запросите Уведомление от вышестоящего брокера у менеджера «Финам».
                ✅ Уведомление предоставляется вышестоящим брокером за завершенный отчетный период (календарный год).
                ✅ Дополнительно в ваш личный кабинет будет выгружено Уведомление о присвоении торгового кода по ссылке: https://lk.finam.ru/reports/documents 
                Документ содержит информацию о соответствии зарубежного торгового кода и ваших паспортных данных.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Выплата дохода_Ещё_Комиссии за начисление
        a: ✅ По «старым» счетам комиссия за начисление дивидендов в рублях составляет 1,18%, за начисление купонов - 0,236%.
            ✅ По счетам, открытым после 26.11.2020, а также по счетам с самостоятельно измененным тарифом позже указанной даты, комиссия не взимается.
            ✅ За начисление дивидендов и купонов в валюте комиссия не удерживается.

    state: Срочный рынок || sessionResultColor = "#15952F"
        intent!: /008 Срочный рынок
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Как купить/продать фьючерс" -> /Срочный рынок_Купить фьючерс
            "Гарантийное обеспечение" -> /Срочный рынок_Обеспечение
            "Вариационная маржа" -> /Срочный рынок_Маржа
            "Доступ к опционам" -> /Срочный рынок_Опционы
            "Ещё" -> /Срочный рынок_Ещё

    state: Срочный рынок_Купить фьючерс
        a: Выставить заявку на покупку и продажу фьючерса вы можете через любую торговую систему или с помощью голосового поручения.
            ✅ Рекомендуем обращать внимание на время проведения торгов для корректного выставления заявки.
            ✅ Торговая сессия начинается вечером и длится с 19:05 до 23:50, продолжается на следующий день — с 10:00 до 14:00 и с 14:05 до 18:50 МСК.

    state: Срочный рынок_Обеспечение
        a: При покупке или продаже фьючерса на вашем счете блокируется гарантийное обеспечение (ГО).
            ✅ Сумма ГО по каждому инструменту формируется на основании ставок риска. Стандартное гарантийное обеспечение по каждому фьючерсу публикуется на сайте биржи. 
            ✅ По счетам единой денежной позиции со статусом риска КСУР гарантийное обеспечение увеличено в связи с действующими требованиями к риск-менеджменту. 
            Проверить актуальное ГО по Вашему счету можно в системе Transaq в информации по инструменту, либо уточнить у менеджера.
            ❗ В момент выставления рыночной заявки гарантийное обеспечение увеличивается в 1,5 раза. 
            Уменьшить гарантийное обеспечение можно одним из способов:
        buttons:
            "Отключение фондовой и валютной секций по счетам ЕДП" -> /Срочный рынок_Обеспечение_Отключение
            "Услуга «Пониженное ГО» (ПГО)" -> /Срочный рынок_Обеспечение_ПГО
            "Использование брокерского договора с отдельными счетами" -> /Срочный рынок_Обеспечение_Отдельные счета
            "Получить статус КПУР" -> /Маржа_Уровни риска
            "Назад" -> /Срочный рынок

    state: Срочный рынок_Обеспечение_Отключение
        a: Отключить секцию можно, обратившись к менеджеру.
            ❗ Предварительно необходимо заполнить раздел «Инвестиционный профиль» в личном кабинете: https://lk.finam.ru/user/invest-profile 
            А также нужно проверить счет на соответствие требованиям:
            ✅ сумма средств по счету больше 10000₽
            ✅ отсутствуют сделки с ценными бумагами и валютой по счету
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Срочный рынок_Обеспечение_ПГО || sessionResultColor = "#B65A1E"
        a: Услуга пониженного гарантийного обеспечения (ПГО) предоставляется клиентам «Финам», удовлетворяющим одновременно нескольким требованиям.
            ✅ [Условия подключения услуги «Пониженное ГО»|https://www.finam.ru/landings/services-pgo-forts]
            ✅ [Подключить или отключить услугу в личном кабинете|https://edox.finam.ru/orders/ReducedGuarantee.aspx]
            ❗ Услуга «Пониженное ГО» действует в будние дни с 9:00 до 19:30 МСК, предоставляется по ограниченному перечню инструментов.
            ❗ Услуга недоступна при торговле через терминал MetaTrader 5.
        a: Услуга пониженного гарантийного обеспечения (ПГО) может быть недоступна в ближайшие дни в связи с возможной повышенной волатильностью курса рубля. Просим учитывать данную информацию при планировании торговых операций.

    state: Срочный рынок_Обеспечение_Отдельные счета
        a: По договорам с раздельными счетами (моносчетами) размер ГО равен указанному на бирже.
            ✅ Открыть новый счет можно в личном кабинете по ссылке: https://edox.finam.ru/NewAccount/Product?ContextId=fff807f6-7837-4bff-9484-014dc3c5f94c

    state: Срочный рынок_Маржа
        a: Прибыль (убыток) по фьючерсам и опционам зачисляется (списывается) в виде вариационной маржи.
            ✅ Позиционная вариационная маржа начисляется на контракты, которые есть в портфеле на утро. 
            ✅ Посделочная вариационная маржа начисляется в день открытия позиции по фьючерсу или опциону. На следующий день и до момента закрытия позиции начисляется позиционная вариационная маржа. Если позиция открыта и закрыта внутри торговой сессии, то будет зачислена посделочная маржа.
            ✅ Фактическое зачисление вариационной маржи на счет происходит в основной клиринг (в 19:05 по МСК). 
            Движение позиционной вариационной маржи отображается в справке по счету ( https://lk.finam.ru/reports/tax ), а также в истории операций ( https://lk.finam.ru/history ). 
            ✅ Параметры инструментов для расчета вариационной маржи доступны на сайте биржи: https://www.moex.com/ru/derivatives/  
            Расчет можно произвести по формуле:
            ВМ = (PS - PB)*W/R, где: 
            PS – цена продажи,
            PB – цена покупки,
            W – стоимость шага цены,
            R – шаг цены.

    state: Срочный рынок_Опционы
        a: 1. Опционы на Московской бирже:
            ✅ Доступ к поставочным опционам (на фьючерсы) предоставляется только в рамках договоров с раздельными брокерскими счетами.
            ✅ Доступ к опционам на российские ценные бумаги предоставляется по «Единым счетам» (доступны только в системе Quik) и по договорам с раздельными брокерскими счетами.
            ✅ Для оценки доходности, определения размера гарантийного обеспечения и биржевой комиссии при торговле опционами на ММВБ вы можете использовать опционный калькулятор по ссылке https://www.moex.com/msn/ru-options-calc 
            2.  Доступ к американским опционам (США) предоставляется по:
            ✅ «Единым счетам» (для получения доступа обратитесь к менеджеру)
            ✅ по счетам «US Market Options»
            ✅ по счетам «Сегрегированный Global»
            ✅ счетам «Иностранные биржи» (с 1.02.2024 открытие новых счетов недоступно)
            ❗ Для работы с данными инструментами требуется статус квалифицированного инвестора.
            ❗ Все расчеты производятся в долларах США, автоконвертация валюты при покупке не осуществляется.
        buttons:
            "Доска опционов" -> /Срочный рынок_Опционы_Доска

    state: Срочный рынок_Опционы_Доска
        a: Доска опционов доступна в торговых системах: TRANSAQ, TRANSAQ US, QUIK, FinamTrade (web).
            1. QUIK:
            — на панели инструментов нужно нажать «Создать окно» → «Все типы окон» → «Доска опционов».
            2. TRANSAQ/TRANSAQ US:
            — на панели инструментов нажать «Таблицы» → «Финансовые инструменты»,
            — нажать правой кнопкой мыши по таблице и с помощью выбора/поиска инструмента добавить необходимый базовый актив (фьючерс),
            — нажать правой кнопкой мыши по добавленному инструменту и выбрать меню «Доска опционов».
            3. FinamTrade:
            — слева на панели инструментов нужно перейти в категорию «Рынки» и выбрать необходимый фьючерс,
            — справа от кнопки «Заявка» будет доступна кнопка «Опционы».

    state: Срочный рынок_Ещё
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Комиссия на срочном рынке" -> /Срочный рынок_Ещё_Комиссия
            "Ошибки при выставлении заявок" -> /Срочный рынок_Ещё_Ошибки
            "Тестирование для работы на срочном рынке" -> /Срочный рынок_Ещё_Тестирование
            "Исполнение фьючерсов/опционов" -> /Срочный рынок_Ещё_Исполнение
            "Назад" -> /Срочный рынок

    state: Срочный рынок_Ещё_Комиссия || sessionResultColor = "#15952F"
        a: При торговле на срочном рынке FORTS возникает два вида комиссий — биржевая и брокерская.
                Биржевая комиссия:
                ✅ За исполнение лимитных заявок, создающих ликвидность рынка (формирующих стакан спроса/предложения), комиссия со стороны биржи не удерживается. 
                ✅ За исполнение рыночных заявок, а также лимитных ордеров с мгновенным исполнением, комиссии взимаются согласно тарифам, указанным в спецификации каждого срочного контракта: https://www.moex.com/ru/derivatives/ 
                ✅ Комиссия брокера зависит от тарифного плана. 
                ✅ Полное описание тарифных планов с учетом всех торговых инструментов доступно в регламенте брокерского обслуживания: http://zaoik.finam.ru/broker/regulations 
                ✅ Выберите свой тариф:
        buttons:
            "ФриТрейд/Единый Тест-Драйв" -> /Срочный рынок_Ещё_Комиссия_ФриТрейд
            "Стратег" -> /Срочный рынок_Ещё_Комиссия_Стратег
            "Инвестор" -> /Срочный рынок_Ещё_Комиссия_ФриТрейд
            "Единый Дневной" -> /Срочный рынок_Ещё_Комиссия_ФриТрейд
            "Единый Дневной Options" -> /Срочный рынок_Ещё_Комиссия_Единый Options
            "Другие тарифы" -> /Срочный рынок_Ещё_Комиссия_Другие тарифы
            "Назад" -> /Срочный рынок_Ещё

    state: Срочный рынок_Ещё_Комиссия_ФриТрейд
        a: 1. Московская биржа:
            ✅ Заявка через ИТС:
            – фьючерсы и опционы – 0,45 ₽ за контракт 
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы – 0,0354% от стоимости контракта
            – опционы - 2 ₽ за контракт
            2. Биржи США:
            ✅ Заявка через ИТС:
            – стандартные фьючерсы — 10 $ за контракт 
            – микро и мини-фьючерсы — 5 $ за контракт
            – опционы – 3$ за контракт
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы и опционы – 0,1% от стоимости контракта.
            ❗ Открытие новых позиций с иностранными фьючерсами временно недоступно.

    state: Срочный рынок_Ещё_Комиссия_Стратег
        a: 1. Московская биржа:
            ✅ Заявка через ИТС:
            – фьючерсы и опционы – 0,9 ₽ за контракт 
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы – 0,0354% от стоимости контракта
            – опционы - 2 ₽ за контракт
            2. Биржи США:
            ✅ Заявка через ИТС:
            – стандартные фьючерсы — 10 $ за контракт
            – микро и мини-фьючерсы — 5 $ за контракт
            – опционы – 3$ за контракт
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы и опционы – 0,1% от стоимости контракта.
            ❗ Открытие новых позиций с иностранными фьючерсами временно недоступно.

    state: Срочный рынок_Ещё_Комиссия_Единый Options
        a: 1. Московская биржа:
            ✅ Заявка через ИТС:
            – фьючерсы и опционы – 0,45 ₽ за контракт
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы – 0,0354% от стоимости контракта
            – опционы - 2 ₽ за контракт
            2. Биржи США:
            ✅ Заявка через ИТС:
            – стандартные фьючерсы — 10 $ за контракт
            – микро и мини-фьючерсы — 5 $ за контракт
            – опционы – 0,65 $ за контракт
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы и опционы – 0,1% от стоимости контракта.

    state: Срочный рынок_Ещё_Комиссия_Другие тарифы
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Тест-Драйв" -> /Срочный рынок_Ещё_Комиссия_Тест-драйв
            "Стандартный ФОРТС" -> /Срочный рынок_Ещё_Комиссия_Тест-драйв
            "Консультационный ФОРТС" -> /Срочный рынок_Ещё_Комиссия_Консультационный ФОРТС
            "Единый Фиксированный" -> /Срочный рынок_Ещё_Комиссия_Тест-драйв
            "Единый Консультационный" -> /Срочный рынок_Ещё_Комиссия_Единый Консультационный
            "Назад" -> /Срочный рынок_Ещё_Комиссия
    
    state: Срочный рынок_Ещё_Комиссия_Тест-драйв
        a: ✅ Заявка через ИТС:
            – фьючерсы и опционы – 0,45 ₽ за контракт 
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы – 0,0354% от стоимости контракта
            – опционы - 2 ₽ за контракт

    state: Срочный рынок_Ещё_Комиссия_Консультационный ФОРТС
        a: ✅ Заявка через ИТС:
            – фьючерсы и опционы – 4,65 ₽ за контракт 
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы – 0,03611% от стоимости контракта
            – опционы - 4,65 ₽ за контракт

    state: Срочный рынок_Ещё_Комиссия_Единый Консультационный
        a: 1. Московская биржа:
            ✅ Заявка через ИТС:
            – фьючерсы и опционы – 4,65 ₽ за контракт
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы – 0,03611% от стоимости контракта
            – опционы - 4,65 ₽ за контракт
            2. Биржи США:
            ✅ Заявка через ИТС:
            – фьючерсы — 10 $ за контракт
            – опционы – 3$ за контракт
            ✅ Заявка с голоса (или закрытие брокером):
            – фьючерсы и опционы – 0,1% от стоимости контракта.

    state: Срочный рынок_Ещё_Ошибки || sessionResultColor = "#BC3737"
        a: При торговле на срочном рынке нужно знать ряд моментов:
            1. Нужно пройти тестирование «Производные финансовые инструменты».
            Сдать тест, чтобы получить доступ к срочному рынку, можно в личном кабинете: https://lk.finam.ru/user/invest-status/qual-exam/tests  
            2. При выставлении «рыночной заявки» размер ГО будет в 1,5 раза выше стандартного. Из-за этого у вас может не хватать средств на открытие новых позиций. Рекомендуем использовать «лимитные» заявки.
            3. Под активные ордера блокируется ГО, что может помешать открытию новых позиций.
            4. Торговая сессия на срочной секции ММВБ начинается вечером и длится с 19:05 до 23:50, продолжается на следующий день — с 10:00 до 14:00 и с 14:05 до 18:50 (по московскому времени). 
        go!: /Ошибки заявок

    state: Срочный рынок_Ещё_Тестирование
        a: Для того, чтобы начать торговать на срочном рынке, пройдите тестирование «Производные финансовые инструменты» в личном кабинете: https://lk.finam.ru/user/invest-status/qual-exam/tests

    state: Срочный рынок_Ещё_Исполнение
        a: ✅ Расчетные фьючерсы не предполагают поставки базового актива, исполняются биржей автоматически в день исполнения (дата указана в спецификации инструментов на бирже и торговых системах), дополнительные заявки не требуются.
            ✅ Опционы на акции - расчетные, они не предполагают поставки базового актива, исполняются биржей автоматически в день исполнения.
        a: ✅ Поставочные фьючерсы на ценные бумаги закрываются брокером, начиная с вечерней сессии за день до последнего дня обращения.
            ❗ Для выхода на поставку базового актива нужно подписать Уведомление о поставке не позднее 19:00 МСК за два рабочих дня (Т-2) до исполнения контракта, обратившись к менеджеру компании.
            ✅ Заявку на поставку вечных фьючерсов можно подать через отдел голосового трейдинга. Подача поручений на исполнение происходит 4 раза в год за три дня до исполнения квартального фьючерса на соответствующие базовые активы в течение одного торгового дня. При исполнении такой заявки биржа удерживает дополнительную комиссию 1%. 
            ✅ Опционы на фьючерсы исполняются автоматически в день исполнения контракта, дополнительные заявки не требуются. 
            ❗ Досрочное исполнение и отказ от исполнения опционов возможен через отдел голосового трейдинга.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Валютный рынок || sessionResultColor = "#15952F"
        intent!: /009 Валютный рынок
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Как купить/продать валюту" -> /Валютный рынок_Как купить
            "Комиссии за сделки с валютой" -> /Валютный рынок_Комиссия
            "Ввод/вывод/хранение" -> /Валютный рынок_Ввод и вывод
            "Доступные инструменты" -> /Валютный рынок_Инструменты
            "Ошибки при выставлении заявок" -> /Валютный рынок_Ошибки

    state: Валютный рынок_Как купить
        a: Валютные пары торгуются на валютной секции Мосбиржи.
            1. Полные лоты доступны в виде контрактов _TOD (расчеты в текущий рабочий день после 23:50 МСК) и _TOM (расчеты на следующий рабочий день после 23:50 МСК). Размер 1 лота равен:
            ✅ USDRUB, USDCNY, EURRUB, EURUSD, CNYRUB, HKDRUB, BYNRUB, TRYRUB – 1000 ед. валюты.
            ✅ KZTRUB, AMDRUB – 10000 ед. валюты.
            ✅ UZSRUB – 1000000 ед. валюты.
            2. Неполные лоты валют доступны в виде контрактов _TMS (торгуются кратно 0,01 ед. валюты, минимальная заявка от 1 ед. валюты, расчеты на следующий рабочий день после 23:50 МСК).
            3. В режиме _ТОМ доступны контракты на золото (1 лот = 1 грамм) и серебро (1 лот = 100 грамм).
            4. Сделки с валютными парами доступны как в рамках стандартных брокерских договоров, так и договоров ИИС.
            Для открытия позиции можно воспользоваться поиском в торговой системе, или выбрать инструмент из раздела «Валюты». Далее будет доступна опция «Заявка».
            ❗ В терминале Finam Trade в разделе «Мировые валюты» транслируются индикативные форекс-котировки, торги такими валютными парами недоступны.

    state: Валютный рынок_Комиссия
        a: При торговле на валютном рынке возникает два вида комиссии: биржевая и брокерская.
            Дополнительно может возникать перенос необеспеченных позиций (сделки СВОП), при наличии чистого минуса в рублях/валюте.
        buttons:
            "Комиссия брокера" -> /Валютный рынок_Комиссия брокера
            "Комиссия биржи" -> /Валютный рынок_Комиссия биржи
            "Комиссия за перенос займа (СВОП)" -> /Валютный рынок_Комиссия СВОП
            "Назад" -> /Валютный рынок

    state: Валютный рынок_Комиссия брокера
        a: Комиссия брокера зависит от выбранного тарифа.
            Выберите тариф:
        buttons:
            "ФриТрейд" -> /Валютный рынок_Комиссия брокера_ФриТрейд
            "Стратег" -> /Валютный рынок_Комиссия брокера_ФриТрейд
            "Инвестор" -> /Валютный рынок_Комиссия брокера_ФриТрейд
            "Единый Дневной" -> /Валютный рынок_Комиссия брокера_ФриТрейд
            "Единый Консультационный" -> /Валютный рынок_Комиссия брокера_Единый Консультационный
            "Другие тарифы" -> /Валютный рынок_Комиссия_Другие тарифы
            "Назад" -> /Валютный рынок_Комиссия
    
    state: Валютный рынок_Комиссия брокера_ФриТрейд
        a: ✅ За торговлю полными лотами валюты (контракты _TOD и _TOM) комиссия - 0,03682%, мин 41.30 ₽ при обороте до 1000000 ₽ за торговую сессию.
            ✅ За торговлю мелкими лотами валюты (контракты _TMS) комиссия - 0,03682%.
            ✅ За торговлю металлами (контракты _TOM) комиссия - 0,05% от оборота.
    
    state: Валютный рынок_Комиссия брокера_Единый Консультационный
        a:  ✅ За торговлю полными лотами (контракты _TOD и _TOM) комиссия - 0,08262% при любом обороте за торговую сессию. 
             ✅ За торговлю мелкими лотами (контракты _TMS) комиссия - 0,08262%.
             ✅ За торговлю металлами (контракты _TOM) комиссия - 0,05% от оборота.
    
    state: Валютный рынок_Комиссия_Другие тарифы
        a: Другие тарифы:
        buttons:
            "Единый Фиксированный" -> /Валютный рынок_Комиссия_Единый Фиксированный
            "Единый Оптимум" -> /Валютный рынок_Комиссия_Единый Оптимум
            "Назад" -> /Валютный рынок_Комиссия брокера

    state: Валютный рынок_Комиссия_Единый Фиксированный
        a: ✅ За торговлю полными лотами (контракты _TOD и _TOM) комиссия - 0,03682%, при обороте до 1000000 ₽ за торговую сессию.
            ✅ За торговлю мелкими лотами (контракты _TMS) комиссия - 0,03682%.
            ✅ За торговлю металлами (контракты _TOM) комиссия - 0,05% от оборота.

    state: Валютный рынок_Комиссия_Единый Оптимум
        a: ✅ За торговлю полными лотами (контракты _TOD и _TOM) комиссия - 0,03386%, мин 41.30 ₽ при обороте до 1000000 ₽ за торговую сессию.
            ✅ За торговлю мелкими лотами (контракты _TMS) комиссия - 0,03386%.
            ✅ За торговлю металлами (контракты _TOM) комиссия - 0,05% от оборота.

    state: Валютный рынок_Комиссия биржи
        a: ✅ За торговлю полными лотами (контракты TOD и TOM) — 0% для мейкеров и 0,0045% от оборота для тейкеров, при этом минимальная комиссия за сделку 50 ₽ (исключение USDRUB, EURRUB – 100 ₽), если заявка на совершение сделки подана объемом менее 50 лотов;
            если более 50 лотов – минимальная комиссия 0,02 ₽ для мейкеров и 1 ₽ для тейкеров. 
            Комиссия за сделки СВОП составляет 0,0006% от суммы первой части сделки СВОП, но не менее 1 ₽ за сделку.
            ✅ За торговлю мелкими лотами (контракты _TMS) — 0% для мейкеров и 0,075% от оборота для тейкеров, при этом минимальная комиссия за сделку 1 ₽.
            ✅ За торговлю драгоценными металлами комиссия биржи:
            — золото - при покупке - 1 ₽, при продаже - 0,02%, но не менее 1 ₽, 
            — серебро - при покупке - 0,006375%, но не менее 1 ₽, при продаже - 0,017875%, но не менее 1 ₽.

    state: Валютный рынок_Комиссия СВОП || sessionResultColor = "#CD4C2B"
        a: Сделки СВОП – сделки переноса необеспеченных валютных позиций.
            Брокер осуществляет перенос позиций в том случае, если по счету клиента не могут пройти расчеты. 
            Такая ситуация может сложиться, если на дату расчетов у клиента по счету отрицательная чистая позиция по рублям или валюте (фактически, одна валюта выступает обеспечением для другой).
            1. Комиссия СВОП за перенос длинных позиций - не более максимальной из следующих величин: 
            ✅ КС/365 + 0,02192 
            и 
            ✅ максимальное значение величины MaxSwap/t/БК, рассчитываемой по данным ПАО Московская Биржа за 7 (семь) торговых дней, предшествующих дню заключения сделки своп по переносу чистой открытой позиции.
            2. Комиссия СВОП за перенос коротких позиций - не менее минимальной из следующих величин (с учетом знака): 
            ✅ (- 1) × (КС/365 + 0,0137) 
            и 
            ✅ минимальное значение величины MinSwap/t/БК, рассчитываемой по данным ПАО Московская Биржа за 7 (семь) торговых дней на валютном рынке, предшествующих дню заключения сделки своп по переносу чистой открытой позиции.
            Детальнее на сайте: https://www.finam.ru/landings/tariff-learn-more/

    state: Валютный рынок_Ввод и вывод
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Хранение валюты" -> /Валютный рынок_Ввод и вывод_Хранение валюты
            "Ввод валюты" -> /Движение ДС_Пополнение_Реквизиты
            "Вывод валюты" -> /Движение ДС_Вывод_Реквизиты
            "Назад" -> /Валютный рынок

    state: Валютный рынок_Ввод и вывод_Хранение валюты
        a: ❗ В связи с повышенными рисками хранения валюты в российской юрисдикции, с 26 февраля 2024 года повышены комиссии за хранение валюты в долларах США и британских фунтах. При свободном остатке валюты от 10000 до 100000 ед. валюты - ставка будет увеличена с 5% до 10% годовых, при остатках свыше 100000 ед. валюты - ставка будет увеличена с 3% до 6% годовых.
                ✅ Чтобы сохранить сниженные ставки, клиенты «Финам» могут подписать соглашение с рисками хранения валюты по запросу через менеджера поддержки. После подписания, ставки останутся прежними, комиссии за хранение долларов США и фунтов на брокерских счетах:
                – если сумма до 10000 ед. валюты – комиссия не списывается
                – если сумма свыше 10000 и до 100000 ед. валюты – 5% годовых
                – если свыше 100000 ед. валюты – 3% годовых
                ✅ Комиссия удерживается в рублях по курсу Банка России на дату списания, расчет осуществляется исходя из количества валюты на счете по состоянию на конец календарного дня. Списание происходит не позднее окончания соответствующего дня.
                ✅ Комиссия за хранение валюты не взимается по счетам «Сегрегированный Global».
        buttons:
            "Сегрегированный Global" -> /Сегрегированный
            "Перевод на оператора" -> /Перевод на оператора
            "Назад" -> /Валютный рынок_Ввод и вывод

    state: Валютный рынок_Инструменты
        a: ✅ Сделки с валютными парами доступны как в рамках стандартных брокерских договоров, так и договоров ИИС.
            ✅ В рамках счетов АО Финам предоставляется доступ к валютным парам: доллар США/российский рубль, евро/российский рубль, китайский юань/российский рубль, гонконгский доллар/российский рубль, турецкая лира/российский рубль, белорусский рубль/российский рубль, казахстанский тенге/российский рубль.
            Также доступны валютные пары евро/доллар США и доллар США/китайский юань. 
            ❗ Торги швейцарским франком и британским фунтом временно приостановлены по инициативе биржи. 
            ✅ Дополнительно в режиме _ТОМ доступны контракты на  золото (1 лот = 1 грамм) и серебро (1 лот = 100 грамм).

    state: Валютный рынок_Ошибки || sessionResultColor = "#BC3737"
        a: Для совершения сделок с валютными парами/драг. металлами нужно знать ряд моментов:
                1. Использовать счет ЕДП (Единая денежная позиция) с подключенной валютной секцией, или счет валютного рынка в рамках договора с раздельными счетами. 
                Наименование и вид счета можно проверить в личном кабинете https://lk.finam.ru/ 
                2. Выставление «рыночных» и «лимитных» ордеров доступно только в рабочие дни в торговое время биржи.
        go!: /Ошибки заявок
                    
                    

    state: Драгметаллы || sessionResultColor = "#418614"
        intent!: /010 Драгметаллы
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Как купить/продать металлы" -> /Драгметаллы_Как купить
            "Комиссии при торговле металлами" -> /Драгметаллы_Комиссии
            "Вывод и поставка металлов" -> /Драгметаллы_Вывод и поставка металлов
            "Доступные инструменты" -> /Драгметаллы_Доступные
            "Ошибки при выставлении заявок" -> /Ошибки заявок

    state: Драгметаллы_Как купить
        a: В рамках валютного рынка доступны контракты на золото GLDRUB_TOM (1 лот = 1 грамм) и серебро SLVRUB_TOM (1 лот = 100 грамм)
            ✅ Для открытия позиции можно воспользоваться поиском в торговой системе, или выбрать инструмент из раздела «Валюты». Далее будет доступна опция «Заявка»
            ✅ Торги проводятся с 10:00 до 19:00 МСК
            ✅ Статус квал не требуется
        buttons:
            "Купить физическое золото" -> /Драгметаллы_Комиссии_Купить физическое золото
            "Назад" -> /Драгметаллы    

    state: Драгметаллы_Комиссии_Купить физическое золото || sessionResultColor = "#CD4C2B"
        a: Клиенты «Финам» могут купить запатентованные золотые слитки и монеты 999,9 пробы и любого веса по индивидуальной цене.
            ✅ Подробная информация в [презентации|https://www.finam.ru/dicwords/file/files_chatbot_zolotopresentation]
            ✅ Чтобы получить консультацию и приобрести физическое золото, обратитесь к менеджеру поддержки.
            
    state: Драгметаллы_Комиссии
        a: При торговле контрактами на металлы возникает два вида комиссий: биржевая и брокерская.
            1. Комиссия биржи:
            ✅ золото — при покупке — 1 ₽, при продаже — 0,02%, но не менее 1 ₽
            ✅ серебро — при покупке — 0,006375%, но не менее 1 ₽, при продаже — 0,017875%, но не менее 1 ₽
            2. Комиссия брокера: 
            ✅ по тарифам «Единый дневной», «Инвестор» и «Дневной валютный» — 0,05% при обороте до 1 000 000 ₽ за торговую сессию
            ✅ «Стратег», «Консультационный» и «ФриТрейд» — 0,3% от оборота
            3. Маржинальная торговля (доступны только длинные позиции).
            Комиссия за займ — КС+8% 

    state: Драгметаллы_Вывод и поставка металлов
        a: ✅ Физический ввод и вывод металлов не осуществляется по счетам «Финам»
            ✅ Перевод контрактов на металлы между брокерскими счетами недоступен
            ✅ Клиенты компании могут купить запатентованные золотые слитки и монеты 999,9 пробы и любого веса по индивидуальной цене
        buttons:
            "Купить физическое золото" -> /Драгметаллы_Комиссии_Купить физическое золото

    state: Драгметаллы_Доступные
        a: В режиме _ТОМ доступны контракты на золото GLDRUB_TOM (1 лот = 1 грамм) и серебро SLVRUB_TOM (1 лот = 100 грамм).
                Торги другими драгоценными металлами (платина и палладий) биржа не проводит.

    state: Маржа || sessionResultColor = "#15952F"
        intent!: /011 Маржа
        a: ❗ В ближайшее время ставки риска по ценным бумагам на иностранных биржах и валютам будут повышаться в связи с требованием рыночной конъюктуры.
        a: Маржинальная торговля — операции с использованием заемных средств брокера. [Подробнее о маржинальной торговле|https://www.finam.ru/landings/attestation-margin-trading/]
            ❗ Важно:
            ✅ Использовать маржинальную торговлю можно на фондовом, срочном, валютном рынках и рынке драгоценных металлов. 
            ✅ [Информация о доступной сумме займа и инструментам|https://www.finam.ru/documents/commissionrates/marginal/], по которым разрешено открывать короткие позиции
            ✅ На Бирже HKEX маржинальная торговля недоступна
            ✅ На Бирже СПБ маржинальная торговля доступна с ограниченным рядом инструментов, открытие коротких позиций доступно только через терминалы TRANSAQ и FinamTrade
            ✅ На срочном рынке для открытия позиций необходимо иметь на счете сумму средств, равную гарантийному обеспечению
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Уровни риска (КСУР/КПУР)" -> /Маржа_Уровни риска
            "Ставки риска по инструментам" -> /Маржа_Ставки риска
            "Подключить/отключить маржинальную торговлю" -> /Маржа_Подключить отключить
            "Комиссия за маржинальную торговлю" -> /Комиссии_Другие_Маржинальная
            "Уровень маржи по счету" -> /Маржа_Уровень маржи
            "Принудительное закрытие" -> /Маржа_Принудительное закрытие
            
    state: Маржа_Уровни риска || sessionResultColor = "#B65A1E"
        a: В «Финам» две группы (два уровня) риска.
            ✅ При открытии брокерского счета инвестору по умолчанию присваивается стандартный уровень риска (КСУР). 
            ✅ При соблюдении одного из условий, инвестор может получить категорию повышенного уровня риска (КПУР):
            1. Иметь активы стоимостью от 600000 ₽, быть клиентом брокера в течение последних 180 дней и заключать сделки с ценными бумагами или производными финансовыми инструментами на протяжении пяти и более дней.  
            2. Иметь активы стоимостью от 3000000 ₽. 
            Инвесторы с уровнями риска КСУР и КПУР имеют разные ставки маржинального обеспечения, для КПУР применяются ставки ниже, чем для КСУР.  
            Уровень КПУР дает больше возможностей для наращивания маржинальных позиций (размера плеча), но повышает финансовые риски.

    state: Маржа_Ставки риска || sessionResultColor = "#B65A1E"
        a: ❗ В ближайшее время ставки риска по ценным бумагам на иностранных биржах и валютам будут повышаться в связи с требованием рыночной конъюктуры.
        a: Ставка риска — это показатель, который используется для расчета маржинального обеспечения по конкретному активу. С помощью ставки риска брокер рассчитывает обеспечение позиции — то есть сумму, которую инвестору нужно иметь на счете, чтобы открыть или удерживать непокрытую позицию.
            Ставки риска по финансовым инструментам можно посмотреть на сайте:
            ✅ [для КСУР|https://zaoik.finam.ru/documents/commissionrates/marginal/ksur/] 
            ✅ [для КПУР|https://zaoik.finam.ru/documents/commissionrates/marginal/kpur/] 
            ❗ Ставки риска могут отличаться на сайте и в торговых системах в зависимости от рыночной ситуации. Самую актуальную информацию по ставкам можно узнать в торговой системе TRANSAQ в «описании инструмента», а также у менеджера «Финам».
      
    state: Маржа_Подключить отключить
        a: ✅ Для подключения достаточно пройти тестирование по категории [«Необеспеченные сделки»|https://lk.finam.ru/user/invest-status/qual-exam/tests]
            В течение дня функция активируется автоматически.
            ✅ Для отключения нужно обратиться к менеджеру компании. Самостоятельно отключить маржинальную торговлю через личный кабинет и торговый терминал нельзя.  
            При отключении маржинальной торговли будет заблокирована возможность использования заемных средств брокера, а также доступ к коротким позициям (шортам).
            ❗ По счетам с услугой «Финам Автоследование» невозможно отключить маржинальную торговлю.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Маржа_Уровень маржи
        a: 1. Информацию о состоянии портфеля, значениях маржи и запас портфеля до принудительного закрытия можно посмотреть в [личном кабинете|https://lk.finam.ru/details] — выбрать раздел «Детали» — раскрыть строку «Показатели риска»
            2. В терминале FinamTrade начальные требования, суммарную оценку денежных средств, ценных бумаг и обязательств клиента можно посмотреть в разделе «Аналитика» по счету (прокрутить ниже), в мобильном приложении FinamTrade – в разделе «Детали» по счету. 
            3. В терминале QUIK следить за маржинальными требованиями можно с помощью таблицы «Клиентский портфель»: «Создать окно» → «Все типы окон» → «Клиентский портфель».  Чтобы добавить необходимые графы, нажмите правой кнопкой мыши по таблице и выберите «Редактировать таблицу».
            4. В терминале MetaTrader 5 в строке «Баланс» показатели «Активы, Маржа, Уровень маржи, Первоначальная маржа, Поддерж. маржи» будут отображаться только при открытых позициях на фондовой и валютной секциях. В случае, если торговля ведется только по фьючерсным контрактам, то за показателями риска можно следить через личный кабинет.

    state: Маржа_Принудительное закрытие
        a: ❗ Использование заёмных средств (плеча) ведёт к увеличению доходности и риска по сравнению с торговлей на собственные средства.
            1. Чтобы избежать получение маржин-колла, важно следовать следующим рекомендациям:
            ✅ не заходите в позиции с максимальным плечом;
            ✅ регулярно отслеживайте состояние своих маржинальных позиций и не допускайте снижение оценки счета ниже уровня Минимальных требований;
            ✅ сокращайте позиции и/или вовремя пополняйте счет.
            2. Принудительное закрытие производится согласно [регламенту брокерского обслуживания|http://zaoik.finam.ru/broker/regulations]
            3. Возможные причины принудительного закрытия позиций:
            ✅ Резкие колебания рыночных цен, которые
            повлекли уменьшение стоимости вашего портфеля ниже уровня Минимальных требований (Минимальной маржи).
            ✅ Отсутствие подписанного заявления на поставку базового актива по фьючерсу на ценные бумаги перед экспирацией поставочных фьючерсов на Московкой бирже (FORTS).
            ✅ По ряду ценных бумаг брокер закрывает короткие позиции на дату дивидендной отсечки.
            За короткие позиции по российским ЦБ, не закрытым на дату отсечки, оплачивается штраф в размере дивидендов.
            ✅ Принудительное закрытие позиции может быть вызвано требованиями нормативных актов (например, перед корпоративным действием эмитента).
            ✅ Принудительное закрытие может быть вызвано изменением значений ставок риска, рассчитываемых
            клиринговой организацией.

    state: КВАЛ || sessionResultColor = "#15952F"
        intent: /012 КВАЛ
        
        # script:
        #     if ( typeof $parseTree._kval_redirect != "undefined" ){
        #         $session.kval_redirect = $parseTree._kval_redirect;
        #         $reactions.transition("/КВАЛ_" + $session.kval_redirect.name);
        #     }
            
        a: Статус квалифицированного инвестора открывает доступ к большему количеству финансовых инструментов: 
            ✅ зарубежные ценные бумаги
            ✅ инструменты срочного рынка на биржах США
            ✅ расширенный список инструментов на российских биржах 
            ❗ Для работы с опционами на ММВБ статус квалифицированного инвестора не требуется
            ❗ Неквалифицированным инвесторам по иностранным активам доступно только закрытие позиций через отдел голосового трейдинга, системы TRANSAQ и QUIK
            ✅ [Проверить ваш инвестиционный статус (квал/неквал инвестор) можно в личном кабинете|https://lk.finam.ru/user/invest-status]
            ❗ Проверить необходимость статуса для торговли определенным инструментом можно с помощью кнопки меню «Проверка инструмента на КВАЛ»
        a: Пожалуйста, выберите один из предложенных вариантов: 
        buttons:
            "Как получить статус КВАЛ" -> /КВАЛ_Статус
            "Заказ выписки из реестра КВАЛ" -> /КВАЛ_Документы
            "Тестирование для неквалифицированных инвесторов" -> /КВАЛ_Тестирование
            "Смена инвестиционного профиля" -> /КВАЛ_Смена профиля
            "Проверка инструмента на КВАЛ" -> /КВАЛ_Проверка инструмента
    
    state: КВАЛ_Статус
        a: Cтатус квалифицированного инвестора можно получить при соответствии одному из условий:
            ✅ владение активами и средствами на сумму от 6000000 ₽
            ✅ торговый оборот от 6000000 ₽ и наличие сделок за последние 4 квартала: не реже 10 сделок в квартал и не менее 1 сделки в месяц 
            ✅ диплом об образовании от организации, аккредитованной ЦБ РФ 
            ✅ квалификация в сфере экономики и финансов 
            ✅ опыт работы в организации, связанный с совершением сделок с финансовыми инструментами
            ✅ наличие статуса у другого брокера
        a: Выберите подходящий вариант:
        buttons:
            "Активы и средства во владении" -> /КВАЛ_Статус_Активы
            "Оборот по брокерским счетам" -> /КВАЛ_Статус_Оборот
            "Опыт работы" -> /КВАЛ_Статус_Опыт работы
            "Образование" -> /КВАЛ_Статус_Образование
            "Статус от другого брокера" -> /КВАЛ_Статус_Другой брокер
            "Назад" -> /КВАЛ
    
    state: КВАЛ_Статус_Активы
        a: Владеете активами или денежными средствами на брокерских и банковских счетах на общую сумму от 6000000 ₽. При определении общей стоимости активов также учитываются финансовые инструменты, переданные в доверительное управление.
            1. Чтобы получить статус квал по количеству активов на сумму от 6000000 ₽, необходимо предоставить следующие документы по типу активов:
            → Если хотите заявить актив Ценные бумаги – нужна  Выписка со счета ДЕПО (там указаны затраты на приобретенные Ценные бумаги) либо Выписка по лицевому счету в реестре,
            → Деньги на банковских счетах – Выписка с банковского счета с паспортными данными,
            → Деньги на брокерских счетах – брокерский отчет или справка об активах + дополнительный документ, в котором будут номер брокерского счета и паспортные данные. (Как правило, это справка об открытии счета).
            2. Общие требования к документам:
            → Документы в электронном нередактируемом виде с печатью и подписью.
            → Все документы на одну дату, не старше 5 рабочих дней.
            → В документах обязательно указаны паспортные данные, вместо них допустимо указание адреса регистрации или ИНН. 
            3. Направить документы можно в этом чате либо на электронную почту service@corp.finam.ru
        buttons:
            "Назад" -> /КВАЛ_Статус

    state: КВАЛ_Статус_Оборот
        a: 1. Критерии присвоения статуса Квалифицированный инвестор по обороту:
            ✅ За последние 4 квартала торговый оборот не менее 6000000 ₽, в каждом квартале не менее 10 сделок. В каждом месяце не менее 1 сделки. 
            ✅ Можно заявить суммарный оборот у нескольких брокеров.
            ✅ Нужно предоставить скан брокерского отчета с подписью и печатью брокера + дополнительный документ с паспортными данными и номером брокерского счета из отчета.
            2. Общие требования к документам:
            ✅ Документы в электронном нередактируемом виде с печатью и подписью.
            ✅ Все документы на одну дату, не старше 5 рабочих дней.
            ✅ В документах обязательно указаны паспортные данные, вместо них допустимо указание адреса регистрации или ИНН. 
            3. Направить документы можно в этом чате либо на электронную почту service@corp.finam.ru
        buttons:
            "Торговый оборот" -> /Справка_Торговый оборот
            "Назад" -> /КВАЛ_Статус

    state: КВАЛ_Статус_Опыт работы
        a: 1. Имеете опыт работы от двух лет, непосредственно связанный с совершением сделок с финансовыми инструментами, подготовкой индивидуальных инвестиционных рекомендаций, управления рисками, связанными с совершением указанных сделок, в российской и (или) иностранной организации.
            2. Имеете опыт работы от трех лет в должности, при назначении (избрании) на которую в соответствии с федеральными законами требовалось согласование с Банком России. 
            3. Чтобы подтвердить опыт работы, предоставьте скан документа:
            → Трудовой книжки;
            → Трудовой договор (если в документах отсутствует описание деятельности, нужно приложить должностную инструкцию);
            → Уведомление о согласовании Банком России кандидата на должность, которая требовала согласования по действующему законодательству.
            4. Направить документы можно в этом чате либо на электронную почту service@corp.finam.ru
        buttons:
            "Назад" -> /КВАЛ_Статус

    state: КВАЛ_Статус_Образование
        a: 1. У Вас есть диплом о высшем экономическом образовании государственного образца РФ, выданный организацией, которая на момент выдачи диплома осуществляла аттестацию граждан в сфере профессиональной деятельности на рынке ценных бумаг. Проверить аккредитацию ВУЗа можно на сайте ЦБ РФ: https://www.cbr.ru/vfs/finmarkets/files/supervision/list_Accred_org.xlsx
            2. Вы имеете квалификацию в сфере финансовых рынков, подтвержденную свидетельством.
            3. У Вас есть международный сертификат: 
            → Chartered Financial Analyst (CFA)
            → Certified International Investment Analyst (CIIA)
            → Financial Risk Manager (FRM)
            Важно! Сертификаты ФСФР не принимаются для присвоения статуса Квалифицированный инвестор, в связи с изменением законодательства с 01.10.2021 года.
            4. Чтобы подтвердить наличие образования или квалификации, необходимо предоставить сканы документов: 
            → Диплом государственного образца РФ о высшем экономическом образовании
            → Свидетельство о квалификации
            → Международный сертификат
            5. Направить документы можно в этом чате либо на электронную почту service@corp.finam.ru
        buttons:
            "Назад" -> /КВАЛ_Статус

    state: КВАЛ_Статус_Другой брокер
        a: Если у вас уже есть статус квалифицированного инвестора у другого брокера, то можно подтвердить его, предоставив выписку из реестра квалифицированных инвесторов.
            ✅ Требования к выписке:
            1. не старше 5 рабочих дней,
            2. указаны паспортные данные,
            3. указание на совершение всех видов сделок со всеми финансовыми инструментами для квалифицированного инвестора,
            4. наличие незаполненного поля «Исключен из реестра»,
            5. подпись/печать уполномоченного лица.
            ✅ Направить документы можно в этом чате либо на электронную почту service@corp.finam.ru 
            ❗ Выписки от «Тинькофф Инвестиций» не принимаются по причине отсутствия указания на виды услуг (совершение всех типов сделок и операций), в отношении которых лицо признано квалифицированным инвестором. Рекомендуется подтвердить инвестиционный статус другим способом.
        buttons:
            "Назад" -> /КВАЛ_Статус

    state: КВАЛ_Документы
        a: Заказ выписки из реестра квалифицированных лиц «Финам» доступен в личном кабинете [по ссылке|https://edox.finam.ru/orders/QualifiedInvestorRequestStatementStatus].
        buttons:
            "Назад" -> /КВАЛ

    state: КВАЛ_Тестирование
        a: Прохождение всех тестирований для неквалифицированных инвесторов не подразумевает присвоение статуса квалифицированного инвестора, но открывает доступ ко многим категориям инструментов. 
            ✅ [Пройти тестирование в личном кабинете|https://lk.finam.ru/user/invest-status/qual-exam/tests]
            ✅ Доступ к инструментам и сделкам предоставляется сразу после прохождения тестирования, для этого нужно подписать результат прохождения тестирования в личном кабинете в [разделе «Результаты»|https://lk.finam.ru/user/invest-status/qual-exam/tests]
            ❗ Доступ к инструментам и сделкам в системе QUIK предоставляется со следующей торговой сессии.
            ✅ Чтобы подготовиться к тестированию, воспользуйтесь [учебными материалами|https://www.finam.ru/landings/attestation-main/]
        buttons:
            "Назад" -> /КВАЛ

    state: КВАЛ_Смена профиля || sessionResultColor = "#B65A1E"
        a: Инвестиционный профиль (риск-профиль) – характеристика инвестора, его своеобразный портрет, описывающий поведение на финансовом рынке, готовность принимать риски.
            ✅ [Смена риск-профиля доступна в личном кабинете|https://lk.finam.ru/user/invest-profile]
        buttons:
            "Назад" -> /КВАЛ

    state: КВАЛ_Проверка инструмента
        InputText: 
            prompt = Укажите тикер инструмента - краткое название финансового инструмента на бирже, например: SBER.
            varName = tiker
            then = /КВАЛ_Проверка инструмента_Выбор биржи
            actions = [{"buttons": [{"name": "Назад","transition": "/КВАЛ"}],"type": "buttons"}]

    state: КВАЛ_Проверка инструмента_Выбор биржи
        a: Выберите биржу
        buttons:
            "Московская биржа" -> /КВАЛ_Проверка инструмента_Определение биржи
            "Биржа СПБ" -> /КВАЛ_Проверка инструмента_Определение биржи
            "Гонконгская биржа HKEX" -> /КВАЛ_Проверка инструмента_Определение биржи
            "Биржи США" -> /КВАЛ_Проверка инструмента_Биржи США
            "Назад" -> /КВАЛ_Проверка инструмента

    state: КВАЛ_Проверка инструмента_Биржи США
        a: Выберите биржу США
        buttons:
            "NYSE" -> /КВАЛ_Проверка инструмента_Определение биржи
            "NASDAQ" -> /КВАЛ_Проверка инструмента_Определение биржи
            "USA OTC" -> /КВАЛ_Проверка инструмента_Определение биржи
            "Назад" -> /КВАЛ_Проверка инструмента_Выбор биржи

    state: КВАЛ_Проверка инструмента_Определение биржи
        script:
            $session.exchange = getExchangeVariable($request.query);
            $session.exchangeText = getExchangeName($request.query);
            $reactions.transition("/КВАЛ_Проверка инструмента_Запрос на получение квала0");

    state: КВАЛ_Проверка инструмента_Запрос на получение квала0
        script:
            
            if($session.exchange[0] == undefined){
                $reactions.transition("/КВАЛ_Проверка инструмента_Неверный тип инструмента");
            } else {
                $session.exchangeVal = $session.exchange[0];
                $session.exchangeValState= "КВАЛ_Проверка инструмента_Запрос на получение квала1";
                $reactions.transition("/Отправка запроса КВАЛ");
            }
       
    state: КВАЛ_Проверка инструмента_Запрос на получение квала1
        script:
            
            if($session.exchange[1] == undefined){
                $reactions.transition("/КВАЛ_Проверка инструмента_Неверный тип инструмента");
            } else {
                $session.exchangeVal = $session.exchange[1];
                $session.exchangeValState= "КВАЛ_Проверка инструмента_Запрос на получение квала2";
                $reactions.transition("/Отправка запроса КВАЛ");
            }
       
    state: КВАЛ_Проверка инструмента_Запрос на получение квала2
        script:
            
            if($session.exchange[2] == undefined){
                $reactions.transition("/КВАЛ_Проверка инструмента_Неверный тип инструмента");
            } else {
                $session.exchangeVal = $session.exchange[2];
                $session.exchangeValState= "КВАЛ_Проверка инструмента_Неверный тип инструмента";
                $reactions.transition("/Отправка запроса КВАЛ");
            }
            
    state: Отправка запроса КВАЛ
        # url = https://cbdev.finam.ru/grpc-json/txscreener/v1/qualifiedInvestor # дев контур
        HttpRequest: 
            url = https://ftrr01.finam.ru/grpc-json/txscreener/v1/qualifiedInvestor
            method = PUT
            body = {"id": {"tickerMic": {"ticker": "{{$session.tiker}}","mic": "{{$session.exchangeVal}}"}}}
            timeout = 100
            headers = [{"name":"Authorization","value": "{{$injector.api_key_kval}}"},{"name":"x-shard","value":"ftrr01-dev"},{"name":"Content-Type","value":"application/json"}]
            vars = [{"name":"isQualifiedInvestor","value":"$httpResponse.isQualifiedInvestor"}]
            okState = /КВАЛ_Проверка инструмента_Определение КВАЛА
            errorState = /{{$session.exchangeValState}}
        

    state: КВАЛ_Проверка инструмента_Определение КВАЛА
        if: $session.isQualifiedInvestor == true
            a: Для открытия позиций по данному инструменту на {{$session.exchangeText}} требуется статус квалифицированного инвестора.
        else: 
            a: Для открытия позиций по данному инструменту на {{$session.exchangeText}} статус квалифицированного инвестора не нужен.
        buttons:
            "Проверить другой инструмент" -> /КВАЛ_Проверка инструмента
            "Назад" -> /КВАЛ_Проверка инструмента_Выбор биржи
        script:
            $session.exchange = {}
            $session.exchangeText = {}

    state: КВАЛ_Проверка инструмента_Неверный тип инструмента
        a: Информация по указанному инструменту на данном рынке отсутствует. Рекомендуем проверить корректность указанного тикера и выбранную торговую площадку.
        buttons:
            "Выбрать другой тикер" -> /КВАЛ_Проверка инструмента
            "Выбрать другую биржу" -> /КВАЛ_Проверка инструмента_Выбор биржи

    state: Депозитарное поручение || sessionResultColor = "#D93275"
        intent!: /013 Депозитарное поручение
        a: ❗ После перевода СПБ Биржей бумаг на неторговый раздел, бумаги исключены из торговых лимитов биржи, и не отображаются в терминале, но их наличие отражено во вкладке «Портфель» в [личном кабинете|https://lk.finam.ru/].
            ❗ Заблокированные бумаги, которые учитывались в СПБ Банке на субсчетах с разделами типа BN, были переведены 15.02.2024 на новый счет депо типа Y в СПБ Банке, данная операция отображается в [«Истории»|https://lk.finam.ru/] по счету как «Перевод ценных бумаг». Нажмите кнопку ниже, чтобы узнать подробнее.
        a: ✅ Заявки на ввод/вывод ценных бумаг (ЦБ) можно сформировать дистанционно в личном кабинете, в разделе [«Операции с ценными бумагами»|https://edox.finam.ru/orders/capitalIssuesTransfer/selectAccount.aspx]
            ✅ Реквизиты депозитарного счета для ввода/вывода ценных бумаг также доступны в [личном кабинете|https://lk.finam.ru], в разделе «Детали» по счету.
            ✅ Статус перевода ЦБ можно отслеживать в [«Журнале поручений»|https://lk.finam.ru/reports/documents]
            ✅ Выберите интересующую тему, чтобы узнать подробнее:
        buttons:
            "Перевод бумаг от другого брокера" -> /Депозитарное поручение_Перевод другой брокер
            "Перевод бумаг между счетами" -> /Депозитарное поручение_Перевод между счетами
            "Перевод бумаг между разделами ДЕПО" -> /Депозитарное поручение_Перевод ДЕПО
            "Предоставить затратные документы" -> /Депозитарное поручение_Предоставить документы
            "Комиссии за депозитарные операции" -> /Депозитарное поручение_Комиссии
            "Счет депо типа «Y»" -> /Депозитарное поручение_Счеn депо типа Y
            "Ещё" -> /Депозитарное поручение_Ещё

    state: Депозитарное поручение_Перевод другой брокер || sessionResultColor = "#BC3737"
        a: ✅ Как перевести бумаги от другого брокера в «Финам»?
            Шаг 1: в личном кабинете https://lk.finam.ru нужно взять   депозитарные реквизиты в разделе «Детали» по счету.
            Шаг 2: по данным реквизитам подать поручение на вывод активов у другого брокера.
            Шаг 3: подать поручение на ввод ценных бумаг в личном кабинете по ссылке: https://edox.finam.ru/orders/capitalIssuesTransfer/selectAccount.aspx  
            ❗ Если в поручении на вывод есть референс, то в поручении на ввод должен быть идентичный референс.
            Шаг 4: после успешного перевода бумаг нужно предоставить документы в «Финам», подтверждающие стоимость активов (выписка об операциях со счета ДЕПО с момента приобретения и брокерский отчет с ценой приобретения).
            ✅ «Финам» не взимает комиссию за ввод ценных бумаг, комиссия может взиматься со стороны брокера-отправителя.
            ❗ Для успешного перевода необходимо обеспечить на счетах сумму, нужную для оплаты комиссий.
            ✅ Статус перевода ЦБ можно отслеживать в «Журнале поручений» по ссылке: https://lk.finam.ru/reports/documents 
            ✅ Сроки перевода в среднем до 7 рабочих дней. Перевод может проходить дольше по причине задержки поручения от брокера-отправителя. Если встречное поручение не поступило в течение 10 дней, либо были допущены ошибки, то поручение будет отменено.
            ✅ Перевести можно акции, купленные на Российских биржах. ПФИ (фьючерсы и опционы) перевести невозможно. Перевести можно только поставленные активы (позиции с займом не переводятся).
            ✅ Вы можете перевести заблокированные ценные бумаги. Продать на бирже их возможности не будет, так как они   останутся заблокированными. Но у вас будет возможность продать их на   внебиржевом рынке с дисконтом.
            ✅ Чтобы перевести бумаги для «квалов» предварительно необходимо получить статус квалифицированного инвестора в «Финам».
        buttons:
            "Как получить статус КВАЛ" -> /КВАЛ_Статус

    state: Депозитарное поручение_Перевод между счетами
        a: 1. Поручение на перевод ценных бумаг между своими счетами можно подать в личном кабинете по ссылке: https://edox.finam.ru/orders/AccountSecurityTransfer/Index  Выберите счет списания, затем счет зачисления и ценные бумаги с указанием их количества, которые хотите перевести.
            ✅ Перевод ценных бумаг между своими счетами осуществляется без комиссии.
            ❗ Для подачи поручения на перевод активов со счета ИИС нужно обратиться к менеджеру. После исполнения поручения счет ИИС будет расторгнут.
            ✅ Статус перевода ЦБ можно отслеживать в «Журнале поручений»: https://lk.finam.ru/reports/documents 
            2. Для перевода бумаг между разными клиентами «Финам» с передающей стороны нужно подать поручение на вывод ЦБ, а с принимающей стороны — на ввод ЦБ, депозитарная комиссия 150 ₽ за поручение с каждой стороны.
            ✅ Подача поручения на прием/снятие ценных бумаг со сменой прав собственности по ссылке: https://edox.finam.ru/orders/capitalIssuesTransfer/selectAccount.aspx 
            Выберите счет списания, прием/снятие из/в депозитарий другого брокера, следуйте инструкции в личном кабинете, далее вы сможете приложить документы-основания для перевода со сменой владельца ЦБ.
            ✅ Статус перевода ЦБ можно отслеживать в «Журнале поручений»: https://lk.finam.ru/reports/documents

    state: Депозитарное поручение_Перевод ДЕПО
        a: Подать поручение на перемещение ценных бумаг между торговыми разделами или площадками (биржами) можно через личный кабинет по ссылке: https://edox.finam.ru/Orders/ChangeSecurityPlace

    state: Депозитарное поручение_Предоставить документы
        a: Важно предоставить затратные документы по введенным ценным бумагам до конца года, в котором они были проданы.
            ✅ Уведомление с перечнем необходимых документов будет отображено в личном кабинете после ввода ценных бумаг по ссылке: https://edox.finam.ru/info/debts.aspx 
            Стандартный перечень документов:
            1. Справка по счету (с отображением даты и цены покупки).
            2. Выписка об операциях по счету ДЕПО/в регистраторе (с момента покупки и до момента вывода).
            3. Договор купли-продажи/дарения/обмена (если был факт передачи прав собственности).
            ❗ Могут быть запрошены дополнительные документы для подтверждения факта затрат на введенные ценные бумаги.

    state: Депозитарное поручение_Комиссии
        a: Пожалуйста, выберите вид операции:
        buttons:
            "Перевод бумаг между счетами" -> /Депозитарное поручение_Комиссии_Перевод
            "Ввод ценных бумаг" -> /Депозитарное поручение_Комиссии_Ввод
            "Вывод ценных бумаг" -> /Депозитарное поручение_Комиссии_Вывод
            "Назад" -> /Депозитарное поручение

    state: Депозитарное поручение_Комиссии_Перевод
        a: ✅ Перевод ценных бумаг между своими счетами осуществляется без комиссии.
            ✅ При переводе ценных бумаг между разными клиентами «Финам» — комиссия 150 ₽ за каждое поручение с каждой стороны.

    state: Депозитарное поручение_Комиссии_Ввод
        a: ✅ За прием ценных бумаг на брокерские счета в «Финам» комиссия не удерживается. Однако другие брокеры могут взимать комиссию за вывод активов. Рекомендуем уточнять эту информацию заранее.
            ✅ При переводе ценных бумаг между разными клиентами «Финам» — комиссия 150 ₽ за каждое поручение с каждой стороны.

    state: Депозитарное поручение_Комиссии_Вывод
        a: 1. За вывод ценных бумаг депозитарий «Финам» удерживает 1000 ₽ за каждое поручение. Дополнительные комиссии взимаются вышестоящим депозитарием при переводе активов в депозитарий другого брокера:
            ✅ 65 ₽ — за поручение при переводе в рамках Мосбиржи;
            ✅ 75 ₽ — за поручение при переводе в рамках Биржи СПБ;
            ✅ 500 ₽ — при переводе ценных бумаг в регистратор. 
            2. При переводе ценных бумаг между разными клиентами «Финам» — комиссия 150 ₽ за каждое поручение с каждой стороны.
            
    state: Депозитарное поручение_Счеn депо типа Y
        a: Заблокированные иностранные ценные бумаги, которые учитывались в СПБ Банке на субсчетах с разделами типа BN, были переведены 15.02.2024 на новый счет депо типа Y в СПБ Банке. 
            ✅ Счет депо типа «Y» - счет депо, предназначенный для учета иностранных ценных бумаг и открываемый исключительно участникам клиринга, депозитариям таких участников клиринга, клиентам участника клиринга и иным лицам, у которых на дату открытия счетов депо типа «Y» имеются остатки иностранных ценных бумаг на разделах «Неторговый» субсчетов депо. 
            ✅ Данный счет открывается под возможную разблокировку активов, куда по инициативе СПБ Биржи переводятся бумаги. [Подробнее|https://spbexchange.ru/ru/about/news.aspx?bid=25&news=45859]
            ✅ Открытие счета депо типа «Y», регулируется Условиями осуществления депозитарной деятельности ПАО «СПБ Банк».
            ❗ Если в [«Истории»|https://lk.finam.ru/] по счету отображается частичный перевод бумаг, то это означает, что остальные заблокированные акции хранились на разделе неторгового счета, где данный перевод не требовался, или был уже произведен ранее, при предыдущих блокировках.

    state: Депозитарное поручение_Ещё
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Перевод активов из Банка в АО Финам" -> /Депозитарное поручение_Ещё_Перевод активов
            "Разблокировка ЦБ после конвертации ДР" -> /Депозитарное поручение_Ещё_Разблокировка ЦБ
            "Назад" -> /Депозитарное поручение

    state: Депозитарное поручение_Ещё_Перевод активов || sessionResultColor = "#CD4C2B"
        a: «Финам» перешел к завершающему этапу перевода брокерских счетов из Банка «Финам» в инвестиционную компанию АО «Финам».
            1. Общие технические моменты необходимые для успешного перевода:
            ✅ если по счету имеются заемные позиции (плечи) – их необходимо закрыть,
            ✅ если по счету открыты позиции срочного рынка (Фьючерсы) – их также необходимо закрыть,
            ✅ если брокерский счет подключен к стратегии сервиса Comon – ее необходимо отключить, после перевода счета услугу можно будет подключить вновь.
            2. Перевод брокерского счета в АО «Финам» для клиентов банка осуществляется дистанционно и без комиссий.
            Сроки перевода активов: денежные средства, ценные бумаги (ММВБ) переводятся в течение 2-3 рабочих дней, иностранные ценные бумаги - в течение: 2-2,5 недель.
            ❗ При переводе активов будет произведен расчет актуальной налоговой базы и списание НДФЛ за текущий налоговый период.
            Для получения дополнительной информации и выгрузки поручений обратитесь к менеджеру «Финам».
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Депозитарное поручение_Ещё_Разблокировка ЦБ
        a: Чтобы снять ограничения с акций, полученных после расконвертации депозитарных расписок, нужно подать поручение на перемещение этих акций на торговый раздел через менеджера «Финам».
            ❗ Если расписки приобретены не через «Финам», то к поручению необходимо приложить документы, подтверждающие факт приобретения расписок.
            В связи с новым предписанием ЦБ РФ и Указом Президента про обособление ценных бумаг после конвертации АДР/ГДР на российские акции, введено ограничение в отношении акций, полученных после конвертации АДР, которые были приобретены после 01.03.2022 у недружественных нерезидентов. Сделки с такими акциями допускаются при наличии разрешений, выдаваемых Центральным банком или Правительственной комиссией.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Депозитарии || sessionResultColor = "#15952F"
        intent!: /014 Депозитарии
        a: На депозитарных счетах (счетах депо) учитываются только ценные бумаги: акции, облигации, депозитарные расписки, паи и ETF.
            Производные финансовые инструменты (фьючерсы, опционы, свопы) и валюта не подлежат депозитарному учету, т.к. не являются ценными бумагами.
            ✅ Место хранения ценной бумаги определяется биржей, на которой она приобреталась
            ✅ Для подтверждения наличия ценных бумаг на депозитарном счете можно заказать выписку со счёта ДЕПО 
            Выберите рынок, чтобы узнать подробнее:
        buttons:
            "Московская биржа" -> /Депозитарии_Московская биржа
            "Биржа СПБ" -> /Депозитарии_Биржа СПБ
            "Американский фондовый рынок NYSE/NASDAQ" -> /Депозитарии_Американский фондовый рынок
            "Гонконгская биржа HKEX" -> /Депозитарии_Гонконгская биржа
            "Депозитарные документы" -> /Документы_Депозитарные

    state: Депозитарии_Московская биржа
        a: Расчетным депозитарием Московской биржи является центральный депозитарий НРД – Национальный Расчетный Депозитарий.
            ✅ Для учета российских ЦБ у НРД открыты счета номинального держателя напрямую у Реестродержателей.
            ✅ Для учета ИЦБ, в том числе АДР, у НРД открыты счета в номинальном держании Euroclear и Clearstream. Это международные депозитарно-клиринговые компании, сайты депозитариев https://www.clearstream.com/clearstream-en/ и https://www.euroclear.com/en.html
            ❗ НРД не раскрывает конечного номинального держателя в данной цепочке, поэтому депозитарий не располагает информацией о внешнем депозитарии - Euroclear это или Clearstream.
            Актуальную информацию о местах расчетов по ЦБ можно посмотреть на сайте НРД: https://www.nsd.ru/services/depozitariy/operatsii-s-tsennymi-bumagami/vnebirzhevye-raschety/mesta-raschetov-po-tsennym-bumagam/

    state: Депозитарии_Биржа СПБ
        a: ✅ Для учета российских ЦБ в НРД счета номинального держателя открыты напрямую у Реестродержателей: ЦБ учитываются на счете депо клиента в номинальном держании АО «Финам». Клиринг по данному счету осуществляется через НКО-ЦК «СПБ Клиринг» (АО).
            Наименование счета в личном кабинете «Торговый, СПБ КЛИРИНГ НКО-ЦК АО».
            ✅ Для учета ИЦБ у расчетного депозитария СПБ Биржи ПАО «СПБ Банк» (до 2 июня 2022 г.  ПАО «Бест Эффортс Банк») открыт счет в международном центральном депозитарии (Euroclear/Clearstream) через номинального держателя НРД.
            В ПАО «СПБ Банк» внутри клирингового счета открыт субсчет номинального держателя АО «ФИНАМ», и внутри АО «ФИНАМ» открываются счета депо владельца ЦБ. 
            Наименование счета в личном кабинете «Торговый, СПБ КЛИРИНГ НКО-ЦК АО».
            ❗ НРД не раскрывает конечного номинального держателя в данной цепочке, поэтому депозитарий не располагает информацией о внешнем депозитарии - Euroclear это или Clearstream.

    state: Депозитарии_Американский фондовый рынок
        a: Ценные бумаги, приобретенные на американском фондовом рынке NYSE/NASDAQ, имеют место хранения DTCС (Depository Trust and Clearing Corporation).
            Подробнее: https://www.dtcc.com/about/businesses-and-subsidiaries/dtc

    state: Депозитарии_Гонконгская биржа
        a: Ценные бумаги, приобретенные на Гонконгской бирже HKEX имеют место хранения Hong Kong Exchanges and Clearing Limited
            Подробнее: https://www.hkex.com.hk/?sc_lang=en

    state: Движение ДС || sessionResultColor = "#15952F"
        intent!: /015 Движение ДС
        
        # script:
        #     if ( typeof $parseTree._dvijenieDS_redirect != "undefined" ){
        #         $session.dvijenieDS_redirect = $parseTree._dvijenieDS_redirect;
        #         $reactions.transition("/Движение ДС_" + $session.dvijenieDS_redirect.name);
        #     }
            
        a: Выберите тему вашего вопроса:
        buttons:
            "Пополнение счета" -> /Движение ДС_Пополнение
            "Перевод между счетами в «Финам»" -> /Движение ДС_Перевод
            "Вывод средств" -> /Движение ДС_Вывод
            "Когда пройдут расчеты по сделкам" -> /Режим расчетов

    state: Движение ДС_Пополнение
        a: Пополнение доступно в валюте рубль РФ, доллар США, китайский юань, белорусский рубль, казахстанский тенге
            ✅ [Пополнить счёт можно в личном кабинете|https://lk.finam.ru/deposit]
            ✅ [Комиссии и сроки зачисления средств|https://lk.finam.ru/commissions/deposit]
            ❗ В выходные дни сроки зачисления могут быть увеличены
            ❗ В торговых системах баланс обновляется в рабочее время бирж
            ✅ Перечень ваших действующих счетов с наименованием доступен в [личном кабинете|https://lk.finam.ru/]
        a: Выберите способ пополнения:
        buttons:
            "СБП" -> /Движение ДС_Пополнение_СБП
            "Банковской картой" -> /Движение ДС_Пополнение_Карта
            "По реквизитам" -> /Движение ДС_Пополнение_Реквизиты
            "Наличными в офисе Финам" -> /Движение ДС_Пополнение_Наличными
            "Назад" -> /Движение ДС

    state: Движение ДС_Пополнение_СБП
        a: В личном кабинете доступно [пополнение брокерского счета через систему быстрых платежей|https://lk.finam.ru/deposit/bank/quick]
            ❗ Для успешного пополнения данные отправителя и получателя должны совпадать (ФИО, номер телефона). Если у вас изменились ФИО или номер телефона, их можно изменить в разделе личного кабинета [«Изменение анкетных данных»|https://edox.finam.ru/Client/EditInfo]
            ❗ Пополнение со счетов третьих лиц недоступно.
            ✅ «Финам» не удерживает комиссию за данную операцию, но ее может удерживать банк-отправитель.
            ✅ Моментальное зачисление средств в течение рабочего дня с 10:00 до 19:00 МСК. При пополнении после 18:30 МСК, в выходные или праздничные дни деньги поступят в ближайший рабочий день.
            ❗ Платежи, поступившие после 21:00 МСК отобразятся в торговой системе после 6:30 МСК, после завершения технического обслуживания торговых серверов.
        buttons:
            "Другой способ" -> /Движение ДС_Пополнение

    state: Движение ДС_Пополнение_Карта
        a: [Пополнение брокерского счета с помощью банковской карты|https://lk.finam.ru/deposit/card/new]
            ✅ Зачисление средств происходит в течение 1 рабочего дня (с 10:00 до 19:00 МСК)
            ✅ Пополнение с карты Банка «Финам» – бесплатно
            ✅ При пополнении с карты стороннего банка комиссия за первое пополнение не взимается, за второе и последующее — 1%, минимум 50 ₽
            ❗ Комиссию может удерживать ваш банк-отправитель
        buttons:
            "Другой способ" -> /Движение ДС_Пополнение    
    
    state: Движение ДС_Пополнение_Реквизиты
        a: [Пополнение брокерского счёта по реквизитам|https://lk.finam.ru/deposit/1528731/bank/requisites]
            ✅ Зачисление денежных средств в течение 3 рабочих дней, в выходные и праздничные дни зачисления не совершаются.
            ❗ Срок зачисления может быть увеличен при переводе иностранной валюты из-за проверки корректности платежа со стороны банков-корреспондентов.
            ✅ «Финам» не удерживает комиссию за ввод рублей РФ с банковских счетов физических лиц.
            ✅ Комиссия за ввод долларов США/евро со счетов Банка «Финам» не удерживается. При пополнении со счетов стороннего банка – 0,6%, но не менее 25 $/€ и не более 4000 $/€ (по счетам «Сегрегированный Global» не взимается). За ввод средств в других валютах комиссия не удерживается.
            ❗ При пополнении с банков Казахстана может понадобиться дополнительное соглашение.
            ✅ Прямые переводы с брокерских счетов в других организациях также производятся по реквизитам. Рекомендуем заранее уточнить у брокера-отправителя информацию об ограничениях и комиссиях за отправку средств.
        buttons:
            "Доп. соглашение для банков Казахстана" -> /Документы_Общие_Соглашение Казахстан
            "Другой способ" -> /Движение ДС_Пополнение
            
    state: Движение ДС_Пополнение_Наличными
        a: Пополнить брокерский счет наличными рублями РФ можно в кассах офисов Финам.
            Для этого понадобится действующий паспорт гражданина РФ. 
            ✅ [Адрес ближайшего офиса|https://www.finam.ru/about/contacts]
            ✅ Зачисление в течение 1 часа
            ✅ Комиссия – 0 ₽
        buttons:
            "Другой способ" -> /Движение ДС_Пополнение

    state: Движение ДС_Перевод
        a: Выберите необходимый тип перевода:
        buttons:
            "Перевод денежных средств между счетами" -> /Движение ДС_Перевод_Средств
            "Перевод бумаг между счетами" -> /Депозитарное поручение_Перевод между счетами
            "Назад" -> /Движение ДС

    state: Движение ДС_Перевод_Средств
        a: Поручение на перевод денежных средств можно [подать в личном кабинете|https://lk.finam.ru/deposit/finam]. Выберите счёт списания, затем счёт зачисления и сумму, которую хотите перевести.
            ✅ Поручения исполняются в течение дня, на практике, до получаса. 
            ✅ Перевод денежных средств между своими счетами осуществляется без комиссии.
            ❗ Для подачи поручения на перевод активов со счета ИИС нужно обратиться к менеджеру. После исполнения поручения счет ИИС будет расторгнут.
            ❗ Перевод денежных средств между счетами разных клиентов не осуществляется.

    state: Движение ДС_Вывод || sessionResultColor = "#CD4C2B"
        a: На банковские счета сторонних банков доступны выводы в валюте рубль РФ, доллар США, китайский юань и казахстанский тенге 
            ❗ Валютные резиденты РФ могут выводить валюту с брокерского счета только в банки РФ
            ✅ Подать поручение на вывод денежных средств можно в [личном кабинете|https://lk.finam.ru/withdraw]
            ✅ [Комиссии и сроки вывода средств|https://lk.finam.ru/commissions/withdraw]
            ✅ После исполнения вывода вам поступит уведомление от брокера с указанием фактической суммы вывода и удержанной суммы налога
        a: Выберите способ вывода средств:
        buttons:
            "СБП" -> /Движение ДС_Вывод_СБП
            "По реквизитам" -> /Движение ДС_Вывод_Реквизиты
            "Наличными в офисе Финам" -> /Движение ДС_Вывод_Наличными
            "Вывод онлайн 24/7" -> /Движение ДС_Вывод_24-7
            "Отменить вывод средств" -> /Перевод на оператора
            "Назад" -> /Движение ДС
    
    state: Движение ДС_Вывод_СБП
        a: Поручение на вывод денежных средств по СБП можно [подать в личном кабинете|https://lk.finam.ru/withdraw/bank/quick]
            ✅ Для успешного вывода через СБП действует ряд правил:
            — услуга доступна только для граждан РФ
            — данная услуга недоступна для счетов ИИС
            — сумма поручения в пределах от 1000 до 1000000 ₽
            — сумма поручения не может превышать 80% от активов, с учётом удерживаемого НДФЛ
            — не более одного вывода через СБП в день
            — комиссия — 100 ₽ за операцию
            ✅ Сроки зачисления средств:
            — если поручение подано до 17:00 МСК, то зачисление в течение одного рабочего дня
            — если поручение подано после 17:00 МСК, то зачисление на следующий рабочий день
            ❗ Вывод происходит не ранее проведения биржевых расчётов по сделкам в соответствии с режимом Т+1/Т+2.
        buttons:
            "Другой способ" -> /Движение ДС_Вывод

    state: Движение ДС_Вывод_Реквизиты
        a: Вывести средства безналичным платежом можно в рублях, долларах США, китайских юанях, гонконгских долларах.
            ✅ При выводе средств в иностранной валюте на счета в сторонних банках удерживается комиссия.
            ✅ При выводе валюты на счета в «Банке Финам» комиссия отсутствует. Но есть комиссия со стороны банка за зачисление и хранение долларов и евро.
            ✅ [Сформировать поручение можно в личном кабинете|https://lk.finam.ru/withdraw/bank]
            ❗ Вывод валюты на банковские счета за пределы РФ недоступен.
            ✅ Дополнительно можно воспользоваться опцией «Срочный вывод» рублей РФ (вывод происходит на 1 день раньше), стоимость 300 ₽ (по тарифам «Дневной СПБ», «Консультационный СПБ», «Cтратег US» комиссия 7,5 $).
            Ограничение по сумме вывода: от 1000 ₽ до 5000000 ₽ (но не более 80% от счета).
            ❗ Вывод происходит не ранее проведения биржевых расчётов по сделкам в соответствии с режимом Т+1/Т+2.
        buttons:
            "Комиссии" -> /Движение ДС_Вывод_Реквизиты_Комиссии
            "Сроки зачисления" -> /Движение ДС_Вывод_Реквизиты_Сроки
            "Другой способ" -> /Движение ДС_Вывод
                
    state: Движение ДС_Вывод_Реквизиты_Комиссии
        a: ✅ За вывод рублей РФ комиссия не взимается
                ✅ За вывод иностранной валюты физическим лицом на счета по реквизитам в сторонние банки взимаются комиссии:
                Доллары — 0,3% от суммы вывода, но не менее 30 $ и не более 150 $,
                Китайские юани — 0,07% от суммы вывода, но не менее 25 € и не более 100 €.
                Дополнительно удерживается комиссия за обработку поручений на вывод долларов:
                0,4% от суммы вывода, но не менее 1500 ₽ и не более 250000 ₽.
                Комиссии удерживается в рублях по курсу ЦБ РФ на дату исполнения вывода (по тарифам «Дневной СПБ», «Консультационный СПБ», «Cтратег US» комиссия удерживается в долларах). Комиссии не взимаются по счетам «Сегрегированный Global».
                ✅ За вывод иностранной валюты на счета в «Банке Финам» комиссия не взимается, но введена комиссия со стороны банка за зачисление долларов: 3% от суммы операции, но не менее 300 $/€ и не более суммы операции, по счетам в иных валютах – не взимается.
                ✅ Опция «срочный вывод» рублей (вывод происходит на 1 день раньше) стоит 300 ₽ (по тарифам «Дневной СПБ», «Консультационный СПБ», «Cтратег US» комиссия 7,5 $).
        buttons:
            "Назад" -> /Движение ДС_Вывод_Реквизиты
            
    state: Движение ДС_Вывод_Реквизиты_Сроки
        a: 1. Рубли:
                ✅ если поручение подано до 17:00 МСК, зачисление на следующий рабочий день
                ✅ если поручение подано после 17:00 МСК, зачисление через один рабочий день
                ✅ возможно досрочное исполнение вывода, если на счете завершены все расчеты по позициям
                2. Валюта:
                ✅ если поручение подано до 13:00 МСК, зачисление на следующий рабочий день
                ✅ если поручение подано после 13:00 МСК, зачисление через один рабочий день
                ✅ срочного вывода нет
        buttons:
            "Назад" -> /Движение ДС_Вывод_Реквизиты
    
    state: Движение ДС_Вывод_Наличными
        a: Оформлять и получать вывод средств наличными с брокерского счета можно только при условии, что вы уже подтверждали ранее свою личность при личном визите в офисе компании.
            ✅ Вывести наличными можно только рубли РФ
            ✅ [Подать поручение можно в личном кабинете|https://lk.finam.ru/withdraw/cash]
            ✅ Если поручение подано до 14:00 по местному времени, получить средства в кассе можно на следующий рабочий день
            ✅ Если поручение подано после 14:00 по местному времени, получить средства можно через один рабочий день
            ✅ Прийти в кассу можно после 13:30 МСК, в центральном офисе в Москве — после 11:00 МСК
            ✅ Комиссия за вывод наличными не удерживается
        buttons:
            "Другой способ" -> /Движение ДС_Вывод

    state: Движение ДС_Вывод_24-7 || sessionResultColor = "#CD4C2B"
        a: Чтобы не ожидать расчеты по сделкам и выводить деньги с брокерского счета в удобное для вас время, даже в выходные и праздничные дни, можно подключить услугу «Вывод 24/7». Денежные средства после продажи ценных бумаг можно мгновенно выводить на карту в Банке «Финам».
            ✅ Условия подключения и использования услуги:
            1. чтобы подключить услугу, нужно иметь действующую карту Банка «Финам»
            2. чтобы пользоваться услугой, нужно иметь на своих брокерских счетах сумму не менее 5000 ₽
            3. выводить можно только рубли РФ
            4. максимальная сумма вывода за один раз 100000 ₽
            5. услуга предоставляется бесплатно
            ✅ Алгоритм подключения и услуги:
            1. [подключить услугу можно в личном кабинете|https://lk.finam.ru/withdraw/overdraft] в рабочие дни с 10:00 до 18:00 МСК 
            2. подписать заявление на подключение услуги и индивидуальные условия для открытия специального счета с подключённым овердрафтом
            3. по факту подключения услуги поступает уведомление по SMS, с этого момента можно использовать данную услугу [в личном кабинете в разделе «Вывод»|https://lk.finam.ru/withdraw]
            4. после исполнения такого вывода, в ближайший рабочий день, либо, когда происходит полный расчёт по сделке (в период Т, Т+1), брокер исполняет поручение на вывод денежных средств и таким образом погашается сумма овердрафта
        buttons:
            "Открыть карту Банка Финам" -> /Банк_Банковская карта
            "Другой способ" -> /Движение ДС_Вывод

    state: Займ ЦБ || sessionResultColor = "#15952F"
        intent!: /016 Займ ЦБ
        a: Согласно п. 17.12 регламента брокер имеет право брать бумаги клиентов для внутреннего учета. Это не приводит к потере права реализации данной ценной бумаги. Вы в любой момент можете ее продать.
            Данная операция в обязательном порядке фиксируется в справке по счету, в разделе «Сделки РЕПО, сделки СВОП, сделки займа ЦБ». 
            За предоставление бумаг для внутреннего учета вы получаете дополнительное вознаграждение — 0,05% годовых от стоимости ценных бумаг.
            Если ценные бумаги находились на внутреннем учете компании в момент дивидендной отсечки, брокер возместит вам сумму дивидендов, увеличенную в 1,15 раза. 
            Если вы планируете участвовать в собрании акционеров, обратитесь к менеджеру «Финам» за несколько дней до даты фиксации и установите запрет на использование ваших ценных бумаг на период корпоративного события.

    state: Документы и справки || sessionResultColor = "#15952F"
        intent!: /017 Документы и справки
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Общие документы" -> /Документы_Общие
            "Налоговые документы" -> /Документы_Налоговые
            "Депозитарные документы" -> /Документы_Депозитарные
   
    state: Документы_Общие
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Регламент брокерского обслуживания" -> /Документы_Общие_Регламент
            "Документы об открытии счета" -> /Документы_Общие_Открытие счета
            "Справка по счету" -> /Справка по счету
            "Справка об активах" -> /Документы_Депозитарные_Справка об активах
            "Реквизиты счетов" -> /Документы_Общие_Реквизиты
            "Доп. соглашение для банков Казахстана" -> /Документы_Общие_Соглашение Казахстан
            "Согл. для торговли иностранными активами" -> /Документы_Общие_Согл для торговли иностранными активами
            "Назад" -> /Документы и справки

    state: Документы_Общие_Регламент
        a: ✅ Регламент «Финам» представлен в открытом доступе на [сайте компании|https://www.finam.ru/services/OpenAccount0000A/]
            ✅ Обновления регламента можно отслеживать на [на главной странице сайта finam.ru|https://www.finam.ru/] в информационном блоке «Анонсы». Страница сайта разделена на блоки, для вашего удобства вы можете заменить любой блок на нужный новостной раздел нажатием на стрелку в верхнем правом углу блока. 
            Чтобы найти раздел «Анонсы», пролистните страницу сайта вниз либо настройте удобный блок на отображение раздела «Анонсы».
            Наиболее популярные пункты регламента:
        buttons:
            "п. 6.1.6 регламента" -> /Документы_Общие_Регламент_П 6.1.6
            "п. 17.12 регламента" -> /Займ ЦБ
            "п. 16 регламента" -> /Комиссии_Другие_Автоследование
            "п. 14.4 регламента" -> /Документы_Общие_Регламент_П 14.4
            "п. 4.4 регламента" -> /Документы_Общие_Регламент_П 4.4

    state: Документы_Общие_Регламент_П 6.1.6
        a: Согласно пункту 6.1.6 [регламента брокерского обслуживания|http://zaoik.finam.ru/broker/regulations] брокер может отказать в исполнении поручения на вывод денежных средств через систему СБП.
            Для подачи таких выводов действует ряд ограничений:
            ✅ услуга доступна только для граждан РФ
            ✅ данная услуга недоступна для счетов ИИС
            ✅ сумма поручения в пределах от 1000 до 1000000 ₽
            ✅ сумма поручения не может превышать 80% от активов, с учётом удерживаемого НДФЛ
            ✅ не более одного вывода через СБП в день
            ✅ в банке-получателе должна быть подключена услуга СБП
    
    state: Документы_Общие_Регламент_П 14.4
        a: Выплата процентов на остаток по договорам ИИС, заключенным до 08.07.2021, производится согласно п. 14.4 [регламента брокерского обслуживания|http://zaoik.finam.ru/broker/regulations]
            ✅ Начисление процентов осуществляется за каждый календарный день на сумму свободного остатка средств в рублях РФ по ставке (%, годовых), равной ½ (одна вторая) ключевой ставки Банка России
            ✅ Выплата производится ежемесячно не позднее 3 (трех) первых рабочих дней месяца, следующего за расчетным

    state: Документы_Общие_Регламент_П 4.4
        a: На основании п.4.4 [регламента брокерского обслуживания|http://zaoik.finam.ru/broker/regulations] брокер вправе отказать в принятии заявлений о присоединении/о выборе условий обслуживания и не заключить договор присоединения как по причине не предоставления/не соответствия представленных документов требованиям брокера, не выполнения потенциальным клиентом каких-либо действий, так и по своему усмотрению без объяснения причин.

    state: Документы_Общие_Открытие счета || sessionResultColor = "#B65A1E"
        a: Документы, подтверждающие открытие брокерского счета, находятся в [личном кабинете|https://edox.finam.ru/catalog/documents.aspx]
            Основными документами являются: 
            — «Заявление о выборе условий обслуживания»
            — «Уведомление о заключении договора присоединения» 
            — «Заявление о присоединении к регламенту» 
            — «Уведомление для ИФНС» (документ находится в «Журнале уведомлений» и не является обязательным)

    state: Документы_Общие_Реквизиты
        a: Реквизиты для ввода денежных средств и ценных бумаг доступны в [личном кабинете|https://lk.finam.ru] в разделе «Детали» по счету.

    state: Документы_Общие_Соглашение Казахстан
        a: «Дополнительное соглашение об ограничении суммы инвестирования» для банков Казахстана можно получить в личном кабинете в разделе «Отчетность» → «Основные документы» либо по [ссылке|https://edox.finam.ru/catalog/documents.aspx]
            ❗ Если вы не нашли данный документ, обратитесь к менеджеру поддержки
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Документы_Общие_Согл для торговли иностранными активами || sessionResultColor = "#CD4C2B"
        a: ✅ «Согласие на торговые операции с заблокированными иностранными ценными бумагами» для торговли на внебирже доступно по [ссылке|https://edox.finam.ru/ForeignSecurities/BlockedSecuritiesConsent]
            ✅ Чтобы подписать соглашение с рисками хранения валюты, обратитесь к менеджеру поддержки
        buttons:
            "Торговля заблокированными ЦБ" -> /Как закрыть позиции_Продажа БлокЦБ
            "Перевод на оператора" -> /Перевод на оператора

    state: Документы_Налоговые
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Справка 2-НДФЛ" -> /Документы_Налоговые_2-НДФЛ
            "Справка об убытках" -> /Документы_Налоговые_Справка об убытках
            "Справка 1042S" -> /Документы_Налоговые_Справка 1042S
            "Форма W-8BEN" -> /Форма W8BEN
            "Справка для госслужащего" -> /Документы_Налоговые_Справка госслужащего
            "Документы для отчетности по дивидендам" -> /Выплата дохода_Ещё_Документы для отчетности
            "Назад" -> /Документы и справки

    state: Документы_Налоговые_2-НДФЛ
        a: 1. Справка 2-НДФЛ — официальный документ по форме ИФНС РФ о рассчитанных доходах и удержанных с них налогах за отчетный период (календарный год).
            ✅ Заказ справки 2-НДФЛ доступен в личном кабинете [в разделе «Налоги и справки»|https://lk.finam.ru/reports/tax]
            ✅ Электронный формат справки будет доступен в личном кабинете в течение 3-х рабочих дней.
            Изготовление справки на бумажном носителе в течение одной рабочей недели.
            2. Содержание справки:
            ✅ Доход — общая стоимость сделок продажи за отчетный период.
            ✅ Вычет — общая стоимость сделок покупки за отчетный период, а также комиссии, соответствующие коду дохода.
            ✅ Налогооблагаемая база — итоговая прибыль, рассчитывается как разница дохода и вычета. Если в данной графе указан «0», за текущий отчетный период отсутствуют доходы и необходимо проверить справку об убытках.
    
    state: Документы_Налоговые_Справка об убытках
        a: Справка об убытках — официальный документ по форме ИФНС РФ о рассчитанных убытках за отчетный период (календарный год), доступна для формирования за последние 10 лет (за каждый убыточный период заказывается отдельная справка).
            ✅ Заказ справки об убытках доступен в личном кабинете [в разделе «Налоги и справки»|https://lk.finam.ru/reports/tax]
            ✅ Электронный формат справки будет доступен в личном кабинете в течение 3-х рабочих дней.
            Изготовление справки на бумажном носителе в течение одной рабочей недели.

    state: Документы_Налоговые_Справка 1042S || sessionResultColor = "#CD4C2B"
        a: Справку формы 1042s формируют НРД и СПБ-биржа и направляют брокеру. Готовые формы 1042S загружаются автоматически в личном кабинете [в разделе «Налоги и справки»|https://lk.finam.ru/reports/tax]
            ✅ Справка выдается только за календарный год.
            ✅ Изготовление справки на бумажном носителе занимает до 3-х рабочих дней.
            ❗ По выплатам, которые будут производиться после 01.01.2024, формы 1042-s предоставляться не будут. Для налоговой отчетности за 2024-й и последующие годы подойдут Уведомление о перечислении дохода и брокерский отчет.            
            ✅ В справке представлены сводные данные без разбивки по эмитентам.
            ✅  Во втором пункте указана сумма причитающегося инвестору дохода. Необходимо ожидать перечисление всех валютных доходов за прошлый год от СПБ-биржи.
            ✅ Сумма дохода и сумма налога в справке округлены до целого числа по правилам математического округления.
            ❗ Доходы в валюте от СПБ-биржи поступают частично, распределение поступивших сумм происходит пропорционально количеству ценных бумаг на всех владельцев, остальная часть выплаты не поступает из-за блокировки цепочки с Euroclear/Clearstream. Информации о сроках доплаты/снятия ограничений пока не поступало.
            Подробнее на [сайте СПБ Биржи|https://spbbank.ru/ru/depobsl/Soobshcheniia_Depozitariia]

    state: Документы_Налоговые_Справка госслужащего || sessionResultColor = "#CD4C2B"
        a: Справку для госслужащего (по форме 5798-У) вы можете запросить в личном кабинете [в разделе «Налоги и справки»|https://lk.finam.ru/reports/tax]
            Изготовление справки занимает до пяти рабочих дней.
            Содержание справки:
            ✅ В первом разделе указаны сведения по банковским счетам, соответственно при получении данной справки от АО «Финам» раздел не заполняется.
            Для получения сведений об остатках средств на брокерских счетах можно получить [справку по счету|https://lk.finam.ru/reports/tax]
            ✅ Во втором разделе указана информация о поставленных ценных бумагах, а также сведения о доходах (налоги, дивиденды, купоны).
            ❗ ПФИ (фьючерсы, опционы) не являются ценными бумагами.
            ✅ В третьем разделе указана информация об иных доходах (проценты на остаток, доходы от продажи валюты, доходы по драгоценным металлам и прочее).
            ✅ В четвертом разделе указана информация о займах, сделках РЕПО и иных обязательствах клиента и брокера перед клиентом, если они превышали сумму 500000 ₽.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Документы_Депозитарные
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Справка об активах" -> /Документы_Депозитарные_Справка об активах
            "Выписки из депозитария" -> /Документы_Депозитарные_Выписки из депозитария
            "Назад" -> /Документы и справки

    state: Документы_Депозитарные_Справка об активах
        a: Справку об активах за закрытую торговую сессию можно запросить в личном кабинете [в разделе «Налоги и справки»|https://lk.finam.ru/reports/tax]
            Изготовление справки занимает до двух рабочих дней.
            
    state: Документы_Депозитарные_Выписки из депозитария
        a: Заказать депозитарные документы вы можете в личном кабинете [в разделе «Налоги и справки»|https://lk.finam.ru/reports/tax]
            Заказ документов является платным: 
            ✅ 200 ₽ — «Выписка со счёта ДЕПО» и «Выписка об операциях по счету ДЕПО».
            Изготовление справки в течение 3-х рабочих дней.
            ✅ 500 ₽ — «Выписки из НРД».
            Изготовление справки в течение месяца.

    state: Справка по счету || sessionResultColor = "#15952F"
        intent!: /018 Справка по счету
        if: technicalBreak()
            a: ✅ Баланс счета не отображается ночью в будние дни, во время технических перерывов, связанных с обслуживанием серверов торговых систем:: 
                — QUIK: с 3:00 до 6:40 МСК, 
                — TRANSAQ и FinamTrade: с 5:00 до 6:40 МСК. 
                В выходные дни дополнительные технические работы могут проводится в дневное время, так как торги не проводятся.
                ✅ В выходные и праздничные дни торги не проводятся, либо осуществляются в ограниченном формате.
                ✅ В рамках учебных счетов в неторговый период выставление всех типов заявок недоступно. Сервера учебных счетов начинают работать с 10:00 по МСК. В выходные и праздничные дни торги не проводятся.
        
        a: ❗ После перевода СПБ Биржей бумаг на неторговый раздел, бумаги исключены из торговых лимитов биржи, и не отображаются в терминале, но их наличие отражено во вкладке «Портфель» в [личном кабинете|https://lk.finam.ru/].
            ❗ Заблокированные бумаги, которые учитывались в СПБ Банке на субсчетах с разделами типа BN, были переведены 15.02.2024 на новый счет депо типа Y в СПБ Банке, данная операция отображается в [«Истории»|https://lk.finam.ru/] по счету как «Перевод ценных бумаг». Нажмите кнопку ниже, чтобы узнать подробнее.
        a: ✅ Детальное описание операций и сделок, цены покупок и продаж, начисления и списания, корпоративные действия с бумагами в портфеле и другие движения по счету можно изучить в [Справке по счету|https://lk.finam.ru/reports/tax]
            ❗ Максимальный интервал единоразового формирования отчета составляет 92 дня. Если нужна информация за год, нужно загрузить 4 отчета
            ✅ Также в личном кабинете автоматически выгружаются [брокерские отчеты|https://lk.finam.ru/reports/brokerage] на подписание
            ✅ Историю операций в онлайн режиме можно посмотреть в личном кабинете во вкладке [«История»|https://lk.finam.ru/history], а также в [FinamTrade|https://trading.finam.ru/] (Android, IOS и Web) 
            ✅ Детальнее по справке:
        buttons:
            "Торговый оборот" -> /Справка_Торговый оборот
            "Начисление пени" -> /Справка_Начисление пени
            "Сделки СВОП" -> /Валютный рынок_Комиссия СВОП
            "Сделки РЕПО" -> /Справка_Сделки РЕПО
            "Займ ЦБ" -> /Займ ЦБ
            "Другие документы" -> /Документы и справки
            "Счет депо типа «Y»" -> /Депозитарное поручение_Счеn депо типа Y

    state: Справка_Торговый оборот
        a: ✅ Оборот можно изучить в справке по счету в разделе «Виды движений денежных средств». Загрузить справку за закрытый торговый период можно в личном кабинете: https://lk.finam.ru/reports/tax
            ✅ Торговый оборот за последние 4 завершенных квартала с целью получения статуса квалифицированного инвестора вы можете уточнить в личном кабинете по ссылке: https://lk.finam.ru/user/invest-status 
            ❗ Согласно требованиям ЦБ валютные операции не учитываются при подсчете оборота для присвоения статуса.
    
    state: Справка_Начисление пени || sessionResultColor = "#CD4C2B"
        a: Клиент уплачивает Брокеру за каждый день займа пеню на сумму неисполненных обязательств.
            ✅ Для того чтобы пеня не списывалась — нужно иметь свободные денежные средства на удержание сопутствующих торговле комиссий.
            ✅ Пеня — это не штраф, это «процент за использование заемных средств». Иначе говоря, пеня — это комиссия за займ денежных средств, которая образовалась в результате списаний по тарифам, а не открытия позиции. 
            ✅ В базу немаржинальной задолженности попадают все списания в минус (на практике, — это начисленные комиссии за сделки и переносы займа). С этой базы берется пеня по единой ставке (ставка соответствует комиссии за заем по тарифу).

    state: Справка_Сделки РЕПО || sessionResultColor = "#CD4C2B"
        a: Сделки РЕПО - являются сделками переноса ваших необеспеченных позиций. В отчете отображаются две сделки:
            ✅ сделка предоставления займа (продажа, либо покупка ценных бумаг),
            ✅ сделка возврата займа (сделка обратного откупа, либо продажи).
             С помощью данных сделок вы получаете возможность взять в займ ценные бумаги у брокера, либо денежные средства под покупку ценных бумаг. Сделки РЕПО проводятся брокером автоматически и фактически в них заложена комиссия по тарифу за займ денежных средств и ценных бумаг. 
            Важно понимать, с помощью сделок РЕПО брокер не берет ваши ценные бумаги в займ.

    state: Форма W8BEN || sessionResultColor = "#CD4C2B"
        intent!: /019 Форма W8BEN
        a: Подписать форму W-8BEN можно в личном кабинете [в разделе «Налоги и справки»|https://lk.finam.ru/reports/tax].
            Сформировать документ, распечатать, подписать и прикрепить заявление необходимо в течение одного дня.
            ✅ Результат рассмотрения отобразится в течение 30 календарных дней в этом же разделе личного кабинета. Для корректного отображения результата выберите верный временной интервал.

    state: Личный кабинет || sessionResultColor = "#418614"
        intent!: /020 Личный кабинет
        a: Личный кабинет доступен по ссылке: https://lk.finam.ru/
            Старая версия: https://edox.finam.ru/
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Восстановить доступ от Личного кабинета" -> /Личный кабинет_Восстановить доступ
            "Торговый код по счету" -> /Личный кабинет_Торговый код
            "Удаление аккаунта/Отключение рассылок" -> /Личный кабинет_Удаление аккаунта
            "Ещё" -> /Личный кабинет_Ещё

    state: Личный кабинет_Восстановить доступ
        a: ✅ По умолчанию логином от личного кабинета является номер телефона в международном формате (например: начиная с «7…» - Россия, «375…» - Беларусь, «997…» - Казахстан)
            ✅ Пароль вы задавали самостоятельно
            ✅ Для восстановления доступа к личному кабинету:
            1. перейдите по ссылке → https://lk.finam.ru/
            2. нажмите на кнопку «Забыли логин или пароль?»
            3. введите ФИО, паспортные данные и подтвердите восстановление
            4. На вашу электронную почту придет письмо с логином и ссылкой на создание нового пароля

    state: Личный кабинет_Торговый код
        a: Торговый код присваивается к каждому брокерскому счету. Узнать свой торговый код можно в личном кабинете: https://lk.finam.ru/details
            Код понадобится вам при обращении в техническую поддержку, а также при выставлении голосовых заявок. Не сообщайте его третьим лицам.
        buttons:
            "Кодовое слово" -> /Личный кабинет_Торговый код_Кодовое слово
    
    state: Личный кабинет_Торговый код_Кодовое слово || sessionResultColor = "#CD4C2B"
        a: Кодовое слово не является обязательным, но служит дополнительной защитой ваших данных при выставлении торговой заявки через отдел голосового трейдинга.
            ✅ После создания кодового слова, при звонке в отдел голосового трейдинга нужно назвать торговый код и кодовое слово.
            ✅ Создать, изменить или удалить кодовое слово можно в личном кабинете в разделе «Учетные данные» по ссылке: https://lk.finam.ru/user/credentials

    state: Личный кабинет_Удаление аккаунта
        a: ✅ Чтобы отключиться от рассылок компании на вашу электронную почту, нужно обратиться к менеджеру «Финам» и сообщить:
            1. адрес вашей электронной почты,
            2. название нежелательной рассылки.
            ✅ Чтобы заблокировать доступ в личный кабинет и удалить аккаунт, нужно:
            1. вывести все активы с брокерских счетов,
            2. расторгнуть все действующие договоры,
            3. обратиться к менеджеру «Финам».
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Личный кабинет_Ещё
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Отключение двухфакторной авторизации" -> /Личный кабинет_Ещё_Отключение двухфакторной авторизации
            "Открытие счетов" -> /Открытие_счета
            "Закрытие счетов" -> /Закрытие_счета
            "Сообщить о проблеме" -> /Личный кабинет_Ещё_Сообщить о проблеме
            "Назад" -> /Личный кабинет

    state: Личный кабинет_Ещё_Отключение двухфакторной авторизации
        a: Запрос ввода кода из СМС при входе в личный кабинет отключить нельзя.

    state: Личный кабинет_Ещё_Сообщить о проблеме
        a: Чтобы сообщить о проблеме в личном кабинете нажмите кнопку «Перевод на оператора», либо напишите «оператор». Просьба детально описать ситуацию, а также предоставить скриншот с воспроизведенной ошибкой для быстрого решения проблемы.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Замена персональных данных || sessionResultColor = "#418614"
        intent!: /021 Замена персональных данных
        a: ✅ Подать поручение на смену паспортных данных, номера телефона, электронной почты можно в личном кабинете: https://edox.finam.ru/Client/EditInfo
            ❗ Для замены паспортных данных вложите копии полных страниц документа, подтверждающих смену данных. Копии должны хорошо читаться, не иметь бликов, посторонних надписей или рисунков.

    state: Авторизация || sessionResultColor = "#418614"
        intent!: /022 Авторизация
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Личный кабинет" -> /Личный кабинет_Восстановить доступ
            "FinamTrade" -> /ИТС_FinamTrade_Авторизация
            "TRANSAQ/TRANSAQ Connector" -> /ИТС_TRANSAQ_Авторизация
            "QUIK" -> /ИТС_QUIK_Авторизация
            "MetaTrader 5" -> /ИТС_MetaTrader 5_Авторизация
            "Сайт Comon.ru" -> /Comon_Авторизация
            "Сайт Finam.ru" -> /Авторизация_Сайт Finam.ru
    
    state: Авторизация_Сайт Finam.ru
        a: После регистрации на сайте Finam.ru вам придет письмо со сгенерированными никнеймом и паролем. В качестве логина для авторизации на сайте можно использовать:
            — никнейм
            — номер телефона в международном формате (например: начиная с «7…» - Россия, «375…» - Беларусь, «997…» - Казахстан)
            — электронную почту
            Также вы можете использоваться для авторизации данные от личного кабинета брокера. На странице авторизации необходимо выбрать «ЛК Финам». 
            Восстановить пароль можно по номеру телефона, либо по электронной почте на странице авторизации.
    
    state: Подпись || sessionResultColor = "#418614"
        intent!: /023 Подпись
        a: Для подписания документа зайдите:
            ✅ в личный кабинет: https://lk.finam.ru/ в раздел «Документы» либо по ссылке https://lk.finam.ru/reports/documents
            или
            ✅ в личный кабинет (старый вид):
            https://edox.finam.ru/ раздел «Отчетность» → «История операций» либо по ссылке https://edox.finam.ru/Journals/UnionDocumentJournal 
            ❗ Чтобы подписать «Согласие на торговые операции с иностранными бумагами с местом хранения недруж. инфраструктура» после 01.04.2023, перейдите по ссылке https://edox.finam.ru/ForeignSecurities/UnfriendlyDepoConsent 
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Электронная подпись" -> /Подпись_Электронная
            "SMS-подпись" -> /Подпись_SMS
            "Не приходит SMS/письмо" -> /Ошибки СМС_Почта
            "Ошибки при подписании" -> /Подпись_Ошибки
   
    state: Подпись_Электронная
        a: Перед созданием ключа ЭП перейдите в личном кабинете https://edox.finam.ru/ в раздел «Помощь» → «Инструкции, шаблоны, ПО» или по ссылке https://edox.finam.ru/global/software.aspx , скачайте и установите «Плагин для генерации электронной подписи», он доступен на устройствах с системой Windows, необходимо использовать браузер Google Chrome.
            Далее следуйте по пунктам:
            1. В личном кабинете, перейти в раздел «Сервис» → «Электронная подпись» → «Создание ключей» или перейти по ссылке https://edox.finam.ru/Sign/CreateCertificate → нажать «Создать сертификат».
            2. В открывшемся диалоговом окне выбрать пустую папку для хранения нового ключа и нажать «Старт». Шевелите мышкой до заполнения индикатора.
            3. После успешного создания ключа «Активировать» новый сертификат ключа ЭП, а затем его «Сохранить». Плагин должен быть включен в настройках, три точки справа вверху «Дополнительные инструменты» - «Расширения».
            Расширение плагина для работы с электронной подписью через браузер Google Chrome: https://chrome.google.com/webstore/detail/signal-com-signature-plug/ceifjolbdjihdddppedlcocpgckafcak
    
    state: Подпись_SMS
        a: Чтобы подписывать электронные документы в личном кабинете, подключите SMS-подпись по ссылке: https://edox.finam.ru/info/ApplicationSms/ApplicationSmsOn.aspx
            Также в данном меню можно выбрать активный номер телефона для получения SMS (если Вы добавили несколько номеров).

    state: Подпись_Ошибки
        a: Если вам не удается подписать документ или не удалось найти необходимый, пожалуйста, обратитесь к менеджеру.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Ошибки СМС_Почта
        intent!: /024 Ошибки смс_почта
        a: Рекомендации при возникновении проблем с доставкой
            ✅  СМС-сообщений:
            1. проверить свой аппарат на наличие сбоев, перезагрузить аппарат;
            2. очистить память аппарата от устаревших сообщений;
            3. проверить черные списки и спам-фильтры аппарата;
            4. проверить услугу черный список у оператора;
            5. удалить стороннее ПО для работы с sms, установленное на аппарате;
            6. в случае Multi-SIM аппарата убедиться в том, что активна данная sim-карта;
            7. при необходимости провести обновление системного ПО, или сброс настроек аппарата к заводским,
            8. протестировать прием данных сообщений, переставив sim-карту в другой, заведомо исправный аппарат.
            ✅ Сообщений на электронную почту:
            1. проверить папку «спам»;
            2. убедиться, что в личном кабинете указан предпочтительный адрес электронной почты в разделе «информация о клиенте» по ссылке https://edox.finam.ru/client/info 
            3. изменить еmail можно в разделе «изменение анкетных данных» по ссылке https://edox.finam.ru/Client/EditInfo 
            ❗ Если после выполнения рекомендаций проблема сохраняется, просьба обратиться к менеджеру.

    state: Банк || sessionResultColor = "#15952F"
        intent!: /025 Банк
        
        # script:
        #     if ( typeof $parseTree._bank_redirect != "undefined" ){
        #         $session.bank_redirect = $parseTree._bank_redirect;
        #         $reactions.transition("/Банк_" + $session.bank_redirect.name);
        #     }
            
        a: В АО «Банк ФИНАМ» вас ждут низкие тарифы на все финансовые операции и квалифицированный сервис.
                ✅ Информация по тарифам и услугам банка на сайте: https://www.finambank.ru/person/rates/
                ✅ Для консультации вы можете обратиться к сотрудникам Банка:
                — по телефонам +7(495) 796-90-23 и +7 (800) 200-44-00 (бесплатно по России)
                — по электронной почте support@finambank.ru
                — посетив офис компании «Финам», адреса и контактная информация по ссылке: https://www.finambank.ru/about/offices 
                ✅ Режим работы Банка:
                понедельник–пятница – с 09:00 до 21:00,
                суббота, воскресенье – выходной день.
                Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Банковский счет" -> /Банк_Банковский счет
            "Банковская карта" -> /Банк_Банковская карта
            "Конвертация валюты" -> /Банк_Конвертация валюты
            "Переводы за границу/SWIFT/МИР" -> /Банк_Переводы загран
            "Платежи по СБП" -> /Банк_Платежи по СБП

    state: Банк_Банковский счет
        a: ✅ Счета в Банке «Финам» открываются в рублях РФ, долларах США, евро, китайских юанях, казахстанских тенге, армянских драмах.
                Открытие бесплатное, ведение счета по условиям тарифов.
                ✅ Актуальные тарифы представлены на сайте Банка, в разделе «Рассчетно-кассовое обслуживание» или по ссылке https://www.finambank.ru/person/rates
                Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Как открыть банковский счет" -> /Банк_Банковский счет_Как открыть
            "Получение наличных в кассе" -> /Банк_Банковский счет_Получение наличных
            "Зачисление/хранение валюты" -> /Банк_Банковский счет_ЗачислениеХранение Валюты
            "Вклады" -> /Банк_Банковский счет_Вклады
            "Назад" -> /Банк

    state: Банк_Банковская карта
        a: «Банк Финам» выпускает банковские карты платежной системы МИР по пакетам услуг «Комфорт», «Премиум», «Корпоративный».
            ✅ Данные пакеты услуг принимают участие в Программе лояльности «CASHBACK», действующей в Банке.
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Как открыть банковскую карту" -> /Банк_Банковская карта_Как открыть
            "Тарифы на обслуживание карт" -> /Банк_Банковская карта_Тарифы
            "Банкоматы/Снятие наличных" -> /Банк_Банковская карта_Банкоматы
            "Назад" -> /Банк

    state: Банк_Конвертация валюты
        a: ✅ На данный момент конвертация валюты в рамках банковского обслуживания возможна только в личном кабинете «Банка Финам» по ссылке https://ibank.finam.ru
            ✅ Если у вас есть брокерский счет, вы можете приобретать необходимую валюту на Московской бирже. 
            ❗ При планировании операций в иностранной валюте, просим учесть, что на сегодняшний день в кредитных организациях РФ действуют ограничения по выдаче наличной иностранной валюты со счетов физических лиц, открытых в иностранной валюте. Выплаты осуществляются в рублях в наличной форме без ограничений по курсу, определяемому АО «Банк Финам».
            *Банки могут продавать гражданам доллары США и евро, поступившие в их кассы с 9 апреля 2022 года.
            Предварительно необходимо уточнять наличие средств в кассе Банка.

    state: Банк_Переводы загран
        a: 1. Через «Банк Финам» доступны переводы по реквизитам заграницу следующих валют:
            ✅ рубль РФ
            ✅ армянский драм 
            ✅ китайский юань (кроме банков Еврозоны, банков Швейцарии, банков Великобритании, банков США, как банков посредников, так и банков получателей)
            ✅ казахстанский тенге
            Ознакомиться с тарифами можно по ссылке: https://www.finambank.ru/person/rates в разделе «Рассчетно-кассовое обслуживание»
            2. В интернет-банке «Финам» реализована возможность перевода с карты платежной системы МИР на карты следующих стран: Таджикистан (ПС Корти Милли), Киргизия (ПС Элкарт), Абхазия (ПС Апра), Армения (ПС АрКа, ПС Мир), Беларусь (ПС Мир, ПС Белкарт), Казахстан (ПС Мир), Осетия (ПС Мир).*
            * ПС – платёжная система.
            ✅ Чтобы осуществить перевод нужно:
            1. перейти по ссылке: https://ibank.finam.ru/Operations/Card2Card/Init и авторизоваться в личном кабинете интернет-банка
            2. в открывшейся форме переводов «С карты на карту» выбрать карту МИР, с которой будет произведено списание
            3. указать карту получателя перевода, на которую будет проведено зачисление
            4. указать фамилию и имя получателя перевода на латинице
            5. подтвердить операцию кодом из СМС
            ✅ Комиссия за перевод взимается в соответствии с действующими тарифами банка «Финам»:
            1. Тариф «Корпоративный»
            До 30000 ₽ в месяц – без комиссии. Свыше 30000 ₽ в месяц – 1,5% от суммы перевода, но не менее 50 ₽
            2. Тариф «Комфорт»:
            До 20000 ₽ в месяц – без комиссии. Свыше 20000 ₽ в месяц – 1,5% от суммы перевода, но не менее 50 ₽
            3. Тариф «Премиум»:
            До 100000 ₽ в месяц – без комиссии. Свыше 100000 ₽ в месяц – 1,5% от суммы перевода, но не менее 50 ₽
            ❗ Если карта открыта в местной валюте, курс пересчета осуществляется по курсу банка эмитента.

    state: Банк_Платежи по СБП
        a: ✅ Максимальная сумма одного перевода/платежа с использованием Системы быстрых платежей (СБП) составляет 1000000 ₽
                ✅ Максимальная сумма переводов в месяц - 5000000 ₽
                ✅ Максимальная сумма переводов в месяц на свой банковский счет (вклад), открытый в другой кредитной организации - 30000000 ₽
                ✅ Лимиты и комиссии на исходящие переводы через Систему быстрых платежей (СБП) по тарифам:
                1. «Комфорт» — до 200000 ₽ в месяц — без комиссии
                свыше 200000 ₽ в месяц — 0,5% от суммы перевода, не более 1500 ₽
                2. «Премиум» — до 5000000 ₽, без комиссий
                3. «Корпоративный» — до 300000 ₽ в месяц - без комиссии, свыше 300000 ₽ в месяц - 0,5% от суммы перевода, не более 1500 ₽
                ✅ При расчете максимальной суммы переводов, совершенных в течение календарного месяца, а также при расчете комиссии за исходящий перевод, учитывается совокупный объем денежных средств по исходящим переводам с использованием СБП за текущий календарный месяц по всем счетам клиента, открытым в Банке.
                ✅ Актуальные тарифы на сайте Банка: https://www.finambank.ru/person/rates в разделах «Пакет услуг Корпоративный» и «Пакеты услуг Комфорт и Премиум»
    
    state: Банк_Банковский счет_Как открыть
        a: Если вы ранее открывали брокерский счет в «Финам» при личном визите в офисе компании, то для вас доступно открытие банковского счета дистанционно в личном кабинете по ссылке https://lk.finam.ru/open/bank/savings
            ❗ Если вы не открывали ранее брокерский счет или открывали его дистанционно, то открыть банковский счет можно только при личном посещении офиса.

    state: Банк_Банковский счет_Получение наличных
        a: ✅ Получение наличных рублей со счетов/вкладов, открытых в рублях на сумму более 100000 ₽ осуществляется Банком при условии их предварительного заказа клиентом до 12:30 рабочего дня, предшествующего дню получения.
            ✅ Получение наличных рублей со счетов/вкладов, открытых в иностранной валюте на сумму более 100000 ₽ осуществляется Банком при условии их предварительного заказа клиентом не менее, чем за 5 рабочих дней до даты их получения (день приема заявки не учитывается).﻿
            ✅ Получение наличной иностранной валюты (доллары США, евро) возможно только в объеме, находящемся на счетах клиента в Банке (брокерские счета сюда не входят!) до 09.03.2022 (00:00), но не более 10000 $.
            ✅ Если валюта находится на брокерском счете (в АО или в Банке), при переводе на счета в Банке (после: 09.09.2022) получение валюты наличными доступно в рублях по курсу Банка. 
            ✅ Средства, размещенные на банковских валютных счетах (в евро и долларах США) до: 09.09.2022 включительно, можно получить в рублях (ограничений нет) по курсу ЦБ на дату выплаты.
            ✅ Получение наличной иностранной валюты в китайских юанях возможно в отделениях банка «Финам» в г. Москва на Настасьинском пер. дом 7, стр.2, в г. Благовещенск и г. Владивосток.
            ❗ Предварительно необходимо уточнять наличие средств в кассе Банка: https://www.finambank.ru/about/offices

    state: Банк_Банковский счет_ЗачислениеХранение Валюты
        a: 1. Комиссия за зачисление долларов США и евро на банковские счета Банка «Финам»:
            ✅ По счетам в USD/EUR – 3% от суммы операции, но не менее 300 USD/EUR и не более суммы операции.
            ✅ По счетам в иных валютах – не взимается.
            2. Комиссия за обслуживание банковских счетов в долларах США и евро:
            ✅ Если совокупный остаток не превышает 3000 единиц валюты – комиссия не списывается. 
            ✅ Если совокупный остаток равен либо превышает 3000 единиц валюты – 0,013 % в день от остатка.
            ❗ Комиссия удерживается ежедневно на сумму остатка на начало дня: учитывается совокупный (суммарный) остаток денежных средств по всем валютным текущим счетам/карточным счетам, открытым после 15.08.2022  включительно в долларах США/евро.
            ❗ Комиссия взимается в валюте Счета отдельно с каждого счета (счетов) в долларах США/евро.

    state: Банк_Банковский счет_Вклады
        a: С информацией о вкладах и накопительных счетах можно ознакомиться по ссылке: https://www.finambank.ru/person/deposits/

    state: Банк_Банковская карта_Как открыть
        a: ✅ Если вы ранее дистанционно открывали брокерский счет в «Финам», то для вас доступно открытие банковской карты по тарифу «Комфорт» дистанционно в личном кабинете по ссылке https://lk.finam.ru/open/bank/card  
            ❗ Виртуальная карта и личный кабинет интернет-банка https://ibank.finam.ru/ активируются в момент выпуска карты.
            ❗ Пластиковая карта доступна к получению в офисе компании, изготовление до 5 рабочих дней.
            ✅ Если ранее вы не открывали брокерский счет в компании «Финам», или желаете оформить банковскую карту по тарифам «Премиум» или «Корпоративный», то оформить банковскую карту можно только при личном посещении офиса компании.

    state: Банк_Банковская карта_Тарифы
        a: ✅ Актуальные тарифы «Корпоративный», «Комфорт» и «Премиум» представлены по ссылке: https://www.finambank.ru/person/cards/
            ❗ Информация по обслуживанию архивных пакетов услуг по банковским картам доступна на сайте Банка по ссылке https://www.finambank.ru/person/rates - в разделе «Архив тарифов».

    state: Банк_Банковская карта_Банкоматы
        a: ✅ Снять денежные средства можно в кассе офиса «Финам»: https://www.finambank.ru/about/offices
            ✅ А также в пунктах выдачи наличных/банкоматах сторонних банков.
            ✅ Актуальные тарифы на снятие наличных представлены на сайте Банка https://www.finambank.ru/person/rates в разделах «Пакет услуг Корпоративный» и «Пакеты услуг Комфорт и Премиум».
            ❗ Информация по обслуживанию архивных пакетов услуг по банковским картам - в разделе «Архив тарифов».

    state: Финам Форекс || sessionResultColor = "#15952F"
        intent!: /026 Финам Форекс
        a: ✅ По счетам «Финам Форекс» предоставляется отдельный личный кабинет. Авторизация доступна по ссылке: https://forexcabinet.finam.ru/
            ✅ Чтобы активировать счет, необходимо пополнить его на сумму от 15000 ₽ и пройти тестирование для допуска к торгам
            ✅ Размер стандартного плеча по счетам «Финам Форекс» составляет максимум 1:35
            ✅ Если у вас есть статус квалифицированного инвестора, плечо можно увеличить до 1:40
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Как открыть счет в Финам Форекс" -> /Финам Форекс_Как открыть
            "Условия обслуживания" -> /Финам Форекс_Условия обслуживания
            "Ввод/вывод средств" -> /Финам Форекс_Ввод_вывод средств
            "Программы для торговли" -> /ИТС_Другие_MetaTrader 4
            "Налог на доходы" -> /Финам Форекс_Налог на доходы
            "Учебный счет" -> /Финам Форекс_Учебный счет

    state: Финам Форекс_Как открыть
        a: ✅ Если вы впервые открываете счет в «Финам», то для этого достаточно подать заявку на сайте: https://forex.finam.ru/
            ✅ Дополнительные счета «Финам Форекс» можно открыть в личном кабинете: https://forexcabinet.finam.ru/
            ✅ Также вы можете открыть счет в офисе «Финам». Не забудьте взять с собой паспорт
            Адреса и контакты офисов доступны по ссылке: https://finamfx.ru/about/contacts
            ❗ На текущий момент дистанционный договор «Финам Форекс» заключается только с гражданами РФ. Код номера телефона автоматически проставлен на «(+7)» и не может быть изменен.

    state: Финам Форекс_Условия обслуживания
        a: ✅ По счетам «Финам Форекс» доступно 26 валютных пар: https://finamfx.ru/Solutions/TradingConditions/
            ✅ Минимальный объем торговли — 0,01 лота. 
            ✅ Счета номинированы в рублях. 
            ✅ Чтобы активировать счет, необходимо пополнить его на сумму от 15000 ₽ и пройти тестирование для допуска к торгам. 
            ✅ Конвертация валюты происходит по текущему курсу на рынке Forex.

    state: Финам Форекс_Ввод_вывод средств
        a: Пополнять и выводить денежные средства по счетам «Финам Форекс» можно только в виде банковских переводов по реквизитам.
            Срок зачисления — до 2 рабочих дней.

    state: Финам Форекс_Налог на доходы
        a: Компания «Финам Форекс» является налоговым агентом и самостоятельно отчитывается о доходах по счетам своих клиентов.

    state: Финам Форекс_Учебный счет
        a: Открыть учебный счет для торговли на рынке Forex можно по ссылке https://forex.finam.ru/ через кнопку «Демосчет».
            Доступ предоставляется на 14 дней, в рамках демо-счета предоставляется плечо до 1/40.
            Разницы в размерах спредов на демо и реальном счете нет.

    state: О компании || sessionResultColor = "#418614"
        intent!: /027 О компании
        a: «Финам» работает на рынке с 1994 года. Мы успешно сотрудничаем с крупнейшими российскими и зарубежными биржами и предлагаем широкий спектр возможностей для тех, кто действительно хочет зарабатывать на бирже — качественная аналитика, актуальные прогнозы, своевременные инвестидеи, обучение, большой выбор готовых решений, современные торговые системы, для комфортной и быстрой торговли и многое другое. Подробности по ссылке: https://www.finam.ru/landings/reasons/
        buttons:
            "О компании" -> /О компании_О компании
            "Лицензии компании" -> /О компании_Лицензии
            "Отчетность компании" -> /О компании_Отчетность

    state: О компании_О компании
        a: Познакомиться с историей «Финама», а также узнать о наших наградах вы можете по ссылке: https://www.finam.ru/about/history/

    state: О компании_Лицензии
        a: Действующий перечень лицензий компании доступен на сайте: https://www.finam.ru/about/license/

    state: О компании_Отчетность
        a: С отчетностью компании «Финам» можно ознакомиться на сайте: https://www.finam.ru/about/annualreport/

    state: Контакты || sessionResultColor = "#418614"
        intent!: /028 Контакты
        if: holidays()
            a: ✅ 9 мая - нерабочий день для всех офисов компании «Финам»
                ✅ 10 мая в Москве открыты для обслуживания клиентов центральный офис «Финам» в Настасьинском пер. и дополнительный офис на Кутузовском пр., с 10:00 до 19:00. В других городах офисы компании работают в дежурном режиме, перед посещением офиса рекомендуем [связаться|https://www.finam.ru/about/contacts/] с менеджером
                ✅ [График работы «Финам» в майские праздники|https://www.finam.ru/publications/item/grafik-raboty-finama-27-aprelya-12-maya-20240422-1338/]
        a: ❗ Cлужба технической поддержки работает в режиме 24/7.
            Связаться с менеджером «Финам» можно:
            ✅ в чате
            ✅ по телефону по одному из указанных номеров (набрать доб. 2222)
            +7 (495) 796-93-88 
            +7 (495) 1-346-346 
            *1945 (Бесплатно по РФ для МТС, Билайн, МегаФон и Tele2)
            ✅ по электронной почте service@corp.finam.ru
            ✅ звонок с сайта и контакты представительств: https://www.finam.ru/about/contacts 
            ✅ перед визитом в центральный офис «Финам» на Настасьинском пер. дом 7, стр.2 можно заказать парковочное место, обратившись к менеджеру компании.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            
    state: Соцсети || sessionResultColor = "#418614"
        intent!: /029 Соцсети
        a: Будьте в курсе с «Финам» в социальных сетях:
            ✅ Я. Дзен https://dzen.ru/finam.ru?utm_referer=www.finam.ru 
            ✅ VK «Finam: главные новости фондового рынка» https://vk.com/finam_ru 
            Список официальных TG-каналов:
            ✅ @finam_invest – официальный канал для публикации корпоративных новостей и анонсов мероприятий «Финам».
            ✅ @finamalert – канал о рыночных сигналах, аналитике, торговых идеях и прогнозах.
            ✅ @FinamInvestLAB – канал персональных консультантов о торговых идеях.
            ✅ @FinamPrem – канал для VIP-клиентов с премиальными аналитикой и инвестиционными стратегиями.
            ✅ @EducationFinam_bot – обучение трейдингу.
        
    state: Вакансии || sessionResultColor = "#418614"
        intent!: /030 Вакансии
        a: Если вас интересует работа в «Финам», получить подробную информацию вы можете на сайте: https://job.finam.ru/

    state: Отзыв || sessionResultColor = "#15952F"
        intent!: /031 Отзыв
        a: ✅ В FinamTrade реализованы полезные Stories, в которых публикуется важная торговая информация, ссылки на актуальные онлайн-встречи и свежие обучающие курсы, новые услуги, сервисы и инвестиционные идеи.
            ✅ Мы стараемся всесторонне развивать наши продукты и сервисы, прислушиваясь к вашим комментариям и отзывам.
            ✅ Если у вас есть какие-либо пожелания или предложения по работе сервисов «Финам» — вы можете оставить их, перейдя [по ссылке|https://www.finam.ru/landings/finam-invest-feedback/]
            ✅ Также в Лаборатории клиентского опыта «Финам» у клиентов компании есть возможность записаться на участие в интервью, поделиться опытом использования наших продуктов, показать с какими сложностями сталкиваются, получить приятные подарки от «Финам». Подробнее об интервью: https://www.finam.ru/landings/client-experience/ 
            Будем признательны за обратную связь!
            
    state: Оператор
        intent!: /032 Оператор
        script:
            $analytics.setMessageLabel("Клиент запросил оператора TB", "Тех метки");
        go!: /Перевод на оператора

    state: Оператор (сущность)
        q!: * @operator *
        script:
            $analytics.setMessageLabel("Клиент запросил оператора TB", "Тех метки");
        go!: /Перевод на оператора    

    # state: Приветствие || sessionResult = "Orange", sessionResultColor = "#B65A1E"
    #    intent!: /033 Приветствие
    #    a: Я – виртуальный консультант «Финам».
    #        Быстро помогу в любое время. Какой у вас вопрос?
    #  
    state: Благодарность
        intent!: /034 Благодарность
        a: Рады были помочь вам!
        
    state: Прощание || sessionResultColor = "#B65A1E"
        intent!: /035 Прощание
        go!: /Закрытие обращения
        
    state: Оценки || sessionResultColor = "#B65A1E"
        intent!: /036 Оценки
        a: Рады были помочь вам!
        go!: /Закрытие обращения
    
    state: Назад
        intent!: /037 Назад
        go!: /Я-робот_ЖП
    
    state: Уточнение вопроса || sessionResultColor = "#418614"
        intent!: /038 Есть вопрос
        a: Какой у вас вопрос?
        a: Обычно меня спрашивают:
        buttons:
            "Как открыть счет?" -> /Открытие_счета
            "Как пополнить счет?" -> /Движение ДС_Пополнение
            "Как вывести деньги?" -> /Движение ДС_Вывод
            "Как начать инвестировать?" -> /Как начать
            "Заказ справки 2НДФЛ" -> /Документы_Налоговые_2-НДФЛ
            "Выкуп ИЦБ по указу № 844" -> /Ограничение ЦБ_844 

    state: Претензия || sessionResultColor = "#418614"
        intent!: /039 Претензия
        a: Приносим извинения за доставленные неудобства. По данному вопросу вам поможет менеджер. Пожалуйста, ожидайте перевод на оператора.
        go!: /Перевод на оператора
        
    state: Ненормативная лексика
        intent!: /040 Ненормативная лексика
        go!: /Перевод на оператора

    state: Редкие вопросы || sessionResultColor = "#418614"
        intent!: /041 Редкие вопросы
        a: Информацию по данному вопросу можно уточнить у менеджера «Финам».
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            
    state: Доверенности || sessionResultColor = "#418614"
        intent!: /042 Доверенность
        a: Шаблон доверенности для физических лиц доступен в личном кабинете https://edox.finam.ru/ в разделе «Помощь» → «Инструкции, шаблоны, ПО» или по ссылке: https://edox.finam.ru/global/Requisites/Warrant.aspx
            Доверенность должна быть предоставлена в офис компании лично доверенным лицом.
            Если у вас есть вопросы, информацию можно уточнить у менеджера «Финам».
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
            
    state: Наследство || sessionResultColor = "#418614"
        intent!: /043 Наследство
        a: ✅ Информация об остатках на счетах «Финам» предоставляется только в ответ на официальный запрос от нотариуса на розыск имущества. ﻿﻿
            ✅ Официальный адрес компании АО «Инвестиционная компания ФИНАМ»: 127006 г. Москва, пер. Настасьинский, д.7, стр.2. ﻿ 
            ✅ После получения документов, подтверждающих вступление в права наследования, необходимо обратится к менеджеру компании для дальнейшего подписания поручений на перевод активов. 
            ❗ Если зачисление активов планируется на счета сторонних организаций, необходимо предоставить полные реквизиты компании-получателя для формирования поручений.

    state: Доступные биржи || sessionResultColor = "#418614"
        intent!: /044 Доступные биржи
        if: holidays()
            a: ✅ 9 мая - торги и расчёты на Московской бирже не проводятся. На иностранных биржах торги проводятся в стандартном режиме
                ✅ 10 мая торги на биржах проводятся в стандартном режиме. Не проводятся торги и расчёты на валютной секции Московской биржи в режимах TOD и своп (TODTOM)
                ✅ [График работы «Финам» в майские праздники|https://www.finam.ru/publications/item/grafik-raboty-finama-27-aprelya-12-maya-20240422-1338/]

        # script:
        #     if ( typeof $parseTree._dostupnieBirji_redirect != "undefined" ){
        #         $session.dostupnieBirji_redirect = $parseTree._dostupnieBirji_redirect;
        #         $reactions.transition("/Доступные биржи_" + $session.dostupnieBirji_redirect.name);
        #     }
            
        a: Клиентам «Финам» доступны следующие биржи:
            ✅ Московская биржа
            ✅ Биржа СПБ
            ✅ Американский фондовый рынок NYSE/NASDAQ
            ✅ Иностранные опционы США
            ✅ Гонконгская биржа HKEX
            ✅ Внебиржевой рынок
            ✅ Иностранные облигации (Турция, Оман, США, Китай)
            Пожалуйста, выберите интересующую биржу или инструмент:
        buttons:
            "Московская биржа" -> /Доступные биржи_Московская
            "Биржа СПБ" -> /Доступные биржи_СПБ
            "NYSE/NASDAQ" -> /Доступные биржи_NYSE и NASDAQ
            "Иностранные опционы США" -> /Доступные биржи_Опционы США
            "Ещё" -> /Доступные биржи_Ещё

    state: Доступные биржи_Московская
        a: На Московской бирже предоставляется доступ к:
            ✅ Фондовому рынку — российские ценные бумаги (акции, облигации, фонды, еврооблигации, депозитарные расписки) и иностранные.
            ✅ Срочному рынку — фьючерсы и опционы (опционы доступны в рамках договора с отдельными брокерскими счетами, по счетам ЕДП доступна торговля только опционами на акции, только в лонг и через QUIK).
            ✅ Валютному рынку — валютные пары (доллар США, евро,  турецкая лира, китайский юань, гонконгский доллар, белорусский рубль, казахстанский тенге). Все валюты торгуются в паре с российским рублем, за исключением пар евро/доллар США, доллар США/китайский юань.
            В рамках валютного рынка дополнительно доступны контракты на золото и серебро в режиме _TOM (поставка металлов не предоставляется).
            ✅ Внебиржевому рынку  — ценные бумаги (поручение на сделку подается через менеджера «Финам»).

    state: Доступные биржи_СПБ
        a: На СПБ Бирже приостановлены торги иностранными ценными бумагами. Доступно закрытие позиций по бумагам эмитентов РФ. Актуальная информация размещается на [официальном сайте биржи|https://spbexchange.ru/ru/about/news2.aspx].
            ✅ В рамках Указа № 665, депозитарии используют рубли РФ для выплат по заблокированным активам. Выплаты первой очереди, поступившие брокеру от СПБ Биржи, перечислены инвесторам. В настоящий момент информации о предстоящих выплатах от биржи не поступало.
            ❗ После перевода СПБ Биржей бумаг на неторговый раздел, бумаги исключены из торговых лимитов биржи, и не отображаются в терминале, но их наличие отражено во вкладке «Портфель» в [личном кабинете|https://lk.finam.ru/].
            ❗ Брокер «Финам» с 01.01.2024 является налоговым агентом по дивидендам на иностранные ценные бумаги, то есть выплаты будут облагаться налогом.
            ❗ При торговле на иностранных биржах через брокера «Финам» инфраструктура СПБ Биржи не задействована. Вышестоящий брокер-партнёр не раскрывает перед американскими биржами гражданство своих клиентов, поэтому риски в данном направлении минимальны.
            скрывает перед американскими биржами гражданство своих клиентов, поэтому риски в данном направлении минимальны.

    state: Доступные биржи_NYSE и NASDAQ
        a: В рамках биржи NYSE/NASDAQ предоставляется доступ к иностранным акциям и фондам.
            Доступ предоставляется по:
            ✅ «Единым счетам» (кроме счетов, открытых в период с 15.08.2022 по 13.02.2023)
            ✅ по счетам «US Market Options»
            ✅ по счетам «Сегрегированный Global»
            ✅ счетам «Иностранные биржи» (с 1.02.2024 открытие новых счетов недоступно)
            ❗ Для работы с данными инструментами требуется статус квалифицированного инвестора.
            ❗ Все расчеты производятся в долларах США, автоконвертация валюты при покупке не осуществляется.
        buttons:
            "Типы счетов" -> /Типы счетов

    state: Доступные биржи_Опционы США
        a: «Финам» предоставляет доступ к американским опционам на иностранные акции.
            Доступ предоставляется по:
            ✅ «Единым счетам» (для получения доступа обратитесь к менеджеру)
            ✅ по счетам «US Market Options»
            ✅ по счетам «Сегрегированный Global»
            ✅ счетам «Иностранные биржи» (с 1.02.2024 открытие новых счетов недоступно)
            ❗ Для работы с данными инструментами требуется статус квалифицированного инвестора.
            ❗ Все расчеты производятся в долларах США, автоконвертация валюты при покупке не осуществляется.
        buttons:
            "Типы счетов" -> /Типы счетов

    state: Доступные биржи_Ещё
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Гонконгская биржа HKEX" -> /Доступные биржи_Ещё_Гонконгская
            "Внебиржевой рынок" -> /Доступные биржи_Ещё_Внебиржа
            "Иностранные облигации" -> /Иностранные облигации
            "Время торгов на биржах" -> /Время торгов
            "Назад" -> /Доступные биржи

    state: Доступные биржи_Ещё_Гонконгская
        a: ✅ Минимальный объем заявки — 8000 HKD. Все активы торгуются в гонконгских долларах, автоконвертация валюты при покупке не происходит.
            ✅ Маржинальная торговля недоступна
            ❗ Для работы с данными инструментами требуется статус квалифицированного инвестора
            ❗ По счетам ЕДП, открытым с 15.08.2022 по 13.02.2023, доступ не предоставляется

    state: Доступные биржи_Ещё_Внебиржа
        a: Для клиентов «Финам» доступно совершение внебиржевых сделок:
            1. Внебиржевые сделки (кроме указанных ниже) — выставление заявок доступно через подписание поручения в личном кабинете: https://edox.finam.ru/Journals/UnionDocumentJournal 
            ❗ Предварительно нужно обратится к менеджеру и согласовать параметры заявки (количество, цену, найти покупателя/продавца).
            2. Московская и СПБ Биржа (на СПБ Бирже торги приостановлены) — по ограниченному списку инструментов, ранее заблокированных европейскими депозитариями Euroclear и Clearstream, доступны сделки купли/продажи через систему TRANSAQ и FinamTrade. Для приобретения ЦБ требуется статус квалифицированного инвестора (квал), продажа доступна без статуса. 
            ❗ Перед выставлением заявок обязательно ознакомьтесь с условиями совершения сделок. 
            3. Московская биржа — по ограниченному списку инструментов можно будет выставлять заявки через торговые системы TRANSAQ и QUIK, а также через звонок в отдел голосового трейдинга (доб. 2200). Для приобретения ЦБ требуется статус квалифицированного инвестора, продажа доступна без статуса. Актуальный список ЦБ и подробности сделок доступны по ссылке: https://www.moex.com/a8428
        buttons:
            "Торговля заблокированными ЦБ" -> /Как закрыть позиции_Продажа БлокЦБ
            "Назад" -> /Доступные биржи_Ещё

    state: Время торгов || sessionResultColor = "#418614"
        intent: /045 Время торгов
        if: holidays()
            a: ✅ 9 мая - торги и расчёты на Московской бирже не проводятся. На иностранных биржах торги проводятся в стандартном режиме
                ✅ 10 мая торги на биржах проводятся в стандартном режиме. Не проводятся торги и расчёты на валютной секции Московской биржи в режимах TOD и своп (TODTOM)
                ✅ [График работы «Финам» в майские праздники|https://www.finam.ru/publications/item/grafik-raboty-finama-27-aprelya-12-maya-20240422-1338/]
        a: ✅ Для уточнения штатного времени работы биржи, выберите соответствующую секцию:
        buttons:
            "Московская биржа" -> /Время торгов_Московская биржа
            "Биржа СПБ" -> /Время торгов_Биржа СПБ
            "Биржи США" -> /Время торгов_Биржи США
            "Гонконг (HKEX)" -> /Время торгов_Гонконг
            "Европейские биржи" -> /Время торгов_Европейские биржи
            "Внебиржевые торги" -> /Время торгов_Внебиржевые торг
            "Демо-счета" -> /Демо-счет

    state: Время торгов_Московская биржа
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Фондовый рынок" -> /Время торгов_Московская биржа_Фондовый рынок
            "Срочный рынок" -> /Время торгов_Московская биржа_Срочный рынок
            "Валютный рынок" -> /Время торгов_Московская биржа_Валютный рынок
            "Назад" -> /Время торгов

    state: Время торгов_Московская биржа_Фондовый рынок
        a: ✅ Премаркет основной сессии — с 9:50 до 10:00 МСК,
            ✅ основная сессия — с 10:00 до 18:40 МСК, 
            ✅ постмаркет основной сессии — с 18:40 до 18:50 МСК, 
            ✅ премаркет вечерней сессии — с 19:00 до 19:05 МСК,
            ✅ вечерняя сессия — с 19:05 до 23:50 МСК.   
            В выходные дни торги не проводятся. 
            Торговый календарь: https://www.moex.com/ru/tradingcalendar/

    state: Время торгов_Московская биржа_Срочный рынок
        a: Торговая сессия начинается вечером и длится с 19:05 до 23:50, продолжается на следующий день — с 9:00 до 14:00 и с 14:05 до 18:50 МСК.
            Торговый календарь: https://www.moex.com/ru/tradingcalendar/

    state: Время торгов_Московская биржа_Валютный рынок
        a: Торги драгоценными металлами в режиме _TOM проводятся с 6:50 до 19:00 МСК.
            Торги валютными парами в режиме _SPT, _TOM, _TMS проводятся с 6:50 до 19:00 МСК. 
            Торги по валютным парам в режиме _TOD и СВОП проводятся согласно регламенту брокерского обслуживания (Приложение 24.1) с 6:50 до:
            ✅ USDRUB – 17:25
            ✅ EURRUB, EURUSD – 14:45
            ✅ USDCNY, CNYRUB – 11:50
            ✅ BYNRUB, TRYRUB, KZTRUB – 11:45
            ✅ HKDRUB – 10:25.
            Торговый календарь: https://www.moex.com/ru/tradingcalendar/

    state: Время торгов_Биржа СПБ
        a: 1. Российские ценные бумаги:
            ✅ Основная сессия — с 10:00 до 18:50 МСК.
            2. Американские бумаги:
            ✅ Основная сессия — с 8:00 до 19:00 МСК, (по разным категориям инструментов время начала торгов отличается: https://spbexchange.ru/ru/stocks/master-release.aspx ),
            ✅ Вечерняя сессия — с 19:00 до 1:45 МСК (по ETF фондам доступ только до 00:00 МСК)
            3. Гонконгские бумаги:
            ✅ Основная сессия — с 8:00 до 00:00 МСК, (по ETF время начала торгов отличается: https://spbexchange.ru/ru/listing/etf/ ).
            ❗ В выходные дни торги не проводятся.
            Торговый календарь: https://spbexchange.ru/ru/about/torg_calendar/

    state: Время торгов_Биржи США
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "NYSE/NASDAQ" -> /Время торгов_Биржи США_NYSE и NASDAQ
            "Опционы США" -> /Время торгов_Биржи США_Опционы США
            "Назад" -> /Время торгов

    state: Время торгов_Биржи США_NYSE и NASDAQ
        a: Летнее время:
            ✅ премаркет — с 11:00 до 16:29 МСК,
            ✅ основная сессия — с 16:30 до 23:00 МСК,
            ✅ постмаркет — с 23:00 до 00:00 МСК (по сегрегированным счетам доступа нет).
            Зимнее время:
            ✅ премаркет — с 12:00 до 17:29 МСК,
            ✅ основная сессия — с 17:30 до 00:00 МСК,
            ✅ постмаркет — с 00:00 до 01:00 МСК (по сегрегированным счетам доступа нет).
            В выходные дни торги не проводятся.
            ❗ Во время премаркета и постмаркета на рынке NYSE/NASDAQ недоступно выставление «рыночных» заявок, рекомендуем работать с «лимитными» ордерами (по определенной цене).

    state: Время торгов_Биржи США_Опционы США
        a: ✅ Летнее время: с 16:30 до 23:00 МСК
            ✅ Зимнее время: с 17:30 до 24:00 МСК
            В выходные дни биржа не работает

    state: Время торгов_Гонконг
        a: Основная сессия — с 8:00 до 11:00 МСК.
            В выходные дни торги не проводятся.

    state: Время торгов_Европейские биржи
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Париж (EURONEXT)" -> /Время торгов_Европейские биржи_Расписание
            "Мадрид (BME)" -> /Время торгов_Европейские биржи_Расписание
            "Франкфурт (Xetra)" -> /Время торгов_Европейские биржи_Расписание
            "Лондон (LSE)" -> /Время торгов_Европейские биржи_Расписание
            "Назад" -> /Время торгов

    state: Время торгов_Европейские биржи_Расписание
        a: ✅ Летнее время:
            10:00 — 18:30 (по МСК).
            ✅ Зимнее время:
            11:00 — 19:30 (по МСК). 
            В выходные дни торги не проводятся.

    state: Время торгов_Внебиржевые торг
        a: ✅ Внебиржевые торги на ММВБ (ОТС с ЦК):
            Основная сессия — с 10:00 до 18:40 МСК, 
            Вечерняя сессия — с 19:05 до 23:50 МСК.   
            В выходные дни торги не проводятся. 
            Торговый календарь: https://www.moex.com/ru/tradingcalendar/ 
            ✅ Внебиржевые торги заблокированными ЦБ на ММВБ — с 11:00 до 17:00 МСК
            В выходные дни торги не проводятся

    state: Праздники || sessionResultColor = "#418614"
        intent!: /046 Праздники
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Расписание торгов на бирже" -> /Время торгов
            "Режим работы «Финам»" -> /Контакты
 
    state: Режим расчетов || sessionResultColor = "#418614"
        intent!: /047 Режим расчетов
        if: holidays()
            a: ✅ 9 мая - торги и расчёты на Московской бирже не проводятся. На иностранных биржах торги проводятся в стандартном режиме
                ✅ 10 мая торги на биржах проводятся в стандартном режиме. Не проводятся торги и расчёты на валютной секции Московской биржи в режимах TOD и своп (TODTOM)
                ✅ [График работы «Финам» в майские праздники|https://www.finam.ru/publications/item/grafik-raboty-finama-27-aprelya-12-maya-20240422-1338/]
        a: Торги на биржах осуществляются в режимах T+0, Т+1 и Т+2.
            Это значит, что регистрация прав на ценные бумаги/валюту происходит не в момент заключения сделки, а позднее.
            ✅ Т+0 – расчеты в день сделки,
            ✅ Т+1 (Y1) – расчеты на следующий рабочий день,
            ✅ Т+2 (Y2) – расчеты на второй рабочий день.
            1. Торги облигациями осуществляются в режиме Т+1.
            Накопленный купонный доход считается на дату расчетов по сделке и перечисляется продавцу в тот же день.
            2. Торги на Московской бирже акциями, инвестиционными паями и ETF проводятся в режиме Т+1. Подробнее в презентации: https://fs.moex.com/files/25603 
            3. Торги на СПБ Бирже российскими и квазироссийскими акциями проводятся в режиме Т+1, международными ценными бумагами — в режиме Т+2.
            4. Торги международными ценными бумагами на иностранных биржах проходят в режиме Т+2.
            5. Внебиржевые торги на ММВБ ОТС с ЦК осуществляются в режиме Т+1.
            6. Торги валютой на бирже осуществляются в режимах: T+0 (TOD), T+1 (TOM, TMS) и T+2 (SPT).
            7. Торги на срочном рынке (ПФИ) не имеют отложенных расчетов, но фактическое начисление вариационной маржи происходит только в основной клиринг (на FORTS в 18:50 - 19:00 МСК).
        a: ❗ С 31 июля 2023 года Московская биржа перевела торги акциями и облигациями, а СПБ Биржа – российскими и квазироссийскими акциями, на единый расчетный цикл T+1.
            Расчеты по заключенным сделкам в основных режимах торгов с ценными бумагами и поставка бумаг, в том числе по срочным контрактам на акции, теперь осуществляются на следующий торговый день.
            Подробнее на сайте биржи: 
            ✅ Московская биржа: https://www.moex.com/n56493/?nt=0 
            ✅ СБП Биржа и список квазироссийских ЦБ: https://spbexchange.ru/ru/about/news.aspx?bid=25&news=43242 
            ❗ По международным ценным бумагам на СПБ Бирже (за исключением квазироссийских) код расчётов не изменился и остаётся Т+2.

    state: Ограничение ЦБ || sessionResultColor = "#418614"
        intent!: /048 Ограничения ЦБ
        a: На СПБ Бирже приостановлены торги иностранными ценными бумагами. Актуальная информация размещается на [официальном сайте биржи|https://spbexchange.ru/ru/about/news2.aspx].
            ✅ В рамках Указа № 665, депозитарии используют рубли РФ для выплат по заблокированным активам. Выплаты первой очереди, поступившие брокеру от СПБ Биржи, перечислены инвесторам. В настоящий момент информации о предстоящих выплатах от биржи не поступало.
            ❗ После перевода СПБ Биржей бумаг на неторговый раздел, бумаги исключены из торговых лимитов биржи, и не отображаются в терминале, но их наличие отражено во вкладке «Портфель» в [личном кабинете|https://lk.finam.ru/].
            ❗ При торговле на иностранных биржах через брокера «Финам» инфраструктура СПБ Биржи не задействована. Вышестоящий брокер-партнёр не раскрывает перед американскими биржами гражданство своих клиентов, поэтому риски в данном направлении минимальны.
        a: ✅ Актуальные новости о решении проблемы заблокированных активов, в связи с санкциями западных регуляторов на российскую финансовую систему, публикуются на сайте «Финам» по ссылке: https://www.finam.ru/theme/unlocking-foreign-securities/
            ✅ Клиентам «Финам» доступны сделки с заблокированными иностранными ценными бумагами на внебирже, ознакомиться можно, выбрав соответствующую кнопку ниже.
            ✅ Если у вас сохранились акции, полученные после конвертации АДР/ГДР в 2022 году, нажмите на кнопку ниже, чтобы узнать, как перенести их на торговый раздел.
            ✅ На брокерских счетах Банка «Финам» ограничены торговые операции, в связи с переводом брокерских счетов из Банка в инвестиционную компанию АО «Финам». Для сохранения инвестиционных возможностей рекомендуем открыть брокерский счет в АО «Финам».
            Выберите нужную тему, чтобы узнать подробнее:
        buttons:
            "Выкуп ИЦБ по указу № 844" -> /Ограничение ЦБ_844
            "Разблокировка ЦБ после конвертации ДР" -> /Депозитарное поручение_Ещё_Разблокировка ЦБ
            "Перевод активов из Банка в АО Финам" -> /Депозитарное поручение_Ещё_Перевод активов
            "Торговля заблокированными ЦБ" -> /Как закрыть позиции_Продажа БлокЦБ
            "Фонды FinEX" -> /Ограничение ЦБ_FinEX
            "Указ Президента РФ № 665" -> /Ограничение ЦБ_665

    state: Ограничение ЦБ_FinEX || sessionResultColor = "#B65A1E"
        a: Московская биржа исключила из списка ценных бумаг 22 фонда FinEx 9 августа 2023 года.
            ✅ У держателей ETF FinEx фонды отображаются в справке по счету и [в личном кабинете|https://lk.finam.ru/]
            ✅ [Официальный сайт FinEx|https://finex-etf.ru/]
            Выберите тему, чтобы узнать подробнее:
        buttons:
            "Выкуп ИЦБ по указу № 844" -> /Ограничение ЦБ_844
            "Торговля заблокированными ЦБ" -> /Как закрыть позиции_Продажа БлокЦБ
    
    state: Ограничение ЦБ_844 || sessionResultColor = "#B65A1E"
        a: Прием заявок на обмен активов в рамках Указа № 844 завершен.
            ✅ До 9:00 МСК 6 мая клиенты «Финама» могли подать заявку на обмен заблокированных активов российских инвесторов в рамках Указа № 844 «О дополнительных временных мерах экономического характера, связанных с обращением иностранных ценных бумаг». [Подробнее|https://www.finam.ru/publications/item/finam-nachal-priem-zayavok-na-obmen-zablokirovannymi-aktivami-20240325-1446/]
            ✅ Статус своей поданной заявки можно посмотреть в личном кабинете в [разделе «Журнал поручений»|https://lk.finam.ru/reports/documents]
            ✅ Организатор выкупа [ООО «Инвестиционная палата»|https://vykupicb.investpalata.ru/documents]

    state: Ограничение ЦБ_665 || sessionResultColor = "#B65A1E"
        a: На СПБ Бирже приостановлены торги иностранными ценными бумагами. Актуальная информация размещается на [официальном сайте биржи|https://spbexchange.ru/ru/about/news2.aspx].
            ✅ В рамках Указа № 665, депозитарии используют рубли РФ для выплат по заблокированным активам. Выплаты первой очереди, поступившие брокеру от СПБ Биржи, перечислены инвесторам. В настоящий момент информации о предстоящих выплатах от биржи не поступало.
            ❗ Брокер «Финам» с 01.01.2024 является налоговым агентом по дивидендам на иностранные ценные бумаги, то есть выплаты будут облагаться налогом.

    state: Санкции || sessionResultColor = "#418614"
        intent!: /049 Санкции
        a: На СПБ Бирже приостановлены торги иностранными ценными бумагами. Актуальная информация размещается на [официальном сайте биржи|https://spbexchange.ru/ru/about/news2.aspx].
            ✅ В рамках Указа № 665, депозитарии используют рубли РФ для выплат по заблокированным активам. Выплаты первой очереди, поступившие брокеру от СПБ Биржи, перечислены инвесторам. В настоящий момент информации о предстоящих выплатах от биржи не поступало.
            ❗ После перевода СПБ Биржей бумаг на неторговый раздел, бумаги исключены из торговых лимитов биржи, и не отображаются в терминале, но их наличие отражено во вкладке «Портфель» в [личном кабинете|https://lk.finam.ru/].
        a: 1. Влияет ли новый пакет санкций на торговлю через «Финам» и на возможность приобретать иностранные активы.
            Нет, квалифицированным инвесторам через «Финам» доступны иностранные ценные бумаги на Американских биржах.
            2. Есть ли риск внесения «Финам» в санкционный список?
            В данный момент никакой информации о возможном внесении в санкционный список нет.
            3. Могут ли заблокировать акции на ММА, купленные через «Финам»?
            Наш вышестоящий брокер-партнёр не раскрывает перед американскими биржами гражданство своих клиентов, поэтому риски в данном направлении минимальны.
            4. Где хранятся иностранные ценные бумаги, купленные на иностранных биржах?
            Хранение осуществляется в депозитарии DTCC.
            5. Были ли введены санкции против АО «Финам» или Банк «Финам»?
            Нет, санкции не затронули ни одну из компаний финансовой группы «Финам».
            
    state: Корпоративные действия || sessionResultColor = "#418614"
        intent!: /050 Корпоративные действия
        
        # script:
        #     if ( typeof $parseTree._KD_redirect != "undefined" ){
        #         $session.KD_redirect = $parseTree._KD_redirect;
        #         $reactions.transition("/Корпоративные действия_" + $session.KD_redirect.name);
        #     }
            
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Размещение акций и облигаций" -> /Корпоративные действия_Размещение
            "Замещение облигаций" -> /Корпоративные действия_Замещение
            "Оферта по облигации" -> /Корпоративные действия_Оферта
            "Другие корпоративные действия" -> /Корпоративные действия_Другие

    state: Корпоративные действия_Размещение || sessionResultColor = "#B65A1E"
        a: Инвесторам «Финам» доступно участие в публичных размещениях облигаций и ценных бумаг (IPO).
            ✅ Подать заявку и посмотреть ее статус можно:
            — [в личном кабинете|https://lk.finam.ru/ipo]
            — в мобильном приложении или в веб-терминале [FinamTrade|https://trading.finam.ru] в левом вертикальном меню в разделе «Первичные размещения» (значок ракеты) выбрать «Мои заявки»
            ❗ Номер заявки отобразится в личном кабинете в день размещения
            ❗ Отредактировать уже поданную заявку на размещение - невозможно. Если нужно внести изменения, следует отменить заявку и подать заново
            ✅ IPO на зарубежных площадках доступно со статусом квалифицированного инвестора
            ✅ Заявка на участие в аукционе ОФЗ аналогична стандартной процедуре участия в IPO
            ✅ Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Условия участия в IPO" -> /Корпоративные действия_Размещение_Условия
            "Календарь IPO" -> /Корпоративные действия_Размещение_Календарь
            "Отменить заявку на IPO" -> /Корпоративные действия_Размещение_Отмена
            "Назад" -> /Корпоративные действия

    state: Корпоративные действия_Замещение || sessionResultColor = "#B65A1E"
        a: Замещающие облигации — это локальные, выпущенные и обращающиеся в российской юрисдикции долговые бумаги, которые держатель получает взамен замещаемых евробондов.
            ✅ Подробная информация и полный перечень выпущенных замещающих облигаций в [разделе «Частые вопросы»|https://www.finam.ru/landings/replacement-bonds/]
            ✅ Как подать заявку: 
            1. перейдите [на сайт|https://edox.finam.ru/Ipo/Securities]
            2. выберите соответствующий выпуск
            3. подайте заявку
            4. выберите счет, на котором находятся еврооблигации, в графе «Мин. купон» — выберите нужный купон из диапазона (от 1000 долларов/евро). «Сумма заявки» — количество еврооблигаций для обмена * 1000
            5. подпишите заявление
            ❗ Если после подачи поручения пришел отказ из-за нехватки денежных средств, то повторно заполнять форму не нужно, текущая заявка будет обработана.

    state: Корпоративные действия_Оферта || sessionResultColor = "#B65A1E"
        a: Оферта по облигации — это возможность досрочно продать ее эмитенту. [Подробнее|https://www.finam.ru/publications/item/oferta-obligacii-chto-eto-zachem-o-neiy-znat-investoru-20200917-16480/]
            ✅ Подать заявку на участие в оферте можно в личном кабинете по ссылке: https://edox.finam.ru/orders/default.aspx?ts=OFFER%20BONDS 
            ✅ Детальную информацию об условиях участия в корпоративных действиях по конкретному инструменту можно уточнить у менеджера.

    state: Корпоративные действия_Другие
        a: Корпоративными действиями называются мероприятия эмитента, направленные на распределение доходов в денежной или иной форме между держателями ценных бумаг или изменение структуры ценных бумаг. Примеры:
            1. дробление/консолидация ценных бумаг (сплит),
            2. смена тикера,
            3. добровольные и принудительные выкупы ценных бумаг.
            ❗ Реквизиты для зачисления средств необходимо [предоставить в личном кабинете|https://edox.finam.ru/orders/depoBankAccountDetails.aspx]
            ❗ Инвестор должен самостоятельно отслеживать корпоративные действия с ценными бумагами на сайте биржи, где бумага приобреталась, также «Финам» публикует актуальные корпоративные действия в [разделе «Новости депозитария»|https://www.finam.ru/publications/section/deponews/]
            ✅ Детальную информацию об условиях участия в корпоративных действиях по конкретному инструменту можно уточнить у менеджера.
        buttons:
            "Как подать заявку на участие" -> /Корпоративные действия_Другие_Как подать
            "Сроки выплат/зачислений" -> /Выплата дохода_Срок выплаты
            "Назад" -> /Корпоративные действия

    state: Корпоративные действия_Размещение_Условия
        a: Условия подачи заявки на участие в IPO зависят от выбранной биржи. Выберите биржу:
        buttons:
            "Московская ФБ" -> /Корпоративные действия_Размещение_Условия_Москва
            "Биржа СПБ" -> /Корпоративные действия_Размещение_Условия_СПБ
            "NYSE/NASDAQ" -> /Корпоративные действия_Размещение_Условия_NYSE NASDAQ
            "Назад" -> /Корпоративные действия_Размещение

    state: Корпоративные действия_Размещение_Календарь
        a: Следите за предстоящими IPO в специальном [календаре|https://edox.finam.ru/Ipo/Securities]

    state: Корпоративные действия_Размещение_Отмена
        a: Чтобы отменить заявку на участие в IPO:
            ✅ зайдите в раздел [подачи заявок на IPO|https://lk.finam.ru/ipo]
            ✅ выберите нужное размещение
            ✅ в появившемся меню выберите «Отменить поручение (КЛФ-***)»

    state: Корпоративные действия_Размещение_Условия_Москва
        a: Заявки принимаются от 1000 ₽ для размещений бумаг «Финама» и от 10000 ₽ для сторонних размещений, если иное не установлено эмитентом.
            ✅ Комиссии за участие в размещении:
            1. облигаций российских эмитентов: 0,04% от оборота (при обороте в день до 1000000 ₽), 0,015% комиссия биржи за урегулирование, комиссия от оборота на фондовой секции по тарифному плану.
            2. иных ценных бумаг: 0,236% от суммы сделки, 0,03% комиссия биржи за урегулирование, комиссия от оборота на фондовой секции по тарифному плану.
            ✅ Статус квалифицированного инвестора не требуется, если иное не установлено эмитентом.

    state: Корпоративные действия_Размещение_Условия_СПБ
        a: Заявки принимаются от 1000 ₽ для размещений бумаг «Финама» и от 10000 ₽ для сторонних размещений, если иное не установлено эмитентом.
            ✅ Комиссии за участие в размещении: 0,01% комиссия биржи за урегулирование, комиссия от оборота на фондовой секции по тарифному плану.
            ✅ Статус квалифицированного инвестора не требуется, если иное не установлено эмитентом.

    state: Корпоративные действия_Размещение_Условия_NYSE NASDAQ
        a: Заявки принимаются от 1000 $, если иное не установлено эмитентом.
            Дополнительная комиссия за участие в размещении составляет 5% от размещенной суммы (2,5% - в рублях РФ, 2,5% - в долларах США).
            ✅ На момент подачи заявки по счету необходимо обеспечить свободные доллары США в сумме, достаточной для подачи заявки.
            ✅ Доступно только со статусом квалифицированного инвестора.

    state: Корпоративные действия_Другие_Как подать
        a: ✅ [Подать заявку на участие в добровольном выкупе ценных бумаг в личном кабинете|https://edox.finam.ru/Depo/CorporateActionsTypes]
            ✅ Заявка на участие в принудительном выкупе не требуется
            ❗ Реквизиты для зачисления средств необходимо [предоставить в личном кабинете|https://edox.finam.ru/orders/depoBankAccountDetails.aspx]

    state: Американский турнир || sessionResultColor = "#418614"
        intent!: /051 Американский турнир
        a: ✅ Зарегистрируйтесь в конкурсе «Американский турнир» и получите виртуальные 100000 $ на конкурсный демо-счет на платформе web-версии терминала FinamTrade.
            ✅ В период действия конкурса торгуйте на эти деньги на американских биржах NASDAQ и NYSE и получите приз до 100000 ₽ за лучшую доходность по счету.
            ✅ [Узнать подробнее и зарегистрироваться в турнире|https://www.finam.ru/landings/konkurs-trader-2023]

    state: Pre-IPO || sessionResultColor = "#418614"
        intent!: /052 Pre-IPO
        a: Pre-IPO позволяет инвестировать в акции компаний, которые пока не торгуются на бирже, но в ближайшем будущем могут там появиться. Услуга доступна только квалифицированным инвесторам. Минимальная сумма вложений — 10000 $. [Больше информации о Pre-IPO и доступных инструментах|https://www.finam.ru/landings/landing-pre-ipo]

    state: Криптовалюты || sessionResultColor = "#418614"
        intent!: /053 Криптовалюты
        a:«Финам» не предоставляет прямой доступ к торговле криптовалютой. В терминале FinamTrade текущие котировки криптовалют отображаются для ознакомления.
            У клиентов «Финам» со статусом квалифицированного инвестора есть возможность альтернативных инвестиций:
            ✅ Спотовые биткоин-ETF:
            — Bitwise: [BITB|https://trading.finam.ru/profile/NYSE-BITB] 
            — iShares BlackRock: [IBIT|https://trading.finam.ru/profile/NSDQ-IBIT] 
            — Valkyrie: [BRRR|https://trading.finam.ru/profile/NSDQ-BRRR] 
            ✅ Готовый портфель «Криптовалюта, майнинг, блокчейн» - возможность инвестировать в высокотехнологичные крипто- и майнинг-компании без активного совершения сделок и рисков блокировки активов. Нажмите кнопку ниже, чтобы узнать подробнее.
        buttons:
            "Портфель Криптовалюта, майнинг" -> /Криптовалюта_Портфель

    state: Криптовалюта_Портфель
        a: Вы сможете участвовать в росте компаний высокотехнологичного сектора, инвестируя в готовый портфель. Портфель «Криптовалюта, майнинг, блокчейн» реализован в виде стратегии автоследования – это значит, что все сделки будут автоматически повторяться на счёте инвестора, подключившего стратегию.
            [Подробнее|https://www.comon.ru/strategies/113405/]

    state: Индексы и сырье || sessionResultColor = "#15952F"
        intent!: /054 Индексы и сырье
        a: Индексы и сырье являются индикативными инструментами и недоступны для торговли.
            Альтернативными вариантами вложений могут являться:
            ✅ российские и американские фьючерсные контракты на индексы и сырье, [подробнее|https://www.moex.com/ru/derivatives/]
            ✅ ETF, основанные на ценных бумагах, входящих в состав индекса
            ✅ ETF, основанные на ценных бумагах сырьевой отрасли

    state: CFD контракты || sessionResultColor = "#15952F"
        intent!: /055 CFD контракты
        a: «Финам» не предоставляет доступ к торговле CFD-контрактами.

    state: Аристократы Финам || sessionResultColor = "#418614"
        intent!: /056 Аристократы Финам
        a: 2 мая 2023 года ООО «Управляющая компания «Финам Менеджмент» приняла решение о прекращении БПИФ «Дивидендные Аристократы РФ» и «Дивидендные Аристократы США».
            ✅ Официальная информация [на сайте Управляющей компании|https://www.fdu.ru/news/3716]
            ✅ В целях соблюдения интересов пайщиков Управляющая компания завершила все действия, связанные с прекращением фондов и выплатой денежных средств, учитывая требования Законодательства РФ.
            ✅ Денежные средства от погашения инвестиционных паев были выплачены инвесторам в августе 2023 года.

    state: Форвардные контракты || sessionResultColor = "#15952F"
        intent!: /057 Форвардные контракты
        a: «Финам» не предоставляет доступ к торговле форвардными контрактами.

    state: Бинарные опционы || sessionResultColor = "#15952F"
        intent!: /058 Бинарные опционы
        a: «Финам» не предоставляет доступ к торговле бинарными опционами.

    state: J2T (Lime Trading) || sessionResultColor = "#15952F"
        intent!: /059 Lime Trading
        a: По вопросам обслуживания счетов Just2Trade (Lime Trading) обратитесь по контактам: https://just2trade.online/ru/

    state: Стороннее ПО || sessionResultColor = "#418614"
        intent!: /060 Стороннее ПО
        a: Подключить сторонние программы можно с помощью TRANSAQ Connector, ComonTrade API и QUIK.
            ✅ Оплата доступа к стороннему ПО производится на сайте разработчиков.
            ✅ Подробнее о программах для торговли по ссылке: https://www.finam.ru/howtotrade/Welcome/#auto.area_a
        buttons:
            "TRANSAQ Connector" -> /Стороннее ПО_TRANSAQ Connector
            "QUIK" -> /Стороннее ПО_QUIK
            "ComonTrade API" -> /Стороннее ПО_ComonTrade API
            "Sterling Trader Pro" -> /Стороннее ПО 2
            "Lightspeed Trader" -> /Стороннее ПО 2

    state: Стороннее ПО_TRANSAQ Connector || sessionResultColor = "#CD4C2B"
        a: Подключить счет к TRANSAQ Connector можно в личном кабинете: https://edox.finam.ru/ITS/AddTerminal
            Доступ к системе бесплатный. 
            После того, как вы подпишите заявление на подключение терминала, вам придет СМС с паролем от системы. Логин находится в личном кабинете: https://edox.finam.ru/Home/Account/Terminals?id= Найдите в открывшемся списке идентификатор терминала TRANSAQ Connector.

    state: Стороннее ПО_QUIK
        a: Подключить счет к QUIK можно в личном кабинете: https://edox.finam.ru/ITS/AddTerminal
            Доступ к системе бесплатный. 
            Подробная информация о том, как сгенерировать ключи (логин и пароль), доступна по ссылке: https://www.finam.ru/howtotrade/KeyGen/  
            После того, как вы подключите счет к терминалу, свяжитесь с менеджером «Финама». Он поможет активировать поток обезличенных сделок.

    state: Стороннее ПО_ComonTrade API
        a: Comon Trade Api — это REST API, предназначенное для организации взаимодействия пользовательских приложений с сервером TRANSAQ. Детальнее ознакомится и получить токен можно на сайте: https://finamweb.github.io/trade-api-docs/

    state: ИТС || sessionResultColor = "#418614"
        intent!: /061 ИТС
        if: technicalBreak()
            a: ✅ Баланс счета не отображается ночью в будние дни, во время технических перерывов, связанных с обслуживанием серверов торговых систем:: 
                — QUIK: с 3:00 до 6:40 МСК, 
                — TRANSAQ и FinamTrade: с 5:00 до 6:40 МСК. 
                В выходные дни дополнительные технические работы могут проводится в дневное время, так как торги не проводятся.
                ✅ В выходные и праздничные дни торги не проводятся, либо осуществляются в ограниченном формате.
                ✅ В рамках учебных счетов в неторговый период выставление всех типов заявок недоступно. Сервера учебных счетов начинают работать с 10:00 по МСК. В выходные и праздничные дни торги не проводятся.
        
        script:
            if ( typeof $parseTree._ITS != "undefined" ){
                $session.ITS = $parseTree._ITS;
                $reactions.transition("/ИТС_" + $session.ITS.name);
            }
        
        a: «Финам» предоставляет доступ к нескольким торговым системам:
            ✅ FinamTrade (веб-версия и мобильное приложение),
            ✅ TRANSAQ/TRANSAQ US (программы для ПК),
            ✅ QUIK (программа для ПК),
            ✅ QUIKX/WebQUIK (платное мобильное приложение — 420 ₽ в месяц),
            ✅ MetaTrader 5 (программа для ПК).
            ✅ Подключить стороннее ПО можно через TRANSAQ Connector (шлюз данных для подключения), ComonTrade API и QUIK.
            Выберите торговую систему, по которой у вас вопрос:
        buttons:
            "FinamTrade" -> /ИТС_FinamTrade
            "TRANSAQ" -> /ИТС_TRANSAQ
            "QUIK" -> /ИТС_QUIK
            "Другие ИТС" -> /ИТС_Другие
            "Демо-счета/Обучение" -> /Демо-счет

    state: ИТС_FinamTrade
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Авторизация в FinamTrade" -> /ИТС_FinamTrade_Авторизация
            "Настройки" -> /ИТС_FinamTrade_Настройки
            "Работа с портфелем" -> /ИТС_FinamTrade_Работа с портфелем
            "Работа с заявками" -> /Заявки
            "Работа с графиком" -> /ИТС_FinamTrade_Работа с графиком
            "Дополнительные функции" -> /ИТС_FinamTrade_Дополнительные функции
            "Частые ошибки" -> /Ошибки заявок
            "Назад" -> /ИТС

    state: ИТС_FinamTrade_Авторизация
        a: Данными для входа в FinamTrade, при использовании типа авторизации «Личный кабинет», являются логин и пароль от личного кабинета https://lk.finam.ru/
            ✅ По умолчанию логином от личного кабинета является номер телефона в международном формате (например: начиная с «7…» - Россия, «375…» - Беларусь, «997…» - Казахстан)
            ✅ Пароль вы задавали самостоятельно
            ✅ Для восстановления доступа к личному кабинету:
            1. перейдите по [ссылке|https://lk.finam.ru/]
            2. нажмите на кнопку «Забыли логин или пароль?»
            3. введите ФИО, паспортные данные и подтвердите восстановление
            4. На вашу электронную почту придет письмо с логином и ссылкой на создание нового пароля

    state: ИТС_FinamTrade_Настройки
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Выбор счета по умолчанию" -> /ИТС_FinamTrade_Настройки_Выбор счета
            "Сохранение настроек" -> /ИТС_FinamTrade_Настройки_Сохранение настроек
            "Настройка интерфейса" -> /ИТС_FinamTrade_Настройки_Настройка интерфейса
            "Настройка горячих клавиш" -> /ИТС_FinamTrade_Настройки_Горячие клавиши
            "Назад" -> /ИТС_FinamTrade

    state: ИТС_FinamTrade_Настройки_Выбор счета
        a: Стандартно в заявке выбирается первый активный счёт, выбранный счёт можно поменять в самой заявке, либо выставить счёт по умолчанию через раздел «Меню» → «Настройки» для Android системы, на IOS системе выбрать счет по умолчанию нельзя.

    state: ИТС_FinamTrade_Настройки_Сохранение настроек
        a: Сохранение списков избранного происходит автоматически на сервере торговой системы, списки синхронизируются на всех ваших устройствах с выбранным аккаунтом.
            Сохраненные настройки графика сохраняются автоматически локально на устройстве (в кэше устройства), синхронизация с другими устройствами не происходит. 
            Также, возможен сброс всех настроек до исходного состояния в настройках веб-терминала: «Настройки приложения» → «Сервис» → «Сбросить состояние приложения».

    state: ИТС_FinamTrade_Настройки_Настройка интерфейса
        a: Во всех версиях торговой системы доступен выбор светлой/темной темы, а также выбор языка интерфейса. Настройка данных опций находится в меню основных настроек.

    state: ИТС_FinamTrade_Настройки_Горячие клавиши
        a: Для упрощения работы с интерфейсом веб-версии FinamTrade можно использовать комбинации горячих клавиш, список актуальных комбинаций и настройка доступны в меню: «Настройки приложения» → «Горячие клавиши».

    state: ИТС_FinamTrade_Работа с портфелем
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Состояние счета" -> /ИТС_FinamTrade_Работа с портфелем_Состояние
            "Выбор валюты отображения" -> /ИТС_FinamTrade_Работа с портфелем_Выбор валюты
            "Средняя цена" -> /Балансовая средняя
            "Закрытие позиций" -> /ИТС_FinamTrade_Работа с портфелем_Закрытие позиций
            "Риск параметры" -> /ИТС_FinamTrade_Работа с портфелем_Риск параметры
            "История операций" -> /ИТС_FinamTrade_Работа с портфелем_История операций
            "Назад" -> /ИТС_FinamTrade

    state: ИТС_FinamTrade_Работа с портфелем_Состояние
        a: Для просмотра состояния портфеля необходимо выбрать счет (в личном кабинете доступно обозначение счета вручную), в состоянии счета отображаются все открытые позиции и остатки средств в соответствующих валютах.
            Также, отображается общая оценка вашего портфеля с учетом всех активов по текущему биржевому курсу в соответствии с выбранной настройкой валюты отображения.

    state: ИТС_FinamTrade_Работа с портфелем_Выбор валюты
        a: Валюту отображения можно выбрать самостоятельно на странице портфеля, по умолчанию инструменты транслируются в валюте номинала. При изменении валюты отображения происходит перерасчет по текущему биржевому курсу.

    state: ИТС_FinamTrade_Работа с портфелем_Закрытие позиций
        a: Для просмотра состояния портфеля необходимо выбрать счет (в личном кабинете доступно обозначение счета вручную), в состоянии счета отображаются все открытые позиции и остатки средств в соответствующих валютах.
            Также, отображается общая оценка вашего портфеля с учетом всех активов по текущему биржевому курсу в соответствии с выбранной настройкой валюты отображения.

    state: ИТС_FinamTrade_Работа с портфелем_Риск параметры
        a: В терминале FinamTrade начальные требования, суммарную оценку денежных средств, ценных бумаг и обязательств клиента можно посмотреть в разделе «Аналитика» по счету (прокрутить ниже), в мобильном приложении FinamTrade – в разделе «Детали» по счету.

    state: ИТС_FinamTrade_Работа с портфелем_История операций
        a: ✅ Историю операций в онлайн режиме можно посмотреть в [FinamTrade|https://trading.finam.ru/] (Android, IOS и [Web|https://trading.finam.ru/]), а также в личном кабинете во [вкладке «История»|https://lk.finam.ru/history]
            ✅ Справку по счету с детальным описанием операций и сделок можно загрузить только за закрытый торговый день в [личном кабинете|https://lk.finam.ru/reports/tax]

    state: ИТС_FinamTrade_Работа с графиком
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Настройки графика" -> /ИТС_FinamTrade_Работа с графиком_Настройки
            "Инструменты тех. анализа" -> /ИТС_FinamTrade_Работа с графиком_Тех анализ
            "Одновременное отображение нескольких графиков" -> /ИТС_FinamTrade_Работа с графиком_Несколько графиков
            "Назад" -> /ИТС_FinamTrade

    state: ИТС_FinamTrade_Работа с графиком_Настройки
        a: На странице отображения графика расположены меню управления основными параметрами: установка отображаемого периода, выбор вида графика, добавление отображения открытых позиций и активных заявок.

    state: ИТС_FinamTrade_Работа с графиком_Тех анализ
        a: 1. В мобильном приложении, чтобы выбрать и добавить индикаторы или инструменты технического анализа на график инструмента, выберите символ «ƒₓ» над графиком.
            2. В веб-версии терминала, инструменты и индикаторы («ƒₓ») находятся в верхнем левом углу над графиком инструмента.
            ✅ В основных настройках веб-терминала есть возможность выбора отображения индикаторов для всех инструментов, либо индивидуально для каждого.
            ✅ Подробные инструкции по видам и использованию индикаторов по ссылке: https://www.finam.ru/landings/tech-analysis-ab-test/?utm_source=mass&utm_medium=email&utm_campaign=tech_analyse 
            ✅ Список индикаторов с описанием: https://www.finam.ru/publications/section/indicators/ 
            ❗ Задействованные инструменты не переносятся на другие графики.
            ❗ Загрузка индикаторов от сторонних разработчиков недоступна.

    state: ИТС_FinamTrade_Работа с графиком_Несколько графиков
        a: Одновременное отображение нескольких графиков доступно только в веб-терминале для созданных списков. Выбор режима отображения доступен во вкладке избранных инструментов. Одновременное отображение одного инструмента с разными периодами недоступно.

    state: ИТС_FinamTrade_Дополнительные функции
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Работа с алертами" -> /ИТС_FinamTrade_Дополнительные функции_Алерты
            "Подключение готовых портфелей" -> /ИТС_FinamTrade_Дополнительные функции_Готовые портфели
            "Переход в личный кабинет" -> /ИТС_FinamTrade_Дополнительные функции_Личный кабинет
            "Доска опционов" -> /ИТС_FinamTrade_Дополнительные функции_Доска опционов
            "Назад" -> /ИТС_FinamTrade

    state: ИТС_FinamTrade_Дополнительные функции_Алерты || sessionResultColor = "#CD4C2B"
        a: В торговой системе есть возможность добавить оповещения о достижении уровня цены (алерты) по инструменту.
            ✅ Чтобы включить оповещение, на странице инструмента рядом с его наименованием нужно нажать на «колокольчик» и выбрать нужные параметры алерта.
            ✅ Для просмотра журнала алертов нужно выбрать значок «колокольчик» в левом вертикальном меню, при необходимости можно удалить неактуальные оповещения и просмотреть архив.
            ✅ Исполненные алерты отображаются в разделе вертикального меню «Уведомления».
            ✅ Алерты бесплатны.

    state: ИТС_FinamTrade_Дополнительные функции_Готовые портфели
        a: В торговой системе есть возможность использования готовых инвестиционных решений от ведущих аналитиков Финам. Возможно подключение объемных портфельных решений (сумму и срок инвестиции можно выбрать самостоятельно), а также доступно самостоятельное формирование портфеля на основании инвестиционных идей.
            ❗ Данный функционал доступен только при использовании типа авторизации «Личный кабинет».

    state: ИТС_FinamTrade_Дополнительные функции_Личный кабинет
        a: В мобильной версии FinamTrade реализована возможность перехода в личный кабинет: необходимо нажать на три полоски для вызова панели с меню, далее необходимо нажать на имя и в конце списка выбрать опцию «Переход в личный кабинет».

    state: ИТС_FinamTrade_Дополнительные функции_Доска опционов
        a: Доска опционов доступна в торговых системах: TRANSAQ, QUIK, FinamTrade (web).
            1. QUIK:
            ✅ на панели инструментов нужно нажать «Создать окно» → «Все типы окон» → «Доска опционов».
            2. TRANSAQ:
            ✅ на панели инструментов нажать «Таблицы» → «Финансовые инструменты»,
            ✅ нажать правой кнопкой мыши по таблице и с помощью выбора/поиска инструмента добавить необходимый базовый актив (фьючерс),
            ✅ нажать правой кнопкой мыши по добавленному инструменту и выбрать меню «Доска опционов».
            3. FinamTrade:
            ✅ слева на панели инструментов нужно перейти в категорию «Рынки» и выбрать необходимый фьючерс,
            ✅ справа от кнопки «Заявка» будет доступна кнопка «Опционы».

    state: ИТС_TRANSAQ || sessionResultColor = "#CD4C2B"
        a: Торговая система TRANSAQ предназначена для установки на ПК с системой Windows.
            ✅ Предоставляется бесплатно.
            ✅ Скачать дистрибутив можно по ссылке:
            1. TRANSAQ https://www.finam.ru/howtotrade/transaq/
            2. TRANSAQ US https://www.finam.ru/howtotrade/soft/transaq/downloads-us/
            ❗ Язык интерфейса TRANSAQ US – английский.
            ❗ Торговый сервер TRANSAQ US запускается в 11:30 МСК, подключение до этого времени недоступно.
            ✅ Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Авторизация в Transaq" -> /ИТС_TRANSAQ_Авторизация
            "Настройки" -> /ИТС_TRANSAQ_Настройки
            "Особенности отображения портфеля" -> /ИТС_TRANSAQ_Отображение
            "Работа с короткими позициями" -> /ИТС_TRANSAQ_Шорты
            "Частые ошибки" -> /Ошибки заявок
            "Назад" -> /ИТС

    state: ИТС_TRANSAQ_Авторизация
        a: Вы можете посмотреть логин в личном кабинете: https://edox.finam.ru/Home/Account/Terminals?id=
            Нажмите на счет, к которому нужен логин в TRANSAQ. Вы увидите список подключенных ко счету платформ. Найдите в нем идентификатор TRANSAQ. Пароль к терминалу вы получали в виде СМС при открытии счета. Если сообщение утеряно, вы можете восстановить пароль в личном кабинете: https://edox.finam.ru/ITS/ChangeTerminalPassword
            После первого входа нужно поменять пароль в настройках TRANSAQ.
            При подключении брокерских счетов АО «Финам» используется сервер tr1.finam.ru и порт 3900. 
            При подключении брокерских счетов АО «Банк Финам» используется сервер tr1.finambank.ru и порт: 3324.

    state: ИТС_TRANSAQ_Настройки
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Сохранение настроек" -> /ИТС_TRANSAQ_Настройки_Сохранение
            "Отображение/добавление вкладок" -> /ИТС_TRANSAQ_Настройки_Вкладки
            "Функции по умолчанию" -> /ИТС_TRANSAQ_Настройки_Функции
            "Настройки стакана котировок" -> /ИТС_TRANSAQ_Настройки_Стакан
            "Включение/отключение уведомлений" -> /ИТС_TRANSAQ_Настройки_Уведомления
            "Трансляция в системы тех. анализа" -> /ИТС_TRANSAQ_Настройки_Трансляция
            "Назад" -> /ИТС_TRANSAQ

    state: ИТС_TRANSAQ_Настройки_Сохранение
        a: Обращаем Ваше внимание, что при подключении брокерских счетов АО Финам используется сервер: tr1.finam.ru, порт: 3900. При подключении брокерских счетов АО Банк Финам используется сервер: tr1.finambank.ru, порт: 3324.

    state: ИТС_TRANSAQ_Настройки_Вкладки
        a: Для добавления вкладок необходимо зайти в меню Вид (на верхней панели терминала), далее необходимо перейти в пункт Настройка экранов. Скрыть, либо снова вернуть отображение вкладок можно в меню Вид – Закладки.

    state: ИТС_TRANSAQ_Настройки_Функции
        a: В меню Настройки – Параметры торгового терминала - Ввод заявок, есть возможность устанавливать по умолчанию такие параметры как: стандартное количество лотов, использование кредитных средств, принцип расчета выставляемой заявки и прочее.

    state: ИТС_TRANSAQ_Настройки_Стакан
        a: В меню Настройки – Параметры торгового терминала – Представление информации, есть возможность устанавливать/менять параметры отображения стакана котировок: порядок отображения, отображение собственных заявок.

    state: ИТС_TRANSAQ_Настройки_Уведомления
        a: Отключение/включение уведомлений и звуковых сигналов доступно в меню «Настройки» → «Параметры торгового терминала» → «Прочее» → «Звуковое оповещение».

    state: ИТС_TRANSAQ_Настройки_Трансляция
        a: Для активации функции экспорта данных необходимо в меню «Настройки» → «Параметры торгового терминала» → «Прочее», активировать функцию. Загрузить библиотеку экспорта данных в системы теханализа. Далее можно выбирать параметры экспорта в меню Файл → Экспорт сделок рынка.

    state: ИТС_TRANSAQ_Отображение
        a: Для корректного отображения портфеля по счетам типа «Единая денежная позиция» в верхней панели инструментов необходимо нажать кнопку «Режим Клиент/Юнион». В данном режиме весь состав портфеля отобразится в таблице «Единый портфель».

    state: ИТС_TRANSAQ_Шорты
        a: По умолчанию использование кредитных средств при выставлении заявки отключено. Для работы с короткими позициями в момент выставления заявки необходимо включать пункт «Использовать кредит».

    state: ИТС_QUIK
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Установка и создание ключей QUIK" -> /ИТС_QUIK_Ключи
            "Авторизация в QUIK" -> /ИТС_QUIK_Авторизация
            "Настройки QUIK" -> /ИТС_QUIK_Настройки
            "Работа с портфелем" -> /ИТС_QUIK_Портфель
            "Работа с заявками" -> /ИТС_QUIK_Заявки
            "Работа с графиком" -> /ИТС_QUIK_График
            "Частые ошибки" -> /Ошибки заявок_Другие_QUIK
            "Назад" -> /ИТС

    state: ИТС_QUIK_Ключи
        a: Шаг 1. Скачайте и установите программу по ссылке: https://www.finam.ru/howtotrade/quik/
            Шаг 2. Чтобы открыть доступ к QUIK для уже имеющегося счета, зайдите в личный кабинет → https://edox.finam.ru/ITS/AddTerminal  и подключите терминал к желаемому счету.
            Шаг 3. В корневой папке программы откройте папку KeyGen и запустите приложение с таким же названием.
            Шаг 4. В программе KeyGen на первом этапе выберите, где будут храниться файлы ключей (по умолчанию они сохраняются в папку KeyGen). Придумайте логин (имя пользователя) и пароль. Далее вы будете использовать их для входа в программу. Нажмите кнопку «Создать».
            Шаг 5. Зайдите в личный кабинет: https://edox.finam.ru/cryptography/CreateQuikCertificates.aspx
            Выберите идентификатор терминала для регистрации ключей, нажмите кнопку «Выберите файл» и укажите публичный ключ (pubring.txk), который вы создали на третьем шаге. Далее нажмите кнопку «Загрузить».  
            Шаг 6. Откройте QUIK, зайдите в меню «Система» → «Настройки» → «Основные настройки» → «Программа» → «Шифрование». Нажмите на кнопку с изображением молотка в конце третьей строки и укажите путь к файлам ключей pubring.txk и secring.txk в соответствующих полях. Для этого нажмите на кнопку с тремя точками и выберите ключ в открывшемся окне.   
            Шаг 7. После прохождения четвертого шага необходимо подождать 1 час. Затем вы сможете авторизоваться в QUIK с помощью логина и пароля, который придумали при создании ключей.
            Видеоинструкция по установке QUIK по ссылке: https://www.youtube.com/watch?v=A1dpP0fRToQ

    state: ИТС_QUIK_Авторизация
        a: Логин и пароль вы придумываете на этапе регистрации ключей для QUIK. К сожалению, восстановить эти данные невозможно, поэтому вам придется создать новую пару ключей.
            ✅ Рекомендуем пройти обучение по работе в QUIK. В первом уроке — инструкция по регистрации ключей: https://education.finam.ru/lk/course/kak-nastroit-torgovyi-terminal-quik/ 

    state: ИТС_QUIK_Настройки
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Подключение счета к QUIK" -> /ИТС_QUIK_Настройки_Подключение
            "Поток обезличенных сделок" -> /ИТС_QUIK_Настройки_Поток сделок
            "Сохранение рабочего места" -> /ИТС_QUIK_Настройки_Сохранение места
            "Подключение роботов" -> /ИТС_QUIK_Настройки_Роботы
            "Очистка от временных файлов" -> /ИТС_QUIK_Настройки_Очистка файлов
            "Модуль опционной аналитики" -> /ИТС_QUIK_Настройки_Модуль аналитики
            "Опционы в QUIK" -> /ИТС_QUIK_Настройки_Опционы
            "Назад" -> /ИТС_QUIK

    state: ИТС_QUIK_Настройки_Подключение
        a: В QUIK можно одновременно подключить только счета одного типа, например, два счета типа «Единая денежная позиция».
            ✅ При открытии счета типа «Единая денежная позиция» обратите внимание, будет ли вам доступен QUIK. Счета с запретом использования данного терминала не предусматривают его ручного подключения. 
            ✅ Создание нового терминала по счету доступно в личном кабинете по ссылке: https://edox.finam.ru/ITS/AddTerminal 
            ✅ Подключение дополнительного счета к терминалу QUIK доступно в личном кабинете по ссылке: https://edox.finam.ru/ITS/AddTerminalByAccount/IndexAsync

    state: ИТС_QUIK_Настройки_Поток сделок || sessionResultColor = "#CD4C2B"
        a: ✅ Чтобы подключить/отключить поток обезличенных сделок нужно:
            1. Оформить заявку в личном кабинете по ссылке: https://edox.finam.ru/ITS/DepersonalizedTransactions 
            2. Через час после подписания заявления активируется поток данных. Обязательно нужно переподключиться к торговому серверу.
            ✅ Если нужно подключить поток по классу «Индексы», обратитесь к менеджеру «Финам».
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: ИТС_QUIK_Настройки_Сохранение места
        a: ✅ Для сохранения рабочего места в QUIK необходимо перейти в меню «Система» → «Сохранить настройки в файл».
            ✅ Для автоматического сохранения настроек необходимо в меню «Система» → «Настройки» → «Основные настройки» → «Программа» → «Файлы настроек» → установить галку «Сохранять настройки в файл при выходе», предварительно необходимо выбрать файл для автоматического сохранения в меню «Использовать файл настроек».

    state: ИТС_QUIK_Настройки_Роботы
        a: Для подключения готовых алгоритмов (роботов) для системы QUIK необходимо зайти в меню «Сервисы» → «Lua скрипты».
            Для уточнения деталей и особенностей работы таких алгоритмов рекомендуем обращаться к разработчикам программы: https://arqatech.com/ru/support/

    state: ИТС_QUIK_Настройки_Очистка файлов
        a: Для очистки временных файлов в системе QUIK рекомендуется выполнить следующие действия:
            1. Закрыть программу QUIK, если она при этом открыта.
            2. В директории с программой удалить все файлы с расширением «*.log» и «*.dat» (кроме файлов с расширением «*.dat», в которых хранятся настройки внешних систем технического анализа, если такие подключены). 
            3. Запустить программу QUIK.

    state: ИТС_QUIK_Настройки_Модуль аналитики
        a: Последнюю версию модуля опционной аналитики можно получить у менеджера.
            ✅ Перед установкой данного обновления убедитесь, что у вас установлена версия Рабочего места QUIK не ниже 9.0.0.
            ✅ Версия отображается в заголовке окна Рабочего места QUIK.
            1. Установите Рабочее место QUIK.
            2. Распакуйте архив с обновлением Модуля опционной аналитики (StratVolat) в каталог с Рабочим местом QUIK.
            3. Запустите Рабочее место QUIK.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: ИТС_QUIK_Настройки_Опционы
        a: Система QUIK позволяет работать с опционами Московской биржи FORTS только в рамках срочного рынка договора с раздельными счетами.
            ❗ В рамках счетов типа «Единая денежная позиция» поток опционов не отображается. 
            ✅ Доска опционов открывается через панель инструментов: «Создать окно» → «Все типы окон» → «Доска опционов».

    state: ИТС_QUIK_Портфель
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Нулевые позиции в состоянии счета" -> /ИТС_QUIK_Портфель_Нулевые позиции
            "Не отобразились позиции в портфеле" -> /ИТС_QUIK_Портфель_Позиции в портфеле
            "Состояние портфеля моносчета FORTS" -> /ИТС_QUIK_Портфель_Состояние портфеля
            "Как закрыть все позиции?" -> /ИТС_QUIK_Портфель_Закрыть позиции
            "Назад" -> /ИТС_QUIK

    state: ИТС_QUIK_Портфель_Нулевые позиции
        a: В таблице «Состояние счета» могут отображаться позиции с количеством «0».
            Данное отображение показывает, что категория инструментов доступна для торгов. В верхней панели таблицы можно выбрать пункт «Открытые», в данном режиме отобразится только текущий состав портфеля.

    state: ИТС_QUIK_Портфель_Позиции в портфеле
        a: Расчеты на бирже являются отложенными, и не совпадают с моментом сделки. Для отображения портфеля с учетом будущих расчетов необходимо переключить в таблице «Состояние счета» показатель «На дату» в положение T2.

    state: ИТС_QUIK_Портфель_Состояние портфеля
        a: Таблица «Состояние счета» предназначена для работы со счетами типа «Единая денежная позиция».
            Для отображения позиций и средств на срочном рынке в рамках договора с раздельными счетами нужно открыть таблицы в меню «Создать окно» → «Все типы окон» → «Ограничения по клиентским счетам» и «Позиции по клиентским счетам».

    state: ИТС_QUIK_Портфель_Закрыть позиции
        a: Для закрытия всех позиций в портфеле в верхней панели таблицы «Состояние счета» необходимо нажать кнопку «Закрыть все». В диалоговом окне необходимо выбрать тип заявки и подтвердить действие.

    state: ИТС_QUIK_Заявки
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Нет торгового кода в заявке" -> /Ошибки заявок_Другие_QUIK_Системные_Торговый код
            "Выставление заявок с графика" -> /ИТС_QUIK_Заявки_Выставление заявок
            "Настройка скальперского стакана" -> /ИТС_QUIK_Заявки_Настройка стакана
            "Назад" -> /ИТС_QUIK

    state: ИТС_QUIK_Заявки_Выставление заявок
        a: Ввод заявки из окна графика возможен несколькими способами:
            1. На графике необходимо навести курсор на тело свечи и нажать левую кнопку мыши, удерживая нажатой клавишу «Ctrl». 
            2. Необходимо включить режим ввода заявки из окна диаграммы, нажав кнопку «Поставить новую заявку» на панели инструментов «Графики» (отображена рука с двумя поднятыми пальцами). При этом окно ввода заявки открывается по нажатию левой кнопки мыши на теле свечи графика.
            3. Выбрать пункт «Новая заявка»/«Новая стоп-заявка» в контекстном меню на линии либо на легенде графика.

    state: ИТС_QUIK_Заявки_Настройка стакана
        a: Для активации «скальперского стакана» необходимо нажать правой кнопкой мыши по «Таблице котировок» и перейти в меню «Редактировать…».
            В редакторе необходимо включить пункт «Панель торговли». 
            Нажатием на кнопку «…» открывается окно «Настройки панели торговли». При использовании панели торговли в «Таблице котировок» становятся доступными дополнительные комбинации клавиш.

    state: ИТС_QUIK_График
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Добавление индикаторов" -> /ИТС_QUIK_График_Добавляение
            "Как отобразить заявки на графике?" -> /ИТС_QUIK_График_Заявки
            "Склейка графиков" -> /ИТС_QUIK_График_Склейка
            "Назад" -> /ИТС_QUIK

    state: ИТС_QUIK_График_Добавляение
        a: Для добавления индикатора из стандартного набора необходимо нажать правой кнопкой мыши по окну графика и выбрать «Добавить график (индикатор)…».
            Также, система позволяет загружать сторонние индикаторы совместимые с системой QUIK. Скачанный файл с индикатором необходимо поместить в папку «LuaIndicators» в корневом каталоге программы. 
            Далее необходимо повторно запустить программу QUIK.

    state: ИТС_QUIK_График_Заявки
        a: Для включения отображения необходимо нажать правой кнопкой мыши по окну графика и выбрать пункт «Редактировать …».
            В окне редактора необходимо слева нажать на наименование инструмента и перейти во вкладку «Дополнительно».
            По желанию можно активировать отображение заявок, сделок, стоп-заявок, а также настроить цвет линий.

    state: ИТС_QUIK_График_Склейка
        a: При замене текущего фьючерса на следующий система предлагает произвести автоматическую склейку графиков.
            ✅ Включить данную опцию можно в меню «Система» → «Настройки» → «Основные настройки» → «Программа» → «Замена инструментов». 
            ✅ Произвести склейку самостоятельно можно в меню «Система» → «Заказ данных» → «Склейка архивов графиков». 
            ✅ Если удалить файлы с расширением «.dat» по нужному инструменту из папки «archive» (находится в папке установки программы QUIK), то можно убрать склейку графиков.

    state: ИТС_Другие
        a: Выберите торговую систему:
        buttons:
            "QUIK X/WebQUIK" -> /ИТС_Другие_QUIK X
            "MetaTrader 4" -> /ИТС_Другие_MetaTrader 4
            "MetaTrader 5" -> /ИТС_Другие_MetaTrader 5
            "Стороннее ПО" -> /Стороннее ПО
            "Назад" -> /ИТС

    state: ИТС_Другие_QUIK X || sessionResultColor = "#CD4C2B"
        a: Подключить WebQUIK к брокерскому счету можно в личном кабинете: https://edox.finam.ru/Items/ItsEnablePaidService
            Торговая система платная — 420 ₽ в месяц. Пароль к терминалу вы получите в СМС при подключении к счету, логин — в личном кабинете: https://edox.finam.ru/Home/Account/Terminals?id= Найдите в открывшемся списке идентификатор терминала QUIK.  
            QUIK X доступен по ссылке: https://webquik6.finam.ru/  
            Здесь используются те же логин и пароль, что и в WebQUIK. После того, как вы подключите счет к терминалу, свяжитесь с менеджером «Финама». Он поможет активировать QUIK X.

    state: ИТС_Другие_MetaTrader 4
        a: Клиентский терминал MetaTrader 4 предназначен для проведения торговых операций и технического анализа в режиме реального времени при работе на рынке Forex. 
            ✅ [Ознакомиться с техническими характеристиками и скачать дистрибутив|https://finamfx.ru/solutions/platforms/]
    
    state: ИТС_Другие_MetaTrader 5 || sessionResultColor = "#CD4C2B"
        a: Торговая система MetaTrader 5 предназначена для установки на ПК с системой Windows.
            ✅ Скачать дистрибутив для установки MetaTrader 5 можно по ссылке: https://download.mql5.com/cdn/web/jsc.investment.company/mt5/finam5setup.exe 
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Подключить счет к MetaTrader 5" -> /ИТС_MetaTrader 5_Подключить счет
            "Авторизация в MetaTrader 5" -> /ИТС_MetaTrader 5_Авторизация
            "Не отобразились средства в MetaTrader 5" -> /ИТС_MetaTrader 5_Не отобразились средства
            "Сохранение настроек" -> /ИТС_MetaTrader 5_Сохранение настроек
            "Таблица Портфель" -> /ИТС_MetaTrader 5_Таблица Портфель
            "Особенности терминала" -> /ИТС_MetaTrader 5_Особенности
            "Назад" -> /ИТС_Другие

    state: ИТС_MetaTrader 5_Подключить счет
        a: Подключить счет к терминалу можно в личном кабинете: https://edox.finam.ru/ITS/AddTerminal
            Договора с раздельными брокерскими счетами недоступны для подключения к MetaTrader 5. 
            К одному идентификатору (логину) можно подключить только один брокерский счет.

    state: ИТС_MetaTrader 5_Авторизация
        a: Логин отображается в личном кабинете: https://edox.finam.ru/Home/Account/Terminals?id=  Идентификатор терминала является логином. Пароль вам придет в виде СМС после того, как вы подпишете заявление на получение новой ИТС: https://edox.finam.ru/ITS/AddTerminal
            Восстановить пароль можно в личном кабинете: https://edox.finam.ru/ITS/ChangeTerminalPassword

    state: ИТС_MetaTrader 5_Не отобразились средства
        a: Данная ошибка может возникнуть при первой авторизации в терминале. Обратитесь к менеджеру для принудительного обновления информации в торговой системе.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: ИТС_MetaTrader 5_Сохранение настроек
        a: Под панелью «Инструменты» отображается текущее название профиля. При нажатии левой кнопкой мыши по названию, появляется возможность сохранять и выбирать разные профили.

    state: ИТС_MetaTrader 5_Таблица Портфель
        a: «Портфель» MetaTrader 5 – приложение-сервис разработки компании «Финам», визуально схожее с таблицей «Портфель» в терминале TRANSAQ.
            ✅ Скачать «Портфель» и изучить инструкцию по установке можно по ссылке: https://www.finam.ru/Files/htt/metatrader/portfolio/MQL5.zip

    state: ИТС_MetaTrader 5_Особенности
        a: Если вы столкнулись с ошибкой в работе терминала, обратитесь к менеджеру «Финама». Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Балансовая цена" -> /ИТС_MetaTrader 5_Особенности_Цена
            "Отображение истории" -> /ИТС_MetaTrader 5_Особенности_История
            "Синхронизация с другими ИТС" -> /ИТС_MetaTrader 5_Особенности_Синхронизация
            "Срок действия стоп заявок" -> /ИТС_MetaTrader 5_Особенности_Стоп заявки
            "Склейка фьючерсов" -> /ИТС_MetaTrader 5_Особенности_Склейка фьючерсов
            "Назад" -> /ИТС_Другие_MetaTrader 5

    state: ИТС_MetaTrader 5_Особенности_Цена
        a: Трансляция балансовой цены может быть некорректной, ведется работа по данному функционалу.

    state: ИТС_MetaTrader 5_Особенности_История
        a: Раздел «История» находится в процессе доработки, может содержать некорректную информацию.

    state: ИТС_MetaTrader 5_Особенности_Синхронизация
        a: При исполнении заявок в других терминалах, позиции не отображаются в системе MetaTrader 5.
            При этом, все совершенные сделки в MetaTrader 5 будут отображены в других торговых терминалах.

    state: ИТС_MetaTrader 5_Особенности_Стоп заявки
        a: В торговой системе MetaTrader 5 можно выставлять ордера только со сроком «До конца дня».

    state: ИТС_MetaTrader 5_Особенности_Склейка фьючерсов
        a: Выполнить склейку фьючерсных контрактов можно при добавлении пользовательского индикатора в торговую систему. Ознакомится с инструкцией можно на сайте разработчиков MetaTrader 5 https://www.mql5.com/ru/articles/802

    state: Заявки || sessionResultColor = "#15952F"
        intent!: /062 Заявки
        a: ✅ Для торговли иностранными активами нужно получить статус квалифицированного инвестора, а также рекомендуется использовать [сегрегированный счёт|https://www.finam.ru/landing/segregated-account/]
            ❗ Неквалифицированным инвесторам доступно только закрытие позиций по иностранным активам через отдел голосового трейдинга, системы TRANSAQ и QUIK.
        a: Выберите один из предложенных вариантов:
        buttons:
            "Типы заявок" -> /Заявки_Типы
            "Выставление заявок" -> /Заявки_Выставление
            "Отмена заявки" -> /Заявки_Отмена
            "Как закрыть позицию" -> /Как закрыть позиции
            "Статус заявки" -> /Заявки_Статус
            "Ошибки при выставлении заявок" -> /Ошибки заявок

    state: Заявки_Типы
        a: ❗ По индикативным и недоступным для торговли инструментам данная опция заблокирована.
                ❗ Прежде чем подтвердить выставленную заявку, проверьте номер выбранного счета и наличие свободных средств на нем. Если у Вас возникла ошибка в процессе выставления заявки с активного брокерского счета и в рабочее время биржи, обратитесь к менеджеру «Финам». 
                В торговых системах доступно несколько видов заявок:
        buttons:
            "Рыночная заявка" -> /Заявки_Типы_Рыночная
            "Лимитная заявка" -> /Заявки_Типы_Лимитная
            "Условная заявка" -> /Заявки_Типы_Условная
            "Стоп-заявка/Тейк-профит" -> /Заявки_Типы_Стоп_Тейк
            "Связанные заявки" -> /Заявки_Типы_Связанные
            "Назад" -> /Заявки
    
    state: Заявки_Выставление
        a: ✅ Для выставления заявки через таблицу инструментов:
                1. выбрать инструмент,
                2. нажать кнопку «заявка» в FinamTrade либо в терминалах QUIK, MetaTrader 5, TRANSAQ нужно начать правой кнопкой мыши по инструменту и выбрать «новая заявка»/«новый ордер».
                ✅ Для выставление заявки с графика:
                1. в веб-версии FinamTrade, данная опция активируется в настройках терминала, в меню «торговля»,
                2. в других торговых системах выставить заявку можно нажатием правой кнопки мыши по графику инструмента.
                ❗ По индикативным и недоступным для торговли инструментам данная опция заблокирована.
                ❗ Прежде чем подтвердить выставленную заявку, проверьте номер выбранного счета и наличие свободных средств на нем. Если у вас возникла ошибка в процессе выставления заявки с активного брокерского счета и в рабочее время биржи, обратитесь к менеджеру «Финам».
        buttons:
            "Фондовый рынок МБ/СПБ" -> /Заявки_Выставление_Фондовый
            "Срочный рынок МБ FORTS" -> /Срочный рынок_Купить фьючерс
            "Валютный рынок МБ" -> /Валютный рынок_Как купить
            "NYSE/NASDAQ" -> /Заявки_Выставление_NYSE_NASDAQ
            "Гонконг (HKEX)" -> /Доступные биржи_Ещё_Гонконгская
            "Торговля заблокированными ЦБ" -> /Как закрыть позиции_Продажа БлокЦБ
            "Назад" -> /Заявки
        
    state: Заявки_Отмена
        a: Отмена/снятие заявки доступно на странице отображения портфеля в разделе Заявки. Необходимо нажать на интересующую заявку и выбрать действие. Исполненные/снятые/отмененные заявки редактирование не подлежат.

    state: Заявки_Статус
        a: Статус заявки можно проверить в торговом терминале.
            Заявки со статусами «исполнена», «отклонена» или «снята» не переносятся на следующую торговую сессию, информация по ним очищается в системе. 
            Условные и стоп-заявки до момента их исполнения хранятся на сервере торговой системы. Поэтому ордера, выставленные в одном терминале, не отображаются в другом до тех пор, пока не будут активированы.

    state: Заявки_Типы_Рыночная
        a: Рыночная заявка исполняется по принципу «лучшего исполнения», при покупке будет автоматически выбрана наименьшая доступная цена среди продавцов, при продаже – наибольшая доступная цена среди покупателей.
            ❗ В момент выставления рыночной заявки требуется больше обеспечения по сравнению с лимитными заявками. 
            Для фьючерсов при выставлении рыночной заявки блокируется 1,5 гарантийного обеспечения (ГО). 
            По некоторым инструментам данный вид заявок недоступен.
            ❗ Во время премаркета и постмаркета на рынке NYSE/NASDAQ недоступно выставление «рыночных» заявок, рекомендуем работать с «лимитными» ордерами (по определенной цене).

    state: Заявки_Типы_Лимитная
        a: Лимитная заявка исполняется по принципу «лучшего исполнения».
            Для покупки исполнение происходит по цене не выше указанной (меньше или равно), для продажи не ниже указанной (больше или равно).

    state: Заявки_Типы_Условная
        a: В системе реализованы следующие условия выставления заявки:
            ✅ «Время исполнения» (заявка будет активирована и отправлена на биржу в указанное время), 
            ✅ «Сделка выше или равна» (условие выставления заявки считается выполненным, если сервер получит данные о появлении на рынке хотя бы одной сделки по цене выше или равно заданной в условии, при выполнении указанного условия заявка будет выставлена на биржу по цене, заданной в поле «цена исполнения»), 
            ✅ «Сделка ниже или равна» (условие выставления заявки считается выполненным, если сервер получит данные о появлении на рынке хотя бы одной сделки по цене ниже или равно заданной в условии, при выполнении указанного условия заявка будет выставлена на биржу по цене заданной в поле «цена исполнения»).
            ✅ Срок действия заявки выбирается самостоятельно: до отмены, до конца дня, до указанной даты.

    state: Заявки_Типы_Стоп_Тейк
        a: Стоп-заявка содержит два либо одно из условий:
                ✅ Стоп-лосс (далее SL),
                ✅ Тейк-профит (далее TP).
                Стоп-заявка предполагает, что инвестор заранее выбирает условия, при которых заявка активируется - и выставится лимитная либо рыночная.
                Для закрытия коротких позиций следует выставлять стоп-заявки на покупку, для закрытия длинных позиций - на продажу. 
                ❗ Заявка может быть выставлена со сроком действия: до отмены, до конца дня, до указанной даты.
        buttons:
            "Стоп-лосс (SL)" -> /Заявки_Типы_Стоп_Тейк_SL
            "Тейк-профит (TP)" -> /Заявки_Типы_Стоп_Тейк_TP
            "Назад" -> /Заявки_Типы
    
    state: Заявки_Типы_Связанные
        a: В веб-терминале есть возможность с графика выставить лимитную заявку со связанными стоп-заявками (стоп-лоссом и тейк-профитом) на закрытие позиции. Возможность привязки заявки к уже существующей отсутствует.

    state: Заявки_Типы_Стоп_Тейк_SL || sessionResultColor = "#CD4C2B"
        a: SL на продажу активируется, когда цена на рынке станет меньше либо равна цене активации.
            SL на покупку активируется, когда цена на рынке станет больше либо равна цене активации.
            При выставлении SL необходимо задать «Цену активации» и «Цену заявки».
            При активации SL на биржу будет выставлена заявка по цене, заданной в поле «Цена исполнения».

    state: Заявки_Типы_Стоп_Тейк_TP
        a: TP на продажу активируется, когда цена на рынке станет больше либо равна цене активации.
            TP на покупку активируется, когда цена на рынке станет меньше либо равна цене активации. 
            1. Можно увеличить вероятность совершения сделки при исполнении стопа, задав «Защитный спред» либо использовав ✅ «Рыночная».
            Если указать «0» в поле «Защитный спред», то на Биржу будет отправлена заявка с ценой, равной цене первой же сделки на рынке, которая удовлетворяет цене активации.
            — Для определения цены заявки, исполняющей TP на покупку, защитный спред прибавляется к цене рынка.
            — Для определения цены заявки, исполняющей TP на продажу, защитный спред вычитается из цены рынка. 
            2. «Коррекция» используется для того, чтобы включить механизм отслеживания тренда, используется следующим образом: 
            — для TP на продажу считается, что растущий тренд заканчивается в тот момент, когда после того, как рынок вырос до уровня цены активации или выше, он снизится на величину коррекции от максимальной цены; 
            — для TP на покупку считается, что нисходящий тренд заканчивается в тот момент, когда после того, как рынок снизился до уровня цены активации или ниже, он вырастет на величину коррекции от минимальной цены.

    state: Заявки_Выставление_Фондовый
        a: ✅ С 01.01.2023 вступил в силу запрет Банка России на покупку инвесторами без статуса «квал» акций компаний из недружественных стран, доступно только закрытие позиций через отдел голосового трейдинга, системы QUIK и TRANSAQ.
            ✅ Торговля акциями дружественных стран, хранящихся в депозитариях недружественных юрисдикций, без статуса квалифицированного инвестора доступна через терминалы TRANSAQ и FinamTrade. 
            Дополнительно нужно:
            1. Подписать «Согласие на торговые операции с иностранными бумагами с местом хранения недруж. инфраструктура» по ссылке https://edox.finam.ru/ForeignSecurities/UnfriendlyDepoConsent 
            2. Пройти тестирование в личном кабинете «ИЦБ, требующие тестирования»: https://edox.finam.ru/Questionnaire/Questionnaires

    state: Заявки_Выставление_NYSE_NASDAQ
        a: Во время премаркета и постмаркета на рынке NYSE/NASDAQ недоступно выставление «рыночных» заявок, рекомендуем работать с «лимитными» ордерами (по определенной цене).
        
    state: Голосовой трейдинг || sessionResultColor = "#15952F"
        intent!: /063 Голосовой трейдинг
        a: Связаться с отделом голосового трейдинга можно по одному из номеров:
            +7 (495) 796-93-88 — доб. 2200
            +7 (495) 1-346-346 — доб. 2200
            *1945 — доб. 2200 (Бесплатно по РФ для МТС, Билайн, МегаФон и Tele2)
            ✅ Выставление заявок доступно в рабочие дни бирж с 6:50 до 00:00 МСК
            ❗ Чтобы выставить торговую заявку, подготовьте заранее и назовите трейдеру торговый код счета, а также кодовое слово (если вы задавали его ранее).
            Выберите, чтобы узнать подробнее:
        buttons:
            "Комиссии за сделки голосом" -> /Голосовой трейдинг_Комиссии
            "Торговый код" -> /Личный кабинет_Торговый код
            "Кодовое слово" -> /Личный кабинет_Торговый код_Кодовое слово
    
    state: Голосовой трейдинг_Комиссии
        a: Выберите торговую площадку:
        buttons:
            "На срочном рынке" -> /Голосовой трейдинг_Комиссии_Срочный
            "На фондовом рынке" -> /Голосовой трейдинг_Комиссии_Фондовый
            "На валютном рынке" -> /Голосовой трейдинг_Комиссии_Фондовый
            "Назад" -> /Голосовой трейдинг

    state: Голосовой трейдинг_Комиссии_Срочный
        a: Минимальный объем гарантийного обеспечения (ГО) для открытия новой позиции — от 10000 ₽. Закрытие позиции в полном объеме, стоимостью меньше 10000 ₽, доступно только по рыночной цене.
            По заявкам, принятым через отдел голосового трейдинга, удерживается комиссия от стоимости инструмента, а не фиксированная сумма:
            ✅ По фьючерсам:
            0,0354 % от суммы ПФИ (начиная с первого поручения) + 236 ₽ за каждое последующее поручение, начиная с шестого по соответствующему клиентскому счету за день.
            (Исключение: ТП «Консультационный Фортс»: 0,03611% от суммы ПФИ, начиная с первого поручения + 236 ₽ за каждое последующее поручение, начиная с шестого по соответствующему клиентскому счету за день).
            ✅ По опционам:
            2 ₽ за каждый ПФИ (начиная с первого поручения) + 236 ₽ за каждое последующее поручение, начиная с шестого по соответствующему клиентскому счету за день.

    state: Голосовой трейдинг_Комиссии_Фондовый
        a: Минимальный объем заявки на открытие позиции составляет от 10000 ₽. Закрытие позиции в полном объеме, стоимостью меньше 10000 ₽, доступно только по рыночной цене.
            ✅ Начиная с 6-й заявки в день, по каждому из счетов удерживается дополнительная комиссия 236 ₽ за заявку.
            

    state: Как закрыть позиции || sessionResultColor = "#15952F"
        intent!: /064 Как закрыть позиции
        a: В любой торговой системе вы можете закрыть позицию, перейдя в раздел «Портфель». Например, в терминале FinamTrade для этого достаточно нажать на строку с нужным активом, а в TRANSAQ и QUIK – нажать правой кнопкой мыши по позиции в портфеле и выбрать действие.
            ✅ Также закрыть позицию можно с помощью новой заявки, купленные инструменты нужно продать, проданные (шорт позиции) - откупить.
            ✅ Прежде чем закрыть позицию, убедитесь, что она активна и проверьте наличие выставленных ордеров на ее закрытие. 
            ✅ Если в рабочее время биржи у вас не получается закрыть позицию, обратитесь за помощью к менеджеру «Финам».
            ❗ С 01.01.2023 вступил в силу запрет Банка России на покупку инвесторами без статуса «квал» акций компаний из недружественных стран, доступно только закрытие позиций через отдел голосового трейдинга, системы QUIK и TRANSAQ.  
            ❗ С 26.04.23 «Финам» запустил сервис по продаже и покупке иностранных ценных бумаг, ранее заблокированных европейскими депозитариями Euroclear и Clearstream.
            Выберите, чтобы узнать подробнее:
        buttons:
            "Неполный лот" -> /Как закрыть позиции_Неполный лот
            "Продать валюту" -> /Валютный рынок
            "Выкуп ИЦБ по указу № 844" -> /Ограничение ЦБ_844
            "Продажа заблокированных ЦБ" -> /Как закрыть позиции_Продажа БлокЦБ
            "Закрыть задолженность" -> /Как закрыть позиции_Закрыть задолженность
            "Как выставить заявку" -> /Заявки

    state: Как закрыть позиции_Неполный лот
        a: 1. Продать неполный лот ценных бумаг вы можете через отдел голосового трейдинга. (С мобильного: тел. *1945 — доб. 2200)
            2. Также режим торгов в неполном лоте доступен в торговых системах:
            ✅ TRANSAQ — в форме ввода заявки нужно сменить режим на «неполные лоты»,
            ✅ QUIK — в профиле поиска инструментов нужно выбрать инструмент с припиской неполный лот.
            3. Неполные лоты валют доступны в виде контрактов _TMS (торгуются кратно 0,01 ед. валюты, минимальная заявка от 1 ед. валюты, расчеты на следующий рабочий день после 23:50 МСК).

    state: Как закрыть позиции_Продажа БлокЦБ || sessionResultColor = "#B65A1E"
        a: «Финам» предоставляет сервис по продаже и покупке на Московской и СПБ Биржах* иностранных ценных бумаг, ранее заблокированных европейскими депозитариями Euroclear и Clearstream.
            *- торги на СПБ Бирже временно приостановлены.
            ✅ В рамках сервиса заблокированные ИЦБ представляют собой торговый инструмент с тикером, состоящим из оригинального торгового кода бумаги и постфикса «SPBZ» либо «MMBZ».
            ✅ Торги доступны в дни работы бирж 11:00–17:00 МСК через ИТС TRANSAQ и FinamTrade.
            ✅ В терминале FinamTrade список доступных инструментов находится в левом вертикальном меню в разделе «рынки», в подборках «Заблокированные инструменты».
            ✅ Все поручения на сделки являются неторговыми и проводятся исключительно между клиентами «Финам»
            ✅ Валюта расчетов – рубли РФ
            ✅ Комиссия за сделку — 0,8%
            ❗ Недоступно для ИИС
            ❗ Предварительно перед совершением сделок нужно [подписать «Согласие на торговые операции с заблокированными ИЦБ»|https://edox.finam.ru/ForeignSecurities/BlockedSecuritiesConsent]
            ❗ Для покупки заблокированных ЦБ нужен статус квалифицированного инвестора, для продажи - не требуется.
            ✅ [Подробнее об услуге, инструкции и список бумаг|https://www.finam.ru/landings/blocked-securities/?key=2e6d8aef-4940-4105-aee4-d0b892894664]

    state: Как закрыть позиции_Закрыть задолженность || sessionResultColor = "#B65A1E"
        a: ✅ Если у вас в портфеле появилась графа «Обязательства», это говорит о том, что у вас возникла маржинальная позиция или задолженность перед брокером.
            ✅ Погасить задолженность на брокерском счете можно следующими способами:
            1. внести денежные средства на брокерский счет в количестве не менее суммы задолженности;
            2. перевести денежные средства с одного брокерского счета на другой;
            3. изменить структуру портфеля, например, закрыв часть позиций;
            4. если у вас на счете есть валютная задолженность, то вы можете приобрести соответствующую валюту.
        buttons:
            "Пополнение счета" -> /Движение ДС_Пополнение
            "Перевод между счетами в «Финам»" -> /Движение ДС_Перевод
            "Как купить/продать валюту" -> /Валютный рынок_Как купить
            "Маржинальная торговля" -> /Маржа
            "Назад" -> /Как закрыть позиции

    state: Ошибки заявок || sessionResultColor = "#15952F"
        intent!: /065 Ошибки заявок
        a: ✅ Популярные типы ошибок при выставлении заявок:
            1. «Не пройдено тестирование»
            Вы не прошли тестирование «Производные финансовые инструменты».
            2. «Не подтвержден квалификационный уровень»
            Для подтверждения уровня достаточно пройти тестирование по категории «Необеспеченные сделки»
            3. «Вам запрещены сделки с инструментами»
            Данная ошибка может возникать в том случае, если у вас отсутствует квалификационный уровень для работы с данным инструментом.
            Решение:
            — Тестирование для неквалифицированных инвесторов по ряду категорий можно пройти в личном кабинете: https://lk.finam.ru/user/invest-status/qual-exam/tests 
            — Получить статус квалифицированного инвестора.
            ✅ Другие ошибки и способы их решения при выставлении заявок:
        buttons:
            "Нет кнопки Заявка/Замок" -> /Ошибки заявок_Нет кнопки
            "Доступное количество в портфеле "0" -> /Ошибки заявок_Доступное кол-во
            "Дежурный режим" -> /Ошибки заявок_Дежурный режим
            "Сделки по данному инструменту запрещены" -> /Ошибки заявок_Сделки по данному
            "Нехватка средств/Недостаточно обеспечения" -> /Ошибки заявок_Нехватка
            "Цена сделки вне лимита" -> /Ошибки заявок_Вне лимита
            "Запрет трейдера на открытие позиций" -> /Ошибки заявок_Запрет трейдера
            "Запрет приобретения на ИИС" -> /Ошибки заявок_Запрет на ИИС
            "Другие ошибки" -> /Ошибки заявок_Другие
            

    state: Ошибки заявок_Нет кнопки || sessionResultColor = "#CD4C2B"
        a: По недоступным для торговли инструментам кнопка «Заявка» заблокирована, в терминале FinamTrade такие инструменты отмечены символом «Замок».
            ✅ В торговых системах есть как торговые так и индикативные инструменты, индикативные не торгуются (индексы, криптовалюты, сырье), а несут информационный характер.
            ✅ Торги могут быть заблокированы на период корпоративных событий, или по инициативе вышестоящих организаций. 
            ✅ По счету может быть установлен запрет электронных торгов (часто встречается в день открытия счета, доступ появится на следующий торговый день).
            ❗ Обращаем Ваше внимание, активация счета происходит после пополнения от 150 рулей. 
            ✅ Фиолетовый символ  «Замок» в терминале FinamTrade говорит о том, что инструмент доступен со статусом квалифицированного инвестора.

    state: Ошибки заявок_Доступное кол-во
        a: При продаже уже рассчитанных и поставленных валютных пар/металлов с помощью контрактов TOM/TMS количество в портфеле может отображаться «0», при этом выставление заявки будет доступно.
            Поставленные валюты и металлы отображаются как контракты TOD/TMS. Проверить доступное количество для продажи можно в портфеле. 
            При выставлении заявки на сумму, не превышающую остаток валюты/металлов в портфеле, короткая позиция не возникнет. 
            Для продажи валюты (доллар США, евро, юань) в количестве менее 1000 ед. используйте инструменты с окончанием TMS.

    state: Ошибки заявок_Дежурный режим || sessionResultColor = "#CD4C2B"
        a: ✅ Торги на разных торговых площадках проводятся в разный период времени. В выходные и праздничные дни торги не проводятся, либо осуществляются в ограниченном формате. Рекомендуем ознакомится детальнее.
                При выставлении «лимитных» и «рыночных» заявок в неторговое время возникает сообщение «Торговые операции недоступны в дежурном режиме». В данный период доступно выставление только условных заявок и стоп ордеров (стоп-лосса и тейк-профита). 
                ✅ В рамках учебных счетов в неторговый период выставление всех типов заявок недоступно. Сервера учебных счетов начинают работать с 10:00 по МСК. В выходные и праздничные дни торги не проводятся.
        buttons:
            "Время торгов на биржах" -> /Время торгов

    state: Ошибки заявок_Сделки по данному
        a: Данная ошибка может возникать в том случае, если заявка выставляется со счета нерезидента РФ.
                ✅ На текущий момент открытие позиций нерезидентам РФ доступно только на валютном рынке московской биржи.
                ✅ Резидентам дружественных стран дополнительно доступны сделки на фондовом рынке московской биржи.
                ❗ Проверка производится со стороны биржи. 
                Подробнее об особенностях выставления заявок на разных рынках:
        buttons:
            "Доступные биржи" -> /Доступные биржи

    state: Ошибки заявок_Нехватка
        a: Данная ошибка может возникать в том случае, если у вас недостаточно средств для выставления заявки.
                ❗ Сумма, свободная для открытия позиций, отображается в портфеле в виде денежных остатков.
                ✅ Обеспечение для открытия позиций с займом («плечом») рассчитывается как разница оценки портфеля и заблокированной начальной маржи по счету (начальных требований).
                ✅ При выставлении «рыночной заявки» размер обеспечения выше стандартного (на срочном рынке обеспечение увеличивается в 1,5 раза). Рекомендуем использовать «лимитные» заявки (по определенной цене).
                ✅ Обязательно убедитесь, что у вас нет лишних активных заявок по каким-либо инструментам. Под активные ордера блокируется обеспечение (маржа), что может помешать выставлению заявки.
                ✅ По счетам типа «Единая денежная позиция» со стандартным уровнем риска (КСУР) на срочном рынке может блокироваться ГО в 1,5—2 раза выше биржевого. Для снижения ГО можно отключить фондовый и валютный рынки по счету через обращение к менеджеру, а также воспользоваться услугой «Пониженное ГО».
                Подробнее:
        buttons:
            "Гарантийное обеспечение" -> /Срочный рынок_Обеспечение
            "Маржинальная торговля" -> /Маржа

    state: Ошибки заявок_Вне лимита
        a: По каждому инструменту существует свой диапазон выставления заявок.
            ✅ По инструментам срочного рынка в спецификации контракта на бирже указываются значения верхнего и нижнего лимита на момент последнего клиринга. 
            ✅ По ценным бумагам данный диапазон составляет от 5% до 15% от текущей стоимости инструмента. 
            ❗ Биржа может ограничивать диапазон выставления заявок на свое усмотрение в случае резкого изменения цены инструмента.

    state: Ошибки заявок_Запрет трейдера || sessionResultColor = "#CD4C2B"
        a: Открытие позиций в поставочных фьючерсах в последний день обращения (за день до экспирации и поставки) недоступно. Используйте следующие контакты.
        
    state: Ошибки заявок_Запрет на ИИС || sessionResultColor = "#CD4C2B"
        a: На договора ИИС недоступно приобретение иностранных ценных бумаг и бумаг с эмитентами, зарегистрированными за пределами РФ. Для закрытия позиций можно использовать рыночные или лимитные заявки. Использование условных и стоп-заявок недоступно.
        go!: /ИИС

    state: Ошибки заявок_Другие
        a: Другие ошибки и способы их решения:
        buttons:
            "Недопустимое значение для данного инструмента" -> /Ошибки заявок_Другие_Недопустимое значение
            "Не найден доступный маршрут" -> /Ошибки заявок_Другие_Маркетные
            "Маркетные заявки в условных поручениях не разрешены" -> /Ошибки заявок_Другие_Маркетные
            "Данная ценная бумага не допущена к заключению сделок" -> /Ошибки заявок_Сделки по данному
            "HALT_INSTRUMENT" -> /Ошибки заявок_Другие_HALT_UNSTRUMENT
            "BAD_CLIENTID/Попытка операции на несуществующий код клиента" -> /Ошибки заявок_Другие_BAD_CLIENTID
            "Типовые ошибки в Рабочем месте QUIK" -> /Ошибки заявок_Другие_QUIK
            "Назад" -> /Ошибки заявок

    state: Ошибки заявок_Другие_Недопустимое значение
        a: Данная ошибка может возникать в том случае, если в форме заявки указано некорректное значение.
            ✅ Цена заявки должна соответствовать шагу цены по инструменту, информация указана в описании инструмента. 
            ✅ Обращайте внимание на разрядность цены.

    state: Ошибки заявок_Другие_Маркетные
        a: Данная ошибка может возникать в том случае, если нарушены условия выставления заявки.
            ✅ Необходимо соблюдать минимальный объем заявок, при торговле на бирже HKEX (Гонконг) минимальный объем заявки — 8000 HKD.
            ✅ Во время премаркета и постмаркета на рынке NYSE/NASDAQ недоступно выставление «рыночных» заявок, рекомендуем работать с «лимитными» ордерами (по определенной цене). 
            Для исключения ошибок при срабатывания отложенных ордеров на рынке NYSE/NASDAQ запрещено выставление условных заявок, тейк-профита и стоп-лосса с «рыночным» исполнением. Цену исполнения и защитный спред необходимо указать самостоятельно (как «лимитный» ордер). 
            Подробнее об особенностях выставления заявок на разных рынках:
        buttons:
            "Выставление заявок" -> /Заявки_Выставление

    state: Ошибки заявок_Другие_HALT_UNSTRUMENT
        a: Данная ошибка может возникать в том случае, если по торговому инструменту приостановлены/заблокированы/прекращены торги.

    state: Ошибки заявок_Другие_BAD_CLIENTID
        a: Данная ошибка может возникать в том случае, если счет с данным торговым кодом не зарегистрирован.
            Регистрация торгового кода возможна до трех рабочих дней с момента отправки заявки. 
            Уточнить информацию по конкретному счету можно у менеджера «Финам».

    state: Ошибки заявок_Другие_QUIK
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Ошибки при авторизации в QUIK" -> /Ошибки заявок_Другие_QUIK_Авторизация
            "Системные ошибки в QUIK" -> /Ошибки заявок_Другие_QUIK_Системные
            "Назад" -> /Ошибки заявок_Другие

    state: Ошибки заявок_Другие_QUIK_Авторизация
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Error 2 reading file" -> /Ошибки заявок_Другие_QUIK_Авторизация_Error2
            "Ключ сервера или пользователя не найден" -> /Ошибки заявок_Другие_QUIK_Авторизация_Ключ сервера
            "Вы используете ключи, не зарегистрированные на сервере" -> /Ошибки заявок_Другие_QUIK_Авторизация_РегКлючей
            "Вы уже работаете в системе" -> /Ошибки заявок_Другие_QUIK_Авторизация_Вы уже работаете
            "Назад" -> /Ошибки заявок_Другие_QUIK

    state: Ошибки заявок_Другие_QUIK_Системные
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Не обновляется программа QUIK" -> /Ошибки заявок_Другие_QUIK_Системные_Обновление
            "Не хватило памяти под объекты" -> /Ошибки заявок_Другие_QUIK_Системные_Память
            "General protection fault" -> /Ошибки заявок_Другие_QUIK_Системные_GeneralProtection
            "Нет данных в таблицах Сделки/Заявки" -> /Ошибки заявок_Другие_QUIK_Системные_ДанныеСделки
            "Не обновляются данные на графиках/в таблицах" -> /Ошибки заявок_Другие_QUIK_Системные_Данные график
            "Нет торгового кода в заявке" -> /Ошибки заявок_Другие_QUIK_Системные_Торговый код
            "Назад" -> /Ошибки заявок_Другие_QUIK

    state: Ошибки заявок_Другие_QUIK_Авторизация_Error2
        a: Данная ошибка означает, что при соединении с сервером, программа QUIK не может найти файлы с публичной и/или секретной частью ключей.
            Необходимо выполнить следующее:
            1. Открываем пункт меню «Система» → «Настройки» → «Основные настройки» → «Основные» → «Программа» → «Шифрование» и нажимаем на кнопку в поле «Настройки по умолчанию».
            2. В появившейся форме «Текущие настройки» в полях «Файл с публичными ключами» и «Файл с секретными ключами» при нажатии на кнопки вида […] нужно указать местоположение публичного ключа «pubring.txk» и секретного «secring.txk» ключа соответственно.

    state: Ошибки заявок_Другие_QUIK_Авторизация_Ключ сервера
        a: Данная ошибка может возникать в том случае, когда пользователь совершает ошибку при наборе своего логина.
            В поле «Введите Ваше Имя» можно ввести только один первый символ, а не весь логин полностью, учитывайте раскладку клавиатуры (английская или русская). Например, если логин «Иванов», то можно ввести только букву «И» или, если логин «2081263954» — только «2». 
            Пароль нужно вводить полностью. Также нужно обратить особое внимание, что верхний и нижний регистр (большие и маленькие буквы) программой идентифицируются как разные символы.

    state: Ошибки заявок_Другие_QUIK_Авторизация_РегКлючей
        a: Данное сообщение об ошибке может возникать если пользователь пытается установить соединение с сервером QUIK с ключами, незарегистрированными на сервере.
            После подписания заявления на регистрацию публичной части ключа «pubring.txk» необходимо ожидать не менее часа. Регистрация новой пары ключей к одному идентификатору QUIK деактивирует предыдущую пару ключей.

    state: Ошибки заявок_Другие_QUIK_Авторизация_Вы уже работаете
        a: Сервер QUIK не допускает одновременную работу двух пользователей с одинаковыми ключами доступа.
            Для одновременной работы с одной парой ключей можно выбирать подключение к разным серверам (MAIN1 и т.д.). Если такое сообщение получено при восстановлении соединения после обрыва, то достаточно повторить попытку через несколько секунд, когда сервер QUIK прекратит обработку предыдущего соединения.

    state: Ошибки заявок_Другие_QUIK_Системные_Обновление
        a: После автоматического обновления программы QUIK или выводе на экран сообщения после соединения с сервером «На сервере появилась новая версия программы…», принятии файлов и перезапуска программы, версия программы QUIK не изменилась.
            Данная проблема актуальна для 32-х разрядных операционных систем Windows. Для корректного обновления программы QUIK необходимо установить 64-х разрядную систему Windows. 
            Возможна ситуация, когда произошло некорректное обновление версии программы, после которого программа не запускается. 
            В данном случае нужно восстановить предыдущее состояние программы. Для этого в рабочей директории программы QUIK нужно найти папку «backup». В данной папке расположены подпапки с именами формата: «DDMMYYYY», где «DD» — число, «MM» — месяц, а «YYYY» — год даты последнего успешного обновления программы. 
            Выберите папку с датой последнего обновления и скопируйте из нее все файлы в рабочую директорию QUIK с заменой текущих файлов. После чего программу нужно запустить от имени администратора, и выполнить обновление.

    state: Ошибки заявок_Другие_QUIK_Системные_Память
        a: Причиной данной ошибки может являться недостаток ресурсов компьютера и/или программный сбой. Первым делом нужно проверить потребление оперативной памяти и загрузку ЦП в диспетчере задач Windows.
            Для очистки временных файлов в системе QUIK рекомендуется выполнить следующие действия:
            1. Закрыть программу QUIK, если она при этом открыта.
            2. В директории с программой удалить все файлы с расширением «*.log» и «*.dat» (кроме файлов с расширением «*.dat», в которых хранятся настройки внешних систем технического анализа, если такие подключены). 
            3. Запустить программу QUIK.
            Если вышеприведенные рекомендации не помогут, то это означает, что файл с настройками (по умолчанию, «finam.wnd») поврежден. 
            В данном случае нужно удалить файл с настройками, запустить программу без файла, и создать настройки заново (если сохранялись резервные копии настроек, то возможен запуск настроек с предыдущего сохранения из папки «WNDSAV»).

    state: Ошибки заявок_Другие_QUIK_Системные_GeneralProtection
        a: При запуске/работе с программой QUIK выводится сообщение вида — «General protection fault. Internal exception happened. Please send info.rpt to support@quik.ru Sorry for inconvenience».
            Данное сообщение означает, что произошел программный сбой, и программа была завершена аварийно. 
            В большинстве случаев работоспособность программы можно восстановить путем удаления из директории с программой всех файлов с расширением «*.log» и «*.dat». 
            Если вышеприведенные рекомендации не помогут, то это означает, что файлы повреждены. 
            В данном случае нужно повторно установить программу (перед удалением можно сохранить файл с настройками «*.wnd» и ключи («pubring.txk», «secring.txk») в отдельную папку).

    state: Ошибки заявок_Другие_QUIK_Системные_ДанныеСделки
        a: Если при работе с заявками в таблицах Заявки/Сделки/Стоп-Заявки не появляются данные, необходимо нажать правой кнопкой мыши по таблице и выбрать пункт «Редактировать». В окне редактора необходимо снять «лишние» фильтры. Также, если постоянно ведется работа с разными счетами, можно устанавливать нужный номер счета в общем фильтре клиентов (устанавливается в верхней панели инструментов программы QUIK).

    state: Ошибки заявок_Другие_QUIK_Системные_Данные график
        a: Если происходил кратковременный разрыв с сервером (потеря интернет связи) возможно отставание данных на графиках и в таблицах.
            В данном случае нужно зайти в меню «Система» → «Заказ данных» → «Перезаказать данные», рекомендуется указывать все пункты для перезаказа данных.

    state: Ошибки заявок_Другие_QUIK_Системные_Торговый код
        a: Если на форме ввода заявки не отображается торговый счет (список выбора торгового счета пустой), необходимо выполнить следующее: открыть пункт меню «Система» → «Настройки» → «Основные настройки» → «Торговля» → «Настройка счетов» и переместить (кнопка «Добавить все») все счета из поля «Доступные» в поле «Выбранные».
            Если в поле «Доступные» не отображается ни один торговый счет, то это означает, что по данному коду клиента не задан ни один лимит по бумагам (возможно счет пустой).

    state: Некорректное отображение || sessionResultColor = "#15952F"
        intent!: /066 Некорректное отображение
        if: technicalBreak()
            a: ✅ Баланс счета не отображается ночью в будние дни, во время технических перерывов, связанных с обслуживанием серверов торговых систем:: 
                — QUIK: с 3:00 до 6:40 МСК, 
                — TRANSAQ и FinamTrade: с 5:00 до 6:40 МСК. 
                В выходные дни дополнительные технические работы могут проводится в дневное время, так как торги не проводятся.
                ✅ В выходные и праздничные дни торги не проводятся, либо осуществляются в ограниченном формате.
                ✅ В рамках учебных счетов в неторговый период выставление всех типов заявок недоступно. Сервера учебных счетов начинают работать с 10:00 по МСК. В выходные и праздничные дни торги не проводятся.
        
        a: Для устранения проблем, связанных с отображением информации, необходимо выполнить следующие действия:
            1. Личный кабинет и FinamTrade (web):
            — проверить скорость интернета,
            — проверить работу в разных браузерах (Chrome, Firefox, Yandex), 
            — очистить cache и cookies файлы (могут быть утеряны персональные настройки),
            — отключить все плагины и расширения на устройстве, vpn и антивирус.
            2. FinamTrade (android/ios):
            — проверить скорость интернета,
            — отключить vpn,
            — произвести повторную авторизацию,
            — переустановить приложение (могут быть утеряны персональные настройки).
            3. QUIK: 
            — проверить скорость интернета,
            — при авторизации подключиться к другому серверу QUIK (main1/main2),
            — перезаказать данные в разделе Система - Заказ данных - Перезаказать данные (рекомендуется предварительно сохранить настройки в файл через: Система- Сохранить настройки в файл),
            — переоткрыть необходимую таблицу/график,
            — активировать в настройках клиентского места пункт «получать пропущенные данные».
            4. TRANSAQ:
            — проверить скорость интернета,
            — при авторизации подключиться к резервному серверу,
            — обновить TRANSAQ до последней версии (Файл - Произвести обновление программы),
            — переустановить TRANSAQ с сайта: https://www.finam.ru/howtotrade/soft/transaq/ 
            Если произведенные действия не привели к решению проблемы, просьба обратиться к менеджеру технической поддержки.

    state: Сайт || sessionResultColor = "#15952F"
        intent!: /067 Сайт
        a: Для устранения проблем на ресурсах компании (сайте, портале и т.д.), связанных с отображением информации, необходимо выполнить следующие действия:
            — проверить скорость интернета,
            — проверить работу в разных браузерах (Chrome, Firefox, Yandex), 
            — закрыть все вкладки кроме FinamTrade, 
            — очистить cache и cookies файлы (могут быть утеряны персональные настройки),
            — отключить все плагины и расширения на устройстве, vpn и антивирус.
            Если произведенные действия не привели к решению проблемы, просьба обратиться к менеджеру технической поддержки.

    state: Актуальный портфель || sessionResultColor = "#15952F"
        intent!: /068 Актуальный портфель
        if: technicalBreak()
            a: ✅ Баланс счета не отображается ночью в будние дни, во время технических перерывов, связанных с обслуживанием серверов торговых систем:: 
                — QUIK: с 3:00 до 6:40 МСК, 
                — TRANSAQ и FinamTrade: с 5:00 до 6:40 МСК. 
                В выходные дни дополнительные технические работы могут проводится в дневное время, так как торги не проводятся.
                ✅ В выходные и праздничные дни торги не проводятся, либо осуществляются в ограниченном формате.
                ✅ В рамках учебных счетов в неторговый период выставление всех типов заявок недоступно. Сервера учебных счетов начинают работать с 10:00 по МСК. В выходные и праздничные дни торги не проводятся.

        a: На СПБ Бирже приостановлены торги иностранными ценными бумагами. Актуальная информация размещается на [официальном сайте биржи|https://spbexchange.ru/ru/about/news2.aspx].
            ✅ В рамках Указа № 665, депозитарии используют рубли РФ для выплат по заблокированным активам. Выплаты первой очереди, поступившие брокеру от СПБ Биржи, перечислены инвесторам. В настоящий момент информации о предстоящих выплатах от биржи не поступало.
            ❗ После перевода СПБ Биржей бумаг на неторговый раздел, бумаги исключены из торговых лимитов биржи, и не отображаются в терминале, но их наличие отражено во вкладке «Портфель» в [личном кабинете|https://lk.finam.ru/].
        a: ✅ Актуальная стоимость портфеля с учетом текущих биржевых цен отображается:
            1. во вкладке «портфель» в личном кабинете https://lk.finam.ru/ 
            2. в мобильном приложении (нажать «три полоски» в верхнем левом углу экрана и выбрать нужный счет в отрывшемся меню).
            ✅ Оценка портфеля включает в себя стоимость открытых позиций и свободный остаток денежных средств. 
            ❗Вариационная маржа по срочному рынку также учитывается в общей оценке портфеля, но фактическое зачисление/списание средств происходит только после 18:50 МСК.
            ✅ При пополнении счетов нужно учитывать сроки зачисления средств в зависимости от способа пополнения. В выходные дни сроки зачисления могут быть увеличены. В торговых системах баланс обновляется в рабочее время бирж.
            ✅ Если вы подали поручение на вывод средств с брокерского счета, то средства под вывод в личном кабинете отображаются как «заблокировано (под вывод)».
            ✅ Ценная бумага может отображаться заблокированной в портфеле по причине проходящего по ней корпоративного действия (отражается в справке по счету) либо по причине ограничений вышестоящих депозитариев.
            ✅ Оценка портфеля уменьшается на сумму купона или дохода от погашения облигации за день до выплаты.
            ✅ При переводе бумаг от другого брокера (из реестра) в торговой системе может отображаться другая балансовая (средняя) стоимость позиции.
            ❗ Личный кабинет и торговые терминалы проводят оценку счета на основе разных источников данных, данные могут отличаться в период перезапуска торговых серверов.
        buttons:
            "Баланс в торговых системах" -> /Актуальный портфель_Баланс
            "Справка по счету" -> /Справка по счету
            "Ограничения" -> /Ограничение ЦБ
            "Изменить балансовую (среднюю) цену" -> /Балансовая средняя

    state: Актуальный портфель_Баланс
        a: ✅ Наиболее распространенной причиной отсутствия счета в терминале является отсутствие подключения к выбранному идентификатору (логину).
            Подключите счет к терминалу в личном кабинете: https://edox.finam.ru/ITS/AddTerminalAccount 
            ✅ В день открытия брокерского договора новые счета не отображаются в системе QUIK.
            ✅ На текущий момент распространенной проблемой является блокировка иностранных ценных бумаг со стороны вышестоящих организаций, данные бумаги не отображаются в торговых системах по причине хранения на неторговом разделе счета ДЕПО. Необходимо ожидать снятия ограничений. 
            ✅ Активация счета происходит после пополнения от 99 ₽.
            ✅ Технические перерывы в будние дни, связанные с обслуживанием серверов торговых систем (в эти периоды баланс счета не отображается): 
            — QUIK: с 3:00 до 6:40 МСК, 
            — TRANSAQ и FinamTrade: с 5:00 до 6:40 МСК. 
            В выходные дни дополнительные технические работы могут проводится в дневное время, так как торги не проводятся.

    state: Минималка || sessionResultColor = "#15952F"
        intent!: /069 Минималка
        a: Чтобы активировать счет в любой торговой системе, внесите на него минимальную сумму — 99 ₽.

    state: Балансовая средняя || sessionResultColor = "#15952F"
        intent!: /070 Балансовая средняя
        a: Балансовая стоимость рассчитывается как среднее арифметическое всех открытых позиций.
            ✅ В балансовой стоимости также учитывается накопленный купонный доход по облигациям, уплаченный покупателем продавцу. 
            ✅ С 07.08.2023 в качестве средней цены приобретения на срочном рынке используется фактическая средняя цена открытия позиции (ранее использовалась цена последнего клиринга). Для корректного отображения, нужно обновить торговый терминал. 
            ✅ Если ценные бумаги переведены от другого брокера, балансовая цена в валюте может отличаться от цены приобретения, так как в торговой системе отображается курс на дату ввода бумаг (в целях налогообложения будет учтен курс покупки, проверить информацию можно в справке по счету). 
            При необходимости, для вашего удобства, цена может быть скорректирована. Обратитесь к менеджеру «Финам».
            ✅ По заблокированным ценным бумагам балансовая стоимость будет скорректирована только после снятия ограничений.

    state: Как начать || sessionResultColor = "#15952F"
        intent!: /071 Как начать
        a: Инвестировать — просто! 
            ✅ Для начала рекомендуем:
            1. открыть и пополнить брокерский счет
            2. ознакомиться с принципами выставления заявок в торговых системах
            ❗ Для торговли иностранными активами нужно получить статус квалифицированного инвестора, а также рекомендуется использовать [сегрегированный счет|https://www.finam.ru/landing/segregated-account/]
            ✅ Чтобы усовершенствовать навыки торговли вы можете:
            1. открыть демо-счет
            2. пройти обучение в учебном центре «Финам» или ознакомиться с обучающими видеоматериалами по торговым системам
            ✅ Воспользуйтесь готовыми инвестиционными решениями от профессионалов «Финам».
        a: Выберите один из предложенных вариантов:
        buttons:
            "Открыть брокерский счет" -> /Открытие_счета
            "Стать квалифицированным инвестором" -> /КВАЛ
            "Как выставить заявку" -> /Заявки
            "Обучение от Финам" -> /Обучение на сайте
            "Открыть демо-счет" -> /Демо-счет
            "Инвестиционные услуги" -> /Услуги компании

    state: Учебные видео || sessionResultColor = "#15952F"
        intent!: /072 Учебные видео
        a: Предлагаем посмотреть обучающие видеокурсы по работе торговых терминалов:
        buttons:
            "FinamTrade" -> /Учебные видео_FinamTrade
            "TRANSAQ" -> /Учебные видео_TRANSAQ
            "QUIK" -> /Учебные видео_QUIK

    state: Учебные видео_FinamTrade
        a: 1. Для просмотра цикла обучающих видео выберите нужный формат торгового терминала и перейдите по ссылке:
            ✅ [Веб-версия терминала|https://education.finam.ru/lk/course/kak-nacat-polzovatsia-finamtrade/]
            ✅ [Мобильная версия|https://education.finam.ru/lk/course/mobilnoe-prilozenie-finamtrade/]
            2. Также вы можете посмотреть серию видеоуроков на [Youtube-канале «Финам Инвестиции»|https://www.youtube.com/watch?v=OgIQQJr92F8&t=8s], в которых подробно рассказывается, как пользоваться торговой платформой FinamTrade.

    state: Учебные видео_TRANSAQ
        a: Смотрите бесплатный обучающий курс [«Как настроить торговый терминал TRANSAQ»|https://education.finam.ru/lk/course/kak-nastroit-torgovyi-terminal-transaq/]

    state: Учебные видео_QUIK
        a: Смотрите бесплатный обучающий курс [«Как настроить торговый терминал QUIK»|https://education.finam.ru/lk/course/kak-nastroit-torgovyi-terminal-quik/]

    state: Обучение на сайте || sessionResultColor = "#15952F"
        intent!: /073 Обучение на сайте
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Учебный центр «Финам»" -> /Обучение на сайте_Учебный центр
            "Обучение для неквалифицированных инвесторов" -> /Обучение на сайте_Неквалы
            "Демо-счета" -> /Демо-счет
            "Видеокурсы по торговым системам" -> /Учебные видео

    state: Обучение на сайте_Учебный центр || sessionResultColor = "#CD4C2B" 
        a: Учебный центр «Финам» проводит регулярные встречи, вебинары, учебные курсы, для новичков, для продвинутых инвесторов и для профессионалов:
            ✅ [Дистанционные курсы|https://education.finam.ru/all-courses/]
            ✅ [Актуальное расписание видеосеминаров|https://www.finam.ru/webinars/timetable/]
            ✅ [Очные встречи, обучения, клубы инвесторов|https://www.finam.ru/landings/training/?market=&level=&type=2&cost=&agency]
            ❗ Контактная информация для решения вопросов, касательно мероприятий, указана в карточке самого мероприятия.
            ❗ [Подробнее об Учебном центре|https://www.finam.ru/landings/about-education/]
        buttons:
            "Курс «Первые шаги»" -> /Обучение на сайте_Учебный центр_Первые шаги
            "Финансовый наставник" -> /Обучение на сайте_Учебный центр_Финансовый наставник
            "Назад" -> /Обучение на сайте

    state: Обучение на сайте_Учебный центр_Первые шаги
        a: Онлайн-курс «Первые шаги» — обучение инвестиционному делу от преподавателей с многолетним стажем.
            Записаться можно в [личном кабинете Учебного центра «Финам»|https://education.finam.ru/all-courses]

    state: Обучение на сайте_Учебный центр_Финансовый наставник
        a: Осваивайте науку успешного инвестирования под руководством опытного трейдера.
            ✅ [Выбрать преподавателя и записаться|https://www.finam.ru/landings/management-colsunting/]
            ✅ Подпишитесь на консультационное обслуживание, чтобы бесплатно просматривать вебинары, выбранного преподавателя. 
            ✅ Отключиться можно в любой момент, необходимо обратится к менеджеру «Финам».

    state: Обучение на сайте_Неквалы
        a: [Учебные материалы для неквалифицированных инвесторов|https://www.finam.ru/landings/attestation-main] позволят успешно пройти тестирование и получить доступ к большему количеству финансовых инструментов.
            Все материалы разделены по темам тестов.

    state: Демо-счет || sessionResultColor = "#15952F"
        intent!: /074 Демо счет
        a: Демо-счета предназначены для ознакомления с функционалом торговой системы, торги проводятся на основе учебных котировок, поставляемых биржей.
            Режим работы демо-серверов:
            ✅ Фондовый рынок – с 10 до 19 МСК
            ✅ Срочный рынок – с 09:00 до 13:00, с 13:05 до 15:45, с 16:00 до 22:00 МСК
            ❗ В выходные и праздничные дни торги не проводятся, и сервера недоступны.
            ❗ Чтобы добавить срочную секцию в QUIK Junior, обратитесь к менеджеру «Финам» и сообщите ему логин своего учебного счета в формате 000000******. 
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Открыть демо-счет" -> /Открытие учебного счета
            "Срок действия демо-счета" -> /Демо-счет_Срок действия
            "Доступные рынки на демо-счетах" -> /Демо-счет_Доступные рынки
            "Проблемы при торговле на демо-счете" -> /Демо-счет_Проблемы
            "Программа Умный старт" -> /Умный старт
            "Обучающие видеокурсы" -> /Учебные видео

    state: Демо-счет_Срок действия
        a: Срок действия демо-счета в:
            — FinamTrade и TRANSAQ — 2 недели, 
            — TRANSAQ Connector — 1 неделя, 
            — QUIK — 3 месяца.
            Продлить действие учебного счета нельзя. Если вы хотите повторно использовать систему в учебном режиме, создайте новую заявку на открытие демо-счета с использованием другого номера телефона и электронной почты.

    state: Демо-счет_Доступные рынки
        a: На демо-счетах разных торговых систем доступны различные финансовые рынки:
            ✅ FinamTrade - Московская биржа, NYSE/NASDAQ
            ✅ QUIK - Московская биржа
            ✅ TRANSAQ - Московская биржа, Биржа СПБ, NYSE/NASDAQ

    state: Демо-счет_Проблемы
        a: ✅ Если у вас не отображаются открытые позиции и доступные для торговли средства, обратитесь к менеджеру «Финам». Обращаем внимание, сервера учебных счетов начинают работать с 10:00 МСК. В выходные и праздничные дни торги не проводятся.
            При выставлении заявки в неторговое время возникает сообщение «Торговые операции недоступны в дежурном режиме».
            ✅ Если у вас завершился период действия демо-счета, торговля будет заблокирована. Дату открытия и срок действия можно проверить в письме на почте. 
            ✅ Если при открытии счета не поступило письмо с данными демо-счета, проверьте папку «Спам» у себя на почте. Частой ошибкой являются опечатки при заполнении регистрационных данных. Необходимо создать новую заявку с другим номером телефона и почтой.

    state: Comon || sessionResultColor = "#15952F"
        intent!: /075 Comon
        a: Детальная [информация и правила сервиса «Финам Автоследование»|https://docs.comon.ru/general-information/]
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Авторизация на comon.ru" -> /Comon_Авторизация
            "Подключить/отключить стратегию" -> /Comon_Подключить стратегию
            "Синхронизация" -> /Comon_Синхронизация
            "Торговля по счету с автоследованием" -> /Comon_Торговля по счету
            "Комиссии за автоследование" -> /Комиссии_Другие_Автоследование
            "Если аккаунт заблокирован" -> /Comon_Блок
            "Тестирование для автоследования" -> /Comon_Тестирование
            "ComonTrade API" -> /Стороннее ПО_ComonTrade API

    state: Comon_Авторизация
        a: Данными для входа на сайт [comon.ru|https://www.comon.ru/] являются логин и пароль от [личного кабинета|https://lk.finam.ru/]
            ✅ По умолчанию логином от личного кабинета является номер телефона в международном формате (например: начиная с «7…» - Россия, «375…» - Беларусь, «997…» - Казахстан)
            ✅ Пароль вы задавали самостоятельно
            ✅ Для восстановления доступа к личному кабинету:
            1. перейдите в [личный кабинет|https://lk.finam.ru/]
            2. нажмите на кнопку «Забыли логин или пароль?»
            3. введите ФИО, паспортные данные и подтвердите восстановление
            4. на вашу электронную почту придет письмо с логином и ссылкой на создание нового пароля

    state: Comon_Подключить стратегию
        a: 1. Подключение:
            ✅ авторизуйтесь на сайте [comon.ru|https://www.comon.ru/]
            ✅ убедитесь, что на вашем брокерском счете есть денежные средства
            ✅ пройдите [инвестиционное профилирование|https://lk.finam.ru/user/invest-profile]
            ✅ выберите нужную стратегию, перейдите на страницу с ее описанием и нажмите на кнопку «подключить»
            ❗ Не все стратегии подходят для счетов ИИС (торги на иностранных биржах по счетам ИИС недоступны).
            2. Отключение:
            ✅ авторизуйтесь на сайте [comon.ru|https://www.comon.ru/]
            ✅ нажмите на свой никнейм и перейдите в раздел «подписки» (здесь отображается информация о подключенных стратегиях)
            ✅ нажмите на значок шестеренки и выберите «отключить автоследование»
            ❗ Позиции будут закрыты во время активной торговой сессии рынка, на котором торгуются используемые ценные бумаги. Если рынок закрыт, то подписка перейдет в статус удаления. Дальнейшие действия будут доступны только после закрытия позиций.

    state: Comon_Синхронизация
        a: Для синхронизации со стратегией автора необходимо:
            ✅ авторизоваться на сайте [comon.ru|https://www.comon.ru/] 
            ✅ нажать на свой никнейм и перейти в раздел «подписки» (здесь отображается информация о подключенных стратегиях) 
            ✅ при нажатии на значок шестеренки отобразится меню «синхронизировать портфель»

    state: Comon_Торговля по счету
        a: Мы не рекомендуем вам совершать торговые операции со счета, к которому подключена стратегия автоследования. Такое вмешательство нарушит доходность по данной стратегии, а также может привести к совершению дополнительных сделок.
            Например, автор может купить или продать выбранный вами инструмент. В этом случае есть вероятность получить убыток по позиции и заплатить дополнительную комиссию за покупку/продажу.

    state: Comon_Блок
        a: Если аккаунт заблокирован, отправьте письмо со своей электронной почты на autotrade@corp.finam.ru

    state: Comon_Тестирование
        a: Перед подключением к любой из стратегий, при отсутствии статуса «квал», нужно пройти ряд тестирований для неквалифицированных инвесторов.
            ✅ [Пройти тестирование в личном кабинете|https://lk.finam.ru/user/invest-status/qual-exam/tests] 
            ✅ Перечень необходимых тестов:
            — Производные финансовые инструменты
            — Акции вне котировальных списков
            — Необеспеченные сделки
            — Облигации со структурным доходом
            — ИЦБ, требующие тестирования

    state: Услуги компании || sessionResultColor = "#15952F"
        intent!: /076 Услуги компании
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Помощники инвестирования" -> /Услуги компании_Помощники
            "Готовые решения" -> /Услуги компании_Готовые решения
            "Акции в подарок" -> /Услуги компании_Акции в подарок
            "Партнерская программа" -> /Услуги компании_Партнерская программа
            "Финам Бонус" -> /Финам-бонус
            "Умный старт" -> /Умный старт

    state: Услуги компании_Помощники
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Аналитика" -> /Услуги компании_Помощники_Аналитика
            "Диагностика" -> /Услуги компании_Помощники_Диагностика
            "Прямой доступ на биржи (DMA услуги)" -> /Услуги компании_Помощники_Прямой доступ
            "Консультационное управление" -> /КУ
            "ИИ советник" -> /Услуги компании_Помощники_ИИ советник
            "AI-cкринер" -> /Услуги компании_Помощники_AI-cкринер
            "Назад" -> /Услуги компании

    state: Услуги компании_Помощники_Аналитика
        a: Подпишитесь на бесплатную аналитику от «Финам» — и узнавайте в числе первых, как главные мировые события влияют на финансовые рынки.
            ✅ [Подробнее|https://www.finam.ru/landings/analytics/]
            ✅ Для отключения услуги нужно обратиться к менеджеру «Финам».

    state: Услуги компании_Помощники_Диагностика
        a: «Диагностика» — бесплатный аналитический сервис, который помогает увеличивать эффективность вложений начинающим и опытным инвесторам.
            [Подключить сервис и ознакомится детальнее|https://www.finam.ru/landings/diagnostics]
            Отключение сервиса не требуется.

    state: Услуги компании_Помощники_Прямой доступ || sessionResultColor = "#CD4C2B"
        a: «Финам» предлагает услуги прямого подключения (DMA) на российские рынки.
            [Ознакомится детальнее|https://broker.finam.ru/landings/direct-access]
            Чтобы получить консультацию, подключить или отключить услугу можно обратиться по телефону *1945 доб.3024 или на электронную почту dma@corp.finam.ru

    state: Услуги компании_Помощники_ИИ советник || sessionResultColor = "#CD4C2B"
        script:
            $analytics.setMessageLabel("ИИ советник", "КАЦ TB");
        a: «ИИ советник» — это нейросеть, основанная на современных алгоритмах и искусственном интеллекте, что позволяет вам подобрать инвестиционные инструменты, исходя из ваших предпочтений о рискованности инструментов, целевой доходности, планируемом сроке инвестирования.
        buttons:
            "Детальнее у специалиста" -> /Перевод на оператора КАЦ

    state: Услуги компании_Помощники_AI-cкринер || sessionResultColor = "#CD4C2B"
        a: «AI-скринер» — это инструмент для инвесторов на основе искусственного интеллекта, обученный и протестированный на финансовых показателях компаний, макроэкономических данных, техническом анализе и других данных. Скринер дает годовой прогноз по российским и иностранным ценным бумагам.
            ✅ [Подключить сервис и ознакомится детальнее|https://www.finam.ru/landings/ai-screener/]
            Отключение сервиса не требуется.

    state: Услуги компании_Готовые решения
        a: Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Высокодоходный портфель" -> /Высокодоходный портфель
            "Защитный портфель" -> /Защитный портфель
            "Управляющая компания" -> /Услуги компании_Готовые решения_УК
            "Инвестиционное сопровождение" -> /Услуги компании_Готовые решения_Сопровождение
            "Структурные облигации" -> /Услуги компании_Готовые решения_Структурные облигации
            "Ежемесячный купон (ОФЗ)" -> /Услуги компании_Готовые решения_ОФЗ
            "Назад" -> /Услуги компании

    state: Высокодоходный портфель
        script:
            $analytics.setMessageLabel("Высокодоходный портфель", "КАЦ TB");
        a: «Высокодоходный портфель» – это продукт с высоким уровнем риска и возможностью кратно увеличить вложенный капитал. Вы можете инвестировать всего от 30000 ₽.
        buttons:
            "Детальнее у специалиста" -> /Перевод на оператора КАЦ
    
    state: Защитный портфель
        script:
            $analytics.setMessageLabel("Защитный портфель", "КАЦ TB");
        a: «Защитный портфель» предлагает выгодное сочетание защиты капитала и потенциальной доходности. Вы сами выбираете уровень защиты, планируемый срок инвестирования и направление движения рынка, в которое верите.
        buttons:
            "Детальнее у специалиста" -> /Перевод на оператора КАЦ
            
    # state: Услуги компании_Готовые решения_ИИП_Отключить
    #     a: Каждый индивидуальный инвестиционный портфель имеет заранее установленный срок действия, так как его основу составляют опционы с конечной датой исполнения. Чтобы отключить ИИП раньше срока, обратитесь к менеджеру компании. Предупреждаем, досрочный выход из продукта может привести к снижению доходности или повлечь убытки.
    #     buttons:
    #         "Детальнее у специалиста" -> /Перевод на оператора КАЦ
            
    state: Услуги компании_Готовые решения_УК
        a: По вопросам счетов, открытых в рамках УК «Финам Менеджмент», обратитесь к менеджеру управляющей компании любым удобным [способом|https://www.fdu.ru/funds/aboutcompany/]

    state: Услуги компании_Готовые решения_Сопровождение
        script:
            $analytics.setMessageLabel("Инвест сопровождение", "КАЦ TB");
        a: «Инвестиционное сопровождение» — это комплексная услуга, которая включает в себя: профессиональный анализ рынка, управление портфелем и последующую информационно-аналитическую поддержку, как по ранее составленным портфелям, так и по торговым операциям, налоговое консультирование, а также обучение и персонализированную поддержку.
            ✅ Важная особенность продукта - тариф «Прибыльное партнёрство», комиссия по которому взимается, только если клиент получил прибыль.
            ✅ Порог входа — 1000000 ₽
            ✅ [Подробнее|https://www.finam.ru/landings/invest-soprovojdenie/]
        buttons:
            "Детальнее у специалиста" -> /Перевод на оператора КАЦ

    state: Услуги компании_Готовые решения_Структурные облигации || sessionResultColor = "#CD4C2B"
        a: Структурная облигация — это ценная бумага, выплата номинала и купона которой зависит от заранее оговоренных условий.
            ✅ Структурные облигации выпускаются в российской юрисдикции, поэтому нет риска заморозки активов
            ✅ [Подробнее|https://www.finam.ru/publications/item/strukturnye-obligacii-chto-nuzhno-znat-investoru-20230510-153700/]
            ❗ Продукт предназначен только для квалифицированных инвесторов
            ✅ [Узнать подробности, заказать консультацию, подключить или отключить услугу|https://www.finam.ru/landings/structured-note/]

    state: Услуги компании_Готовые решения_ОФЗ
        a: Подробности и подключение услуги доступны по [ссылке|https://trading.finam.ru/investments] 
            После подключения услуги, произойдет покупка бумаг. Реализовать инструмент вы можете в любой момент времени с помощью обратной сделки - продажи.

    state: Услуги компании_Акции в подарок || sessionResultColor = "#CD4C2B"
        a: Через «Финам» вы можете покупать акции в подарок родственникам, друзьям или коллегам.
            ✅ Все ценные бумаги, доступные для приобретения, на одной [странице|https://shop.finam.ru/]

    state: Услуги компании_Партнерская программа
        a: Станьте партнером и зарабатывайте вместе с «Финам».
            Вы можете выбрать оптимальную форму сотрудничества в зависимости от своих ресурсов, планов развития и особенностей вашего города.
            ✅ [Подать заявку и узнать подробнее|https://edox.finam.ru/Partner/Registration]

    state: КУ || sessionResultColor = "#15952F"
        intent!: /077 КУ
        a: В рамках Консультационного управления (КУ) от «Отдела Инвестиционного Консультирования» предлагается три варианта, которые зависят от суммы средств инвестирования.
        buttons:
            "Пакет «Лайт»" -> /КУ_Лайт
            "Пакет «Эксперт»" -> /КУ_Эксперт
            "«Персональный брокер»" -> /КУ_Персональный брокер
            "Как отключить услугу" -> /КУ_Отключить услугу

    state: КУ_Лайт
        a: Пакет «Лайт» активируется при подключении тарифа «Консультационный». В рамках пакета услуг предоставляются инвестиционные идеи от профессиональных трейдеров.
            Минимальная рекомендуемая сумма для инвестирования — от 30000 ₽. 
            [Подробнее о тарифе|https://www.finam.ru/landings/tariffs/]
            Подключить тариф можно в личном кабинете в [разделе «Детали» по счёту|https://lk.finam.ru/details]
            Услуга подходит для клиентов, которым нужна аналитическая поддержка. А также для тех, кто торгует небольшими объемами ценных бумаг на московской бирже. 
            На электронную почту предоставляются следующие информационные материалы:
            ✅ ежедневные инвестиционные идеи по Российскому рынку,
            ✅ ежедневные инвестиционные идеи по Американскому рынку,
            ✅ ежедневный обзор рынка,
            ✅ еженедельный обзор рынка по понедельникам,
            ✅ фундаментальные отчеты по инструментам глобального рынка.

    state: КУ_Эксперт
        a: Пакет «Эксперт» — продвинутая аналитическая поддержка.
            Минимальная рекомендуемая сумма для инвестирования — от 300000 ₽.
            0,1% от суммы активов (данная комиссия удерживается со счета клиента в течение пяти рабочих дней после окончания расчетного периода).
            Для подключения необходимо обратиться к менеджеру компании.
            Включает в себя:
            ✅ ежедневные торговые идеи по российским и американским биржевым инструментам,
            ✅ еженедельный аналитический обзор рынков РФ и США, включающий «закрытые» данные: инсайдерские покупки, дивидендные выплаты/отсечки, изменение консенсус-прогнозов аналитиков, статистика по открытым коротким/длинным позициям и другое,
            ✅ закрытый еженедельный вебинар.

    state: КУ_Персональный брокер
        a: В рамках услуги «Персональный брокер» предоставляется информация о текущей рыночной ситуации, торговые идеи и торговые сигналы, консультанты помогают подобрать подходящую инвестиционную стратегию и сформировать инвестиционный портфель. Услугу предоставляет «Отдел Инвестиционного Консультирования».
            Таким образом, «Персональный брокер» — это фактически полное финансовое сопровождение с самыми разнообразными финансовыми инструментами на всех рынках.
            Для подключения необходимо обратиться к менеджеру компании.
            Рекомендуемая минимальная сумма активов — от 3000000 ₽.
            [Детальнее об услуге|https://www.finam.ru/landings/personal-broker/]

    state: КУ_Отключить услугу
        a: Для отключения консультационного управления нужно обратиться к менеджеру «Финам».
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: Умный старт || sessionResultColor = "#15952F"
        intent!: /078 Умный старт
        a: Научитесь инвестировать на реальном рынке с помощью уникальной программы «Умный старт».
            После регистрации в программе участник получает 50000 ₽, на которые он может торговать в течение 5 рабочих дней акциями на Московской бирже и может получить денежное вознаграждение, ничего не теряя в случае убытка.
            ✅ [Узнать больше о программе «Умный старт» и зарегистрироваться|https://www.finam.ru/landings/test-drive-vers/]
        buttons:
            "Как получить доступ" -> /Умный старт_Получить доступ
            "Как получить приз" -> /Умный старт_Получить приз
            "Демо-счета" -> /Демо-счет

    state: Умный старт_Получить доступ
        a: ✅ [Зарегистрироваться в программе|https://www.finam.ru/landings/test-drive-vers/]
            ✅ Участие в программе бесплатное и доступно только для новых клиентов.
            ✅ Принимать участие в программе можно до тех пор, пока инвестиции не принесут доход в размере 1500 ₽, который можно будет вывести на реальный брокерский счет. 
            ✅ В период участия в программе инвесторам не потребуется платить комиссию за сделки и подоходный налог, эти расходы возьмет на себя «Финам».

    state: Умный старт_Получить приз
        a: Для получения приза нужно открыть и пополнить брокерский счет в «Финам» от 30000 ₽ в течение 7 рабочих дней.
            «Финам» переведет деньги на брокерский счет. После этого можно купить на них акции, валюту и другие инструменты.

    state: Финам-бонус || sessionResultColor = "#15952F"
        intent!: /079 Финам бонус
        a: Участвуйте в акции «Финам бонус»!
            ✅ Пройдите регистрацию и получите 500 приветственных бонусов. До конца июня 2024 года накопите 3000 бонусов за использование сервисов «Финам» и получите их на брокерский счет. 1 бонус = 1 ₽
            ✅ [Регистрация, выбор заданий для получения бонусов, детали акции и получения призов|https://www.finam.ru/landing/finam-bonus-2024/]
            ✅ Бонусный баланс обновляется один раз в сутки
            ✅ Выплата вознаграждения за участие во 2 этапе акции (с апреля по июнь 2024 года) будет осуществляться до 15 июля 2024 года
            ❗ Выплата вознаграждения для участников 1 этапа акции (с января по март 2024 года) будет перечислена на брокерский счет (не ИИС) в период с 5 по 15 апреля 2024 года, если участники не предпочтут обменять бонусы на онлайн курс.
        buttons:
            "Кабинет участника Финам бонус" -> /Финам-бонус_Кабинет участника

    state: Финам-бонус_Кабинет участника
        a: Для входа в кабинет участника «Финам бонус» нужно: 
            1.  Авторизоваться на сайте [finam.ru|https://www.finam.ru], для этого:
            ✅ в верхнем правом углу страницы сайта нажать на квадратную иконку меню и выбрать вход в finam.ru
            ✅ использовать логин/пароль, полученный при регистрации на сайте
            2. Перейти в [личный кабинет участника «Финам бонус»|https://bonus.finam.ru/account/]
            ❗ Увидеть накопленные бонусы участник сможет только в том случае, если будет авторизован на сайте [finam.ru|https://www.finam.ru]. В противном случае участнику будет отображаться нулевой баланс. 
            ❗ Если ранее вы не регистрировались на сайте [finam.ru|https://www.finam.ru], то на вашу электронную почту одновременно с письмом о регистрации в акции «Финам бонус» поступит письмо с логином и паролем от сайта [finam.ru|https://www.finam.ru]

    state: Стороннее ПО 2 || sessionResultColor = "#15952F"
        intent!: /080 Стороннее ПО 2
        a: Профессиональные торговые решения Sterling Trader Pro и Lightspeed Trader
            ✅ Для активных трейдеров и Prop Trading групп
            ✅ Выгодные условия при обороте более 250 000 акций в месяц
            ✅ [Узнать подробнее и подать заявку на консультацию|https://www.finam.ru/landings/sterling/]

    state: Счет Иностранные биржи || sessionResultColor = "#15952F"
        intent!: /081 Счет Иностранные биржи
        a: С 1 февраля 2024 года открытие нового счета «Иностранные биржи» недоступно.
            ✅ Торговля по уже открытым счетам доступна на фондовых биржах NYSE/NASDAQ и опционы на площадке CBOE.
            ✅ Доступные торговые системы: FinamTrade, TRANSAQ и MetaTrader 5.
            ❗ 1 февраля 2024 года – окончание акции с возможностью пополнения счета долларами США и их последующим бесплатным хранением. При пополнении счета «Иностранные биржи» до 31 января включительно – хранение долларов США – без комиссии, при пополнении с 1 февраля – на свободный остаток долларов США будет начисляться комиссия за хранение валюты.
        buttons:
            "Пополнение/вывод средств" -> /Счет Иностранные биржи_пополнение_вывод
            "Комиссия за хранение валюты" -> /Валютный рынок_Ввод и вывод_Хранение валюты

    state: Счет Иностранные биржи_пополнение_вывод
        a: 1. Пополнить брокерский счет «Иностранные биржи» можно только рублями РФ:
            – Через кассу Банка «Финам»
            – Онлайн в [личном кабинете|https://lk.finam.ru/deposit]
            – С действующих брокерских счетов, открытых в «Финам» доступен перевод средств только в рублях РФ
            2. Вывод средств доступен в рублях РФ или в долларах США в [личном кабинете|https://lk.finam.ru/withdraw]

    state: Сегрегированный || sessionResultColor = "#15952F"
        intent!: /082 Сегрегированный
        a: Сегрегированные счета позволяют хранить ценные бумаги обособленно от остальных бумаг брокера. Соответственно, помогут защитить вложения от рисков ограничений на расчеты. [Подробнее|https://www.finam.ru/landings/segregated-account/]
            ✅ Счет доступен только квалифицированным инвесторам
            ✅ Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Открытие счета Global" -> /Сегрегированный_Открытие
            "Пополнение счета Global" -> /Сегрегированный_Пополнение
            "Доступные инструменты" -> /Сегрегированный_Инструменты
            "Доступ к торговой системе" -> /Сегрегированный_Доступ к ТС
            "Налогообложение" -> /Сегрегированный_Налогообложение
            "Вывод средств" -> /Сегрегированный_Вывод
            "Перевод ценных бумаг" -> /Сегрегированный_ПереводЦБ

    state: Сегрегированный_Открытие
        a: Открытие счета «Сегрегированный Global» доступно только квалифицированным инвесторам, при личном посещении офиса или дистанционно. Помимо внутреннего паспорта РФ для открытия счета нужен второй документ из перечня на выбор:
            – заграничный паспорт
            – водительское удостоверение
            – справка из банка (любого содержания, не старше 6 месяцев) с указанием ФИО и адреса
            – счет за коммунальные услуги (не старше 6 месяцев) с указанием ФИО и адреса.
            ✅ Счет открывается в течение одного дня. Торговля доступна с момента пополнения счета.
            ✅ Перед посещением офиса предварительно рекомендуется согласовать время и цель визита с менеджером. [Время работы и адреса офисов|https://www.finam.ru/about/contacts]
            ✅ Дистанционное открытие счета «Сегрегированный Global» доступно для клиентов, у которых уже были ранее открыты счета в компании «Финам» при личном посещении офиса компании. Дистанционное открытие доступно в личном кабинете [edox.finam.ru|https://edox.finam.ru/] → «Открыть счет» → «Брокерская компания» → «Иностранные рынки» → «Сегрегированный Global». (Данный раздел личного кабинета доступен только квалифицированным инвесторам).
            ✅ [Иллюстрированная инструкция по открытию счета|https://www.finam.ru/dicwords/file/files_chatbot_instrukciysegregopen]

    state: Сегрегированный_Пополнение
        a: Пополнение счета «Сегрегированный Global» доступно в валютах рубли РФ и доллары США.
            ✅ Валюта счета «Сегрегированный Global» - доллар США. При пополнении счета в другой валюте конвертация в доллары США происходит по текущему курсу + 1%.
            ✅ [Иллюстрированная инструкция по пополнению «Сегрегированный Global» через Банк «Финам»|https://www.finam.ru/dicwords/file/files_chatbot_instrukciysegreg]
            ❗ Перед пополнением счетов обязательно проконсультируйтесь с менеджером «Финам».

    state: Сегрегированный_Инструменты
        a: ✅ На счете «Сегрегированный Global» предоставляется доступ к биржам NYSE, NASDAQ и СВОЕ, в рамках которых доступна торговля Акциями, АДР, ETF, а также опционами и опционными стратегиями.

    state: Сегрегированный_Доступ к ТС
        a: Торговля доступна через торговые системы TRANSAQ US и FinamTrade.
            Выберите, чтобы узнать подробнее:
        buttons:
            "FinamTrade" -> /ИТС_FinamTrade_Авторизация
            "TRANSAQ US" -> /ИТС_TRANSAQ
            "Назад" -> /Сегрегированный

    state: Сегрегированный_Налогообложение
        a: ✅ АО «Финам» является налоговым агентом на доходы, полученные при самостоятельной торговле в рамках счета «Сегрегированный Global», за исключением дивидендов, полученных в иностранной валюте до 2024 года, по ним отчитываться необходимо самостоятельно в ИФНС.
            ✅ Так как счет открывается в иностранной компании, об открытии счета необходимо уведомить налоговую службу.
            Подать сведения об открытии счета в ФНС можно в мобильном приложении или через сайт [nalog.ru|https://www.nalog.gov.ru/] в разделе «Жизненные ситуации» → «Информировать о счете в банке, расположенном за пределами РФ».
            ❗ Подать сведения нужно в течение 1 месяца с даты открытия счета. Подавать выписку (отчет брокера Lime Trading) о движении средств необходимо до 1 июня года следующего за отчетным.

    state: Сегрегированный_Вывод
        a: Поручение на вывод денежных средств со счета «Сегрегированный Global» можно подать через [личный кабинет Lime Trading|https://j2t.tech/ru/]
            1. Комиссии за вывод на счета Банка «Финам»:
            ✅ комиссии за вывод со стороны брокера – отсутствуют;
            ✅ комиссия за зачисление долларов и евро на банковский счет: 3% от суммы операции, но не менее 300 $/€ и не более суммы операции.
            2. Комиссии за вывод в иной неподсанкционный банк РФ: 0,1%, мин. 1500 ₽, макс. 2500 ₽
            Минимальная сумма вывода за транзакцию – 3000 ₽
            3. Комиссии за вывод валюты в зарубежные банки (требуется предварительное согласование):
            ✅ комиссия за вывод со стороны вышестоящего брокера:
            – в долларах США: 0,40% (мин. 40 $, макс. 1000 $).
            – в евро: без комиссии
            Минимальная сумма вывода за транзакцию – 20 $/20 €
            Актуальные комиссии на [сайте вышестоящего брокера|https://j2t.tech/ru/solutions/mt5global/withdrawal/]
            ✅ комиссии за зачисление валюты на счета зарубежных банков необходимо уточнять самостоятельно у банка-получателя.
            ❗ Выводы в валюте на счета в российских банках, кроме Банка «Финам», не осуществляются.

    state: Сегрегированный_ПереводЦБ
        a: К переводу на счет «Сегрегированный Global» доступны ценные бумаги, приобретенные на иностранных биржах со счетов «Финам».
            Для корректного перевода активов обратитесь к менеджеру.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора

    state: FinamSmart || sessionResultColor = "#15952F"
        intent!: /083 Finam Smart
        a: Приложение FinamSmart является сервисом управления автоследованием. Установить приложение можно через:
                ✅ AppStore — https://apps.apple.com/ru/app/id434829194 
                ✅ PlayMarket — https://play.google.com/store/apps/details?id=ru.finam.android 
                ✅ AppGallery — https://appgallery.huawei.com/#/app/C104184793 
                Детальнее о сервисе автоследования:
        buttons:
            "Детальнее об автоследовании" -> /Comon

    state: ЛЧИ || sessionResultColor = "#15952F"
        intent!: /084 ЛЧИ
        a: Конкурс «Лучший частный инвестор 2023» (ЛЧИ) проводился ПАО Московская биржа в период с 05 октября по 21 декабря 2023 года. 
            ✅ Итоги ежегодного конкурса «Лучший частный инвестор 2023», который проводился с 05 октября по 21 декабря 2023 года на [сайте конкурса|https://investor.moex.com/].

    state: Экспорт котировок || sessionResultColor = "#15952F"
        intent!: /085 Экспорт котировок
        a: Бесплатная загрузка архивных котировок для их дальнейшего использования в системах технического анализа доступна по ссылке: https://www.finam.ru/profile/moex-akcii/gazprom/export/

    state: Анекдоты || sessionResultColor = "#15952F"
        intent!: /086 Анекдоты
        random:
            a: Деньги не сделают вас счастливее.
                У меня сейчас 50 миллионов, и я так же счастлив, как и тогда, когда у меня было 48 миллионов.
            a: — Мама, мне сказали, что я шизофреник.
                — Ну что ты сына, кто тебе такое сказал?
                — Болинджеры... После того, как я Ар-Си-Айку зафильтровал через ФНЧ, кинул на нее веера Фибоначчи, и наложил на всё это Экспоненциаьные мувинги, форсированные по амплитуде, взвесив их по объему в моменте.
            a: Чем отличается инвестор от спекулянта? 
                Спекулянт покупает дешево, продаёт дорого; а инвестор покупает дорого, а продать не может вообще.
            a: У аналитика спрашивают:
                — Скажите, а ваши прогнозы всегда совпадают?
                — Конечно, всегда, только даты иногда не совпадают...
            a: Умирает брокер, над ним мечется бригада врачей-реаниматоров. 
                Электрошок, искусственное дыхание. 
                - Мы его теряем!!! 
                - Пульс? 
                - 9...8...7...6...5... 
                Умирающий подскакивает: 
                - Упадет до 3-х – начинай покупать!!!
            a: — И что у тебя на завтрак?
                — Овсяная каша, овсяный кисель. 
                — Англия?
                — Ипотека.
            a: Устраивается молодой выпускник финансового института на работу трейдером.
                Начальник отдела объясняет ему его обязанности и, указывая на компьютер, говорит: 
                — А это ваш персональный помощник — компьютер. Он будет выполнять за вас половину работы. Вопросы есть? 
                — Есть. А можно мне два компьютера?
            a: Клиент брокеру: «Мо-мо-же-жет д-д-дадите п-п-плечо 40?»
                Брокер: «Даже не заикайтесь…»

    state: Сервис Intelinvest || sessionResultColor = "#15952F"
        intent!: /087 Сервис Intelinvest
        a: Иллюстрированная [инструкция по генерации API-токена для сервиса Intelinvest|https://www.finam.ru/dicwords/file/files_chatbot_instrukciyapogeneraciiapitokenaintelinvest]

    state: Иностранные облигации || sessionResultColor = "#15952F"
        intent!: /089 Иностранные облигации
        a: Квалифицированным инвесторам «Финам» доступны для покупки иностранные облигации на внебиржевом рынке:
            ✅ Государственные облигации США «Treasuries»
            ✅ Государственные облигации иных стран (Оман и Турция)
            ✅ Корпоративные и государственные облигации Китая
            Пожалуйста, выберите один из предложенных вариантов:
        buttons:
            "Облигации США" -> /Иностранные облигации_США
            "Облигации Китая" -> /Иностранные облигации_Китая
            "Облигации иных стран" -> /Иностранные облигации_Иных стран

    state: Иностранные облигации_США
        a: Квалифицированным инвесторам «Финам» доступны сделки на внебиржевом рынке с иностранными государственными облигациями США.
            ✅ Минимальная сумма сделки от 50000 $
            ✅ Купон 2,875 – 4,125 %
            ✅ Комиссия за сделки - 0.7%
            Чтобы заказать консультацию или подать поручение, нужно обратиться к менеджеру «Финам».

    state: Иностранные облигации_Китая
        a: Квалифицированным инвесторам «Финам» доступны сделки на внебиржевом рынке по ряду корпоративных и государственных бондов Китая.
            ✅ Минимальная сумма сделки от 70000 $
            ✅ Конечное хранение данных бумаг в дружественном Гонконгском брокере.
            ✅ Комиссия за сделки - 0.118%
            Чтобы заказать консультацию или подать поручение, нужно обратиться к менеджеру «Финам».

    state: Иностранные облигации_Иных стран
        a: Квалифицированным инвесторам «Финам» доступны сделки на внебиржевом рынке с иностранными государственными облигациями Турции и Омана.
            ✅ Минимальная сумма сделки от 200000 $
            ✅ Купон 6 - 6,75 %
            ✅ Комиссия за сделки - 0.7%
            Чтобы заказать консультацию или подать поручение, нужно обратиться к менеджеру «Финам».

    state: Finam Invest || sessionResultColor = "#15952F"
        intent!: /090 Finam Invest
        a: Finam Invest — это новое мобильное приложение от «Финам», включает в себя все необходимое для осознанных инвестиций, полагаясь на интересы и предпочтения самого пользователя:
            ✅ Готовые подборки ценных бумаг для портфеля и инвестиционных продуктов.
            ✅ Инвестор определяет параметры своего инвестпортфеля, а сервис рекомендует варианты с указанием соответствия, интересности, перспективности, предельно кратко и понятно.
            ✅ Информация о состоянии брокерских и банковских счетов пользователя наглядно отображается на одной странице.
            ✅ Самая актуальная финансовая информация из интеллектуальной новостной ленты Limex. В приложение встроен новостной агрегатор с возможностью фильтрации по темам. Умная лента настраивается под предпочтения инвестора.
            ✅ Опция «Избранное» работает в двух вариантах: первый — список интересующих инструментов, сервисов и инвестпродуктов, второй — отслеживание ценных бумаг (Watchlist). Список может включать до 350 позиций.
            ✅ Визуализация портфеля в 3D позволит наглядно увидеть соотношение активов в портфеле, а алгоритмы искусственного интеллекта подскажут прогноз по ним.
            ✅ Сервис «Диагностика» проведет анализ портфеля пользователя по 7 параметрам, оценит качество вашей торговли.
            ✅ Выставляйте торговые заявки прямо из Watchlist. Форма ввода заявок простая и интуитивно понятная.
            ❗ Приложение находится в разработке, пожелания по улучшению сервиса можно оставить по [ссылке|https://www.finam.ru/landings/finam-invest-feedback/]
            ❗ Основной функционал приложения доступен всем пользователям приложения, но некоторые инвестиционные продукты — только клиентам «Финам».
            ❗ Представленные подборки не являются индивидуальной инвестиционной рекомендацией.

    state: ИПИФ «Алгоритм роста» || sessionResultColor = "#15952F"
        intent!: /091 ИПИФ Алгоритм роста
        a: ИПИФ «Алгоритм роста» позволяет воспользоваться всеми преимуществами высокочастотного трейдинга при относительно небольших вложениях, так как оборудование для такого вида инвестирования обычно стоит несколько миллионов рублей.
            ✅ Количество паев ограничено.
            ✅ Услуга доступна для квалифицированных инвесторов.
            ✅ Чтобы воспользоваться предложением, нужно:
            1. Открыть счет в управляющей компании (УК) «Финам», перейдя по ссылке https://edox.finam.ru
            2. Выбрать стратегию «Алгоритм роста»
            3. Пополнить новый счёт в УК от 100000 ₽
            ✅ Подробности и инструкции по ссылке https://www.finam.ru/landings/asset-management-hft/ 
            ✅ Актуальные новости для владельцев паев:
            С 23 по 24 ноября 2023 года ожидается начисление паев регистратором. На данный момент происходят последние настройки архитектуры ИПИФ «Алгоритм Роста». В ближайшие 2 недели будет полноценный запуск фонда.

    state: Пресса || sessionResultColor = "#15952F"
        intent!: /092 Пресса
        a: ✅ Финансовая группа «Финам» рада сотрудничеству со СМИ и блогерами, освещающими деловые, экономические, общественно-политические и информационно-развлекательные темы. Пресс-служба и специалисты профильных подразделений,
            в том числе профессиональная команда биржевых аналитиков,
            всегда готовы на регулярной основе предоставлять аналитические материалы, пресс-релизы и комментарии по широкому кругу экономических тем и вопросам, связанным с бизнесом финансовой группы. Связаться с пресс-службой и узнать подробнее можно по ссылке: https://www.finam.ru/landings/press 
            ✅ Ознакомиться с опубликованными публикациями «Финам» в прессе можно по ссылке: https://www.finam.ru/publications/section/press

    state: Инструменты
        intent!: /093 Инструменты
        a: ✅ Найти интересующий торговый инструмент можно как на сайтах бирж, например, Московской https://www.moex.com/s4  или СПБ Биржи https://spbexchange.ru/ru/listing/securities/
            Так и воспользовавшись поиском в торговой системе.
            ✅ Для подбора облигаций, также можно воспользоваться сервисом от «Финам» по ссылке: https://bonds.finam.ru/issue/info/ 
            ✅ Информация о доступности инструментов для отдельных категорий инвесторов и по доступным биржам с брокером «Финам» - по кнопке «Актуальные доступы».
            ✅ Бумаги могут отображаться недоступными для торгов по причине проходящего корпоративного действия либо по причине ограничений вышестоящих депозитариев.
            ✅ Проверить необходимость статуса для торговли определенным инструментом можно с помощью кнопки меню «Проверка инструмента на КВАЛ».
            Чтобы узнать подробнее, выберите один из предложенных вариантов:
        buttons:
            "Как найти инструмент" -> /Инструменты_Найти
            "Актуальные доступы" -> /Доступные биржи
            "Ограничения" -> /Ограничение ЦБ
            "Предложить добавить инструмент в ИТС" -> /Инструменты_Предложить добавить инструмент
            "Проверка инструмента на КВАЛ" -> /КВАЛ_Проверка инструмента

    state: Инструменты_Найти
        a: ✅ Самый удобный поиск инструментов в торговой системе FinamTrade: инструменты можно выбирать как через строку поиска, где дополнительно будут предложены альтернативные и производные финансовые инструменты, так и через готовые подборки инструментов в левом вертикальном меню «Рынки»
            ✅ В системе TRANSAQ для поиска и выбора инструмента нужно нажать правой кнопкой мыши по таблице «Финансовые инструменты». Обучающие видеоматериалы по ссылке: https://education.finam.ru/articles/43 
            ✅ Для поиска инструмента в системе QUIK нужно перейти по следующим вкладкам: «Система» → «Заказ данных» → «Поток котировок», далее выбрать классы нужных инструментов, при необходимости в этом же поле настроить «Фильтр инструментов»
            Обучающие видеоматериалы по ссылке: https://education.finam.ru/lk/course/de5a9d1c-224f-4376-882b-8eb221fa779b 
            ✅ Обзор бумаг и фундаментальный анализ и прогнозы с сервисом «Финам AI-скринер» по ссылке: https://ai.finam.ru/
            ✅ Нажмите кнопку ниже, если не удалось найти ценную бумагу в торговых системах.
        buttons:
            "Предложить добавить инструмент в ИТС" -> /Инструменты_Предложить добавить инструмент

    state: Инструменты_Предложить добавить инструмент
        a: Если не удалось найти ценную бумагу в торговых системах, то для предложения к добавлению такой бумаги, в продолжение данного чата, направьте:
            ✅ полное наименование бумаги
            ✅ тикер
            ✅ ISIN (международный идентификационный код ценной бумаги)
            ✅ наименование торговой системы
            После введенных данных, нажмите «Перевод на оператора»
            Менеджер поддержки проверит информацию и сориентирует вас.
        buttons:
            "Перевод на оператора" -> /Перевод на оператора
