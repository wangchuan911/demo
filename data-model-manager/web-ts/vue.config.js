const {defineConfig} = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    configureWebpack: {
        devServer: {
            proxy: {
                "/dev": {
                    target: 'http://localhost:8433/',
                    pathRewrite: {'^/dev': ''},
                    changeOrigin: true,     // target是域名的话，需要这个参数，
                    secure: false,          // 设置支持https协议的代理
                }
            }
        }
    },
})
