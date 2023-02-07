import {createApp} from 'vue'
import App from './App.vue'
import Http from "@/config/Http";
import router from './router'

const app = createApp(App).use(router)
Http.config(app)
app.mount('#app')
