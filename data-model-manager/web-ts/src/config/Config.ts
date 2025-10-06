import Http from "@/config/Http";
import {App} from "@vue/runtime-core";
import Import from "@/components/Import";
import Directives from "@/config/Directives";


export default {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    config: (app: App) => {
        [Http, Import, Directives].forEach(value => value.config(app));
    }
};