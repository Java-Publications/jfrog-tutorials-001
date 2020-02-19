package demo;

import org.testcontainers.containers.GenericContainer;

public class RepoRunner {
  public static void main(String[] args) throws InterruptedException {
    final  GenericContainer artifactory = new GenericContainer<>("jfrog-repo.sven-ruppert.de:8081/docker/jfrog/artifactory-oss:latest")
        .withExposedPorts(443, 8443, 8080, 8081);

    artifactory.start();

    Thread.currentThread().join(); // keep the main running
  }

}
