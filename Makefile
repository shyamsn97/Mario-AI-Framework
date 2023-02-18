build-interactive:
	javac -sourcepath src -d out src/PlayLevel.java
	cd out/; jar cvfm PlayLevel.jar ManifestPlayLevel.txt PlayLevel.class *; mv PlayLevel.jar ../; cd ../

build-astar:
	javac -sourcepath src -d out src/PlayAstar.java
	cd out/; jar cvfm PlayAstar.jar ManifestPlayAstar.txt PlayAstar.class *; mv PlayAstar.jar ../; cd ../
