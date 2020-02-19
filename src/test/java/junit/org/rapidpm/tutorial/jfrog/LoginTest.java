package junit.org.rapidpm.tutorial.jfrog;

import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.Repositories;
import org.jfrog.artifactory.client.model.LightweightRepository;
import org.jfrog.artifactory.client.model.impl.RepositoryTypeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static junit.org.rapidpm.tutorial.jfrog.Constants.*;

public class LoginTest {


  @Test
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
