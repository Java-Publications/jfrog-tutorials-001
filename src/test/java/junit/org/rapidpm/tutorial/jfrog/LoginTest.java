package junit.org.rapidpm.tutorial.jfrog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.DownloadableArtifact;
import org.jfrog.artifactory.client.Repositories;
import org.jfrog.artifactory.client.model.*;
import org.jfrog.artifactory.client.model.impl.LocalRepositoryImpl;
import org.jfrog.artifactory.client.model.impl.RepositoryTypeImpl;
import org.jfrog.artifactory.client.model.repository.settings.impl.GenericRepositorySettingsImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rapidpm.frp.functions.CheckedFunction;
import org.rapidpm.frp.model.Result;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class LoginTest {


  private static final String USERNAME = "admin";
  private static final String PASSWORD = "wyJzd4X7hD7e5zD";
  private static final String URL      = "http://jfrog-repo.sven-ruppert.de:8081/artifactory";


//  @Container
//  public GenericContainer artifactory = new GenericContainer<>("jfrog-repo.sven-ruppert.de:8081/docker/jfrog/artifactory-oss:latest")
//      .withImagePullPolicy(PullPolicy.alwaysPull())
//      .withExposedPorts(8081);


  @Test @Disabled
  void startupTest() {
//    final GenericContainer genericContainer = new GenericContainer<>(
//        "docker.bintray.io/jfrog/artifactory-oss:latest")
//        .withImagePullPolicy(PullPolicy.alwaysPull())
//        .withExposedPorts(8081);
//    genericContainer.start();
//
//
//    String containerIpAddress = genericContainer.getContainerIpAddress();
//    System.out.println("containerIpAddress = " + containerIpAddress);


    Artifactory artifactory = ArtifactoryClientBuilder.create()
//                                                      .setUrl("http://" + containerIpAddress + ":8081/artifactory")
                                                      .setUrl("http://127.0.0.1:8081/artifactory")
                                                      .setUsername(USERNAME)
                                                      .setPassword("1234567890")
                                                      .build();
    Assertions.assertNotNull(artifactory);
    Repositories repositories = artifactory.repositories();
    Assertions.assertNotNull(repositories);

    //assume a local generic repo was created manually
    List<LightweightRepository> localRepos = repositories.list(RepositoryTypeImpl.LOCAL);
    Assertions.assertNotNull(localRepos);
    Assertions.assertFalse(localRepos.isEmpty());
    localRepos.forEach(r -> System.out.println("[" + r.getType() + "] " + r.getUrl()));
//    localRepos.forEach(r -> System.out.println(r.getUrl()));

    String repo             = "generic-local";
    String destPath         = "/org.rapidpm/data/demo/";
    String fileNameToUpload = "README.md";

//    Path path = Paths.get(fileNameToUpload);
//      Files.write(path, Collections.singleton("This is an example line"), StandardCharsets.UTF_8);
    java.io.File file = new java.io.File(fileNameToUpload);
    Assertions.assertTrue(file.exists());
    File upload = artifactory.repository(repo)
                             .upload(destPath + "/" + fileNameToUpload, file)
                             .bySha1Checksum()
                             .doUpload();
    List<RepoPath> repoPaths = artifactory.searches()
                                          .repositories(repo)
                                          .artifactsByName(fileNameToUpload)
                                          .doSearch();

    Assertions.assertFalse(repoPaths.isEmpty());


    long count = repoPaths.stream()
                          .peek(repoPath -> System.out.println(repoPath.getItemPath()))
                          .peek(repoPath -> System.out.println(repoPath.getRepoKey()))
                          .peek(System.out::println)
                          .count();
    Assert.assertEquals(1, count);

    repoPaths.stream()
             .map(rp -> artifactory.repository(rp.getRepoKey()).download(rp.getItemPath()))
             .map(downloadableArtifact -> ((CheckedFunction<DownloadableArtifact, InputStream>) o -> downloadableArtifact.doDownload()).apply(downloadableArtifact))
             .map(Result::get)
             .map(is -> {
               ByteArrayOutputStream buffer = new ByteArrayOutputStream();
               int nRead;
               byte[] data = new byte[1024];
               while (true) {
                 try {
                   if (!((nRead = is.read(data, 0, data.length)) != -1)) break;
                   buffer.write(data, 0, nRead);
                   buffer.flush();
                 } catch (IOException e) {
                   e.printStackTrace();
                 }
               }
               byte[] byteArray = buffer.toByteArray();

               return new String(byteArray, StandardCharsets.UTF_8);
             })
             .forEach(System.out::println);


//    GenericRepositorySettingsImpl settings = new GenericRepositorySettingsImpl();
//    Repository repository = artifactory.repositories()
//                                       .builders()
//                                       .localRepositoryBuilder()
//                                       .key("generic-repo-locale")
//                                       .description("new generic local repository")
//                                       .repositorySettings(settings)
//                                       .build();
//    String result = artifactory.repositories()
//                          .create(1, repository);
//    System.out.println("result = " + result);


  }

  @Test @Disabled
  void login001Test() {
    Artifactory artifactory = ArtifactoryClientBuilder.create()
                                                      .setUrl(URL)
                                                      .setUsername(USERNAME)
                                                      .setPassword(PASSWORD)
                                                      .build();
    Assertions.assertNotNull(artifactory);
    Repositories repositories = artifactory.repositories();
    Assertions.assertNotNull(repositories);
    List<LightweightRepository> virtualRepos = repositories.list(RepositoryTypeImpl.VIRTUAL);
    Assertions.assertNotNull(virtualRepos);
    Assertions.assertFalse(virtualRepos.isEmpty());

    virtualRepos.forEach(r -> System.out.println(r.getUrl()));
  }
}
