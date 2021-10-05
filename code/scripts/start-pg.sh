#---
# Excerpted from "From Objects to Functions",
# published by The Pragmatic Bookshelf.
# Copyrights apply to this code. It may not be used to create training material,
# courses, books, articles, and the like. Contact us if you are in doubt.
# We make no guarantees that this code is fit for any purpose.
# Visit http://www.pragmaticprogrammer.com/titles/uboop for more book information.
#---
readonly BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# delete the volumes if they exists
docker volume rm pg-volume || true
# create fresh volumes
docker volume create pg-volume

(docker-compose --file ${BASE_DIR}/docker-compose-local-pg.yml up -d)
