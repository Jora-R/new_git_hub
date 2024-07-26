function isQualified(authenticationId){
    
    // $reactions.answer("Проверка на наличие статуса квала");
    
    var response = $http.query($jsapi.context().injector.url_edox_personal, {
        method: "GET",
        headers: {
            "Authorization": $jsapi.context().injector.token_edox_userinfo,
            "Accept": "application/json",
            "x-authentication-Id": authenticationId
        },
        timeout: 1000
    });
    
     
    if (response.isOk){
        if (response.data.clientFirmsInfo[0].investorStatus.id == "QUALIFIED"){
            return true;
        }
        if (response.data.clientFirmsInfo[0].investorStatus.id == "NON_QUALIFIED"){
            return false;
        }
        
    } else {
        return "error";
    }
    return response.data;
}