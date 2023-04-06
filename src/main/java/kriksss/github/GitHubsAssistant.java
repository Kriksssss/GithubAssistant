package kriksss.github;

import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GitHubsAssistant {

    private final GitHub gitHub;
    private final IconApp iconApp = new IconApp();
    private final Set<Long> allPrIds = new HashSet<>();

    public GitHubsAssistant() {
        try {
            gitHub = new GitHubBuilder()
                    .withAppInstallationToken(System.getenv("My_Token")).build();
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void init() throws IOException {
        GHMyself myself = gitHub.getMyself();
        String login = myself.getLogin();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    boolean empty = !allPrIds.isEmpty();
                    HashSet<GHPullRequest> newPrs = new HashSet<>();

                    List<RepositoryDescription> reposDesc= myself.getAllRepositories()
                            .values()
                            .stream()
                            .map(repository -> {
                                try {
                                    List<GHPullRequest> prs = repository.queryPullRequests()
                                            .list()
                                            .toList();
                                    Set<Long> prIds = prs.stream().map(GHPullRequest::getId)
                                            .collect(Collectors.toSet());
                                    prIds.removeAll(allPrIds);
                                    allPrIds.addAll(prIds);
                                    prs.forEach(pr -> {
                                        if (prIds.contains(pr.getId())) {
                                            newPrs.add(pr);
                                        }
                                    });
                                    return new RepositoryDescription(
                                            repository.getFullName(),
                                            repository, prs
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList());

                    iconApp.setMenu(login,reposDesc);

                    if (empty) {
                        newPrs.forEach(pullRequest -> {
                            iconApp.showNotification("new PR in"
                                    + pullRequest.getRepository().getFullName(), pullRequest.getTitle());
                        });
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, 1000);
    }
}
