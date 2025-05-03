FROM openjdk:21-jdk
ADD ./build/libs/*.jar stock-board.jar
ENTRYPOINT ["java", "-jar", "stock-board.jar"]