enum class SupportedLanguage {
    Java,
    Python,
    CSharp,
    Kotlin, ;

    companion object {
        public fun getLanguageFromExtension(extension: String): SupportedLanguage {
            with(extension) {
                return when {
                    endsWith("java") -> Java
                    endsWith("py") -> Python
                    endsWith("cs") -> CSharp
                    endsWith("kt") -> Kotlin
                    else -> throw IllegalArgumentException("Unsupported extension: $extension")
                }
            }
        }
    }
}


