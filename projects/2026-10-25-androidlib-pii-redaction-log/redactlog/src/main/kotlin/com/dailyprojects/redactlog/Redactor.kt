package com.dailyprojects.redactlog

/** One pattern to scrub. [replacement] may use `$1`-style group references. */
class Rule(
    val name: String,
    val pattern: Regex,
    val replacement: String,
    /** Optional second check on each match; return false to leave that match alone. */
    val accepts: (String) -> Boolean = { true },
)

/** Applies [rules] in order. Immutable and thread-safe. */
class Redactor(val rules: List<Rule> = DefaultRules.all) {

    fun redact(text: String): String =
        rules.fold(text) { acc, rule ->
            rule.pattern.replace(acc) { m ->
                if (rule.accepts(m.value)) m.value.replace(rule.pattern, rule.replacement) else m.value
            }
        }

    /** Names of the rules that would fire on [text]; handy for tests and metrics. */
    fun matchedRules(text: String): List<String> =
        rules.filter { r -> r.pattern.findAll(text).any { r.accepts(it.value) } }.map { it.name }

    operator fun plus(extra: Rule) = Redactor(rules + extra)
}
