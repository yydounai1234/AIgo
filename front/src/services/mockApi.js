import { mockData } from './mockData'

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

export const mockApi = {
  async login(username, password) {
    await delay(500)
    if (username && password) {
      const user = mockData.getCurrentUser()
      return {
        success: true,
        data: { user, token: user.token }
      }
    }
    return {
      success: false,
      error: { message: '用户名或密码错误', code: 'AUTH_FAILED' }
    }
  },

  async register(username, email, password) {
    await delay(500)
    const user = {
      id: '1',
      username,
      email,
      coinBalance: 100,
      token: 'mock-jwt-token-new'
    }
    mockData.setCurrentUser(user)
    return {
      success: true,
      data: { user, token: user.token }
    }
  },

  async getUserBalance() {
    await delay(300)
    const user = mockData.getCurrentUser()
    return {
      success: true,
      data: { balance: user.coinBalance }
    }
  },

  async createWork(workData) {
    await delay(500)
    const work = mockData.addWork(workData)
    return {
      success: true,
      data: work
    }
  },

  async getWork(workId) {
    await delay(300)
    const work = mockData.getWork(workId)
    if (work) {
      return {
        success: true,
        data: work
      }
    }
    return {
      success: false,
      error: { message: '作品不存在', code: 'NOT_FOUND' }
    }
  },

  async updateWork(workId, updates) {
    await delay(400)
    const user = mockData.getCurrentUser()
    const work = mockData.getWork(workId)
    
    if (!work) {
      return {
        success: false,
        error: { message: '作品不存在', code: 'NOT_FOUND' }
      }
    }
    
    if (work.userId !== user.id) {
      return {
        success: false,
        error: { message: '无权限修改此作品', code: 'FORBIDDEN' }
      }
    }
    
    const updated = mockData.updateWork(workId, updates)
    return {
      success: true,
      data: updated
    }
  },

  async deleteWork(workId) {
    await delay(400)
    const user = mockData.getCurrentUser()
    const work = mockData.getWork(workId)
    
    if (!work) {
      return {
        success: false,
        error: { message: '作品不存在', code: 'NOT_FOUND' }
      }
    }
    
    if (work.userId !== user.id) {
      return {
        success: false,
        error: { message: '无权限删除此作品', code: 'FORBIDDEN' }
      }
    }
    
    mockData.deleteWork(workId)
    return {
      success: true,
      data: { message: '删除成功' }
    }
  },

  async createEpisode(workId, episodeData) {
    await delay(500)
    const user = mockData.getCurrentUser()
    const work = mockData.getWork(workId)
    
    if (!work) {
      return {
        success: false,
        error: { message: '作品不存在', code: 'NOT_FOUND' }
      }
    }
    
    if (work.userId !== user.id) {
      return {
        success: false,
        error: { message: '无权限创建集数', code: 'FORBIDDEN' }
      }
    }
    
    const episode = mockData.addEpisode({ ...episodeData, workId })
    return {
      success: true,
      data: episode
    }
  },

  async getEpisode(episodeId) {
    await delay(300)
    const episode = mockData.getEpisode(episodeId)
    
    if (!episode) {
      return {
        success: false,
        error: { message: '集数不存在', code: 'NOT_FOUND' }
      }
    }
    
    if (!episode.isPublished) {
      const user = mockData.getCurrentUser()
      const work = mockData.getWork(episode.workId)
      if (!work || work.userId !== user.id) {
        return {
          success: false,
          error: { message: '集数未发布', code: 'NOT_PUBLISHED' }
        }
      }
    }
    
    if (!episode.isFree && episode.coinPrice > 0) {
      const hasPurchased = mockData.hasPurchased(episodeId)
      if (!hasPurchased) {
        const user = mockData.getCurrentUser()
        const work = mockData.getWork(episode.workId)
        if (!work || work.userId !== user.id) {
          return {
            success: false,
            needsPurchase: true,
            data: {
              id: episode.id,
              episodeId: episode.id,
              episodeNumber: episode.episodeNumber,
              title: episode.title,
              coinPrice: episode.coinPrice,
              workId: episode.workId,
              isFree: episode.isFree
            }
          }
        }
      }
    }
    
    return {
      success: true,
      data: episode
    }
  },

  async updateEpisode(episodeId, updates) {
    await delay(400)
    const user = mockData.getCurrentUser()
    const episode = mockData.getEpisode(episodeId)
    
    if (!episode) {
      return {
        success: false,
        error: { message: '集数不存在', code: 'NOT_FOUND' }
      }
    }
    
    const work = mockData.getWork(episode.workId)
    if (!work || work.userId !== user.id) {
      return {
        success: false,
        error: { message: '无权限修改此集数', code: 'FORBIDDEN' }
      }
    }
    
    if (episode.isPublished) {
      return {
        success: false,
        error: { message: '已发布的集数不可修改', code: 'ALREADY_PUBLISHED' }
      }
    }
    
    const updated = mockData.updateEpisode(episodeId, updates)
    return {
      success: true,
      data: updated
    }
  },

  async publishEpisode(episodeId) {
    await delay(400)
    const user = mockData.getCurrentUser()
    const episode = mockData.getEpisode(episodeId)
    
    if (!episode) {
      return {
        success: false,
        error: { message: '集数不存在', code: 'NOT_FOUND' }
      }
    }
    
    const work = mockData.getWork(episode.workId)
    if (!work || work.userId !== user.id) {
      return {
        success: false,
        error: { message: '无权限发布此集数', code: 'FORBIDDEN' }
      }
    }
    
    if (episode.isPublished) {
      return {
        success: false,
        error: { message: '集数已发布', code: 'ALREADY_PUBLISHED' }
      }
    }
    
    const published = mockData.publishEpisode(episodeId)
    return {
      success: true,
      data: published
    }
  },

  async getMyWorks() {
    await delay(300)
    const user = mockData.getCurrentUser()
    const allWorks = mockData.getWorks()
    const myWorks = allWorks.filter(w => w.userId === user.id)
    
    const worksWithEpisodes = myWorks.map(work => ({
      ...work,
      episodes: mockData.getEpisodes(work.id)
    }))
    
    return {
      success: true,
      data: worksWithEpisodes
    }
  },

  async getGallery(options = {}) {
    await delay(300)
    const { sortBy = 'latest' } = options
    const allWorks = mockData.getWorks()
    const publicWorks = allWorks.filter(w => w.isPublic)
    
    let sorted = [...publicWorks]
    if (sortBy === 'likes') {
      sorted.sort((a, b) => (b.likesCount || 0) - (a.likesCount || 0))
    } else {
      sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    }
    
    const user = mockData.getCurrentUser()
    const worksWithLikeStatus = sorted.map(work => ({
      ...work,
      isLiked: mockData.hasLiked(work.id),
      episodeCount: mockData.getEpisodes(work.id).filter(e => e.isPublished).length
    }))
    
    return {
      success: true,
      data: worksWithLikeStatus
    }
  },

  async purchaseEpisode(episodeId) {
    await delay(500)
    const episode = mockData.getEpisode(episodeId)
    
    if (!episode) {
      return {
        success: false,
        error: { message: '集数不存在', code: 'NOT_FOUND' }
      }
    }
    
    if (episode.isFree) {
      return {
        success: false,
        error: { message: '该集数为免费内容', code: 'ALREADY_FREE' }
      }
    }
    
    const user = mockData.getCurrentUser()
    if (mockData.hasPurchased(episodeId)) {
      return {
        success: false,
        error: { message: '您已购买过此集数', code: 'ALREADY_PURCHASED' }
      }
    }
    
    if (user.coinBalance < episode.coinPrice) {
      return {
        success: false,
        error: { 
          message: `金币不足，需要 ${episode.coinPrice} 金币，当前余额 ${user.coinBalance}`,
          code: 'INSUFFICIENT_COINS'
        }
      }
    }
    
    const result = mockData.purchaseEpisode(episodeId, episode.coinPrice)
    if (result) {
      return {
        success: true,
        data: {
          episodeId,
          coinCost: episode.coinPrice,
          newBalance: result.newBalance
        }
      }
    }
    
    return {
      success: false,
      error: { message: '购买失败', code: 'PURCHASE_FAILED' }
    }
  },

  async likeWork(workId) {
    await delay(300)
    const work = mockData.getWork(workId)
    
    if (!work) {
      return {
        success: false,
        error: { message: '作品不存在', code: 'NOT_FOUND' }
      }
    }
    
    if (mockData.hasLiked(workId)) {
      return {
        success: false,
        error: { message: '您已点赞过此作品', code: 'ALREADY_LIKED' }
      }
    }
    
    mockData.likeWork(workId)
    return {
      success: true,
      data: { message: '点赞成功' }
    }
  },

  async unlikeWork(workId) {
    await delay(300)
    const work = mockData.getWork(workId)
    
    if (!work) {
      return {
        success: false,
        error: { message: '作品不存在', code: 'NOT_FOUND' }
      }
    }
    
    if (!mockData.hasLiked(workId)) {
      return {
        success: false,
        error: { message: '您未点赞过此作品', code: 'NOT_LIKED' }
      }
    }
    
    mockData.unlikeWork(workId)
    return {
      success: true,
      data: { message: '取消点赞成功' }
    }
  },

  async rechargeCoins(amount) {
    await delay(500)
    
    if (!amount || amount <= 0) {
      return {
        success: false,
        error: { message: '充值金额必须大于0', code: 'INVALID_AMOUNT' }
      }
    }
    
    if (amount > 1000) {
      return {
        success: false,
        error: { message: '单次充值金额不能超过1000金币', code: 'AMOUNT_EXCEEDS_LIMIT' }
      }
    }
    
    const result = mockData.rechargeCoins(amount)
    if (result) {
      return {
        success: true,
        data: {
          rechargeAmount: amount,
          newBalance: result.newBalance
        }
      }
    }
    
    return {
      success: false,
      error: { message: '充值失败', code: 'RECHARGE_FAILED' }
    }
  },

  async retryEpisode(episodeId) {
    await delay(500)
    const user = mockData.getCurrentUser()
    const episode = mockData.getEpisode(episodeId)
    
    if (!episode) {
      return {
        success: false,
        error: { message: '集数不存在', code: 'NOT_FOUND' }
      }
    }
    
    const work = mockData.getWork(episode.workId)
    if (!work || work.userId !== user.id) {
      return {
        success: false,
        error: { message: '无权限重试此集数', code: 'FORBIDDEN' }
      }
    }
    
    const updated = mockData.updateEpisode(episodeId, { 
      status: 'PENDING',
      errorMessage: null
    })
    
    return {
      success: true,
      data: updated
    }
  }
}
