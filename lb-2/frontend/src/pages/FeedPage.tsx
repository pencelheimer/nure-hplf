import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { Post } from '../api';
import type { FriendRequest } from '../types';
import { AppContext } from '../context';
import { timeAgo } from '../utils';

export function FeedPage() {
  const { user: me, liveEvent } = useContext(AppContext);
  const navigate = useNavigate();
  const [posts, setPosts] = useState<Post[]>([]);
  const [content, setContent] = useState('');
  const [requests, setRequests] = useState<FriendRequest[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.posts.feed().then(setPosts).catch(() => { });
    api.users.getFriendRequests().then(setRequests).catch(() => { });
  }, []);

  useEffect(() => {
    if (!liveEvent) return;

    if (liveEvent.type === 'new_post') {
      const post = liveEvent.data as Post;
      setPosts(prev => prev.some(p => p.id === post.id) ? prev : [{ ...post, comment_count: 0 }, ...prev]);
    } else if (liveEvent.type === 'new_friend_request') {
      const req = liveEvent.data as FriendRequest;
      setRequests(prev => prev.some(r => r.id === req.id) ? prev : [req, ...prev]);
    }
  }, [liveEvent]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const post = await api.posts.create(content);
      setPosts(prev => [{ ...post, comment_count: 0 }, ...prev]);
      setContent('');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error');
    }
  };

  const acceptRequest = async (req: FriendRequest) => {
    await api.users.acceptFriendRequest(req.id);
    setRequests(prev => prev.filter(r => r.id !== req.id));
    api.posts.feed().then(setPosts).catch(() => { });
  };

  return (
    <div>
      {requests.length > 0 && (
        <div style={{ background: '#f9f9f9', padding: '10px 14px', marginBottom: 24, borderLeft: '3px solid #333' }}>
          <strong style={{ fontSize: 15 }}>Friend requests:</strong>
          {requests.map(r => (
            <span key={r.id} style={{ marginLeft: 12 }}>
              <a href="#" onClick={e => { e.preventDefault(); navigate(`/profile/${r.user_id}`); }}>
                {r.username}
              </a>
              {' '}
              <button onClick={() => acceptRequest(r)} style={{ fontSize: 13, padding: '2px 8px' }}>Accept</button>
            </span>
          ))}
        </div>
      )}

      <form onSubmit={submit} style={{ marginBottom: 32 }}>
        <div className="form-group">
          <textarea
            value={content}
            onChange={e => setContent(e.target.value)}
            placeholder="What's on your mind?"
            rows={3}
          />
        </div>
        {error && <p className="error">{error}</p>}
        <button type="submit">Post</button>
      </form>

      {posts.length === 0 && (
        <p style={{ color: '#888' }}>No posts yet. Find and add some friends to see their posts here.</p>
      )}

      {posts.map(post => (
        <div key={post.id} className="post">
          <div className="post-meta">
            <a href="#" onClick={e => { e.preventDefault(); navigate(`/profile/${post.user_id}`); }}>
              {post.username}
            </a>
            {post.user_id === me.id && ' (you)'}
            {' · '}{timeAgo(post.created_at)}
          </div>
          <p style={{ whiteSpace: 'pre-wrap', margin: '6px 0 8px' }}>{post.content}</p>
          <a href="#" style={{ fontSize: 14 }} onClick={e => { e.preventDefault(); navigate(`/post/${post.id}`); }}>
            {post.comment_count ?? 0} {(post.comment_count ?? 0) === 1 ? 'comment' : 'comments'}
          </a>
        </div>
      ))}
    </div>
  );
}
