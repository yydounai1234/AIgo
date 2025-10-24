import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Navigation from './components/Navigation'
import Home from './pages/Home'
import WorkEditor from './pages/WorkEditor'
import MyWorks from './pages/MyWorks'
import Gallery from './pages/Gallery'
import EpisodeViewer from './pages/EpisodeViewer'
import api from './services/api'
import './App.css'

function App() {
  const [userBalance, setUserBalance] = useState(500)

  useEffect(() => {
    loadUserBalance()
  }, [])

  const loadUserBalance = async () => {
    try {
      const result = await api.getUserBalance()
      if (result.success) {
        setUserBalance(result.data.balance)
      }
    } catch (err) {
      console.error('Failed to load user balance', err)
    }
  }

  return (
    <Router>
      <div className="app">
        <Navigation userBalance={userBalance} />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/work/:workId/edit" element={<WorkEditor />} />
          <Route path="/my-works" element={<MyWorks />} />
          <Route path="/gallery" element={<Gallery />} />
          <Route path="/episode/:episodeId" element={<EpisodeViewer />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App
