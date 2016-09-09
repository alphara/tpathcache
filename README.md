# tpathcache

Backend on clojure exposes one endpoint: GET /geocode?address="some address", which returns and caches all successful responses from 3rd party service.

3rd party service might occasionally return 5xx errors or respond slowly.

Errors are not caching because service (most probably) will respond successfully next time.

Clients awaits the response from the backend for 1 sec at max, and may retry a few times.

3rd party service is GET http://geo.truckerpathteam.com/maps/api/geocode/json?address="some address"

Each request to 3rd party service is very expensive so the backend minimizes number of calls to the bare minimum. At the same time backend does not return data older than 24 hours.

## Where to look to code?

Look on
src/clj/tpathcache/routes/home.clj

There is the most part of the backend.

## Prerequisites

Generated using Luminus version "2.9.10.96"
You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run

## License

Copyright © 2016, Artem Arakcheev
