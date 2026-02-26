package core.game.system.command.sets

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import core.ServerConstants
import core.cache.Cache
import core.cache.def.impl.*
import core.game.system.command.Privilege
import core.game.world.map.RegionManager
import core.game.world.map.RegionPlane
import core.plugin.Initializable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Initializable
class CacheCommandSet : CommandSet(Privilege.ADMIN) {

    override fun defineCommands() {

        /*
         * Command for finding all object with given name.
         */

        define(
            name = "findobjectname",
            privilege = Privilege.ADMIN,
            usage = "::findobjectname <name>",
            description = "Scan regions for all instances of objects matching the given name."
        ) { player, args ->

            val nameQuery = args
                ?.drop(1)
                ?.joinToString(" ")
                ?.takeIf { it.isNotBlank() }
                ?.lowercase()
                ?: run {
                    reject(player, "Usage: ::findobjectname <name>")
                    return@define
                }

            val exportDir = File("dumps")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM_HH-mm"))

            val dump = File(exportDir, "objects_name=${nameQuery}_$timestamp.txt")

            GlobalScope.launch {
                var found = 0

                dump.bufferedWriter().use { writer ->
                    for (region in RegionManager.regionCache.values) {
                        for (z in 0..3) {
                            val plane = region.planes[z] ?: continue
                            val grid = plane.objects ?: continue

                            for (x in 0 until RegionPlane.REGION_SIZE) {
                                for (y in 0 until RegionPlane.REGION_SIZE) {

                                    val scenery = grid[x][y] ?: continue
                                    val def = SceneryDefinition.forId(scenery.id) ?: continue
                                    val defName = def.name?.lowercase() ?: continue

                                    if (!defName.contains(nameQuery)) continue

                                    val loc = scenery.location

                                    writer.write(
                                        "[${scenery.id}] - (${loc.x}, ${loc.y}, ${loc.z}) " +
                                                "[region=${region.id}, name=${def.name}]\n"
                                    )

                                    found++
                                }
                            }
                        }
                    }
                }

                player.debug("Saved $found objects matching '$nameQuery' -> ${dump.path}")
            }
        }

        /*
         * Command for finding all object with given id.
         */

        define(
            name = "findobjectid",
            privilege = Privilege.ADMIN,
            usage = "::findobjectid <id>",
            description = "Scan regions for all instances of the given object ID."
        ) { player, args ->

            val id = args?.getOrNull(1)?.toIntOrNull() ?: run {
                reject(player, "Usage: ::findobjectid id")
                return@define
            }

            val exportDir = File("dumps")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM_HH-mm"))
            val dump = File(exportDir, "objects_id=${id}_$timestamp.txt")

            GlobalScope.launch {
                var found = 0
                dump.bufferedWriter().use { writer ->
                    for (region in RegionManager.regionCache.values) {
                        for (z in 0..3) {
                            val plane = region.planes[z] ?: continue
                            val grid = plane.objects ?: continue
                            for (x in 0 until RegionPlane.REGION_SIZE) {
                                for (y in 0 until RegionPlane.REGION_SIZE) {
                                    val scenery = grid[x][y] ?: continue
                                    if (scenery.id != id) continue

                                    val def = SceneryDefinition.forId(scenery.id)
                                    val loc = scenery.location
                                    writer.write(
                                        "[${scenery.id}] - (${loc.x}, ${loc.y}, ${loc.z}) " +
                                                "[region=${region.id}, name=${def?.name ?: "Unknown"}]\n"
                                    )
                                    found++
                                }
                            }
                        }
                    }
                }
                player.debug("Saved $found instances of object ID $id -> ${dump.path}")
            }
        }

        /*
         * Command for finding pickpocket-able npcs.
         */

        define(
            name = "thievingnpc",
            privilege = Privilege.ADMIN,
            usage = "::findpickpocketnpcs",
            description = "Scan all NPCs with the pickpocket option and dump their names and IDs."
        ) { player, _ ->

            val exportDir = File("dumps")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM_HH-mm"))
            val dump = File(exportDir, "pickpocket_npcs_$timestamp.txt")

            GlobalScope.launch {
                val results = mutableListOf<Pair<String, Int>>()

                for (npcDef in NPCDefinition.getDefinitions().values) {
                    val options = npcDef.options ?: continue
                    if (options.any { it.equals("pick-pocket", ignoreCase = true) || it.equals("pickpocket", ignoreCase = true) }) {
                        val name = npcDef.name ?: "Unknown"
                        results.add(name to npcDef.id)
                    }
                }

                results.sortWith(compareBy({ it.first.lowercase() }, { it.second }))

                dump.bufferedWriter().use { writer ->
                    for ((name, id) in results) {
                        writer.write("${name.uppercase()}_$id\n")
                    }
                }

                player.debug("Saved ${results.size} NPCs to ${dump.path}")
            }
        }

        define(
            name = "ifaceitem",
            privilege = Privilege.ADMIN,
            usage = "::ifaceitem <sceneryId>",
            description = "Find item definitions that use any model used by a scenery object."
        ) { player, args ->

            val sceneryId = args?.getOrNull(1)?.toIntOrNull()

            if (sceneryId == null) {
                reject(player, "Usage: ::ifaceitem <sceneryId>")
                return@define
            }

            val sceneryDef = SceneryDefinition.forId(sceneryId)
            if (sceneryDef == null) {
                reject(player, "Scenery definition $sceneryId not found.")
                return@define
            }

            val sceneryModels: List<Int> = when {
                sceneryDef.modelIds != null -> sceneryDef.modelIds!!.toList()
                else -> emptyList()
            }

            if (sceneryModels.isEmpty()) {
                reject(player, "Scenery $sceneryId has no model IDs.")
                return@define
            }
            player.debug("[loc_$sceneryId] name=${sceneryDef.name}")

            GlobalScope.launch {

                val results = mutableMapOf<Int, MutableList<String>>()

                for (modelId in sceneryModels) {
                    results[modelId] = mutableListOf()
                }

                for (itemId in 0 until Cache.getItemDefinitionsSize()) {
                    val def = ItemDefinition.forId(itemId) ?: continue

                    val itemModels: List<Int> = buildList {
                        def.interfaceModelId?.let { add(it) }
                    }

                    for (modelId in sceneryModels) {
                        if (modelId in itemModels) {
                            results[modelId]!!.add("[obj_${def.id}] name=${def.name}")
                        }
                    }
                }

                var foundAny = false

                for ((modelId, items) in results) {
                    if (items.isNotEmpty()) {
                        foundAny = true
                        player.debug("model=model_$modelId")
                        items.forEach { player.debug("--->$it") }
                    }
                }
                if (!foundAny) {
                    player.debug("No items found using any model from scenery $sceneryId.")
                }
            }
        }

        define(
            name = "finditembymodel",
            privilege = Privilege.ADMIN,
            usage = "::finditembymodel <modelId>",
            description = "Find item definitions that use the given model ID."
        ) { player, args ->

            val modelId = args?.getOrNull(1)?.toIntOrNull()

            if (modelId == null) {
                reject(player, "Usage: ::finditembymodel <modelId>")
                return@define
            }

            player.debug("Searching for items using model_$modelId...")

            GlobalScope.launch {

                val matches = mutableListOf<String>()

                val total = Cache.getItemDefinitionsSize()
                for (itemId in 0 until total) {
                    val def = ItemDefinition.forId(itemId) ?: continue

                    val itemModel = def.interfaceModelId ?: continue
                    if (itemModel == modelId) {
                        matches.add("[obj_${def.id}] name=${def.name}")
                    }
                }

                if (matches.isEmpty()) {
                    player.debug("No items found using model_$modelId.")
                    return@launch
                }

                player.debug("Items using model_$modelId:")
                matches.forEach { player.debug("---> $it") }
            }
        }

        define(
            name = "dumpquests",
            privilege = Privilege.ADMIN,
            usage = "::dumpquests",
            description = "Dump all registered quests to a text file."
        ) { player, _ ->

            player.debug("Dumping all quests to file...")

            GlobalScope.launch {
                try {
                    val quests = core.game.node.entity.player.link.quest.QuestRepository
                        .getQuests()
                        .values

                    val exportDir = File("dumps")
                    if (!exportDir.exists()) exportDir.mkdirs()

                    val file = File(exportDir, "quests_dump.txt")

                    file.bufferedWriter().use { writer ->
                        writer.write("=== QUEST DUMP ===\n")
                        writer.write("Total quests: ${quests.size}\n\n")

                        for (quest in quests) {
                            writer.write("Name: ${quest.name}\n")
                            writer.write("Index: ${quest.index}\n")
                            writer.write("ButtonId: ${quest.buttonId}\n")
                            writer.write("QuestPoints: ${quest.questPoints}\n")
                            writer.write("Configs: ${quest.configs.joinToString(", ")}\n")
                            writer.write("----------------------------------------\n")
                        }
                    }

                    player.debug("Dumped ${quests.size} quests -> ${file.path}")

                } catch (e: Exception) {
                    player.debug("Error while dumping quests: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        /*
         * Command for listing all object options in-game.
         */

        define(
            name = "findobjects_all",
            privilege = Privilege.ADMIN,
            usage = "::findobjects_all",
            description = ""
        ) { player, _ ->

            val objectOptions = arrayOf("activate", "add", "adjust-temperature", "admire", "ascend", "attack", "bait", "bang", "bank", "block", "board", "board ( pay 10 )", "break", "build", "buy", "call", "capture", "cast", "cast-alchemy", "cast-teleport", "change", "check", "choose", "chop", "chop down", "chop-down", "churn", "climb", "climb down", "climb up", "climb-across", "climb-down", "climb-into", "climb-off", "climb-on", "climb-out", "climb-over", "climb-through", "climb-under", "climb-up", "close", "collapse", "collect", "collect-from", "construct", "continue-trek", "control", "convert", "count", "count-plunder", "crack", "craft", "craft-rune", "crawl", "crawl-down", "crawl-through", "crawl-under", "cross", "cross-bridge", "cut", "cut down", "cut-branch", "cut-down", "cut-wood", "deposit", "deposit-all", "descend", "destroy", "dig", "dig-below", "dig-up", "dip-egg", "dive", "dive in", "drink from", "drink-from", "dunk", "empty", "enter", "enter-cloud", "escape", "escape-event", "evade-event", "examine", "exchange", "exit", "exit-cave", "exit-room", "exit-through", "fill", "fill-bucket", "fill-buckets", "fire", "fire-at", "fish-in", "fix", "flag", "flush", "fly", "follow", "forfeit", "free", "get in", "get-spikes", "go-down", "go-through", "go-through-wall", "go-up", "grab", "grapple", "guide", "hack", "hidden", "hide-behind", "hide-in", "history", "hit", "infuse-pouch", "inspect", "investigate", "j-mod options", "jump", "jump off", "jump-across", "jump-down", "jump-from", "jump-on", "jump-onto", "jump-over", "jump-through", "jump-to", "jump-up", "knock", "knock-at", "lean against", "leave", "leave through", "leave tomb", "leave-warren", "levitate", "light", "listen-at", "listen-to", "load", "look", "look at", "look in", "look-at", "look-in", "look-inside", "look-into", "look-over", "look-under", "look-up", "loot", "lure", "make-canoe", "make-wish", "milk", "mine", "mine-through", "move", "move-creature", "net", "observe", "open", "operate", "paint-bauble", "pan", "pass", "pass-through", "pay-fare", "pay-toll(10gp)", "pay-toll(2-ecto)", "pedal", "peek-in", "pick", "pick-from", "pick-fruit", "pick-leaf", "pick-lock", "pick-up", "play", "plunder", "pray", "pray at", "pray-at", "press", "process", "prod", "prospect", "pull", "pull up", "pull-down", "pump", "push", "push through", "push-through", "push-up", "put-inside", "put-ore-on", "quick-leave", "quick-pass", "quick-pay(100)", "raise", "reach", "read", "read-book", "read-plaque on", "refuel", "remove-coal", "renew-points", "repair", "return", "return-to-burgh", "revert-crystal", "ride", "ring", "ring-bell", "rotate", "rummage", "say-name", "search", "search for traps", "set-trap", "sets", "shake", "shoot", "shoot-at", "shout-in", "shout-through", "shut", "sing-glass", "slash", "slide", "slide-down", "smash", "smash-to-bits", "smelt", "smelt-ore", "spectate", "spin", "squeeze-past", "squeeze-through", "squeeze-under", "status", "steal", "steal from", "steal-cowbell", "steal-from", "step", "step-into", "step-off", "step-on", "step-over", "stock-up", "store", "store-plunder", "study", "swim", "swim to", "swing", "swing across", "swing-across", "swing-on", "tag", "take", "take from", "take treasure", "take-armour", "take-arrows", "take-axe", "take-barrier", "take-beam", "take-beer", "take-bow", "take-bowl", "take-bucket", "take-clay", "take-coffin", "take-crackers", "take-decoration", "take-egg", "take-food", "take-from", "take-honey", "take-log", "take-meat", "take-potion", "take-pouch", "take-powder", "take-pulley-beam", "take-rock", "take-rope", "take-runes", "take-seed", "take-sigil", "take-stone", "take-tofu", "take-tool", "take-vial", "take-weapon", "take-worms", "talk to", "talk-into", "talk-through", "talk-to", "taunt-through", "teeth-grip", "teleport", "throw", "touch", "travel", "tread-softly", "turn", "turn-left", "turn-right", "unlock", "untie", "use", "use-quickly", "vent", "view", "view-game", "walk through", "walk-across", "walk-down", "walk-here", "walk-on", "walk-through", "walk-up", "watch", "weave", "wind", "worship")

            val exportDir = File("dumps")
            if (!exportDir.exists()) exportDir.mkdirs()

            GlobalScope.launch {
                val optionMap = objectOptions.associateWith { mutableListOf<String>() }.toMutableMap()

                for (region in RegionManager.regionCache.values) {
                    for (z in 0..3) {
                        val plane = region.planes[z] ?: continue
                        val grid = plane.objects ?: continue
                        for (x in 0 until RegionPlane.REGION_SIZE) {
                            for (y in 0 until RegionPlane.REGION_SIZE) {
                                val scenery = grid[x][y] ?: continue
                                val def = SceneryDefinition.forId(scenery.id) ?: continue
                                def.options?.filterNotNull()?.forEach { option ->
                                    val lowerOption = option.lowercase()
                                    if (optionMap.containsKey(lowerOption)) {
                                        val loc = scenery.location
                                        val entry = "[${scenery.id}] - (${loc.x}, ${loc.y}, ${loc.z}) [region=${region.id}, name=${def.name}]"
                                        optionMap[lowerOption]?.add(entry)
                                    }
                                }
                            }
                        }
                    }
                }

                optionMap.forEach { (option, entries) ->
                    if (entries.isNotEmpty()) {
                        // val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM_HH-mm"))
                        val dump = File(exportDir, "objects_opt=${option}_[2009scape].txt")
                        dump.bufferedWriter().use { writer ->
                            entries.forEach { writer.write(it + "\n") }
                        }
                        // player.debug("Saved ${entries.size} objects with option '$option' -> ${dump.path}")
                    }
                }

                player.debug("Finished scanning all options.")
            }
        }

        /*
         * Command for listing all object options in-game.
         */

        define(
            name = "listobjectoptions",
            privilege = Privilege.ADMIN,
            usage = "::listobjectoptions",
            description = ""
        ) { player, _ ->

            val exportDir = File("dumps")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM_HH-mm"))
            val dumpCsv = File(exportDir, "object_options_$timestamp.csv")

            GlobalScope.launch {
                val optionsSet = mutableSetOf<String>()

                for (region in RegionManager.regionCache.values) {
                    for (z in 0..3) {
                        val plane = region.planes[z] ?: continue
                        val grid = plane.objects ?: continue
                        for (x in 0 until RegionPlane.REGION_SIZE) {
                            for (y in 0 until RegionPlane.REGION_SIZE) {
                                val scenery = grid[x][y] ?: continue
                                val def = SceneryDefinition.forId(scenery.id) ?: continue
                                def.options?.filterNotNull()?.forEach { optionsSet.add(it.lowercase()) }
                            }
                        }
                    }
                }

                dumpCsv.bufferedWriter().use { writer ->
                    writer.write("Options:\n")
                    optionsSet.sorted().forEach { writer.write("\"$it\"\n") }
                }

                player.debug("Saved ${optionsSet.size} unique object options -> ${dumpCsv.path}")
            }
        }

        /*
         * Command for finding objects with given option.
         */

        define(
            name = "findobjects",
            privilege = Privilege.ADMIN,
            usage = "::findobjects <option>",
            description = "Scan regions for all objects with the given option."
        ) { player, args ->

            val option = args?.getOrNull(1)?.lowercase() ?: run {
                reject(player, "Usage: ::findobjects option")
                return@define
            }

            val exportDir = File("dumps")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM_HH-mm"))
            val dump = File(exportDir, "objects_list_opt=${option}_$timestamp.txt")

            GlobalScope.launch {
                var found = 0
                dump.bufferedWriter().use { writer ->
                    for (region in RegionManager.regionCache.values) {
                        for (z in 0..3) {
                            val plane = region.planes[z] ?: continue
                            val grid = plane.objects ?: continue
                            for (x in 0 until RegionPlane.REGION_SIZE) {
                                for (y in 0 until RegionPlane.REGION_SIZE) {
                                    val scenery = grid[x][y] ?: continue
                                    val def = SceneryDefinition.forId(scenery.id) ?: continue

                                    val matchedOption = def.options?.firstOrNull { it.equals(option, ignoreCase = true) }
                                    if (matchedOption != null) {
                                        found++
                                        val loc = scenery.location
                                        writer.write(
                                            "[${scenery.id}] - (${loc.x}, ${loc.y}, ${loc.z}) [region=${region.id}, name=${def.name}, option=$matchedOption]\n"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                player.debug("Saved $found objects with option '$option' -> ${dump.path}")
            }
        }

        /*
         * Command for finding anim for items.
         */

        define(
            name = "dumprenderanims",
            privilege = Privilege.ADMIN,
            usage = "::dumprenderanims",
            description = "Dumps render anims to a JSON file."
        ) { player, _ ->

            val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
            val exportDir = File("dumps")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM_HH-mm"))
            val dumpFile = File(exportDir, "render_animation_definitions_$timestamp.json")

            GlobalScope.launch {
                val allDefs = mutableListOf<Map<String, Any?>>()
                for (id in 0 until Cache.getRenderAnimationDefinitionsSize()) {
                    val def = RenderAnimationDefinition.forId(id) ?: continue
                    val usedByItemIds = (0 until Cache.getItemDefinitionsSize()).filter { itemId ->
                        val itemDef = ItemDefinition.forId(itemId) ?: return@filter false
                        itemDef.renderAnimationId == id
                    }

                    if (usedByItemIds.isEmpty()) continue

                    val defMap = mutableMapOf<String, Any?>(
                        "usedByItems" to usedByItemIds
                    )

                    for (prop in def::class.memberProperties) {
                        prop.isAccessible = true
                        try {
                            val value = prop.getter.call(def)
                            defMap[prop.name] = when (value) {
                                is Array<*> -> value.map { it?.toString() ?: "null" }
                                is IntArray -> value.toList()
                                is ShortArray -> value.toList().map { it.toInt() }
                                is ByteArray -> value.toList()
                                is BooleanArray -> value.toList()
                                else -> value
                            }
                        } catch (e: Exception) {
                            defMap[prop.name] = "Error: ${e.message}"
                        }
                    }

                    allDefs.add(defMap)
                }

                try {
                    dumpFile.writeText(gson.toJson(allDefs))
                    player.debug("Render anims dumped to ${dumpFile.path}")
                } catch (e: Exception) {
                    player.debug("Failed to dump render anims: ${e.message}")
                }
            }
        }

        define(
            name = "droptabledesc",
            privilege = Privilege.ADMIN,
            usage = "::droptabledesc",
            description = "Fill descriptions for drop tables."
        ) { player, _ ->

            val gson = GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()

            val sourceFile = File(ServerConstants.CONFIG_PATH + "drop_tables.json")
            val outputFile = File(ServerConstants.CONFIG_PATH + "drop_table_descriptions.json")

            if (!sourceFile.exists()) {
                player.debug("drop_tables.json not found.")
                return@define
            }

            val configs = gson.fromJson(FileReader(sourceFile), JsonArray::class.java)
            var filledCount = 0

            for (element in configs) {
                val e = element.asJsonObject
                val currentDescription = e.get("description")?.asString ?: ""

                if (currentDescription.isEmpty()) {
                    val ids = e.get("ids")?.asString
                        ?.split(",")
                        ?.mapNotNull { it.trim().toIntOrNull() }
                        ?: continue

                    val firstNpcDescription = ids.mapNotNull { npcId ->
                        NPCDefinition.forId(npcId)
                    }.firstOrNull()?.let { "${it.name} (level: ${it.combatLevel})" }

                    if (firstNpcDescription != null) {
                        e.addProperty("description", firstNpcDescription)
                        filledCount++
                    }
                }
            }

            FileWriter(outputFile).use { writer ->
                gson.toJson(configs, writer)
            }

            player.debug("Filled empty descriptions for $filledCount drop table entries. Output: ${outputFile.name}")
        }

        /*
         * Print xteas to txt.
         */

        define(
            name = "dumpxteas",
            privilege = Privilege.ADMIN,
            usage = "::dumpxteas",
            description = "Dumps XTEA keys from xteas.json into individual txt files."
        ) { player, _ ->

            val gson = GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()

            val source = File(ServerConstants.CONFIG_PATH + "xteas.json")
            val output = File(ServerConstants.CONFIG_PATH + "xteas_txt")

            if (!output.exists())
                output.mkdirs()

            val data = gson.fromJson(FileReader(source), JsonObject::class.java)
            val xteas = data.getAsJsonArray("xteas")

            for (entry in xteas) {
                val obj = entry.asJsonObject
                val regionId = obj.get("regionId").asInt
                val keysString = obj.get("keys").asString
                val keys = keysString.split(",").map { it.trim() }

                val filePath = File(output, "$regionId.txt")

                filePath.printWriter().use { writer ->
                    keys.forEach { key ->
                        writer.println(key)
                    }
                }
                player.debug("Dumped XTEA keys to txt=${filePath.name}")
            }
        }

        /*
         * Modify object_configs.json.
         */

        define(
            name = "dumpobjects",
            privilege = Privilege.ADMIN,
            usage = "::dumpobjects",
            description = "A dump that allows you to add data to objects_configs."
        ) { player, _ ->

            val gson = GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()

            val sourceFile = File(ServerConstants.CONFIG_PATH + "object_configs.json")
            val outputFile = File(ServerConstants.CONFIG_PATH + "object_configs_dump.json")

            if (!sourceFile.exists()) {
                player.debug("object_configs.json not found.")
                return@define
            }

            val configs = gson.fromJson(FileReader(sourceFile), JsonArray::class.java)

            val dataMap = linkedMapOf<Pair<String, String>, MutableSet<Int>>()

            for (element in configs) {
                val e = element.asJsonObject
                val ids = e.get("ids")?.asString
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?: continue

                val firstId = ids.firstOrNull() ?: continue
                val objDef = SceneryDefinition.forId(firstId)

                val name = objDef?.name ?: "null"
                val examine = objDef?.examine ?: e.get("examine")?.asString ?: "null"

                dataMap.computeIfAbsent(examine to name) { linkedSetOf() }.addAll(ids)
            }

            val outputArray = JsonArray()
            for ((key, ids) in dataMap) {
                val (examine, name) = key
                val ordered = JsonObject().apply {
                    addProperty("name", name)
                    addProperty("ids", ids.joinToString(","))
                    addProperty("examine", examine)
                }
                outputArray.add(ordered)
            }

            FileWriter(outputFile).use { writer ->
                gson.toJson(outputArray, writer)
            }

            player.debug("Dumped ${outputArray.size()} grouped object configs to ${outputFile.name}.")
        }

        /*
         * Split npc spawns.
         */

        define(
            name = "splitspawns",
            privilege = Privilege.ADMIN,
            usage = "::splitspawns",
            description = "Splits npc_spawns.json into region-based json (merged loc_data)."
        ) { player, _ ->

            val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
            val sourceFile = File(ServerConstants.CONFIG_PATH + "npc_spawns.json")
            val outputDir = File(ServerConstants.CONFIG_PATH + "npc_spawns/")
            outputDir.mkdirs()

            if (!sourceFile.exists()) {
                player.debug("npc_spawns.json not found.")
                return@define
            }

            val configs = gson.fromJson(FileReader(sourceFile), JsonArray::class.java)
            val regionMap = mutableMapOf<Int, MutableMap<Int, JsonObject>>() // region -> npc_id -> object

            for (configElement in configs) {
                val e = configElement.asJsonObject
                val npcId = e.get("npc_id").asInt
                val npcName = NPCDefinition.forId(npcId)?.name ?: "null"

                val locData = e.get("loc_data").asString
                val points = locData.split("-").map { it.trim() }.filter { it.isNotEmpty() }

                for (point in points) {
                    val tokens = point.removePrefix("{").removeSuffix("}").split(",").map { it.trim() }
                    if (tokens.size < 5) continue

                    val x = tokens[0].toInt()
                    val y = tokens[1].toInt()
                    val regionId = (x shr 6 shl 8) or (y shr 6)

                    val regionEntries = regionMap.computeIfAbsent(regionId) { mutableMapOf() }
                    val npcEntry = regionEntries.computeIfAbsent(npcId) {
                        JsonObject().apply {
                            addProperty("name", npcName)
                            addProperty("npc_id", npcId.toString())
                            addProperty("loc_data", "")
                        }
                    }

                    val updatedLocData = npcEntry.get("loc_data").asString + "$point-"
                    npcEntry.addProperty("loc_data", updatedLocData)
                }
            }

            for ((regionId, npcMap) in regionMap) {
                val outFile = File(outputDir, "region_$regionId.json")
                FileWriter(outFile).use { writer ->
                    gson.toJson(npcMap.values.toList(), writer)
                }
            }

            player.debug("Split ${configs.size()} NPC spawns into ${regionMap.size} regions.")
        }

        /*
         * Dump RangeWeapon definitions.
         */

        define(
            name = "dumpranged",
            privilege = Privilege.ADMIN,
            usage = "::dumpranged",
            description = "Dumps all RangeWeapon definitions to a .txt file."
        ) { player, _ ->

            val dump = File("dumps/ranged_weapons_config.txt")
            dump.parentFile.mkdirs()

            val builder = StringBuilder()

            core.game.node.entity.combat.equipment.RangeWeapon.getAll().forEach { weapon ->
                builder.appendLine(weapon.toString())
            }

            dump.writeText(builder.toString())

            player.debug("RangeWeapon definitions dumped to $dump.")
        }

        /*
         * Dump ammunition definitions.
         */

        define(
            name = "dumpammo",
            privilege = Privilege.ADMIN,
            usage = "::dumpammo",
            description = "Dumps all Ammunition definitions to a .txt file."
        ) { player, _ ->

            val dump = File("dumps/ammunition_config.txt")
            dump.parentFile.mkdirs()

            val builder = StringBuilder()

            core.game.node.entity.combat.equipment.Ammunition.getAll().forEach { ammo ->
                builder.appendLine(ammo.toString())
            }

            dump.writeText(builder.toString())

            player.debug("Ammunition definitions dumped to $dump.")
        }

        /*
         * Show icons.
         */

        define(
            name = "dumpiface",
            privilege = Privilege.ADMIN,
            usage = "::dumpiface <id>",
            description = "Dumps interface definitions as JSON to a file.",
        ) { p, args ->

            val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
            val dumps = File("dumps")
            if (!dumps.exists()) dumps.mkdirs()

            if (args.size < 2) {
                p.debug("Usage: ::dumpiface <id>")
                return@define
            }

            val i = args[1].toIntOrNull()
            if (i == null) {
                p.debug("Invalid interface id=$i.")
                return@define
            }

            val iface = IfaceDefinition.forId(i)
            if (iface == null) {
                p.debug("Interface for id=$i not found.")
                return@define
            }

            fun dumpIface(def: IfaceDefinition, isRoot: Boolean = false): Map<String, Any?>? {
                val map = mutableMapOf<String, Any?>()

                fun <T> addIfNotNull(key: String, value: T?) {
                    when (value) {
                        null -> return
                        is Int -> if (value == 0) return
                        is Boolean -> if (!value) return
                        is String -> if (value.isEmpty()) return
                        is IntArray -> {
                            val filtered = value.filter { it != 0 }
                            if (filtered.isEmpty()) return
                            map[key] = filtered
                            return
                        }
                        is Array<*> -> {
                            val filtered = value.mapNotNull {
                                when (it) {
                                    is IfaceDefinition -> dumpIface(it)
                                    is IntArray -> it.filter { v -> v != 0 }.takeIf { it.isNotEmpty() }
                                    is String -> if (it.isEmpty()) null else it
                                    else -> it
                                }
                            }.filterNotNull()
                            if (filtered.isEmpty()) return
                            map[key] = filtered
                            return
                        }
                    }
                    map[key] = value
                }

                if (isRoot) {
                    addIfNotNull("id", def.id)
                    addIfNotNull("parent", def.parent)
                }

                addIfNotNull("type", def.type?.name?.lowercase())
                addIfNotNull("version", def.version)
                addIfNotNull("clientCode", def.clientCode)
                addIfNotNull("baseX", def.baseX)
                addIfNotNull("baseY", def.baseY)
                addIfNotNull("baseWidth", def.baseWidth)
                addIfNotNull("baseHeight", def.baseHeight)
                addIfNotNull("dynWidth", def.dynWidth)
                addIfNotNull("dynHeight", def.dynHeight)
                addIfNotNull("xMode", def.xMode)
                addIfNotNull("yMode", def.yMode)
                addIfNotNull("overlayer", def.overlayer)
                addIfNotNull("hidden", def.hidden)
                addIfNotNull("scrollMaxH", def.scrollMaxH)
                addIfNotNull("scrollMaxV", def.scrollMaxV)
                addIfNotNull("noClickThrough", def.noClickThrough)
                addIfNotNull("spriteId", def.spriteId)
                addIfNotNull("activeSpriteId", def.activeSpriteId)
                addIfNotNull("angle2d", def.angle2d)
                addIfNotNull("hasAlpha", def.hasAlpha)
                addIfNotNull("spriteTiling", def.spriteTiling)
                addIfNotNull("alpha", def.alpha)
                addIfNotNull("outlineThickness", def.outlineThickness)
                addIfNotNull("shadowColor", def.shadowColor)
                addIfNotNull("hFlip", def.hFlip)
                addIfNotNull("vFlip", def.vFlip)
                addIfNotNull("modelType", def.modelType)
                addIfNotNull("activeModelType", def.activeModelType)
                addIfNotNull("modelId", def.modelId)
                addIfNotNull("activeModelId", def.activeModelId)
                addIfNotNull("unknownModelProp_1", def.unknownModelProp_1)
                addIfNotNull("unknownModelProp_2", def.unknownModelProp_2)
                addIfNotNull("modelXAngle", def.modelXAngle)
                addIfNotNull("modelYAngle", def.modelYAngle)
                addIfNotNull("modelYOffset", def.modelYOffset)
                addIfNotNull("modelZoom", def.modelZoom)
                addIfNotNull("modelAnimId", def.modelAnimId)
                addIfNotNull("activeModelAnimId", def.activeModelAnimId)
                addIfNotNull("modelOrtho", def.modelOrtho)
                addIfNotNull("unknownModelProp_3", def.unknownModelProp_3)
                addIfNotNull("unknownModelProp_4", def.unknownModelProp_4)
                addIfNotNull("unknownModelProp_5", def.unknownModelProp_5)
                addIfNotNull("unknownModelProp_6", def.unknownModelProp_6)
                addIfNotNull("unknownModelProp_7", def.unknownModelProp_7)
                addIfNotNull("font", def.font)
                addIfNotNull("text", def.text)
                addIfNotNull("activeText", def.activeText)
                addIfNotNull("vPadding", def.vPadding)
                addIfNotNull("halign", def.halign)
                addIfNotNull("valign", def.valign)
                addIfNotNull("shadowed", def.shadowed)
                addIfNotNull("color", def.color)
                addIfNotNull("activeColor", def.activeColor)
                addIfNotNull("overColor", def.overColor)
                addIfNotNull("unknownColor", def.unknownColor)
                addIfNotNull("filled", def.filled)
                addIfNotNull("lineWidth", def.lineWidth)
                addIfNotNull("unknownProp_8", def.unknownProp_8)
                addIfNotNull("unknownIntArray_1", def.unknownIntArray_1)
                addIfNotNull("unknownIntArray_2", def.unknownIntArray_2)
                addIfNotNull("unknownByteArray_1", def.unknownByteArray_1)
                addIfNotNull("unknownByteArray_2", def.unknownByteArray_2)
                addIfNotNull("optionBase", def.optionBase)
                addIfNotNull("ops", def.ops)
                addIfNotNull("dragDeadzone", def.dragDeadzone)
                addIfNotNull("dragDeadtime", def.dragDeadtime)
                addIfNotNull("dragRenderBehavior", def.dragRenderBehavior)
                addIfNotNull("opCircumfix", def.opCircumfix)
                addIfNotNull("opSuffix", def.opSuffix)
                addIfNotNull("option", def.option)
                addIfNotNull("unknownProp_9", def.unknownProp_9)
                addIfNotNull("unknownProp_10", def.unknownProp_10)
                addIfNotNull("unknownProp_11", def.unknownProp_11)
                addIfNotNull("cs1ComparisonOperands", def.cs1ComparisonOperands)
                addIfNotNull("cs1ComparisonOpcodes", def.cs1ComparisonOpcodes)
                addIfNotNull("cs1Scripts", def.cs1Scripts)
                addIfNotNull("objCounts", def.objCounts)
                addIfNotNull("objTypes", def.objTypes)
                addIfNotNull("invMarginX", def.invMarginX)
                addIfNotNull("invMarginY", def.invMarginY)
                addIfNotNull("invOffsetX", def.invOffsetX)
                addIfNotNull("invOffsetY", def.invOffsetY)
                addIfNotNull("invSprite", def.invSprite)
                addIfNotNull("buttonType", def.buttonType)
                addIfNotNull("invOptions", def.invOptions)

                def.scripts?.let { s ->
                    val sMap = mutableMapOf<String, Any?>()
                    s.unknown?.let { sMap["unknown"] = listOf(it.id, it.args) }
                    s.onMouseOver?.let { sMap["onMouseOver"] = listOf(it.id, it.args) }
                    s.onMouseLeave?.let { sMap["onMouseLeave"] = listOf(it.id, it.args) }
                    s.onUseWith?.let { sMap["onUseWith"] = listOf(it.id, it.args) }
                    s.onUse?.let { sMap["onUse"] = listOf(it.id, it.args) }
                    s.onVarpTransmit?.let { sMap["onVarpTransmit"] = listOf(it.id, it.args) }
                    s.onInvTransmit?.let { sMap["onInvTransmit"] = listOf(it.id, it.args) }
                    s.onStatTransmit?.let { sMap["onStatTransmit"] = listOf(it.id, it.args) }
                    s.onTimer?.let { sMap["onTimer"] = listOf(it.id, it.args) }
                    s.onOptionClick?.let { sMap["onOptionClick"] = listOf(it.id, it.args) }
                    s.onMouseRepeat?.let { sMap["onMouseRepeat"] = listOf(it.id, it.args) }
                    s.onClickRepeat?.let { sMap["onClickRepeat"] = listOf(it.id, it.args) }
                    s.onDrag?.let { sMap["onDrag"] = listOf(it.id, it.args) }
                    s.onRelease?.let { sMap["onRelease"] = listOf(it.id, it.args) }
                    s.onHold?.let { sMap["onHold"] = listOf(it.id, it.args) }
                    s.onDragStart?.let { sMap["onDragStart"] = listOf(it.id, it.args) }
                    s.onDragRelease?.let { sMap["onDragRelease"] = listOf(it.id, it.args) }
                    s.onScroll?.let { sMap["onScroll"] = listOf(it.id, it.args) }
                    s.onVarcTransmit?.let { sMap["onVarcTransmit"] = listOf(it.id, it.args) }
                    s.onVarcstrTransmit?.let { sMap["onVarcstrTransmit"] = listOf(it.id, it.args) }
                    if (sMap.isNotEmpty()) map["scripts"] = sMap
                }

                def.triggers?.let { t ->
                    val tMap = mutableMapOf<String, Any?>()
                    t.varpTriggers?.let { tMap["varpTriggers"] = it }
                    t.inventoryTriggers?.let { tMap["inventoryTriggers"] = it }
                    t.statTriggers?.let { tMap["statTriggers"] = it }
                    t.varcTriggers?.let { tMap["varcTriggers"] = it }
                    t.varcstrTriggers?.let { tMap["varcstrTriggers"] = it }
                    if (tMap.isNotEmpty()) map["triggers"] = tMap
                }

                def.children?.let { children ->
                    val childMap = children.mapNotNull { it?.let { dumpIface(it) } }
                    if (childMap.isNotEmpty()) map["children"] = childMap
                }

                return if (map.isEmpty()) null else map
            }

            val dumpMap = dumpIface(iface, isRoot = true)
            if (dumpMap != null) {
                val dump = File(dumps, "$i.json")
                dump.writeText(gson.toJson(dumpMap))
                p.debug("Iface id=$i dumped to $dump.")
            } else {
                p.debug("Iface id=$i has no dumpable data.")
            }
        }

        /*
         * Dumps for educational purposes item definitions into a .json.
         */

        define(
            name = "dumpitems",
            privilege = Privilege.ADMIN,
            usage = "::dumpitems",
            description = "Dumps item definitions data to a .json file.",
        ) { p, _ ->
            val gson = GsonBuilder().setPrettyPrinting().create()
            val dump = File("dumps/item_definitions.json")
            dump.parentFile?.let { parent ->
                if (!parent.exists()) parent.mkdirs()
            }
            val items = mutableListOf<Map<String, Any?>>()

            for (itemId in 0 until Cache.getItemDefinitionsSize()) {
                val itemDef = ItemDefinition.forId(itemId) ?: continue
                val itemMap =
                    itemDef::class
                        .memberProperties
                        .filter { prop ->
                            prop.returnType.classifier !in listOf(ItemDefinition::class, List::class, Map::class)
                        }.associate { prop ->
                            prop.isAccessible = true
                            try {
                                prop.name to prop.getter.call(itemDef)
                            } catch (e: Exception) {
                                prop.name to "Error"
                            }
                        }

                if (itemMap.isNotEmpty()) {
                    items.add(itemMap)
                }
            }
            dump.writeText(gson.toJson(items))
            p.debug("Item data has been successfully dumped to $dump.")
        }

        /*
         * Command to dump all CS2 mappings to a file.
         */

        define(
            name = "dumpcs2",
            privilege = Privilege.ADMIN,
            usage = "::dumpcs2",
            description = "Dumps all CS2 mappings to ./cs2.txt",
        ) { player, _ ->
            val path = Paths.get("dumps/cs2.txt")
            Files.createDirectories(path.parent)
            try {
                BufferedWriter(Files.newBufferedWriter(path)).use { bw ->
                    for (i in 0 until 10000) {
                        val mapping = CS2Mapping.forId(i) ?: continue
                        val mappingMap = mapping.map ?: continue
                        bw.append("ScriptAPI - $i [")
                        for ((index, value) in mappingMap) {
                            bw.append("$value: $index ")
                        }
                        bw.append("]")
                        bw.newLine()
                    }
                }
                player.debug("CS2 mappings dumped successfully to dumps/cs2.txt")
            } catch (e: Exception) {
                player.debug("Failed to dump CS2 mappings: ${e.message}")
                e.printStackTrace()
            }

            return@define
        }

        /*
         * Dumps for educational purposes the NPC definitions into a .json file.
         */

        define(
            name = "dumpnpcs",
            privilege = Privilege.ADMIN,
            usage = "::dumpnpcs",
            description = "Dumps NPC definitions data to a .json file.",
        ) { p, _ ->
            val gson =
                GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                    .create()

            val dump = File("dumps/npc_definitions.json")
            dump.parentFile?.let { parent ->
                if (!parent.exists()) parent.mkdirs()
            }
            val items = mutableListOf<Map<String, Any?>>()

            val excludedFields = setOf("handlers")

            for (npcId in 0 until Cache.getNPCDefinitionsSize()) {
                val npcDef = NPCDefinition.forId(npcId) ?: continue
                val npcMap = mutableMapOf<String, Any?>()

                for (prop in npcDef::class.memberProperties) {
                    if (prop.name in excludedFields) {
                        continue
                    }

                    prop.isAccessible = true
                    try {
                        val value = prop.getter.call(npcDef)

                        npcMap[prop.name] =
                            when (value) {
                                is String -> {
                                    if (prop.name == "combat_audio") {
                                        value
                                    } else {
                                        value
                                            .contains("Animation [priority=")
                                            .toString()
                                            .replace("Animation [priority=", "priority=")
                                            .replace("]", " ")
                                    }
                                }

                                is Number, is Boolean -> value
                                is List<*> -> value.map { it.toString() }
                                is Map<*, *> -> value.mapValues { it.value.toString() }
                                is ShortArray -> value.toList().map { it.toInt().toString() }
                                is ByteArray -> value.toList().map { it.toString() }
                                is IntArray -> value.toList().map { it.toInt().toString() }
                                is Array<*> ->
                                    when (value) {
                                        is ShortArray -> value.toList().map { it.toInt().toString() }
                                        is ByteArray -> value.toList().map { it.toString() }
                                        is IntArray -> value.toList().map { it.toInt().toString() }
                                        else -> value.toList().joinToString(",") { it.toString() }
                                    }

                                null -> "null"
                                else -> value.toString()
                            }
                    } catch (e: Exception) {
                        p.debug("Err read prop '${prop.name}': ${e.message}")
                        npcMap[prop.name] = "Err: ${e.message}"
                    }
                }

                if (npcMap.isNotEmpty()) {
                    items.add(npcMap)
                }
            }

            try {
                dump.writeText(gson.toJson(items))
                p.debug("NPC data has been successfully dumped to $dump.")
            } catch (e: Exception) {
                p.debug("Error saving JSON file: ${e.message}")
            }
        }
    }
}
