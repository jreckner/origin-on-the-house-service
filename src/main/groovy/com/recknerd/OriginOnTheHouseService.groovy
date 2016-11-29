package com.recknerd

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class OriginOnTheHouseService {

    static final String ON_THE_HOUSE = 'on the house'

    static final String storeBase = "https://www.origin.com/usa/en-us/store"
    static final String apiBase = "https://api2.origin.com/supercat/US/en_US"
    static final String file = "/supercat-PCWIN_MAC-US-en_US.json.gz"

    static String email = '''
        Origin's current 'On The House' games: %s
    '''

    def getOnTheHouseGames() {
        def gameMasterTitles = []
        new JsonSlurper().parse(new URL("${apiBase}${file}")).offers.each { def entry ->
            if (entry.gameDistributionSubType?.toLowerCase() == ON_THE_HOUSE) {
                log.debug 'On the House Title Found: {}', entry.itemName
                log.debug "On the House Download Link: $storeBase{}", entry.offerPath
                gameMasterTitles.add(entry.masterTitle.replaceAll('\\s','_'))
            }
        }
        return gameMasterTitles
    }

    static void main(String[] args) {
        def previousOthGames = []

        def oth = new File( '/tmp/oth.previous' )
        if (oth.exists()) {
            oth.eachLine { line ->
                previousOthGames << line
            }
        }

        def currentOthGames = new OriginOnTheHouseService().onTheHouseGames

        def commons = previousOthGames.intersect(currentOthGames)
        def difference = previousOthGames.plus(currentOthGames)
        difference.removeAll(commons)

        if (!difference.empty) {
            oth.with { file ->
                file.withWriter { out ->
                    currentOthGames.each {
                        log.debug 'New On the House Game Found: {}', it
                        out.println it
                    }
                }
            }
        }

        System.out.println String.format("$OriginOnTheHouseService.email", currentOthGames)
    }
}

