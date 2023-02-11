import {RouteComponent, Router} from "vue-router";
import {App} from "@vue/runtime-core";
import {AxiosInstance} from "axios";

export declare interface TabInfo {
    title: string
    name: string
    path?: string
    component?: RouteComponent
}


export declare interface TabController {
    open(url: string, params: object): void;

    getTabs(): TabInfo[]
}

declare module '@vue/runtime-core' {
    interface ComponentCustomProperties {
        $tab: TabController;
    }
}

export default {
    config: (app: App, router: Router): TabController => {
        const tabComponents: TabInfo[] = [];
        router.getRoutes()
            .filter(value => value?.meta?.tab != null)
            .forEach(async value => {
                async function _get(value: any) {
                    if (typeof (value) == "function")
                        return value()
                    return value
                }

                const tab: any = value?.meta?.tab || {};
                tabComponents.push({
                    name: value.path,
                    component: (await _get(value.components?.default)).default,
                    ...tab
                })
            });
        const controller: TabController = {
            // eslint-disable-next-line @typescript-eslint/no-empty-function

            open(url: string, params: object): void {
                console.log(1)
                return
            },
            getTabs(): TabInfo[] {
                return tabComponents;
            }
        };
        app.config.globalProperties.$tab = controller
        return controller
    }
}