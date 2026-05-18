import type { Response } from "express";

type FlushableResponse = Response & { flush?: () => void };

const clients = new Map<number, Set<FlushableResponse>>();

export function addClient(userId: number, res: FlushableResponse): void {
  if (!clients.has(userId)) clients.set(userId, new Set());
  clients.get(userId)!.add(res);
}

export function removeClient(userId: number, res: FlushableResponse): void {
  const set = clients.get(userId);
  if (!set) return;
  set.delete(res);
  if (set.size === 0) clients.delete(userId);
}

export function sendToUser(userId: number, event: string, data: unknown): void {
  const set = clients.get(userId);
  if (!set) return;
  const msg = `event: ${event}\ndata: ${JSON.stringify(data)}\n\n`;
  for (const res of set) {
    res.write(msg);
    res.flush?.();
  }
}
