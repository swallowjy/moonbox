package moonbox.hook

/**
  * Hook Context keeps all the necessary information for all the hooks.
  *
  * @param user
  * @param command
  * @param inputs input table set, table form like db.name
  * @param output output table form like db.name, when command isn't insert, the value is null
  */
class HookContext(user: String, command: String, inputs: Set[String], output: String) {

  object HookType extends Enumeration {
    type HookType = Value
    val PRE_EXEC_HOOK, POST_EXEC_HOOK, ON_FAILURE_HOOK = Value
  }

  private var hookType: HookType.HookType = _

  def getUser: String = this.user

  def getCommand: String = this.command

  def getInputs: Set[String] = this.inputs

  def getOutput: String = this.output

  def setHookType(hookType: HookType.HookType) = {
    this.hookType = hookType
  }

  def getHookType: HookType.HookType = this.hookType
}

