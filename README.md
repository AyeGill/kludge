# Forked from [play-clj](https://github.com/oakes/play-clj) by Oakes. See readme there.

The main alteration from play-clj is that the collection of entities is a map of uuids to records, rather than just a vector.

It doesn't really make sense to have pass/fail unit tests in the usual sense, so our tests are a bit weird.

I've added the .so files necessary for this to run in the /resources file. These are only necessary for tests.

No changes have been tested on android, although they *should* work.
