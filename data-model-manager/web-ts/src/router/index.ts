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
    {
        path: '/about',
        name: 'about',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue'),
        meta: {
            tab: {
                title: '首页'
            }
        }
    },
    {
        path: '/detail',
        name: 'detail',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/* webpackChunkName: "about" */ '../views/detail/DetailView.vue'),
        meta: {
            tab: {
                title: '详情'
            }
        }
    },
    {
        path: '/index',
        name: 'index',
        component: () => import(/* webpackChunkName: "about" */ '../views/MainIndex.vue')
    }
]
console.log(routes)
const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router
