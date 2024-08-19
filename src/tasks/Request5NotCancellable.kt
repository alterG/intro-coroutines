package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgRepos(req.org) // Executes request and suspend
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    val deferred = repos.map { repo ->
        GlobalScope.async {
            log("starting loading for ${repo.name}")
            delay(3000)
            service
                .getRepoContributors(req.org, repo.name) // Executes request and suspend
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    return deferred.awaitAll().flatten().aggregate()
}