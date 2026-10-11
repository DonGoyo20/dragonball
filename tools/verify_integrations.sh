#!/usr/bin/env bash
# Ejecuta las pruebas dentro del juego de cada integración y EXIGE resultados explícitos.
# Un "BUILD SUCCESSFUL" por sí solo no vale: una ejecución con 0 pruebas también termina con éxito.
#
# Uso:  tools/verify_integrations.sh            (las tres ejecuciones)
#       tools/verify_integrations.sh core       (solo una: core | ftb | kubejs)
set -u
cd "$(dirname "$0")/.."
LOGDIR="${TMPDIR:-/tmp}/dragonball-verify"
mkdir -p "$LOGDIR"
fail=0

check() {  # nombre, comando de gradle, patrones obligatorios...
  local name="$1" cmd="$2"; shift 2
  local log="$LOGDIR/$name.log"
  echo "=== [$name] $cmd"
  ./gradlew $cmd --console=plain > "$log" 2>&1
  # Pruebas: debe haber una línea "All N required tests passed" con N >= 1
  local n
  n=$(grep -oE "All [0-9]+ required tests passed" "$log" | grep -oE "[0-9]+" | head -1)
  if [ -z "${n:-}" ] || [ "$n" -lt 1 ]; then
    echo "  FALLO: no hay 'All N required tests passed' con N>=1"; grep -E "LogTestReporter|No test functions|required tests failed" "$log" | cut -c1-220 | head -8
    fail=1
  else
    echo "  OK: $n pruebas pasaron"
  fi
  local pat
  for pat in "$@"; do
    if grep -qE "$pat" "$log"; then echo "  OK: registro contiene /$pat/"; else echo "  FALLO: falta en el registro /$pat/"; fail=1; fi
  done
  if grep -E "ERROR" "$log" | grep -vE "Yggdrasil|server.properties|Settings\]|LogTestReporter" | grep -qE "ftbquests|FTB Quests|patchouli|geckolib|kubejs|KubeJS|dragonball"; then
    echo "  AVISO: hay errores de integraciones en el registro:"; grep -E "ERROR" "$log" | grep -E "ftbquests|FTB Quests|patchouli|geckolib|kubejs|KubeJS|dragonball" | grep -vE "Yggdrasil|server.properties|LogTestReporter" | cut -c1-200 | head -5
    fail=1
  fi
}

want="${1:-all}"
[ "$want" = all ] || [ "$want" = core ]   && check core   "runGameTestServer"                         "Dragon Ball RPG cargado"
[ "$want" = all ] || [ "$want" = ftb ]    && check ftb    "runGameTestFtbQuests -PwithFtbQuests"      "FTB Quests/\\]: Loaded 1 chapter groups, 1 chapters, 3 quests"
[ "$want" = all ] || [ "$want" = kubejs ] && check kubejs "runGameTestKubeJs -PwithKubeJs"            "KubeJS Server/\\]: Loaded 1/1 KubeJS server scripts in .* with 0 errors"
echo; [ $fail -eq 0 ] && echo "TODAS LAS VERIFICACIONES PASARON" || echo "HAY FALLOS (registros en $LOGDIR)"
exit $fail
