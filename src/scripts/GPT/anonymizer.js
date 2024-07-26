function anonymizer() {
    //Затираение клиентских личных данных с помощью регулярных выражений
    
    var result;

    if($jsapi.context().request.query === undefined) {
        return false;
    }
    
    var regex1 = /\b(?:КЛФ|КЛФ-|КЛФ |КВФ|КВФ-|КВФ |КЛЮ|КЛЮ-|КЛЮ |БКЛФ|БКЛФ-|БКЛФ |БКЛЮ|БКЛЮ-|БКЛЮ |КЛФ-ИИС|КЛФ-ИИС-|КЛФ-ИИС )\d{7}\b/gi;
    var regex2 = /\b(?!(RU|US|XC|NL))(?=[A-Za-z0-9]*[A-Za-z][A-Za-z0-9]*[0-9]|[A-Za-z0-9]*[0-9][A-Za-z0-9]*[A-Za-z])[A-Za-z0-9]{5,6}[/\\][A-Za-z0-9]{5,6}\b|\b(?!(RU|US|XC))(?=[A-Za-z0-9]*[A-Za-z][A-Za-z0-9]*[0-9]|[A-Za-z0-9]*[0-9][A-Za-z0-9]*[A-Za-z])[A-Za-z0-9]{11}\b/gi;
    var regex3 = /(?:\+|\d)[\d\-\(\) ]{8,}\d/g;
    var regex4 = /([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\.[a-zA-Z0-9_-]+)/gi;
    var regex5 = /\x20*(\d{10,12})/gi;
    var regex6 = /[0-9]{3}([-]{0,1})?([0-9]{0,3})?([-]{0,1})?([0-9]{0,3})?([-]{0,1})?([0-9]{0,2})?( [0-9]{2,3})?([-]{0,1})?([0-9]{0,3})?( [0-9]{2,3})?([-]{0,1})?([0-9]{0,2})?( [0-9]{2})?|(\x20*(\d{10}))/gi;
    var regex7 = /[0-9]{2}[-]{1}[0-9]{17}[А-Я]{1}/gi;
    var regex8 = /\b\d{2}[A-Za-zА-Яа-яЁё]{1,2}[/\\ ]\d{4}[- ](?:\d{7}|\d{4}[/\\ ]\d{1})\b|\b\d{2,5}[/\\ ][A-Za-zА-Яа-яЁё]{1,2}[/\\ ]\d{2,4}/g;


    // В таком порядке регулярки меньше пересекаются
    result = $jsapi.context().request.query.replace(regex1,' ***** '); //номер счета КЛФ
    result = result.replace(regex2, ' ***** '); //торговый код
    result = result.replace(regex8, ' ***** '); //номер договора + номер договора ДЕПО
    result = result.replace(regex7, ' ***** '); //номер счета ДЕПО
    result = result.replace(regex3, ' ***** '); //номер телефона
    result = result.replace(regex4, ' ***** '); //почта
    result = result.replace(regex5, ' ***** '); //ИНН
    result = result.replace(regex6, ' ***** '); //СНИЛС
    
    
    $jsapi.context().request.query = result;
    return;
    
}