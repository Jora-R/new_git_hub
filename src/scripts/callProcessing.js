function callProcessing(phoneNumber){
    var phoneNumberFinal = segment(phoneNumber);
    $jsapi.context().response.replies.push({
        type: "switch",
        phoneNumber: phoneNumberFinal,
        timeout: "30",
        method: "refer"
    });
}