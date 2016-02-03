/*
 Copyright 2012 Rustici Software, LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

var TINCAN = (TINCAN || {});

//
// Using the given lrsList, this object will fetch from many LRSs
// and allow consumers to process those statements by descending stored
// date, respecting order across the many LRSs
//
TINCAN.MultiLRSStatementStream = function (lrsList) {
    this.lrsList = lrsList;
    this.state = {};

    this.initializeState();
};
TINCAN.MultiLRSStatementStream.prototype = {
    getLrsId: function (lrs) {
        return lrs.endpoint + lrs.auth + lrs.version;
    },

    initializeState: function () {
        var i, lrs;
        for (i = 0; i < this.lrsList.length; i += 1) {
            lrs = this.lrsList[i];
            this.state[this.getLrsId(lrs)] = {
                lrs: lrs,
                statements: [],
                moreUrl: null
            };
        }
    },

    getIdMap: function () {
        var k, ids = {};
        for (k in this.state) {
            if (this.state.hasOwnProperty(k)) {
                ids[k] = true;
            }
        }
        return ids;
    },

    // Returns the next statement from the given statement streams based
    // on most recent stored date.
    getNextStatement: function () {
        var lrsId, lrsState, recentStatement, maxDate = "0", nextLrsId = null;

        for (lrsId in this.getIdMap()) {
            lrsState = this.state[lrsId];

            // We have to stop giving statements here because the most recent
            // statement could be one we haven't yet fetched
            if (lrsState.statements.length === 0 && lrsState.moreUrl !== null) {
                return null;
            }
            if (lrsState.statements.length > 0) {
                recentStatement = lrsState.statements[0];
                if (recentStatement.stored === null || recentStatement.stored.localeCompare(maxDate) > 0) {
                    if (recentStatement.stored !== null) {
                        maxDate = recentStatement.stored;
                    }
                    nextLrsId = lrsId;
                }
            }
        }
        return (nextLrsId === null) ? null : this.state[nextLrsId].statements.shift();
    },

    // Use getNextStatement to get all loaded statements that can be
    // guaranteed to be in order
    getAllStatements: function () {
        var stmt, statements = [];
        stmt = this.getNextStatement();
        while (stmt !== null) {
            if (_.isEmpty(stmt.verb.display)) {
                var verbId = stmt.verb.id;
                stmt.verb.display["en-US"] = verbId.toString().substr(verbId.lastIndexOf('/') + 1)
            }
            statements.push(stmt);
            stmt = this.getNextStatement();
        }
        return statements;
    },

    // Load statements from multiple LRSs, and call the given callback when
    // those statements are ready passing in this multi stream object as the argument
    loadStatements: function (queryObj, callback) {
        var multiStream = this,
            lrsListToUse = this.lrsList,
            lrsId,
            lrsState,
            i,
            callbackCount,
            createCallback,
            url,
            versionHeader,
            _requestCfg,
            requestCfg = null;


        this.initializeState();


        // Capture total count of lrs's to help w/ multiple callbacks below
        callbackCount = lrsListToUse.length;

        // Setup a function which will create a callback for this lrs fetch
        createCallback = function (lrsId) {
            return function (err, xhr) {

                stResult = JSON.parse(xhr.response);
                stResultStatement = _.map(stResult.statements, function (st) {
                        var statement = TinCan.Statement.fromJSON(JSON.stringify(st));
                        statement.originalJSON = JSON.stringify(st, "", 4);
                        return statement;

                    }
                )
                var streamState = multiStream.state[lrsId];
                callbackCount--;

                if (err === null) {
                    // Capture this lrs's statements into state, note more url
                    Array.prototype.push.apply(streamState.statements, stResultStatement);
                    streamState.moreUrl = stResult.more;
                }

                // Only do handed in callback after all versions done
                if (callbackCount === 0) {
                    callback(multiStream);
                }
            };
        };

        // Loop through the lrs's, setting up callbacks and fetching statements
        for (i = 0; i < lrsListToUse.length; i += 1) {
            // Get reference to lrs we're using
            lrs = lrsListToUse[i];
            lrsId = this.getLrsId(lrs);

            _requestCfg = lrs.sendRequest(
                {
                    url: "statements",
                    method: "GET",
                    params: queryObj,
                    callback: createCallback(lrsId)

                }
            )

            if (requestCfg === null) {
                requestCfg = _requestCfg;
            }
        }
        return {
            url: "statements",
            method: "GET",
            params: queryObj,
            callback: createCallback(lrsId)
        }
    },

    // Load more statements from the saved multiple LRSs and current query
    // started with loadStatements. Call callback when statements are ready,
    // passing in this multi stream object as the argument
    loadMoreStatements: function (callback) {
        this.loadStatements("more", callback);
    },

    // Returns true if there are no more statements available, whether fetched
    // or potentially available through a continuation query (i.e. "more" url)
    exhausted: function () {
        var lrsId, lrsState;
        for (lrsId in this.getIdMap()) {
            lrsState = this.state[lrsId];
            if (lrsState.statements.length > 0 || lrsState.moreUrl !== null) {
                return false;
            }
        }
        return true;
    }
};