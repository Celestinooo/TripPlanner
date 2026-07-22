#!/bin/sh
# Finaliza o setup do git — rode uma vez e delete em seguida
set -e

cd "$(dirname "$0")"

echo "→ Removendo index.lock..."
rm -f .git/index.lock

echo "→ Marcando gradlew como executável..."
git update-index --chmod=+x gradlew

echo "→ Buscando gradle-wrapper.jar no cache local..."
JAR=$(find ~/.gradle/wrapper/dists -name "gradle-wrapper.jar" 2>/dev/null | head -1)
if [ -n "$JAR" ]; then
    cp "$JAR" gradle/wrapper/gradle-wrapper.jar
    git add gradle/wrapper/gradle-wrapper.jar
    echo "  ✓ gradle-wrapper.jar adicionado de: $JAR"
else
    echo "  ⚠ gradle-wrapper.jar não encontrado no cache."
    echo "    Gere com: gradle wrapper --gradle-version 9.3.0"
fi

echo ""
echo "→ Status final:"
git status --short

echo ""
echo "Pronto! Rode: git commit -m 'feat: standalone TripPlanner module'"
