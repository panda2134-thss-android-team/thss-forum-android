package site.panda2134.thssforum.data

import site.panda2134.thssforum.models.Comment


class CommentItemDataSource(private val pageSize: Int = 10) {
    companion object {
        private val _posts = ArrayList<Comment>()
        fun addComment(comment: Comment) { _posts.add(0, comment) }
    }
    private var currentIndex = 0
    val hasNextPage: Boolean get() = kotlin.run {
        _posts.size > currentIndex + pageSize
    }

    fun getPosts(): List<Comment> {
        val ret = _posts.drop(currentIndex).take(pageSize)
        currentIndex += pageSize
        return ret
    }
    fun reset() {
        currentIndex = 0
    }
}