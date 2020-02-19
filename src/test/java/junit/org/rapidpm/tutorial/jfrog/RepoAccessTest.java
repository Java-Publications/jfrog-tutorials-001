package junit.org.rapidpm.tutorial.jfrog;

import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.DownloadableArtifact;
import org.jfrog.artifactory.client.Repositories;
import org.jfrog.artifactory.client.model.LightweightRepository;
import org.jfrog.artifactory.client.model.RepoPath;
import org.jfrog.artifactory.client.model.impl.RepositoryTypeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rapidpm.frp.functions.CheckedFunction;
import org.rapidpm.frp.model.Result;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static junit.org.rapidpm.tutorial.jfrog.Constants.*;

public class RepoAccessTest {

  @Test
  @Disabled
  void startupTest() {
    Artifactory artifactory = ArtifactoryClientBuilder.create()
                                                      .setUrl(URL)
                                                      .setUsername(USERNAME)
                                                      .setPassword(PASSWORD)
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
    String destPath         = "/de.sven-ruppert/data/demo/";
    String fileNameToUpload = "README.md";

    ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
    baos.writeBytes("# Hello JFrog Artifactory OSS".getBytes());

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    artifactory.repository(repo)
               .upload(destPath + "/" + fileNameToUpload, bais)
               .bySha1Checksum()
               .doUpload();
    List<RepoPath> repoPaths = artifactory.searches()
                                          .repositories(repo)
                                          .artifactsByName(fileNameToUpload)
                                          .doSearch();

    Assertions.assertFalse(repoPaths.isEmpty());
    Assertions.assertEquals(1, repoPaths.size());

    repoPaths.stream()
             .map(rp -> artifactory.repository(rp.getRepoKey())
                                   .download(rp.getItemPath()))
             .map(
                 downloadableArtifact -> ((CheckedFunction<DownloadableArtifact, InputStream>) o -> downloadableArtifact.doDownload()).apply(
                     downloadableArtifact))
             .map(Result::get)
             .map(is -> {
               ByteArrayOutputStream buffer = new ByteArrayOutputStream();
               int                   nRead;
               byte[]                data   = new byte[512];
               while (true) {
                 try {
                   if ((nRead = is.read(data, 0, data.length)) == -1) break;
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

}
