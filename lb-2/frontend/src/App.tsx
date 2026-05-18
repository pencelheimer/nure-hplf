import { useState, useEffect, useCallback, useRef } from 'react';
import { Routes, Route, Link, Navigate } from 'react-router-dom';
import { api } from './api';
import type { User, LiveEvent } from './types';
import { AppContext } from './context';
import { LoginPage } from './pages/LoginPage';
import { FeedPage } from './pages/FeedPage';
import { ProfilePage } from './pages/ProfilePage';
import { PostPage } from './pages/PostPage';
import { MessagesPage } from './pages/MessagesPage';
import { SearchPage } from './pages/SearchPage';

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [liveEvent, setLiveEvent] = useState<LiveEvent | null>(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const seqRef = useRef(0);

  const refreshUnread = useCallback(() => {
    api.messages.unreadCount().then(r => setUnreadCount(r.count)).catch(() => { });
  }, []);

  useEffect(() => {
    if (localStorage.getItem('token')) {
      api.auth.me()
        .then(u => {
          setUser(u as User);
          refreshUnread();
        })
        .catch(() => localStorage.removeItem('token'));
    }
  }, []);

  useEffect(() => {
    if (!user) return;
    const token = localStorage.getItem('token') ?? '';
    const es = new EventSource(`/api/events?token=${encodeURIComponent(token)}`);
    const emit = (type: LiveEvent['type']) => (e: MessageEvent) => {
      setLiveEvent({ type, data: JSON.parse(e.data as string), seq: ++seqRef.current });
    };
    es.addEventListener('new_post', emit('new_post'));
    es.addEventListener('new_friend_request', emit('new_friend_request'));
    es.addEventListener('new_message', (e: MessageEvent) => {
      emit('new_message')(e);
      setUnreadCount(c => c + 1);
    });
    es.addEventListener('friend_request_accepted', emit('friend_request_accepted'));
    return () => es.close();
  }, [user]);

  const handleLogin = (u: User) => {
    setUser(u);
    refreshUnread();
  };

  const handleLogout = async () => {
    await api.auth.logout().catch(() => { });
    localStorage.removeItem('token');
    setUser(null);
  };

  if (!user) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <AppContext.Provider value={{ user, liveEvent, unreadCount, refreshUnread }}>
      <div>
        <nav>
          <strong>Social</strong>
          <Link to="/feed">Feed</Link>
          <Link to="/messages">
            Messages{unreadCount > 0 && (
              <span style={{ marginLeft: 5, fontSize: 12, background: '#333', color: 'white', padding: '1px 6px', borderRadius: 10, verticalAlign: 'middle' }}>
                {unreadCount}
              </span>
            )}
          </Link>
          <Link to="/search">Search</Link>
          <Link to={`/profile/${user.id}`}>{user.username}</Link>
          <a href="#" onClick={e => { e.preventDefault(); handleLogout(); }} style={{ marginLeft: 'auto', color: '#aaa', fontSize: 15 }}>
            Logout
          </a>
        </nav>

        <Routes>
          <Route path="/feed" element={<FeedPage />} />
          <Route path="/profile/:userId" element={<ProfilePage />} />
          <Route path="/post/:postId" element={<PostPage />} />
          <Route path="/messages" element={<MessagesPage />} />
          <Route path="/messages/:userId" element={<MessagesPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="*" element={<Navigate to="/feed" replace />} />
        </Routes>
      </div>
    </AppContext.Provider>
  );
}
