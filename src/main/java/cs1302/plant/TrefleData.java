package cs1302.plant;

import com.google.gson.annotations.SerializedName;

/**
 * Trefle basic data.
 */
public class TrefleData {
    String id;
    @SerializedName("common_name")
    String commonName;
    @SerializedName("scientific_name")
    String scientificName;
    String family;
    @SerializedName("image_url")
    String imageURL;
}
