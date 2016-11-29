package com.recknerd

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class OriginOnTheHouseService {

    static final String ON_THE_HOUSE = 'on the house'
    static final String ON_THE_HOUSE_OFFERS_DB = '/tmp/oth.previous'

    static final String storeBase = "https://www.origin.com/usa/en-us/store"
    static final String apiBase = "https://api2.origin.com/supercat/US/en_US"
    static final String file = "/supercat-PCWIN_MAC-US-en_US.json.gz"

    static String offerTemplate = '''
        Origin's current 'On The House' game: %s
        Download the game here: %s

    '''

    List<OnTheHouseOffer> getCurrentOnTheHouseOffers() {
        def offers = []
        new JsonSlurper().parse(new URL("${apiBase}${file}")).offers.each { def entry ->
            if (entry.gameDistributionSubType?.toLowerCase() == ON_THE_HOUSE) {
                log.debug 'On the House Title Found: {}', entry.itemName
                log.debug "On the House Download Link: $storeBase{}", entry.offerPath
                def offer = new OnTheHouseOffer(
                        masterTitle: entry.masterTitle,
                        itemName: entry.itemName,
                        offerPath: entry.offerPath
                )
                offers.add(offer)
            }
        }
        log.debug 'currentOthGames loaded from Url: {}', offers
        return offers
    }

    List<String> getPreviousOnTheHouseOffers() {
        def previousOthGames = []
        def oth = new File(ON_THE_HOUSE_OFFERS_DB)
        if (oth.exists()) { oth.eachLine { previousOthGames << it } }
        log.debug 'previousOthGames loaded from file: {}', previousOthGames
        return previousOthGames
    }

    void setPreviousOnTheHouseOffers(currentOnTheHouseOffers) {
        def oth = new File(ON_THE_HOUSE_OFFERS_DB)
        oth.with { file ->
            file.withWriter { out ->
                currentOnTheHouseOffers.each {
                    log.debug 'New On the House Game Found: {}', it.masterTitleCleaned
                    out.println it.masterTitleCleaned
                }
            }
        }
    }

    List<String> getOffersDelta(previousOthOffers, currentOthOffers) {
        def currentOthOfferMasterTitlesCleaned = []
        currentOthOffers.each {
            currentOthOfferMasterTitlesCleaned.add(it.masterTitleCleaned)
        }
        def commons = previousOthOffers.intersect(currentOthOfferMasterTitlesCleaned)
        def difference = previousOthOffers.plus(currentOthOfferMasterTitlesCleaned)
        difference.removeAll(commons)
        return difference
    }

    static buildEmailParameters(currentOnTheHouseOffers) {
        def email = ''
        currentOnTheHouseOffers.each { OnTheHouseOffer offer ->
            def downloadLink = "${storeBase}${offer.offerPath}"
            email += String.format("$OriginOnTheHouseService.offerTemplate", offer.itemName, downloadLink)
        }
        return email
    }

    static void main(String[] args) {
        OriginOnTheHouseService service = new OriginOnTheHouseService()

        def previousOnTheHouseOffers = service.previousOnTheHouseOffers
        def currentOnTheHouseOffers = service.currentOnTheHouseOffers
        if (!service.getOffersDelta(previousOnTheHouseOffers, currentOnTheHouseOffers).empty) {
            service.setPreviousOnTheHouseOffers(currentOnTheHouseOffers)

            // Dump our email body to stdout to be picked up by system mail
            System.out.println buildEmailParameters(currentOnTheHouseOffers)
        }

    }
}

