// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import {createRouter, createWebHashHistory, RouteRecordRaw} from 'vue-router'
import HomeView from '../views/HomeView.vue'


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
        component: () => import(/!* webpackChunkName: "about" *!/ '../views/detail/ObjectView.vue'),
    },*/
    {
        path: '/index',
        name: 'index',
        component: () => import(/* webpackChunkName: "about" */ '../views/MainIndex.vue'),
        children: [
            {
                path: 'about',
                component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue'),
                meta: {
                    standalone: true
                }
            },
            {
                path: 'detail',
                component: () => import(/* webpackChunkName: "about" */ '../views/entity/object/ObjectView.vue'),
                meta: {
                    standalone: true
                }
            },
            {
                path: 'object-query',
                component: () => import(/* webpackChunkName: "about" */ '../views/query/ObjectQueryView.vue'),
                meta: {
                    standalone: true
                }
            },
            {
                path: 'table-define-query',
                component: () => import(/* webpackChunkName: "about" */ '../views/entity/table/TableQueryView.vue'),
                meta: {
                    standalone: true
                }
            },
            {
                path: 'table-config/:id(\\d+)',
                component: () => import(/* webpackChunkName: "about" */ '../views/entity/table/TableConfigView.vue'),
                meta: {
                    standalone: true
                }
            },
        ]
    }
]

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

export default router
