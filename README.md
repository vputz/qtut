# qtut

Example project playing with Clojurescript, boot, and various visualizations to
make an interactive quantum computer simulator.

## Development

In a terminal do:

```bash
boot serve -d target/ watch speak reload cljs-repl cljs -sO none
```

This builds a pipeline for your project:

* **`serve`** Starts a local web server.  This task comes from the
  [pandeiro/boot-http](https://github.com/pandeiro/boot-http) library.
  * **`-d`** Use `target/` as the document root

* **`watch`** Starts incremental build loop. Project will be rebuilt when source
  files change.

* **`speak`** Audible notification (plays a sound file) for each build iteration,
  notifying of errors or warnings when appropriate.

* **`reload`** Starts live-reload websocket server and connects browser client
  to it. Resources (stylesheets, images, HTML, JavaScript) in the page are
  reloaded when they change.

* **`cljs`** Compiles ClojureScript namespaces to JavaScript.
  * **`-s`** Create source maps for compiled JavaScript files.
  * **`-O none`** Use optimizations `none` (no [GClosure][gclosure] compiler pass).

You can view the generated content by opening
[http://localhost:3000/index.html](http://localhost:3000/index.html)
in your browser.


## Start Browser REPL

With the build pipeline humming in the background, you can connect to the running nREPL
server with either your IDE or at the command line in a new terminal:

```bash
boot repl --client
```

Then, you can start a CLJS REPL:

```clojure
boot.user=> (start-repl)
```

Reload the page in your browser.  Your REPL is now connected to the page.

## License

Copyright © 2017 VPutz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

