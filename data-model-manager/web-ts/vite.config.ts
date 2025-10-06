import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import {fileURLToPath, URL} from 'node:url';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers';

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        AutoImport({
            resolvers: [ElementPlusResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver()],
        })],
    resolve: {
        extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    build: {
        commonjsOptions: {
            esmExternals: true,
        },
    },
    server: {
        proxy: {
            "/dev/dmm": {
                target: 'http://localhost:8443',
                rewrite: (path) => {
                    const path2 = path.replace(/^\/dev\/dmm\//, '/md/');
                    return path2;
                },
                changeOrigin: true,     // target是域名的话，需要这个参数，
            }
        }
    },
});
