window.$http = (() => {
    const http = (method, url, data, options) => {
        return new Promise((resolve, reject) => {
            var xhr = new XMLHttpRequest();
            //使用HTTP POST请求与服务器交互数据
            if (["GET"].indexOf(method) >= 0) {
                const nodata = url.indexOf("?") < 0
                Object.keys(data).forEach((value, index) => {
                    url += `${nodata && index == 0 ? "?" : "&"}${value}=${data[value]}`;
                })
            }
            xhr.open(method, url, true);
            //设置发送数据的请求格式
            xhr.setRequestHeader('content-type', 'application/json');
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    //根据服务器的响应内容格式处理响应结果
                    if (xhr.getResponseHeader('content-type') === 'application/json') {
                        resolve(JSON.parse(xhr.responseText));
                    } else {
                        resolve(xhr.responseText);
                    }
                } else {
                    reject({state: xhr.readyState, data: xhr.responseText})
                }
            }
            //将用户输入值序列化成字符串
            xhr.send(["POST"].indexOf(method) >= 0 ? JSON.stringify(data) : null);
        })
    }
    const obj = {};
    ["post", "get"].forEach(value => {
        obj[value] = (url, data, option) => http(value.toUpperCase(), url, data, option)
    });
    return obj;
})()