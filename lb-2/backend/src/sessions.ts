import crypto from "crypto";

const sessions = new Map<string, number>();

export function createSession(userId: number): string {
  const token = crypto.randomBytes(32).toString("hex");
  sessions.set(token, userId);
  return token;
}

export function getSession(token: string): number | undefined {
  return sessions.get(token);
}

export function deleteSession(token: string): void {
  sessions.delete(token);
}
