// EJEMPLO de integración con KubeJS (solo entorno de desarrollo; no se distribuye con el mod).
// Demuestra cambiar el balance SIN recompilar Java: sobrescribe la entrada del bestiario de la araña.
// El bestiario es un mapa de datos de NeoForge (dragonball:bestiary); KubeJS inyecta un archivo de datos
// en la etapa 'last', que prevalece sobre los valores que trae el mod.
//
// NO se usa KubeJS para lógica crítica (fórmulas de XP, guardado, validaciones): eso vive en Java.
ServerEvents.generateData('last', event => {
  event.json('dragonball:data_maps/entity_type/bestiary', {
    values: {
      'minecraft:spider': { level: 4, kind: 'normal', archetype: 'veloz', xp: 50 }
    }
  })

  // Balance del combate (data/dragonball/balance/combat.json). Solo se cambia la clave que se menciona;
  // las demás conservan su valor por defecto. Comprobación en el juego: /dragonball balance.
  event.json('dragonball:balance/combat', { continuous_floor: 0.25 })
})
