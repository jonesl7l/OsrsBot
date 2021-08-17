package combat

import KillState
import com.epicbot.api.shared.APIContext
import com.epicbot.api.shared.entity.NPC
import com.epicbot.api.shared.util.time.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import utils.Consts
import utils.printMsg

class KillCommand {

    private var killstate: MutableStateFlow<KillState> = MutableStateFlow(KillState.NO_TARGET)

    private var currentHealthPercentage: Int = 0
    private var currentTarget: NPC? = null

    private var itemsToLoot: List<String> = listOf(Consts.ITEM_BONES)

    //region Init

    fun initKillCommand(apiContext: APIContext) {
        currentHealthPercentage = apiContext.localPlayer().healthPercent
        printMsg("initKillCommand")
        acquireTarget(apiContext)
    }

    suspend fun observeKillStateUpdates(apiContext: APIContext) {
        killstate.collect {
            when (it) {
                KillState.NO_TARGET,
                KillState.NPC_DEAD_LOOTED -> acquireTarget(apiContext)
                KillState.PLAYER_FIGHTING_NPC -> finishKillingCurrentTarget(apiContext)
                KillState.NPC_DEAD_UNLOOTED -> lootTarget(apiContext)
                else -> {}
            }
        }
    }

    //endregion

    //region Kill State Actions

    private fun acquireTarget(apiContext: APIContext) {
        printMsg("acquireTarget")
        apiContext.npcs().getAll { it.name == Consts.NPC_COW && it.canReach(apiContext, targetDistance) }.find { !it.isInCombat }?.let {
            printMsg("$targetName found...\nAttacking $targetName")
            killstate.value = KillState.NPC_TARGETED
            currentTarget = it
            it.interact(Consts.INTERACTION_ATTACK)
            Time.sleep(1000, 2000)
            finishKillingCurrentTarget(apiContext)
        }
    }

    private fun finishKillingCurrentTarget(apiContext: APIContext) {
        if (currentTarget?.isInCombat == true) {
            killstate.value = KillState.PLAYER_FIGHTING_NPC
        }
        if (currentTarget != null && currentTarget?.isDead == true) {
            Time.sleep(300, 1200)
            lootTarget(apiContext)
            return
        }
        Time.sleep(500, 1000)
        finishKillingCurrentTarget(apiContext)
    }

    private fun lootTarget(apiContext: APIContext) {
        killstate.value = KillState.NPC_DEAD_UNLOOTED
        apiContext.groundItems().query().asList().filter { itemsToLoot.contains(it.name) }.forEach {
            Time.sleep(300, 600)
            printMsg("Picking up $it")
            it.interact(Consts.INTERACTION_TAKE)
        }
        currentTarget = null
        killstate.value = KillState.NPC_DEAD_LOOTED
    }

    //endregion

    companion object {

        const val targetName: String = "Cow" //Change me to your target npc
        const val targetDistance: Int = 9 //Change me to your target distance
    }
}