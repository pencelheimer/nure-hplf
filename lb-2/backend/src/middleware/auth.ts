import { Request, Response, NextFunction } from "express";
import { getSession } from "../sessions";

export interface AuthRequest extends Request {
  userId: number;
}

export function requireAuth(
  req: Request,
  res: Response,
  next: NextFunction,
): void {
  const token = req.headers.authorization?.replace("Bearer ", "");
  if (!token) {
    res.status(401).json({ error: "Unauthorized" });
    return;
  }
  const userId = getSession(token);
  if (!userId) {
    res.status(401).json({ error: "Unauthorized" });
    return;
  }
  (req as AuthRequest).userId = userId;
  next();
}
