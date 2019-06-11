/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2019 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.action.motion.gn

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.action.motion.TextObjectAction
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.handler.TextObjectActionHandler

/**
 * @author Alex Plate
 */

private object GnNextTextObjectHandler : TextObjectActionHandler() {
  override fun getRange(editor: Editor, caret: Caret, context: DataContext, count: Int, rawCount: Int, argument: Argument?): TextRange? {
    val range = VimPlugin.getSearch().getNextSearchRange(editor, count, true)
    val adj = VimPlugin.getVisualMotion().selectionAdj
    return range?.let { TextRange(it.startOffset, it.endOffset - adj) }
  }
}

class GnNextTextObject : TextObjectAction(GnNextTextObjectHandler)