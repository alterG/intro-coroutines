package tasks

import contributors.*
import kotlinx.coroutines.*
import samples.log

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgRepos(req.org) // Executes request and suspend
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    return repos.flatMap { repo ->
        service
            .getRepoContributors(req.org, repo.name) // Executes request and suspend
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}

fun main() = runBlocking {
    val deferredList: List<Deferred<Int>> = (1..3).map {
        async {
            delay(1000L*it)
            log("$it")
            it
        }
    }
    deferredList.awaitAll().forEach { log("meow $it") }
    log("All done")
}