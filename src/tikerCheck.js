function getExchangeVariable (query){
    var exchangeTiker = '';
    switch(query){
        case 'Московская биржа':
            exchangeTiker = ["MISX","RTSX"];
            break;
        case 'Биржа СПБ':
            exchangeTiker = ["RUSX"];
            break;
        case 'Гонкогская биржа HKEX':
            exchangeTiker = ["XHKG"];
            break;
        case 'NYSE':
            exchangeTiker = ["ARCX","XNYS","XASE"];
            break;
        case 'NASDAQ':
            exchangeTiker = ["XNMS","XNGS","XNCM"];
            break;
        case 'USA OTC':
            exchangeTiker = ['PINX'];
            break;
    } 
    return exchangeTiker;
}

function getExchangeName (query){
    var exchangeName = '';
    switch(query){
        case 'Московская биржа':
            exchangeName = "Московской бирже"
            break;
        case 'Биржа СПБ':
            exchangeName = "Бирже СПБ"
            break;
        case 'Гонконгская биржа HKEX':
            exchangeName = 'Гонконгской бирже'
            break;
        case 'NYSE':
            exchangeName = 'NYSEe'
            break;
        case 'NASDAQ':
            exchangeName = 'NASDAQ'
            break;
        case 'USA OTC':
            exchangeTiker = 'USA OTC'
            break;
    }
    return exchangeName;
}