execute as @a[tag=!db_setup] at @s run function dragonball_dev:setup
execute as @a at @s if entity @e[type=dragonball:wild_wolf,distance=..40] run tp @s ~ ~ ~ facing entity @e[type=dragonball:wild_wolf,limit=1,sort=nearest] eyes
execute as @a at @s unless entity @e[type=dragonball:wild_wolf] run title @s actionbar "NO HAY LOBO"
