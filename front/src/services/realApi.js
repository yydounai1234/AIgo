const BASE_URL = 'http://localhost:8080'

const getAuthToken = () => {
  return localStorage.getItem('token')
}

const request = async (endpoint, options = {}) => {
  const url = `${BASE_URL}${endpoint}`
  const token = getAuthToken()
  
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    ...options.headers
  }
  
  if (token && !options.skipAuth) {
    headers['Authorization'] = `Bearer ${token}`
  }
  
  try {
    const response = await fetch(url, {
      ...options,
      headers
    })
    
    const data = await response.json()
    
    if (!response.ok) {
      return data
    }
    
    return data
  } catch (error) {
    return {
      success: false,
      error: {
        code: 'NETWORK_ERROR',
        message: error.message || '网络请求失败'
      }
    }
  }
}

export const realApi = {
  async register(username, email, password) {
    return await request('/api/auth/register', {
      method: 'POST',
      skipAuth: true,
      body: JSON.stringify({ username, email, password })
    })
  },

  async login(username, password) {
    return await request('/api/auth/login', {
      method: 'POST',
      skipAuth: true,
      body: JSON.stringify({ username, password })
    })
  },

  async getUserBalance() {
    return await request('/api/user/balance')
  },

  async createWork(workData) {
    return await request('/api/works', {
      method: 'POST',
      body: JSON.stringify(workData)
    })
  },

  async getWork(workId) {
    return await request(`/api/works/${workId}`)
  },

  async updateWork(workId, updates) {
    return await request(`/api/works/${workId}`, {
      method: 'PUT',
      body: JSON.stringify(updates)
    })
  },

  async deleteWork(workId) {
    return await request(`/api/works/${workId}`, {
      method: 'DELETE'
    })
  },

  async createEpisode(workId, episodeData) {
    return await request(`/api/works/${workId}/episodes`, {
      method: 'POST',
      body: JSON.stringify(episodeData)
    })
  },

  async getEpisode(episodeId) {
    return await request(`/api/episodes/${episodeId}`)
  },

  async updateEpisode(episodeId, updates) {
    return await request(`/api/episodes/${episodeId}`, {
      method: 'PUT',
      body: JSON.stringify(updates)
    })
  },

  async publishEpisode(episodeId) {
    return await request(`/api/episodes/${episodeId}/publish`, {
      method: 'POST'
    })
  },

  async getMyWorks() {
    return await request('/api/my-works')
  },

  async getGallery(options = {}) {
    const { sortBy = 'latest' } = options
    return await request(`/api/gallery?sortBy=${sortBy}`)
  },

  async purchaseEpisode(episodeId) {
    return await request(`/api/episodes/${episodeId}/purchase`, {
      method: 'POST'
    })
  },

  async likeWork(workId) {
    return await request(`/api/works/${workId}/like`, {
      method: 'POST'
    })
  },

  async unlikeWork(workId) {
    return await request(`/api/works/${workId}/like`, {
      method: 'DELETE'
    })
  },

  async retryEpisode(episodeId) {
    return await request(`/api/episodes/${episodeId}/retry`, {
      method: 'POST'
    })
  },

  async getWorkCharacters(workId) {
    return await request(`/api/characters/work/${workId}`)
  },

  async updateCharacter(characterId, updates) {
    return await request(`/api/characters/${characterId}`, {
      method: 'PUT',
      body: JSON.stringify(updates)
    })
  }
}
