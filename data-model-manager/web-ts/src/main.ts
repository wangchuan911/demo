import {createApp} from 'vue'
import App from '@/App.vue'
import Http from "@/config/Http";
import router from '@/router'
import Import from "@/components/Import";

const app = createApp(App).use(router)
Http.config(app)
Import.config(app)
app.mount('#app')
