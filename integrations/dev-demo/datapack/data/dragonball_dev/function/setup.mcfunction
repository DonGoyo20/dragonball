# Se ejecuta una vez por jugador: lobo delante, día despejado y vida enorme para ver las animaciones sin morir.
time set day
weather clear
attribute @s minecraft:generic.max_health base set 2000
effect give @s minecraft:instant_health 1 30 true
summon dragonball:wild_wolf ~2 ~ ~6 {PersistenceRequired:1b}
tag @s add db_setup
