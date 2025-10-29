let currentUser = {
  id: '1',
  username: 'demo_user',
  email: 'demo@example.com',
  coinBalance: 500,
  avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user',
  token: 'mock-jwt-token-12345'
}

let works = [
  {
    id: '1',
    userId: '1',
    title: '魔法学院奇遇记',
    description: '一个少年进入魔法学院后的奇幻冒险',
    isPublic: true,
    coverImage: 'https://via.placeholder.com/300x400?text=魔法学院',
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-20T15:30:00Z',
    likesCount: 156,
    viewsCount: 1240,
    authorName: 'demo_user',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user'
  },
  {
    id: '2',
    userId: '1',
    title: '星际旅行日记',
    description: '探索未知星系的科幻冒险',
    isPublic: true,
    coverImage: 'https://via.placeholder.com/300x400?text=星际旅行',
    createdAt: '2024-02-01T09:00:00Z',
    updatedAt: '2024-02-10T14:20:00Z',
    likesCount: 89,
    viewsCount: 650,
    authorName: 'demo_user',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user'
  }
]

let episodes = [
  {
    id: '1',
    workId: '1',
    episodeNumber: 1,
    title: '第一集：入学之日',
    novelText: '阳光洒在古老的魔法学院门前，少年李明背着行囊，仰望着高耸的尖塔。\n\n"欢迎来到银月魔法学院。"一位身穿长袍的老师微笑着说。\n\n李明深吸一口气，踏入了这个充满魔法的世界。',
    scenes: [
      { id: 1, text: '阳光洒在古老的魔法学院门前，少年李明背着行囊，仰望着高耸的尖塔。', imageUrl: 'https://via.placeholder.com/800x450?text=Scene+1' },
      { id: 2, text: '"欢迎来到银月魔法学院。"一位身穿长袍的老师微笑着说。', imageUrl: 'https://via.placeholder.com/800x450?text=Scene+2' },
      { id: 3, text: '李明深吸一口气，踏入了这个充满魔法的世界。', imageUrl: 'https://via.placeholder.com/800x450?text=Scene+3' }
    ],
    isFree: true,
    coinPrice: 0,
    isPublished: true,
    createdAt: '2024-01-15T10:30:00Z',
    authorName: 'demo_user',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user'
  },
  {
    id: '2',
    workId: '1',
    episodeNumber: 2,
    title: '第二集：魔法觉醒',
    novelText: '在魔法课上，李明第一次尝试施展咒语。\n\n他的手掌中闪烁着微弱的光芒。\n\n"做得很好！"老师鼓励道。',
    scenes: [
      { id: 1, text: '在魔法课上，李明第一次尝试施展咒语。', imageUrl: 'https://via.placeholder.com/800x450?text=Episode+2+Scene+1' },
      { id: 2, text: '他的手掌中闪烁着微弱的光芒。', imageUrl: 'https://via.placeholder.com/800x450?text=Episode+2+Scene+2' },
      { id: 3, text: '"做得很好！"老师鼓励道。', imageUrl: 'https://via.placeholder.com/800x450?text=Episode+2+Scene+3' }
    ],
    isFree: false,
    coinPrice: 50,
    isPublished: true,
    createdAt: '2024-01-16T11:00:00Z',
    authorName: 'demo_user',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user'
  },
  {
    id: '3',
    workId: '1',
    episodeNumber: 3,
    title: '第三集：黑暗来袭',
    novelText: '夜幕降临，一股邪恶的力量笼罩着学院。\n\n李明和他的朋友们必须联手对抗这个威胁。',
    scenes: [
      { id: 1, text: '夜幕降临，一股邪恶的力量笼罩着学院。', imageUrl: 'https://via.placeholder.com/800x450?text=Episode+3+Scene+1' },
      { id: 2, text: '李明和他的朋友们必须联手对抗这个威胁。', imageUrl: 'https://via.placeholder.com/800x450?text=Episode+3+Scene+2' }
    ],
    isFree: false,
    coinPrice: 100,
    isPublished: false,
    createdAt: '2024-01-17T09:00:00Z',
    authorName: 'demo_user',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user'
  },
  {
    id: '4',
    workId: '2',
    episodeNumber: 1,
    title: '第一集：启程',
    novelText: '飞船缓缓离开地球轨道，向着未知的星系飞去。\n\n船长看着窗外的星空，心中充满期待。',
    scenes: [
      { id: 1, text: '飞船缓缓离开地球轨道，向着未知的星系飞去。', imageUrl: 'https://via.placeholder.com/800x450?text=Space+1' },
      { id: 2, text: '船长看着窗外的星空，心中充满期待。', imageUrl: 'https://via.placeholder.com/800x450?text=Space+2' }
    ],
    isFree: true,
    coinPrice: 0,
    isPublished: true,
    createdAt: '2024-02-01T10:00:00Z',
    authorName: 'demo_user',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user'
  },
  {
    id: '5',
    workId: '2',
    episodeNumber: 2,
    title: '第二集：新世界',
    novelText: '飞船降落在一颗陌生的星球上。\n\n到处都是从未见过的生物和植物。',
    scenes: [
      { id: 1, text: '飞船降落在一颗陌生的星球上。', imageUrl: 'https://via.placeholder.com/800x450?text=New+Planet+1' },
      { id: 2, text: '到处都是从未见过的生物和植物。', imageUrl: 'https://via.placeholder.com/800x450?text=New+Planet+2' }
    ],
    isFree: false,
    coinPrice: 60,
    isPublished: true,
    createdAt: '2024-02-05T11:00:00Z',
    authorName: 'demo_user',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=demo_user'
  }
]

let purchases = [
  { id: '1', userId: '1', episodeId: '2', coinCost: 50, purchasedAt: '2024-01-16T12:00:00Z' }
]

let likes = [
  { id: '1', userId: '1', workId: '2', createdAt: '2024-02-06T10:00:00Z' }
]

export const mockData = {
  getCurrentUser: () => ({ ...currentUser }),
  
  setCurrentUser: (user) => {
    currentUser = { ...currentUser, ...user }
  },
  
  getWorks: () => [...works],
  
  getWork: (id) => works.find(w => w.id === id),
  
  addWork: (work) => {
    const newWork = {
      ...work,
      id: String(works.length + 1),
      userId: currentUser.id,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      likesCount: 0,
      viewsCount: 0
    }
    works.push(newWork)
    return newWork
  },
  
  updateWork: (id, updates) => {
    const index = works.findIndex(w => w.id === id)
    if (index !== -1) {
      works[index] = { ...works[index], ...updates, updatedAt: new Date().toISOString() }
      return works[index]
    }
    return null
  },
  
  deleteWork: (id) => {
    const index = works.findIndex(w => w.id === id)
    if (index !== -1) {
      works.splice(index, 1)
      episodes = episodes.filter(e => e.workId !== id)
      return true
    }
    return false
  },
  
  getEpisodes: (workId) => episodes.filter(e => e.workId === workId),
  
  getEpisode: (id) => episodes.find(e => e.id === id),
  
  addEpisode: (episode) => {
    const workEpisodes = episodes.filter(e => e.workId === episode.workId)
    const newEpisode = {
      ...episode,
      id: String(episodes.length + 1),
      episodeNumber: workEpisodes.length + 1,
      scenes: [],
      isPublished: false,
      createdAt: new Date().toISOString()
    }
    episodes.push(newEpisode)
    return newEpisode
  },
  
  updateEpisode: (id, updates) => {
    const index = episodes.findIndex(e => e.id === id)
    if (index !== -1) {
      episodes[index] = { ...episodes[index], ...updates }
      return episodes[index]
    }
    return null
  },
  
  publishEpisode: (id) => {
    const index = episodes.findIndex(e => e.id === id)
    if (index !== -1) {
      episodes[index].isPublished = true
      return episodes[index]
    }
    return null
  },
  
  hasPurchased: (episodeId) => {
    return purchases.some(p => p.userId === currentUser.id && p.episodeId === episodeId)
  },
  
  purchaseEpisode: (episodeId, coinCost) => {
    if (currentUser.coinBalance >= coinCost) {
      currentUser.coinBalance -= coinCost
      const purchase = {
        id: String(purchases.length + 1),
        userId: currentUser.id,
        episodeId,
        coinCost,
        purchasedAt: new Date().toISOString()
      }
      purchases.push(purchase)
      return { purchase, newBalance: currentUser.coinBalance }
    }
    return null
  },
  
  hasLiked: (workId) => {
    return likes.some(l => l.userId === currentUser.id && l.workId === workId)
  },
  
  likeWork: (workId) => {
    if (!mockData.hasLiked(workId)) {
      const like = {
        id: String(likes.length + 1),
        userId: currentUser.id,
        workId,
        createdAt: new Date().toISOString()
      }
      likes.push(like)
      const work = works.find(w => w.id === workId)
      if (work) {
        work.likesCount = (work.likesCount || 0) + 1
      }
      return true
    }
    return false
  },
  
  unlikeWork: (workId) => {
    const index = likes.findIndex(l => l.userId === currentUser.id && l.workId === workId)
    if (index !== -1) {
      likes.splice(index, 1)
      const work = works.find(w => w.id === workId)
      if (work && work.likesCount > 0) {
        work.likesCount -= 1
      }
      return true
    }
    return false
  },
  
  rechargeCoins: (amount) => {
    if (amount > 0 && amount <= 1000) {
      currentUser.coinBalance += amount
      return { newBalance: currentUser.coinBalance }
    }
    return null
  }
}
