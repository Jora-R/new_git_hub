function getBondsData(url){
    
    var response = $http.query(url, {
        method: "GET",
        timeout: 15000
    });
    
    var responseError = {code: "default"}
    
    try{
        responseError = JSON.parse(response.error);
    }catch (error) {
    }
    

    if (response.isOk){
        return response.data;
        }
    else {
        return false;
    }
}