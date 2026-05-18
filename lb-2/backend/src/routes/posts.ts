import { Router, Request, Response } from "express";
import db from "../db";
import { requireAuth, AuthRequest } from "../middleware/auth";
import { sendToUser } from "../sse";

const router = Router();

// Return the feed: own posts and accepted friends' posts with comment counts
router.get("/", requireAuth, (req: Request, res: Response): void => {
  const userId = (req as AuthRequest).userId;

  const posts = db
    .prepare(
      `
        SELECT p.*, u.username,
          (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) as comment_count
        FROM posts p
        JOIN users u ON p.user_id = u.id
        WHERE p.user_id = ? OR p.user_id IN (
          SELECT CASE WHEN requester_id = ? THEN addressee_id ELSE requester_id END
          FROM friendships
          WHERE (requester_id = ? OR addressee_id = ?) AND status = 'accepted'
        )
        ORDER BY p.created_at DESC LIMIT 50
      `,
    )
    .all(userId, userId, userId, userId);
  res.json(posts);
});

// Create a post and push new_post SSE event to the author and all friends
router.post("/", requireAuth, (req: Request, res: Response): void => {
  const userId = (req as AuthRequest).userId;
  const { content } = req.body as { content?: string };

  if (!content?.trim()) {
    res.status(400).json({ error: "Content required" });
    return;
  }

  const result = db
    .prepare("INSERT INTO posts (user_id, content) VALUES (?, ?)")
    .run(userId, content.trim());

  const post = db
    .prepare(
      "SELECT p.*, u.username FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = ?",
    )
    .get(result.lastInsertRowid);
  res.json(post);

  const friends = db
    .prepare(
      `
        SELECT CASE WHEN requester_id = ? THEN addressee_id ELSE requester_id END as friend_id
        FROM friendships WHERE (requester_id = ? OR addressee_id = ?) AND status = 'accepted'
      `,
    )
    .all(userId, userId, userId) as { friend_id: number }[];

  sendToUser(userId, "new_post", post);
  for (const { friend_id } of friends) sendToUser(friend_id, "new_post", post);
});

// Get a single post with all its comments
router.get("/:id", requireAuth, (req: Request, res: Response): void => {
  const post = db
    .prepare(
      "SELECT p.*, u.username FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = ?",
    )
    .get(req.params.id);

  if (!post) {
    res.status(404).json({ error: "Not found" });
    return;
  }

  const comments = db
    .prepare(
      "SELECT c.*, u.username FROM comments c JOIN users u ON c.user_id = u.id WHERE c.post_id = ? ORDER BY c.created_at ASC",
    )
    .all(req.params.id);

  res.json({ ...(post as object), comments });
});

// Add a comment to a post and return the updated comment list
router.post(
  "/:id/comments",
  requireAuth,
  (req: Request, res: Response): void => {
    const userId = (req as AuthRequest).userId;
    const { content } = req.body as { content?: string };

    if (!content?.trim()) {
      res.status(400).json({ error: "Content required" });
      return;
    }

    const postId = parseInt(req.params.id);

    db.prepare(
      "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)",
    ).run(postId, userId, content.trim());

    const comments = db
      .prepare(
        "SELECT c.*, u.username FROM comments c JOIN users u ON c.user_id = u.id WHERE c.post_id = ? ORDER BY c.created_at ASC",
      )
      .all(postId);

    res.json(comments);
  },
);

export default router;
