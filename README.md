# AromaJ

**AromaJ** is a lightweight, fast, and developer-friendly Java web framework inspired by modern Node.js frameworks like Aroma.js and Express. AromaJ provides an elegant routing system, middleware support, static file serving, JSON response utilities, and more.

---

## Features

- Minimal and clean syntax
- Express-style routing
- Middleware support
- JSON request/response utilities
- Static file serving
- Cookie and session support
- Fully written in Java

---

## Installation

### Maven

Add this to your `pom.xml` (once published to GitHub Packages):

```xml
<dependency>
  <groupId>io.github.aaveshdev</groupId>
  <artifactId>aromaj</artifactId>
  <version>1.0.0</version>
</dependency>
```

Make sure to include the GitHub Packages repository:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/aaveshdev/aromaj</url>
  </repository>
</repositories>
```

---

## Quick Start

```java
import com.aromaj.Aroma;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Aroma app = new Aroma();

        app.get("/", (req, res, next) -> {
            res.send("Welcome to AromaJ!");
        });

        app.get("/json", (req, res, next) -> {
            res.json(Map.of("message", "Hello from JSON!"));
        });

        app.listen(3000, () -> {
            System.out.println("Server running on http://localhost:3000");
        });
    }
}
```

---

## Middleware Example

```java
app.use((req, res, next) -> {
    System.out.println("Request received at " + req.getPath());
    next.run();
});
```

---

## Static Files

```java
app.staticFiles("public");
```

Now files in the `public/` folder are served at `http://localhost:3000/<filename>`.

---

## Documentation

👉 Full documentation and examples coming soon at:  
https://github.com/aaveshdev/aromaj/wiki

---

## 🛠️ Roadmap

- [x] Routing
- [x] Middleware
- [x] JSON support
- [x] Static files
- [x] Cookie/session management
- [ ] Template engine
- [ ] CLI tools and project scaffolding

---

## Contributing

Contributions are welcome! Please open issues or pull requests. For major changes, open a discussion first.

---

## License

MIT License © 2025 [Aavesh Dev](https://github.com/aaveshdev)
