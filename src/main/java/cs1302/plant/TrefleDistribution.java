package cs1302.plant;

import cs1302.plant.TrefleDistributionData;
import com.google.gson.annotations.SerializedName;

/**
 * Trefle initial distribution.
 */
public class TrefleDistribution {
    @SerializedName("native")
    TrefleDistributionData[] nativeData;
    TrefleDistributionData[] introduced;
}
