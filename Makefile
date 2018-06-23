
ALL=*.java
MAIN_SRC=SwarmChemistry.java
MAIN=SwarmChemistry

n=1

build: $(MAIN_SRC)
	javac $(MAIN_SRC)

run: build
	java $(MAIN) $(n)

clean:
	rm -rf *.class
