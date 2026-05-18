import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { PostDetail, Comment } from '../api';
import { AppContext } from '../context';
import { timeAgo } from '../utils';

export function PostPage() {
  const { postId: postIdStr } = useParams<{ postId: string }>();
  const postId = parseInt(postIdStr!);
  const { user: me } = useContext(AppContext);
  const navigate = useNavigate();
  const [data, setData] = useState<PostDetail | null>(null);
  const [content, setContent] = useState('');

  useEffect(() => {
    setData(null);
    api.posts.get(postId).then(setData).catch(() => { });
  }, [postId]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim() || !data) return;
    const comments = await api.posts.addComment(postId, content);
    setData(d => d ? { ...d, comments } : d);
    setContent('');
  };

  if (!data) return <p style={{ color: '#888' }}>Loading...</p>;

  return (
    <div>
      <div className="post" style={{ borderBottomWidth: 2, borderBottomColor: '#333' }}>
        <div className="post-meta">
          <a href="#" onClick={e => { e.preventDefault(); navigate(`/profile/${data.user_id}`); }}>
            {data.username}
          </a>
          {data.user_id === me.id && ' (you)'}
          {' · '}{timeAgo(data.created_at)}
        </div>
        <p style={{ whiteSpace: 'pre-wrap', margin: '6px 0 0' }}>{data.content}</p>
      </div>

      <div style={{ margin: '20px 0 24px' }}>
        {data.comments.length === 0 && <p style={{ color: '#888', fontSize: 15 }}>No comments yet.</p>}
        {data.comments.map((c: Comment) => (
          <div key={c.id} style={{ padding: '10px 0', borderBottom: '1px solid #f0f0f0' }}>
            <div style={{ fontSize: 14, color: '#888', marginBottom: 4 }}>
              <a href="#" onClick={e => { e.preventDefault(); navigate(`/profile/${c.user_id}`); }}>
                {c.username}
              </a>
              {' · '}{timeAgo(c.created_at)}
            </div>
            <p style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{c.content}</p>
          </div>
        ))}
      </div>

      <form onSubmit={submit}>
        <div className="form-group">
          <textarea value={content} onChange={e => setContent(e.target.value)} placeholder="Add a comment..." rows={2} />
        </div>
        <button type="submit">Comment</button>
      </form>
    </div>
  );
}
