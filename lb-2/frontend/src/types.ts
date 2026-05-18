export interface User {
  id: number;
  username: string;
  created_at: number;
}

export interface FriendRequest {
  id: number;
  user_id: number;
  username: string;
  created_at: number;
}

export interface LiveEvent {
  type:
    | "new_post"
    | "new_friend_request"
    | "friend_request_accepted"
    | "new_message";
  data: unknown;
  seq: number;
}

