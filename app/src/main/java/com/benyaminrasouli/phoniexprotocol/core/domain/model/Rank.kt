package com.benyaminrasouli.phoniexprotocol.core.domain.model

enum class Rank(val displayName: String, val levelRequired: Int) {
    INITIATE("Initiate", 1),
    SURVIVOR("Survivor", 5),
    HUNTER("Hunter", 10),
    WARRIOR("Warrior", 15),
    ELITE("Elite", 20),
    COMMANDER("Commander", 30),
    PHANTOM("Phantom", 40),
    TITAN("Titan", 50),
    ASCENDANT("Ascendant", 65),
    PHOENIX("Phoenix", 80);

    companion object {
        fun forLevel(level: Int): Rank {
            return entries.last { it.levelRequired <= level }
        }
    }
}
