(ns lyn.example1)

(comment
 
--- JavaScript ---
function hello(name)
{
 if (name)
   return "Hello " + name;
 else
 	return "Hello World!";
}
hello("Zero Cool");
 
 
)



(defn hello [name]
  (if name
		(str "Hello" name)
		"Hello World!"))

(hello "Crash Override")