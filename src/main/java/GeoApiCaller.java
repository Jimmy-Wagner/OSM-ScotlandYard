import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import retrofit2.Callback;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import retrofit2.Call;;
import retrofit2.Response;

import java.util.List;

/**
 * This class serves for communication with mapbox geocoder api
 */
public class GeoApiCaller {

    // Personal Mapbox access token for api requests
    private final static String ACCESS_TOKEN = "pk.eyJ1IjoiamltbXl3YWduZXIiLCJhIjoiY2t2emZ3M3hnNHppdDJudGtubnZybHdtYSJ9.JXJW_2riuBye8QTdCy1NGw";

    // Point of the address
    private Point cameraPoint;

    /**
     * Calls mapbox geocoder api and saves the response in camerapoint variable
     * @param address which will be converted to an point in latitude and longitude
     */
    public void callApi(String address){

        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(ACCESS_TOKEN)
                .query(address)
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {

                    // Log the first results Point.
                    cameraPoint = results.get(0).center();
                    System.out.println("Your address is valid!");
                    System.out.println("Latitude: " + cameraPoint.latitude());
                    System.out.println("longitude: " + cameraPoint.longitude());

                } else {

                    // No result for your request were found.
                    System.out.println("Your address is not valid");
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Returns cameraPoint
     * @return cameraPoint
     */
    public Point getCameraPoint() {
        return cameraPoint;
    }
}
