function screenerAnswers (path) {
    var answer = ''
    switch (path) {
        case 'Доходность':
            answer = $jsapi.context().session.bondData.yield > 0
                ? "Доходность по облигации {{($jsapi.context().session.bondData.yield * 100).toFixed(2)}}%"
                : "Мы не нашли доходность по данной облигации"
            break;
        case 'Цена':
            answer = $jsapi.context().session.bondData.price > 0
                ? "Цена облигации {{$jsapi.context().session.bondData.price.toFixed(2)}}"
                : "Мы не нашли цену по данной облигации"
            break;
        case 'Модифицированная дюрация':
            answer = $jsapi.context().session.bondData.modifiedDuration > 0 
                ? "Модифицированная дюрация облигации {{$jsapi.context().session.bondData.modifiedDuration.toFixed(2)}}"
                : "Мы не нашли модифицированную дюрацию по данной облигации"
            break;
        case 'Номинал':
            answer = $jsapi.context().session.bondData.nominalPrice > 0
                ? "Номинал облигации {{Math.round($jsapi.context().session.bondData.nominalPrice)}}"
                : "Мы не нашли номинал по данной облигации"
            break;
        case 'Частота выплат в год':
            answer = $jsapi.context().session.bondData.couponFrequency == 1 
                ? "Выплаты по купону осществляются {{$jsapi.context().session.bondData.couponFrequency}} раз в год"
                : ($jsapi.context().session.bondData.couponFrequency > 1 
                ? "Выплаты по купону осществляются {{$jsapi.context().session.bondData.couponFrequency}} раза в год" 
                : "Мы не нашли частота выплат в год по данной облигации")
            break;
        case 'Купон':
            answer = $jsapi.context().session.bondData.couponValue > 0 
                ? "Купон по облигации составляет {{$jsapi.context().session.bondData.couponValue}} {{$jsapi.context().session.bondData.currency}}"
                : "Мы не нашли купон по данной облигации"
            break;
        case 'Ставка купона':
            answer = $jsapi.context().session.bondData.couponPercent > 0 
                ? "Ставка купона по облигации составляет {{($jsapi.context().session.bondData.couponPercent * 100).toFixed(2)}}%"
                : "Мы не нашли ставку купона по данной облигации"
            break
        case 'Валюта':
            answer = $jsapi.context().session.bondData.currency 
                ? "Валюта облигации {{$jsapi.context().session.bondData.currency}}"
                : "Мы не нашли валюту по данной облигации"
            break;
        case 'Дата начала торгов':
            answer = "Дата начала торгов {{moment($jsapi.context().session.bondData.issueDate).format(\"DD.MM.YYYY\")}}"
            break;
        case 'Объем выпуска':
            answer = $jsapi.context().session.bondData.issueSize > 0 
                ? "Объем выпуска {{$jsapi.context().session.bondData.issueSize}}"
                : "Мы не нашли объем выпуска по данной облигации"
            break;
        case 'Дата погашения':
            answer = "Дата погашения облигации {{moment($jsapi.context().session.bondData.matDate).format(\"DD.MM.YYYY\")}}"
            break;
        case 'Следующая дата выплаты купона':
            answer = "Следующая дата выплаты купона {{moment($jsapi.context().session.bondData.nextCoupon).format(\"DD.MM.YYYY\")}}"
            break;
        case 'НКД':
            answer = $jsapi.context().session.bondData.accruedint > 0
                ? "НКД по облигации {{$jsapi.context().session.bondData.accruedint}}"
                : "Мы не нашли НКД по данной облигации"
            break;
        case 'Дюрация Маколея':
            answer = $jsapi.context().session.bondData.macaulayDuration > 0 
                ? "Дюрация Маколея по облигации составляет {{$jsapi.context().session.bondData.macaulayDuration.toFixed(2)}}"
                : "Мы не нашли дюрацию Маколея по данной облигации"
            break;
        case 'Выпуклость облигации':
            answer = $jsapi.context().session.bondData.convexity > 0
                ? "Выпуклость по облигации составляет {{$jsapi.context().session.bondData.convexity.toFixed(2)}}"
                : "Мы не нашли выпуклость по данной облигации"
            break;
        case 'Рейтинг':
            answer = $jsapi.context().session.ratingData.length > 0 
                ? "Рейтинг облигации {{$jsapi.context().session.ratingData[0].rating}}"
                : "Мы не нашли рейтинг по данной облигации"
            break;
    }
    return answer;
}
