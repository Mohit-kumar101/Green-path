#!/bin/sh
set -e

trim() {
  printf '%s' "$1" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'
}

resolve_mongo_uri() {
  for key in MONGODB_URI SPRING_DATA_MONGODB_URI MONGO_URI MONGO_URL DATABASE_URL; do
    value=$(trim "$(eval "printf '%s' \"\${$key}\"")")
    if [ -n "$value" ]; then
      printf '%s' "$value"
      return 0
    fi
  done
  return 1
}

mongo_uri=$(resolve_mongo_uri || true)

if [ -n "$mongo_uri" ]; then
  host=$(printf '%s' "$mongo_uri" | sed -E 's|^mongodb(\+srv)?://||; s|^[^@]+@||; s|[/?].*||')
  echo "MongoDB URI detected from environment (host: ${host})"
  exec java $JAVA_OPTS \
    -Dspring.data.mongodb.uri="$mongo_uri" \
    -DMONGODB_URI="$mongo_uri" \
    -jar /app/app.jar
fi

echo "ERROR: No MongoDB URI env var found."
echo "Set MONGODB_URI on the Render web service Environment tab (no quotes), then redeploy."
echo "Checked: SPRING_DATA_MONGODB_URI, MONGODB_URI, MONGO_URI, MONGO_URL, DATABASE_URL"
exit 1
