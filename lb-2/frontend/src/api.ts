const BASE = "/api";

function getToken() {
  return localStorage.getItem("token") ?? "";
}

async function request<T>(
  method: string,
  path: string,
  body?: unknown,
): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getToken()}`,
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const err = (await res.json().catch(() => ({ error: res.statusText }))) as {
      error?: string;
    };
    throw new Error(err.error ?? res.statusText);
  }
  return res.json() as Promise<T>;
}

export const api = {
  auth: {
    register: (username: string, password: string) =>
      request<{ token: string; user: { id: number; username: string } }>(
        "POST",
        "/auth/register",
        { username, password },
      ),
    login: (username: string, password: string) =>
      request<{ token: string; user: { id: number; username: string } }>(
        "POST",
        "/auth/login",
        { username, password },
      ),
    logout: () => request<{ ok: boolean }>("POST", "/auth/logout"),
    me: () =>
      request<{ id: number; username: string; created_at: number }>(
        "GET",
        "/auth/me",
      ),
  },
  users: {
    get: (id: number) =>
      request<Record<string, unknown>>("GET", `/users/${id}`),
    sendFriendRequest: (id: number) =>
      request<{ ok: boolean }>("POST", `/users/${id}/friend-request`),
    acceptFriendRequest: (id: number) =>
      request<{ ok: boolean }>("POST", `/users/friend-requests/${id}/accept`),
    getFriendRequests: () =>
      request<
        { id: number; user_id: number; username: string; created_at: number }[]
      >("GET", "/users/me/friend-requests"),
  },
  posts: {
    feed: () => request<Post[]>("GET", "/posts"),
    create: (content: string) => request<Post>("POST", "/posts", { content }),
    get: (id: number) => request<PostDetail>("GET", `/posts/${id}`),
    addComment: (postId: number, content: string) =>
      request<Comment[]>("POST", `/posts/${postId}/comments`, { content }),
  },
  messages: {
    unreadCount: () =>
      request<{ count: number }>("GET", "/messages/unread-count"),
    conversations: () => request<Conversation[]>("GET", "/messages"),
    thread: (userId: number) =>
      request<Message[]>("GET", `/messages/${userId}`),
    send: (userId: number, content: string) =>
      request<Message>("POST", `/messages/${userId}`, { content }),
  },
  search: (q: string) =>
    request<{
      users: { id: number; username: string; created_at: number }[];
      posts: Post[];
    }>("GET", `/search?q=${encodeURIComponent(q)}`),
};

export interface Post {
  id: number;
  user_id: number;
  username: string;
  content: string;
  created_at: number;
  comment_count?: number;
}

export interface Comment {
  id: number;
  post_id: number;
  user_id: number;
  username: string;
  content: string;
  created_at: number;
}

export interface PostDetail extends Post {
  comments: Comment[];
}

export interface Message {
  id: number;
  from_id: number;
  to_id: number;
  from_username: string;
  content: string;
  created_at: number;
  is_read: number;
}

export interface Conversation {
  other_id: number;
  other_username: string;
  last_message_at: number;
}
