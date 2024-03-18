async function postData (url = "", data = {}) {
    try {
        const response = await fetch (url, {
        method: "POST",
        headers: {
            "X-ApiKey": $injector.api_key_callCRM,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });
    return await response;
    } catch (error) { 
        log (error);
        return error;
    }
}

export default { postData }