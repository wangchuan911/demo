import axios, {AxiosError} from "axios";
import {AxiosInstance} from "axios";
import {App} from "@vue/runtime-core";
import {ElMessage, ElMessageBox} from "element-plus";

import 'element-plus/es/components/message/style/css';
import 'element-plus/es/components/message-box/style/css';

axios.defaults.baseURL = "/dev/dmm";
axios.defaults.timeout = 30000;
axios.interceptors.response.use((response) => {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    return response;
}, (error) => {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    if (error instanceof AxiosError)
        ElMessage.error(error.response?.data || error);
    else
        ElMessage.error(error);
    return Promise.reject(error);
});
declare module '@vue/runtime-core' {
    interface ComponentCustomProperties {
        $http: AxiosInstance;
    }
}

export default {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    config: (app: App) => {
        app.config.globalProperties.$http = axios;
    }
};