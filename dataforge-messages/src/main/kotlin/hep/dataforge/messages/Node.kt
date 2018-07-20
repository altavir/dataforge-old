package hep.dataforge.messages

import hep.dataforge.Named
import hep.dataforge.names.Name
import hep.dataforge.utils.ReferenceRegistry

interface MessageInterceptor {
    //TODO turn interceptor on or off?

    /**
     * Select message to be intercepted
     */
    fun select(message: Message): Boolean

    /**
     * Process previously selected message. The message should be evaluated on a separate thread
     */
    suspend fun process(message: Message)
}

/**
 * A local or remote execution node
 */
interface Node : Named {
    /**
     * Direct parent of this node. Could be null if it is a root node
     */
    val parent: Node?

    /**
     * A name relative to parent
     */
    override val name: String

    /**
     * Full name relative to root node
     */
    @JvmDefault
    val fullName: Name
        get() = parent?.fullName?.plus(name) ?: Name.ofSingle(name)

    /**
     * add interceptor to this node and return its handler
     */
    @JvmDefault
    fun intercept(pattern: String, action: suspend Node.(Message) -> Unit): MessageInterceptor {
        return object : MessageInterceptor {
            override fun select(message: Message): Boolean {
                return message.target.name.matches(pattern.toRegex())
            }

            override suspend fun process(message: Message) {
                action.invoke(this@Node, message)
            }

        }.also { intercept(it) }
    }

    /**
     * Add existing interception handler to this node
     */
    fun intercept(handler: MessageInterceptor)

    /**
     * Remove existing interception handler if it is present
     */
    fun removeHandler(handler: MessageInterceptor)

    /**
     * Resolve a node assuming its name is absolute
     * FIXME add rules to resolve absolute and relative paths
     */
    fun resolve(target: Target): Node?

    /**
     * Send message to this node
     */
    suspend fun send(message: Message)

    /**
     * Respond to message targeted at this node
     */
    suspend fun respond(message: Message): Message
}

abstract class AbstractNode(override val name: String, override val parent: Node? = null) : Node {

    private val interceptors = ReferenceRegistry<MessageInterceptor>()

    override fun intercept(handler: MessageInterceptor) {
        interceptors.add(handler)
    }

    override fun removeHandler(handler: MessageInterceptor) {
        interceptors.remove(handler)
    }
}

class LocalNode(name: String, parent: Node? = null, val action: suspend Node.(Message) -> Unit) : AbstractNode(name, parent) {
    override fun resolve(target: Target): Node? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun send(message: Message) {
        action.invoke(this, message)
    }

    override suspend fun respond(message: Message): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}