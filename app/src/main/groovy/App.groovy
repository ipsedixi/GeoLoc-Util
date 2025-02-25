import groovy.json.JsonSlurper
import java.net.URLEncoder
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters


@CommandLine.Command(
    name = "geoloc-util",
    mixinStandardHelpOptions = true,
    description = "A CLI tool for fetching location data."
)
class App implements Runnable {
    @CommandLine.Parameters(arity = "1..*", description = "Locations to look up ('City, State Code' OR 'Zip Code')")
    List<String> locations

    static final String API_KEY = 'f897a99d971b5eef57be6fafa0d83239'
    List<Map> results = []

    void run() {
        locations.each { location ->
            String encodedLocation = URLEncoder.encode(location, "UTF-8")
            String apiUrl
            if (location.isNumber()) {
                apiUrl = "http://api.openweathermap.org/geo/1.0/zip?zip=${encodedLocation},us&appid=${API_KEY}"
            } else {
                apiUrl = "http://api.openweathermap.org/geo/1.0/direct?q=${encodedLocation},us&limit=1&appid=${API_KEY}"
            }

            Map response = makeHttpRequest(apiUrl)
            results.add(response)
            println "Response for $location: $response"
        }
    }

    List<Map> getResults() {
        return results
    }

    Map makeHttpRequest(String url) {
        CloseableHttpClient client = HttpClients.createDefault()
        HttpGet request = new HttpGet(url)
        CloseableHttpResponse response = client.execute(request)

        HttpEntity entity = response.getEntity()
        String responseBody = EntityUtils.toString(entity)
        def jsonResponse = new JsonSlurper().parseText(responseBody)

        if (jsonResponse instanceof List) {
            jsonResponse = (jsonResponse.size() > 0) ? jsonResponse[0] : [:]
        }

        client.close()
        return jsonResponse
    }

    static void main(String[] args) {
        if (args.contains('--run-tests')) runTests()
        else {
            App app = new App()
            new CommandLine(app).execute(args)
            println "all results:"
            println app.getResults()
        }
    }

}
