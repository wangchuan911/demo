// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import {createRouter, createWebHashHistory, RouteMeta, RouteRecordRaw} from 'vue-router'
import HomeView from '../views/HomeView.vue'

const tabsPageState: Record<string, TabState> = {}

export declare interface TabState {
    using: Record<string, string>,
    path: string,
    max: number
}

function tabsPage(parents: Array<RouteRecordRaw>, routes: Array<RouteRecordRaw>): void {
    for (let i = 0, len = routes.length; i < len; i++) {
        const row: RouteRecordRaw = routes[i];
        const tabsCountMax = row?.meta?.tabsCountMax as number;
        if (tabsCountMax > 0) {
            console.log(row);
            const arr: Array<RouteRecordRaw> = [];
            for (let j = 0; j < tabsCountMax; j++) {
                arr.push({
                    name: row.name,
                    component: row.component,
                    path: `${j}/${row.path}`,
                    meta: {...row.meta, tabsCountMax: -1, standalone: false}
                } as RouteRecordRaw)
            }

            if (parents.length == 0) {
                routes.push(...arr)
            } else {
                parents[parents.length - 1].children?.push(...arr);
            }
            const prefix = parents.map(value => {
                let text = value.path;
                if (text.endsWith("/")) {
                    text = text.substring(0, text.length - 1);
                }
                if (text.startsWith("/")) {
                    text = text.substring(1, text.length);
                }
                return text;
            }).join("/")
            tabsPageState[`${prefix}/${row.path}`] = {
                path: `${prefix}/{{count}}/${row.path}`,
                using: {},
                max: tabsCountMax
            } as TabState
        } else if (row.children != null) {
            parents.push(row);
            tabsPage(parents, row.children);
        }
    }
}

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'home',
        component: HomeView
    },
    /*{
        path: '/about',
        name: 'about',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/!* webpackChunkName: "about" *!/ '../views/AboutView.vue'),
    },
    {
        path: '/detail',
        name: 'detail',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/!* webpackChunkName: "about" *!/ '../views/detail/ObjectDetailView.vue'),
    },*/
    {
        path: '/index',
        name: 'index',
        component: () => import( '../views/MainIndex.vue'),
        children: [
            {
                path: 'about',
                component: () => import( '../views/AboutView.vue'),
                meta: {
                    standalone: true
                }
            },
            {
                path: 'object-detail/:id(\\d+)',
                component: () => import( '../views/entity/object/ObjectDetailView.vue'),
                meta: {
                    standalone: true,
                    tabsCountMax: 20
                }
            }
            ,
            {
                path: 'object-query',
                component: () => import( '../views/entity/object/ObjectQueryView.vue'),
                meta: {
                    standalone: true
                }
            },
            /*{
                path: 'table-define-query',
                component: () => import(/!* webpackChunkName: "about" *!/ '../views/entity/table/TableQueryView.vue'),
                meta: {
                    standalone: true
                }
            },
            {
                path: 'table-config/:id(\\d+)',
                component: () => import(/!* webpackChunkName: "about" *!/ '../views/entity/table/TableConfigView.vue'),
                meta: {
                    standalone: true
                }
            },*/
        ]
    }
]

tabsPage([], routes)
routes.filter(value => (value?.children || []).length > 0)
    .forEach(value1 => {
        (value1?.children || [])
            .filter(value => value?.meta?.standalone)
            .forEach(value => {
                if (value.path.startsWith("/"))
                    return
                const obj: RouteRecordRaw = {...value}
                obj.path = `/${obj.path}`
                routes.push(obj)
            })
    })

console.log(routes)
const router = createRouter({
    history: createWebHashHistory(),
    routes
})
console.log(tabsPageState)
export default {
    router,
    tabsPageState
}
