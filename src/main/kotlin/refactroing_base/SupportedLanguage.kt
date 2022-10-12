package refactroing_base

enum class SupportedLanguage(val extension: String) {
    JAVA(".java"),
    PYTHON(".py"),
    CSharp(".cs");

    companion object {
        public fun getLanguageFromExtension(extension: String): SupportedLanguage {
            try {
                return SupportedLanguage.valueOf(extension)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Unsupported language: $extension")
            }
        }
    }
}


