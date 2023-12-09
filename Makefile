compile: src/Main.java
	javac -d bin/ -cp bin/ src/Main.java
	
1: compile # 1st phase of the project (edge weight)
	java -cp bin/ Main false false false

2: compile # 2nd phase of the project (A*)
	java -cp bin/ Main true false false

d2: compile # 2nd phase of the project (Dijkstra)
	java -cp bin/ Main true true false

debug: compile # 2st phase of the project (debug)
	java -cp bin/ Main true false true