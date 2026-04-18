package com.lawrence.justsitting

import androidx.compose.ui.graphics.Color

data class Teaching(
    val title: String,
    val subtitle: String,
    val content: String
)

data class WisdomSection(
    val title: String,
    val description: String,
    val color: Color,
    val teachings: List<Teaching>,
    val author: String
)

object WisdomRepository {
    val sections = listOf(
        // 1. SHAMATHA
        WisdomSection(
            title = "Shamatha",
            description = "Calming the mind",
            color = Color(0xFF00BCD4),
            author = "Dzongsar Jamyang Khyentse Rinpoche",
            teachings = listOf(
                Teaching(
                    title = "The Simple Technique",
                    subtitle = "THE METHOD",
                    content = "The actual technique is very simple. All the great meditators of the past advised us to sit up straight when we meditate. When we sit up straight, there is a sense of alertness, a sense of importance—it produces the right atmosphere.\n\n" +
                            "In this particular instruction, I’m going to suggest we don’t use an external object, such as a flower, but instead follow the standard Theravada tradition of using our breath as the object. So we concentrate on our breathing: we simply follow our breath in and out. That’s it. Our mind is focused on the breathing, our posture is straight, our eyes are open. That’s the essential technique: basically doing nothing."
                ),

                Teaching(
                    title = "The Art of Coming Back",
                    subtitle = "INSTRUCTIONS",
                    content = "We simply sit straight and we watch our breathing. We are not concerned with distractions, with all the thoughts that occupy our mind. We just sit—alone, by ourselves, no reference at all. Us, the breathing, and the concentration. That’s all we have.\n\n" +
                            "Then some thoughts may come, and any number of distractions: things you talked about yesterday, movies you watched last week, a conversation you just had, things you need to do tomorrow, a sudden panic—did I switch off the gas in the kitchen this morning? All of this will come, and when it does, go back to the breathing. This is the slogan of shamatha instruction: just come back."
                ),

                Teaching(
                    title = "Letting Go of Ambition",
                    subtitle = "ATTITUDE",
                    content = "If we have ambitions—even if our aim is enlightenment— then there is no meditation, because we are thinking about it, craving it, fantasizing, imagining things. That is not meditation.\n\n" +
                            "This is why an important characteristic of shamatha meditation is to let go of any goal and simply sit for the sake of sitting. We breathe in and out, and we just watch that. Nothing else. It doesn’t matter if we get enlightenment or not. It doesn’t matter if our friends get enlightened faster. Who cares? We are just breathing. We let go of our ambitions. This includes trying to do a perfect shamatha meditation. We should get rid of even that. Just sit."
                ),

                Teaching(
                    title = "Things Don't Matter",
                    subtitle = "PERSPECTIVE",
                    content = "The beautiful thing about having less obsessions and ambitions—and just sitting straight and watching the breathing—is that nothing will disturb us. Things only disturb us when we have an aim. When we have an aim, we become obsessed.\n\n" +
                            "For a few moments, things don’t matter. It doesn’t matter whether the hot water is boiling in the kettle, it doesn’t matter whether the telephone is ringing, and it doesn’t matter whether it’s one of our friends. For a few moments, things don’t matter."
                ),

                Teaching(
                    title = "Control and Naturalness",
                    subtitle = "BENEFITS",
                    content = "If you are not interested in enlightenment, you can practice shamatha to be natural—to not be so swayed by circumstances. Most of the time we are not in control of ourselves; our mind is always attracted to, or distracted by, something—our enemies, our lovers, our friends, hope, fear, jealousy, pride, attachment, aggression.\n\n" +
                            "In other words, all these phenomena control our mind. Letting go is quite important if you want to become a shamatha practitioner. We do shamatha meditation so we can achieve this power to let go."
                ),

                Teaching(
                    title = "The Snake Uncoiling",
                    subtitle = "THE PROCESS",
                    content = "Meditation is one of the rare occasions when we’re not doing anything. Otherwise, we’re always doing something, we’re always thinking something, we’re always occupied. We get lost in millions of obsessions or fixations.\n\n" +
                            "But by meditating—by not doing anything—all these fixations are revealed. Beginners might find this a little frightening, but slowly they will gain inner confidence, and these fixations will automatically lessen. The classical meditation instruction texts say our obsessions will undo themselves like a snake uncoiling itself."
                ),

                Teaching(
                    title = "Thoughts are Not Your Job",
                    subtitle = "THE MIND",
                    content = "Thoughts are coming and I’m telling you to go back to the breathing. You automatically interpret this as “We should stop the thoughts.” This is not what I mean. I’m not saying you should stop thinking.\n\n" +
                            "All I’m saying is, concentrate on the breathing. When thoughts come, don’t stop them, don’t increase them, don’t encourage them, don’t discourage them. Your job is to concentrate on the breathing. That’s it. Stopping the thoughts is not your job."
                ),

                Teaching(
                    title = "Short, Frequent, Spontaneous",
                    subtitle = "PRACTICE TIPS",
                    content = "Always do short but frequent shamatha sessions. If you’re going to meditate for fifteen minutes, start fresh at least thirty times. Over time we can start doing longer sessions. Keeping it short is important because if you do too much at the beginning, you’ll get fed up with the technique. We are human beings—we don’t like to get bored.\n\n" +
                            "Practice time is always now—it’s never in the future. Don’t ever leave your shamatha thinking, 'I’m going to do it next weekend'. Do it now. Anyway, you’re only doing it for about forty-five seconds if you’re a beginner. It’s easy."
                ),

                Teaching(
                    title = "Having Fun and Discipline",
                    subtitle = "DISCIPLINE",
                    content = "The whole purpose of Buddhism is to have fun, isn’t it? And in order to have fun you have to have control. If someone else has control over you, that’s it: there’s no fun.\n\n" +
                            "Lamas often advise us to do meditation in a group. It’s like working out—if you buy the machines and bring them home, the machines end up in the garage. But if you go to a gym, you see the other people who are diligently doing it, and it gives you inspiration. Keep it simple. Every day, do a few minutes, and, on top of that, do it spontaneously in different places—not just in front of the shrine, but everywhere."
                )
            )
        ),

        // 2. VIPASHYANA
        WisdomSection(
            title = "Vipashyana",
            description = "Insight into the nature of reality.",
            color = Color(0xFF009688),
            author = "Traditional Analytical Meditation",
            teachings = listOf(
                Teaching(
                    title = "Beyond Tranquility",
                    subtitle = "THE PURPOSE",
                    content = "Shamatha is like a still, clear pond. But merely staying still does not liberate us from ignorance. Vipashyana is the 'special insight' that uses this stillness to cut the root of confusion.\n\n" +
                            "We are not seeking to become a 'spiritual vegetable' without thoughts; instead, we use the mind's stability to analyze the nature of the thinker. Without Vipashyana, meditation remains a relaxation exercise; with it, it becomes a sword that severs illusion."
                ),
                Teaching(
                    title = "Deconstructing the Self",
                    subtitle = "THE FIVE AGGREGATES",
                    content = "Tradition teaches that what we call 'I' is merely a label placed upon five heaps of phenomena (Skandhas): form, feeling, perception, mental formations, and consciousness.\n\n" +
                            "In practice, when a strong emotion arises, do not call it 'mine.' Observe how the physical sensation is distinct from the mental image and the label of 'anger' or 'fear.' Look for the owner of these states. You will find a process, but no 'processor.' There is suffering, but no solid 'sufferer.'"
                ),
                Teaching(
                    title = "The Three Marks",
                    subtitle = "DIRECT INVESTIGATION",
                    content = "Authentic Vipashyana means recognizing three characteristics in every moment:\n\n" +
                            "1. Impermanence (Anicca): Everything that arises in the mind vanishes. Nothing lasts for even a second.\n" +
                            "2. Suffering (Dukkha): Everything that is composite and impermanent cannot provide ultimate satisfaction.\n" +
                            "3. Non-Self (Anatta): No phenomenon has a hard, independent core.\n\n" +
                            "By watching a thought rise and die, you realize there is nothing to cling to. This non-clinging is the gateway to freedom."
                ),
                Teaching(
                    title = "Looking at the Looker",
                    subtitle = "THE PITH INSTRUCTION",
                    content = "When the mind is quiet, turn the attention back upon itself. Who is observing the breath? Who hears the sound from outside?\n\n" +
                            "If you look directly at the 'observer,' you will see it has no shape, color, or location. It is a hollow yet cognizant presence. In that moment of 'not finding anything,' you are closest to the truth. Do not manufacture an intellectual answer; simply rest in the state of not finding a solid 'Self.'"
                ),
                Teaching(
                    title = "Meditation in Action",
                    subtitle = "INTEGRATION",
                    content = "Vipashyana does not end when you leave the cushion. True practice is seeing the illusory nature of things while walking, eating, or speaking. The world is like a mirage: it appears clearly, but lacks substance.\n\n" +
                            "When insulted, observe how the concept of 'me' contracts. When praised, observe how it expands. By watching these movements with detachment, as if watching a movie, you begin to live in the Ultimate Truth amidst the relative world."
                )
            )
        ),

        // 3. MADHYAMAKA
        WisdomSection(
            title = "Madhyamaka",
            description = "The Middle Way: Beyond Existence and Non-Existence.",
            color = Color(0xFFE91E63),
            author = "The Great Dialectic Tradition",
            teachings = listOf(
                Teaching(
                    title = "The Middle Way",
                    subtitle = "FREEDOM FROM EXTREMES",
                    content = "Madhyamaka is the razor-sharp practice of avoiding the two abysses: Eternalism (believing things have a permanent, independent soul) and Nihilism (believing that nothing matters or exists at all).\n\n" +
                            "We see a table and it seems solid. But through analysis, we find only parts, atoms, and space. The 'table' exists as a function, but is empty of a core. To dwell in this 'appearing yet empty' nature is the Middle Way—where we are neither fooled by appearances nor lost in nothingness."
                ),
                Teaching(
                    title = "Dependent Arising",
                    subtitle = "PRATITYASAMUTPADA",
                    content = "Nothing in the universe stands alone. A flower is a collection of non-flower elements: sunlight, soil, rain, and time. If you remove any of these, the flower vanishes.\n\n" +
                            "Similarly, your 'self' depends on a body, thoughts, and an environment. When you realize that everything is a web of causes and conditions, the walls of the ego dissolve. You are not a lonely island; you are a dynamic, empty flow. This realization is the death of grasping."
                ),
                Teaching(
                    title = "The Two Truths",
                    subtitle = "RELATIVE AND ULTIMATE",
                    content = "We navigate life through Relative Truth: we pay bills, we feel pain, and we follow rules. But we must never forget the Ultimate Truth: that none of these have a permanent, solid existence.\n\n" +
                            "The secret of Madhyamaka is to live fully in the relative world while knowing its ultimate emptiness. It's like watching a movie: you can be moved by the story, but you never forget it is just light playing on a screen. This brings a profound sense of humor and lightness to every tragedy."
                ),
                Teaching(
                    title = "The Four-Point Analysis",
                    subtitle = "DECONSTRUCTING PHENOMENA",
                    content = "To find the truth, we analyze any 'thing' using the four possibilities. A thing cannot arise from:\n" +
                            "1. Itself (it would already be there).\n" +
                            "2. Something else (there is no bridge between two truly separate things).\n" +
                            "3. Both (logical contradiction).\n" +
                            "4. Neither (things don't happen by magic).\n\n" +
                            "When the mind realizes that no 'thing' can be found under this scrutiny, it stops searching outside. It collapses into its own nature—unborn and unceasing."
                ),
                Teaching(
                    title = "Emptiness is not Nothingness",
                    subtitle = "SHUNYATA",
                    content = "A common mistake is thinking emptiness means the world is 'not there.' On the contrary, because everything is empty, everything is possible.\n\n" +
                            "Emptiness is the 'room' that allows the world to breathe and change. If things were not empty, they would be frozen forever, unable to grow or transform. Realizing emptiness is not a loss; it is the ultimate opening of the heart to the infinite potential of every moment."
                )
            )
        ),

        // 4. DZOGCHEN
        WisdomSection(
            title = "Dzogchen",
            description = "The Great Perfection: Resting in the Natural State.",
            color = Color(0xFFFF9800), // Tibetan Orange
            author = "Luminous Natural Wisdom",
            teachings = listOf(
                Teaching(
                    title = "The Sky of Awareness",
                    subtitle = "THE VIEW",
                    content = "Dzogchen is the realization that your mind is already perfect as it is. You don't need to change, fix, or improve anything.\n\n" +
                            "Imagine the sky: clouds pass through it, storms happen, but the sky itself remains untouched, vast, and pure. Your awareness is like that sky. The practice is not about creating a new state of mind, but simply resting in the spacious, clear, and open nature that has always been there."
                ),
                Teaching(
                    title = "Rigpa: Naked Awareness",
                    subtitle = "NON-DUAL RECOGNITION",
                    content = "Rigpa is the innermost nature of the mind—a state of pure intelligence that is empty, yet lucid and unobstructed.\n\n" +
                            "Usually, we get lost in the 'waves' of thoughts and emotions. In Dzogchen, we recognize the 'ocean' itself. This recognition is instantaneous and effortless. We stop looking at the objects of experience and instead recognize the 'looker.' This is coming home to your true identity."
                ),
                Teaching(
                    title = "The Art of Non-Doing",
                    subtitle = "THE MEDITATION",
                    content = "The greatest obstacle in Dzogchen is trying too hard. If you look for the mind with effort, you won't find it—it's like trying to see your own eyes without a mirror.\n\n" +
                            "Instead, just relax completely. Let thoughts arise and dissolve on their own, like drawings made on water. Don't follow them, but don't suppress them either. In this state of 'non-doing,' the natural brilliance of your mind reveals itself spontaneously."
                ),
                Teaching(
                    title = "Self-Liberation",
                    subtitle = "THE PROCESS",
                    content = "In other paths, you apply an antidote to negative emotions. In Dzogchen, you simply leave them alone. \n\n" +
                            "When a thought or emotion arises, if you don't grasp it or reject it, it will liberate itself. The classical metaphor is a snake uncoiling itself. There is no need for a 'meditator' to do anything; the energy of the thought simply dissolves back into the vast space of Rigpa."
                ),
                Teaching(
                    title = "The Everyday Majesty",
                    subtitle = "THE FRUIT",
                    content = "The result of Dzogchen is not a trance, but a profound ordinary presence. Everything you see, hear, or feel is experienced as the 'ornament' of your own awareness.\n\n" +
                            "There is no longer a distinction between 'sacred' and 'profane.' Every moment is a display of the Great Perfection. You live with a sense of 'carefree dignity,' knowing that nothing can ever stain or improve the primordial purity of your being."
                )
            )
        ),

        // 5. MAHAMUDRA
        WisdomSection(
            title = "Mahamudra",
            description = "The Great Seal",
            color = Color(0xFF673AB7),
            author = "Various Masters", // Autor generic pentru secțiune
            teachings = listOf(
                Teaching(
                    title = "ESSENTIAL ORAL INSTRUCTION",
                    subtitle = "HEADER",
                    content = ""
                ),
                Teaching(
                    title = "The Mind's Nature",
                    subtitle = "ESSENTIAL ORAL INSTRUCTION",
                    content = "Mahamudra is often called the 'Great Seal' because every experience is sealed with the nature of mind. \n\nMind is not a thing you can find, but it is not 'nothing'. It is like water: sometimes it is turbulent with waves (thoughts), and sometimes it is still. But whether turbulent or still, the essence is always water. In Mahamudra, we don't try to stop the waves; we simply recognize their wetness—the essence of awareness in every thought."
                ),
                Teaching(
                    title = "The Yoga of Non-Meditation",
                    subtitle = "ESSENTIAL ORAL INSTRUCTION",
                    content = "In Mahamudra, the highest form of meditation is 'non-meditation'. This doesn't mean being distracted, but rather being so natural that there is no 'meditator' trying to 'meditate'. \n\nIt is like a river that flows without effort. You don't need to push the water; it simply moves. When you stop trying to manufacture a special state of mind, the natural, unconditioned state of Mahamudra shines through. Just rest in the ordinary, present awareness."
                ),
                Teaching(
                    title = "Non-Dual View",
                    subtitle = "ESSENTIAL ORAL INSTRUCTION",
                    content = "We usually divide the world into 'subject' (me) and 'object' (the world). Mahamudra dissolves this boundary. \n\nWhen you hear a sound, there is just the 'hearing'. There isn't a 'hearer' separate from the 'sound'. By resting in this non-dual experience, we are freed from the struggle of liking or disliking, and we enter a state of profound peace where everything is seen as the play of our own mind."
                ),
                Teaching(
                    title = "THE FOUR DHARMAS OF GAMPOPA",
                    subtitle = "HEADER",
                    content = ""
                ),
                // --- GAMPOPA SELECTIONS ---
                Teaching(
                    title = "The Four Dharmas",
                    subtitle = "GAMPOPA",
                    content = "These four sentences comprise the complete path of all Buddhas:\n\n" +
                            "1. May the mind enter the Dharma.\n" +
                            "2. May the Dharma become the path.\n" +
                            "3. May the confusion of the path be dispelled.\n" +
                            "4. May confusion arise as wisdom."
                ),
                Teaching(
                    title = "The Mind Enters Dharma",
                    subtitle = "GAMPOPA",
                    content = "That Dharma becomes Dharma. This is accomplished through deep reflection on impermanence, both of the external world and the internal one. You must realize that at the moment of death you will leave everything behind—possessions, relationships, and attachments—stepping alone into the unknown. Then you will understand that nothing except Dharma has any real value. Until you cultivate the firm conviction that spiritual practice is the only thing that truly matters, Dharma will not become a real practice in your heart.\n\n" +
                            "Death has no fixed schedule; you cannot know whether it will take you tomorrow or next month. The only companion that your “conscious wisdom” will have after death is your own karma—the imprints of your positive or negative actions. You will not escape the consequences of what you have done, nor will you inherit what you have not built. In whatever corner of cyclic existence you might be born—whether the brutal suffering of the lower realms, the limitations of human life, or the agony of the gods who lose their privileges—everywhere you will find only dukkha. Your practice will not become authentic until you develop complete renunciation toward this world of suffering.\n\n" +
                            "In Mahamudra, these “Four Dharmas of Gampopa” represent the structure of the entire path. This first passage refers to the orientation of the mind.\n\n" +
                            "Dharma becoming Dharma: This is the preliminary but fundamental stage. Here Gampopa is not speaking about merely beginning the practice, but about making it authentic. He emphasizes that one may study philosophy, recite mantras, or sit in meditation, but if the motivation is still tied to the “eight worldly concerns” (the desire for gain, fame, praise, or pleasure in this life), then the action is not yet “Dharma.” It is only an intellectual or cultural activity. \n\n" +
                            "Impermanence as the gateway: In Mahamudra, we do not meditate on death in order to frighten ourselves, but to cut through the stream of attachment to deceptive appearances. Meditation on impermanence is not merely a philosophical contemplation but an existential shock. If the mind is occupied with long-term plans in this illusory world, it cannot rest in its “essence” or “entity” (Entity Mahamudra). In Mahamudra this is essential, because without this break the mind cannot relax into its own nature. Attachment sustains the construction of the ego.\n\n" +
                            "Self-knowing wisdom: What is present then is not a person, nor a mind that observes, but self-present clarity (rang rig), unsupported by any basis. When this clarity is not recognized as one’s own nature, it manifests as wandering and suffering, regardless of the form of existence in which it appears.\n\n" +
                            "Renunciation (Shenlok): This does not necessarily mean going to live in a cave. It means a “turning of interest” away from external objects toward the nature of the mind. Without this disenchantment with the cyclic nature of suffering (Samsara), the practitioner will never develop the intensity required to recognize the nature of mind directly.\n"
                ),
                Teaching(
                    title = "The Dharma become the path",
                    subtitle = "GAMPOPA",
                    content = "For your spiritual practice to transform into an authentic path: it is first necessary to cultivate relative Bodhicitta—that altruistic and compassionate attitude which values others more than oneself. Upon this foundation, you must add the realization that everything we experience (the external world and the inner mental states) arises only through interdependence and has no solid existence, being like an illusion. When compassion unites with the understanding of the illusory nature of reality, then simple practice truly becomes a path to liberation.\n\n" +
                            "This passage is crucial because it defines the “Path” as the union of Method and Wisdom. In Mahamudra, one cannot jump directly to meditation on the nature of mind without this foundation:\n\n" +
                            "1. Conventional Bodhicitta (Method): Gampopa emphasizes that selfishness is the greatest obstacle. “Valuing others more than oneself” is not merely a moral imperative, but a technical tool for “thinning out” the fixation on a Self (ego). Without this opening of the heart, meditation risks becoming cold, analytical, or even reinforcing spiritual pride.\n" +
                            "2. The Illusory Nature (Wisdom): Here the reference is to Pratītyasamutpāda (dependent origination). In Mahamudra, the “path” truly begins when you stop believing that thoughts, emotions, or physical objects are intrinsically real. They are like a rainbow: clearly appearing, yet impossible to grasp.\n" +
                            "3. Why “Primordial Dharma becomes the path”? In Gampopa’s terminology, if you only recognize that life is suffering (the first step discussed previously), you merely have “Dharma.” But when you begin to apply Compassion and Emptiness (the understanding of illusion) in every moment, that Dharma is set into motion. It becomes a vehicle—a path—that actively carries you beyond confusion.\n"
                ),
                Teaching(
                    title = "The confusion of the path be dispelled",
                    subtitle = "GAMPOPA",
                    content = "In Mahamudra, confusion (moha or avidya) is not something that must be forcefully destroyed, but something that must be “dispelled” through recognition, just as light dispels darkness. Here, Gampopa offers us a map of deconditioning:\n\n" +
                            "1. The deconstruction of mundane attachment: The first three points (impermanence, karma, and suffering) are the “Four Thoughts that Turn the Mind.” They pull the practitioner out of the hypnotic state of everyday life, in which we believe that happiness comes from external objects.\n" +
                            "2. Transcending spiritual selfishness: Mahamudra is a path of the “Great Vehicle” (Mahayana). Gampopa warns that even the spiritual path can become a form of confusion if it is centered only on personal peace. Compassion dispels the confusion of the “Lesser Vehicle” through the expansion of the heart.\n" +
                            "3. The non-conceptual ground: The final step is the closest to the essence of Mahamudra. The ultimate confusion is “conceptual grasping” (dzinpa). We perceive the world through labels: “good,” “bad,” “me,” “you,” “real.” Meditation on the dream-like nature of phenomena trains us to see appearances without attributing substance to them.\n" +
                            "4. From top to bottom: This expression suggests an exhaustive approach. You cannot understand the nature of mind (Mahamudra) if you are still confused about karma or completely attached to your possessions. Each level of practice prepares the ground for the next, until the mind remains empty of confusion yet full of clarity.\n"
                ),
                Teaching(
                    title = "Confusion arise as wisdom",
                    subtitle = "GAMPOPA",
                    content = "Ultimately, confusion itself must be allowed to manifest as wisdom. When, through the power of meditation, you realize that all phenomena are, in essence, beyond birth and death, any perception or thought that arises is directly recognized in its pure nature. At that moment, the process of confusion dissolves and is realized as the very flow of awakened wisdom.\n\n" +
                            "This is the culminating point where “Entity Mahamudra” (Mahamudra as entity/essence) is directly realized.\n\n" +
                            "1. Free from birth and cessation (Ajata): In Mahamudra, one discovers that phenomena (thoughts, sounds, emotions) do not “come” from anywhere and do not “go” anywhere. They appear, yet they have no solid basis. If you search for the origin of a thought, you do not find it (absence of birth); if you search for where it disappears, you find no place (absence of cessation).\n" +
                            "2. Resolving in its own essence (Rang sar): This is a key concept. It does not mean that we destroy confusion or replace it with something “better.” It means that when you look directly into the “face” of a confused thought or a disturbing emotion, without judging it, it liberates itself in its own fundamental nature (luminous emptiness). Confusion is nothing other than unrecognized wisdom; wisdom is confusion recognized.\n" +
                            "3. Whatever appears and whatever is known: Mahamudra does not require the stopping of thoughts. “Whatever appears” (the external world) and “whatever is known” (the inner mind) become the “fuel” for realization. The distinction between meditation and post-meditation disappears.\n" +
                            "4. The dawn of wisdom: Gampopa uses the metaphor of sunrise. Confusion is like darkness: it does not need to be “swept away” with a broom, but vanishes by itself when the light of recognizing the nature of mind appears.\n"
                ),
                Teaching(
                    title = "GAMPOPA ENTITY MAHAMUDRA",
                    subtitle = "HEADER",
                    content = ""
                ),
                Teaching(
                    title = "This Path has eight sections",
                    subtitle = "DIALOGUE",
                    content = "The Mahamudra path is structured in eight dimensions which, when integrated into daily practice, anchor you firmly in the state of awakening:\n\n" +
                            "1. Omniscience (Knowledge of all aspects): It arises when you stop separating the mind from its thoughts, recognizing both as the four bodies of Buddha.\n" +
                            "2. Knowledge of the Path: It is born from the certainty that absolutely every experience is, in itself, the path to liberation.\n" +
                            "3. Knowledge of the Ground: This is not a skill acquired through effort, but the recognition that a thought is, by its very nature, primordially pure.\n" +
                            "4. Full Manifestation: It appears when you understand that the source (cause), the practice (path), and the result (fruit) are a single indivisible “entity.”\n" +
                            "5. Realization of the Summit (the Peak): As wisdom sharpens, it refines itself naturally, without effort, reaching the pinnacle of clarity.\n" +
                            "6. Gradual Realization: This is the process of integrating everything step by step, without trying to eliminate or suppress any thought.\n" +
                            "7. Instantaneous Enlightenment: It is the lightning-like recognition, in any moment of presence, that all phenomena share the same value and essence (equality).\n" +
                            "8. Realization (Dharmakāya): Everything that exists is experienced as the infinite play of the enlightened mind, leading to ultimate perfection.\n\n" +
                            "Here Gampopa uses the structure of the famous text Abhisamayālaṅkāra (The Ornament of Realizations), but he “translates” it into the direct language of Mahamudra. The key point is non-duality.\n\n" +
                            "Thought as Ground and Path: Gampopa insists that one does not need to become “skilled” in the sense of manipulating the mind. The secret lies in recognizing the primordial nature of thought. If you see that a thought is already Dharmakāya, then there is nothing you need to “do” with it. This is the Knowledge of the Ground.\n\n" +
                            "Without abandoning thoughts: In many paths, thoughts are abandoned in order to reach tranquility. In Mahamudra, Gradual Realization means learning to “ride” the waves of thoughts without being knocked down by them.\n\n" +
                            "The equality of phenomena (Nyam-nyi): This is a radical experience. From the perspective of wisdom (prajñā), a thought of anger and a thought of love have the same “substance” (luminous emptiness). They are equal. When this is realized, instant enlightenment occurs because one no longer wastes time judging or choosing between them.\n\n" +
                            "Irreversibility: This is the stage at which the practitioner can no longer be deceived by appearances. Once you have seen the “mechanism” through which reality is projected by the mind, you can no longer believe in its solidity—just as you cannot be frightened by a paper tiger once you know what it is made of.\n\n" +
                            "The essence: Mahamudra transforms the entire complex architecture of Buddhist philosophy into a direct experience, where everything is recognized as the activity of Dharmakāya.\n"
                ),
                Teaching(
                    title = "Entity Mahamudra (śūnyatā)",
                    subtitle = "INSTRUCTION",
                    content = "I asked the Master: ‘How should we understand emptiness? Is it a state of extreme purity perceived by someone? Is it a concept upon which the mind reflects, telling itself “everything is empty”? Or is it that emptiness which transcends any description or mental construction?’\n\n" +
                            "He answered me: ‘The first two are merely projections of the mind. True emptiness is that which lies beyond any conceptual elaboration.\n\n" +
                            "Rigpa (aware presence) and emptiness (the absence of any solid nature) arise together, simultaneously. This union, this co-emergence, has always existed within us, from the primordial state. The moment you recognize this state is itself the moment of awakening\n\n" +
                            "Here Gampopa distinguishes between experienced emptiness and conceptualized emptiness.\n\n" +
                            "The trap of the experiencer: Many meditators reach a state of deep and “pure” calm and believe that this is emptiness. Gampopa says “no,” because there is still an “I” (an experiencer) enjoying that purity. It is still a form of duality.\n\n" +
                            "The trap of the thinker: This is philosophical emptiness. If you sit in meditation repeating to yourself, “everything is empty of substance,” you remain in the domain of the intellect. The thought “this is emptiness” is not emptiness—it is just another thought.\n\n" +
                            "Freedom from elaborations (Niṣprapañca / Trödral): This is the Entity itself. It refers to a state that cannot be described by “it is,” “it is not,” “both,” or “neither.” It is raw reality before the mind manages to place any label upon it.\n\n" +
                            "Co-emergence (Sahaja): This is the central concept in Mahamudra.\n\n" +
                            "Rigpa (clarity/luminosity) and emptiness (space/the absence of form) are not two things that come together; they are like fire and its heat.\n\n" +
                            "The mind is empty (it has no form, color, or location), yet at the same time it is aware (it perceives and knows).\n\n" +
                            "You cannot have one without the other. This unity is called the co-emergence of wisdom.\n\n" +
                            "The essence: Wisdom is not something you construct; it is the recognition that your mind has always been this perfect unity of space and luminosity, beyond all words.\n"
                )
            )
        ),

        // 6. GLOSSARY (EXTENDED)
        WisdomSection(
            title = "Glossary",
            description = "Comprehensive reference of the Path's terminology.",
            color = Color(0xFFB0BEC5), // Steel Grey
            author = "Traditional Definitions",
            teachings = listOf(
                Teaching(
                    title = "Shamatha",
                    subtitle = "CALM ABIDING",
                    content = "A meditation practice aimed at developing a stable, focused, and peaceful state of mind by resting attention on an object, such as the breath. It is the essential foundation for all higher insights."
                ),
                Teaching(
                    title = "Vipashyana",
                    subtitle = "INSIGHT",
                    content = "Often translated as 'insight' or 'clear seeing'. It is the practice of investigating the nature of reality and the mind, leading to an understanding of impermanence and emptiness."
                ),
                Teaching(
                    title = "Shunyata",
                    subtitle = "EMPTINESS",
                    content = "The realization that all phenomena are empty of a permanent, independent existence. It does not mean nothingness, but rather that things arise through interdependence."
                ),
                Teaching(
                    title = "Madhyamaka",
                    subtitle = "THE MIDDLE WAY",
                    content = "A tradition of Buddhist philosophy that avoids the extremes of Eternalism and Nihilism. It focuses on the 'Two Truths' and the deconstruction of solid reality through logic."
                ),
                Teaching(
                    title = "Dzogchen",
                    subtitle = "GREAT PERFECTION",
                    content = "The realization that the mind is already perfect and pure as it is. It emphasizes resting in the natural state without effort or 'doing'."
                ),
                Teaching(
                    title = "Mahamudra",
                    subtitle = "THE GREAT SEAL",
                    content = "A tradition where every experience is 'sealed' with the nature of mind. It emphasizes the recognition of the essence of awareness in every thought and emotion."
                ),
                Teaching(
                    title = "Rigpa",
                    subtitle = "PURE AWARENESS",
                    content = "A Tibetan term for 'innermost awareness' or 'pure intelligence'. It refers to the ground state of the mind, which is naturally empty, lucid, and unobstructed."
                ),
                Teaching(
                    title = "The Five Skandhas",
                    subtitle = "AGGREGATES",
                    content = "The five components (form, feeling, perception, mental formations, and consciousness) that make up a person's experience. Analysis shows no solid 'Self' exists within them."
                ),
                Teaching(
                    title = "Pratityasamutpada",
                    subtitle = "DEPENDENT ARISING",
                    content = "The principle that all things arise in dependence upon causes and conditions. Nothing exists in isolation; everything is part of an infinite web of interconnection."
                ),
                Teaching(
                    title = "Anicca / Anatta / Dukkha",
                    subtitle = "THE THREE MARKS",
                    content = "The three characteristics of existence: Impermanence (Anicca), Non-Self (Anatta), and Suffering/Unsatisfactoriness (Dukkha)."
                ),
                Teaching(
                    title = "Dharmakaya",
                    subtitle = "TRUTH BODY",
                    content = "The unconditioned, formless essence of the awakened mind. It is the ultimate state where everything is experienced as the play of enlightened mind."
                ),
                Teaching(
                    title = "Bodhicitta",
                    subtitle = "AWAKENED HEART",
                    content = "The altruistic and compassionate attitude that values others more than oneself. It is considered a technical tool for 'thinning out' the fixation on an ego."
                ),
                Teaching(
                    title = "Non-Dualism (Advaya)",
                    subtitle = "BEYOND DUALITY",
                    content = "The realization that the boundary between 'subject' (me) and 'object' (the world) is a mental illusion. It refers to the unified nature of experience."
                ),
                Teaching(
                    title = "Relative & Ultimate Truth",
                    subtitle = "THE TWO TRUTHS",
                    content = "Relative truth is how things appear to function in daily life; Ultimate truth is their empty, interdependent nature. Wisdom is the union of both."
                ),
                Teaching(
                    title = "Sahaja",
                    subtitle = "CO-EMERGENCE",
                    content = "The central concept in Mahamudra where clarity (Rigpa) and emptiness arise together simultaneously. They are inseparable, like fire and its heat."
                ),
                Teaching(
                    title = "Samsara",
                    subtitle = "CYCLIC EXISTENCE",
                    content = "The beginningless cycle of birth, suffering, and death driven by ignorance and attachment. It is the world of deceptive appearances."
                ),
                Teaching(
                    title = "Dharma",
                    subtitle = "THE TEACHINGS",
                    content = "The laws of reality and the path leading to liberation from suffering. It becomes authentic only through reflection on impermanence and renunciation."
                ),
                Teaching(
                    title = "Rang Rig",
                    subtitle = "SELF-PRESENT CLARITY",
                    content = "Self-knowing wisdom that is not a person or an observer, but a clarity unsupported by any basis."
                ),
                Teaching(
                    title = "The Eight Worldly Concerns",
                    subtitle = "MUNDANE ATTACHMENTS",
                    content = "The desires for gain, fame, praise, or pleasure, and the fear of their opposites. These keep the mind tied to Samsara and prevent authentic Dharma practice."
                )
            )
        )
    )
}