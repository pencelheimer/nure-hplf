import { Router, Request, Response } from "express";
import bcrypt from "bcryptjs";
import db from "../db";
import { createSession, deleteSession } from "../sessions";
import { requireAuth, AuthRequest } from "../middleware/auth";

const router = Router();

// Create a new user account and return a session token
router.post("/register", (req: Request, res: Response): void => {
  const { username, password } = req.body as {
    username?: string;
    password?: string;
  };

  if (!username?.trim() || !password) {
    res.status(400).json({ error: "Username and password required" });
    return;
  }

  const hash = bcrypt.hashSync(password, 10);
  try {
    const result = db
      .prepare("INSERT INTO users (username, password_hash) VALUES (?, ?)")
      .run(username.trim(), hash);

    const token = createSession(result.lastInsertRowid as number);

    res.json({
      token,
      user: { id: result.lastInsertRowid, username: username.trim() },
    });
  } catch {
    res.status(409).json({ error: "Username already taken" });
  }
});

// Verify credentials and return a session token
router.post("/login", (req: Request, res: Response): void => {
  const { username, password } = req.body as {
    username?: string;
    password?: string;
  };

  const user = db
    .prepare("SELECT * FROM users WHERE username = ?")
    .get(username) as Record<string, unknown> | undefined;

  if (
    !user ||
    !bcrypt.compareSync(password ?? "", user.password_hash as string)
  ) {
    res.status(401).json({ error: "Invalid credentials" });
    return;
  }

  const token = createSession(user.id as number);
  res.json({ token, user: { id: user.id, username: user.username } });
});

// Invalidate the current session token
router.post("/logout", requireAuth, (req: Request, res: Response): void => {
  const token = req.headers.authorization?.replace("Bearer ", "") ?? "";
  deleteSession(token);
  res.json({ ok: true });
});

// Return profile of the currently authenticated user
router.get("/me", requireAuth, (req: Request, res: Response): void => {
  const userId = (req as AuthRequest).userId;

  const user = db
    .prepare("SELECT id, username, created_at FROM users WHERE id = ?")
    .get(userId);

  res.json(user);
});

export default router;
