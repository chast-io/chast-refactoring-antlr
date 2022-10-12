package refactorings.rearrange_class_members

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import refactorings.rearrange_class_members.config_file.RearrangeClassMembersConfig
import java.util.*

abstract class RearrangeClassMembersLanguageProcessorProvider<ClassBody : ParseTree, Declaration : ParserRuleContext> {
    abstract fun getProcessor(config: RearrangeClassMembersConfig): RearrangeClassMembersLanguageProcessor<ClassBody, Declaration>

    protected fun getDeclarationTypeFilters(
        config: RearrangeClassMembersConfig,
        languageKey: String,
        memberFilterMap: Map<String, (Declaration) -> Boolean>,
        visibilityMap: Map<String, (Declaration) -> Boolean>
    ): List<(Declaration) -> Boolean> {
        val declarationTypeFilters = LinkedList<(Declaration) -> Boolean>()

        val javaConfig = config.getLanguageConfig(languageKey)
        javaConfig.membersOrder.forEach { memberConfig ->
            if (!memberFilterMap.containsKey(memberConfig.member)) {
                throw IllegalArgumentException("Unknown member type: ${memberConfig.member}. Available types: ${memberFilterMap.keys}")
            }

            val memberFilterFunction = memberFilterMap[memberConfig.member]!!
            if (memberConfig.visibilityOrder.isNotEmpty()) {

                val visibilities = memberConfig.visibilityOrder.map { visibility ->
                    if (!visibilityMap.containsKey(visibility)) {
                        throw IllegalArgumentException("Unknown visibility type: $visibility. Available visibilities: ${visibilityMap.keys}")
                    }
                    visibilityMap[visibility]!!
                }

                declarationTypeFilters.addAll(
                    visibilityExtension(
                        memberFilterFunction,
                        visibilities
                    )
                )

            } else {
                declarationTypeFilters.add(memberFilterFunction)
            }
        }

        return declarationTypeFilters
    }

    protected fun visibilityExtension(
        nested: (Declaration) -> Boolean,
        order: List<(Declaration) -> Boolean>
    ): List<(Declaration) -> Boolean> {
        return order.map { selector -> { selector(it) && nested(it) } }
    }
}
