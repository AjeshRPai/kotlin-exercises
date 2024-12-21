package coroutines.comment.commentservice

import domain.comment.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: UserService,
    private val commentFactory: CommentFactory
) {
    suspend fun addComment(
        token: String,
        collectionKey: String,
        body: AddComment
    ) {
        val user = userService.findUser(token)
        val comment = commentFactory.toCommentDocument(user.id, collectionKey, body)
        commentRepository.addComment(comment)
    }

    suspend fun getComments(
        collectionKey: String
    ): CommentsCollection {
        return coroutineScope {
            val comments = commentRepository.getComments(collectionKey)
            val commentElements = comments.map { comment ->
                async {
                    val user = userService.findUserById(comment.userId)
                    CommentElement(
                        id = comment._id,
                        collectionKey = comment.collectionKey,
                        user = user,
                        comment = comment.comment,
                        date = comment.date
                    )
                }
            }.awaitAll()
            CommentsCollection(collectionKey, commentElements)
        }
    }
}
