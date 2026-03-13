package content.region.kandarin.baxtorian

import content.global.plugins.item.books_and_scrolls.BookInterface
import content.global.plugins.item.books_and_scrolls.BookLine
import content.global.plugins.item.books_and_scrolls.Page
import content.global.plugins.item.books_and_scrolls.PageSet
import core.api.getAttribute
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.tools.BLUE
import shared.consts.Items

class BarbarianBook : InteractionListener {
    companion object {
        private val TITLE = "What Otto told me."

        private val BARB_TRAINING_BASIC_PAGE = arrayOf(
            PageSet(
                Page(
                    BookLine("I have noted down what Otto", 97),
                    BookLine("has told me in this journal, so", 68),
                    BookLine("that I may not forget my", 69),
                    BookLine("tasks. His instructions are", 70),
                    BookLine("thus faithfully recorded for", 71),
                    BookLine("posterity.", 72),
                ),
            ),
        )

        private val FISHING_BASICS = arrayOf(
            PageSet(
                Page(
                    BookLine("I have noted down what Otto", 97),
                    BookLine("has told me in this journal, so", 68),
                    BookLine("that I may not forget my", 69),
                    BookLine("tasks. His instructions are", 70),
                    BookLine("thus faithfully recorded for", 71),
                    BookLine("posterity.", 72),
                    BookLine(BLUE + "Otto's words on Fishing with", 74),
                    BookLine(BLUE + "rods.", 75),
                    BookLine("'While you civilised folk use", 77),
                    BookLine("small, weak fishing rods, we", 78),
                ),
                Page(
                    BookLine("barbarians are skilled with", 82),
                    BookLine("heavier tackle. We fish in the", 83),
                    BookLine("lake nearby. Take the rod", 84),
                    BookLine("from under my bed in my", 85),
                    BookLine("dwelling and fish in the lake.", 86),
                    BookLine("When you have caught a few", 87),
                    BookLine("fish I am sure you will be", 88),
                    BookLine("ready to talk more with me.", 89),
                    BookLine("You will know when you are", 90),
                    BookLine("ready, since my inspiration will fill", 91),
                    BookLine("your mind. We do not use", 92),
                    BookLine("these fish quite as you might", 93),
                    BookLine("expect. When you return", 94),
                    BookLine("from Fishing we can speak", 95),
                    BookLine("more of this matter.'", 96),
                ),
            ),
        )

        private val FISHING_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("I have noted down what Otto", 97),
                    BookLine("has told me in this journal, so", 68),
                    BookLine("that I may not forget my", 69),
                    BookLine("tasks. His instructions are", 70),
                    BookLine("thus faithfully recorded for", 71),
                    BookLine("posterity. ", 72),
                    BookLine(BLUE + "Otto's words on Fishing with", 74),
                    BookLine(BLUE + "rods.", 75),
                    BookLine("'While you civilised folk use", 77),
                    BookLine("small, weak fishing rods, we", 78),
                ),
                Page(
                    BookLine("barbarians are skilled with", 82),
                    BookLine("heavier tackle. We fish in the", 83),
                    BookLine("lake nearby. Take the rod", 84),
                    BookLine("from under my bed in my", 85),
                    BookLine("dwelling and fish in the lake.", 86),
                    BookLine("When you have caught a few", 87),
                    BookLine("fish I am sure you will be", 88),
                    BookLine("ready to talk more with me.", 89),
                    BookLine("You will know when you are", 90),
                    BookLine("ready, since my inspiration will fill", 91),
                    BookLine("your mind. We do not use", 92),
                    BookLine("these fish quite as you might", 93),
                    BookLine("expect. When you return", 94),
                    BookLine("from Fishing we can speak", 95),
                    BookLine("more of this matter.'", 96),
                ),
            ),
            PageSet(
                Page(
                    BookLine("'Your mind is as clear as the", 97),
                    BookLine("waters you have fished. This", 68),
                    BookLine("is good. These are fish", 69),
                    BookLine("are fat with eggs rather than", 70),
                    BookLine("fat with flesh; this is what we", 71),
                    BookLine("will make use of.'", 72),
                ),
            ),
        )

        private val BAREHAND_BASICS = arrayOf(
            PageSet(
                Page(
                    BookLine(BLUE + "Otto's words on Fishing", 97),
                    BookLine(BLUE + "without harpoons", 68),
                    BookLine("'First you must know more", 69),
                    BookLine("of harpoons through special", 70),
                    BookLine("study of fish that are usually", 71),
                    BookLine("caught with such a device.", 72),
                    BookLine("You must catch fish which", 73),
                    BookLine("are usually harpooned,", 74),
                    BookLine("without a harpoon. You will", 75),
                    BookLine("be using your skill and", 76),
                    BookLine("strength. Use your arm as", 77),
                    BookLine("bait. Wriggle you fingers as", 78),
                    BookLine("if they are a tasty snack and", 79),
                ),
                Page(
                    BookLine("hungry tuna and swordfish", 82),
                    BookLine("will throng to be caught by", 83),
                    BookLine("you. The method also works", 84),
                    BookLine("with shark - though in this", 85),
                    BookLine("case the action must be more", 86),
                    BookLine("of a frenzied thrashing of the", 87),
                    BookLine("arm than a wriggle.'", 88),
                ),
            ),
        )

        private val BAREHAND_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("'I sense you have more", 89),
                    BookLine("understanding of spears", 90),
                    BookLine("through your studies.'", 91),
                ),
            ),
        )

        private val FM_BOW_BASICS = arrayOf(
            PageSet(
                Page(
                    BookLine("I have noted down what Otto", 97),
                    BookLine("has told me in this journal, so", 68),
                    BookLine("that I may not forget my", 69),
                    BookLine("tasks. His instructions are", 70),
                    BookLine("thus faithfully recorded for", 71),
                    BookLine("posterity.", 72),
                    BookLine(BLUE + "Otto's words on Firemaking", 74),
                    BookLine("'The first point in your", 76),
                    BookLine("progression is that of lighting", 77),
                    BookLine("fires without a tinderbox. For", 78),
                ),
                Page(
                    BookLine("this process you will require", 82),
                    BookLine("a strung bow. You use the", 83),
                    BookLine("bow to quickly rotate pieces", 84),
                    BookLine("of wood against one another.", 85),
                    BookLine("As you rub the wood", 86),
                    BookLine("becomes hot, eventually", 87),
                    BookLine("springing into flame. The", 88),
                    BookLine("spirits will aid you, the power", 89),
                    BookLine("they supply you will guide your", 90),
                    BookLine("hands. Go and benefit from", 91),
                    BookLine("their guidance upon an oaken", 92),
                    BookLine("log.'", 93),
                ),
            ),
        )

        private val FM_BOW_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("I have noted down what Otto", 97),
                    BookLine("has told me in this journal, so", 68),
                    BookLine("that I may not forget my", 69),
                    BookLine("tasks. His instructions are", 70),
                    BookLine("thus faithfully recorded for", 71),
                    BookLine("posterity.", 72),
                    BookLine(BLUE + "Otto's words on Firemaking", 74),
                    BookLine("'The first point in your", 76),
                    BookLine("progression is that of lighting", 77),
                    BookLine("fires without a tinderbox. For", 78),
                ),
                Page(
                    BookLine("this process you will require", 82),
                    BookLine("a strung bow. You use the", 83),
                    BookLine("bow to quickly rotate pieces", 84),
                    BookLine("of wood against one another.", 85),
                    BookLine("As you rub the wood", 86),
                    BookLine("becomes hot, eventually", 87),
                    BookLine("springing into flame. The", 88),
                    BookLine("spirits will aid you, the power", 89),
                    BookLine("they supply you will guide your", 90),
                    BookLine("hands. Go and benefit from", 91),
                    BookLine("their guidance upon an oaken", 92),
                    BookLine("log.'", 93),
                ),
            ),
            PageSet(
                Page(
                    BookLine("'Firemaking with your bow", 97),
                    BookLine("worked - fine news indeed,", 68),
                    BookLine("secrets of our spirit boats now", 69),
                    BookLine("await your attention.'", 70),
                ),
            ),
        )

        private val FISHING_FM_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("I have noted down what Otto", 97),
                    BookLine("has told me in this journal, so", 68),
                    BookLine("that I may not forget my", 69),
                    BookLine("tasks. His instructions are", 70),
                    BookLine("thus faithfully recorded for", 71),
                    BookLine("posterity.", 72),
                    BookLine(BLUE + "Otto's words on Fishing with", 74),
                    BookLine(BLUE + "rods.", 75),
                    BookLine("'While you civilised folk use", 77),
                    BookLine("small, weak fishing rods, we", 78),
                ),
                Page(
                    BookLine("barbarians are skilled with", 82),
                    BookLine("heavier tackle. We fish in the", 83),
                    BookLine("lake nearby. Take the rod", 84),
                    BookLine("from under my bed in my", 85),
                    BookLine("dwelling and fish in the lake.", 86),
                    BookLine("When you have caught a few", 87),
                    BookLine("fish I am sure you will be", 88),
                    BookLine("ready to talk more with me.", 89),
                    BookLine("You will know when you are", 90),
                    BookLine("ready, since my inspiration will", 91),
                    BookLine("fill your mind. We do not use", 92),
                    BookLine("these fish quite as you might", 93),
                    BookLine("expect. When you return", 94),
                    BookLine("from Fishing we can speak", 95),
                    BookLine("more of this matter.'", 96),
                ),
            ),
            PageSet(
                Page(
                    BookLine("'Your mind is as clear as the", 97),
                    BookLine("waters you have fished. This", 68),
                    BookLine("is good. These are fish", 69),
                    BookLine("are fat with eggs rather than", 70),
                    BookLine("fat with flesh; this is what we", 71),
                    BookLine("will make use of.'", 72),
                    BookLine(BLUE + "Otto's words on Firemaking", 74),
                    BookLine("'The first point in your", 76),
                    BookLine("progression is that of lighting", 77),
                    BookLine("fires without a tinderbox. For", 78),
                ),
                Page(
                    BookLine("this process you will require", 82),
                    BookLine("a strung bow. You use the", 83),
                    BookLine("bow to quickly rotate pieces", 84),
                    BookLine("of wood against one another.", 85),
                    BookLine("As you rub the wood", 86),
                    BookLine("becomes hot, eventually", 87),
                    BookLine("springing into flame. The", 88),
                    BookLine("spirits will aid you, the power", 89),
                    BookLine("they supply you will guide your", 90),
                    BookLine("hands. Go and benefit from", 91),
                    BookLine("their guidance upon an oaken", 92),
                    BookLine("log.'", 93),
                ),
            ),
            PageSet(
                Page(
                    BookLine("'Firemaking with your bow", 97),
                    BookLine("worked - fine news indeed,", 68),
                    BookLine("secrets of our spirit boats now", 69),
                    BookLine("await your attention.'", 70),
                ),
            ),
        )

        private val PYRESHIP_BASICS = arrayOf(
            PageSet(
                Page(
                    BookLine(BLUE + "Otto's words on Crafting", 97),
                    BookLine(BLUE + "pyre ships", 68),
                    BookLine("'The next step is quite", 69),
                    BookLine("complex, so listen well. In", 70),
                    BookLine("order to send our ancestors", 71),
                    BookLine("into the spirit world, their", 72),
                    BookLine("mortal remains must be", 73),
                    BookLine("burned with due ceremony.", 74),
                    BookLine("This can only be performed", 75),
                    BookLine("close to the water on the", 76),
                    BookLine("shore of the lake, just to our", 77),
                    BookLine("north-east. You will recognise", 78),
                    BookLine("the correct places by the", 79),
                ),
                Page(
                    BookLine("ashes to be seen there. You", 82),
                    BookLine("will need to construct a small", 83),
                    BookLine("ship by using an axe upon", 84),
                    BookLine("logs in this area, the add the", 85),
                    BookLine("bones of a long dead", 86),
                    BookLine("barbarian hero. From the", 87),
                    BookLine("caverns beneath this lake.", 88),
                    BookLine("Many of our ancestors", 89),
                    BookLine("travelled to these caverns in", 90),
                    BookLine("order to hunt for glory and", 91),
                    BookLine("found only death. Their", 92),
                    BookLine("bones must still lie inside,", 93),
                    BookLine("their spirits trapped in", 94),
                    BookLine("torment. The spirit will", 95),
                    BookLine("ascend to glory, the pyre will", 96),
                ),
            ),
            PageSet(
                Page(
                    BookLine("send the earthly remains to", 97),
                    BookLine("the depths. You will also", 68),
                    BookLine("obtain a closer link to the", 69),
                    BookLine("spirit world. During this", 70),
                    BookLine("heightened contact, any bones", 71),
                    BookLine("you bury will have increased", 72),
                    BookLine("importance to the gods. The", 73),
                    BookLine("number of bones that may be", 74),
                    BookLine("buried, before the link fades,", 75),
                    BookLine("is increased with the difficulty", 76),
                    BookLine("of obtaining the wood which", 77),
                    BookLine("you use. I have little", 78),
                    BookLine("knowledge of the caverns,", 79),
                    BookLine("they are blocked from the", 80),
                    BookLine("sight of spirits with whom", 81),
                ),
                Page(
                    BookLine("I commune. I can only", 82),
                    BookLine("suspect that whatever slew", 83),
                    BookLine("barbarians heroes is indeed", 84),
                    BookLine("mighty. I also suggest", 85),
                    BookLine("that these bones might well be", 86),
                    BookLine("very uncommon, since heroes", 87),
                    BookLine("are not found in vast", 88),
                    BookLine("numbers. Good luck. Dive", 89),
                    BookLine("into the whirlpool in the lake", 90),
                    BookLine("to the east. The spirits will", 91),
                    BookLine("use their abilities to ensure", 92),
                    BookLine("you arrive in the correct", 93),
                    BookLine("location, though their", 94),
                    BookLine("influence fades so you must", 95),
                    BookLine("find your own way out.'", 96),
                ),
            ),
        )
        private val PYRESHIP_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("The spirits herald your", 97),
                    BookLine("presence with a spectral", 68),
                    BookLine("fanfare. On this great", 69),
                    BookLine("day you have my thanks,", 70),
                    BookLine("eternally. May you find", 71),
                    BookLine("riches while rescuing my", 72),
                    BookLine("spiritual ancestors in", 73),
                    BookLine("the caverns for many moons", 74),
                    BookLine("to come.", 75),
                ),
            ),
        )

        private val HERBLORE_BASICS = arrayOf(
            PageSet(
                Page(
                    BookLine(BLUE + "Otto's words on potion", 97),
                    BookLine(BLUE + "enhancement", 68),
                    BookLine("'If you use a knife upon a", 69),
                    BookLine("fat fish, several new fishy", 70),
                    BookLine("items will be produced. Fish", 71),
                    BookLine("parts can be used as bait; the", 72),
                    BookLine("roe or caviar is more useful", 73),
                    BookLine("for us. Mixing these items", 74),
                    BookLine("with two dose potions is what", 75),
                    BookLine("should be performed. This", 76),
                    BookLine("results in nutritious slop,", 77),
                    BookLine("perfect for healing as well as", 78),
                    BookLine("imparting the effect of the", 79),
                ),
                Page(
                    BookLine("potion. Roe can only be used", 82),
                    BookLine("for some of the more easily", 83),
                    BookLine("combined mixes, while caviar", 84),
                    BookLine("may be used for any of the", 85),
                    BookLine("mixes of which I am aware.", 86),
                    BookLine("You will discover that you", 87),
                    BookLine("are be able to decant a four", 88),
                    BookLine("dose potion into an empty", 89),
                    BookLine("vial, thus giving two potions", 90),
                    BookLine("of two doses. This will aid", 91),
                    BookLine("you in the process. In this", 92),
                    BookLine("case I in fact require a", 93),
                    BookLine("potion, for my own stocks.", 94),
                    BookLine("Bring a lesser attack", 95),
                    BookLine("potion combined with fish roe.'", 96),
                ),
            ),
        )

        private val HERBLORE_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine(BLUE + "Otto's words on potion", 97),
                    BookLine(BLUE + "enhancement", 68),
                    BookLine("'If you use a knife upon a", 69),
                    BookLine("fat fish, several new fishy", 70),
                    BookLine("items will be produced. Fish", 71),
                    BookLine("parts can be used as bait; the", 72),
                    BookLine("roe or caviar is more useful", 73),
                    BookLine("for us. Mixing these items", 74),
                    BookLine("with two dose potions is what", 75),
                    BookLine("should be performed. This", 76),
                    BookLine("results in nutritious slop,", 77),
                    BookLine("perfect for healing as well as", 78),
                    BookLine("imparting the effect of the", 79),
                ),
                Page(
                    BookLine("potion. Roe can only be used", 82),
                    BookLine("for some of the more easily", 83),
                    BookLine("combined mixes, while caviar", 84),
                    BookLine("may be used for any of the", 85),
                    BookLine("mixes of which I am aware.", 86),
                    BookLine("You will discover that you", 87),
                    BookLine("are be able to decant a four", 88),
                    BookLine("dose potion into an empty", 89),
                    BookLine("vial, thus giving two potions", 90),
                    BookLine("of two doses. This will aid", 91),
                    BookLine("you in the process. In this", 92),
                    BookLine("case I in fact require a", 93),
                    BookLine("potion, for my own stocks.", 94),
                    BookLine("Bring a lesser attack", 95),
                    BookLine("potion combined with fish roe.'", 96),
                ),
            ),
            PageSet(
                Page(
                    BookLine("'I see you have my potion. I", 97),
                    BookLine("will say no more than that I", 68),
                    BookLine("am eternally grateful.'", 69),
                ),
            ),
        )

        private val FISHING_FM_HERBLORE_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("I have noted down what Otto", 97),
                    BookLine("has told me in this journal, so", 68),
                    BookLine("that I may not forget my", 69),
                    BookLine("tasks. His instructions are", 70),
                    BookLine("thus faithfully recorded for", 71),
                    BookLine("posterity.", 72),
                    BookLine(BLUE + "Otto's words on Fishing with", 74),
                    BookLine(BLUE + "rods.", 75),
                    BookLine("'While you civilised folk use", 77),
                    BookLine("small, weak fishing rods, we", 78),
                ),
                Page(
                    BookLine("barbarians are skilled with", 82),
                    BookLine("heavier tackle. We fish in the", 83),
                    BookLine("lake nearby. Take the rod", 84),
                    BookLine("from under my bed in my", 85),
                    BookLine("dwelling and fish in the lake.", 86),
                    BookLine("When you have caught a few", 87),
                    BookLine("fish I am sure you will be", 88),
                    BookLine("ready to talk more with me.", 89),
                    BookLine("You will know when you are", 90),
                    BookLine("ready, since my inspiration will fill", 91),
                    BookLine("your mind. We do not use", 92),
                    BookLine("these fish quite as you might", 93),
                    BookLine("expect. When you return", 94),
                    BookLine("from Fishing we can speak", 95),
                    BookLine("more of this matter.'", 96),
                ),
            ),
            PageSet(
                Page(
                    BookLine("'Your mind is as clear as the", 97),
                    BookLine("waters you have fished. This", 68),
                    BookLine("is good. These are fish", 69),
                    BookLine("are fat with eggs rather than", 70),
                    BookLine("fat with flesh; this is what we", 71),
                    BookLine("will make use of.'", 72),
                ),
                Page(
                    BookLine(BLUE + "Otto's words on Firemaking", 82),
                    BookLine("'The first point in your", 83),
                    BookLine("progression is that of lighting", 84),
                    BookLine("fires without a tinderbox. For", 85),
                ),
            ),
            PageSet(
                Page(
                    BookLine("this process you will require", 97),
                    BookLine("a strung bow. You use the", 68),
                    BookLine("bow to quickly rotate pieces", 69),
                    BookLine("of wood against one another.", 70),
                    BookLine("As you rub the wood", 71),
                    BookLine("becomes hot, eventually", 72),
                    BookLine("springing into flame. The", 73),
                    BookLine("spirits will aid you, the power", 74),
                    BookLine("they supply you will guide your", 75),
                    BookLine("hands. Go and benefit from", 76),
                    BookLine("their guidance upon an oaken", 77),
                    BookLine("log.'", 78),
                ),
                Page(
                    BookLine("'Firemaking with your bow", 82),
                    BookLine("worked - fine news indeed,", 83),
                    BookLine("secrets of our spirit boats now", 84),
                    BookLine("await your attention.'", 85),
                ),
            ),
            PageSet(
                Page(
                    BookLine(BLUE + "Otto's words on potion", 97),
                    BookLine(BLUE + "enhancement", 68),
                    BookLine("'If you use a knife upon a", 69),
                    BookLine("fat fish, several new fishy", 70),
                    BookLine("items will be produced. Fish", 71),
                    BookLine("parts can be used as bait; the", 72),
                    BookLine("roe or caviar is more useful", 73),
                    BookLine("for us. Mixing these items", 74),
                    BookLine("with two dose potions is what", 75),
                    BookLine("should be performed. This", 76),
                    BookLine("results in nutritious slop,", 77),
                    BookLine("perfect for healing as well as", 78),
                    BookLine("imparting the effect of the", 79),
                ),
                Page(
                    BookLine("potion. Roe can only be used", 82),
                    BookLine("for some of the more easily", 83),
                    BookLine("combined mixes, while caviar", 84),
                    BookLine("may be used for any of the", 85),
                    BookLine("mixes of which I am aware.", 86),
                    BookLine("You will discover that you", 87),
                    BookLine("are be able to decant a four", 88),
                    BookLine("dose potion into an empty", 89),
                    BookLine("vial, thus giving two potions", 90),
                    BookLine("of two doses. This will aid", 91),
                    BookLine("you in the process. In this", 92),
                    BookLine("case I in fact require a", 93),
                    BookLine("potion, for my own stocks.", 94),
                    BookLine("Bring a lesser attack", 95),
                    BookLine("potion combined with fish roe.'", 96),
                ),
            ),
            PageSet(
                Page(
                    BookLine("'I see you have my potion. I", 97),
                    BookLine("will say no more than that I", 68),
                    BookLine("am eternally grateful.'", 69),
                ),
            ),
        )

        private val SMITHING_SPEAR_BASICS_NO_QUEST = arrayOf(
            PageSet(
                Page(
                    BookLine(BLUE + "Otto's words on Smithing", 97),
                    BookLine(BLUE + "spears", 68),
                    BookLine("'The next step is to", 69),
                    BookLine("manufacture a spear, suitable", 70),
                    BookLine("for combat. Our distant", 71),
                    BookLine("cousins on Karamja are in", 72),
                    BookLine("need of help, however, and", 73),
                    BookLine("you must aid them before I", 74),
                    BookLine("can aid you. You must go", 75),
                    BookLine("now and complete Tai", 76),
                    BookLine("Bwo Wannai Trio quest.'", 77),
                ),
            ),
        )
        private val SMITHING_SPEAR_BASICS = arrayOf(
            PageSet(
                Page(
                    BookLine(BLUE + "Since I have completed this", 97),
                    BookLine(BLUE + "quests, he adds", 68),
                    BookLine("'Many warriors complain that", 62),
                    BookLine("spears are difficult to find,", 63),
                    BookLine("we barbarians thus forge our", 64),
                    BookLine("own. If you use our special", 65),
                    BookLine("barbarian anvil here, you will", 66),
                    BookLine("find it ideal. Other forges are", 68),
                    BookLine("not sturdy enough or shaped", 69),
                    BookLine("appropriately for the forging", 70),
                    BookLine("work involved. Make any of", 70),
                    BookLine("our spears and return.", 72),
                    BookLine("Note well that you will", 73),
                    BookLine("require wood for the spear", 74),
                    BookLine("shafts, the quality of the wood", 75),
                    BookLine("must be similar to that of the", 76),
                ),
                Page(
                    BookLine("metal involved.'", 97),
                ),
            ),
        )
        private val SMITHING_SPEAR_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("'The manufacture of spears is", 97),
                    BookLine("now yours as a speciality.", 68),
                    BookLine("Use your skill well. In", 69),
                    BookLine("addition, I am ready to", 70),
                    BookLine("reveal more spear-related", 71),
                    BookLine("crafts.'", 72),
                ),
            ),
        )

        private val SMITHING_HASTAE_BASICS = arrayOf(
            PageSet(
                Page(
                    BookLine(BLUE + "Otto's words on one-handed", 97),
                    BookLine(BLUE + "spears", 68),
                    BookLine("'The next step is to", 69),
                    BookLine("manufacture a one handed", 70),
                    BookLine("version of a spear, suitable", 71),
                    BookLine("for combat. Such a spear is", 72),
                    BookLine("known to us as a hasta. As", 73),
                    BookLine("you might suspect, our ways", 74),
                    BookLine("require greater", 75),
                    BookLine("understanding than is gained", 76),
                    BookLine("by simply looking at a", 77),
                    BookLine("weapon.", 78),
                    BookLine("It is also the case that the", 79),
                ),
                Page(
                    BookLine("the process involves a differently", 82),
                    BookLine("balanced spear.", 83),
                    BookLine("Before you may use such a", 84),
                    BookLine("weapon in anger, you must", 85),
                    BookLine("make an example. Only then", 86),
                    BookLine("will you fully understand the", 87),
                    BookLine("poise and techniques involved.", 88),
                    BookLine("You may use our special", 89),
                    BookLine("anvil for this spear type too.", 90),
                    BookLine("As the ways of black and", 91),
                    BookLine("and dragon are beyond", 92),
                    BookLine("our knowledge, however, these", 93),
                    BookLine("spears may not be created.'", 94),
                ),
            ),
        )

        private val SMITHING_HASTAE_COMPLETE = arrayOf(
            PageSet(
                Page(
                    BookLine("I see you have constructed", 97),
                    BookLine("your hasta, and are", 68),
                    BookLine("approaching readiness to live", 69),
                    BookLine("life to its fullest - that you", 70),
                    BookLine("may be a peaceful spirit when", 71),
                    BookLine("your time ends.'", 72),
                ),
            ),
        )
    }

    private fun displayGuide(player: Player, guideType: GuideType) {
        BookInterface.pageSetup(player, BookInterface.FANCY_BOOK_26, TITLE, guideType.content, false)
    }

    override fun defineListeners() {
        on(Items.BARBARIAN_SKILLS_11340, IntType.ITEM, "read") { player, _ ->
            val barbarianAttributes = mapOf(
                BarbarianTraining.FISHING_BASE to GuideType.FISH_BASE,
                BarbarianTraining.FISHING_FULL to GuideType.FISH_FULL,
                BarbarianTraining.FM_BASE to GuideType.FM_BASE,
                BarbarianTraining.FM_FULL to GuideType.FM_FULL,
                BarbarianTraining.PYRESHIP_BASE to GuideType.PS_BASE,
                BarbarianTraining.PYRESHIP_FULL to GuideType.PS_FULL,
                BarbarianTraining.HERBLORE_BASE to GuideType.H_BASE,
                BarbarianTraining.HERBLORE_FULL to GuideType.H_FULL,
                BarbarianTraining.SPEAR_BASE to GuideType.SMITH_N_REQS,
                BarbarianTraining.SPEAR_FULL to GuideType.SMITH_S_FULL,
                BarbarianTraining.HASTA_FULL to GuideType.SMITH_H_FULL,
            )

            val attributes = barbarianAttributes.entries.firstOrNull { getAttribute(player, it.key, false) }
                ?.let { it.key to it.value }

            val guideType = when {
                attributes == null -> GuideType.DEFAULT
                attributes.first == BarbarianTraining.FISHING_FULL && getAttribute(
                    player,
                    BarbarianTraining.FM_FULL,
                    false,
                ) -> GuideType.FISH_FM_FULL

                attributes.first == BarbarianTraining.FM_FULL && getAttribute(
                    player,
                    BarbarianTraining.PYRESHIP_BASE,
                    false,
                ) -> GuideType.PS_FULL

                attributes.first == BarbarianTraining.HERBLORE_FULL && getAttribute(
                    player, BarbarianTraining.FISHING_FULL, false
                ) && getAttribute(
                    player, BarbarianTraining.FM_FULL, false
                ) -> GuideType.FISH_FM_H_FULL

                attributes.first == BarbarianTraining.SPEAR_BASE && getAttribute(
                    player,
                    BarbarianTraining.FISHING_FULL,
                    false,
                ) -> GuideType.SMITH_M_REQS

                attributes.first == BarbarianTraining.SPEAR_FULL && getAttribute(
                    player,
                    BarbarianTraining.HASTA_BASE,
                    false,
                ) -> GuideType.SMITH_H_BASE

                else -> attributes.second
            }

            displayGuide(player, guideType)
            return@on true
        }
    }

    private enum class GuideType(val content: Array<PageSet>) {
        FISH_BASE(FISHING_BASICS),
        FISH_FULL(FISHING_COMPLETE),
        BARE_H_BASE(BAREHAND_BASICS),
        BARE_H_FULL(BAREHAND_COMPLETE),
        FM_BASE(FM_BOW_BASICS),
        FM_FULL(FM_BOW_COMPLETE),
        PS_BASE(PYRESHIP_BASICS),
        PS_FULL(PYRESHIP_COMPLETE),
        H_BASE(HERBLORE_BASICS),
        H_FULL(HERBLORE_COMPLETE),
        FISH_FM_FULL(FISHING_FM_COMPLETE),
        FISH_FM_H_FULL(FISHING_FM_HERBLORE_COMPLETE),
        SMITH_N_REQS(SMITHING_SPEAR_BASICS_NO_QUEST),
        SMITH_M_REQS(SMITHING_SPEAR_BASICS),
        SMITH_S_FULL(SMITHING_SPEAR_COMPLETE),
        SMITH_H_BASE(SMITHING_HASTAE_BASICS),
        SMITH_H_FULL(SMITHING_HASTAE_COMPLETE),
        DEFAULT(BARB_TRAINING_BASIC_PAGE)
    }
}
