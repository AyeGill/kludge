# Forked from [play-clj](https://github.com/oakes/play-clj) by Oakes. See readme there.

The main alteration from play-clj is that the collection of entities is a map of uuids to records, rather than just a vector.

I've added the .so files necessary for this to run in the /resources file. These are only necessary for tests.

The tags that are version numbers all refer to the "old" versions, i.e. play-clj versions. I've reset the version at 0.0.1 for now, to reflect that fact that the changes are still mostly untested.
