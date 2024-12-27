- Configure the environment
  ```
  export JAVA_HOME=export JAVA_HOME=~/bin/graalvm-jdk-21.0.5+9.1/Contents/Home
  export PATH=$JAVA_HOME/bin:$PATH
  ```
- Build the executable
  ```
  mvn clean package
  ```

- Generate the native image data
  ```
  java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image/com.github.ferstl/skydemon-exporter -jar target/skydemon-exporter-1.0.0-SNAPSHOT.jar /some/empty/directory
  ```
- Build the native image
    ```
  mvn clean package
  ```

