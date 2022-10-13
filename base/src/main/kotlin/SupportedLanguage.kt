enum class SupportedLanguage {
    JAVA,
    PYTHON,
    CSharp;

    companion object {
        public fun getLanguageFromExtension(extension: String): SupportedLanguage {
            with(extension) {
                return when {
                    endsWith("java") -> JAVA
                    endsWith("py") -> PYTHON
                    endsWith("cs") -> CSharp
                    else -> throw IllegalArgumentException("Unsupported extension: $extension")
                }
            }
        }
    }
}


