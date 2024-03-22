function callProcessing(phoneNumber, response, client){
    
    response.replies.push({
        type: "switch",
        phoneNumber: phoneNumber,
        timeout: "30",
        // headers: {
        //     "callReason": "support",
        //     "crmClientId": client.id || "none"
        // },
        // transferChannel: "1000002-voicebot_prod-1000002-CqG-523",
        // continueCall: false,
        // continueRecording: false,
        method: "refer"
    });
}