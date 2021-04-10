package ie.ul.fitbook.recording;

import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.interfaces.ActionHandlerConsumer;
import ie.ul.fitbook.recording.services.RecordingService;

/**
 * This class provides utilities for activity recording
 */
public final class RecordingUtils {
    /**
     * The altitude difference to determine if a location should go for elevation gain calculations
     */
    private static final int ELEVATION_GAIN_THRESHOLD = 1;
    /**
     * The max locations we can query in a request
     */
    private static final int MAX_REQUEST_LOCATIONS = 512;
    /**
     * The start of our request url
     */
    private static final String REQUEST_URL_START = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
    /**
     * Arraylist of request URLs if one request url exceeds 512 locations, otherwise this will be size 1
     */
    private static ArrayList<String> requestURLs;
    /**
     * The JSONObject we are building if requestURLs size is greater than 1
     */
    private static JSONObject builtObject;
    /**
     * The index the request is at
     */
    private static int requestIndex;
    /**
     * Returned when elevation gain could not be found. Use this to detect that an error occurred.
     */
    public static final Double ELEVATION_GAIN_UNAVAILABLE = -1.0;

    /**
     * Prevent instantiation
     */
    private RecordingUtils() {}

    /**
     * Takes the JSON response and calculates elevation gain
     * @param response the response to handle
     * @param consumer the consumer object which will take the computed value
     */
    private static void handleSuccessfulResponse(JSONObject response, ActionHandlerConsumer<Double> consumer) {
        try {
            double elevationGain = 0.0;
            JSONArray results = response.getJSONArray("results");
            JSONObject initialLocation = results.getJSONObject(0);
            double currentElevation = initialLocation.getDouble("elevation");

            for (int i = 1; i < results.length(); i++) {
                JSONObject elevation = results.getJSONObject(i);
                double receivedElevation = elevation.getDouble("elevation");
                if (receivedElevation > currentElevation) {
                    elevationGain += receivedElevation - currentElevation;
                }

                currentElevation = receivedElevation;
            }

            consumer.doAction(elevationGain);
        } catch (JSONException e) {
            e.printStackTrace();
            consumer.doAction(ELEVATION_GAIN_UNAVAILABLE);
        }
    }

    /**
     * Handles an erroneous request
     * @param error the error to handle
     */
    private static void handleErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    /**
     * Generates the URL for the google elevation request api
     * @param recordingService the service which recorded the locations
     */
    private static void url(RecordingService recordingService) {
        requestURLs = new ArrayList<>();
        StringBuilder locationsString = new StringBuilder(REQUEST_URL_START);

        String key = "&key=" + recordingService.getResources().getString(R.string.mapsApiKey);
        int appendedLocations = 0;
        Location previousLocation = null;
        List<Location> locations = recordingService.getLocations();

        if (locations.size() > 1) {
            for (Location location : locations) {
                boolean appendLocation = false;
                if (previousLocation == null) {
                    appendLocation = true;
                } else {
                    double previousAltitude = previousLocation.getAltitude();
                    double currentAltitude = location.getAltitude();

                    if (Math.abs(currentAltitude - previousAltitude) >= ELEVATION_GAIN_THRESHOLD) {
                        appendLocation = true;
                    }
                }

                if (appendLocation) {
                    locationsString.append(location.getLatitude()).append(",").append(location.getLongitude()).append("|");
                    if (++appendedLocations >= MAX_REQUEST_LOCATIONS) {
                        appendedLocations = 0;
                        locationsString.deleteCharAt(locationsString.length() - 1);
                        locationsString.append(key);
                        requestURLs.add(locationsString.toString());
                        locationsString = new StringBuilder(REQUEST_URL_START);
                    }
                }
                previousLocation = location;
            }

            locationsString.deleteCharAt(locationsString.length() - 1);
            locationsString.append(key);
            requestURLs.add(locationsString.toString());
        }
    }

    /**
     * Build the response object if url requests size is greater than 0
     * @param jsonObject the json object to add on
     * @param consumer the consumer to consume the elevation gain
     */
    private static void buildResponseObject(JSONObject jsonObject, ActionHandlerConsumer<Double> consumer, RecordingService recordingService) {
        try {
            boolean initialResponse = false;
            if (builtObject == null) {
                builtObject = new JSONObject();
                initialResponse = true;
            }

            JSONArray array = jsonObject.getJSONArray("results");
            if (initialResponse) {
                builtObject.put("results", array);
            } else {
                JSONArray results = builtObject.getJSONArray("results");
                for (int i = 0; i < array.length(); i++) {
                    results.put(array.get(i));
                }
            }

            if (requestIndex == requestURLs.size()) {
                handleSuccessfulResponse(builtObject, consumer);
                builtObject = null;
            } else {
                RequestQueue requestQueue = Volley.newRequestQueue(recordingService);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURLs.get(requestIndex++),
                        null, response1 -> buildResponseObject(response1, consumer, recordingService), RecordingUtils::handleErrorResponse);
                requestQueue.add(request);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            consumer.doAction(ELEVATION_GAIN_UNAVAILABLE);

        }
    }

    /**
     * Calculate elevation gain using google maps elevation api
     * @param recordingService the service that recorded the locations
     * @param response the consumer of the elevation gain variable
     */
    public static void calculateElevationGain(RecordingService recordingService, ActionHandlerConsumer<Double> response) {
        url(recordingService);

        RequestQueue requestQueue = Volley.newRequestQueue(recordingService);
        int size = requestURLs.size();
        if (size == 1) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURLs.get(0), null,
                    response1 -> handleSuccessfulResponse(response1, response), RecordingUtils::handleErrorResponse);
            requestQueue.add(request);
        } else if (size > 1) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURLs.get(requestIndex++), null,
                    response1 -> buildResponseObject(response1, response, recordingService), RecordingUtils::handleErrorResponse);
            requestQueue.add(request);
        } else {
            response.doAction(ELEVATION_GAIN_UNAVAILABLE);
        }
    }
}
