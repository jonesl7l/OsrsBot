package combat

import com.epicbot.api.shared.APIContext
import com.epicbot.api.shared.entity.GroundItem
import com.epicbot.api.shared.entity.NPC
import com.epicbot.api.shared.util.time.Time
import utils.Consts
import utils.printMsg

class KillCommand {

    private var currentHealthPercentage: Int = 0
    private var currentTarget: NPC? = null

    private lateinit var apiContext: APIContext

    //region Init

    fun initKillCommand(apiContext: APIContext) {
        this.apiContext = apiContext
        this.currentHealthPercentage = apiContext.localPlayer().healthPercent
        printMsg("initKillCommand")
        acquireTarget()
    }

    fun pollKillCommand() {
        when {
            isPlayerMoving() -> return
            !doesPlayerHaveLiveTarget() -> acquireTarget()
            doesPlayerHaveLiveTarget() && currentTarget.isFightingSomeoneElse() -> acquireTarget()
            doesPlayerHaveLiveTarget() && !currentTarget.isInCombat() -> attackTarget()
            doesPlayerHaveLowHealth() -> eatFood()
            doesPlayerHaveLiveTarget() && currentTarget.isInCombat() -> return
            !doesPlayerHaveLiveTarget() -> lootTarget()
        }
    }

    //endregion

    //region Kill State Actions

    private fun acquireTarget() {
        printMsg("acquireTarget")
        apiContext.npcs().getAll { it.name == Consts.NPC_COW && it.canReachTarget() }?.find { !it.isInCombat }?.let {
            printMsg("$targetName found...\nAttacking $targetName")
            this.currentTarget = it
            it.interact(Consts.INTERACTION_ATTACK)
        }
    }

    private fun attackTarget() {
        printMsg("attackTarget")
        this.currentTarget?.interact(Consts.INTERACTION_ATTACK)
    }

    private fun eatFood() {
        printMsg("eatFood")
        apiContext.inventory().items.find { it.name == foodName }?.let {
            printMsg("Eating ${it.name}...")
            it.click()
        }
    }

    private fun lootTarget() {
        printMsg("lootTarget")
        apiContext.groundItems().query().asList().filter { itemsToLoot.contains(it.name) && it.canReachTarget() }.forEach {
            printMsg("Picking up $it")
            it.interact(Consts.INTERACTION_TAKE)
        }
        this.currentTarget = null
    }

    //endregion

    //region Player

    private fun isPlayerMoving(): Boolean = apiContext.localPlayer().isMoving

    private fun isPlayerFighting(): Boolean = apiContext.localPlayer().isInCombat || apiContext.localPlayer().isAttacking

    private fun doesPlayerHaveLiveTarget(): Boolean = currentTarget != null && !(currentTarget?.isDead ?: false)

    private fun doesPlayerHaveLowHealth(): Boolean = currentHealthPercentage <= 20

    //endregion

    //region Target

    private fun NPC.canReachTarget(): Boolean = this.canReach(apiContext, targetDistance)

    private fun NPC?.isFightingSomeoneElse(): Boolean = (this?.isInCombat ?: false) && !isPlayerFighting()

    private fun NPC?.isInCombat(): Boolean = this?.isInCombat ?: false

    //endregion

    //region Item

    private fun GroundItem.canReachTarget(): Boolean = this.canReach(apiContext, targetDistance)

    //endregion

    companion object {

        const val targetName: String = Consts.NPC_HILL_GIANTS //Change me to your target npc
        const val targetDistance: Int = 9 //Change me to your target distance

        const val foodName: String = Consts.ITEM_SALMON //Change me to your food

        private var itemsToLoot: List<String> = listOf(
            Consts.ITEM_COINS, Consts.ITEM_BIG_BONES,
            Consts.ITEM_DEATH_RUNE, Consts.ITEM_COSMIC_RUNE, Consts.ITEM_DEATH_RUNE, Consts.ITEM_LAW_RUNE,
            Consts.ITEM_UNCUT_SAPPHIRE, Consts.ITEM_UNCUT_EMERALD, Consts.ITEM_UNCUT_RUBY, Consts.ITEM_UNCUT_DIAMOND
        )
    }
}