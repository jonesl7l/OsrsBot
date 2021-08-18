import com.epicbot.api.shared.GameType
import com.epicbot.api.shared.script.LoopScript
import com.epicbot.api.shared.script.ScriptManifest
import combat.KillCommand
import utils.printMsg
import kotlin.random.Random

@ScriptManifest(name = "KillCommand", gameType = GameType.OS)
class main : LoopScript() {

    private var killCommand: KillCommand = KillCommand()

    //region LoopScript

    override fun onStart(vararg p0: String?): Boolean {
        printMsg("onstart")
        killCommand.apply {
            initKillCommand(apiContext)
        }
        return true
    }

    override fun loop(): Int {
        killCommand.pollKillCommand()
        return LOOP_TIMER
    }

    //endregion

    companion object {
        private val LOOP_TIMER by lazy { Random.nextInt(636, 1048) }
    }
}