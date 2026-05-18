import { useState } from 'react';
import { api } from '../api';
import type { User } from '../types';

export function LoginPage({ onLogin }: { onLogin: (user: User) => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const result = mode === 'login'
        ? await api.auth.login(username, password)
        : await api.auth.register(username, password);
      localStorage.setItem('token', result.token);
      onLogin(result.user as User);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error');
    }
  };

  return (
    <div style={{ maxWidth: 380, margin: '80px auto' }}>
      <h1>Social</h1>
      <p style={{ color: '#666', marginBottom: 24 }}>
        {mode === 'login' ? 'Welcome back.' : 'Create your account.'}
      </p>
      <form onSubmit={submit}>
        <div className="form-group">
          <label>Username</label>
          <input value={username} onChange={e => setUsername(e.target.value)} required autoFocus style={{ width: '100%' }} />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required style={{ width: '100%' }} />
        </div>
        {error && <p className="error">{error}</p>}
        <button type="submit" style={{ width: '100%', padding: '9px' }}>
          {mode === 'login' ? 'Login' : 'Register'}
        </button>
      </form>
      <p style={{ marginTop: 14, fontSize: 15 }}>
        {mode === 'login' ? 'No account? ' : 'Have an account? '}
        <a href="#" onClick={e => { e.preventDefault(); setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
          {mode === 'login' ? 'Register' : 'Login'}
        </a>
      </p>
    </div>
  );
}
