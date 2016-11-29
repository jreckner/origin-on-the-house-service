package com.recknerd

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includePackage = false, includeNames = true)
class OnTheHouseOffer {

    def masterTitle
    def offerPath
    def itemName

    def getMasterTitleCleaned() {
        return masterTitle.replaceAll('\\s','_')
    }

}
