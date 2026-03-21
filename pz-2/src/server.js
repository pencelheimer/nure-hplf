import { Database } from "bun:sqlite";

const db = new Database("chat.sqlite");
db
  .query("CREATE TABLE IF NOT EXISTS rooms (name TEXT UNIQUE)")
  .run();
db
  .query("CREATE TABLE IF NOT EXISTS messages (room TEXT, text TEXT, sender TEXT, time INTEGER)")
  .run();

try {
  db
    .query("INSERT INTO rooms (name) VALUES ('Головна')")
    .run();
} catch { }

await Bun.build({
  entrypoints: ['./app.jsx'],
  outdir: './build',
});

Bun.serve({
  port: 6969,
  async fetch(req) {
    const url = new URL(req.url);
    const method = req.method;

    if (url.pathname === "/")
      return new Response(Bun.file("index.html"));

    if (url.pathname === "/app.js")
      return new Response(Bun.file("build/app.js"));

    if (url.pathname === "/api/rooms" && method === "GET") {
      const rooms = db.query("SELECT name FROM rooms").all().map(r => r.name);
      return Response.json(rooms);
    }

    if (url.pathname === "/api/rooms" && method === "POST") {
      const { name } = await req.json();
      try { db.query("INSERT INTO rooms (name) VALUES (?)").run(name); } catch { }
      return Response.json({ success: true });
    }

    if (url.pathname === "/api/messages" && method === "GET") {
      const room = url.searchParams.get("room") || "Головна";
      const search = url.searchParams.get("search") || "";
      const sort = url.searchParams.get("sort") || "oldest";

      const order = sort === "newest" ? "DESC" : "ASC";

      const msgs = db
        .query(`SELECT * FROM messages WHERE room = ? AND text LIKE ? ORDER BY time ${order}`)
        .all(room, `%${search}%`);

      return Response.json(msgs);
    }

    if (url.pathname === "/api/messages" && method === "POST") {
      const { room, text, sender } = await req.json();

      db
        .query("INSERT INTO messages (room, text, sender, time) VALUES (?, ?, ?, ?)")
        .run(room, text, sender, Date.now());

      return Response.json({ success: true });
    }

    return new Response("Not found", { status: 404 });
  }
});
