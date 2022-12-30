package com.lunatech.chef.api.auth

import io.ktor.server.application.ApplicationCall

/**
 * https://medium.com/@shrikantjagtap99/role-based-authorization-feature-in-ktor-web-framework-in-kotlin-dda88262a86a
 */
class RoleAuthorization internal constructor(config: Configuration) {

    constructor(provider: RoleBasedAuthorizer) : this(Configuration(provider))

    private var config = config.copy()

    class Configuration internal constructor(provider: RoleBasedAuthorizer) {
        var provider = provider

        internal fun copy(): Configuration = Configuration(provider)
    }

    class RoleBasedAuthorizer {
        internal var authorizationFunction: suspend ApplicationCall.(Set<Role>) -> Boolean = { false }

        fun validate(body: suspend ApplicationCall.(Set<Role>) -> Boolean) {
            authorizationFunction = body
        }
    }

    // fun interceptPipeline(pipeline: ApplicationCallPipeline, roles: Set<Role>) {
    //     pipeline.insertPhaseAfter(ApplicationCallPipeline.Features, authorizationPhase)
    //     pipeline.intercept(authorizationPhase) {
    //         val call = call
    //         config.provider.authorizationFunction(call, roles)
    //     }
    // }

//    companion object Feature : ApplicationPlugin<ApplicationCallPipeline, RoleBasedAuthorizer, RoleAuthorization> {
//        private val authorizationPhase = PipelinePhase("authorization")
//
//        override val key: AttributeKey<RoleAuthorization> = AttributeKey("RoleAuthorization")
//
//        override fun install(
//            pipeline: ApplicationCallPipeline,
//            configure: RoleBasedAuthorizer.() -> Unit
//        ): RoleAuthorization {
//            val configuration = RoleBasedAuthorizer().apply { configure }
//            val feature = RoleAuthorization(configuration)
//
//            return feature
//        }
//    }
}

enum class Role(val roleStr: String) {
    ADMIN("admin"),
    USER("user")
}
