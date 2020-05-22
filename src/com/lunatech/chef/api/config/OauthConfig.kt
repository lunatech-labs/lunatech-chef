package com.lunatech.chef.api.config

import com.typesafe.config.Config
import io.github.config4k.getValue

data class OauthConfig(
  val name: String,
  val authorizeUrl: String,
  val accessTokenUrl: String,
  val clientId: String,
  val clientSecret: String,
  val defaultScopes: List<String>
) {
    companion object {
        fun fromConfig(config: Config): OauthConfig {
            val name: String by config
            val authorizeUrl: String by config
            val accessTokenUrl: String by config
            val clientId: String by config
            val clientSecret: String by config
            val defaultScopes: List<String> by config

            return OauthConfig(
                name,
                authorizeUrl,
                accessTokenUrl,
                clientId,
                clientSecret,
                defaultScopes
            )
        }
    }
}
