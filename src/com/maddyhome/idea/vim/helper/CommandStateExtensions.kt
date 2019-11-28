@file:JvmName("CommandStateHelper")

package com.maddyhome.idea.vim.helper

import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.command.CommandState

val CommandState.Mode.isEndAllowed
  get() = when (this) {
    CommandState.Mode.INSERT, CommandState.Mode.VISUAL, CommandState.Mode.SELECT -> true
    CommandState.Mode.COMMAND, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE -> false
  }

val CommandState.Mode.isBlockCaret
  get() = when (this) {
    CommandState.Mode.VISUAL, CommandState.Mode.COMMAND -> true
    CommandState.Mode.INSERT, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE, CommandState.Mode.SELECT -> false
  }

val CommandState.Mode.hasVisualSelection
  get() = when (this) {
    CommandState.Mode.VISUAL, CommandState.Mode.SELECT -> true
    CommandState.Mode.REPLACE, CommandState.Mode.CMD_LINE, CommandState.Mode.COMMAND, CommandState.Mode.INSERT -> false
  }

val Editor.mode
  get() = CommandState.getInstance(this).mode

var Editor.subMode
  get() = CommandState.getInstance(this).subMode
  set(value) {
    CommandState.getInstance(this).subMode = value
  }

@get:JvmName("inNormalMode")
val Editor.inNormalMode
  get() = this.mode == CommandState.Mode.COMMAND

@get:JvmName("inInsertMode")
val Editor.inInsertMode
  get() = this.mode == CommandState.Mode.INSERT || this.mode == CommandState.Mode.REPLACE

@get:JvmName("inRepeatMode")
val Editor.inRepeatMode
  get() = CommandState.getInstance(this).isDotRepeatInProgress

@get:JvmName("inVisualMode")
val Editor.inVisualMode
  get() = this.mode == CommandState.Mode.VISUAL

@get:JvmName("inSelectMode")
val Editor.inSelectMode
  get() = this.mode == CommandState.Mode.SELECT

@get:JvmName("inBlockSubMode")
val Editor.inBlockSubMode
  get() = this.subMode == CommandState.SubMode.VISUAL_BLOCK

@get:JvmName("inSingleCommandMode")
val Editor.inSingleCommandMode
  get() = this.subMode == CommandState.SubMode.SINGLE_COMMAND && this.inNormalMode
