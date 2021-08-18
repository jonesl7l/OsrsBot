package combat

import com.epicbot.api.shared.APIContext
import com.epicbot.api.shared.entity.GroundItem
import com.epicbot.api.shared.entity.NPC
import com.epicbot.api.shared.util.time.Time
import utils.Consts
import utils.printMsg
import kotlin.random.Random

class KillCommand {

    private var currentHealthPercentage: Int = 0
    private var currentTarget: NPC? = null

    private lateinit var apiContext: APIContext

    //region Init

    fun initKillCommand(apiContext: APIContext) {
        printMsg("initKillCommand")
        this.apiContext = apiContext
        this.currentHealthPercentage = apiContext.localPlayer().healthPercent
    }

    fun pollKillCommand() {
        when {
            doesPlayerHaveLowHealth() -> eatFood()
            isPlayerMoving() -> return
            isPlayerFighting() -> return
            !doesPlayerHaveLiveTarget() && isLootNearby() -> lootTarget()
            doesPlayerHaveLiveTarget() && !currentTarget.isInCombat() -> attackTarget()
            !doesPlayerHaveLiveTarget() -> acquireTarget()
            doesPlayerHaveLiveTarget() && currentTarget.isFightingSomeoneElse() -> acquireTarget()
        }
    }

    //endregion

    //region Kill Actions

    private fun acquireTarget() {
        printMsg("acquireTarget")
        currentTarget = null
        apiContext.npcs().query().nameMatches(targetName)
            .notInCombat()
            .animation(-1)
            .reachable()
            .results()
            .nearest()?.let {
                printMsg("$targetName found...\nAttacking $targetName")
                this.currentTarget = it
            }
    }

    private fun attackTarget() {
        printMsg("attackTarget")
        this.currentTarget?.let {
            printMsg("Attacking ${it.name}")
            Time.sleep(Random.nextInt(645, 1123))
            it.click()
            apiContext.mouse().moveOffScreen()
            if (isPlayerInteracting()) { // if our player is interacting
                Time.sleep(Random.nextInt(4895, 6233)) { this.currentTarget?.isDead == true } // sleep until cow npc is dead
            }
        }
        Random.nextInt(1243, 2967)
    }

    private fun eatFood() {
        printMsg("eatFood")
        apiContext.inventory().items.find { it.name == foodName }?.let {
            printMsg("Eating ${it.name}...")
            it.click()
            Time.sleep(Random.nextInt(955, 2786))
        }
    }

    private fun lootTarget() {
        printMsg("lootTarget")
        apiContext.groundItems().query().named(*itemsToLoot.toTypedArray()).reachable().results().forEach {
            if (isInventoryFull()) {
                printMsg("Inventory full; go to bank")
                return@forEach
            }
            printMsg("Picking up $it")
            it.interact(Consts.INTERACTION_TAKE)
            Time.sleep(Random.nextInt(1234, 3248)) { !isPlayerMoving() }
            buryBones(it)
        }
        this.currentTarget = null
    }

    private fun buryBones(item: GroundItem) {
        printMsg("buryBones")
        if (item.name == targetBones) {
            printMsg("Burying ${item.name}")
            apiContext.inventory().getItem(item.name).click()
            Time.sleep(Random.nextInt(2434, 3594))
        }
    }

    //endregion

    //region Player

    private fun isPlayerMoving(): Boolean = apiContext.localPlayer().isMoving

    private fun isPlayerInteracting(): Boolean = apiContext.localPlayer().interacting != null

    private fun isPlayerFighting(): Boolean = apiContext.localPlayer().isInCombat || apiContext.localPlayer().isAttacking

    private fun doesPlayerHaveLiveTarget(): Boolean = currentTarget != null && !(currentTarget?.isDead ?: false)

    private fun doesPlayerHaveLowHealth(): Boolean = currentHealthPercentage <= 50

    private fun isInventoryFull(): Boolean = apiContext.inventory().isFull

    //endregion

    //region Target

    private fun NPC?.isFightingSomeoneElse(): Boolean = (this?.isInCombat ?: false) && !isPlayerFighting()

    private fun NPC?.isInCombat(): Boolean = this?.isInCombat ?: false

    //endregion

    //region Item

    private fun isLootNearby(): Boolean = apiContext.groundItems().query().named(*itemsToLoot.toTypedArray()).reachable().results().isNotEmpty()

    private fun GroundItem.canReachTarget(): Boolean = this.canReach(apiContext, targetDistance)

    //endregion

    companion object {

        const val targetName: String = Consts.NPC_HILL_GIANTS //Change me to your target npc
        const val targetBones: String = Consts.ITEM_BIG_BONES //Change me to your target npc
        const val targetDistance: Int = 5 //Change me to your target distance

        const val foodName: String = Consts.ITEM_SALMON //Change me to your food

        private var itemsToLoot: List<String> = listOf(
            Consts.ITEM_COINS, Consts.ITEM_BIG_BONES,
            Consts.ITEM_DEATH_RUNE, Consts.ITEM_COSMIC_RUNE, Consts.ITEM_DEATH_RUNE, Consts.ITEM_LAW_RUNE,
            Consts.ITEM_UNCUT_SAPPHIRE, Consts.ITEM_UNCUT_EMERALD, Consts.ITEM_UNCUT_RUBY, Consts.ITEM_UNCUT_DIAMOND
        )
    }
}