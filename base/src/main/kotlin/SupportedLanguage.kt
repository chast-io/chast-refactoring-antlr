enum class SupportedLanguage {
    Java,
    Python,
    CSharp,;

    companion object {
        public fun getLanguageFromExtension(extension: String): SupportedLanguage {
            with(extension) {
                return when {
                    endsWith("java") -> Java
                    endsWith("py") -> Python
                    endsWith("cs") -> CSharp
                    else -> throw IllegalArgumentException("Unsupported extension: $extension")
                }
            }
        }
    }
}


