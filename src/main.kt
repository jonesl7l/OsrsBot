import com.epicbot.api.shared.APIContext
import com.epicbot.api.shared.GameType
import com.epicbot.api.shared.script.LoopScript
import com.epicbot.api.shared.script.ScriptManifest
import com.epicbot.api.shared.util.paint.frame.PaintFrame
import com.epicbot.api.shared.util.time.Time
import combat.KillCommand
import java.awt.Color
import java.awt.Graphics2D
import kotlin.random.Random


@ScriptManifest(name = "KillCommand", gameType = GameType.OS)
class main : LoopScript() {

    private var killCommand: KillCommand = KillCommand()
    private var startTime: Long = 0

    //region LoopScript

    override fun onStart(vararg p0: String?): Boolean {
        startTime = System.currentTimeMillis()
        killCommand.initKillCommand(apiContext)
        return apiContext.client().isLoggedIn
    }

    override fun loop(): Int {
        killCommand.pollKillCommand()
        return LOOP_TIMER
    }

    // paint shit.
    override fun onPaint(g: Graphics2D, ctx: APIContext?) {
        if (!apiContext.client().isLoggedIn) {
            Time.sleep(15000) { apiContext.client().isLoggedIn }
        }
        if (apiContext.client().isLoggedIn) {
            val frame = PaintFrame()
            frame.setTitle("Kill Command")
            frame.addLine("Runtime: ", Time.getFormattedRuntime(startTime)) // we use startTime here from the very beginning
//            frame.addLine("State: ", playerState) //we get whatever the player's state is equal to and print it onto the paint.
            frame.addLine("", "")
            frame.addLine("Current Strength level: ", apiContext.skills().strength().currentLevel)
            frame.addLine("% to next level", apiContext.skills().strength().percentToNextLevel)
            frame.draw(g, 0.0, 90.0, ctx) //drawing the actual frame.
            g.color = Color(208, 189, 155, 255)
            g.fillRect(11, 468, 120, 15) //name covering stuff, honestly might remove it cuz kinda pointless? Dunno
        }
    }

    //endregion

    companion object {
        private val LOOP_TIMER by lazy { Random.nextInt(636, 1048) }
    }
}