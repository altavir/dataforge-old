package hep.dataforge.messages

import hep.dataforge.names.Name

/**
 * A local or remote execution node
 */
interface Node {
    /**
     * Direct parent of this node. Could be null if it is a root node
     */
    val parent: Node?

    /**
     * Direct children of this node. Could be empty
     */
    val children: Collection<Node>

    /**
     * A name relative to parent
     */
    val name: Name

    /**
     * Full name relative to root node
     */
    val fullName: Name

    fun addInterceptor(mask: String, action: suspend (Message) -> Unit)

    /**
     * Resolve a node assuming its name is absolute
     * FIXME add rules to resolve absolute and relative paths
     */
    fun resolve(target: Target): Node?

    fun send(target: Target, message: Message)

    suspend fun respond(target: Target, message: Message): Message
}