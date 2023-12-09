compile: src/Main.java
	javac -d bin/ -cp bin/ src/Main.java
	
1: compile
	java -cp bin/ Main false false

2: compile
	java -cp bin/ Main true false

d1: compile
	java -cp bin/ Main false true

d2: compile
	java -cp bin/ Main true true