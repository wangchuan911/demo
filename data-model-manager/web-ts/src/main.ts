import {createApp} from 'vue'
import App from '@/App.vue'
import Http from "@/config/Http";
import router from '@/router'
import Import from "@/components/Import";
import TabView from "@/config/TabView";

const app = createApp(App).use(router)
Http.config(app)
Import.config(app)
TabView.config(app, router)
app.mount('#app')
