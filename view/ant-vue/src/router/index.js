import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import mainView from '@/components/base/mainPage'
import loginPage from '@/components/base/loginPage'
import testVeiw from '@/components/test'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
      path: '/main',
      name: 'main',
      component: mainView
    },
    {
      path: '/login',
      name: 'login',
      component: loginPage
    },
    {
      path: '/test',
      name: 'test',
      component: testVeiw
    }
  ]
})
