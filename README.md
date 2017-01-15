OpenService-BCB
===============

## License ##

Developed and open-sourced by VNG Corporation. See [LICENSE.txt](LICENSE.txt).


## Release & Installation ##

Latest release version: `0.1.2`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

Download, Build and Install: see [INSTALL.md](INSTALL.md).


## Introduction ##

BCB is a service that provides APIs to:

- Store & Retrieve top-k ranking tables.
- Support unlimited versions of a ranking table (each version is associated with a timestamp for latter retrieval).
- Support unlimited ranking tables (each ranking table has a unique name).
- Retrieve ranking history of an item.


## APIs ##

As of v0.1.0, only REST-like APIs are supported. All APIs are invoked via HTTP POST method, inputs and outputs are JSON encoded. The output as the following format:

```json
{
    "status" : (int) result status (see table below),
    "message": "(string)" status message,
    "data"   : (various) API's result if successful call
}
```

### Common result status ###

- 200: Ok/Successful, API was called successfully.
- 400: Client error, API call was not successful due to client error (e.g. missing or invalid parameters).
- 500: Server error, API call was not successful due to server error (e.g. exception at server side).
- 403: Access denied, client is not allowed to call the API.

### /api/updateRankings ###

Update/store a version of ranking table.

**Inputs:**

- `name (string)`      : name of the rankings table to update/store.
- `timestamp (integer)`: timestamp/version of the ranking data.
- `rankings (Array)`   : rankings data as an array of items, sorted from the highest ranking (#1) to the lowest,
  each item has the following fields:
  - `id (string)`   : item's unique name/id
  - `value (double)`: item's value
  - `info (string)` : item's extra info (will be stored and returned as-is)

**Outputs:**

- (integer) number of item added.

### /api/getRankings ###

Get rankings data.

**Inputs:**

- `name (string)`      : name of the rankings table to retrieve data from.
- `timestamp (integer)`: timestamp/version of rankings data to retrieve.
- `info (boolean)`     : (optional) if `true` returns `info` field for each item.

**Outputs:**

- (Array) rankings data as an array of items, sorted from the highest ranking (#1) to the lowest,
  each item has the following fields:
  - `id (string)`   : item's unique name/id
  - `value (double)`: item's value
  - `info (string)` : item's extra info (if input `info = true`)

### /api/getHistory ###

Get historical ranking data of an item.

**Inputs:**

- `name (string)`  : name of the rankings table to retrieve data from.
- `id (string)`    : item's unique name/id
- `start (integer)`: (optional) starting timestamp (inclusive)
- `end (integer)`  : (optional) ending timestamp (exclusive)

**Outputs:**

- (Array) historical ranking data of the specified item, sorted by timestamp, each entry has the following fields:
  - `timestamp (integer)`: timestamp/version of the ranking data
  - `value (double)`     : item's value
  - `position (integer)` : item's position in the ranking table
