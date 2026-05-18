import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { Post } from '../api';
import { AppContext } from '../context';
import { timeAgo } from '../utils';

export function ProfilePage() {
  const { userId: userIdStr } = useParams<{ userId: string }>();
  const userId = parseInt(userIdStr!);
  const { user: me } = useContext(AppContext);
  const navigate = useNavigate();
  const [data, setData] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    setData(null);
    setError('');
    api.users.get(userId).then(setData).catch(() => setError('User not found'));
  }, [userId]);

  const sendRequest = async () => {
    try {
      await api.users.sendFriendRequest(userId);
      setData(d => d ? { ...d, friendship: { status: 'pending', requester_id: me.id } } : d);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error');
    }
  };

  if (error) return <p className="error">{error}</p>;
  if (!data) return <p style={{ color: '#888' }}>Loading...</p>;

  const isMe = userId === me.id;
  const friendship = data.friendship as { status: string; requester_id: number } | null | undefined;

  let friendBadge: React.ReactNode = null;
  if (!isMe) {
    if (!friendship) {
      friendBadge = <button onClick={sendRequest}>Add friend</button>;
    } else if (friendship.status === 'pending') {
      friendBadge = <span style={{ fontSize: 14, color: '#888' }}>Request pending</span>;
    } else {
      friendBadge = <span style={{ fontSize: 14, color: '#888' }}>Friends</span>;
    }
  }

  const posts = (data.posts as Post[]) ?? [];

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 14, flexWrap: 'wrap', marginBottom: 4 }}>
        <h1 style={{ marginBottom: 0 }}>{data.username as string}</h1>
        {friendBadge}
        {!isMe && (
          <button
            onClick={() => navigate(`/messages/${userId}`)}
            style={{ background: 'white', color: '#333', border: '1px solid #ccc' }}
          >
            Message
          </button>
        )}
      </div>
      <p style={{ color: '#888', fontSize: 14, marginBottom: 24 }}>
        Joined {timeAgo(data.created_at as number)}
      </p>
      {error && <p className="error">{error}</p>}
      {posts.length === 0 && <p style={{ color: '#888' }}>No posts yet.</p>}
      {posts.map(post => (
        <div key={post.id} className="post">
          <div className="post-meta">{timeAgo(post.created_at)}</div>
          <p style={{ whiteSpace: 'pre-wrap', margin: '6px 0 8px' }}>{post.content}</p>
          <a href="#" style={{ fontSize: 14 }} onClick={e => { e.preventDefault(); navigate(`/post/${post.id}`); }}>
            Comments
          </a>
        </div>
      ))}
    </div>
  );
}
