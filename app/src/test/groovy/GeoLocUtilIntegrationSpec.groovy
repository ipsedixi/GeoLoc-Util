import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.TempDir
import library.enums.StateCode
import App
import picocli.CommandLine

class GeoLocUtilIntegrationSpec extends Specification {

    def 'Valid city and state lookup returns accurate data'() {
        given: 'Build search term'
        String cityAndState = "${city}, ${stateCode.code}"

        when: 'Run request'
        List<Map> result = runGeoLocUtil(cityAndState)

        then: 'Returned results are accurate'
        result.size() == 1
        result[0].name == city
        result[0].state == stateCode.name
        result[0].country == 'US'

        // correct coordinates within 0.1 degree, so this test is resilient to small changes
        (result[0].lat - latitude).abs() < 0.1
        (result[0].lon - longitude).abs() < 0.1

        // local names may be added later, so to prevent this from becoming a false positive,
        // we won't verify local names are not present just because they aren't present now
        if (hasLocalNames) assert result[0].containsKey('local_names')

        where:
        city                      | stateCode    || latitude | longitude | hasLocalNames
        'New York'                | StateCode.NY || 40.7     | -74.0     | true
        'Los Angeles'             | StateCode.CA || 34.1     | -118.2    | true
        'Chicago'                 | StateCode.IL || 41.9     | -87.6     | true
        'Houston'                 | StateCode.TX || 29.8     | -95.4     | true
        'Phoenix'                 | StateCode.AZ || 33.4     | -112.1    | true
        'Philadelphia'            | StateCode.PA || 40.0     | -75.2     | true
        'San Antonio'             | StateCode.TX || 29.4     | -98.5     | true
        'San Diego'               | StateCode.CA || 32.7     | -117.2    | true
        'Dallas'                  | StateCode.TX || 32.8     | -96.8     | true
        'Jacksonville'            | StateCode.FL || 30.3     | -81.7     | true
        'Washington'              | StateCode.DC || 38.9     | -77.0     | true
        'Truth or Consequences'   | StateCode.NM || 33.1     | -107.3    | true          // cool name
        'Bellefontaine Neighbors' | StateCode.MO || 38.7     | -90.2     | false         // longest name
        'Winston-Salem'           | StateCode.NC || 36.1     | -80.2     | true          // -
        'Martha\'s Vineyard'      | StateCode.MA || 41.4     | -70.6     | true          // '
        'Hagåtña'                 | StateCode.GU || 13.5     | 144.8     | true          // å, ñ and organized, unincorporated territory
        'Utqiaġvik'               | StateCode.AK || 71.3     | -156.8    | true          // ġ 
        '\'Amanave'               | StateCode.AS || -14.3    | -170.8    | true          // ' and unincorporated territory
        'César Chávez'            | StateCode.TX || 26.3     | -98.1     | true          // é, á
        'Lindström'               | StateCode.MN || 45.4     | -92.8     | true          // ö
        'Bayamón'                 | StateCode.PR || 18.4     | -66.2     | false         // ó and unincorporated commonwealth
        'Charlotte Amalie'        | StateCode.VI || 18.3     | -64.9     | true
    }

    def 'Full state names are valid search terms'() {
        given: 'Build search term'
        String cityAndState = "${city}, ${stateCode.name}"

        when: 'Run request'
        List<Map> result = runGeoLocUtil(cityAndState)

        then: 'Returned results are accurate'
        result.size() == 1
        result[0].name == city
        result[0].state == stateCode.name

        where:
        city                      | stateCode    
        'New York'                | StateCode.NY 
        'Los Angeles'             | StateCode.CA 
        'Chicago'                 | StateCode.IL 
        'Houston'                 | StateCode.TX 
        'Phoenix'                 | StateCode.AZ
        'Philadelphia'            | StateCode.PA
        'San Antonio'             | StateCode.TX
        'San Diego'               | StateCode.CA
        'Dallas'                  | StateCode.TX
        'Jacksonville'            | StateCode.FL
        'Washington'              | StateCode.DC
        'Truth or Consequences'   | StateCode.NM 
        'Bellefontaine Neighbors' | StateCode.MO
        'Winston-Salem'           | StateCode.NC
        'Martha\'s Vineyard'      | StateCode.MA
        'Hagåtña'                 | StateCode.GU
        'Utqiaġvik'               | StateCode.AK
        '\'Amanave'               | StateCode.AS
        'César Chávez'            | StateCode.TX
        'Lindström'               | StateCode.MN
        'Bayamón'                 | StateCode.PR
        'Charlotte Amalie'        | StateCode.VI
    }

    def 'Famous cities can be found without a state'() {
        given: 'Build search term'
        String cityAndState = "${city}"

        when: 'Run request'
        List<Map> result = runGeoLocUtil(cityAndState)

        then: 'Returned results are accurate'
        result.size() == 1
        result[0].name == city
        result[0].state == stateCode.name

        where:
        city                      || stateCode    
        'New York'                || StateCode.NY 
        'Los Angeles'             || StateCode.CA 
        'Chicago'                 || StateCode.IL 
        'Houston'                 || StateCode.TX 
        'Phoenix'                 || StateCode.AZ
        'Philadelphia'            || StateCode.PA
        'San Antonio'             || StateCode.TX
        'San Diego'               || StateCode.CA
        'Dallas'                  || StateCode.TX
        'Jacksonville'            || StateCode.FL
        'Washington'              || StateCode.DC
    }

    def 'Cities with special characters or multiple correct spellings can be found by variant names'() {
        given: 'Build search term'
        String cityAndState = "${variantCityName}, ${stateCode.code}"

        when: 'Run request'
        List<Map> result = runGeoLocUtil(cityAndState)

        then: 'Correct city is found'
        result.size() == 1
        result[0].name == standardCityName

        where:
        variantCityName    | stateCode    || standardCityName
        'Hagatna'          | StateCode.GU || 'Hagåtña'
        'Utqiagvik'        | StateCode.AK || 'Utqiaġvik'
        'Amanave'          | StateCode.AS || '\'Amanave'
        'ʻĀmanave'         | StateCode.AS || '\'Amanave'
        'Cesar Chavez'     | StateCode.TX || 'César Chávez' 
        'Lindstrom'        | StateCode.MN || 'Lindström'
        'Bayamon'          | StateCode.PR || 'Bayamón' 
        'St. Louis'        | StateCode.MO || 'Saint Louis'
        'cHaRlOtTe aMaLiE' | StateCode.VI || 'Charlotte Amalie'  // spongecase
        '   Austin   '     | StateCode.TX || 'Austin'            // leading and trailing spaces           
        '奥兰多'            | StateCode.FL || 'Orlando'           // non-Latin script
    }

    def 'Valid zip code lookup returns accurate data'() {
        when: 'Run request'
        List<Map> result = runGeoLocUtil(zipCode)

        then: 'Returned results are accurate'
        result.size() == 1
        result[0].zip == zipCode[-5..-1]  // last five characters of zipCode
        result[0].name == name
        result[0].country == 'US'

        // correct coordinates within 0.1 degree, so this test is resilient to small changes
        (result[0].lat - latitude).abs() < 0.1
        (result[0].lon - longitude).abs() < 0.1

        where: 
        zipCode  || name                  | latitude | longitude
        '-10001' || 'New York'            | 40.7     | -74.0     // negative zip code gets corrected
        '00501'  || 'Suffolk County'      | 40.8     | -73.0     // lowest zip code
        '10118'  || 'New York'            | 40.7     | -74.0     // Empire State Building
        '11109'  || 'New York'            | 40.7     | -74.0     // smallest
        '12345'  || 'Schenectady'         | 42.8     | -73.9     // easiest to remember
        '20252'  || 'Washington'          | 39.0     | -77.1     // Smokey Bear's personal zip code. Who knew!
        '79936'  || 'El Paso'             | 31.8     | -106.3    // most populous
        '90210'  || 'Beverly Hills'       | 34.1     | -118.4    // most famous?
        '96799'  || 'Pago Pago'           | -14.3    | -170.7    // southernmost municipality
        '96898'  || 'Wake Island'         | 19.3     | 166.6     // most remote?
        '96950'  || 'Saipan Municipality' | 15.2     | 145.7     // easternmost municipality
        '99546'  || 'Adak'                | 51.9     | -176.6    // westernmost municipality
        '99723'  || 'Utqiaġvik'           | 71.2     | -156.8    // northernmost municipality
        '99734'  || 'North Slope'         | 70.3     | -148.7    // largest
        '99950'  || 'Ketchikan'           | 55.3     | -131.6    // highest zip code
    }

    def "Nonexistent or malformed input do not return results"() {
        when: 'Run request'
        List<Map> result = runGeoLocUtil(searchTerm)

        then: 'No results are found'
        result[0].isEmpty() || result[0].cod == '404'

        where:
        searchTerm << [
            "Lost City of Atlanta, ${StateCode.GA.code}", 
            "88888", // zip code for letters to Santa Claus
            '12345, TX',
            'ABCDE',
            '12 345',
            '!@#$%',
            '12201-7050',
            'New York 10001',
            'New York,,NY',
            ', New York',
            'Los Angeles CA',
            'Los Angeles,,,',
            'New York, XYZ',
            '123456',
            '10001.5',
            '12345, 67890',
            '10001, US',
            'New York, USA',
            '1000A',
            ' ',
            ''
        ]
    }
    
    def "Request with many valid and invalid search terms"() {
        given: 'Build search term'
        String[] searchTerms = [
            "Los Angeles, ${StateCode.CA.code}",
            "Los Angeles,,,",                       // Invalid
            "New York, ${StateCode.NY.code}",
            "Chicago, ${StateCode.IL.code}",
            "Houston, ${StateCode.TX.code}",
            "Phoenix, ${StateCode.AZ.code}",
            "Philadelphia, ${StateCode.PA.code}",
            "San Antonio, ${StateCode.TX.code}",
            "San Diego, ${StateCode.CA.code}",
            "Dallas, ${StateCode.TX.code}",
            "San Jose, ${StateCode.CA.code}",
            "Austin, ${StateCode.TX.code}",
            "Lost City of Atlanta, GA",             // Invalid 
            "Springfield, ZZ",                      // Invalid
            "New York, USA",                        // Invalid
            "Los Angeles CA",                       // Invalid
            "Chicago, qwerty",                      // Invalid
            "New York,,NY",                         // Invalid
            ", New York",                           // Invalid
            "Jacksonville, ${StateCode.FL.code}",
            "Fort Worth, ${StateCode.TX.code}",
            "Columbus, ${StateCode.OH.code}",
            "Indianapolis, ${StateCode.IN.code}",
            "Charlotte, ${StateCode.NC.code}",
            "San Francisco, ${StateCode.CA.code}",
            "Seattle, ${StateCode.WA.code}",
            "Denver, ${StateCode.CO.code}",
            "Washington, ${StateCode.DC.code}",
            "Boston, ${StateCode.MA.code}",
            "El Paso, ${StateCode.TX.code}",
            "Nashville-Davidson, ${StateCode.TN.code}",
            "Detroit, ${StateCode.MI.code}",
            "Oklahoma City, ${StateCode.OK.code}",
            "53705", 
            "Portland, ${StateCode.OR.code}",
            "10001", 
            "Las Vegas, ${StateCode.NV.code}",
            "30301", 
            "Memphis, ${StateCode.TN.code}",
            "33101", 
            "Louisville, ${StateCode.KY.code}",
            "60601",
            "Baltimore, ${StateCode.MD.code}",
            "99999",                                // Invalid
            "Milwaukee, ${StateCode.WI.code}",
            "00000",                                // Invalid
            "Albuquerque, ${StateCode.NM.code}",
            "ABCDE",                                // Invalid
            "Tucson, ${StateCode.AZ.code}",
            "12 345",                               // Invalid
            "Fresno, ${StateCode.CA.code}",
            "*10001",                               // Invalid
            "Mesa, ${StateCode.AZ.code}",
            "10001.5",                              // Invalid
            "Sacramento, ${StateCode.CA.code}",
            "123456",                               // Invalid
            "Atlanta, ${StateCode.GA.code}",
            "1000A",                                // Invalid
            "Kansas City, ${StateCode.MO.code}",
            "100",                                  // Invalid
            "Colorado Springs, ${StateCode.CO.code}",
            "Miami, ${StateCode.FL.code}",
            "Raleigh, ${StateCode.NC.code}",
            "Omaha, ${StateCode.NE.code}",
            "Long Beach, ${StateCode.CA.code}",
            "Virginia Beach, ${StateCode.VA.code}",
            "Oakland, ${StateCode.CA.code}",
            "Minneapolis, ${StateCode.MN.code}",
            "Tulsa, ${StateCode.OK.code}",
            "Tampa, ${StateCode.FL.code}",
            "Arlington, ${StateCode.TX.code}",
            "New Orleans, ${StateCode.LA.code}",
            "Wichita, ${StateCode.KS.code}",
            "Cleveland, ${StateCode.OH.code}",
            "Bakersfield, ${StateCode.CA.code}",
            "Aurora, ${StateCode.CO.code}",
            "Anaheim, ${StateCode.CA.code}",
            "Honolulu, ${StateCode.HI.code}",
            "Santa Ana, ${StateCode.CA.code}",
            "Corpus Christi, ${StateCode.TX.code}",
            "Riverside, ${StateCode.CA.code}",
            "Lexington, ${StateCode.KY.code}",
            "Saint Louis, ${StateCode.MO.code}",
            "Stockton, ${StateCode.CA.code}",
            "Pittsburgh, ${StateCode.PA.code}",
            "Saint Paul, ${StateCode.MN.code}",
            "Cincinnati, ${StateCode.OH.code}",
            "Anchorage, ${StateCode.AK.code}",
            "Henderson, ${StateCode.NV.code}",
            "Greensboro, ${StateCode.NC.code}",
            "Plano, ${StateCode.TX.code}",
            "Newark, ${StateCode.NJ.code}",
            "Lincoln, ${StateCode.NE.code}",
            "Orlando, ${StateCode.FL.code}",
            "Irvine, ${StateCode.CA.code}",
            "Toledo, ${StateCode.OH.code}",
            "Durham, ${StateCode.NC.code}",
            "Chula Vista, ${StateCode.CA.code}"
        ]
        List<int> invalidIndices = [
            1, 
            12, 
            13, 
            14, 
            15, 
            16, 
            17, 
            18, 
            43, 
            45, 
            47, 
            49, 
            51, 
            53, 
            55, 
            57, 
            59
        ]
        
        when: 'Run request'
        List<Map> result = runGeoLocUtil(searchTerms)

        then: 'Results are returned correctly for valid search terms'
        searchTerms.eachWithIndex { term, index ->
            Map resultMap = result[index]
            if (invalidIndices.contains(index)) {
                assert resultMap.isEmpty() || resultMap.cod == '404'
            } else {
                if (term.isNumber()) {
                    assert resultMap.zip == term
                } else {
                    assert resultMap.name == term.split(",")[0].trim()
                }
                assert resultMap.country == 'US'
            }
        }
    }

    private List<Map> runGeoLocUtil(String[] args) {
        App app = new App()
        new CommandLine(app).execute(args)
        return app.getResults()
    }

}

