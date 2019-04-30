<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header>
      <a-row>
        <a-col :span="2">
          <a-row type="flex" justify="center" align="middle">
            <a-col :span="12">
              <img class="ant-col-24" src="../../assets/logo.png" style="height: 100%"/></a-col>
          </a-row>
        </a-col>
        <a-col :span="22">
          <header-page></header-page>
        </a-col>
      </a-row>
      <a-divider></a-divider>
    </a-layout-header>
    <a-layout-content>
      <a-form
        :form="form"
        @submit="handleSubmit"
      >
        <a-form-item
          label="Login Code" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
          <a-input
            v-decorator="['note',{rules: [{ required: true, message: 'Please input your User Name | Phone Number | E-mail!' }]}        ]"/>
        </a-form-item>
        <a-form-item label="Password" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
          <a-input
            v-decorator="['password',{  rules: [{    required: true, message: 'Please input your password!',  }, {    validator: validateToNextPassword,  }],}        ]"
            type="password"/>
        </a-form-item>
        <a-form-item
          :wrapper-col="{ span: 12, offset: 5 }"
        >
          <a-button
            type="primary"
            html-type="submit"
          >
            Submit
          </a-button>
        </a-form-item>
      </a-form>
    </a-layout-content>
    <a-layout-footer :style="{ textAlign: 'center' }">
      Ant Design ©2018 Created by Ant UED
    </a-layout-footer>
  </a-layout>
</template>

<script>
import ALayoutSider from 'ant-design-vue/es/layout/Sider'
import Header from 'ant-design-vue/es/calendar/Header'
import HeaderPage from './headerPage'
import ARow from 'ant-design-vue/es/grid/Row'
import ACol from 'ant-design-vue/es/grid/Col'
import AFormItem from 'ant-design-vue/es/form/FormItem'
export default {
  name: 'loginPage',
  components: {AFormItem, ACol, ARow, HeaderPage, Header, ALayoutSider}
}
</script>

<style scoped>
  div .ant-layout-header{
   background: #fff;
   padding: 0;
 }
  .ant-divider,.ant-divider-horizontal{
    margin:0px
  }
</style>
