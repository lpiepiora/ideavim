package com.maddyhome.idea.vim.troubleshooting

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.containers.MultiMap
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.key.MappingOwner
import com.maddyhome.idea.vim.key.ToKeysMappingInfo

// [WIP] https://youtrack.jetbrains.com/issue/VIM-2658/Troubleshooter
@Service
class Troubleshooter {
  private val problems: MultiMap<String, Problem> = MultiMap()

  fun removeByType(type: String) {
    problems.remove(type)
  }

  fun addProblem(type: String, problem: Problem) {
    problems.putValue(type, problem)
  }

  fun findIncorrectMappings(): List<Problem> {
    val problems = ArrayList<Problem>()
    MappingMode.values().forEach { mode ->
      injector.keyGroup.getKeyMapping(mode).getByOwner(MappingOwner.IdeaVim.InitScript).forEach { (_, to) ->
        if (to is ToKeysMappingInfo) {
          if (":action" in to.toKeys.joinToString { it.keyChar.toString() }) {
            problems += Problem("Mappings contain `:action` call")
          }
        }
      }
    }
    return problems
  }

  companion object {
    val instance = service<Troubleshooter>()
  }
}

class Problem(description: String)
