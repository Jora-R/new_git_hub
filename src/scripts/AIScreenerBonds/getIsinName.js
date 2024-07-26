function dateConverter(matDate){
    moment.locale("ru")
    var date = moment(matDate, "DD.MM.YYYY", true)
    if(!date.isValid()){
        if (matDate.includes(".")){
            date = moment(matDate, "MM.YYYY" ).format("MM.YYYY")
        } else { 
            date = moment(matDate, "MMMM YYYY", 'ru').format("MM.YYYY")
        }
    }
    return date;
}

function getIsinName(data, name, date){
    var inputDate = moment(date, ["DD.MM.YYYY", "MM.YYYY", "MMMM YYYY"], true)

    return data.filter(item => {
        var itemDate = moment(item.MATDATE, "DD.MM.YYYY", true)
        var nameMatch = item.EMITENTNAME.toLowerCase().includes(name.toLowerCase()) || item.name.toLowerCase().includes(name.toLowerCase())
        if (inputDate.isValid() && nameMatch){
            if(date.length === 10){
                return inputDate.isSame(itemDate,"day")
            } else {
                return inputDate.isSame(itemDate,"month") && inputDate.isSame(itemDate,"year")
            }
        }
        return false
        })
}


export default { getIsinName, dateConverter }