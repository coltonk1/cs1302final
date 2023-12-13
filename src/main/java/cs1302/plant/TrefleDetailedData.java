package cs1302.plant;

import cs1302.plant.TrefleDistribution;
import com.google.gson.annotations.SerializedName;

/**
 * Trefle detailed data.
 */
public class TrefleDetailedData {
    @SerializedName("common_name")
    String commonName;
    @SerializedName("scientific_name")
    String scientificName;
    String year;
    String bibliography;
    String author;
    Boolean vegetable;
    String genus;
    String family;
    Boolean edible;
    @SerializedName("edible_part")
    String[] ediblePart;
    String[] duration;
    TrefleDistribution distributions;
}
