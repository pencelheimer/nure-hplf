import { useState, useEffect, useRef, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { Message, Conversation } from '../api';
import { AppContext } from '../context';
import { timeAgo } from '../utils';

export function MessagesPage() {
  const { userId: userIdStr } = useParams<{ userId?: string }>();
  const activeUserId = userIdStr ? parseInt(userIdStr) : undefined;
  const { user: me, liveEvent, refreshUnread } = useContext(AppContext);
  const navigate = useNavigate();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [messages, setMessages] = useState<Message[]>([]);
  const [content, setContent] = useState('');
  const activeUserIdRef = useRef(activeUserId);
  useEffect(() => { activeUserIdRef.current = activeUserId; });

  useEffect(() => {
    api.messages.conversations().then(setConversations).catch(() => { });
  }, []);

  useEffect(() => {
    if (activeUserId) {
      api.messages.thread(activeUserId).then(msgs => {
        setMessages(msgs);
        refreshUnread();
      }).catch(() => { });
    } else {
      setMessages([]);
    }
  }, [activeUserId]);

  useEffect(() => {
    if (!liveEvent || liveEvent.type !== 'new_message') return;
    const msg = liveEvent.data as Message;
    if (msg.from_id === activeUserIdRef.current) {
      setMessages(prev => [...prev, msg]);
    }
    api.messages.conversations().then(setConversations).catch(() => { });
  }, [liveEvent]);

  const send = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeUserId || !content.trim()) return;
    const msg = await api.messages.send(activeUserId, content);
    setMessages(prev => [...prev, msg]);
    setContent('');
    api.messages.conversations().then(setConversations).catch(() => { });
  };

  const activeConv = conversations.find(c => c.other_id === activeUserId);

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '190px 1fr', gap: 28, minHeight: 400 }}>
      <div style={{ borderRight: '1px solid #eee', paddingRight: 16 }}>
        <h3 style={{ marginBottom: 12 }}>Messages</h3>
        {conversations.length === 0 && (
          <p style={{ color: '#888', fontSize: 14 }}>No conversations yet.</p>
        )}
        {conversations.map(c => (
          <div
            key={c.other_id}
            onClick={() => navigate(`/messages/${c.other_id}`)}
            style={{
              padding: '7px 4px',
              cursor: 'pointer',
              fontWeight: c.other_id === activeUserId ? 'bold' : 'normal',
              borderBottom: '1px solid #f0f0f0',
            }}
          >
            {c.other_username}
          </div>
        ))}
      </div>

      <div>
        {activeUserId ? (
          <>
            <h3 style={{ marginBottom: 16 }}>
              <a href="#" onClick={e => { e.preventDefault(); navigate(`/profile/${activeUserId}`); }}>
                {activeConv?.other_username ?? `User #${activeUserId}`}
              </a>
            </h3>
            <div style={{ marginBottom: 16, maxHeight: 420, overflowY: 'auto' }}>
              {messages.length === 0 && (
                <p style={{ color: '#888', fontSize: 15 }}>No messages yet. Say hello!</p>
              )}
              {messages.map(m => (
                <div key={m.id} style={{ padding: '5px 0', textAlign: m.from_id === me.id ? 'right' : 'left' }}>
                  <span style={{
                    display: 'inline-block',
                    padding: '6px 12px',
                    background: m.from_id === me.id ? '#333' : '#f0f0f0',
                    color: m.from_id === me.id ? 'white' : 'inherit',
                    maxWidth: '70%',
                    fontSize: 16,
                  }}>
                    {m.content}
                  </span>
                  <div style={{ fontSize: 12, color: '#aaa', marginTop: 2 }}>{timeAgo(m.created_at)}</div>
                </div>
              ))}
            </div>
            <form onSubmit={send} style={{ display: 'flex', gap: 8 }}>
              <input
                value={content}
                onChange={e => setContent(e.target.value)}
                placeholder="Type a message..."
                style={{ flex: 1 }}
              />
              <button type="submit">Send</button>
            </form>
          </>
        ) : (
          <p style={{ color: '#888' }}>Select a conversation or open one from a user profile.</p>
        )}
      </div>
    </div>
  );
}
