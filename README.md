# tpathcache

## Backend part

Backend on clojure exposes one endpoint: GET /geocode?address="some address", which returns and caches all successful responses from 3rd party service.

3rd party service might occasionally return 5xx errors or respond slowly.

Errors are not caching because service (most probably) will respond successfully next time.

Clients awaits the response from the backend for 1 sec at max, and may retry a few times.

3rd party service is GET http://geo.truckerpathteam.com/maps/api/geocode/json?address="some address"

Each request to 3rd party service is very expensive so the backend minimizes number of calls to the bare minimum. At the same time backend does not return data older than 24 hours.

## Frontend part

Simple SPA based on ES2015, React, CSS with a web interface to that backend:
* a text input field with location data printed out beneath it (latitude + longitude values);
* no "submit" button, a system reacts on input's value;
* some basic "loading" indicator is shown as soon as query is changed;
* requests to backend isn't performed more often than 300 ms;

## Where to find the code?

Please, open the most interesting file for backend:
[src/clj/tpathcache/routes/home.clj](src/clj/tpathcache/routes/home.clj)

And for frontend:
[react.js](react.js)

There is the most part of the backend.

## Prerequisites

Generated using Luminus version "2.9.10.96"
You will need:
* [Leiningen][1] 2.0 or above installed.
* npm - to build react application through webpack

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    npm install
    npm run build
    lein run

Then, open in browswer:

    http://localhost:3000/

## License

Copyright © 2016, Artem Arakcheev
