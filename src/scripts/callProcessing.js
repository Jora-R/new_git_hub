function callProcessing(phoneNumber, response, client){
    
    // response.replies = response.replies || [];
    response.replies.push({
        type: "switch",
        sipUri: phoneNumber + "@10.77.102.30",
        timeout: "30",
        headers: {
            "callReason": "support",
            "crmClientId": client.id || "none"
        },
        transferChannel: "1000002-voicebot_prod-1000002-CqG-523",
        continueCall: false,
        continueRecording: false
    });
}
