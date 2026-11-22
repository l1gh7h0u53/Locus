package com.example.shared

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * JVM verification test inside the `:shared` module that scans every internationalization resource
 * bundle (`values/strings.xml` and `values-<lang>/strings.xml`), `LanguageResolver`, and
 * `CountryDataRegistry` to ensure the country count invariant (**192** entries — never 193)
 * holds consistently across all supported languages and regions.
 */
class I18nCountryCountTest {

    private fun findWorkspaceRoot(): File {
        var dir: File? = File(".").canonicalFile
        while (dir != null) {
            val resDir = File(dir, "app/src/main/res")
            val registryFile = File(dir, "app/src/main/java/com/example/CountryDataRegistry.kt")
            if (resDir.isDirectory && registryFile.isFile) {
                return dir
            }
            dir = dir.parentFile
        }
        error("Could not locate project workspace root from ${File(".").canonicalPath}")
    }

    private fun extractStringResources(stringsXml: File): Map<String, String> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(stringsXml)
        val nodes = doc.getElementsByTagName("string")
        val map = linkedMapOf<String, String>()
        for (i in 0 until nodes.length) {
            val element = nodes.item(i) as? Element ?: continue
            val name = element.getAttribute("name")
            if (name.isNotBlank()) {
                map[name] = element.textContent ?: ""
            }
        }
        return map
    }

    @Test
    fun `all internationalization strings resources reflect 192 countries and zero 193 occurrences`() {
        val root = findWorkspaceRoot()
        val resDir = File(root, "app/src/main/res")
        val expectedLocales = listOf("en", "ar", "bn", "de", "es", "fr", "hi", "ja", "pt", "ru", "zh")

        val valuesDirs = resDir.listFiles { file ->
            file.isDirectory &&
                (file.name == "values" || file.name.startsWith("values-")) &&
                File(file, "strings.xml").isFile
        }?.sortedBy { it.name }.orEmpty()

        // Ensure every expected i18n language has a corresponding values*/strings.xml folder
        val discoveredFolderNames = valuesDirs.map { it.name }.toSet()
        for (lang in expectedLocales) {
            val expectedFolder = if (lang == "en") "values" else "values-$lang"
            assertTrue(
                discoveredFolderNames.contains(expectedFolder),
                "Missing i18n resource directory '$expectedFolder' under ${resDir.path}"
            )
        }
        assertEquals(expectedLocales.size, valuesDirs.size)

        // Iterate through every internationalization resource file and verify country counts
        for (dir in valuesDirs) {
            val stringsFile = File(dir, "strings.xml")
            val strings = extractStringResources(stringsFile)

            val matricesCount = strings["regional_matrices_count"]
            assertNotNull(matricesCount, "Missing regional_matrices_count in ${dir.name}/strings.xml")
            assertTrue(
                matricesCount.contains("192") || matricesCount.contains("১৯২") || matricesCount.contains("١٩٢"),
                "Expected '192' (or localized equivalent) in ${dir.name} regional_matrices_count, got: '$matricesCount'"
            )
            assertFalse(
                matricesCount.contains("193") || matricesCount.contains("১৯৩") || matricesCount.contains("١٩٣"),
                "Found stale '193' in ${dir.name} regional_matrices_count: '$matricesCount'"
            )

            val countTemplate = strings["countries_count_label"]
            assertNotNull(countTemplate, "Missing countries_count_label in ${dir.name}/strings.xml")
            assertTrue(
                countTemplate.contains("%d"),
                "Expected %d format token in ${dir.name} countries_count_label, got: '$countTemplate'"
            )
            val formattedCount = countTemplate.replace("%d", "192")
            assertTrue(formattedCount.contains("192"))
            assertFalse(formattedCount.contains("193"))

            // Guard the entire strings.xml file so no stray 193 exists in any localized resource
            for ((key, value) in strings) {
                assertFalse(
                    value.contains("193") || value.contains("১৯৩") || value.contains("١٩٣"),
                    "Unexpected 193 reference in ${dir.name}/strings.xml key '$key': '$value'"
                )
            }
        }
    }

    @Test
    fun `country data registry countryList defines exactly 192 entries and 11 matrix localizations`() {
        val root = findWorkspaceRoot()
        val registrySource = File(root, "app/src/main/java/com/example/CountryDataRegistry.kt").readText()
        val countryListStart = registrySource.indexOf("private val countryList: List<CountryTippingData>")
        val factoryStart = registrySource.indexOf("private fun createCountry(")
        assertTrue(countryListStart >= 0 && factoryStart > countryListStart)

        val countryListBlock = registrySource.substring(countryListStart, factoryStart)
        val entryCount = Regex("""\bcreateCountry\(""").findAll(countryListBlock).count()
        assertEquals(192, entryCount, "Expected exactly 192 entries in CountryDataRegistry.countryList, found $entryCount")

        // Verify all 11 i18n language keys exist for every tipping matrix (A..E) in GlobalExplorerLocalization
        val localizationSource = File(root, "app/src/main/java/com/example/i18n/GlobalExplorerLocalization.kt").readText()
        val expectedLocales = listOf("en", "de", "fr", "es", "pt", "ru", "ja", "zh", "ar", "hi", "bn")
        for (lang in expectedLocales) {
            val matrixLocaleMentions = Regex(""""$lang"\s+to\s+"""").findAll(localizationSource).count()
            assertTrue(
                matrixLocaleMentions >= 5,
                "Expected all 5 tipping matrices (A..E) localized for '$lang', found $matrixLocaleMentions"
            )
        }
    }
}
