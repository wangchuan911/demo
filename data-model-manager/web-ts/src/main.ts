import {createApp} from 'vue'
import App from '@/App.vue'
import Config from "@/config/Config";
import router from '@/router'

console.log(router)
const app = createApp(App).use(router.router)
Config.config(app)
app.mount('#app')
