(ns app.core)

(defn main []
  (let [c (.. js/document (createElement "DIV"))]
    (aset c "innerHTML" "<p>im dynamic lol</p>")
    (.. js/document (getElementById "container") (appendChild c)))) 
