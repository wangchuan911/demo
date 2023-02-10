import axios from "axios"

axios.defaults.baseURL = "/api"
axios.defaults.timeout = 30000

export default axios