import {App} from "@vue/runtime-core";

import ElementPlus from 'element-plus'

export default {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    config(app: App) {
        app.use(ElementPlus)
    }
}