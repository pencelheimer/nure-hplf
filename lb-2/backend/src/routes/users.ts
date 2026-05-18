import { Router, Request, Response } from "express";
import db from "../db";
import { requireAuth, AuthRequest } from "../middleware/auth";
import { sendToUser } from "../sse";

const router = Router();

// List incoming pending friend requests for the current user
router.get(
  "/me/friend-requests",
  requireAuth,
  (req: Request, res: Response): void => {
    const userId = (req as AuthRequest).userId;
    const requests = db
      .prepare(
        `
          SELECT f.id, f.created_at, u.id as user_id, u.username
          FROM friendships f
          JOIN users u ON f.requester_id = u.id
          WHERE f.addressee_id = ? AND f.status = 'pending'
        `,
      )
      .all(userId);
    res.json(requests);
  },
);

// List up to 50 users ordered by registration date
router.get("/", requireAuth, (_req: Request, res: Response): void => {
  const users = db
    .prepare(
      "SELECT id, username, created_at FROM users ORDER BY created_at DESC LIMIT 50",
    )
    .all();
  res.json(users);
});

// Get a user's profile, friendship status with current user, and recent posts
router.get("/:id", requireAuth, (req: Request, res: Response): void => {
  const userId = (req as AuthRequest).userId;
  const targetId = parseInt(req.params.id);

  const user = db
    .prepare("SELECT id, username, created_at FROM users WHERE id = ?")
    .get(targetId) as Record<string, unknown> | undefined;

  if (!user) {
    res.status(404).json({ error: "User not found" });
    return;
  }

  const friendship = db
    .prepare(
      `
        SELECT * FROM friendships
        WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)
      `,
    )
    .get(userId, targetId, targetId, userId);

  const posts = db
    .prepare(
      `
        SELECT p.*, u.username FROM posts p
        JOIN users u ON p.user_id = u.id
        WHERE p.user_id = ?
        ORDER BY p.created_at DESC LIMIT 20
      `,
    )
    .all(targetId);

  res.json({ ...user, friendship, posts });
});

// Send a friend request and notify the recipient via SSE
router.post(
  "/:id/friend-request",
  requireAuth,
  (req: Request, res: Response): void => {
    const userId = (req as AuthRequest).userId;
    const targetId = parseInt(req.params.id);

    if (userId === targetId) {
      res.status(400).json({ error: "Cannot add yourself" });
      return;
    }

    try {
      const result = db
        .prepare(
          "INSERT INTO friendships (requester_id, addressee_id) VALUES (?, ?)",
        )
        .run(userId, targetId);
      res.json({ ok: true });

      const requester = db
        .prepare("SELECT username FROM users WHERE id = ?")
        .get(userId) as { username: string };

      sendToUser(targetId, "new_friend_request", {
        id: result.lastInsertRowid,
        user_id: userId,
        username: requester.username,
        created_at: Math.floor(Date.now() / 1000),
      });
    } catch {
      res.status(409).json({ error: "Request already exists" });
    }
  },
);

// Accept a pending friend request and notify the requester via SSE
router.post(
  "/friend-requests/:id/accept",
  requireAuth,
  (req: Request, res: Response): void => {
    const userId = (req as AuthRequest).userId;
    const requestId = parseInt(req.params.id);

    const friendship = db
      .prepare(
        "SELECT * FROM friendships WHERE id = ? AND addressee_id = ? AND status = ?",
      )
      .get(requestId, userId, "pending");

    if (!friendship) {
      res.status(404).json({ error: "Request not found" });
      return;
    }

    db.prepare("UPDATE friendships SET status = ? WHERE id = ?").run(
      "accepted",
      requestId,
    );
    res.json({ ok: true });

    const fr = friendship as { requester_id: number };

    const accepter = db
      .prepare("SELECT username FROM users WHERE id = ?")
      .get(userId) as { username: string };

    sendToUser(fr.requester_id, "friend_request_accepted", {
      user_id: userId,
      username: accepter.username,
    });
  },
);

export default router;
