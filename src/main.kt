import com.epicbot.api.shared.GameType
import com.epicbot.api.shared.entity.NPC
import com.epicbot.api.shared.entity.details.Locatable
import com.epicbot.api.shared.script.LoopScript
import com.epicbot.api.shared.script.ScriptManifest
import com.epicbot.api.shared.util.time.Time
import combat.KillCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import utils.Consts
import utils.printMsg
import kotlin.coroutines.coroutineContext

@ScriptManifest(name = "KillCommand", gameType = GameType.OS)
class main : LoopScript() {

    private var killCommand: KillCommand = KillCommand()

    //region LoopScript

    override fun onStart(vararg p0: String?): Boolean {
        printMsg("onstart")
        killCommand.apply {
            initKillCommand(apiContext)
        }
        return false
    }

    override fun loop(): Int {
        printMsg("loop")
        return LOOP_COUNT
    }

    //endregion

    companion object {
        private const val LOOP_COUNT = 2
        private const val AREA_DIMENSIONS = 9 //squared
    }
}