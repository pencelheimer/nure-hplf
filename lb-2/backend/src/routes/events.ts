import { Router, Request, Response } from "express";
import { getSession } from "../sessions";
import { addClient, removeClient } from "../sse";

const router = Router();

// Open a persistent SSE connection for real-time push events
router.get("/", (req: Request, res: Response): void => {
  const token = req.query.token as string | undefined;
  if (!token) {
    res.status(401).end();
    return;
  }
  const userId = getSession(token);
  if (!userId) {
    res.status(401).end();
    return;
  }

  res.setHeader("Content-Type", "text/event-stream");
  res.setHeader("Cache-Control", "no-cache");
  res.setHeader("Connection", "keep-alive");
  res.setHeader("X-Accel-Buffering", "no");
  res.flushHeaders();

  const keepAlive = setInterval(() => res.write(": ping\n\n"), 25000);
  addClient(userId, res);

  req.on("close", () => {
    clearInterval(keepAlive);
    removeClient(userId, res);
  });
});

export default router;
