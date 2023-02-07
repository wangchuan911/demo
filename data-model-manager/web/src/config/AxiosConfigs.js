import axios from "axios"

const Api = axios.create({
    baseURL: process.env.VUE_APP_URL,
    timeout: 30000,
})

export default Api