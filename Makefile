build:
	javac -sourcepath src -d out src/PlayLevel.java
	cd out/; jar cvfm Mario.jar Manifest.txt PlayLevel.class *; mv Mario.jar ../; cd ../
