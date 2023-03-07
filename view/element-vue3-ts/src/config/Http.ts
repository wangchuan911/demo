import axios from "axios";
import {AxiosInstance} from "axios";
import {App} from "@vue/runtime-core";

axios.defaults.baseURL = "/dev/dmm"
axios.defaults.timeout = 30000

declare module '@vue/runtime-core' {
    interface ComponentCustomProperties {
        $http: AxiosInstance;
    }
}

export default {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    config: (app: App) => {
        app.config.globalProperties.$http = axios
    }
}