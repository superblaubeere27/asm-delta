# asm-delta
A library for generating and applying patches for Java class files
## Usage
### Generate patch
``java -jar asm-delta.jar --jar1 <original-jar> --jar2 <jar-with-changes> -o <patch-file>``
### For java agent
``java -jar asm-delta.jar --jar1 <original-jar> --jar2 <jar-with-changes> --forAgent --added-classes <output-for-new-classes> -o <patch-file>``
