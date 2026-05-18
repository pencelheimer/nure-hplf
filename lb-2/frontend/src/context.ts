import { createContext } from 'react';
import type { User, LiveEvent } from './types';

export interface AppContextValue {
  user: User;
  liveEvent: LiveEvent | null;
  unreadCount: number;
  refreshUnread: () => void;
}

export const AppContext = createContext<AppContextValue>(null!);
