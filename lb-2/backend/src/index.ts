import express from "express";
import compression from "compression";
import path from "path";
import authRouter from "./routes/auth";
import usersRouter from "./routes/users";
import postsRouter from "./routes/posts";
import messagesRouter from "./routes/messages";
import searchRouter from "./routes/search";
import eventsRouter from "./routes/events";

const app = express();
const PORT = process.env.PORT ?? 3001;

app.use(compression());
app.use(express.json());

app.use("/api/auth", authRouter);
app.use("/api/users", usersRouter);
app.use("/api/posts", postsRouter);
app.use("/api/messages", messagesRouter);
app.use("/api/search", searchRouter);
app.use("/api/events", eventsRouter);

// NOTE(pencelheimer): serve compiled frontend if exists
const distPath = path.join(__dirname, "../../frontend/dist");
app.use(express.static(distPath));
app.get("*", (_req, res) => {
  res.sendFile(path.join(distPath, "index.html"));
});

app.listen(PORT, () => console.log(`Server: http://localhost:${PORT}`));
