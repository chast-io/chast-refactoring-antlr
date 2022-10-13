package config_file

import kotlinx.serialization.Serializable

@Serializable
data class RearrangeClassMembersConfig(
    val languages: List<RearrangeClassMembersLanguageConfig>
){
    fun getLanguageConfig(languageKey: String): RearrangeClassMembersLanguageConfig {
        val javaConfigs = languages.filter { language -> language.language == languageKey }
        if (javaConfigs.isEmpty()) {
            throw IllegalArgumentException("No configuration for $languageKey")
        }
        if (javaConfigs.size > 1) {
            throw IllegalArgumentException("Multiple configurations for $languageKey")
        }
        return javaConfigs.first()
    }
}

@Serializable
data class RearrangeClassMembersLanguageConfig(
    val language: String,
    val membersOrder: List<RearrangeClassMembersMemberConfig>
)

@Serializable
data class RearrangeClassMembersMemberConfig(
    val member: String,
    val visibilityOrder: List<String> = emptyList()
)
