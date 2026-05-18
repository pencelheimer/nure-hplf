import { Router, Request, Response } from "express";
import db from "../db";
import { requireAuth, AuthRequest } from "../middleware/auth";
import { sendToUser } from "../sse";

const router = Router();

// List all conversations grouped by the other participant
router.get("/", requireAuth, (req: Request, res: Response): void => {
  const userId = (req as AuthRequest).userId;
  const conversations = db
    .prepare(
      `
        SELECT
          CASE WHEN from_id = ? THEN to_id ELSE from_id END as other_id,
          u.username as other_username,
          MAX(m.created_at) as last_message_at
        FROM messages m
        JOIN users u ON u.id = CASE WHEN from_id = ? THEN to_id ELSE from_id END
        WHERE from_id = ? OR to_id = ?
        GROUP BY other_id
        ORDER BY last_message_at DESC
      `,
    )
    .all(userId, userId, userId, userId);
  res.json(conversations);
});

// Count unread incoming messages for the current user
router.get(
  "/unread-count",
  requireAuth,
  (req: Request, res: Response): void => {
    const userId = (req as AuthRequest).userId;
    const row = db
      .prepare(
        "SELECT COUNT(*) as count FROM messages WHERE to_id = ? AND is_read = 0",
      )
      .get(userId) as { count: number };
    res.json({ count: row.count });
  },
);

// Load the full message thread with a user and mark their messages as read
router.get("/:userId", requireAuth, (req: Request, res: Response): void => {
  const userId = (req as AuthRequest).userId;
  const otherId = parseInt(req.params.userId);

  db.prepare(
    "UPDATE messages SET is_read = 1 WHERE from_id = ? AND to_id = ?",
  ).run(otherId, userId);

  const msgs = db
    .prepare(
      `
        SELECT m.*, u.username as from_username
        FROM messages m
        JOIN users u ON m.from_id = u.id
        WHERE (from_id = ? AND to_id = ?) OR (from_id = ? AND to_id = ?)
        ORDER BY m.created_at ASC
      `,
    )
    .all(userId, otherId, otherId, userId);
  res.json(msgs);
});

// Send a message and push new_message SSE event to the recipient
router.post("/:userId", requireAuth, (req: Request, res: Response): void => {
  const userId = (req as AuthRequest).userId;
  const toId = parseInt(req.params.userId);
  const { content } = req.body as { content?: string };
  if (!content?.trim()) {
    res.status(400).json({ error: "Content required" });
    return;
  }
  const result = db
    .prepare("INSERT INTO messages (from_id, to_id, content) VALUES (?, ?, ?)")
    .run(userId, toId, content.trim());
  const msg = db
    .prepare(
      "SELECT m.*, u.username as from_username FROM messages m JOIN users u ON m.from_id = u.id WHERE m.id = ?",
    )
    .get(result.lastInsertRowid);
  res.json(msg);
  sendToUser(toId, "new_message", msg);
});

export default router;
