import { mockApi } from './mockApi'

const USE_MOCK = true

const api = USE_MOCK ? mockApi : null

export default api
