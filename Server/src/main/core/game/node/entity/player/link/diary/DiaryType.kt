package core.game.node.entity.player.link.diary

import content.data.items.LampItem
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Items
import shared.consts.NPCs
import java.util.*

/**
 * The Diary type.
 */
enum class DiaryType(
    val displayName: String,
    val child: Int,
    val levelNames: Array<String>,
    val achievements: Array<Array<String>>,
    val rewards: Array<Array<Item>>,
    val info: String,
    val npcs: IntArray
) {
    KARAMJA(
        "Karamja", 11,
        arrayOf("Easy", "Medium", "Hard"),
        arrayOf(
            arrayOf(
                "Pick 5 bananas from the plantation located east of the <br><br>volcano",
                "Use the rope swing to travel to the small island north-west<br><br>of Karamja, where the moss giants are",
                "Mine some gold from the rocks on the north-west<br><br>peninsula of Karamja",
                "Travel to Port Sarim via the dock, east of Musa Point",
                "Travel to Ardougne via the port near Brimhaven",
                "Explore Cairn Island to the west of Karamja",
                "Use the Fishing spots north of the banana plantation",
                "Collect 5 seaweed from anywhere on Karamja",
                "Attempt the TzHaar Fight Pits or Fight Cave",
                "Kill a Jogre in the Pothole dungeon"
            ),
            arrayOf(
                "Claim a ticket from the Agility arena in Brimhaven",
                "Discover hidden wall in the dungeon below the volcano",
                "Visit the Isle of Crandor via the dungeon below the volcano",
                "Use Vigroy and Hajedy's cart service",
                "Earn 100% favour in the village of Tai Bwo Wannai Cleanup",
                "Cook a spider on stick",
                "Charter the Lady of the Waves from Cairn Isle to Port Khazard",
                "Cut a log from a teak tree",
                "Cut a log from a mahogany tree",
                "Catch a karambwan",
                "Exchange gems, a gout tuber, trading sticks for a machete",
                "Use the gnome glider to travel to Karamja",
                "Grow a healthy fruit tree in the patch near Brimhaven",
                "Trap a Horned Graahk",
                "Chop the vines to gain deeper access to Brimhaven Dungeon",
                "Cross the lava using the stepping stones within Brimhaven<br><br>Dungeon",
                "Climb the stairs within Brimhaven Dungeon",
                "Charter a ship from the shipyard in the far east of Karamja",
                "Mine a red topaz from a gem rock"
            ),
            arrayOf(
                "Become the Champion of the Fight Pits",
                "Successfully kill a Ket-Zek in the Fight Caves",
                "Eat an oomlie wrap",
                "Craft some nature runes",
                "Cook a karambwan thoroughly",
                "Kill a deathwing in the dungeon under the Kharazi Jungle",
                "Use the crossbow shortcut south of the volcano",
                "Collect five palm leaves",
                "Be assigned a Slayer task by Duradel or Lapalok in Shilo<br><br>Village.",
                "Kill a metal dragon in Brimhaven Dungeon."
            )
        ),
        arrayOf(
            arrayOf(Item(Items.KARAMJA_GLOVES_1_11136), Item(LampItem.KARAMJA_ACHIEVEMENT_EASY.item)),
            arrayOf(Item(Items.KARAMJA_GLOVES_2_11138), Item(LampItem.KARAMJA_ACHIEVEMENT_MEDIUM.item)),
            arrayOf(Item(Items.KARAMJA_GLOVES_3_11140), Item(LampItem.KARAMJA_ACHIEVEMENT_HARD.item))
        ),
        "To start marking off tasks in your journal, speak to Pirate<br><br>Jackie the Fruit in Brimhaven, Kaleb Paramay in Shilo<br><br>Village or one of the Jungle Foresters north of the<br><br>Kharazi Jungle.",
        intArrayOf(NPCs.PIRATE_JACKIE_THE_FRUIT_1055, NPCs.KALEB_PARAMAYA_512, NPCs.JUNGLE_FORESTER_401)

    ),
    VARROCK(
        "Varrock", 15,
        arrayOf("Easy", "Medium", "Hard"),
        arrayOf(
            arrayOf(
                "Have Thessalia show you what outfits you can wear",
                "Have Aubury teleport you to the essence mine",
                "Mine some Iron in the south east mining patch near Varrock",
                "Make a plank at the sawmill",
                "Enter the second level of the Stronghold of Security",
                "Jump over the fence south of Varrock",
                "Chop down a dying tree in the Lumber Yard",
                "Buy a copy of the Varrock Herald",
                "Give a stray dog a bone",
                "Spin a bowl on the pottery wheel and fire it in the oven in<br><br>Barbarian Village",
                "Enter Edgeville Dungeon using the entrance to the south of<br><br>Edgeville",
                "Move your player-owned house portal to a different location<br><br>using the Varrock estate agent",
                "Speak to Haig Halen after obtaining at least 50 quest points",
                "Enter the Earth Altar using an earth tiara or talisman",
                "Have Elsie tell you a story",
                "Mine some limestone near Paterdomus, the temple to the east<br><br>of Varrock",
                "Catch a trout in the river to the east of Barbarian Village.",
                "Venture through the cobwebbed corridor in Varrock Sewers",
                "Find the highest point in Varrock"
            ),
            arrayOf(
                "Have the Apothecary in Varrock make you a strength potion.",
                "Enter the Champions Guild.",
                "Take the Dagon'Hai shortcut to the chaos portal",
                "Get a full complement of rats on your rat pole",
                "Escape from the spider lair in Varrock Sewers with some red<br><br>spiders eggs",
                "Use the spirit tree north of Varrock",
                "Perform the 4 emotes from the Stronghold of Security.",
                "Select a colour for a new kitten",
                "Use the shortcut under the wall, north-west of the Grand<br><br>Exchange",
                "Enter the A Soul's Bane rift",
                "Teleport to the Digsite using a Digsite pendant",
                "Craft an earth tiara on the Earth Altar",
                "Pickpocket a guard in the Varrock Palace courtyard",
                "Use the teleport to Varrock spell",
                "Get a Slayer task from Vannaka",
                "Buy twenty mahogany planks from the Sawmill Operator in one<br><br>transaction",
                "Pick a fruit from the White Tree",
                "Use the hot air balloon to travel from Varrock to somewhere<br><br>else",
                "Get a cat training medal from Gertrude",
                "Dial to the fairy ring west of Varrock",
                "Browse through Oziach's Armour Shop"
            ),
            arrayOf(
                "Pick poison ivy from your bush Farming patch in Varrock<br><br>(west of Champions' Guild)",
                "Use the pipe shortcut in Varrock Sewers, near the moss<br><br>giants",
                "Trade furs with the Fancy Dress Seller for a spottier cape",
                "Smith an adamantite medium helm on the south-east anvil in<br><br>Varrock, next to Aubury's Rune Shop",
                "Speak to Orlando Smith when you have achieved 153 kudos",
                "Talk to Romily Weaklax and give him a wild pie",
                "Craft an air battlestaff",
                "Give your player-owned house a tropical wood or fancy stone<br><br>finish at the Varrock estate agent's",
                "Make a Varrock teleport tablet on a mahogany lectern",
                "Obtain a new set of Family Crest gauntlets from Dimintheis",
                "Make a Waka Canoe near Edgeville",
                "Use the Home Teleport spell in the Ancient Magicks spellbook<br><br>to teleport to Edgeville",
                "Use the skull sceptre to teleport to Barbarian Village"
            )
        ),
        arrayOf(
            arrayOf(Item(Items.VARROCK_ARMOUR_1_11756), Item(LampItem.VARROCK_ACHIEVEMENT_EASY.item)),
            arrayOf(Item(Items.VARROCK_ARMOUR_2_11757), Item(LampItem.VARROCK_ACHIEVEMENT_MEDIUM.item)),
            arrayOf(Item(Items.VARROCK_ARMOUR_3_11758), Item(LampItem.VARROCK_ACHIEVEMENT_HARD.item))
        ),
        "To start marking off tasks in your journal, speak to<br><br>Rat Burgiss south of the city, Reldo in the palace<br><br>library, or Vannaka in the sewers.",
        intArrayOf(NPCs.RAT_BURGISS_5833, NPCs.RELDO_2660, NPCs.VANNAKA_1597)

    ),
    LUMBRIDGE(
        "Lumbridge", 2,
        arrayOf("Beginner", "Easy", "Medium"),
        arrayOf(
            arrayOf(
                "Climb to the highest point in Lumbridge",
                "Raise the flag on the roof of the Lumbridge Bank",
                "Speak to the Duke of Lumbridge",
                "Speak with the Doomsayer about the Warning System",
                "Pass through the Al Kharid gate",
                "Mine some clay in the Mining patch north of the Champions'<br><br>Guild",
                "Make some soft clay in the Barbarian Village",
                "Fire a pot in the kiln in the Barbarian Village potter's<br><br>house",
                "Enter the courtyard of the spooky mansion in Draynor Village",
                "Visit the Draynor Village market",
                "Find out about the Rules of Conduct from the Draynor<br><br>Town Crier",
                "Climb to the top of the Wizards' Tower",
                "Mine some copper in the Mining spot to the south-east of<br><br>Lumbridge Swamp",
                "Catch some shrimp in the Fishing spot to the east of<br><br>Lumbridge Swamp",
                "Have the Fishing Tutor send you on an errand",
                "Look through Father Aereck's selection of gravestones",
                "Play the organ in the Lumbridge Church",
                "Find out about the Stronghold of Security from the Lumbridge<br><br>Guide",
                "Browse the Lumbridge General Store",
                "Visit Fred the Farmer's chicken and sheep farm",
                "Grind some flour in the windmill north of Lumbridge"
            ),
            arrayOf(
                "Mine some iron ore from the Al Kharid Mining spot",
                "Obtain a cow-hide from a cow in the field north-east of<br><br>Lumbridge",
                "Have Ellis tan your cow-hide to make soft leather at his<br><br>shop in Al Kharid",
                "Craft a pair of soft leather gloves",
                "Catch a pike in the river to the east of Lumbridge Castle",
                "Smelt a steel bar in the Lumbridge furnace",
                "Search the shed in Lumbridge Swamp",
                "Kill a giant rat in Lumbridge Swamp",
                "Cut down a dead tree in Lumbridge Swamp",
                "Light a campfire from normal logs in Lumbridge Swamp",
                "Cook some rat meat on a campfire in Lumbridge Swamp",
                "Craft a water rune at the Water Altar",
                "Get a replacement Ghostspeak Amulet from Father Urhney",
                "Taunt the demon at the top of the Wizard's Tower",
                "Have Sedridor teleport you to the Essence Mine",
                "Access the bank in Draynor Village",
                "Have the Wise Old man check your bank for unnecessary<br><br>quest-related items",
                "Discover what the Wise Old man is watching through his<br><br>telescope",
                "Defeat a zombie in the sewers under the jail"
            ),
            arrayOf(
                "Smith a steel longsword on the anvil in the jailhouse<br><br>sewers",
                "Take a ride on Gnomecopter",
                "Use the teleport Lumbridge spell",
                "Light a willow log fire in Lumbridge Castle courtyard",
                "Cook a lobster on the range in Lumbridge Castle kitchen",
                "Obtain an Anti-dragonbreath shield from Duke Horacio",
                "Cut a willow tree, east of Lumbridge Castle",
                "Smelt a silver bar in the Lumbridge furnace",
                "Craft a holy symbol in the Lumbridge furnace",
                "Catch a salmon in the river to the east of Lumbridge Castle",
                "Mine some silver from the mining spot north of Al Kharid",
                "Mine some coal in the Mining spot south-west of Lumbridge<br><br>Swamp"
            )
        ),
        arrayOf(
            arrayOf(Item(Items.EXPLORERS_RING_1_13560), Item(LampItem.LUMBRIDGE_ACHIEVEMENT_BEGINNER.item)),
            arrayOf(Item(Items.EXPLORERS_RING_2_13561), Item(LampItem.LUMBRIDGE_ACHIEVEMENT_EASY.item)),
            arrayOf(Item(Items.EXPLORERS_RING_3_13562), Item(LampItem.LUMBRIDGE_ACHIEVEMENT_MEDIUM.item))
        ),
        "To start marking off tasks in your journal, speak to<br><br>Explorer Jack near the Lumbridge General Store, Bob<br><br>in his axe store or Ned in his house in Draynor Village.",
        intArrayOf(NPCs.EXPLORER_JACK_7969, NPCs.BOB_519, NPCs.NED_743)

    ),
    FALADOR(
        "Falador", 23,
        arrayOf("Easy", "Medium", "Hard"),
        arrayOf(
            arrayOf(
                "Buy a Farming amulet from Sarah on the farm north of Port<br><br>Sarim",
                "Buy a stat-boosting beer from a waitress in the Rising Sun<br><br>Tavern",
                "Buy a black chainbody from Wayne's Chains, and try it on in<br><br>the shop",
                "Climb to the top of the White Knights' Castle",
                "Discover your family crest from Sir Renitee",
                "Enter the mole's lair under Falador Park",
                "Feed Ridgeley, the hairdresser's pet",
                "Fill a bucket from the pump north of the west Falador bank",
                "Heal an elemental wizard by casting an appropriate elemental<br><br>spell on him (Air, Water, Earth, Fire)",
                "Kill a duck in Falador Park",
                "Kill a highwayman on the road south of Falador",
                "Make an air tiara",
                "Pop a party balloon",
                "Recharge your Prayer points at the altar south-west of Port<br><br>Sarim",
                "Take the boat to Entrana"
            ),
            arrayOf(
                "Craft a Fruit basket using the loom at the farm north of<br><br>Port Sarim",
                "Crawl under Falador's south wall",
                "Grapple up, and then jump off the north Falador wall",
                "Increase your reputation with the White Knights by killing<br><br>a black knight",
                "Kill an ice giant in the Asgarnian Ice Dungeon",
                "Light a bullseye lantern in the chemist's",
                "Pickpocket a Falador guard",
                "Place a scarecrow to protect your sweetcorn as it grows in<br><br>the patch north of Port Sarim",
                "Salute Sir Tiffy Cashien while wearing full initiate armour",
                "Smith blurite crossbow limbs on Thurgo's anvil",
                "Travel from Port Sarim to Musa Point for free (with a little<br><br>help from Charos)",
                "Visit the Port Sarim rat pits"
            ),
            arrayOf(
                "Ascend the Dark Wizards' Tower while wearing full proselyte<br><br>armour",
                "Change your family crest to the Saradomin symbol",
                "Craft 196 or more air runes simultaneously",
                "Cut down a Yew tree or Magic tree that you grew in Falador<br><br>Park",
                "Dial to the fairy ring on Mudskipper Point",
                "Dye a cape pink with Pink dye from Betty in Port Sarim",
                "Enter the Mining Guild",
                "Kill a mogre at Mudskipper Point",
                "Kill a skeletal wyvern in the Asgarnian Ice Dungeon",
                "Summon an Ibis in the Port Sarim fish store"
            )
        ),
        arrayOf(
            arrayOf(Item(Items.FALADOR_SHIELD_1_14577), Item(LampItem.FALADOR_ACHIEVEMENT_EASY.item)),
            arrayOf(Item(Items.FALADOR_SHIELD_2_14578), Item(LampItem.FALADOR_ACHIEVEMENT_MEDIUM.item)),
            arrayOf(Item(Items.FALADOR_SHIELD_3_14579), Item(LampItem.FALADOR_ACHIEVEMENT_HARD.item))
        ),
        "To start marking off tasks in your journal, speak to<br><br>Redbeard Frank outside the bar in Port Sarim, The<br><br>Chemist west of Rimmington, or Sir Vyvin's Squire in the<br><br>Falador Castle courtyard.",
        intArrayOf(NPCs.REDBEARD_FRANK_375, NPCs.CHEMIST_367, NPCs.SQUIRE_606)

    ),
    FREMENNIK(
        "Fremennik", 19,
        arrayOf("Easy", "Medium", "Hard"),
        arrayOf(
            arrayOf(
                "Kill a cave crawler in the Fremennik Slayer Dungeon",
                "Kill five rock crabs on the shore near Rellekka or on<br><br>Waterbirth Island",
                "Find the highest tree on the Fremennik mainland",
                "View the rewards in the Barbarian Assault tutorial",
                "Speak to Otto Godblessed about barbarian training",
                "Collect three seaweed from the shore north-east of Rellekka",
                "Find the Hunting Expert on the northern ice plains",
                "Catch a fish off one of Rellekka's piers",
                "Recharge your Summoning points near Rellekka's gate",
                "Kill an adult black unicorn"
            ),
            arrayOf(
                "Learn of the history of the Fremennik and the Outerlanders<br><br>from Chieftain Brundt",
                "Watch a shouting match between Fremennik Isles tower guards<br><br>(the guards can be found between Jatizso and Neitiznot in<br><br>one of the towers)",
                "Interact with a Pet rock",
                "Make three vials in the furnace building at Rellekka",
                "Charm the Fossegrimen into accepting a raw bass",
                "Wear yak-hide armour and kill an ice troll",
                "Make cheese in the dairy churn in Rellekka",
                "Use a fairy ring to appear on a mountaintop, near the<br><br>windswept tree",
                "Look at Yrsa's options for recolouring your boots in her<br><br>clothes shop in Rellekka",
                "Successfully hunt a sabre-toothed kyatt",
                "Steal a fish from the fishing stall in the Rellekka<br><br>marketplace"
            ),
            arrayOf(
                "Kill three dagannoths in the first layer of the Waterbirth<br><br>Island Dungeon",
                "Wear rockshell, spined or skeletal armour and have the<br><br>locals use an honorific with your Fremennik name",
                "Complete the Barbarian Outpost Agility Course",
                "Mine pure essence on Lunar Isle",
                "Make a barbarian pyre ship from arctic pine",
                "Catch a tuna without a harpoon",
                "Bake a pie using Magic",
                "Kill a Mithril dragon",
                "Get mahogany from your Etceterian subjects"
            )
        ),
        arrayOf(
            arrayOf(Item(Items.FREMENNIK_SEA_BOOTS_1_14571), Item(LampItem.FREMENNIK_ACHIEVEMENT_EASY.item)),
            arrayOf(Item(Items.FREMENNIK_SEA_BOOTS_2_14572), Item(LampItem.FREMENNIK_ACHIEVEMENT_MEDIUM.item)),
            arrayOf(Item(Items.FREMENNIK_SEA_BOOTS_3_14573), Item(LampItem.FREMENNIK_ACHIEVEMENT_HARD.item))
        ),
        "To start marking off tasks in your journal, speak to<br><br>Council Workman on the bridge between the Seers' Village<br><br>and Rellekka, Yrsa in Rellekka or Advisor Ghrim on the<br><br>Miscellania.",
        intArrayOf(NPCs.COUNCIL_WORKMAN_1287, NPCs.YRSA_1301, NPCs.ADVISOR_GHRIM_1375)
    ),
    SEERS_VILLAGE(
        "Seers' Village",
        27,
        arrayOf("Easy", "Medium", "Hard"),
        arrayOf(
            arrayOf(
                "Pick five flax from the flax field",
                "Walk clockwise around the big mysterious statue",
                "Have Sir Galahad make you a cup of tea",
                "Take the poison chalice to King Arthur",
                "Spin five bow strings",
                "Fill five pots with flour from the Sinclair Mansion",
                "Give five locals a glass of cider in the Forester's Arms",
                "Plant some jute",
                "Use the churn in the Sinclair Mansion garden",
                "Buy a candle from the candle-maker",
                "Pray at the Seers' Village altar",
                "Catch a mackerel"
            ),
            arrayOf(
                "Use the Sinclair Mansion to Fremennik agility shortcut",
                "Talk to Thormac the Sorcerer about making mystic staves",
                "Transport a full load of coal to Seers' Village",
                "Find the highest point in Seers' Village",
                "Defeat each type of elemental in the Elemental Workshop",
                "Teleport to Camelot",
                "Kill one guard on each tower of the Ranging Guild using a longbow",
                "Have the Ranging Guild competition judge congratulate you<br><br>for acquiring over 1,000 Archery tickets",
                "Buy something from the ticket exchange in the Ranging Guild",
                "Use a familiar to make a fire from maple logs within Seers'<br><br>Village",
                "Get a pet fish from Harry",
                "Catch and cook a bass in Catherby"
            ),
            arrayOf(
                "Teleport to the Ranging Guild",
                "Cut five sets of yew logs",
                "Fletch a magic shortbow in the Seers' Village Bank",
                "Enter the Seers' Village courthouse with your Piety prayer<br><br>turned on",
                "Use the fairy ring in McGrubor's Wood",
                "Burn a magic log in Seers' Village",
                "High Alch a magic shortbow in the Seers' Village bank",
                "Catch five sharks in Catherby",
                "Cook 5 sharks in Catherby using the Cooking gauntlets",
                "Charge five water orbs in one go",
                "Use the grapple shortcut to get from the water obelisk<br><br>island to Catherby shore"
            )
        ),
        arrayOf(
            arrayOf(Item(Items.SEERS_HEADBAND_1_14631), Item(LampItem.SEERS_ACHIEVEMENT_EASY.item)),
            arrayOf(Item(Items.SEERS_HEADBAND_2_14640), Item(LampItem.SEERS_ACHIEVEMENT_MEDIUM.item)),
            arrayOf(Item(Items.SEERS_HEADBAND_3_14641), Item(LampItem.SEERS_ACHIEVEMENT_HARD.item))
        ),
        "To start marking off tasks in your journal, speak to<br><br>any seer in Seers' Village, Stankers by the coal<br><br>trucks or Sir Kay in Camelot.",
        intArrayOf(NPCs.SEER_388, NPCs.STANKERS_383, NPCs.SIR_KAY_241)

    ),
    ARDOUGNE(
        "Ardougne",
        31,
        arrayOf("Easy", "Medium", "Hard"),
        arrayOf(
            arrayOf( // Easy
                "Have Wizard Cromperty teleport you to the essence mine",
                "Attempt to steal from any stall in Ardougne Market, or from any guard",
                "Sell silk to the silk trader in Ardougne for 60 coins each",
                "Use the altar in West Ardougne's church",
                "Enter the Castle Wars waiting room",
                "Go out fishing on the Fishing Trawler",
                "Enter the Combat Training Camp north of Ardougne",
                "Talk to a civilian in West Ardougne about selling a cat",
                "Kill a Unicow in the Tower of Life",
                "Get a civilian to throw a tomato at the army recruiter in West Ardougne",
                "Sail from East Ardougne to Karamja",
                "Attempt to pick-lock a certain door east of the gem stall in Ardougne's marketplace",
                "Spend some Penguin points with Larry (or Chuck) at Ardougne Zoo",
                "Use the Summoning obelisk to the east of the Fight Arena",
                "Pop a balloon in the monastery south of Ardougne",
                "Buy a water-filled vial from the general store run by Aemad and Kortan",
                "Use the noticeboard near the Observatory to gather information about shooting stars",
                "Kill something on the Khazard Battlefield",
                "Buy a skewered kebab from the Poison Arrow pub",
                "View the Hunter equipment in Aleck's shop",
                "Talk to the head servant at the servants' guild to find out about servants for your house",
                "Use a Ring of Dueling to teleport to Castle Wars",
                "Talk to Tindel Marchant about identifying swords"
            ),
            arrayOf(// Medium
                "Enter the unicorn pen in Ardougne Zoo using the fairy rings",
                "Teleport to the Wilderness using the lever in Ardougne",
                "Grapple over Yanille's southern wall",
                "Craft some runes (any kind) at Ourania Altar",
                "Sell some rubium to Ezekial Lovecraft in the town of Witchaven",
                "Pick watermelons from the Farming patch north of Ardougne",
                "Cast the Watchtower Teleport spell",
                "Travel to Castle Wars by hot-air balloon",
                "Claim buckets of sand from Bert in Yanille",
                "After Meeting History, return to the past and talk to Sarah about Herblore",
                "Catch a fish at the Fishing Platform using either a net or rod",
                "Cross the River Dougne using the log balance",
                "Pickpocket the master farmer north of Ardougne",
                "Attempt to steal a nature rune from a chest in Ardougne",
                "Mine some coal from the rocks east of Ardougne",
                "Kill a swordchick in the Tower of Life",
            ),
            arrayOf( // Hard
                "Recharge a bracelet of combat or a necklace of skills at the Legends' Guild",
                "Kill a shadow warrior in the Legends' Guild basement",
                "Enter the Magic Guild in Yanille",
                "Use the portal in the Magic Guild to teleport to Thormac's tower (west portal)",
                "Be on the winning side in a game of Castle Wars",
                "Cast the Ourania Teleport spell",
                "Pickpocket a watchman in Yanille while wearing gloves of silence",
                "Kill a frogeel in the Tower of Life",
                "Have a zookeeper put you in Ardougne Zoo's monkey cage",
                "Kill your own jade vine after the Back to my Roots quest",
                "Use the Air Guitar emote near the musician southeast of the monastery south of Ardougne",
                "Cast the Ardougne Teleport spell",
                "Cross the monkey bars in the Yanille Agility Dungeon",
                "Catch a red salamander from the Hunter area outside of Ourania Altar",
                "Pick a papaya or coconut from the fruit tree patch near Tree Gnome Village",
                "Attempt to steal blood runes from the chest in the Chaos Druid Tower's dungeon",
                "Use a catapult in Castle Wars after gaining the knowledge from the Catapult Construction quest"
            ),
        ),
        arrayOf(

            // - Ability to teleport to the monastery south of Ardougne an unlimited number of times
            // - Better chance at successful thieving in Ardougne
            // - Twice as many death runes when trading a cat to a citizen in West Ardougne
            // - More drops in the Tower of Life will become noted.
            // - A boost of Prayer on entry to Castle Wars

            arrayOf(Item(Items.ARDOUGNE_CLOAK_1_14701), Item(LampItem.ARDOUGNE_ACHIEVEMENT_EASY.item)),

            // - Teleport to Ardougne Farm once per day.
            // - Better chance at successful pickpocket in Ardougne.
            // - Better chance at getting more runes at Ourania Altar.
            // - Even more of the drops from the Tower of Life will be noted.
            // - 100 Pure Essence per day from Wizard Cromperty.
            // - Longer immunity from attack once teleporting to the wilderness from the Ardougne Lever.
            // - Toggle Ring of life teleport to Ardougne.

            arrayOf(Item(Items.ARDOUGNE_CLOAK_2_14702), Item(LampItem.ARDOUGNE_ACHIEVEMENT_MEDIUM.item)),

            // - Better chance at stealing and pickpocketing anywhere in RuneScape.
            // - Magical traps on chests will teleport the player to Ardougne's North Bank, instead of a random location.
            // - Even more drops from the creatures in the Tower of Life will be noted.
            // - Wizard Cromperty will give 150 pure essence per day.
            // - Ability to change the Watchtower teleport to take the player within Yanille's walls.

            arrayOf(Item(Items.ARDOUGNE_CLOAK_3_14703), Item(LampItem.ARDOUGNE_ACHIEVEMENT_HARD.item))
        ),
        "To start making off tasks in your journal, speak to Doctor" + "Orbon in East Ardougne's church, Ardougne's town crier in" + "the marketplace, or Aleck in Yannile.",
        intArrayOf(NPCs.DOCTOR_ORBON_290, NPCs.TOWN_CRIER_6138, NPCs.ALECK_5110)
    );

    /**
     * Returns the NPC id associated with a diary level.
     *
     * @param level The level index of the diary.
     * @return The NPC id that can be spoken to for this level's rewards.
     */
    fun getNpc(level: Int): Int = npcs[level]

    /**
     * Checks whether the player has any reward equipment from this diary type.
     *
     * @param player The player to check.
     * @return True if the player has any non-lamp reward equipment, false otherwise.
     */
    fun hasRewardEquipment(player: Player): Boolean {
        return rewards.any { tier ->
            tier.any { item ->
                !item.name.lowercase(Locale.getDefault()).contains("lamp") && player.hasItem(item)
            }
        }
    }

    /**
     * Retrieves all reward items for a diary level.
     *
     * @param level The level index.
     * @return An array of items representing the rewards for the level.
     */
    fun getRewards(level: Int): Array<Item> = rewards[level]

    /**
     * Retrieves all achievement tasks for a diary level.
     *
     * @param level The level index.
     * @return An array of strings representing the tasks for the level.
     */
    fun getAchievements(level: Int): Array<String> = achievements[level]

    companion object {
        /**
         * Returns the diary type for child id.
         *
         * @param child The interface child id.
         * @return The matching DiaryType, or null if no match is found.
         */
        fun forChild(child: Int): DiaryType? =
            values().find { type -> child in type.child..(type.child + 3) }
    }
}