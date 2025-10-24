import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import Navigation from './components/Navigation'
import ProtectedRoute from './components/ProtectedRoute'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import WorkEditor from './pages/WorkEditor'
import MyWorks from './pages/MyWorks'
import Gallery from './pages/Gallery'
import EpisodeViewer from './pages/EpisodeViewer'
import api from './services/api'
import './App.css'

function AppContent() {
  const [userBalance, setUserBalance] = useState(0)
  const { isAuthenticated, user } = useAuth()

  useEffect(() => {
    if (isAuthenticated()) {
      loadUserBalance()
    }
  }, [isAuthenticated, user])

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
    <div className="app">
      <Navigation userBalance={userBalance} />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route 
          path="/work/:workId/edit" 
          element={
            <ProtectedRoute>
              <WorkEditor />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/my-works" 
          element={
            <ProtectedRoute>
              <MyWorks />
            </ProtectedRoute>
          } 
        />
        <Route path="/gallery" element={<Gallery />} />
        <Route 
          path="/episode/:episodeId" 
          element={
            <ProtectedRoute>
              <EpisodeViewer />
            </ProtectedRoute>
          } 
        />
      </Routes>
    </div>
  )
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  )
}

export default App
