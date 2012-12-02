(ns lyn.example1)

(comment
 




--- JavaScript ---
function hello(name) {
 if (name)
   return "Hello " + name;
 else
   return "Hello World!";
}

hello("Foo");
 
 
)



(defn hello [name]
  (if name
    (str "Hello " name)
    "Hello World!"))

(println (hello "Bar"))


;-------------------------------------


(def variable1 "a")





(def myFunc
  (fn [a b]
    (+ a b)))







(defn myFunc 
  [a b] 
  (+ a b))








