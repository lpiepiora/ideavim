/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package org.jetbrains.plugins.ideavim

import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.ex.exExceptionMessage
import com.maddyhome.idea.vim.options.OptionScope
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo

/**
 * @author Alex Plate
 *
 * This test case helps you to test IdeaVim options
 *
 * While inheriting from this class you should specify (via constructor), which options you are going to test.
 *   After that each test method in this class should contains [VimOptionTestConfiguration] annotation with
 *   description of which values of option should be set before starting test.
 *
 * e.g.
 * ```
 * @VimOptionTestConfiguration(VimTestOption("keymodel", LIST, ["startsel"]), VimTestOption("selectmode", LIST, ["key"]))
 * ```
 *
 * If you want to keep default configuration, you can put [VimOptionDefaultAll] annotation
 */
abstract class VimOptionTestCase(option: String, vararg otherOptions: String) : VimTestCase() {
  private val options: Set<String> = setOf(option, *otherOptions)

  @BeforeEach
  override fun setUp(testInfo: TestInfo) {
    super.setUp(testInfo)
    val testMethod = this.testInfo.testMethod.get()
    if (!testMethod.isAnnotationPresent(VimOptionDefaultAll::class.java)) {
      if (!testMethod.isAnnotationPresent(VimOptionTestConfiguration::class.java)) kotlin.test.fail("You should add VimOptionTestConfiguration with options for this method")

      val annotationValues = testMethod.getDeclaredAnnotation(VimOptionTestConfiguration::class.java) ?: run {
        kotlin.test.fail("You should have at least one VimOptionTestConfiguration annotation. Or you can use VimOptionDefaultAll")
      }
      val defaultOptions = testMethod.getDeclaredAnnotation(VimOptionDefault::class.java)?.values ?: emptyArray()

      val annotationsValueList = annotationValues.value.map { it.optionName } + defaultOptions
      val annotationsValueSet = annotationsValueList.toSet()
      if (annotationsValueSet.size < annotationsValueList.size) kotlin.test.fail("You have duplicated options")
      if (annotationsValueSet != options) kotlin.test.fail("You should present all options in annotations")

      annotationValues.value.forEach {
        val option = injector.optionGroup.getOption(it.optionName) ?: throw exExceptionMessage("E518", it.optionName)
        when (it.valueType) {
          OptionValueType.STRING -> injector.optionGroup.setOptionValue(option, OptionScope.GLOBAL, VimString(it.value))
          OptionValueType.NUMBER -> injector.optionGroup.setOptionValue(option, OptionScope.GLOBAL, VimInt(it.value))
        }
      }
    }
  }
}

@Target(AnnotationTarget.FUNCTION)
annotation class VimOptionDefaultAll

@Target(AnnotationTarget.FUNCTION)
annotation class VimOptionDefault(vararg val values: String)

@Target(AnnotationTarget.FUNCTION)
annotation class VimOptionTestConfiguration(vararg val value: VimTestOption)

@Target(AnnotationTarget.PROPERTY)
annotation class VimTestOption(
  val optionName: String,
  val valueType: OptionValueType,
  val value: String,
)

enum class OptionValueType {
  STRING,
  NUMBER,
}
