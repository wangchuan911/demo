import {createApp} from 'vue'
import App from './App.vue'
import axios from "./config/AxiosConfigs";

const app = createApp(App)
app.config.globalProperties.$http = axios
app.mount('#app')
