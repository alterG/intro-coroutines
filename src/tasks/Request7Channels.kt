package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org) // Executes request and suspend
            .also { logRepos(req, it) }
            .body() ?: emptyList()

        val channel = Channel<List<User>>()

        repos.forEach { repo ->
            launch {
                val users = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }

        var allUsers = emptyList<User>()
        repeat(repos.size) {
            val newUsers = channel.receive()
            allUsers = (allUsers + newUsers).aggregate()
            updateResults(allUsers, it == repos.lastIndex)
        }
    }
}
