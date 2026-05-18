import { Router, Request, Response } from "express";
import db from "../db";
import { requireAuth } from "../middleware/auth";

const router = Router();

// Search users by username and posts by content (LIKE match)
router.get("/", requireAuth, (req: Request, res: Response): void => {
  const q = (req.query.q as string | undefined)?.trim() ?? "";
  if (!q) {
    res.json({ users: [], posts: [] });
    return;
  }
  const pattern = `%${q}%`;
  const users = db
    .prepare(
      "SELECT id, username, created_at FROM users WHERE username LIKE ? LIMIT 20",
    )
    .all(pattern);
  const posts = db
    .prepare(
      `
        SELECT p.*, u.username FROM posts p
        JOIN users u ON p.user_id = u.id
        WHERE p.content LIKE ?
        ORDER BY p.created_at DESC LIMIT 20
      `,
    )
    .all(pattern);
  res.json({ users, posts });
});

export default router;
