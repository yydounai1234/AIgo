import { mockApi } from './mockApi'
import { realApi } from './realApi'

const USE_MOCK = false

const api = USE_MOCK ? mockApi : realApi

export default api
