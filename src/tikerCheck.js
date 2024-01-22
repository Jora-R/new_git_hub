function getExchangeVariable (query){
    var exchangeTiker = '';
    switch(query){
        case 'Московская биржа':
            exchangeTiker = "misx"
            break;
        case 'Биржа СПБ':
            exchangeTiker = 'rusx'
            break;
        case 'Гонкогская биржа HKEX':
            exchangeTiker = 'hnkg'
            break;
        case 'NYSE':
            exchangeTiker = 'arcx'
            break;
        case 'NASDAQ':
            exchangeTiker = 'xnms'
            break;
        case 'USA OTC':
            exchangeTiker = 'pinx'
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