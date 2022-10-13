package config_file

import com.charleskorn.kaml.Yaml
import java.io.File

object RearrangeClassMembersConfigReader {
    fun readConfig(file: File): RearrangeClassMembersConfig {
        if(!file.exists()){
            throw IllegalArgumentException("File ${file.absolutePath} does not exist")
        }
        return Yaml.default.decodeFromStream(RearrangeClassMembersConfig.serializer(), file.inputStream())
    }
}
