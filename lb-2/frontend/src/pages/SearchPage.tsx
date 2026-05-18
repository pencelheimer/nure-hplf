import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { Post } from '../api';
import { timeAgo } from '../utils';

export function SearchPage() {
  const navigate = useNavigate();
  const [q, setQ] = useState('');
  const [results, setResults] = useState<{
    users: { id: number; username: string; created_at: number }[];
    posts: Post[];
  } | null>(null);

  const search = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!q.trim()) return;
    const data = await api.search(q);
    setResults(data);
  };

  return (
    <div>
      <form onSubmit={search} style={{ display: 'flex', gap: 8, marginBottom: 28 }}>
        <input
          value={q}
          onChange={e => setQ(e.target.value)}
          placeholder="Search users or posts..."
          style={{ flex: 1 }}
          autoFocus
        />
        <button type="submit">Search</button>
      </form>

      {results && (
        <>
          <h2 style={{ marginTop: 0 }}>Users</h2>
          {results.users.length === 0 && <p style={{ color: '#888' }}>No users found.</p>}
          {results.users.map(u => (
            <div key={u.id} style={{ padding: '8px 0', borderBottom: '1px solid #eee' }}>
              <a href="#" onClick={e => { e.preventDefault(); navigate(`/profile/${u.id}`); }}>
                {u.username}
              </a>
            </div>
          ))}

          <h2>Posts</h2>
          {results.posts.length === 0 && <p style={{ color: '#888' }}>No posts found.</p>}
          {results.posts.map(p => (
            <div key={p.id} className="post">
              <div className="post-meta">
                <a href="#" onClick={e => { e.preventDefault(); navigate(`/profile/${p.user_id}`); }}>
                  {p.username}
                </a>
                {' · '}{timeAgo(p.created_at)}
              </div>
              <p style={{ whiteSpace: 'pre-wrap', margin: '6px 0 8px' }}>{p.content}</p>
              <a href="#" style={{ fontSize: 14 }} onClick={e => { e.preventDefault(); navigate(`/post/${p.id}`); }}>
                View post
              </a>
            </div>
          ))}
        </>
      )}
    </div>
  );
}
