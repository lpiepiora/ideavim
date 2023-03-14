/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package org.jetbrains.plugins.ideavim.group.visual

import com.intellij.codeInsight.editorActions.BackspaceHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.LogicalPosition
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.VimStateMachine
import com.maddyhome.idea.vim.helper.editorMode
import com.maddyhome.idea.vim.helper.subMode
import com.maddyhome.idea.vim.listener.VimListenerManager
import org.jetbrains.plugins.ideavim.SkipNeovimReason
import org.jetbrains.plugins.ideavim.TestWithoutNeovim
import org.jetbrains.plugins.ideavim.VimTestCase
import org.jetbrains.plugins.ideavim.assertDoesntChange
import org.jetbrains.plugins.ideavim.rangeOf
import org.jetbrains.plugins.ideavim.waitAndAssert
import org.jetbrains.plugins.ideavim.waitAndAssertMode
import org.junit.jupiter.api.Test

/**
 * @author Alex Plate
 */
class NonVimVisualChangeTest : VimTestCase() {
  @TestWithoutNeovim(reason = SkipNeovimReason.NOT_VIM_TESTING)
  @Test
  fun `test save mode after removing text`() {
    // PyCharm uses BackspaceHandler.deleteToTargetPosition to remove indent
    // See https://github.com/JetBrains/ideavim/pull/186#issuecomment-486656093
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
    )
    VimListenerManager.EditorListeners.add(fixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(VimStateMachine.Mode.INSERT)
    ApplicationManager.getApplication().runWriteAction {
      CommandProcessor.getInstance().runUndoTransparentAction {
        BackspaceHandler.deleteToTargetPosition(fixture.editor, LogicalPosition(2, 0))
      }
    }
    assertState(
      """
            A Discovery

            found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
    )
    assertMode(VimStateMachine.Mode.INSERT)
  }

  @TestWithoutNeovim(reason = SkipNeovimReason.NOT_VIM_TESTING)
  @Test
  fun `test enable and disable selection`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
    )
    VimListenerManager.EditorListeners.add(fixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(VimStateMachine.Mode.INSERT)

    // Fast add and remove selection
    fixture.editor.selectionModel.setSelection(0, 10)
    fixture.editor.selectionModel.removeSelection()

    assertDoesntChange { fixture.editor.editorMode == VimStateMachine.Mode.INSERT }
  }

  @TestWithoutNeovim(reason = SkipNeovimReason.NOT_VIM_TESTING)
  @Test
  fun `test enable, disable, and enable selection again`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
    )
    VimListenerManager.EditorListeners.add(fixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(VimStateMachine.Mode.INSERT)

    // Fast add and remove selection
    fixture.editor.selectionModel.setSelection(0, 10)
    fixture.editor.selectionModel.removeSelection()
    fixture.editor.selectionModel.setSelection(0, 10)

    waitAndAssertMode(fixture, VimStateMachine.Mode.VISUAL)
  }

  @TestWithoutNeovim(reason = SkipNeovimReason.NOT_VIM_TESTING)
  @Test
  fun `test switch from char to line visual mode`() {
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)
    VimListenerManager.EditorListeners.add(fixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(VimStateMachine.Mode.INSERT)

    val range = text.rangeOf("Discovery")
    fixture.editor.selectionModel.setSelection(range.startOffset, range.endOffset)
    waitAndAssertMode(fixture, VimStateMachine.Mode.VISUAL)
    kotlin.test.assertEquals(VimStateMachine.SubMode.VISUAL_CHARACTER, fixture.editor.subMode)

    val rangeLine = text.rangeOf("A Discovery\n")
    fixture.editor.selectionModel.setSelection(rangeLine.startOffset, rangeLine.endOffset)
    waitAndAssert { fixture.editor.subMode == VimStateMachine.SubMode.VISUAL_LINE }
  }
}
